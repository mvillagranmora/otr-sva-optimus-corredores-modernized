package org.apache.pdfbox.pdmodel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.util.Matrix;

/**
 * ORACLE SHIM of PDFBox PDPageContentStream. Drawing operations are ignored; every showText string is recorded in TEXTOS, in order,
 * so the oracle pins the TEXT the legacy code puts in the PDF (header names, number formatting "#,##0.##" with ',' decimal and '.'
 * grouping), not the geometry.
 */
public class PDPageContentStream implements AutoCloseable {
    public static final List<String> TEXTOS = new ArrayList<>();

    public PDPageContentStream(PDDocument doc, PDPage page) throws IOException {
    }

    public void setNonStrokingColor(float r, float g, float b) throws IOException {
    }

    public void setNonStrokingColor(int r, int g, int b) throws IOException {
    }

    public void addRect(float x, float y, float w, float h) throws IOException {
    }

    public void fill() throws IOException {
    }

    public void setLineWidth(float w) throws IOException {
    }

    public void moveTo(float x, float y) throws IOException {
    }

    public void lineTo(float x, float y) throws IOException {
    }

    public void stroke() throws IOException {
    }

    public void beginText() throws IOException {
    }

    public void endText() throws IOException {
    }

    public void setFont(PDFont font, float size) throws IOException {
    }

    public void setTextMatrix(Matrix m) throws IOException {
    }

    public void showText(String text) throws IOException {
        TEXTOS.add(text);
    }

    @Override
    public void close() throws IOException {
    }
}
