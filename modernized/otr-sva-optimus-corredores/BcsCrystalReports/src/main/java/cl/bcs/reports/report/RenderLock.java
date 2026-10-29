package cl.bcs.reports.report;

import java.util.concurrent.locks.ReentrantLock;

/**
 * One fair lock for the whole JRC critical section (open template, bind data, export, close; also template
 * upload/update, which open the document too). The Crystal runtime keeps global state and
 * {@link ListMapResultSet} keeps its column metadata in a static field, so two concurrent renders with different row
 * shapes could read each other's metadata. The legacy servlet container did not serialize either; this removes the
 * race without touching ListMapResultSet. Fair = FIFO, so a long render does not starve the others.
 */
public final class RenderLock {

    private static final ReentrantLock LOCK = new ReentrantLock(true);

    private RenderLock() {
    }

    public static void lock() {
        LOCK.lock();
    }

    public static void unlock() {
        LOCK.unlock();
    }
}
