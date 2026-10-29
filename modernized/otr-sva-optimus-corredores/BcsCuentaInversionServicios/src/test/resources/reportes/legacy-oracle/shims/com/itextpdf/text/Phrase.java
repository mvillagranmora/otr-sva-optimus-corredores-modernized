package com.itextpdf.text;

/** ORACLE SHIM of iText 5 Phrase: keeps the text (and the font) only. */
public class Phrase implements Element {
    public final String content;
    public final Font font;

    public Phrase(String content) {
        this(content, null);
    }

    public Phrase(String content, Font font) {
        this.content = content;
        this.font = font;
    }
}
