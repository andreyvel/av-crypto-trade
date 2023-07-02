package av.bitcoin.trade.gui.draw;

import java.awt.*;
import java.time.LocalDateTime;

public class ChartLine extends IChartEntity {
    private LocalDateTime date0;
    private LocalDateTime date1;
    private double value0;
    private double value1;
    private Color color;
    private Stroke lineStroke = new BasicStroke(1.0f);

    public ChartLine(LocalDateTime date0, double value0, LocalDateTime date1, double value1, Color color) {
        this.date0 = date0;
        this.date1 = date1;
        this.value0 = value0;
        this.value1 = value1;
        this.color = color;
    }

    @Override
    public void draw(Graphics2D g, calcPosX calcX, calcPosY calcY) {
        int posX0 = calcX.accept(date0);
        int posX1 = calcX.accept(date1);
        if (posX0 < 0 && posX1 < 0) {
            return;
        }

        int posY0 = calcY.accept(value0);
        int posY1 = calcY.accept(value1);
        if (posY0 < 0 && posY1 < 0) {
            return;
        }

        g.setColor(color);
        g.setStroke(lineStroke);
        g.drawLine(posX0, posY0, posX1, posY1);
    }
}
