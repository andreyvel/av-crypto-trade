package av.crypto.trade.gui;

import av.crypto.trade.gui.chart.ChartTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class TabLogPanel extends JPanel {
    public TabLogPanel() {
        this.setLayout(new BorderLayout());

        JTextArea textLogArea = new JTextArea();
        textLogArea.setBackground(ChartTheme.chartBackground());
        textLogArea.setEditable(false);
        TextLogAppender.setTextLogArea(textLogArea);

        JScrollPane areaScrollPane = new JScrollPane(textLogArea);
        areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        this.add(areaScrollPane);

        JPopupMenu popup = new JPopupMenu();
        JMenuItem menuCopy = new JMenuItem("Copy");
        popup.add(menuCopy);
        menuCopy.addActionListener(e -> {
            textLogArea.selectAll();
            String textAll = textLogArea.getText();

            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            StringSelection stringSelection = new StringSelection(textAll);
            clipboard.setContents(stringSelection, null);

        });

        JMenuItem menuClear = new JMenuItem("Clear");
        popup.add(menuClear);
        menuClear.addActionListener(e -> {
            textLogArea.setText(null);
        });

        popup.addSeparator();
        JMenuItem wordWrapMenu = new JMenuItem("Word wrap");
        popup.add(wordWrapMenu);
        wordWrapMenu.addActionListener(e -> {
            boolean wordWrap = !textLogArea.getLineWrap();
            textLogArea.setLineWrap(wordWrap);
            wordWrapMenu.setText(wordWrap ? "Word unwrap" : "Word wrap");
        });

        textLogArea.setComponentPopupMenu(popup);
    }
}
