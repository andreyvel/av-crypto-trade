package av.crypto.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NanoTimer implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(NanoTimer.class.getName());

    private String message;
    private int testNum;
    private long startTime = System.nanoTime();

    public NanoTimer(String message, int testNum)
    {
        this.testNum = testNum;
        this.message = message;
    }

    @Override
    public void close() {
        long deltaMs = (System.nanoTime() - startTime) / 1000000;
        long perfSec = (deltaMs == 0) ? 0 :  1000L * testNum / deltaMs;

        long deltaMcs = (System.nanoTime() - startTime) / 1000;
        log.warn(message + ": OperationNum=" + testNum  + ", TotalMs=" + deltaMcs / 1000 + ", PerfSec=" + perfSec);
    }
}
