package edu.jhu.privtext.test;

import java.io.PrintStream;
import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import edu.jhu.bouncycastle.crypto.InvalidCipherTextException;
import edu.jhu.bouncycastle.util.encoders.Hex;
import edu.jhu.privtext.crypto.GZEncode;
import edu.jhu.privtext.crypto.GZEngine;

public class GZEngineTest
{
  private GZEngine myCipher;
  private final byte[] myKey;

  public GZEngineTest()
  {
    byte[] arrayOfByte = Hex.decode("15B3CA14A92A2F7F2B827A49B901ED76");
    this.myKey = arrayOfByte;
  }

  private void printHex(byte[] paramArrayOfByte)
  {
    byte[] arrayOfByte = Hex.encode(paramArrayOfByte);
    StringBuffer localStringBuffer1 = new StringBuffer();
    int i = 0;
    while (true)
    {
      int j = arrayOfByte.length;
      if (i >= j)
      {
        System.out.println(localStringBuffer1);
        return;
      }
      char c = (char)arrayOfByte[i];
      StringBuffer localStringBuffer2 = localStringBuffer1.append(c);
      i += 1;
    }
  }

  public byte[] getRandomData(int paramInt)
  {
    byte[] arrayOfByte = new byte[paramInt];
    long l = System.currentTimeMillis();
    Random localRandom = new Random(l);
    int i = 0;
    while (true)
    {
      int j = arrayOfByte.length;
      if (i >= j)
        return arrayOfByte;
      int k = (byte)localRandom.nextInt();
      arrayOfByte[i] = k;
      i += 1;
    }
  }

  @Before
  public void setupCipher()
  {
    GZEngine localGZEngine = new GZEngine();
    this.myCipher = localGZEngine;
  }

  @Test
  public void testAEXTest()
    throws IllegalStateException, InvalidCipherTextException
  {
    String[] arrayOfString = new String[3];
    arrayOfString[0] = "";
    arrayOfString[1] = "abcdefghijklmnopqrstuvqxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!@#$%^&";
    arrayOfString[2] = "01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567";
    byte[] arrayOfByte1 = getRandomData(1);
    int i = arrayOfString.length;
    int j = 0;
    while (true)
    {
      if (j >= i)
        return;
      String str1 = arrayOfString[j];
      byte[] arrayOfByte2 = GZEncode.encodeString(str1);
      GZEngine localGZEngine1 = this.myCipher;
      byte[] arrayOfByte3 = this.myKey;
      byte[] arrayOfByte4 = localGZEngine1.encrypt(arrayOfByte2, arrayOfByte3, arrayOfByte1);
      GZEngine localGZEngine2 = this.myCipher;
      byte[] arrayOfByte5 = this.myKey;
      String str2 = GZEncode.decodeString(localGZEngine2.decrypt(arrayOfByte4, arrayOfByte5, arrayOfByte1));
      Assert.assertEquals(str1, str2);
      j += 1;
    }
  }

  @Test
  public void testCipherTextPayloadLength()
    throws IllegalStateException, InvalidCipherTextException
  {
    StringBuilder localStringBuilder1 = new StringBuilder();
    byte[] arrayOfByte1 = new byte[0];
    byte[] arrayOfByte2 = new byte[1];
    int i = 0;
    if (i != 0)
    {
      PrintStream localPrintStream = System.err;
      StringBuilder localStringBuilder2 = new StringBuilder("Max user str len: ");
      int j = localStringBuilder1.length();
      String str = j;
      localPrintStream.println(str);
      if (arrayOfByte1.length > 134)
        break label162;
    }
    label162: int m;
    for (int k = 1; ; m = 0)
    {
      Assert.assertTrue(k);
      return;
      StringBuilder localStringBuilder3 = localStringBuilder1.append(97);
      byte[] arrayOfByte3 = GZEncode.encodeString(localStringBuilder1.toString());
      GZEngine localGZEngine1 = this.myCipher;
      byte[] arrayOfByte4 = this.myKey;
      arrayOfByte1 = localGZEngine1.encrypt(arrayOfByte3, arrayOfByte4, arrayOfByte2);
      GZEngine localGZEngine2 = this.myCipher;
      byte[] arrayOfByte5 = new byte[0];
      if (localGZEngine2.packageMessage(arrayOfByte2, arrayOfByte5, arrayOfByte1).length >= 134);
      for (i = 1; ; i = 0)
        break;
    }
  }

  @Test
  public void testPackaging()
  {
    byte[] arrayOfByte1 = getRandomData(1);
    byte[] arrayOfByte2 = getRandomData(0);
    byte[] arrayOfByte3 = getRandomData(123);
    byte[] arrayOfByte4 = this.myCipher.packageMessage(arrayOfByte1, arrayOfByte2, arrayOfByte3);
    byte[] arrayOfByte5 = this.myCipher.getNonce(arrayOfByte4);
    byte[] arrayOfByte6 = this.myCipher.getHeader(arrayOfByte4);
    byte[] arrayOfByte7 = this.myCipher.getCiphertext(arrayOfByte4);
    Assert.assertArrayEquals(arrayOfByte1, arrayOfByte5);
    Assert.assertArrayEquals(arrayOfByte2, arrayOfByte6);
    Assert.assertArrayEquals(arrayOfByte3, arrayOfByte7);
  }
}

/* Location:           /home/urbanus/workspace/PrivText/Android/recovery/dex2jar/com.belvin.privtext.test.apk.dex2jar.jar
 * Qualified Name:     com.belvin.privtext.test.GZEngineTest
 * JD-Core Version:    0.6.0
 */