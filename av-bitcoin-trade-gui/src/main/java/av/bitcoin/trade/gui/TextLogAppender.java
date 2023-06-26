package av.bitcoin.trade.gui;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import av.bitcoin.common.Utils;

import javax.swing.JTextArea;
import java.time.LocalDateTime;

public class TextLogAppender extends AppenderBase<ILoggingEvent> {
    private static JTextArea textLogArea;

    public static void setTextLogArea(JTextArea textLogArea) {
        TextLogAppender.textLogArea = textLogArea;
    }

    @Override
    protected void append(ILoggingEvent event) {
        String message = event.getFormattedMessage();
        LocalDateTime date = LocalDateTime.now();
        textLogArea.append(Utils.dateTime(date) + " " + message + "\n");
    }
}