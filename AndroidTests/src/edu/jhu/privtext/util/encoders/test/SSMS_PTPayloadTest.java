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

package edu.jhu.privtext.util.encoders.test;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.jhu.privtext.util.encoders.SSMS_PTPayload;
import edu.jhu.privtext.util.encoders.UserDataPart;


public class SSMS_PTPayloadTest {
	
	@Test
	public void testPTPadding() {
		byte[] plaintext = {1,2,3,4};
		
		UserDataPart ud = new UserDataPart();
		byte[] wrapped = SSMS_PTPayload.wrapPlainText(ud.getMaxPayloadSize(), plaintext);
		byte[] recovered = SSMS_PTPayload.parse(wrapped);
		
		assertArrayEquals(plaintext, recovered);
	}
}
