package av.bitcoin.binance.http;

import com.sun.net.httpserver.HttpExchange;
import av.bitcoin.binance.TradeSession;
import org.trade.common.QuoteBar;
import org.trade.common.dto.QuoteBarCont;
import org.trade.common.dto.QuoteAggCont;
import org.trade.common.dto.ErrorDto;
import org.trade.common.dto.QuoteBarDto;
import org.trade.common.httpserver.HttpHandlerEx;

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
