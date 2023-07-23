package av.crypto.binance.http;

import com.sun.net.httpserver.HttpExchange;
import av.crypto.binance.TradeSession;
import org.json.JSONArray;
import org.json.JSONObject;
import av.crypto.common.dto.OrderDto;
import av.crypto.common.httpserver.HttpHandlerEx;

public class OrdersAll extends HttpHandlerEx {
    @Override
    public String getResponseBody(HttpExchange req) {
        this.mediaType = MediaType.APPLICATION_JSON;

        JSONObject jsonRoot = new JSONObject();
        JSONArray arr1 = new JSONArray();
        for (OrderDto order : TradeSession.ordersAllDto.values()) {
            JSONObject order2 = order.serialize();
            arr1.put(order2);
        }

        jsonRoot.put("orders", arr1);
        return jsonRoot.toString();
    }
}
