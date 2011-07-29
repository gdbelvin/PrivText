package edu.jhu.privtext.util.encoders.test;

import java.util.Random;

import static org.junit.Assert.*;
import org.junit.Test;

import edu.jhu.bouncycastle.util.encoders.Hex;
import edu.jhu.privtext.crypto.GZEncode;
import edu.jhu.privtext.util.encoders.TPDCS;

public class EncodingTest {
	private final int MAX_SMS_BYTES = 140;//Should really be 134

	private void testEncodings(String[] theTestVectors, TPDCS paramTPDCS) {
		if (theTestVectors.length <= 0)
			return;

		for (String s : theTestVectors) {
			byte[] encoded = GZEncode.encodeString(s, paramTPDCS);
			String decoded = GZEncode.decodeString(encoded, paramTPDCS);
			assertEquals(s, decoded);
			assertTrue("encoded to " + encoded.length + " bytes",
					encoded.length <= MAX_SMS_BYTES);

		}
	}

	private void testEncodingsFail(String[] theTestVectors, TPDCS paramTPDCS) {
		if (theTestVectors.length <= 0)
			return;
		
		for (String s : theTestVectors) {
			byte[] encoded = GZEncode.encodeString(s, paramTPDCS);
			String decoded = GZEncode.decodeString(encoded, paramTPDCS);
			assertEquals(s, decoded);
			assertTrue("encoded to " + encoded.length + " bytes",
					encoded.length > MAX_SMS_BYTES);
		}
	}

	public byte[] getRandomData(int paramInt) {
		byte[] arrayOfByte = new byte[paramInt];
		long l = System.currentTimeMillis();
		Random localRandom = new Random(l);
		int i = 0;
		while (true) {
			int j = arrayOfByte.length;
			if (i >= j)
				return arrayOfByte;
			int k = (byte) localRandom.nextInt();
			arrayOfByte[i] = (byte) k;
			i += 1;
		}
	}

	@Test
	public void test7bitEncoding() {
		String[] arrayOfString = new String[3];
		arrayOfString[0] = "";
		arrayOfString[1] = "abcdefghijklmnopqrstuvqxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!@#$%^&*";
		arrayOfString[2] = "0000000000111111111122222222223333333333444444444455555555556666666666777777777788888888889999999999000000000011111111112222222222333333333344444444445555555555";
		TPDCS localTPDCS = TPDCS.SevenBit;
		testEncodings(arrayOfString, localTPDCS);
	}

	@Test
	public void test7bitEncodingFail() {
		String[] arrayOfString = new String[1];
		arrayOfString[0] = "0000000000111111111122222222223333333333444444444455555555556666666666777777777788888888889999999999000000000011111111112222222222333333333344444444445555555555-";
		TPDCS localTPDCS = TPDCS.SevenBit;
		testEncodingsFail(arrayOfString, localTPDCS);
	}

	@Test
	public void test8bitEncoding() {
		String[] arrayOfString = new String[3];
		arrayOfString[0] = "";
		arrayOfString[1] = "abcdefghijklmnopqrstuvqxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!@#$%^&*";
		arrayOfString[2] = "00000000001111111111222222222233333333334444444444555555555566666666667777777777888888888899999999990000000000111111111122222222223333333333";
		TPDCS localTPDCS = TPDCS.EightBit;
		testEncodings(arrayOfString, localTPDCS);
	}

	@Test
	public void test8bitEncodingfail() {
		String[] arrayOfString = new String[1];
		arrayOfString[0] = "00000000001111111111222222222233333333334444444444555555555566666666667777777777888888888899999999990000000000111111111122222222223333333333*";
		TPDCS localTPDCS = TPDCS.EightBit;
		testEncodingsFail(arrayOfString, localTPDCS);
	}

	@Test
	public void testUCS2bitEncoding() {
		String[] arrayOfString = new String[4];
		arrayOfString[0] = "";
		arrayOfString[1] = "abcdefghijklmnopqrstuvqxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!@#$%^&";
		arrayOfString[2] = "0000000000111111111122222222223333333333444444444455555555556666666666";
		arrayOfString[3] = "ʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤ";
		TPDCS localTPDCS = TPDCS.UCS2;
		testEncodings(arrayOfString, localTPDCS);
	}

	@Test
	public void testUCS2bitEncodingFail() {
		String[] arrayOfString = new String[3];
		arrayOfString[0] = "abcdefghijklmnopqrstuvqxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!@#$%^&_-";
		arrayOfString[1] = "0000000000111111111122222222223333333333444444444455555555556666666666-";
		arrayOfString[2] = "ʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤʤ";
		TPDCS localTPDCS = TPDCS.UCS2;
		testEncodingsFail(arrayOfString, localTPDCS);
	}
}