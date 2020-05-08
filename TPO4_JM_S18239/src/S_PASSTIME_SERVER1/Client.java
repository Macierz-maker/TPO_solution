/**
 * @author Jaworski Maciej S18239
 */

package S_PASSTIME_SERVER1;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class Client {

    private static final Charset codingCharset = StandardCharsets.UTF_8;

    private SocketChannel channel;
    private InetSocketAddress address;
    private String id;
    private ByteBuffer buffer = ByteBuffer.allocate(1024);
    StringBuilder resp;
    private boolean keepReading;

    public Client(String host, int port, String id) {
        this.channel = null;
        this.address = new InetSocketAddress(host, port);
        this.id = id;


    }

    public String getId() {
        return id;
    }

    public void connect() {
        try {
            SocketChannel channel = SocketChannel.open(address);
            channel.configureBlocking(false);
            this.channel = channel;
            while (!channel.finishConnect()) {
                Thread.sleep(50);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String send(String requirement) {
        resp = new StringBuilder();
        try {
            channel.write(codingCharset.encode(String.format("#%s#%s",id, requirement)));
            int i = channel.read(buffer);
            if (i == 0) {
                Thread.sleep(100);
                i = channel.read(buffer);
            }
            buffer.flip();
            resp.append(codingCharset.decode(buffer));
            buffer.clear();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return resp.toString();
    }
}