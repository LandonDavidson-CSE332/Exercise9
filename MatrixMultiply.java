import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

public class MatrixMultiply {
    static ForkJoinPool POOL = new ForkJoinPool();
    static int CUTOFF;

    // Behavior should match Sequential.multiply.
    // Ignoring the initialization of arrays, your implementation should have n^3 work and log(n) span
    public static int[][] multiply(int[][] a, int[][] b, int cutoff){
        MatrixMultiply.CUTOFF = cutoff;
        int[][] product = new int[a.length][b[0].length];
        POOL.invoke(new MatrixMultiplyAction()); // TODO: add parameters to match your constructor
        return product;
    }

    // Behavior should match the 2d version of Sequential.dotProduct.
    // Your implementation must have linear work and log(n) span
    public static int dotProduct(int[][] a, int[][] b, int row, int col, int cutoff){
        MatrixMultiply.CUTOFF = cutoff;
        // Grab the one dimensional row from 2d a
        int[] ina = a[row];
        // Copy each column value from 2d b into 1d array
        int[] inb = new int[ina.length];
        for (int i = 0; i < inb.length; i++) {
            inb[i] = b[i][col];
        }
        return POOL.invoke(new DotProductTask(0, ina.length, ina, inb, cutoff));
    }

    private static class MatrixMultiplyAction extends RecursiveAction{
        // TODO: select fields

        public MatrixMultiplyAction(){
            // TODO: implement constructor
        }

        public void compute(){
            // TODO: implement
        }

    }

    // Exact copy of DotProduct since dotProduct converts from 2d to 1d
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
