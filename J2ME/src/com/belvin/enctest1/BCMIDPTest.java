package com.belvin.enctest1;


import com.belvin.bouncy.util.encoders.Hex;
import com.belvin.privtext.crypto.GZEngine;
import javax.microedition.midlet.MIDlet;
import javax.microedition.lcdui.*;

/**
 * MIDP is a simple graphics application for the J2ME CLDC/MIDP.
 *
 * It has hardcoded values for the key and plain text. It also performs the
 * standard testing for the chosen cipher, and displays the results.
 *
 * This example shows how to use the light-weight API and a symmetric cipher.
 *
 */
public class BCMIDPTest extends MIDlet
{
    private Display             d           = null;

    private boolean             doneEncrypt = false;

    private String              key         = "0123456789abcdef0123456789abcdef";
    private String              plainText   = "www.bouncycastle.org";
    private byte[]              keyBytes    = null;
    private byte[]              cipherText  = null;
    private GZEngine            cipher      = new GZEngine();

    private Form                output      = null;

    public void startApp()
    {
        Display.getDisplay(this).setCurrent(output);
    }

    public void pauseApp()
    {

    }

    public void destroyApp(boolean unconditional)
    {

    }

    public BCMIDPTest()
    {
        output = new Form("BouncyCastle");
        output.append("Key: " + key.substring(0, 7) + "...\n");
        output.append("In : " + plainText.substring(0, 7) + "...\n");

        cipherText = performEncrypt(Hex.decode(key), plainText);
        String ctS = new String(Hex.encode(cipherText));

        output.append("\nCT : " + ctS.substring(0, 7) + "...\n");

        String decryptText = performDecrypt(Hex.decode(key), cipherText);

        output.append("PT : " + decryptText.substring(0, 7) + "...\n");

        if (decryptText.compareTo(plainText) == 0)
        {
            output.append("Success");
        }
        else
        {
            output.append("Failure");
            message("[" + plainText + "]");
            message("[" + decryptText + "]");
        }

    }

    private byte[] performEncrypt(byte[] key, String plainText)
    {
        byte[] ptBytes = plainText.getBytes();
        byte[] ctBytes = new byte[0];

        message("Using GZ");
        try
        {
            ctBytes = cipher.encrypt(ptBytes, key, new byte[1]);
        }
        catch (Exception ce)
        {
            message("Ooops, encrypt exception");
            status(ce.toString());
        }
        return ctBytes;
    }

    private String performDecrypt(byte[] key, byte[] cipherText)
    {
        byte[] ptBytes = new byte[0];

        try
        {
            ptBytes = cipher.decrypt(cipherText, key, new byte[1]);
        }
        catch (Exception ce)
        {
            message("Ooops, decrypt exception");
            status(ce.toString());
        }
        return new String(ptBytes).trim();
    }

   
    public void message(String s)
    {
        System.out.println("M:" + s);
    }

    public void status(String s)
    {
        System.out.println("S:" + s);
    }

}
