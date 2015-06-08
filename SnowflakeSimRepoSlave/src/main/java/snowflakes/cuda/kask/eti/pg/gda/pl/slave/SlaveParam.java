package snowflakes.cuda.kask.eti.pg.gda.pl.slave;

/**
 * Created by Kuba on 2015-05-27.
 */
public class SlaveParam {

    private float windForce, windAngle;
    private int snowflakesCount;

    public int getSnowflakesCount() {
        return snowflakesCount;
    }

    public void setSnowflakesCount(int snowflakesCount) {
        this.snowflakesCount = snowflakesCount;
    }

    public float getWindForce() {
        return windForce;
    }

    public void setWindForce(float windForce) {
        this.windForce = windForce;
    }

    public float getWindAngle() {
        return windAngle;
    }

    public void setWindAngle(float windAngle) {
        this.windAngle = windAngle;
    }
}
