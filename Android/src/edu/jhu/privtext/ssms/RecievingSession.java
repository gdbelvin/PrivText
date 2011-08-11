
package edu.jhu.privtext.ssms;

public class RecievingSession extends Session {
  /** Replay window size. */
  private static final int REPLAYWINDOW = 4;
  /** A data structure for storing temporary keys with timed erasure. */
  private final KeyWindow my_keywindow = new KeyWindow(REPLAYWINDOW, MSGKEYBYTES);

  /** A 32 bit roll over counter. */
  private long my_rollovercounter;
  /** The last valid sequence number received. */
  private byte my_sl;

  /**
   * Initializes the receiving session.
   * @param the_sessionid of the session
   * @param the_masterkey either responder to initiator or visa versa
   */
  public RecievingSession(final String the_sessionid, final byte[] the_masterkey) {
    super(the_sessionid);
    final long firstindex = getInitMessageIndex(the_masterkey, the_sessionid);
    my_rollovercounter = firstindex >> 8;
    my_sl = (byte) (firstindex & 0xff);
    final byte[] firstkey = computeMessageKey(the_masterkey, firstindex);
    my_keywindow.putFirstKey(firstkey, firstindex);
    System.arraycopy(EMPTYKEY, 0, firstkey, 0, MSGKEYBYTES);
  }

  /**
   * Updates the session state after receiving a valid message. 1) Advances the
   * session key as needed 2) Erases message key 3) Updates the rollover counter
   * and highest seen sequence number.
   * @param the_messageindex of the message to confirm
   */
  public void confirmMessage(final long the_messageindex) {
    // Make sure we only advance by the width of one window.
    assert mod40(the_messageindex - my_keywindow.getHeadIndex()) < REPLAYWINDOW;
    while (my_keywindow.getHeadIndex() >= the_messageindex) {
      // 5. Advance session key as needed, storing intermediate values of
      // skipped messages for a short
      // period of time.
      final byte[] key = my_keywindow.getKey(my_keywindow.getHeadIndex());
      final long nextindex = mod40(my_keywindow.getHeadIndex() + 1);
      final byte[] newkey = computeMessageKey(key, nextindex);
      my_keywindow.putKey(newkey, nextindex);
      System.arraycopy(EMPTYKEY, 0, key, 0, MSGKEYBYTES);
      System.arraycopy(EMPTYKEY, 0, newkey, 0, MSGKEYBYTES);
    }
    // Update replay window by erasing key.
    my_keywindow.erasekey(the_messageindex);

    // 7. Update the rollover counter and highest received sequence number.
    // adapted from RFC 3711 section 3.3.1

    final long v = the_messageindex >> 8;
    final byte seq = (byte) (the_messageindex & 0xff);
    // if v > my_rollovercounter mod 2^32
    if (v == my_rollovercounter) {
      if (seq > my_sl) {
        my_sl = seq;
      }
    } else if (v == mod32(my_rollovercounter + 1)) {
      my_rollovercounter = v;
      my_sl = seq;
    }
  }

  /**
   * Estimates the index number given a sequence number and the rollover
   * counter.
   * 
   * Adapted from RFC 3711 Appendix A - Pseudocode for Index Determination
   * 
   * @param the_sequencenum of the message
   * @return the index number
   */
  public long estimateMessageIndex(final byte the_sequencenum) {
    final int halfway = 0x80; // Using 8 bit sequence numbers
    long v;
    if (my_sl < halfway) {
      if (the_sequencenum - my_sl > halfway) {
        v = mod32(my_rollovercounter - 1);
      } else {
        v = my_rollovercounter;
      }
    } else {
      if (my_sl - halfway > the_sequencenum) {
        v = mod32(my_rollovercounter + 1);
      } else {
        v = my_rollovercounter;
      }
    }
    return (v << 8) + (long)the_sequencenum;
  }

  /**
   * Verifies that this message is not out of order.
   * @param the_messageindex to check
   * @return whether we could have a valid key for that message.
   */
  public boolean hasKey(final long the_messageindex) {
    final boolean havekey = my_keywindow.hasKey(the_messageindex);
    final boolean couldhavekey =
      (the_messageindex - my_keywindow.getHeadIndex()) < REPLAYWINDOW &&
      (the_messageindex - my_keywindow.getHeadIndex()) > 0;

    return havekey || couldhavekey;
  }

  /**
   * @param the_messageindex of the key requested.
   * @return the key of the requested message index.
   */
  public byte[] getKey(final long the_messageindex) {
    return my_keywindow.getKey(the_messageindex);
  }

}
