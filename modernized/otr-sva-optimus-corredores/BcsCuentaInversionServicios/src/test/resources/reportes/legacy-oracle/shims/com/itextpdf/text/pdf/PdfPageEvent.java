package com.itextpdf.text.pdf;

import com.itextpdf.text.Document;

/** ORACLE SHIM of iText 5 PdfPageEvent (only the callback the legacy overrides). */
public interface PdfPageEvent {
    void onEndPage(PdfWriter writer, Document document);
}
