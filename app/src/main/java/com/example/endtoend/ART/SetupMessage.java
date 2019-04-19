package com.example.endtoend.ART;

import java.io.Serializable;

public class SetupMessage implements Serializable {
	ARTKey[] groupIdentities;
	public ARTKey[] getGroupIdentities() {
		return groupIdentities;
	}
	public ARTKey[] getGroupPrekeys() {
		return groupPrekeys;
	}
	public ARTKey getPubSetupKey() {
		return pubSetupKey;
	}
	public ARTKeyNode getPubTree() {
		return pubTree;
	}
	ARTKey[] groupPrekeys;
	ARTKey pubSetupKey;
	ARTKeyNode pubTree;
	SetupMessage(ARTKey[] IDs, ARTKey[] EKs, ARTKey SUK, ARTKeyNode T){
		groupIdentities = IDs;
		groupPrekeys = EKs;
		pubSetupKey = SUK;
		pubTree = T;
	}
}