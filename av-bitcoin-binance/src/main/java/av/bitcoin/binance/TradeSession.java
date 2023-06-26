package av.bitcoin.binance;

import av.bitcoin.binance.dto.AccountLoader;
import av.bitcoin.binance.dto.KlinesLoader;
import av.bitcoin.binance.dto.OrderLoader;
import av.bitcoin.common.dto.OrderDto;
import av.bitcoin.common.dto.PingDto;
import av.bitcoin.common.dto.QuoteAggCont;
import av.bitcoin.common.dto.QuoteTickCont;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import av.bitcoin.common.QuoteBar;
import av.bitcoin.common.dto.AccountDto;
import av.bitcoin.common.dto.QuoteTickDto;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;

public class TradeSession {
    private static final Logger log = LoggerFactory.getLogger(TradeSession.class);
    public static final Map<Long, OrderDto> ordersAllDto = new ConcurrentHashMap<>();
    public static final Collection<String> ordersNew = new ConcurrentLinkedQueue<>();
    private static final Map<String, QuoteAggCont> quoteContMap = new ConcurrentHashMap<>();
    private static final Map<String, QuoteTickCont> tickContMap = new ConcurrentHashMap<>();

    public static JSONObject openOrders = null;
    public static JSONObject allOrders = null;
    public static JSONObject ocoOrders = null;
    public static JSONObject myTrades = null;


    private static PingDto pingDto = null;
    private static String userDataListenKey = null;
    public static AccountDto accountStatus = null;


    public static PingDto pingDto() {
        return pingDto;
    }
    public static String userDataListenKey() {
        return userDataListenKey;
    }
    public static QuoteTickCont tickCont(String symbol) {
        return tickContMap.get(symbol);
    }
    public static QuoteAggCont quoteCont(String symbol) {
        return quoteContMap.get(symbol);
    }

    public static OrderDto ordersAllUpdate(OrderDto orderNew, String source) {
        synchronized (ordersAllDto) {
            OrderDto orderOld = ordersAllDto.get(orderNew.orderId());
            if (orderOld == null) {
                ordersAllDto.put(orderNew.orderId(), orderNew);
                return orderNew;
            }
            orderOld.update(orderNew);
            return orderOld;
        }
    }
    public static void updateAccountStatus(String event, JSONObject jsonEvent) {
        accountStatus = AccountLoader.load(jsonEvent);
    }
    public static void updatePing(String event, JSONObject jsonEvent, LocalDateTime created) {
        // {"id":"b83acfb3-0095-4f19-a450-eb77d0405c58","status":200,"result":{}
        int status = jsonEvent.optInt("status");
        pingDto = new PingDto(status, created, LocalDateTime.now());
        log.warn("Received ping response, delayMs={} ms", pingDto.delayMs());
    }
    public static void updateKlines(String symbol, String interval, String event, JSONObject jsonEvent) {
        List<QuoteBar> quotes = KlinesLoader.load(jsonEvent);

        QuoteAggCont cont = null;
        synchronized (quoteContMap) {
            cont = quoteContMap.get(symbol);
            if (cont == null) {
                cont = new QuoteAggCont(symbol, AppConfig.subscribeIntervals());
                quoteContMap.put(symbol, cont);
            }
        }

        cont.update(interval, quotes);
    }

    public static void updateUserDataListenKey(String userDataListenKey) {
        TradeSession.userDataListenKey = userDataListenKey;
    }

    public static OrderDto updateNewOrder(String event, JSONObject jsonEvent, String source) {
        OrderDto orderDto = OrderLoader.loadNewOrder(jsonEvent);
        orderDto = ordersAllUpdate(orderDto, source);
        ordersNew.add(event);
        return orderDto;
    }

    public static OrderDto updateCancelOrder(String event, JSONObject jsonEvent, String source) {
        OrderDto orderDto = OrderLoader.loadCancelOrder(jsonEvent);
        orderDto = ordersAllUpdate(orderDto, source);
        return orderDto;
    }

    public static void updateOcoOrders(String event, JSONObject jsonEvent) {
        ocoOrders = jsonEvent;
    }

    public static void updateOpenOrders(String event, JSONObject jsonEvent, String source) {
        openOrders = jsonEvent;
        List<OrderDto> orders = OrderLoader.loadOrdersAll(jsonEvent);
        for (OrderDto order : orders) {
            ordersAllUpdate(order, source);
        }
    }

    public static void updateAllOrders(String event, JSONObject jsonEvent, String source) {
        allOrders = jsonEvent;
        List<OrderDto> orders = OrderLoader.loadOrdersAll(jsonEvent);
        for(OrderDto order : orders) {
            ordersAllUpdate(order, source);
        }
    }
    public static void updateMyTrades(String event, JSONObject jsonEvent) {
        myTrades = jsonEvent;
        List<OrderDto> orders = OrderLoader.loadMyTrades(jsonEvent);
        for(OrderDto order : orders) {
            ordersAllUpdate(order, "myTrades");
        }
    }

    public static void updateTickStream(QuoteTickDto tick) {
        QuoteTickCont cont = null;
        synchronized (tickContMap) {
            cont = tickContMap.get(tick.symbol);
            if (cont == null) {
                cont = new QuoteTickCont();
                tickContMap.put(tick.symbol, cont);
            }
        }
        cont.update(tick.quoteTick);
    }

    public static void updateQuoteStream(QuoteTickDto tick) {
        QuoteAggCont cont = null;
        synchronized (quoteContMap) {
            cont = quoteContMap.get(tick.symbol);
            if (cont == null) {
                cont = new QuoteAggCont(tick.symbol, AppConfig.subscribeIntervals());
                quoteContMap.put(tick.symbol, cont);
            }
        }
        cont.update(tick.quoteTick);
    }
}
