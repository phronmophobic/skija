#include "SkCanvas.h"
#include "SkFont.h"
#include "SkPoint.h"
#include "SkRefCnt.h"
#include "SkTextBlob.h"

class TextLine: public SkNVRefCnt<TextLine> {
public:
    struct Run {
        SkFont   fFont;
        uint8_t  fBidiLevel;
        SkScalar fPosition;
        SkScalar fWidth;
        // size_t   fBegin16;
        // size_t   fSize16;
        // SkScalar fBaseline;
        size_t    fGlyphCount;
        const uint16_t* fGlyphs;
        const SkPoint*  fPos;
        std::vector<uint32_t> fClusters;
        size_t fEnd16;

        Run(const SkFont& font,
            uint8_t bidiLevel,
            SkScalar position,
            SkScalar width,
            size_t glyphCount):
          fFont(font),
          fBidiLevel(bidiLevel),
          fPosition(position),
          fWidth(width),
          fGlyphCount(glyphCount),
          fGlyphs(nullptr),
          fPos(nullptr),
          fClusters(glyphCount) {
        }
    };

    SkScalar fAscent = 0;
    SkScalar fCapHeight = 0;
    SkScalar fDescent = 0;
    SkScalar fLeading = 0;
    SkScalar fWidth = 0;
    std::vector<Run> fRuns;
    sk_sp<SkTextBlob> fBlob;
};