package av.bitcoin.trade.gui.chart;

import av.bitcoin.trade.gui.LimitOrderButton;
import av.bitcoin.common.dto.OrderDto;
import av.bitcoin.trade.gui.draw.IChartEntity;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.HashMap;
import java.util.List;

public class ChartPanel extends JPanel {
    private Rectangle rectLeft = null;
    private Rectangle rectRight = null;
    private Rectangle rectBody = null;
    private Rectangle rectTime = null;
    private Point lastDragPoint = null;
    private ChartManager chartMan = null;
    private HashMap<Long, LimitOrderButton> limitOrders = new HashMap<>();

    public ChartManager chartMan() {
        return chartMan;
    }
    private enum DragTypeEnum {None, DragChart, LimitOrder};
    private DragTypeEnum dragType = DragTypeEnum.None;
    private int limitOrderDragY = 0;


    public ChartPanel(String tradeQuote, String interval) {
        this.setLayout(new DummyLayout());
        this.chartMan = new ChartManager(this, tradeQuote, interval);
        this.setBackground(ChartTheme.chartBackground());
        refreshLayout();

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastDragPoint = e.getPoint();
                dragType = dragType.None;

                if (rectBody.contains(lastDragPoint)) {
                    dragType = DragTypeEnum.DragChart;
                }
                else if (rectRight.contains(lastDragPoint)) {
                    dragType = DragTypeEnum.LimitOrder;
                    limitOrderDragY = chartMan.scaleRight().posY(chartMan.lastPrice());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragType = dragType.None;
                super.mouseReleased(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (lastDragPoint == null) {
                    return;
                }

                if (dragType == DragTypeEnum.DragChart) {
                    chartMan.move(lastDragPoint, e.getPoint());
                    lastDragPoint = e.getPoint();
                }

                if (dragType == DragTypeEnum.LimitOrder) {
                    limitOrderDragY += lastDragPoint.y - e.getPoint().y;
                    lastDragPoint = e.getPoint();
                    chartMan.refreshUI();
                }            }
        });

        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastDragPoint == null) {
                    return;
                }

                if (dragType == DragTypeEnum.DragChart) {
                    chartMan.move(lastDragPoint, e.getPoint());
                    lastDragPoint = e.getPoint();
                }

                if (dragType == DragTypeEnum.LimitOrder) {
                    limitOrderDragY += lastDragPoint.y - e.getPoint().y;
                    lastDragPoint = e.getPoint();
                    chartMan.refreshUI();
                }
            }
        });
    }

    public double dragPriceY() {
        if (dragType == DragTypeEnum.LimitOrder) {
            double priceY = chartMan.scaleRight().priceY(limitOrderDragY);
            return priceY;
        }
        return 0d;
    }

    public void refreshLayout() {
        int scaleTimeHeight = ChartTheme.scaleTimeHeight();
        int scaleLeftWidth = chartMan.scaleLeft().scaleWidth();
        int scaleRightWidth = chartMan.scaleRight().scaleWidth();

        int bodyY = 10;
        int bodyHeight = this.getHeight() - scaleTimeHeight - bodyY;
        chartMan.scaleLeft().updateValueKof(bodyHeight);
        chartMan.scaleRight().updateValueKof(bodyHeight);

        rectLeft = new Rectangle(0, bodyY, scaleLeftWidth, bodyHeight);
        rectBody = new Rectangle(scaleLeftWidth, bodyY, this.getWidth() - scaleLeftWidth - scaleRightWidth, bodyHeight);
        rectRight = new Rectangle(rectBody.x + rectBody.width, bodyY, scaleRightWidth, bodyHeight);
        rectTime = new Rectangle(scaleLeftWidth, rectBody.y + rectBody.height, rectBody.width, scaleTimeHeight);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        refreshLayout();

        Graphics2D g2 = setClip(g, rectLeft);
        chartMan.scaleLeft().paint(g2);

        g2 = setClip(g, rectRight);
        chartMan.scaleRight().paint(g2);
        chartMan.scaleRight().paintRunner(g2);

        g2 = setClip(g, rectTime);
        chartMan.scaleTime().paint(g2);

        g2 = setClip(g, rectBody);
        drawGrid(g2);
        drawData(g2);

        drawRunner(g2);
        drawLimitOrders(g2);

        if (dragType == DragTypeEnum.LimitOrder) {
            g2 = setClip(g, rectBody);
            drawLimitOrderDrag(g2);

            g2 = setClip(g, rectRight);
            double priceY = dragPriceY();
            chartMan.scaleRight().paintRunner(g2, ChartTheme.limitOrderBackground(), priceY);
        }
    }

    public void refreshLimitOrders() {
        HashMap<Long, LimitOrderButton> limitOrdersDel = new HashMap<>();
        limitOrdersDel.putAll(limitOrders);

        boolean refreshUI = false;
        for(OrderDto order : chartMan.limitOrders()) {
            LimitOrderButton orderPanel = limitOrdersDel.remove(order.orderId());
            if (orderPanel == null) {
                orderPanel = new LimitOrderButton(order);
                limitOrders.put(order.orderId(), orderPanel);

                int posY = chartMan.scaleRight().posY(order.price());
                int posY2 = rectBody.height - posY + 2;
                orderPanel.setLocation(rectBody.width + rectBody.x - orderPanel.getWidth() - 14, posY2);
                this.add(orderPanel);
                refreshUI = true;
            }
        }

        for(Long orderId : limitOrdersDel.keySet()) {
            LimitOrderButton button = limitOrders.remove(orderId);
            this.remove(button);
            refreshUI = true;
        }

        if (refreshUI) {
            this.updateUI();
        }
    }

    public void drawLimitOrders(Graphics2D g) {
        Rectangle rect = g.getClipBounds();
        HashMap<Long, LimitOrderButton> limitOrdersDel = new HashMap<>();
        limitOrdersDel.putAll(limitOrders);

        for(OrderDto order : chartMan.limitOrders()) {
            LimitOrderButton orderButton = limitOrdersDel.remove(order.orderId());

            if (orderButton != null) {
                int posY = chartMan.scaleRight().posY(order.price());
                int posY2 = rectBody.height - posY + 1;
                orderButton.setLocation(rectBody.width + rectBody.x - orderButton.getWidth() - 14, posY2);

                g.setColor(ChartTheme.limitOrderLine());
                g.setStroke(new BasicStroke(1.0f));
                g.drawLine(rect.x, posY, orderButton.getX() - rectBody.x, posY);
            }
        }
    }

    public void drawLimitOrderDrag(Graphics2D g) {
        Rectangle rect = g.getClipBounds();
        g.setColor(ChartTheme.limitOrderLine());
        g.setStroke(new BasicStroke(1.0f));
        g.drawLine(0, limitOrderDragY, rect.width, limitOrderDragY);
    }

    public void drawRunner(Graphics2D g) {
        Rectangle rect = g.getClipBounds();
        g.setColor(ChartTheme.runnerLine());
        g.setStroke(new BasicStroke());

        int posY = chartMan.scaleRight().posY(chartMan.lastPrice());
        g.drawLine(0, posY, rect.width, posY);
    }

    public void drawData(Graphics2D g) {
        BasicStroke basicStroke = new BasicStroke(1.0f);
        g.setStroke(basicStroke);

        ScaleTime scaleTime = chartMan.scaleTime();
        ScalePrice scaleLeft = chartMan.scaleLeft();

        for(IChartEntity img : chartMan.chartEntities()) {
            img.draw(g, scaleTime::posX, scaleLeft::posY);
        }
    }

    public Graphics2D setClip(Graphics g, Rectangle rect) {
        return setClip(g, rect, false);
    }

    public Graphics2D setClip(Graphics g, Rectangle rect, boolean visible) {
        Graphics2D g2 = (Graphics2D)g.create(rect.x, rect.y, rect.width, rect.height);
        g2.scale(1, -1);
        g2.translate(0, -rect.height);

        if (visible) {
            g2.setColor(Color.red);
            g2.drawOval(0, 0, 30, 30);
            g2.drawRect(0, 0, rect.width - 1, rect.height - 1);
        }
        return g2;
    }

    public void drawGrid(Graphics2D g) {
        Rectangle rect = g.getClipBounds();
        g.setColor(ChartTheme.colorGrid());

        Stroke dashed = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{3}, 0);
        g.setStroke(dashed);

        List<ScaleTime.ScaleTimeItem> timeValues = chartMan.scaleTime().scaleValues(rect.width);
        for(ScaleTime.ScaleTimeItem value : timeValues) {
            if (value.labelGrid) {
                g.drawLine(value.posX, 0, value.posX, rect.height);
            }
        }

        List<ScalePrice.ScalePriceItem> priceValues = chartMan.scaleLeft().scaleValues(rect.height);
        for(ScalePrice.ScalePriceItem value : priceValues) {
            if (value.label) {
                g.drawLine(0, value.posY, rect.width, value.posY);
            }
        }
    }
}
