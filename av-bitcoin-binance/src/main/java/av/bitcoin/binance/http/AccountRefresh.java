package av.bitcoin.binance.http;

import com.sun.net.httpserver.HttpExchange;
import av.bitcoin.binance.AppMain;
import org.json.JSONObject;
import org.trade.common.httpserver.HttpHandlerEx;

public class AccountRefresh extends HttpHandlerEx {
    @Override
    public String getResponseBody(HttpExchange req) {
        this.mediaType = HttpHandlerEx.MediaType.APPLICATION_JSON;
        JSONObject jsonMsg = AppMain.wsApiBinance().dataStreamPing(null);
        return jsonMsg.toString();
    }
}
