package org.apache.pdfbox.util;

/** ORACLE SHIM of PDFBox Matrix. */
public class Matrix {
    public final float x;
    public final float y;

    private Matrix(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public static Matrix getTranslateInstance(float x, float y) {
        return new Matrix(x, y);
    }
}
