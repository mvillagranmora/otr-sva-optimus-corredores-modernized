package com.itextpdf.text;

/** ORACLE SHIM of iText 5 Rectangle. */
public class Rectangle implements Element {
    public static final int NO_BORDER = 0;
    private final float width, height;

    public Rectangle(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public Rectangle rotate() {
        return new Rectangle(height, width);
    }
}
