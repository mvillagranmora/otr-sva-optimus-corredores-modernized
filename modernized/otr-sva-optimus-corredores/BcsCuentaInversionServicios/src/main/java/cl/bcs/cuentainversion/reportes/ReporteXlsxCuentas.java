package cl.bcs.cuentainversion.reportes;

import cl.bcs.utils.StringUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Common flow of the three "report as .xlsx" services (GenerarReporteAcreedores, GenerarReporteAntiguedadDeudaCTA,
 * GenerarReporteAntiguedadDeudaBolsaCTA; CTA-B-137..156): file location from the company configuration, broker code, column layout
 * from the platform file-format tables, header row, one row per result row, workbook written under
 * {@code <PLA.RutaBaseArchivos><RutaArchivosCta><name>.xlsx}.
 *
 * <p>The layout (column of each field, in Spanish) is data: {@code PLA.Archivo / ArchivoFormato / Formato}. Amounts are written as
 * TEXT (their plain decimal string), the sequence as a number. With Consolidar = S the portfolio column is dropped by the layout
 * query and the following positions shift by one.
 *
 * <p>LEGACY DEFECT kept (see README CTA-B-14): when the consultation fails the workbook is still written, with the header only, and
 * its path is returned together with the error messages.
 */
public final class ReporteXlsxCuentas {

    private static final Logger log = LoggerFactory.getLogger(ReporteXlsxCuentas.class);

    private static final int ALTO_FILA_CABECERA = 1000;
    private static final int ANCHO_COLUMNA = 6000;
    private static final int ANCHO_SECUENCIA = 3000;
    private static final int ANCHO_GRUPO_MONTOS = 8000;

    /** Output of the three services. */
    public static class Salida {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public String P_OUT_RUTAARCHIVO = "";
        public String P_OUT_CODIGOCORREDOR = "";
    }

    /** SQL resources of a report and the two ordering quirks of the legacy classes. */
    public record Recursos(String raizArchivo, String codigoCorredora, String formato, boolean hojaAntesDelFormato) {
    }

    /** Where the files go: {@code ruta} is written to, {@code modulo} is what the caller gets back. */
    public static class RaizArchivo {
        public String RutaArchivo = "";
        public String RutaModulo = "";
    }

    public static class Corredora {
        public String IdEmpresa;
        public String CodigoCorredora;
    }

    public static class CampoArchivo {
        public String CodigoCampo;
        public String CampoNombreCompleto;
        public int Posicion;
    }

    /** Result of the consultation a report is built on. */
    public static class Consulta<T> {
        public final List<T> filas;
        public final long errores;
        public final List<MensajeSalida> mensajes;

        public Consulta(List<T> filas, long errores, List<MensajeSalida> mensajes) {
            this.filas = filas;
            this.errores = errores;
            this.mensajes = mensajes;
        }

        /** The consultation threw: the legacy printed the stack trace and went on with an empty, error-free answer. */
        public static <T> Consulta<T> vacia() {
            return new Consulta<>(new ArrayList<>(), 0L, new ArrayList<>());
        }
    }

    /** Writes the cells of one data row; {@code posiciones} maps the layout field codes to their column. */
    @FunctionalInterface
    public interface EscritorFila<T> {
        void escribir(Row fila, Map<String, Integer> posiciones, T dato, long secuencia);
    }

    private final SqlReportes sql;
    private final Mensajeria mensajes;
    private final Recursos recursos;

    public ReporteXlsxCuentas(SqlReportes sql, Mensajeria mensajes, Recursos recursos) {
        this.sql = sql;
        this.mensajes = mensajes;
        this.recursos = recursos;
    }

    /**
     * @param consolidar     "S" / "N": decides the layout and the width of the amount columns
     * @param consulta       the consultation (runs after the layout is known, like the legacy)
     * @param escritor       writes a data row
     */
    public <T> Salida generar(Long idEmpresa, String fechaSolicitud, String consolidar, String nombreInterno,
                              Supplier<Consulta<T>> consulta, EscritorFila<T> escritor) throws Exception {
        Salida out = new Salida();
        RaizArchivo raiz = sql.consultar(SqlReportes.recurso(recursos.raizArchivo()), Map.of("IdEmpresa", idEmpresa), RaizArchivo.class)
                .stream().findFirst().orElse(new RaizArchivo());
        if (StringUtils.isNullOrBlank(raiz.RutaArchivo) || StringUtils.isNullOrBlank(raiz.RutaModulo)) {
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_MENSAJES.add(mensajes.error("FALTA_CONFIGURACION_RUTA_ARCHIVO"));
            return out;
        }

        String codigoCorredora = codigoCorredora(idEmpresa);
        if (codigoCorredora.trim().length() == 0) {
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_MENSAJES.add(mensajes.error("CODIGO_CORREDOR_NO_EXISTE"));
            return out;
        }

        String nombreArchivo = nombreInterno + ".xlsx";
        String ruta = crear(raiz, nombreArchivo, codigoCorredora, idEmpresa, fechaSolicitud, consolidar, consulta, escritor, out);
        if (ruta.trim().length() == 0) {
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_MENSAJES.add(mensajes.error("UI_ERROR_AL_GENERAR_REPORTE"));
            return out;
        }
        out.P_OUT_RUTAARCHIVO = ruta;
        out.P_OUT_CODIGOCORREDOR = "CODIGOCORREDOR_" + codigoCorredora;
        return out;
    }

    private String codigoCorredora(Long idEmpresa) {
        List<Corredora> r = sql.consultar(SqlReportes.recurso(recursos.codigoCorredora()), Map.of("IdEmpresa", idEmpresa), Corredora.class);
        return r.size() == 1 ? r.get(0).CodigoCorredora : "";
    }

    private <T> String crear(RaizArchivo raiz, String nombreArchivo, String codigoCorredora, Long idEmpresa, String fechaSolicitud,
                             String consolidar, Supplier<Consulta<T>> consulta, EscritorFila<T> escritor, Salida out) throws Exception {
        Workbook libro = new XSSFWorkbook();
        Sheet hoja = null;
        if (recursos.hojaAntesDelFormato()) {
            hoja = libro.createSheet(nombreHoja(codigoCorredora, fechaSolicitud));
        }
        Map<String, CampoArchivo> campos = campos(idEmpresa, consolidar);
        if (campos.isEmpty()) {
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_MENSAJES.add(mensajes.error("FALTA_CONFIGURACION_ARCHIVO_FORMATO"));
            return "";
        }
        if (hoja == null) {
            hoja = libro.createSheet(nombreHoja(codigoCorredora, fechaSolicitud));
        }
        cabecera(libro, hoja, campos, consolidar);
        cuerpo(hoja, campos, consulta, escritor, out);

        try (OutputStream os = Files.newOutputStream(Path.of(raiz.RutaArchivo + nombreArchivo)); libro) {
            libro.write(os);
            return raiz.RutaModulo + nombreArchivo;
        } catch (IOException e) {
            log.error("cannot write the report {}: {}", raiz.RutaArchivo + nombreArchivo, e.toString());
            return "";
        }
    }

    private Map<String, CampoArchivo> campos(Long idEmpresa, String consolidar) throws Exception {
        Map<String, Object> p = new HashMap<>();
        p.put("IdEmpresa", idEmpresa);
        p.put("Consolidado", consolidar);
        Map<String, CampoArchivo> campos = new HashMap<>();
        for (CampoArchivo c : sql.consultar(SqlReportes.recurso(recursos.formato()), p, CampoArchivo.class)) {
            if (campos.put(c.CodigoCampo, c) != null) {
                throw new Exception("key field is not unique");
            }
        }
        return campos;
    }

    private static String nombreHoja(String codigoCorredora, String fechaSolicitud) {
        String[] f = fechaSolicitud.split("-");
        return codigoCorredora + "_" + f[2] + f[1] + f[0] + "_cuentas_por_cobrar";
    }

    private static void cabecera(Workbook libro, Sheet hoja, Map<String, CampoArchivo> campos, String consolidar) {
        CellStyle estilo = libro.createCellStyle();
        estilo.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estilo.setAlignment(HorizontalAlignment.CENTER);
        estilo.setVerticalAlignment(VerticalAlignment.CENTER);
        estilo.setBorderLeft(BorderStyle.THIN);
        estilo.setBorderRight(BorderStyle.THIN);
        estilo.setBorderBottom(BorderStyle.THIN);

        Row fila = hoja.createRow(0);
        fila.setHeight((short) ALTO_FILA_CABECERA);
        for (CampoArchivo c : campos.values()) {
            int p = c.Posicion;
            if (p == 0) {
                hoja.setColumnWidth(p, ANCHO_SECUENCIA);
            } else if (p >= 5 && p <= 7 && "S".equalsIgnoreCase(consolidar)) {
                hoja.setColumnWidth(p, ANCHO_GRUPO_MONTOS);
            } else if (p >= 6 && p <= 8 && "N".equalsIgnoreCase(consolidar)) {
                hoja.setColumnWidth(p, ANCHO_GRUPO_MONTOS);
            } else {
                hoja.setColumnWidth(p, ANCHO_COLUMNA);
            }
            Cell celda = fila.createCell(p);
            celda.setCellStyle(estilo);
            celda.setCellValue(c.CampoNombreCompleto);
        }
    }

    private <T> void cuerpo(Sheet hoja, Map<String, CampoArchivo> campos, Supplier<Consulta<T>> consulta, EscritorFila<T> escritor, Salida out) {
        Consulta<T> resultado = consulta.get();
        if (resultado.errores > 0 || !resultado.mensajes.isEmpty()) {
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_MENSAJES = resultado.mensajes;
        }
        List<T> filas = resultado.errores == 0 ? resultado.filas : new ArrayList<>();
        if (out.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0 && !filas.isEmpty()) {
            Map<String, Integer> posiciones = new HashMap<>();
            campos.forEach((codigo, c) -> posiciones.put(codigo, c.Posicion));
            long secuencia = 1;
            int numeroFila = 1;
            for (T dato : filas) {
                escritor.escribir(hoja.createRow(numeroFila++), posiciones, dato, secuencia++);
            }
        }
    }

    /** Number of ERROR messages (the services recompute the counter from the messages before answering). */
    public static long contarErrores(Salida s) {
        return MensajeSalida.contarErrores(s.P_OUT_MENSAJES);
    }
}
