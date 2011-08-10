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
 * Parses the User Data Header to extract the source and destination phone
 * number and port numbers. Expects 16 bit application port addressing
 * 
 * <pre>
 *   7 6 5 4 3 2 1 0 
 *   +-+-+-+-+-+-+-+ 
 * 0 | UDHL        | 
 * 1 | IEI (0x05)  | 
 * 2 | len (0x02)  |
 * 3 | destination | 
 * 4 | port num    | 
 * 5 | IEI (0x05)  | 
 * 6 | len (0x02)  | 
 * 7 | source      | 
 * 8 | port num    | 
 *   +-+-+-+-+-+-+-+ 
 *   | 8 bit data  | 
 *   +-+-+-+-+-+-+-+
 * </pre>
 * @author Gary Belvin
 * @version 0.1
 */
public class UserDataHeader {
  /** The user data header length. */
  public static final byte UHDL = 6;
  /** The data type in the first field. = application port */
  private static final byte IEI1 = 0x05;
  /** The length of the first field. = 16 bits*/
  private static final byte IEI1LEN = 0x04;
  /** The source port for the application port addressing. */
  private short my_appdstport;
  /** the destination port for in the application port addressing. */
  private short my_appsrcport;

  /**
   * Parses the PDU so the src and dst ports can be extracted.
   * @param the_pdu to read
   */
  public UserDataHeader(final byte[] the_pdu) {
    parse(the_pdu);
  }

  /**
   * Creates a UserDataHeader from scratch with given src and destination ports.
   * @param the_srcport 16 bit application source port
   * @param the_dstport 16 bit application destination port
   */
  public UserDataHeader(final short the_srcport, final short the_dstport) {
    my_appsrcport = the_srcport;
    my_appdstport = the_dstport;
  }

  /**
   * Extracts the user data header fields from a PDU.
   * @param the_pdu to examine.
   * @return true if this is a user data header for 16 bit port addressing
   */
  private boolean parse(final byte[] the_pdu) {
    assert the_pdu.length > UHDL;
    final ByteBuffer bb = ByteBuffer.wrap(the_pdu);
    boolean isvalid = true;
    isvalid = isvalid && (bb.get() == UHDL);
    isvalid = isvalid && (bb.get() == IEI1);
    isvalid = isvalid && (bb.get() == IEI1LEN);
    my_appdstport = bb.getShort();
    my_appsrcport = bb.getShort();
    return isvalid;
  }
  
  /** @return Returns the UDH in byte format. */
  public byte[] getUDH() {
    final ByteBuffer bb = ByteBuffer.allocate(UHDL+1);
    bb.put(UHDL);
    bb.put(IEI1);
    bb.put(IEI1LEN);
    bb.putShort(my_appdstport);
    bb.putShort(my_appsrcport);
    return bb.array();
  }

  /** @return the application source port. */
  public short getSrcPort() {
    return my_appsrcport;
  }

  /** @return the application destination port. */
  public short getDstPort() {
    return my_appdstport;
  }

}
