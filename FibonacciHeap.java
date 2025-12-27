/**
 * FibonacciHeap
 *
 * An implementation of Fibonacci heap over positive integers.
 *
 */
public class FibonacciHeap
{
	private HeapNode min;
	private HeapNode first; // pointer to the first tree's root
	private int heapSize; // heap's size
	private int numTrees;
	private int linksCnt;
	private int cutCnt;

	/**
	 * empty constructor
	 */
	public FibonacciHeap()
	{
		min = null;
		first = null;
		heapSize = 0;
		numTrees = 0;
		linksCnt = 0;
		cutCnt = 0;		
	}
	
	/**
	 * pre: key > 0
	 *
	 * Insert (key,info) into the heap and return the newly generated HeapNode.
	 *
	 */
	public HeapNode insert(int key, String info) 
	{    
		HeapNode newNode = new HeapNode(key, info);
		// if the heap was empty
		if (heapSize == 0) {
			first = newNode;
			min = newNode;
		}
		else {
			newNode.insertBefore(first);
			first = newNode;
			updateMin(newNode); // assign the min field to the newNode if necessary			
		}
		heapSize++;
		numTrees++;
		return newNode; 
	}
	/**
	 * relocate the input node to the heap's roots
	 */
	public void insertNodeToRoots(HeapNode node)
	{
		// insert node before first with the appropriate pointers attached
		node.insertBefore(first);
		node.parent = null;
		first = node;
		numTrees++; // update counter
		node.mark = false;	
	}

	/**
	 * 
	 * Return the minimal HeapNode, null if empty.
	 *
	 */
	public HeapNode findMin()
	{
		return min;		
	}
	
	/**
	 * 
	 * For each node in the chain of the input node, remove its parent
	 *
	 */
	private void removeParents(HeapNode node)
	{
		HeapNode curr = node;
		do {
			curr.parent = null;
			curr = curr.next;
			cutCnt ++;
		} while (curr != node);
	}
	
	/**
	 * 
	 * Delete the minimal item
	 *
	 */
	public void deleteMin()
	{
		this.genericDelete(min, true);
	}
	
	/**
	 * Deletes the input node considering the need for consolidating according to isMin parameter
	 */
	private void genericDelete(HeapNode x, boolean isMin) 
	{    
		// edge case of a single-node-heap
		if (heapSize == 1)
		{
			// make this heap an empty new heap with the previous counters
			int prevLinks = linksCnt;
			int prevCuts = cutCnt;
			this.duplicateOf(new FibonacciHeap()); 
			linksCnt = prevLinks;
			cutCnt = prevCuts;
			return;
		}
		// checks if the heap includes a single tree
		if (numTrees == 1) {
			first = x.child;
			// for each child, remove the parent
			this.removeParents(first); 
		}
		else {
			if (first == x)
				first = x.next;
			// remove node x
			x.prev.connectNext(x.next);
			
			// add the deleted node's children as trees to the heap
			if (x.child != null) {
				HeapNode second = first.next;
				HeapNode currChild = x.child;	
				HeapNode lastChild = currChild.prev;
				// for each child, remove the parent
				this.removeParents(currChild);
				// connect the children to the heap's roots						
				first.connectNext(currChild);
				lastChild.connectNext(second);		
			}
		}
		heapSize -= 1;
		numTrees += x.rank - 1;
		
		// if it's DeleteMin() - consolidate, link and fix the heap to be a valid binomial heap
		if (isMin)
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
			
			HeapNode insideTree = buckets[curr.rank];
			while(insideTree != null)
			{				
				buckets[curr.rank] = null; // remove the inside tree from its bucket
				curr = this.link(curr, insideTree); // assign curr to be the root of the linked tree
				insideTree = buckets[curr.rank]; // assign to the next bucket			
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
	 * pre: 0<diff<x.key
	 * 
	 * Decrease the key of x by diff and fix the heap. 
	 * 
	 */
	public void decreaseKey(HeapNode x, int diff) 
	{    
		this.decreaseKeyWithoutMinUpdate(x,  diff); // decrease x's key and initiate cuts accordingly
		this.updateMin(x); // update the min node
	}

	/**
	 * pre: 0<diff<x.key
	 * Decrease the key of x by diff and fix the heap without updating the min node 
	 */
	public void decreaseKeyWithoutMinUpdate(HeapNode x, int diff) 
	{    
		x.key -= diff;
		if (x.parent != null && x.key < x.parent.key) // the rule of heap is violated
			this.cascadingCut(x); // initiate the cascading cut process
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
				prnt.child = node.next;
				node.prev.connectNext(node.next); // omit node from its original chain
			}		
		// insert node to the heap's roots 
		this.insertNodeToRoots(node);	
		// update parameters
		prnt.rank--;
		cutCnt++;
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
			if (!prnt.mark) // if it's not marked - mark it
				prnt.mark = true;
			else // call cascadingCut recursively
				cascadingCut(prnt);
		}
	}

	/**
	 * 
	 * Delete x from the heap
	 *
	 */
	public void delete(HeapNode x) 
	{    		
		// if it's not the min, decrease its key to be the smallest without updating the min
		if (x != min) {
			int diff = (x.key - min.key) + 1;
			this.decreaseKeyWithoutMinUpdate(x, diff);
		}
		// activate the deletion
		this.genericDelete(x, x == min);
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
	 * Meld the heap with heap2
	 *
	 */
	public void meld(FibonacciHeap heap2)
	{
		// the other heap is empty so no changes required
		if (heap2 == null || heap2.size() == 0)
			return;
		// this heap is empty
		if (this.size() == 0) {
			this.duplicateOf(heap2);
			return;
		}
		
		// update the min field if needed
		this.updateMin(heap2.min); 

		// update attributes 
		heapSize += heap2.heapSize;
		numTrees += heap2.numTrees;
		linksCnt += heap2.linksCnt;
		cutCnt += heap2.cutCnt;
		
		//connecting 'edges'
		HeapNode lastNodeHeap2 = heap2.first.prev;
		this.first.prev.connectNext(heap2.first); 
		lastNodeHeap2.connectNext(this.first);	
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
	 * updates the min field of self to point to  the smaller node 
	 */
	public void updateMin(HeapNode node)
	{
		if (node.key < this.min.key)
			min = node;
	}
	
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
	 * make this heap a duplicate of "other" heap
	 */
	public void duplicateOf(FibonacciHeap other)
	{
		min = other.min;
		first = other.first;
		heapSize = other.heapSize;
		numTrees = other.numTrees;
		linksCnt = other.linksCnt;
		cutCnt = other.cutCnt;
	}

	/**
	 * Class implementing a node in a Fibonacci Heap.
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
		 * connects next node to be the next of self
		 */
		public void connectNext(HeapNode next)
		{
			this.next = next;
			next.prev = this;
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
	}
}
