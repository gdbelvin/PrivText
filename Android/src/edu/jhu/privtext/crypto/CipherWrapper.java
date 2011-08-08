/*
 * Copyright (c) 2011 Gary Belvin
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package edu.jhu.privtext.crypto;

import edu.jhu.bouncycastle.crypto.InvalidCipherTextException;
import edu.jhu.bouncycastle.crypto.RuntimeCryptoException;
import edu.jhu.bouncycastle.crypto.modes.AEADBlockCipher;
import edu.jhu.bouncycastle.crypto.params.CCMParameters;
import edu.jhu.bouncycastle.crypto.params.KeyParameter;
import edu.jhu.privtext.util.encoders.UserDataPart;

/**
 * Wrapper for encryption, decryption, and verification functions.
 * @author Gary Belvin
 * @version 0.1
 */
public final class CipherWrapper {

  /** Prevent instantiation of the utility class. */
  private CipherWrapper() {
  }

  /**
   * Implements EAX style MAC verification. When used with an OMAC on the same
   * cipher as the encrypted data, is is safe to use the same key for mac tags
   * and encryption.
   * 
   * @param the_cipher The Authenticated Encryption Scheme to use
   * @param the_key They key for the mac
   * @param the_message the message to verify.
   * @param the_nonce the message index
   * @return whether the mac tag is valid.
   */
  public static boolean verifyMac(final AEADBlockCipher the_cipher, final byte[] the_key,
                                  final UserDataPart the_message, final byte[] the_nonce) {
    final KeyParameter keyparam = new KeyParameter(the_key);
    final byte[] associated_text = the_message.getUserDataHeader();
    final CCMParameters params =
        new CCMParameters(keyparam, the_message.getMacBits(), the_nonce, associated_text);
    the_cipher.init(false, params);

    final int minSize = the_cipher.getOutputSize(the_message.getEncryptedPayload().length);
    final byte[] plaintext = new byte[minSize];

    try {
      final int len =
          the_cipher.processBytes(the_message.getEncryptedPayload(), 0,
                                  the_message.getEncryptedPayload().length, plaintext, 0);
      the_cipher.doFinal(plaintext, len);
      return true;
    } catch (final InvalidCipherTextException e) {
      return false;
    }
  }

  /**
   * Decrypts a message.
   * @param the_cipher The Authenticated Encryption Scheme to use
   * @param the_key They key for the mac
   * @param the_message the message to verify.
   * @param the_nonce the message index
   * @return the plaintext
   */
  public static byte[] decrypt(final AEADBlockCipher the_cipher, final byte[] the_key,
                               final UserDataPart the_message, final byte[] the_nonce) {
    final KeyParameter keyparam = new KeyParameter(the_key);
    final byte[] associated_text = the_message.getUserDataHeader();
    final CCMParameters params =
        new CCMParameters(keyparam, the_message.getMacBits(), the_nonce, associated_text);

    the_cipher.init(false, params);
    final int minSize = the_cipher.getOutputSize(the_message.getEncryptedPayload().length);
    final byte[] plaintext = new byte[minSize];

    try {
      final int len =
          the_cipher.processBytes(the_message.getEncryptedPayload(), 0,
                                  the_message.getEncryptedPayload().length, plaintext, 0);
      the_cipher.doFinal(plaintext, len);
      return plaintext;
    } catch (final InvalidCipherTextException e) {
      return null;
    }
  }

  /**
   * 
   * @param the_cipher the authenticated encryption scheme to use.
   * @param the_key to encrypt with
   * @param the_envelope with a set UserDataHeader and proper mac size set.
   * @param the_message the plaintext
   * @param the_nonce the index of the message to encrypt.
   * @return authenticated encryption payload
   */
  public static byte[] encrypt(final AEADBlockCipher the_cipher, final byte[] the_key,
                               final UserDataPart the_envelope, final byte[] the_message,
                               final byte[] the_nonce) {
    final KeyParameter keyparam = new KeyParameter(the_key);
    final byte[] associated_text = the_envelope.getUserDataHeader();
    final CCMParameters params =
        new CCMParameters(keyparam, the_envelope.getMacBits(), the_nonce, associated_text);

    the_cipher.init(true, params);

    final byte[] ciphertext = new byte[the_cipher.getOutputSize(the_message.length)];
    try {
      final int len =
          the_cipher.processBytes(the_message, 0, the_message.length, ciphertext, 0);
      the_cipher.doFinal(ciphertext, len);
    } catch (final InvalidCipherTextException e) {
      throw new RuntimeCryptoException("Uncaught Cipher Exception");
    }
    return ciphertext;
  }
}
