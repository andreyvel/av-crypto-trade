package av.bitcoin.emulator;

import av.bitcoin.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AppConfig {
    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);
    private static int tradeRestApiPort;
    private static String restApiService;

    private static String zmqTradeAdvicePub;
    private static String zmqTradeStreamPub;
    private static String zmqTradeCommandSub;
    private static List<String> subscribeSymbols = new ArrayList<>();
    private static final List<String> subscribeIntervals = new ArrayList<>();

    public static List<String> subscribeIntervals() {
        return subscribeIntervals;
    }

    public static String zmqTradeAdvicePub() {
        return zmqTradeAdvicePub;
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
        restApiService = getPropertyString(propertyMap, "rest_api_service", true);
        zmqTradeAdvicePub = getPropertyString(propertyMap, "trade_advice_pub", true);
        zmqTradeStreamPub = getPropertyString(propertyMap, "trade_stream_pub", true);
        zmqTradeCommandSub = getPropertyString(propertyMap, "trade_command_sub", true);

        String subscribeSymbols2 = getPropertyString(propertyMap, "subscribe_symbols", true);
        subscribeSymbols.addAll(Utils.parseList(subscribeSymbols2));
        String subscribeIntervals2 = getPropertyString(propertyMap, "subscribe_intervals", true);
        subscribeIntervals.addAll(Utils.parseList(subscribeIntervals2));
    }

    private static String getPropertyString(Map<String, Object> propertyMap, String  propertyName, Boolean required) {
        String value = Utils.getProperty(propertyMap, propertyName, required);
        return value;
    }

    private static int getPropertyInt(Map<String, Object> propertyMap, String  propertyName, Boolean required) {
        String value2 = Utils.getProperty(propertyMap, propertyName, required);
        int value = Integer.parseInt(value2);
        log.warn("{}: {}", propertyName, value);
        return value;
    }

    public static List<String> subscribeSymbols() {
        return subscribeSymbols;
    }

    public static int tradeRestApiPort() {
        return tradeRestApiPort;
    }

    public static String restApiService() {
        return restApiService;
    }

    public static String zmqTradeStreamPub() {
        return zmqTradeStreamPub;
    }

    public static String zmqTradeCommandSub() {
        return zmqTradeCommandSub;
    }
}
