package av.crypto.common;

import org.json.JSONObject;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Map;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

import static java.time.temporal.ChronoUnit.MILLIS;

public class Utils {
    private static long pow10[] = null;
    public static final double DOUBLE_THRESHOLD = 0.00000001d;
    private static final NumberFormat num2 = new DecimalFormat("#0.00", DecimalFormatSymbols.getInstance(Locale.US));
    private static final NumberFormat num4 = new DecimalFormat("#0.0000", DecimalFormatSymbols.getInstance(Locale.US));
    private static final NumberFormat num8 = new DecimalFormat("#0.00000000", DecimalFormatSymbols.getInstance(Locale.US));
    private static final DateTimeFormatter frmDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter frmDateTimeISO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter frmDateTimeISOms = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    public static long delayMs(LocalDateTime first, LocalDateTime last) {
        long delayMs = MILLIS.between(first, last);
        return delayMs;
    }
    public static long delayMsUtc(long startMsUtc) {
        LocalDateTime startUtc = LocalDateTime.ofInstant(Instant.ofEpochMilli(startMsUtc), ZoneOffset.UTC);
        return delayMsUtc(startUtc);
    }
    public static long delayMsUtc(LocalDateTime startUtc) {
        LocalDateTime curUtc = LocalDateTime.now(ZoneOffset.UTC);

        long delayMs = MILLIS.between(startUtc, curUtc);
        return delayMs;
    }

    public static String combinePath(String path1, String path2) {
        Path folder = Paths.get(path1, path2);
        return folder.toString();
    }

    public static String getProperty(Map<String, Object> propertyMap, String  propertyName, Boolean required) {
        Object value = propertyMap.get(propertyName);
        if (required && value == null) {
            throw new RuntimeException("Property is not found: " + propertyName);
        }

        if (value == null) {
            return null;
        }

        return value.toString().trim();
    }

    public static String pullValue(JSONObject params, String key, boolean required) {
        Object value = params.opt(key);
        params.remove(key);

        if (required && value == null) {
            throw new RuntimeException("Required key=" + key + " is not found in: " + params);
        }
        return (value == null) ? null : value.toString();
    }

    public static void checkDouble(double value) {
        if (Double.isInfinite(value)) {
            throw new RuntimeException("Double.isInfinite");
        }
        if (value == Double.NaN) {
            throw new RuntimeException("Double.NaN");
        }
        if (value == Double.MIN_VALUE) {
            throw new RuntimeException("Double.MIN_VALUE");
        }
        if (value == Double.MAX_VALUE) {
            throw new RuntimeException("Double.MAX_VALUE");
        }
    }
    public static String num2(double value) {
        checkDouble(value);
        return num2.format(value);
    }
    public static String num4(double value) {
        checkDouble(value);
        return num4.format(value);
    }
    public static String num8(double value) {
        checkDouble(value);
        return num8.format(value);
    }

    public static boolean isZero(double value){
        return -DOUBLE_THRESHOLD <= value && value <= DOUBLE_THRESHOLD;
    }

    public static boolean isZeroOrNaN(double value){
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return true;
        }
        return -DOUBLE_THRESHOLD <= value && value <= DOUBLE_THRESHOLD;
    }

    public static boolean equals(double value1, double value2){
        double delta = value1 - value2;
        return -DOUBLE_THRESHOLD <= delta && delta <= DOUBLE_THRESHOLD;
    }

    public static String dateTime(LocalDateTime dateTime) {
        dateTime = dateTime.truncatedTo(ChronoUnit.SECONDS);
        String ret = frmDateTime.format(dateTime);
        return ret;
    }

    public static String dateTimeIso(LocalDateTime dateTime) {
        dateTime = dateTime.truncatedTo(ChronoUnit.SECONDS);
        String ret = frmDateTimeISO.format(dateTime);
        return ret;
    }

    public static String dateTimeIsoMs(LocalDateTime dateTime) {
        dateTime = dateTime.truncatedTo(ChronoUnit.MILLIS);
        String ret = frmDateTimeISOms.format(dateTime);
        return ret;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        if (pow10 == null) {
            long valuePow = 1;
            pow10 = new long[8];
            for(int ind = 0; ind < pow10.length; ind++) {
                pow10[ind] = valuePow;
                valuePow *= 10;
            }
        }

        long factor = pow10[places];
        long tmp = Math.round(value * factor);
        return (double) tmp / factor;
    }
    public static double roundRange(double value, double range) {
        if (range < 0) throw new IllegalArgumentException();
        if (range == 0) return value;

        double delta = (range / 2) * Math.signum(value);
        value = (int)((value + delta) / range);
        value = round(value * range, 4);
        return value;
    }
    public static double pct(double open, double close, double range) {
        double pct = pct(open, close);
        return roundRange(pct, range);
    }

    public static double pct(double open, double close) {
        if (open == 0 || close == 0) {
            return 0;
        }

        double val = 100 * (close - open) / open;
        return val;
    }

    public static boolean between(double value, double valueFrom, double valueTo) {
        return valueFrom <= value && value <= valueTo;
    }

    public static boolean between(LocalDate date, LocalDate dateFrom, LocalDate dateTo) {
        int res1 = date.compareTo(dateFrom);
        if (res1 < 0) return false;

        int res2 = date.compareTo(dateTo);
        return res2 <= 0;
    }

    public static boolean between(LocalDateTime date, LocalDateTime dateFrom, LocalDateTime dateTo) {
        int res1 = date.compareTo(dateFrom);
        if (res1 < 0) return false;

        int res2 = date.compareTo(dateTo);
        return res2 <= 0;
    }

    public static long deltaMs(LocalDateTime dateFrom, LocalDateTime dateTo) {
        long deltaMs = Duration.between(dateFrom, dateTo).toMillis();
        return deltaMs;
    }

    public static LocalDateTime epochDateTime(long epochMsUtc) {
        LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMsUtc), ZoneOffset.UTC);
        return date;
    }

    public static long ofEpochMilli(LocalDateTime date) {
        long epochMsUtc = date.toInstant(ZoneOffset.UTC).toEpochMilli();
        return epochMsUtc;
    }

    public static List<String> parseList(String content) {
        if (content == null) {
            return null;
        }

        List<String> listRet = new ArrayList<>();
        String[] data = content.split(",");

        for (String symbol: data) {
            listRet.add(symbol.trim());
        }

        return listRet;
    }

    public static LocalDateTime getDateTime(int dateYMD, int timeHMS) {
        int year = dateYMD / 10_000;
        int mon = (dateYMD / 100) % 100;
        int day = dateYMD % 100;

        if (year < 100)
        {
            year = 2000 + year;
        }

        int hh = timeHMS / 10_000;
        int mm = (timeHMS / 100) % 100;
        int ss = timeHMS % 100;

        LocalDateTime dat = LocalDateTime.of(year, mon, day, hh, mm, ss);
        return dat;
    }

    public static String format(double value, int decimalNum) {
        if (decimalNum <= 0) {
            throw new IllegalArgumentException("decimalNum=" + decimalNum + " <= 0");
        }

        int decimalLast = 0;
        String doubleValue = num8(value);

        int pointPos = doubleValue.indexOf('.');
        doubleValue = doubleValue.substring(pointPos + 1);
        char[] decimals = doubleValue.toCharArray();

        for(int pos = 0; pos < decimals.length; pos++) {
            if (decimals[pos] != '0') {
                decimalLast = pos;
            }
        }

        StringBuilder sb = new StringBuilder((int)value + ".");
        decimalNum = Math.max(decimalNum, decimalLast + 1);

        for(int pos = 0; pos < decimalNum; pos++) {
            sb.append(decimals[pos]);
        }
        return sb.toString();
    }
}

