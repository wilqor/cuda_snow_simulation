package snowflakes.cuda.kask.eti.pg.gda.pl.slave;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import snowflakes.cuda.kask.eti.pg.gda.pl.commons.Commons;
import snowflakes.cuda.kask.eti.pg.gda.pl.commons.TimeLogger;
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

    private final static TimeLogger logger = TimeLogger.getTimeLogger(SlaveEndpoint.class.getSimpleName());
    private final static int FRAME_SIZE = 1200;

    private SlaveParam slaveParam;
    private CudaGate cudaGate;
    private boolean isOpen = true;

    public boolean isOpen() {
        return isOpen;
    }

    public  SlaveEndpoint(URI serverURI) {
        super(serverURI);
        slaveParam = new SlaveParam();
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        logger.log("Connected: " + this.getURI() + "with handshake: " + serverHandshake.getHttpStatusMessage() );
    }

    @Override
    public void onMessage(String message) {
        logger.log("Received message: " + message);
        handleMessage(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        logger.log("Closing with code: " + code + ". Reason: " + reason);
        finishWork();
    }

    private void finishWork() {
        cudaGate.cleanup();
        isOpen = false;
    }

    @Override
    public void onError(Exception e) {
        logger.log("Error: " + e.getMessage());
    }

    private void handleMessage(String message) {
        JSONObject dto =  (JSONObject) JSONValue.parse(message);
        if (dto.containsKey(Commons.MESSAGE_ID)) {
            // 1st message - setup cuda, send back capability
            Integer id = ((Long) dto.get(Commons.MESSAGE_ID)).intValue();
            int capacity;
            cudaGate = new CudaGate(id, slaveParam);
            boolean initSuccess = cudaGate.init();
            if (initSuccess) {
                capacity = cudaGate.getDeviceSnowflakeCapacity();
                logger.log("Successfully initialised Cuda Computation unit with capacity: " + capacity);
            } else {
                capacity = 0;
                logger.log("Could not init Cuda Computation unit");
            }
            JSONObject msg = new JSONObject();
            msg.put(Commons.CAPACITY, new Integer(capacity));
            send(msg.toJSONString());
        } else if (dto.containsKey(Commons.MESSAGE_SNOWFLAKES_COUNT)) {
            // regular message - calculate and send back
            Integer snowFlakesCount;
            Float windForce, windAngle;
            snowFlakesCount = ((Long) dto.get(Commons.MESSAGE_SNOWFLAKES_COUNT)).intValue();
            windForce = ((Double) dto.get(Commons.MESSAGE_WIND_FORCE)).floatValue();
            windAngle = ((Double) dto.get(Commons.MESSAGE_WIND_ANGLE)).floatValue();
            updateSlaveParam(windForce, windAngle, snowFlakesCount);
            calculateAndSend();
        } else if (dto.containsKey(Commons.NO_MORE_WORK)) {
            // end message
            logger.log("Received finish work message from master");
            finishWork();
        }
    }

    private void updateSlaveParam(float windForce, float windAngle, int snowflakesCount) {
        slaveParam.setWindForce(windForce);
        slaveParam.setWindAngle(windAngle);
        slaveParam.setSnowflakesCount(snowflakesCount);
    }

    private void calculateAndSend() {
        Map<Integer, Queue<Float>> snowflakesQueues;
        snowflakesQueues = cudaGate.getNextIteration();
        logger.log("Starting sending...");
        if(snowflakesQueues.size() > FRAME_SIZE){
            TreeMap<Integer, Queue<Float>> fullMessage = new TreeMap<Integer, Queue<Float>>(snowflakesQueues);
            int framesNo = (int)Math.ceil((double)snowflakesQueues.size()/(double)FRAME_SIZE);
            for(int i = 0; i < framesNo; i++){
                JSONObject msg = new JSONObject();
                msg.put(Commons.SNOWFLAKES_PART, fullMessage.subMap(i * FRAME_SIZE, (i + 1) * FRAME_SIZE));
                logger.log("Starting serializing part " + (i + 1) + " of " + framesNo);
                String msgString = msg.toJSONString();
                logger.log("Finished serializing part " + (i + 1) + " of " + framesNo);
                logger.log("Starting sending part " + (i + 1) + " of " + framesNo);
                send(msgString);
                logger.log("Finished sending part " + (i + 1) + " of " + framesNo);
            }
        } else {
            JSONObject msg = new JSONObject();
            msg.put(Commons.SNOWFLAKES_PART, snowflakesQueues);
            logger.log("Starting serializing...");
            String msgString = msg.toJSONString();
            logger.log("Finished serializing");
            logger.log("Starting sending");
            send(msgString);
            logger.log("Finished sending");
        }
        JSONObject finishMessage = new JSONObject();
        finishMessage.put(Commons.PART_FINISH, new Integer(snowflakesQueues.size()));
        String finishMessageString = finishMessage.toJSONString();
        logger.log("Sending finish parts message");
        send(finishMessageString);
        logger.log("Finished sending finish parts message");
        logger.log("Finished sending");
    }
}
