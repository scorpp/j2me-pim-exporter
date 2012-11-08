package executor;

import logger.Logger;
import util.Queue;

/**
 * Thread maintaining a queue of tasks waiting for execution. Intended to execute commands out of UI thread.
 */
public class ExecutorThread extends Thread {

    private final Logger logger;

    private Queue runnables = new Queue(5);


    public ExecutorThread() {
        logger = new Logger(getClass());
    }

    public void run() {
        logger.info("Thread started, waiting for commands");

        while (true) {
            if (runnables.isEmpty()) {
                try {
                    logger.debug("Suspended");
                    synchronized (this) {
                        wait();
                    }
                } catch (InterruptedException e) {
                    return;
                }
            }

            if (!runnables.isEmpty()) {
                Runnable r = (Runnable) runnables.pop();
                try {
                    r.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public synchronized void doInThread(Runnable r) {
        runnables.push(r);
        notify();
    }
}
