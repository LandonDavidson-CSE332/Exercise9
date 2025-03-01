import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class FilterEmpty {
    static ForkJoinPool POOL = new ForkJoinPool();
    static int CUTOFF;

    // Behavior should match Sequential.filterEmpty
    // Ignoring the initialization of arrays, your implementation must have linear work and log(n) span
    public static String[] filterEmpty(String[] arr, int cutoff){
        FilterEmpty.CUTOFF = cutoff;
        // TODO: Implement to match the filter/pack procedure discussed in class.
        // Reminder: the main steps are:
        // 1) do a map on the arr of strings
        // 2) do prefix sum on the map result (implementation provided for you in ParallelPrefix.java)
        // 3) initialize and array whose length matches the last value in the prefix sum result
        // 4) do a map to populate that new array.

        // Invoke filter action to get our bit result array
        int[] result = new int[arr.length];
        POOL.invoke(new FilterEmptyAction(0, arr.length, result, arr));

        return null;
    }

    // Recursive action to map the strings
    public static class FilterEmptyAction extends RecursiveAction {
        int lo;
        int hi;
        int[] results;
        String[] input;

        public FilterEmptyAction(int lo, int hi, int[] results, String[] input) {
            this.lo = lo;
            this.hi = hi;
            this.results = results;
            this.input = input;
        }

        @Override
        protected void compute() {
            // If we are in the cutoff range process sequentially
            if (hi - lo <= FilterEmpty.CUTOFF) {
                for (int i = lo; i < hi; i++) {
                    if (!input[i].isEmpty()) {
                        results[i] = 1;
                    }
                }
                // We are done with this subarray so return
                return;
            }
            // Otherwise create left and right tasks and fork/compute
            int mid = lo + (hi - lo) / 2;
            FilterEmptyAction left = new FilterEmptyAction(lo, mid, results, input);
            FilterEmptyAction right = new FilterEmptyAction(mid, hi, results, input);
            right.fork();
            left.compute();
            right.join();
        }
    }
}
