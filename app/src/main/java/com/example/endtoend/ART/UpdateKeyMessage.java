package com.example.endtoend.ART;

import java.io.Serializable;

public class UpdateKeyMessage implements Serializable {
	int idx;
	ARTKey[] updatedPath;
	public int getIdx() {
		return idx;
	}
	public ARTKey[] getUpdatedPath() {
		return updatedPath;
	}
	public UpdateKeyMessage(int idx, ARTKey[] updatedPath) {
		super();
		this.idx = idx;
		this.updatedPath = updatedPath;
	}
	
}