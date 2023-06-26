package av.bitcoin.binance.dto;

import org.junit.Assert;
import org.junit.Test;
import av.bitcoin.common.QuoteBar;
import av.bitcoin.common.Utils;
import av.bitcoin.common.dto.QuoteBarDto;

import java.nio.file.Files;
import java.nio.file.Path;

public class TickerLoaderTest {
    @Test
    public void loaderTest() throws Exception {
        String content = """
                {"data":{"a":"27145.91000000","A":"8.02467000","b":"27145.90000000","B":"4.40313000",
                "c":"27145.90000000","C":1686081215919,"e":"24hrTicker","E":1686081215919,"F":3134720288,
                "h":"27216.89000000","l":"25351.02000000","L":3135994262,"n":1273975,"o":"25633.17000000",
                "O":1685994815919,"p":"1512.73000000","P":"5.901","Q":"0.00156000","q":"1615535756.11935950",
                "s":"BTCUSDT","v":"62058.46855000","w":"26032.47862647","x":"25633.16000000"},"stream":"btcusdt@ticker"}
                """;

        QuoteBarDto row = TickerLoader.load(content);
    }

    @Test
    public void tickerTest() throws Exception {
        String fileName = "src/test/resources/ticker.json";
        Path filePath = Path.of(fileName);
        String content = Files.readString(filePath);

        QuoteBarDto row = TickerLoader.load(content);
        Assert.assertEquals("BTCUSDT", row.symbol);

        QuoteBar quoteBar = row.quoteBar;
        Assert.assertEquals(27600.50d, row.quoteBar.open(), Utils.DOUBLE_THRESHOLD);
        Assert.assertEquals(27708.70d, quoteBar.close(), Utils.DOUBLE_THRESHOLD);
        Assert.assertEquals(26829.24d, quoteBar.low(), Utils.DOUBLE_THRESHOLD);
        Assert.assertEquals(29856.63d, quoteBar.high(), Utils.DOUBLE_THRESHOLD);
        Assert.assertEquals(4412.322084d, quoteBar.vol(), Utils.DOUBLE_THRESHOLD);
    }
}
