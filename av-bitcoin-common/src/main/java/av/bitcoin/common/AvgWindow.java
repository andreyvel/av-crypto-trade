package av.bitcoin.common;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AvgWindow {
    private int windowSec;
    private Map<LocalDateTime, AvgValue> values = new HashMap<>();

    public AvgWindow(int windowSec) {
        if (windowSec <= 0) {
            throw new IllegalArgumentException("Bad value windowSec=" + windowSec);
        }

        this.windowSec = windowSec;
    }

    public double update(LocalDateTime dateNow, double value) {
        dateNow = dateNow.truncatedTo(ChronoUnit.SECONDS);

        AvgValue secAvg = values.get(dateNow);
        if (secAvg == null) {
            secAvg = new AvgValue();
            values.put(dateNow, secAvg);
        }
        secAvg.update(value);

        AvgValue retAvg = new AvgValue();
        LocalDateTime dateFrom = dateNow.minusSeconds(windowSec);

        List<LocalDateTime> dates = new ArrayList<>(values.keySet());
        for(LocalDateTime dateKey : dates) {
            if (dateKey.isBefore(dateFrom)) {
                values.remove(dateKey);
            } else {
                retAvg.update(values.get(dateKey));
            }
        }

        return retAvg.avg();
    }
}
