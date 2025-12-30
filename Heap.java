/**
 * Heap
 *
 * An implementation of Fibonacci heap over positive integers 
 * with the possibility of not performing lazy melds and 
 * the possibility of not performing lazy decrease keys.
 *
 */
public class Heap

{
    public final boolean lazyMelds;
    public final boolean lazyDecreaseKeys;
    private HeapNode min;
	private HeapNode first; // pointer to the first tree's root
	private int heapSize; // heap's size
	private int numTrees;
	private int linksCnt;
	private int cutCnt;
    private int totalHeapifyCosts;
    private int numMarkedNodes;

    /* ###############################################################################
    //                               HELP METHODS  
    ############################################################################### */
	
    /** 
	 * updates the min field out of all roots 
	 */
	public void updateMin() {
	    HeapNode currNode = first;
	    do {
	        updateMin(currNode);
	        currNode = currNode.next;
	    } while (currNode != first);
	}

    public void updateMin(HeapNode node)
	{
		if (node.key < this.min.key)
			min = node;
	}

    /**
	 * pre: 0<diff<x.key
	 * Decrease the key of x by diff and fix the heap without updating the min node 
	 */
	public void decreaseLazy(HeapNode x) 
	{    
		if (x.parent != null && x.key < x.parent.key) // the rule of heap is violated
			this.cascadingCut(x); // initiate the cascading cut process
	}

    /**
	 * pre: 0<diff<x.key
	 * Decrease the key of x by diff and heapfyUp node without updating the min node 
	 */
    public void decreaseNotLazy(HeapNode x) {
        if (x.parent != null && x.key >= x.parent.key) // the rule of heap is not violated
            return;
        else {
           this.HeapifyUp(x);
        }
    }

    /**
	 * initiate the cascading cut process from node upwards
	 */
	public void cascadingCut(HeapNode node)
	{
		HeapNode prnt = node.parent;
		// perform the cut of node from its parent
		this.cut(node);
		// if prnt is not a root
		if (prnt.parent != null) {
			if (!prnt.mark){ // if it's not marked - mark it
				prnt.mark = true;
                numMarkedNodes++;
            }
			else { // call cascadingCut recursively
				cascadingCut(prnt);
            }
		}
	}

    public void HeapifyUp(HeapNode node)
    {
        HeapNode prnt = node.parent;
        while (prnt != null && node.key < prnt.key) {
            int tmpKey = prnt.key;
            String tmpInfo = prnt.info;
            prnt.key = node.key;
            prnt.info = node.info;
            node.key = tmpKey;
            node.info = tmpInfo;
            node = prnt;
            prnt = node.parent;
            totalHeapifyCosts++;
        }
    }

    /**
	 * pre: node is in heap
	 * cut node from its parent, add it to the heap's roots
	 * and cascade cut to parent if needed 
	 */
	public void cut(HeapNode node)
	{
		HeapNode prnt = node.parent;
		// check if node is an only child
		if (node.next == node) 
			prnt.child = null;
		else {
            if (prnt.child == node){ // if node is the first child of prnt
				prnt.child = node.next;
            }
				node.prev.connectNext(node.next); // omit node from its original chain
			}		
		// insert node to the heap's roots 
		this.insertNodeToRoots(node);	
		// update parameters
		prnt.rank--;
		cutCnt++;
	}

    /**
	 * relocate the input node to the heap's roots
	 */
	public void insertNodeToRoots(HeapNode node){
		// insert node before first with the appropriate pointers attached
		node.insertBefore(first);
		node.parent = null;
		first = node;
		numTrees++; // update counter
		node.mark = false;	
	}

    public void lazyMeld(Heap heap2)
    {
        // the other heap is empty so no changes required
		if (heap2 == null || heap2.size() == 0)
			return;
		// this heap is empty
		if (this.size() == 0) {
			min = heap2.min;
            first = heap2.first;
            heapSize = heap2.heapSize;
            numTrees = heap2.numTrees;
            linksCnt = heap2.linksCnt;
            cutCnt = heap2.cutCnt;
            totalHeapifyCosts = heap2.totalHeapifyCosts;
			return;
		}

		//connecting 'edges'
		HeapNode lastNodeHeap2 = heap2.first.prev;
		this.first.prev.connectNext(heap2.first); 
		lastNodeHeap2.connectNext(this.first);	

        // update attributes 
		heapSize += heap2.heapSize;
		numTrees += heap2.numTrees;
		linksCnt += heap2.linksCnt;
		cutCnt += heap2.cutCnt;
        totalHeapifyCosts += heap2.totalHeapifyCosts;
		
        // update the min field if needed
		this.updateMin(); 
    }

    public void notLazyMeld(Heap heap2)
    {
        this.consolidate();
    }

    /**
	 * consolidate the heap's trees as a valid binomial heap
	 */
	public void consolidate()
	{
		// initial the "buckets" in which we keep the trees of same ranks
		int bound = 2*((int) Math.floor(Math.log(heapSize) / Math.log(2)) + 2);
		HeapNode[] buckets = new HeapNode[bound];
		
		// iterate through the heap
		HeapNode curr = first;
		for (int i = 0; i < numTrees; i++)
		{
			// separate curr from others, keep a pointer for the next tree
			HeapNode nxt = curr.next;
			curr.next = curr;
			curr.prev = curr;
			
			HeapNode subTree = buckets[curr.rank];
			while(subTree != null)
			{				
				buckets[curr.rank] = null; // remove the inside tree from its bucket
				curr = this.link(curr, subTree); // assign curr to be the root of the linked tree
				subTree = buckets[curr.rank]; // assign to the next bucket			
			}			
			buckets[curr.rank] = curr; // insert the new tree to the relevant bucket	
			curr = nxt;
		}
		this.fromBucketsToHeap(buckets); // update the heap to match the trees in the buckets
	}
	
	/**
	 * pre: x, y roots in the heap
	 * link the two nodes according to heaps' rule
	 * returns a pointer to the root of the linked tree
	 */
	public HeapNode link(HeapNode x,HeapNode y)
	{
		// make x node the smaller one
		if (y.key < x.key) {
			HeapNode temp = x;
			x = y;
			y = temp;
		}
		
		// consider whether x node has no children
		if (x.child == null)
			y.next = y;
		else {
			y.connectNext(x.child.next);
			x.child.connectNext(y);
		}
		// connect parent - child
		x.child = y;
		y.parent = x;
		// update rank	
		x.rank++ ;
		// increase the heap's links count by 1
		linksCnt++ ;
		return x;
	}
	
	/**
	 * creates a valid binomial heap out of buckets' nodes
	 */
	public void fromBucketsToHeap(HeapNode[] buckets)
	{
		min = null;
		numTrees = 0;
		// go over the nodes in buckets from the biggest to smallest 
		for (int i = buckets.length - 1; i >= 0; i--) {
			HeapNode node = buckets[i]; 
			if (node != null)
			{
				// checks if we didn't encounter real nodes yet
				if (min == null) {			
					first = node;
					min = node;
				}
				else {
					node.insertBefore(first);
					first = node;
					this.updateMin(node);
				}
				// increase the trees counter by one for each inserted tree
				numTrees += 1;				
			}			
		}
	}

    /**
	 * 
	 * For each node in the chain of the input node, remove its parent
	 *
	 */
	private void removeParents(HeapNode node){
		HeapNode curr = node;
		do {
			curr.parent = null;
			curr = curr.next;
			cutCnt ++;
		} while (curr != node);
	}

    /**
	 * make this heap a duplicate of "other" heap
	 */
	public void duplicateOf(Heap other)
	{
		min = other.min;
		first = other.first;
		heapSize = other.heapSize;
		numTrees = other.numTrees;
		linksCnt = other.linksCnt;
		cutCnt = other.cutCnt;
	}





    /* ###############################################################################
    //                               main methods
    ############################################################################### */
    /**
     *
     * Constructor to initialize an empty heap.
     *
     */
    public Heap(boolean lazyMelds, boolean lazyDecreaseKeys)
    {
        this.lazyMelds = lazyMelds;
        this.lazyDecreaseKeys = lazyDecreaseKeys;
        this.min = null;
        this.first = null;
        this.heapSize = 0;
        this.numTrees = 0;
        this.linksCnt = 0;
        this.cutCnt = 0;
    }

    /**
     * 
     * pre: key > 0
     *
     * Insert (key,info) into the heap and return the newly generated HeapNode.
     *
     */
    public HeapNode insert(int key, String info) 
    {    
        HeapNode newNode = new HeapNode(key, info);
        Heap newHeap = new Heap(this.lazyMelds, this.lazyDecreaseKeys);
        newHeap.first = newNode;
        newHeap.min = newNode;
        newHeap.heapSize = 1;
        newHeap.numTrees = 1;
        
		// if the heap was empty
		if (heapSize == 0) {
			this.meld(newHeap);
		}
		else {
            this.meld(newHeap);
			first = newNode;
			updateMin(); 	
           
		}
		// heapSize++;
		// numTrees++;
		return newNode; 
    }

    /**
	 * 
	 * Return the minimal HeapNode, null if empty.
	 *
	 */
	public HeapNode findMin(){
		return min;		
	}

    /**
     * 
     * Delete the minimal item.
     *
     */
    public void deleteMin()
    {
        HeapNode min_node = min;
        // edge case of a single-node-heap
		if (heapSize == 1)
		{
			// make this heap an empty new heap with the previous counters
			int prevLinks = linksCnt;
			int prevCuts = cutCnt;
			this.duplicateOf(new Heap(this.lazyMelds, this.lazyDecreaseKeys)); 
			linksCnt = prevLinks;
			cutCnt = prevCuts;
			return;
		}
		// checks if the heap includes a single tree
		if (numTrees == 1) {
			first = min_node.child;
			// for each child, remove the parent
			this.removeParents(first); 
		}
		else {
			
			// add the deleted node's children as trees to the heap
			if (min_node.child != null) {
				HeapNode second = first.next;
				HeapNode currChild = min_node.child;	
				HeapNode lastChild = currChild.prev;
				// for each child, remove the parent
				this.removeParents(currChild);
				// connect the children to the heap's roots						
				first.connectNext(currChild);
				lastChild.connectNext(second);		
			}
            else {
                // no children - nothing to add
                min_node.prev.connectNext(min_node.next);
            }
		}
		heapSize -= 1;
		numTrees += min_node.rank - 1;
		this.consolidate();	
        this.updateMin();
    }

    /**
     * 
     * pre: 0<=diff<=x.key
     * 
     * Decrease the key of x by diff and fix the heap.
     * 
     */
    public void decreaseKey(HeapNode x, int diff) 
    {    
        x.key -= diff;
        if (lazyDecreaseKeys) {
            decreaseLazy(x); 
        }
        else {
            decreaseNotLazy(x);
        }

        this.updateMin(x); // update the min node if necessary
    }

    /**
     * 
     * Delete the x from the heap.
     *
     */
    public void delete(HeapNode x) 
	{    		
		// if it's not the min, decrease its key to be the smallest without updating the min
		if (x != min) {
			int diff = x.key + 1;
            decreaseKey(x, diff);
		}
		// activate the deletion
		deleteMin();
	}


    /**
     * 
     * Meld the heap with heap2
     * pre: heap2.lazyMelds = this.lazyMelds AND heap2.lazyDecreaseKeys = this.lazyDecreaseKeys
     *
     */
    public void meld(Heap heap2)
	{
        this.lazyMeld(heap2);
        if (!this.lazyMelds) {
            this.notLazyMeld(heap2);
        }
        return;
	}
    
    
    /**
     * 
     * Return the number of elements in the heap
     *   
     */
    public int size()
    {
        return heapSize;
    }


    /**
     * 
     * Return the number of trees in the heap.
     * 
     */
    public int numTrees()
    {
        return numTrees;
    }
    
    
    /**
     * 
     * Return the number of marked nodes in the heap.
     * 
     */
    public int numMarkedNodes()
    {
        return numMarkedNodes;
    }
    
    
    /**
     * 
     * Return the total number of links.
     * 
     */
    public int totalLinks()
    {
        return linksCnt;
    }
    
    
    /**
     * 
     * Return the total number of cuts.
     * 
     */
    public int totalCuts()
    {
        return cutCnt;
    }
    

    /**
     * 
     * Return the total heapify costs.
     * 
     */
    public int totalHeapifyCosts()
    {
        return totalHeapifyCosts;
    }
    
    
    /**
     * Class implementing a node in a ExtendedFibonacci Heap.
     *  
     */
    public static class HeapNode{
        public int key;
        public String info;
        public HeapNode child;
        public HeapNode next;
        public HeapNode prev;
        public HeapNode parent;
        public int rank;
        public boolean mark;

	/**
		 * constructor with key and info
		 * assign prev and next to be this
		 */
		public HeapNode(int key, String info) 
		{
			this.key = key;
			this.info = info;
			this.child = null;
			this.next = this;
			this.prev = this;
			this.parent = null;
			this.rank = 0;
			this.mark = false;
		}


    /**
     * inserts self to be before node by adjusting
     * the pointers to be in the order of: prev, self, node
     */
    public void insertBefore(HeapNode node)
		{
			HeapNode preNode = node.prev;
			this.next = node;
			this.prev = preNode;
			node.prev = this;
			preNode.next = this;
		}		

    /**
     * connects next node to be the next of self
     */
    public void connectNext(HeapNode next)
    {
        this.next = next;
        next.prev = this;
    }

    }
}

