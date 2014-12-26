package de.gost.scario
import breeze.linalg._

/**
 * Copyright (c) 2014/12, Gleb Ostrowski, glebos at web dot de
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1) Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2) Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3) Neither the name of the copyright holder nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
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
class NeuralNetwork( _numIn: Int, _numOut: Int, _numHid : Int = 0, _connectionMatrix: DenseMatrix[Int], var weight : DenseVector[Double], _activation :Double => Double ) {
  private var _node = DenseVector.zeros[Double](_numOut + _numHid )
  private var _delay = DenseVector.zeros[Double](_numOut + _numHid )

  def activate(input: Array[Double]) = {
    //TODO if FeedForward
    for( nodeCnt <- 0 until _numOut + _numHid; inputCnt <- 0 until _numIn; innerCnt <- 0 until _numOut + _numHid){
      _node(nodeCnt) = 0
      if (_connectionMatrix(inputCnt, nodeCnt) != -1 ){ //TODO change nodeCnt and inputCnt? Same for line below
        _node(nodeCnt) += weight(_connectionMatrix(inputCnt, nodeCnt)) * input(inputCnt)
      }

      if (_connectionMatrix(innerCnt + _numIn, nodeCnt) != -1 ){
        _node(nodeCnt) += weight(_connectionMatrix(innerCnt + _numIn, nodeCnt)) * _delay(innerCnt)
      }
    }

    for(i <- 0 until _numOut + _numHid){
      _node(i) = _activation(_node(i))
      _delay(i) = _node(i)
    }
    //TODO is the size always _numOut?
    val output = Array[Double](_numOut)
    for(i <- 0 until _numOut) {
      output(i) = _node(i)
    }
    output
  }

  def reset() {  _delay = DenseVector.zeros[Double](_delay.size ) }

  def copy(numIn: Int = _numIn, numOut: Int = _numOut, numHid : Int = _numHid, connectionMatrix: DenseMatrix[Int] = _connectionMatrix, weight : DenseVector[Double] = weight, activation :Double => Double = _activation) = {
    val tmp = new NeuralNetwork(numIn,numOut,numHid,connectionMatrix,weight,activation)
    tmp._node = _node
    tmp._delay = _delay
    tmp
  }

}

object test {

}
