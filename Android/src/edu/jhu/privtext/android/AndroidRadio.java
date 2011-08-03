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

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.widget.Toast;
import edu.jhu.privtext.SMSIO;
import edu.jhu.privtext.ssms.SessionManager;

/**
 * An Android specific interface for sending and receiving text messages.
 * Represents the bottom, radio layer interface for exiting and entering messages.
 * 
 * @author Gary belvin
 * @version 0.1
 * 
 */
public class AndroidRadio extends SMSIO {
  /** Label for delivered intents. */
  private static final String DELIVERED = "SMS_DELIVERED";
  /** Label for sent intents. */
  private static final String SENT = "SMS_SENT";
  /** The context for runtime android objects. */
  private final Context my_context;
  private PendingIntent deliveredPI = null;
  private PendingIntent sentPI = null;

  /** Initializes the radio.
   * @param the_context is what allows the radio to access Android runtime objects.
   */
  public AndroidRadio(final Context the_context) {
    my_context = the_context;
    setupIntents();
    
  }

  // /** Sends an SMS with an intent.
  // * Not Currently used */
  // private void sendSMSIntent() {
  // Intent sendIntent = new Intent(Intent.ACTION_VIEW);
  // sendIntent.putExtra("sms_body", "Content of the SMS goes here...");
  // sendIntent.setType("vnd.android-dir/mms-sms");
  // myContext.startActivity(sendIntent);
  // }

  private void setupIntents() {
    sentPI = PendingIntent.getBroadcast(my_context, 0, new Intent(SENT), 0);
    deliveredPI = PendingIntent.getBroadcast(my_context, 0, new Intent(DELIVERED), 0);

    my_context.registerReceiver(new SentReciever(), new IntentFilter(SENT));
    my_context.registerReceiver(new DeliveredReciever(), new IntentFilter(DELIVERED));
  }

  public void sendPDU(String paramString, byte[] paramArrayOfByte) {
    sendPDU(paramString, paramArrayOfByte, getAppPort());
  }

  public void sendPDU(String thePhoneNo, byte[] theMessage, short thePort) {
    if (theMessage.length >= SmsMessage.MAX_USER_DATA_BYTES_WITH_HEADER) // 134
    {
      Toast.makeText(my_context, "payload exceeded 134 bytes", Toast.LENGTH_SHORT).show();
      return;
    }

    // ((TelephonyManager)
    // this.myContext.getSystemService("phone")).getPhoneType();
    SmsManager.getDefault().sendDataMessage(thePhoneNo, null, thePort, theMessage, sentPI,
                                            deliveredPI);
  }

  public void sendText(String thePhoneNo, String theMessage) {
    SmsManager.getDefault().sendTextMessage(thePhoneNo, null, theMessage, sentPI, deliveredPI);
  }

  /** Processes Asymetric call back for sending */
  class SentReciever extends BroadcastReceiver {
    public void onReceive(Context paramContext, Intent paramIntent) {
      String msg = "";
      switch (getResultCode()) {
        case Activity.RESULT_OK: // -1:
          msg = "SMS sent";
          break;
        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
          msg = "Generic failure";
          break;
        case SmsManager.RESULT_ERROR_NO_SERVICE:
          msg = "No service";
          break;
        case SmsManager.RESULT_ERROR_NULL_PDU:
          msg = "Null PDU";
          break;
        case SmsManager.RESULT_ERROR_RADIO_OFF:
          msg = "Radio Off";
          break;
        default:
          msg = "Unkown Result";
          break;
      }
      Toast.makeText(my_context, msg, Toast.LENGTH_SHORT).show();
    }
  }

  /**
   * Processes Asymetric call back for delivery confirmation -- to the service
   * center, not the recipient.
   */
  class DeliveredReciever extends BroadcastReceiver {
    public void onReceive(Context paramContext, Intent paramIntent) {
      String msg = "";
      switch (getResultCode()) {
        case Activity.RESULT_OK:
          msg = "SMS delivered";
          break;
        case Activity.RESULT_CANCELED:
          msg = "SMS not delivered";
          break;
        default:
      }
      Toast.makeText(my_context, msg, Toast.LENGTH_SHORT).show();
    }
  }
}
