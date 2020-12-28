#include <iostream>
#include <jni.h>
#include "interop.hh"
#include "SkShaper.h"
#include "TextLine.hh"

static void unrefTextLine(TextLine* ptr) {
    ptr->unref();
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skija_TextLine__1nGetFinalizer
  (JNIEnv* env, jclass jclass) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&unrefTextLine));
}

extern "C" JNIEXPORT jfloat JNICALL Java_org_jetbrains_skija_TextLine__1nGetAscent
  (JNIEnv* env, jclass jclass, jlong ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>(static_cast<uintptr_t>(ptr));
    return instance->fAscent;
}

extern "C" JNIEXPORT jfloat JNICALL Java_org_jetbrains_skija_TextLine__1nGetCapHeight
  (JNIEnv* env, jclass jclass, jlong ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>(static_cast<uintptr_t>(ptr));
    return instance->fCapHeight;
}

extern "C" JNIEXPORT jfloat JNICALL Java_org_jetbrains_skija_TextLine__1nGetDescent
  (JNIEnv* env, jclass jclass, jlong ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>(static_cast<uintptr_t>(ptr));
    return instance->fDescent;
}

extern "C" JNIEXPORT jfloat JNICALL Java_org_jetbrains_skija_TextLine__1nGetLeading
  (JNIEnv* env, jclass jclass, jlong ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>(static_cast<uintptr_t>(ptr));
    return instance->fLeading;
}

extern "C" JNIEXPORT jfloat JNICALL Java_org_jetbrains_skija_TextLine__1nGetWidth
  (JNIEnv* env, jclass jclass, jlong ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>(static_cast<uintptr_t>(ptr));
    return instance->fWidth;
}

extern "C" JNIEXPORT jfloat JNICALL Java_org_jetbrains_skija_TextLine__1nGetHeight
  (JNIEnv* env, jclass jclass, jlong ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>(static_cast<uintptr_t>(ptr));
    return -instance->fAscent + instance->fDescent + instance->fLeading;
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skija_TextLine__1nGetTextBlob
  (JNIEnv* env, jclass jclass, jlong ptr) {
    TextLine* instance = reinterpret_cast<TextLine*>(static_cast<uintptr_t>(ptr));
    instance->fBlob->ref();
    return reinterpret_cast<jlong>(instance->fBlob.get());
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skija_TextLine__1nGetOffsetAtCoord
  (JNIEnv* env, jclass jclass, jlong ptr, jfloat x) {
    TextLine* instance = reinterpret_cast<TextLine*>(static_cast<uintptr_t>(ptr));

    for (auto& run: instance->fRuns) {
        const SkPoint* pos = run.fPos;
        SkScalar glyphLeft = pos[0].fX;
        uint32_t idx = 0;
        for (; idx < run.fGlyphCount; ++idx) {
            SkScalar glyphRight = idx < run.fGlyphCount - 1 ? pos[idx + 1].fX : run.fPosition + run.fWidth;

            if (SkScalarNearlyEqual(glyphRight, glyphLeft))
                continue;

            SkScalar glyphCenter = (glyphLeft + glyphRight) / 2;
            if (x < glyphCenter)
                return run.fClusters[idx];

            glyphLeft = glyphRight;
        }
    }

    return instance->fRuns.back().fEnd16;
}

// extern "C" JNIEXPORT jobject JNICALL Java_org_jetbrains_skija_TextBlob__1nGetCoordAtOffset
//   (JNIEnv* env, jclass jclass, jlong ptr, jint target16) {
//     SkTextBlob* instance = reinterpret_cast<SkTextBlob*>(static_cast<uintptr_t>(ptr));
//     SkTextBlob::Iter iter(*instance);
//     SkTextBlob::Iter::Run run;
//     uint32_t runStart16 = 0;
//     SkScalar prevGlyphLeft = 0;
//     SkScalar prevGlyphBaseline = 0;

//     while (iter.next(&run)) {
//         auto runRecord = reinterpret_cast<const RunRecordClone*>(run.fGlyphIndices) - 1;
//         if (runRecord->positioning() != 2 || !runRecord->isExtended()) // kFull_Positioning
//             return nullptr;
//         const SkFont& font = runRecord->fFont;
//         skija::UtfIndicesConverter conv(runRecord->textBuffer(), runRecord->textSize());
//         SkScalar* posBuffer = runRecord->posBuffer();
//         uint32_t* clusterBuffer = runRecord->clusterBuffer();
//         uint32_t offset16, prevOffset16 = 0;
        
//         for (int glyphIdx = 0; glyphIdx < run.fGlyphCount + 1; ++glyphIdx) {
//             uint32_t offset8;
//             SkScalar glyphLeft;
//             if (glyphIdx < run.fGlyphCount) {
//                 offset8 = clusterBuffer[glyphIdx] - clusterBuffer[0];
//                 glyphLeft = posBuffer[glyphIdx * 2];
//             } else {
//                 // one step after last glyph
//                 offset8 = runRecord->textSize();
//                 SkScalar lastGlyphWidth;
//                 font.getWidths(&run.fGlyphIndices[run.fGlyphCount - 1], 1, &lastGlyphWidth);
//                 glyphLeft = prevGlyphLeft + lastGlyphWidth;
//             }

//             offset16 = conv.from8To16(offset8);

//             // between prev and this: approximate
//             if (target16 - runStart16 < offset16) {
//                 float ratio = (float) ((target16 - runStart16) - prevOffset16) / (float) (offset16 - prevOffset16);
//                 return skija::Point::fromSkPoint(env, {(glyphLeft - prevGlyphLeft) * ratio + prevGlyphLeft, posBuffer[1]});
//             }

//             // exactly this
//             if (target16 - runStart16 == offset16)
//                 return skija::Point::fromSkPoint(env, {glyphLeft, posBuffer[1]});

//             prevGlyphLeft = glyphLeft;
//             prevOffset16 = offset16;
//         }

//         // to next run
//         runStart16 += offset16;
//         prevGlyphBaseline = posBuffer[1];
//     }
    
//     // no run matched, should not happen
//     std::cout << "WARN: no run matched " << prevGlyphLeft << " " << prevGlyphBaseline << std::endl;
//     return nullptr; // skija::Point::fromSkPoint(env, {prevGlyphLeft, prevGlyphBaseline});
// }