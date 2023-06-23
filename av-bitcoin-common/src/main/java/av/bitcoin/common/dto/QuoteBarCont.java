package av.bitcoin.common.dto;

import av.bitcoin.common.QuoteBar;
import av.bitcoin.common.QuoteTick;
import av.bitcoin.common.TimeScale;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class QuoteBarCont {
    private final NavigableMap<LocalDateTime, QuoteBar> quoteBarMap = new ConcurrentSkipListMap<>();
    private QuoteTick lastTick = null;
    private final String interval;

    public QuoteBarCont(String interval) {
        this.interval = interval;
    }

    public QuoteTick lastTick() {
        return lastTick;
    }

    public List<QuoteBar> lastQuotes(int limit) {
        List<QuoteBar> listRet = new ArrayList<>();
        for(LocalDateTime dateKey : quoteBarMap.descendingKeySet()) {
            if (listRet.size() >= limit) {
                break;
            }
            QuoteBar quote = quoteBarMap.get(dateKey);
            listRet.add(quote);
        }
        return listRet;
    }

    private void updateTick(QuoteBar newBar) {
        QuoteTick newTick = new QuoteTick(newBar.date(), newBar.close());
        updateTick(newTick);
    }

    private void updateTick(QuoteTick newTick) {
        if (this.lastTick == null || lastTick.date().isBefore(newTick.date())) {
            this.lastTick = newTick;
        }
    }

    public void update(QuoteTick newTick) {
        LocalDateTime dateKey = TimeScale.truncateTo(newTick.date(), interval);
        updateTick(newTick);

        QuoteBar quote = quoteBarMap.get(dateKey);
        if (quote == null) {
            quote = new QuoteBar(dateKey, newTick.price());
            quoteBarMap.put(quote.date(), quote);
        } else {
            quote.update(newTick.price());
        }
    }

    public void update(QuoteBar quote) {
        LocalDateTime dateKey = TimeScale.truncateTo(quote.date(), interval);
        updateTick(quote);

        QuoteBar bar = quoteBarMap.get(dateKey);
        if (bar == null) {
            bar = new QuoteBar(dateKey, quote.open(), quote.high(), quote.low(), quote.close(), quote.vol());
            quoteBarMap.put(bar.date(), bar);
        } else {
            bar.update(quote);
        }
    }

    public void update(List<QuoteBar> quotes) {
        QuoteBar prev = null;
        for(QuoteBar quote : quotes) {
            if (prev != null && !prev.date().isBefore(quote.date())) {
                throw new RuntimeException("Bad ordering: prev=" + prev + ", quote=" + quote);
            }

            LocalDateTime dateKey = TimeScale.truncateTo(quote.date(), interval);
            quoteBarMap.put(dateKey, quote);
            prev = quote;
        }

        if (quotes.size() > 0) {
            updateTick(quotes.get(quotes.size() - 1));
        }
    }
}
