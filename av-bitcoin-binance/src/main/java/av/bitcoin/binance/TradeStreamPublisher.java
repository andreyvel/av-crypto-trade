package av.bitcoin.binance;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class TradeStreamPublisher {
    private static final Logger log = LoggerFactory.getLogger(TradeStreamPublisher.class);
    private ZContext zmqContext = null;
    private ZMQ.Socket zmqStreamPublisher = null;

    public void start() {
        log.warn("Starting ZMQ trade events publisher: {}", AppConfig.zmqTradeStreamPub());
        zmqContext = new ZContext();
        zmqStreamPublisher = zmqContext.createSocket(SocketType.PUB);
        zmqStreamPublisher.bind(AppConfig.zmqTradeStreamPub());
    }

    public void stop() {
        if (!zmqContext.isClosed()) {
            log.warn("Stop ZMQ trade events publisher: {}", AppConfig.zmqTradeStreamPub());
            zmqStreamPublisher.close();
            zmqContext.close();
        }
    }

    public synchronized void send(String eventType, String source, JSONObject jsonObj) {
        if (!zmqContext.isClosed()) {
            jsonObj.put("event_type", eventType);
            jsonObj.put("source", source);
            zmqStreamPublisher.send(jsonObj.toString());
        }
    }
}
