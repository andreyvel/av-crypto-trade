package av.crypto.trade.gui.data;

import av.crypto.common.QuoteBar;
import av.crypto.common.QuoteTick;

import java.util.Collection;

public interface IQuoteConsumer {
    String uuid();
    String symbol();
    String interval();
    int limit();
    void update(QuoteTick quoteTick);
    void update(Collection<QuoteBar> list);
}
