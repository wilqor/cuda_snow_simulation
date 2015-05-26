package pl.edu.pg;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Ariel on 2015-05-25.
 */
public class DataGenerator {

    public static final float MIN_X = -200.0f;
    public static final float MAX_X = 1200.0f;
    public static final float START_Y = -100.0f;
    public static final float END_Y = 1100.0f;

    public static Random random = new Random();

    public static float GRAVITY_FORCE = 1.0f;
    public static float WIND_FORCE = 100.0f/50.0f; //wind /50
    public static float angle = 50.0f;






    public static Map<Integer, Queue<Float>> generateSnowflakes() {
        Map<Integer, Queue<Float>> snowflakesQueues = new LinkedHashMap<Integer, Queue<Float>>();

        final int NUMBER_OF_SNOWFLAKES = random.nextInt(50) + 1;

        for(int id = 0; id < NUMBER_OF_SNOWFLAKES; id++){
            float size = random.nextFloat() * 5;
            Queue<Float> tempQueue = new ConcurrentLinkedQueue<Float>();

            Snowflake snowflake = new Snowflake(random.nextInt(100000)+id, random.nextFloat() * (MAX_X - MIN_X) + MIN_X, START_Y, size);
            tempQueue.add(snowflake.getSizeModulator());
            tempQueue.add(snowflake.getPosX());
            tempQueue.add(snowflake.getPosY());

            while(snowflake.getPosY() < END_Y){
                float newX = snowflake.getPosX()+WIND_FORCE*(float)Math.sin(angleToRadian(angle));
                if(newX < MIN_X){
                    newX = MAX_X-100 + newX;
                } else if(newX > MAX_X){
                    newX = MIN_X+100 + (newX - MAX_X);
                }
                snowflake.setPosX(newX);
                snowflake.setPosY(snowflake.getPosY() + GRAVITY_FORCE*snowflake.getSizeModulator() + WIND_FORCE*(float)Math.cos(angleToRadian(angle)));

                tempQueue.add(snowflake.getPosX());
                tempQueue.add(snowflake.getPosY());
            }
            snowflakesQueues.put(id, tempQueue);
        }




        return snowflakesQueues;
    }

    public static double angleToRadian(float angle){
        return angle * Math.PI/180.0;
    }
}
