/*
 *
 * Copyright (c) 2008, Sun Microsystems, Inc.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *  * Neither the name of Sun Microsystems nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.belvin.enctest1;

import com.belvin.privtext.crypto.GZSessionManager;
import com.belvin.privtext.crypto.SMSIO;
import javax.microedition.io.PushRegistry;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;
import javax.wireless.messaging.*;
import java.io.IOException;

/**
 * An example MIDlet displays text from an SMS MessageConnection
 */
public class SMSReceive0 extends MIDlet implements CommandListener, Listener, MessageListener {

    Command exitCommand = new Command("Exit", Command.EXIT, 2);
    Command replyCommand = new Command("Reply", Command.OK, 1);
    Alert content;
    Display display;
    Alert sendingMessageAlert;
    Message mylastmsg;
    private J2MESMS radioIO;
    private GZSessionManager mySessionMgr;
    /**
     * The screen to display when we return from being paused
     */
    Displayable resumeScreen;

    /**
     * Initialize the MIDlet with the current display object and
     * graphical components.
     */
    public SMSReceive0() {
        //smsPort = getAppProperty ("SMS-Port");

        display = Display.getDisplay(this);

        content = new Alert("SMS Receive");
        content.setTimeout(Alert.FOREVER);
        content.addCommand(exitCommand);
        content.setCommandListener(this);
        content.setString("Receiving...");

        sendingMessageAlert = new Alert("SMS", null, null, AlertType.INFO);
        sendingMessageAlert.setTimeout(5000);
        sendingMessageAlert.setCommandListener(this);

        //sender = new SMSSender (smsPort, display, content, sendingMessageAlert);

        //Setup sending code
        radioIO = new J2MESMS((short) 16001);
        mySessionMgr = new GZSessionManager(radioIO);
        //Setup listening code
        radioIO.connectMessageListener(this);
        radioIO.connectSessionMgr(mySessionMgr);

        resumeScreen = content;
    }

    /**
     * Start creates the thread to do the MessageConnection receive
     * text.
     * It should return immediately to keep the dispatcher
     * from hanging.
     */
    public void startApp() {
        /** SMS connection to be read. */
        radioIO.listenForPDUs();

        /** Initialize the text if we were started manually. */
        String[] connections = PushRegistry.listConnections(true);

        if ((connections == null) || (connections.length == 0)) {
            content.setString("Waiting for SMS on port " + "...");
        }

        display.setCurrent(resumeScreen);
    }

    /**
     * Pause signals the thread to stop by clearing the thread field.
     * If stopped before done with the iterations it will
     * be restarted from scratch later.
     */
    public void pauseApp() {
        radioIO.stopListening();
        resumeScreen = display.getCurrent();
    }

    /**
     * Destroy must cleanup everything.  The thread is signaled
     * to stop and no result is produced.
     *
     * @param unconditional true if a forced shutdown was requested
     */
    public void destroyApp(boolean unconditional) {
        radioIO.stopListening();
    }

    /**
     * Respond to commands, including exit
     *
     * @param c user interface command requested
     * @param s screen object initiating the request
     */
    public void commandAction(Command c, Displayable s) {
        try {
            if ((c == exitCommand) || (c == Alert.DISMISS_COMMAND)) {
                destroyApp(false);
                notifyDestroyed();
            } else if (c == replyCommand) {
                reply();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Allow the user to reply to the received message
     */
    private void reply() {
        // remove the leading "sms://" for displaying the destination address
        String address = mylastmsg.getAddress().substring(6);
        String statusMessage = "Sending message to " + address + "...";
        sendingMessageAlert.setString(statusMessage);
        SendUI sender = new SendUI();
        //sender.promptAndSend(senderAddress);
    }

    public void processSMS(TextMessage msg) {
        if (msg != null) {
            mylastmsg = msg;
            String senderAddress = msg.getAddress();
            content.setTitle("From: " + senderAddress);

            if (msg instanceof TextMessage) {
                content.setString(((TextMessage) msg).getPayloadText());
            } else {
                StringBuffer buf = new StringBuffer();
                byte[] data = ((BinaryMessage) msg).getPayloadData();

                for (int i = 0; i < data.length; i++) {
                    int intData = (int) data[i] & 0xFF;

                    if (intData < 0x10) {
                        buf.append("0");
                    }

                    buf.append(Integer.toHexString(intData));
                    buf.append(' ');
                }

                content.setString(buf.toString());
            }
            content.addCommand(replyCommand);
            display.setCurrent(content);
        }
    }

    public void notifyIncomingMessage(MessageConnection arg0) {
        radioIO.notifyIncomingMessage(arg0);
    }
}
