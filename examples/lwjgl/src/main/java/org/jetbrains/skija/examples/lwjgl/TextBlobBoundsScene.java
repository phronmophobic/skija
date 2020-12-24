package org.jetbrains.skija.examples.lwjgl;

import java.util.*;
import java.util.stream.*;
import org.jetbrains.skija.*;
import org.jetbrains.skija.shaper.*;

public class TextBlobBoundsScene extends Scene {
    private Paint fill = new Paint().setColor(0xFFCC3333);
    private Paint blueFill = new Paint().setColor(0xFF3333CC);
    private Paint selectionFill = new Paint().setColor(0x80b3d7ff);
    private Paint stroke = new Paint().setColor(0x803333CC).setMode(PaintMode.STROKE).setStrokeWidth(1);
    private Font inter9 = new Font(inter, 9).setSubpixel(true);
    private Font inter18 = new Font(inter, 36).setSubpixel(true);
    private Font zapfino13 = new Font(FontMgr.getDefault().matchFamilyStyle("Zapfino", FontStyle.NORMAL), 13);
    private Font emoji18 = new Font(FontMgr.getDefault().matchFamilyStyle("Apple Color Emoji", FontStyle.NORMAL), 36);

    public TextBlobBoundsScene() {
        _variants = new String[] { "Block", "Tight", "Loose" };
    }
    
    public float drawLine(Canvas canvas, String text, Font font, Point cursor) {
        return drawLine(canvas, text, font, Float.POSITIVE_INFINITY, cursor);
    }

    public float drawLine(Canvas canvas, String text, Font font, float width, Point cursor) {
        return drawLine(canvas, new String[] { text }, font, width, cursor);
    }

    public float drawLine(Canvas canvas, String[] texts, Font font, Point cursor) {
        return drawLine(canvas, texts, font, Float.POSITIVE_INFINITY, cursor);
    }

    public Point drawTable(Canvas canvas, Shaper shaper, String[] data) {
        float leftWidth = 0;
        float leftHeight = 0;
        for (int i = 0; i < data.length; i += 2) {
            try (var blob = shaper.shape(data[i], inter9, Float.POSITIVE_INFINITY); ) {
                canvas.drawTextBlob(blob, 0, leftHeight, inter9, blueFill);
                var bounds = blob.getBlockBounds();
                leftWidth = Math.max(leftWidth, bounds.getWidth());
                leftHeight += bounds.getHeight();
            }
        }

        float rightWidth = 0;
        float rightHeight = 0;
        for (int i = 1; i < data.length; i += 2) {
            try (var blob = shaper.shape(data[i], inter9, Float.POSITIVE_INFINITY); ) {
                canvas.drawTextBlob(blob, leftWidth + 5, rightHeight, inter9, blueFill);
                var bounds = blob.getBlockBounds();
                rightWidth = Math.max(rightWidth, bounds.getWidth());
                rightHeight += bounds.getHeight();
            }
        }
        return new Point(leftWidth + 5 + rightWidth, Math.max(leftHeight, rightHeight));
    }

    public float drawLine(Canvas canvas, String[] texts, Font font, float width, Point cursor) {
        float bottom = 0;
        canvas.save();
        for (var text: texts) {
            try (var shaper = Shaper.makeShapeThenWrap();
                 var blob = shaper.shape(text, font, width);)
            {
                Rect bounds;
                if ("Block".equals(variantTitle()))
                    bounds = blob.getBlockBounds();
                else if ("Tight".equals(variantTitle()))
                    bounds = blob.getTightBounds();
                else // if ("Loose".equals(variantTitle()))
                    bounds = blob.getBounds();
                float firstBaseline = blob.getFirstBaseline();
                float lastBaseline = blob.getLastBaseline();
                    
                canvas.translate(-bounds.getLeft(), 0);
                cursor = cursor.offset(bounds.getLeft(), 0);
                int offset = blob.getOffsetAtCoord(cursor.getX(), cursor.getY());

                // origin
                canvas.drawCircle(0, 0, 3, stroke);

                // selection
                Point coord = blob.getCoordAtOffset(offset);
                canvas.drawRect(Rect.makeLTRB(bounds.getLeft(), bounds.getTop(), coord.getX(), bounds.getBottom()), selectionFill);
                
                // bounds
                canvas.drawRect(bounds, stroke);
                canvas.drawLine(bounds.getLeft(), firstBaseline, bounds.getRight(), firstBaseline, stroke);
                canvas.drawLine(bounds.getLeft(), lastBaseline, bounds.getRight(), lastBaseline, stroke);

                // text
                canvas.drawTextBlob(blob, 0, 0, font, fill);

                // coords
                for (int i = 0; i < text.length() + 1; ++i) {
                    coord = blob.getCoordAtOffset(i);
                    canvas.drawLine(coord.getX(), coord.getY() - 2, coord.getX(), coord.getY() + 2, stroke);
                }

                // extra info
                canvas.save();
                canvas.translate(0, bounds.getBottom() + 5);
                float[] positionsXY = blob.getPositions();
                String positions = "[";
                for (int i = 0; i < positionsXY.length; i += 2)
                    positions += String.format(Locale.ENGLISH, "%.02f, ", positionsXY[i]);
                positions = positions.substring(0, positions.length() - 2) +  "] .. " + String.format(Locale.ENGLISH, "%.02f", bounds.getRight());
                var tableSize = drawTable(canvas, shaper, new String[] {
                    // "Chars",     text.chars().mapToObj(c -> String.format("U+%04X", c)).collect(Collectors.joining(" ")),
                    "RelCoord",  "(" + (int) cursor.getX() + ", " + (int) cursor.getY() + ")",
                    "Offset",    offset + " / " + text.length(),
                    "Positions", positions,
                    "Glyphs",    Arrays.toString(blob.getGlyphs()),
                    "Clusters",  Arrays.toString(blob.getClusters()),
                    "Widths",    Arrays.toString(emoji18.getWidths(blob.getGlyphs()))
                });
                canvas.restore();

                float offsetLeft = Math.max(bounds.getRight(), tableSize.getX()) + 10;
                canvas.translate(offsetLeft, 0);
                cursor = cursor.offset(-offsetLeft, 0);
                bottom = Math.max(bottom, bounds.getBottom() + 5 + tableSize.getY());
            }
        }
        canvas.restore();
        canvas.translate(0, bottom + 10);
        return bottom + 10;
    }

    @Override
    public void draw(Canvas canvas, int width, int height, float dpi, int xpos, int ypos) {
        Point cursor = new Point(xpos, ypos);
        canvas.translate(20, 20);
        cursor = cursor.offset(-20, -20);
        // cursor = cursor.offset(0, -drawLine(canvas, new String[] { "one", "xyfz", "два", "الخطوط", "🧛", "one xyfz два الخطوط 🧛" }, inter18, cursor));
        // cursor = cursor.offset(0, -drawLine(canvas, "fiz officiad", zapfino13, cursor)); // swashes
        // cursor = cursor.offset(0, -drawLine(canvas, "multiple lines", inter18, 50, cursor));
        // cursor = cursor.offset(0, -drawLine(canvas, "واحد اثنين ثلاثة", inter18, cursor)); // RTL
        // cursor = cursor.offset(0, -drawLine(canvas, new String[] {"<->", "a<->b", "🧑🏿", "a🧑🏿b"}, inter18, cursor)); // Ligatures
        // cursor = cursor.offset(0, -drawLine(canvas, new String[] {"x̆x̞̊x̃", "c̝̣̱̲͈̝ͨ͐̈ͪͨ̃ͥͅh̙̬̿̂a̯͎͍̜͐͌͂̚o̬s͉̰͊̀"}, inter18, cursor)); // Zero-witdh modifiers
        // cursor = cursor.offset(0, -drawLine(canvas, new String[] {"क्", "क्‍", "👨👩👧👦", "👨‍👩‍👧‍👦", "a👨‍👩‍👧‍👦b"}, inter18, cursor)); // ZWJ
        // cursor = cursor.offset(0, -drawLine(canvas, new String[] {"می‌خواهم‎ ", "میخواهم"}, inter18, cursor)); // ZWNJ
        cursor = cursor.offset(0, -drawLine(canvas, new String[] {"👨‍👩‍👧‍👦b"}, inter18, cursor)); // ZWJ
    }
}