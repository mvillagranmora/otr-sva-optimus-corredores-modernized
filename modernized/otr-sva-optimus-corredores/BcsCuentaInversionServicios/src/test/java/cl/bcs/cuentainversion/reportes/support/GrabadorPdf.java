package cl.bcs.cuentainversion.reportes.support;

import cl.bcs.cuentainversion.port.out.GeneradorPdf;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Test-side implementation of the {@link GeneradorPdf} port (the module has no PDF library, see the port javadoc). It mirrors the
 * legacy oracle shims of iText ({@code legacy-oracle/shims/com/itextpdf/text/**}): it records, in order, what a PDF would show (the
 * "pdfTextos" of the goldens: document opening, line breaks, the table shape, every cell with alignment / border / spans / text, and
 * the page header and footer cells prefixed {@code pagina/}) and returns a stub that starts with {@code %PDF-1.4}.
 * Not reproduced: layout, fonts, colours, pagination (the header / footer are recorded once, a single page is assumed) and image decoding
 * (only the magic number of the logo is checked, like the shim).
 */
public final class GrabadorPdf implements GeneradorPdf {

    private final List<String> textos = new ArrayList<>();

    /** The recorded texts, in order. */
    public List<String> textos() {
        return List.copyOf(textos);
    }

    @Override
    public byte[] generar(DocumentoPdf d) throws IOException {
        comprobarImagen(d.logo().imagen());
        PaginaPdf p = d.pagina();
        textos.add("documento abierto " + p.ancho() + "x" + p.alto() + " margenes=" + p.margenIzquierdo() + "," + p.margenDerecho() + ","
                + p.margenSuperior() + "," + p.margenInferior());
        for (int i = 0; i < d.saltosAntes(); i++) {
            textos.add("salto de linea");
        }
        TablaPdf cuerpo = d.cuerpo();
        textos.add("tabla[columnas=" + cuerpo.anchosRelativos().length + ",filasCabecera=" + cuerpo.filasCabecera() + ",celdas=" + cuerpo.celdas().size() + "]");
        cuerpo.celdas().forEach(c -> textos.add(describir(c)));
        for (int i = 0; i < d.saltosDespues(); i++) {
            textos.add("salto de linea");
        }
        // page event on close: header table, then footer table
        d.encabezadoPagina().celdas().forEach(c -> textos.add("pagina/" + describir(c)));
        d.pieDePagina().celdas().forEach(c -> textos.add("pagina/" + describir(c)));
        return "%PDF-1.4\n% oracle shim: not a real PDF\n".getBytes(StandardCharsets.ISO_8859_1);
    }

    private static String describir(CeldaPdf c) {
        String texto = c.imagen() ? "[imagen]" : c.texto();
        return "celda[h=" + nombre(c.horizontal()) + ",v=" + nombre(c.vertical()) + ",borde=" + c.borde() + ",colspan=" + c.colspan()
                + ",rowspan=" + c.rowspan() + "]: " + texto;
    }

    /** iText names alignment 0 "LEFT"; an unset alignment is 0. */
    private static String nombre(Alineacion a) {
        return switch (a == null ? Alineacion.IZQUIERDA : a) {
            case IZQUIERDA -> "LEFT";
            case CENTRO -> "CENTER";
            case DERECHA -> "RIGHT";
            case ARRIBA -> "TOP";
            case MEDIO -> "MIDDLE";
        };
    }

    /** Same check as the oracle Image shim: PNG, JPEG, GIF or BMP magic number, else the IOException iText raises. */
    private static void comprobarImagen(byte[] img) throws IOException {
        if (img == null) {
            throw new NullPointerException("image bytes");
        }
        boolean png = img.length > 4 && (img[0] & 0xff) == 0x89 && img[1] == 'P' && img[2] == 'N' && img[3] == 'G';
        boolean jpg = img.length > 3 && (img[0] & 0xff) == 0xff && (img[1] & 0xff) == 0xd8;
        boolean gif = img.length > 3 && img[0] == 'G' && img[1] == 'I' && img[2] == 'F';
        boolean bmp = img.length > 2 && img[0] == 'B' && img[1] == 'M';
        if (!(png || jpg || gif || bmp)) {
            throw new IOException("The byte array is not a recognized imageformat.");
        }
    }
}
