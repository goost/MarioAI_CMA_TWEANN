package de.goost.jcmatweann;

/**
 * Copyright (c) 2014/11, Gleb Ostrowski, glebos at web dot de
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     1) Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     2) Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     3) Neither the name of the copyright holder nor the names of its
 *        contributors may be used to endorse or promote products derived from
 *        this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

public class CMATWEANN {

    public class GetPointerFailedException extends Exception{
        public GetPointerFailedException(String reason){
            super(reason);
        }
    }

    private long _pt; //pointer (handler), RAM address of the native object

    public CMATWEANN(int numIn, int numOut, int numHid, double sigma, double sigmaMin, double probNode, double probEdge, boolean bFF) throws GetPointerFailedException {
        long pt = generateCMATWEANN(numIn, numOut, numHid, sigma, sigmaMin, probNode, probEdge, bFF);
        if(pt == 0) throw new GetPointerFailedException("Contructor: Received Handler is ZERO!");
        setPt(pt);

    }

    public native int getPopSize();

    /*//method calls getNNNative and wraps the received handler in a NeuralNet-Object
    public NeuralNet getNN(int nnID) throws GetPointerFailedException {
        long pt = getNNNative(nnID);
        if(pt == 0) throw new GetPointerFailedException("GetNN: Received Handler is ZERO!");
        return new NeuralNet(pt);
    }*/

    public native void setScore(int nnID, double tmpScore);

    public native void produceOffspring();

    public native void proceedGen();

    //Call these if the CMA-TWEANN is no more needed
    public native void dispose();

    //Gets the NN from native Code and returns the pointer
    private native long getNNNative(int nnID);

    //Constructs the CMA-TWEANN in native Code and returns the pointer
    private native long generateCMATWEANN(int numIn, int numOut, int numHid, double sigma, double sigmaMin, double probNode, double probEdge, boolean bFF);

    public native double[] activate(int nnID, double[] inputs, int outputSize);

    //TODO RENAME AND RECOMPILE LIB
    public native double[] activateBest(double[] inputs, int outputSize);

    /*GETTER AND SETTER BELOW*/
    public void setPt(long _pt) {
        this._pt = _pt;
    }

    public long getPt() {
        return _pt;
    }

    static {
        System.loadLibrary("libjcmatweann");
    }

}

