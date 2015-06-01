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
import java.util.TreeMap;

/**
 * Created by Ariel on 2015-05-18.
 */
public class SlaveEndpoint extends WebSocketClient {

    private SlaveParam slaveParam;
    private CudaGate cudaGate;

    public  SlaveEndpoint(URI serverURI) {

        super(serverURI);
        slaveParam = new SlaveParam();
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
            Integer id, snowFlakesCount;
            id =  ((Long) dto.get(Commons.MESSAGE_ID)).intValue();
            snowFlakesCount = ((Long) dto.get(Commons.MESSAGE_SNOWFLAKES_COUNT)).intValue();
            updateSlaveParam( ((Double) dto.get(Commons.MESSAGE_WIND_FORCE)).floatValue(),
                ((Double) dto.get(Commons.MESSAGE_WIND_ANGLE)).floatValue());
            // initialize
            cudaGate = new CudaGate(snowFlakesCount, id, slaveParam);
            calculateAndSend();
        } else {
            updateSlaveParam( ((Double) dto.get(Commons.MESSAGE_WIND_FORCE)).floatValue(),
                    ((Double) dto.get(Commons.MESSAGE_WIND_ANGLE)).floatValue());
            calculateAndSend();
        }
    }

    private void updateSlaveParam(float windForce, float windAngle) {
        slaveParam.setWindForce(windForce);
        slaveParam.setWindAngle(windAngle);
    }

    private void calculateAndSend() {
        Map<Integer, Queue<Float>> snowflakesQueues;
        snowflakesQueues = cudaGate.getNextIteration();

        final int FRAME_SIZE = 1200;

        if(snowflakesQueues.size() > FRAME_SIZE){
            TreeMap<Integer, Queue<Float>> fullMessage = new TreeMap<Integer, Queue<Float>>(snowflakesQueues);

            int framesNo = (int)Math.ceil((double)snowflakesQueues.size()/(double)FRAME_SIZE);

            for(int i = 0; i < framesNo; i++){
                send(JSONValue.toJSONString(fullMessage.subMap(i*FRAME_SIZE, (i+1)*FRAME_SIZE)));
            }

        } else {
            send(JSONValue.toJSONString(snowflakesQueues));
        }
    }
}
