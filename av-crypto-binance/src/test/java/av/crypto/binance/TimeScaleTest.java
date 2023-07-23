package av.crypto.binance;

import org.junit.Assert;
import org.junit.Test;
import av.crypto.common.TimeScale;
import java.time.LocalDateTime;

public class TimeScaleTest {
    @Test
    public void truncateTo()  {
        LocalDateTime date = LocalDateTime.parse("2023-01-02T11:22:33");
        LocalDateTime dateRet = TimeScale.truncateTo(date, "10s");
        Assert.assertEquals(LocalDateTime.parse("2023-01-02T11:22:30"), dateRet);

        dateRet = TimeScale.truncateTo(date, "15s");
        Assert.assertEquals(LocalDateTime.parse("2023-01-02T11:22:30"), dateRet);

        dateRet = TimeScale.truncateTo(date, "5m");
        Assert.assertEquals(LocalDateTime.parse("2023-01-02T11:20:00"), dateRet);

        dateRet = TimeScale.truncateTo(date, "60m");
        Assert.assertEquals(LocalDateTime.parse("2023-01-02T11:00:00"), dateRet);

        dateRet = TimeScale.truncateTo(date, "1h");
        Assert.assertEquals(LocalDateTime.parse("2023-01-02T11:00:00"), dateRet);

        dateRet = TimeScale.truncateTo(date, "2h");
        Assert.assertEquals(LocalDateTime.parse("2023-01-02T10:00:00"), dateRet);

        dateRet = TimeScale.truncateTo(date, "1d");
        Assert.assertEquals(LocalDateTime.parse("2023-01-02T00:00:00"), dateRet);

        dateRet = TimeScale.truncateTo(date, "1w");
        Assert.assertEquals(LocalDateTime.parse("2023-01-02T00:00:00"), dateRet);

        dateRet = TimeScale.truncateTo(date, "1M");
        Assert.assertEquals(LocalDateTime.parse("2023-01-01T00:00:00"), dateRet);
    }
}
