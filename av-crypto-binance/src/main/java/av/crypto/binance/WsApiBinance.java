package av.crypto.binance;

import com.binance.connector.client.impl.WebSocketApiClientImpl;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import av.crypto.common.Utils;
import av.crypto.common.dto.OrderDto;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;

import static av.crypto.common.Enums.*;

/*
Available timeInForce options, setting how long the order should be active before expiration:
GTC	Good 'til Canceled – the order will remain on the book until you cancel it, or the order is completely filled.
IOC	Immediate or Cancel – the order will be filled for as much as possible, the unfilled quantity immediately expires.
FOK	Fill or Kill – the order will expire unless it cannot be immediately filled for the entire quantity.
*/
public class WsApiBinance {
    private static final Logger log = LoggerFactory.getLogger(WsApiBinance.class);
    private static final String KLINES = "klines";
    private static final String NEW_ORDER = "order.place";
    private static final String CANCEL_ORDER = "order.cancel";

    private static final String ACCOUNT_STATUS = "account.status";
    private static final String GET_OPEN_ORDERS = "openOrders.status";
    private static final String ACCOUNT_ALL_ORDERS = "allOrders"; // Account order history
    private static final String ACCOUNT_ALL_OCO_ORDERS = "allOrderLists"; // Account OCO history
    private static final String ACCOUNT_TRADE_HISTORY = "myTrades"; // Account trade history
    private static final String USER_DATA_STREAM_PING = "userDataStream.ping";
    private static final String USER_DATA_STREAM_START = "userDataStream.start";

    private HashMap<String, ApiCommand> requestMap = new LinkedHashMap<>(); // TODO delete old elements
    private WebSocketApiClientImpl wsApiClient;
    private TradeStreamPublisher tradeStreamPublisher;

    public WsApiBinance(WebSocketApiClientImpl wsApiClient, TradeStreamPublisher tradeStreamPublisher) {
        this.wsApiClient = wsApiClient;
        this.tradeStreamPublisher = tradeStreamPublisher;
    }

    public void start() {
        wsApiClient.connect(((event) -> {
            try {
                log.warn("ws_api_response: {}", event);
                JSONObject jsonEvent = new JSONObject(event);

                String requestId = jsonEvent.optString("id");
                if (requestId == null) {
                    log.warn("RequestId is not found in source message");
                    return;
                }

                ApiCommand cmd = requestMap.remove(requestId);
                if (cmd == null) {
                    log.warn("RequestId={} is not found in requestMap", requestId);
                    return;
                }

                int status = jsonEvent.optInt("status");
                if (status != 200) {
                    log.warn("Bad status={}, {}", status, cmd);
                    tradeStreamPublisher.send("bad_status", cmd.method, jsonEvent);
                    return;
                }

                long delayMs = Utils.delayMs(cmd.created, LocalDateTime.now());
                if (delayMs > AppConfig.slowlyResponceAlertMs()) {
                    log.warn("Slowly response for requestId={} delayMs={}", requestId, delayMs);
                }

                if (ACCOUNT_STATUS.equals(cmd.method)) {
                    TradeSession.updateAccountStatus(event, jsonEvent);
                    JSONObject jsonObj = TradeSession.accountStatus.serialize();
                    tradeStreamPublisher.send("account_changed", cmd.method, jsonObj);
                }
                else if (NEW_ORDER.equals(cmd.method)) {
                    OrderDto orderDto = TradeSession.updateNewOrder(event, jsonEvent, cmd.method);
                    JSONObject jsonDto = orderDto.serialize();
                    tradeStreamPublisher.send("new_order", cmd.method, jsonDto);
                }
                else if (CANCEL_ORDER.equals(cmd.method)) {
                    OrderDto orderDto = TradeSession.updateCancelOrder(event, jsonEvent, cmd.method);
                    JSONObject jsonDto = orderDto.serialize();
                    tradeStreamPublisher.send("cancel_order", cmd.method, jsonDto);
                }
                else if (GET_OPEN_ORDERS.equals(cmd.method)) {
                    TradeSession.updateOpenOrders(event, jsonEvent, cmd.method);
                }
                else if (ACCOUNT_ALL_ORDERS.equals(cmd.method)) {
                    TradeSession.updateAllOrders(event, jsonEvent, cmd.method);
                }
                else if (ACCOUNT_ALL_OCO_ORDERS.equals(cmd.method)) {
                    TradeSession.updateOcoOrders(event, jsonEvent);
                }
                else if (ACCOUNT_TRADE_HISTORY.equals(cmd.method)) {
                    TradeSession.updateMyTrades(event, jsonEvent);
                }
                else if (USER_DATA_STREAM_PING.equals(cmd.method)) {
                    TradeSession.updatePing(event, jsonEvent, cmd.created);
                    JSONObject jsonObj = TradeSession.pingDto().serialize();
                    tradeStreamPublisher.send("ping", cmd.method, jsonObj);
                }
                else if (USER_DATA_STREAM_START.equals(cmd.method)) {
                    jsonEvent = jsonEvent.optJSONObject("result");
                    String userDataListenKey = jsonEvent.optString("listenKey");

                    if (userDataListenKey != null) {
                        TradeSession.updateUserDataListenKey(userDataListenKey);
                        AppMain.wsStreamBinance().subscribeUserStream(userDataListenKey);
                        dataStreamPing(null);
                    } else {
                        log.error("updateUserDataStreamStart: userDataListenKey==null");
                    }
                }
                else if (KLINES.equals(cmd.method)) {
                    String symbol = cmd.params.optString("symbol");
                    String interval = cmd.params.optString("interval");
                    TradeSession.updateKlines(symbol, interval, event, jsonEvent);
                }
                else {
                    log.warn("Unknown wsApiClient.event={}", event);
                }
            }
            catch(Exception e) {
                log.error(event, e);
            }
        }));
    }

    private ApiCommand createParams(String requestId, String method) {
        if (requestId == null) {
            requestId = UUID.randomUUID().toString();
        }

        JSONObject params = new JSONObject();
        params.put("requestId", requestId);

        ApiCommand cmd = new ApiCommand(method, requestId, params);
        requestMap.put(cmd.requestId, cmd);
        return cmd;
    }

    public JSONObject userDataStreamStart(String requestId) {
        ApiCommand cmd = createParams(requestId, USER_DATA_STREAM_START);
        wsApiClient.userDataStream().userDataStreamStart(cmd.params);

        log.warn(cmd.toString());
        return cmd.params;
    }

    public JSONObject accountStatus(String requestId) {
        ApiCommand cmd = createParams(requestId, ACCOUNT_STATUS);
        wsApiClient.account().accountStatus(cmd.params);

        log.warn(cmd.toString());
        return cmd.params;
    }

    public JSONObject dataStreamPing(String requestId) {
        String listenKey = TradeSession.userDataListenKey();
        if (listenKey == null) {
            throw new RuntimeException("listenKey == null");
        }

        ApiCommand cmd = createParams(requestId, USER_DATA_STREAM_PING);
        wsApiClient.userDataStream().userDataStreamPing(listenKey, cmd.params);
        cmd.params.put("listenKey", listenKey);

        log.warn(cmd.toString());
        return cmd.params;
    }

    public JSONObject ocoOrders(String requestId) {
        ApiCommand cmd = createParams(requestId, ACCOUNT_ALL_OCO_ORDERS);
        cmd.params.put("limit", 100);

        wsApiClient.account().accountAllOcoOrders(cmd.params);
        log.warn(cmd.toString());
        return cmd.params;
    }

    public JSONObject allOpenOrders(String requestId, String symbol) {
        ApiCommand cmd = createParams(requestId, GET_OPEN_ORDERS);
        JSONObject params = cmd.params;
        if (symbol != null) {
            params.put("symbol", symbol); // If omitted, open orders for all symbols are returned
        }

        wsApiClient.trade().getOpenOrders(params);
        log.warn(cmd.toString());
        return cmd.params;
    }

    public JSONObject myTrades24h(String requestId, String symbol) {
        LocalDateTime dateToUtc = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime dateFromUtc = dateToUtc.minusDays(1);
        JSONObject resp = myTrades(requestId, symbol, 0, 0, dateFromUtc, dateToUtc);
        return resp;
    }

    public JSONObject myTrades(String requestId, String symbol, long fromOrderId,
                               int limit, LocalDateTime dateFromUtc, LocalDateTime dateToUtc) {
        ApiCommand cmd = createParams(requestId, ACCOUNT_TRADE_HISTORY);
        JSONObject params = cmd.params;
        params.put("symbol", symbol);

        if (fromOrderId > 0) {
            params.put("orderId", fromOrderId);
        }

        if (limit > 0) {
            params.put("limit", limit); // Default 500; max 1000.
        }

        if (dateFromUtc != null) {
            params.put("startTime", Utils.ofEpochMilli(dateFromUtc));
            params.put("endTime", Utils.ofEpochMilli(dateToUtc));
        }

        wsApiClient.account().accountTradeHistory(symbol, params);
        params.put("symbol", symbol);
        log.warn(cmd.toString());
        return cmd.params;
    }

    public JSONObject allOrders24h(String requestId, String symbol) {
        LocalDateTime dateToUtc = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime dateFromUtc = dateToUtc.minusDays(1);
        JSONObject resp = allOrders(requestId, symbol, 0, 0, dateFromUtc, dateToUtc);
        return resp;
    }

    public JSONObject allOrders(String requestId, String symbol, long fromOrderId,
                                int limit, LocalDateTime dateFromUtc, LocalDateTime dateToUtc) {
        ApiCommand cmd = createParams(requestId, ACCOUNT_ALL_ORDERS);
        JSONObject params = cmd.params;

        if (fromOrderId > 0) {
            params.put("orderId", fromOrderId);
        }

        if (limit > 0) {
            params.put("limit", limit); // Default 500; max 1000.
        }

        if (dateFromUtc != null) {
            params.put("startTime", Utils.ofEpochMilli(dateFromUtc));
             params.put("endTime", Utils.ofEpochMilli(dateToUtc));
        }

        wsApiClient.account().accountAllOrders(symbol, params);
        params.put("symbol", symbol);
        log.warn(cmd.toString());
        return cmd.params;
    }

    public JSONObject klines(String requestId, String symbol, String interval, int limit) {
        return klines(requestId, symbol, interval, limit, null, null);
    }

    public JSONObject klines(String requestId, String symbol, String interval, int limit,
                             LocalDateTime startTime, LocalDateTime endTime) {
        ApiCommand cmd = createParams(requestId, KLINES);
        JSONObject params = cmd.params;
        params.put("limit", limit); // Default 500; max 1000

        if (startTime != null) {
            params.put("startTime", Utils.ofEpochMilli(startTime));
        }
        if (endTime != null) {
            params.put("endTime", Utils.ofEpochMilli(endTime));
        }

        wsApiClient.market().klines(symbol, interval, params);
        params.put("interval", interval);
        params.put("symbol", symbol);

        log.warn(cmd.toString());
        return cmd.params;
    }

    public JSONObject cancelOrder(String requestId, String symbol, long orderId) {
        ApiCommand cmd = createParams(requestId, CANCEL_ORDER);
        JSONObject params = cmd.params;
        params.put("recvWindow", "3000");

        params.put("symbol", symbol);
        params.put("orderId", orderId);
        wsApiClient.trade().cancelOrder(symbol, params);

        params.put("symbol", symbol);
        log.warn(cmd.toString());
        return cmd.params;
    }

    public JSONObject marketOrder(String requestId, String symbol, OrderSide orderSide, double quantity) {
        if (Utils.isZero(quantity) || quantity < 0) {
            throw new IllegalArgumentException("quantity <= 0");
        }

        ApiCommand cmd = createParams(requestId, NEW_ORDER);
        JSONObject params = cmd.params;
        params.put("quantity", Utils.num4(Math.abs(quantity)));
        params.put("recvWindow", "1000");

        String type = "MARKET";
        String side = orderSide.toString();
        wsApiClient.trade().newOrder(symbol, side, "MARKET", params);

        params.put("symbol", symbol);
        params.put("side", side);
        params.put("type", type);

        log.warn(cmd.toString());
        return cmd.params;
    }

    public JSONObject limitOrder(String requestId, String symbol, OrderSide orderSide, double quantity, double price) {
        if (Utils.isZero(quantity) || quantity < 0) {
            throw new IllegalArgumentException("quantity <= 0");
        }
        if (Utils.isZero(price) || price < 0) {
            throw new IllegalArgumentException("price <= 0");
        }

        ApiCommand cmd = createParams(requestId, NEW_ORDER);
        JSONObject params = cmd.params;
        params.put("recvWindow", "1000");
        params.put("timeInForce", "GTC");
        params.put("price", Utils.num2(price));
        params.put("quantity", Utils.num4(Math.abs(quantity)));

        String type = "LIMIT";
        String side = orderSide.toString();
        wsApiClient.trade().newOrder(symbol, side, type, params);

        params.put("symbol", symbol);
        params.put("side", side);
        params.put("type", type);

        log.warn(cmd.toString());
        return cmd.params;
    }

    public void stop() {
        JSONObject params = new JSONObject();
        params.put("method", USER_DATA_STREAM_START);
        params.put("requestId", UUID.randomUUID().toString());

        String listenKey = TradeSession.userDataListenKey();
        if (listenKey != null) {
            log.warn("userDataStreamStop: listenKey={}", listenKey);
            wsApiClient.userDataStream().userDataStreamStop(listenKey, params);
        }
    }

    private static class ApiCommand {
        public final LocalDateTime created = LocalDateTime.now();
        public final String method;
        public final String requestId;
        public final JSONObject params;

        public ApiCommand(String method, String requestId, JSONObject params) {
            this.method = method;
            this.requestId = requestId;
            this.params = params;
        }

        @Override
        public String toString() {
            return "requestId=" + requestId + ", " + "method=" + method + ", " + params.toString();
        }
    }
}
