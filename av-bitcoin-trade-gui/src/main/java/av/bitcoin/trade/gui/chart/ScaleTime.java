package av.bitcoin.trade.gui.chart;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class ScaleTime {
    private static DateTimeFormatter frmHHMM = DateTimeFormatter.ofPattern("HH:mm");
    private LocalDateTime dateTimeMin = null; // LocalDateTime.now().truncatedTo(ChronoUnit.DAYS)
    private ChartManager chartMan;

    public ScaleTime(ChartManager chartMan) {
        this.chartMan = chartMan;
    }
    private Font font = new Font(Font.DIALOG, Font.PLAIN, 11);

    public void setDateTimeMin(LocalDateTime dateTimeMin) {
        this.dateTimeMin = dateTimeMin;
    }

    public LocalDateTime dateTimeMin() {
        return dateTimeMin;
    }

    public int posX(LocalDateTime date) {
        double scaleTimeStepSec = chartMan.scaleTimeStepSec();
        double barNum = Duration.between(dateTimeMin, date).toSeconds() / scaleTimeStepSec;
        return (int)Math.round(barNum * ChartTheme.barWidthPx());
    }

    public void move(int deltaX) {
        double scaleTimeStepSec = chartMan.scaleTimeStepSec();
        double barNum = (double)deltaX / ChartTheme.barWidthPx();

        double deltaSec = barNum * scaleTimeStepSec;
        dateTimeMin = dateTimeMin.minusSeconds((long)deltaSec);
    }

    public static class ScaleTimeItem {
        public final int posX;
        public final LocalDateTime date;
        public final boolean labelGrid;
        public final boolean labelText;

        public ScaleTimeItem(int posX, LocalDateTime date, boolean labelGrid, boolean labelText) {
            this.posX = posX;
            this.date = date;
            this.labelGrid = labelGrid;
            this.labelText = labelText;
        }
    }

    public List<ScaleTimeItem> scaleValues(int rectWidth) {
        List<ScaleTimeItem> list = new ArrayList<>();

        int scaleTimeStepSec = chartMan.scaleTimeStepSec();
        long epochSec = dateTimeMin.toEpochSecond(ZoneOffset.UTC);
        epochSec = epochSec - (epochSec % scaleTimeStepSec);
        LocalDateTime datePtr = LocalDateTime.ofEpochSecond(epochSec, 0, ZoneOffset.UTC);

        while(true) {
            int posX = posX(datePtr);
            if (posX > rectWidth) {
                break;
            }

            long epochSecNum = datePtr.toEpochSecond(ZoneOffset.UTC) / scaleTimeStepSec;
            boolean labelText = (epochSecNum % 10) == 0;
            boolean labelGrid = (epochSecNum % 5) == 0;

            list.add(new ScaleTimeItem(posX, datePtr, labelGrid, labelText));
            datePtr = datePtr.plus(scaleTimeStepSec, ChronoUnit.SECONDS);
        }
        return list;
    }

    public void paint(Graphics2D g) {
        g.setFont(font);
        Rectangle rect = g.getClipBounds();

        g.setColor(ChartTheme.colorAxis());
        g.drawLine(0, rect.height, rect.width, rect.height);

        List<ScaleTimeItem> values = scaleValues(rect.width);
        for(ScaleTimeItem value : values) {
            if (value.labelGrid) {
                g.drawLine(value.posX, rect.height - 1, value.posX, rect.height - 3);
            }

            if (value.labelText) {
                String text = frmHHMM.format(value.date);
                int textWidth = g.getFontMetrics().stringWidth(text);

                Graphics2D g2 = (Graphics2D) g.create();
                g2.translate(value.posX - textWidth / 2, rect.height - 15);
                g2.scale(1, -1);
                g2.drawString(text, 0, 0);
            }
        }
    }
}
