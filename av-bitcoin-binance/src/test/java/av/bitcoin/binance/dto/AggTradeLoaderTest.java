package av.bitcoin.binance.dto;

import org.junit.Assert;
import org.junit.Test;
import av.bitcoin.common.Utils;
import av.bitcoin.common.dto.QuoteTickDto;

import java.nio.file.Files;
import java.nio.file.Path;

public class AggTradeLoaderTest {
    @Test
    public void aggTradeTest() throws Exception {
        String fileName = "src/test/resources/aggTrade.json";
        Path filePath = Path.of(fileName);
        String content = Files.readString(filePath);

        QuoteTickDto row = AggTradeLoader.load(content);
        Assert.assertEquals("BTCUSDT", row.symbol);

        Assert.assertEquals(27708.70d, row.quoteTick.price(), Utils.DOUBLE_THRESHOLD);
        Assert.assertEquals(0.0010d, row.quoteTick.qnt(), Utils.DOUBLE_THRESHOLD);
        Assert.assertTrue(row.quoteTick.qnt() > 0);
    }
}
