package snowflakes.cuda.kask.eti.pg.gda.pl.cuda;

import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.driver.*;
import jcuda.jcurand.curandGenerator;
import jcuda.jcurand.curandRngType;
import jcuda.runtime.cudaDeviceProp;
import snowflakes.cuda.kask.eti.pg.gda.pl.commons.TimeLogger;
import java.io.*;

import static jcuda.driver.JCudaDriver.*;
import static jcuda.jcurand.JCurand.curandCreateGenerator;
import static jcuda.jcurand.JCurand.curandGenerateUniform;
import static jcuda.jcurand.JCurand.curandSetPseudoRandomGeneratorSeed;
import static jcuda.runtime.JCuda.cudaGetDeviceProperties;

/**
 * Created by Kuba on 2015-05-25.
 */
public class CudaComputation {

    private final static TimeLogger logger = TimeLogger.getTimeLogger(CudaComputation.class.getSimpleName());

    public static final String KERNEL_FILE_NAME = "SnowflakeSimulation.cu";
    public static final String KERNEL_PREPARATION_FUNCTION = "prepare";
    public static final String KERNEL_CALCULATION_FUNCTION = "calculate";
    private static final int RANDOM_VALUES_FOR_SNOWFLAKE = 2, COORDINATES_FOR_SNOWFLAKE = 2, DEVICE_NUMBER = 0;

    // rounded up (14000*8 + 4 + 8)
    private static final long SNOWFLAKE_MAX_REQUIREMENT = 200000;
    private int iterations;
    private final float minScale, maxScale, minX, maxX, minY, maxY, gravity;
    private CUfunction preparationFunction, calculationFunction;
    private CUdeviceptr snowflakesUsageIndexes, snowflakePositions, deviceRandomInit;
    private float hostSnowflakePositions[];
    private int hostUsageIndexes[];

    private CUdevice device;

    private int gridSizeX, blockSizeX, snowflakeCapacity;

    public CudaComputation(int iterations, float minScale, float maxScale,
                           float minX, float maxX, float minY, float maxY,
                           float gravity) {
        this.iterations = iterations;
        this.minScale = minScale;
        this.maxScale = maxScale;
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.gravity = gravity;
    }

    private void queryDeviceParameters()
    {
        int array[] = { 0 }, warpSize = 32, blocksPerSM, numberOfSM;
        // block size set to a minimum of 32 to trigger maximum number of concurrent blocks per SM
        cuOccupancyMaxActiveBlocksPerMultiprocessorWithFlags(array, calculationFunction, warpSize, 0, 0);
        blocksPerSM = array[0];

        cudaDeviceProp deviceProp = new cudaDeviceProp();
        cudaGetDeviceProperties(deviceProp, DEVICE_NUMBER);
        numberOfSM = deviceProp.multiProcessorCount;
        logger.log("Available SM: " + numberOfSM);

        gridSizeX = blocksPerSM * numberOfSM;
        logger.log("Total blocks used: " + gridSizeX);

        blockSizeX = deviceProp.maxThreadsPerBlock;
        logger.log("Max threads per block: " + blockSizeX);

        snowflakeCapacity = calculateSnowflakeCapacity(deviceProp.totalGlobalMem);
        logger.log("Global memory: " + deviceProp.totalGlobalMem);
        logger.log("Capacity: " + snowflakeCapacity);
    }

    private static int calculateSnowflakeCapacity(long memorySize) {
        // memory available / max memory needed for 1 snowflake
        return (int) (memorySize / SNOWFLAKE_MAX_REQUIREMENT);
    }

    public boolean init() {
        JCudaDriver.setExceptionsEnabled(true);
        String ptxFileName = "";
        try {
            ptxFileName = preparePtxFile(KERNEL_FILE_NAME);
        } catch (IOException e) {
            logger.log("Could not prepare kernel PTX file.");
            return false;
        }

        // initializing the 1st available device as default
        cuInit(0);
        device = new CUdevice();
        cuDeviceGet(device, DEVICE_NUMBER);
        CUcontext context = new CUcontext();
        cuCtxCreate(context, 0, device);

        // loading the module containing used functions
        CUmodule module = new CUmodule();
        cuModuleLoad(module, ptxFileName);

        // getting function pointers
        preparationFunction = new CUfunction();
        calculationFunction = new CUfunction();
        cuModuleGetFunction(preparationFunction, module, KERNEL_PREPARATION_FUNCTION);
        cuModuleGetFunction(calculationFunction, module, KERNEL_CALCULATION_FUNCTION);

        queryDeviceParameters();

        // + 1 for scale
        hostSnowflakePositions = new float[snowflakeCapacity * (iterations * COORDINATES_FOR_SNOWFLAKE + 1)];
        hostUsageIndexes = new int[snowflakeCapacity];

        // allocate device memory
        snowflakesUsageIndexes = new CUdeviceptr();
        cuMemAlloc(snowflakesUsageIndexes, snowflakeCapacity * Sizeof.INT);

        deviceRandomInit = new CUdeviceptr();
        cuMemAlloc(deviceRandomInit, snowflakeCapacity * RANDOM_VALUES_FOR_SNOWFLAKE * Sizeof.FLOAT);

        snowflakePositions = new CUdeviceptr();
        cuMemAlloc(snowflakePositions, snowflakeCapacity * (iterations * COORDINATES_FOR_SNOWFLAKE + 1) * Sizeof.FLOAT);
        return true;
    }

    public ComputationResult calculate(float wind, float angle, int snowflakesCount) {
        logger.log("Starting calculations...");
        fillWithRandomValues(deviceRandomInit, snowflakesCount * RANDOM_VALUES_FOR_SNOWFLAKE);

        // prepare positions for each snowflake
        Pointer preparationParameters = Pointer.to(
                Pointer.to(snowflakePositions),
                Pointer.to(snowflakesUsageIndexes),
                Pointer.to(new int[]{snowflakesCount}),
                Pointer.to(new int[]{iterations}),
                Pointer.to(deviceRandomInit),
                Pointer.to(new float[]{minScale}),
                Pointer.to(new float[]{maxScale}),
                Pointer.to(new float[]{minX}),
                Pointer.to(new float[]{maxX}),
                Pointer.to(new float[]{minY})
        );
        cuLaunchKernel(preparationFunction,
                gridSizeX, 1, 1,            // grid dimensions
                blockSizeX, 1, 1,           // block dimensions
                0, null,                    // shared memory size and stream
                preparationParameters, null // kernel- and extra parameters
        );
        cuCtxSynchronize();
        // calculate all the iterations
        Pointer calculationParameters = Pointer.to(
                Pointer.to(snowflakePositions),
                Pointer.to(snowflakesUsageIndexes),
                Pointer.to(new int[]{snowflakesCount}),
                Pointer.to(new int[]{iterations}),
                Pointer.to(new float[]{wind}),
                Pointer.to(new float[]{angle}),
                Pointer.to(new float[]{gravity}),
                Pointer.to(new float[]{maxX}),
                Pointer.to(new float[]{minX}),
                Pointer.to(new float[]{maxY})
        );
        cuLaunchKernel(calculationFunction,
                gridSizeX, 1, 1,
                blockSizeX, 1, 1,
                0, null,                    // shared memory size and stream
                calculationParameters, null // kernel- and extra parameters
        );
        cuCtxSynchronize();
        logger.log("Finished calculations");
        logger.log("Starting copying from device...");
        // copy from device to host
        cuMemcpyDtoH(Pointer.to(hostSnowflakePositions), snowflakePositions,
                snowflakesCount * (iterations * COORDINATES_FOR_SNOWFLAKE + 1) * Sizeof.FLOAT);
        cuMemcpyDtoH(Pointer.to(hostUsageIndexes), snowflakesUsageIndexes,
                snowflakesCount * Sizeof.INT);
        logger.log("Finished copying from device");
        // return results from host array
        return new ComputationResult(hostSnowflakePositions, hostUsageIndexes);
    }

    private static void fillWithRandomValues(CUdeviceptr destination, int size) {
        // prepare pseudo-random number generator
        curandGenerator generator = new curandGenerator();
        curandCreateGenerator(generator, curandRngType.CURAND_RNG_PSEUDO_DEFAULT);
        curandSetPseudoRandomGeneratorSeed(generator, System.currentTimeMillis());

        // the generation itself
        curandGenerateUniform(generator, destination, size * Sizeof.FLOAT);
    }

    public void cleanup() {
        cuMemFree(snowflakesUsageIndexes);
        cuMemFree(deviceRandomInit);
        cuMemFree(snowflakePositions);
    }

    public int getSnowflakeCapacity() {
        return snowflakeCapacity;
    }

    /**
     * From JCuda Tutorial
     *
     * The extension of the given file name is replaced with "ptx".
     * If the file with the resulting name does not exist, it is
     * compiled from the given file using NVCC. The name of the
     * PTX file is returned.
     *
     * @param cuFileName The name of the .CU file
     * @return The name of the PTX file
     * @throws java.io.IOException If an I/O error occurs
     */
    private static String preparePtxFile(String cuFileName) throws IOException
    {
        int endIndex = cuFileName.lastIndexOf('.');
        if (endIndex == -1)
        {
            endIndex = cuFileName.length()-1;
        }
        String ptxFileName = cuFileName.substring(0, endIndex+1)+"ptx";
        File ptxFile = new File(ptxFileName);
        if (ptxFile.exists())
        {
            return ptxFileName;
        }

        File cuFile = new File(cuFileName);
        if (!cuFile.exists())
        {
            throw new IOException("Input file not found: "+cuFileName);
        }
        String modelString = "-m"+System.getProperty("sun.arch.data.model");
        String command =
                "nvcc " + modelString + " -ptx "+
                        cuFile.getPath()+" -o "+ptxFileName;

        System.out.println("Executing\n"+command);
        Process process = Runtime.getRuntime().exec(command);

        String errorMessage =
                new String(toByteArray(process.getErrorStream()));
        String outputMessage =
                new String(toByteArray(process.getInputStream()));
        int exitValue = 0;
        try
        {
            exitValue = process.waitFor();
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new IOException(
                    "Interrupted while waiting for nvcc output", e);
        }

        if (exitValue != 0)
        {
            System.out.println("nvcc process exitValue "+exitValue);
            System.out.println("errorMessage:\n"+errorMessage);
            System.out.println("outputMessage:\n"+outputMessage);
            throw new IOException(
                    "Could not create .ptx file: "+errorMessage);
        }

        System.out.println("Finished creating PTX file");
        return ptxFileName;
    }

    /**
     * From JCuda Tutorial
     *
     * Fully reads the given InputStream and returns it as a byte array
     *
     * @param inputStream The input stream to read
     * @return The byte array containing the data from the input stream
     * @throws java.io.IOException If an I/O error occurs
     */
    private static byte[] toByteArray(InputStream inputStream)
            throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte buffer[] = new byte[8192];
        while (true)
        {
            int read = inputStream.read(buffer);
            if (read == -1)
            {
                break;
            }
            baos.write(buffer, 0, read);
        }
        return baos.toByteArray();
    }

}
