package com.itextpdf.text.pdf;

import java.util.ArrayList;
import java.util.List;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;

/** ORACLE SHIM of iText 5 PdfPTable: keeps the cells in order; no layout. */
public class PdfPTable implements Element {
    public final int columns;
    public final List<PdfPCell> cells = new ArrayList<>();
    public int headerRows = 0;
    private final PdfPCell defaultCell = new PdfPCell();
    public float[] widths;

    public PdfPTable(int columns) {
        this.columns = columns;
    }

    public PdfPTable(float[] relativeWidths) {
        this.columns = relativeWidths.length;
        this.widths = relativeWidths;
    }

    public void setWidthPercentage(float p) {
    }

    public void setTotalWidth(float w) {
    }

    public void setLockedWidth(boolean b) {
    }

    public void setHeaderRows(int n) {
        headerRows = n;
    }

    public void setWidths(float[] w) throws DocumentException {
        if (w.length != columns) {
            throw new DocumentException("Wrong number of columns.");
        }
        widths = w;
    }

    public PdfPCell getDefaultCell() {
        return defaultCell;
    }

    public PdfPCell addCell(PdfPCell c) {
        cells.add(c);
        return c;
    }

    public float writeSelectedRows(int rowStart, int rowEnd, float x, float y, PdfContentByte cb) {
        cells.forEach(c -> org.apache.pdfbox.pdmodel.PDPageContentStream.TEXTOS.add("pagina/" + c.describir()));
        return y;
    }

    public String resumen() {
        return "tabla[columnas=" + columns + ",filasCabecera=" + headerRows + ",celdas=" + cells.size() + "]";
    }
}
