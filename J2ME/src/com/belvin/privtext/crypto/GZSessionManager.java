package com.belvin.privtext.crypto;

import com.belvin.bouncy.util.encoders.Hex;

/**
 * This class manages the session keys for all GZ secure conversations
 * @author urbanus
 *
 */
public class GZSessionManager {
	
	private static final String TAG = "Privtext-GZSession";
	private GZEngine myCipher = new GZEngine();
	private SMSIO myRadio;
	
	/**
	 * 
	 * @param theRadio The SMS input and output functions.
	 * This is needed in case messages span more than one message. 
	 */
	public GZSessionManager(SMSIO theRadio){
		myRadio = theRadio;
	}
	
	
	/**
	 * Sends a message using the GZprotocol
	 * 
	 * @param phoneNo
	 * @param message
	 */
	public void sendSecureSMS(String phoneNo, String message) {
		// TODO Verification
		// if( phoneNo is not valid phone number ) throw new invalid phone

		// Key Setup
		byte[] key = Hex.decode("15B3CA14A92A2A7F2B827A49B901ED76"); 
		byte[] nonce = { 0x01 };

		byte[] bmsg = GZEncode.encodeString(message);
		byte[] ciphertext = new byte[0];
		byte[] payload;

		try {			
			ciphertext = myCipher.encrypt(bmsg, key, nonce);
			payload = myCipher.packageMessage(nonce, new byte[0], ciphertext);

			// Message size = [ 1 byte nonce][x byte message][ 1 byte mac]
			
			myRadio.sendPDU(phoneNo, payload);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
	
	/**
	 * 
	 * @param thePDU
	 * @return
	 */
	public String recieveSecureText(byte[] theUserData){
		//TODO: Figure out if this an encrypted message or not.
		//Currently this is determined by the port number.
		//An alternate method is to just try decrypting it and checking the mac.
	
		// Key Setup
		byte[] key = Hex.decode("15B3CA14A92A2A7F2B827A49B901ED76");
		//byte[] nonce;
		
		//These are evil, adversary controlled values

		byte[] payload = theUserData;	
		//Does the user header need to be extracted?
		byte[] nonce = myCipher.getNonce(payload);
		byte[] ciphertext =	myCipher.getCiphertext(payload);	
		byte[] plaintext = {};
		String message = "";
		
		try {
			plaintext = myCipher.decrypt(ciphertext, key, nonce);
			message = GZEncode.decodeString(plaintext);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return message;
	}

}
