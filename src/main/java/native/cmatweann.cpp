/**
 * Copyright (c) 2014/11, Gleb Ostrowski, glebos at web dot de
 * All rights reserved.
 * <p>
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
 * <p>
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

#include "de_goost_jcmatweann_CMATWEANN.h"
#include "pointerMagic.h"
#include "cmatweann/cmat.h"
//#include "myHeader.h"
//#include "NeuralNet.cpp"

using namespace goost;


jint Java_de_goost_jcmatweann_CMATWEANN_getPopSize
  (JNIEnv* env, jobject obj){
    CMATWEANN* cmannPt = getPointer<CMATWEANN>(env, obj);
    return cmannPt->getPopSize();
  }

void Java_de_goost_jcmatweann_CMATWEANN_setScore
  (JNIEnv* env, jobject obj, jint nnID, jdouble tmpScore){
    CMATWEANN* cmannPt = getPointer<CMATWEANN>(env, obj);
    cmannPt->setScore(nnID, tmpScore);
  }

void Java_de_goost_jcmatweann_CMATWEANN_produceOffspring
  (JNIEnv* env, jobject obj) {
    CMATWEANN* cmannPt = getPointer<CMATWEANN>(env, obj);
    cmannPt->produceOffspring();
  }

void Java_de_goost_jcmatweann_CMATWEANN_proceedGen
  (JNIEnv* env, jobject obj) {
    CMATWEANN* cmannPt = getPointer<CMATWEANN>(env, obj);
    cmannPt->proceedGen();
  }

void Java_de_goost_jcmatweann_CMATWEANN_dispose
  (JNIEnv* env, jobject obj) {
    CMATWEANN* cmannPt = getPointer<CMATWEANN>(env, obj);
    setPointer<jlong>(env, obj, 0);
    delete cmannPt;
  }

jlong Java_de_goost_jcmatweann_CMATWEANN_getNNNative
  (JNIEnv* env, jobject obj, jint nnID) {
    CMATWEANN* cmannPt = getPointer<CMATWEANN>(env, obj);
    return reinterpret_cast<jlong> (cmannPt->getNN(nnID) );
  }

jlong Java_de_goost_jcmatweann_CMATWEANN_generateCMATWEANN
  (JNIEnv* env, jobject obj, jint numIn, jint numOut, jint numHid,
    jdouble sigma, jdouble sigmaMin, jdouble probNode, jdouble probEdge, jboolean bff) {

    return reinterpret_cast<jlong>(new CMATWEANN(numIn, numOut, numHid, sigma, sigmaMin, probNode, probEdge, bff));
    }

jdoubleArray activateHelper
        (JNIEnv* env, NN* nnPt , jdoubleArray inputsArray, jint outputSize){
          //Transfer the input_jdoubleArray to a VectorXd for using as input
          jdouble* inputs = env->GetDoubleArrayElements(inputsArray, NULL);
          if (NULL == inputs) return NULL;
          jsize inputLength = env->GetArrayLength(inputsArray);
          VectorXd inputVector = VectorXd::Zero(inputLength);
          for(int cnt = 0; cnt < inputLength; cnt ++){
            inputVector[cnt] = inputs[cnt];
          }
          env->ReleaseDoubleArrayElements(inputsArray, inputs, 0);
          //activate NeuralNet
          VectorXd outputVector = VectorXd::Zero(outputSize);
          nnPt->activate(inputVector, outputVector);

         //transfer the outputVectorXd to an jdoubleArray and return
          jdouble* outputs = new jdouble[outputSize];
          for(int cnt = 0; cnt < outputSize; cnt ++){
             outputs[cnt] = outputVector[cnt];
          }
          jdoubleArray outputsArray = env->NewDoubleArray(outputSize);  // allocate
          if (NULL == outputsArray) return NULL;
          env->SetDoubleArrayRegion(outputsArray, 0, outputSize, outputs);  // copy
          delete[] outputs;
          return outputsArray;
        }

jdoubleArray Java_de_goost_jcmatweann_CMATWEANN_activate
      (JNIEnv* env, jobject obj, jint nnID, jdoubleArray inputsArray, jint outputSize){
        NN* nnPt = getPointer<CMATWEANN>(env, obj)->getNN(nnID);
        return activateHelper(env,nnPt, inputsArray, outputSize);
      }

jdoubleArray Java_de_goost_jcmatweann_CMATWEANN_activateBest
       (JNIEnv* env, jobject obj, jdoubleArray inputsArray, jint outputSize){
         NN* nnPt = getPointer<CMATWEANN>(env, obj)->getBestNN();
         return activateHelper(env,nnPt, inputsArray, outputSize);
        }