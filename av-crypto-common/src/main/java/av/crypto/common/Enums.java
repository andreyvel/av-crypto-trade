package av.crypto.common;

public class Enums {
    public enum ValueUnit {ABS, PCT}
    public enum OrderSide {
        BUY, SELL;

        public static boolean equals(String side0, OrderSide side1) {
            return side1.toString().equals(side0);
        }
    }
    public enum OrderType {
        LIMIT, MARKET;

        public static boolean equals(String orderType0, OrderType orderType1) {
            return orderType1.toString().equals(orderType0);
        }
    }
    public enum OrderStatus {
        NEW, FILLED, CANCELED;

        public static boolean equals(String status0, OrderStatus status1) {
            return status1.toString().equals(status0);
        }
    }
}
