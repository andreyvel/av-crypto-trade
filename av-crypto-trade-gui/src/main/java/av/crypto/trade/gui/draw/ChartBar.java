package av.crypto.trade.gui.draw;

import av.crypto.trade.gui.chart.ChartTheme;
import av.crypto.common.QuoteBar;
import av.crypto.common.Utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class ChartBar extends IChartEntity {
    private QuoteBar bar;

    public ChartBar(QuoteBar bar) {
        this.bar = bar;
    }

    @Override
    public void draw(Graphics2D g, calcPosX calcX, calcPosY calcY) {
        int barWidthPx = ChartTheme.barWidthPx();
        int posXL = calcX.accept(bar.date());
        int posXM = posXL + (barWidthPx - 1) / 2;
        Rectangle rect = g.getClipBounds();

        if (Utils.between(posXL, -barWidthPx, rect.width)) {
            int open = calcY.accept(bar.open());
            int close = calcY.accept(bar.close());
            int high = calcY.accept(bar.high());
            int low = calcY.accept(bar.low());

            if (open < close) {
                g.setColor(ChartTheme.colorBarGreen());
                g.fillRect(posXL + 1, open, barWidthPx - 3, close - open - 1);

                g.setColor(Color.black);
                g.drawLine(posXM, close, posXM, high);
                g.drawLine(posXM, open, posXM, low);
                g.drawRect(posXL, open, barWidthPx - 2, close - open);
            }
            else {
                g.setColor(ChartTheme.colorBarRed());
                g.fillRect(posXL + 1, close, barWidthPx - 3, open - close - 1);

                g.setColor(Color.black);
                g.drawLine(posXM, open, posXM, high);
                g.drawLine(posXM, close, posXM, low);
                g.drawRect(posXL, close, barWidthPx - 2, open - close);
            }
        }
    }
}
