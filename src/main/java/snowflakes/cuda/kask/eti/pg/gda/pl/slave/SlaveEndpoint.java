package snowflakes.cuda.kask.eti.pg.gda.pl.slave;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import snowflakes.cuda.kask.eti.pg.gda.pl.commons.Commons;
import snowflakes.cuda.kask.eti.pg.gda.pl.cuda.CudaGate;

import java.net.URI;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Queue;

/**
 * Created by Ariel on 2015-05-18.
 */
public class SlaveEndpoint extends WebSocketClient {

    private Integer id, snowFlakesCount;
    private Float windForce, windAngle;
    private CudaGate cudaGate;

    public  SlaveEndpoint(URI serverURI) {
        super(serverURI);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        System.out.println("Connected: " + this.getURI() + "with handshake: " + serverHandshake.getHttpStatusMessage() );
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Received message: " + message);
        handleMessage(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Closing with code: " + code + ". Reason: " + reason );
    }

    @Override
    public void onError(Exception e) {
        System.out.println("Error: " + e.getMessage());
    }

    private void handleMessage(String message) {
        JSONObject dto =  (JSONObject) JSONValue.parse(message);
        System.out.println(dto.toString());
        // 1st message
        if (dto.containsKey(Commons.MESSAGE_ID)) {
            id =  ((Long) dto.get(Commons.MESSAGE_ID)).intValue();
            snowFlakesCount = ((Long) dto.get(Commons.MESSAGE_SNOWFLAKES_COUNT)).intValue();
            windForce = ((Double) dto.get(Commons.MESSAGE_WIND_FORCE)).floatValue();
            windAngle = ((Double) dto.get(Commons.MESSAGE_WIND_ANGLE)).floatValue();
            // initialize
            cudaGate = new CudaGate(snowFlakesCount, id);
            calculateAndSend();
        } else {
            windForce = ((Double) dto.get(Commons.MESSAGE_WIND_FORCE)).floatValue();
            windAngle = ((Double) dto.get(Commons.MESSAGE_WIND_ANGLE)).floatValue();
            calculateAndSend();
        }
    }

    private void calculateAndSend() {
        Map<Integer, Queue<Float>> snowflakesQueues;
        snowflakesQueues = cudaGate.getNextIteration(windForce, windAngle);
        send(JSONValue.toJSONString(snowflakesQueues));
    }
}
