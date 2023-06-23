package av.bitcoin.trade.gui;

import org.trade.common.Utils;

import javax.swing.JTextField;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Locale;

public class NumericField extends JTextField {
    private int numDecimals;
    private NumberFormat format = null;
    private static HashSet<Integer> enabledChars = new HashSet<>();

    static {
        enabledChars.add(8); // Backspace
        enabledChars.add(37); // Left
        enabledChars.add(39); // Right
        enabledChars.add(46); // Delete?
        enabledChars.add(127); // Delete
        enabledChars.add((int)'.'); // Delimiter
        enabledChars.add((int)'\t'); // Tab
    }

    public NumericField() {
        setNumDecimals(2);
        setValue(0);
    }

    public NumericField(double value, int numDecimals) {
        setNumDecimals(numDecimals);
        setValue(value);
    }

    @Override
    public void processKeyEvent(KeyEvent ev) {
        int keyCode = ev.getKeyCode();
        if ('0' <= ev.getKeyChar() & ev.getKeyChar() <= '9' || enabledChars.contains(keyCode)) {
            NumericField.super.processKeyEvent(ev);
        }
    }
    public void setNumDecimals(int numDecimals) {
        if (numDecimals < 0) {
            throw new IllegalArgumentException("numDecimals=" + numDecimals + " < 0");
        }

        this.numDecimals = numDecimals;
        if (numDecimals == 0) {
            format = new DecimalFormat("#0", DecimalFormatSymbols.getInstance(Locale.US));
            return;
        }

        StringBuilder frm = new StringBuilder("#0.");
        for(int ind = 0; ind < numDecimals; ind++) {
            frm.append("0");
        }
        format = new DecimalFormat(frm.toString(), DecimalFormatSymbols.getInstance(Locale.US));
    }

    public int getNumDecimals() {
        return numDecimals;
    }

    public double getValue() {
        double value = Double.parseDouble(this.getText());
        value = Utils.round(value, numDecimals);
        return value;
    }

    public void setValue(double value) {
        String text = format.format(value);
        this.setText(text);
    }
}
