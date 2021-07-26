package cmsc420_f20;

import java.util.ArrayList;
import java.util.List;

/** PBJTree (skeletal) implementation.
*
* MODIFY THE FOLLOWING CLASS.
*
* You are free to make whatever changes you like or to create additional
* classes and files, but avoid reusing/modifying the other files given in this
* folder.
*/
public class PBJTree<Key extends Comparable<Key>, Value> {

	// Parameters used to determine when to rebalance (see Lecture 13 slides)

	private final float ALPHA = 0.66667f; // maximum allowed balance ratio
	private final float BETA = 0.25f; // max ratio filter for exemption
	
	// -------------------------------------------------
	// PARENT NODE CLASS
	// -------------------------------------------------
	
	private abstract class Node { // generic node type
		float weight; // The weight associated with this node
		float maxWt; // the max weight in this subtree
		
		abstract Value find(Key x, int t); // no function body for these! 
		abstract Node insert(Key x, Value v, float w, int t) throws Exception;
		abstract ArrayList<String> getPreorderList(int t);
		abstract ArrayList<String> getFullPreorderList();
		abstract Value getMin(int t);
		abstract Value getMax(int t);
		abstract Value findUp(Key x, int t);
		abstract Value findDown(Key x, int t);
		
		// Additional functions 
		abstract Node rebalance(Key x, int t);
		// abstract Node buildTree(List<Node> list);
		abstract ArrayList<ExternalNode> getExternalNodes(ArrayList<ExternalNode> extList);
	}
	
	// -------------------------------------------------
	// TEMPORAL NODE CLASS
	// -------------------------------------------------
	
	private class TemporalNode extends Node { // temporal node
		int time; // timestamp
		Node pre; // subtree prior to timestamp
		Node post; // subtree on or after timestamp
		
		TemporalNode(int time, Node pre, Node post) {
			this.time = time;
			this.pre = pre;
			this.post = post;
			this.weight = this.post.weight;
			this.maxWt = this.post.maxWt;
		}
		
		Value find(Key x, int t) { 
			if (t < time) {
				return this.pre.find(x, t);
			} else { // t >= time
				return this.post.find(x, t);
			}
		}	
		
		Node insert(Key x, Value v, float w, int t) throws Exception {
			this.post = this.post.insert(x,  v,  w,  t);
			this.weight = this.post.weight;
			this.maxWt = this.post.maxWt;
			
			return this;
		}
		
		Node rebalance(Key x, int t) { 
			this.post = this.post.rebalance(x, t);
			
			if (TemporalNode.class.isInstance(this.post)) {
				// LEFT ROTATION IS NEEDED
				TemporalNode rotation = (TemporalNode) this.post;
				
				this.post = rotation.pre;
				
				rotation.pre = this;
				
				rotation.weight = this.post.weight;
				rotation.maxWt = this.post.maxWt;
				
				return rotation;
			}
			
			// UPDATING THE WEIGHTS
			this.weight = this.post.weight;
			this.maxWt = this.post.maxWt;
			
			return this;
		}
		
		// Node buildTree(List<Node> list) { return null; }
		
		ArrayList<ExternalNode> getExternalNodes(ArrayList<ExternalNode> arr) { 
			return this.post.getExternalNodes(arr);
		}
		
		ArrayList<String> getPreorderList(int t) { 
			ArrayList<String> list = new ArrayList<String>();
			if (t < this.time) {
				list.addAll(this.pre.getPreorderList(t)); // add pre
			} else { // t is >= time
				list.addAll(this.post.getPreorderList(t)); // add post
			}
			return list;
		}
		
		ArrayList<String> getFullPreorderList() {
			ArrayList<String> list = new ArrayList<String>();
			list.add("<" + this.time + ">");
			list.addAll(this.pre.getFullPreorderList());
			list.addAll(this.post.getFullPreorderList());
			return list;
		}
		
		Value getMin(int t) { 
			if (t < this.time) {
				return this.pre.getMin(t);
			} else {
				return this.post.getMin(t);
			}
		}
		
		Value getMax(int t) {
			if (t < this.time) {
				return this.pre.getMax(t);
			} else {
				return this.post.getMax(t);
			}
		}
		
		Value findUp(Key x, int t) { 
			if (t < this.time) {
				return this.pre.findUp(x, t);
			} else {
				return this.post.findUp(x, t);
			}
		}
		
		Value findDown(Key x, int t) { 
			if (t < this.time) {
				return this.pre.findDown(x, t);
			} else {
				return this.post.findDown(x, t);
			}
		}
	}
	
	// -------------------------------------------------
	// INTERNAL NODE CLASS
	// -------------------------------------------------
	
	private class InternalNode extends Node { // internal node
		Key key; // the key
		Node left; // subtree smaller than key
		Node right; // subtree great or equal to key
		
		InternalNode(Key key, Node left, Node right) {
			this.key = key;
			this.left = left;
			this.right = right;
			this.weight = this.left.weight + this.right.weight;
			this.maxWt = Math.max(this.left.maxWt, this.right.maxWt);
		}
		
		Value find(Key x, int t) { 
			if (x.compareTo(this.key) < 0) {
				return this.left.find(x, t);
			} else {
				return this.right.find(x, t);
			}
		}	
		
		Node insert(Key x, Value v, float w, int t) throws Exception { 
			if (x.compareTo(this.key) < 0) { 
				this.left = this.left.insert(x,  v,  w,  t);
			} else { // x is >= this.key
				this.right = this.right.insert(x,  v,  w,  t);
			}
			
			// Update the weights and max weights as the tree traverses back from the recursion
			this.weight = this.left.weight + this.right.weight;
			this.maxWt = Math.max(this.left.maxWt, this.right.maxWt);
			
			return this;
		}
		
		Node rebalance(Key x,int t) { 
			float bal = (float) ((Math.max(this.left.weight, this.right.weight)) / this.weight);
			float maxR = this.maxWt / this.weight;
			
			// REBUILD SEQUENCE
			if (bal > ALPHA && maxR <= BETA) { // fails one of the (ALPHA, BETA) conditions
				ArrayList<ExternalNode> extList = new ArrayList<ExternalNode>();
				// create array of external nodes 
				extList = this.getExternalNodes(extList);
				InternalNode rebuilt = (InternalNode) rebuild(extList);
				
				rebuilt.weight = this.left.weight + this.right.weight;
				rebuilt.maxWt = Math.max(rebuilt.left.maxWt, rebuilt.right.maxWt);
				
				TemporalNode temp = new TemporalNode(t, this, rebuilt);
				temp.weight = temp.post.weight;
				temp.maxWt = temp.post.maxWt;
				
				return temp;
			} else {
				if (x.compareTo(this.key) < 0) {
					this.left = this.left.rebalance(x, t);
				} else {
					this.right = this.right.rebalance(x, t);
				}
			}
			return this;
		}
		
		// Node buildTree(List<Node> list) { return null; }
		
		ArrayList<ExternalNode> getExternalNodes(ArrayList<ExternalNode> extList) { 
			return this.right.getExternalNodes(this.left.getExternalNodes(extList));
		}
		
		ArrayList<String> getPreorderList(int t) { 
			ArrayList<String> list = new ArrayList<String>();
			list.add("(" + this.key.toString() + ")"); // add this node, ignoring the weight
			list.addAll(this.left.getPreorderList(t)); // add left
			list.addAll(this.right.getPreorderList(t)); // add right
			return list;
		}
		
		ArrayList<String> getFullPreorderList() { 
			ArrayList<String> list = new ArrayList<String>();
			list.add("(" + this.key.toString() + ")"); // add this node, ignoring the weight
			list.addAll(this.left.getFullPreorderList()); // add left
			list.addAll(this.right.getFullPreorderList()); // add right
			return list;
		}
		
		Value getMin(int t) { 
			return this.left.getMin(t);
		}
		
		Value getMax(int t) { 
			return this.right.getMax(t);
		}
		
		Value findUp(Key x, int t) { 
			if (x.compareTo(this.key) < 0) { // x goes in the left subtree
				Value v = this.left.findUp(x, t);
				if (v == null) {
					return this.right.getMin(t);
				} else {
					return v;
				}
			} else { // x goes in the right subtree
				return this.right.findUp(x, t);
			}
		}
		
		Value findDown(Key x, int t) { 
			if (x.compareTo(this.key) < 0) { // x goes in the left subtree
				return this.left.findDown(x, t);
			} else { // x goes in the right subtree
				Value v = this.right.findDown(x, t);
				if (v == null) {
					return this.left.getMax(t);
				} else {
					return v;
				}
			}
		}
		
	}
	
	// -------------------------------------------------
	// EXTERNAL NODE CLASS
	// -------------------------------------------------
	
	private class ExternalNode extends Node { // external node
		Key key; // the key
		Value value; // the associate value object
		
		ExternalNode(Key key, Value value, float w) {
			this.key = key;
			this.value = value;
			this.weight = w;
			this.maxWt = w;
		}
		
		Value find(Key x, int t) { 
			if (x.compareTo(this.key) == 0) {
				return this.value;
			} else {
				return null;
			}
		}	
		
		// ** MAY BE WRONG ** 
		Node insert(Key x, Value v, float w, int t) throws Exception { 
			if (this.value == null) { // Makes a new internal node using the external node key and value
				return new ExternalNode(x, v, w);
			}
			if (x.compareTo(this.key) == 0) {
				throw new Exception("Duplicate Key Error!");
			}
			
			// Converts this external node to internal and makes an external with new key and value
			if (x.compareTo(this.key) < 0) {
				ExternalNode leftIntChild = new ExternalNode(x, v, w);
				InternalNode rightTempChild = new InternalNode(this.key, leftIntChild, this);
				rightTempChild.weight = rightTempChild.left.weight + rightTempChild.right.weight;
				rightTempChild.maxWt = Math.max(rightTempChild.left.maxWt, rightTempChild.right.maxWt);
				
				TemporalNode temp = new TemporalNode(t, this, rightTempChild);
				temp.weight = temp.post.weight;
				temp.maxWt = temp.post.maxWt;
				return temp;
			} else {
				ExternalNode rightIntChild = new ExternalNode(x, v, w);
				InternalNode rightTempChild = new InternalNode(x, this, rightIntChild);
				rightTempChild.weight = rightTempChild.left.weight + rightTempChild.right.weight;
				rightTempChild.maxWt = Math.max(rightTempChild.left.maxWt, rightTempChild.right.maxWt);
				
				TemporalNode temp = new TemporalNode(t, this, rightTempChild);
				temp.weight = temp.post.weight;
				temp.maxWt = temp.post.maxWt;
				return temp;
			}
		}
		
		Node rebalance(Key x, int t) { 
			return this;
		}
		
		// Node buildTree(List<Node> list) { return null; }
		
		ArrayList<ExternalNode> getExternalNodes(ArrayList<ExternalNode> arr) { 
			arr.add(this);
			return arr;
		}
		
		ArrayList<String> getPreorderList(int t) { 
			ArrayList<String> list = new ArrayList<String>();
			list.add("[" + this.key.toString() + " " + this.value.toString() + "] wt: " + this.weight);
			return list;
		}
		
		ArrayList<String> getFullPreorderList() { 
			ArrayList<String> list = new ArrayList<String>();
			list.add("[" + this.key.toString() + " " + this.value.toString() + "] wt: " + this.weight);
			return list;
		}
		
		Value getMin(int t) { 
			return this.value;
		}
		
		Value getMax(int t) { 
			return this.value;
		}
		
		Value findUp(Key x, int t) { 
			if (x.compareTo(this.key) <= 0) {
				return this.value;
			} else {
				return null;
			}
		}
		
		Value findDown(Key x, int t) { 
			if (x.compareTo(this.key) >= 0) {
				return this.value;
			} else {
				return null;
			}
		}
	}
	
	// -------------------------------------------------
	// PUBLIC PBJTree FUNCTIONS
	// -------------------------------------------------
	
	// public interface (and trivial function bodies to keep the compiler from complaining)

	private Node root;
	int firstUpdateTime = -1;
	int lastUpdateTime = -1;
	
	public PBJTree() {
		this.root = null;
	}
	
	// -------------------------------------------------
	// Standard Dictionary Operations 
	// -------------------------------------------------
	
	public Value find(Key x, int t) { 
		if (this.root == null || t < firstUpdateTime) {
			return null;
		} else {
			return this.root.find(x, t);
		}
	}
	
	public void insert(Key x, Value v, float w, int t) throws Exception { 
		if (this.root == null) {
			this.root = new ExternalNode(x, v, w);
			firstUpdateTime = lastUpdateTime = t;
		} else { // tree is non-empty
			if (t > lastUpdateTime) { // insertion times must be monotonically increasing
				lastUpdateTime = t;
				this.root = this.root.insert(x,  v,  w, t);
			} else {
				throw new Exception("Update time must exceed prior update time!");
			}
		}
		if (this.root != null) {
			this.root = this.root.rebalance(x, t);
		}
	}
	
	public ArrayList<String> getPreorderList(int t) { 
		ArrayList<String> array = new ArrayList<>();
		if (this.root == null || t < firstUpdateTime) {
			return array;
		} else {
			return this.root.getPreorderList(t);
		}
	}
	
	public ArrayList<String> getFullPreorderList() { 
		ArrayList<String> array = new ArrayList<>();
		if (this.root == null) {
			return array;
		} else {
			return this.root.getFullPreorderList();
		}
	}
	
	// -------------------------------------------------
	// Additional Dictionary Operations
	// -------------------------------------------------
	
	public Value getMin(int t) { 
		if (this.root != null && t >= firstUpdateTime) {
			return this.root.getMin(t);
		} else {
			return null;
		}
	}
	
	public Value getMax(int t) { 
		if (this.root != null && t >= firstUpdateTime) {
			return this.root.getMax(t);
		} else {
			return null;
		}
	}
	
	public Value findUp(Key x, int t) { 
		if (this.root != null && t >= firstUpdateTime) {
			return this.root.findUp(x, t);
		} else {
			return null;
		}
	}
	
	public Value findDown(Key x, int t) { 
		if (this.root != null && t >= firstUpdateTime) {
			return this.root.findDown(x,  t);
		} else {
			return null;
		}
	}
	
	// --------------------------------------------------
	// buildSubtree, rebuild, and totalWeight functions
	// --------------------------------------------------
	
	protected Node buildSubtree(List<ExternalNode> list) {
		if (list.size() == 1) {
			return list.get(0); // copy this node
		} else {
			float totalWeight = 0;
			for (int i = 0; i < list.size(); i++) { // get total weight in the list
				totalWeight += list.get(i).weight;
			}
			float leftWeight = 0; // weight on the left side
			float rightWeight = totalWeight; // weight on the right side
			int b = 0; // best splitting index
			float minDiff = rightWeight - leftWeight; // minimum difference so far
			for (int i = 0; i < list.size(); i++) {
				leftWeight += list.get(i).weight;
				rightWeight -= list.get(i).weight;
				float curDiff = Math.abs(rightWeight - leftWeight);
				if (curDiff < minDiff) { // found better split point?
					minDiff = curDiff; // save it
					b = i + 1; // split just after this point
				}
			}
			// Invariant: 0 < bestSplit < list.size
			// split and recurse
			Node left = buildSubtree(list.subList(0, b));
			Node right = buildSubtree(list.subList(b, list.size()));
			return new InternalNode(list.get(b).key, left, right);
		}
	}
	
	protected Node rebuild(List <ExternalNode> extList)
	{

		if (extList.size() == 1)
			return extList.get(0);

		float totalWeight = getTotalWeight(extList);
		int b = 0;
		float lWeight = 0;
		float rWeight = totalWeight;
		float deltaMin = totalWeight;

		for (int i = 0; i < extList.size(); i++)
		{
			lWeight += extList.get(i).weight;
			rWeight -= extList.get(i).weight;
			float delta  = Math.abs(rWeight - lWeight);
			if (delta < deltaMin)
			{
				b = i + 1;
				deltaMin = delta;
			}
		}
		Node leftNode = this.rebuild((extList.subList(0, b)));
		Node rightNode = this.rebuild((extList.subList(b, extList.size())));

		return new InternalNode (extList.get(b).key, leftNode, rightNode);
	}
	
	protected float getTotalWeight(List<ExternalNode> extList)
	{
		float totalWeight = 0;
		for (ExternalNode ext : extList)
		{
			totalWeight += ext.weight;
		}
		return totalWeight;
	}
	
}
