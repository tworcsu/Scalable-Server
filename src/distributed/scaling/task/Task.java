package distributed.scaling.task;

/**
 * Created by toddw on 2/26/17.
 */
public interface Task extends Runnable {
    public static final int READ_TASK = 1;
    public static final int WRITE_TASK = 2;
}
