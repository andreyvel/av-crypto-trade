package av.crypto.common.dto;

import org.json.JSONArray;
import org.json.JSONObject;
import av.crypto.common.QuoteTick;
import av.crypto.common.Utils;

import java.time.LocalDateTime;
import java.util.Collection;

public class QuoteTickDto {
    public final String symbol;
    public final QuoteTick quoteTick;

    public QuoteTickDto(String symbol, LocalDateTime date, double price, double qnt) {
        this.quoteTick = new QuoteTick(date, price, qnt);
        this.symbol = symbol;
    }

    public QuoteTickDto(String symbol, long epochMsUtc, double price, double qnt) {
        this.quoteTick = new QuoteTick(epochMsUtc, price, qnt);
        this.symbol = symbol;
    }

    public QuoteTickDto(JSONObject rootObj) {
        symbol = rootObj.optString("symbol");
        long dateMs = rootObj.optLong("dateMs");
        double price = rootObj.optDouble("price");
        double qnt = rootObj.optDouble("qnt");
        quoteTick = new QuoteTick(dateMs, price, qnt);
    }

    @Override
    public String toString() {
        return "symbol=" + symbol + ", " + quoteTick.toString();
    }

    public JSONObject serialize() {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("symbol", this.symbol);
        serialize(jsonObj, this.quoteTick);
        return jsonObj;
    }

    private static void serialize(JSONObject rootObj, QuoteTick tick) {
        rootObj.put("dateMs", Utils.ofEpochMilli(tick.date()));
        rootObj.put("price", tick.price());
        rootObj.put("qnt", tick.qnt());
    }

    public static String serialize(Collection<QuoteTick> ticks) {
        JSONObject rootObj = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        rootObj.put("result", jsonArray);

        for(QuoteTick tick : ticks) {
            JSONObject obj = new JSONObject();
            serialize(obj, tick);
            jsonArray.put(obj);
        }
        return rootObj.toString();
    }
}
