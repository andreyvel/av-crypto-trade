package av.bitcoin.binance;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicInteger;

/*
https://learning-0mq-with-pyzmq.readthedocs.io/en/latest/pyzmq/patterns/client_server.html
Request and server replies to the request (zmq.REQ + zmq.REP)
It will block on recv() to get a request before it can send a reply.
ZMQ REQ sockets can connect to many servers.
The requests will be interleaved or distributed to both the servers.

while True:
    #  Wait for next request from client
    message = socket.recv()
    print "Received request: ", message
    time.sleep (1)
    socket.send("World from %s" % port)

https://learning-0mq-with-pyzmq.readthedocs.io/en/latest/pyzmq/patterns/pubsub.html
Messages are published without the knowledge of what or if any subscriber of that knowledge exists.
*/

public class TestZeromq {
    private static final Logger log = LoggerFactory.getLogger(TestZeromq.class);
    private static final String publisherAddress = "tcp://*:4501";
    private static final String addressPush = "tcp://*:4502";
    private static final String repAddress = "tcp://*:4503";
    private static final AtomicInteger counterReq = new AtomicInteger();
    private static final AtomicInteger counterReseive = new AtomicInteger();
    private static final AtomicInteger serviceReqCounter = new AtomicInteger();
    private static final AtomicInteger counterPush = new AtomicInteger();

    public static void receiverSub(String name) {
        try (ZContext context = new ZContext()) {
            try (ZMQ.Socket subscriber = context.createSocket(SocketType.SUB)) {
                subscriber.subscribe(ZMQ.SUBSCRIPTION_ALL);
                subscriber.connect(publisherAddress);

                while (!Thread.currentThread().isInterrupted()) {
                    String msg = subscriber.recvStr();
                    counterReseive.incrementAndGet();
                }
            }
        }
    }

    @Test
    public void testPubSub() {
        Thread thread1 = new Thread(() -> receiverSub("SUB1"));
        thread1.start();

        Thread thread2 = new Thread(() -> receiverSub("SUB2"));
        thread2.start();

        int countMsg = 0;
        counterReseive.getAndSet(0);

        try (ZContext context = new ZContext()) {
            try (ZMQ.Socket publisher = context.createSocket(SocketType.PUB)) {
                publisher.bind(publisherAddress);

                for(int ind = 0; ind < 10; ind++) {
                    publisher.send("ind=" + ind++);
                }
            }
        }

        Assert.assertEquals(countMsg * 2, counterReseive.get());
    }

    public void receiverPull(String name) {
        try (ZContext context = new ZContext()) {
            try (ZMQ.Socket subscriber = context.createSocket(SocketType.PULL)) {
                subscriber.connect(addressPush);

                while (!Thread.currentThread().isInterrupted()) {
                    String msg = subscriber.recvStr();
                    counterPush.incrementAndGet();
                }
            }
        }
    }

    @Test
    public void testPushPull() throws Exception {
        Thread thread1 = new Thread(() -> {receiverPull("PULL1");});
        thread1.start();

        Thread thread2 = new Thread(() -> {receiverPull("PULL2");});
        thread2.start();

        int messageNum = 10;
        counterPush.getAndSet(0);

        try (ZContext context = new ZContext()) {
            try (ZMQ.Socket publisher = context.createSocket(SocketType.PUSH)) {
                publisher.bind(addressPush);

                for(int ind = 0; ind < messageNum; ind++) {
                    publisher.send("ind=" + ind++);
                }
            }
        }

        //Thread.sleep(100);
        //Assert.assertEquals(messageNum, counterPush.get());
    }

    @Test
    public void testTimeUtc() {
        long timestamp = 1682703707185L;
        LocalDateTime triggerTime0 = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp),  ZoneOffset.UTC);

        String str = "{\"id\":\"5873e3b3-ebbc-4bec-b521-7c396e8f83bf\",\"status\":200,\"result\":{\"serverTime\":1682704640798},\"rateLimits\":[{\"rateLimitType\":\"REQUEST_WEIGHT\",\"interval\":\"MINUTE\",\"intervalNum\":1,\"limit\":1200,\"count\":4}]}";
        JSONObject obj = new JSONObject(str);
        long val = obj.getJSONObject("result").getLong("serverTime");
    }

    public void serviceReq(String name) {
        try (ZContext context = new ZContext()) {
            try (ZMQ.Socket subscriber = context.createSocket(SocketType.REQ)) {
                subscriber.connect(repAddress);

                while (!Thread.currentThread().isInterrupted()) {
                    String msgSend = name + ":" + counterReq.addAndGet(1);
                    subscriber.send(msgSend);

                    String msgResp = subscriber.recvStr();
                    Assert.assertTrue(msgResp.contains(msgSend));
                    serviceReqCounter.incrementAndGet();
                }
            }
        }
    }

    @Test
    public void testReqRep() throws Exception {
        Thread thread1 = new Thread(() -> {serviceReq("REQ1");});
        thread1.start();

        Thread thread2 = new Thread(() -> {serviceReq("REQ2");});
        thread2.start();

        int messageNum  = 42;
        try (ZContext context = new ZContext()) {
            try (ZMQ.Socket service = context.createSocket(SocketType.REP)) {
                service.bind(repAddress);

                for(int ind = 0; ind < messageNum; ind++) {
                    String req = service.recvStr();
                    service.send("=" + req);
                }
            }
        }

        for(int ind = 0; ind < 10; ind++) {
            if (messageNum * 2 != serviceReqCounter.get()) {
                Thread.sleep(10);
            }
        }
        Assert.assertEquals(messageNum, serviceReqCounter.get());
    }
}
