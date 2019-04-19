package com.example.endtoend.ART;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.Arrays;

import at.favre.lib.crypto.HKDF;
import djb.Curve25519;

public class ARTKeyPair implements Serializable {
	byte[] pr;
	byte[] pu;
	ARTKeyPair(byte[] key_bytes){
		pr = key_bytes;
		pu = new byte[Curve25519.KEY_SIZE];
		Curve25519.keygen(pu, null, pr);
	}
	public static ARTKeyPair getRandom() {
		byte[] key_bytes = new byte[Curve25519.KEY_SIZE];
		new SecureRandom().nextBytes(key_bytes);
		return new ARTKeyPair(key_bytes);
	}
	private byte[] sharedSecret(ARTKey pub) {
		byte[] sec = new byte[Curve25519.KEY_SIZE];
		Curve25519.curve(sec, this.pr, pub.getKeyInBytes());
		return sec;
	}
	public ARTKeyPair getKeyPairFromSharedSecret(ARTKey pub) {
		byte[] sharedSecret = sharedSecret(pub);
		HKDF hkdf = HKDF.fromHmacSha256();
		byte[] pseudoRandomKey = hkdf.extract(null, sharedSecret);
		byte[] expandedKey = hkdf.expand(pseudoRandomKey, null, 32);
	    return new ARTKeyPair(expandedKey);
	}
	public ARTKey getPublicKey() {
		return new ARTKey(pu);
	}
	public ARTKey getPrivKey() {
		return new ARTKey(pr);
	}
	public boolean equals(Object o) {
		if(o==null) return false;
		if(this == o) return true;
		if(o instanceof ARTKeyPair) {
			ARTKeyPair ok = (ARTKeyPair) o;
			if(Arrays.equals(this.pr, ok.pr) && Arrays.equals(this.pu, ok.pu)) return true;
		}
		return false;
	}
	public String toString() {
		return Arrays.toString(pu) + ":" + Arrays.toString(pr);
	}
}