package snowflakes.cuda.kask.eti.pg.gda.pl.main;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import snowflakes.cuda.kask.eti.pg.gda.pl.commons.Commons;
import snowflakes.cuda.kask.eti.pg.gda.pl.communication.MasterEndpoint;
import snowflakes.cuda.kask.eti.pg.gda.pl.snowflakes.Snowflake;

import java.text.DecimalFormat;
import java.util.Map;


/**
 * Created by Ariel on 2015-05-19.
 */
public class RenderManager {
    private static DecimalFormat df = new DecimalFormat("0.00");
    public static void renderSnowflakes(Map<Integer, Snowflake> snowflakes, Image image){
        for(Snowflake snowflake : snowflakes.values()){
            image.draw(snowflake.getPosX(), snowflake.getPosY(), Commons.BASE_IMAGE_SCALING *snowflake.getSizeModulator());
        }
    }

    public static void renderWindInfo(Graphics g) {
        g.drawString("Wind force: " + df.format(SnowflakeSimMain.windForce), Commons.SCREEN_W-200, 20.0f);
        g.drawString("Wind angle: " + df.format(SnowflakeSimMain.windAngle), Commons.SCREEN_W-200, 40.0f);
    }

    public static void renderHostInfo(Graphics g, MasterEndpoint server) {
        g.drawString("Waiting for hosts... press ENTER to finish waiting and start simulation. Currently connected: "
                + server.getConnectionPool().size() + " slaves.", 5.0f, Commons.SCREEN_H/2.0f);
    }
}
