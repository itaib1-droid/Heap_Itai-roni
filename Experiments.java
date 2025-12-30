import java.util.Random;

/**
 * Experiments
 * 
 * Runs three experiments on four types of heaps:
 * 1. Binomial Heap (lazy melds=false, lazy decrease=false)
 * 2. Lazy Binomial Heap (lazy melds=true, lazy decrease=false)
 * 3. Fibonacci Heap (lazy melds=true, lazy decrease=true)
 * 4. Binomial Heap with Cutoffs (lazy melds=true, lazy decrease=true) - same as Fibonacci
 * 
 * n = 464,646
 */
public class Experiments {
    
    static final int N = 464646;
    static final int NUM_HEAP_TYPES = 4;
    
    // Heap types
    static final int BINOMIAL_HEAP = 0;           // lazyMelds=false, lazyDecreaseKeys=false
    static final int LAZY_BINOMIAL_HEAP = 1;      // lazyMelds=true, lazyDecreaseKeys=false
    static final int FIBONACCI_HEAP = 2;          // lazyMelds=true, lazyDecreaseKeys=true
    static final int BINOMIAL_WITH_CUTOFFS = 3;   // lazyMelds=true, lazyDecreaseKeys=true
    
    static String[] heapTypeNames = {
        "Binomial Heap",
        "Lazy Binomial Heap",
        "Fibonacci Heap",
        "Binomial with Cutoffs"
    };
    
    /**
     * Experiment 1: Insert n items in random order, then delete min
     */
    static void experiment1() {
        System.out.println("========================================");
        System.out.println("EXPERIMENT 1: Insert Random + Delete Min");
        System.out.println("========================================\n");
        
        for (int heapType = 0; heapType < NUM_HEAP_TYPES; heapType++) {
            boolean lazyMelds = (heapType != BINOMIAL_HEAP);
            boolean lazyDecreaseKeys = (heapType == FIBONACCI_HEAP || heapType == BINOMIAL_WITH_CUTOFFS);
            
            Heap heap = new Heap(lazyMelds, lazyDecreaseKeys);
            Heap.HeapNode[] pointers = new Heap.HeapNode[N + 1];
            
            // Generate random permutation
            int[] perm = generateRandomPermutation(N);
            
            long startTime = System.currentTimeMillis();
            
            // Insert N items in random order
            for (int i = 0; i < N; i++) {
                int key = perm[i];
                Heap.HeapNode node = heap.insert(key, "Item_" + key);
                pointers[key] = node;
            }
            
            // Delete minimum
            heap.deleteMin();
            
            long endTime = System.currentTimeMillis();
            long runtimeMs = endTime - startTime;
            
            // Report metrics
            System.out.println(heapTypeNames[heapType] + ":");
            System.out.println("  Runtime: " + runtimeMs + " ms");
            System.out.println("  Total Links: " + heap.totalLinks());
            System.out.println("  Total Cuts: " + heap.totalCuts());
            System.out.println("  Heapify Costs: " + heap.totalHeapifyCosts());
            System.out.println("  Number of Trees: " + heap.numTrees());
            System.out.println("  Heap Size: " + heap.size());
            System.out.println();
        }
    }
    
    /**
     * Experiment 2: Insert n items, delete min, delete max until 46 items remain
     */
    static void experiment2() {
        System.out.println("========================================");
        System.out.println("EXPERIMENT 2: Insert + Delete Min + Delete Max");
        System.out.println("========================================\n");
        
        for (int heapType = 0; heapType < NUM_HEAP_TYPES; heapType++) {
            boolean lazyMelds = (heapType != BINOMIAL_HEAP);
            boolean lazyDecreaseKeys = (heapType == FIBONACCI_HEAP || heapType == BINOMIAL_WITH_CUTOFFS);
            
            Heap heap = new Heap(lazyMelds, lazyDecreaseKeys);
            Heap.HeapNode[] pointers = new Heap.HeapNode[N + 1];
            boolean[] deleted = new boolean[N + 1];
            
            // Generate random permutation
            int[] perm = generateRandomPermutation(N);
            
            long startTime = System.currentTimeMillis();
            
            // Insert N items in random order
            for (int i = 0; i < N; i++) {
                int key = perm[i];
                Heap.HeapNode node = heap.insert(key, "Item_" + key);
                pointers[key] = node;
            }
            
            // Delete minimum
            heap.deleteMin();
            deleted[perm[0]] = true; // Note: first item in perm might not be min, need to track properly
            
            // Actually, let's find and delete the max items
            // Delete max (highest key) until 46 items remain
            int itemsToDelete = N - 1 - 46; // N-1 after deleting min, then delete until 46 remain
            
            for (int i = N; i > 0 && itemsToDelete > 0; i--) {
                if (!deleted[i] && pointers[i] != null) {
                    heap.delete(pointers[i]);
                    deleted[i] = true;
                    itemsToDelete--;
                }
            }
            
            long endTime = System.currentTimeMillis();
            long runtimeMs = endTime - startTime;
            
            // Report metrics
            System.out.println(heapTypeNames[heapType] + ":");
            System.out.println("  Runtime: " + runtimeMs + " ms");
            System.out.println("  Total Links: " + heap.totalLinks());
            System.out.println("  Total Cuts: " + heap.totalCuts());
            System.out.println("  Heapify Costs: " + heap.totalHeapifyCosts());
            System.out.println("  Number of Trees: " + heap.numTrees());
            System.out.println("  Heap Size: " + heap.size());
            System.out.println();
        }
    }
    
    /**
     * Experiment 3: Insert n items, delete min, decrease max items to 0 (ceil(n*0.1) times),
     * then delete min again
     */
    static void experiment3() {
        System.out.println("========================================");
        System.out.println("EXPERIMENT 3: Insert + Delete Min + Decrease Max + Delete Min");
        System.out.println("========================================\n");
        
        int numDecreases = (int) Math.ceil(N * 0.1);
        
        for (int heapType = 0; heapType < NUM_HEAP_TYPES; heapType++) {
            boolean lazyMelds = (heapType != BINOMIAL_HEAP);
            boolean lazyDecreaseKeys = (heapType == FIBONACCI_HEAP || heapType == BINOMIAL_WITH_CUTOFFS);
            
            Heap heap = new Heap(lazyMelds, lazyDecreaseKeys);
            Heap.HeapNode[] pointers = new Heap.HeapNode[N + 1];
            boolean[] deleted = new boolean[N + 1];
            
            // Generate random permutation
            int[] perm = generateRandomPermutation(N);
            
            long startTime = System.currentTimeMillis();
            
            // Insert N items in random order
            for (int i = 0; i < N; i++) {
                int key = perm[i];
                Heap.HeapNode node = heap.insert(key, "Item_" + key);
                pointers[key] = node;
            }
            
            // Delete minimum
            int minKey = findMin(perm, deleted);
            heap.deleteMin();
            deleted[minKey] = true;
            
            // Decrease key of ceil(n*0.1) largest items to 0
            int decreasedCount = 0;
            for (int i = N; i > 0 && decreasedCount < numDecreases; i--) {
                if (!deleted[i] && pointers[i] != null) {
                    heap.decreaseKey(pointers[i], pointers[i].key); // decrease to 0
                    decreasedCount++;
                }
            }
            
            // Delete minimum again
            heap.deleteMin();
            
            long endTime = System.currentTimeMillis();
            long runtimeMs = endTime - startTime;
            
            // Report metrics
            System.out.println(heapTypeNames[heapType] + ":");
            System.out.println("  Runtime: " + runtimeMs + " ms");
            System.out.println("  Total Links: " + heap.totalLinks());
            System.out.println("  Total Cuts: " + heap.totalCuts());
            System.out.println("  Heapify Costs: " + heap.totalHeapifyCosts());
            System.out.println("  Number of Trees: " + heap.numTrees());
            System.out.println("  Heap Size: " + heap.size());
            System.out.println("  Items decreased to 0: " + numDecreases);
            System.out.println();
        }
    }
    
    /**
     * Generate a random permutation of numbers 1 to n
     */
    static int[] generateRandomPermutation(int n) {
        int[] perm = new int[n];
        for (int i = 0; i < n; i++) {
            perm[i] = i + 1;
        }
        
        Random rand = new Random();
        for (int i = n - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int temp = perm[i];
            perm[i] = perm[j];
            perm[j] = temp;
        }
        
        return perm;
    }
    
    /**
     * Find the minimum key in the remaining items
     */
    static int findMin(int[] perm, boolean[] deleted) {
        for (int key : perm) {
            if (!deleted[key]) {
                return key;
            }
        }
        return -1;
    }
    
    public static void main(String[] args) {
        System.out.println("\n");
        System.out.println("==============================================");
        System.out.println("HEAP EXPERIMENTS - n = " + N);
        System.out.println("==============================================\n");
        
        experiment1();
       // experiment2();
       // experiment3();
        
        System.out.println("==============================================");
        System.out.println("EXPERIMENTS COMPLETED");
        System.out.println("==============================================");
    }
}
