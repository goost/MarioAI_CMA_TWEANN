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

    /**
     * Constructs the CMA-TWEANN in native Code and returns the pointer
     * A high sigma in the beginning and a low in the end is recommended to avoid
     * local optima
     *
     * @param numIn number of input neurons
     * @param numOut number of output neurons
     * @param numHid initial number of hidden neurons
     * @param sigma "learning speed", the higher, the more curious is the net
     * @param sigmaMin sigma is changed during the course, this is the minimum
     * @param probNode the probability for a new hidden node
     * @param probEdge the probability for a new conenction
     * @param bFF true, net will be feed-forward, false, net will be recurrent
     * @throws GetPointerFailedException
     */
    public CMATWEANN(int numIn, int numOut, int numHid, double sigma, double sigmaMin, double probNode, double probEdge, boolean bFF) throws GetPointerFailedException {
        long pt = generateCMATWEANN(numIn, numOut, numHid, sigma, sigmaMin, probNode, probEdge, bFF);
        if(pt == 0) throw new GetPointerFailedException("Contructor: Received Handler is ZERO!");
        setPt(pt);

    }

    public native int getPopSize();

    /**
     * Sets the score of the net with the nnID
     * Beware, that the nets are sorted in ascending order.
     * For the best net (= highest score) to be first in order,
     * insert the score as a negative.
     *
     * @param nnID the ID of the net to set the score for
     * @param tmpScore the score
     */
    public native void setScore(int nnID, double tmpScore);

    /**
     * Produces the Offspring, based on the best nets in the population.
     * Call before any new evalution of the population
     */
    public native void produceOffspring();

    /**
     * Proceed the Generation, changing all variables
     * Call after every net is evaluated once.
     */
    public native void proceedGen();

    /**Call these if the CMA-TWEANN is no more needed
     *
     */
    public native void dispose();

    /**
     * Gets the NN from native Code and returns the pointer
     *
     * @param nnID
     * @return
     */
    private native long getNNNative(int nnID);

    /**
     * Constructs the CMA-TWEANN in native Code and returns the pointer
     * A high sigma in the beginning and a low in the end is recommended to avoid
     * local optima
     *
     * @param numIn number of input neurons
     * @param numOut number of output neurons
     * @param numHid initial number of hidden neurons
     * @param sigma "learning speed", the higher, the more curious is the net
     * @param sigmaMin sigma is changed during the course, this is the minimum
     * @param probNode the probability for a new hidden node
     * @param probEdge the probability for a new conenction
     * @param bFF true, net will be feed-forward, false, net will be recurrent
     * @return the handler
     */
    private native long generateCMATWEANN(int numIn, int numOut, int numHid, double sigma, double sigmaMin, double probNode, double probEdge, boolean bFF);

    /**
     * activate the net with nnID and inputs
     * @param nnID the net to activate
     * @param inputs array containing the inputs. client must check array size = numIn
     * @param outputSize the size of the output array (= numOut). needed for wrapping reasons
     * @return a double array containing the output
     */
    public native double[] activate(int nnID, double[] inputs, int outputSize);

    /**
     * activates the best net sofar
     * @param inputs array containing the inputs. client must check array size = numIn
     * @param outputSize the size of the output array (= numOut). needed for wrapping reasons
     * @return a double array containing the output
     */
    public native double[] activateBest(double[] inputs, int outputSize);

    /**
     * prints the connectionMatrix
     * -1 = no connection, number = connection,
     * row = neuron with its connections (order=> first input, then output, then all hidden)
     * prints the weight matrix, each column is a net of the population
     */
    public native void printNetInfos();

    /*GETTER AND SETTER BELOW*/
    private void setPt(long _pt) {
        this._pt = _pt;
    }

    public long getPt() {
        return _pt;
    }

    //load native lib
    static {
        System.loadLibrary("libjcmatweann");
    }

}

