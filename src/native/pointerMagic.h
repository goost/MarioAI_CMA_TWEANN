#ifndef POINTERMAGIC_H
#define POINTERMAGIC_H
/* Code is taken from
 * http://thebreakfastpost.com/2012/01/26/wrapping-a-c-library-with-jni-part-2/
 * Thanks for the great tutorial!
*/
namespace goost{
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

#include "jni.h"

jfieldID getPointerField(JNIEnv* env, jobject obj){
    jclass c = env->GetObjectClass(obj);
    //"J" for long
    return env->GetFieldID(c, "_pt", "J");
}

template <typename T>
T* getPointer(JNIEnv* env, jobject obj){
    jlong pointer = env->GetLongField(obj, getPointerField(env, obj));
    return reinterpret_cast<T*>(pointer);
}

template <typename T>
void setPointer(JNIEnv* env, jobject obj, T* t){
    jlong pointer = reinterpret_cast<jlong>(t);
    env->SetLongField(obj, getPointerField(env, obj), pointer);
}
}
#endif