package av.bitcoin.common;

public class StatBar {
    private long count;
    private double min = Double.MAX_VALUE;
    private double max = -Double.MAX_VALUE;

    public long count() {
        return count;
    }

    public void update(QuoteBar bar) {
        update(bar.low(), bar.high());
    }

    public void update(double low, double high) {
        min = Math.min(min, low);
        max = Math.max(max, high);
        count++;
    }

    public double hwm(double value) {
        if (count == 0) {
            return 0.5d;
        }

        return (value - min) / (max - min);
    }

    public double min() {
        return min;
    }

    public double max() {
        return max;
    }

    public double minPct(double value) {
        return Utils.pct(value, min);
    }

    public double maxPct(double value) {
        return Utils.pct(value, max);
    }
}
