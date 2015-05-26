package snowflakes.cuda.kask.eti.pg.gda.pl.main;

import org.newdawn.slick.Input;
import snowflakes.cuda.kask.eti.pg.gda.pl.commons.Commons;
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
                    snowflakes.put(id, new Snowflake(id, queue.poll(), queue.poll(), SnowflakeSimMain.snowflakeSizes.get(id)));
                else{
                    snowflakes.get(id).setPosX(queue.poll());
                    snowflakes.get(id).setPosY(queue.poll());
                    if(snowflakes.get(id).getPosY() > (float)(Commons.SCREEN_H))
                        snowflakes.get(id).setSizeModulator(SnowflakeSimMain.snowflakeSizes.get(id));
                }
            }
        }
    }
    public static void windInteractions(int keyPressed){
        switch (keyPressed) {
            case Input.KEY_UP:
                if(SnowflakeSimMain.windForce >= 100){
                    SnowflakeSimMain.windForce = 100.0f;
                } else
                    SnowflakeSimMain.windForce += 0.2f;
                break;
            case Input.KEY_DOWN:
                if(SnowflakeSimMain.windForce <= 0)
                    SnowflakeSimMain.windForce=0.0f;
                else
                    SnowflakeSimMain.windForce-=0.2f;
                break;
            case Input.KEY_LEFT:
                if(SnowflakeSimMain.windAngle <= -90) SnowflakeSimMain.windAngle = -90.0f;
                else SnowflakeSimMain.windAngle -= 0.2f;
                break;
            case Input.KEY_RIGHT:
                if(SnowflakeSimMain.windAngle >= 90) SnowflakeSimMain.windAngle = 90.0f;
                else SnowflakeSimMain.windAngle += 0.2f;
                break;
        }
    }
}
