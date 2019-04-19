package com.example.endtoend.ART;

import java.io.Serializable;

class ARTPairNode extends ARTnode implements Serializable {
	ARTKeyPair keyPair;
	public ARTKeyPair getKeyPair() {
		return keyPair;
	}
	ARTPairNode(ARTKeyPair pair, ARTPairNode left, ARTPairNode right){
		super(left, right); //fill
		keyPair = pair;
		
	}	
	public static void print(ARTPairNode n, int i, int j) {
		System.out.println(i+","+j+"->"+n.getKeyPair());
		if(n.getLeft()!= null) {
			print((ARTPairNode) n.getLeft(), i+1,2*j);
		}
		if(n.getRight()!= null) {
			print((ARTPairNode) n.getRight(), i+1,2*j+1);
		}
	}
}