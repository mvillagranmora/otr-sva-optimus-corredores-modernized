package com.itextpdf.text;

import java.io.IOException;

/**
 * ORACLE SHIM of iText 5 Image. getInstance(byte[]) only checks the magic number (PNG, JPEG, GIF, BMP); anything else raises the
 * IOException iText raises ("The byte array is not a recognized imageformat."). Pixels are not decoded.
 */
public class Image implements Element {
    public final int length;
    public float width = 0, height = 0;

    private Image(int length) {
        this.length = length;
    }

    public static Image getInstance(byte[] img) throws IOException {
        if (img == null) {
            throw new NullPointerException("image bytes");
        }
        boolean png = img.length > 4 && (img[0] & 0xff) == 0x89 && img[1] == 'P' && img[2] == 'N' && img[3] == 'G';
        boolean jpg = img.length > 3 && (img[0] & 0xff) == 0xff && (img[1] & 0xff) == 0xd8;
        boolean gif = img.length > 3 && img[0] == 'G' && img[1] == 'I' && img[2] == 'F';
        boolean bmp = img.length > 2 && img[0] == 'B' && img[1] == 'M';
        if (!(png || jpg || gif || bmp)) {
            throw new IOException("The byte array is not a recognized imageformat.");
        }
        return new Image(img.length);
    }

    public void scaleToFit(float w, float h) {
        this.width = w;
        this.height = h;
    }
}
