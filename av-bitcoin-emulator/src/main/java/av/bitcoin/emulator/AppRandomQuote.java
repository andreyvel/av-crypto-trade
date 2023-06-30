package av.bitcoin.emulator;

import av.bitcoin.common.Enums;
import av.bitcoin.common.QuoteBar;
import av.bitcoin.common.QuoteTick;
import av.bitcoin.common.TimeScale;
import av.bitcoin.common.Utils;
import av.bitcoin.common.dto.AccountDto;
import av.bitcoin.common.dto.OrderDto;
import av.bitcoin.common.dto.QuoteAggCont;
import av.bitcoin.common.dto.QuoteTickDto;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class AppRandomQuote {
    private static final Logger log = LoggerFactory.getLogger(AppRandomQuote.class);

    public static double COMMISSION_MULT = 0.001d;
    public static final String MONEY_CODE = "USDT";
    private static final int INTERVAL_TICK_MS = 100;
    private static final double START_QNT = 0.5;
    private static final double START_BALANCE = 7_000;
    private static final double START_PRICE = 27_000;
    private static final double VOLATILITY_MULT_IN_SEC = 0.0002d;
    public static TradeStreamPublisher tradeStreamPublisher;
    public static TradeSession tradeSession;

    public static void main(String[] args) {
        try {
            AppConfig.loadValues("av-bitcoin-emulator.yaml");

            tradeStreamPublisher = new TradeStreamPublisher();
            tradeSession = new TradeSession(tradeStreamPublisher);
            tradeStreamPublisher.start();

            HttpService httpService = new HttpService();
            httpService.start(AppConfig.tradeRestApiPort());

            TradeCommandListener commandSubscriber = new TradeCommandListener(tradeStreamPublisher);
            commandSubscriber.start();

            AccountDto.AccountBalance bal = new AccountDto.AccountBalance(MONEY_CODE, START_BALANCE, 0);
            tradeSession.account.balances().put(bal.asset(), bal);

            for (String symbol : AppConfig.subscribeSymbols()) {
                log.warn("Generate balance for: {}, startQnt={}", symbol, Utils.num2(START_QNT));
                bal = new AccountDto.AccountBalance(symbol, START_QNT, 0);
                tradeSession.account.balances().put(bal.asset(), bal);
            }
            createRandomPrices();

            Thread quoteTickThread = new Thread(() -> threadCode());
            quoteTickThread.start();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private static void threadCode() {
        Random rnd = new Random();
        double volatilityTick = VOLATILITY_MULT_IN_SEC / Math.sqrt(1000 / INTERVAL_TICK_MS);

        HashMap<String, Double> lastPriceMap = new HashMap<>();
        for (String quote : AppConfig.subscribeSymbols()) {
            QuoteAggCont cont = tradeSession.quoteContMap.get(quote);
            lastPriceMap.put(cont.symbol(), cont.lastTick().price());
        }

        while (!Thread.interrupted()) {
            try {
                Thread.sleep(INTERVAL_TICK_MS);
                for (String symbol : AppConfig.subscribeSymbols()) {
                    QuoteAggCont cont = tradeSession.quoteContMap.get(symbol);
                    double lastPrice = lastPriceMap.get(cont.symbol());
                    lastPrice = lastPrice * (1 + (rnd.nextDouble() - 0.5d) * volatilityTick);
                    lastPriceMap.put(cont.symbol(), lastPrice);

                    double qnt = 100 + rnd.nextInt() % 1000;
                    lastPrice = Utils.round(lastPrice, 2);
                    QuoteTick lastTick = new QuoteTick(LocalDateTime.now(), lastPrice, qnt);
                    cont.update(lastTick);

                    QuoteTickDto tickDto = new QuoteTickDto(symbol, lastTick.date(), lastTick.price(), lastTick.qnt());
                    JSONObject jsonObj = tickDto.serialize();
                    tradeStreamPublisher.send("quote_tick", "emulator", jsonObj);
                }

                for(OrderDto order : tradeSession.allOrders.values()) {
                    if (!Enums.OrderStatus.equals(order.status(), Enums.OrderStatus.NEW)) {
                        continue;
                    }

                    QuoteAggCont cont = tradeSession.quoteContMap.get(order.symbol());
                    double lastPrice = lastPriceMap.get(cont.symbol());
                    boolean orderFilled = false;

                    if (Enums.OrderSide.equals(order.side(), Enums.OrderSide.BUY) && lastPrice < order.price()) {
                        orderFilled = true;
                    }
                    else if (Enums.OrderSide.equals(order.side(), Enums.OrderSide.SELL) && order.price() < lastPrice) {
                        orderFilled = true;
                    }

                    if (orderFilled) {
                        order.status(Enums.OrderStatus.FILLED.toString());
                        long updated = Utils.ofEpochMilli(LocalDateTime.now());
                        double commision = Utils.round(order.quantity() * order.price() * COMMISSION_MULT, 4);
                        order.commission(commision);
                        order.updated(updated);

                        JSONObject jsonDto = order.serialize();
                        tradeStreamPublisher.send("order_changed", "limitOrder", jsonDto);

                        JSONObject jsonAcc = AppRandomQuote.tradeSession.account.serialize();
                        tradeStreamPublisher.send("account_changed", "limitOrder", jsonAcc);
                    }
                }
            } catch (Exception e) {
                log.error(null, e);
            }
        }
    }

    private static void createRandomPrices() {
        int barNum1h = 24 * 7;
        for (String symbol : AppConfig.subscribeSymbols()) {
            log.warn("Generate random price for: {}, startPrice={}", symbol, Utils.num2(START_PRICE));

            LocalDateTime dateTo = LocalDateTime.now();
            LocalDateTime dateFrom = dateTo.minus(barNum1h * 3600, ChronoUnit.SECONDS);

            QuoteAggCont cont = new QuoteAggCont(symbol, AppConfig.subscribeIntervals());
            List<QuoteBar> listRet = getRandomPrices(START_PRICE, dateFrom, dateTo, 1);
            for (QuoteBar quote : listRet) {
                cont.update(quote);
            }

            tradeSession.quoteContMap.put(symbol, cont);
        }
    }

    private static List<QuoteBar> getRandomPrices(double startPrice, LocalDateTime dateFrom, LocalDateTime dateTo, int stepSec) {
        Random rnd = new Random();
        List<QuoteBar> listRet = new ArrayList<>();
        LocalDateTime datePtr = TimeScale.truncateTo(dateFrom, "1s");

        while (datePtr.isBefore(dateTo)) {
            double price1 = Utils.round(startPrice * (1 + (rnd.nextDouble() - 0.5d) * VOLATILITY_MULT_IN_SEC), 2);
            double price2 = Utils.round(price1 * (1 + (rnd.nextDouble() - 0.5d) * VOLATILITY_MULT_IN_SEC), 2);
            double close = Utils.round(price2 * (1 + (rnd.nextDouble() - 0.5d) * VOLATILITY_MULT_IN_SEC), 2);

            double low = Math.min(Math.min(price1, price2), Math.min(startPrice, close));
            double high = Math.max(Math.max(price1, price2), Math.max(startPrice, close));
            QuoteBar bar = new QuoteBar(datePtr, startPrice, high, low, close);
            listRet.add(bar);

            datePtr = datePtr.plusSeconds(stepSec);
            startPrice = close;
        }

        return listRet;
    }
}
