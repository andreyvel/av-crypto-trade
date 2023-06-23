package av.bitcoin.common;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class UtilsTest {
    @Test
    public void formatTest() {
        double value = 9900.49552;
        String valueFrm = Utils.format(value, 4);
        Assert.assertEquals("9900.49552", valueFrm);

        Assert.assertEquals("1.2", Utils.format(1.20000d, 1));
        Assert.assertEquals("1.23", Utils.format(1.23000d, 2));
        Assert.assertEquals("1.234", Utils.format(1.23400d, 3));
        Assert.assertEquals("1.2345", Utils.format(1.23450d, 4));

        Assert.assertEquals("1.23", Utils.format(1.2300d, 1));
        Assert.assertEquals("1.23", Utils.format(1.2300d, 2));
        Assert.assertEquals("1.230", Utils.format(1.2300d, 3));
        Assert.assertEquals("1.2300", Utils.format(1.2300d, 4));
        Assert.assertEquals("1.23000", Utils.format(1.2300d, 5));
    }

    @Test
    public void pctTest() {
        double val = Utils.pct(100, 110);
        Assert.assertEquals(10d, val, Utils.DOUBLE_THRESHOLD);

        val = Utils.pct(100, 90);
        Assert.assertEquals(-10d, val, Utils.DOUBLE_THRESHOLD);
    }

    @Test
    public void roundTest() {
        double val = Utils.round(10.123456d, 3);
        Assert.assertEquals(10.123d, val, Utils.DOUBLE_THRESHOLD);

        val = Utils.round(10.12345d, 4);
        Assert.assertEquals(10.1235d, val, Utils.DOUBLE_THRESHOLD);
    }

    @Test
    public void dateUtcTest() {
        LocalDateTime dateNow = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        long epochMsUtc = Utils.ofEpochMilli(dateNow);
        LocalDateTime dateNow2 = Utils.epochDateTime(epochMsUtc);
        Assert.assertEquals(dateNow, dateNow2);
   }
}
