package edu.jhu.privtext.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import edu.jhu.bouncycastle.util.encoders.Hex;

public class JavaTests {

	@Test
	public void testNullPadding() {
		byte[] plaintext = { 1, 2, 3 };
		int outputlen = plaintext.length + 1;
		int paddinglen = outputlen ;
		byte[] padding = new byte[paddinglen];

		ByteBuffer ud = ByteBuffer.allocate(outputlen);
		ud.put(padding);

		System.out.println("pad: " + padding.toString());
		System.out.println("ud: " + ud.array().toString());
		assertTrue(ud.array().length == 0);
	}
	
	@Test
	public void testModulus() {
		int a = -1;
		assertEquals(Integer.MAX_VALUE -1, a % Integer.MAX_VALUE);
	}
	
	@Test
	public void testCollectionwithBytes() {
		byte[] a = {0,1,2};
		byte[] b = {0,1,2};
		Map<byte[], String> m = new HashMap<byte[], String>();
		m.put(a, "A");
		assertTrue(m.containsKey(a));
		assertTrue(m.containsKey(b));
	}
	
	@Test
	public void testCollectionwithString() {
		byte[] a = {0,1,2};
		String A = Hex.toHexString(a);
		byte[] b = {0,1,2};
		String B = Hex.toHexString(b);
		Map<String, String> m = new HashMap<String, String>();
		m.put(A, "A");
		assertTrue(m.containsKey(A));
		assertTrue(m.containsKey(B));
	}
}
