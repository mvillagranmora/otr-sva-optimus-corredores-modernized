package com.itextpdf.text;

/** ORACLE SHIM of iText 5 BaseColor (only the constant the legacy uses). */
public class BaseColor {
    public static final BaseColor WHITE = new BaseColor(255, 255, 255);
    public final int r, g, b;

    public BaseColor(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }
}
