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

import java.security.SecureRandom;

import edu.jhu.bouncycastle.crypto.CipherParameters;
import edu.jhu.bouncycastle.crypto.InvalidCipherTextException;
import edu.jhu.bouncycastle.crypto.Mac;
import edu.jhu.bouncycastle.crypto.engines.AESEngine;
import edu.jhu.bouncycastle.crypto.modes.EAXBlockCipher;
import edu.jhu.bouncycastle.crypto.params.CCMParameters;
import edu.jhu.bouncycastle.crypto.params.KeyParameter;
import edu.jhu.privtext.util.encoders.UserDataPart;

public class GZEngine {
  private static final byte nTAG = 0x0;

  private static final byte hTAG = 0x1;

  private static final byte cTAG = 0x2;

  private static short myNonce;
  private static SecureRandom myRandom;

  public GZEngine() {
    myRandom = new SecureRandom(); // TODO: where is this class getting
                                   // randomness from?
    myNonce = (short) myRandom.nextInt();
  }

  /**
   * Implements EAX style MAC verification. When used with an OMAC on the same
   * cipher as the encrypted data, is is safe to use the same key for mac tags
   * and encryption.
   * 
   * @param the_alg The MAC algorithm to use. Preferably OMAC/AES
   * @param the_key They key for the mac
   * @param the_message the message to verify.
   * @param the_nonce the message index
   * @return whether the mac tag is valid.
   */
  public static boolean verifyMac(final Mac the_alg, final byte[] the_key,
                                  final UserDataPart the_message, final byte[] the_nonce) {
    final CipherParameters key = new KeyParameter(the_key);
    final byte[] tag = new byte[the_alg.getMacSize()];

    the_alg.init(key);
    tag[blockSize - 1] = hTAG;
    mac.update(tag, 0, blockSize);
    mac.update(associatedText, 0, associatedText.length);
    mac.doFinal(associatedTextMac, 0);

    tag[blockSize - 1] = nTAG;
    mac.update(tag, 0, blockSize);
    mac.update(nonce, 0, nonce.length);
    mac.doFinal(nonceMac, 0);

    tag[blockSize - 1] = cTAG;
    mac.update(tag, 0, blockSize);
    mac.update(tmp, 0, extra);

    byte[] outC = new byte[blockSize];
    mac.doFinal(outC, 0);

    for (int i = 0; i < macBlock.length; i++) {
      macBlock[i] = (byte) (nonceMac[i] ^ associatedTextMac[i] ^ outC[i]);
    }
  }

  public synchronized byte[] encrypt(final byte[] thePlaintext, final byte[] theKey,
                                     final byte[] theNonce, final int MAC_SIZE)
      throws IllegalStateException, InvalidCipherTextException {
    assert MAC_SIZE > 1;
    final byte[] associated_text = new byte[0];
    final EAXBlockCipher eax = new EAXBlockCipher(new AESEngine());
    final KeyParameter keyparam = new KeyParameter(theKey);
    final CCMParameters params =
        new CCMParameters(keyparam, MAC_SIZE * 8, theNonce, associated_text);
    final int minSize = eax.getOutputSize(thePlaintext.length);
    final int macSize = MAC_SIZE;
    final byte[] ciphertext = new byte[minSize + macSize];
    eax.init(true, params);
    int len = eax.processBytes(thePlaintext, 0, thePlaintext.length, ciphertext, 0);
    len += eax.doFinal(ciphertext, len);
    return ciphertext;
  }

  public synchronized byte[] decrypt(final byte[] theCiphertext, final byte[] theKey,
                                     final byte[] theNonce, final int MAC_SIZE)
      throws IllegalStateException, InvalidCipherTextException {
    assert MAC_SIZE > 1;
    final byte[] associated_text = new byte[0];
    final EAXBlockCipher eax = new EAXBlockCipher(new AESEngine());
    final KeyParameter keyparam = new KeyParameter(theKey);
    final CCMParameters params =
        new CCMParameters(keyparam, MAC_SIZE * 8, theNonce, associated_text);

    final int minSize = eax.getOutputSize(theCiphertext.length);
    final int macSize = MAC_SIZE;
    final byte[] plaintext = new byte[minSize - macSize];

    eax.init(false, params);
    int len = eax.processBytes(theCiphertext, 0, theCiphertext.length, plaintext, 0);
    len += eax.doFinal(plaintext, len);

    return plaintext;

  }
}
