package org.apache.pdfbox.pdmodel;

import org.apache.pdfbox.pdmodel.common.PDRectangle;

/** ORACLE SHIM of PDFBox PDPage. */
public class PDPage {
    private final PDRectangle box;

    public PDPage(PDRectangle box) {
        this.box = box;
    }

    public PDRectangle getMediaBox() {
        return box;
    }
}
