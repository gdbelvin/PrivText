/**
 * PrivText - a secure text messaging API for phones, Copyright (C) 2011 Gary Belvin
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.belvin.privtext.crypto;

import com.belvin.bouncy.crypto.EAXBlockCipher;
import com.belvin.bouncy.crypto.InvalidCipherTextException;
import com.belvin.bouncy.crypto.engines.AESLightEngine;
import com.belvin.bouncy.crypto.params.CCMParameters;
import com.belvin.bouncy.crypto.params.KeyParameter;
import com.belvin.java.security.SecureRandom;

/**
 * @author urbanus
 *
 */

public class GZEngine {

    protected static final int MAC_SIZE = (48 / 8);	  //32 bits min.
    protected static final int HEADER_SIZE = 0;
    public static final int NONCE_SIZE = 1;
    private static short myNonce;
    private static SecureRandom myRandom;

    public GZEngine() {
        myRandom = new SecureRandom();       //TODO: where is this class getting randomness from?
        myNonce = (short) myRandom.nextInt();
    }

    public byte[] getNonce() {
        byte[] nonce = new byte[NONCE_SIZE];
        for (int i = 0; i < NONCE_SIZE; i++) {
            myNonce++;
            nonce[i] = (byte) (myNonce & 0xff);
        }
        return nonce;
    }

    public byte[] packageMessage(byte[] theNonce, byte[] theHeader, byte[] theCiphertext) {
        int len = theNonce.length + theHeader.length + theCiphertext.length;
        byte[] ret = new byte[len];
        System.arraycopy(theNonce, 0, ret, 0, theNonce.length);
        System.arraycopy(theHeader, 0, ret, theNonce.length, theHeader.length);
        System.arraycopy(theCiphertext, 0, ret, theNonce.length + theHeader.length, theCiphertext.length);

        return ret;
    }

    public byte[] getNonce(byte[] thePayload) {
        byte[] ret = new byte[NONCE_SIZE];
        System.arraycopy(thePayload, 0, ret, 0, NONCE_SIZE);
        return ret;
    }

    public byte[] getHeader(byte[] thePayload) {
        byte[] ret = new byte[HEADER_SIZE];
        System.arraycopy(thePayload, NONCE_SIZE, ret, 0, HEADER_SIZE);
        return ret;
    }

    public byte[] getCiphertext(byte[] thePayload) {
        int len = thePayload.length - NONCE_SIZE - HEADER_SIZE;
        byte[] ret = new byte[len];
        System.arraycopy(thePayload, NONCE_SIZE + HEADER_SIZE, ret, 0, len);
        return ret;
    }

    public synchronized byte[] encrypt(byte[] thePlaintext, byte[] theKey, byte[] theNonce)
            throws IllegalStateException, InvalidCipherTextException {

        byte[] associated_text = new byte[0];
        EAXBlockCipher eax = new EAXBlockCipher(new AESLightEngine());
        KeyParameter keyparam = new KeyParameter(theKey);
        CCMParameters params = new CCMParameters(keyparam, MAC_SIZE * 8, theNonce, associated_text);

        int minSize = eax.getOutputSize(thePlaintext.length);
        int macSize = MAC_SIZE;
        byte[] ciphertext = new byte[minSize + macSize];

        eax.init(true, params);
        int len = eax.processBytes(thePlaintext, 0, thePlaintext.length, ciphertext, 0);
        len += eax.doFinal(ciphertext, len);

        return ciphertext;
    }

    public synchronized byte[] decrypt(byte[] theCiphertext, byte[] theKey, byte[] theNonce)
            throws IllegalStateException, InvalidCipherTextException {
        byte[] associated_text = new byte[0];
        EAXBlockCipher eax = new EAXBlockCipher(new AESLightEngine());
        KeyParameter keyparam = new KeyParameter(theKey);
        CCMParameters params = new CCMParameters(keyparam, MAC_SIZE * 8, theNonce, associated_text);

        int minSize = eax.getOutputSize(theCiphertext.length);
        int macSize = MAC_SIZE;
        byte[] plaintext = new byte[minSize - macSize];

        eax.init(false, params);
        int len = eax.processBytes(theCiphertext, 0, theCiphertext.length, plaintext, 0);
        len += eax.doFinal(plaintext, len);

        return plaintext;

    }
}
