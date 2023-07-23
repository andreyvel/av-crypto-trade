package av.crypto.binance.http;

import com.sun.net.httpserver.HttpExchange;
import av.crypto.binance.TradeSession;
import av.crypto.common.dto.AccountDto;
import av.crypto.common.dto.ErrorDto;
import av.crypto.common.httpserver.HttpHandlerEx;

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
