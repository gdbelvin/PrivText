/*
 * Copyright (c) 2000 - 2011 The Legion Of The Bouncy Castle
 * (http://www.bouncycastle.org)
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
package edu.jhu.bouncycastle.crypto.modes;

import edu.jhu.bouncycastle.crypto.BlockCipher;
import edu.jhu.bouncycastle.crypto.CipherParameters;
import edu.jhu.bouncycastle.crypto.DataLengthException;
import edu.jhu.bouncycastle.crypto.params.ParametersWithIV;

/**
 * Implements the Segmented Integer Counter (SIC) mode on top of a simple
 * block cipher. This mode is also known as CTR mode.
 */
public class SICBlockCipher implements BlockCipher
{
    private final BlockCipher     cipher;
    private final int             blockSize;
    
    private byte[]          IV;
    private byte[]          counter;
    private byte[]          counterOut;


    /**
     * Basic constructor.
     *
     * @param c the block cipher to be used.
     */
    public SICBlockCipher(BlockCipher c)
    {
        this.cipher = c;
        this.blockSize = cipher.getBlockSize();
        this.IV = new byte[blockSize];
        this.counter = new byte[blockSize];
        this.counterOut = new byte[blockSize];
    }


    /**
     * return the underlying block cipher that we are wrapping.
     *
     * @return the underlying block cipher that we are wrapping.
     */
    public BlockCipher getUnderlyingCipher()
    {
        return cipher;
    }


    public void init(
        boolean             forEncryption, //ignored by this CTR mode
        CipherParameters    params)
        throws IllegalArgumentException
    {
        if (params instanceof ParametersWithIV)
        {
          ParametersWithIV ivParam = (ParametersWithIV)params;
          byte[]           iv      = ivParam.getIV();
          System.arraycopy(iv, 0, IV, 0, IV.length);

          reset();
          cipher.init(true, ivParam.getParameters());
        }
        else
        {
            throw new IllegalArgumentException("SIC mode requires ParametersWithIV");
        }
    }

    public String getAlgorithmName()
    {
        return cipher.getAlgorithmName() + "/SIC";
    }

    public int getBlockSize()
    {
        return cipher.getBlockSize();
    }


    public int processBlock(byte[] in, int inOff, byte[] out, int outOff)
          throws DataLengthException, IllegalStateException
    {
        cipher.processBlock(counter, 0, counterOut, 0);

        //
        // XOR the counterOut with the plaintext producing the cipher text
        //
        for (int i = 0; i < counterOut.length; i++)
        {
          out[outOff + i] = (byte)(counterOut[i] ^ in[inOff + i]);
        }

        int    carry = 1;
        
        for (int i = counter.length - 1; i >= 0; i--)
        {
            int    x = (counter[i] & 0xff) + carry;
            
            if (x > 0xff)
            {
                carry = 1;
            }
            else
            {
                carry = 0;
            }
            
            counter[i] = (byte)x;
        }

        return counter.length;
    }


    public void reset()
    {
        System.arraycopy(IV, 0, counter, 0, counter.length);
        cipher.reset();
    }
}