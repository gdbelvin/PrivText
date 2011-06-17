package com.belvin.enctest1;

import com.belvin.privtext.crypto.GZSessionManager;
import javax.microedition.io.PushRegistry;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.wireless.messaging.BinaryMessage;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.MessageListener;
import javax.wireless.messaging.TextMessage;

/**
 * Main UI used sending text messages
 * However, it also listens for incomming messagse as well.
 */
public class SendUI extends MIDlet implements CommandListener, ItemCommandListener, Listener {
    /* Constants */

    private static final String myDefaultDest = "4438577196";
    private static final String myDefaultMsg = "Test MIDP Message";
    private static final int myMaxMsgSize = 160;  //Max size of a SMS message
    private static final int myMaxPhoneNoSize = 160;  //Max size of a SMS message
    private Display display;
    /** GUI Elements */
    private Form mySendingForm;
    private TextField txtDestAddress;
    private TextBox txtMessage;
    /** Alert Elements */
    private Alert errorMessageAlert;
    private Alert sendingMessageAlert;
    private Alert receivedMessageAlert;
    /** Commands */
    Command myCmdGotoMessage = new Command("OK", Command.OK, 0);
    Command myCmdSendMessage = new Command("OK", Command.OK, 0);
    Command exitCommand = new Command("Exit", Command.EXIT, 2);
    /**
     * The last visible screen when we paused
     */
    Displayable resumeScreen = null;
    String myAddress = "";
    private J2MESMS radioIO;
    private GZSessionManager mySessionMgr;

    /**
     * Initialize the display
     */
    public SendUI() {
        //Setup sending code
        String smsPort = getAppProperty ("SMS-Port");
        int port = Integer.parseInt(smsPort);
        radioIO = new J2MESMS(port);
        mySessionMgr = new GZSessionManager(radioIO);
        //Setup listening code
        radioIO.connectMessageListener(this);
        radioIO.connectSessionMgr(mySessionMgr);

        setupGUI();
    }

    private void setupGUI() {
        display = Display.getDisplay(this);

        /* Initialize GUI */
        txtDestAddress = new TextField("To:", myDefaultDest, myMaxPhoneNoSize, TextField.PHONENUMBER);
        txtDestAddress.addCommand(myCmdGotoMessage);
        txtDestAddress.addCommand(exitCommand);
        // txtDestAddress.setDefaultCommand(myCmdGotoMessage);
        txtDestAddress.setItemCommandListener(this);

        txtMessage = new TextBox("Message:", myDefaultMsg, myMaxMsgSize, TextField.PLAIN);
        txtMessage.addCommand(myCmdSendMessage);
        txtMessage.setCommandListener(this);

        errorMessageAlert = new Alert("ERROR", null, null, AlertType.ERROR);
        errorMessageAlert.setTimeout(5000);
        errorMessageAlert.setCommandListener(this);

        sendingMessageAlert = new Alert("SMS Sent", null, null, AlertType.INFO);
        sendingMessageAlert.setTimeout(5000);
        sendingMessageAlert.setCommandListener(this);

        receivedMessageAlert = new Alert("SMS Received", null, null, AlertType.INFO);
        receivedMessageAlert.setTimeout(5000);
        receivedMessageAlert.setCommandListener(this);

        mySendingForm = new Form("Send a text message");
        mySendingForm.append(txtDestAddress);

        //sender = new SMSSender (smsPort, display, destinationAddressBox, sendingMessageAlert);

        resumeScreen = mySendingForm;
    }

    protected void startApp() throws MIDletStateChangeException {
        this.display.setCurrent(resumeScreen);
        radioIO.listenForPDUs();

        /** Initialize the text if we were started manually. */
        String[] connections = PushRegistry.listConnections(true);
        if ((connections == null) || (connections.length == 0)) {
            
        }
    }

    /**
     * Remember what screen is showing
     */
    public void pauseApp() {
        resumeScreen = display.getCurrent();
    }

    /**
     * Destroy must cleanup everything.
     */
    public void destroyApp(boolean unconditional) {
        radioIO.stopListening();
    }

    /**
     * Respond to commands
     *
     * @param c user interface command requested
     * @param s screen object initiating the request
     */
    public void commandAction(Command c, Displayable s) {
        try {
            if ((c == exitCommand) || (c == Alert.DISMISS_COMMAND)) {
                destroyApp(false);
                notifyDestroyed();
            } else if (c == myCmdGotoMessage) {
                promptForMessage();
            } else if (c == myCmdSendMessage) {
                sendMessage();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void commandAction(Command c, Item item) {
        commandAction(c, resumeScreen);
    }

    private void sendMessage() {
        mySessionMgr.sendSecureSMS(myAddress, txtMessage.getString());
        display.setCurrent(sendingMessageAlert);
        //sender.promptAndSend("sms://" + address);
    }

    /**
     * Checks the validity of the given phone number and advances the screen
     * Prompt for and send the message
     */
    private void promptForMessage() {
        String address = txtDestAddress.getString();
        display.setCurrent(txtMessage);

        if (!isValidPhoneNumber(address)) {
            errorMessageAlert.setString("Invalid phone number");
            display.setCurrent(errorMessageAlert, mySendingForm);
            return;
        }
        myAddress = address;
        String statusMessage = "Sending message to " + address + "...";
        sendingMessageAlert.setString(statusMessage);
        display.setCurrent(txtMessage);
    }

    /**
     * Check the phone number for validity
     * Valid phone numbers contain only the digits 0 thru 9, and may contain
     * a leading '+'.
     */
    private static boolean isValidPhoneNumber(String number) {
        char[] chars = number.toCharArray();

        if (chars.length == 0) {
            return false;
        }

        int startPos = 0;

        // initial '+' is OK
        if (chars[0] == '+') {
            startPos = 1;
        }

        for (int i = startPos; i < chars.length; ++i) {
            if (!Character.isDigit(chars[i])) {
                return false;
            }
        }

        return true;
    }

    public void processSMS(TextMessage theMessage) {
        if(theMessage == null){
           receivedMessageAlert.setString("Message Recieved");
            display.setCurrent(receivedMessageAlert, display.getCurrent());
        }
        else if (theMessage instanceof TextMessage) {
            TextMessage tmsg = (TextMessage) theMessage;
            String content = tmsg.getPayloadText();
            receivedMessageAlert.setString(content);
            display.setCurrent(receivedMessageAlert, display.getCurrent());
        } else if (theMessage instanceof BinaryMessage) {
            BinaryMessage bmsg = (BinaryMessage) theMessage;
            receivedMessageAlert.setString("Binary Message:\n" + bmsg.getPayloadData().toString());
            display.setCurrent(receivedMessageAlert, display.getCurrent());
        } else {
            receivedMessageAlert.setString("Unknown message format");
            display.setCurrent(receivedMessageAlert, display.getCurrent());
        }
    }
}