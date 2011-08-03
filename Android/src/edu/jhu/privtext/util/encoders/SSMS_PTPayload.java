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

import android.telephony.SmsMessage;

/**
 * The plain text message format
 * "The cipher text itself contains a padding format: 
 * First a one octet value denoting the length of the plaintext, 
 * the plaintext, optionally followed a null padding to 
 * expand the payload to the full 140 octet envelope" 
 * @author Gary Belvin
 *
 */
public class SSMS_PTPayload {
	/** A flag that turns on plain-text padding
	 * Security implication: by making all messages equal length, 
	 * an adversary will not be able to learn anything from the ciphertext length. 
	 */
	private final static boolean PlaintextPadding = true;

	public static byte[] parse(byte[] thePayload) {
		// The first byte should be the length
		ByteBuffer bb = ByteBuffer.wrap(thePayload);
		byte ptlen = bb.get();
		byte[] pt = new byte[ptlen];
		bb.get(pt);

		return pt;
	}

	/**
	 * Wraps a plain text message with the appropriate header and padding. 
	 * @param theMaxPayloadSize The size of the desired resulting package. 
	 * This is determined by the size of MAC used in the SSMS_UserData layer
	 * @param thePlaintext 
	 * @return A message of the format [bytes of plaintext][the plaintext][null padding]
	 */
	public static byte[] wrapPlainText(byte theMaxPayloadSize,
			byte[] thePlaintext) {
		assert theMaxPayloadSize > 1
				&& theMaxPayloadSize < SmsMessage.MAX_USER_DATA_SEPTETS_WITH_HEADER;
		assert (thePlaintext.length + 1) <= theMaxPayloadSize;

		byte outputlen;
		if (PlaintextPadding) {
			outputlen = theMaxPayloadSize;

		} else {
			// Reserve one byte for the payload length
			outputlen = (byte) (thePlaintext.length + 1);
		}

		ByteBuffer ud = ByteBuffer.allocate(outputlen);
		ud.put((byte) thePlaintext.length);
		ud.put(thePlaintext);
		// The if statement is probably unnecessary but adds clarity.
		if (PlaintextPadding) {
			int paddinglen = outputlen - 1 - thePlaintext.length;
			byte[] padding = new byte[paddinglen];
			ud.put(padding);
		}

		return ud.array();
	}

}
