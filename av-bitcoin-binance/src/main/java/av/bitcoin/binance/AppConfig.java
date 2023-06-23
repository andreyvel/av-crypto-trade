package av.bitcoin.binance;

import com.binance.connector.client.utils.signaturegenerator.HmacSignatureGenerator;
import com.binance.connector.client.utils.signaturegenerator.RsaSignatureGenerator;
import com.binance.connector.client.utils.signaturegenerator.SignatureGenerator;
import org.trade.common.Utils;
import org.yaml.snakeyaml.Yaml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.*;

public class AppConfig {
    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);
    private static int tradeRestApiPort;
    private static String zmqTradeStreamPub;
    private static String zmqTradeStreamSub;
    private static String apiKey;
    private static String wsApiUrl;
    private static String wsStreamUrl;
    private static SignatureGenerator signatureGenerator = null;
    private static List<String> subscribeSymbols = new ArrayList<>();
    private static List<KlineInfo> loadOnStartupKlines = new ArrayList<>();
    private static final List<String> subscribeIntervals = new ArrayList<>();
    private static final int slowlyResponceAlertMs = 1500;

    public static int slowlyResponceAlertMs() {
        return slowlyResponceAlertMs;
    }
    public static List<String> subscribeIntervals() {
        return subscribeIntervals;
    }

    public static List<KlineInfo> loadOnStartupKlines() {
        return loadOnStartupKlines;
    }

    public static void loadValues(String configFileName) throws FileNotFoundException {
        String configFileNamePrivate = "." + configFileName;
        File file = new File(configFileNamePrivate);
        if (file.exists()) {
            configFileName = configFileNamePrivate;
        }

        Yaml yaml = new Yaml();
        log.warn("Loading config file: " + configFileName);
        Map<String, Object> propertyMap = yaml.load(new FileInputStream(configFileName));

        tradeRestApiPort = getPropertyInt(propertyMap, "trade_rest_api_port", true);
        zmqTradeStreamPub = getPropertyString(propertyMap, "trade_stream_pub", true);
        zmqTradeStreamSub = getPropertyString(propertyMap, "trade_stream_sub", true);

        apiKey = getPropertyString(propertyMap, "api_key", true);
        wsApiUrl = getPropertyString(propertyMap, "ws_api_url", true);
        wsStreamUrl = getPropertyString(propertyMap, "ws_stream_url", true);

        String hmacPrivateKey = Utils.getProperty(propertyMap, "hmac_private_key", false);
        if (hmacPrivateKey != null) {
            signatureGenerator = new HmacSignatureGenerator(hmacPrivateKey);
        }

        String rsaPrivateFile = Utils.getProperty(propertyMap, "rsa_private_file", false);
        if (rsaPrivateFile != null) {
            signatureGenerator = new RsaSignatureGenerator(rsaPrivateFile);
        }

        if (rsaPrivateFile != null && hmacPrivateKey != null) {
            throw new RuntimeException("Property hmac_private_key and rsa_private_file has values, select one");
        }
        if (signatureGenerator == null) {
            throw new RuntimeException("Property rsa_private_file or hmac_private_key is empty");
        }


        String subscribeSymbols2 = getPropertyString(propertyMap, "subscribe_symbols", true);
        subscribeSymbols.addAll(Utils.parseList(subscribeSymbols2));
        String subscribeIntervals2 = getPropertyString(propertyMap, "subscribe_intervals", true);
        subscribeIntervals.addAll(Utils.parseList(subscribeIntervals2));
        int subscribeIntervalNum = getPropertyInt(propertyMap, "subscribe_interval_num", true);

        for (String symbol : subscribeSymbols) {
            for(String interval : subscribeIntervals) {
                loadOnStartupKlines.add(new KlineInfo(symbol, interval, subscribeIntervalNum));
            }
        }
    }

    private static String getPropertyString(Map<String, Object> propertyMap, String  propertyName, Boolean required) {
        String value = Utils.getProperty(propertyMap, propertyName, required);

        if ("api_key".equals(propertyName)) {
            log.warn("{}: ********", propertyName);
        } else {
            log.warn("{}: {}", propertyName, value);
        }
        return value;
    }

    private static int getPropertyInt(Map<String, Object> propertyMap, String  propertyName, Boolean required) {
        String value2 = Utils.getProperty(propertyMap, propertyName, required);
        int value = Integer.parseInt(value2);
        log.warn("{}: {}", propertyName, value);
        return value;
    }

    public static String wsApiUrl() {
        return wsApiUrl;
    }

    public static String getApiKey() {
        return apiKey;
    }

    public static SignatureGenerator getSignatureGenerator() {
        return signatureGenerator;
    }

    public static String wsStreamUrl() {
        return wsStreamUrl;
    }
    public static List<String> subscribeSymbols() {
        return subscribeSymbols;
    }

    public static int tradeRestApiPort() {
        return tradeRestApiPort;
    }

    public static String zmqTradeStreamPub() {
        return zmqTradeStreamPub;
    }

    public static String zmqTradeStreamSub() {
        return zmqTradeStreamSub;
    }

    public static class KlineInfo {
        public final String symbol;
        public final String interval;
        public final int barNum;

        public KlineInfo(String symbol, String interval, int barNum) {
            this.symbol = symbol;
            this.interval = interval;
            this.barNum = barNum;
        }
    }
}
