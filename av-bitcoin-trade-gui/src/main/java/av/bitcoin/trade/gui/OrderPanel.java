package av.bitcoin.trade.gui;

import av.bitcoin.trade.gui.chart.ChartManager;
import av.bitcoin.trade.gui.chart.Colors;
import av.bitcoin.common.AvgWindow;
import av.bitcoin.common.Enums;
import av.bitcoin.common.QuoteTick;
import av.bitcoin.common.dto.OrderDto;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.*;
import java.time.LocalDateTime;

import static java.time.temporal.ChronoUnit.MILLIS;

public class OrderPanel extends JPanel {
    private NumericField numBuyQnt = new NumericField(0, 4);
    private NumericField numSellQnt = new NumericField(0, 4);
    private NumericField numBuyTotal = new NumericField(0, 4);
    private NumericField numSellTotal = new NumericField(0, 4);
    private NumericField numBuyPrice = new NumericField(0, 2);
    private NumericField numSellPrice = new NumericField(0, 2);
    private NumericField numDelayMs = new NumericField(0, 0);
    private JCheckBox checkBuyMarket = new JCheckBox("market");
    private JCheckBox checkSellMarket = new JCheckBox("market");
    private ChartManager chartManager = null;
    private AvgWindow delayMsAgg = new AvgWindow(3);

    public OrderPanel(double orderQnt) throws HeadlessException {
        this.setLayout(null);
        this.setPreferredSize(new Dimension(200, 55));
        addComponentsAndLocate();

        setBuyQnt(orderQnt);
        setSellQnt(orderQnt);

        checkBuyMarket.addActionListener(e -> {
            numBuyPrice.setEditable(!checkBuyMarket.isSelected());
        });
        checkSellMarket.addActionListener(e -> {
            numSellPrice.setEditable(!checkSellMarket.isSelected());
        });

        checkBuyMarket.setSelected(true);
        checkSellMarket.setSelected(true);

        numBuyPrice.setEditable(!checkBuyMarket.isSelected());
        numSellPrice.setEditable(!checkSellMarket.isSelected());

        numBuyTotal.setEditable(false);
        numSellTotal.setEditable(false);
        numDelayMs.setEditable(false);
    }

    public void addComponentsAndLocate() {
        int posY1 = 5;
        int lineHeight = 20;
        int posY2 = posY1 + lineHeight + 3;

        int posX = 4;
        JLabel labelBuyQnt = new JLabel("Buy qnt#");
        JLabel labelSellQnt = new JLabel("Sell qnt#");
        labelBuyQnt.setBounds(posX, posY1, 75, lineHeight);
        labelSellQnt.setBounds(posX, posY2, 75, lineHeight);
        this.add(labelBuyQnt);
        this.add(labelSellQnt);

        posX = labelBuyQnt.getX() + labelBuyQnt.getWidth() + 4;
        numBuyQnt.setBounds(posX, posY1, 70, lineHeight);
        numSellQnt.setBounds(posX, posY2, 70, lineHeight);
        this.add(numBuyQnt);
        this.add(numSellQnt);

        posX = numBuyQnt.getX() + numBuyQnt.getWidth() + 8;
        JLabel labelBuyPrice = new JLabel("Buy price");
        JLabel labelSellPrice = new JLabel("Sell price");
        labelBuyPrice.setBounds(posX, posY1, 70, lineHeight);
        labelSellPrice.setBounds(posX, posY2, 70, lineHeight);
        this.add(labelBuyPrice);
        this.add(labelSellPrice);

        posX = labelBuyPrice.getX() + labelBuyPrice.getWidth() + 4;
        numBuyPrice.setBounds(posX, posY1, 80, lineHeight);
        numSellPrice.setBounds(posX, posY2, 80, lineHeight);
        this.add(numBuyPrice);
        this.add(numSellPrice);

        posX = numBuyPrice.getX() + numBuyPrice.getWidth() + 4;
        checkBuyMarket.setBounds(posX, posY1 - 1, 80, lineHeight);
        checkSellMarket.setBounds(posX, posY2 - 1, 80, lineHeight);
        this.add(checkBuyMarket);
        this.add(checkSellMarket);

        posX = checkBuyMarket.getX() + checkBuyMarket.getWidth() + 4;
        JLabel labelBuyTot = new JLabel("Buy total#");
        JLabel labelSellTot = new JLabel("Sell total#");
        labelBuyTot.setBounds(posX, posY1, 80, lineHeight);
        labelSellTot.setBounds(posX, posY2, 80, lineHeight);
        this.add(labelBuyTot);
        this.add(labelSellTot);

        posX = labelBuyTot.getX() + labelBuyTot.getWidth() + 4;
        numBuyTotal.setBounds(posX, posY1, 70, lineHeight);
        numSellTotal.setBounds(posX, posY2, 70, lineHeight);
        this.add(numBuyTotal);
        this.add(numSellTotal);

        posX = numSellTotal.getX() + numSellTotal.getWidth() + 4;
        JLabel labelDelay = new JLabel("Delay ms.");
        labelDelay.setBounds(posX, posY1, 70, lineHeight);
        this.add(labelDelay);

        posX = labelDelay.getX() + labelDelay.getWidth() + 4;
        numDelayMs.setBounds(posX, posY1, 60, lineHeight);
        this.add(numDelayMs);
    }

    public void setChartManager(ChartManager chartManager) {
        this.chartManager = chartManager;

        Timer timer = new Timer(AppConfig.refreshChartMs(), e -> timerTick());
        timer.start();
    }

    public void timerTick() {
        double lastPrice = chartManager.lastPrice();
        if (checkBuyMarket.isSelected()) {
            numBuyPrice.setValue(lastPrice);
        }
        if (checkSellMarket.isSelected()) {
            numSellPrice.setValue(lastPrice);
        }

        double totalBuy = 0;
        double totalSell = 0;
        for(OrderDto order : AppMain.clientSession.ordersAll().values()) {
            if (Enums.OrderStatus.equals(order.status(), Enums.OrderStatus.NEW)) {
                if (Enums.OrderSide.equals(order.side(), Enums.OrderSide.BUY)) {
                    totalBuy += order.quantity();
                } else {
                    totalSell += order.quantity();
                }
            }
        }
        numBuyTotal.setValue(totalBuy);
        numSellTotal.setValue(totalSell);

        QuoteTick lastTick = chartManager.quoteConsumer().lastTick();
        if (lastTick != null) {
            long delayMs = MILLIS.between(lastTick.date(), lastTick.createdLocal());
            long delayMsUtc = MILLIS.between(lastTick.date(), lastTick.createdUtc());
            if (Math.abs(delayMsUtc) < Math.abs(delayMs)) {
                delayMs = delayMsUtc; // TODO Remove adjust between UTC or Local time
            }

            long delayMsAvg = (long) delayMsAgg.update(LocalDateTime.now(), delayMs);
            numDelayMs.setValue(delayMsAvg);

            if (delayMsAvg > 3_000) {
                numDelayMs.setForeground(Colors.getColor("#880000"));
            } else {
                numDelayMs.setForeground(Color.black);
            }
        }
    }

    public double getBuyQnt() {
        return numBuyQnt.getValue();
    }

    public void setBuyQnt(double value) {
        this.numBuyQnt.setValue(value);
    }

    public double getSellQnt() {
        return numSellQnt.getValue();
    }

    public void setSellQnt(double value) {
        this.numSellQnt.setValue(value);
    }

    public double getBuyPrice() {
        return numBuyPrice.getValue();
    }

    public double getSellPrice() {
        return numSellPrice.getValue();
    }

    public boolean isBuyMarket() {
        return checkBuyMarket.isSelected();
    }
    public boolean isSellMarket() {
        return checkSellMarket.isSelected();
    }
}
