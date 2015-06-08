package snowflakes.cuda.kask.eti.pg.gda.pl.message;

import org.java_websocket.WebSocket;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import snowflakes.cuda.kask.eti.pg.gda.pl.commons.Commons;
import snowflakes.cuda.kask.eti.pg.gda.pl.commons.TimeLogger;
import snowflakes.cuda.kask.eti.pg.gda.pl.communication.MasterEndpoint;
import snowflakes.cuda.kask.eti.pg.gda.pl.main.SnowflakeSimMain;
import snowflakes.cuda.kask.eti.pg.gda.pl.snowflakes.Snowflake;

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

    private static void initSnowFlakeCounters() {
        snowFlakesRemaining = Commons.SNOWFLAKES_NUMBER;
        snowFlakesDistributed = 0;
    }

    private static int calculatePartSize(int capacity) {
        return Math.min(snowFlakesRemaining - snowFlakesDistributed, capacity);
    }

    private static void sendPartToSlave(WebSocket socket) {
        // check remaining size and distribute
        int partSize = calculatePartSize(slaveCapacities.get(socket));
        // send, update counters
        JSONObject msg = new JSONObject();
        if (partSize != 0) {
            msg.put(Commons.MESSAGE_SNOWFLAKES_COUNT, new Integer(partSize));
            msg.put(Commons.MESSAGE_WIND_FORCE, new Float(SnowflakeSimMain.getTransferWindForce()));
            msg.put(Commons.MESSAGE_WIND_ANGLE, new Float(SnowflakeSimMain.windAngle));
            // LOG
            logger.log("Remaining is: " + snowFlakesRemaining + " distributed is: " + snowFlakesDistributed);
            socket.send(msg.toJSONString());
            logger.log("Sent PART: " + msg.toJSONString() + " to: " + socket.getRemoteSocketAddress());
            snowFlakesDistributed += partSize;
        }
        // send finish
        else {
            msg.put(Commons.NO_MORE_WORK, new Integer(0));
            logger.log("Remaining is: " + snowFlakesRemaining + " distributed is: " + snowFlakesDistributed);
            socket.send(msg.toJSONString());
            logger.log("Sent NO MORE WORK: " + msg.toJSONString() + " to: " + socket.getRemoteSocketAddress());
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
            handlePartsMessage(rawMessageParsed);
        } else if (dto.containsKey(Commons.PART_FINISH)) {
            // finished task
            Integer size = ((Long) dto.get(Commons.PART_FINISH)).intValue();
            logger.log("Received finish of parts with size: " + size);
            // reduce counters
            snowFlakesDistributed -= size;
            snowFlakesRemaining -= size;
            // send next part
            sendPartToSlave(ws);
        }
    }

    private static void handlePartsMessage(Map<String, JSONArray> rawMessageParsed) {
        if (countQueuesOversize(ACTIVE_SNOWFLAKE_QUEUE_SIZE) < DISPLAY_LIMIT_INITIAL) {
            Map<Integer, Queue<Float>> receivedSnowflakesQueues = new ConcurrentHashMap<Integer, Queue<Float>>();
            for(String sid : rawMessageParsed.keySet()){
                JSONArray jsonArray = rawMessageParsed.get(sid);
                List<Float> snowflakeOrderedPositions = new ArrayList<Float>(jsonArray.size());
                for(Object o : jsonArray){
                    snowflakeOrderedPositions.add(((Double)o).floatValue());
                }
                receivedSnowflakesQueues.put(Integer.parseInt(sid), new ConcurrentLinkedQueue<Float>(snowflakeOrderedPositions));
            }
            logger.log("Finished deserialization");

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
