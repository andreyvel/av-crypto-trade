package av.bitcoin.binance;

import av.bitcoin.common.NanoTimer;
import org.json.JSONObject;
import org.junit.Test;

public class TestUnit {
    @Test
    public void testJSONObject() {
        int testNum = 10_000;
        String json = "{\"id\":\"5873e3b3-ebbc-4bec-b521-7c396e8f83bf\",\"status\":200," +
                "\"result\":{\"serverTime\":1682704640798},\"rateLimits\":[{\"rateLimitType\":\"REQUEST_WEIGHT\"," +
                "\"interval\":\"MINUTE\",\"intervalNum\":1,\"limit\":1200,\"count\":4}]}";

        for (int num = 0; num < 2; num++) {
            // OperationNum=100000, TotalMs=625, PerfSec=160000
            try (NanoTimer nt = new NanoTimer("JSONObject#1", testNum)) {
                for (int ind = 0; ind < testNum; ind++) {
                    JSONObject jsonObj = new JSONObject(json);
                }
            }

            // OperationNum=100_000, TotalMs=620, PerfSec=161_290
            try (NanoTimer nt = new NanoTimer("JSONObject#2", testNum)) {
                for (int ind = 0; ind < testNum; ind++) {
                    JSONObject jsonObj = new JSONObject(json);
                    jsonObj = jsonObj.getJSONObject("result");
                }
            }


            // OperationNum=100_000, TotalMs=628, PerfSec=159_235
            try (NanoTimer nt = new NanoTimer("JSONObject#3", testNum)) {
                for (int ind = 0; ind < testNum; ind++) {
                    JSONObject jsonObj = new JSONObject(json);
                    jsonObj = jsonObj.getJSONObject("result");
                    long val = jsonObj.getLong("serverTime");
                }
            }

            // OperationNum=100_000, TotalMs=715, PerfSec=139_860
            try (NanoTimer nt = new NanoTimer("JSONObject#4", testNum)) {
                for (int ind = 0; ind < testNum; ind++) {
                    JSONObject jsonObj = new JSONObject(json);
                    String str = jsonObj.toString();
                }
            }
        }
    }
}
