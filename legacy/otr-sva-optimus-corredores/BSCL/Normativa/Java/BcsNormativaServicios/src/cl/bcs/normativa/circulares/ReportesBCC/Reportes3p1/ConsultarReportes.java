package cl.bcs.normativa.circulares.ReportesBCC.Reportes3p1;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

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
            "cl/bcs/normativa/circulares/ReportesBCC/Reportes3p1/sql/ConsultarReportes_Reporte_select.sql");

    /**
     * Clase de entrada para el reporte 3.1
     */
    public static class EntradaConsultaReporte3p1 extends BcsPojo {
        public Long idEmpresa;
        public String idUsuario;
        public String claveUsuario;
        public String claveReporte;
        public String rutReportante;
        public String fechaOperacion;
        public String horaOperacion;
        public String identificador;
        
        public EntradaConsultaReporte3p1() {
            this.idEmpresa = 1L;
            this.idUsuario = "";
            this.claveUsuario = "";
            this.claveReporte = "";
        }
    }

    /**
     * Clase de salida para el reporte 3.1
     */
    public static class ResultadoConsultaReporte3p1 extends BcsPojo {
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
        public String RUTReportante;
        public Date FechaOperacion;
        public String HoraOperacion;
        public String TipoOperacion;
        public String EventoReporte;
        public String Identificador;
        public String RUTContraparte;
        public String LEIContraparte;
        public String NombreContraparte;
        public String PlataformaNegociacion;
        public BigDecimal MontoMonedaComprada;
        public BigDecimal MontoMonedaVendida;
        public BigDecimal TipoCambio;
    }

    @Trace
    public ResultadoConsultaReporte3p1 consultarReporte(Connection con, EntradaConsultaReporte3p1 in) throws Exception {
        logger.info("Consultando reporte");
        logger.info("Parametros de entrada: {}", in);
        ResultadoConsultaReporte3p1 out = new ResultadoConsultaReporte3p1();
        HashMap<String, Object> parametros = new HashMap<>();
        SQL sql = new SQL();

        if (in.rutReportante != null && !in.rutReportante.isEmpty()) {
            String soloDigitos = in.rutReportante.replaceAll("\\D", "");
            if (soloDigitos.length() > 11 || !ValidacionRut(in.rutReportante)) {
                logger.error("RUT invalido: maximo 11 digitos y digito verificador correcto");
                out.Resultado = Collections.emptyList();
                out.codigoRes = 400;
                out.mensajeRes = "RUT invalido: maximo 11 digitos y digito verificador correcto";
                return out;
            }
        } else {
            in.rutReportante = null;
        }

        Date fechaOp = null;
        if (in.fechaOperacion != null && !in.fechaOperacion.isEmpty()) {
            try {
                LocalDate ld = LocalDate.parse(
                        in.fechaOperacion,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                fechaOp = java.sql.Date.valueOf(ld);
            } catch (DateTimeParseException e) {
                logger.error("Fecha invalida: {}", in.fechaOperacion);
                out.Resultado = Collections.emptyList();
                out.codigoRes = 400;
                out.mensajeRes = "Fecha invalida";
                return out;
            }
        }

        if (in.horaOperacion != null && !in.horaOperacion.isEmpty()) {
            try {
                LocalTime.parse(
                        in.horaOperacion,
                        DateTimeFormatter.ofPattern("HH:mm:ss"));
            } catch (DateTimeParseException e) {
                logger.error("Hora invalida: {}", in.horaOperacion);
                out.Resultado = Collections.emptyList();
                out.codigoRes = 400;
                out.mensajeRes = "Hora invalida";
                return out;
            }
        } else {
            in.horaOperacion = null;
        }

        if (in.identificador != null) {
            if (in.identificador.isEmpty() || in.identificador.length() > 20) {
                logger.error("Identificador invalido: maximo 20 caracteres");
                out.Resultado = Collections.emptyList();
                out.codigoRes = 400;
                out.mensajeRes = "Identificador invalido: maximo 20 caracteres";
                return out;
            }
        } else {
            in.identificador = null;
        }

        parametros.put("rutReportante", in.rutReportante);
        parametros.put("fechaOperacion", fechaOp);
        parametros.put("horaOperacion", in.horaOperacion);
        parametros.put("identificador", in.identificador);

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

    public boolean ValidacionRut(String rut) {
        if (rut == null || rut.isEmpty()) {
            return false;
        }
        String soloDigitos = rut.replaceAll("\\D", "");
        if (soloDigitos.length() > 11) {
            return false;
        }
        String[] partes = rut.split("-");
        if (partes.length != 2) {
            return false;
        }
        String numero = partes[0];
        String dv = partes[1].toUpperCase();
        if (!numero.matches("\\d+") || !dv.matches("[0-9K]")) {
            return false;
        }
        return true;
    }

    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        Gson gson = new GsonBuilder().create();
        EntradaConsultaReporte3p1 entrada = gson.fromJson(in, EntradaConsultaReporte3p1.class);
        ResultadoConsultaReporte3p1 out = consultarReporte(conn, entrada);

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
                        "    \"rutReportante\": \"76011193-7\",\n" +
                        "    \"fechaOperacion\": \"2025-07-17\",\n" +
                        "    \"horaOperacion\": \"17:03:52\",\n" +
                        "    \"identificador\": 722220\n" +
                        "}";
                EntradaConsultaReporte3p1 Objin = gson.fromJson(json, EntradaConsultaReporte3p1.class);

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
