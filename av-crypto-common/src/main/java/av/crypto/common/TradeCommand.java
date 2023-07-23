package av.crypto.common;

import org.json.JSONObject;

import java.util.UUID;

import static av.crypto.common.Enums.*;

public class TradeCommand {
    public static final String PING = "ping";
    public static final String ACCOUNT = "account";
    public static final String LIMIT_ORDER = "limit_order";
    public static final String MARKET_ORDER = "market_order";
    public static final String CANCEL_ORDER = "cancel_order";
    public static final String CANCEL_ALL_ORDERS = "cancel_all_orders";

    public static JSONObject ping() {
        JSONObject params = new JSONObject();
        params.put("requestId", UUID.randomUUID().toString());
        params.put("method", PING);
        return params;
    }

    public static JSONObject account() {
        JSONObject params = new JSONObject();
        params.put("requestId", UUID.randomUUID().toString());
        params.put("method", ACCOUNT);
        return params;
    }

    public static JSONObject cancelOrder(String symbol, long orderId) {
        JSONObject params = new JSONObject();
        params.put("requestId", UUID.randomUUID().toString());
        params.put("method", CANCEL_ORDER);

        params.put("symbol", symbol);
        params.put("orderId", orderId);
        return params;
    }

    public static JSONObject cancelAllOrders(String symbol) {
        JSONObject params = new JSONObject();
        params.put("requestId", UUID.randomUUID().toString());
        params.put("method", CANCEL_ALL_ORDERS);
        params.put("symbol", symbol);
        return params;
    }

    public static JSONObject marketOrder(String symbol, OrderSide orderSide, double quantity) {
        if (Utils.isZero(quantity) || quantity < 0) {
            throw new IllegalArgumentException("quantity <= 0");
        }

        JSONObject params = new JSONObject();
        params.put("requestId", UUID.randomUUID().toString());
        params.put("method", MARKET_ORDER);

        params.put("symbol", symbol);
        params.put("side", orderSide.toString());
        params.put("quantity", Utils.num4(quantity));
        return params;
    }

    public static JSONObject limitOrder(String symbol, OrderSide orderSide, double quantity, double price) {
        if (Utils.isZero(quantity) || quantity < 0) {
            throw new IllegalArgumentException("quantity <= 0");
        }
        if (Utils.isZero(price) || price < 0) {
            throw new IllegalArgumentException("price <= 0");
        }

        JSONObject params = new JSONObject();
        params.put("requestId", UUID.randomUUID().toString());
        params.put("method", LIMIT_ORDER);

        params.put("symbol", symbol);
        params.put("side", orderSide.toString());
        params.put("price", Utils.num2(price));
        params.put("quantity", Utils.num4(quantity));
        return params;
    }
}
