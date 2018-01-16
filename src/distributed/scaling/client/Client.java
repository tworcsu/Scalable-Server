package distributed.scaling.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;

import static java.lang.Integer.parseInt;

/**
 * Created by toddw on 2/19/17.
 */
public class Client {
    private int serverPortNumber;
    private String serverHostName;
    private int messageRate;
    private SocketChannel channel;
    private Selector selector;
    LinkedList<String> hashList;
    private ByteBuffer readBuffer = ByteBuffer.allocate(40);
    private int numReceived = 0;
    private int numSent = 0;

    public Client(String serverHostName, int serverPortNumber, int messageRate) throws IOException {
        this.serverHostName = serverHostName;
        this.serverPortNumber = serverPortNumber;
        this.messageRate = messageRate;
        this.selector = Selector.open();
        this.channel = connectionInit();
        this.channel.finishConnect();
        this.hashList = new LinkedList<>();
    }

    private SocketChannel connectionInit() throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress(this.serverHostName, this.serverPortNumber));

        return socketChannel;
    }

    public SocketChannel getChannel() {
        return this.channel;
    }

    public void sendData(byte[] data) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        int bytes = this.channel.write(byteBuffer);
        if (bytes != 8192)
            System.out.println("Didn't write all the bytes");
        this.incrementSent();
    }

    public void resetCounts() {
        this.numReceived = 0;
        this.numSent = 0;
    }

    public void incrementReceived() {
        this.numReceived++;
    }

    public void incrementSent() {
        this.numSent++;
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: Client server_host server_port message_rate");
        }

        try {
            Client client = new Client(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));

            if (client.channel.finishConnect()) {
                System.out.println("Begin sending");
                new Thread(new MessageSender(client)).start();
                new Thread(new ClientLog(client)).start();
            }
            else {
                System.out.println("Did not connect");
            }
            while (true) {
                client.read();
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void read() throws UnsupportedEncodingException {
        this.incrementReceived();
        this.readBuffer.clear();
        int numRead = 0;
        try {
            numRead = channel.read(this.readBuffer);
            if (numRead <= 0) {
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.readBuffer.flip();
        String hashString = new String(this.readBuffer.array(), "UTF-8");
        if (numRead < 40)
            hashString = hashString.substring(0,numRead);
        if (!this.hashList.remove(hashString)) {
            System.out.println("Bad hash");
            System.out.println(hashString);
        }
    }


    public synchronized void addHash(String s) {
        this.hashList.add(s);
    }

    public boolean removeHash(String s) {
        return this.hashList.remove(s);
    }

    public int hashListSize() {
        return this.hashList.size();
    }

    public static String SHA1FromBytes(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA1");
        byte[] hash = digest.digest(data);
        BigInteger hashInt = new BigInteger(1, hash);
        return hashInt.toString(16);
    }


    public int getMessageRate() {
        return messageRate;
    }

    public int getSent() {
        return this.numSent;
    }

    public int getReceived() {
        return this.numReceived;
    }
}
