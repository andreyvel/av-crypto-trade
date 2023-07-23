package av.crypto.emulator;

import av.crypto.common.Enums;
import av.crypto.common.Enums.OrderSide;
import av.crypto.common.Utils;
import av.crypto.common.dto.AccountDto;
import av.crypto.common.dto.AccountDto.AccountBalance;
import av.crypto.common.dto.OrderDto;
import av.crypto.common.dto.QuoteAggCont;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

public class TradeSession {
    public final AccountDto account = new AccountDto();
    public final AtomicLong orderIdCounter = new AtomicLong(System.nanoTime());

    public final Map<Long, OrderDto> allOrders = new ConcurrentSkipListMap<>();
    public final Map<String, QuoteAggCont> quoteContMap = new ConcurrentHashMap<>();
    public final TradeStreamPublisher tradeStreamPublisher;
    public TradeSession(TradeStreamPublisher tradeStreamPublisher) {
        this.tradeStreamPublisher = tradeStreamPublisher;
    }

    private void makeTransaction(String symbol, double qntAdd, double qntLock, double moneyAdd, double moneyLock) {
        AccountBalance balanceSymbol = account.balances().get(symbol);
        if (balanceSymbol == null) {
            throw new RuntimeException("AccountBalance is not found for symbol=" + symbol);
        }

        AccountBalance balanceMoney = account.balances().get(AppRandomQuote.MONEY_CODE);
        if (balanceMoney == null) {
            throw new RuntimeException("AccountBalance is not found for symbol=" + AppRandomQuote.MONEY_CODE);
        }

        balanceMoney.freeAddCheck(moneyAdd);
        balanceMoney.lockAddCheck(moneyLock);
        balanceSymbol.freeAddCheck(qntAdd);
        balanceSymbol.lockAddCheck(qntLock);

        balanceMoney.freeAdd(moneyAdd);
        balanceMoney.lockAdd(moneyLock);
        balanceSymbol.freeAdd(qntAdd);
        balanceSymbol.lockAdd(qntLock);
    }

    public synchronized void cancelOrder(String symbol, long orderId) {
        OrderDto orderDto = allOrders.get(orderId);
        if (orderDto == null) {
            throw new RuntimeException("Order orderId=" + orderId + " is not found");
        }

        orderDto.status(Enums.OrderStatus.CANCELED.toString());
        double amount = Utils.round(orderDto.quantity() * orderDto.price(), 4);

        if (OrderSide.equals(orderDto.side(), OrderSide.BUY)) {
            makeTransaction(symbol, 0, 0, amount, -amount);
        } else if (OrderSide.equals(orderDto.side(), OrderSide.SELL)) {
            makeTransaction(symbol, orderDto.quantity(), -orderDto.quantity(), 0, 0);
        } else {
            throw new RuntimeException("Unknown operation orderSide=" + orderDto.side());
        }

        JSONObject jsonDto = orderDto.serialize();
        tradeStreamPublisher.send("order_changed", "cancelOrder", jsonDto);

        JSONObject jsonAcc = AppRandomQuote.tradeSession.account.serialize();
        tradeStreamPublisher.send("account_changed", "cancelOrder", jsonAcc);
    }

    public synchronized void marketOrder(String symbol, OrderSide orderSide, double quantity) {
        QuoteAggCont cont = quoteContMap.get(symbol);
        if (cont == null) {
            throw new RuntimeException("Quote is not found for symbol=" + symbol);
        }

        double priceLast = cont.lastTick().price();
        OrderDto orderDto = marketOrder(symbol, orderSide, quantity, priceLast);

        JSONObject jsonDto = orderDto.serialize();
        tradeStreamPublisher.send("order_changed", "marketOrder", jsonDto);

        JSONObject jsonAcc = AppRandomQuote.tradeSession.account.serialize();
        tradeStreamPublisher.send("account_changed", "marketOrder", jsonAcc);
    }

    private OrderDto marketOrder(String symbol, OrderSide orderSide, double quantity, double price) {
        AccountBalance balance = account.balances().get(symbol);
        if (balance == null) {
            throw new RuntimeException("AccountBalance is not found for symbol=" + symbol);
        }

        long orderId = orderIdCounter.incrementAndGet();
        double amount = Utils.round(quantity * price, 4);
        OrderDto orderDto = new OrderDto(orderId, symbol, orderSide.toString(),
                Enums.OrderType.MARKET.toString(), price, quantity);

        if (orderSide == OrderSide.BUY) {
            makeTransaction(symbol, quantity, 0, -amount, 0);
        } else if (orderSide == OrderSide.SELL) {
            makeTransaction(symbol, -quantity, 0, amount, 0);
        } else {
            throw new RuntimeException("Unknown operation orderSide=" + orderSide);
        }

        orderDto.status(Enums.OrderStatus.FILLED.toString());
        long created = Utils.ofEpochMilli(LocalDateTime.now());
        double commision = Utils.round(quantity * price * AppRandomQuote.COMMISSION_MULT, 4);
        orderDto.commission(commision);
        orderDto.created(created);
        orderDto.updated(created);

        allOrders.put(orderId, orderDto);
        return orderDto;
    }

    public void limitOrder(String symbol, OrderSide orderSide, double quantity, double price) {
        OrderDto orderDto = null;

        QuoteAggCont cont = quoteContMap.get(symbol);
        if (cont == null) {
            throw new RuntimeException("Quote is not found for symbol=" + symbol);
        }

        double priceLast = cont.lastTick().price();
        if (orderSide == OrderSide.BUY && price > priceLast) {
            orderDto = marketOrder(symbol, OrderSide.BUY, quantity, priceLast);
        }
        else if (orderSide == OrderSide.SELL && price < priceLast) {
            orderDto = marketOrder(symbol, OrderSide.SELL, quantity, priceLast);
        } else {
            long orderId = orderIdCounter.incrementAndGet();
            double amount = Utils.round(quantity * price, 4);
            orderDto = new OrderDto(orderId, symbol, orderSide.toString(),
                    Enums.OrderType.LIMIT.toString(), price, quantity);

            if (orderSide == OrderSide.BUY) {
                makeTransaction(symbol, 0, 0, -amount, amount);
            } else if (orderSide == OrderSide.SELL) {
                makeTransaction(symbol, -quantity, quantity, 0, 0);
            } else {
                throw new RuntimeException("Unknown operation orderSide=" + orderSide);
            }

            orderDto.status(Enums.OrderStatus.NEW.toString());
            long created = Utils.ofEpochMilli(LocalDateTime.now());
            double commision = Utils.round(quantity * price * AppRandomQuote.COMMISSION_MULT, 4);
            orderDto.commission(commision);
            orderDto.created(created);
            orderDto.updated(created);

            allOrders.put(orderId, orderDto);
        }

        JSONObject jsonDto = orderDto.serialize();
        tradeStreamPublisher.send("order_changed", "limitOrder", jsonDto);

        JSONObject jsonAcc = AppRandomQuote.tradeSession.account.serialize();
        tradeStreamPublisher.send("account_changed", "limitOrder", jsonAcc);
    }
}
