package edu.jhu.privtext.test;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;

import org.junit.Test;

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
}
