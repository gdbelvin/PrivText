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
import edu.jhu.bouncycastle.crypto.engines.AESEngine;
import edu.jhu.bouncycastle.crypto.modes.AEADBlockCipher;
import edu.jhu.bouncycastle.crypto.modes.EAXBlockCipher;
import edu.jhu.bouncycastle.crypto.params.CCMParameters;
import edu.jhu.bouncycastle.crypto.params.KeyParameter;
import edu.jhu.privtext.util.encoders.UserDataPart;

public class GZEngine {
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

    final int minSize = the_cipher.getOutputSize(the_message.getEncryptedPayload().length);
    final byte[] plaintext = new byte[minSize - the_message.getMacBytes()];

    the_cipher.init(false, params);
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

    final int minSize = the_cipher.getOutputSize(the_message.getEncryptedPayload().length);
    final byte[] plaintext = new byte[minSize - the_message.getMacBytes()];

    the_cipher.init(false, params);
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

  public static byte[] encrypt(final AEADBlockCipher the_cipher, final byte[] the_key,
                               final UserDataPart the_message, final byte[] the_nonce) {
    final KeyParameter keyparam = new KeyParameter(the_key);
    final byte[] associated_text = the_message.getUserDataHeader();
    final CCMParameters params =
        new CCMParameters(keyparam, the_message.getMacBits(), the_nonce, associated_text);

    final int minSize = the_cipher.getOutputSize(the_message.get.length);
    final int macSize = MAC_SIZE;
    final byte[] ciphertext = new byte[minSize + macSize];
    eax.init(true, params);
    int len = eax.processBytes(thePlaintext, 0, thePlaintext.length, ciphertext, 0);
    len += eax.doFinal(ciphertext, len);
    return ciphertext;
  }
}
