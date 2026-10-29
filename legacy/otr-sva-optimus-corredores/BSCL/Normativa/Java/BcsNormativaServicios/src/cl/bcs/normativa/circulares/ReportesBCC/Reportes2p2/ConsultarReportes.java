package cl.bcs.normativa.circulares.ReportesBCC.Reportes2p2;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import datadog.trace.api.Trace;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import static cl.bcs.normativa.circulares.ReportesBCC.Reportes2p2.utils.ReporteUtils.*;

/**
 * Servicio para consultar reportes SICAM / Merrill Lynch
 * 
 */
public class ConsultarReportes implements JavaCall {

    static final Logger logger = LogManager.getLogger(ConsultarReportes.class);

    /**
     * Llamadas SQL
     */
    private final String SQL_SelectReporte = loadResourceToString(
            "cl/bcs/normativa/circulares/ReportesBCC/Reportes2p2/sql/ConsultarReportes_Reporte_select.sql");

    /**
     * Clase de entrada para el reporte 2.2
     */
    public static class EntradaConsultaReporte2p2 extends BcsPojo {
        public Long IdEmpresa;
        public String IdUsuario;
        public String ClaveUsuario;
        public String ClaveReporte;
        public String RutEntidadMCF;
        public String Fecha;
        public String MonedaSaldo;
        
        public EntradaConsultaReporte2p2() {
            this.IdEmpresa = 1L;
            this.IdUsuario = "";
            this.ClaveUsuario = "";
            this.ClaveReporte = "";
        }
    }

    /**
     * Clase de salida para el reporte 2.2
     */
    public static class ResultadoConsultaReporte2p2 extends BcsPojo {
        public List<Datos> Resultado;
        public int codigoRes;
        public String mensajeRes;
    }

    public static class Datos extends BcsPojo {
        public Long ID;
        public Long IdDocumento;
        public String FuenteDatos;
        public String Estado;
        public String EstadoSICAM;
        public String TipoRegistro;
        public String EventoReporte;
        public String TipoOperacion;
        public String RutEntidadMCF;
        public Date Fecha; 
        public BigDecimal SaldoCuentaCambioConversion; 
        public BigDecimal SaldoMonedaOriginal; 
        public String MonedaSaldo; 
        public BigDecimal Paridad;
    }

    /**
     * Carga el contenido de un recurso SQL como una cadena de texto.
     * 
     * @param con               Conexion a la base de datos.
     * @param EntradaReporte2p2 entrada con los parametros del reporte.
     * @return Contenido del recurso como una cadena de texto.
     * @throws Exception Si ocurre un error al cargar el recurso.
     */
    @Trace
    public ResultadoConsultaReporte2p2 consultarReporte(Connection con, EntradaConsultaReporte2p2 in) throws Exception {
        logger.info("Consultando reporte 2.2");
        logger.info("Parametros de entrada: {}", in);
        ResultadoConsultaReporte2p2 out = new ResultadoConsultaReporte2p2();
        HashMap<String, Object> parametros = new HashMap<>();
        SQL sql = new SQL();

        if (in.RutEntidadMCF != null && !in.RutEntidadMCF.trim().isEmpty()) {
            String rut = in.RutEntidadMCF.trim();
            if (!validacionRut(rut)) {
                logger.error("Rut Entidad MCF invalido: {}", rut);
                out.Resultado = Collections.emptyList();
                out.codigoRes = 400;
                out.mensajeRes = "RutEntidadMCF invalido.";
                return out;
            }
            in.RutEntidadMCF = rut;
        } else {
            in.RutEntidadMCF = null;
        }

        java.sql.Date fechaSql = null;
        try {
            if (in.Fecha == null || in.Fecha.trim().isEmpty()) {
                out.Resultado = Collections.emptyList();
                out.codigoRes = 400;
                out.mensajeRes = "Fecha es obligatoria (yyyy-MM-dd).";
                logger.error("Fecha obligatoria ausente");
                return out;
            }
            LocalDate ld = LocalDate.parse(in.Fecha.trim(),
                DateTimeFormatter.ofPattern("uuuu-MM-dd"));
            LocalDate ultimoDelMes = ld.with(TemporalAdjusters.lastDayOfMonth());
            if (!ld.equals(ultimoDelMes)) {
                logger.error("Fecha no es ultimo dia del mes: {}, esperado: {}", in.Fecha, ultimoDelMes);
                out.Resultado = Collections.emptyList();
                out.codigoRes = 400;
                out.mensajeRes = "Fecha debe ser el ultimo dia del mes.";
                return out;
            }
            fechaSql = java.sql.Date.valueOf(ld);
        } catch (DateTimeParseException e) {
            logger.error("Fecha invalida: {}", in.Fecha);
            out.Resultado = Collections.emptyList();
            out.codigoRes = 400;
            out.mensajeRes = "Fecha invalida, formato debe ser yyyy-MM-dd.";
            return out;
        }


        if (in.MonedaSaldo != null && !in.MonedaSaldo.trim().isEmpty()) {
            String mon = in.MonedaSaldo.trim().toUpperCase(Locale.ROOT);
            if (!(mon.length() == 3 && isIso4217(mon))) {
                logger.error("MonedaSaldo invalida: {}", in.MonedaSaldo);
                out.Resultado = Collections.emptyList();
                out.codigoRes = 400;
                out.mensajeRes = "MonedaSaldo invalida; debe ser ISO 4217 (3 letras).";
                return out;
            }
            in.MonedaSaldo = mon;
        } else {
            in.MonedaSaldo = null;
        }

        parametros.put("rutEntidadMCF", in.RutEntidadMCF);
        parametros.put("fecha", fechaSql);
        parametros.put("monedaSaldo", in.MonedaSaldo);

        List<Datos> rows = sql.query(
                con,
                SQL_SelectReporte,
                parametros,
                Datos.class);

        if (!rows.isEmpty()) {
            logger.info("Se encontraron {} resultados", rows.size());
        } else {
            logger.info("No se encontraron resultados para la consulta");
        }

        out.Resultado = rows;
        out.codigoRes = rows.isEmpty()
                ? 400
                : 200;
        out.mensajeRes = rows.isEmpty()
                ? "No se encontraron registros"
                : "OK";

        logger.info("Resultado de la consulta: {}", rows);

        return out;
    }

    /**
     * Carga el contenido de un recurso SQL como una cadena de texto.
     *
     * @param conn Conexion a la base de datos.
     * @param in   Entrada en formato JSON que contiene los parametros del reporte.
     * @return Un objeto JSON que contiene el resultado de la operacion.
     */
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        Gson gson = new GsonBuilder().create();
        EntradaConsultaReporte2p2 entrada = gson.fromJson(in, EntradaConsultaReporte2p2.class);
        ResultadoConsultaReporte2p2 out = consultarReporte(conn, entrada);

        JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
        return dataout;
    }

    /**
     * Metodo principal para probar la consulta de reportes.
     * 
     * @param args Argumentos de linea de comandos (no se utilizan).
     */
    public static void main(String[] args) {
        try (Connection con = PlataformaUtils.getPropertiesConnection("desa01",
                "C:\\Users\\vaguilera_ext\\Documents\\GIT_OPTIMUS\\optimus-corredores\\connections.properties")) {
            try {
                Gson gson = Utils.getGsonBuilder();
                String json = "{\n" +        		
                        "    \"RutEntidadMCF\": \"20720657-1\",\n" +
                        "    \"Fecha\": \"2025-09-31\",\n" +
                        "    \"MonedaSaldo\": \"USD\"\n" +
                        "}";
                EntradaConsultaReporte2p2 Objin = gson.fromJson(json, EntradaConsultaReporte2p2.class);

                ConsultarReportes service = new ConsultarReportes();

                service.consultarReporte(con, Objin);
                con.rollback();
            } catch (Exception e) {
                logger.error("Error al consultar los reportes", e);
                con.rollback();
            }
        } catch (Exception e) {
            logger.error("Error al conectar a la base de datos", e);
        }
    }
}
