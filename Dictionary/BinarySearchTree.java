/**
 * Custom binary search tree implementation for dictionary
 * applications.
 */
class BinarySearchTree{

    private Node root;
    private int height;
    private int size;
    private int leaves;
    /** The sum of all depths.*/
    private int sumOfDepths;
    /** 
     * Array holding the number of nodes pr. depth. Is initialized
     * by computeStats() and its values are set by nodes().
     */
    private int[] nodesPrDepth;

    /**
     * Inner class representing nodes in the tree.
     */
    private class Node{
	
	Node left;
	Node right;
	String word;
		
	Node(String v){
		
	    word = v;
	}
		
	/**
	 * Internal insertion method.
	 * @param t The word to insert.
	 */
	void insert(String t){
					
	    if(t.compareTo(word) < 0){
		if(left == null)
		    left = new Node(t);
		else
		    left.insert(t);
	    }else{
		if(right == null)
		    right = new Node(t);
		else
		    right.insert(t);
	    }
	}
		
	/**
	 * Internal removal method.
	 * @param t The String to be removed.
	 * @param n The node that roots the subtree.
	 * @return The new root of the subtree.
	 */
	Node remove(String t, Node n){
			
	    if(n == null)
		return n;
			
	    int compareResult = t.compareTo(n.word);
		
	    if(compareResult < 0)
		n.left = remove(t, n.left);
	    else if(compareResult > 0)
		n.right = remove(t, n.right);
	    else if(n.left != null && n.right != null){
		n.word = findMin().word;
		n.right = remove(t, n.right);
			
	    }else
		n = (n.left != null) ? n.left : n.right;
						
	    return n;
	}
	
	/**
	 * Pre-order traversal to find a given String.
	 * @param t the String for which to search.
	 * @return Reference to the node containing the String (null if not found).
	 */
	Node find(String t){
			
	    Node toFind = null;
			
	    if(t.equals(word))
		return this;
			
	    if(left != null && toFind == null)
		toFind = left.find(t);
	    if(right != null && toFind == null)
		toFind = right.find(t);
				
	    return toFind;
	}
		
	/**
	 * Sums up all nodes in the tree by pre-order traversal. Also counts leaves, nodes
	 * pr depth, and sums up the depths.
	 * @param sum The sum passed along the recursive calls. Must start at 1.
	 * @param currDepth The current depth. Starts at 0 and is incremented at
	 * each node.
	 * @return The updated sum.
	 */
	int sumNodes(int sum, int currDepth){
				
	    sum++;
	    currDepth++;
	    nodesPrDepth[currDepth]++;
		
	    if(left == null && right == null){
		leaves++;
		sumOfDepths += currDepth;
	    }
		
	    if(left != null)
		sum = left.sumNodes(sum, currDepth);
		
	    if(right != null)
		sum = right.sumNodes(sum, currDepth);
				
	    return sum;
	}
		
	Node findMin(){
	    if(left == null)
		return this;
				
	    return left.findMin();
	}
		
	Node findMax(){
	    if(right == null)
		return this;
				
	    return right.findMax();
	}
		
	void printTree(){
		
	    if(left != null)
		left.printTree();
	    System.out.println(word);
	    if(right != null)
		right.printTree();
	}
			
	/**
	 * Post-order traversal to compute the height of the entire tree.
	 * @param r Reference to the node that roots the subtree.
	 * @return -1 if the tree is empty.
	 * @return the highest String of the left and right subtrees of the root.
	 */
	int height(Node r){
	    if(r == null)
		return -1;
		
	    return 1 + Math.max(height(r.left), height(r.right));	
	}
		
	/**
	 * Finds all words of the given length or length +/- 1 in a 
	 * BinarySearchTree, and places them into a new tree. 
	 * Words are placed unordered into the new tree by pre-order 
	 * traversal of the original tree.
	 * @param length Length of words for which to search.
	 * @param b A new BinarySearchTree into which the found words will be put.
	 * @return Reference to the tree containing the found words.
	 */
	BinarySearchTree getMatches(int length, BinarySearchTree b){
		
	    if(word.length() == length-1 
	       || word.length() == length+1
	       || word.length() == length)
		b.insert(word);
		
	    if(left != null){
		left.getMatches(length, b);
	    }
			
	    if(right != null){
		right.getMatches(length, b);
	    }
							
	    return b;
	}
    } //end Node
	
    /*The following methods are wrapped calls/drivers for the 
      internal methods, or just simple utilities.*/
    public BinarySearchTree getMatches(int length){
	BinarySearchTree matches = new BinarySearchTree();
	if(root != null)
	    matches = root.getMatches(length, matches);
	return matches;		
    }
	
    public void insert(String t){
	if(root == null){	
	    root = new Node(t);
	    return;
	}
	root.insert(t);
    }
	
    public void remove(String t){
	if(root == null)
	    return;
	root.remove(t, root);
	return;
    }
	
    public boolean contains(String t){
	if(root == null)
	    return false;
	Node toFind = root.find(t);
	return toFind != null;
    }
	
    public boolean isEmpty(){
	return root == null;
    }
		
    public void printTree(){
	if(root != null){
	    root.printTree();
	}
    }
	
    public int getSize(){
	return size;
    }
	
    public int getLeaves(){
	return leaves;
    }
	
    public int getHeight(){
	return height;
    }
	
    public String printNPrDepth(){
	String output = "";
	for(int i = 0; i < nodesPrDepth.length; i++){
	    output += "Nodes in level " + i + ":\t" + nodesPrDepth[i] + "\n";
	}
	return output;
    }
		
    public String computeStats(){
	leaves = 0;
	sumOfDepths = 0;
	if(root != null){
	    height = root.height(root);
	    nodesPrDepth = new int[height+1];
	    size = root.sumNodes(1, -1);
	    return printNPrDepth() + "Average depth is " + (sumOfDepths/leaves)
		+ ". \nTree has " + leaves + " leaves, " 
		+ size + " nodes, and its height is " + height 
		+  ".\nFirst word:\t'" + root.findMin().word 
		+ "'.\nLast word:\t'" + root.findMax().word + "'.\n";
	}		
	return "Tree is empty";
    }
}
