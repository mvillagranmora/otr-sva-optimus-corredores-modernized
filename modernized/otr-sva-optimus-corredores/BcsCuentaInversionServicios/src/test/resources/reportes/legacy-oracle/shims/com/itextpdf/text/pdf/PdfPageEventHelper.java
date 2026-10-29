package com.itextpdf.text.pdf;

import com.itextpdf.text.Document;

/** ORACLE SHIM of iText 5 PdfPageEventHelper. */
public abstract class PdfPageEventHelper implements PdfPageEvent {
    @Override
    public void onEndPage(PdfWriter writer, Document document) {
    }
}
