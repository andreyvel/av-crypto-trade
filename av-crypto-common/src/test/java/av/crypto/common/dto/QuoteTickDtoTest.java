package av.crypto.common.dto;

import av.crypto.common.Utils;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;

public class QuoteTickDtoTest {
    @Test
    public void serializeTest() {
        LocalDateTime dateNow = LocalDateTime.now();
        String symbol = "USD";
        double price = 100d;
        double qnt = 42d;

        QuoteTickDto tick = new QuoteTickDto(symbol, Utils.ofEpochMilli(dateNow), price, qnt);
        JSONObject tickObj = tick.serialize();
        String content = tickObj.toString();

        JSONObject rootObj = new JSONObject(content);
        QuoteTickDto tick2 = new QuoteTickDto(rootObj);

        Assert.assertEquals(tick.symbol, tick2.symbol);
        Assert.assertEquals(tick.quoteTick.date(), tick2.quoteTick.date());
        Assert.assertEquals(tick.quoteTick.qnt(), tick2.quoteTick.qnt(), Utils.DOUBLE_THRESHOLD);
        Assert.assertEquals(tick.quoteTick.price(), tick2.quoteTick.price(), Utils.DOUBLE_THRESHOLD);
    }
}
