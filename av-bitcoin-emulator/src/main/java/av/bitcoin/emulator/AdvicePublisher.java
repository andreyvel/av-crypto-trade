package av.bitcoin.emulator;

import av.bitcoin.common.Enums;
import av.bitcoin.common.QuoteBar;
import av.bitcoin.common.TimeScale;
import av.bitcoin.common.Utils;
import av.bitcoin.common.dto.ChartItemDto;
import av.bitcoin.common.dto.ChartLineDto;
import av.bitcoin.common.dto.QuoteBarDto;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AdvicePublisher {
    private static final Logger log = LoggerFactory.getLogger(AdvicePublisher.class);
    private static final String PREDICT_INTERVAL = "1h";
    private static final int PREDICT_INTERVALS = 24;

    public void start(String publisherAddress) {
        Thread advicePublisher = new Thread(() -> {
            try(ZContext context = new ZContext()) {
                log.warn("Starting ZMQ dummy advice publisher {}...", publisherAddress);

                try (ZMQ.Socket publisher = context.createSocket(SocketType.PUB)) {
                    publisher.bind(publisherAddress);

                    while (!Thread.interrupted()) {
                        try {
                            Thread.sleep(1000);
                            for (String symbol : AppConfig.subscribeSymbols()) {
                                JSONObject rootObj = predictSymbol(symbol);
                                String json = rootObj.toString();
                                publisher.send(json);
                            }
                        } catch (Exception ex) {
                            log.error(null, ex);
                        }
                    }
                }
            }
        });
        advicePublisher.start();
    }

    private JSONObject predictSymbol(String symbol) throws Exception {
        JSONObject rootObj = new JSONObject();
        rootObj.put("symbol", symbol);

        int intervalSec = TimeScale.totalSec(PREDICT_INTERVAL);
        LocalDateTime lineFrom = TimeScale.truncateTo(LocalDateTime.now(), PREDICT_INTERVAL).plusSeconds(intervalSec);
        LocalDateTime lineTo = lineFrom.plusSeconds(intervalSec * PREDICT_INTERVALS);;

        // Add 2% levels relative last price to the chart
        List<ChartLineDto> lines = new ArrayList<>();
        lines.add(addValueLines(lineFrom, lineTo, 2.0d));
        lines.add(addValueLines(lineFrom, lineTo, -2.0d));
        JSONArray linesArr = ChartLineDto.serialize(lines);
        rootObj.put("chartLines", linesArr);

        // At this point should be real ML algorithm
        // in this code only dummy mirror prediction
        List<QuoteBar> lastQuotes = loadLastQuotes(symbol);
        List<ChartItemDto> list = new ArrayList<>();

        for (int ind = 0; ind < PREDICT_INTERVALS; ind++) {
            LocalDateTime predictDate = lineFrom.plusSeconds(intervalSec * ind);
            int dummyInd = lastQuotes.size() - ind - 1;
            if (dummyInd < 0) {
                break;
            }

            QuoteBar dummyBar = lastQuotes.get(dummyInd);
            QuoteBar lastBar = lastQuotes.get(lastQuotes.size() - 1);

            ChartItemDto dto = new ChartItemDto(predictDate, "0", "#006400", Enums.ValueUnit.PCT, 2);
            double predicrPctUp = Utils.pct(lastBar.close(),  dummyBar.high());
            dto.value(predicrPctUp);
            list.add(dto);

            dto = new ChartItemDto(predictDate, "0", "#bb0044", Enums.ValueUnit.PCT, 2);
            double predicrPctDown = Utils.pct(lastBar.close(),  dummyBar.low());
            dto.value(predicrPctDown);
            list.add(dto);
        }

        JSONArray jsonArr = ChartItemDto.serialize(list);
        rootObj.put("chartItems", jsonArr);

        return rootObj;
    }

    private ChartLineDto addValueLines(LocalDateTime dateFrom, LocalDateTime dateTo, double valuePct) {
        ChartLineDto line = new ChartLineDto(dateFrom, dateTo, "#8A3324", Enums.ValueUnit.PCT);
        line.value0(valuePct);
        line.value1(valuePct);
        return line;
    }

    public static List<QuoteBar> loadLastQuotes(String symbol) throws Exception {
        String params = "symbol=" + symbol + "&interval=" + PREDICT_INTERVAL + "&limit=1000";
        URL restUrl = new URL(new URL(AppConfig.restApiService()), "quotes?" + params);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(restUrl.toURI()).build();
        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String content = response.body().toString();

        List<QuoteBar> quotes = QuoteBarDto.deserialize(content);
        quotes.sort(Comparator.comparing(QuoteBar::date));
        return quotes;
    }
}
