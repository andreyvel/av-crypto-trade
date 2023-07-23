package av.crypto.binance;

import av.crypto.binance.dto.AccountLoader;
import av.crypto.binance.dto.AggTradeLoader;
import av.crypto.binance.dto.OrderLoader;
import com.binance.connector.client.impl.WebSocketStreamClientImpl;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import av.crypto.common.dto.OrderDto;
import av.crypto.common.dto.QuoteTickDto;
import java.util.ArrayList;

public class WsStreamBinance {
    private static final Logger log = LoggerFactory.getLogger(WsStreamBinance.class);
    private WebSocketStreamClientImpl wsStreamClient;
    private TradeStreamPublisher tradeStreamPublisher;

    public WsStreamBinance(WebSocketStreamClientImpl wsStreamClient, TradeStreamPublisher tradeStreamPublisher) {
        this.wsStreamClient = wsStreamClient;
        this.tradeStreamPublisher = tradeStreamPublisher;
    }

    public void start() {
        ArrayList<String> streams = new ArrayList<>();
        for (String symbol : AppConfig.subscribeSymbols()) {
            // All symbols for streams are lowercase
            // https://binance-docs.github.io/apidocs/spot/en/#websocket-market-streams

            String symbol2 = symbol.toLowerCase();
            streams.add(symbol2 + "@aggTrade");
            log.warn("Starting stream: {}@aggTrade", symbol2);
        }

        wsStreamClient.combineStreams(streams, (event -> {
            try {
                combineStreamProcessor(event);
            } catch (Exception ex) {
                log.error(event, ex);
            }
        }));
    }

    public void stop() {
        log.warn("websocketStreamClient.closeAllConnections()...");
        wsStreamClient.closeAllConnections();
    }

    public void combineStreamProcessor(String event) {
        JSONObject jsonEvent = new JSONObject(event);
        String stream = jsonEvent.optString("stream");
        if (stream == null) {
            log.error("combineStreams: Unknown event={}", event);
            return;
        }

        String[] data = stream.split("@");
        if (data.length != 2) {
            log.error("combineStreams: parsed stream length != 2, event={}", event);
            return;
        }

        String streamName = data[1];
        if ("aggTrade".equals(streamName)) {
            // {"stream":"btcusdt@aggTrade","data":{"e":"aggTrade","E":1682862410111...
            QuoteTickDto tickDto = AggTradeLoader.load(jsonEvent);
            TradeSession.updateTickStream(tickDto);
            TradeSession.updateQuoteStream(tickDto);

            if (tradeStreamPublisher != null) {
                JSONObject jsonObj = tickDto.serialize();
                tradeStreamPublisher.send("quote_tick", "aggTrade", jsonObj);
            }
        } else {
            log.warn("combineStreams: Unknown streamName={}, event={}", streamName, event);
        }
    }

    public void subscribeUserStream(String listenKey) {
        log.warn("Start listening user stream, listenKey={}", listenKey);
        wsStreamClient.listenUserStream(listenKey, ((event) -> {
            try {
                userStreamProcessor(event);
            } catch (Exception e) {
                log.error(event, e);
            }
        }));
    }

    private void userStreamProcessor(String event) {
        log.warn("userStreamProcessor: {}", event);
        JSONObject jsonEvent = new JSONObject(event);
        String eventName = jsonEvent.optString("e");

        if ("balanceUpdate".equals(eventName)) {
            // Balance Update occurs during deposit or withdrawals
            AccountLoader.changeBalance(TradeSession.accountStatus, jsonEvent);
            JSONObject jsonObj = TradeSession.accountStatus.serialize();
            tradeStreamPublisher.send("account_changed", "balanceUpdate", jsonObj);
        }
        else if ("outboundAccountPosition".equals(eventName)) {
            AccountLoader.updateAccount(TradeSession.accountStatus, jsonEvent);
            JSONObject jsonObj = TradeSession.accountStatus.serialize();
            tradeStreamPublisher.send("account_changed", "outboundAccountPosition", jsonObj);
        }
        else if ("executionReport".equals(eventName)) {
            OrderDto orderDto = OrderLoader.loadExecutionReport(jsonEvent);
            orderDto = TradeSession.ordersAllUpdate(orderDto, eventName);

            JSONObject jsonObj = orderDto.serialize();
            tradeStreamPublisher.send("order_changed", "executionReport", jsonObj);
        }
        else {
            log.warn("updateUserDataStream: Unknown eventName={}, event={}", eventName, event);
        }
    }
}
