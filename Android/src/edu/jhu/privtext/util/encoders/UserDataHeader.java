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
  private byte my_uhdl;
  private byte my_iei1;
  private byte my_iei1len;
  private short my_appdstport;
  private byte my_iei2;
  private byte my_iei2len;
  private short my_appsrcport;

  /**
   * Parses the PDU so the src and dst ports can be extracted.
   * @param the_pdu to read
   */
  public UserDataHeader(final byte[] the_pdu) {
    parse(the_pdu);
  }

  /**
   * Extracts the user data header fields from a PDU.
   * @param the_pdu to examine.
   * @return true if this is a user data header for 16 bit port addressing
   */
  private boolean parse(final byte[] the_pdu) {
    final byte portaddressing = 0x05;
    final byte sixteenbitport = 0x02;
    final ByteBuffer bb = ByteBuffer.wrap(the_pdu);
    my_uhdl = bb.get();
    my_iei1 = bb.get();
    // Application port addressing
    if (my_iei1 != portaddressing) {
      return false;
    }
    my_iei1len = bb.get();
    // Two byte addressing
    if (my_iei1len != sixteenbitport) {
      return false;
    }
    my_appdstport = bb.getShort();
    my_iei2 = bb.get();
    // Application port addressing
    if (my_iei2 != portaddressing) {
      return false;
    }
    my_iei2len = bb.get();
    if (my_iei2len != sixteenbitport) {
      return false;
    }
    my_appsrcport = bb.getShort();
    return true;
  }

  public short getSrcPort() {
    return my_appsrcport;
  }

  public short getDstPort() {
    return my_appdstport;
  }

}
