package snowflakes.cuda.kask.eti.pg.gda.pl.main;

import org.java_websocket.WebSocket;
import snowflakes.cuda.kask.eti.pg.gda.pl.commons.Commons;
import snowflakes.cuda.kask.eti.pg.gda.pl.commons.TimeLogger;
import snowflakes.cuda.kask.eti.pg.gda.pl.communication.MasterEndpoint;
import snowflakes.cuda.kask.eti.pg.gda.pl.message.Chamberlain;
import snowflakes.cuda.kask.eti.pg.gda.pl.snowflakes.Snowflake;
import static snowflakes.cuda.kask.eti.pg.gda.pl.commons.SnowflakeMessages.*;


import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


/**
 * Created by Ariel on 2015-05-11.
 */
public class SnowflakeSimMain {

    private final static TimeLogger logger = TimeLogger.getTimeLogger(SnowflakeSimMain.class.getSimpleName());

    public volatile static Map<Integer, Queue<Float>> snowflakesQueues = null;
    public volatile static Map<Integer, Float> snowflakeSizes = null;
    public volatile static Map<Integer, Snowflake> snowflakes = null;
    public static float windForce = 50.0f;
    public static float windAngle = 0.0f;
    private static MasterEndpoint server = null;
    public volatile static boolean waitingForHosts;



    public static void init(){
        snowflakeSizes = new HashMap<Integer, Float>(Commons.SNOWFLAKES_NUMBER);
        snowflakesQueues = new ConcurrentHashMap<Integer, Queue<Float>>(Commons.SNOWFLAKES_NUMBER);
        snowflakes = new ConcurrentHashMap<Integer, Snowflake>(Commons.SNOWFLAKES_NUMBER);
        try {
            server = MasterEndpoint.getInstance();
        } catch (UnknownHostException e) {
            System.out.println("Could not get instance of SERVER!");
            e.printStackTrace();
        }
        server.start();
        waitingForHosts = true;
    }

    private static void setSnowflakeUpdater(){
        Timer timer = new Timer();
        UpdaterTask updaterTask = new UpdaterTask();
        timer.schedule(updaterTask, 0, 50);

    }

    private static void sendInitMessage() {
        int i = 0;
        logger.log("Starting init messages...");
        Chamberlain.startTime();
        for (WebSocket ws : server.getConnectionPool()) {

            ws.send(SnowflakeData.newBuilder().setSlaveId(i).build().toByteArray());
            logger.log("Sent id: " + i + " to " + ws.getRemoteSocketAddress());
            i++;
        }
        Chamberlain.CONNECTIONS_NUMBER = i;
        // setup total snowflakes for Chamberlain

        logger.log("Finished sending init messages");
    }

    public static Float getTransferWindForce() {
        return windForce / 50.0f;
    }

    public static void main(String[] args) throws InterruptedException {
        Commons.SNOWFLAKES_NUMBER = Integer.parseInt(args[0]);
        final int requiredConnections = Integer.parseInt(args[1]);


        init();
        logger.log("Waiting for " + requiredConnections + " slaves...");
        while(server.getConnectionPool().size() < requiredConnections) {
            TimeUnit.SECONDS.sleep(1);
        }

        sendInitMessage();
        setSnowflakeUpdater();
        while(server.getConnectionPool().size() > 0){
            TimeUnit.SECONDS.sleep(5);
        }


    }
}
