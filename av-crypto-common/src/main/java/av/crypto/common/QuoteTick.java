package av.crypto.common;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class QuoteTick {
    private LocalDateTime createdLocal = LocalDateTime.now();
    private LocalDateTime createdUtc = LocalDateTime.now(ZoneOffset.UTC);
    private LocalDateTime date;
    private double price;
    private double qnt;

    @Override
    public String toString() {
        return "date=" + date + ", price=" + Utils.num2(price) + ", qnt=" + Utils.num2(qnt);
    }

    public QuoteTick(LocalDateTime dateTime, double price) {
        this.date = dateTime;
        this.price = price;
    }

    public QuoteTick(LocalDateTime dateTime, double price, double qnt) {
        this.date = dateTime;
        this.price = price;
        this.qnt = qnt;
    }

    public QuoteTick(long epochMsUtc, double price) {
        this.date = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMsUtc), ZoneOffset.UTC);
        this.price = price;
    }

    public QuoteTick(long epochMsUtc, double price, double qnt) {
        this.date = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMsUtc), ZoneOffset.UTC);
        this.price = price;
        this.qnt = qnt;
    }

    public void update(QuoteTick tickNew) {
        Long delayMs = Utils.delayMs(this.date, tickNew.date);
        if (delayMs != 0) {
            throw new RuntimeException("date=" +  this.date + " > tickNew=" + tickNew.date);
        }

        this.price = tickNew.price;
        this.qnt += tickNew.qnt;
    }

    public LocalDateTime date() {
        return date;
    }

    public double price() {
        return price;
    }

    public double qnt() {
        return qnt;
    }

    public LocalDateTime createdLocal() {
        return createdLocal;
    }
    public LocalDateTime createdUtc() {
        return createdUtc;
    }
}
