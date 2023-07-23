package av.crypto.trade.gui;

import av.crypto.trade.gui.chart.ChartTheme;
import av.crypto.trade.gui.chart.DummyLayout;
import av.crypto.common.Utils;
import av.crypto.common.dto.OrderDto;

import javax.swing.*;
import java.awt.*;

public class LimitOrderButton extends JPanel {
    private OrderDto order;
    private JButton button;

    public LimitOrderButton(OrderDto order) {
        this.setLayout(new DummyLayout());
        this.order = order;

        button = new JButton("X");
        button.addActionListener(e -> AppMain.clientSession.cancelOrder(order.symbol(), order.orderId()));
        button.setPreferredSize(new Dimension(20, 22));
        button.setLocation(0, 0);
        this.add(button);

        JLabel label = new JLabel("qnt=" + Utils.num4(order.quantity()));
        label.setLocation(24, 2);
        this.add(label);

        this.setBackground(ChartTheme.limitOrderBackground());
        this.setBorder(BorderFactory.createLineBorder(ChartTheme.limitOrderLine()));
        this.setPreferredSize(new Dimension(110, 20));
    }
}
