 extern "C"
 __global__ void prepare(float *positions, int *usageIndexes, int count, int iterations,
                        float *random, float minScale, float maxScale,
                        float minX, float maxX, float minY)
{
    int id = blockIdx.x * blockDim.x + threadIdx.x, jump = gridDim.x * blockDim.x, i,
        row = iterations * 2 + 1;
    float x, y, scale;
    // for each snowflake
    for (i = id; i < count; i += jump)
    {
        // reset usageIndex
        usageIndexes[i] = 0;
        scale = random[i] * (maxScale - minScale) + minScale;
        // starting positions
        x = random[i + 1] * (maxX - minX) + minX;
        y = minY;
        // store in positions
        positions[i * row + 0] = scale;
        positions[i * row + 1] = x;
        positions[i * row + 2] = y;
    }
}

 extern "C"
 __global__ void calculate(float *positions, int *usageIndexes, int count, int iterations,
                            float wind, float angle, float gravity, float maxX, float minX, float maxY)
{
     int id = blockIdx.x * blockDim.x + threadIdx.x, jump = gridDim.x * blockDim.x, i, j, usageIndex,
            row = iterations * 2 + 1;
     float x, y, windX, windY, scale, sin, cos, prevX, prevY;
     // trigonometrics used for wind force
     sincosf(angle, &sin, &cos);
     windX = wind * sin;
     windY = wind * cos;
     // for each snowflake
     for (i = id; i < count; i += jump)
     {
        scale = positions[i * row + 0];
        usageIndex = usageIndexes[i];
        prevX = positions[i * row + 1];
        prevY = positions[i * row + 2];
        // starting from index 2, as <0, 2> is to be prepared earlier
        for (j = 3; j < iterations * 2 + 1; j += 2)
        {
            x = prevX + windX;
            if (x < minX)
            {
                x = maxX + x - minX;
            }
            else if (x > maxX)
            {
                x = minX + x - maxX;
            }
            y = prevY + gravity * scale + windY;
            if (y > maxY && usageIndex == 0)
            {
                usageIndex = j;
                usageIndexes[i] = usageIndex;
            }
            positions[i * row + j] = x;
            positions[i * row + j + 1] = y;
            prevX = x;
            prevY = y;
        }
     }
}
