package av.crypto.trade.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import av.crypto.common.Utils;
import av.crypto.common.dto.AccountDto;
import av.crypto.common.dto.AccountDto.AccountBalance;
import av.crypto.common.dto.OrderDto;
import av.crypto.common.QuoteTick;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import static av.crypto.common.Enums.*;

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
            appendOrdersHtml(sb, ordersActive, null, false);

            List<OrderDto> ordersAll = new ArrayList<>();
            ordersAll.addAll(AppMain.clientSession.ordersAll().values());
            ordersAll.sort(Comparator.comparingLong(OrderDto::created));

            HashSet<String> symbolSet = new HashSet<>();
            for(OrderDto order : ordersAll) {
                if (!symbolSet.contains(order.symbol())) {
                    symbolSet.add(order.symbol());
                    sb.append("<br/><h3>All orders: " + order.symbol() + "</h3>\n");
                    appendOrdersHtml(sb, ordersAll, order.symbol(), true);
                }
            }

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

    public void appendOrdersHtml(StringBuilder sb, List<OrderDto> orders, String symbol, boolean totalVisible) {
        sb.append("<table width='98%' cellspacing='0' cellpadding='1' border='1'>");
        sb.append("<tr style='font-weight:bold'><td>#</td><td>orderId</td>\n");
        sb.append("<td>created</td><td>updated</td>\n");
        sb.append("<td>symbol</td><td>side</td><td>type</td>\n");
        sb.append("<td>status</td><td>price</td><td>qnt</td>\n");
        sb.append("<td>executedQty</td><td>commission</td></tr>\n");

        int rowNum = 1;
        double totalQnt = 0;
        double totalProfit = 0;
        double totalComission = 0;

        for(OrderDto order : orders) {
            if (symbol != null && !symbol.equals(order.symbol())) {
                continue;
            }

            totalComission += order.commission();
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
            if (OrderStatus.equals(order.status(), OrderStatus.NEW)) {
                statusStyle = "color:#000066";
            } else if (OrderStatus.equals(order.status(), OrderStatus.CANCELED)) {
                statusStyle = "color:#660000";
            } else if (OrderStatus.equals(order.status(), OrderStatus.FILLED)) {
                statusStyle = "color:#006600";

                if (OrderSide.equals(order.side(), OrderSide.BUY)) {
                    totalQnt += order.quantity();
                    totalProfit -= order.quantity() * order.price();
                } else  if (OrderSide.equals(order.side(), OrderSide.SELL)) {
                    totalQnt -= order.quantity();
                    totalProfit += order.quantity() * order.price();
                }
            }

            sb.append("<td style='" + statusStyle + "' align='left'>" + order.status() + "</td>");
            sb.append("<td align='right'>" + Utils.format(order.price(), 2) + "</td>");
            sb.append("<td align='right'>" + Utils.format(order.quantity(), 2) + "</td>");
            sb.append("<td align='right'>" + Utils.format(order.executedQty(), 2) + "</td>");
            sb.append("<td align='right'>" + Utils.format(order.commission(), 2) + "</td>");
            sb.append("</tr>\n");
        }

        if (totalVisible) {
            if (!Utils.isZero(totalQnt)) {
                QuoteTick lastTick = AppMain.clientSession.symbolTicks().get(symbol);
                if (lastTick != null) {
                    // virtual close all positions using last price
                    totalProfit += totalQnt * lastTick.price();
                }
            }

            sb.append("<tr style='font-weight:bold'>");
            sb.append("<td colspan=8 align='right'>Total (last 24h):</td>");
            sb.append("<td align='right'>" + Utils.format(totalProfit, 2) + "</td>");
            sb.append("<td align='right'>&nbsp;</td>");
            sb.append("<td align='right'>" + Utils.format(totalQnt, 2) + "</td>");
            sb.append("<td align='right'>" + Utils.format(totalComission, 2) + "</td>");
            sb.append("</tr>\n");
        }
        sb.append("</table>");
    }

    @Override
    public void refresh() {
        refreshPage();
    }
}
