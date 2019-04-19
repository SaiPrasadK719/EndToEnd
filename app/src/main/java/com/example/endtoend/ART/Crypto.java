package com.example.endtoend.ART;

import 	djb.Curve25519;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import at.favre.lib.crypto.HKDF;
import java.util.Random;


public  class Crypto {
  public static ARTPairNode createTree(ARTKeyPair[] leafKeys, int start, int end){
    int len = end - start;
    if(len == 1){
      return new ARTPairNode(leafKeys[start], null, null);
    }
    else {
      int h = leftSubtreeSize(len);
      ARTPairNode left = createTree(leafKeys, start, start + h);
      ARTPairNode right = createTree(leafKeys, start + h, end);
      return new ARTPairNode(left.getKeyPair().getKeyPairFromSharedSecret(right.getKeyPair().getPublicKey()), left, right);
    }
  }
  public static int leftSubtreeSize(int x){
    if(Integer.bitCount(x) <= 1){
      return x >> 1;
    }
    else {
      return Integer.highestOneBit(x);
    }
  }
  public static ARTKeyNode publicKeys(ARTPairNode tree){
    if(tree == null) return null;
    ARTKeyNode left = publicKeys((ARTPairNode) tree.getLeft());
    ARTKeyNode right = publicKeys((ARTPairNode) tree.getRight());
    return new ARTKeyNode(tree.getKeyPair().getPublicKey(), left, right);
  }
  public static SetupMessage setupGroup(State state, ARTKey[] identityKeys, ARTKey[] prekeys) {
	  state.setLeafKey(ARTKeyPair.getRandom());
	  ARTKeyPair suk = ARTKeyPair.getRandom();
	  ARTKeyPair[] leafKeys = new ARTKeyPair[prekeys.length + 1];
	  leafKeys[0] = state.getLeafKey();
	  ARTKey[] groupIdentities = new ARTKey[identityKeys.length + 1];
	  groupIdentities[0] = state.getIdentityKey().getPublicKey();
	  ARTKey[] groupPrekeys = new ARTKey[prekeys.length + 1];
	  groupPrekeys[0] = state.getPrekey().getPublicKey();
	  for(int i=0; i < prekeys.length; i++) {
		  leafKeys[i + 1] = keyExchange(state.getIdentityKey(), identityKeys[i], suk, prekeys[i]);
		  //System.out.println(leafKeys[i+1]);
		  groupIdentities[i+1] = identityKeys[i];
		  groupPrekeys[i+1] = prekeys[i];
	  }
	  state.leafKeys = leafKeys; //test
	  ARTPairNode secretTree = createTree(leafKeys, 0, leafKeys.length);
	  //System.out.println("secret tree:");
	  //ARTPairNode.print(secretTree, 0, 0);
	  state.setPubTree(publicKeys(secretTree));
	  //System.out.println("pub tree");
	  //ARTKeyNode.print(state.getPubTree(), 0, 0);
	  state.setGroupPubIdentities(groupIdentities);
	  state.setGroupPubPrekeys(groupPrekeys);
	  SetupMessage setupMsg = new SetupMessage(groupIdentities, groupPrekeys, suk.getPublicKey(), state.getPubTree());
	  /*if(!suk.getPublicKey().equals(setupMsg.getPubSetupKey())) {
		  System.out.println("Setup key mismatch");
	  }
	  else {
		  System.out.println("Setup key ok");
	  }*/
	  state.setTreeKey(secretTree.getKeyPair().getPrivKey());
	  state.setIndex(0);
	  deriveStageKey(state);
	  return setupMsg;
  }
  
  public static void processSetupMessage(State state, SetupMessage setupMsg) {
	  state.setGroupPubIdentities(setupMsg.getGroupIdentities());
	  state.setGroupPubPrekeys(setupMsg.getGroupPrekeys());
	  state.setPubTree(setupMsg.getPubTree());
	  for(int i=0; i < setupMsg.getGroupIdentities().length; i++) {
		  if(state.getIdentityKey().getPublicKey().equals(setupMsg.getGroupIdentities()[i])) {
			  state.setIndex(i);
		  }
	  }
	  ARTKeyPair leafKey = keyExchange(state.getIdentityKey(), state.getGroupPubIdentities()[0], state.getPrekey(), setupMsg.getPubSetupKey());
	  //System.out.println("derived leaf:" + leafKey);
	  state.setLeafKey(leafKey);
	  ARTKeyPair[] nks = pathNodeKeys(leafKey, state.getCopath());
	  state.setTreeKey(nks[0].getPrivKey());
	  deriveStageKey(state);
	  
  }
  
  public static UpdateKeyMessage updateKey(State state) {
	  ARTKeyPair newLeaf = ARTKeyPair.getRandom();
	  state.setLeafKey(newLeaf);
	  ARTKeyPair[] nks = pathNodeKeys(newLeaf, state.getCopath());
	  state.setTreeKey(nks[0].getPrivKey());
	  ARTKey[] publicPath = new ARTKey[nks.length];
	  for(int i = 0; i < nks.length; i++) {
		  publicPath[i] = nks[i].getPublicKey();
	  }
	  state.updatePath(publicPath, state.getIndex());
	  deriveStageKey(state);
	  return new UpdateKeyMessage(state.getIndex(), publicPath);
  }
  
  public static void processKeyUpdate(State state, UpdateKeyMessage updateMsg) {
	  state.updatePath(updateMsg.getUpdatedPath(), updateMsg.getIdx());
	  ARTKeyPair[] nks = pathNodeKeys(state.getLeafKey(), state.getCopath());
	  state.setTreeKey(nks[0].getPrivKey());
	  deriveStageKey(state);
  }
  
  
  public static ARTKeyPair keyExchange(ARTKeyPair priv1, ARTKey pub1, ARTKeyPair priv2, ARTKey pub2) {
	  return priv2.getKeyPairFromSharedSecret(pub2);
  }
  public static void deriveStageKey(State state) {
	  byte[] tk = state.getTreeKey().getKeyInBytes();
	  byte[] sk = state.getStageKey().getKeyInBytes();
	  byte[] concat = new byte[tk.length + sk.length];
	  System.arraycopy(tk, 0, concat, 0, tk.length);
	  System.arraycopy(sk, 0, concat, tk.length, sk.length);
	  HKDF hkdf = HKDF.fromHmacSha256();
	  byte[] pseudoRandomKey = hkdf.extract(null, concat);
	  byte[] expandedKey = hkdf.expand(pseudoRandomKey, null, 16);
	  state.setStageKey(new ARTKey(expandedKey));
  }
  public static ARTKeyPair[] pathNodeKeys(ARTKeyPair leafKey, List<ARTKey> copath) {
	  ARTKeyPair[] nks = new ARTKeyPair[copath.size() + 1];
	  nks[copath.size()] = leafKey;
	  for(int j = copath.size() - 1; j >= 0; j--) {
		  nks[j] = nks[j+1].getKeyPairFromSharedSecret(copath.get(j));
	  }
	  return nks;
  }
}
