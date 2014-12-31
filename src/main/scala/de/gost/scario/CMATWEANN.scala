package de.gost.scario
import scala.math._
import scala.util.Random
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
class CMATWEANN(_numIn: Int, _numOut:Int, private var _numHid:Int, private var _sigma:Double,_sigmaMin:Double,_probNode:Double,_probEdge:Double, _activation : Double => Double ) {

  def this (_numIn: Int, _numOut:Int,  _numHid:Int, _sigma:Double,_sigmaMin:Double,_probNode:Double,_probEdge:Double) = this (_numIn, _numOut, _numHid,_sigma,_sigmaMin,_probNode,_probEdge, x => x / (1+abs(x)))
  private var _connectionMatrix = DenseMatrix.fill(_numIn+_numOut+_numHid,_numOut+_numHid) {-1}
  private var _nctr = 0
//S
  private var first = true
  private var _deviateAvailable = false
  private var _storedDeviate = 0.0
  private var _lambda = 0
  private var _mu = 0
  private var _cc = 0.0
  private var _ccov = 0.0
  private var _wmu  = DenseVector.zeros[Double](1)
  private var _cw = 0.0
  private var _mueff = 0.0
  private var _mucov = 0.0
  private var _csigma = 0.0
  private var _dsigma = 0.0
  private var _ccu = 0.0
  private var _csigmau = 0.0
  private var _chin = 0.0
  private var _sigmaInit = _sigma
  private var _score = DenseVector.zeros[Double](1)
  private var _rank = DenseVector.zeros[Int](1)


  // setup connections from input to hidden-output nodes
  for(inputCnt <- 0 until _numIn; otherCnt <- 0 until _numHid + _numOut){
    _connectionMatrix(inputCnt,otherCnt) = _nctr
    _nctr += 1
  }
  for(innerCnt <- 0 until _numHid + _numOut; otherCnt <- 0 until _numHid+ _numOut){
    _connectionMatrix(innerCnt + _numIn, otherCnt) = _nctr
    _nctr += 1
  }


  private var _dim = _nctr
  initializeCMAParameters()

  private var _pc = DenseVector.zeros[Double](_dim)
  private var _psigma = DenseVector.zeros[Double](_dim)
  private var _C = diag(DenseVector.fill[Double](_dim) {1})
  private var _xbase = DenseVector.zeros[Double](_dim)
  private var _zbase = DenseVector.zeros[Double](_dim)

  private var _X = DenseMatrix.zeros[Double](_dim, _lambda)
  private var _Z = DenseMatrix.zeros[Double](_dim, _lambda)
  private var _B = diag(DenseVector.fill[Double](_dim){1})
  private var _D = diag(DenseVector.fill[Double](_dim){1})
  private var _xvec = DenseVector.zeros[Double](_dim)
  private var _nn = new NeuralNetwork(_numIn, _numOut, _numHid, _connectionMatrix, _xbase, _activation)

  println()
  def getPopSize = _lambda

  def getNN(nnID: Int) : NeuralNetwork = {
    _nn.weight = _X(::,nnID)
    _nn.reset()
    _nn
  }

  def setScore(nnID: Int, tmpScore: Double): Unit = {
    _score(nnID) = tmpScore
  }

  def produceOffspring() : Unit = {
    for(i <- 0 until _lambda){
      for(j <- 0 until _dim) {
        _Z(j, i) = randn(0, 1)
      }
      _X(::,i) := _xbase +  _sigma :* _B * _D *_Z(::,i)
    }
  }


  def proceedGen(): Unit = {
    // sort z & x
    // 1. reveal rank
    var bestscore = 0.0
    rankNeuralNetworks()
    //2. weighted sum
    _xbase = DenseVector.zeros[Double](_dim)
    _zbase = DenseVector.zeros[Double](_dim)
    for(i <- 0 until _mu){
      _xbase += _X(::, _rank(i)) * _wmu(i)
      _zbase += _Z(::, _rank(i)) * _wmu(i)
    }

    //update pc and c
    _pc = _pc * (1-_cc) + _ccu *sqrt(_mueff) :* _B * _D * _zbase

    //rank-mu update
    var Zmu = DenseMatrix.zeros[Double](_dim, _dim)
    for(i <- 0 until _mu){
      Zmu += _Z(::,_rank(i)) * (_Z(::, _rank(i)).t)*_wmu(i)
    }

    val BDt = (_B * _D).t
    Zmu = _B*_D*Zmu*BDt
    //rank-mu update
    _C = _C * (1-_ccov) + (_pc * _pc.t * (1/_mucov)  + (1-1/_mucov):* Zmu) * _ccov
    //TODO DEBUG
    //println(_C)

    //update psigma and sigma
    _psigma = _psigma*(1-_csigma) + _csigma * sqrt(_mueff) :* _B * _zbase
    _sigma = _sigma * exp(_csigma * (norm(_psigma)-_chin)/(_dsigma*_chin))

    if(bestscore < _score(_rank(0))) {
      bestscore = _score(_rank(0))
    }
    val rand_mut = Random.nextDouble()
    if(rand_mut < _probNode){
      _connectionMatrix = expandMiC(_connectionMatrix,1,1,-1)
      _connectionMatrix(abs(Random.nextInt()) % (_numIn + _numHid + _numOut),_numOut+_numHid) = _nctr
      _nctr += 1
      _connectionMatrix(_numIn+_numOut+_numHid, abs(Random.nextInt()) % (_numOut + _numHid)) = _nctr
      _nctr += 1
      updateCMAParameters(2,1)
      _nn = new NeuralNetwork(_numIn,_numOut,_numHid,_connectionMatrix,_xbase,_activation)
    }else if(rand_mut < (_probNode + _probEdge) && breeze.linalg.min(_connectionMatrix) < 0){
      def addRandomConnection {
        var randRow = 0
        var randCol = 0
        while (true) {
          randRow = Random.nextInt() % (_numHid + _numIn + _numOut)
          randCol = Random.nextInt() % (_numHid + _numOut)
          if (_connectionMatrix(randRow, randCol) == -1) {
            _connectionMatrix(randRow, randCol) = _nctr
            _nctr += 1
            return
          }
        }
      }
      addRandomConnection //TODO rewrite (pretty) or unpretty (break) if problem arise
      updateCMAParameters(1,0)
      _nn = new NeuralNetwork(_numIn,_numOut,_numHid,_connectionMatrix,_xbase,_activation)
    }
      val svd.SVD(u, s, v) = svd(_C)
      _B = u
      _D = diag(s)
      _D = _D.map {sqrt _ }

      //lower bound on variance
      if (_sigma * _D(_dim - 1, _dim - 1) < _sigmaMin * _sigmaInit) {
        _sigma = _sigmaMin * _sigmaInit / _D(_dim - 1, _dim - 1)
      }
  }

  def getBestNN = {
    rankNeuralNetworks()
    getNN(_rank(11))
  }

  private def rankNeuralNetworks(): Unit = {
    val rankScore  = new Array[(Double, Int)](_lambda)
    for(i <- 0 until _lambda) {
      rankScore(i) = (_score(i), i)
    }
    val rankScoreSorted = rankScore.sortBy(_._1)
    println("Sorted Rank (last should be best):")
    rankScoreSorted.foreach(tupel => println(s"Tupel: $tupel"))
    for(i <- 0 until _lambda){ //TODO the sorting could be wrong
      _rank(i) = rankScoreSorted(rankScoreSorted.length -1-i)._2
    }
  }



  private def initializeCMAParameters() : Unit = {
    _lambda = scala.math.max( 4 + (3*log(_dim)).toInt, 5 )
    _mu = _lambda/2
    _cc = 4.0 /(4.0 + _dim.toDouble)
    _ccov = 2.0 / pow(_dim + sqrt(2.0 ), 2 )

    _wmu = DenseVector.zeros[Double](_mu)
    for(i <- 0 until _mu){
      _wmu(i) = log((_lambda+1)/2) + log(i+1)
    }
    _wmu = _wmu :/ breeze.linalg.sum(_wmu)
    //_wmu.map{x => x / breeze.linalg.sum(_wmu)}
    //println("wmu") //TODO DEBUG
    //println(_wmu)
    _cw = breeze.linalg.sum(_wmu) / norm(_wmu)

    _mueff = 1 / pow( norm(_wmu),2)
    _mucov = _mueff
    _csigma = (_mueff + 2) / (_dim + _mueff + 2)
    _dsigma = 1+ 2 * scala.math.max (0.0, sqrt( (_mueff -1.0) / (_dim + 1.0))-1.0) + _csigma
    _chin = _dim * 0.5 * (1-1/(4*_dim)+1/(21*_dim*_dim))

    _score = DenseVector.zeros[Double](_lambda)
    _rank = DenseVector.zeros[Int](_lambda)

    if(first) {
      _X = DenseMatrix.zeros[Double](_dim, _lambda)
      _Z = DenseMatrix.zeros[Double](_dim, _lambda)
      first = false
    }
  }

  private def updateCMAParameters(ddiff: Int, ndiff: Int ) : Unit = {
    _dim += ddiff
    _numHid += ndiff
    initializeCMAParameters()
    _pc = expandVZ(_pc, ddiff)
    _psigma = expandVZ(_psigma,ddiff)
    _C = expandM(_C, ddiff, ddiff)
    _C(_C.rows - ddiff to _C.rows -1, _C.cols -ddiff to _C.cols -1) := diag(DenseVector.fill[Double](ddiff){pow(_sigmaInit / _sigma, 2)})
    _xbase = expandVZ(_xbase,ddiff)
    _zbase = expandVZ(_zbase,ddiff)
    _X= expandM(_X,ddiff,_lambda - _X.cols)
    _Z = expandM(_Z,ddiff,_lambda - _Z.cols)
    _B = expandM(_B,ddiff,ddiff)
    _D =expandM(_D,ddiff,ddiff)
    _xvec = expandVZ(_xvec,ddiff)

  }

  private def expandM(m: DenseMatrix[Double], rdiff: Int, cdiff : Int): DenseMatrix[Double]  ={
    val newM = DenseMatrix.zeros[Double](m.rows + rdiff, m.cols + cdiff)
    newM(0 until m.rows, 0 until m.cols) := m
    newM
  }

  private def expandMiC(m: DenseMatrix[Int], rdiff: Int, cdiff : Int, value: Int): DenseMatrix[Int]  ={
    val newM = DenseMatrix.fill[Int](m.rows + rdiff, m.cols + cdiff){value}
    newM(0 until m.rows, 0 until m.cols) := m
    newM
  }

  private def expandVZ(v: DenseVector[Double], sdiff: Int) : DenseVector[Double] = {
    val newV = DenseVector.zeros[Double](v.size + sdiff)
    newV(0 until v.size) := v
    newV
  }

  private def expandVC(v: DenseVector[Double], sdiff: Int, value: Int) : DenseVector[Double] = {
    val newV = DenseVector.fill[Double](v.size + sdiff){value}
    newV(0 until v.size) := v
    newV
  }

  private def randn(mu: Double, sigma: Double) :Double = {
    if(!_deviateAvailable) {
      val dist = sqrt (-2.0 * log(Random.nextDouble()))
      val angle = 2.0 * Pi * Random.nextDouble()
      _storedDeviate = dist * cos(angle)
      _deviateAvailable = true
      dist * sin(angle) * sigma + mu
    }
    else {
      _deviateAvailable = false
      _storedDeviate * sigma + mu

    }
  }

}
