package av.crypto.trade.gui.draw;

import av.crypto.trade.gui.chart.ChartTheme;

import java.awt.Color;
import java.awt.Graphics2D;
import java.time.LocalDateTime;

public class ChartOrderFilled extends IChartEntity {
    private LocalDateTime date;
    private double value;
    private Color color;

    public ChartOrderFilled(LocalDateTime date, double value, Color color) {
        this.date = date;
        this.value = value;
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
        int barWidth = ChartTheme.barWidthPx();
        g.fillRect(posX, posY - barWidth / 2, barWidth - 1, barWidth - 2);
    }
}
