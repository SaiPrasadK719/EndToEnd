package com.example.endtoend.ART;

import java.io.Serializable;
import java.util.List;

class ARTKeyNode extends ARTnode implements Serializable {
	ARTKey key;
	public ARTKey getKey() {
		return key;
	}
	ARTKeyNode(ARTKey key, ARTKeyNode left, ARTKeyNode right){
		super(left, right);
		this.key = key;
	}
	void copath(int i, List<ARTKey> p, int treeSize) {
		  int l = Crypto.leftSubtreeSize(treeSize);
		  if( i < l) {
			  p.add(getRight().key);
			  getLeft().copath(i, p, l);
		  }
		  else if(left != null) {
			  p.add(getLeft().key);
			  getRight().copath(i - l, p, treeSize - l);
		  }
	  }
	ARTKeyNode getLeft() {
		return (ARTKeyNode) left;
	}
	ARTKeyNode getRight() {
		return (ARTKeyNode) right;
	}
	public static void print(ARTKeyNode n, int i, int j) {
		System.out.println(i+","+j+"->"+n.getKey());
		if(n.getLeft()!= null) {
			print(n.getLeft(), i+1,2*j);
		}
		if(n.getRight()!= null) {
			print(n.getRight(), i+1,2*j+1);
		}
	}
	void updatePath(ARTKey[] path, int next, int i, int treeSize) {
		if(next  >= path.length) return ;
		key = path[next++];
		int l = Crypto.leftSubtreeSize(treeSize);
		if(i<l) {
			getLeft().updatePath(path, next, i, l);
		}
		else if(getRight() != null){
			getRight().updatePath(path, next, i-l, treeSize-l);
		}
	}
}
