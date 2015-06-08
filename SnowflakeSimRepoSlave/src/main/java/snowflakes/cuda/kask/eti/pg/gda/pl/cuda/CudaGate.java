package snowflakes.cuda.kask.eti.pg.gda.pl.cuda;

import snowflakes.cuda.kask.eti.pg.gda.pl.commons.Commons;
import snowflakes.cuda.kask.eti.pg.gda.pl.commons.TimeLogger;
import snowflakes.cuda.kask.eti.pg.gda.pl.slave.SlaveEndpoint;
import snowflakes.cuda.kask.eti.pg.gda.pl.slave.SlaveParam;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Kuba on 2015-05-26.
 */
public class CudaGate {

    private final static TimeLogger logger = TimeLogger.getTimeLogger(CudaGate.class.getSimpleName());

    private final int slaveNumber;
    private final CudaComputation cudaComputation;
    private final SlaveParam param;

    public CudaGate(int slaveNumber, SlaveParam slaveParam) {
        this.slaveNumber = slaveNumber;
        this.param = slaveParam;

        cudaComputation = new CudaComputation(Commons.MAX_ITERATIONS, Commons.MIN_SCALE,
                Commons.MAX_SCALE, Commons.MIN_X, Commons.MAX_X,
                Commons.MIN_Y, Commons.MAX_Y, Commons.GRAVITY);

    }

    public boolean init() {
        return cudaComputation.init();
    }

    public int getDeviceSnowflakeCapacity() {
        return cudaComputation.getSnowflakeCapacity();
    }

    public Map<Integer, Queue<Float>> getNextIteration() {
        float angle = param.getWindAngle(), wind = param.getWindForce();
        int snowflakesCount = param.getSnowflakesCount();

        logger.log("The angle for calculation is " + (float) Math.toRadians(angle));
        ComputationResult result = cudaComputation.calculate(wind, (float) Math.toRadians(angle), snowflakesCount);
        logger.log("Before putting to queues...");
        float snowflakePositions[] = result.getHostSnowflakePositions();
        int usageIndexes[] = result.getHostUsageIndexes();
        int rowSize = Commons.MAX_ITERATIONS * 2 + 1;
        Map<Integer, Queue<Float>> snowflakesQueues = new LinkedHashMap<Integer, Queue<Float>>();
        for (int i=0; i < snowflakesCount; i++) {
            Queue<Float> tempQueue = new ConcurrentLinkedQueue<Float>();
            float[] positions;
            int usageIndex = usageIndexes[i], startIndex, endIndex;
            startIndex = i * rowSize;
            if (usageIndex == 0) {
                endIndex = startIndex + rowSize;
            } else {
                endIndex = startIndex + usageIndex;
            }
            positions = Arrays.copyOfRange(snowflakePositions, startIndex, endIndex);
            tempQueue.addAll(floatListFromArray(positions));
            // add with proper index
            snowflakesQueues.put(slaveNumber * snowflakesCount + i,  tempQueue);
        }
        logger.log("After putting to queues");
        return snowflakesQueues;
    }

    public void cleanup() {
        cudaComputation.cleanup();
    }

    private static List<Float> floatListFromArray(float[] array) {
        List<Float> output = new ArrayList<Float>();
        for (float f : array) {
            output.add(f);
        }
        return output;
    }

}
