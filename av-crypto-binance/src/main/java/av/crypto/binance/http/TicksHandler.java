package av.crypto.binance.http;

import com.sun.net.httpserver.HttpExchange;
import av.crypto.binance.TradeSession;
import av.crypto.common.QuoteTick;
import av.crypto.common.Utils;
import av.crypto.common.dto.QuoteTickCont;
import av.crypto.common.dto.ErrorDto;
import av.crypto.common.dto.QuoteTickDto;
import av.crypto.common.httpserver.HttpHandlerEx;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TicksHandler extends HttpHandlerEx {
    @Override
    public String getResponseBody(HttpExchange req) {
        this.mediaType = HttpHandlerEx.MediaType.APPLICATION_JSON;

        HashMap<String, String> params = getParams(req);
        String symbol = params.get("symbol");
        if (symbol == null) {
            return ErrorDto.serialize("symbol==null");
        }

        QuoteTickCont tickCont = TradeSession.tickCont(symbol);
        if (tickCont == null) {
            return ErrorDto.serialize("QuoteCont is not found for " + symbol);
        }

        long fromMs = Utils.ofEpochMilli(LocalDateTime.now(ZoneOffset.UTC));
        String fromSec = params.get("fromSec");
        if (fromSec != null) {
            fromMs -= Long.parseLong(fromSec) * 1000;
        } else {
            fromMs -= 30_000;
        }

        List<QuoteTick> listRet = new ArrayList<>();
        for(QuoteTick tick : tickCont.quoteTicks().values()) {
            long epochMs = Utils.ofEpochMilli(tick.date());
            if (fromMs <= epochMs) {
                listRet.add(tick);
            }
        }
        String content = QuoteTickDto.serialize(listRet);
        return content;
    }
}
