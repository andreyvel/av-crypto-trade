package av.bitcoin.binance.dto;

import org.json.JSONObject;
import org.trade.common.dto.QuoteTickDto;

public class AggTradeLoader {
    public static QuoteTickDto load(String content) {
        JSONObject jsonEvent = new JSONObject(content);
        return load(jsonEvent);
    }

    /*
    "stream": "btcusdt@aggTrade",
    "data": {
        "e": "aggTrade",  // Event type
        "E": 123456789,   // Event time
        "s": "BNBBTC",    // Symbol
        "a": 12345,       // Aggregate trade ID
        "p": "0.001",     // Price
        "q": "100",       // Quantity
        "f": 100,         // First trade ID
        "l": 105,         // Last trade ID
        "T": 123456785,   // Trade time
        "m": true,        // Is the buyer the market maker?
        "M": true         // Ignore
    }
    */
    public static QuoteTickDto load(JSONObject jsonEvent) {
        String stream = jsonEvent.getString("stream");
        if (!stream.endsWith("@aggTrade")) {
            throw new RuntimeException("@aggTrade is not found in " + jsonEvent.toString());
        }

        jsonEvent = jsonEvent.getJSONObject("data");
        String symbol = jsonEvent.optString("s");
        if (symbol == null) {
            throw new RuntimeException("symbol == null in " + jsonEvent.toString());
        }

        long epochMsUtc = jsonEvent.optLong("E");
        double price = jsonEvent.optDouble("p");
        double qnt = jsonEvent.optDouble("q");

        QuoteTickDto row = new QuoteTickDto(symbol, epochMsUtc, price, qnt);
        return row;
    }
}
