package com.itextpdf.text;

/** ORACLE SHIM of iText 5 Font: family and size only (no font metrics, no embedding). */
public class Font {
    public final String family;
    public final float size;

    public Font(String family, float size) {
        this.family = family;
        this.size = size;
    }
}
