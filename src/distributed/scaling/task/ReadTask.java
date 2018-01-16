package distributed.scaling.task;


import distributed.scaling.threadpool.ThreadPool;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by toddw on 2/26/17.
 */
public class ReadTask implements Task {

    private SelectionKey key;
    private ByteBuffer readBuffer = ByteBuffer.allocate(8192);
    private ThreadPool threadPool;

    public ReadTask(ThreadPool threadPool, SelectionKey key) {
        this.key = key;
        this.threadPool = threadPool;
    }

    @Override
    public void run() {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        this.readBuffer.clear();
        int numRead;
        try {
            numRead = socketChannel.read(this.readBuffer);

            if (numRead == -1) {
                key.channel().close();
                key.cancel();
                return;
            }
            else if (numRead == 0) {
                return;
            }
        } catch (IOException e) {
            key.cancel();
            return;
        }
        this.readBuffer.flip();

        byte[] message = (byte[]) this.key.attachment();
        byte[] newMessage;
        if (message == null) {
            newMessage = new byte[numRead];
            System.arraycopy(this.readBuffer.array(),0,newMessage,0,numRead);
        }

        else {
            newMessage = new byte[numRead + message.length];
            System.arraycopy(message, 0, newMessage, 0, message.length);
            System.arraycopy(this.readBuffer.array(), 0, newMessage, message.length, numRead);
        }


        if (newMessage.length == 8192) {
            this.threadPool.incrementmessagesProcessed();

            //Hash data and add write task to queue
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA1");
                byte[] hash = digest.digest(newMessage);
                BigInteger hashInt = new BigInteger(1, hash);
                this.key.attach(null);
                this.threadPool.addTask(new WriteTask(this.threadPool, this.key, hashInt.toString(16)));
                //this.key.interestOps(SelectionKey.OP_WRITE);
                //this.key.selector().wakeup();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        else {
            this.key.attach(newMessage);
        }

    }
}
