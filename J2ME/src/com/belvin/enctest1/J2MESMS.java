package com.belvin.enctest1;

import com.belvin.privtext.crypto.GZSessionManager;
import com.belvin.privtext.crypto.SMSIO;
import java.io.IOException;
import javax.microedition.io.Connector;
import javax.wireless.messaging.BinaryMessage;
import javax.wireless.messaging.Message;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.MessageListener;
import javax.wireless.messaging.TextMessage;

public class J2MESMS implements SMSIO, MessageListener {

    //check if this is the correct value on basic phones
    private static short MAX_PDU_PAYLOAD_WITH_HEADER = 134;
    private static short MAX_GSM7_PAYLOAD = 160;
    private int myPort;
    //The session manager is needed for the listening callback functions.
    private GZSessionManager mySessionMgr;
    private Listener myListener;
    /**
     * PDU listener is only needed when using a loop to listen to incomming messages.
     */
    ListenThread pduListener;
    MessageConnection mc;

    public J2MESMS(int thePort) {
        myPort = thePort;
    }

    public void connectSessionMgr(GZSessionManager theSessionMgr) {
        mySessionMgr = theSessionMgr;
    }

    public void connectMessageListener(Listener theListener) {
        myListener = theListener;
    }

    public int getAppPort(){
        return myPort;
    }

    public void sendText(String thePhoneNo, String theMessage) {
        if (theMessage.length() >= MAX_GSM7_PAYLOAD) {
            System.out.println("payload exceeded " + MAX_GSM7_PAYLOAD + "characters");
            return;
        }
        String myAddress = "sms://" + thePhoneNo;
        SendTextThread th = new SendTextThread(myAddress, theMessage);
        new Thread(th).start();
    }

    public void sendPDU(String thePhoneNo, byte[] theUserData) {
        sendPDU(thePhoneNo, theUserData, myPort);
    }

    public void sendPDU(String thePhoneNo, byte[] theMessage, int port) {
        if (theMessage.length >= MAX_PDU_PAYLOAD_WITH_HEADER) {
            System.out.println("payload exceeded " + MAX_PDU_PAYLOAD_WITH_HEADER + "bytes");
            return;
        }

        String myAddress = "sms://" + thePhoneNo + ":" + myPort;
        SendPDUThread th = new SendPDUThread(myAddress, theMessage);
        new Thread(th).start();
    }

    public void listenForPDUs() {
        listenForPDUs(myPort);
    }

    public void listenForPDUs(int thePort) {
        System.out.println("listening for pdus on port:" + thePort);
        //What if we're already listening?
        stopListeningThread();    

        try {
            //Throws exception if the port is already registered?
            //J2ME does not allow for a a generic "sms://" listeneing port
            String address = "sms://:" + thePort;

            if (mc == null) {
                //Throws a security exception when the recieve permission is missing
                mc = (MessageConnection) Connector.open(address);

               try {
                    //Throws exception if this method is not supported.
                    mc.setMessageListener(this);
                    System.out.println("set message listener");
               } catch (IOException ex) {
                    //Use the loop method
                    pduListener = new ListenThread(mc, false);
                    System.out.println("Using loop");
                    Thread th = new Thread(pduListener);
                    th.start();
               }
                    
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void listenforTexts() {
        listenForPDUs((short) 0);
    }


    private void stopListeningThread(){
        //Stop the thread
        if (pduListener != null) {
            pduListener.stop();
            pduListener = null;
        }
    }

    public void stopListening() {
        stopListeningThread();

        //Unregister listeners
        if (mc != null) {
            try {
                mc.setMessageListener(null);
                //Stop listening on the port
                mc.close();
                mc = null;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void notifyIncomingMessage(MessageConnection arg0) {
        if (mc != null) {
            //Launch single message thread
            ListenThread l = new ListenThread(mc, true);
            new Thread(l).start();
        }
    }

    /**
     * Send the message. Called on a separate thread so we don't have
     * contention for the display
     */
    private class SendTextThread implements Runnable {

        private String myAddress;
        private String myMessage;

        public SendTextThread(String theAddress, String theMessage) {
            myAddress = theAddress;
            myMessage = theMessage;
        }

        public void run() {
            MessageConnection smsconn = null;

            try {
                /** Open the message connection. */
                smsconn = (MessageConnection) Connector.open(myAddress);

                TextMessage txtmessage =
                        (TextMessage) smsconn.newMessage(MessageConnection.TEXT_MESSAGE);
                txtmessage.setAddress(myAddress);
                txtmessage.setPayloadText(myMessage);
                smsconn.send(txtmessage);
            } catch (Throwable t) {
                System.out.println("Send caught: ");
                t.printStackTrace();
            }

            if (smsconn != null) {
                try {
                    smsconn.close();
                } catch (IOException ioe) {
                    System.out.println("Closing connection caught: ");
                    ioe.printStackTrace();
                }
            }
        }
    }

    private class SendPDUThread implements Runnable {

        private String myAddress;
        private byte[] myPayload;

        public SendPDUThread(String theAddress, byte[] thePayload) {
            myAddress = theAddress;
            myPayload = thePayload;
        }

        public void run() {
            MessageConnection smsconn = null;

            try {
                /** Open the message connection. */
                smsconn = (MessageConnection) Connector.open(myAddress);

                BinaryMessage datamessage =
                        (BinaryMessage) smsconn.newMessage(MessageConnection.BINARY_MESSAGE);
                datamessage.setAddress(myAddress);
                datamessage.setPayloadData(myPayload);
                smsconn.send(datamessage);
            } catch (Throwable t) {
                System.out.println("Send caught: ");
                t.printStackTrace();
            }

            if (smsconn != null) {
                try {
                    smsconn.close();
                } catch (IOException ioe) {
                    System.out.println("Closing connection caught: ");
                    ioe.printStackTrace();
                }
            }
        }
    }

    private class ListenThread implements Runnable {

        private MessageConnection myMC;
        private boolean single_pass = true;
        private boolean stop = false;

        public ListenThread(MessageConnection theMC, boolean isSinglePass) {
            myMC = theMC;
            single_pass = isSinglePass;
        }

        public void stop() {
            stop = true;
        }

        private void processMessage() {
            Message msg = null;
            try {
                //This method blocks untill a new message is recieved.
                msg = myMC.receive();

                TextMessage tmsg = null;
                if (msg instanceof TextMessage) {
                    tmsg = (TextMessage) msg;
                } else if (msg instanceof BinaryMessage) {
                    BinaryMessage bmsg = (BinaryMessage) msg;
                    byte[] data = bmsg.getPayloadData();
                    if (mySessionMgr != null) {
                        //The heavy work...
                        String message = mySessionMgr.recieveSecureText(data);

                        //Recreate orignial message
                        tmsg = (TextMessage) myMC.newMessage(MessageConnection.TEXT_MESSAGE);
                        tmsg.setAddress(bmsg.getAddress());
                        tmsg.setPayloadText(message);
                    }
                } else {/*  Ignore   */ }

                if (myListener != null && tmsg != null) {
                    //More heavy work..
                    myListener.processSMS(tmsg);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                if (single_pass) {
                    processMessage();
                } else {
                    while (!stop) {
                        processMessage();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
