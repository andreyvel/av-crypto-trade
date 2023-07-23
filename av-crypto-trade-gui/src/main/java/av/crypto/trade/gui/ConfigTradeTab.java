package av.crypto.trade.gui;

public class ConfigTradeTab {
    private String caption;
    private String symbol;
    private String interval;
    private double orderQnt;

    public ConfigTradeTab(String caption, String symbol, String interval, double orderQnt) {
        this.caption = caption;
        this.symbol = symbol;
        this.interval = interval;
        this.orderQnt = orderQnt;
    }

    public String caption() {
        return caption;
    }
    public String symbol() {
        return symbol;
    }

    public String interval() {
        return interval;
    }

    public double orderQnt() {
        return orderQnt;
    }
}
