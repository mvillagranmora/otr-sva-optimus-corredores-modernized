package cl.bcs.cuentainversion.service;

import cl.bcs.cuentainversion.reportes.ConfiguracionSistema;
import cl.bcs.cuentainversion.reportes.ExcelInformes;
import cl.bcs.cuentainversion.reportes.JsonReportes;
import cl.bcs.cuentainversion.reportes.Mensajeria;
import cl.bcs.cuentainversion.reportes.SqlReportes;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Collection summary report (legacy {@code GenerarInformeResumenCobro}), behaviours CTA-B-161..164.
 *
 * <p>Writes {@code <p_Ruta>/InformeResumenCobro_ddMMyyyy.xls} (HSSF, sheet "Resumen Cobro": client RUT, portfolio, accumulated
 * commission, client name) from {@link ConsultarCobroComisionRegistrosService} (module forced to CUS, no business rules) plus a
 * {@code .log}. Same failure handling as the daily patrimony report (README CTA-B-15).
 */
@Service
public class GenerarInformeResumenCobroService {

    private static final Logger log = LoggerFactory.getLogger(GenerarInformeResumenCobroService.class);
    private static final String[] ENCABEZADOS = {"Rut Cliente", "Portafolio", "Comisión Diaria Acumulada", "Nombre Cliente"};
    private static final int[] ANCHOS = {4000, 8000, 4000, 15000};

    private final SqlReportes sql;
    private final ConsultarCobroComisionRegistrosService registros;

    public GenerarInformeResumenCobroService(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlReportes(jdbc);
        this.registros = new ConsultarCobroComisionRegistrosService(jdbc.getJdbcTemplate().getDataSource());
    }

    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject params) throws Exception {
        return JsonReportes.escribir(generar(JsonReportes.leer(params, Entrada.class)));
    }

    Salida generar(Entrada entrada) throws Exception {
        log.info("INICIO::GenerarInformeResumenCobro");
        Salida out = new Salida();
        String fechaSistema = new ConfiguracionSistema(sql).cadena(entrada.p_IdEmpresa, ConfiguracionSistema.PLATAFORMA, "PLA.FechaSistema");
        Date fechaInforme = new SimpleDateFormat("yyyy-MM-dd").parse(fechaSistema);
        String nombre = "InformeResumenCobro_" + new SimpleDateFormat("ddMMyyyy").format(fechaInforme);

        out.p_out_ErroresContador = 0L;
        out.p_out_NombreArchivo = nombre + ".xls";
        out.p_out_NombreArchivoLog = nombre + ".log";
        out.p_out_RutaArchivo = entrada.p_Ruta + "/" + out.p_out_NombreArchivo;
        out.p_out_RutaArchivoLog = entrada.p_Ruta + "/" + out.p_out_NombreArchivoLog;
        out.p_out_Comentario = new Mensajeria(sql, entrada.p_IdEmpresa).texto("ARCHIVO_GENERADO_EXITOSAMENTE", Map.of());

        JsonObject consulta = new JsonObject();
        consulta.addProperty("p_IdEmpresa", entrada.p_IdEmpresa);
        consulta.addProperty("p_IdUsuario", entrada.p_IdUsuario);
        consulta.addProperty("p_IdModulo", "CUS");
        consulta.addProperty("p_UsaReglasNegocio", "N");
        consulta.addProperty("p_Cliente", entrada.p_IdCliente);
        consulta.addProperty("p_Portafolio", entrada.p_IdPortafolio);
        consulta.addProperty("p_FechaDesde", entrada.p_FechaDesde);
        consulta.addProperty("p_FechaHasta", entrada.p_FechaHasta);
        consulta.addProperty("p_IdProductoAgrupador", entrada.p_IdProductoAgrupador);
        consulta.addProperty("p_IdEstado", entrada.p_IdEstado);
        consulta.addProperty("p_Periodo", entrada.p_Periodo);
        List<JsonElement> filas = registros.execute(consulta).getAsJsonArray("P_OUT_TABLA").asList();

        try (FileOutputStream salida = new FileOutputStream(out.p_out_RutaArchivo); Workbook libro = new HSSFWorkbook()) {
            escribir(libro, filas);
            libro.write(salida);
        } catch (Exception e) {
            out.p_out_ErroresContador++;
            out.p_out_Comentario = e.getMessage();
        }

        // LEGACY DEFECT kept (see README CTA-B-15): the log write is not protected, a missing directory throws FileNotFoundException.
        ExcelInformes.escribirLog(out.p_out_RutaArchivoLog, out.p_out_Comentario);
        log.info("FIN::GenerarInformeResumenCobro");
        return out;
    }

    private static void escribir(Workbook libro, List<JsonElement> filas) {
        Sheet hoja = libro.createSheet("Resumen Cobro");
        for (int c = 0; c < ANCHOS.length; c++) {
            hoja.setColumnWidth(c, ANCHOS[c]);
        }
        CellStyle moneda = ExcelInformes.conBorde(libro, ExcelInformes.FORMATO_MONEDA);
        CellStyle letra = ExcelInformes.conBorde(libro, null);
        CellStyle encabezado = ExcelInformes.encabezado(libro);

        Row cabecera = hoja.createRow(0);
        for (int c = 0; c < ENCABEZADOS.length; c++) {
            Cell celda = cabecera.createCell(c);
            celda.setCellValue(ENCABEZADOS[c]);
            celda.setCellStyle(encabezado);
        }
        int fila = 0;
        for (JsonElement e : filas) {
            JsonObject d = e.getAsJsonObject();
            Row r = hoja.createRow(++fila);
            texto(r, 0, letra, d, "RutCliente");
            texto(r, 1, letra, d, "Portafolio");
            Cell monto = r.createCell(2);
            monto.setCellValue(d.get("MontoComision").getAsBigDecimal().doubleValue());
            monto.setCellStyle(moneda);
            texto(r, 3, letra, d, "Nombre");
        }
    }

    private static void texto(Row fila, int columna, CellStyle estilo, JsonObject d, String campo) {
        Cell c = fila.createCell(columna);
        JsonElement v = d.get(campo);
        c.setCellValue(v == null || v.isJsonNull() ? (String) null : v.getAsString());
        c.setCellStyle(estilo);
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
        public String p_FechaDesde;
        public String p_FechaHasta;
        public Long p_IdProductoAgrupador;
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
}
