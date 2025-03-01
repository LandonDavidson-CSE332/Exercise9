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
        POOL.invoke(new MatrixMultiplyAction(a, b, product, 0, a.length, 0, a[0].length)); // TODO: add parameters to match your constructor
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
        return POOL.invoke(new DotProductTask(0, ina.length, ina, inb));
    }

    private static class MatrixMultiplyAction extends RecursiveAction{
        int[][] a;
        int[][] b;
        int[][] output;
        int row_lo;
        int row_hi;
        int col_lo;
        int col_hi;

        public MatrixMultiplyAction(int[][] a, int[][] b, int[][] output, int row_lo, int row_hi, int col_lo, int col_hi){
            this.a = a;
            this.b = b;
            this.output = output;
            // Y axis bounds
            this.row_lo = row_lo;
            this.row_hi = row_hi;
            // X axis bounds
            this.col_lo = col_lo;
            this.col_hi = col_hi;
        }

        public void compute(){
            // If the bounded area is within the cutoff process sequentially and return
            if ((row_hi - row_lo) * (col_hi - col_lo) >= MatrixMultiply.CUTOFF) {
                // Loop over each entry in our sub array and 
                // compute the dot product with dotProduct
                for (int row = row_lo; row < row_hi; row++) {
                    for (int col = col_lo; col < col_hi; col++) {
                        output[row][col] = dotProduct(a, b, row, col, MatrixMultiply.CUTOFF);
                    }
                }
                // This subarray is done so return
                return;
            }
            // Otherwise create the four divided subtasks
            // Top left region, lo to hi / 2 for row and col
            MatrixMultiplyAction topleft = new MatrixMultiplyAction(a, b, output, row_lo, row_hi / 2, col_lo, col_hi / 2);
            // Top right, lo to hi / 2 for row and hi / 2 to hi for col
            MatrixMultiplyAction topright = new MatrixMultiplyAction(a, b, output, row_lo, row_hi / 2, col_hi / 2, col_hi);
            // Lower left, hi / 2 to hi for row and low to hi / 2 for col
            MatrixMultiplyAction lowleft = new MatrixMultiplyAction(a, b, output, row_hi / 2, row_hi, col_lo, col_hi / 2);
            // Lower right, hi / 2 to hi for row and col
            MatrixMultiplyAction lowright = new MatrixMultiplyAction(a, b, output, row_hi / 2, row_hi, col_hi / 2, col_hi);
            // Fork three of the actions, compute one after, then join back
            topright.fork();
            lowleft.fork();
            lowright.fork();
            topleft.compute();
            topright.join();
            lowleft.join();
            lowright.join();
            // All actions are completed so we can exit
        }

    }

    // Exact copy of DotProduct since dotProduct converts from 2d to 1d
    private static class DotProductTask extends RecursiveTask<Integer>{
        int[] a;
        int[] b;

        public DotProductTask(int lo, int hi, int[] a, int[] b){
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
            if (a.length <= MatrixMultiply.CUTOFF) {
                return Sequential.dotProduct(a, b);
            }
            // Calculate mid point and create left/right tasks around it
            int mid = a.length / 2;
            DotProductTask left = new DotProductTask(0, mid, a, b);
            DotProductTask right = new DotProductTask(mid, a.length, a, b);
            // Fork left and then compute right in this thread
            left.fork();
            int right_sum = right.compute();
            int left_sum = left.join();
            return right_sum + left_sum;
        }
    }
    
}
