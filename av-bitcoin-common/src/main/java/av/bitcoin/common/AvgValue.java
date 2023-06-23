package av.bitcoin.common;

public class AvgValue {
    public double totalSum = 0;
    public int counter = 0;

    public void update(double value) {
        totalSum += value;
        counter++;
    }

    public void update(double value, int inc) {
        totalSum += value;
        counter += inc;
    }

    public void updateNz(double value) {
        if (!Double.isNaN(value) && value != 0) {
            totalSum += value;
            counter++;
        }
    }

    public void update(AvgValue item) {
        totalSum += item.totalSum;
        counter += item.counter;
    }

    public double avg() {
        if (counter == 0) {
            return 0;
        }
        return totalSum / counter;
    }

    public double totalSum() {
        return totalSum;
    }
}
