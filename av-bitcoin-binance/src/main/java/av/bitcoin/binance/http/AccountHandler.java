package av.bitcoin.binance.http;

import com.sun.net.httpserver.HttpExchange;
import av.bitcoin.binance.TradeSession;
import org.trade.common.dto.AccountDto;
import org.trade.common.dto.ErrorDto;
import org.trade.common.httpserver.HttpHandlerEx;

public class AccountHandler extends HttpHandlerEx {
    @Override
    public String getResponseBody(HttpExchange req) {
        this.mediaType = HttpHandlerEx.MediaType.APPLICATION_JSON;

        AccountDto accountStatus = TradeSession.accountStatus;
        if (accountStatus == null) {
            return ErrorDto.serialize("accountStatus == null");
        }
        return accountStatus.serialize().toString();
    }
}
