package distributed.scaling.threadpool;


/**
 * Created by toddw on 2/20/17.
 */
public class PThread extends Thread {
    private ThreadPool pool;

    public PThread(ThreadPool pool) {
        this.pool = pool;
    }

    public void run() {
        while (true) {
            Runnable task = null;
            try {
                task = (Runnable) pool.removeTask();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            task.run();
        }
    }
}
