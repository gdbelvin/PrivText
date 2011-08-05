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

package edu.jhu.privtext.util.encoders;

import java.nio.ByteBuffer;

/**
 * Encapsulates a raw PDU.
 * 
 * @author Gary Belvin
 * @version 0.1
 * 
 * <pre>
   7 6 5 4 3 2 1 0 
   +-+-+-+-+-+-+-+
0  |  User Data  |
.. |  Header     |
8  |             |
   +-+-+-+-+-+-+-+
9  |   SEQ       |
   +-+-+-+-+-+-+-+<-+
10 |  Data Len   |  |
11 |  payload    |  +-- 127 Encrypted octets
.. |    " "      |  |   126 Octet maximum payload size
   | 0x00 Padding|  |
   +-+-+-+-+-+-+-+<-+   
137|             |
138|   24 bit    |
139|    MAC      |
   +-+-+-+-+-+-+-+
   </pre>
 */
public class UserDataPart {
  /** The size of the sequence number in bytes. */
  public static final int SEQ_SIZE = 1;

  /** The maximum size of the total PDU in bytes. */
  public static final int MAX_PDU_SIZE = 140;
  
  /** The size of the UserDataHeader in bytes. */
  protected static final int UDH_SIZE = 9;

  /**
   * The size of the mac tag in bytes. If field scenarios detect that the MACs
   * are being attacked, this value can be increased to 32 or 48 bits.
   */
  private static final int DefaultMAC_size = (24 / 8);
  private final int my_macbytes;
  /** The maximum size of the encrypted data payload. */
  private final byte MAXEncPayload;

  private byte[] myUserDataHeader = new byte[UDH_SIZE];
  /** The Sequence Number. */
  private byte my_sequenceNum;
  private byte[] myEncryptedPayload;
  private byte[] myMac;

  /** Create a UserDataPart using the default MAC size. */
  public UserDataPart() {
    this(DefaultMAC_size);
  }

  /**
   * Create the user data part with a custom MAC size.
   * 
   * @param the_macsize in bytes
   */
  public UserDataPart(final int the_macsize) {
    assert the_macsize >= 2 && the_macsize <= (MAX_PDU_SIZE - SEQ_SIZE);
    MAXEncPayload = (byte) (MAX_PDU_SIZE - (UDH_SIZE + SEQ_SIZE + the_macsize));

    my_macbytes = the_macsize;
    my_sequenceNum = 0;
    myEncryptedPayload = new byte[MAXEncPayload];
    myMac = new byte[the_macsize];
  }

  /**
   * Breaks down a PDU into its parts according to this format.
   * 
   * @param the_userdata of the message to be parsed
   * @param the_macsize of the message to be parsed
   */
  public UserDataPart(final byte[] the_userdata, final int the_macsize) {
    assert the_macsize >= 2 && the_macsize <= (MAX_PDU_SIZE - SEQ_SIZE);

    my_macbytes = the_macsize;
    MAXEncPayload = (byte) (MAX_PDU_SIZE - (UDH_SIZE + SEQ_SIZE + the_macsize));
    
    parse(the_userdata);
  }

  /**
   * Create a custom UserDataPart with the default MAC size.
   * 
   * @param the_seq Sequence number
   * @param the_ciphertext Ciphertext
   * @param the_mac message authentication code
   */
  public UserDataPart(final byte the_seq, final byte[] the_ciphertext, 
                      final byte[] the_mac) {
    this(the_seq, the_ciphertext, the_mac, DefaultMAC_size);
  }

  /**
   * Create a completely custom UserDataPart.
   * 
   * @param the_seq sequence number
   * @param the_ciphertext ciphertext
   * @param the_mac message authentication code
   * @param the_macsize custom message authentication code size in bytes
   */
  public UserDataPart(final byte the_seq, final byte[] the_ciphertext, final byte[] the_mac,
                      final int the_macsize) {
    assert the_macsize >= 2 && the_macsize <= (MAX_PDU_SIZE - SEQ_SIZE);
    MAXEncPayload = (byte) (MAX_PDU_SIZE - (UDH_SIZE + SEQ_SIZE + the_macsize));

    my_macbytes = the_macsize;
    setSequenceNumber(the_seq);
    setEncryptedPayload(the_ciphertext);
    setMac(the_mac);
  }

  /** Extracts the appropriate fields from the payload. */
  private void parse(final byte[] the_payload) {
    myUserDataHeader = new byte[UDH_SIZE];
    my_sequenceNum = 0;
    final int ctextlen = the_payload.length - UDH_SIZE - SEQ_SIZE;
    myEncryptedPayload = new byte[ctextlen];
    myMac = new byte[my_macbytes];

    System.arraycopy(the_payload, 0, myUserDataHeader, 0, UDH_SIZE);
    System.arraycopy(the_payload, UDH_SIZE, my_sequenceNum, 0, SEQ_SIZE);
    System.arraycopy(the_payload, UDH_SIZE + SEQ_SIZE, myEncryptedPayload, 0, ctextlen);
    System.arraycopy(the_payload, UDH_SIZE + SEQ_SIZE + ctextlen, myMac, 0, my_macbytes);
  }

  /** @return everything past the User Data Header */
  public byte[] getUserData() {
    final int len = SEQ_SIZE + MAXEncPayload + my_macbytes;
    final ByteBuffer ud = ByteBuffer.allocate(len);
    ud.put(my_sequenceNum);
    ud.put(myEncryptedPayload);
    ud.put(myMac);

    return ud.array();
  }

  public byte getMaxPayloadSize() {
    return MAXEncPayload;
  }

  public byte[] getUserDataHeader() {
    return myUserDataHeader;
  }

  public void setUserDataHeader(final byte[] myUserDataHeader) {
    assert myUserDataHeader.length == UDH_SIZE;
    this.myUserDataHeader = myUserDataHeader;
  }

  /** @return the sequence number of the message. */
  public byte getSequenceNumber() {
    return my_sequenceNum;
  }

  /** Set the sequence number.
   * @param the_sequencenum the sequence number.
   */
  public void setSequenceNumber(final byte the_sequencenum) {
    this.my_sequenceNum = the_sequencenum;
  }

  public byte[] getEncryptedPayload() {
    return myEncryptedPayload;
  }

  public void setEncryptedPayload(final byte[] theEncryptedPayload) {
    assert (theEncryptedPayload.length + UDH_SIZE + SEQ_SIZE + my_macbytes) <= MAX_PDU_SIZE;
    this.myEncryptedPayload = theEncryptedPayload;
  }

  /** @return the mac value. */
  public byte[] getMac() {
    return myMac;
  }

  /**
   * Sets the mac tag value.
   * @param the_mac size must be equal to the initialized mac size
   */
  public void setMac(final byte[] the_mac) {
    assert the_mac.length == my_macbytes;
    this.myMac = the_mac;
  }
  
  /**
   * @return The size of the mac in bits.
   */
  public int getMacBits() {
    return my_macbytes * Byte.SIZE;
  }
  
  /** @return the size of the mac in bytes. */
  public int getMacBytes() {
    return my_macbytes;
  }
}
