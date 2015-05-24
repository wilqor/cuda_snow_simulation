package snowflakes.cuda.kask.eti.pg.gda.pl.message;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import snowflakes.cuda.kask.eti.pg.gda.pl.main.SnowflakeSimMain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Ariel on 2015-05-18.
 */
public class Chamberlain {

    public static void handle(String message){

        Map<String, JSONArray> rawMessageParsed = (Map<String, JSONArray>) JSONValue.parse(message);

        Map<Integer, Queue<Float>> receivedSnowflakesQueues = new ConcurrentHashMap<Integer, Queue<Float>>();
        for(String sid : rawMessageParsed.keySet()){
            JSONArray jsonArray = rawMessageParsed.get(sid);
            List<Float> snowflakeOrderedPositions = new ArrayList<Float>(jsonArray.size());
            for(Object o : jsonArray){
                snowflakeOrderedPositions.add(((Double)o).floatValue());
            }
            receivedSnowflakesQueues.put(Integer.parseInt(sid), new ConcurrentLinkedQueue<Float>(snowflakeOrderedPositions));
        }

        for(int id : receivedSnowflakesQueues.keySet()){
            if(!SnowflakeSimMain.snowflakesQueues.containsKey(id) || SnowflakeSimMain.snowflakesQueues.get(id) == null)
                SnowflakeSimMain.snowflakesQueues.put(id, new ConcurrentLinkedQueue<Float>(receivedSnowflakesQueues.get(id)));
            else SnowflakeSimMain.snowflakesQueues.get(id).addAll(receivedSnowflakesQueues.get(id));

        }
        int queueSize = 0;
        for(Queue q : SnowflakeSimMain.snowflakesQueues.values())
            queueSize += q.size();
        System.out.println("Successfully handled message. New queue size: " + queueSize);
    }
}
