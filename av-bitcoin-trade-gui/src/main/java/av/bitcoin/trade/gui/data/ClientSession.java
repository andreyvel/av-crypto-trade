package av.bitcoin.trade.gui.data;

import av.bitcoin.trade.gui.AppConfig;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import av.bitcoin.common.QuoteBar;
import av.bitcoin.common.TradeCommand;
import av.bitcoin.common.Utils;
import av.bitcoin.common.dto.AccountDto;
import av.bitcoin.common.dto.ChartItemDto;
import av.bitcoin.common.dto.ChartLineDto;
import av.bitcoin.common.dto.OrderDto;
import av.bitcoin.common.dto.PingDto;
import av.bitcoin.common.dto.QuoteBarDto;
import av.bitcoin.common.dto.QuoteTickDto;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.net.ConnectException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;

import static av.bitcoin.common.Enums.*;

public class ClientSession {
    private static final Logger log = LoggerFactory.getLogger(ClientSession.class);
    private AccountDto accountStatus = new AccountDto();
    private SortedMap<Long, OrderDto> ordersAll = new ConcurrentSkipListMap<>();

    private HttpClient client = HttpClient.newHttpClient();
    private final SortedMap<String, QuoteConsumer> quoteSubscribers = new ConcurrentSkipListMap<>();
    private LocalDateTime refreshAccountLast = LocalDateTime.now().minusDays(1);
    public List<ChartItemDto> chartItems = null;
    public List<ChartLineDto> chartLines = null;
    private ZContext zmqContext = new ZContext();
    private ZMQ.Socket tradeStreamPublisher = null;

    public ClientSession() {
        log.warn("Starting ZMQ events publisher: {}", AppConfig.tradeStreamSub());
        tradeStreamPublisher = zmqContext.createSocket(SocketType.PUB);
        tradeStreamPublisher.bind(AppConfig.tradeStreamSub());
    }

    public AccountDto accountStatus() {
        return accountStatus;
    }

    public SortedMap<Long, OrderDto> ordersAll() {
        return ordersAll;
    }

    public AccountDto.AccountBalance accountBalance(String symbol) {
        AccountDto.AccountBalance bal = accountStatus.balances().get(symbol);
        return bal;
    }

    public void startScheduler() {
        int schedulerIntervalMs = 1000;
        Thread threadScheduler = new Thread(() -> {
            log.warn("Starting quote scheduler, schedulerIntervalMs={}", schedulerIntervalMs);

            int delayMult = 1;
            while (!Thread.interrupted()) {
                try {
                    Thread.sleep(schedulerIntervalMs * delayMult);
                    //errorNum = refreshQuoteConsumers();

                    if (Math.abs(Utils.delayMs(refreshAccountLast, LocalDateTime.now())) > 3_000) {
                        // errorNum += refreshAccountStatusRest();
                    }
                    delayMult = 1;
                } catch (Exception e) {
                    log.error(null, e);
                    delayMult = 5;
                }
            }
        });
        threadScheduler.start();

        Thread threadAdviceEvents = new Thread(() -> tradeAdviceSubscribe());
        threadAdviceEvents.start();

        Thread threadStreamSubscribe = new Thread(() -> tradeStreamSubscribe());
        threadStreamSubscribe.start();

        refreshRestData();
    }
    public void tradeAdviceSubscribe() {
        log.warn("Starting ZMQ advice subscriber: {}...", AppConfig.tradeAdvicePub());
        try (ZContext context = new ZContext()) {
            try (ZMQ.Socket subscriber = context.createSocket(SocketType.SUB)) {
                subscriber.subscribe(ZMQ.SUBSCRIPTION_ALL);
                subscriber.connect(AppConfig.tradeAdvicePub());

                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        String jsonStr = subscriber.recvStr();
                        JSONObject rootObj = new JSONObject(jsonStr);
                        JSONArray jsonArr = rootObj.optJSONArray("chartItems");
                        chartItems = ChartItemDto.deserialize(jsonArr);

                        jsonArr = rootObj.optJSONArray("chartLines");
                        chartLines = ChartLineDto.deserialize(jsonArr);
                    } catch (Exception e) {
                        log.error(null, e);
                    }
                }
            }
        }
    }

    private int refreshAccountStatus() throws Exception {
        URL restUrl = new URL(new URL(AppConfig.restService()), "accountStatus");

        try {
            HttpRequest request = HttpRequest.newBuilder().uri(restUrl.toURI()).build();
            HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String content = response.body().toString();
            JSONObject balObj = new JSONObject(content);

            AccountDto accountNew = new AccountDto(balObj);
            refreshAccountLast = LocalDateTime.now();
            accountStatus = accountNew;
        } catch(ConnectException ex) {
            log.error("{}, {}", restUrl, ex.toString());
            return 1;
        }
        return 0;
    }

    private int refreshOrdersAll() throws Exception {
        URL restUrl = new URL(new URL(AppConfig.restService()), "ordersAll");

        try {
            HttpRequest request = HttpRequest.newBuilder().uri(restUrl.toURI()).build();
            HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String content = response.body().toString();
            JSONObject ordersObj = new JSONObject(content);
            SortedMap<Long, OrderDto> ordersNew = new ConcurrentSkipListMap<>();

            JSONArray resultArr = ordersObj.optJSONArray("orders");
            for(int ind = 0; ind < resultArr.length(); ind++) {
                JSONObject rowObj = resultArr.optJSONObject(ind);
                OrderDto order = new OrderDto(rowObj);
                ordersNew.put(order.orderId(), order);
            }
            ordersAll = ordersNew;
        } catch(ConnectException ex) {
            log.error("{}, {}", restUrl, ex.toString());
            return 1;
        }
        return 0;
    }

    public void unsubscribe(String uuid) {
        quoteSubscribers.remove(uuid);
    }

    public void subscribe(QuoteConsumer consumer) {
        try {
            quoteSubscribers.put(consumer.uuid(), consumer);
            refreshQuoteConsumer(consumer);
        } catch(Exception ex) {
            log.error(null, ex);
        }
    }

    public int refreshQuoteConsumers() {
        int errorNum = 0;
        for(QuoteConsumer sub : quoteSubscribers.values()) {
            try {
                errorNum += refreshQuoteConsumer(sub);
            } catch(Exception ex) {
                log.error(null, ex);
            }
        }
        return errorNum;
    }

    public int refreshQuoteConsumer(QuoteConsumer sub) throws Exception {
        String content = null;
        String params = "symbol=" + sub.symbol() + "&interval=" + sub.interval() + "&limit=1000"; // sub.limit()
        URL restUrl = new URL(new URL(AppConfig.restService()), "quotes?" + params);

        try {
            HttpRequest request = HttpRequest.newBuilder().uri(restUrl.toURI()).build();
            HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
            content = response.body().toString();
        } catch(ConnectException ex) {
            log.error("{}, {}", restUrl, ex.toString());
            return 1;
        }

        List<QuoteBar> quotes = QuoteBarDto.deserialize(content);
        sub.update(quotes);
        return 0;
    }

    public void tradeStreamSubscribe() {
        log.warn("Starting ZMQ trade stream subscriber: {}...", AppConfig.tradeStreamPub());
        try (ZContext context = new ZContext()) {
            try (ZMQ.Socket subscriber = context.createSocket(SocketType.SUB)) {
                subscriber.subscribe(ZMQ.SUBSCRIPTION_ALL);
                subscriber.connect(AppConfig.tradeStreamPub());

                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        String jsonStr = subscriber.recvStr();
                        JSONObject jsonObj = new JSONObject(jsonStr);
                        String eventType = jsonObj.optString("event_type");

                        if (!"quote_tick".equals(eventType)) {
                            log.warn("Received message: {}", jsonStr);
                        }

                        if ("quote_tick".equals(eventType)) {
                            // {"symbol":"BTCUSDT","dateMs":1686062700943,"price":26004.6,
                            // "qnt":0.062669,"source":"aggTrade","type":"quote_tick"}
                            for(QuoteConsumer sub : quoteSubscribers.values()) {
                                try {
                                    QuoteTickDto tickDto = new QuoteTickDto(jsonObj);
                                    sub.update(tickDto.quoteTick);
                                } catch(Exception ex) {
                                    log.error(null, ex);
                                }
                            }
                        } else if ("ping".equals(eventType)) {
                            PingDto pin = new PingDto(jsonObj);
                            log.warn("Ping delay {} ms", pin.delayMs());
                        } else if ("account_changed".equals(eventType)) {
                            accountStatus = new AccountDto(jsonObj);
                        } else if ("order_changed".equals(eventType)) {
                            OrderDto orderDto = new OrderDto(jsonObj);
                            ordersAll.put(orderDto.orderId(), orderDto);
                        }
                    } catch (Exception e) {
                        log.error(null, e);
                    }
                }
            }
        }
    }

    public void ping() {
        String pingCmd = TradeCommand.ping().toString();
        log.warn("Send message: {}", pingCmd);
        tradeStreamPublisher.send(pingCmd);
    }

    public void marketOrder(String symbol, OrderSide orderSide, double quantity) {
        String orderCmd = TradeCommand.marketOrder(symbol, orderSide, quantity).toString();
        log.warn("Send message: {}", orderCmd);
        tradeStreamPublisher.send(orderCmd);
    }

    public void limitOrder(String symbol, OrderSide orderSide, double quantity, double price) {
        String orderCmd = TradeCommand.limitOrder(symbol, orderSide, quantity, price).toString();
        log.warn("Send message: {}", orderCmd);
        tradeStreamPublisher.send(orderCmd);
    }

    public void cancelOrder(String symbol, long orderId) {
        String orderCmd = TradeCommand.cancelOrder(symbol, orderId).toString();
        log.warn("Send message: {}", orderCmd);
        tradeStreamPublisher.send(orderCmd);
    }

    public void cancelAllOrders(String symbol) {
        String orderCmd = TradeCommand.cancelAllOrders(symbol).toString();
        log.warn("Send message: {}", orderCmd);
        tradeStreamPublisher.send(orderCmd);
    }

    public void refreshRestData() {
        try {
            log.warn("Refresh data using REST API.");
            refreshQuoteConsumers();
            refreshAccountStatus();
            refreshOrdersAll();
        }
        catch(Exception e) {
            log.error(null, e);
        }
    }

    public static double roundPrice(double priceRaw, String symbol) {
        // TODO remove hardcode
        return Utils.round(priceRaw, 0);
    }
}
