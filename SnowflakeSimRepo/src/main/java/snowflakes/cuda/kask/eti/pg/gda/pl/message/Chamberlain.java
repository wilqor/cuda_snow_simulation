package snowflakes.cuda.kask.eti.pg.gda.pl.message;

import com.google.protobuf.InvalidProtocolBufferException;
import org.java_websocket.WebSocket;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import snowflakes.cuda.kask.eti.pg.gda.pl.commons.Commons;
import snowflakes.cuda.kask.eti.pg.gda.pl.commons.SnowflakeMessages;
import snowflakes.cuda.kask.eti.pg.gda.pl.commons.TimeLogger;
import snowflakes.cuda.kask.eti.pg.gda.pl.communication.MasterEndpoint;
import snowflakes.cuda.kask.eti.pg.gda.pl.main.SnowflakeSimMain;
import snowflakes.cuda.kask.eti.pg.gda.pl.snowflakes.Snowflake;
import static snowflakes.cuda.kask.eti.pg.gda.pl.commons.SnowflakeMessages.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Ariel on 2015-05-18.
 */
public class Chamberlain {

    private final static TimeLogger logger = TimeLogger.getTimeLogger(Chamberlain.class.getSimpleName());

    public static int CONNECTIONS_NUMBER;
    private static int MAX_QUEUE_SIZE = 28000;
    private static int ACTIVE_SNOWFLAKE_QUEUE_SIZE = 20;
    private static int DISPLAY_LIMIT = 500;
    private static int DISPLAY_LIMIT_INITIAL = 300;

    private static int initResponses = 0, snowFlakesRemaining, snowFlakesDistributed;
    private static Map<WebSocket, Integer> slaveCapacities = new HashMap<WebSocket, Integer>();

    private static long startTimeStamp, endTimeStamp;

    private static void initSnowFlakeCounters() {
        snowFlakesRemaining = Commons.SNOWFLAKES_NUMBER;
        snowFlakesDistributed = 0;
    }

    private static int calculatePartSize(int capacity) {
        return Math.min(snowFlakesRemaining - snowFlakesDistributed, capacity);
    }

    public static void startTime() {
        startTimeStamp = (new Date()).getTime();
        logger.log("STARTING time measure");
    }

    private static void endTime() {
        endTimeStamp = (new Date()).getTime();
        logger.log("ENDING time measure");
    }

    private static void sendPartToSlave(WebSocket socket) {
        // check remaining size and distribute
        int partSize = calculatePartSize(slaveCapacities.get(socket));
        // send, update counters
        SnowflakeData.Builder msgBuilder = SnowflakeData.newBuilder();
        if(partSize != 0) {
            msgBuilder.setSnowflakeTask(
                    SnowflakeData.SnowflakeTask.newBuilder()
                            .setCurrentSnowflakesCount(partSize)
                            .setWindForce(SnowflakeSimMain.getTransferWindForce())
                            .setWindAngle(SnowflakeSimMain.windAngle).build());
            snowFlakesDistributed += partSize;
        } else {
            msgBuilder.setEndOfWork(true);
        }
        logger.log("Remaining is: " + snowFlakesRemaining + " distributed is: " + snowFlakesDistributed);
        socket.send(msgBuilder.build().toByteArray());
        if(partSize != 0) logger.log("Sent PART: " + partSize + " to: " + socket.getRemoteSocketAddress());
        else logger.log("Sent NO MORE WORK to: " + socket.getRemoteSocketAddress());
        
        

//        JSONObject msg = new JSONObject();
//        if (partSize != 0) {
//            msg.put(Commons.MESSAGE_SNOWFLAKES_COUNT, new Integer(partSize));
//            msg.put(Commons.MESSAGE_WIND_FORCE, new Float(SnowflakeSimMain.getTransferWindForce()));
//            msg.put(Commons.MESSAGE_WIND_ANGLE, new Float(SnowflakeSimMain.windAngle));
//            // LOG
//            logger.log("Remaining is: " + snowFlakesRemaining + " distributed is: " + snowFlakesDistributed);
//            socket.send(msg.toJSONString());
//            logger.log("Sent PART: " + msg.toJSONString() + " to: " + socket.getRemoteSocketAddress());
//            snowFlakesDistributed += partSize;
//        }
//        // send finish
//        else {
//            msg.put(Commons.NO_MORE_WORK, new Integer(0));
//            logger.log("Remaining is: " + snowFlakesRemaining + " distributed is: " + snowFlakesDistributed);
//            socket.send(msg.toJSONString());
//            logger.log("Sent NO MORE WORK: " + msg.toJSONString() + " to: " + socket.getRemoteSocketAddress());
//        }
    }

    public static void handle(byte[] message, WebSocket ws){
        SnowflakeData snowflakeData = null;
        try {
            snowflakeData = SnowflakeData.parseFrom(message);

        } catch (InvalidProtocolBufferException e) {
            logger.log("Invalid message! Please check protobuf.");
        }
        if(snowflakeData == null){
            logger.log("Invalid message! Message null! Please check protobuf.");
            return;
        }

        if(snowflakeData.hasSlaveCapacity()){
            Integer capacity = snowflakeData.getSlaveCapacity();
            if (capacity.equals(0)) {
                logger.log("Slave cannot compute");
            } else {
                // add to dictionary
                slaveCapacities.put(ws, capacity);
            }
            initResponses++;
            if (initResponses == CONNECTIONS_NUMBER) {
                initSnowFlakeCounters();
                logger.log("Finished gathering capacities, sending initial parts...");
                // send part to everyone
                for (WebSocket socket : slaveCapacities.keySet()) {
                    sendPartToSlave(socket);
                }
                logger.log("Finished sending initial parts");
            }
        } else if (snowflakeData.getMapFieldCount() > 0 && !snowflakeData.hasTotalMessageSize()){
            Map<Integer, Queue<Float>> receivedMap = new HashMap<Integer, Queue<Float>>();
            for(SnowflakeData.MyMapField field : snowflakeData.getMapFieldList()){
                receivedMap.put(field.getKey(), new ConcurrentLinkedQueue<Float>(field.getValueList()));
            }
            logger.log("Finished deserialization");
            handlePartsMessage(receivedMap);
        } else if (snowflakeData.getMapFieldCount() > 0 && snowflakeData.hasTotalMessageSize()){
            Map<Integer, Queue<Float>> receivedMap = new HashMap<Integer, Queue<Float>>();
            for(SnowflakeData.MyMapField field : snowflakeData.getMapFieldList()){
                receivedMap.put(field.getKey(), new ConcurrentLinkedQueue<Float>(field.getValueList()));
            }
            logger.log("Finished deserialization");
            handlePartsMessage(receivedMap);

            // finished task
            Integer size = snowflakeData.getTotalMessageSize();
            logger.log("Received finish of parts with size: " + size);
            // reduce counters
            snowFlakesDistributed -= size;
            snowFlakesRemaining -= size;
            if (snowFlakesRemaining == 0) {
                endTime();
                logger.log("FINISHED CALCULATIONS - overall time is: " + (endTimeStamp - startTimeStamp) + " ms");
            }
            // send next part
            sendPartToSlave(ws);

        }
    }

    public static void handle(String message, WebSocket ws){
        JSONObject dto = (JSONObject) JSONValue.parse(message);
        if (dto.containsKey(Commons.CAPACITY)) {
            // init response from slave
            Integer capacity = ((Long) dto.get(Commons.CAPACITY)).intValue();
            if (capacity.equals(0)) {
                logger.log("Slave cannot compute");
            } else {
                // add to dictionary
                slaveCapacities.put(ws, capacity);
            }
            initResponses++;
            if (initResponses == CONNECTIONS_NUMBER) {
                initSnowFlakeCounters();
                logger.log("Finished gathering capacities, sending initial parts...");
                // send part to everyone
                for (WebSocket socket : slaveCapacities.keySet()) {
                    sendPartToSlave(socket);
                }
                logger.log("Finished sending initial parts");
            }
        } else if (dto.containsKey(Commons.SNOWFLAKES_PART)) {
            // data part
            logger.log("Starting deserialization...");
            Map<String, JSONArray> rawMessageParsed = (Map<String, JSONArray>) dto.get(Commons.SNOWFLAKES_PART);
            //handlePartsMessage(rawMessageParsed);
        } else if (dto.containsKey(Commons.PART_FINISH)) {
            // finished task
            Integer size = ((Long) dto.get(Commons.PART_FINISH)).intValue();
            logger.log("Received finish of parts with size: " + size);
            // reduce counters
            snowFlakesDistributed -= size;
            snowFlakesRemaining -= size;
            if (snowFlakesRemaining == 0) {
                endTime();
                logger.log("FINISHED CALCULATIONS - overall time is: " + (endTimeStamp - startTimeStamp) + " ms");
            }
            // send next part
            sendPartToSlave(ws);
        }
    }

    private static void handlePartsMessage(Map<Integer, Queue<Float>> receivedSnowflakesQueues) {
        if (countQueuesOversize(ACTIVE_SNOWFLAKE_QUEUE_SIZE) < DISPLAY_LIMIT_INITIAL) {

            for(int id : receivedSnowflakesQueues.keySet()){
                // do not exceed max display limit
                if (countQueuesOversize(ACTIVE_SNOWFLAKE_QUEUE_SIZE) > DISPLAY_LIMIT) {
                    break;
                }
                SnowflakeSimMain.snowflakeSizes.put(id, receivedSnowflakesQueues.get(id).poll());

                if(!SnowflakeSimMain.snowflakesQueues.containsKey(id) || SnowflakeSimMain.snowflakesQueues.get(id) == null) {
                    SnowflakeSimMain.snowflakesQueues.put(id, new ConcurrentLinkedQueue<Float>(receivedSnowflakesQueues.get(id)));
                }
                else if (SnowflakeSimMain.snowflakesQueues.get(id).size() < MAX_QUEUE_SIZE) {
                    SnowflakeSimMain.snowflakesQueues.get(id).addAll(receivedSnowflakesQueues.get(id));
                }

            }
            int queueSize = 0;
            for(Queue q : SnowflakeSimMain.snowflakesQueues.values())
                queueSize += q.size();
            logger.log("Successfully handled message. Current queue size: " + queueSize);
        }
    }

    private static int countQueuesOversize(int size) {
        int count = 0;
        for (Integer key : SnowflakeSimMain.snowflakesQueues.keySet()) {
            if((!SnowflakeSimMain.snowflakesQueues.containsKey(key) || SnowflakeSimMain.snowflakesQueues.get(key) == null)
                    && SnowflakeSimMain.snowflakesQueues.get(key).size() > size) {
                count++;
            }
        }
        return count;
    }
}
