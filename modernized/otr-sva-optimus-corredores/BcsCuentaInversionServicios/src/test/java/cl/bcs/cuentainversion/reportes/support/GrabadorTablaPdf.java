package cl.bcs.cuentainversion.reportes.support;

import cl.bcs.cuentainversion.port.out.GeneradorPdf;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Test-side {@link GeneradorPdf} for the documents that are just a table (GenerarArchivoSaldoCuentas): keeps the text of every cell
 * that shows something, in order (a null or empty cell is skipped: the legacy wraps a cell's text into lines and calls showText once
 * per line, and an empty text gives no line, GenerarArchivoSaldoCuentas.java:745, so the oracle's PDFBox shim records nothing for it), and returns a stub that starts with {@code %PDF-1.4} (the goldens pin "is a pdf", not its bytes). The module ships no PDF
 * library, see the port javadoc.
 */
public final class GrabadorTablaPdf implements GeneradorPdf {

    private final List<String> textos = new ArrayList<>();

    public List<String> textos() {
        return List.copyOf(textos);
    }

    @Override
    public byte[] generar(DocumentoPdf documento) {
        documento.cuerpo().celdas().forEach(c -> {
            if (c.texto() != null && !c.texto().isEmpty()) {
                textos.add(c.texto());
            }
        });
        return "%PDF-1.4\n% test double: not a real PDF\n".getBytes(StandardCharsets.ISO_8859_1);
    }
}
