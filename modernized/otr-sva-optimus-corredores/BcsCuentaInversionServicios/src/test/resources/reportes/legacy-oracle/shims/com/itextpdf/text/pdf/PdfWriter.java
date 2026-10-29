package com.itextpdf.text.pdf;

import java.io.OutputStream;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;

/** ORACLE SHIM of iText 5 PdfWriter: binds a document to an output stream and a page event. */
public class PdfWriter {
    public final Document document;
    public final OutputStream out;
    public PdfPageEvent pageEvent;
    private final PdfContentByte cb = new PdfContentByte();

    private PdfWriter(Document d, OutputStream out) {
        this.document = d;
        this.out = out;
    }

    public static PdfWriter getInstance(Document d, OutputStream out) throws DocumentException {
        PdfWriter w = new PdfWriter(d, out);
        d.writer = w;
        return w;
    }

    public void setPageEvent(PdfPageEvent e) {
        pageEvent = e;
    }

    public PdfContentByte getDirectContent() {
        return cb;
    }
}
