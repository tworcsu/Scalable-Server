package distributed.scaling.util;

import distributed.scaling.task.Task;

import java.util.LinkedList;

/**
 * Created by toddw on 2/22/17.
 */
public class MyBlockingQueue {
    private LinkedList<Task> queue;

    public MyBlockingQueue() {
        this.queue = new LinkedList<>();
    }

    public synchronized void add(Task o) {
        if (this.queue.size() == 0)
            notifyAll();
        this.queue.add(o);

    }

    public synchronized Object remove() throws InterruptedException{
        while (this.queue.size() == 0)
            wait();
        return this.queue.remove();
    }

    public int size() {
        return queue.size();
    }
}
