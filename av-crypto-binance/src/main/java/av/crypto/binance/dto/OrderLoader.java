package av.crypto.binance.dto;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import av.crypto.common.AvgValue;
import av.crypto.common.dto.OrderDto;

import java.util.ArrayList;
import java.util.List;

public class OrderLoader {
    private static final Logger log = LoggerFactory.getLogger(OrderLoader.class);
    public static OrderDto loadOrderPlace(String content) {
        JSONObject jsonEvent = new JSONObject(content);
        return loadOrderPlace(jsonEvent);
    }

    public static OrderDto loadOrderPlace(JSONObject jsonObj) {
        jsonObj = jsonObj.getJSONObject("result");
        if (jsonObj == null) {
            return null;
        }

        long orderId = jsonObj.optLong("orderId");
        String symbol = jsonObj.optString("symbol");
        String side = jsonObj.optString("side"); // BUY or SELL
        String type = jsonObj.optString("type"); // LIMIT or MARKET
        double price = jsonObj.getDouble("price");
        double qnt = jsonObj.getDouble("origQty");
        OrderDto order = new OrderDto(orderId, symbol, side, type, price, qnt);

        String status = jsonObj.optString("status"); // FILLED
        order.status(status);

        String clientOrderId = jsonObj.optString("clientOrderId");
        order.clientOrderId(clientOrderId);

        double execQty = jsonObj.getDouble("executedQty");
        order.executedQty(execQty);

        double commission = jsonObj.optDouble("commission");
        order.commission(commission);

        long created = jsonObj.optLong("transactTime");
        order.created(created);
        order.updated(created);

        return order;
    }

    public static OrderDto loadExecutionReport(String content) {
        JSONObject jsonEvent = new JSONObject(content);
        return loadExecutionReport(jsonEvent);
    }

    public static OrderDto loadCancelOrder(JSONObject jsonObj) {
        // "result":{"symbol":"BTCUSDT","origClientOrderId":"LHaFmRzztgnhqGO2TFbQlN","orderId":6101073,
        // "orderListId":-1,"clientOrderId":"yZAkV9WXK8dUTYVKinvpv6","price":"28600.00000000","origQty":"0.00200000",
        // "executedQty":"0.00000000","cummulativeQuoteQty":"0.00000000","status":"CANCELED","timeInForce":"GTC",
        // "type":"LIMIT","side":"BUY","selfTradePreventionMode":"NONE"}

        JSONObject resObj = jsonObj.getJSONObject("result");
        long orderId = resObj.getLong("orderId");
        String symbol = resObj.getString("symbol");
        String side = resObj.getString("side");
        String type = resObj.getString("type");
        String status = resObj.getString("status");

        double price = resObj.getDouble("price");
        double origQty = resObj.getDouble("origQty");
        double executedQty = resObj.getDouble("executedQty");

        OrderDto order = new OrderDto(orderId, symbol, side, type, price, origQty);
        order.executedQty(executedQty);
        order.status(status);
        return order;
    }

    public static OrderDto loadNewOrder(JSONObject jsonObj) {
        //"result":{"symbol":"BTCUSDT","orderId":853538,"orderListId":-1,"clientOrderId":"bdYlKQmdVg8n4f4U6RFyhd",
        //"transactTime":1686317918146, "price":"0.00000000","origQty":"0.00100000","executedQty":"0.00100000",
        //"cummulativeQuoteQty":"26.67330000", "status":"FILLED","type":"MARKET","side":"BUY","workingTime":1686317918146

        JSONObject resObj = jsonObj.getJSONObject("result");
        long orderId = resObj.getLong("orderId");
        String symbol = resObj.getString("symbol");
        String side = resObj.getString("side");
        String type = resObj.getString("type");
        String status = resObj.getString("status");

        double price = resObj.getDouble("price");
        double origQty = resObj.getDouble("origQty");
        double executedQty = resObj.getDouble("executedQty");

        OrderDto order = new OrderDto(orderId, symbol, side, type, price, origQty);
        order.executedQty(executedQty);
        order.status(status);

        long created = resObj.optLong("transactTime");
        if (created > 0) {
            order.created(created);
        }

        // "fills":[{"price":"26673.30000000","qty":"0.00100000",
        // "commission":"0.00000000","commissionAsset":"BTC","tradeId":249426}]
        JSONArray fillsArr = resObj.optJSONArray("fills");
        if (fillsArr != null) {
            AvgValue priceAvg = new AvgValue();
            double commission = 0;

            for (int ind = 0; ind < fillsArr.length(); ind++) {
                JSONObject obj = fillsArr.getJSONObject(ind);
                priceAvg.update(obj.getDouble("price"));
                commission += obj.getDouble("commission");
            }
            order.price(priceAvg.avg());
            order.commission(commission);
        }
        return order;
    }

    public static OrderDto loadExecutionReport(JSONObject jsonObj) {
        long orderId = jsonObj.optLong("i");
        String symbol = jsonObj.optString("s");
        String side = jsonObj.optString("S"); // BUY or SELL
        String type = jsonObj.optString("o"); // LIMIT or MARKET
        double price = jsonObj.getDouble("p");
        double qnt = jsonObj.getDouble("q");
        OrderDto order = new OrderDto(orderId, symbol, side, type, price, qnt);

        long created = jsonObj.optLong("O"); // Order creation time
        order.created(created);

        long updated = jsonObj.optLong("E"); // Event time
        order.updated(updated);

        String status = jsonObj.optString("X"); // FILLED
        order.status(status);

        String clientOrderId = jsonObj.optString("c");
        order.clientOrderId(clientOrderId);

        double commission = jsonObj.getDouble("n");
        order.commission(commission);
        return order;
    }

    public static List<OrderDto> loadOrdersAll(String content) {
        JSONObject jsonEvent = new JSONObject(content);
        return loadOrdersAll(jsonEvent);
    }
    public static List<OrderDto> loadMyTrades(JSONObject rootObj) {
        List<OrderDto> listRet = new ArrayList<>();
        int statusOk = rootObj.optInt("status");
        if (statusOk != 200) {
            log.error("loadOrdersAll: status != 200, {}", rootObj);
            return listRet;
        }

        JSONArray resultArr = rootObj.optJSONArray("result");
        for(int ind = 0; ind < resultArr.length(); ind++) {
            JSONObject rowObj = resultArr.optJSONObject(ind);
            //{"symbol":"BTCUSDT","orderListId":-1,"quoteQty":"267.80010000","orderId":46137,"isBestMatch":true,
            //"isMaker":false,"commissionAsset":"BTC","isBuyer":true,"price":"26780.01000000","qty":"0.01000000",
            // "commission":"0.00000000","id":16261,"time":1686142766303},

            long orderId = rowObj.getLong("orderId");
            String symbol = rowObj.getString("symbol");

            double price = rowObj.getDouble("price");
            double qnt = rowObj.getDouble("qty");
            OrderDto order = new OrderDto(orderId, symbol, null, null, price, qnt);

            long created = rowObj.getLong("time");
            order.created(created);
            order.status("FILLED");

            double commission = rowObj.getDouble("commission");
            order.commission(commission);
            listRet.add(order);
        }

        return listRet;
    }
    public static List<OrderDto> loadOrdersAll(JSONObject rootObj) {
        List<OrderDto> listRet = new ArrayList<>();
        int statusOk = rootObj.optInt("status");
        if (statusOk != 200) {
            log.error("loadOrdersAll: status != 200, {}", rootObj);
            return listRet;
        }

        JSONArray resultArr = rootObj.optJSONArray("result");
        for(int ind = 0; ind < resultArr.length(); ind++) {
            JSONObject rowObj = resultArr.optJSONObject(ind);

            long orderId = rowObj.optLong("orderId");
            String symbol = rowObj.optString("symbol");
            String side = rowObj.optString("side"); // BUY or SELL
            String type = rowObj.optString("type"); // LIMIT or MARKET
            double price = rowObj.getDouble("price");
            double qnt = rowObj.getDouble("origQty");
            OrderDto order = new OrderDto(orderId, symbol, side, type, price, qnt);

            String status = rowObj.optString("status"); // FILLED
            order.status(status);

            String clientOrderId = rowObj.optString("clientOrderId");
            order.clientOrderId(clientOrderId);

            double execQty = rowObj.getDouble("executedQty");
            order.executedQty(execQty);

            long created = rowObj.optLong("time");
            order.created(created);

            long updated = rowObj.optLong("updateTime");
            order.updated(updated);
            listRet.add(order);
        }

        return listRet;
    }
}
