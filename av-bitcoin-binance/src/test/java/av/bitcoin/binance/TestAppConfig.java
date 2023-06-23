package av.bitcoin.binance;

import org.junit.Test;

import java.io.FileNotFoundException;

public class TestAppConfig {
    @Test
    public void loadValuesTest() throws FileNotFoundException {
        AppConfig.loadValues("../av-bitcoin-binance.yaml");
    }
}
