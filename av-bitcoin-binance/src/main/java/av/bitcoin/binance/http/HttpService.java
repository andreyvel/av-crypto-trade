package av.bitcoin.binance.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;

import av.bitcoin.binance.AppConfig;
import av.bitcoin.binance.AppMain;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.common.dto.ErrorDto;
import org.trade.common.httpserver.HttpHandlerEx;

import static org.trade.common.Enums.*;

public class HttpService {
    private static final Logger log = LoggerFactory.getLogger(HttpService.class.getName());

    public void start() throws IOException {
        log.warn("Starting HttpService: http://localhost:{}", AppConfig.tradeRestApiPort());
        HttpServer server = HttpServer.create(new InetSocketAddress(AppConfig.tradeRestApiPort()), 0);
        server.createContext("/ticks", new TicksHandler());
        server.createContext("/quotes", new QuotesHandler());

        server.createContext("/ping", new PingHandler());
        server.createContext("/account", new AccountHandler());
        server.createContext("/accountRefresh", new AccountRefresh());

        server.createContext("/ordersAll", new OrdersAll());
        server.createContext("/ordersRaw", new OrdersRaw());
        server.createContext("/cancelOrder", new CancelOrderHandler());

        server.createContext("/myTradesRefresh", new MyTradesRefresh());
        server.createContext("/ordersAllRefresh", new OrdersAllRefresh());
        server.createContext("/ordersOcoRefresh", new OrdersOcoRefresh());
        server.createContext("/ordersOpenRefresh", new OrdersOpenRefresh());

        server.createContext("/", new DefaultHandler());
        server.start();
    }

    private static class DefaultHandler extends HttpHandlerEx {
        @Override
        public String getResponseBody(HttpExchange req) {
            StringBuilder sb = new StringBuilder();
            sb.append("<html><body>\n");

            String url = "ticks?symbol=BTCUSDT&fromSec=5";
            sb.append("<a href='" + url + "'>" + url + "</a><br/>\n");

            url = "quotes?symbol=BTCUSDT&interval=1m";
            sb.append("<a href='" + url + "'>" + url + "</a><br/>\n");
            sb.append("<br/>\n");

            sb.append("<a href='ping'>ping</a><br/>\n");
            sb.append("<a href='account'>account</a><br/>\n");
            sb.append("<a href='accountRefresh'>accountRefresh</a><br/>\n");
            sb.append("<br/>\n");

            sb.append("<a href='ordersAll'>ordersAll</a><br/>\n");
            sb.append("<a href='ordersRaw'>ordersRaw</a><br/>\n");

            url = "cancelOrder?symbol=BTCUSDT&orderId=0";
            sb.append("<a href='" + url + "'>" + url + "</a><br/>\n");
            sb.append("<br/>\n");

            sb.append("<a href='myTradesRefresh'>myTradesRefresh</a><br/>\n");
            sb.append("<a href='ordersAllRefresh'>ordersAllRefresh</a><br/>\n");
            sb.append("<a href='ordersOcoRefresh'>ordersOcoRefresh</a><br/>\n");
            sb.append("<a href='ordersOpenRefresh'>ordersOpenRefresh</a><br/>\n");
            sb.append("<br/>\n");

            sb.append("</body></html>");
            return sb.toString();
        }
    }

    private static class MyTradesRefresh extends HttpHandlerEx {
        @Override
        public String getResponseBody(HttpExchange req) {
            this.mediaType = HttpHandlerEx.MediaType.APPLICATION_JSON;

            StringBuilder sb = new StringBuilder();
            for (String symbol : AppConfig.subscribeSymbols()) {
                JSONObject jsonMsg = AppMain.wsApiBinance().myTrades24h(null, symbol);

                if (sb.length() > 0) sb.append("\n");
                sb.append(jsonMsg.toString());
            }
            return sb.toString();
        }
    }

    private static class OrdersOcoRefresh extends HttpHandlerEx {
        @Override
        public String getResponseBody(HttpExchange req) {
            this.mediaType = HttpHandlerEx.MediaType.APPLICATION_JSON;
            JSONObject jsonMsg = AppMain.wsApiBinance().ocoOrders(null);
            return jsonMsg.toString();
        }
    }

    private static class OrdersOpenRefresh extends HttpHandlerEx {
        @Override
        public String getResponseBody(HttpExchange req) {
            this.mediaType = HttpHandlerEx.MediaType.APPLICATION_JSON;
            JSONObject jsonMsg = AppMain.wsApiBinance().allOpenOrders(null, null);
            return jsonMsg.toString();
        }
    }

    private static class OrdersAllRefresh extends HttpHandlerEx {
        @Override
        public String getResponseBody(HttpExchange req) {
            this.mediaType = HttpHandlerEx.MediaType.APPLICATION_JSON;
            StringBuilder sb = new StringBuilder();
            for(String symbol : AppConfig.subscribeSymbols()) {
                JSONObject jsonMsg = AppMain.wsApiBinance().allOrders24h(null, symbol);

                if (sb.length() > 0) sb.append("\n");
                sb.append(jsonMsg.toString());
            }
            return sb.toString();
        }
    }

    public class CancelOrderHandler extends HttpHandlerEx {
        @Override
        public String getResponseBody(HttpExchange req) {
            this.mediaType = HttpHandlerEx.MediaType.APPLICATION_JSON;
            HashMap<String, String> params = getParams(req);

            String symbol = params.get("symbol");
            if (symbol == null) {
                return ErrorDto.serialize("symbol==null");
            }

            String orderId2 = params.get("orderId");
            if (orderId2 == null) {
                return ErrorDto.serialize("orderId==null");
            }

            long orderId = Long.parseLong(orderId2);
            JSONObject jsonMsg = AppMain.wsApiBinance().cancelOrder(null, symbol, orderId);
            return jsonMsg.toString();
        }
    }

//public class OrdersOpen extends HttpHandlerEx {
//    @Override
//    public String getResponseBody(HttpExchange req) {
//        this.mediaType = MediaType.APPLICATION_JSON;
//
//        JSONObject jsonRoot = new JSONObject();
//        JSONArray arr1 = new JSONArray();
//        for (OrderDto order : TradeSession.openOrdersDto.values()) {
//            JSONObject order2 = order.serialize();
//            arr1.put(order2);
//        }
//
//        jsonRoot.put("orders", arr1);
//        return jsonRoot.toString();
//    }
//}
}
