package av.crypto.binance.http;

import com.sun.net.httpserver.HttpExchange;
import av.crypto.binance.TradeSession;
import av.crypto.common.QuoteBar;
import av.crypto.common.dto.QuoteBarCont;
import av.crypto.common.dto.QuoteAggCont;
import av.crypto.common.dto.ErrorDto;
import av.crypto.common.dto.QuoteBarDto;
import av.crypto.common.httpserver.HttpHandlerEx;

import java.util.HashMap;
import java.util.List;

public class QuotesHandler extends HttpHandlerEx {
    @Override
    public String getResponseBody(HttpExchange req) {
        this.mediaType = HttpHandlerEx.MediaType.APPLICATION_JSON;

        HashMap<String, String> params = getParams(req);
        String symbol = params.get("symbol");

        if (symbol == null) {
            return ErrorDto.serialize("symbol==null");
        }

        String interval = params.get("interval");
        if (interval == null) {
            return ErrorDto.serialize("interval==null for " + symbol);
        }

        QuoteAggCont quoteCont = TradeSession.quoteCont(symbol);
        if (quoteCont == null) {
            return ErrorDto.serialize("QuoteCont is not found for " + symbol);
        }

        QuoteBarCont quoteBarCont = quoteCont.quoteBarCont(interval);
        if (quoteBarCont == null) {
            return ErrorDto.serialize("Interval=" + interval + " is not found for " + symbol);
        }

        int limit = 500;
        String limit2 = params.get("limit");
        if (limit2 != null) {
            limit = Integer.parseInt(limit2);
        }

        List<QuoteBar> listRet = quoteBarCont.lastQuotes(limit);
        String content = QuoteBarDto.serialize(listRet);
        return content;
    }
}
