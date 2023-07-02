package av.bitcoin.trade.gui.chart;

import av.bitcoin.trade.gui.AppConfig;
import av.bitcoin.trade.gui.AppMain;
import av.bitcoin.trade.gui.draw.ChartLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import av.bitcoin.common.TimeScale;
import av.bitcoin.common.dto.ChartItemDto;
import av.bitcoin.common.dto.ChartLineDto;
import av.bitcoin.common.dto.OrderDto;
import av.bitcoin.trade.gui.data.QuoteConsumer;
import av.bitcoin.common.QuoteBar;
import av.bitcoin.common.indicator.Sma;
import av.bitcoin.trade.gui.draw.ChartBar;
import av.bitcoin.trade.gui.draw.ChartOrderFilled;
import av.bitcoin.trade.gui.draw.ChartPoint;
import av.bitcoin.trade.gui.draw.IChartEntity;

import java.awt.Color;
import java.awt.Point;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static av.bitcoin.common.Enums.*;

public class ChartManager {
    private static final Logger log = LoggerFactory.getLogger(ChartManager.class);
    private ScaleTime scaleTime = null;
    private ScalePrice scaleLeft = null;
    private ScalePrice scaleRight = null;
    private ChartPanel chartPanel = null;
    private QuoteConsumer consumer = null;

    private List<OrderDto> limitOrders = new ArrayList<>();
    private List<IChartEntity> chartEntities = new ArrayList<>();

    public List<IChartEntity> chartEntities() {
        return chartEntities;
    }

    public List<OrderDto> limitOrders() {
        return limitOrders;
    }

    public ChartManager(ChartPanel chartPanel, String tradeQuote, String interval) {
        this.chartPanel = chartPanel;
        this.scaleTime = new ScaleTime(this);
        this.scaleLeft = new ScalePrice(this, ScalePrice.ScalePosition.Left, ValueUnit.PCT);
        this.scaleRight = new ScalePrice(this, ScalePrice.ScalePosition.Right, ValueUnit.ABS);

        this.consumer = new QuoteConsumer(tradeQuote, interval);
        AppMain.clientSession.subscribe(consumer);
        startScheduler();
    }

    public QuoteConsumer quoteConsumer() {
        return consumer;
    }
    public String symbol() {
        return consumer.symbol();
    }

    public int scaleTimeStepSec() {
        int totalSec = TimeScale.totalSec(scaleInteval());
        return totalSec;
    }

    public String scaleInteval() {
        return consumer.interval();
    }

    public double lastPrice() {
        if (consumer == null || consumer.lastBar() == null) {
            return 0;
        }
        return consumer.lastBar().close();
    }

    public ScalePrice scaleLeft() {
        return scaleLeft;
    }

    public ScalePrice scaleRight() {
        return scaleRight;
    }

    public ScaleTime scaleTime() {
        return scaleTime;
    }

    public void move(Point beginDrag, Point endDrag) {
        int deltaX = endDrag.x - beginDrag.x;
        if (deltaX != 0) {
            scaleTime.move(deltaX);
            chartPanel.repaint();
        }
    }
    public void refreshUI() {
        chartPanel.repaint();
    }

    public void startScheduler() {
        Thread threadScheduler = new Thread(() -> {
            while (!Thread.interrupted()) {
                try {
                    Thread.sleep(AppConfig.refreshChartMs());
                    dataSourceChanged();
                    chartPanel.refreshLimitOrders();
                    chartPanel.repaint();
                } catch (Exception e) {
                    log.error(null, e);
                }
            }
        });

        threadScheduler.start();
        dataSourceChanged();
    }

    public void dataSourceChanged() {
        double delta = 0.01d;
        double priceMin = consumer.priceMin() * (1 - delta);
        double priceMax = consumer.priceMax() * (1 + delta);

        scaleLeft.setRange(priceMin, priceMax);
        scaleRight.setRange(priceMin, priceMax);

        QuoteBar lastBar = consumer.lastBar();
        LocalDateTime scaleDateMin = scaleTime.dateTimeMin();

        if (scaleDateMin == null) {
            int barNum = 800 / ChartTheme.barWidthPx();
            LocalDateTime showFrom = lastBar.date().minusSeconds(barNum * scaleTimeStepSec());
            scaleTime.setDateTimeMin(showFrom);
        }

        Sma ma = new Sma(25);
        Color colorMa = Colors.getColor("#880000");
        List<IChartEntity> chartEntitiesNew = new ArrayList<>();

        double prevMa = 0;
        QuoteBar barPrev = null;
        for(QuoteBar bar : consumer.quoteBarMap().values()) {
            double valueMa = ma.update(bar.close());
            long deltaSec = Duration.between(scaleTime.dateTimeMin(), bar.date()).toSeconds();

            if (-scaleTimeStepSec() < deltaSec && barPrev != null) {
                // Add bar renders
                ChartBar cb = new ChartBar(bar);
                chartEntitiesNew.add(cb);

                // IndicatorMa
                ChartPoint cp = new ChartPoint(bar.date(), valueMa, 2, colorMa);
                //ChartLine cp = new ChartLine(barPrev.date(), prevMa, bar.date(), valueMa, colorMa);
                chartEntitiesNew.add(cp);
            }
            barPrev = bar;
            prevMa = valueMa;
        }

        double lastPrice = lastPrice();
        List<ChartLineDto> chartLines = AppMain.clientSession.chartLines;
        if (chartLines != null) {
            for(ChartLineDto item : chartLines) {
                double value0 = item.calcValue0(lastPrice);
                double value1 = item.calcValue1(lastPrice);
                Color color = Colors.getColor(item.color());

                ChartLine cp = new ChartLine(item.date0(), value0, item.date1(), value1, color);
                chartEntitiesNew.add(cp);
            }
        }

        List<ChartItemDto> lastAdvice = AppMain.clientSession.chartItems;
        if (lastAdvice != null) {
            for(ChartItemDto item : lastAdvice) {
                double value = item.calcValue(lastPrice);
                Color color = Colors.getColor(item.color());

                ChartPoint cp = new ChartPoint(item.date(), value, item.radius(), color);
                chartEntitiesNew.add(cp);
            }
        }

        List<OrderDto> limitOrdersNew = new ArrayList<>();
        for(OrderDto order : AppMain.clientSession.ordersAll().values()) {
            if (!consumer.symbol().equals(order.symbol())) {
                continue;
            }

            if (OrderStatus.equals(order.status(), OrderStatus.FILLED)) {
                Color color = ChartTheme.orderFilledSell();
                if (OrderSide.equals(order.side(), OrderSide.BUY)) {
                    color = ChartTheme.orderFilledBuy();
                }

                LocalDateTime orderDate = TimeScale.truncateTo(order.updatedUtc(), scaleInteval());
                ChartOrderFilled cp = new ChartOrderFilled(orderDate, order.price(), color);
                chartEntitiesNew.add(cp);
            }

            if (OrderStatus.equals(order.status(), OrderStatus.NEW)) {
                limitOrdersNew.add(order);
            }
        }

        chartEntities = chartEntitiesNew;
        limitOrders = limitOrdersNew;
    }
}
