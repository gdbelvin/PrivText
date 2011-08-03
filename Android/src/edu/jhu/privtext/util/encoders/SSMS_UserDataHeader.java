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

/**
 * Parses the User Data Header to extract the source and destination
 * phone number and port numbers. Expects 16 bit application port addressing
 * 
 *    7 6 5 4 3 2 1 0 
 *    +-+-+-+-+-+-+-+
 * 0  |     UDHL    |
 * 1  | IEI (0x05)  |  
 * 2  | len (0x02)  |
 * 3  | destination |
 * 4  |  port num   |
 * 5  | IEI (0x05)  |
 * 6  | len (0x02)  |
 * 7  |   source    |
 * 8  |  port num   |
 *    +-+-+-+-+-+-+-+
 *    |  8 bit data |
 *    +-+-+-+-+-+-+-+
 * @author Gary Belvin
 *
 */
public class SSMS_UserDataHeader {
	private byte myUHDL;
	private byte myIEI1;
	private byte mylen1;
	private short myDstPort;
	private byte myIEI2;
	private byte mylen2;
	private short mySrcPort;
	
	public SSMS_UserDataHeader(final byte[] thePDU) {
		
	}
	
	private void parse(final byte[] thePDU) {
		
	}
	
	public short getSrcPort(){
		return mySrcPort;
	}
	
	public short getDstPort() {
		return myDstPort;
	}

}
