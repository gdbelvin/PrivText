/*
 * Copyright (c) 2011 Gary Belvin
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package edu.jhu.privtext.ssms;

import edu.jhu.bouncycastle.util.Arrays;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Stores a window of Crypto keys and allows secure key erasure.
 * 
 * @author Gary Belvin
 * @version 0.1
 */
public class KeyWindow {
  /** a comparison key for null. */
  private final byte[] EMPTYKEY;
  /** The delay, in miliseconds after which, keys are to be deleted. */
  // 5 minute grace period for out of order messasges
  private final long DELETE_DELAY = 5 * 60 * 1000;
  /** Key length for messages. */
  private final int my_keylen;
  /** Replay window size. */
  // Must be a power of two to prevent problems when the index rolls over.
  private final int my_windowsize;

  /** The replay window stores keys for messages that have not been received. */
  private final byte[][] my_keywindow;
  /** An array of key deletion timers. */
  private final Timer[] my_timers;
  /** The message index for the key at the front of the key window. */
  private long my_headkeyid;

  /**
   * Creates a key window.
   * 
   * @param the_windowsize the number of keys to store concurrently.
   * @param the_keylength the size of the keys to store in bytes
   */
  public KeyWindow(final int the_windowsize, final int the_keylength) {
    my_windowsize = the_windowsize;
    my_keylen = the_keylength;

    my_keywindow = new byte[my_windowsize][my_keylen];
    my_timers = new Timer[my_windowsize];
    EMPTYKEY = new byte[my_keylen];
  }

  /**
   * Inserts the key into the window.
   * @param the_key the key to insert. ERASE LOCAL KEY AFTERWARDS.
   * @param the_index the index of the first key.
   */
  public void putFirstKey(final byte[] the_key, final long the_index) {
    assert the_key.length == my_keylen;
    my_headkeyid = the_index;

    final int windowindex = (int) (the_index % my_windowsize);
    System.arraycopy(the_key, 0, my_keywindow[windowindex], 0, my_keylen);
  }

  /**
   * Inserts the next key into the window. Also sets up a timer to delete the
   * previous key in the window.
   * @param the_key the key to insert.
   * @param the_index the index of the key which must be one greater than the
   *          head mod2^40
   */
  public void putKey(final byte[] the_key, final long the_index) {
    assert the_key.length == my_keylen;
    assert mod40(the_index - my_headkeyid) == 1;
    my_headkeyid = the_index;

    final int windowindex = (int) (the_index % my_windowsize);
    System.arraycopy(the_key, 0, my_keywindow[windowindex], 0, my_keylen);

    //Cancel deletion timers for this key
    if (my_timers[windowindex] != null) {
      my_timers[windowindex].cancel();
    }
    // Auto-delete the previous key?
    final int previousindex = (int) (mod40(the_index - 1) % my_windowsize);
    if (hasKey(previousindex)) {
      if (my_timers[previousindex] != null) {
        my_timers[previousindex].cancel();
      }
      my_timers[previousindex] = new Timer();
      my_timers[previousindex].schedule(new DeleteKeyTask(previousindex), DELETE_DELAY);
    }
  }

  /**
   * Erases a key from memory.
   * @param the_index of the key to erase
   */
  public void erasekey(final long the_index) {
    if (hasKey(the_index)) {
      final int windowindex = (int) (the_index % my_windowsize);
      System.arraycopy(EMPTYKEY, 0, my_keywindow[windowindex], 0, my_keylen);
      
      if (my_timers[windowindex] != null) {
        my_timers[windowindex].cancel();
      }
    }
  }

  /** @return the value of the_num mod 2^40. */
  private long mod40(long the_num) {
    final long two40 = 0x10000000000L;
    long r = the_num % two40;
    if (r < 0) {
      r += two40;
    }
    return r;
  }

  /**
   * Verifies that this message is not out of order.
   * @param the_messageindex to check
   * @return whether we could have a valid key for that message.
   */
  public boolean hasKey(final long the_messageindex) {
    final long history = my_headkeyid - the_messageindex;

    if (history >= my_windowsize || history < 0) {
      return false;
    }
    // Check if the key is null.
    final long windowindex = the_messageindex % my_windowsize;
    return !Arrays.areEqual(my_keywindow[(int) windowindex], EMPTYKEY);
  }

  /**
   * returns the key for the specified message.
   * @param the_messageindex of the key requested.
   * @return the key
   */
  public byte[] getKey(final long the_messageindex) {
    final long history = my_headkeyid - the_messageindex;
    assert history >= 0;
    assert history < my_windowsize;
    return my_keywindow[(int) (the_messageindex % my_windowsize)];
  }

  /** @return the index for the latest key. */
  public long getHeadIndex() {
    return my_headkeyid;
  }

  /**
   * A timer for the auto deletion of intermediate keys in the key window.
   * @author Gary Belvin
   * @version 0.1
   */
  class DeleteKeyTask extends TimerTask {
    /** The index of the key to delete. */
    private final long my_index;

    /**
     * Creates a timer for key auto destruction.
     * @param the_index of the key to delete.
     */
    public DeleteKeyTask(final long the_index) {
      my_index = the_index;
    }

    @Override
    public void run() {
      erasekey(my_index);
    }
  }
}
