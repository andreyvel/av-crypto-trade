package av.bitcoin.binance.http;

import com.sun.net.httpserver.HttpExchange;
import av.bitcoin.binance.AppMain;
import org.json.JSONObject;
import org.trade.common.httpserver.HttpHandlerEx;

public class PingHandler extends HttpHandlerEx {
    @Override
    public String getResponseBody(HttpExchange req) {
        this.mediaType = MediaType.APPLICATION_JSON;
        JSONObject jsonMsg = AppMain.wsApiBinance().dataStreamPing(null);
        return jsonMsg.toString();
    }
}
