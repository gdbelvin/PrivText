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

package edu.jhu.privtext;

/**
 * An interface that defines the external interface to PrivText. Individual
 * phones implement the interface according to their needs
 * @author Gary Belvin
 * @version 0.1
 */
public abstract class SMSIO {
  /** The port used for PDUs. */
  //Standard port for this application
  // 16000-16999 are valid application ports
  private static short APP_PORT = (short) 16474; 

  /**
   * Override this function to provide phone specific data IO to send a PDU.
   * @param the_phonenum to send the message to
   * @param the_port destination application port?
   * @param the_userdata The user portion of the PDU. This is not the raw PDU
   *          unfortunately since Android does not support that functionality
   */
  public abstract void sendPDU(final String the_phonenum, final short the_port,
                               final byte[] the_userdata);

  /**
   * Override this function to provide phone specific metadata gathering and
   * processing needed for privtext before transmission.
   * @param the_phonenum to send the message to
   * @param the_message as a string
   */
  public abstract void sendSecureText(final String the_phonenum, final String the_message);

  /** @return The application port number for Privtext. */ 
  public short getAppPort() {
    return APP_PORT;
  }

}
