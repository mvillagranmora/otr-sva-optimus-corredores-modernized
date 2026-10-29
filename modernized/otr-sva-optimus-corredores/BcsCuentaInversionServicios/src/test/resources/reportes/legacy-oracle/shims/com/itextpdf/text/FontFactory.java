package com.itextpdf.text;

/** ORACLE SHIM of iText 5 FontFactory. */
public class FontFactory {
    public static final String HELVETICA = "Helvetica";
    public static final String HELVETICA_BOLD = "Helvetica-Bold";

    public static Font getFont(String family, float size) {
        return new Font(family, size);
    }
}
