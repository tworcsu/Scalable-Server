package distributed.scaling.task;

import distributed.scaling.threadpool.ThreadPool;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created by toddw on 2/27/17.
 */
public class WriteTask implements Task {
    private ThreadPool threadPool;
    private SelectionKey key;
    private String hash;

    public WriteTask(ThreadPool threadPool, SelectionKey key, String s) {
        this.threadPool = threadPool;
        this.key = key;
        this.hash = s;
    }

    @Override
    public void run() {
        ByteBuffer byteBuffer = ByteBuffer.wrap(this.hash.getBytes());
        SocketChannel channel = (SocketChannel) key.channel();
        try {
            int bytes = channel.write(byteBuffer);
            if (bytes <= 0) {
                System.out.println("Didn't write anything!!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //this.key.interestOps(SelectionKey.OP_READ);
        //this.key.selector().wakeup();



    }
}
