package cl.bcs.cuentainversion.port.out;

import java.io.IOException;
import java.util.List;

/**
 * Port that renders the PDF of the "Anexo 16" report (legacy {@code GenerarArchivoCuentasPorCobrarAnexo16.generarPdf}, written with
 * iText 5). The service describes WHAT the legacy document contains (page, logo, page header and footer, body table with its cells,
 * alignments, borders, spans) and an adapter draws it.
 *
 * <p><b>There is no production adapter yet.</b> Neither iText nor PDFBox is available in the offline dependency cache, so this module
 * ships only the port; which library to use (iText 5/7/8, OpenPDF, PDFBox) and its licence is a human decision. The characterization
 * tests use a recording implementation (test side) that keeps the cell texts and a stub file that starts with {@code %PDF-1.4}.
 *
 * <p>Contract of an adapter:
 * <ul>
 *   <li>{@code logo} must be an image the library can decode; otherwise {@link #generar} throws {@link IOException}
 *       (legacy: {@code Image.getInstance} raised "The byte array is not a recognized imageformat", README CTA-B-205/206);</li>
 *   <li>{@code encabezadoPagina} and {@code pieDePagina} are drawn on EVERY page (legacy page event {@code onEndPage}: the header at the top
 *       margin, the footer above the bottom margin, locked width); the body table repeats its first {@code filasCabecera} rows on each page;</li>
 *   <li>cells use a thin box border unless {@code borde} is {@code 0} (no border), padding as given, white background.</li>
 * </ul>
 */
public interface GeneradorPdf {

    /** Renders the document and returns the bytes of the PDF. */
    byte[] generar(DocumentoPdf documento) throws IOException;

    /** Horizontal / vertical alignment of a cell (vertical: ARRIBA, MEDIO; {@code null} = the library default). */
    enum Alineacion { IZQUIERDA, CENTRO, DERECHA, ARRIBA, MEDIO }

    /** Page size in points and margins (legacy: A4 landscape 842x595, margins 36 / 36 / 130 / 36). */
    record PaginaPdf(int ancho, int alto, int margenIzquierdo, int margenDerecho, int margenSuperior, int margenInferior) {
    }

    /** Font: bold or regular Helvetica of the given size. */
    record FuentePdf(boolean negrita, int tamano) {
    }

    /** Logo drawn in the page header, scaled to fit the given box (points). */
    record LogoPdf(byte[] imagen, int anchoMaximo, int altoMaximo) {
    }

    /**
     * One table cell. {@code imagen} marks the cell that carries the logo (its text is then {@code null}).
     * {@code borde} 15 = box, 0 = none; padding in points (negative = library default).
     */
    record CeldaPdf(String texto, FuentePdf fuente, Alineacion horizontal, Alineacion vertical, int borde, int colspan, int rowspan,
                    float padding, boolean imagen) {
    }

    /** Table with relative column widths and the number of header rows (repeated on every page). */
    record TablaPdf(float[] anchosRelativos, int filasCabecera, List<CeldaPdf> celdas) {
    }

    /**
     * The whole document: {@code saltosAntes} / {@code saltosDespues} blank lines around the body table.
     */
    record DocumentoPdf(PaginaPdf pagina, LogoPdf logo, TablaPdf encabezadoPagina, TablaPdf pieDePagina, int saltosAntes,
                        TablaPdf cuerpo, int saltosDespues) {
    }
}
