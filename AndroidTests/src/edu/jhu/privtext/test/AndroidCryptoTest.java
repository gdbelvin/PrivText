package com.belvin.droid.test;

import android.test.ActivityInstrumentationTestCase2;
import com.belvin.privtext.SendMessage;
import java.io.PrintStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class AndroidCryptoTest extends ActivityInstrumentationTestCase2<SendMessage>
{
  public AndroidCryptoTest()
  {
    super("com.belvin.privtext.SendMessage", SendMessage.class);
  }

  public static String[] getCryptoImpls(String paramString)
  {
    HashSet localHashSet = new HashSet();
    Provider[] arrayOfProvider = Security.getProviders();
    int i = 0;
    int j = arrayOfProvider.length;
    if (i >= j)
    {
      String[] arrayOfString = new String[localHashSet.size()];
      return (String[])localHashSet.toArray(arrayOfString);
    }
    Iterator localIterator = arrayOfProvider[i].keySet().iterator();
    while (true)
    {
      if (!localIterator.hasNext())
      {
        i += 1;
        break;
      }
      String str1 = ((String)localIterator.next()).split(" ")[0];
      String str2 = String.valueOf(paramString);
      String str3 = str2 + ".";
      if (str1.startsWith(str3))
      {
        int k = paramString.length() + 1;
        String str4 = str1.substring(k);
        boolean bool1 = localHashSet.add(str4);
        continue;
      }
      String str5 = "Alg.Alias." + paramString + ".";
      if (!str1.startsWith(str5))
        continue;
      int m = paramString.length() + 11;
      String str6 = str1.substring(m);
      boolean bool2 = localHashSet.add(str6);
    }
  }

  public void testAvailableCiphers()
  {
    String[] arrayOfString = getCryptoImpls("Cipher");
    int i = arrayOfString.length;
    int j = 0;
    while (true)
    {
      if (j >= i)
        return;
      String str = arrayOfString[j];
      System.out.println(str);
      j += 1;
    }
  }

  public void testJavaCryptoProvider()
    throws NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException, InvalidKeyException, ShortBufferException, IllegalBlockSizeException, BadPaddingException
  {
    int i = Security.addProvider(new BouncyCastleProvider());
    byte[] arrayOfByte1 = "this is a test message".getBytes();
    byte[] arrayOfByte2 = new byte[24];
    arrayOfByte2[1] = 1;
    arrayOfByte2[2] = 2;
    arrayOfByte2[3] = 3;
    arrayOfByte2[4] = 4;
    arrayOfByte2[5] = 5;
    arrayOfByte2[6] = 6;
    arrayOfByte2[7] = 7;
    arrayOfByte2[8] = 8;
    arrayOfByte2[9] = 9;
    arrayOfByte2[10] = 10;
    arrayOfByte2[11] = 11;
    arrayOfByte2[12] = 12;
    arrayOfByte2[13] = 13;
    arrayOfByte2[14] = 14;
    arrayOfByte2[15] = 15;
    arrayOfByte2[16] = 16;
    arrayOfByte2[17] = 17;
    arrayOfByte2[18] = 18;
    arrayOfByte2[19] = 19;
    arrayOfByte2[20] = 20;
    arrayOfByte2[21] = 21;
    arrayOfByte2[22] = 22;
    arrayOfByte2[23] = 23;
    SecretKeySpec localSecretKeySpec = new SecretKeySpec(arrayOfByte2, "AES");
    Cipher localCipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
    localCipher.init(1, localSecretKeySpec);
    int j = arrayOfByte1.length;
    byte[] arrayOfByte3 = new byte[localCipher.getOutputSize(j)];
    int k = arrayOfByte1.length;
    int m = localCipher.update(arrayOfByte1, 0, k, arrayOfByte3, 0);
    int n = localCipher.doFinal(arrayOfByte3, m);
    int i1 = m + n;
    PrintStream localPrintStream = System.out;
    String str1 = new String(arrayOfByte3);
    localPrintStream.println(str1);
    System.out.println(i1);
    localCipher.init(2, localSecretKeySpec);
    byte[] arrayOfByte4 = new byte[localCipher.getOutputSize(i1)];
    int i2 = localCipher.update(arrayOfByte3, 0, i1, arrayOfByte4, 0);
    int i3 = localCipher.doFinal(arrayOfByte4, i2);
    int i4 = i2 + i3;
    String str2 = new String(arrayOfByte4);
    System.out.println(str2);
    System.out.println(i4);
    assertEquals("this is a test message", str2);
  }
}

/* Location:           /home/urbanus/workspace/PrivText/Android/recovery/dex2jar/com.belvin.privtext.test.apk.dex2jar.jar
 * Qualified Name:     com.belvin.droid.test.AndroidCryptoTest
 * JD-Core Version:    0.6.0
 */