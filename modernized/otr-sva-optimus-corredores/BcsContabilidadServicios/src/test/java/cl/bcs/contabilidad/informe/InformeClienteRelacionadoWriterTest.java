package cl.bcs.contabilidad.informe;

import cl.bcs.contabilidad.domain.model.clienterelacionado.OperacionClienteRelacionado;
import cl.bcs.contabilidad.domain.model.clienterelacionado.ResultadoConsultaClienteRelacionado;
import cl.bcs.contabilidad.domain.model.clienterelacionado.TotalConcepto;
import cl.bcs.contabilidad.infrastructure.informe.InformeClienteRelacionadoWriter;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFPicture;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static cl.bcs.contabilidad.informe.ClienteRelacionadoFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Characterization of the workbook produced by legacy {@code GenerarInformeClienteRelacionado}
 * (GICR = legacy/otr-sva-optimus-corredores/BSCL/Contabilidad/JAVA/BcsContabilidadServicios/src/cl/bcs/contabilidad/
 * GenerarInformeClienteRelacionado.java, lines 153-155 and 212-623).
 *
 * <p>Every test renders through {@code InformeClienteRelacionadoWriter.escribir(..)} and reads the bytes back with
 * POI's {@link HSSFWorkbook} - the legacy writes a BIFF8 {@code .xls} (HSSFWorkbook, GICR:212), so the bytes must be
 * an OLE2/HSSF workbook, not OOXML.
 *
 * <p>Row/column indices below are 0-based (POI). "row 9" is Excel row 10.
 */
class InformeClienteRelacionadoWriterTest {

    static final String FORMATO_CON_SIGNO = "#,##0;-#,##0;-;-";  // obtenerEstiloNumerico(0, wb, false), GICR:577-597
    static final String FORMATO_VACIO = "#,##0;#,##0;;";         // obtenerEstiloNumerico(0, wb, true)
    static final String TITULO =
        "Informe de Operaciones con Clientes Relacionados\nDESDE 01-03-2025 HASTA 31-03-2025";

    private final InformeClienteRelacionadoWriter writer = new InformeClienteRelacionadoWriter();

    private static TotalConcepto tc(String concepto, String cantidad, String monto) {
        return new TotalConcepto(concepto, bd(cantidad), bd(monto), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    private HSSFWorkbook render(List<TotalConcepto> conceptos, TotalConcepto total, List<OperacionClienteRelacionado> ops,
                                byte[] logo) {
        byte[] bytes = writer.escribir("2025-03-01", "2025-03-31", logo,
            new ResultadoConsultaClienteRelacionado(conceptos, total, ops));
        try {
            return new HSSFWorkbook(new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            throw new AssertionError("el informe no es un workbook HSSF (.xls) legible", e);
        }
    }

    private HSSFWorkbook renderOps(OperacionClienteRelacionado... ops) {
        return render(List.of(), tc("Total", "0", "0"), List.of(ops), new byte[0]);
    }

    private static Cell cell(Sheet s, int row, int col) {
        Row r = s.getRow(row);
        assertThat(r).as("fila %d de %s", row, s.getSheetName()).isNotNull();
        Cell c = r.getCell(col);
        assertThat(c).as("celda (%d,%d) de %s", row, col, s.getSheetName()).isNotNull();
        return c;
    }

    private static void assertTexto(Sheet s, int row, int col, String esperado) {
        Cell c = cell(s, row, col);
        assertThat(c.getCellType()).as("tipo celda (%d,%d)", row, col).isEqualTo(CellType.STRING);
        assertThat(c.getStringCellValue()).isEqualTo(esperado);
    }

    private static void assertNumero(Sheet s, int row, int col, double esperado, String formato) {
        Cell c = cell(s, row, col);
        assertThat(c.getCellType()).as("tipo celda (%d,%d)", row, col).isEqualTo(CellType.NUMERIC);
        assertThat(c.getNumericCellValue()).as("valor (%d,%d)", row, col).isCloseTo(esperado, within(0.0));
        if (formato != null) {
            assertThat(c.getCellStyle().getDataFormatString()).as("formato (%d,%d)", row, col).isEqualTo(formato);
        }
    }

    private static void assertBlanco(Sheet s, int row, int col) {
        Cell c = cell(s, row, col);
        assertThat(c.getCellType()).as("celda (%d,%d) debe estar en blanco", row, col).isEqualTo(CellType.BLANK);
    }

    private static Font font(HSSFWorkbook wb, Cell c) {
        return wb.getFontAt(c.getCellStyle().getFontIndex());
    }

    private static List<String> merged(Sheet s) {
        List<String> out = new ArrayList<>();
        for (CellRangeAddress r : s.getMergedRegions()) out.add(r.formatAsString());
        return out;
    }

    // ------------------------------------------------------------------ CONT-D-30

    @Test
    @DisplayName("CONT-D-30: nombre de archivo = InformeOperacionesClientesRelacionados_ddMMyyyy_HHmmss.xls")
    void contD30_nombreArchivo() {
        // GICR:153-155
        assertThat(InformeClienteRelacionadoWriter.nombreArchivo(LocalDateTime.of(2025, 8, 20, 14, 5, 9)))
            .isEqualTo("InformeOperacionesClientesRelacionados_20082025_140509.xls");
        assertThat(InformeClienteRelacionadoWriter.nombreArchivo(LocalDateTime.of(2025, 1, 2, 3, 4, 5, 999_000_000)))
            .isEqualTo("InformeOperacionesClientesRelacionados_02012025_030405.xls");
    }

    // ------------------------------------------------------------------ CONT-D-31

    @Test
    @DisplayName("CONT-D-31: workbook .xls (HSSF) con exactamente dos hojas: 'Conceptos' y 'Operaciones', en ese orden")
    void contD31_hojas() {
        // GICR:212, 216, 220
        HSSFWorkbook wb = render(List.of(), tc("Total", "0", "0"), List.of(), new byte[0]);
        assertThat(wb.getNumberOfSheets()).isEqualTo(2);
        assertThat(wb.getSheetName(0)).isEqualTo("Conceptos");
        assertThat(wb.getSheetName(1)).isEqualTo("Operaciones");
    }

    // ------------------------------------------------------------------ CONT-D-32

    @Test
    @DisplayName("CONT-D-32: título en A1 de ambas hojas con fechas dd-MM-yyyy, negrita 14pt centrado con ajuste, fila de 40pt")
    void contD32_titulo() {
        // GICR:242-265 (Conceptos) y GICR:284-307 (Operaciones); obtenerFechaFormateada GICR:619-623
        HSSFWorkbook wb = render(List.of(), tc("Total", "0", "0"), List.of(), new byte[0]);
        for (int i = 0; i < 2; i++) {
            HSSFSheet s = wb.getSheetAt(i);
            assertTexto(s, 0, 0, TITULO);
            Cell c = cell(s, 0, 0);
            CellStyle st = c.getCellStyle();
            assertThat(font(wb, c).getBold()).isTrue();
            assertThat(font(wb, c).getFontHeightInPoints()).isEqualTo((short) 14);
            assertThat(st.getWrapText()).isTrue();
            assertThat(st.getAlignment()).isEqualTo(HorizontalAlignment.CENTER);
            assertThat(st.getVerticalAlignment()).isEqualTo(VerticalAlignment.CENTER);
            assertThat(s.getRow(0).getHeightInPoints()).isEqualTo(40f);
        }
        assertThat(merged(wb.getSheetAt(0))).contains("A1:H5");
        assertThat(merged(wb.getSheetAt(1))).contains("A1:M5");
    }

    // ------------------------------------------------------------------ CONT-D-33

    @Test
    @DisplayName("CONT-D-33: hoja Conceptos - encabezados en filas 10-11 con celdas combinadas")
    void contD33_encabezadosConceptos() {
        // GICR:360-391 (filaIdx = 9); merges GICR:252, 373-375
        HSSFWorkbook wb = render(List.of(), tc("Total", "0", "0"), List.of(), new byte[0]);
        HSSFSheet s = wb.getSheetAt(0);

        assertTexto(s, 9, 0, "Concepto");
        assertTexto(s, 9, 1, "Total Transacción");
        assertTexto(s, 9, 4, "Saldo");
        assertTexto(s, 10, 1, "Cantidad");
        assertTexto(s, 10, 2, "Monto \nM$");
        assertTexto(s, 10, 3, "Resultado \nM$");
        assertTexto(s, 10, 4, "Activo \nM$");
        assertTexto(s, 10, 5, "Pasivo \nM$");
        assertThat(merged(s)).containsExactlyInAnyOrder("A1:H5", "A10:A11", "B10:D10", "E10:F10");

        Cell h = cell(s, 9, 0);
        assertThat(font(wb, h).getBold()).isTrue();
        assertThat(h.getCellStyle().getAlignment()).isEqualTo(HorizontalAlignment.CENTER);
        assertThat(h.getCellStyle().getWrapText()).isTrue();
        // inner header cell keeps the thin borders of headerStyle (GICR:336-339)
        CellStyle sub = cell(s, 10, 2).getCellStyle();
        assertThat(sub.getBorderTop()).isEqualTo(BorderStyle.THIN);
        assertThat(sub.getBorderBottom()).isEqualTo(BorderStyle.THIN);
        assertThat(sub.getBorderLeft()).isEqualTo(BorderStyle.THIN);
        assertThat(sub.getBorderRight()).isEqualTo(BorderStyle.THIN);
    }

    // ------------------------------------------------------------------ CONT-D-34

    @Test
    @DisplayName("CONT-D-34: una fila por concepto desde fila 12: nombre con 3 espacios de sangría, números redondeados HALF_UP a 0 decimales")
    void contD34_filasConceptos() {
        // GICR:393-409; formatearCeldaNumerica GICR:613-617 (setScale(0, HALF_UP) -> double)
        HSSFWorkbook wb = render(
            List.of(tc("Comisiones", "3", "1500.5"), tc("Spot", "1", "1500.49"), tc("Notas", "2", "-2.5"),
                    new TotalConcepto("Nulos", null, null, BigDecimal.ZERO, null, null)),
            tc("Total", "6", "2998.49"), List.of(), new byte[0]);
        HSSFSheet s = wb.getSheetAt(0);

        assertTexto(s, 11, 0, "   Comisiones");
        assertThat(font(wb, cell(s, 11, 0)).getBold()).isTrue();
        assertNumero(s, 11, 1, 3, FORMATO_CON_SIGNO);
        assertNumero(s, 11, 2, 1501, FORMATO_CON_SIGNO);
        assertNumero(s, 11, 3, 0, FORMATO_VACIO);
        assertNumero(s, 11, 4, 0, FORMATO_VACIO);
        assertNumero(s, 11, 5, 0, FORMATO_VACIO);

        assertTexto(s, 12, 0, "   Spot");
        assertNumero(s, 12, 2, 1500, FORMATO_CON_SIGNO);

        assertTexto(s, 13, 0, "   Notas");
        assertNumero(s, 13, 2, -3, FORMATO_CON_SIGNO); // HALF_UP rounds -2.5 away from zero

        // null Cantidad/Monto/Activos/Pasivos are written as 0 (GICR:404-408 "... != null ? ... : \"0\"")
        assertTexto(s, 14, 0, "   Nulos");
        assertNumero(s, 14, 1, 0, FORMATO_CON_SIGNO);
        assertNumero(s, 14, 2, 0, FORMATO_CON_SIGNO);
        assertNumero(s, 14, 4, 0, FORMATO_VACIO);
        assertNumero(s, 14, 5, 0, FORMATO_VACIO);

        // column borders applied with RegionUtil (GICR:443-447): right of A and right of D are MEDIUM
        assertThat(cell(s, 11, 0).getCellStyle().getBorderRight()).isEqualTo(BorderStyle.MEDIUM);
        assertThat(cell(s, 11, 3).getCellStyle().getBorderRight()).isEqualTo(BorderStyle.MEDIUM);
        assertThat(cell(s, 11, 5).getCellStyle().getBorderRight()).isEqualTo(BorderStyle.MEDIUM);
    }

    // ------------------------------------------------------------------ CONT-D-35

    @Test
    @DisplayName("CONT-D-35: fila 'Total' justo después de los conceptos, sin sangría, negrita, borde medio arriba y abajo")
    void contD35_filaTotal() {
        // GICR:413-432
        HSSFWorkbook wb = render(List.of(tc("A", "1", "10"), tc("B", "2", "20.5")), tc("Total", "3", "30.5"),
            List.of(), new byte[0]);
        HSSFSheet s = wb.getSheetAt(0);

        assertTexto(s, 13, 0, "Total");
        assertThat(font(wb, cell(s, 13, 0)).getBold()).isTrue();
        assertNumero(s, 13, 1, 3, FORMATO_CON_SIGNO);
        assertNumero(s, 13, 2, 31, FORMATO_CON_SIGNO);
        assertNumero(s, 13, 3, 0, FORMATO_VACIO);
        assertNumero(s, 13, 4, 0, FORMATO_VACIO);
        assertNumero(s, 13, 5, 0, FORMATO_VACIO);
        for (int col = 0; col <= 5; col++) {
            assertThat(cell(s, 13, col).getCellStyle().getBorderTop()).as("top col %d", col).isEqualTo(BorderStyle.MEDIUM);
            assertThat(cell(s, 13, col).getCellStyle().getBorderBottom()).as("bottom col %d", col).isEqualTo(BorderStyle.MEDIUM);
        }
        assertThat(s.getLastRowNum()).isEqualTo(13);
    }

    @Test
    @DisplayName("CONT-D-35: sin conceptos la fila 'Total' queda en la fila 12 (índice 11)")
    void contD35_sinConceptos_totalEnFila12() {
        HSSFWorkbook wb = render(List.of(), tc("Total", "0", "0"), List.of(), new byte[0]);
        HSSFSheet s = wb.getSheetAt(0);
        assertTexto(s, 11, 0, "Total");
        assertThat(s.getLastRowNum()).isEqualTo(11);
    }

    // ------------------------------------------------------------------ CONT-D-36

    @Test
    @DisplayName("CONT-D-36: hoja Operaciones - fila de grupos (10) y 17 sub-encabezados (11) con el relleno de String.format del legacy")
    void contD36_encabezadosOperaciones() {
        // GICR:480-504
        HSSFWorkbook wb = render(List.of(), tc("Total", "0", "0"), List.of(), new byte[0]);
        HSSFSheet s = wb.getSheetAt(1);

        assertTexto(s, 9, 0, "");
        assertTexto(s, 9, 7, "RV, RF, IIF y Sin Mercado");
        assertTexto(s, 9, 9, "Mercado Dólar");
        assertTexto(s, 9, 11, "Pactos");
        for (int col : new int[]{1, 2, 3, 4, 5, 6, 8, 10, 12, 13, 14, 15, 16}) assertBlanco(s, 9, col);

        String[] sub = {
            "Rut                       ",          // %-26s
            "Cliente                     ",        // %-28s
            "Porta\nfolio ",                       // %-12s
            "Tipo \nComp",                         // %-10s (already 10 chars)
            "Mercado     ",                        // %-12s
            "Folio \nComp.",                       // %-10s (12 chars, unchanged)
            "Fecha     ",                          // %-10s
            "Monto Operado", "Comisión\n Cobrada", "Monto Venta", "Monto \nCompra", "Fecha \nInicio",
            "Fecha \nTermino", "Monto Inicial", "Monto Final", "Perdida", "Ganancia"};
        for (int col = 0; col < sub.length; col++) assertTexto(s, 10, col, sub[col]);
        assertThat(s.getRow(10).getLastCellNum()).isEqualTo((short) 17);

        assertThat(merged(s)).containsExactlyInAnyOrder("A1:M5", "A10:G10", "H10:I10", "J10:K10", "L10:Q10");

        Cell h = cell(s, 10, 1);
        assertThat(font(wb, h).getBold()).isFalse();
        assertThat(h.getCellStyle().getAlignment()).isEqualTo(HorizontalAlignment.LEFT);
        assertThat(h.getCellStyle().getVerticalAlignment()).isEqualTo(VerticalAlignment.CENTER);
        assertThat(h.getCellStyle().getWrapText()).isTrue();
        assertThat(h.getCellStyle().getBorderTop()).isEqualTo(BorderStyle.THIN);
        // with no operations row 10 is the last table row: the full-table RegionUtil box (GICR:557-561)
        // turns its bottom border MEDIUM
        assertThat(h.getCellStyle().getBorderBottom()).isEqualTo(BorderStyle.MEDIUM);
        assertThat(cell(s, 9, 7).getCellStyle().getBorderBottom()).isEqualTo(BorderStyle.MEDIUM); // estiloGrupo
        assertThat(s.getLastRowNum()).isEqualTo(10);
    }

    // ------------------------------------------------------------------ CONT-D-37

    @Test
    @DisplayName("CONT-D-37: fila de comprobante - A..G identificación, H monto operado, I comisión; J..Q en blanco")
    void contD37_filaComprobante() {
        // GICR:507-534
        HSSFWorkbook wb = renderOps(comprobante(100).doc(RUT_PERSONA).nombre(NOMBRE_PERSONA).tipoDocumento("FAC")
            .tipoInstrumento("ACN").fecha("2025-03-05").operado("1234567.5").comision("1234.49").build());
        HSSFSheet s = wb.getSheetAt(1);

        assertTexto(s, 11, 0, RUT_PERSONA);
        assertTexto(s, 11, 1, NOMBRE_PERSONA);
        assertTexto(s, 11, 2, PORTAFOLIO);
        assertTexto(s, 11, 3, "FA");
        assertTexto(s, 11, 4, "AC");
        assertNumero(s, 11, 5, 100, null);
        assertTexto(s, 11, 6, "05-03-2025");
        assertNumero(s, 11, 7, 1234568, FORMATO_CON_SIGNO);
        assertNumero(s, 11, 8, 1234, FORMATO_CON_SIGNO);
        for (int col = 9; col <= 16; col++) assertBlanco(s, 11, col);
        assertThat(s.getRow(11).getLastCellNum()).isEqualTo((short) 17);
        assertThat(s.getLastRowNum()).isEqualTo(11);

        // RegionUtil borders GICR:557-570
        assertThat(cell(s, 11, 0).getCellStyle().getBorderLeft()).isEqualTo(BorderStyle.MEDIUM);
        assertThat(cell(s, 11, 6).getCellStyle().getBorderRight()).isEqualTo(BorderStyle.MEDIUM);
        assertThat(cell(s, 11, 8).getCellStyle().getBorderRight()).isEqualTo(BorderStyle.MEDIUM);
        assertThat(cell(s, 11, 10).getCellStyle().getBorderRight()).isEqualTo(BorderStyle.MEDIUM);
        assertThat(cell(s, 11, 16).getCellStyle().getBorderRight()).isEqualTo(BorderStyle.MEDIUM);
        assertThat(cell(s, 11, 16).getCellStyle().getBorderBottom()).isEqualTo(BorderStyle.MEDIUM);
        assertThat(cell(s, 11, 1).getCellStyle().getBorderRight()).isEqualTo(BorderStyle.NONE);
        // with data rows the sub-header keeps estiloCabecera's THIN bottom border (GICR:465)
        assertThat(cell(s, 10, 1).getCellStyle().getBorderBottom()).isEqualTo(BorderStyle.THIN);
    }

    @Test
    @DisplayName("CONT-D-37: montos negativos (notas de crédito ya negadas) se escriben negativos")
    void contD37_montosNegativos() {
        HSSFWorkbook wb = renderOps(comprobante(5).tipoDocumento("NCR").operado("-1500.5").comision("-10").build());
        HSSFSheet s = wb.getSheetAt(1);
        assertTexto(s, 11, 3, "NC");
        assertNumero(s, 11, 7, -1501, FORMATO_CON_SIGNO);
        assertNumero(s, 11, 8, -10, FORMATO_CON_SIGNO);
    }

    // ------------------------------------------------------------------ CONT-D-38

    @Test
    @DisplayName("CONT-D-38: fila dólar - J = MontoVenta, K = MontoCompra; H, I y L..Q en blanco")
    void contD38_filaDolar() {
        // GICR:535-541
        HSSFWorkbook wb = renderOps(dolar(200).tipoInstrumento("SPT").tipoDocumento("FAC").compraVenta("C")
            .compra("950000.4").venta("0").build());
        HSSFSheet s = wb.getSheetAt(1);

        assertTexto(s, 11, 0, RUT_EMPRESA);
        assertTexto(s, 11, 1, NOMBRE_EMPRESA);
        assertTexto(s, 11, 4, "DO");
        assertNumero(s, 11, 5, 200, null);
        assertTexto(s, 11, 6, "06-03-2025");
        assertBlanco(s, 11, 7);
        assertBlanco(s, 11, 8);
        assertNumero(s, 11, 9, 0, FORMATO_CON_SIGNO);
        assertNumero(s, 11, 10, 950000, FORMATO_CON_SIGNO);
        for (int col = 11; col <= 16; col++) assertBlanco(s, 11, col);
    }

    // ------------------------------------------------------------------ CONT-D-39

    @Test
    @DisplayName("CONT-D-39: fila pacto/término - Mercado 'Pactos', L/M fechas, N inicial, O final, P pérdida, Q ganancia (null -> 0)")
    void contD39_filaPacto() {
        // GICR:526 instanceof PactoTermino -> "Pactos"; GICR:542-551
        HSSFWorkbook wb = renderOps(
            pacto(300).tipoOperacion("VENPAC").tipoDocumento(null).tipoInstrumento("ACN")
                .fechaInicio("2025-03-03").fechaTermino("2025-03-10")
                .montoInicial("10000000").montoFinal("10025000.5").perdida("-25000.5").ganancia(null).build());
        HSSFSheet s = wb.getSheetAt(1);

        assertTexto(s, 11, 3, "VP");
        assertTexto(s, 11, 4, "Pactos");
        assertNumero(s, 11, 5, 300, null);
        assertTexto(s, 11, 6, "03-03-2025");
        for (int col = 7; col <= 10; col++) assertBlanco(s, 11, col);
        assertTexto(s, 11, 11, "03-03-2025");
        assertTexto(s, 11, 12, "10-03-2025");
        assertNumero(s, 11, 13, 10000000, FORMATO_CON_SIGNO);
        assertNumero(s, 11, 14, 10025001, FORMATO_CON_SIGNO);
        assertNumero(s, 11, 15, -25001, FORMATO_CON_SIGNO);
        assertNumero(s, 11, 16, 0, FORMATO_CON_SIGNO);
    }

    @Test
    @DisplayName("CONT-D-39: filas en el mismo orden de la lista de operaciones, una por operación")
    void contD39_ordenFilas() {
        HSSFWorkbook wb = renderOps(comprobante(1).build(), dolar(2).build(), pacto(3).build());
        HSSFSheet s = wb.getSheetAt(1);
        assertNumero(s, 11, 5, 1, null);
        assertNumero(s, 12, 5, 2, null);
        assertNumero(s, 13, 5, 3, null);
        assertThat(s.getLastRowNum()).isEqualTo(13);
    }

    // ------------------------------------------------------------------ CONT-D-40

    @ParameterizedTest(name = "CONT-D-40: TipoDocumento={0}, IdTipoOperacion={1} -> Tipo Comp ''{2}''")
    @CsvSource({
        "FAC,COMACC,FA", "FAE,COMACC,FD", "FOC,COMACC,FO", "FOE,COMACC,FT", "FEE,COMACC,FX", "FOX,COMACC,FX",
        "NCR,COMACC,NC", "NCE,COMACC,ND", "NDR,COMACC,NB", "NEO,COMACC,NT", "NEE,COMACC,NT",
        "COMFWD,COMACC,FW", "VENFWD,COMACC,FW", "FFM,COMACC,FM",
        "COMPAC,COMACC,CP", "VENPAC,COMACC,VP", "COMTER,COMACC,CT", "VENTER,COMACC,VT",
        // TipoDocumento not in map -> lookup by IdTipoOperacion -> else TipoDocumento itself
        "XYZ,COMPAC,CP", "XYZ,COMACC,XYZ", "NDE,COMACC,NDE", ",VENTER,VT"
    })
    void contD40_tipoComprobante(String tipoDocumento, String idTipoOperacion, String esperado) {
        // GICR:44-71 TIPO_COMPROBANTE_MAP; GICR:514-516
        HSSFWorkbook wb = renderOps(comprobante(1).tipoDocumento(tipoDocumento).tipoOperacion(idTipoOperacion).build());
        assertTexto(wb.getSheetAt(1), 11, 3, esperado);
    }

    @Test
    @DisplayName("CONT-D-40: TipoDocumento null y operación desconocida -> celda Tipo Comp en blanco")
    void contD40_tipoComprobanteNull_enBlanco() {
        // GICR:516 getOrDefault(op, null) -> setCellValue((String) null) -> BLANK
        HSSFWorkbook wb = renderOps(comprobante(1).tipoDocumento(null).tipoOperacion("COMACC").build());
        assertBlanco(wb.getSheetAt(1), 11, 3);
    }

    // ------------------------------------------------------------------ CONT-D-41

    @ParameterizedTest(name = "CONT-D-41: IdTipoInstrumento={0}, Clasificacion={1} -> Mercado ''{2}''")
    @CsvSource({
        "OSA,,AC", "SIM,,AC", "ACN,RV,AC", "ETN,,EN", "ETX,,ET", "MIL,,ST", "ACX,,AO", "CFX,,CO", "CFM,,CM",
        "FWD,,FW", "CFI,,CF", "SPT,,DO",
        // instrument not in map -> classification map -> else "Otros Conceptos"
        "BONO,RF,RF", "LH,IF,IF", "CFMX,FM,FM", "USD,SPT,DO", "XXX,RV,Otros Conceptos", ",,Otros Conceptos"
    })
    void contD41_mercado(String idTipoInstrumento, String clasificacion, String esperado) {
        // GICR:73-96 CLASIFICACION_MAP / TIPO_INSTRUMENTO_MAP; GICR:518-520, 526
        HSSFWorkbook wb = renderOps(comprobante(1).tipoInstrumento(idTipoInstrumento).clasificacion(clasificacion).build());
        assertTexto(wb.getSheetAt(1), 11, 4, esperado);
    }

    // ------------------------------------------------------------------ CONT-D-42

    @ParameterizedTest(name = "CONT-D-42: fecha ''{0}'' -> ''{1}''")
    @CsvSource({
        "2025-03-05,05-03-2025",
        "2025-02-30,02-03-2025",         // SimpleDateFormat is lenient: rolls over
        "2025-13-01,01-01-2026",         // lenient month overflow
        "2025-3-5,05-03-2025",
        "2025-03-05T10:00,05-03-2025",   // parse() ignores trailing text
        "05/03/2025,05/03/2025",         // unparseable -> raw string (catch branch)
        ",<BLANK>"                       // null -> setCellValue((String) null) -> blank
    })
    void contD42_formatoFecha(String entrada, String esperado) {
        // setFechaString GICR:599-611
        HSSFWorkbook wb = renderOps(comprobante(1).fecha(entrada).build(),
            pacto(2).fechaInicio(entrada).fechaTermino(entrada).build());
        HSSFSheet s = wb.getSheetAt(1);
        for (int[] rc : new int[][]{{11, 6}, {12, 11}, {12, 12}}) {
            if ("<BLANK>".equals(esperado)) assertBlanco(s, rc[0], rc[1]);
            else assertTexto(s, rc[0], rc[1], esperado);
        }
    }

    // ------------------------------------------------------------------ CONT-D-43

    @Test
    @DisplayName("CONT-D-43: con logo (PNG no vacío) cada hoja lleva una imagen anclada en A1; sin logo no hay imágenes")
    void contD43_logo() {
        // GICR:268-281 y GICR:310-323 (addPicture una vez por hoja => 2 imágenes en el workbook)
        HSSFWorkbook con = render(List.of(), tc("Total", "0", "0"), List.of(), pngUnPixel());
        assertThat(con.getAllPictures()).hasSize(2);
        for (int i = 0; i < 2; i++) {
            HSSFPatriarch p = con.getSheetAt(i).getDrawingPatriarch();
            assertThat(p).as("dibujo en hoja %d", i).isNotNull();
            assertThat(p.getChildren()).hasSize(1);
            HSSFPicture pic = (HSSFPicture) p.getChildren().get(0);
            assertThat(pic.getClientAnchor().getCol1()).isZero();
            assertThat(pic.getClientAnchor().getRow1()).isZero();
        }

        HSSFWorkbook sin = render(List.of(), tc("Total", "0", "0"), List.of(), new byte[0]);
        assertThat(sin.getAllPictures()).isEmpty();
        assertThat(sin.getSheetAt(0).getDrawingPatriarch()).isNull();
        assertThat(sin.getSheetAt(1).getDrawingPatriarch()).isNull();

        HSSFWorkbook nulo = render(List.of(), tc("Total", "0", "0"), List.of(), null);
        assertThat(nulo.getAllPictures()).isEmpty();
    }
}
