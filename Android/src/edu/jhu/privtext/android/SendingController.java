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

import android.app.PendingIntent;
import android.content.Context;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import edu.jhu.bouncycastle.util.encoders.Hex;
import edu.jhu.privtext.SMSIO;
import edu.jhu.privtext.crypto.GZEncode;
import edu.jhu.privtext.ssms.RekeyException;
import edu.jhu.privtext.ssms.SessionManager;

/**
 * Android specific controller for Privtext.
 * @author Gary Belvin
 * @version 0.1
 */
public class SendingController extends SMSIO {

  /** The manager for SSMS sessions. */
  private final SessionManager my_sessionmgr;
  /** The android context. */
  private final Context my_context;

  /** Pending intent for delivery feedback. */
  private PendingIntent my_deliveredpi;
  /** Pending intent for delivery feedback. */
  private PendingIntent my_sentpi;

  /**
   * Instantiates the controller with references to the view.
   * @param the_context of the android activity
   * @param the_sentpi the PendingIntent to call when a message has been sent
   * @param the_deliveredpi The PendingIntent to call when a message has been
   *          delivered
   */
  public SendingController(final Context the_context, final PendingIntent the_sentpi,
                           final PendingIntent the_deliveredpi) {
    my_sessionmgr = SessionManager.getInstance();
    my_context = the_context;
    my_sentpi = the_sentpi;
    my_deliveredpi = the_deliveredpi;
  }

  @Override
  public void sendPDU(final String the_phonenum, final short the_port,
                      final byte[] the_userdata) {
    if (the_userdata.length >= SmsMessage.MAX_USER_DATA_BYTES_WITH_HEADER) {
      return;
    }

    // TODO: Filter for CDMA vs. GSM?
    // this.myContext.getSystemService("phone")).getPhoneType();
    SmsManager.getDefault().sendDataMessage(the_phonenum, null, the_port, the_userdata,
                                            my_sentpi, my_deliveredpi);
  }

  @Override
  public void sendSecureText(final String the_phonenum, final String the_message) {
    final TelephonyManager tMgr =
        (TelephonyManager) my_context.getSystemService(Context.TELEPHONY_SERVICE);

    final String srcnum = tMgr.getLine1Number();
    final short srcport = getAppPort();
    final String dstnum = the_phonenum;
    final short dstport = getAppPort();
    final byte[] message = GZEncode.encodeString(the_message);
    if (my_sessionmgr.needsKey(srcnum, dstnum, srcport, dstport)) {
      // TODO: Trigger KAPS Negotiation
      //Temporary keys
      final byte[] i2rkey = Hex.decode("4fc417d3152f5c824ee50bdec4a57ac7" + 
                                       "6e51f186eec43526030157bd385887b4" +
                                       "f949fe3f6c9f3b9fe9865f2cb029e5bc" + 
                                       "42f92553d8102c4969e73ba759a74914");
      final byte[] r2ikey = Hex.decode("96bf676939ff7ca372e4c15d9ea72aa8" + 
                                       "ee57daca1c38fecf93979fb4be84439b" +
                                       "059066a1d48e12395a871ea5a2fcb3d8" + 
                                       "4f7a8aaec34f27913f87e340fc87d6a1");

      my_sessionmgr.newSession(srcnum, dstnum, srcport, dstport, i2rkey, r2ikey, true);
    }

    try {
      final byte[] pdu = my_sessionmgr.processOutgoingSMS(srcnum, dstnum, srcport, dstport, message);
      byte[] test = {0x06, 0x05, 0x02, 0x07, 0x06, 0x05, 0x04, 0x03, 0x02, 0x01, 0x00};
      sendPDU(the_phonenum, dstport, test);
    } catch (final RekeyException e) {
      // TODO: Trigger KAPS Negotiation
    }
  }
}
