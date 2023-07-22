package av.bitcoin.emulator;

import av.bitcoin.common.TradeCommand;
import av.bitcoin.common.Utils;
import av.bitcoin.common.dto.OrderDto;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import static av.bitcoin.common.Enums.OrderSide;
import static av.bitcoin.common.Enums.OrderStatus;

public class TradeCommandListener {
    private static final Logger log = LoggerFactory.getLogger(TradeCommandListener.class);
    private Thread threadTradeStreamSub = null;
    private ZContext zmqContext = null;
    private TradeStreamPublisher tradeStreamPublisher;

    public TradeCommandListener(TradeStreamPublisher tradeStreamPublisher) {
        this.tradeStreamPublisher = tradeStreamPublisher;
    }

    public void start() {
        zmqContext = new ZContext();
        threadTradeStreamSub = new Thread(() -> tradeStreamListener());
        threadTradeStreamSub.start();
    }

    private void tradeStreamListener() {
        try (ZMQ.Socket subscriber = zmqContext.createSocket(SocketType.SUB)) {
            log.warn("Starting ZMQ commands listener: {}", AppConfig.zmqTradeCommandSub());
            subscriber.subscribe(ZMQ.SUBSCRIPTION_ALL);
            subscriber.connect(AppConfig.zmqTradeCommandSub());

            String method = null;
            String requestId = null;

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String jsonParams = subscriber.recvStr();
                    log.warn("Received command: {}", jsonParams);

                    JSONObject params = new JSONObject(jsonParams);
                    TradeSession tradeSession = AppRandomQuote.tradeSession;

                    method = Utils.pullValue(params, "method", true);
                    if (method == null) {
                        log.error("Parameter [method] is null: {}", jsonParams);
                        return;
                    }

                    requestId = params.optString("requestId");
                    if (requestId == null) {
                        log.error("Parameter [requestId] is null: {}", jsonParams);
                        return;
                    }

                    if (TradeCommand.PING.equals(method)) {
                        // ping broker
                    } else if (TradeCommand.ACCOUNT.equals(method)) {
                        JSONObject jsonAcc = AppRandomQuote.tradeSession.account.serialize();
                        tradeStreamPublisher.send("account_changed", "marketOrder", jsonAcc);
                    } else if (TradeCommand.CANCEL_ORDER.equals(method)) {
                        String symbol = params.getString("symbol");
                        long orderId = params.getLong("orderId");
                        tradeSession.cancelOrder(symbol, orderId);
                    } else if (TradeCommand.CANCEL_ALL_ORDERS.equals(method)) {
                        String symbol = params.getString("symbol");
                        for (OrderDto order : tradeSession.allOrders.values()) {
                            if (OrderStatus.equals(order.status(), OrderStatus.NEW)) {
                                tradeSession.cancelOrder(symbol, order.orderId());
                            }
                        }
                    } else if (TradeCommand.MARKET_ORDER.equals(method)) {
                        String symbol = params.getString("symbol");
                        String side = params.getString("side");
                        OrderSide orderSide = "BUY".equals(side) ? OrderSide.BUY : OrderSide.SELL;

                        double quantity = params.getDouble("quantity");
                        tradeSession.marketOrder(symbol, orderSide, quantity);
                    } else if (TradeCommand.LIMIT_ORDER.equals(method)) {
                        String symbol = params.getString("symbol");
                        String side = params.getString("side");
                        OrderSide orderSide = "BUY".equals(side) ? OrderSide.BUY : OrderSide.SELL;

                        double quantity = params.getDouble("quantity");
                        double price = params.getDouble("price");
                        tradeSession.limitOrder(symbol, orderSide, quantity, price);
                    } else {
                        log.warn("Method is not found: {}", jsonParams);
                    }
                }
                catch(Exception ex) {
                    if (!zmqContext.isClosed()) {
                        JSONObject jsonMsg = new JSONObject();
                        jsonMsg.put("requestId", requestId);
                        jsonMsg.put("method", method);
                        jsonMsg.put("message", ex.getMessage());

                        tradeStreamPublisher.send("error", "command_listener", jsonMsg);
                        log.error(null, ex);
                    }
                }
            }
        }
    }
}
