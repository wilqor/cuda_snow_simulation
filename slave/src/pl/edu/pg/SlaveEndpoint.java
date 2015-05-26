package pl.edu.pg;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

/**
 * Created by Ariel on 2015-05-18.
 */
public class SlaveEndpoint extends WebSocketClient {

    public SlaveEndpoint(URI serverURI) {
        super(serverURI);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        System.out.println("Connected: " + this.getURI() + "with handshake: " + serverHandshake.getHttpStatusMessage() );
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Received message: " + message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Closing with code: " + code + ". Reason: " + reason );
    }

    @Override
    public void onError(Exception e) {
        System.out.println("Error: " + e.getMessage());
    }
}
