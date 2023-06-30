package av.bitcoin.common.dto;

import org.json.JSONArray;
import org.json.JSONObject;
import av.bitcoin.common.Utils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;

public class AccountDto {
    private Map<String, AccountBalance> balances = new TreeMap<>();
    private LocalDateTime created = LocalDateTime.now();
    private LocalDateTime updated;
    private String accountType;

    public String accountType() {
        return accountType;
    }

    public LocalDateTime updated() {
        return updated;
    }

    public LocalDateTime created() {
        return created;
    }

    public void addBalance(AccountBalance bal) {
        this.balances.put(bal.asset, bal);
    }

    public AccountDto() {
        this.updated = LocalDateTime.now();
    }

    public AccountDto(String accountType, LocalDateTime updateTime) {
        this.accountType = accountType;
        this.updated = updateTime;
    }

    public AccountDto(JSONObject obj) {
        deserialize(obj);
    }

    public Map<String, AccountBalance> balances() {
        return balances;
    }

    public void changeBalance(String asset, long updateTime, double delta) {
        this.updated = Utils.epochDateTime(updateTime);
        AccountBalance bal = this.getOrCreateBalance(asset);
        bal.update(bal.free + delta, bal.locked);
    }

    public void updateBalance(String asset, long updateTime, double free, double locked) {
        this.updated = Utils.epochDateTime(updateTime);
        AccountBalance bal = this.getOrCreateBalance(asset);
        bal.update(free, locked);
    }

    private AccountBalance getOrCreateBalance(String asset) {
        AccountBalance bal = balances.get(asset);
        if (bal == null) {
            bal = new AccountBalance(asset);
            balances.put(asset, bal);
        }
        return bal;
    }

    public JSONObject serialize() {
        JSONObject rootObj = new JSONObject();
        serialize(rootObj);
        return rootObj;
    }

    private void serialize(JSONObject rootObj) {
        rootObj.put("accountType", accountType);
        rootObj.put("created", created);
        rootObj.put("updated", updated);

        JSONArray arr = new JSONArray();
        for(AccountBalance bal : balances.values()) {
            JSONObject balObj = new JSONObject();
            bal.serialize(balObj);
            arr.put(balObj);
        }
        rootObj.put("balances", arr);
    }

    public void deserialize(JSONObject rootObj) {
        accountType = rootObj.optString("accountType");

        String updated2 = rootObj.optString("updated");
        updated = LocalDateTime.parse(updated2);

        String created2 = rootObj.optString("created");
        created = LocalDateTime.parse(created2);

        JSONArray arr = rootObj.getJSONArray("balances");
        for(int ind = 0; ind < arr.length(); ind++) {
            JSONObject balObj = arr.getJSONObject(ind);
            AccountBalance bal = new AccountBalance(balObj);
            this.addBalance(bal);
        }
    }

    public static class AccountBalance {
        private String asset;
        private double free;
        private double locked;

        public AccountBalance(String asset) {
            this.asset = asset;
        }

        public AccountBalance(String asset, double free, double locked) {
            this.asset = asset;
            this.free = free;
            this.locked = locked;
        }

        public AccountBalance(JSONObject obj) {
            asset = obj.optString("asset");
            free = obj.optDouble("free");
            locked = obj.optDouble("locked");
        }

        public String asset() {
            return asset;
        }

        public double free() {
            return free;
        }

        public void freeAddCheck(double freeAdd) {
            if (this.free + freeAdd < 0) {
                throw new RuntimeException("Too low balance for " + asset
                        + ": free=" + Utils.format(free, 4)
                        + ", locked=" + Utils.format(locked, 4)
                        + ", freeAdd=" + Utils.format(freeAdd, 4));
            }
        }

        public void freeAdd(double valueAdd) {
            freeAddCheck(valueAdd);
            this.free += valueAdd;
        }

        public double locked() {
            return locked;
        }

        public void lockAddCheck(double lockAdd) {
            freeAddCheck(-lockAdd);
            if (this.locked + lockAdd < 0) {
                throw new RuntimeException("Too low balance for " + asset
                        + ": free=" + Utils.format(free, 4)
                        + ", locked=" + Utils.format(locked, 4)
                        + ", lockAdd=" + Utils.format(lockAdd, 4));
            }
        }

        public void lockAdd(double value) {
            lockAddCheck(value);
            this.locked += value;
            this.free -= value;
        }

        public void serialize(JSONObject obj) {
            obj.put("asset", asset);
            obj.put("free", free);
            obj.put("locked", locked);
        }

        public void update(double free, double locked) {
            this.free = free;
            this.locked = locked;
        }
    }
}
