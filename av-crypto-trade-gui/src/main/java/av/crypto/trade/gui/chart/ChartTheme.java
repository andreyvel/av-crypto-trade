package av.crypto.trade.gui.chart;

import java.awt.Color;

public class ChartTheme {
    private static final int barWidthPx = 8;
    private static final int scaleTimeHeight = 30;
    private static final int scalePriceWidth = 65;
    private static final int scalePctWidth = 45;

    private static final Color runnerLine = Colors.getColor("#994444");
    private static final Color runnerBackground = Colors.getColor("#dd7788");
    private static final Color colorBarRed = Colors.getColor("#994444");
    private static final Color colorBarGreen = Colors.getColor("#66AA66");
    private static final Color chartBackground = Colors.getColor("#e0e0e0");
    private static final Color colorGrid = Colors.getColor("707070");
    private static final Color colorAxis = Color.BLACK;
    private static final Color orderFilledBuy = Colors.getColor("#008030");
    private static final Color orderFilledSell = Colors.getColor("#DD0000");
    private static final Color limitOrderLine = Colors.getColor("#884488");
    private static final Color limitOrderBackground = Colors.getColor("#bbcaaa");

    public static Color limitOrderBackground() {
        return limitOrderBackground;
    }
    public static Color limitOrderLine() {
        return limitOrderLine;
    }
    public static Color runnerLine() {
        return runnerLine;
    }
    public static Color runnerBackground() {
        return runnerBackground;
    }

    public static  Color colorBarRed() {
        return colorBarRed;
    }

    public static Color colorBarGreen() {
        return colorBarGreen;
    }

    public static Color chartBackground() {
        return chartBackground;
    }

    public static Color colorGrid() {
        return colorGrid;
    }

    public static Color colorAxis() {
        return colorAxis;
    }

    public static int barWidthPx() {
        return barWidthPx;
    }

    public static int scaleTimeHeight() {
        return scaleTimeHeight;
    }

    public static int scalePriceWidth() {
        return scalePriceWidth;
    }
    public static int scalePctWidth() {
        return scalePctWidth;
    }
    public static Color orderFilledBuy() {
        return orderFilledBuy;
    }
    public static Color orderFilledSell() {
        return orderFilledSell;
    }

}
