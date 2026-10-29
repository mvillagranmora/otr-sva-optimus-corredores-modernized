package com.itextpdf.text.pdf;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;

/** ORACLE SHIM of iText 5 PdfPCell: keeps text/image, alignment, border and colspan; no layout. */
public class PdfPCell implements Element {
    public final Phrase phrase;
    public final Image image;
    public int border = 15, horizontal = 0, vertical = 0, colspan = 1, rowspan = 1;
    public float padding = 2;
    public BaseColor background;

    public PdfPCell() {
        this.phrase = null;
        this.image = null;
    }

    public PdfPCell(Phrase phrase) {
        this.phrase = phrase;
        this.image = null;
    }

    public PdfPCell(Image image, boolean fit) {
        this.phrase = null;
        this.image = image;
    }

    public void setBorder(int b) {
        border = b;
    }

    public void setHorizontalAlignment(int a) {
        horizontal = a;
    }

    public void setVerticalAlignment(int a) {
        vertical = a;
    }

    public void setBackgroundColor(BaseColor c) {
        background = c;
    }

    public void setPadding(float p) {
        padding = p;
    }

    public void setRowspan(int r) {
        rowspan = r;
    }

    public void setColspan(int c) {
        colspan = c;
    }

    /** One line describing the cell for the recording. */
    public String describir() {
        String t = phrase != null ? phrase.content : "[imagen]";
        return "celda[h=" + Element.name(horizontal) + ",v=" + Element.name(vertical) + ",borde=" + border + ",colspan=" + colspan + ",rowspan=" + rowspan + "]: " + t;
    }
}
