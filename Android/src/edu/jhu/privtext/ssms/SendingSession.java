
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
  private final int my_rekeyfrequency = 30;
  
  /** The number of messages sent under this key. */
  private int my_messagecount;
  
  /** The key for the listed messageindex. */
  private final byte[] my_key;

  /** im = 28 · ROLL+SEQ (mod 240). */
  private long my_messageindex;

  /**
   * Sets up a sending session.
   * @param the_masterkey of the key agreement
   * @param the_sessionid of this session
   */
  public SendingSession(final byte[] the_sessionid, final byte[] the_masterkey) {
    super(the_sessionid);
    my_messageindex = getInitMessageIndex(the_masterkey, the_sessionid);
    my_key = computeMessageKey(the_masterkey, my_messageindex);
    my_messagecount = 0;
  }

  /**
   * Advance session key by executing the KDF function Update the rollover
   * counter if necessary.
   * @throws RekeyException when it is time to refresh the session with a new master key.
   */
  public void advanceIndex() throws RekeyException {
    if (my_messagecount > my_rekeyfrequency) {
      throw new RekeyException();
    }
    
    my_messageindex = mod40(my_messageindex + 1);
    my_messagecount++;
    final byte[] nextkey = computeMessageKey(my_key, my_messageindex);
    System.arraycopy(nextkey, 0, my_key, 0, MSGKEYBYTES);
    System.arraycopy(EMPTYKEY, 0, nextkey, 0, MSGKEYBYTES);
  }
  
  /** @return the status of the age of the key */
  public boolean needsReKey() {
    return my_messagecount >= my_rekeyfrequency;
  }

  /** @return the message index. */
  public long getMessageIndex() {
    return my_messageindex;
  }

  /** @return the sequence number part of the message index. */
  public byte getSequenceNumber() {
    return (byte) (my_messageindex & 0xff);
  }

  /** @return the key for the current message index. */
  public byte[] getKey() {
    return my_key;
  }

}
