package snowflakes.cuda.kask.eti.pg.gda.pl.main;

import org.newdawn.slick.Image;
import snowflakes.cuda.kask.eti.pg.gda.pl.commons.Commons;
import snowflakes.cuda.kask.eti.pg.gda.pl.snowflakes.Snowflake;

import java.util.Map;


/**
 * Created by Ariel on 2015-05-19.
 */
public class RenderManager {
    public static void renderSnowflakes(Map<Integer, Snowflake> snowflakes, Image image){
        for(Snowflake snowflake : snowflakes.values()){
            image.draw(snowflake.getPosX(), snowflake.getPosY(), Commons.BASE_IMAGE_SCALING *snowflake.getSizeModulator());
        }
    }
}
