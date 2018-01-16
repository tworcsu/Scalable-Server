package distributed.scaling.client;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by toddw on 3/6/17.
 */
public class ClientLog implements Runnable {
    private Client client;

    public ClientLog(Client client) {
        this.client = client;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            System.out.printf("[%s] Total Sent Count: %d, Total received count: %d\n",
                    sdf.format(cal.getTime()), client.getSent(), client.getReceived());
            client.resetCounts();
        }
    }
}
