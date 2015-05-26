package snowflakes.cuda.kask.eti.pg.gda.pl.commons;

/**
 * Created by Ariel on 2015-05-11.
 */
public class Commons {
    public static final int SNOWFLAKES_NUMBER = 1000;
    public static final float BASE_IMAGE_SCALING = 0.05f;
    public static final int SCREEN_W = 1000;
    public static final int SCREEN_H = 800;

    public static final int MAX_ITERATIONS = 14000;

    public static final float MIN_SCALE = 0.1f;
    public static final float MAX_SCALE = 5.0f;
    public static final float MIN_X = -200.0f;
    public static final float MAX_X = 1200.0f;
    public static final float MIN_Y = -100.0f;
    public static final float MAX_Y = 1100.0f;
    public static final float GRAVITY = 1.0f;
    public static final float X_MARGIN = 100.0f;

    public static final String MESSAGE_ID = "id";
    public static final String MESSAGE_SNOWFLAKES_COUNT = "snowFlakeNumber";
    public static final String MESSAGE_WIND_FORCE = "windForce";
    public static final String MESSAGE_WIND_ANGLE = "windAngle";

    public static double angleToRadian(float angle){
        return angle * Math.PI/180.0;
    }
}
