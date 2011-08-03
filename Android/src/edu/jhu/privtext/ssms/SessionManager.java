/*
 * Copyright (c) 2011 Gary Belvin
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package edu.jhu.privtext.ssms;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.HashMap;

import android.util.Log;
import edu.jhu.bouncycastle.crypto.Digest;
import edu.jhu.bouncycastle.crypto.digests.Skein;
import edu.jhu.bouncycastle.util.encoders.Hex;
import edu.jhu.privtext.crypto.GZEncode;
import edu.jhu.privtext.crypto.GZEngine;
import edu.jhu.privtext.util.encoders.SSMS_UserDataHeader;
import edu.jhu.privtext.util.encoders.UserDataPart;

/**
 * This singleton class manages the session keys for all SSMS secure
 * conversations.
 * 
 * @author Gary Belvin
 */
public final class SessionManager {
  private static final String TAG = SessionManager.class.getCanonicalName();
  private static SessionManager myself;
  private GZEngine myCipher;

  /** The hash function used for the session identifier */
  public Digest sessionIDHash;

  private HashMap<byte[], Session> my_Sessions = new HashMap<byte[], Session>();

  /**
   * @param theRadio The SMS input and output functions. This is needed in case
   *          messages span more than one message.
   * @param theDeviceNo the Phone number for this device
   */
  private SessionManager() {
    myCipher = new GZEngine();

    /** according to table 3.1, Skein-512 is to be used in version 0.1. */
    sessionIDHash = new Skein(512, 512);
  }

  /** @return the single instance of the session manager. */
  public static synchronized SessionManager getInstance() {
    if (myself == null) {
      myself = new SessionManager();
    }
    return myself;
  }

  /**
   * Obfuscates the identities of communicating parties. Session Identifier =
   * H(src number||0x3A||src port|| 0x00 ||dest number||0x3A||dest port)
   */
  private byte[] computeSessionID(final String srcNum, final short srcPort,
                                  final String dstNum, final short dstPort) {
    try {
      final byte variableLengthSeparator = 0x00;

      final byte[] src = srcNum.getBytes("UTF-8");
      final byte[] dst = dstNum.getBytes("UTF-8");

      final int capacity = src.length + 1 + 2 + 1 + dst.length + 1 + 2;
      final ByteBuffer bb = ByteBuffer.allocate(capacity);
      bb.put(src);
      bb.put((byte) 0x3A);
      bb.put((byte) (srcPort >> 8 & 0xff)); // Big endian
      bb.put((byte) (srcPort & 0xff));
      bb.put(variableLengthSeparator);
      bb.put(dst);
      bb.put((byte) 0x3A);
      bb.put((byte) (dstPort >> 8 & 0xff)); // Big endian
      bb.put((byte) (dstPort & 0xff));

      final byte[] id = new byte[sessionIDHash.getDigestSize()];
      sessionIDHash.reset();
      sessionIDHash.update(bb.array(), 0, capacity);
      sessionIDHash.doFinal(id, 0);

      return id;

    } catch (final UnsupportedEncodingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }
  }

  public void sendSecureSMS(final String phoneNo, final String message) {
    // Look up session
    final Session myses;

    // TODO Verification
    // if( phoneNo is not valid phone number ) throw new invalid phone

    final byte[] bmsg = GZEncode.encodeString(message);
    byte[] ciphertext = new byte[0];
    final byte[] payload;
    final byte[] mac;
    try {
      ciphertext = this.myCipher.encrypt(bmsg, key, nonce, myMacSize);
      Log.v("Privtext-GZSession", "CT    " + Hex.printHex(ciphertext));
      Log.v("Privtext-GZSession", "Key   " + Hex.printHex(key));
      Log.v("Privtext-GZSession", "Nonce " + Hex.printHex(nonce));

      final SSMS_UserData userdata =
          new SSMS_UserData(myses.macSize, myses.SequenceNumber, ciphertext, mac);
      // Message size = [ 1 byte nonce][x byte message][ 1 byte mac]

      if (payload.length >= 134) {
        return;
      }
      this.myRadio.sendPDU(phoneNo, userdata.getUserData());
      return;
    } catch (final Exception localException) {
      Log.v("Privtext-GZSession", "Cipher not initialized");
      localException.printStackTrace();
    }
  }

  /**
   * Examines message to see if it is part of an active session. If so, the
   * plaintext of the message is returned. Section 2.8.3
   * 
   * @param the_userdata The User Data portion of the SMS
   * @param the_srcnum The sender's phone number
   * @param the_dstnum This device's phone number
   */
  public void processIncomingSMS(final String the_srcnum, final String the_dstnum,
                                 final byte[] the_userdata) {
    // 1 Determine the session identifier based on source address and port
    // number
    final SSMS_UserDataHeader udh = new SSMS_UserDataHeader(the_userdata);
    final byte[] sessionID =
        computeSessionID(the_srcnum, udh.getSrcPort(), the_dstnum, udh.getDstPort());

    /*
     * When receiving an incoming message, an end point uses the session
     * identifier as an index into a table of active sessions. If no active
     * session is found for an incoming message, the message is dropped without
     * further inspection
     */
    if (my_Sessions.containsKey(sessionID)) {
      // 2 Determine the index of the message
      final Session ses = my_Sessions.get(sessionID);
      final UserDataPart ud = new UserDataPart(the_userdata, ses.getMacSize());
      final long indx = ses.getMessageIndex(ud.getSequenceNumber());

      // Check if the message has been replayed.
      // If the message has been replayed, discard, and log the event.
      if (ses.hasKey(indx)) {
        byte[] mac = ud.getMac();
        // Verify MAC
        // Warn user if invalid mac is found (Section 2.6)

        // 5. Advance session key as needed, storing intermediate values of
        // skipped messages for a short
        // period of time.
        // 6. Decrypt the encrypted portion of the message.
        // 7. Update the rollover counter and highest sequence number. Update
        // replay window by erasing
        // keys.        
      }
      else{
        //Message played out of sequence. Log event
      }
    }
  }

  public String recieveSecureText(final byte[] theUserData) {

      plaintext = myCipher.decrypt(ciphertext, key, nonce);
      message = GZEncode.decodeString(plaintext);

    return message;
  }

}
