/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class de_goost_jcmatweann_CMATWEANN */

#ifndef _Included_de_goost_jcmatweann_CMATWEANN
#define _Included_de_goost_jcmatweann_CMATWEANN
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     de_goost_jcmatweann_CMATWEANN
 * Method:    getPopSize
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_de_goost_jcmatweann_CMATWEANN_getPopSize
  (JNIEnv *, jobject);

/*
 * Class:     de_goost_jcmatweann_CMATWEANN
 * Method:    setScore
 * Signature: (ID)V
 */
JNIEXPORT void JNICALL Java_de_goost_jcmatweann_CMATWEANN_setScore
  (JNIEnv *, jobject, jint, jdouble);

/*
 * Class:     de_goost_jcmatweann_CMATWEANN
 * Method:    produceOffspring
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_de_goost_jcmatweann_CMATWEANN_produceOffspring
  (JNIEnv *, jobject);

/*
 * Class:     de_goost_jcmatweann_CMATWEANN
 * Method:    proceedGen
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_de_goost_jcmatweann_CMATWEANN_proceedGen
  (JNIEnv *, jobject);

/*
 * Class:     de_goost_jcmatweann_CMATWEANN
 * Method:    dispose
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_de_goost_jcmatweann_CMATWEANN_dispose
  (JNIEnv *, jobject);

/*
 * Class:     de_goost_jcmatweann_CMATWEANN
 * Method:    getNNNative
 * Signature: (I)J
 */
JNIEXPORT jlong JNICALL Java_de_goost_jcmatweann_CMATWEANN_getNNNative
  (JNIEnv *, jobject, jint);

/*
 * Class:     de_goost_jcmatweann_CMATWEANN
 * Method:    generateCMATWEANN
 * Signature: (IIIDDDDZ)J
 */
JNIEXPORT jlong JNICALL Java_de_goost_jcmatweann_CMATWEANN_generateCMATWEANN
  (JNIEnv *, jobject, jint, jint, jint, jdouble, jdouble, jdouble, jdouble, jboolean);

/*
 * Class:     de_goost_jcmatweann_CMATWEANN
 * Method:    activate
 * Signature: (I[DI)[D
 */
JNIEXPORT jdoubleArray JNICALL Java_de_goost_jcmatweann_CMATWEANN_activate
  (JNIEnv *, jobject, jint, jdoubleArray, jint);

/*
 * Class:     de_goost_jcmatweann_CMATWEANN
 * Method:    activateBest
 * Signature: ([DI)[D
 */
JNIEXPORT jdoubleArray JNICALL Java_de_goost_jcmatweann_CMATWEANN_activateBest
  (JNIEnv *, jobject, jdoubleArray, jint);

#ifdef __cplusplus
}
#endif
#endif
