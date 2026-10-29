package org.apache.pdfbox.pdmodel.common;

/** ORACLE SHIM of PDFBox PDRectangle: LEGAL is 612 x 1008 points (US Legal), like PDFBox. */
public class PDRectangle {
    public static final PDRectangle LEGAL = new PDRectangle(612f, 1008f);
    private final float width;
    private final float height;

    public PDRectangle(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }
}
