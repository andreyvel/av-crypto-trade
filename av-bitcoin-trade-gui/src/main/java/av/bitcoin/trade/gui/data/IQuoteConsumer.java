package av.bitcoin.trade.gui.data;

import org.trade.common.QuoteBar;
import org.trade.common.QuoteTick;

import java.util.Collection;

public interface IQuoteConsumer {
    String uuid();
    String symbol();
    String interval();
    int limit();
    void update(QuoteTick quoteTick);
    void update(Collection<QuoteBar> list);
}
