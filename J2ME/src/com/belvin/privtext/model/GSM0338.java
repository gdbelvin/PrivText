package com.belvin.privtext.model;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class GSM0338 {

    /** GSM-7 to Unicode */
    private static byte ESC_TO_EXTENTION_TABLE = 0x1B;
    private static char[] lookuptable = new char[128];//{ };
    private static char[] extlookuptable = new char[128];//{ };
    private static Hashtable GSM7;
    private static Hashtable GSM7_ext;

    static {
        GSM7 = new Hashtable(160);
        GSM7_ext = new Hashtable();

        GSM7.put(new Character('\u0040'), new Byte((byte) 0x00));  //	COMMERCIAL AT
        //GSM7.put(new Character('\u0000'), new Byte((byte)0x00));  //	NULL (see note above)
        GSM7.put(new Character('\u00A3'), new Byte((byte) 0x01));  //	POUND SIGN
        GSM7.put(new Character('\u0024'), new Byte((byte) 0x02));  //	DOLLAR SIGN
        GSM7.put(new Character('\u00A5'), new Byte((byte) 0x03));  //	YEN SIGN
        GSM7.put(new Character('\u00E8'), new Byte((byte) 0x04));  //	LATIN SMALL LETTER E WITH GRAVE
        GSM7.put(new Character('\u00E9'), new Byte((byte) 0x05));  //	LATIN SMALL LETTER E WITH ACUTE
        GSM7.put(new Character('\u00F9'), new Byte((byte) 0x06));  //	LATIN SMALL LETTER U WITH GRAVE
        GSM7.put(new Character('\u00EC'), new Byte((byte) 0x07));  //	LATIN SMALL LETTER I WITH GRAVE
        GSM7.put(new Character('\u00F2'), new Byte((byte) 0x08));  //	LATIN SMALL LETTER O WITH GRAVE
        GSM7.put(new Character('\u00E7'), new Byte((byte) 0x09));  //	LATIN SMALL LETTER C WITH CEDILLA
        //GSM7.put(new Character('\u00C7'), new Byte((byte)0x09));  //	LATIN CAPITAL LETTER C WITH CEDILLA (see note above)
        GSM7.put(new Character('\n'), new Byte((byte) 0x0A));  //	LINE FEED
        GSM7.put(new Character('\u00D8'), new Byte((byte) 0x0B));  //	LATIN CAPITAL LETTER O WITH STROKE
        GSM7.put(new Character('\u00F8'), new Byte((byte) 0x0C));  //	LATIN SMALL LETTER O WITH STROKE
        GSM7.put(new Character('\r'), new Byte((byte) 0x0D));  //	CARRIAGE RETURN
        GSM7.put(new Character('\u00C5'), new Byte((byte) 0x0E));  //	LATIN CAPITAL LETTER A WITH RING ABOVE
        GSM7.put(new Character('\u00E5'), new Byte((byte) 0x0F));  //	LATIN SMALL LETTER A WITH RING ABOVE
        GSM7.put(new Character('\u0394'), new Byte((byte) 0x10));  //	GREEK CAPITAL LETTER DELTA
        GSM7.put(new Character('\u005F'), new Byte((byte) 0x11));  //	LOW LINE
        GSM7.put(new Character('\u03A6'), new Byte((byte) 0x12));  //	GREEK CAPITAL LETTER PHI
        GSM7.put(new Character('\u0393'), new Byte((byte) 0x13));  //	GREEK CAPITAL LETTER GAMMA
        GSM7.put(new Character('\u039B'), new Byte((byte) 0x14));  //	GREEK CAPITAL LETTER LAMDA
        GSM7.put(new Character('\u03A9'), new Byte((byte) 0x15));  //	GREEK CAPITAL LETTER OMEGA
        GSM7.put(new Character('\u03A0'), new Byte((byte) 0x16));  //	GREEK CAPITAL LETTER PI
        GSM7.put(new Character('\u03A8'), new Byte((byte) 0x17));  //	GREEK CAPITAL LETTER PSI
        GSM7.put(new Character('\u03A3'), new Byte((byte) 0x18));  //	GREEK CAPITAL LETTER SIGMA
        GSM7.put(new Character('\u0398'), new Byte((byte) 0x19));  //	GREEK CAPITAL LETTER THETA
        GSM7.put(new Character('\u039E'), new Byte((byte) 0x1A));  //	GREEK CAPITAL LETTER XI
        GSM7.put(new Character('\u00A0'), new Byte((byte) 0x1B));  //	ESCAPE TO EXTENSION TABLE (or displayed as NBSP, see note above)

        GSM7_ext.put(new Character('\u000C'), new Byte((byte) 0x0A)); // #	FORM FEED
        GSM7_ext.put(new Character('\u005E'), new Byte((byte) 0x14)); //	CIRCUMFLEX ACCENT
        GSM7_ext.put(new Character('\u007B'), new Byte((byte) 0x28)); //	LEFT CURLY BRACKET
        GSM7_ext.put(new Character('\u007D'), new Byte((byte) 0x29)); //	RIGHT CURLY BRACKET
        GSM7_ext.put(new Character('\\'), new Byte((byte) 0x2F)); //	REVERSE SOLIDUS
        GSM7_ext.put(new Character('\u005B'), new Byte((byte) 0x3C)); //	LEFT SQUARE BRACKET
        GSM7_ext.put(new Character('\u007E'), new Byte((byte) 0x3D)); //	TILDE
        GSM7_ext.put(new Character('\u005D'), new Byte((byte) 0x3E)); //	RIGHT SQUARE BRACKET
        GSM7_ext.put(new Character('\u007C'), new Byte((byte) 0x40)); //	VERTICAL LINE
        GSM7_ext.put(new Character('\u20AC'), new Byte((byte) 0x65)); //	EURO SIGN

        GSM7.put(new Character('\u00C6'), new Byte((byte) 0x1C));  //	LATIN CAPITAL LETTER AE
        GSM7.put(new Character('\u00E6'), new Byte((byte) 0x1D));  //	LATIN SMALL LETTER AE
        GSM7.put(new Character('\u00DF'), new Byte((byte) 0x1E));  //	LATIN SMALL LETTER SHARP S (German)
        GSM7.put(new Character('\u00C9'), new Byte((byte) 0x1F));  //	LATIN CAPITAL LETTER E WITH ACUTE
        GSM7.put(new Character('\u0020'), new Byte((byte) 0x20));  //	SPACE
        GSM7.put(new Character('\u0021'), new Byte((byte) 0x21));  //	EXCLAMATION MARK
        GSM7.put(new Character('\u0022'), new Byte((byte) 0x22));  //	QUOTATION MARK
        GSM7.put(new Character('\u0023'), new Byte((byte) 0x23));  //	NUMBER SIGN
        GSM7.put(new Character('\u00A4'), new Byte((byte) 0x24));  //	CURRENCY SIGN
        GSM7.put(new Character('\u0025'), new Byte((byte) 0x25));  //	PERCENT SIGN
        GSM7.put(new Character('\u0026'), new Byte((byte) 0x26));  //	AMPERSAND
        GSM7.put(new Character('\''), new Byte((byte) 0x27));  //	APOSTROPHE
        GSM7.put(new Character('\u0028'), new Byte((byte) 0x28));  //	LEFT PARENTHESIS
        GSM7.put(new Character('\u0029'), new Byte((byte) 0x29));  //	RIGHT PARENTHESIS
        GSM7.put(new Character('\u002A'), new Byte((byte) 0x2A));  //	ASTERISK
        GSM7.put(new Character('\u002B'), new Byte((byte) 0x2B));  //	PLUS SIGN
        GSM7.put(new Character('\u002C'), new Byte((byte) 0x2C));  //	COMMA
        GSM7.put(new Character('\u002D'), new Byte((byte) 0x2D));  //	HYPHEN-MINUS
        GSM7.put(new Character('\u002E'), new Byte((byte) 0x2E));  //	FULL STOP
        GSM7.put(new Character('\u002F'), new Byte((byte) 0x2F));  //	SOLIDUS
        GSM7.put(new Character('\u0030'), new Byte((byte) 0x30));  //	DIGIT ZERO
        GSM7.put(new Character('\u0031'), new Byte((byte) 0x31));  //	DIGIT ONE
        GSM7.put(new Character('\u0032'), new Byte((byte) 0x32));  //	DIGIT TWO
        GSM7.put(new Character('\u0033'), new Byte((byte) 0x33));  //	DIGIT THREE
        GSM7.put(new Character('\u0034'), new Byte((byte) 0x34));  //	DIGIT FOUR
        GSM7.put(new Character('\u0035'), new Byte((byte) 0x35));  //	DIGIT FIVE
        GSM7.put(new Character('\u0036'), new Byte((byte) 0x36));  //	DIGIT SIX
        GSM7.put(new Character('\u0037'), new Byte((byte) 0x37));  //	DIGIT SEVEN
        GSM7.put(new Character('\u0038'), new Byte((byte) 0x38));  //	DIGIT EIGHT
        GSM7.put(new Character('\u0039'), new Byte((byte) 0x39));  //	DIGIT NINE
        GSM7.put(new Character('\u003A'), new Byte((byte) 0x3A));  //	COLON
        GSM7.put(new Character('\u003B'), new Byte((byte) 0x3B));  //	SEMICOLON
        GSM7.put(new Character('\u003C'), new Byte((byte) 0x3C));  //	LESS-THAN SIGN
        GSM7.put(new Character('\u003D'), new Byte((byte) 0x3D));  //	EQUALS SIGN
        GSM7.put(new Character('\u003E'), new Byte((byte) 0x3E));  //	GREATER-THAN SIGN
        GSM7.put(new Character('\u003F'), new Byte((byte) 0x3F));  //	QUESTION MARK
        GSM7.put(new Character('\u00A1'), new Byte((byte) 0x40));  //	INVERTED EXCLAMATION MARK
        GSM7.put(new Character('\u0041'), new Byte((byte) 0x41));  //	LATIN CAPITAL LETTER A
        //GSM7.put(new Character('\u0391'), new Byte((byte)0x41));  //	GREEK CAPITAL LETTER ALPHA
        GSM7.put(new Character('\u0042'), new Byte((byte) 0x42));  //	LATIN CAPITAL LETTER B
        //GSM7.put(new Character('\u0392'), new Byte((byte)0x42));  //	GREEK CAPITAL LETTER BETA
        GSM7.put(new Character('\u0043'), new Byte((byte) 0x43));  //	LATIN CAPITAL LETTER C
        GSM7.put(new Character('\u0044'), new Byte((byte) 0x44));  //	LATIN CAPITAL LETTER D
        GSM7.put(new Character('\u0045'), new Byte((byte) 0x45));  //	LATIN CAPITAL LETTER E
        //GSM7.put(new Character('\u0395'), new Byte((byte)0x45));  //	GREEK CAPITAL LETTER EPSILON
        GSM7.put(new Character('\u0046'), new Byte((byte) 0x46));  //	LATIN CAPITAL LETTER F
        GSM7.put(new Character('\u0047'), new Byte((byte) 0x47));  //	LATIN CAPITAL LETTER G
        GSM7.put(new Character('\u0048'), new Byte((byte) 0x48));  //	LATIN CAPITAL LETTER H
        //GSM7.put(new Character('\u0397'), new Byte((byte)0x48));  //	GREEK CAPITAL LETTER ETA
        GSM7.put(new Character('\u0049'), new Byte((byte) 0x49));  //	LATIN CAPITAL LETTER I
        //GSM7.put(new Character('\u0399'), new Byte((byte)0x49));  //	GREEK CAPITAL LETTER IOTA
        GSM7.put(new Character('\u004A'), new Byte((byte) 0x4A));  //	LATIN CAPITAL LETTER J
        GSM7.put(new Character('\u004B'), new Byte((byte) 0x4B));  //	LATIN CAPITAL LETTER K
        //GSM7.put(new Character('\u039A'), new Byte((byte)0x4B));  //	GREEK CAPITAL LETTER KAPPA
        GSM7.put(new Character('\u004C'), new Byte((byte) 0x4C));  //	LATIN CAPITAL LETTER L
        GSM7.put(new Character('\u004D'), new Byte((byte) 0x4D));  //	LATIN CAPITAL LETTER M
        //GSM7.put(new Character('\u039C'), new Byte((byte)0x4D));  //	GREEK CAPITAL LETTER MU
        GSM7.put(new Character('\u004E'), new Byte((byte) 0x4E));  //	LATIN CAPITAL LETTER N
        //GSM7.put(new Character('\u039D'), new Byte((byte)0x4E));  //	GREEK CAPITAL LETTER NU
        GSM7.put(new Character('\u004F'), new Byte((byte) 0x4F));  //	LATIN CAPITAL LETTER O
        //#GSM7.put(new Character('\u039F'), new Byte((byte)0x4F));  //	GREEK CAPITAL LETTER OMICRON
        GSM7.put(new Character('\u0050'), new Byte((byte) 0x50));  //	LATIN CAPITAL LETTER P
        //GSM7.put(new Character('\u03A1'), new Byte((byte)0x50));  //	GREEK CAPITAL LETTER RHO
        GSM7.put(new Character('\u0051'), new Byte((byte) 0x51));  //	LATIN CAPITAL LETTER Q
        GSM7.put(new Character('\u0052'), new Byte((byte) 0x52));  //	LATIN CAPITAL LETTER R
        GSM7.put(new Character('\u0053'), new Byte((byte) 0x53));  //	LATIN CAPITAL LETTER S
        GSM7.put(new Character('\u0054'), new Byte((byte) 0x54));  //	LATIN CAPITAL LETTER T
        //GSM7.put(new Character('\u03A4'), new Byte((byte)0x54));  //	GREEK CAPITAL LETTER TAU
        GSM7.put(new Character('\u0055'), new Byte((byte) 0x55));  //	LATIN CAPITAL LETTER U
        GSM7.put(new Character('\u0056'), new Byte((byte) 0x56));  //	LATIN CAPITAL LETTER V
        GSM7.put(new Character('\u0057'), new Byte((byte) 0x57));  //	LATIN CAPITAL LETTER W
        GSM7.put(new Character('\u0058'), new Byte((byte) 0x58));  //	LATIN CAPITAL LETTER X
        //GSM7.put(new Character('\u03A7'), new Byte((byte)0x58));  //	GREEK CAPITAL LETTER CHI
        GSM7.put(new Character('\u0059'), new Byte((byte) 0x59));  //	LATIN CAPITAL LETTER Y
        //GSM7.put(new Character('\u03A5'), new Byte((byte)0x59));  //	GREEK CAPITAL LETTER UPSILON
        GSM7.put(new Character('\u005A'), new Byte((byte) 0x5A));  //	LATIN CAPITAL LETTER Z
        //GSM7.put(new Character('\u0396'), new Byte((byte)0x5A));  //	GREEK CAPITAL LETTER ZETA
        GSM7.put(new Character('\u00C4'), new Byte((byte) 0x5B));  //	LATIN CAPITAL LETTER A WITH DIAERESIS
        GSM7.put(new Character('\u00D6'), new Byte((byte) 0x5C));  //	LATIN CAPITAL LETTER O WITH DIAERESIS
        GSM7.put(new Character('\u00D1'), new Byte((byte) 0x5D));  //	LATIN CAPITAL LETTER N WITH TILDE
        GSM7.put(new Character('\u00DC'), new Byte((byte) 0x5E));  //	LATIN CAPITAL LETTER U WITH DIAERESIS
        GSM7.put(new Character('\u00A7'), new Byte((byte) 0x5F));  //	SECTION SIGN
        GSM7.put(new Character('\u00BF'), new Byte((byte) 0x60));  //	INVERTED QUESTION MARK
        GSM7.put(new Character('\u0061'), new Byte((byte) 0x61));  //	LATIN SMALL LETTER A
        GSM7.put(new Character('\u0062'), new Byte((byte) 0x62));  //	LATIN SMALL LETTER B
        GSM7.put(new Character('\u0063'), new Byte((byte) 0x63));  //	LATIN SMALL LETTER C
        GSM7.put(new Character('\u0064'), new Byte((byte) 0x64));  //	LATIN SMALL LETTER D
        GSM7.put(new Character('\u0065'), new Byte((byte) 0x65));  //	LATIN SMALL LETTER E
        GSM7.put(new Character('\u0066'), new Byte((byte) 0x66));  //	LATIN SMALL LETTER F
        GSM7.put(new Character('\u0067'), new Byte((byte) 0x67));  //	LATIN SMALL LETTER G
        GSM7.put(new Character('\u0068'), new Byte((byte) 0x68));  //	LATIN SMALL LETTER H
        GSM7.put(new Character('\u0069'), new Byte((byte) 0x69));  //	LATIN SMALL LETTER I
        GSM7.put(new Character('\u006A'), new Byte((byte) 0x6A));  //	LATIN SMALL LETTER J
        GSM7.put(new Character('\u006B'), new Byte((byte) 0x6B));  //	LATIN SMALL LETTER K
        GSM7.put(new Character('\u006C'), new Byte((byte) 0x6C));  //	LATIN SMALL LETTER L
        GSM7.put(new Character('\u006D'), new Byte((byte) 0x6D));  //	LATIN SMALL LETTER M
        GSM7.put(new Character('\u006E'), new Byte((byte) 0x6E));  //	LATIN SMALL LETTER N
        GSM7.put(new Character('\u006F'), new Byte((byte) 0x6F));  //	LATIN SMALL LETTER O
        GSM7.put(new Character('\u0070'), new Byte((byte) 0x70));  //	LATIN SMALL LETTER P
        GSM7.put(new Character('\u0071'), new Byte((byte) 0x71));  //	LATIN SMALL LETTER Q
        GSM7.put(new Character('\u0072'), new Byte((byte) 0x72));  //	LATIN SMALL LETTER R
        GSM7.put(new Character('\u0073'), new Byte((byte) 0x73));  //	LATIN SMALL LETTER S
        GSM7.put(new Character('\u0074'), new Byte((byte) 0x74));  //	LATIN SMALL LETTER T
        GSM7.put(new Character('\u0075'), new Byte((byte) 0x75));  //	LATIN SMALL LETTER U
        GSM7.put(new Character('\u0076'), new Byte((byte) 0x76));  //	LATIN SMALL LETTER V
        GSM7.put(new Character('\u0077'), new Byte((byte) 0x77));  //	LATIN SMALL LETTER W
        GSM7.put(new Character('\u0078'), new Byte((byte) 0x78));  //	LATIN SMALL LETTER X
        GSM7.put(new Character('\u0079'), new Byte((byte) 0x79));  //	LATIN SMALL LETTER Y
        GSM7.put(new Character('\u007A'), new Byte((byte) 0x7A));  //	LATIN SMALL LETTER Z
        GSM7.put(new Character('\u00E4'), new Byte((byte) 0x7B));  //	LATIN SMALL LETTER A WITH DIAERESIS
        GSM7.put(new Character('\u00F6'), new Byte((byte) 0x7C));  //	LATIN SMALL LETTER O WITH DIAERESIS
        GSM7.put(new Character('\u00F1'), new Byte((byte) 0x7D));  //	LATIN SMALL LETTER N WITH TILDE
        GSM7.put(new Character('\u00FC'), new Byte((byte) 0x7E));  //	LATIN SMALL LETTER U WITH DIAERESIS
        GSM7.put(new Character('\u00E0'), new Byte((byte) 0x7F));  //	LATIN SMALL LETTER A WITH GRAVE

        Enumeration e = GSM7.keys();
        while (e.hasMoreElements()) {
            Character C = (Character) e.nextElement();
            char c = C.charValue();
            Byte B = (Byte) GSM7.get(C);
            byte b = B.byteValue();
            lookuptable[b] = c;
        }

        e = GSM7_ext.keys();
        while (e.hasMoreElements()) {
            Character C = (Character) e.nextElement();
            char c = C.charValue();
            Byte B = (Byte) GSM7_ext.get(C);
            byte b = B.byteValue();
            extlookuptable[b] = c;
        }
    }

    public static byte[] encode(String theMessage) throws Exception {

        char[] chars = theMessage.toCharArray();
        Vector bb = new Vector();
        for (int i=0; i < chars.length; i++){
            char c = chars[i];
            Character C = new Character(c);
            if (GSM7.get(C) != null) {
                bb.addElement(GSM7.get(C));
            } else if (GSM7_ext.get(C) != null) {
                bb.addElement(new Byte(ESC_TO_EXTENTION_TABLE));
                bb.addElement(GSM7_ext.get(C));
            } else {
                throw new Exception("GSM0338 Could not encode: " + c);
            }

        }
        /* If the total number of characters to be sent equals (8n-1) where n=1,2,3 etc. then there are 7 spare bits
         * at the end of the message. To avoid the situation where the receiving entity confuses 7 binary zero pad bits
         * as the @ character, the carriage return or <CR> character (defined in subclause 7.1.1) shall be used for
         * padding in this situation, just as for Cell Broadcast.
         */
        int num_chars = bb.size();
        int last_index = bb.size() - 1;
        boolean ends_with_confusing_padding = (num_chars % 8) == 7;
        boolean ends_on_word_boundary = (num_chars % 8) == 0;
        Byte char_ret_byte = (Byte) GSM7.get(new Character('\r'));
        if (ends_with_confusing_padding) {
            bb.addElement(char_ret_byte);
        }
        /*If <CR> is intended to be the last character and the message (including the wanted <CR>) ends on an
         * octet boundary, then another <CR> must be added together with a padding bit 0. The receiving entity
         * will perform the carriage return function twice, but this will not result in misoperation as the
         * definition of <CR> in subclause 7.1.1 is identical to the definition of <CR><CR>.
         */
        if (ends_on_word_boundary && last_index > 0 &&
                bb.elementAt(last_index) == char_ret_byte) {
            bb.addElement(char_ret_byte);
        }

        bb.trimToSize();

        //Pack
        int out_len = (int) Math.ceil(bb.size() * 7 / 8.0);
        byte[] out = new byte[out_len];
        int k = 0;										//output index
        int j = 0;										//0 .. 7
        for (int i = 0; i < bb.size(); i++) {
            //low bits are  							//	 j = 0		j = 1		  j = 6		  j = 7
            byte low_mask = (byte) (0x7f >> j);		//0b01111111, 0b00111111 .. 0b00000001, 0b00000000
            byte high_mask = (byte) (0x7f >> (7 - j));	//0b00000000, 0b00000001 .. 0b00111111, 0b01111111
            Byte Cnt = (Byte)bb.elementAt(i);
            byte cnt = (byte) (Cnt.byteValue() & 0x7f);	//7 bit character encoding
            byte low = (byte) ((cnt >> j) & low_mask);	//High (7-j) bits of cnt in LSB position
            byte high = (byte) ((cnt & high_mask) << (8 - j));

            if (k > 0) {
                out[k - 1] = (byte) (out[k - 1] | high);	//Skip first time around
            }
            if (k < out.length) //Are we ending on an octet boundary?
            {
                out[k] = (byte) (out[k] | low);		//Skip the last time around (when a word aligns)
            } 

            if (j != 7) {
                k = k + 1;						//When j=7, two words have become aligned
            }
            j = (j + 1) % 8;
        }
        //byte low_mask  = (byte) (0x7f >> j);		//0b01111111, 0b00111111 .. 0b00000001, 0b00000000
        //out[k] = (byte) (out[k-1] & low_mask);		//pad high end with 0's

        return out;
    }

    public static String decode(byte[] buffer) {
        if (buffer.length < 1) {
            return "";
        }
        Vector gsmchars = new Vector();

        //Unpack
        int j = 0;		//rotation counter [0..7]fffff
        int p = -1;		//Pointer into byte buffer

        //Since we consider pairs, we start at -1 and go till n-1.
        //n can only be accessed if it is a full nibble ie. j==7
        while ((p + 1) < buffer.length || ((p + 1) == buffer.length && j == 7)) {

            //j is only related to p for until the first pause
            //j = (p+1) % 8;		 					//	 j = 0		j = 1		  j = 6		  j = 7
            int x0_mask = (0xff00 >> j) & 0x00ff;	//0b00000000, 0b10000000 .. 0b11111100, 0b11111110
            int x1_mask = (0x7f >> j);			//0b01111111, 0b00111111 .. 0b00000001, 0b00000000

            int x0, x1;

            if (j == 0) {
                x0 = 0x00;
            } else {
                x0 = (buffer[p] & x0_mask);
            }
            if (j == 7) {
                x1 = 0x00;
            } else {
                x1 = (buffer[p + 1] & x1_mask);
            }

            byte high, low, cnt;
            low = (byte) (x0 >> (8 - j));
            high = (byte) (x1 << j);
            cnt = (byte) (high | low);
            gsmchars.addElement(new Byte(cnt));

            if (j != 7) //j = 7 and 0 are spent at the same place
            {
                p++;
            }
            j = (j + 1) % 8;
        }

        //Takes into account the extra j=j+1%8 at the end of the loop.  real values are one less
        boolean ended_on_octet_boundary = j == 0;
        boolean ended_on_octet_boundary_with_extra_cr = j == 1;
        int last_index = gsmchars.size() - 1;
        byte last_char = ((Byte) gsmchars.elementAt(last_index)).byteValue();

        //Remove extra padding
        //Nope, this is supposed to be padded with <CR> if it's just padding
//		if(ended_on_octet_boundary && last_char == 0x00){
//			gsmchars.remove(last_index);
//		}
		/* The receiving entity shall remove the final <CR> character where the message ends on an 
         * octet boundary with <CR> as the last character.
         */
        if (ended_on_octet_boundary && last_char == '\r') {
            gsmchars.removeElementAt(last_index);
        } //Remove the case of two consecutive <CR><CR> when the original CR ends on a boundary.
        else if (ended_on_octet_boundary_with_extra_cr &&
                last_char == '\r' && 
                ((Byte)gsmchars.elementAt(last_index - 1)).byteValue() == '\r') {
            gsmchars.removeElementAt(last_index);
        }

        //Convert to string
        StringBuffer sb = new StringBuffer();
        for (int n = 0; n < gsmchars.size(); n++) {
            byte b = ((Byte)gsmchars.elementAt(n)).byteValue();
            if (b == ESC_TO_EXTENTION_TABLE && (n + 1) < gsmchars.size()) {
                n++;
                b = ((Byte) gsmchars.elementAt(n)).byteValue();
                sb.append(extlookuptable[b]);
            } else {
                sb.append(lookuptable[b]);
            }
        }
        return sb.toString();

    }
}
