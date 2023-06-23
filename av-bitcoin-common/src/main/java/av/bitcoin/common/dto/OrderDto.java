package av.bitcoin.common.dto;

import org.json.JSONObject;
import av.bitcoin.common.Utils;

import java.time.LocalDateTime;

public class OrderDto {
    private String clientOrderId;
    private long orderId;
    private long created;
    private long updated;
    private String symbol;
    private String side; // BUY or SELL
    private String type; // LIMIT or MARKET
    private String status; // FILLED
    private double price;
    private double quantity;
    private double executedQty;
    private double commission;

    public OrderDto(JSONObject jsonObj) {
        clientOrderId = jsonObj.optString("clientOrderId");
        orderId = jsonObj.optLong("orderId");
        created = jsonObj.optLong("created");
        updated = jsonObj.optLong("updated");

        symbol = jsonObj.optString("symbol");
        side = jsonObj.optString("side");
        type = jsonObj.optString("type");
        status = jsonObj.optString("status");

        price = jsonObj.optDouble("price");
        quantity = jsonObj.optDouble("quantity");
        commission = jsonObj.optDouble("commission");
        executedQty = jsonObj.optDouble("executedQty");
    }

    public JSONObject serialize() {
        JSONObject obj = new JSONObject();
        obj.put("orderId", orderId);
        obj.put("created", created);
        obj.put("updated", updated);

        obj.put("symbol", symbol);
        obj.put("side", side);
        obj.put("type", type);
        obj.put("status", status);
        obj.put("clientOrderId", clientOrderId);

        obj.put("price", price);
        obj.put("quantity", quantity);
        obj.put("commission", commission);
        obj.put("executedQty", executedQty);
        return obj;
    }

    public OrderDto(long orderId, String symbol, String side, String type, double price, double qnt) {
        this.orderId = orderId;
        this.symbol = symbol;
        this.side = side;
        this.type = type;
        this.price = price;
        this.quantity = qnt;
    }

    public void update(OrderDto orderNew) {
        if (orderNew.clientOrderId != null) {
            this.clientOrderId = orderNew.clientOrderId;
        }
        if (orderNew.orderId != 0l) {
            this.orderId = orderNew.orderId;
        }
        if (orderNew.created != 0l) {
            this.created = orderNew.created;
        }
        if (orderNew.updated != 0l) {
            this.updated = orderNew.updated;
        }

        if (orderNew.symbol != null) {
            this.symbol = orderNew.symbol;
        }
        if (orderNew.type != null) {
            this.type = orderNew.type;
        }
        if (orderNew.side != null) {
            this.side = orderNew.side;
        }
        if (orderNew.status != null) {
            this.status = orderNew.status;
        }


        if (orderNew.price > 0) {
            this.price = orderNew.price;
        }
        if (orderNew.quantity > 0) {
            this.quantity = orderNew.quantity;
        }
        if (orderNew.executedQty > 0) {
            this.executedQty = orderNew.executedQty;
        }
        if (orderNew.commission > 0) {
            this.commission = orderNew.commission;
        }
    }

    public long orderId() {
        return orderId;
    }

    public long created() {
        return created;
    }

    public void created(long created) {
        this.created = created;
    }
    public LocalDateTime createdUtc() {
        LocalDateTime created2 = Utils.epochDateTime(created);
        return created2;
    }

    public long updated() {
        return updated;
    }
    public void updated(long updated) {
        this.updated = updated;
    }

    public LocalDateTime updatedUtc() {
        LocalDateTime updated2 = Utils.epochDateTime(updated);
        return updated2;
    }

    public String symbol() {
        return symbol;
    }

    public String side() {
        return side;
    }

    public String type() {
        return type;
    }

    public String status() {
        return status;
    }

    public void status(String status) {
        this.status = status;
    }

    public String clientOrderId() {
        return clientOrderId;
    }

    public void clientOrderId(String clientOrderId) {
        this.clientOrderId = clientOrderId;
    }

    public double price() {
        return price;
    }

    public void price(double price) {
        this.price = price;
    }

    public double quantity() {
        return quantity;
    }

    public double executedQty() {
        return executedQty;
    }

    public void executedQty(double executedQty) {
        this.executedQty = executedQty;
    }

    public double commission() {
        return commission;
    }

    public void commission(double commission) {
        this.commission = commission;
    }
}
