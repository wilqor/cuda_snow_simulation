 extern "C"
 __global__ void add(int n, float *a, float *b, float *sum)
{
    int i = blockIdx.x * blockDim.x + threadIdx.x;
    if (i<n)
    {
        // sum[i] = a[i] + b[i];
        sum[i] = sinf(a[i] + b[i]);
    }
}

 extern "C"
 __global__ void dalibomba(int n, float *a, float *b, float *sum)
{
    int i = blockIdx.x * blockDim.x + threadIdx.x;
    if (i<n)
    {
        // sum[i] = a[i] + b[i];
        sum[i] = sinf(a[i] + b[i]);
    }
}
