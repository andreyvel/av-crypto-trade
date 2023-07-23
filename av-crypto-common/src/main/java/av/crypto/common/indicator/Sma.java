package av.crypto.common.indicator;

public class Sma {
    private int interval;
    private int totalCnt = 0;
    private double total = 0;
    private double[] values;

    public Sma(int interval) {
        this.values = new double[interval];
        this.interval = interval;
    }

    public int interval() {
        return interval;
    }

    public double update(double price) {
        int ptr = totalCnt % interval;
        double prev = values[ptr];
        values[ptr] = price;

        total += price;
        totalCnt++;

        if (totalCnt <= interval) {
            return total / totalCnt;
        }

        total -= prev;
        return total / interval;
    }
}
