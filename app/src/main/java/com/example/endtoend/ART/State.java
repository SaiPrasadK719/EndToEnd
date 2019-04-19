package com.example.endtoend.ART;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class State implements Serializable {
	ARTKeyPair leafKey;
	ARTKeyPair identityKey;
	ARTKeyPair prekey;
	ARTKey treeKey;
	ARTKey stageKey;
	int index;
	ARTKeyNode pubTree;
	ARTKey[] groupPubIdentities;
	ARTKey[] groupPubPrekeys;
	ARTKeyPair[] leafKeys; //test
	public State(ARTKeyPair id, ARTKeyPair ek){
		identityKey = id;
		prekey = ek;
		stageKey = ARTKey.emptyKey();
	}
	
	public ARTKeyPair getLeafKey() {
		return leafKey;
	}

	public void setLeafKey(ARTKeyPair leafKey) {
		this.leafKey = leafKey;
	}

	public ARTKeyPair getIdentityKey() {
		return identityKey;
	}

	public void setIdentityKey(ARTKeyPair identityKey) {
		this.identityKey = identityKey;
	}

	public ARTKeyPair getPrekey() {
		return prekey;
	}

	public void setPrekey(ARTKeyPair prekey) {
		this.prekey = prekey;
	}

	public ARTKey getTreeKey() {
		return treeKey;
	}

	public void setTreeKey(ARTKey treeKey) {
		this.treeKey = treeKey;
	}

	public ARTKey getStageKey() {
		return stageKey;
	}

	public void setStageKey(ARTKey stageKey) {
		this.stageKey = stageKey;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public ARTKeyNode getPubTree() {
		return pubTree;
	}

	public void setPubTree(ARTKeyNode pubTree) {
		this.pubTree = pubTree;
	}

	public ARTKey[] getGroupPubIdentities() {
		return groupPubIdentities;
	}

	public void setGroupPubIdentities(ARTKey[] groupPubIdentities) {
		this.groupPubIdentities = groupPubIdentities;
	}

	public ARTKey[] getGroupPubPrekeys() {
		return groupPubPrekeys;
	}

	public void setGroupPubPrekeys(ARTKey[] groupPubPrekeys) {
		this.groupPubPrekeys = groupPubPrekeys;
	}

	List<ARTKey> getCopath() {
		List<ARTKey> path = new ArrayList<>();
		pubTree.copath(index, path, groupPubIdentities.length);
		return path;
	}
	
	void updatePath(ARTKey[] path, int i) {
		pubTree.updatePath(path, 0, i, groupPubPrekeys.length);
	}
	
}