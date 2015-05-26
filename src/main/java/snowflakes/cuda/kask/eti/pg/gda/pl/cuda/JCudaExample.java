package snowflakes.cuda.kask.eti.pg.gda.pl.cuda;

import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.driver.*;
import jcuda.jcurand.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Queue;

import static jcuda.driver.JCudaDriver.*;
import static jcuda.driver.JCudaDriver.cuMemFree;
import static jcuda.jcurand.JCurand.curandCreateGenerator;
import static jcuda.jcurand.JCurand.curandGenerateUniform;
import static jcuda.jcurand.JCurand.curandSetPseudoRandomGeneratorSeed;

/**
 * Created by Kuba on 2015-05-24.
 */
public class JCudaExample {

    public static void test() {


        CudaGate cg = new CudaGate(10, 1);
        Map<Integer, Queue<Float>> result = cg.getNextIteration(2.0f, 50.0f);
        for (Map.Entry<Integer, Queue<Float>> entry : result.entrySet()) {
            System.out.println("Snowflake number " + entry.getKey());
            System.out.println(entry.getValue().toString());
        }
        cg.cleanup();
        /*
        CudaComputation cuda = new CudaComputation(10, 10, 0.1f, 5.0f, -200.0f, 1200.0f,
                                    -100.0f, 1100.0f, 1.0f, 100.0f);
        cuda.init();
        cuda.calculate(2.0f, 50.0f * (float) Math.PI / 180.0f);
        cuda.cleanup();
        */
    }

    public static void run() throws IOException {
        // Enable exceptions and omit all subsequent error checks
        JCudaDriver.setExceptionsEnabled(true);

        // Create the PTX file by calling the NVCC
        String ptxFileName = preparePtxFile("JCudaVectorAddKernel.cu");

        // Initialize the driver and create a context for the first device.
        cuInit(0);
        CUdevice device = new CUdevice();
        cuDeviceGet(device, 0);
        CUcontext context = new CUcontext();
        cuCtxCreate(context, 0, device);

        // Load the ptx file.
        CUmodule module = new CUmodule();
        cuModuleLoad(module, ptxFileName);

        // Obtain a function pointer to the "add" function.
        CUfunction function = new CUfunction();
        cuModuleGetFunction(function, module, "dalibomba");

        int numElements = 100000;

        // Allocate and fill the host input data
        float hostInputA[] = new float[numElements];
        float hostInputB[] = new float[numElements];
        for(int i = 0; i < numElements; i++)
        {
            hostInputA[i] = (float)i;
            hostInputB[i] = (float)i;
        }

        // Allocate the device input data, and copy the
        // host input data to the device
        CUdeviceptr deviceInputA = new CUdeviceptr();
        cuMemAlloc(deviceInputA, numElements * Sizeof.FLOAT);
        cuMemcpyHtoD(deviceInputA, Pointer.to(hostInputA),
                numElements * Sizeof.FLOAT);
        CUdeviceptr deviceInputB = new CUdeviceptr();
        cuMemAlloc(deviceInputB, numElements * Sizeof.FLOAT);
        cuMemcpyHtoD(deviceInputB, Pointer.to(hostInputB),
                numElements * Sizeof.FLOAT);

        // Allocate device output memory
        CUdeviceptr deviceOutput = new CUdeviceptr();
        cuMemAlloc(deviceOutput, numElements * Sizeof.FLOAT);

        // Set up the kernel parameters: A pointer to an array
        // of pointers which point to the actual values.
        Pointer kernelParameters = Pointer.to(
                Pointer.to(new int[]{numElements}),
                Pointer.to(deviceInputA),
                Pointer.to(deviceInputB),
                Pointer.to(deviceOutput)
        );

        // Call the kernel function.
        int blockSizeX = 256;
        int gridSizeX = (int)Math.ceil((double)numElements / blockSizeX);
        cuLaunchKernel(function,
                gridSizeX,  1, 1,      // Grid dimension
                blockSizeX, 1, 1,      // Block dimension
                0, null,               // Shared memory size and stream
                kernelParameters, null // Kernel- and extra parameters
        );
        cuCtxSynchronize();

        // Allocate host output memory and copy the device output
        // to the host.
        float hostOutput[] = new float[numElements];
        cuMemcpyDtoH(Pointer.to(hostOutput), deviceOutput,
                numElements * Sizeof.FLOAT);

        // Verify the result
        boolean passed = true;
        for(int i = 0; i < numElements; i++)
        {
            float expected = (float) Math.sin(2.0d * i);
            if (Math.abs(hostOutput[i] - expected) > 1e-5)
            {
                System.out.println(
                        "At index "+i+ " found "+hostOutput[i]+
                                " but expected "+expected);
                passed = false;
                break;
            }
        }
        System.out.println("Test "+(passed?"PASSED":"FAILED"));

        randomInvocation();

        // Clean up.
        cuMemFree(deviceInputA);
        cuMemFree(deviceInputB);
        cuMemFree(deviceOutput);
    }

    private static void randomInvocation() {
        // Allocate device memory
        CUdeviceptr deviceData = new CUdeviceptr();
        int n = 100;
        cuMemAlloc(deviceData, n * Sizeof.FLOAT);

        // Create and initialize a pseudo-random number generator
        curandGenerator generator = new curandGenerator();
        curandCreateGenerator(generator, curandRngType.CURAND_RNG_PSEUDO_DEFAULT);
        curandSetPseudoRandomGeneratorSeed(generator, 1234);

        // Generate random numbers
        curandGenerateUniform(generator, deviceData, n);

        // Copy the random numbers from the device to the host
        float hostData[] = new float[n];

        cuMemcpyDtoH(Pointer.to(hostData), deviceData,
                n * Sizeof.FLOAT);
        for (int i=0; i < n; i++) {
            System.out.println("The rand value is: " + hostData[i]);
        }
    }

    public static float[] generateRandomValues(int size) {
        float values[] = new float[size];
        // allocate dev memory
        CUdeviceptr deviceData = new CUdeviceptr();
        cuMemAlloc(deviceData, size * Sizeof.FLOAT);

        // prepare pseudo-random number generator
        curandGenerator generator = new curandGenerator();
        curandCreateGenerator(generator, curandRngType.CURAND_RNG_PSEUDO_DEFAULT);
        curandSetPseudoRandomGeneratorSeed(generator, System.currentTimeMillis());

        // the generation itself
        curandGenerateUniform(generator, deviceData, size * Sizeof.FLOAT);

        // return results to host
        cuMemcpyDtoH(Pointer.to(values), deviceData, size * Sizeof.FLOAT);
        return values;
    }

    /**
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
