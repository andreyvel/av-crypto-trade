package av.crypto.binance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import av.crypto.common.Utils;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

public class TradeScheduler {
    private static final Logger log = LoggerFactory.getLogger(TradeScheduler.class);
    private static final int scheduleThreadSleepMs = 300;
    private Thread scheduleThread = null;
    private AtomicBoolean scheduleThreadActive = new AtomicBoolean(false);
    private WsApiBinance wsApiBinance;

    public TradeScheduler(WsApiBinance wsApiBinance) {
        this.wsApiBinance = wsApiBinance;
    }

    public void start() {
        scheduleThread = new Thread(() -> scheduleTask());
        scheduleThread.start();
    }

    public void stop() {
        LocalDateTime start = LocalDateTime.now();
        scheduleThreadActive.set(false); // graceful shutdown

        while (scheduleThread.isAlive()) {
            try {
                Thread.sleep(10);
                long waitMs = Utils.delayMs(start, LocalDateTime.now());
                if (waitMs > scheduleThreadSleepMs * 2) {
                    scheduleThread.interrupt(); // force kill thread
                    break;
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void scheduleTask() {
        scheduleThreadActive.set(true);
        LocalDateTime lastPing = LocalDateTime.now();
        LocalDateTime lastBackup = LocalDateTime.now();

        while (scheduleThreadActive.get()) {
            try {
                Thread.sleep(scheduleThreadSleepMs);

                if (Math.abs(Utils.delayMs(lastPing, LocalDateTime.now())) > 900_000) {
                    lastPing = LocalDateTime.now();
                    wsApiBinance.dataStreamPing(null);
                }
            } catch (Exception e) {
                log.error(null, e);
            }
        }
    }
}
