package av.bitcoin.binance;

import av.bitcoin.binance.http.HttpService;
import com.binance.connector.client.impl.WebSocketApiClientImpl;
import com.binance.connector.client.impl.WebSocketStreamClientImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppMain {
    private static final Logger log = LoggerFactory.getLogger(AppMain.class);
    private static WsApiBinance wsApiBinance;
    private static WsStreamBinance wsStreamBinance;
    private static TradeStreamPublisher tradeStreamPublisher;
    private static TradeCommandListener tradeCommandListener;
    public static TradeScheduler tradeScheduler;

    public static WsStreamBinance wsStreamBinance() {
        return wsStreamBinance;
    }

    public static WsApiBinance wsApiBinance() {
        return wsApiBinance;
    }

    public static void main(String[] args) {
        try {
            AppConfig.loadValues("av-bitcoin-binance.yaml");
            HttpService httpService = new HttpService();
            httpService.start();

            tradeStreamPublisher = new TradeStreamPublisher();
            tradeStreamPublisher.start();

            log.warn("Starting wsStreamClient: " + AppConfig.wsStreamUrl());
            WebSocketStreamClientImpl wsStreamClient = new WebSocketStreamClientImpl(AppConfig.wsStreamUrl());
            wsStreamBinance = new WsStreamBinance(wsStreamClient, tradeStreamPublisher);
            wsStreamBinance.start();

            log.warn("Starting wsApiClient: " + AppConfig.wsApiUrl());
            WebSocketApiClientImpl wsApiClient = new WebSocketApiClientImpl(AppConfig.getApiKey(),
                    AppConfig.getSignatureGenerator(), AppConfig.wsApiUrl());

            wsApiBinance = new WsApiBinance(wsApiClient, tradeStreamPublisher);
            wsApiBinance.start();

            tradeCommandListener = new TradeCommandListener(wsApiBinance);
            tradeCommandListener.start();

            tradeScheduler = new TradeScheduler(wsApiBinance);
            tradeScheduler.start();

            new Thread(() -> {
                try {
                    Thread.sleep(100);
                    wsApiBinance.userDataStreamStart(null);

                    Thread.sleep(100);
                    wsApiBinance.accountStatus(null);

                    Thread.sleep(100);
                    for (AppConfig.KlineInfo kline : AppConfig.loadOnStartupKlines()) {
                        log.warn("Loading history: symbol={}, interval={}, barNum={}, ", kline.symbol, kline.interval, kline.barNum);
                        wsApiBinance.klines(null, kline.symbol, kline.interval, kline.barNum);
                    }

                    Thread.sleep(100);
                    for (String symbol : AppConfig.subscribeSymbols()) {
                        wsApiBinance.allOrders24h(null, symbol);
                    }
                    wsApiBinance.allOpenOrders(null, null);

                    Thread.sleep(100);
                    for (String symbol : AppConfig.subscribeSymbols()) {
                        // get price for market orders
                        wsApiBinance.myTrades24h(null, symbol);
                    }
                } catch (Exception e) {
                    log.error(null, e);
                }
            }).start();

            Runtime.getRuntime().addShutdownHook(new Thread(AppMain::shutdownHook));
        }
        catch(Exception e) {
            log.error("main", e);
        }
    }

    public static void shutdownHook() {
        try {
            wsApiBinance.stop();
        }
        catch (Exception e) {
            log.error(null, e);
        }

        try {
            wsStreamBinance.stop();
        }
        catch (Exception e) {
            log.error(null, e);
        }

        try {
            tradeCommandListener.stop();
        }
        catch (Exception e) {
            log.error(null, e);
        }

        try {
            tradeStreamPublisher.stop();
        }
        catch (Exception e) {
            log.error(null, e);
        }

        tradeScheduler.stop();
    }
}
