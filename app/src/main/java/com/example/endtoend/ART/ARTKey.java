package com.example.endtoend.ART;

import java.io.Serializable;
import java.util.Arrays;

public class ARTKey implements  Serializable{
	byte[] k;
	byte[] getKeyInBytes() {
		return k;
	}
	ARTKey(byte[] b) {
		k = b;
	}
	public static ARTKey emptyKey() {
		return new ARTKey(new byte[0]);
	}
	public boolean equals(Object o) {
		if(o==null) return false;
		if(this == o) return true;
		if(o instanceof ARTKey) {
			ARTKey ok = (ARTKey) o;
			if(Arrays.equals(this.k, ok.k)) return true;
		}
		return false;
	}
	public String toString() {
		return Arrays.toString(k);
	}
}
