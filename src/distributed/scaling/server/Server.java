package distributed.scaling.server;

import distributed.scaling.task.ReadTask;
import distributed.scaling.threadpool.ThreadPool;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;

/**
 * Created by toddw on 2/19/17.
 */
public class Server implements Runnable {
    private String hostName;
    private int portNumber;
    private ServerSocketChannel serverChannel;
    private Selector selector;
    private ThreadPool threadPool;

    private int clientCount = 0;

    public Server(int port, int threadPoolSize) throws IOException {
        this.hostName = InetAddress.getLocalHost().getHostAddress();
        this.portNumber = port;
        this.selector = this.selectorInit();
        this.threadPool = new ThreadPool(threadPoolSize);
    }

    private Selector selectorInit() throws IOException {
        Selector socketSelector = SelectorProvider.provider().openSelector();
        this.serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        InetSocketAddress socketAddress = new InetSocketAddress(this.hostName, this.portNumber);
        serverChannel.socket().bind(socketAddress);
        serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);

        return socketSelector;
    }

    public void run() {
        System.out.printf("Server is running on host %s and port %d\n", this.hostName, this.portNumber);
        while (true) {
            try {
                int x = this.selector.select();
                Iterator selectedKeys = this.selector.selectedKeys().iterator();
                while (selectedKeys.hasNext()) {
                    SelectionKey key = (SelectionKey) selectedKeys.next();
                    selectedKeys.remove();
                    if (!key.isValid())
                        continue;
                    if (key.isAcceptable())
                        this.accept(key);
                    else if (key.isReadable()) {
                        this.read(key);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void read(SelectionKey key) throws IOException {
        this.threadPool.addTask(new ReadTask(this.threadPool, key));
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        Socket socket = socketChannel.socket();
        socketChannel.configureBlocking(false);
        socketChannel.register(this.selector, SelectionKey.OP_READ);
        System.out.printf("Accepted a connection from %s\n", socket.getRemoteSocketAddress().toString());
        this.clientCount++;
    }

    public int getClientCount() {
        return this.clientCount;
    }


    public static void main(String[] args) {
        Server server = null;
        try {
            server = new Server(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
            Thread t = new Thread(server);
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                Thread.sleep(5000);
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                System.out.printf("[%s] Current Server Throughput: %f messages/s, Active Client Connections: %d\n",
                        sdf.format(cal.getTime()),server.threadPool.getMessagesProcessed()/5.0, server.getClientCount());
                server.threadPool.resetMessagesProcessed();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}
