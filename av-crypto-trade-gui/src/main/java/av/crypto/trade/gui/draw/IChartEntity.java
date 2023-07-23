package av.crypto.trade.gui.draw;

import java.awt.Graphics2D;
import java.time.LocalDateTime;

public abstract class IChartEntity {
    @FunctionalInterface
    public interface calcPosX {
        int accept(LocalDateTime date);
    }
    @FunctionalInterface
    public interface calcPosY {
        int accept(double price);
    }

    public abstract void draw(Graphics2D g, calcPosX calcX, calcPosY calcY);
}
