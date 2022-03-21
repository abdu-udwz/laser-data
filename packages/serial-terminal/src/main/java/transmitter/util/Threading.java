package transmitter.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public abstract class Threading {

    public final static ExecutorService FIXED_POOL = Executors.newFixedThreadPool(1);
    public final static ExecutorService SERIAL_EVENTS_POOL = Executors.newFixedThreadPool(1);
    public final static ScheduledExecutorService SCHEDULED_POOL = Executors.newScheduledThreadPool(2);

    private Threading() {
    }

    public static void shutdownAll(boolean shutdownNow) {

        if (shutdownNow) {
            FIXED_POOL.shutdownNow();
            SCHEDULED_POOL.shutdownNow();
            SERIAL_EVENTS_POOL.shutdownNow();
            return;
        }

        SERIAL_EVENTS_POOL.shutdown();
        SCHEDULED_POOL.shutdown();
        FIXED_POOL.shutdown();
    }

}
