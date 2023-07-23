package av.crypto.common.dto;

import av.crypto.common.QuoteBar;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class QuoteBarDtoTest {
    @Test
    public void serializeTest() throws Exception {
        List<QuoteBar> quotes = new ArrayList<>();
        LocalDateTime date = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        for(int ind = 0; ind < 10; ind+=1) {
            int value = 1000 + 100 * ind;
            QuoteBar bar = new QuoteBar(date, value, value + 2, value - 1, value + 2, value + 5);
            quotes.add(bar);
            date = date.plus(1, ChronoUnit.MINUTES);
        }

        String content = QuoteBarDto.serialize(quotes);
        List<QuoteBar> quotes2 = QuoteBarDto.deserialize(content);
        Assert.assertEquals(quotes.size(),quotes2.size());
    }
}
