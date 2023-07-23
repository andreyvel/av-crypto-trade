package av.crypto.trade.gui.chart;

import java.awt.*;

// Dummy layout manager, used for displaying Limit orders
public class DummyLayout extends FlowLayout {
    @Override
    public void layoutContainer(Container target) {
        int nmembers = target.getComponentCount();

        for (int i = 0; i < nmembers; i++) {
            Component m = target.getComponent(i);
            if (m.isVisible()) {
                Dimension d = m.getPreferredSize();
                m.setSize(d.width, d.height);

                Point p = m.getLocation();
                m.setLocation(p.x, p.y);
            }
        }
    }

}
