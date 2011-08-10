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
import java.util.Map;

import edu.jhu.bouncycastle.crypto.Digest;
import edu.jhu.bouncycastle.crypto.InvalidCipherTextException;
import edu.jhu.bouncycastle.crypto.digests.Skein;
import edu.jhu.bouncycastle.util.encoders.Hex;
import edu.jhu.privtext.crypto.CipherWrapper;
import edu.jhu.privtext.util.encoders.SSMS_PTPayload;
import edu.jhu.privtext.util.encoders.UserDataHeader;
import edu.jhu.privtext.util.encoders.UserDataPart;

/**
 * This singleton class that manages the session keys for all SSMS secure
 * conversations.
 * 
 * @author Gary Belvin
 * @version 0.1
 */
public final class SessionManager {
  /** The singleton instance of myself. */
  private static SessionManager myself;

  /** The hash function used for the session identifier. */
  private Digest my_sesidhashfunc;

  /** The store for all active SSMS sessions, both incoming and outgoing. */
  private Map<String, Session> my_sessions = new HashMap<String, Session>();

  /** Instantiates the session manager. */
  private SessionManager() {
    /** according to table 3.1, Skein-512 is to be used in version 0.1. */
    my_sesidhashfunc = new Skein(512, 512);
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
   * H(src number||0x3A||src port|| 0x00 ||dest number||0x3A||dest port).
   * 
   * @param the_srcnum Source Phone Number
   * @param the_srcport Source application port
   * @param the_dstnum Destination Phone Number
   * @param the_dstport Destination application port
   * @return the session id
   */
  private String computeSessionID(final String the_srcnum, final short the_srcport,
                                  final String the_dstnum, final short the_dstport) {
    try {
      final byte variableLengthSeparator = 0x00;

      final byte[] src = the_srcnum.getBytes("UTF-8");
      final byte[] dst = the_dstnum.getBytes("UTF-8");

      final int capacity = src.length + 1 + 2 + 1 + dst.length + 1 + 2;
      final ByteBuffer bb = ByteBuffer.allocate(capacity);
      bb.put(src);
      bb.put((byte) 0x3A);
      bb.putShort(the_srcport);
      bb.put(variableLengthSeparator);
      bb.put(dst);
      bb.put((byte) 0x3A);
      bb.putShort(the_dstport);

      final byte[] id = new byte[my_sesidhashfunc.getDigestSize()];
      my_sesidhashfunc.reset();
      my_sesidhashfunc.update(bb.array(), 0, capacity);
      my_sesidhashfunc.doFinal(id, 0);

      return Hex.printHex(id);

    } catch (final UnsupportedEncodingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }
  }
  
  /**
   * Reports whether the specified session is in need of a new key. Covers new
   * sessions, and sessions that have old keys.
   * @param the_srcnum This phone
   * @param the_dstnum That phone
   * @param the_srcport application port number
   * @param the_dstport application port number
   * @return whether to trigger KAPS agreement
   */
  public boolean needsKey(final String the_srcnum, final String the_dstnum,
                          final short the_srcport, final short the_dstport) {

    final String sessionid =
        computeSessionID(the_srcnum, the_srcport, the_dstnum, the_dstport);
    if (!(my_sessions.containsKey(sessionid))) {
      return true;
    } else if (my_sessions.get(sessionid) instanceof SendingSession) {
      final SendingSession ses = (SendingSession) my_sessions.get(sessionid);
      return ses.needsReKey();
    } else {
      return false;
    }
  }

  /**
   * Establishes or updates a session pair. 
   * @param the_initnum The phone number of the initiator.
   * @param the_respnum The phone number of the responder
   * @param the_initport application port number
   * @param the_respport application port number.
   * @param the_i2rkey Initiator to responder key
   * @param the_r2ikey Responder to initiator key
   * @param is_initiator Are we the initiator or the responder?
   */
  public void newSession(final String the_initnum, final String the_respnum,
                         final short the_initport, final short the_respport,
                         final byte[] the_i2rkey, final byte[] the_r2ikey,
                         final boolean is_initiator) {
    final String i2rid = 
      computeSessionID(the_initnum, the_initport, the_respnum, the_respport);
    final String r2iid =
      computeSessionID(the_respnum, the_respport, the_initnum, the_initport);
    final SendingSession sendses;
    final RecievingSession recses;
    if (is_initiator) {
      sendses = new SendingSession(i2rid, the_i2rkey);
      recses = new RecievingSession(r2iid, the_r2ikey);
      my_sessions.put(i2rid, sendses);
      my_sessions.put(r2iid, recses);
    } else {
      recses = new RecievingSession(i2rid, the_i2rkey);
      sendses = new SendingSession(r2iid, the_r2ikey);
      my_sessions.put(i2rid, recses);
      my_sessions.put(r2iid, sendses);
    }
  }

  /**
   * Examines message to see if it is part of an active session. If so, the
   * plaintext of the message is returned. Section 2.8.3
   * 
   * @param the_userdata The User Data portion of the SMS
   * @param the_srcnum The sender's phone number
   * @param the_dstnum This device's phone number
   * @return the plaintext if a valid message was received. null otherwise.
   * @throws InvalidCipherTextException when an invalid MAC is received.
   * @throws ReplayAttackException when a late or duplicate message is received.
   */
  public byte[] processIncomingSMS(final String the_srcnum, final String the_dstnum,
                                   final byte[] the_userdata)
      throws InvalidCipherTextException, ReplayAttackException {
    // 1 Determine the session identifier based on source address and port
    // number
    final UserDataHeader udh = new UserDataHeader(the_userdata);
    final String sessionID =
        computeSessionID(the_srcnum, udh.getSrcPort(), the_dstnum, udh.getDstPort());

    /*
     * When receiving an incoming message, an end point uses the session
     * identifier as an index into a table of active sessions. If no active
     * session is found for an incoming message, the message is dropped without
     * further inspection
     */
    if (my_sessions.containsKey(sessionID)) {
      // 2 Determine the index of the message
      final RecievingSession ses = (RecievingSession) my_sessions.get(sessionID);
      final UserDataPart ud = new UserDataPart(the_userdata, ses.getMacSize());
      final long indx = ses.estimateMessageIndex(ud.getSequenceNumber());

      // Check if the message has been replayed.
      // If the message has been replayed, discard, and log the event.
      if (ses.hasKey(indx)) {
        // Verify MAC
        final byte[] indxbytes = ses.getIndexBytes(indx);
        if (CipherWrapper.verifyMac(ses.getAECipher(), ses.getKey(indx), ud, indxbytes)) {
          // 6. Decrypt the encrypted portion of the message.
          final byte[] plaintext =
              SSMS_PTPayload.parse(CipherWrapper.decrypt(ses.getAECipher(), ses.getKey(indx),
                                                         ud, indxbytes));

          // 5. Advance session key as needed
          // 6. Advance replay window by erasing keys.
          // 7. Update the rollover counter and highest sequence number.
          ses.confirmMessage(indx);
          return plaintext;

        } else {
          // Warn user if invalid mac is found (Section 2.6)
          throw new InvalidCipherTextException("Invalid MAC");
        }
      } else {
        // Message played out of sequence. Log event
        throw new ReplayAttackException();
      }
    } else {
      return null; // Not a SSMS message for an active session.
    }
  }

  /**
   * Sends an outgoing SSMS message for an active session. Section 2.8.2
   * 
   * @param the_message the encoded message to send
   * @param the_srcnum This device's phone number
   * @param the_dstnum This destination device's phone number
   * @param the_srcport of this protocol
   * @param the_dstport of this protocol
   * @return the plaintext if a valid message was received. null otherwise.
   * @throws RekeyException when a rekey is needed.
   */
  public byte[] processOutgoingSMS(final String the_srcnum, final String the_dstnum,
                                   final short the_srcport, final short the_dstport,
                                   final byte[] the_message) throws RekeyException {
    // 1. Determine the session identifier
    final String sessionID =
        computeSessionID(the_srcnum, the_srcport, the_dstnum, the_dstport);

    if (my_sessions.containsKey(sessionID)) {
      // 2 Determine the index of the message
      final SendingSession ses = (SendingSession) my_sessions.get(sessionID);

      // 2. Determine the index of the message based on rollover counter
      final long indx = ses.getMessageIndex();

      // 3. Encrypt and Authenticate the message.
      final UserDataPart ud = new UserDataPart(ses.getMacSize());
      ud.setSequenceNumber(ses.getSequenceNumber());
      ud.setUserDataHeader(new UserDataHeader(the_srcport, the_dstport));
      final byte[] plaintext =
          SSMS_PTPayload.wrapPlainText(ud.getMaxPayloadSize(), the_message);

      final byte[] aeciphertext =
          CipherWrapper.encrypt(ses.getAECipher(), ses.getKey(), ud, plaintext,
                                ses.getIndexBytes(indx));
      ud.setEncryptedPayload(aeciphertext);

      // 4. Advance session key by executing the KDF function
      // 5. Update the rollover counter if necessary.
      ses.advanceIndex();
      return ud.getUserData();
    } else {
      // No session has been established for these endpoints.
      // Trigger a KAPS negotiation to set it up.
      throw new RekeyException();
    }
  }
}
