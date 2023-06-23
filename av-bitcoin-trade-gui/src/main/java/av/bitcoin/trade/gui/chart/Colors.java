package av.bitcoin.trade.gui.chart;

import java.awt.Color;

public class Colors {
    /**
     * Converts a hex string to a color.
     *
     * @param hex (i.e. #CCCCCCFF or CCCCCC)
     * @return Color
     */
    public static Color getColor(String hex) {
        hex = hex.replace("#", "");

        if (hex.length() == 6) {
            int r = Integer.valueOf(hex.substring(0, 2), 16);
            int g = Integer.valueOf(hex.substring(2, 4), 16);
            int b = Integer.valueOf(hex.substring(4, 6), 16);
            return new Color(r, g, b);
        }

        if (hex.length() == 8) {
            int r = Integer.valueOf(hex.substring(0, 2), 16);
            int g = Integer.valueOf(hex.substring(2, 4), 16);
            int b = Integer.valueOf(hex.substring(4, 6), 16);
            int a = Integer.valueOf(hex.substring(6, 8), 16);
            return new Color(r, g, b, a);
        }

        throw new IllegalArgumentException("Bad color value hex=" + hex);
    }
}
