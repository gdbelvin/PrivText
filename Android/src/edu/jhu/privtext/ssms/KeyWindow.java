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

/**
 * Stores a window of Crypto keys and allows secure key erasure.
 * 
 * @author Gary Belvin
 * @version 0.1
 */
public class KeyWindow {
  /** a comparison key for null. */
  private final byte[] EMPTYKEY;
  /** Key length for messages. */
  private final int my_keylen;
  /** Replay window size. */
  private final int my_windowsize;

  /** The replay window stores keys for messages that have not been received. */
  private final byte[][] my_keywindow;
  /** The message index for the key at the front of the key window. */
  private long my_headkeyid;

  /**
   * Creates a key window.
   * 
   * @param the_windowsize the number of keys to store concurrently.
   * @param the_keylength the size of the keys to store.
   */
  public KeyWindow(final int the_windowsize, final int the_keylength) {
    my_windowsize = the_windowsize;
    my_keylen = the_keylength;

    my_keywindow = new byte[my_windowsize][my_keylen];
    EMPTYKEY = new byte[my_keylen];
  }

  /**
   * Inserts the key into the window.
   * @param the_key the key to insert. ERASE LOCAL KEY STORAGE IMMEDIATELY
   *          AFTERWARDS.
   * @param the_index the index of the first key.
   */
  public void putFirstKey(final byte[] the_key, final long the_index) {
    assert the_key.length == my_keylen;
    my_headkeyid = the_index;
    System.arraycopy(the_key, 0, my_keywindow, 0, my_keylen);
  }

  /**
   * Verifies that this message is not out of order.
   * @param the_messageindex to check
   * @return whether we could have a valid key for that message.
   */
  public boolean hasKey(final long the_messageindex) {
    final long windowindex = my_headkeyid - the_messageindex;
    
    if (windowindex >= my_windowsize) {
      return false;
    }
    //Check if the key is null. 
    return !Arrays.areEqual(my_keywindow[(int) windowindex], EMPTYKEY);
  }
  
  /** @return the index for the latest key. */
  public long getHeadIndex() {
    return my_headkeyid;
  }
}
