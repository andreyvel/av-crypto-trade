package av.crypto.common.dto;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class OrderDtoDtoTest {
    @Test
    public void serializeTest() throws Exception {
        OrderDto order = new OrderDto(1, "2", "3", "4", 5d, 6d);

        order.status("7");
        order.clientOrderId("8");
        order.executedQty(9d);
        order.commission(10d);
        order.created(11);
        order.updated(12);

        JSONObject rootObj = order.serialize();
        String content = rootObj.toString();

        JSONObject rootObj2 = new JSONObject(content);
        OrderDto order2 = new OrderDto(rootObj2);

        JSONObject rootObj3 = order2.serialize();
        String content3 = rootObj.toString();
        Assert.assertEquals(content, content3);
    }
}
