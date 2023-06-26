package av.bitcoin.binance.dto;

import org.json.JSONArray;
import org.json.JSONObject;
import av.bitcoin.common.QuoteBar;

import java.util.ArrayList;
import java.util.List;

public class KlinesLoader {
    public static List<QuoteBar> load(String content) {
        JSONObject jsonEvent = new JSONObject(content);
        return load(jsonEvent);
    }

    /*
      1655971200000,      // Kline open time
      "0.01086000",       // Open price
      "0.01086600",       // High price
      "0.01083600",       // Low price
      "0.01083800",       // Close price
      "2290.53800000",    // Volume
      1655974799999,      // Kline close time
      "24.85074442",      // Quote asset volume
      2283,               // Number of trades
      "1171.64000000",    // Taker buy base asset volume
      "12.71225884",      // Taker buy quote asset volume
      "0"                 // Unused field, ignore
    */
    public static List<QuoteBar> load(JSONObject jsonEvent) {
        List<QuoteBar> listRet = new ArrayList<>();
        int status = jsonEvent.optInt("status");
        if (status != 200) {
            throw new RuntimeException("Bad status=" + status);
        }

        JSONArray resultArr = jsonEvent.optJSONArray("result");
        for(int ind = 0; ind < resultArr.length(); ind++) {
            JSONArray rowObj = resultArr.optJSONArray(ind);
            long epochMsUtc = rowObj.optLong(0);
            double open = rowObj.optDouble(1);
            double high = rowObj.optDouble(2);
            double low = rowObj.optDouble(3);
            double close = rowObj.optDouble(4);
            double vol = rowObj.optDouble(5);

            QuoteBar bar = new QuoteBar(epochMsUtc, open, high, low, close, vol);
            listRet.add(bar);
        }
        return listRet;
    }
}
