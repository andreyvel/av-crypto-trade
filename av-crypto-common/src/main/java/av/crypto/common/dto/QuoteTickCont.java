package av.crypto.common.dto;

import av.crypto.common.QuoteTick;

import java.time.LocalDateTime;
import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class QuoteTickCont {
    private final SortedMap<LocalDateTime, QuoteTick> quoteTicks = new ConcurrentSkipListMap<>();
    private QuoteTick lastTick = null;

    public QuoteTick getLastTick() {
        return lastTick;
    }

    public SortedMap<LocalDateTime, QuoteTick> quoteTicks() {
        return quoteTicks;
    }

    public synchronized void update(QuoteTick lastTick) {
        if (this.lastTick != null && this.lastTick.date().isAfter(lastTick.date())) {
            throw new ArithmeticException("this.lastTick=" + this.lastTick + " > lastTick=" + lastTick);
        }

        this.lastTick = lastTick;
        QuoteTick tickOld = quoteTicks.get(lastTick.date());
        if (tickOld == null) {
            quoteTicks.put(lastTick.date(), lastTick);
        } else {
            tickOld.update(lastTick);
        }
    }
    public synchronized void update(List<QuoteTick> quotes) {
        for(QuoteTick quote : quotes) {
            quoteTicks.put(quote.date(), quote);
        }
    }
}
