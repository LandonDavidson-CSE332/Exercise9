import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class DotProduct {
    static ForkJoinPool POOL = new ForkJoinPool();
    static int CUTOFF;

    // Behavior should match Sequential.dotProduct
    // Your implementation must have linear work and log(n) span
    public static int dotProduct(int[] a, int[]b, int cutoff){
        DotProduct.CUTOFF = cutoff;
        return POOL.invoke(new DotProductTask(0, a.length, a, b, cutoff));
    }

    private static class DotProductTask extends RecursiveTask<Integer>{
        int[] a;
        int[] b;
        int CUTOFF;

        public DotProductTask(int lo, int hi, int[] a, int[] b, int cutoff){
            this.CUTOFF = cutoff;
            // Create a subarray of a and b from lo to hi
            this.a = new int[hi - lo];
            this.b = new int[hi - lo];
            for (int i = lo; i < hi; i++) {
                this.a[i - lo] = a[i];
                this.b[i - lo] = b[i];
            }
        }

        public Integer compute(){
            // If we are at the cutoff then sequentially process
            if (a.length <= CUTOFF) {
                return Sequential.dotProduct(a, b);
            }
            // Calculate mid point and create left/right tasks around it
            int mid = a.length / 2;
            DotProductTask left = new DotProductTask(0, mid, a, b, CUTOFF);
            DotProductTask right = new DotProductTask(mid, a.length, a, b, CUTOFF);
            // Fork left and then compute right in this thread
            left.fork();
            int right_sum = right.compute();
            int left_sum = left.join();
            return right_sum + left_sum;
        }
    }
    
}
