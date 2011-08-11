package com.belvin.privtext.crypto;

import java.io.UnsupportedEncodingException;

import com.belvin.privtext.model.GSM0338;
import com.belvin.privtext.model.TPDCS;

public class GZEncode {
	static TPDCS defaultCoding = TPDCS.EightBit;

	public static byte[] encodeString(String theMessage) {
		return encodeString(theMessage, defaultCoding);
	}

	/**
	 * Converts a string into the plain text blob
	 */
	public static byte[] encodeString(String theMessage, TPDCS encoding) {
		/*
		 * US-ASCII Seven-bit ASCII, a.k.a. ISO646-US, a.k.a. the Basic Latin
		 * block of the Unicode character set ISO-8859-1 ISO Latin Alphabet No.
		 * 1, a.k.a. ISO-LATIN-1 UTF-8 Eight-bit UCS Transformation Format
		 * UTF-16BE Sixteen-bit UCS Transformation Format, big-endian byte order
		 * UTF-16LE Sixteen-bit UCS Transformation Format, little-endian byte
		 * order UTF-16 Sixteen-bit UCS Transformation Format, byte order
		 * identified by an optional byte-order mark
		 */
		try {
			if (encoding.equals(TPDCS.SevenBit)) {
				return GSM0338.encode(theMessage);
			} else {
				return theMessage.getBytes(encoding.getCharsetName());
			}
		} catch (Exception e) {
			e.printStackTrace();
			// return theMessage.getBytes();
			return null;
		} 
	}

	public static String decodeString(byte[] thePlaintext) {
		return decodeString(thePlaintext, defaultCoding);
	}

	public static String decodeString(byte[] thePlaintext, TPDCS encoding) {
		try {
			if (encoding.equals(TPDCS.SevenBit)) {
				return GSM0338.decode(thePlaintext);
			} else {
				return new String(thePlaintext, encoding.getCharsetName());
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
			// return new String(thePlaintext);
		}
	}
}
