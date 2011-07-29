package com.belvin.droid.test;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;
import com.belvin.privtext.SendMessage;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

public class SendTextTest extends ActivityInstrumentationTestCase2<SendMessage>
{
  private SendMessage mActivity;
  private TextView mView;
  private String resourceString;

  public SendTextTest()
  {
    super("com.belvin.privtext.SendMessage", SendMessage.class);
  }

  protected void setUp()
    throws Exception
  {
    super.setUp();
    SendMessage localSendMessage = (SendMessage)getActivity();
    this.mActivity = localSendMessage;
  }

  public void testJavaCryptoProvider()
    throws NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException, InvalidKeyException, ShortBufferException, IllegalBlockSizeException, BadPaddingException
  {
  }

  public void testPreconditions()
  {
    assertNotNull(this.mView);
  }

  public void testText()
  {
    String str1 = this.resourceString;
    String str2 = (String)this.mView.getText();
    assertEquals(str1, str2);
  }
}

/* Location:           /home/urbanus/workspace/PrivText/Android/recovery/dex2jar/com.belvin.privtext.test.apk.dex2jar.jar
 * Qualified Name:     com.belvin.droid.test.SendTextTest
 * JD-Core Version:    0.6.0
 */