package snowflakes.cuda.kask.eti.pg.gda.pl.communication;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import snowflakes.cuda.kask.eti.pg.gda.pl.commons.TimeLogger;
import snowflakes.cuda.kask.eti.pg.gda.pl.message.Chamberlain;

import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ariel on 2015-05-18.
 */
public class MasterEndpoint extends WebSocketServer {

    private final static TimeLogger logger = TimeLogger.getTimeLogger(MasterEndpoint.class.getSimpleName());

    private List<WebSocket> connectionPool = new ArrayList<WebSocket>();

    private static MasterEndpoint instance = null;

    private MasterEndpoint() throws UnknownHostException {
        super(new InetSocketAddress(Inet4Address.getLocalHost().getHostAddress(), 8080));
        logger.log("Server started at: " + Inet4Address.getLocalHost().getHostAddress()+ ":" + 8080);
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
        logger.log("Opening: " + webSocket.getRemoteSocketAddress() + " with handshake: " + clientHandshake.getResourceDescriptor() );
        connectionPool.add(webSocket);
    }

    @Override
    public void onClose(WebSocket webSocket, int code, String reason, boolean remote) {
        logger.log("Closing: " + webSocket.getRemoteSocketAddress() + " with code: " + code + ". Reason: " + reason );
        connectionPool.remove(webSocket);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteBuffer message) {
        logger.log("Message on: " + webSocket.getRemoteSocketAddress() + " Content byte size: " + message.array().length );
        Chamberlain.handle(message.array(), webSocket);
    }

    @Override
    public void onMessage(WebSocket webSocket, String message) {
        logger.log("Message on: " + webSocket.getRemoteSocketAddress() + " Content: " + (message.length()>100?message.substring(0, 100)+" [...] (message print has been shortened)":message) );
        Chamberlain.handle(message, webSocket);
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        logger.log("ERROR on: " + webSocket.getRemoteSocketAddress() + " Error message: " + e.getMessage() );
    }
}
