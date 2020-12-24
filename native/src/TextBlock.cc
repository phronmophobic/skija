#include <iostream>
#include <jni.h>
#include "interop.hh"
#include "TextBlock.hh"

static void unrefTextBlock(TextBlock* ptr) {
    ptr->unref();
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skija_TextBlock__1nGetFinalizer
  (JNIEnv* env, jclass jclass) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&unrefTextBlock));
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skija_TextBlock__1nGetTextBlob
  (JNIEnv* env, jclass jclass, jlong ptr) {
    TextBlock* instance = reinterpret_cast<TextBlock*>(static_cast<uintptr_t>(ptr));
    instance->fBlob->ref();
    return reinterpret_cast<jlong>(instance->fBlob.get());
}

extern "C" JNIEXPORT jobjectArray JNICALL Java_org_jetbrains_skija_TextBlock__1nGetRunBounds
  (JNIEnv* env, jclass jclass, jlong ptr) {
    TextBlock* instance = reinterpret_cast<TextBlock*>(static_cast<uintptr_t>(ptr));
    jobjectArray res = env->NewObjectArray(instance->fRunCount, skija::Rect::cls, nullptr);
    int idx = 0;
    for (auto line: instance->fLines) {
        for (auto run: line.fRuns) {
            env->SetObjectArrayElement(res, idx, skija::Rect::fromSkRect(env, run.fBounds));
            ++idx;
        }
    }
    return res;
}
