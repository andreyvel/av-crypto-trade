package av.crypto.binance;

import org.junit.Test;

import java.io.FileNotFoundException;

public class TestAppConfig {
    @Test
    public void loadValuesTest() throws FileNotFoundException {
        AppConfig.loadValues("../av-crypto-binance.yaml");
    }
}
