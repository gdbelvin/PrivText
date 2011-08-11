package edu.jhu.privtext.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;

import edu.jhu.bouncycastle.crypto.InvalidCipherTextException;
import edu.jhu.bouncycastle.crypto.engines.AESEngine;
import edu.jhu.bouncycastle.crypto.modes.AEADBlockCipher;
import edu.jhu.bouncycastle.crypto.modes.EAXBlockCipher;
import edu.jhu.bouncycastle.util.encoders.Hex;
import edu.jhu.privtext.crypto.CipherWrapper;
import edu.jhu.privtext.crypto.GZEncode;
import edu.jhu.privtext.util.encoders.UserDataPart;

public class CipherWrapTest {
	private final byte[] my_key = Hex
			.decode("15B3CA14A92A2F7F2B827A49B901ED76");
	private final AEADBlockCipher my_cipher = new EAXBlockCipher(
			new AESEngine());

	public byte[] getRandomData(int the_numberofbytes) {
		byte[] out = new byte[the_numberofbytes];
		Random rand = new Random(System.currentTimeMillis());
		rand.nextBytes(out);
		return out;
	}

	@Test
	public void testAEXTest() throws IllegalStateException,
			InvalidCipherTextException {
		String[] vectors = new String[3];
		vectors[0] = "";
		vectors[1] = "abcdefghijklmnopqrstuvqxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!@#$%^&";
		vectors[2] = "01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567";
		for (String s : vectors) {
			UserDataPart the_envelope = new UserDataPart(3);
			byte[] the_seq = { 0x01, 0x03 };
			the_envelope.setSequenceNumber(the_seq[0]);
			byte[] ct = CipherWrapper.encrypt(my_cipher, my_key, the_envelope,
					GZEncode.encodeString(s), the_seq);
			UserDataPart ud = new UserDataPart(the_seq[0], ct, 3);

			byte[] pt = CipherWrapper.decrypt(my_cipher, my_key, ud, the_seq);
			String str2 = GZEncode.decodeString(pt);
			assertEquals(s, str2);
		}
	}

	@Test
	public void testCipherTextPayloadLength() throws IllegalStateException,
			InvalidCipherTextException {
		StringBuilder sb = new StringBuilder();
		while (true) {
			sb.append("a");
			byte[] pt = GZEncode.encodeString(sb.toString());
			UserDataPart the_envelope = new UserDataPart(3);
			byte[] the_seq = { 0x01, 0x03 };
			the_envelope.setSequenceNumber(the_seq[0]);
			byte[] ct = CipherWrapper.encrypt(my_cipher, my_key, the_envelope,
					GZEncode.encodeString(sb.toString()), the_seq);

			if (ct.length > 134) {
				System.err.println("Max user str len: " + sb.length());
				assertTrue(true);
				return;
			}
		}
	}
}