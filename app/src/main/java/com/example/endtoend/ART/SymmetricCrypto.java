package com.example.endtoend.ART;

import javax.crypto.spec.SecretKeySpec;
import javax.crypto.Cipher;
import java.util.Base64;
import java.util.Random;

public class SymmetricCrypto {

    public static String encrypt(ARTKey stageKey, String msg) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(stageKey.getKeyInBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(msg.getBytes("UTF-8")));
        }
        catch(Exception e) {
            System.out.println("Error while encrypting: " + e.toString());
            e.printStackTrace();
        }
        return null;
    }

    public static String decrypt(ARTKey stageKey, String ciphertext) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(stageKey.getKeyInBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(ciphertext)));
        }
        catch(Exception e) {
            System.out.println("Error while decrypting: " + e.toString());
            e.printStackTrace();
        }
        return null;
    }

    public static void groupSimulation() {
        int n = 4; //n peers i.e. n+1 members including initiator
        int u = 4; //u updates/messages sent

        ARTKeyPair init_id = ARTKeyPair.getRandom();  //creator/initiator IK
        ARTKeyPair init_ek = ARTKeyPair.getRandom();  //initiator EK
        ARTKeyPair[] peer_ids = new ARTKeyPair[n];    //peer IKs
        ARTKey[] peer_pubIds = new ARTKey[n];
        ARTKeyPair[] peer_eks = new ARTKeyPair[n];    //peer EKs
        ARTKey[] peer_pubEks = new ARTKey[n];

        //generate IKs and EKs for peers
        for(int i=0;i<n;i++) {
            peer_ids[i] = ARTKeyPair.getRandom();
            peer_pubIds[i] = peer_ids[i].getPublicKey();
            peer_eks[i] = ARTKeyPair.getRandom();
            peer_pubEks[i] = peer_eks[i].getPublicKey();
        }

        State init_state = new State(init_id, init_ek);
        State[] peer_states = new State[n];
        State[] groupState = new State[n+1];
        groupState[0] = init_state;
        for(int i=0;i<n;i++) {
            peer_states[i] = new State(peer_ids[i], peer_eks[i]);
            groupState[i+1] = peer_states[i];
        }

        SetupMessage m = Crypto.setupGroup(init_state, peer_pubIds, peer_pubEks);  //initiator calls setup group
        boolean pass = true;
        for(int i=0;i<n;i++) {
            Crypto.processSetupMessage(peer_states[i], m);  //peer processes setup message
            //after peer processes setup msg peer should have same stage key as initiator
            if(!init_state.getStageKey().equals(peer_states[i].getStageKey())) {
                System.out.println("ERROR: stage key mismatch for peer "+peer_states[i].getIndex());
                pass = false;
            }
        }
        if(pass) System.out.println("Group Setup ok");

        for(int i=0;i<u;i++) {
            int u_idx = new Random().nextInt(n+1);   //index of person who will send next msg
            State s = groupState[u_idx];			 //s is senders state
            UpdateKeyMessage upm = Crypto.updateKey(s);  //sender updates key
            System.out.println("Peer "+u_idx+" key updated");
            String msg = "msg"+i;
            String ciphertext = encrypt(s.getStageKey(), msg);   //sender encrypts with updated stage key
            for(int j=0;j<groupState.length;j++) {
                if(groupState[j]!=s) {
                    Crypto.processKeyUpdate(groupState[j], upm);  //peers process updateKeyMessage to get latest stage key
                    if(s.getStageKey().equals(groupState[j].getStageKey())) {
                        System.out.println("Key update successfully processed by peer "+j);
                    }
                    else {
                        System.out.println("ERROR: Key update not successfully processed for peer "+j);
                    }
                    String decryptedMsg = decrypt(groupState[j].getStageKey(), ciphertext);  //peer uses new stage key to decrypt
                    System.out.println("Decrypted msg:"+decryptedMsg);
                    if(decryptedMsg.equals(msg)) {
                        System.out.println("Message successfully reveived by peer "+j);
                    }
                    else {
                        System.out.println("ERROR: Message successfully reveived by peer "+j);
                    }
                }
            }

        }
    }

    public static void main(String[] args) {
        groupSimulation();
    }

}