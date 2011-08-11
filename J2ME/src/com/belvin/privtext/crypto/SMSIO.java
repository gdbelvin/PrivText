package com.belvin.privtext.crypto;

public interface SMSIO {
	/** The port used for PDUs */
	//static short APP_PORT = (short) 50001;  // 16000-16999 are valid application ports
	
	/**
	 * @param thePhoneNo
	 * @param theUserData  The user portion of the PDU.  
	 * This is not the raw PDU unfortunately since android does not 
	 * support that functionality
	 */
	public abstract void sendPDU(String thePhoneNo, byte[] theUserData);
	
	public abstract void sendText(String thePhoneNo, String theMessage);

        public abstract int getAppPort();

}
