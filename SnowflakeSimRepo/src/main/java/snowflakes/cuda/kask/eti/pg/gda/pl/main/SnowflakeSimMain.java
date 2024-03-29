package snowflakes.cuda.kask.eti.pg.gda.pl.main;

import org.java_websocket.WebSocket;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.newdawn.slick.*;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Created by Ariel on 2015-05-11.
 */
public class SnowflakeSimMain extends BasicGame {

    private final static TimeLogger logger = TimeLogger.getTimeLogger(SnowflakeSimMain.class.getSimpleName());

    private Image snowflakeImage;
    public static Map<Integer, Queue<Float>> snowflakesQueues = null;
    public static Map<Integer, Float> snowflakeSizes = null;
    public static Map<Integer, Snowflake> snowflakes = null;
    public static float windForce = 50.0f;
    public static float windAngle = 0.0f;
    private MasterEndpoint server = null;
    private boolean waitingForHosts;
    private boolean isKeyPressed;
    private int keyPressed = 0;

    public SnowflakeSimMain(String gamename)
    {
        super(gamename);
    }

    @Override
    public void init(GameContainer gc) throws SlickException {
        isKeyPressed = false;
        snowflakeImage = new Image("resources/snowflake.png");
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

    @Override
    public void update(GameContainer gc, int delta) throws SlickException {
        if(!waitingForHosts)
            UpdateManager.getPositionsFromQueue(snowflakesQueues, snowflakes);
        if(isKeyPressed){
            UpdateManager.windInteractions(keyPressed);
        }

    }

    @Override
    public void render(GameContainer gc, Graphics g) throws SlickException
    {
        if(waitingForHosts){
            RenderManager.renderHostInfo(g, server);
        } else {
            RenderManager.renderSnowflakes(snowflakes, snowflakeImage);
            RenderManager.renderWindInfo(g);
        }

    }

    @Override
    public void keyPressed(int key, char c) {
        super.keyPressed(key, c);
        if(waitingForHosts && key == Input.KEY_ENTER){
            waitingForHosts = false;
            sendInitMessage();
        }
        if(!waitingForHosts){
            isKeyPressed = true;
            keyPressed = key;
        }
    }

    private void sendInitMessage() {
        int i = 0;
        logger.log("Starting init messages...");
        Chamberlain.startTime();
        for (WebSocket ws : server.getConnectionPool()) {

//            JSONObject dto = new JSONObject();
//            dto.put(Commons.MESSAGE_ID, new Integer(i));
//            ws.send(dto.toJSONString());
//            System.out.println(JSONValue.toJSONString(dto));
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

    @Override
    public void keyReleased(int key, char c) {
        super.keyReleased(key, c);
        if(!waitingForHosts){
            isKeyPressed = false;
            keyPressed = 0;
        }
    }

    public static void main(String[] args)
    {
        try
        {
            Commons.SNOWFLAKES_NUMBER = Integer.parseInt(args[0]);
            AppGameContainer appgc;
            appgc = new AppGameContainer(new SnowflakeSimMain("Snowflake Simulator"));
            appgc.setDisplayMode(Commons.SCREEN_W, Commons.SCREEN_H, false);
            appgc.setTargetFrameRate(60);

            appgc.setAlwaysRender(true);
            appgc.start();
        }
        catch (SlickException ex)
        {
            Logger.getLogger(SnowflakeSimMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
