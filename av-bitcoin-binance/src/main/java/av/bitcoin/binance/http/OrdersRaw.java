package av.bitcoin.binance.http;

import com.sun.net.httpserver.HttpExchange;
import av.bitcoin.binance.TradeSession;
import org.json.JSONArray;
import org.json.JSONObject;
import av.bitcoin.common.httpserver.HttpHandlerEx;

import java.util.Collection;

public class OrdersRaw extends HttpHandlerEx {
    @Override
    public String getResponseBody(HttpExchange req) {
        this.mediaType = MediaType.APPLICATION_JSON;

        JSONObject jsonRoot = new JSONObject();
        append(jsonRoot, "ordersNew", TradeSession.ordersNew);
        jsonRoot.put("allOrders", TradeSession.allOrders);
        jsonRoot.put("myTrades", TradeSession.myTrades);
        jsonRoot.put("ocoOrders", TradeSession.ocoOrders);
        jsonRoot.put("openedOrders", TradeSession.openOrders);

        return jsonRoot.toString();
    }

    public void append(JSONObject jsonRoot, String nodeName, Collection<String> list) {
        JSONArray arr1 = new JSONArray();
        for (String order : list) {
            JSONObject order2 = new JSONObject(order);
            arr1.put(order2);
        }
        jsonRoot.put(nodeName, arr1);
    }
}
