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

package edu.jhu.privtext.ssms;

import java.nio.ByteBuffer;

import edu.jhu.bouncycastle.crypto.BlockCipher;
import edu.jhu.bouncycastle.crypto.CipherParameters;
import edu.jhu.bouncycastle.crypto.Mac;
import edu.jhu.bouncycastle.crypto.macs.CMac;
import edu.jhu.bouncycastle.crypto.params.KeyParameter;

/**
 * The algorithms and ciphers that SSMS uses are externally provided by the key
 * agreement layer (for which KAPS may be used).
 * 
 * @author Gary Belvin
 * @version 0.1
 */
public final class Session {
  /** Number of bits in the message index. */
  private static final int MSGINDXBITS = 40;
  /** number of bytes in message index. */
  private static final int MSGINDXBYTES = MSGINDXBITS / Byte.SIZE;
  /** Key length for messages. */
  private static final int MSGKEYLEN = 256;
  /** Replay window size. */
  private static final int REPLAYWINDOW = 4;
  
  // // SSMS specific settings ////
  /**
   * The MAC function used for theREPLAYWINDOW KDF is same MAC used in the key agreement
   * layer.
   */
  private final Mac my_kdfmac;

  /**
   * The block cipher, and authentication tag algorithm and size are also
   * externally specified.
   */
  private final BlockCipher myBlockCipher;
  private final Mac myMacTag = new CMac(myBlockCipher);

  private final int portNumber;
  /** The size of the MAC used in this session. */
  private final short my_macbytes = 3;
  
  /**
   * The ReKey frequency is a policy driven value that determines the upper
   * bound on the number of messages to transmit under a single master key
   * before halting and requesting a new master key (which is provided from
   * somewhere else). The rekey frequency must be less than 240, but normal
   * values will be in the 10 – 100 message range. In the event of an end-point
   * security breach, a lower rekey frequency will reduce the window of readable
   * messages before security is restored. Note that a re-key event may be
   * performed at any time by either party – perhaps especially upon learning of
   * a compromise. More frequent rekeying increases the overhead from the key
   * agreement layer.
   */
  private final int rekeyfrequency = 30;
  
  private final KeyWindow my_keywindow = new KeyWindow(REPLAYWINDOW, MSGKEYLEN);

  /** This session id. */
  private final byte[] my_sessionid;
  
  /// Sequence Number Accounting ///
  /** im = 28 · ROLL+SEQ (mod 240). */
  private long my_messageindex;
  /** A 32 bit roll over counter. */
  private long my_rollovercounter;
  /** The 8 bit sequence number. */
  private byte my_sequencenum;
  /** The last valid sequence number recieved. */
  private byte my_s1;

  public Session(final byte[] the_sessionid) {
    my_sessionid = the_sessionid;
    //= Hex.decode("15B3CA14A92A2A7F2B827A49B901ED76");
  }

  /**
   * Sets the message index using the initial master key. Section 2.5.1
   * 
   * @param the_masterkey The master key material
   * @param the_sessionid the session identifier
   */
  private void initMessageIndex(final byte[] the_masterkey, final byte[] the_sessionid) {
    // Initialize rollover counter
    // Ascii for "InitialIndex"
    final byte[] label =
        {0x49, 0x6e, 0x69, 0x74, 0x69, 0x61, 0x6c, 0x49, 0x6e, 0x64, 0x65, 0x78};
    // session identifier||0
    final byte[] context = new byte[the_sessionid.length + 1];
    System.arraycopy(the_sessionid, 0, context, 0, the_sessionid.length);
    context[the_sessionid.length] = 0x00; // Set the last byte to 0

    // i0 = KDF(Kmaster , “InitialIndex”, session identhe_sessionidtifier||0,
    // 40)
    final byte[] i0 = keyDerivationFunction(the_masterkey, label, context, MSGINDXBITS);

    // The rollover counter is assigned the 32 left most bits of im
    // and the sequence number is assigned the following 8 bits
    // such that 2^8 · ROLL + SEQ = i0
    my_rollovercounter = getUnsignedInt(i0, 0);
    my_sequencenum = i0[4];
    my_keywindow.putFirstKey(the_key, the_index)
  }

  /**
   * Generates the next message encryption key in the perfect-forward-secrecy
   * scheme. Section 2.5.2
   * 
   * @param the_prevkey the previous key
   * @param the_msgindex the index of the message to generate the key for
   * @return the next key
   */
  private byte[] computeMessageKey(final byte[] the_prevkey, final long the_msgindex) {
    // K0 = KDF(Kmaster , “MessageKey”, session identifier||i0 )
    final byte[] label = {0x4d, 0x65, 0x73, 0x73, 0x61, 0x67, 0x65, 0x4b, 0x65, 0x79};

    final ByteBuffer context = ByteBuffer.allocate(my_sessionid.length + MSGINDXBYTES);
    context.put(my_sessionid);
    context.put(putUnsignedInt(the_msgindex));

    return keyDerivationFunction(the_prevkey, label, context.array(), MSGKEYLEN);
  }

  /** @return a long containing an unsigned int from the array */
  private long getUnsignedInt(final byte[] the_array, final int the_offset) {
    return the_array[the_offset] & 0xff << 24 | the_array[the_offset + 1] & 0xff << 16 |
           the_array[the_offset + 2] & 0xff << 8 | the_array[the_offset + 3] & 0xff;
  }

  /** @return a 4 element array containing the unsigned bigendian int. */
  private byte[] putUnsignedInt(final long the_int) {
    byte[] out = new byte[4];
    out[0] = (byte) (the_int & (0xff >> 24));
    out[1] = (byte) (the_int & (0xff >> 16));
    out[2] = (byte) (the_int & (0xff >> 8));
    out[3] = (byte) (the_int & 0xff);
    return out;
  }

  /**
   * Takes a single master key and derives additional keying material. KDF(K,
   * Label, Context, L) = MAC(K, 1||Label||0x00||Context||L). Section 2.5
   * 
   * @param the_key a secret random bit string.
   * @param the_label identifies the purpose of the output key material
   * @param the_context ties the output key material to a particular situation
   * @param the_length determines the size in bits of output key material
   * @return does not need to be kept secret due to the key separation
   *         properties of the KDF
   */
  private byte[] keyDerivationFunction(final byte[] the_key, final byte[] the_label,
                                       final byte[] the_context, final int the_length) {
    assert the_length <= (my_kdfmac.getMacSize() * Byte.SIZE);

    final int inputlen = 6 + the_label.length + the_context.length;
    final ByteBuffer bb = ByteBuffer.allocate(inputlen);
    bb.putInt(1);
    bb.put(the_label);
    bb.put((byte) 0x00);
    bb.put(the_context);
    bb.putInt(the_length);

    final byte[] macresult = new byte[my_kdfmac.getMacSize()];
    my_kdfmac.reset();
    final CipherParameters key = new KeyParameter(the_key);
    my_kdfmac.init(key);
    my_kdfmac.update(bb.array(), 0, inputlen);
    my_kdfmac.doFinal(macresult, 0);

    // shorten to appropriate size
    final int outputlen = (int) Math.ceil(the_length / 8.0);
    final byte[] output = new byte[outputlen];
    System.arraycopy(macresult, 0, output, 0, outputlen);
    // Set the leftmost 8Len - Bits of the rightmost octet to zero
    final int numzeros = Byte.SIZE * outputlen - the_length;
    final byte bitmask = (byte) (0xff << numzeros);
    output[outputlen - 1] = (byte) (output[outputlen - 1] & bitmask);

    return output;
  }

  /**
   * Estimates the index number given a sequence number and the rollover
   * counter.
   * 
   * Adapted from RFC 3711 Appendix A - Pseudocode for Index Determination
   * 
   * @param the_sequencenum of the message
   * @return the index number
   */
  public long getMessageIndex(final byte the_sequencenum) {
    final int halfway = 0x80; // Using 8 bit sequence numbers
    long v;
    if (my_s1 < halfway) {
      if (the_sequencenum - my_s1 > halfway) {
        v = mod32(my_rollovercounter - 1);
      } else {
        v = my_rollovercounter;
      }
    } else {
      if (my_s1 - halfway > the_sequencenum) {
        v = mod32(my_rollovercounter + 1);
      } else {
        v = my_rollovercounter;
      }
    }
    return the_sequencenum + (v << 8);
  }

  /** @return the value of the_num mod 2^32. */
  private long mod32(long the_num) {
    final long two32 = 0x100000000L;
    long r = the_num % two32;
    if (r < 0) {
      r += two32;
    }
    return r;
  }

  /** @return the size of the MAC used in this session in bytes. */
  public short getMacSize() {
    return my_macbytes;
  }
  
  /**
   * Verifies that this message is not out of order. 
   * @param the_messageindex to check
   * @return whether we could have a valid key for that message. 
   */
  public boolean hasKey(final long the_messageindex) {
    boolean havekey = my_keywindow.hasKey(the_messageindex);
    boolean couldhavekey = 
      (the_messageindex - my_keywindow.getHeadIndex()) < REPLAYWINDOW;
    
    return havekey || couldhavekey;
  }
}
