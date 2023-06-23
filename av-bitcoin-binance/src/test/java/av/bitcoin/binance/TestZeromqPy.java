package av.bitcoin.binance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class TestZeromqPy {
    private static final Logger log = LoggerFactory.getLogger(TestZeromqPy.class);
    private static final String addressSub = "tcp://*:5556";

    //@Test
    public void testSub() {
        try (ZContext context = new ZContext()) {
            try (ZMQ.Socket subscriber = context.createSocket(SocketType.SUB)) {
                subscriber.subscribe(ZMQ.SUBSCRIPTION_ALL);
                subscriber.connect(addressSub);

                while (!Thread.currentThread().isInterrupted()) {
                    String msg = subscriber.recvStr();
                    log.warn(msg);
                }
            }
        }
    }
}
