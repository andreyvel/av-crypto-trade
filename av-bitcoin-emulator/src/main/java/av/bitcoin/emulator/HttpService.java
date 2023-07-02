package av.bitcoin.emulator;

import av.bitcoin.common.dto.OrderDto;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import av.bitcoin.common.QuoteBar;
import av.bitcoin.common.dto.AccountDto;
import av.bitcoin.common.dto.ErrorDto;
import av.bitcoin.common.dto.QuoteBarCont;
import av.bitcoin.common.dto.QuoteBarDto;
import av.bitcoin.common.dto.QuoteAggCont;
import av.bitcoin.common.httpserver.HttpHandlerEx;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;

public class HttpService {
    private static final Logger log = LoggerFactory.getLogger(HttpService.class.getName());

    public void start(int servicePort) throws IOException {
        log.warn("Starting HttpService: http://localhost:{}", servicePort);

        HttpServer server = HttpServer.create(new InetSocketAddress(servicePort), 0);
        server.createContext("/quotes", new QuotesHandler());
        server.createContext("/ordersAll", new OrdersAll());
        server.createContext("/accountStatus", new AccountHandler());

        server.createContext("/", new DefaultHandler());
        server.start();
    }

    private static class DefaultHandler extends HttpHandlerEx {
        @Override
        public String getResponseBody(HttpExchange req) {
            StringBuilder sb = new StringBuilder();
            sb.append("<html><body>\n");

            sb.append("<a href='ordersAll'>ordersAll</a><br/>\n");
            sb.append("<a href='accountStatus'>accountStatus</a><br/>\n");
            sb.append("<br/>\n");

            String url = "quotes?symbol=BTCUSDT&interval=1m&limit=100";
            sb.append("<a href='" + url + "'>" + url + "</a><br>\n");

            sb.append("</body></html>");
            return sb.toString();
        }
    }

    private static class AccountHandler extends HttpHandlerEx {
        @Override
        public String getResponseBody(HttpExchange req) {
            AccountDto account = AppRandomQuote.tradeSession.account;
            return account.serialize().toString();
        }
    }

    private static class QuotesHandler extends HttpHandlerEx {
        @Override
        public String getResponseBody(HttpExchange req) {
            HashMap<String, String> params = getParams(req);
            String symbol = params.get("symbol");
            if (symbol == null) {
                return ErrorDto.serialize("symbol==null");
            }

            String interval = params.get("interval");
            if (interval == null) {
                return ErrorDto.serialize("interval==null for " + symbol);
            }

            QuoteAggCont quoteCont = AppRandomQuote.tradeSession.quoteContMap.get(symbol);
            if (quoteCont == null) {
                return ErrorDto.serialize("QuoteCont is not found for " + symbol);
            }

            QuoteBarCont quoteBarCont = quoteCont.quoteBarCont(interval);
            if (quoteBarCont == null) {
                return ErrorDto.serialize("Interval=" + interval + " is not found for " + symbol);
            }

            int limit = 1000;
            String limit2 = params.get("limit");
            if (limit2 != null) {
                limit = Integer.parseInt(limit2);
            }

            List<QuoteBar> listRet = quoteBarCont.lastQuotes(limit);
            String content = QuoteBarDto.serialize(listRet);
            return content;
        }
    }

    public static class OrdersAll extends HttpHandlerEx {
        @Override
        public String getResponseBody(HttpExchange req) {
            this.mediaType = MediaType.APPLICATION_JSON;

            JSONObject jsonRoot = new JSONObject();
            JSONArray arr1 = new JSONArray();
            for (OrderDto order : AppRandomQuote.tradeSession.allOrders.values()) {
                JSONObject order2 = order.serialize();
                arr1.put(order2);
            }

            jsonRoot.put("orders", arr1);
            return jsonRoot.toString();
        }
    }
}
