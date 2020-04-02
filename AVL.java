//-----------------------------------------------------------------------
// Empty AVL exception

import javax.swing.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

class EmptyAVLE extends Exception {
}

//-----------------------------------------------------------------------
// Abstract AVL class

abstract class AVL implements TreePrinter.PrintableNode {

	//--------------------------
	// Static fields and methods
	//--------------------------

	static EmptyAVLE EAVLX = new EmptyAVLE();

	static AVL EAVL = new EmptyAVL();

	static AVL AVLLeaf(int elem) {
		return new AVLNode(
				elem,
				new EmptyAVL(),
				new EmptyAVL()
		);
	}

	// Recursively copy the tree changing AVL nodes to BST nodes
	static BST toBST(AVL avl) {
		List<Integer> ordered = null;
		ordered = OrderAVL.breadth(avl);
		BST bst = new EmptyBST();
		for (Integer integer : ordered) {
			bst = bst.BSTinsert(integer);
		}
		return bst;
	}

	//--------------------------
	// Getters and simple methods
	//--------------------------

	abstract int AVLData() throws EmptyAVLE;

	abstract AVL AVLLeft() throws EmptyAVLE;

	abstract AVL AVLRight() throws EmptyAVLE;

	abstract int AVLHeight();

	abstract boolean isEmpty();

	//--------------------------
	// Main methods
	//--------------------------

	abstract boolean AVLfind(int key);

	abstract AVL AVLinsert(int key);

	abstract AVL AVLeasyRight();

	abstract AVL AVLrotateRight();

	abstract AVL AVLeasyLeft();

	abstract AVL AVLrotateLeft();

	abstract AVL AVLdelete(int key) throws EmptyAVLE;

	abstract Pair<Integer, AVL> AVLshrink() throws EmptyAVLE;
}

//-----------------------------------------------------------------------

class EmptyAVL extends AVL {

	//--------------------------
	// Getters and simple methods
	//--------------------------

	int AVLData() throws EmptyAVLE {
		throw EAVLX;
	}

	AVL AVLLeft() throws EmptyAVLE {
		throw EAVLX;
	}

	AVL AVLRight() throws EmptyAVLE {
		throw EAVLX;
	}

	int AVLHeight() {
		return 0;
	}

	boolean isEmpty() {
		return true;
	}

	;

	//--------------------------
	// Main methods
	//--------------------------

	boolean AVLfind(int key) {
		return false;
	}

	AVL AVLinsert(int key) {
		return AVLLeaf(key);
	}

	AVL AVLeasyRight() {
		throw new Error("Internal bug: should never call easyRight on empty tree");
	}

	AVL AVLrotateRight() {
		throw new Error("Internal bug: should never call rotateRight on empty tree");
	}

	AVL AVLeasyLeft() {
		throw new Error("Internal bug: should never call easyLeft on empty tree");
	}

	AVL AVLrotateLeft() {
		throw new Error("Internal bug: should never call rotateLeft on empty tree");
	}

	AVL AVLdelete(int key) throws EmptyAVLE {
		throw EAVLX;
	}

	Pair<Integer, AVL> AVLshrink() throws EmptyAVLE {
		throw EAVLX;
	}

	//--------------------------
	// Override
	//--------------------------

	@Override
	public boolean equals(Object o) {
		return (o instanceof EmptyAVL);
	}

	//--------------------------
	// Printable interface
	//--------------------------

	public TreePrinter.PrintableNode getLeft() {
		return null;
	}

	public TreePrinter.PrintableNode getRight() {
		return null;
	}

	public String getText() {
		return "";
	}
}

//-----------------------------------------------------------------------

class AVLNode extends AVL {
	private int data;
	private AVL left, right;
	private int height;

	// constructor
	public AVLNode(int d, AVL l, AVL r) {
		this.data = d;
		this.left = l;
		this.right = r;
		this.height = (left.AVLHeight() >= right.AVLHeight()) ? left.AVLHeight() + 1 : right.AVLHeight() + 1;

		/*
		System.out.println("Data: " + data);
		System.out.println("Current height: " + this.height);
		System.out.println("left Height: " + left.AVLHeight());
		System.out.println("right height: " + right.AVLHeight());
		TreePrinter.print(this);

		 */


	}

	//--------------------------
	// Getters and simple methods
	//--------------------------

	int AVLData() {
		return this.data;
	}

	AVL AVLLeft() {
		return this.left;
	}

	AVL AVLRight() {
		return this.right;
	}

	int AVLHeight() {
		return this.height;
	}

	boolean isEmpty() {
		return false;
	}

	//--------------------------
	// Main methods
	//--------------------------

	boolean AVLfind(int key) {
		if (key == this.AVLData()) {
			return true;
		} else if (key < this.AVLData()) {
			return left.AVLfind(key);
		} else {
			return right.AVLfind(key);
		}
	}

	// left is heavy, balance to right
	AVL AVLeasyRight() {
		// identify the new root on the immediate left
		try {
			AVL modifiedRight = new AVLNode(this.data, this.left.AVLRight(), this.right);

			return new AVLNode(this.left.AVLData(), this.left.AVLLeft(), modifiedRight);
		} catch (EmptyAVLE e) {
			return EAVL;
		}
	}

	AVL AVLinsert(int key) {

		if (key < this.data) {
			// before returning from recursion, we must ensure balance
			AVL insertedLeft = this.left.AVLinsert(key);


			//check if post-insertion still has a balanced tree
			if (Math.abs(insertedLeft.AVLHeight() - right.AVLHeight()) > 1) {
				// unbalanced from insertion so left is heavy, rotateRight
				return new AVLNode(this.data, insertedLeft, right).AVLrotateRight();
			} else {
				// still balanced
				return new AVLNode(this.data, insertedLeft, right);
			}


			//return AVL.balancedTree(this, insertedLeft, this.right);

		} else {
			// before returning from recursion, we must ensure balance
			AVL insertedRight = this.right.AVLinsert(key);


			//check if the post-insertion still has a balanced tree
			if (Math.abs(insertedRight.AVLHeight() - left.AVLHeight()) > 1) {
				// unbalanced from insertion so right is heavy, rotateLeft
				return new AVLNode(this.data, left, insertedRight).AVLrotateLeft();
			} else {
				// still balanced
				return new AVLNode(this.data, left, insertedRight);
			}


			//return AVL.balancedTree(this, this.left, insertedRight);
		}

	}


	AVL AVLrotateRight() {
		// left is heavy, balance to right
		try {
			// first check if the left's left child is longer. if so, do a simple easyRight
			if (this.left.AVLLeft().AVLHeight() >= this.left.AVLRight().AVLHeight()) {
				// left's left is longer, doing a simple easyRight on the whole tree

				return this.AVLeasyRight();
			} else {
				// left's right is longer
				// 1) must perform an easyLeft on the left, then 2) easyRight on the whole tree


				AVL leftChildRotated = this.left.AVLeasyLeft();

				return new AVLNode(
						leftChildRotated.AVLData(),
						leftChildRotated.AVLLeft(),
						new AVLNode(
								this.data,
								leftChildRotated.AVLRight(),
								this.right
						)
				);

			}
		} catch (EmptyAVLE e) {
			return EAVL;
		}

	}

	AVL AVLeasyLeft() {
		// identify the new root on the immediate right

		try {
			AVL modifiedLeft = new AVLNode(this.data, this.left, this.right.AVLLeft());
			return new AVLNode(this.right.AVLData(), modifiedLeft, this.right.AVLRight());
		} catch (EmptyAVLE e) {
			return EAVL;
		}
	}

	AVL AVLrotateLeft() {

		try {

			if (this.right.AVLRight().AVLHeight() >= this.right.AVLLeft().AVLHeight()) {
				return this.AVLeasyLeft();
			} else {
				AVL rightChildRotated = this.right.AVLeasyRight();
				return new AVLNode(
						rightChildRotated.AVLData(),
						new AVLNode(
								this.data,
								this.left,
								rightChildRotated.AVLLeft()
						),
						rightChildRotated.AVLRight()
				);
			}

		} catch (EmptyAVLE e) {
			return new EmptyAVL();
		}
	}

	AVL AVLdelete(int key) throws EmptyAVLE {

		if (this.data == key) {
			// key matches root

			if ((this.data == key) && (this.height == 1)) {
				// root to-be-deleted is a leaf, easy case
				return EAVL;
			} else {
				// root to-be-deleted has children, hard case

				// the integer replaces the current root, and the new tree replaces the old left child
				Pair<Integer, AVL> substitute = null;
				AVL newTree = null;
				// check if we can traverse left before calling shrink on the left
				if (this.left.isEmpty()) {
					substitute = new Pair<>(this.right.AVLData(), EAVL);
					newTree = new AVLNode(substitute.getFirst(), EAVL, substitute.getSecond());
				} else {
					substitute = this.left.AVLshrink();
					newTree = new AVLNode(substitute.getFirst(), substitute.getSecond(), right);
				}

				// this is our new tree after the deletion, but may be unbalanced


				// check if the new tree is balanced
				if (Math.abs(newTree.AVLLeft().AVLHeight() - newTree.AVLRight().AVLHeight()) > 1) {
					// unbalanced from deletion
					if (newTree.AVLLeft().AVLHeight() < newTree.AVLRight().AVLHeight()) {
						// left is much smaller
						return newTree.AVLrotateLeft();
					} else {
						// right is much smaller
						return newTree.AVLeasyRight();
					}
				} else {
					//balanced, nothing to do
					return newTree;
				}

			}

		} else if (key < this.data) {
			// key is in left child
			AVL deletedLeft = this.left.AVLdelete(key);

			// check if the post-deletion still has a balanced tree
			if (Math.abs(deletedLeft.AVLHeight() - right.AVLHeight()) > 1) {
				// unbalanced from deletion so right is heavy, rotateLeft
				return new AVLNode(this.data, deletedLeft, right).AVLrotateLeft();
			} else {
				// still balanced
				return new AVLNode(this.data, deletedLeft, right);
			}


		} else {
			// key is in right child
			AVL deletedRight = this.right.AVLdelete(key);

			// check if the post-deletion still has a balanced tree
			if (Math.abs(deletedRight.AVLHeight() - left.AVLHeight()) > 1) {
				// unbalanced from deletion so left is heavy, rotateRight
				return new AVLNode(this.data, left, deletedRight).AVLrotateRight();
			} else {
				// still balanced
				return new AVLNode(this.data, left, deletedRight);
			}

		}


	}

	Pair<Integer, AVL> AVLshrink() throws EmptyAVLE {

		if (this.height == 1) {
			// base case
			return new Pair<>(this.data, EAVL);
		} else if (this.right.isEmpty()) {
			// base case

			// determine how to return from a base case (is left empty?)
			if (this.left.isEmpty()) {
				return new Pair<>(this.data, EAVL);
			} else {
				return new Pair<>(this.data, this.left);
			}
		} else {
			Pair<Integer, AVL> temp = this.right.AVLshrink();
			AVL postDeletion = new AVLNode(this.data, left, temp.getSecond());

			if (Math.abs(postDeletion.AVLLeft().AVLHeight() - postDeletion.AVLRight().AVLHeight()) > 1) {
				// unbalanced
				if (postDeletion.AVLLeft().AVLHeight() < postDeletion.AVLRight().AVLHeight()) {
					// left is much smaller
					AVL balanced = postDeletion.AVLrotateLeft();
					return new Pair<>(temp.getFirst(), balanced);
				} else {
					// right is much smaller
					AVL balanced = postDeletion.AVLrotateRight();
					return new Pair<>(temp.getFirst(), balanced);
				}
			} else {
				// balanced
				return new Pair<>(temp.getFirst(), new AVLNode(this.data, left, temp.getSecond()));
			}


		}


	}


	//--------------------------
	// Override
	//--------------------------

	public boolean equals(Object o) {
		if (o instanceof AVLNode) {
			AVLNode other = (AVLNode) o;
			return data == other.data && left.equals(other.left) && right.equals(other.right);
		}
		return false;
	}

	//--------------------------
	// Printable interface
	//--------------------------

	public TreePrinter.PrintableNode getLeft() {
		return left.isEmpty() ? null : left;
	}

	public TreePrinter.PrintableNode getRight() {
		return right.isEmpty() ? null : right;
	}

	public String getText() {
		return String.valueOf(data);
	}


}

class OrderAVL {
	static List<Integer> inOrder(AVL tree) {
		List<Integer> ordered = new ArrayList<>();
		inOrderHelper(tree, ordered);
		return ordered;
	}

	static List<Integer> breadth(AVL tree) {
		List<Integer> ordered = new ArrayList<>();
		breadthHelper(tree, ordered);
		return ordered;
	}

	static private void inOrderHelper(AVL tree, List<Integer> ordered) {
		try {
			if (!tree.isEmpty()) {
				if (!tree.AVLLeft().isEmpty()) {
					inOrderHelper(tree.AVLLeft(), ordered);
				}

				ordered.add(tree.AVLData());

				if (!tree.isEmpty()) {
					inOrderHelper(tree.AVLRight(), ordered);
				}
			}
		} catch (EmptyAVLE e) {
			e.printStackTrace();
		}
	}

	static private void breadthHelper(AVL tree, List<Integer> ordered) {
		try {
			Queue<AVL> q = new LinkedList<>();
			if (tree.isEmpty()) {
				return;
			}
			q.add(tree);
			while (!q.isEmpty()) {

				AVL n = q.remove();
				ordered.add(n.AVLData());
				if (!n.AVLLeft().isEmpty()) {
					q.add(n.AVLLeft());
				}
				if (!n.AVLRight().isEmpty()) {
					q.add(n.AVLRight());
				}
			}
		} catch (EmptyAVLE e) {
			e.printStackTrace();
		}
	}
}

//-----------------------------------------------------------------------
//-----------------------------------------------------------------------
