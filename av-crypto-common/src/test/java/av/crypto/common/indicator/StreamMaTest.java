package av.crypto.common.indicator;

import av.crypto.common.Utils;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StreamMaTest {
    @Test
    public void updateEqTest() {
        Sma ma = new Sma(5);

        for (int ind = 0; ind < 10; ind++) {
            double price = 42d;
            double valueMa = ma.update(price);
            Assert.assertEquals(price, valueMa, Utils.DOUBLE_THRESHOLD);
        }
    }

    @Test
    public void updateTest() {
        double price = 42d;
        Random rnd = new Random();

        List<Double> values = new ArrayList<>();
        for (int ind = 0; ind < 10; ind++) {
            values.add(price);
            price = price * (100.5d - rnd.nextDouble()) / 100d;
        }

        for (int interval = 1; interval < 2 * values.size(); interval++) {
            intervalTest(interval, values);
        }
    }

    private void intervalTest(int interval, List<Double> values) {
        Sma ma = new Sma(interval);
        for (int ind = 0; ind < values.size(); ind++) {
            double total = 0;
            int totalCnt = 0;

            for (int left = 0; left < interval; left++) {
                int ptr = ind - left;
                if (ptr >= 0) {
                    totalCnt++;
                    total += values.get(ptr);
                }
            }

            double valueOk = total / totalCnt;
            double valueMa = ma.update(values.get(ind));
            Assert.assertEquals(valueOk, valueMa, Utils.DOUBLE_THRESHOLD);
        }
    }
}
