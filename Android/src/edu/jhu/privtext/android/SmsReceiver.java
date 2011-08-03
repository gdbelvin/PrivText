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
import edu.jhu.privtext.ssms.SessionManager;

/**
 * Android specific code for processing incoming SMS messages. Adapted from
 * http://mobiforge.com/developing/story/sms-messaging-android
 * 
 * @author Gary Belvin
 */
public class SmsReceiver extends BroadcastReceiver {
  private SessionManager my_SessionMgr;

  public SmsReceiver() {
    my_SessionMgr = SessionManager.getInstance();
  }

  public void onReceive(final Context theContext, final Intent theIntent) {
    final Bundle bundle = theIntent.getExtras();
    SmsMessage[] smsMsgs = null;
    if (bundle != null) {
      // ---retrieve the SMS message received---
      final Object[] pdus = (Object[]) bundle.get("pdus");
      smsMsgs = new SmsMessage[pdus.length];
      for (int i = 0; i < smsMsgs.length; i++) {
        smsMsgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);

        displayPDU(theContext, smsMsgs[i]);
        procesessPDU(theContext, smsMsgs[i]);
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
    final String srcNum = the_pdu.getDisplayOriginatingAddress();
    final String thisNum = tMgr.getLine1Number();

    my_SessionMgr.processIncomingSMS(srcNum, thisNum, the_pdu.getUserData());
  }

  /** Test method to display PDU contents. */
  private void displayPDU(final Context theContext, final SmsMessage thePDU) {
    final StringBuilder sb = new StringBuilder();
    sb.append("SMS from ");
    sb.append(thePDU.getOriginatingAddress());
    sb.append(" :");
    sb.append(thePDU.getMessageBody());
    sb.append("\n");
    sb.append("pdu bytes: ");
    sb.append(thePDU.getPdu().length);
    sb.append("\n");
    sb.append("data bytes: ");
    sb.append(thePDU.getUserData().length);

    thePDU.getOriginatingAddress();
    thePDU.getUserData();

    Toast.makeText(theContext, sb, Toast.LENGTH_SHORT).show();
  }
}
