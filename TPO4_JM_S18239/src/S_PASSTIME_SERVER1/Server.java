/**
 * @author Jaworski Maciej S18239
 */

package S_PASSTIME_SERVER1;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private static final Charset codingCharset = StandardCharsets.UTF_8;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Selector selector;
    private ServerSocketChannel serverChannel;
    private boolean keepProcessing;
    private ByteBuffer buffer;
    private String host;
    private int port;
    private Map<String, List<String>> clientsMap;
    private List<String> requestHistory;

    public Server(String host, int port) {
        try {
            requestHistory = new LinkedList<>();
            this.host = host;
            this.port = port;
            this.selector = Selector.open();

            this.keepProcessing = true;
            this.clientsMap = new HashMap<>();
            buffer = ByteBuffer.allocate(1024);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private ServerSocketChannel getServerChannel(String host, int port) throws IOException {
        ServerSocketChannel channel = ServerSocketChannel.open();
        channel.configureBlocking(false);

        InetSocketAddress serverAddress = new InetSocketAddress(host, port);
        channel.socket().bind(serverAddress);
        channel.register(selector, SelectionKey.OP_ACCEPT);
        return channel;
    }


    public void startServer() {
        executor.execute(() -> {
            try {
                this.serverChannel = getServerChannel(host, port);
                this.serverChannel.register(selector, SelectionKey.OP_ACCEPT);
                while (keepProcessing) {
                    selector.select();
                    Set<SelectionKey> keys = selector.selectedKeys();
                    requestHandler(keys);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void stopServer() {
        keepProcessing = false;
        executor.shutdownNow();
    }

    public String getServerLog() {
        String allHistory = "";
        for (int i = 0; i < requestHistory.size(); i++) {
            allHistory = allHistory + requestHistory.get(i) + "\n";
        }
        return allHistory;
    }

    private void requestHandler(Set<SelectionKey> keys) throws IOException {
        for (SelectionKey key : keys) {
            if (key.isAcceptable()) {
                SocketChannel clientChannel = serverChannel.accept();
                clientChannel.configureBlocking(false);
                clientChannel.register(selector, SelectionKey.OP_READ);
            }
            if (key.isReadable()) {
                SocketChannel clientChannel = (SocketChannel) key.channel();
                clientChannel.read(buffer);
                buffer.flip();
                String request = codingCharset.decode(buffer).toString().substring(1);
                int indexOfHash = request.indexOf('#');
                String clientID = request.substring(0, indexOfHash);
                request = request.substring(indexOfHash + 1);
                requestHistory.add(respond(request, clientID));
                addLog(request, clientID);
                clientChannel.write(codingCharset.encode(addMessage(request, clientID)));
                buffer.clear();
            }
        }
        keys.clear();
    }

    private String respond(String request, String id) throws IOException {
        StringBuilder response = new StringBuilder(id);
        if (request.contains("login")) {
            response.append(String.format(" logged in at %s", LocalTime.now()));
        } else if (request.contains("bye")) {
            response.append(String.format(" logged out at %s", LocalTime.now()));
        } else {
            response.append(String.format(" request at %s: \"%s\"", LocalTime.now(), request));
        }
        return response.toString();
    }

    private void addLog(String request, String id) {
        if (!clientsMap.containsKey(id))
            clientsMap.put(id, new LinkedList<>());
        if (request.contains("login"))
            clientsMap.get(id).add("logged in");
        else if (request.contains("bye"))
            clientsMap.get(id).add("logged out");
        else {
            clientsMap.get(id).add("Request: " + request);
            clientsMap.get(id).add("Result: ");
            String[] tabOfLog = request.split(" ");
            clientsMap.get(id).add(Time.passed(tabOfLog[0], tabOfLog[1]));
        }
    }

    private String addMessage(String request, String id) {
        StringBuilder logs = new StringBuilder();
        if (request.equals("bye"))
            logs.append("logged out");
        else if (request.equals("bye and log transfer")) {
            logs.append(String.format("=== %s log start ===\n", id));
            for (String log : clientsMap.get(id)) {
                logs.append(log).append("\n");
            }
            logs.append(String.format("=== %s log end ===\n", id));
        } else if (request.split(" ")[0].equals("login"))
            logs.append("logged in");
        else {
            String[] tabOfLog = request.split(" ");
            logs.append(Time.passed(tabOfLog[0], tabOfLog[1]));
        }
        return logs.toString();
    }
}