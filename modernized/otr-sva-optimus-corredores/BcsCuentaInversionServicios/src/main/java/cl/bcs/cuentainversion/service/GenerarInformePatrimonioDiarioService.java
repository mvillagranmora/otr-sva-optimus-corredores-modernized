package cl.bcs.cuentainversion.service;

import cl.bcs.cuentainversion.reportes.ConfiguracionSistema;
import cl.bcs.cuentainversion.reportes.ExcelInformes;
import cl.bcs.cuentainversion.reportes.JsonReportes;
import cl.bcs.cuentainversion.reportes.Mensajeria;
import cl.bcs.cuentainversion.reportes.SqlReportes;
import com.google.gson.JsonObject;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Daily patrimony report (legacy {@code GenerarInformePatrimonioDiario}), behaviours CTA-B-157..160.
 *
 * <p>Writes {@code <p_Ruta>/InformePatrimonioDiario_ddMMyyyy.xlsx} (the date is the SYSTEM date, not the query range) and a
 * {@code .log} with the dictionary message {@code ARCHIVO_GENERADO_EXITOSAMENTE}. A failure while writing the workbook is
 * reported in the result (error counter and the exception text as comment), but the log file is written without protection.
 */
@Service
public class GenerarInformePatrimonioDiarioService {

    private static final Logger log = LoggerFactory.getLogger(GenerarInformePatrimonioDiarioService.class);
    private static final String SQL_PATRIMONIO = SqlReportes.recurso("ConsultaInformePatrimonioDario_SelectDetallado");
    private static final String[] ENCABEZADOS = {"Fecha", "Rut Cliente", "Portafolio", "Mercado", "Nemo", "Cantidad Custodia",
            "Cantidad Garantia", "Precio Mercado", "Monto valorización", "Factor comisión", "Factor nemo", "Comisión diaria acumulada"};
    private static final int[] ANCHOS = {4000, 4000, 8000, 4000, 4000, 8000, 4000, 4000, 4000, 4000, 4000, 4000};

    private final SqlReportes sql;

    public GenerarInformePatrimonioDiarioService(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlReportes(jdbc);
    }

    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject params) throws Exception {
        return JsonReportes.escribir(generar(JsonReportes.leer(params, Entrada.class)));
    }

    Salida generar(Entrada entrada) throws Exception {
        log.info("INICIO::GenerarInformePatrimonioDiario");
        Salida out = new Salida();
        String fechaSistema = new ConfiguracionSistema(sql).cadena(entrada.p_IdEmpresa, ConfiguracionSistema.PLATAFORMA, "PLA.FechaSistema");
        Date fechaInforme = new SimpleDateFormat("yyyy-MM-dd").parse(fechaSistema);
        String nombre = "InformePatrimonioDiario_" + new SimpleDateFormat("ddMMyyyy").format(fechaInforme);

        out.p_out_ErroresContador = 0L;
        out.p_out_NombreArchivo = nombre + ".xlsx";
        out.p_out_NombreArchivoLog = nombre + ".log";
        out.p_out_RutaArchivo = entrada.p_Ruta + "/" + out.p_out_NombreArchivo;
        out.p_out_RutaArchivoLog = entrada.p_Ruta + "/" + out.p_out_NombreArchivoLog;
        out.p_out_Comentario = new Mensajeria(sql, entrada.p_IdEmpresa).texto("ARCHIVO_GENERADO_EXITOSAMENTE", Map.of());

        Map<String, Object> p = new HashMap<>();
        p.put("p_Cliente", entrada.p_IdCliente);
        p.put("p_Portafolio", entrada.p_IdPortafolio);
        p.put("p_FechaDesde", entrada.p_FechaDesde);
        p.put("p_FechaHasta", entrada.p_FechaHasta);
        p.put("p_EstadoComision", entrada.p_IdEstado);
        p.put("p_ProductoAgrupador", entrada.p_IdProductoAgrupador);
        p.put("p_Periodo", entrada.p_Periodo);
        List<Detalle> filas = sql.consultar(SQL_PATRIMONIO, p, Detalle.class);

        try (FileOutputStream salida = new FileOutputStream(out.p_out_RutaArchivo); Workbook libro = new XSSFWorkbook()) {
            escribir(libro, filas);
            libro.write(salida);
        } catch (Exception e) {
            out.p_out_ErroresContador++;
            out.p_out_Comentario = e.getMessage();
        }

        // LEGACY DEFECT kept (see README CTA-B-15): the log write is not protected, a missing directory throws FileNotFoundException.
        ExcelInformes.escribirLog(out.p_out_RutaArchivoLog, out.p_out_Comentario);
        log.info("FIN::GenerarInformePatrimonioDiario");
        return out;
    }

    private static void escribir(Workbook libro, List<Detalle> filas) {
        Sheet hoja = libro.createSheet("Patrimonio Diario");
        for (int c = 0; c < ANCHOS.length; c++) {
            hoja.setColumnWidth(c, ANCHOS[c]);
        }
        CellStyle moneda = ExcelInformes.conBorde(libro, ExcelInformes.FORMATO_MONEDA);
        CellStyle precio = ExcelInformes.conBorde(libro, "_($* #,##0.00_);_($* (#,##0.00);_($* \"-\"??_);_(@_)");
        CellStyle cantidad = ExcelInformes.conBorde(libro, "#,##0");
        CellStyle porcentaje = ExcelInformes.conBorde(libro, "#,##0.00%");
        CellStyle letra = ExcelInformes.conBorde(libro, null);
        CellStyle fecha = ExcelInformes.conBorde(libro, "yyyyMMdd");
        CellStyle encabezado = ExcelInformes.encabezado(libro);

        Row cabecera = hoja.createRow(0);
        for (int c = 0; c < ENCABEZADOS.length; c++) {
            Cell celda = cabecera.createCell(c);
            celda.setCellValue(ENCABEZADOS[c]);
            celda.setCellStyle(encabezado);
        }

        int fila = 0;
        for (Detalle d : filas) {
            Row r = hoja.createRow(++fila);
            celda(r, 0, fecha).setCellValue(d.Fecha);
            celda(r, 1, letra).setCellValue(d.RutCliente);
            celda(r, 2, letra).setCellValue(d.Portafolio);
            celda(r, 3, letra).setCellValue(d.Mercado);
            celda(r, 4, letra).setCellValue(d.Nemo);
            celda(r, 5, cantidad).setCellValue(d.CantidadCustodia.doubleValue());
            celda(r, 6, cantidad).setCellValue(d.CantidadGarantia.doubleValue());
            celda(r, 7, precio).setCellValue(d.PrecioMercado.doubleValue());
            celda(r, 8, moneda).setCellValue(d.MontoValorizado.doubleValue());
            celda(r, 9, porcentaje).setCellValue(d.FactorComision.doubleValue());
            if (d.FactorNemo.equals("N/A")) {
                celda(r, 10, letra).setCellValue(d.FactorNemo);
            } else {
                celda(r, 10, porcentaje).setCellValue(Integer.valueOf(d.FactorNemo));
            }
            celda(r, 11, moneda).setCellValue(d.MontoComision.doubleValue());
        }
    }

    private static Cell celda(Row fila, int columna, CellStyle estilo) {
        Cell c = fila.createCell(columna);
        c.setCellStyle(estilo);
        return c;
    }

    // ---------------------------------------------------------------------------------------------- JSON contract

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_Ruta;
        public Long p_IdCliente;
        public Long p_IdPortafolio;
        public String p_IdEstado;
        public Long p_IdProductoAgrupador;
        public String p_FechaDesde;
        public String p_FechaHasta;
        public Long p_Periodo;
    }

    public static class Salida {
        public String p_out_NombreArchivoLog;
        public String p_out_RutaArchivoLog;
        public String p_out_NombreArchivo;
        public String p_out_RutaArchivo;
        public Long p_out_ErroresContador;
        public String p_out_Comentario;
        public List<String> p_out_ArchivoSecundario;
    }

    /** Row of the query. */
    public static class Detalle {
        public Date Fecha;
        public String RutCliente;
        public String Portafolio;
        public BigDecimal FactorComision;
        public BigDecimal MontoComision;
        public BigDecimal MontoValorizado;
        public BigDecimal CantidadCustodia;
        public BigDecimal CantidadGarantia;
        public BigDecimal PrecioMercado;
        public String Nemo;
        public String Mercado;
        public String FactorNemo;
    }
}
