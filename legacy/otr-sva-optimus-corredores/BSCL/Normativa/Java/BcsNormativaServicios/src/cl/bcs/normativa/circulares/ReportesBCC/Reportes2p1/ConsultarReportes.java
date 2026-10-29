package cl.bcs.normativa.circulares.ReportesBCC.Reportes2p1;

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
import com.google.gson.annotations.SerializedName;

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
            "cl/bcs/normativa/circulares/ReportesBCC/Reportes2p1/sql/ConsultarReportes_Reporte_select.sql");

    /**
     * Clase de entrada para el reporte 2.1
     */
    public static class EntradaConsultaReporte2p1 extends BcsPojo {
        public Long IdEmpresa;
        public String IdUsuario;
        public String ClaveUsuario;
        public String ClaveReporte;
        public String RutContraparte1;
        public String FechaNegociacion;
        public String HoraNegociacion;
        public String Identificador;
        
        public EntradaConsultaReporte2p1() {
            this.IdEmpresa = 1L;
            this.IdUsuario = "";
            this.ClaveUsuario = "";
            this.ClaveReporte = "";
        }
    }

    /**
     * Clase de salida para el reporte 2.1
     */
    public static class ResultadoConsultaReporte2p1 extends BcsPojo {
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
        public Date FechaNegociacion;
        public String HoraNegociacion;
        public Date FechaValutaMonedaRecibida;
        public Date FechaValutaMonedaEntregada;
        public String Identificador;
        public String RUTContraparte1;
        public String LEIContraparte1;
        public String NombreContraparte1;
        public String RUTContraparte2;
        public String LEIContraparte2;
        public String NombreContraparte2;
        public String PaisContraparte2;
        public String RUTIntermediario;
        public String PlataformaNegociacion;
        public String CodigoOperacionCambio;
        public String PaisMonedaExtranjera;
        public String MonedaCompradaRecibida;
        public BigDecimal MontoMonedaCompradaRecibida;
        public String MonedaVendidaEntregada;
        public BigDecimal MontoMonedaVendidaEntregada;
        public String ConvencionTipoCambio;
        public BigDecimal TipoCambio;
        public String AfectaDerivados;
        public String IdentificadorContratoDerivados;
        public Date FechaSuscripcionContrato;
        public String HoraSuscripcionContrato;
        public String OperacionEmpresaRelacionada;
        public String NumeroInscripcionObligacionExterior;
        @SerializedName("NombreFinancistaAportante")
        public String NombreFinancistaAporante;
        @SerializedName("LEIFinancistaAportante")
        public String LEIFinancistaAporante;
        public String NombreReceptorInversion;
        public String LEIReceptorInversion;
        public String ModalidadInversionCustodio;
    }

    @Trace
    public ResultadoConsultaReporte2p1 consultarReporte(Connection con, EntradaConsultaReporte2p1 in) throws Exception {
        logger.info("Consultando reporte 2.1");
        logger.info("Parametros de entrada: {}", in);
        ResultadoConsultaReporte2p1 out = new ResultadoConsultaReporte2p1();
        HashMap<String, Object> parametros = new HashMap<>();
        SQL sql = new SQL();

        if (in.RutContraparte1 != null && !in.RutContraparte1.isEmpty()) {
            if (!ValidacionRut(in.RutContraparte1)) {
                logger.error("Rut Contraparte 1 invalido: {}", in.RutContraparte1);
                out.codigoRes = 400;
                out.mensajeRes = "Rut Contraparte1 invalido";
                return out;
            }
        } else {
            in.RutContraparte1 = null;
        }

        Date fechaNeg = null;
        if (in.FechaNegociacion != null && !in.FechaNegociacion.isEmpty()) {
            try {
                LocalDate ld = LocalDate.parse(
                        in.FechaNegociacion,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                fechaNeg = java.sql.Date.valueOf(ld);
            } catch (DateTimeParseException e) {
                logger.error("Fecha invalida: {}", in.FechaNegociacion);
                out.Resultado = Collections.emptyList();
                out.codigoRes = 400;
                out.mensajeRes = "Fecha invalida";
                return out;
            }
        }

        if (in.HoraNegociacion != null && !in.HoraNegociacion.isEmpty()) {
            try {
                LocalTime.parse(
                        in.HoraNegociacion,
                        DateTimeFormatter.ofPattern("HH:mm:ss"));
            } catch (DateTimeParseException e) {
                logger.error("Hora invalida: {}", in.HoraNegociacion);
                out.Resultado = Collections.emptyList();
                out.codigoRes = 400;
                out.mensajeRes = "Hora invalida";
                return out;
            }
        } else {
            in.HoraNegociacion = null;
        }

        if (in.Identificador != null) {
            if (in.Identificador.length() > 15) {
                logger.error("Identificador demasiado largo: {}", in.Identificador);
                out.Resultado = Collections.emptyList();
                out.codigoRes = 400;
                out.mensajeRes = "Identificador demasiado largo";
                return out;
            }
            in.Identificador = in.Identificador.trim();
        } else {
            in.Identificador = null;
        }

        parametros.put("rutContraparte1", in.RutContraparte1);
        parametros.put("fechaNegociacion", fechaNeg);
        parametros.put("horaNegociacion", in.HoraNegociacion);
        parametros.put("identificador", in.Identificador);

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
        EntradaConsultaReporte2p1 entrada = gson.fromJson(in, EntradaConsultaReporte2p1.class);
        ResultadoConsultaReporte2p1 out = consultarReporte(conn, entrada);

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
                        "    \"RutContraparte1\": \"97043000-8\",\n" +
                        "    \"FechaNegociacion\": \"2025-08-28\",\n" +
                        "    \"Identificador\": \"1231274\"\n" +
                        "}";
                EntradaConsultaReporte2p1 Objin = gson.fromJson(json, EntradaConsultaReporte2p1.class);

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
