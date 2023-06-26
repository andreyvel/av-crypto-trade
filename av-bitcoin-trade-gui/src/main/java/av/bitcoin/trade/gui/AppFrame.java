package av.bitcoin.trade.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import javax.swing.JTabbedPane;

import static av.bitcoin.common.Enums.*;

public class AppFrame extends JFrame {
    private static final Logger log = LoggerFactory.getLogger(AppFrame.class);

    private int buttonHeight = 36;
    private int buttonWidth = 128;
    private JPanel headerPanel = new JPanel();
    private TabReportPanel reportPanel = new TabReportPanel();
    private TabLogPanel logPanel = new TabLogPanel();
    private JTabbedPane tabbedPane = new JTabbedPane();


    private JButton buttonBuy = new JButton("Buy");
    private JButton buttonSell = new JButton("Sell");
    private JButton buttonCloseAll = new JButton("Close All");
    private JButton buttonRefresh = new JButton("Refresh");
    private JButton buttonExit = new JButton("Exit");

    public AppFrame() throws HeadlessException {
        this.setMinimumSize(new Dimension(800, 600));
        this.setLayout(null);

        initHeaderPanel();
        headerPanel.setBackground(Color.gray);
        this.add(headerPanel);

        tabbedPane.addTab("Logs", logPanel);
        tabbedPane.addTab("Report", reportPanel);
        this.add(tabbedPane);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                refreshLayout();
            }
        });

        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher( new KeyDispatcher() );
    }

    private class KeyDispatcher implements KeyEventDispatcher {
        public boolean dispatchKeyEvent(KeyEvent e) {
            TabChartPanel panel = selectedTabChartPanel();
            if (panel == null) {
                // allow the event to be redispatched
                return true;
            }
            return panel.dispatchKeyEvent(e);
        }
    }

    public void addChartPanel(ConfigTradeTab configTradeTab) {
        TabChartPanel panel = new TabChartPanel(configTradeTab);

        tabbedPane.insertTab(configTradeTab.caption(), null, panel, null, 0);
        tabbedPane.setSelectedIndex(0);
    }

    public TabChartPanel selectedTabChartPanel() {
        Component comp = tabbedPane.getSelectedComponent();
        if (comp instanceof TabChartPanel) {
            return (TabChartPanel) comp;
        }
        return null;
    }

    public void initHeaderPanel() {
        buttonBuy.setBounds(1, 0, buttonWidth, buttonHeight);
        buttonBuy.addActionListener(e -> {
            TabChartPanel tabChartPanel = selectedTabChartPanel();
            if (tabChartPanel == null) {
                showMessageChartPanelIsNotActive();
                return;
            }

            OrderPanel orderPanel = tabChartPanel.orderPanel();
            double price = orderPanel.getBuyPrice();
            double orderQnt = orderPanel.getBuyQnt();
            makeOrder(orderPanel.isBuyMarket(), OrderSide.BUY, orderQnt, price);
        });

        buttonSell.setBounds(buttonBuy.getX() + buttonBuy.getWidth(), 0, buttonWidth, buttonHeight);
        buttonSell.addActionListener(e -> {
            TabChartPanel tabChartPanel = selectedTabChartPanel();
            if (tabChartPanel == null) {
                showMessageChartPanelIsNotActive();
                return;
            }

            OrderPanel orderPanel = tabChartPanel.orderPanel();
            double price = orderPanel.getSellPrice();
            double orderQnt = orderPanel.getSellQnt();
            makeOrder(orderPanel.isSellMarket(), OrderSide.SELL, orderQnt, price);
        });

        buttonCloseAll.setBounds(buttonSell.getX() + buttonSell.getWidth(), 0, buttonWidth, buttonHeight);
        buttonCloseAll.addActionListener(e -> {
            TabChartPanel tabChartPanel = selectedTabChartPanel();
            if (tabChartPanel == null) {
                showMessageChartPanelIsNotActive();
                return;
            }
            AppMain.clientSession.cancelAllOrders(tabChartPanel.chartPanel().chartMan().symbol());
        });

        buttonRefresh.setBounds(buttonCloseAll.getX() + buttonCloseAll.getWidth(), 0, buttonWidth, buttonHeight);
        buttonRefresh.addActionListener(e -> {
            AppMain.clientSession.refreshRestData();
            AppMain.clientSession.ping();

            Component comp = tabbedPane.getSelectedComponent();
            if (comp instanceof ITabRefresh) {
                ((ITabRefresh) comp).refresh();
            }
        });

        buttonExit.setBounds(buttonRefresh.getX() + buttonRefresh.getWidth(), 0, buttonWidth, buttonHeight);
        buttonExit.addActionListener(e -> {
            AppMain.commandExit();
        });

        headerPanel.setLayout(null);
        headerPanel.add(buttonBuy);
        headerPanel.add(buttonSell);
        headerPanel.add(buttonCloseAll);

        headerPanel.add(buttonRefresh);
        headerPanel.add(buttonExit);
    }

    public void refreshLayout() {
        Dimension size = this.getContentPane().getSize();
        int height = size.height;
        int width = size.width;

        int headerPanelHeight = buttonHeight + 1;
        headerPanel.setBounds(0, 0, width, headerPanelHeight);
        tabbedPane.setBounds(0, headerPanelHeight, width, height - headerPanelHeight);
    }

    public void showMessageChartPanelIsNotActive() {
        JOptionPane.showMessageDialog(this, "Chart panel is not active",
                "Warning", JOptionPane.WARNING_MESSAGE);
    }

    public void makeOrder(boolean isMarket, OrderSide orderSide, double orderQnt, double price) {
        TabChartPanel tabChartPanel = selectedTabChartPanel();
        if (tabChartPanel == null) {
            showMessageChartPanelIsNotActive();
            return;
        }

        String symbol = tabChartPanel.symbol();
        if (isMarket) {
            AppMain.clientSession.marketOrder(symbol, orderSide, orderQnt);
        } else {
            AppMain.clientSession.limitOrder(symbol, orderSide, orderQnt, price);
        }
    }
}
