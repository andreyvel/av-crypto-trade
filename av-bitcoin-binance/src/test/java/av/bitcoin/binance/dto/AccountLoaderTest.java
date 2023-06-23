package av.bitcoin.binance.dto;

import org.junit.Assert;
import org.junit.Test;
import org.trade.common.dto.AccountDto;
import org.trade.common.dto.AccountDto.AccountBalance;

import java.nio.file.Files;
import java.nio.file.Path;

public class AccountLoaderTest {
    @Test
    public void accountStatusTest() throws Exception {
        String fileName = "src/test/resources/accountStatus.json";
        Path filePath = Path.of(fileName);
        String content = Files.readString(filePath);

        AccountDto acc = AccountLoader.load(content);

        Assert.assertTrue(acc.balances().size() > 1);
        Assert.assertEquals("SPOT", acc.accountType());

        AccountBalance bal = acc.balances().get("BTC");
        Assert.assertTrue(bal.free() > 0);
    }
}
