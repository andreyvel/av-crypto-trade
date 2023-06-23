package av.bitcoin.binance.dto;

import org.json.JSONArray;
import org.json.JSONObject;
import org.trade.common.Utils;
import org.trade.common.dto.AccountDto;
import org.trade.common.dto.AccountDto.AccountBalance;

public class AccountLoader {
    //"E": 1573200697110,           //Event Time
    //"a": "BTC",                   //Asset
    //"d": "100.00000000",          //Balance Delta
    //"T": 1573200697068            //Clear Time
    public static void changeBalance(AccountDto acc, JSONObject jsonEvent) {
        String asset = jsonEvent.optString("a");
        long updateTime = jsonEvent.optLong("T");
        double change = jsonEvent.optDouble("d");
        acc.changeBalance(asset, updateTime, change);
    }

    //"e": "outboundAccountPosition", //Event type
    //"E": 1564034571105,             //Event Time
    //"u": 1564034571073,             //Time of last account update
    //"B": [{                         //Balances Array
    //  "a": "ETH",                 //Asset
    //  "f": "10000.000000",        //Free
    //  "l": "0.000000"             //Locked
    //}]
    public static void updateAccount(AccountDto acc, JSONObject jsonEvent) {
        long updateTime = jsonEvent.optLong("u");

        JSONArray jsonArr = jsonEvent.optJSONArray("B");
        for(int ind = 0; ind < jsonArr.length(); ind++) {
            JSONObject jsonObj = jsonArr.getJSONObject(ind);
            double free = jsonObj.optDouble("f");
            double locked = jsonObj.optDouble("l");

            String asset = jsonObj.optString("a");
            acc.updateBalance(asset, updateTime, free, locked);
        }
    }

    public static AccountDto load(String content) {
        JSONObject jsonEvent = new JSONObject(content);
        return load(jsonEvent);
    }
    public static AccountDto load(JSONObject jsonEvent) {
        jsonEvent = jsonEvent.getJSONObject("result");

        long updated2 = jsonEvent.optLong("updateTime");
        String accountType = jsonEvent.optString("accountType");
        AccountDto acc = new AccountDto(accountType, Utils.epochDateTime(updated2));

        JSONArray balancesArr = jsonEvent.optJSONArray("balances");
        for(int ind = 0; ind < balancesArr.length(); ind++) {
            JSONObject balanceObj = balancesArr.getJSONObject(ind);

            String asset = balanceObj.optString("asset");
            double free = balanceObj.optDouble("free");;
            double locked = balanceObj.optDouble("locked");

            if (free != 0 || locked != 0){
                AccountBalance bal = new AccountBalance(asset, free, locked);
                acc.addBalance(bal);
            }
        }

        return acc;
    }
}
