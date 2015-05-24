package snowflakes.cuda.kask.eti.pg.gda.pl.main;

import snowflakes.cuda.kask.eti.pg.gda.pl.snowflakes.Snowflake;

import java.util.Map;
import java.util.Queue;

/**
 * Created by Ariel on 2015-05-19.
 */
public class UpdateManager {

    public static void getPositionsFromQueue(Map<Integer, Queue<Float>> snowflakesQueues, Map<Integer, Snowflake> snowflakes) {
        for(int id : snowflakesQueues.keySet()){
            Queue<Float> queue = snowflakesQueues.get(id);
            if(queue != null && !queue.isEmpty()){
                if(!snowflakes.containsKey(id) || snowflakes.get(id) == null)
                    snowflakes.put(id, new Snowflake(id, queue.poll(), queue.poll(), 1.0f));
                else{
                    snowflakes.get(id).setPosX(queue.poll());
                    snowflakes.get(id).setPosY(queue.poll());
                }
            }
        }
    }
}
