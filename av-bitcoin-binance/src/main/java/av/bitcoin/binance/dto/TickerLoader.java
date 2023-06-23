package av.bitcoin.binance.dto;

import org.json.JSONObject;
import org.trade.common.dto.QuoteBarDto;

public class TickerLoader {
    public static QuoteBarDto load(String content) {
        JSONObject jsonEvent = new JSONObject(content);
        return load(jsonEvent);
    }

    /*
    "stream": "btcusdt@ticker",
    "data": {
        "e": "24hrTicker",  // Event type
        "E": 123456789,     // Event time
        "s": "BNBBTC",      // Symbol
        "p": "0.0015",      // Price change
        "P": "250.00",      // Price change percent
        "w": "0.0018",      // Weighted average price
        "x": "0.0009",      // First trade(F)-1 price (first trade before the 24hr rolling window)
        "c": "0.0025",      // Last price
        "Q": "10",          // Last quantity
        "b": "0.0024",      // Best bid price
        "B": "10",          // Best bid quantity
        "a": "0.0026",      // Best ask price
        "A": "100",         // Best ask quantity
        "o": "0.0010",      // Open price
        "h": "0.0025",      // High price
        "l": "0.0010",      // Low price
        "v": "10000",       // Total traded base asset volume
        "q": "18",          // Total traded quote asset volume
        "O": 0,             // Statistics open time
        "C": 86400000,      // Statistics close time
        "F": 0,             // First trade ID
        "L": 18150,         // Last trade Id
        "n": 18151          // Total number of trades
    }
    */
    public static QuoteBarDto load(JSONObject jsonEvent) {
        String stream = jsonEvent.getString("stream");
        if (!stream.endsWith("@ticker")) {
            throw new RuntimeException("@ticker is not found in " + jsonEvent.toString());
        }

        jsonEvent = jsonEvent.getJSONObject("data");
        String symbol = jsonEvent.optString("s");
        long epochMsUtc = jsonEvent.optLong("E");
        double open = jsonEvent.optDouble("o");
        double high = jsonEvent.optDouble("h");
        double low = jsonEvent.optDouble("l");
        double close = jsonEvent.optDouble("c");
        double vol = jsonEvent.optDouble("v");

        QuoteBarDto bar = new QuoteBarDto(symbol, epochMsUtc, open, high, low, close, vol);
        return bar;
    }
}
