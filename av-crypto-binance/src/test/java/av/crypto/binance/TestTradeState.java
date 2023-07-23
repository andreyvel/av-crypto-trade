package av.crypto.binance;

import org.junit.Test;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TestTradeState {
    @Test
    public void testUpdateCombineStreams() throws IOException {
        String fileName = "./src/test/resources/combineStreams.json";
        WsStreamBinance wsStreamBinance = new WsStreamBinance(null, null);

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                wsStreamBinance.combineStreamProcessor(line);
            }
        }
    }
}
