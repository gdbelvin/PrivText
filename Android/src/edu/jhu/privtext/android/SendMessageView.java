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
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import edu.jhu.privtext.R;
import edu.jhu.privtext.crypto.GZEncode;
import edu.jhu.privtext.ssms.SessionManager;

public class SendMessageView extends Activity {
  /** Label for delivered intents. */
  private static final String DELIVERED = "SMS_DELIVERED";
  /** Label for sent intents. */
  private static final String SENT = "SMS_SENT";
  /** Pending intent for delivery feedback. */
  private PendingIntent my_deliveredpi;
  /** Pending intent for delivery feedback. */
  private PendingIntent my_sentpi;
  
  private Button btnSend;
  private EditText txtMessage;
  private EditText txtPhoneNumber;

  /** The interface to the privtext engine. */
  private SendingController my_privtext;

  public void onCreate(final Bundle paramBundle) {
    super.onCreate(paramBundle);
    setupIntents();
    my_privtext = new SendingController(this, my_sentpi, my_deliveredpi);
    setupUI();
  }
  
  private void setupUI(){
    setContentView(R.layout.main);

    this.txtPhoneNumber = (EditText) findViewById(R.id.txtPhoneNumber);
    this.txtMessage = (EditText) findViewById(R.id.txtMessage);
    this.btnSend = (Button) findViewById(R.id.BtnSend);

    btnSend.setOnClickListener(new OnSendClick()); 
  }
  
  private void setupIntents() {
    my_sentpi = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
    my_deliveredpi = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);

    this.registerReceiver(new SentReciever(), new IntentFilter(SENT));
    this.registerReceiver(new DeliveredReciever(), new IntentFilter(DELIVERED));
  }

  private final class OnSendClick implements View.OnClickListener {
    /** Empty constructor. */
    private OnSendClick() {
    }

    public void onClick(final View paramView) {
      if ((txtPhoneNumber.getText().length() > 0) && (txtMessage.getText().length() > 0)) {
        // Get this phone number
        my_privtext.sendSecureText(txtPhoneNumber.getText().toString(), txtMessage.getText()
            .toString());
      } else {
      Toast.makeText(SendMessageView.this.getBaseContext(),
                     "Please enter both phone number and message.", 0).show();
      }
    }
  }

  /** Processes Asymetric call back for sending. */
  class SentReciever extends BroadcastReceiver {
    /**
     * Give user feedback.
     */
    public void onReceive(Context the_context, Intent the_intent) {
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
      Toast.makeText(SendMessageView.this, msg, Toast.LENGTH_SHORT).show();
    }
  }

  /**
   * Processes Asymetric call back for delivery confirmation -- to the service
   * center, not the recipient.
   */
  class DeliveredReciever extends BroadcastReceiver {
    /** Give user feedback. */
    public void onReceive(Context the_context, Intent the_intent) {
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
      Toast.makeText(SendMessageView.this, msg, Toast.LENGTH_SHORT).show();
    }
  }
}
