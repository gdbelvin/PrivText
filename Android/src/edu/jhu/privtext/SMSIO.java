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
 * An interface that defines the external interface to PrivText. 
 * Individual phones implement the interface according to their needs
 * @author Gary Belvin
 *
 */
public abstract class SMSIO {
	/** The port used for PDUs */
	static short APP_PORT = (short) 16474; 	//Standard port for this application
	// 16000-16999 are valid application ports

	/**
	 * @param thePhoneNo
	 * @param theUserData  The user portion of the PDU.  
	 * This is not the raw PDU unfortunately since Android does not 
	 * support that functionality
	 */
	public abstract void sendPDU(String thePhoneNo, byte[] theUserData);

	public abstract void sendText(String thePhoneNo, String theMessage);

	public short getAppPort() {
		return APP_PORT;
	}

}
