package av.bitcoin.trade.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import av.bitcoin.common.Utils;
import av.bitcoin.common.dto.AccountDto;
import av.bitcoin.common.dto.AccountDto.AccountBalance;
import av.bitcoin.common.dto.OrderDto;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static av.bitcoin.common.Enums.*;

public class TabReportPanel extends JPanel implements ITabRefresh {
    private static final Logger log = LoggerFactory.getLogger(TabReportPanel.class);
    private DateTimeFormatter frmDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private JEditorPane htmlPanel = new JEditorPane();

    public TabReportPanel() {
        this.setLayout(new BorderLayout());
        htmlPanel.setEditable(false);

        JScrollPane areaScrollPane = new JScrollPane(htmlPanel);
        areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        this.add(areaScrollPane);

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                refreshPage();
            }
        });
    }

    public void refreshPage() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("<html><body>");

            AccountDto acc = AppMain.clientSession.accountStatus();
            sb.append("<h3>Account status: " + acc.accountType() + "</h3>");
            appendAccountHtml(sb, acc);

            sb.append("<br/><h3>Active orders</h3>\n");
            List<OrderDto> ordersActive = new ArrayList<>();
            for(OrderDto order : AppMain.clientSession.ordersAll().values()) {
                if (OrderStatus.equals(order.status(), OrderStatus.NEW)) {
                    ordersActive.add(order);
                }
            }
            ordersActive.sort(Comparator.comparingLong(OrderDto::created));
            appendOrdersHtml(sb, ordersActive, false);

            sb.append("<br/><h3>All orders</h3>\n");
            List<OrderDto> ordersAll = new ArrayList<>();
            ordersAll.addAll(AppMain.clientSession.ordersAll().values());
            ordersAll.sort(Comparator.comparingLong(OrderDto::created));
            appendOrdersHtml(sb, ordersAll, true);

            sb.append("<br/>\n");
            sb.append("<font size='-1'>Report created on " + LocalDateTime.now() + "<font/>\n");
            sb.append("<body/></html>");

            htmlPanel.setContentType("text/html");
            htmlPanel.setText(sb.toString());

        } catch (Exception e) {
            log.error(null, e);
        }
    }

    public void appendAccountHtml(StringBuilder sb, AccountDto acc) {
        sb.append("<table width='50%' cellspacing='0' cellpadding='1' border='1'>");
        sb.append("<tr style='font-weight:bold'><td>Asset</td><td>Free</td><td>Locked</td></tr>\n");

        for(AccountBalance bal : acc.balances().values()) {
            if (!Utils.isZero(bal.free()) || !Utils.isZero(bal.locked())) {
                sb.append("<tr>");
                sb.append("<td align='left'>" + bal.asset() + "</td>");
                sb.append("<td align='right'>" + Utils.format(bal.free(), 4) + "</td>");
                sb.append("<td align='right'>" + Utils.format(bal.locked(), 4) + "</td>");
                sb.append("</tr>\n");
            }
        }
        sb.append("</table>");
    }

    public void appendOrdersHtml(StringBuilder sb, List<OrderDto> orders, boolean totalRow) {
        sb.append("<table width='98%' cellspacing='0' cellpadding='1' border='1'>");
        sb.append("<tr style='font-weight:bold'><td>#</td><td>orderId</td>\n");
        sb.append("<td>created</td><td>updated</td>\n");
        sb.append("<td>symbol</td><td>side</td><td>type</td>\n");
        sb.append("<td>status</td><td>price</td><td>qnt</td>\n");
        sb.append("<td>executedQty</td><td>commission</td></tr>\n");

        int rowNum = 1;
        double comissionTotal = 0;

        for(OrderDto order : orders) {
            comissionTotal += order.commission();
            LocalDateTime created = Utils.epochDateTime(order.created());
            LocalDateTime updated = Utils.epochDateTime(order.updated());

            sb.append("<tr>");
            sb.append("<td align='center'>" + (rowNum++) + "</td>");
            sb.append("<td align='left'>" + order.orderId() + "</td>");
            sb.append("<td align='left'>" + frmDateTime.format(created) + "</td>");
            sb.append("<td align='left'>" + frmDateTime.format(updated) + "</td>");
            sb.append("<td align='left'>" + order.symbol() + "</td>");
            sb.append("<td align='left'>" + order.side() + "</td>");
            sb.append("<td align='left'>" + order.type() + "</td>");

            String statusStyle = null;
            if ("NEW".equals(order.status())) {
                statusStyle = "color:#008800";
            } else if ("CANCELED".equals(order.status())) {
                statusStyle = "color:#880000";
            }

            sb.append("<td style='" + statusStyle + "' align='left'>" + order.status() + "</td>");
            sb.append("<td align='right'>" + Utils.format(order.price(), 2) + "</td>");
            sb.append("<td align='right'>" + Utils.format(order.quantity(), 2) + "</td>");
            sb.append("<td align='right'>" + Utils.format(order.executedQty(), 2) + "</td>");
            sb.append("<td align='right'>" + Utils.format(order.commission(), 2) + "</td>");
            sb.append("</tr>\n");
        }

        if (totalRow) {
            sb.append("<tr style='font-weight:bold'>");
            sb.append("<td colspan=11 align='right'>Commission total (last 24h):</td>");
            sb.append("<td align='right'>" + Utils.format(comissionTotal, 2) + "</td>");
            sb.append("</tr>\n");
        }
        sb.append("</table>");
    }

    @Override
    public void refresh() {
        refreshPage();
    }
}
