package snowflakes.cuda.kask.eti.pg.gda.pl.cuda;

import snowflakes.cuda.kask.eti.pg.gda.pl.commons.Commons;
import snowflakes.cuda.kask.eti.pg.gda.pl.slave.SlaveEndpoint;
import snowflakes.cuda.kask.eti.pg.gda.pl.slave.SlaveParam;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Kuba on 2015-05-26.
 */
public class CudaGate {

    private final int snowflakesCount, slaveNumber;
    private final CudaComputation cudaComputation;
    private final SlaveParam param;

    public CudaGate(int snowflakesCount, int slaveNumber, SlaveParam slaveParam) {
        this.snowflakesCount = snowflakesCount;
        this.slaveNumber = slaveNumber;
        this.param = slaveParam;

        cudaComputation = new CudaComputation(Commons.MAX_ITERATIONS, snowflakesCount, Commons.MIN_SCALE,
                Commons.MAX_SCALE, Commons.MIN_X, Commons.MAX_X,
                Commons.MIN_Y, Commons.MAX_Y, Commons.GRAVITY);
        boolean initSuccessful = cudaComputation.init();
        if (!initSuccessful) {
            System.out.println("Could not init Cuda Computation unit");
        }
    }

    public Map<Integer, Queue<Float>> getNextIteration() {
        float angle = param.getWindAngle(), wind = param.getWindForce();

        ComputationResult result = cudaComputation.calculate(wind, (float) Math.toRadians(angle));
        System.out.println("The angle for calculation is " + (float) Math.toRadians(angle));
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
