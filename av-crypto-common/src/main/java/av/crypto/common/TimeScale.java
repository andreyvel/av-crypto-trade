package av.crypto.common;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class TimeScale {
    private static final TemporalField fieldW1 = WeekFields.of(Locale.GERMANY).dayOfWeek();

    public static LocalDateTime truncateTo(LocalDateTime date, String scale) {
        String code = scale.substring(scale.length() - 1);
        String value2 = scale.substring(0, scale.length() - 1);
        int valueStep = Integer.parseInt(value2);

        if ("M".equals(code)) {
            if (valueStep != 1) {
                throw new RuntimeException("Bad format: " + scale);
            }

            LocalDateTime dateRes = date.truncatedTo(ChronoUnit.DAYS);
            dateRes = dateRes.minus(date.getDayOfMonth() - 1, ChronoUnit.DAYS);
            return dateRes;
        }
        else if ("w".equals(code)) {
            if (valueStep != 1) {
                throw new RuntimeException("Bad format: " + scale);
            }

            date = date.truncatedTo(ChronoUnit.DAYS);
            LocalDateTime dateRes = date.with(fieldW1, 1);
            return dateRes;
        }
        else if ("d".equals(code)) {
            if (valueStep != 1) {
                throw new RuntimeException("Bad format: " + scale);
            }

            LocalDateTime dateRes = date.truncatedTo(ChronoUnit.DAYS);
            return dateRes;
        }
        else if ("h".equals(code)) {
            valueStep *= 3600;
        }
        else if ("m".equals(code)) {
            valueStep *= 60;
        }
        else if ("s".equals(code)) {
            // no-op
        }
        else {
            throw new RuntimeException("Bad format: " + scale);
        }

        long timeSec = date.toEpochSecond(ZoneOffset.UTC);
        timeSec = valueStep * (int) (timeSec / valueStep);

        LocalDateTime dateRes = LocalDateTime.ofEpochSecond(timeSec, 0, ZoneOffset.UTC);
        return dateRes;
    }

    public static int totalSec(String scale) {
        String code = scale.substring(scale.length() - 1);
        String value2 = scale.substring(0, scale.length() - 1);
        int valueStep = Integer.parseInt(value2);

        if ("d".equals(code)) {
            return 24 * 3600 * valueStep;
        }
        else if ("h".equals(code)) {
            return 3600 * valueStep;
        }
        else if ("m".equals(code)) {
            return 60 * valueStep;
        }
        else if ("s".equals(code)) {
            return valueStep;
        }

        throw new RuntimeException("Bad format: " + scale);
    }
}
