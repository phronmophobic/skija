package org.jetbrains.skija;

import java.lang.ref.*;
import org.jetbrains.annotations.*;
import org.jetbrains.skija.impl.*;
import org.jetbrains.skija.shaper.*;

public class TextLine extends Managed {
    static { Library.staticLoad(); }
    
    @ApiStatus.Internal
    public TextLine(long ptr) {
        super(ptr, _FinalizerHolder.PTR);
    }

    @ApiStatus.Internal
    public static class _FinalizerHolder {
        public static final long PTR = _nGetFinalizer();
    }

    @Nullable @Contract("_, _ -> new")
    public static TextLine make(String text, Font font) {
        return make(text, font, null, true);
    }

    @Nullable @Contract("_, _, _, _ -> new")
    public static TextLine make(String text, Font font, @Nullable FontFeature[] features, boolean leftToRight) {
        try (var shaper = Shaper.makeShapeDontWrapOrReorder();) {
            return shaper.shapeLine(text, font, features, leftToRight);
        }
    }

    public float getAscent() {
        Stats.onNativeCall();
        try {
            return _nGetAscent(_ptr);
        } finally {
            Reference.reachabilityFence(this);
        }
    }

    public float getCapHeight() {
        Stats.onNativeCall();
        try {
            return _nGetCapHeight(_ptr);
        } finally {
            Reference.reachabilityFence(this);
        }
    }

    public float getDescent() {
        Stats.onNativeCall();
        try {
            return _nGetDescent(_ptr);
        } finally {
            Reference.reachabilityFence(this);
        }
    }

    public float getLeading() {
        Stats.onNativeCall();
        try {
            return _nGetLeading(_ptr);
        } finally {
            Reference.reachabilityFence(this);
        }
    }

    public float getWidth() {
        Stats.onNativeCall();
        try {
            return _nGetWidth(_ptr);
        } finally {
            Reference.reachabilityFence(this);
        }
    }

    public float getHeight() {
        Stats.onNativeCall();
        try {
            return _nGetHeight(_ptr);
        } finally {
            Reference.reachabilityFence(this);
        }
    }

    public TextBlob getTextBlob() {
        Stats.onNativeCall();
        try {
            return new TextBlob(_nGetTextBlob(_ptr));
        } finally {
            Reference.reachabilityFence(this);
        }
    }

    /**
     * @param   x coordinate in px
     * @return  UTF-16 offset of glyph
     */
    public int getOffsetAtCoord(float x) {
        try {
            Stats.onNativeCall();
            return _nGetOffsetAtCoord(_ptr, x);
        } finally {
            Reference.reachabilityFence(this);
        }
    }

    /**
     * @paramt  offset UTF-16 character offset
     * @return  glyph coordinate
     */
    public float getCoordAtOffset(int offset) {
        try {
            Stats.onNativeCall();
            return _nGetCoordAtOffset(_ptr, offset);
        } finally {
            Reference.reachabilityFence(this);
        }
    }

    @ApiStatus.Internal public static native long  _nGetFinalizer();
    @ApiStatus.Internal public static native float _nGetAscent(long ptr);
    @ApiStatus.Internal public static native float _nGetCapHeight(long ptr);
    @ApiStatus.Internal public static native float _nGetDescent(long ptr);
    @ApiStatus.Internal public static native float _nGetLeading(long ptr);
    @ApiStatus.Internal public static native float _nGetWidth(long ptr);
    @ApiStatus.Internal public static native float _nGetHeight(long ptr);
    @ApiStatus.Internal public static native long  _nGetTextBlob(long ptr);
    @ApiStatus.Internal public static native int   _nGetOffsetAtCoord(long ptr, float x);
    @ApiStatus.Internal public static native float _nGetCoordAtOffset(long ptr, int offset);
}