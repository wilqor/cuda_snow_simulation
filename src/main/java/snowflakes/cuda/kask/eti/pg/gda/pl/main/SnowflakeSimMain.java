package snowflakes.cuda.kask.eti.pg.gda.pl.main;

import org.newdawn.slick.*;
import snowflakes.cuda.kask.eti.pg.gda.pl.commons.Commons;
import snowflakes.cuda.kask.eti.pg.gda.pl.communication.MasterEndpoint;
import snowflakes.cuda.kask.eti.pg.gda.pl.snowflakes.Snowflake;


import java.net.UnknownHostException;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Created by Ariel on 2015-05-11.
 */
public class SnowflakeSimMain extends BasicGame {
    private Image snowflakeImage;
    public static Map<Integer, Queue<Float>> snowflakesQueues = null;
    public static Map<Integer, Snowflake> snowflakes = null;
    private MasterEndpoint server = null;
    private boolean waitingForHosts;

    public SnowflakeSimMain(String gamename)
    {
        super(gamename);
    }

    @Override
    public void init(GameContainer gc) throws SlickException {
        snowflakeImage = new Image("resources/snowflake.png");
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
    }

    @Override
    public void render(GameContainer gc, Graphics g) throws SlickException
    {
        if(waitingForHosts){
            g.drawString("Waiting for hosts... press ENTER to finish waiting and start simulation. Currently connected: "
                    + server.getConnectionPool().size() + " slaves.", 5.0f, Commons.SCREEN_H/2.0f);
        } else {
            RenderManager.renderSnowflakes(snowflakes, snowflakeImage);
        }

    }

    @Override
    public void keyPressed(int key, char c) {
        super.keyPressed(key, c);
        if(waitingForHosts && key == Input.KEY_ENTER){
            waitingForHosts = false;
        }
    }

    public static void main(String[] args)
    {
        try
        {
            AppGameContainer appgc;
            appgc = new AppGameContainer(new SnowflakeSimMain("Snowflake Simulator"));
            appgc.setDisplayMode(Commons.SCREEN_W, Commons.SCREEN_H, false);
            appgc.setTargetFrameRate(35);

            appgc.setAlwaysRender(true);
            appgc.start();
        }
        catch (SlickException ex)
        {
            Logger.getLogger(SnowflakeSimMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
