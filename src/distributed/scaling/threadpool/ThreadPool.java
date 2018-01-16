package distributed.scaling.threadpool;

import distributed.scaling.task.Task;
import distributed.scaling.util.MyBlockingQueue;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by toddw on 2/20/17.
 */
public class ThreadPool {
    public MyBlockingQueue tasks;
    private List<PThread> threads;
    private int messagesProcessed = 0;

    public synchronized void incrementmessagesProcessed() {
        this.messagesProcessed++;
    }

    public synchronized void resetMessagesProcessed() {
        this.messagesProcessed = 0;
    }

    public synchronized int getMessagesProcessed() {
        return this.messagesProcessed;
    }

    public ThreadPool(int numberOfThreads) {
        threads = new ArrayList<>();
        tasks = new MyBlockingQueue();

        for (int i = 0; i < numberOfThreads; i++) {
            threads.add(new PThread(this));
        }

        for (PThread thread : threads) {
            thread.start();
        }
    }

    public void addTask(Task task) {
        this.tasks.add(task);

    }

    public Object removeTask() throws InterruptedException {
        return this.tasks.remove();
    }
}
