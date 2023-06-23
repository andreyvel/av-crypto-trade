package av.bitcoin.binance.dto;

import org.junit.Assert;
import org.junit.Test;
import org.trade.common.QuoteBar;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class KlinesLoaderTest {
    @Test
    public void klinesResponseTest() throws Exception {
        String fileName = "src/test/resources/klines.json";
        Path filePath = Path.of(fileName);
        String content = Files.readString(filePath);

        List<QuoteBar> quotes = KlinesLoader.load(content);
        Assert.assertTrue(quotes.size() > 10);

        for(QuoteBar bar : quotes) {
            Assert.assertTrue(bar.open() > 0);
            Assert.assertTrue(bar.close() > 0);
            Assert.assertTrue(bar.low() > 0);
            Assert.assertTrue(bar.high() > 0);
        }
    }
}
