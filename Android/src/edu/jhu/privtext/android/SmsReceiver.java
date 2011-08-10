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

package edu.jhu.privtext.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.widget.Toast;
import edu.jhu.bouncycastle.crypto.InvalidCipherTextException;
import edu.jhu.bouncycastle.util.encoders.Hex;
import edu.jhu.privtext.crypto.GZEncode;
import edu.jhu.privtext.ssms.ReplayAttackException;
import edu.jhu.privtext.ssms.SessionManager;
import edu.jhu.privtext.util.encoders.UserDataHeader;

/**
 * Android specific code for processing incoming SMS messages. Adapted from
 * http://mobiforge.com/developing/story/sms-messaging-android
 * 
 * @author Gary Belvin
 * @version 0.1
 */
public class SmsReceiver extends BroadcastReceiver {
  /** The object that stores all the state related to active SSMS sessions. */
  private final SessionManager my_sessionmgr = SessionManager.getInstance();;

  /**
   * Process a new SMS notification.
   * @param the_context for the current application
   * @param the_intent of the notification
   */
  public void onReceive(final Context the_context, final Intent the_intent) {
    final Bundle bundle = the_intent.getExtras();
    SmsMessage[] smsmsgs = null;
    if (bundle != null) {
      // ---retrieve the SMS message received---
      final Object[] pdus = (Object[]) bundle.get("pdus");
      smsmsgs = new SmsMessage[pdus.length];
      for (int i = 0; i < smsmsgs.length; i++) {
        smsmsgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);

        displayPDU(the_context, smsmsgs[i]);
        procesessPDU(the_context, smsmsgs[i]);
      }
    }
  }

  /**
   * Sends the message on its way through the privtext subsystem.
   * @param the_context used for extracting the phone number of this device
   * @param the_pdu the message to be processed.
   */
  private void procesessPDU(final Context the_context, final SmsMessage the_pdu) {
    final TelephonyManager tMgr =
        (TelephonyManager) the_context.getSystemService(Context.TELEPHONY_SERVICE);
    final String srcNum = the_pdu.getOriginatingAddress();
    final String thisNum = tMgr.getLine1Number();
    final UserDataHeader udh = new UserDataHeader(the_pdu.getUserData());
    final short srcport = udh.getSrcPort();
    final short dstport = udh.getDstPort();

    if (my_sessionmgr.needsKey(srcNum, thisNum, srcport, dstport)) {
      //Ignore. This message is not part of a valid session. 
      //For testing purposes, we create a session for the message.
      
      //Temporary keys
      final byte[] i2rkey = Hex.decode("4fc417d3152f5c824ee50bdec4a57ac7" + 
                                       "6e51f186eec43526030157bd385887b4" +
                                       "f949fe3f6c9f3b9fe9865f2cb029e5bc" + 
                                       "42f92553d8102c4969e73ba759a74914");
      final byte[] r2ikey = Hex.decode("96bf676939ff7ca372e4c15d9ea72aa8" + 
                                       "ee57daca1c38fecf93979fb4be84439b" +
                                       "059066a1d48e12395a871ea5a2fcb3d8" + 
                                       "4f7a8aaec34f27913f87e340fc87d6a1");

      my_sessionmgr.newSession(srcNum, thisNum, srcport, dstport, i2rkey, r2ikey, false);
    } //else{ 
    try {
      final byte[] plaintext =
          my_sessionmgr.processIncomingSMS(srcNum, thisNum, the_pdu.getUserData());
      final String message = GZEncode.decodeString(plaintext);
      // TODO: show message to user
      Toast.makeText(the_context, message, Toast.LENGTH_SHORT).show();
    } catch (final InvalidCipherTextException e) {
      // TODO Warn user
      e.printStackTrace();
    } catch (final ReplayAttackException e) {
      // TODO Warn user
      e.printStackTrace();
    }
  //}
  }

  /** Test method to display PDU contents. */
  private void displayPDU(final Context the_context, final SmsMessage the_pdu) {
    final StringBuilder sb = new StringBuilder();
    sb.append("SMS from ");
    sb.append(the_pdu.getOriginatingAddress());
    sb.append(" :");
    sb.append(the_pdu.getMessageBody());
    sb.append("\n");
    sb.append("pdu bytes: ");
    sb.append(the_pdu.getPdu().length);
    sb.append("\n");
    sb.append("data bytes: ");
    sb.append(the_pdu.getUserData().length);

    the_pdu.getOriginatingAddress();
    the_pdu.getUserData();

    Toast.makeText(the_context, sb, Toast.LENGTH_SHORT).show();
  }
}
