package com.itextpdf.text;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

/**
 * ORACLE SHIM of iText 5 Document. Every element added is recorded in PDPageContentStream.TEXTOS (the oracle "pdfTextos"):
 * line breaks, tables (columns, header rows) and every cell (alignment, border, text). close() fires the page event ONCE (a single page
 * is assumed; iText fires it per page, so multi page reports are not reproduced) and writes a deterministic stub that starts with
 * "%PDF-1.4" to the stream. NOT reproduced: layout, fonts, pagination, images, compression. See reportes/README.md.
 */
public class Document {
    private final Rectangle page;
    private final float left, right, top, bottom;
    private boolean open;
    public PdfWriter writer;

    public Document(Rectangle page, float left, float right, float top, float bottom) {
        this.page = page;
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
    }

    public void open() {
        open = true;
        PDPageContentStream.TEXTOS.add("documento abierto " + (int) page.getWidth() + "x" + (int) page.getHeight()
                + " margenes=" + (int) left + "," + (int) right + "," + (int) top + "," + (int) bottom);
    }

    public boolean add(Element e) throws DocumentException {
        if (!open) {
            throw new DocumentException("The document is not open.");
        }
        if (e instanceof Chunk) {
            PDPageContentStream.TEXTOS.add("salto de linea");
        } else if (e instanceof PdfPTable t) {
            PDPageContentStream.TEXTOS.add(t.resumen());
            t.cells.forEach(c -> PDPageContentStream.TEXTOS.add(c.describir()));
        }
        return true;
    }

    public void close() {
        if (writer != null && writer.pageEvent != null) {
            writer.pageEvent.onEndPage(writer, this);
        }
        open = false;
        if (writer != null) {
            try {
                writer.out.write("%PDF-1.4\n% oracle shim: not a real PDF\n".getBytes(StandardCharsets.ISO_8859_1));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public Rectangle getPageSize() {
        return page;
    }

    public float topMargin() {
        return top;
    }

    public float bottomMargin() {
        return bottom;
    }

    public float leftMargin() {
        return left;
    }

    public float left() {
        return left;
    }

    public float right() {
        return page.getWidth() - right;
    }
}
