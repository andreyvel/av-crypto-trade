package av.crypto.trade.gui.draw;

import java.awt.Color;
import java.awt.Graphics2D;
import java.time.LocalDateTime;

import av.crypto.trade.gui.chart.ChartTheme;

public class ChartPoint extends IChartEntity {
    private LocalDateTime date;
    private double value;
    private int radius;
    private Color color;

    public ChartPoint(LocalDateTime date, double value, int radius, Color color) {
        this.date = date;
        this.value = value;
        this.radius = radius;
        this.color = color;
    }

    @Override
    public void draw(Graphics2D g, calcPosX calcX, calcPosY calcY) {
        int posX = calcX.accept(date);
        if (posX < 0) {
            return;
        }

        int posY = calcY.accept(value);
        if (posY < 0) {
            return;
        }

        g.setColor(color);
        int posL = (ChartTheme.barWidthPx() - radius) / 2;
        g.fillOval(posX + posL, posY - radius, radius * 2, radius * 2);
    }
}
