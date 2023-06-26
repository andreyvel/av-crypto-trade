package av.bitcoin.trade.gui.chart;

import av.bitcoin.common.Enums.ValueUnit;
import av.bitcoin.common.Utils;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ScalePrice {
    private ChartManager chartMan;
    private static final NumberFormat num0 = new DecimalFormat("#0", DecimalFormatSymbols.getInstance(Locale.US));
    private static final NumberFormat num2 = new DecimalFormat("#0.00", DecimalFormatSymbols.getInstance(Locale.US));
    private static final Font font = new Font(Font.DIALOG, Font.PLAIN, 11);
    private double gridPriceStep = 10;
    private double priceToPixelMult;
    private int scaleHeight = 0;
    private double priceMin;
    private double priceMax;
    private ValueUnit valueUnit;
    private ScalePosition scalePosition;

    public static enum ScalePosition {Left, Right}

    public int scaleWidth() {
        if (valueUnit == ValueUnit.PCT) {
            return ChartTheme.scalePctWidth();
        }
        return ChartTheme.scalePriceWidth();
    }

    public ScalePrice(ChartManager chartMan, ScalePosition scalePosition, ValueUnit valueUnit) {
        this.scalePosition = scalePosition;
        this.valueUnit = valueUnit;
        this.chartMan = chartMan;
    }

    public void setRange(double priceMin, double priceMax) {
        this.priceMin = priceMin;
        this.priceMax = priceMax;
    }

    public void updateValueKof(int scaleHeight) {
        //if (Math.abs(this.scaleHeight - scaleHeight) > 5)
        {
            this.scaleHeight = scaleHeight;
            priceToPixelMult = scaleHeight / (priceMax - priceMin);

            int levelNum = scaleHeight / 8;
            gridPriceStep = (priceMax - priceMin) / levelNum;
            gridPriceStep = calculateStep(gridPriceStep);
        }
    }

    public double calculateStep(double step) {
        double scale = 0.0001d;
        for (int ind = 0; ind < 8; ind++) {
            if (scale >= step) {
                return scale;
            }
            if (scale * 2 >= step) {
                return scale * 2;
            }
            if (scale * 5 >= step) {
                return scale * 5;
            }
            scale = scale * 10;
        }
        return step;
    }

    public int posY(double price) {
        return (int) (priceToPixelMult * (price - priceMin));
    }

    public double priceY(int posY) {
        double priceY = posY / priceToPixelMult;
        return priceY + priceMin;
    }

    public static class ScalePriceItem {
        public final int posY;
        public final double price;
        public final boolean label;

        public ScalePriceItem(int posY, double price, boolean label) {
            this.posY = posY;
            this.price = price;
            this.label = label;
        }
    }

    public List<ScalePriceItem> scaleValues(int rectHeight) {
        List<ScalePriceItem> list = new ArrayList<>();

        double priceCur = gridPriceStep * Math.floor(priceMin / gridPriceStep);
        for (int ind = 0; ind < 100; ind++) {
            priceCur += gridPriceStep;
            int posY = posY(priceCur);
            if (posY < 3) {
                continue;
            }
            if (posY > rectHeight - 3) {
                break;
            }
            boolean label = (list.size() + 4) % 5 == 0;
            list.add(new ScalePriceItem(posY, priceCur, label));
        }
        return list;
    }

    public void paint(Graphics2D g) {
        Rectangle rect = g.getClipBounds();
        g.setFont(font);

        g.setColor(ChartTheme.colorAxis());
        if (scalePosition == ScalePosition.Left) {
            g.drawLine(rect.width - 1, 0, rect.width - 1, rect.height);
        } else {
            g.drawLine(0, 0, 0, rect.height);
        }

        List<ScalePriceItem> values = scaleValues(rect.height);
        for (ScalePriceItem value : values) {
            if (scalePosition == ScalePosition.Left) {
                g.drawLine(rect.width - 2, value.posY, rect.width - 3, value.posY);
            } else {
                g.drawLine(1, value.posY, 2, value.posY);
            }

            if (value.label) {
                if (scalePosition == ScalePosition.Left) {
                    double valuePct = Utils.pct(chartMan.lastPrice(), value.price);
                    paintLabel(g, valuePct, value.posY);
                } else {
                    paintLabel(g, value.price, value.posY);
                }
            }
        }
    }

    public void paintLabel(Graphics2D g, double price, int posY) {
        String text = num2.format(price);
        int textWidth = g.getFontMetrics().stringWidth(text);

        Rectangle rect = g.getClipBounds();
        Graphics2D g2 = (Graphics2D)g.create();

        g2.translate((rect.width - textWidth) / 2, posY - 4);
        g2.scale(1, -1);
        g2.drawString(text, 0, 0);
    }

    public void paintRunner(Graphics2D g) {
        paintRunner(g, ChartTheme.runnerBackground(), chartMan.lastPrice());
    }

    public void paintRunner(Graphics2D g, Color background, double price) {
        Rectangle rect = g.getClipBounds();
        int posY = chartMan.scaleRight().posY(price);

        g.setFont(font);
        g.setColor(background);
        g.fillRect(3, posY - 8, rect.width - 4, 18);

        g.setColor(ChartTheme.colorAxis());
        paintLabel(g, price, posY);
    }
}
