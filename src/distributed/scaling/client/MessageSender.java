package distributed.scaling.client;

import java.util.Random;

/**
 * Created by toddw on 3/5/17.
 */
public class MessageSender implements Runnable {
    private Client client;
    public MessageSender(Client client) {
        this.client = client;
    }


    @Override
    public void run() {
        try {
            while (true) {
                byte[] data = new byte[8192];
                new Random().nextBytes(data);
                String hash = client.SHA1FromBytes(data);
                client.addHash(hash);
                client.sendData(data);
                Thread.sleep(1000 / client.getMessageRate());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
