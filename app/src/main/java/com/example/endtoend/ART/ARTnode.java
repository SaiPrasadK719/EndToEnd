package com.example.endtoend.ART;

import java.io.Serializable;

class ARTnode implements Serializable {
  ARTnode left;
  ARTnode right;
  ARTnode(ARTnode left, ARTnode right){
    //this.key = key;
    this.left = left;
    this.right = right;
  }
  ARTnode getLeft(){
    return left;
  }
  ARTnode getRight(){
    return right;
  }
  
}