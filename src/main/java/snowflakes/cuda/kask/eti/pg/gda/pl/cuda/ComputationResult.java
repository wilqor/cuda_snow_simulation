package snowflakes.cuda.kask.eti.pg.gda.pl.cuda;

/**
 * Created by Kuba on 2015-05-26.
 */
public class ComputationResult {
    private final float hostSnowflakePositions[];
    private final int hostUsageIndexes[];

    public ComputationResult(float[] hostSnowflakePositions, int[] hostUsageIndexes) {
        this.hostSnowflakePositions = hostSnowflakePositions;
        this.hostUsageIndexes = hostUsageIndexes;
    }

    public float[] getHostSnowflakePositions() {
        return hostSnowflakePositions;
    }

    public int[] getHostUsageIndexes() {
        return hostUsageIndexes;
    }
}
