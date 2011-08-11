package com.belvin.privtext.model;

public class TPDCS {
    public String test;
        public static final TPDCS Default_ = new TPDCS("UTF-8");
        public static final TPDCS SevenBit = new TPDCS("SCGSM");
	//SevenBit("US-ASCII"), 
	//"SCGSM" (a.k.a. "GSM-default-alphabet", "GSM_0338", "GSM_DEFAULT", "GSM7", "GSM-7BIT")
	public static final TPDCS EightBit = new TPDCS("ISO-8859-1");
	public static final TPDCS UCS2 = new TPDCS("UTF-16BE");
	
	private String myCharsetName;
	
	private TPDCS(String theCharset){
		myCharsetName = theCharset;
	}
	
	public String getCharsetName(){
		return myCharsetName;
	}	
}

