package com.belvin.bouncy.crypto;

import java.io.UnsupportedEncodingException;
import java.util.Random;
import com.belvin.bouncy.crypto.engines.AESLightEngine;
import com.belvin.bouncy.crypto.params.CCMParameters;
import com.belvin.bouncy.crypto.params.KeyParameter;
/**
 *
 * @author Administrator
 */
public class BasicAES {
    private final int MAX_SMS_BYTES = 140;
    private final int MAX_SMS_CHARS = 160;  //Using 7bit character padding
    private final int CIPHER_BLOCK_SIZE = 128; 
    //AES 128 bits = 16 bytes into 140 bytes = 8.75 ciphertext blocks
    //What kind of header information is needed?
    private static short myNonce;
    private AESLightEngine myBlockCipher;
    private final int privKeyLen = 128;
    //private static SecureRandom myRandom;
    
    public BasicAES(){
       myBlockCipher = new AESLightEngine();
       //myRandom = new SecureRandom();       //TODO: where is this class getting randomness from?
       Random rand = new Random(System.currentTimeMillis());
       myNonce = (short) rand.nextInt();
    }

    private byte[] getNonce(){
        myNonce++;
        byte[] nonce = new byte[2];
        nonce [0] = (byte)(myNonce & 0xff);
        nonce [1] = (byte)((myNonce >> 8) & 0xff);
        return nonce;
    }

    public void myTest(){
        String theMessage = "This is a long text message";
        byte[] ciphertext;

        byte[] plaintext = encodeString(theMessage);
        byte[] key = getKey();


    }

    public byte[] encrypt(byte[] plaintext, byte[] key) throws IllegalStateException, InvalidCipherTextException{
        int MAC_SIZE = 8;               //TODO: How large should the mac be?
        byte[] nonce = getNonce();      //TODO: How large should the nonce be?
        byte[] associated_text = new byte[0];

        //Message size = [nonce][header][ciphertext[tag?]]

        //Setup the cipher
        EAXBlockCipher eax   = new EAXBlockCipher(new AESLightEngine());
        KeyParameter keyparam = new KeyParameter(key);
        CCMParameters params = new CCMParameters(keyparam, MAC_SIZE, nonce, associated_text);

        int minSize = eax.getOutputSize(plaintext.length);
        byte[] ciphertext   = new byte[minSize];

        eax.init(true, params);
        int len = eax.processBytes(plaintext, 0, plaintext.length, ciphertext, 0);
        len +=    eax.doFinal(ciphertext, len);
      
        return ciphertext;
    }

    public byte[] getRandomData(){
        byte[] iv = new byte[MAX_SMS_BYTES];
        Random rand = new Random(System.currentTimeMillis());
        int i;
        for(i =0; i< iv.length; i++){
            iv[i] = (byte) rand.nextInt();
        }
        return iv;
    }

    private byte[] getKey(){
        byte[] key = new byte[privKeyLen];
        int i = 0;
        for(i = 0; i < privKeyLen; i++){
            key[i] = (byte)i;
        }
        return key;
    }

    /**
     * Converts a string into the plaintext blob
     */
    private byte[] encodeString(String theMessage){
        try {
            return theMessage.getBytes("UTF8");
            //int length = messageBytes.length + myBlockCipher.getBlockSize();
            //byte[] plaintext = new byte[length];
            //System.arraycopy(iv, 0, plaintext, 0, iv.length);
            //System.arraycopy(messageBytes, 0, plaintext, iv.length, messageBytes.length);
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    /**
     * Verifies that a given string can be transmitted within 1 SMS message
     * in plaintext
     */
    private boolean isStringSMSValid(String theMessage){
        return theMessage.length() <= MAX_SMS_CHARS;
    }

}
