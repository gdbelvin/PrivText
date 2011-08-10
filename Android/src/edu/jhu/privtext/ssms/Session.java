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
import edu.jhu.bouncycastle.crypto.digests.Skein;
import edu.jhu.bouncycastle.crypto.engines.AESEngine;
import edu.jhu.bouncycastle.crypto.macs.HMac;
import edu.jhu.bouncycastle.crypto.modes.AEADBlockCipher;
import edu.jhu.bouncycastle.crypto.modes.EAXBlockCipher;
import edu.jhu.bouncycastle.crypto.params.KeyParameter;
import edu.jhu.bouncycastle.util.encoders.Hex;

/**
 * The algorithms and ciphers that SSMS uses are externally provided by the key
 * agreement layer (for which KAPS may be used).
 * 
 * @author Gary Belvin
 * @version 0.1
 */
public class Session {
  /** Number of bits in the message index. */
  protected static final int MSGINDXBITS = 40;
  /** number of bytes in message index. */
  protected static final int MSGINDXBYTES = MSGINDXBITS / Byte.SIZE;
  /** Key length for messages. */
  protected static final int MSGKEYLEN = 256;
  /** Key length in bytes. */
  protected static final int MSGKEYBYTES = MSGKEYLEN / Byte.SIZE;

  /** An empty byte array for key erasure. */
  protected static final byte[] EMPTYKEY = new byte[MSGKEYBYTES];

  // // SSMS specific settings ////
  /**
   * The MAC function used for theREPLAYWINDOW KDF is same MAC used in the key
   * agreement layer.
   */
  private final Mac my_kdfmac;

  /*
   * The block cipher, and authentication tag algorithm and size are also
   * externally specified.
   */
  /**
   * The authenitcated encryption block cipher for encryption and
   * authenitcation.
   */
  private final AEADBlockCipher my_cipher;

  private final int my_portnumber;
  /** The size of the MAC used in this session. */
  private final short my_macbytes = 3;

  /** This session id. */
  private final String my_sessionid;

  public Session(final String the_sessionid) {
    my_sessionid = the_sessionid;
    my_portnumber = 0;
    my_kdfmac = new HMac(new Skein(512, 512));
    my_cipher = new EAXBlockCipher(new AESEngine());
    // = Hex.decode("15B3CA14A92A2A7F2B827A49B901ED76");
  }

  /**
   * Sets the message index using the initial master key. Section 2.5.1
   * 
   * @param the_masterkey The master key material
   * @param the_sessionid the session identifier
   * @return the first message index
   */
  protected long getInitMessageIndex(final byte[] the_masterkey, final String the_sessionid) {
    // Initialize rollover counter
    // Ascii for "InitialIndex"
    final byte[] label =
    {0x49, 0x6e, 0x69, 0x74, 0x69, 0x61, 0x6c, 0x49, 0x6e, 0x64, 0x65, 0x78};
    final byte[] sesid = Hex.decode(the_sessionid);
    // session identifier||0
    final byte[] context = new byte[sesid.length + 1];
    System.arraycopy(sesid, 0, context, 0, sesid.length);
    context[sesid.length] = 0x00; // Set the last byte to 0

    // i0 = KDF(Kmaster , “InitialIndex”, session identhe_sessionidtifier||0,
    // 40)
    final byte[] i0 = keyDerivationFunction(the_masterkey, label, context, MSGINDXBITS);

    // The rollover counter is assigned the 32 left most bits of im
    // and the sequence number is assigned the following 8 bits
    // such that 2^8 · ROLL + SEQ = i0
    final long rollovercounter = getUnsignedInt(i0, 0);
    final long sequencenum = i0[4];
    return (rollovercounter << 8) | (sequencenum & 0xff);
  }

  /**
   * Generates the next message encryption key in the perfect-forward-secrecy
   * scheme. Section 2.5.2
   * 
   * @param the_prevkey the previous key
   * @param the_msgindex the index of the message to generate the key for
   * @return the next key
   */
  protected byte[] computeMessageKey(final byte[] the_prevkey, final long the_msgindex) {
    // K0 = KDF(Kmaster , “MessageKey”, session identifier||i0 )
    final byte[] label = {0x4d, 0x65, 0x73, 0x73, 0x61, 0x67, 0x65, 0x4b, 0x65, 0x79};
    final byte[] sesid = Hex.decode(my_sessionid);

    final ByteBuffer context = ByteBuffer.allocate(sesid.length + MSGINDXBYTES);
    context.put(sesid);
    context.put(putUnsignedInt(the_msgindex));

    return keyDerivationFunction(the_prevkey, label, context.array(), MSGKEYLEN);
  }

  /**  
   * @param the_array to extract the integer from
   * @param the_offset into the array to start from
   * @return a long containing an unsigned int from the array */
  private long getUnsignedInt(final byte[] the_array, final int the_offset) {
    return ((long)(the_array[the_offset] & 0xff) << 24) | 
           ((the_array[the_offset + 1] & 0xff) << 16) |
           ((the_array[the_offset + 2] & 0xff) << 8) | 
           (the_array[the_offset + 3] & 0xff);
  }

  /** @return a 4 element array containing the unsigned bigendian int. */
  private byte[] putUnsignedInt(final long the_int) {
    final byte[] out = new byte[4];
    out[0] = (byte) (the_int & (0xff >> 24));
    out[1] = (byte) (the_int & (0xff >> 16));
    out[2] = (byte) (the_int & (0xff >> 8));
    out[3] = (byte) (the_int & 0xff);
    return out;
  }

  /** @param the_index to parse
   *  @return the 40 bits of the message index. */
  public byte[] getIndexBytes(final long the_index) {
    final byte[] out = new byte[5];
    out[0] = (byte) ((the_index >> 32) & 0xff);
    out[1] = (byte) ((the_index >> 24) & 0xff);
    out[2] = (byte) ((the_index >> 16) & 0xff);
    out[3] = (byte) ((the_index >> 8 ) & 0xff);
    out[4] = (byte) (the_index & 0xff);
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

    final int inputlen = 4+1+4 + the_label.length + the_context.length;
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

  /** @return the value of the_num mod 2^32. */
  protected long mod32(final long the_num) {
    final long two32 = 0x100000000L;
    long r = the_num % two32;
    if (r < 0) {
      r += two32;
    }
    return r;
  }

  /** @return the value of the_num mod 2^40. */
  protected long mod40(final long the_num) {
    final long two40 = 0x10000000000L;
    long r = the_num % two40;
    if (r < 0) {
      r += two40;
    }
    return r;
  }

  /** @return the size of the MAC used in this session in bytes. */
  public short getMacSize() {
    return my_macbytes;
  }

  /** @return the authenticated encryption cipher. */
  public AEADBlockCipher getAECipher() {
    return my_cipher;
  }
}
