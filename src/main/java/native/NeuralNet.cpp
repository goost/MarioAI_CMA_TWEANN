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

#include "de_goost_jcmatweann_NeuralNet.h"
#include "pointerMagic.h"
#include "cmatweann/nn.h"
//#include "myHeader.h"
using namespace goost;
jdoubleArray Java_de_goost_jcmatweann_NeuralNet_activate
        (JNIEnv* env, jobject obj, jdoubleArray inputsArray, jint outputSize) {
    NN* nnPt = getPointer<NN>(env, obj);
    //Transfer the input_jdoubleArray to a VectorXd for using as input
    jdouble* inputs = env->GetDoubleArrayElements(inputsArray, NULL);
    if (NULL == inputs) return NULL;
    jsize inputLength = env->GetArrayLength(inputsArray);
    VectorXd inputVector = VectorXd::Zero(inputLength);
    for(int cnt = 0; cnt < inputLength; cnt ++){
        inputVector[cnt] = inputs[cnt];
    }
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
