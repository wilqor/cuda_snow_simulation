package snowflakes.cuda.kask.eti.pg.gda.pl.communication;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import snowflakes.cuda.kask.eti.pg.gda.pl.message.Chamberlain;

import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ariel on 2015-05-18.
 */
public class MasterEndpoint extends WebSocketServer {

    private List<WebSocket> connectionPool = new ArrayList<WebSocket>();

    private static MasterEndpoint instance = null;

    private MasterEndpoint() throws UnknownHostException {
        super(new InetSocketAddress(Inet4Address.getLocalHost().getHostAddress(), 8080));
        System.out.println("Server started at: " + Inet4Address.getLocalHost().getHostAddress()+ ":" + 8080);
    }

    public static MasterEndpoint getInstance() throws UnknownHostException {
        if(instance == null) instance = new MasterEndpoint();
        return instance;
    }

    public List<WebSocket> getConnectionPool() {
        return connectionPool;
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        System.out.println("Opening: " + webSocket.getRemoteSocketAddress() + " with handshake: " + clientHandshake.getResourceDescriptor() );
        connectionPool.add(webSocket);
    }

    @Override
    public void onClose(WebSocket webSocket, int code, String reason, boolean remote) {
        System.out.println("Closing: " + webSocket.getRemoteSocketAddress() + " with code: " + code + ". Reason: " + reason );
        connectionPool.remove(webSocket);
    }

    @Override
    public void onMessage(WebSocket webSocket, String message) {
        System.out.println("Message on: " + webSocket.getRemoteSocketAddress() + " Content: " + (message.length()>100?message.substring(0, 100)+" [...] (message print has been shortened)":message) );
        Chamberlain.handle(message);
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        System.out.println("ERROR on: " + webSocket.getRemoteSocketAddress() + " Error message: " + e.getMessage() );
    }
}
