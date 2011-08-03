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
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import edu.jhu.privtext.R;
import edu.jhu.privtext.ssms.SessionManager;

public class SendMessage extends Activity {
  private Button btnSend;
  private SessionManager mySessionMgr;
  private EditText txtMessage;
  private EditText txtPhoneNumber;

  public void onCreate(final Bundle paramBundle) {
    super.onCreate(paramBundle);
    setContentView(R.layout.main);

    this.txtPhoneNumber = (EditText) findViewById(R.id.txtPhoneNumber);
    this.txtMessage = (EditText) findViewById(R.id.txtMessage);
    this.btnSend = (Button) findViewById(R.id.BtnSend);

    this.mySessionMgr = SessionManager.getInstance();
    this.mySessionMgr.attachRadio(new AndroidRadio(getBaseContext()));

    btnSend.setOnClickListener(new OnSendClick());
  }

  private final class OnSendClick implements View.OnClickListener {
    private OnSendClick() {
    }

    public void onClick(final View paramView) {
      if ((txtPhoneNumber.getText().length() > 0) && (txtMessage.getText().length() > 0)) {
        SendMessage.this.mySessionMgr.sendSecureSMS(txtPhoneNumber.getText().toString(),
                                                    txtMessage.getText().toString());
        return;
      }
      Toast.makeText(SendMessage.this.getBaseContext(),
                     "Please enter both phone number and message.", 0).show();
    }
  }
}
