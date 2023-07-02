package av.bitcoin.trade.gui.data;

import av.bitcoin.common.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.SortedMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;

public class QuoteConsumer implements IQuoteConsumer {
    private final String uuid = UUID.randomUUID().toString();
    private String symbol;
    private String interval;
    private QuoteBar firstBar;
    private QuoteBar lastBar;
    private StatBar stat;
    private SortedMap<LocalDateTime, QuoteBar> quoteBarMap = new ConcurrentSkipListMap<>();
    private QuoteTick lastTick = null;

    public QuoteTick lastTick() {
        return lastTick;
    }

    public QuoteConsumer(String symbol, String interval) {
        this.symbol = symbol;
        this.interval = interval;

        // default values, used if publisher is not started
        double defaultMin = 100d;
        double defaultMax = 103d;
        LocalDateTime dateNow = TimeScale.truncateTo(LocalDateTime.now(), interval);
        firstBar = new QuoteBar(dateNow.minusDays(1), defaultMin);
        lastBar = new QuoteBar(dateNow, defaultMax);

        stat = new StatBar();
        stat.update(defaultMin, defaultMax);
    }

    public SortedMap<LocalDateTime, QuoteBar> quoteBarMap() {
        return quoteBarMap;
    }

    @Override
    public int limit() {
        if (lastBar == null || quoteBarMap.size() == 0) {
            return 200;
        }

        long deltaSec = Duration.between(lastBar.date(), LocalDateTime.now()).getSeconds();
        int intervalSec = TimeScale.totalSec(interval);
        return 2 + (int)(deltaSec / intervalSec);
    }
    @Override
    public String uuid() {
        return uuid;
    }
    @Override
    public String symbol() {
        return symbol;
    }

    @Override
    public String interval() {
        return interval;
    }

    @Override
    public void update(Collection<QuoteBar> quotes) {
        for(QuoteBar bar : quotes) {
            quoteBarMap.put(bar.date(), bar);
        }

        firstBar = null;
        StatBar statNew = new StatBar();

        for(QuoteBar bar : quoteBarMap.values()) {
            if (firstBar == null) {
                firstBar = bar;
            }

            smartUpdate(statNew, bar);
            lastBar = bar;
        }

        stat = statNew;
    }

    public void smartUpdate(StatBar stat, QuoteBar bar) {
        double changePct = Utils.pct(bar.low(), bar.high());
        if (Math.abs(changePct) < 5.0d) {
            // ignore testnet anomaly
            stat.update(bar);
        }
    }

    @Override
    public void update(QuoteTick quoteTick) {
        LocalDateTime date = TimeScale.truncateTo(quoteTick.date(), interval);

        if (lastBar == null || !lastBar.date().equals(date)) {
            lastBar = new QuoteBar(date, quoteTick.price());
            quoteBarMap.put(lastBar.date(), lastBar);
        }

        lastBar.update(quoteTick.price());
        smartUpdate(stat, lastBar);
        lastTick = quoteTick;
    }

    public double priceMin() {
        return stat.min();
    }

    public double priceMax() {
        return stat.max();
    }

    public QuoteBar firstBar() {
        return firstBar;
    }

    public QuoteBar lastBar() {
        return lastBar;
    }
}
