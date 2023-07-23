package av.crypto.trade.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import av.crypto.common.Utils;

public class AppConfig {
    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);
    private static final int adviceTimeoutMs = 3_000;
    private static int refreshChartMs = 300;
    private static String restApiService;
    private static String tradeStreamSub;
    private static String tradeCommandPub;
    private static String tradeAdviceSub;
    private static final List<ConfigTradeTab> tradeTabs = new ArrayList<>();

    public static void loadValues(String configFileName) throws FileNotFoundException {
        String configFileNamePrivate = "." + configFileName;
        File file = new File(configFileNamePrivate);
        if (file.exists()) {
            configFileName = configFileNamePrivate;
        }

        Yaml yaml = new Yaml();
        log.warn("Loading config file: " + configFileName);

        Map<String, Object> propertyMap = yaml.load(new FileInputStream(configFileName));
        refreshChartMs = getPropertyInt(propertyMap, "refresh_chart_ms", true);
        restApiService = getPropertyString(propertyMap, "rest_api_service", true);
        tradeAdviceSub = getPropertyString(propertyMap, "trade_advice_sub", true);
        tradeStreamSub = getPropertyString(propertyMap, "trade_stream_sub", true);
        tradeCommandPub = getPropertyString(propertyMap, "trade_command_pub", true);

        ArrayList tradeTabs2 = (ArrayList)propertyMap.get("trade_tabs");
        for(Object propMap : tradeTabs2) {
            Map<String, Object> propMap2 = (Map<String, Object>)propMap;
            String caption = getPropertyString(propMap2, "caption", true);
            String symbol = getPropertyString(propMap2, "symbol", true);
            String interval = getPropertyString(propMap2, "interval", true);
            double order_qnt = getPropertyDouble(propMap2, "order_qnt", true);

            ConfigTradeTab tradeTab = new ConfigTradeTab(caption, symbol, interval, order_qnt);
            tradeTabs.add(tradeTab);
        }
    }

    private static String getPropertyString(Map<String, Object> propertyMap, String  propertyName, Boolean required) {
        String value = Utils.getProperty(propertyMap, propertyName, required);
        log.warn("{}: {}", propertyName, value);
        return value;
    }

    private static int getPropertyInt(Map<String, Object> propertyMap, String  propertyName, Boolean required) {
        String value2 = Utils.getProperty(propertyMap, propertyName, required);
        int value = Integer.parseInt(value2);
        log.warn("{}: {}", propertyName, value);
        return value;
    }

    private static double getPropertyDouble(Map<String, Object> propertyMap, String  propertyName, Boolean required) {
        String value2 = Utils.getProperty(propertyMap, propertyName, required);
        double value = Double.parseDouble(value2);
        log.warn("{}: {}", propertyName, value);
        return value;
    }

    public static String restService() {
        return restApiService;
    }

    public static List<ConfigTradeTab> tradeTabs() {
        return tradeTabs;
    }

    public static String tradeAdviceSub() {
        return tradeAdviceSub;
    }
    public static String tradeStreamSub() {
        return tradeStreamSub;
    }
    public static String tradeCommandPub() {
        return tradeCommandPub;
    }
    public static int refreshChartMs() {
        return refreshChartMs;
    }
    public static int adviceTimeoutMs() {
        return adviceTimeoutMs;
    }
}
