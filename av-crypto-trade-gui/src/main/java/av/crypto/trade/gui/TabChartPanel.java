package av.crypto.trade.gui;

import av.crypto.trade.gui.chart.ChartManager;
import av.crypto.trade.gui.chart.ChartPanel;
import av.crypto.common.Enums;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.event.KeyEvent;

public class TabChartPanel extends JPanel implements ITabRefresh {
    private ChartPanel chartPanel;
    private OrderPanel orderPanel;

    private ConfigTradeTab configTradeTab;
    public TabChartPanel(ConfigTradeTab configTradeTab) {
        this.configTradeTab = configTradeTab;
        this.setLayout(new BorderLayout());

        orderPanel = new OrderPanel(configTradeTab.orderQnt());
        this.add(orderPanel, BorderLayout.NORTH);

        chartPanel = new ChartPanel(configTradeTab.symbol(), configTradeTab.interval());
        this.add(chartPanel, BorderLayout.CENTER);

        orderPanel.setChartManager(chartPanel.chartMan());
    }

    public String symbol() {
        return configTradeTab.symbol();
    }

    public ChartPanel chartPanel() {
        return chartPanel;
    }

    public OrderPanel orderPanel() {
        return orderPanel;
    }

    @Override
    public void refresh() {
        chartPanel.chartMan().dataSourceChanged();
    }

    public boolean dispatchKeyEvent(KeyEvent e) {
        if(e.getID() == KeyEvent.KEY_PRESSED && Character.isSpaceChar(e.getKeyChar())) {
            ChartManager chartMan = chartPanel.chartMan();
            double priceY = chartPanel.dragPriceY();

            if (priceY > 0) {
                String symbol = chartMan.quoteConsumer().symbol();
                priceY = AppMain.clientSession.roundPrice(priceY, symbol);

                if (priceY < chartMan.lastPrice()) {
                    double orderQnt = orderPanel.getBuyQnt();
                    AppMain.clientSession.limitOrder(symbol, Enums.OrderSide.BUY, orderQnt, priceY);
                } else {
                    double orderQnt = orderPanel.getSellQnt();
                    AppMain.clientSession.limitOrder(symbol, Enums.OrderSide.SELL, orderQnt, priceY);
                }
                return true;
            }
        }
        // allow the event to be redispatched
        return false;
    }

}
