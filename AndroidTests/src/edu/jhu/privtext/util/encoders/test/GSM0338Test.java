package edu.jhu.privtext.util.encoders.test;

import static org.junit.Assert.*;

import java.nio.charset.CharacterCodingException;
import java.security.SecureRandom;
import java.util.Random;

import org.junit.Ignore;
import org.junit.Test;

import edu.jhu.privtext.util.encoders.GSM0338;

public class GSM0338Test {
	private Random random;

	public GSM0338Test() {
		SecureRandom localSecureRandom = new SecureRandom();
		this.random = localSecureRandom;
	}

	public void fuzz(byte[] paramArrayOfByte, int paramInt1, int paramInt2) {
		byte[] arrayOfByte = new byte[paramInt2];
		this.random.nextBytes(arrayOfByte);
		int i = arrayOfByte.length;
		System.arraycopy(arrayOfByte, 0, paramArrayOfByte, paramInt1, i);
	}

	public String fuzzString(int paramInt) {
		StringBuilder localStringBuilder1 = new StringBuilder();
		int i = 0;
		while (true) {
			if (i >= paramInt)
				return localStringBuilder1.toString();
			Random localRandom = this.random;
			int j = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890\n\r'\\-=|!@#$%^&*()_+{}[];:\",.<>/?~"
					.length();
			int k = localRandom.nextInt(j);
			char c = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890\n\r'\\-=|!@#$%^&*()_+{}[];:\",.<>/?~"
					.charAt(k);
			StringBuilder localStringBuilder2 = localStringBuilder1.append(c);
			i += 1;
		}
	}

	@Test
	public void testAlignment() throws CharacterCodingException {
		String[] arrayOfString = new String[15];
		arrayOfString[0] = "";
		arrayOfString[1] = "a";
		arrayOfString[2] = "ab";
		arrayOfString[3] = "12345678\r";
		arrayOfString[4] = "12345678@";
		arrayOfString[5] = "_;1i|R'Z\rRPgw@/r#'lXHm@";
		arrayOfString[6] = "][o{X6#[4X~";
		arrayOfString[7] = "12345678\r";
		arrayOfString[8] = "123456\r";
		arrayOfString[9] = "1234567\r";
		arrayOfString[10] = "1234567@";
		arrayOfString[11] = "123456@";
		arrayOfString[12] = "123456";
		arrayOfString[13] = "1234567";
		arrayOfString[14] = "12345678";
		int i = arrayOfString.length;
		int j = 0;
		while (true) {
			if (j >= i)
				return;
			String str1 = arrayOfString[j];
			String str2 = GSM0338.decode(GSM0338.encode(str1));
			assertEquals(str1, str2);
			j += 1;
		}
	}

	@Test
	public void testDecode() {
		String str = GSM0338.decode(new byte[] { 73, 58, 40, 61, 7, (byte) 149,
				(byte) 195, (byte) 243, 60, (byte) 136, (byte) 254, 6,
				(byte) 205, (byte) 203, 110, 50, (byte) 136, 94, (byte) 198,
				(byte) 211, 65, (byte) 237, (byte) 242, 124, 30, 62,
				(byte) 151, (byte) 231, 46 });
		assertEquals("It is easy to send text messages.", str);
	}

	@Test
	public void testEncode() throws CharacterCodingException {
		byte[] arrayOfByte1 = { 73, 58, 40, 61, 7, (byte) 149, (byte) 195,
				(byte) 243, 60, (byte) 136, (byte) 254, 6, (byte) 205,
				(byte) 203, 110, 50, (byte) 136, 94, (byte) 198, (byte) 211,
				65, (byte) 237, (byte) 242, 124, 30, 62, (byte) 151,
				(byte) 231, 46 };
		byte[] arrayOfByte2 = GSM0338
				.encode("It is easy to send text messages.");
		assertArrayEquals(arrayOfByte1, arrayOfByte2);
	}

	@Test
	public void testExtensionChars() throws CharacterCodingException {
		String str = GSM0338.decode(GSM0338.encode("|^{}\\[~]€"));
		assertEquals("|^{}\\[~]€", str);
	}

	@Ignore
	@Test
	public void testFuzzBytes() throws CharacterCodingException {
		int i = 0;
		while (true) {
			if (i >= 100)
				return;
			byte[] arrayOfByte1 = new byte[this.random.nextInt(160)];
			int j = arrayOfByte1.length;
			fuzz(arrayOfByte1, 0, j);
			byte[] arrayOfByte2 = GSM0338.encode(GSM0338.decode(arrayOfByte1));
			assertArrayEquals(arrayOfByte1, arrayOfByte2);
			i += 1;
		}
	}

	@Test
	public void testFuzzStrings() throws CharacterCodingException {
		int i = 0;
		while (true) {
			if (i >= 100)
				return;
			int j = this.random.nextInt(160);
			String str1 = fuzzString(j);
			String str2 = GSM0338.decode(GSM0338.encode(str1));
			assertEquals(str1, str2);
			i += 1;
		}
	}
}