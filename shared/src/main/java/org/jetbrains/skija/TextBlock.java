package org.jetbrains.skija;

import java.lang.ref.*;
import org.jetbrains.annotations.*;
import org.jetbrains.skija.impl.*;

public class TextBlock extends Managed {
    static { Library.staticLoad(); }
    
    @ApiStatus.Internal
    public TextBlock(long ptr) {
        super(ptr, _FinalizerHolder.PTR);
    }

    @ApiStatus.Internal
    public static class _FinalizerHolder {
        public static final long PTR = _nGetFinalizer();
    }

    public TextBlob getTextBlob() {
        Stats.onNativeCall();
        try {
            return new TextBlob(_nGetTextBlob(_ptr));
        } finally {
            Reference.reachabilityFence(this);
        }
    }

    public Rect[] getRunBounds() {
        Stats.onNativeCall();
        try {
            return _nGetRunBounds(_ptr);
        } finally {
            Reference.reachabilityFence(this);
        }
    }

    @ApiStatus.Internal public static native long _nGetFinalizer();
    @ApiStatus.Internal public static native long _nGetTextBlob(long ptr);
    @ApiStatus.Internal public static native Rect[] _nGetRunBounds(long ptr);
}