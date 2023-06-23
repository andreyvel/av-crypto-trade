package av.bitcoin.trade.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import av.bitcoin.trade.gui.data.ClientSession;

import javax.swing.JFrame;
import java.io.FileNotFoundException;

public class AppMain {
    // static { System.setProperty("logback.configurationFile", "logback.xml");}
    private static final Logger log = LoggerFactory.getLogger(AppMain.class);
    private static AppFrame mainFrame;
    public static ClientSession clientSession;

    public static void main(String[] args) {
        try {
            AppConfig.loadValues("av-bitcoin-trade-gui.yaml");
            mainFrame = new AppFrame();
            setTitle("av-bitcoin-trade-gui 1.0.0");

            clientSession = new ClientSession();
            clientSession.startScheduler();

            for(ConfigTradeTab configTradeTab : AppConfig.tradeTabs()) {
                mainFrame.addChartPanel(configTradeTab); // , chartPanel
            }
            mainFrame.setBounds(100, 100, 1200, 800);
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrame.setLocationRelativeTo(null);
            mainFrame.setVisible(true);
        } catch (FileNotFoundException e) {
            log.error("main", e);
        }
    }
    public static void setTitle(String text) {
        mainFrame.setTitle(text);
    }

    public static void commandExit() {
        System.exit(0);
    }
}
