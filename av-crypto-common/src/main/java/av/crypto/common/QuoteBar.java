package av.crypto.common;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class QuoteBar {
    private LocalDateTime date;
    private double open;
    private double high;
    private double low;
    private double close;
    protected double vol;

    @Override
    public String toString() {
        return "date=" + date +
                ", open=" + Utils.num2(open) +
                ", high=" + Utils.num2(high) +
                ", low=" + Utils.num2(low) +
                ", close=" + Utils.num2(close) +
                ", vol=" + Utils.num2(vol);
    }

    public QuoteBar(LocalDateTime dateTime, double price) {
        this.date = dateTime;
        init(price, price, price, price, 0);
    }

    public QuoteBar(LocalDateTime dateTime, double open, double high, double low, double close) {
        this.date = dateTime;
        init(open, high, low, close, 0);
    }

    public QuoteBar(LocalDateTime dateTime, double open, double high, double low, double close, double vol) {
        this.date = dateTime;
        init(open, high, low, close, vol);
    }

    public QuoteBar(long epochMsUtc, double price) {
        this.date = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMsUtc), ZoneOffset.UTC);
        init(price, price, price, price, 0);
    }

    public QuoteBar(long epochMsUtc, double open, double high, double low, double close) {
        this.date = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMsUtc), ZoneOffset.UTC);
        init(open, high, low, close, 0);
    }

    public QuoteBar(long epochMsUtc, double open, double high, double low, double close, double vol) {
        this.date = Utils.epochDateTime(epochMsUtc);
        init(open, high, low, close, vol);
    }

    public void update(double price)  {
        update(price, 0);
    }

    public void update(double price, double vol)  {
        this.close = price;
        this.low = Math.min(this.low, price);
        this.high = Math.max(this.high, price);
        this.vol += vol;
    }

    public void update(QuoteBar bar)  {
        this.close = bar.close;
        this.low = Math.min(this.low, bar.low);
        this.high = Math.max(this.high, bar.high);
        this.vol += bar.vol;
    }

    private void init(double open, double high, double low, double close, double vol) {
        if (low > high) {
            throw new RuntimeException("low > high");
        }
        if (low > open) {
            throw new RuntimeException("low > open");
        }
        if (low > close) {
            throw new RuntimeException("low > close");
        }
        if (high < open) {
            throw new RuntimeException("high < open");
        }
        if (high < close) {
            throw new RuntimeException("high < close");
        }

        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.vol = vol;
    }

    public LocalDateTime date() {
        return date;
    }

    public double open() {
        return open;
    }

    public double close() {
        return close;
    }

    public double low() {
        return low;
    }

    public double high() {
        return high;
    }

    public double vol() {
        return vol;
    }
}
