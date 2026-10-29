package cl.bcs.contabilidad.infrastructure.informe;

import cl.bcs.contabilidad.domain.model.clienterelacionado.Comprobante;
import cl.bcs.contabilidad.domain.model.clienterelacionado.OperacionClienteRelacionado;
import cl.bcs.contabilidad.domain.model.clienterelacionado.OperacionDolar;
import cl.bcs.contabilidad.domain.model.clienterelacionado.PactoTermino;
import cl.bcs.contabilidad.domain.model.clienterelacionado.ResultadoConsultaClienteRelacionado;
import cl.bcs.contabilidad.domain.model.clienterelacionado.TotalConcepto;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Renders the related-client report as a BIFF8 {@code .xls} (HSSF) workbook: legacy
 * {@code GenerarInformeClienteRelacionado} (GenerarInformeClienteRelacionado.java:44-96, 153-155, 212-623),
 * rules CONT-D-30..43.
 *
 * <p>Two sheets. "Conceptos": the title in A1:H5, a header in rows 10-11, one row per concept and the "Total" row.
 * "Operaciones": the title in A1:M5, group and column headers in rows 10-11, then one row per operation. Amounts are
 * rounded HALF_UP to whole units. The broker logo is placed in A1 of each sheet when present. Borders, merges and
 * number formats are the legacy ones.
 */
@Component
public class InformeClienteRelacionadoWriter {

    private static final DateTimeFormatter FORMATO_NOMBRE = DateTimeFormatter.ofPattern("ddMMyyyy_HHmmss");
    private static final String TITULO = "Informe de Operaciones con Clientes Relacionados\nDESDE %s HASTA %s";
    private static final int FILA_TABLA = 9;
    /** obtenerEstiloNumerico(0, wb, false) and (0, wb, true), GICR:577-597. */
    private static final String FORMATO_CON_SIGNO = "#,##0;-#,##0;-;-";
    private static final String FORMATO_VACIO = "#,##0;#,##0;;";

    /** GICR:44-71. */
    private static final Map<String, String> TIPO_COMPROBANTE = Map.ofEntries(
            Map.entry("FAC", "FA"), Map.entry("FAE", "FD"), Map.entry("FOC", "FO"), Map.entry("FOE", "FT"),
            Map.entry("FEE", "FX"), Map.entry("FOX", "FX"), Map.entry("NCR", "NC"), Map.entry("NCE", "ND"),
            Map.entry("NDR", "NB"), Map.entry("NEO", "NT"), Map.entry("NEE", "NT"), Map.entry("COMFWD", "FW"),
            Map.entry("VENFWD", "FW"), Map.entry("FFM", "FM"), Map.entry("COMPAC", "CP"), Map.entry("VENPAC", "VP"),
            Map.entry("COMTER", "CT"), Map.entry("VENTER", "VT"));
    /** GICR:73-96. */
    private static final Map<String, String> CLASIFICACION = Map.of("RF", "RF", "IF", "IF", "FM", "FM", "SPT", "DO");
    private static final Map<String, String> TIPO_INSTRUMENTO = Map.ofEntries(
            Map.entry("OSA", "AC"), Map.entry("SIM", "AC"), Map.entry("ACN", "AC"), Map.entry("ETN", "EN"),
            Map.entry("ETX", "ET"), Map.entry("MIL", "ST"), Map.entry("ACX", "AO"), Map.entry("CFX", "CO"),
            Map.entry("CFM", "CM"), Map.entry("FWD", "FW"), Map.entry("CFI", "CF"), Map.entry("SPT", "DO"));
    private static final String OTROS_CONCEPTOS = "Otros Conceptos";

    private static final String[] SUB_CONCEPTOS = {"Cantidad", "Monto \nM$", "Resultado \nM$", "Activo \nM$", "Pasivo \nM$"};
    /** GICR:490-493, with the padding of the legacy String.format("%-Ns", ..). */
    private static final String[] SUB_OPERACIONES = {
            String.format("%-26s", "Rut"), String.format("%-28s", "Cliente"), String.format("%-12s", "Porta\nfolio"),
            String.format("%-10s", "Tipo \nComp"), String.format("%-12s", "Mercado"), String.format("%-10s", "Folio \nComp."),
            String.format("%-10s", "Fecha"), "Monto Operado", "Comisión\n Cobrada", "Monto Venta", "Monto \nCompra",
            "Fecha \nInicio", "Fecha \nTermino", "Monto Inicial", "Monto Final", "Perdida", "Ganancia"};

    /** {@code InformeOperacionesClientesRelacionados_ddMMyyyy_HHmmss.xls} (GICR:153-155). */
    public static String nombreArchivo(LocalDateTime instante) {
        return "InformeOperacionesClientesRelacionados_" + instante.format(FORMATO_NOMBRE) + ".xls";
    }

    /**
     * The workbook bytes.
     *
     * @param fechaInicio yyyy-MM-dd, shown as dd-MM-yyyy in the title
     * @param logo        PNG of the broker; null or empty for none
     */
    public byte[] escribir(String fechaInicio, String fechaFin, byte[] logo, ResultadoConsultaClienteRelacionado resultado) {
        try (HSSFWorkbook wb = new HSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Estilos estilos = new Estilos(wb);
            String titulo = String.format(TITULO, fechaTitulo(fechaInicio), fechaTitulo(fechaFin));

            HSSFSheet conceptos = wb.createSheet("Conceptos");
            cabecera(wb, conceptos, titulo, 7, 4, logo, 2, 0.14);
            hojaConceptos(conceptos, estilos, resultado.conceptos(), resultado.totalizado());

            HSSFSheet operaciones = wb.createSheet("Operaciones");
            cabecera(wb, operaciones, titulo, 12, 6, logo, 1, 1.0);
            hojaOperaciones(operaciones, estilos, resultado.operaciones());

            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo generar el informe de clientes relacionados", e);
        }
    }

    // ------------------------------------------------------------------ title and logo (GICR:242-324)

    private static void cabecera(HSSFWorkbook wb, HSSFSheet sheet, String titulo, int ultimaColumnaTitulo,
                                 int ultimaCeldaEstilo, byte[] logo, int columnaFinLogo, double escalaLogo) {
        CellStyle estilo = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        estilo.setFont(font);
        estilo.setWrapText(true);
        estilo.setAlignment(HorizontalAlignment.CENTER);
        estilo.setVerticalAlignment(VerticalAlignment.CENTER);

        sheet.addMergedRegion(new CellRangeAddress(0, 4, 0, ultimaColumnaTitulo));
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue(titulo);
        cell.setCellStyle(estilo);
        for (int col = 1; col <= ultimaCeldaEstilo; col++) row.createCell(col).setCellStyle(estilo);
        row.setHeightInPoints(40);

        if (logo != null && logo.length > 0) {
            int indice = wb.addPicture(logo, Workbook.PICTURE_TYPE_PNG);
            ClientAnchor anchor = wb.getCreationHelper().createClientAnchor();
            anchor.setCol1(0);
            anchor.setRow1(0);
            anchor.setCol2(columnaFinLogo);
            anchor.setRow2(1);
            Picture picture = sheet.createDrawingPatriarch().createPicture(anchor, indice);
            picture.resize(escalaLogo, 2.0);
        }
    }

    // ------------------------------------------------------------------ Conceptos (GICR:326-452)

    private static void hojaConceptos(HSSFSheet sheet, Estilos e, List<TotalConcepto> conceptos, TotalConcepto total) {
        int fila = FILA_TABLA;
        Row grupos = sheet.createRow(fila++);
        texto(grupos.createCell(0), "Concepto", e.cabeceraConceptos);
        texto(grupos.createCell(1), "Total Transacción", e.cabeceraConceptos);
        texto(grupos.createCell(4), "Saldo", e.cabeceraConceptos);
        sheet.addMergedRegion(new CellRangeAddress(FILA_TABLA, FILA_TABLA + 1, 0, 0));
        sheet.addMergedRegion(new CellRangeAddress(FILA_TABLA, FILA_TABLA, 1, 3));
        sheet.addMergedRegion(new CellRangeAddress(FILA_TABLA, FILA_TABLA, 4, 5));

        Row sub = sheet.createRow(fila++);
        for (int i = 0; i < SUB_CONCEPTOS.length; i++) texto(sub.createCell(i + 1), SUB_CONCEPTOS[i], e.cabeceraConceptos);

        for (TotalConcepto c : conceptos) {
            Row row = sheet.createRow(fila++);
            texto(row.createCell(0), "   " + c.concepto(), e.concepto);
            filaTotales(row, c, e, e.dato);
        }

        Row row = sheet.createRow(fila++);
        texto(row.createCell(0), total.concepto(), e.concepto);
        filaTotales(row, total, e, e.concepto);
        CellRangeAddress filaTotal = new CellRangeAddress(row.getRowNum(), row.getRowNum(), 0, 5);
        caja(filaTotal, sheet);

        int ultima = fila - 1;
        caja(new CellRangeAddress(FILA_TABLA, ultima, 0, 5), sheet);
        RegionUtil.setBorderRight(BorderStyle.MEDIUM, new CellRangeAddress(FILA_TABLA, ultima, 0, 0), sheet);
        RegionUtil.setBorderRight(BorderStyle.MEDIUM, new CellRangeAddress(FILA_TABLA, ultima, 3, 3), sheet);
        for (int i = 0; i <= 5; i++) sheet.autoSizeColumn(i);
    }

    /** GICR:400-408 / 418-426: cells 1..5 get {@code base}, then the numbers replace it with a numeric style. */
    private static void filaTotales(Row row, TotalConcepto c, Estilos e, CellStyle base) {
        for (int i = 1; i <= 5; i++) row.createCell(i).setCellStyle(base);
        numero(row.getCell(1), c.cantidad(), e.conSigno);
        numero(row.getCell(2), c.monto(), e.conSigno);
        numero(row.getCell(3), c.resultado(), e.vacio);
        numero(row.getCell(4), c.activos(), e.vacio);
        numero(row.getCell(5), c.pasivos(), e.vacio);
    }

    // ------------------------------------------------------------------ Operaciones (GICR:454-575)

    private static void hojaOperaciones(HSSFSheet sheet, Estilos e, List<OperacionClienteRelacionado> operaciones) {
        int fila = FILA_TABLA;
        Row grupos = sheet.createRow(fila++);
        for (int i = 0; i <= 16; i++) grupos.createCell(i).setCellStyle(e.grupo);
        grupos.getCell(0).setCellValue("");
        grupos.getCell(7).setCellValue("RV, RF, IIF y Sin Mercado");
        grupos.getCell(9).setCellValue("Mercado Dólar");
        grupos.getCell(11).setCellValue("Pactos");

        Row sub = sheet.createRow(fila++);
        for (int i = 0; i < SUB_OPERACIONES.length; i++) texto(sub.createCell(i), SUB_OPERACIONES[i], e.cabeceraOperaciones);

        sheet.addMergedRegion(new CellRangeAddress(FILA_TABLA, FILA_TABLA, 0, 6));
        sheet.addMergedRegion(new CellRangeAddress(FILA_TABLA, FILA_TABLA, 7, 8));
        sheet.addMergedRegion(new CellRangeAddress(FILA_TABLA, FILA_TABLA, 9, 10));
        sheet.addMergedRegion(new CellRangeAddress(FILA_TABLA, FILA_TABLA, 11, 16));

        for (OperacionClienteRelacionado op : operaciones) {
            Row row = sheet.createRow(fila++);
            for (int i = 0; i <= 16; i++) row.createCell(i);
            row.getCell(0).setCellValue(op.documentoIdentidad());
            row.getCell(1).setCellValue(op.nombre());
            row.getCell(2).setCellValue(op.portafolio());
            row.getCell(3).setCellValue(tipoComprobante(op));
            row.getCell(4).setCellValue(op instanceof PactoTermino ? "Pactos" : mercado(op));
            if (op.folioOperacion() != null) row.getCell(5).setCellValue(op.folioOperacion());
            row.getCell(6).setCellValue(fechaCelda(op.fechaOperacion()));

            switch (op) {
                case Comprobante c -> {
                    numero(row.getCell(7), requerido(c.montoOperado(), "MontoOperado", op), e.conSigno);
                    numero(row.getCell(8), requerido(c.totalComision(), "TotalComision", op), e.conSigno);
                }
                case OperacionDolar d -> {
                    numero(row.getCell(9), requerido(d.montoVenta(), "MontoVenta", op), e.conSigno);
                    numero(row.getCell(10), requerido(d.montoCompra(), "MontoCompra", op), e.conSigno);
                }
                case PactoTermino p -> {
                    row.getCell(11).setCellValue(fechaCelda(p.fechaInicio()));
                    row.getCell(12).setCellValue(fechaCelda(p.fechaTermino()));
                    numero(row.getCell(13), requerido(p.montoInicial(), "MontoInicial", op), e.conSigno);
                    numero(row.getCell(14), requerido(p.montoFinal(), "MontoFinal", op), e.conSigno);
                    numero(row.getCell(15), p.perdida(), e.conSigno);
                    numero(row.getCell(16), p.ganancia(), e.conSigno);
                }
            }
        }

        int ultima = fila - 1;
        caja(new CellRangeAddress(FILA_TABLA, ultima, 0, 16), sheet);
        for (int col : new int[]{6, 8, 10}) {
            RegionUtil.setBorderRight(BorderStyle.MEDIUM, new CellRangeAddress(FILA_TABLA, ultima, col, col), sheet);
        }
        for (int i = 0; i <= 16; i++) sheet.autoSizeColumn(i);
    }

    /** GICR:514-516: by TipoDocumento, else by IdTipoOperacion, else the TipoDocumento itself (null -> blank cell). */
    private static String tipoComprobante(OperacionClienteRelacionado op) {
        String porDocumento = buscar(TIPO_COMPROBANTE, op.tipoDocumento());
        if (porDocumento != null) return porDocumento;
        String porOperacion = buscar(TIPO_COMPROBANTE, op.idTipoOperacion());
        return porOperacion != null ? porOperacion : op.tipoDocumento();
    }

    /** GICR:518-520: by IdTipoInstrumento, else by classification, else "Otros Conceptos". */
    private static String mercado(OperacionClienteRelacionado op) {
        String porInstrumento = buscar(TIPO_INSTRUMENTO, op.idTipoInstrumento());
        if (porInstrumento != null) return porInstrumento;
        String porClasificacion = buscar(CLASIFICACION, op.idClasificacionTipoInstrumento());
        return porClasificacion != null ? porClasificacion : OTROS_CONCEPTOS;
    }

    private static String buscar(Map<String, String> mapa, String clave) {
        return clave == null ? null : mapa.get(clave);
    }

    /**
     * Legacy defect kept: GICR:532-548 called {@code toString()} on these amounts, so a null one failed the whole
     * report (UI_ERROR_GENERAR_INFORME). It fails here too, with a message naming the field.
     */
    private static BigDecimal requerido(BigDecimal valor, String campo, OperacionClienteRelacionado op) {
        if (valor == null) {
            throw new InformeClienteRelacionadoException(campo + " nulo en la operación " + op.folioOperacion());
        }
        return valor;
    }

    // ------------------------------------------------------------------ cells

    private static void texto(Cell cell, String valor, CellStyle estilo) {
        cell.setCellValue(valor);
        cell.setCellStyle(estilo);
    }

    /** formatearCeldaNumerica, GICR:613-617: HALF_UP to 0 decimals; null is written as 0. */
    private static void numero(Cell cell, BigDecimal valor, CellStyle estilo) {
        BigDecimal v = valor == null ? BigDecimal.ZERO : valor;
        cell.setCellValue(v.setScale(0, RoundingMode.HALF_UP).doubleValue());
        cell.setCellStyle(estilo);
    }

    private static void caja(CellRangeAddress region, HSSFSheet sheet) {
        RegionUtil.setBorderTop(BorderStyle.MEDIUM, region, sheet);
        RegionUtil.setBorderBottom(BorderStyle.MEDIUM, region, sheet);
        RegionUtil.setBorderLeft(BorderStyle.MEDIUM, region, sheet);
        RegionUtil.setBorderRight(BorderStyle.MEDIUM, region, sheet);
    }

    // ------------------------------------------------------------------ dates

    /** Leading {@code y-M-d} digits, as a lenient SimpleDateFormat("yyyy-MM-dd").parse read them (trailing text ignored). */
    private static final Pattern FECHA = Pattern.compile("^(\\d+)-(\\d+)-(\\d+)");

    /**
     * setFechaString, GICR:599-611: yyyy-MM-dd shown as dd-MM-yyyy. Out-of-range days and months roll over, as the
     * lenient SimpleDateFormat did (2025-02-30 -> 02-03-2025); text that does not parse is written unchanged.
     */
    static String fechaCelda(String fecha) {
        if (fecha == null) return null;
        LocalDate d = fechaLenient(fecha);
        return d == null ? fecha : formatear(d);
    }

    /** obtenerFechaFormateada, GICR:619-623: same conversion, but an unparseable date is an error. */
    private static String fechaTitulo(String fecha) {
        LocalDate d = fecha == null ? null : fechaLenient(fecha);
        if (d == null) throw new InformeClienteRelacionadoException("Fecha inválida para el título: " + fecha);
        return formatear(d);
    }

    private static LocalDate fechaLenient(String fecha) {
        Matcher m = FECHA.matcher(fecha);
        if (!m.find()) return null;
        try {
            return LocalDate.of(Integer.parseInt(m.group(1)), 1, 1)
                    .plusMonths(Long.parseLong(m.group(2)) - 1)
                    .plusDays(Long.parseLong(m.group(3)) - 1);
        } catch (RuntimeException fueraDeRango) {
            return null;
        }
    }

    private static String formatear(LocalDate d) {
        return String.format(Locale.ROOT, "%02d-%02d-%04d", d.getDayOfMonth(), d.getMonthValue(), d.getYear());
    }

    // ------------------------------------------------------------------ styles

    /** The cell styles of the legacy, created once per workbook. */
    private static final class Estilos {
        final CellStyle cabeceraConceptos, dato, concepto, conSigno, vacio, cabeceraOperaciones, grupo;

        Estilos(HSSFWorkbook wb) {
            Function<Boolean, Font> fuente = negrita -> {
                Font f = wb.createFont();
                f.setBold(negrita);
                return f;
            };
            // GICR:330-340 headerStyle
            cabeceraConceptos = bordeFino(wb.createCellStyle());
            cabeceraConceptos.setFont(fuente.apply(true));
            cabeceraConceptos.setAlignment(HorizontalAlignment.CENTER);
            cabeceraConceptos.setVerticalAlignment(VerticalAlignment.CENTER);
            cabeceraConceptos.setWrapText(true);
            // GICR:342-355 dataStyle, conceptoStyle
            dato = bordeFino(wb.createCellStyle());
            concepto = bordeFino(wb.createCellStyle());
            concepto.setFont(fuente.apply(true));
            // GICR:357-358, 477
            conSigno = numerico(wb, FORMATO_CON_SIGNO);
            vacio = numerico(wb, FORMATO_VACIO);
            // GICR:458-475 estiloCabecera, estiloGrupo
            cabeceraOperaciones = bordeFino(wb.createCellStyle());
            cabeceraOperaciones.setFont(fuente.apply(false));
            cabeceraOperaciones.setAlignment(HorizontalAlignment.LEFT);
            cabeceraOperaciones.setVerticalAlignment(VerticalAlignment.CENTER);
            cabeceraOperaciones.setWrapText(true);
            grupo = wb.createCellStyle();
            grupo.cloneStyleFrom(cabeceraOperaciones);
            grupo.setBorderTop(BorderStyle.MEDIUM);
            grupo.setBorderBottom(BorderStyle.MEDIUM);
            grupo.setBorderLeft(BorderStyle.MEDIUM);
            grupo.setBorderRight(BorderStyle.MEDIUM);
        }

        private static CellStyle bordeFino(CellStyle s) {
            s.setBorderTop(BorderStyle.THIN);
            s.setBorderBottom(BorderStyle.THIN);
            s.setBorderLeft(BorderStyle.THIN);
            s.setBorderRight(BorderStyle.THIN);
            return s;
        }

        private static CellStyle numerico(HSSFWorkbook wb, String formato) {
            CellStyle s = wb.createCellStyle();
            s.setDataFormat(wb.createDataFormat().getFormat(formato));
            return s;
        }
    }
}
