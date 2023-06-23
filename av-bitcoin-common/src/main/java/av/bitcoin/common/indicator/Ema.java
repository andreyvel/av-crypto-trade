package av.bitcoin.common.indicator;

public class Ema {
    private int interval;
    private double alpha;
    private double lastEma = Double.NaN;

    public Ema(int interval) {
        this.interval = interval;
        this.alpha = 1.0d / (interval + 1);
    }

    public int interval() {
        return interval;
    }

    public double update(double price) {
        if (Double.isNaN(lastEma)) {
            lastEma = price;
        }

        lastEma = (1 - alpha) * lastEma + alpha * price;
        return lastEma;
    }
}
