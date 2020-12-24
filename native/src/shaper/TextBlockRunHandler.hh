#include <iostream>
#include <jni.h>
#include "../interop.hh"
#include "../TextBlock.hh"
#include "SkShaper.h"
#include "SkTextBlob.h"

class TextBlockRunHandler: public SkShaper::RunHandler {
public:
    TextBlockRunHandler(const char* utf8Text, SkPoint offset):
      fOffset(offset),
      fBlock(new TextBlock()) {
    }

    void beginLine() override {
        fCurrentPosition = fOffset;
        fLineAscent = 0;
        fLineDescent = 0;
        fLineLeading = 0;
        fBlock->fLines.emplace_back();
    }

    void runInfo(const RunInfo& info) override {
        SkFontMetrics metrics;
        info.fFont.getMetrics(&metrics);
        fLineAscent  = std::max(fLineAscent, -metrics.fAscent);
        fLineDescent = std::max(fLineDescent, metrics.fDescent);
        fLineLeading = std::max(fLineLeading, metrics.fLeading);
    }

    void commitRunInfo() override {
        fCurrentPosition.fY += fLineAscent;
    }

    Buffer runBuffer(const RunInfo& info) override {
        const SkTextBlobBuilder::RunBuffer& buffer = fBuilder.allocRunPos(info.fFont, info.glyphCount);
        TextBlock::Line& line = fBlock->fLines.back();
        line.fRuns.emplace_back(
            info.fFont,
            info.fBidiLevel,
            SkRect::MakeXYWH(fCurrentPosition.fX, fCurrentPosition.fY - fLineAscent, info.fAdvance.fX, fLineAscent + fLineDescent),
            info.utf8Range.fBegin, // TODO convert to 16
            info.utf8Range.fSize,  // TODO convert to 16
            info.glyphCount);
        fBlock->fRunCount++;
        TextBlock::Run& run = line.fRuns.back();
        return {
            buffer.glyphs,
            buffer.points(),
            nullptr,
            run.fClusters.data(),
            fCurrentPosition
        };
    }

    void commitRunBuffer(const RunInfo& info) override {
        // for (int i = 0; i < info.glyphCount; ++i)
        //     fClusters[i] -= info.utf8Range.begin(); // TODO convert to 16
        fCurrentPosition += info.fAdvance;
    }

    void commitLine() override {
        fOffset += { 0, fLineAscent + fLineDescent + fLineLeading };
    }

    sk_sp<TextBlock> makeBlock() {
        sk_sp<SkTextBlob> blob = fBuilder.make();
        SkTextBlob::Iter iter(*blob);
        SkTextBlob::Iter::Run blobRun;
        for (auto line: fBlock->fLines) {
            for (auto run: line.fRuns) {
                iter.next(&blobRun); // advance SkTextBlob::Iter
                run.fGlyphs = blobRun.fGlyphIndices;
                // positions directly after glyphsIndices, aligned
                //    -----------------------------------------------------------------------------
                //   | SkTextBlob | RunRecord | Glyphs[] | Pos[] | RunRecord | Glyphs[] | Pos[] | ...
                //    -----------------------------------------------------------------------------
                run.fPos = reinterpret_cast<const SkPoint*>(reinterpret_cast<const uint8_t*>(blobRun.fGlyphIndices) +
                                                            SkAlign4(blobRun.fGlyphCount * sizeof(uint16_t)));
            }
        }
        fBlock->fBlob = blob;
        return fBlock;
    }

private:
    sk_sp<TextBlock> fBlock;
    SkTextBlobBuilder fBuilder;

    // const char* const fUtf8Text;
    // uint32_t* fClusters;
    // int fClusterOffset;
    // int fGlyphCount;
    SkScalar fLineAscent;
    SkScalar fLineDescent;
    SkScalar fLineLeading;
    SkPoint fCurrentPosition;
    SkPoint fOffset;
};