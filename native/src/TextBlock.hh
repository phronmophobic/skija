#include "SkCanvas.h"
#include "SkFont.h"
#include "SkPoint.h"
#include "SkRefCnt.h"
#include "SkTextBlob.h"

class TextBlock: public SkNVRefCnt<TextBlock> {
public:
    struct Run {
        SkFont   fFont;
        uint8_t  fBidiLevel;
        SkRect   fBounds;
        size_t   fBegin16;
        size_t   fSize16;
        // SkScalar fBaseline;
        size_t    fGlyphCount;
        const uint16_t* fGlyphs;
        const SkPoint*  fPos;
        std::vector<uint32_t> fClusters;

        Run(const SkFont& font,
            uint8_t bidiLevel,
            SkRect bounds,
            size_t begin16,
            size_t size16,
            size_t glyphCount):
          fFont(font),
          fBidiLevel(bidiLevel),
          fBounds(bounds),
          fBegin16(begin16),
          fSize16(size16),
          fGlyphCount(glyphCount),
          fGlyphs(nullptr),
          fPos(nullptr),
          fClusters(glyphCount) {
        }
    };

    struct Line {
        // SkScalar fAscent;
        // SkScalar fBaseline;
        // SkScalar fDescent;
        // SkScalar fLeading;
        std::vector<Run> fRuns;
    };

    size_t fRunCount;
    std::vector<Line> fLines;
    sk_sp<SkTextBlob> fBlob;
};