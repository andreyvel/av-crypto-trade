package av.crypto.binance.dto;

import org.junit.Assert;
import org.junit.Test;
import av.crypto.common.Utils;
import av.crypto.common.dto.OrderDto;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class OrderLoaderTest {
    @Test
    public void orderPlaceTest() throws Exception {
        String fileName = "src/test/resources/orderPlace.json";
        Path filePath = Path.of(fileName);
        String content = Files.readString(filePath);

        OrderDto order = OrderLoader.loadOrderPlace(content);
        Assert.assertEquals(order.orderId(), 4721739);
        Assert.assertEquals("SELL", order.side());
        Assert.assertEquals("MARKET", order.type());
        Assert.assertEquals(order.quantity(), 0.01, Utils.DOUBLE_THRESHOLD);
        Assert.assertEquals(order.price(), 0, Utils.DOUBLE_THRESHOLD);
    }

    @Test
    public void executionReportTest() throws Exception {
        String fileName = "src/test/resources/listenUserStreamEr.json";
        Path filePath = Path.of(fileName);
        String content = Files.readString(filePath);

        OrderDto order = OrderLoader.loadExecutionReport(content);
        Assert.assertEquals(order.orderId(), 4981481);
        Assert.assertEquals("BUY", order.side());
        Assert.assertEquals("LIMIT", order.type());
        Assert.assertEquals(order.quantity(), 0.01d, Utils.DOUBLE_THRESHOLD);
        Assert.assertEquals(order.price(), 26788.47d, Utils.DOUBLE_THRESHOLD);
    }

    @Test
    public void ordersAllTest() throws Exception {
        String fileName = "src/test/resources/ordersAll.json";
        Path filePath = Path.of(fileName);
        String content = Files.readString(filePath);

        List<OrderDto> orders = OrderLoader.loadOrdersAll(content);
        Assert.assertTrue(orders.size() > 1);
    }

    @Test
    public void openOrdersStatusTest() throws Exception {
        String fileName = "src/test/resources/openOrdersStatus.json";
        Path filePath = Path.of(fileName);
        String content = Files.readString(filePath);

        List<OrderDto> orders = OrderLoader.loadOrdersAll(content);
        Assert.assertTrue(orders.size() > 1);
    }
}
