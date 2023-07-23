package av.crypto.common.dto;

import org.json.JSONArray;
import org.json.JSONObject;
import av.crypto.common.QuoteBar;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class QuoteBarDto {
    public final String symbol;
    public final QuoteBar quoteBar;

    public QuoteBarDto(String symbol, long epochMsUtc, double open, double high, double low, double close, double vol) {
        quoteBar = new QuoteBar(epochMsUtc, open, high, low, close, vol);
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return "symbol=" + symbol + ", " + quoteBar.toString();
    }

    public JSONObject serialize() {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("symbol", this.symbol);
        serialize(jsonObj, this.quoteBar);
        return jsonObj;
    }

    private static void serialize(JSONObject jsonObj, QuoteBar bar) {
        jsonObj.put("date", bar.date());
        jsonObj.put("open", bar.open());
        jsonObj.put("high", bar.high());
        jsonObj.put("low", bar.low());
        jsonObj.put("close", bar.close());
        jsonObj.put("vol", bar.vol());
    }

    public static String serialize(Collection<QuoteBar> quotes) {
        JSONObject rootObj = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        rootObj.put("result", jsonArray);

        for(QuoteBar bar : quotes) {
            JSONObject jsonObj = new JSONObject();
            serialize(jsonObj, bar);
            jsonArray.put(jsonObj);
        }
        return rootObj.toString();
    }
    public static List<QuoteBar> deserialize(String jsonStr) {
        JSONObject rootObj = new JSONObject(jsonStr);
        String error = rootObj.optString("error");
        if (error != null && error.length() > 0) {
            throw new RuntimeException(error);
        }

        List<QuoteBar> quotes = new ArrayList<>();
        JSONArray jsonArr = rootObj.getJSONArray("result");

        for (int ind = 0; ind < jsonArr.length(); ind++) {
            JSONObject obj = jsonArr.getJSONObject(ind);

            double open = obj.optDouble("open");
            double high = obj.optDouble("high");
            double low = obj.optDouble("low");
            double close = obj.optDouble("close");
            long vol = obj.optLong("vol");

            String date2 = obj.optString("date");
            LocalDateTime date = LocalDateTime.parse(date2);
            QuoteBar bar = new QuoteBar(date, open, high, low, close, vol);
            quotes.add(bar);
        }
        return quotes;
    }
}
