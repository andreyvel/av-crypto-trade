package av.crypto.common.dto;

import av.crypto.common.Utils;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;

public class AccountDtoTest {
    @Test
    public void serializeTest() {
        String accountType = "TEST";
        LocalDateTime updateTime = LocalDateTime.now();

        AccountDto accountDto = new AccountDto(accountType, updateTime);
        AccountDto.AccountBalance bal = new AccountDto.AccountBalance("USD", 1d, 2d);
        accountDto.addBalance(bal);

        String content = accountDto.serialize().toString();
        JSONObject rootObj = new JSONObject(content);

        AccountDto as2 = new AccountDto(rootObj);
        Assert.assertEquals(accountDto.accountType(), as2.accountType());
        Assert.assertEquals(accountDto.updated(), as2.updated());

        AccountDto.AccountBalance bal2 = as2.balances().get(bal.asset());
        Assert.assertEquals(bal.asset(), bal2.asset());
        Assert.assertEquals(bal.locked(), bal2.locked(), Utils.DOUBLE_THRESHOLD);
        Assert.assertEquals(bal.free(), bal2.free(), Utils.DOUBLE_THRESHOLD);
    }
}
