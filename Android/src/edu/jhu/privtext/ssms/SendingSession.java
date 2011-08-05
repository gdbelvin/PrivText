package edu.jhu.privtext.ssms;

public class SendingSession extends Session {
  /**
   * The ReKey frequency is a policy driven value that determines the upper
   * bound on the number of messages to transmit under a single master key
   * before halting and requesting a new master key (which is provided from
   * somewhere else). The rekey frequency must be less than 240, but normal
   * values will be in the 10 – 100 message range. In the event of an end-point
   * security breach, a lower rekey frequency will reduce the window of readable
   * messages before security is restored. Note that a re-key event may be
   * performed at any time by either party – perhaps especially upon learning of
   * a compromise. More frequent rekeying increases the overhead from the key
   * agreement layer.
   */
  private final int rekeyfrequency = 30;
  /** im = 28 · ROLL+SEQ (mod 240). */
  private long my_messageindex;
  
  /**
   * Sets the message index using the initial master key. Section 2.5.1
   * 
   * @param the_masterkey The master key material
   * @param the_sessionid the session identifier
   */
  private void initMessageIndex(final byte[] the_masterkey, final byte[] the_sessionid) {
    // Initialize rollover counter
    // Ascii for "InitialIndex"
    final byte[] label =
        {0x49, 0x6e, 0x69, 0x74, 0x69, 0x61, 0x6c, 0x49, 0x6e, 0x64, 0x65, 0x78};
    // session identifier||0
    final byte[] context = new byte[the_sessionid.length + 1];
    System.arraycopy(the_sessionid, 0, context, 0, the_sessionid.length);
    context[the_sessionid.length] = 0x00; // Set the last byte to 0

    // i0 = KDF(Kmaster , “InitialIndex”, session identhe_sessionidtifier||0,
    // 40)
    final byte[] i0 = keyDerivationFunction(the_masterkey, label, context, MSGINDXBITS);

    // The rollover counter is assigned the 32 left most bits of im
    // and the sequence number is assigned the following 8 bits
    // such that 2^8 · ROLL + SEQ = i0
    my_rollovercounter = getUnsignedInt(i0, 0);
    my_sequencenum = i0[4];
    // TODO: my_keywindow.putFirstKey(the_key, the_index)
  }
  
  public long getNextMessageIndex() {
    return my_rollovercounter << 8 + my_sequencenum;
  }

}
