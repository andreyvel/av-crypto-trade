package av.bitcoin.binance;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import av.bitcoin.common.TradeCommand;
import av.bitcoin.common.Utils;
import av.bitcoin.common.dto.OrderDto;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import static av.bitcoin.common.Enums.*;

public class TradeCommandListener {
    private static final Logger log = LoggerFactory.getLogger(TradeCommandListener.class);
    private WsApiBinance wsApiBinance;
    private Thread threadTradeStreamSub = null;
    private ZContext zmqContext = null;
    public TradeCommandListener(WsApiBinance wsApiBinance) {
        this.wsApiBinance = wsApiBinance;
    }

    public void start() {
        zmqContext = new ZContext();
        threadTradeStreamSub = new Thread(() -> tradeStreamListener());
        threadTradeStreamSub.start();
    }

    public void stop() {
        threadTradeStreamSub.interrupt();
        zmqContext.close();
    }

    private void tradeStreamListener() {
        try (ZMQ.Socket subscriber = zmqContext.createSocket(SocketType.SUB)) {
            log.warn("Starting ZMQ commands listener: {}", AppConfig.zmqTradeStreamSub());
            subscriber.subscribe(ZMQ.SUBSCRIPTION_ALL);
            subscriber.connect(AppConfig.zmqTradeStreamSub());

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String jsonParams = subscriber.recvStr();
                    JSONObject params = new JSONObject(jsonParams);

                    String method = Utils.pullValue(params, "method", true);
                    if (method == null) {
                        log.error("Parameter [method] is null: {}", jsonParams);
                        return;
                    }

                    String requestId = params.optString("requestId");
                    if (requestId == null) {
                        log.error("Parameter [requestId] is null: {}", jsonParams);
                        return;
                    }

                    if (TradeCommand.PING.equals(method)) {
                        wsApiBinance.dataStreamPing(requestId);
                    } else if (TradeCommand.ACCOUNT.equals(method)) {
                        wsApiBinance.accountStatus(requestId);
                    } else if (TradeCommand.CANCEL_ORDER.equals(method)) {
                        String symbol = params.getString("symbol");
                        long orderId = params.getLong("orderId");
                        wsApiBinance.cancelOrder(requestId, symbol, orderId);
                    } else if (TradeCommand.CANCEL_ALL_ORDERS.equals(method)) {
                        String symbol = params.getString("symbol");
                        for (OrderDto order : TradeSession.ordersAllDto.values()) {
                            if (OrderStatus.equals(order.status(), OrderStatus.NEW)) {
                                wsApiBinance.cancelOrder(requestId, symbol, order.orderId());
                            }
                        }
                    } else if (TradeCommand.MARKET_ORDER.equals(method)) {
                        String symbol = params.getString("symbol");
                        String side = params.getString("side");
                        OrderSide orderSide = "BUY".equals(side) ? OrderSide.BUY : OrderSide.SELL;

                        double quantity = params.getDouble("quantity");
                        wsApiBinance.marketOrder(requestId, symbol, orderSide, quantity);
                    } else if (TradeCommand.LIMIT_ORDER.equals(method)) {
                        String symbol = params.getString("symbol");
                        String side = params.getString("side");
                        OrderSide orderSide = "BUY".equals(side) ? OrderSide.BUY : OrderSide.SELL;

                        double quantity = params.getDouble("quantity");
                        double price = params.getDouble("price");
                        wsApiBinance.limitOrder(requestId, symbol, orderSide, quantity, price);
                    } else {
                        log.warn("Method is not found: {}", jsonParams);
                    }
                }
                catch(Exception ex) {
                    if (!zmqContext.isClosed()) {
                        log.error(null, ex);
                    }
                }
            }
        }
    }
}
