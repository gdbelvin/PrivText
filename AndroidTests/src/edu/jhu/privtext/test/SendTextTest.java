package edu.jhu.privtext.test;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;
import edu.jhu.privtext.android.SendMessageView;

public class SendTextTest extends ActivityInstrumentationTestCase2<SendMessageView> {
	private SendMessageView mActivity;
	private TextView mView;
	private String resourceString;

	public SendTextTest() {
		super("edu.jhu.privtext.android", SendMessageView.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		SendMessageView localSendMessage = (SendMessageView) getActivity();
		this.mActivity = localSendMessage;
	}

	public void testJavaCryptoProvider() throws NoSuchAlgorithmException,
			NoSuchPaddingException, NoSuchProviderException,
			InvalidKeyException, ShortBufferException,
			IllegalBlockSizeException, BadPaddingException {
	}

	public void testPreconditions() {
		assertNotNull(this.mView);
	}

	public void testText() {
		String str1 = this.resourceString;
		String str2 = (String) this.mView.getText();
		assertEquals(str1, str2);
	}
}