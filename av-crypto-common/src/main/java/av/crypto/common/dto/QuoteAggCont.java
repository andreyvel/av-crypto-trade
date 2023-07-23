package av.crypto.common.dto;

import av.crypto.common.QuoteBar;
import av.crypto.common.QuoteTick;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class QuoteAggCont {
    private final ConcurrentHashMap<String, QuoteBarCont> quoteBarMap = new ConcurrentHashMap<>();
    private final List<String> intervals = new ArrayList<>();
    private QuoteTick lastTick = null;
    private final String symbol;

    public QuoteAggCont(String symbol, List<String> intervals) {
        this.intervals.addAll(intervals);
        this.symbol = symbol;

        for (String interval : intervals) {
            quoteBarMap.put(interval, new QuoteBarCont(interval));
        }
    }

    public String symbol() {
        return symbol;
    }

    public QuoteTick lastTick() {
        return lastTick;
    }

    private void updateTick(QuoteTick newTick) {
        if (this.lastTick == null || lastTick.date().isBefore(newTick.date())) {
            this.lastTick = newTick;
        }
    }

    private void updateTick(QuoteBar newBar) {
        QuoteTick newTick = new QuoteTick(newBar.date(), newBar.close());
        if (this.lastTick == null || lastTick.date().isBefore(newTick.date())) {
            this.lastTick = newTick;
        }
    }

    public void update(QuoteTick tick) {
        updateTick(tick);
        for (String interval : intervals) {
            QuoteBarCont cont = quoteBarMap.get(interval);
            cont.update(tick);
        }
    }

    public void update(QuoteBar quoteBar) {
        updateTick(quoteBar);
        for (String interval : intervals) {
            QuoteBarCont cont = quoteBarMap.get(interval);
            cont.update(quoteBar);
        }
    }

    public void update(String interval, List<QuoteBar> quotes) {
        QuoteBarCont cont = quoteBarMap.get(interval);
        if (cont == null) {
            cont = new QuoteBarCont(interval);
            quoteBarMap.put(interval, cont);
        }

        cont.update(quotes);

        if (cont.lastTick() != null) {
            updateTick(cont.lastTick());
        }
    }

    public QuoteBarCont quoteBarCont(String interval) {
        QuoteBarCont cont = quoteBarMap.get(interval);
        return cont;
    }
}
