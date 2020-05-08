/**
 * @author Jaworski Maciej S18239
 */

package S_PASSTIME_SERVER1;


import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class ClientTask extends FutureTask<String> {

    private static final StringBuilder logger = new StringBuilder();


    public ClientTask(Callable<String> clientTask) {
        super(clientTask);
    }


    public static ClientTask create(Client client, List<String> requests, boolean showSendResponse) {
        return new ClientTask(() -> {
            client.connect();
            if (showSendResponse) {
                System.out.println(client.send("login " + client.getId()));
                requests.forEach(request -> {
                    System.out.println(client.send(request));
                });
                logger.append(client.send("bye and log transfer"));
                System.out.println(logger.toString());
                return logger.toString();
            } else {
                client.send("login " + client.getId());
                requests.forEach(client::send);
                return client.send("bye and log transfer");
            }
        });
    }
}