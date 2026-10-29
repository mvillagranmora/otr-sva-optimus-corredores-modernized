package org.apache.pdfbox.pdmodel;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * ORACLE SHIM (stand-in for Apache PDFBox 3.x, not available offline). Only the members GenerarArchivoSaldoCuentas uses.
 * save() writes a fixed marker instead of a real PDF: the PDF layout is NOT reproduced (see PDPageContentStream for what is pinned).
 */
public class PDDocument implements AutoCloseable {
    public static int PAGINAS = 0;

    public void addPage(PDPage page) {
        PAGINAS++;
    }

    public void save(OutputStream out) throws IOException {
        out.write("%PDF-1.4 oracle-shim (layout not reproduced)".getBytes(StandardCharsets.ISO_8859_1));
    }

    @Override
    public void close() throws IOException {
    }
}
