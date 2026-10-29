package org.apache.pdfbox.pdmodel.font;

import java.io.IOException;

/**
 * ORACLE SHIM of PDFBox PDFont. getStringWidth answers 0 (the real answer comes from the font AFM tables): the legacy word-wrapping
 * (pdfWrapText) therefore never wraps in the oracle and each cell is shown as ONE text. The PDF wrapping is not pinned.
 */
public abstract class PDFont {
    public float getStringWidth(String text) throws IOException {
        return 0f;
    }
}
