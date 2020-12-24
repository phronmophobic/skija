package org.jetbrains.skija.examples.lwjgl;

import org.jetbrains.skija.*;
import org.jetbrains.skija.shaper.*;
import org.jetbrains.skija.paragraph.*;

public class ParagraphMetricsScene extends Scene {
    public FontCollection fc = new FontCollection();
    public long lastUpdate = 0;

    public Font inter13;
    public Paint detailsFill;
    public Paint boundariesStroke;
    
    public ParagraphMetricsScene() {
        fc.setDefaultFontManager(FontMgr.getDefault());
        
        TypefaceFontProvider fm = new TypefaceFontProvider();
        Typeface jbMono = Typeface.makeFromFile("fonts/JetBrainsMono-Regular.ttf");
        fm.registerTypeface(jbMono);
        Typeface inter = Typeface.makeFromFile("fonts/InterHinted-Regular.ttf");
        fm.registerTypeface(inter, "Interface");
        fc.setAssetFontManager(fm);

        inter13 = new Font(inter, 13);
        detailsFill = new Paint().setColor(0xFFCC3333);
        boundariesStroke = new Paint().setColor(0xFFFAA6B2).setMode(PaintMode.STROKE).setStrokeWidth(1f);
    }

    @Override
    public void draw(Canvas canvas, int width, int height, float dpi, int xpos, int ypos) {
        canvas.translate(30, 30);
        drawSelection(canvas, xpos - 30f, ypos - 30f);
        canvas.translate(0, 30);
        drawIndices(canvas);
    }

    public void drawSelection(Canvas canvas, float dx, float dy) {
        try (TextStyle defaultTs = new TextStyle().setFontSize(24).setColor(0xFF000000).setHeight(1.2f);
             TextStyle largeTs   = new TextStyle().setFontSize(36).setColor(0xFF000000);
             TextStyle smallTs   = new TextStyle().setFontSize(12).setColor(0xFF000000);
             TextStyle ligaTs    = new TextStyle().setFontSize(24).setColor(0xFF000000).setFontFamilies(new String[] { "Interface" });
             TextStyle zapfinoTs = new TextStyle().setFontSize(24).setColor(0xFF000000).setFontFamilies(new String[] { "Zapfino" });
             ParagraphStyle ps   = new ParagraphStyle();
             ParagraphBuilder pb = new ParagraphBuilder(ps, fc);
             ParagraphStyle ps2  = new ParagraphStyle().setAlignment(Alignment.RIGHT);)
        {
            // default style
            pb.pushStyle(defaultTs);
            
            pb.addText("123 567 ");
            pb.pushStyle(ligaTs);
            pb.addText("<-> ");
            pb.popStyle();
            pb.addText("👩 👩👩 🧑🏿 🧑🏿🧑🏿 👮‍♀️ 👮‍♀️👮‍♀️ 👩‍👩‍👧‍👧 👩‍👩‍👧‍👧👩‍👩‍👧‍👧\n");

            pb.addText("The following ");
            pb.pushStyle(largeTs);
            pb.addText("sentence");
            pb.popStyle();
            pb.addText(" is true\n");

            pb.pushStyle(largeTs);
            pb.addText("The previous       ");
            pb.popStyle();
            pb.addText("sentence");
            pb.pushStyle(largeTs);
            pb.addText(" is false\n");
            pb.popStyle();

            pb.setParagraphStyle(ps2);
            pb.pushStyle(defaultTs);
            pb.addText("— Vicious circularity, \n");
            pb.pushStyle(smallTs);
            pb.addText("  or infinite regress\n");
            pb.popStyle();

            pb.addText("hello мир дружба <<<");
            pb.addPlaceholder(new PlaceholderStyle(50, 2f, PlaceholderAlignment.BASELINE, BaselineMode.ALPHABETIC, 0f));
            pb.addText(">>> fi fl 👃 one two ثلاثة 12 👂 34 خمسة\n");
            pb.addText("x̆x̞̊x̃ c̝̣̱̲͈̝ͨ͐̈ͪͨ̃ͥͅh̙̬̿̂a̯͎͍̜͐͌͂̚o̬s͉̰͊̀ ");
            pb.pushStyle(zapfinoTs);
            pb.addText("fiz officiad\n");
            pb.popStyle();

            try (Paragraph p = pb.build();) {
                p.layout(600f);

                // getLineMetrics
                for (LineMetrics lm: p.getLineMetrics()) {
                    canvas.drawRect(Rect.makeXYWH((float) lm.getLeft(),
                                                  (float) (lm.getBaseline() - lm.getAscent()),
                                                  (float) lm.getWidth(),
                                                  (float) (lm.getAscent() + lm.getDescent())), boundariesStroke);
                    canvas.drawLine((float) lm.getLeft(), (float) lm.getBaseline(), (float) (lm.getLeft() + lm.getWidth()), (float) lm.getBaseline(), boundariesStroke);
                }

                // getGlyphPositionAtCoordinate
                int glyphx = p.getGlyphPositionAtCoordinate(dx, dy).getPosition();

                try (var blue   = new Paint().setColor(0x80b3d7ff);
                     var orange = new Paint().setColor(0x80ffd7b3);) {
                    
                    // getRectsForRange    
                    for (TextBox box: p.getRectsForRange(0, glyphx, RectHeightMode.TIGHT, RectWidthMode.TIGHT)) {
                        canvas.drawRect(box.getRect(), blue);
                    }

                    // getWordBoundary
                    IRange word = p.getWordBoundary(glyphx);
                    for (TextBox box: p.getRectsForRange(word.getStart(), word.getEnd(), RectHeightMode.TIGHT, RectWidthMode.TIGHT)) {
                        canvas.drawRect(box.getRect(), orange);
                    }
                }
                p.paint(canvas, 0, 0);
                canvas.drawString("idx: " + glyphx, 630, 20, inter13, detailsFill);
                canvas.translate(0, p.getHeight());
            }
        }
    }

    public void paintParagraph(Canvas canvas, Paragraph paragraph) {
        paragraph.layout(600f);
        paragraph.paint(canvas, 0, 0);
        for (LineMetrics lm: paragraph.getLineMetrics()) {
            canvas.drawRect(Rect.makeXYWH((float) lm.getLeft(),
                                          (float) (lm.getBaseline() - lm.getAscent()),
                                          (float) lm.getWidth(),
                                          (float) (lm.getAscent() + lm.getDescent())), boundariesStroke);
            canvas.drawLine((float) lm.getLeft(), (float) lm.getBaseline(), (float) (lm.getLeft() + lm.getWidth()), (float) lm.getBaseline(), boundariesStroke);

            canvas.drawString(
                String.format("start=%d, end w/o space=%d, end=%d, end w/ newline=%d",
                    lm.getStartIndex(),
                    lm.getEndExcludingWhitespaces(),
                    lm.getEndIndex(),
                    lm.getEndIncludingNewline()),
                150, (float) lm.getBaseline(), inter13, detailsFill);
        }
        canvas.translate(0, paragraph.getHeight());
    }

    public void drawIndices(Canvas canvas) {
        try (TextStyle defaultTs = new TextStyle().setFontSize(16).setColor(0xFF000000).setFontFamilies(new String[] { "Interface" }).setHeight(1.5f);
             ParagraphStyle ps   = new ParagraphStyle();
             ParagraphBuilder pb = new ParagraphBuilder(ps, fc);)
        {
            pb.pushStyle(defaultTs);

            String text = "12345\n"
                + "абвгд\n"
                + "space     \n"
                + "\\r\\n\r\n"
                + "\\n\\n\n\n"
                + "👩👩👩👩👩\n"
                + "<-><->\n"
                + "🧑🏿🧑🏿🧑🏿🧑🏿🧑🏿\n"
                + "👮‍♀️👮‍♀️👮‍♀️👮‍♀️👮‍♀️\n"
                + "👩‍👩‍👧‍👧👩‍👩‍👧‍👧👩‍👩‍👧‍👧👩‍👩‍👧‍👧👩‍👩‍👧‍👧";

            pb.addText(text);
            try (Paragraph paragraph = pb.build();) {               
                paintParagraph(canvas, paragraph);
            }
        }
    }
}