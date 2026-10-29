package cl.bcs.normativa.circulares.ReportesBCC.Reportes2p2;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Arrays;
import java.util.function.Function;
import java.util.List;
import java.util.Set;
import java.util.Locale;
import java.util.HashSet;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.ResolverStyle;
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
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.utils.BcsPojo;
import static cl.bcs.normativa.circulares.ReportesBCC.Reportes2p2.utils.ReporteUtils.*;

/**
 * Servicio para modificar reportes SICAM / Merrill Lynch
 * 
 */
public class ModificarReportes implements JavaCall {

    static final Logger logger = LogManager.getLogger(ModificarReportes.class);

    /**
     * Llamadas SQL
     */
    private final String SQL_MODIFICAR_REPORTE = loadResourceToString(
            "cl/bcs/normativa/circulares/ReportesBCC/Reportes2p2/sql/ModificarReporte_Reporte_call.sql");
    private final String SQL_CONSULTA_REPORTE = loadResourceToString(
            "cl/bcs/normativa/circulares/ReportesBCC/Reportes2p2/sql/ConsultarReportes_Reporte_select.sql");

    public static class EntradaReporte2p2 extends BcsPojo {
        public Long IdEmpresa;
        public String IdUsuario;
        public String ClaveUsuario;
        public String ClaveReporte;
        public String RutEntidadMCF;
        public String Fecha; 
        public String MonedaSaldo; 
        // Datos modificables
        public BigDecimal SaldoCuentaCambioConversion; 
        public BigDecimal SaldoMonedaOriginal; 
        public BigDecimal Paridad;

        public EntradaReporte2p2() {
            this.IdEmpresa = 1L;
            this.IdUsuario = "";
            this.ClaveUsuario = "";
            this.ClaveReporte = "";
        }
    }

    public static class SalidaReporte2p2 extends BcsPojo {
        public List<MensajeSalida> P_OUT_MENSAJES;
        public Long P_OUT_ERRORESCONTADOR;
        public String P_OUT_DESCRIPCION;

        public SalidaReporte2p2() {
            this.P_OUT_MENSAJES = new ArrayList<>();
            this.P_OUT_ERRORESCONTADOR = 0L;
            this.P_OUT_DESCRIPCION = "";
        }
    }

    public static class QueryResult extends BcsPojo {
        public Long ID;
        public Long IdDocumento;
        public String FuenteDatos;
        public String Estado;
        public String EstadoSICAM;
        public String SICAM;
        public String TipoRegistro;
        public String EventoReporte;
        public String TipoOperacion;
        public String RutEntidadMCF;
        public Date Fecha; 
        public BigDecimal SaldoCuentaCambioConversion; 
        public BigDecimal SaldoMonedaOriginal; 
        public String MonedaSaldo; 
        public BigDecimal Paridad;
        public Long IdCuentaInversion;
        public Long IdCliente;
        public Long IdCargoAbonoMovimiento;
        public String IdTipoMovimiento;
        public Date FechaEfectivaMovimiento;
    }

    public static class DatosSalidaSP extends BcsPojo {
        public Long p_out_id;
        public Integer p_out_AdvertenciasContador;
        public List<Detalle> p_out_AdvertenciasDetalle;
        public Integer p_out_ErroresContador;
        public List<Detalle> p_out_ErroresDetalle;
    }

    public static class Detalle extends BcsPojo {
        public String Codigo;
        public String RequiereAutorizacion;
        public String Descripcion;

        public Detalle() {
            this.Codigo = "";
            this.RequiereAutorizacion = "";
            this.Descripcion = "";
        }
    }

    public static class diccionarioResult extends BcsPojo{
        public String Codigo;
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
    public SalidaReporte2p2 modificarReporte(Connection con, EntradaReporte2p2 in) throws Exception {
        logger.info("Modificando reporte 2.2");
        logger.info("Parametros de entrada: " + in.toString());
        final DateTimeFormatter DF_YMD = DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
        SalidaReporte2p2 out = new SalidaReporte2p2();
        SQL sql = new SQL();

        if (isNullOrBlank(in.RutEntidadMCF) || !validacionRut(in.RutEntidadMCF)) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_RUT_ENTIDAD_MCF_INVALIDO",
                    new HashMap<String, String>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "El Rut Entidad MCF invalido.";
            logger.error("Rut Entidad MCF invalido: " + in.RutEntidadMCF);
            return out;
        }
        
        try {
            if (in.Fecha == null) throw new NullPointerException("null");
            LocalDate fecha = LocalDate.parse(in.Fecha, DF_YMD);
        } catch (DateTimeParseException | NullPointerException e) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_FECHA_FORMATO",
                    new HashMap<>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Fecha invalida, el formato debe ser YYYY-MM-DD.";
            return out;
        }

        if (isNullOrBlank(in.MonedaSaldo) || !isIso4217(in.MonedaSaldo)) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_MONEDA_SALDO_INVALIDA",
            		new HashMap<>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "La Moneda del Saldo obligatoria y debe ser codigo ISO 4217 (3 letras).";
            logger.error("MonedaSaldo invalida: {}", in.MonedaSaldo);
            return out;
        }
        in.MonedaSaldo = norm(in.MonedaSaldo);

        HashMap<String, Object> ExistParams = new HashMap<>();
        ExistParams.put("rutEntidadMCF", in.RutEntidadMCF);
        ExistParams.put("fecha", in.Fecha);
        ExistParams.put("monedaSaldo", in.MonedaSaldo);

        List<QueryResult> rows = sql.query(
                con,
                SQL_CONSULTA_REPORTE,
                ExistParams,
                QueryResult.class);

        if (rows.isEmpty() || rows.get(0).ID == null) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_REPORTE_NO_EXISTE",
                    new HashMap<String, String>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "El reporte con el folio especificado no existe.";
            logger.error("El reporte con el folio especificado no existe.");
            return out;
        }

        if ("ENV".equals(rows.get(0).EstadoSICAM)) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_REPORTE_ENVIADO",
                    new HashMap<String, String>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "El reporte ya ha sido enviado y no puede ser modificado.";
            logger.error("El reporte ya ha sido enviado y no puede ser modificado.");
            return out;
        }

        if(in.SaldoCuentaCambioConversion != null) {
            if (!isNum20_2(in.SaldoCuentaCambioConversion)) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_SALDO_CTA_CAMBIO_CONVERSION_INVALIDO", 
                        new HashMap<>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "El Saldo Cuenta Cambio Conversion invalido es hasta 18 enteros y 2 decimales.";
                logger.error("SaldoCuentaCambioConversion invalido: {}", in.SaldoCuentaCambioConversion);
                return out;
            }
        }
        
        if(in.SaldoMonedaOriginal != null) {
            if (!isNum20_2(in.SaldoMonedaOriginal)) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_SALDO_MONEDA_ORIGINAL_INVALIDO", 
                        new HashMap<>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "El Saldo Moneda Original invalido es hasta 18 enteros y 2 decimales.";
                logger.error("SaldoMonedaOriginal invalido: {}", in.SaldoMonedaOriginal);
                return out;
            }
        }
        
        if(in.Paridad != null) {
            if (!isNum10_5Pos(in.Paridad)) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
                    "ERROR_PARIDAD_INVALIDA", new HashMap<>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "Paridad obligatoria invalido es hasta 5 enteros y 5 decimales.";
                logger.error("Paridad invalida: {}", in.Paridad);
                return out;
            }
        }

        HashMap<String, Object> paramsIN = new HashMap<>();
        paramsIN.put("IdEmpresa", in.IdEmpresa);
        paramsIN.put("IdUsuario", in.IdUsuario);
        paramsIN.put("IdModulo", "NOR");
        paramsIN.put("IdDocumento", rows.get(0).IdDocumento);
        paramsIN.put("Id", rows.get(0).ID);
        paramsIN.put("Estado", rows.get(0).Estado);
        paramsIN.put("EstadoSICAM", rows.get(0).EstadoSICAM);
        paramsIN.put("SICAM", rows.get(0).SICAM);
        paramsIN.put("FuenteDatos", rows.get(0).FuenteDatos);
        //Datos del reporte
        paramsIN.put("TipoRegistro", rows.get(0).TipoRegistro);
        paramsIN.put("RutEntidadMCF", in.RutEntidadMCF);
        paramsIN.put("Fecha", Date.valueOf(in.Fecha));
        paramsIN.put("SaldoCuentaCambioConversion", in.SaldoCuentaCambioConversion != null ? in.SaldoCuentaCambioConversion : rows.get(0).SaldoCuentaCambioConversion);
        paramsIN.put("SaldoMonedaOriginal", in.SaldoMonedaOriginal != null ? in.SaldoMonedaOriginal : rows.get(0).SaldoMonedaOriginal);
        paramsIN.put("MonedaSaldo", in.MonedaSaldo);
        paramsIN.put("Paridad", in.Paridad != null ? in.Paridad : rows.get(0).Paridad);
        HashMap<String, Class> paramsOUT = new HashMap<>();
        paramsOUT.put("p_out_id", Long.class);
        paramsOUT.put("p_out_AdvertenciasContador", Integer.class);
        paramsOUT.put("p_out_AdvertenciasDetalle", Detalle.class);
        paramsOUT.put("p_out_ErroresContador", Integer.class);
        paramsOUT.put("p_out_ErroresDetalle", Detalle.class);

        HashMap<String, Object> ResultSp = sql.call(con, SQL_MODIFICAR_REPORTE, paramsIN, paramsOUT);

        logger.info("Resultado de la llamada al procedimiento almacenado: " + ResultSp);

        @SuppressWarnings("unchecked")
        List<Detalle> advList = (List<Detalle>) ResultSp.get("p_out_AdvertenciasDetalle");
        @SuppressWarnings("unchecked")
        List<Detalle> errList = (List<Detalle>) ResultSp.get("p_out_ErroresDetalle");

        if (advList.stream().anyMatch(d -> "REQUIERE_AUTORIZACION".equals(d.Codigo))) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "REQUIERE_AUTORIZACION",
                    new HashMap<String, String>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR = 0L;
            out.P_OUT_DESCRIPCION = "La operacion requiere que sea autorizada por un Supervisor.";
            logger.info("La operacion requiere que sea autorizada por un Supervisor.");
        } else if (errList.stream().anyMatch(d -> "Autorizados".equals(d.Codigo))
                || errList.stream().anyMatch(d -> "Usuario".equals(d.Codigo) || errList != null || advList != null)) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "MODIFICACION_NO_AUTORIZADA",
                    new HashMap<String, String>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "No existen autorizados para las etapas del tipo evento: MODIFICACION REPORTE SICAM 2.2";
            logger.info("No existen autorizados para las etapas del tipo evento: MODIFICACION REPORTE SICAM 2.2");
        } else if (advList.stream().anyMatch(d -> "UI_LA_OPCION_NO_ESTA_HABILITADA".equals(d.Codigo))){
        	out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "MODIFICACION_NO_EDITABLES",
                    new HashMap<String, String>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "No hay datos editables para este reporte.";
            logger.info("No hay datos editables para este reporte.");
        } else if (errList == null || errList.isEmpty() && advList == null || advList.isEmpty()) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "REPORTE_MODIFICADO",
                    new HashMap<String, String>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR = 0L;
            out.P_OUT_DESCRIPCION = "El reporte ha sido modificado correctamente.";
            logger.info("Reporte modificado correctamente.");
        } else {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_MODIFICAR_REPORTE",
                    new HashMap<String, String>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Se encontraron errores al modificado el reporte.";
            logger.info("Se encontraron errores al modificado el reporte.");
        }

        return out;
    }

    /**
     * Carga el contenido de un recurso SQL como una cadena de texto.
     *
     * @param conn Conexion a la base de datos.
     * @param in   Entrada en formato JSON que contiene los parametros del reporte.
     * @return Un objeto JSON que contiene el resultado de la operacion.
     */
    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        Gson gson = new GsonBuilder().create();
        EntradaReporte2p2 entrada = gson.fromJson(in, EntradaReporte2p2.class);

        SalidaReporte2p2 out = modificarReporte(conn, entrada);

        JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
        return dataout;
    }

    /**
     * Metodo principal para probar la modificacion de un reporte.
     *
     * @param args Argumentos de linea de comandos (no se utilizan).
     */
    public static void main(String[] args) {
        try (Connection con = PlataformaUtils.getPropertiesConnection("desa01",
                "C:\\Users\\vaguilera_ext\\Documents\\GIT_OPTIMUS\\optimus-corredores\\connections.properties")) {

            try {
                Gson gson = Utils.getGsonBuilder();
                String json = "{\n" +
                        "    \"IdUsuario\": \"1\",\n" +
                        "    \"RutEntidadMCF\": \"20720657-1\",\n" +
                        "    \"Fecha\": \"2025-09-30\",\n" +
                        "    \"MonedaSaldo\": \"USD\",\n" +
                        "    \"SaldoCuentaCambioConversion\": 1000000.00,\n" +
                        "    \"SaldoMonedaOriginal\": 800000.00,\n" +
                        "    \"Paridad\": 0.80000\n" +
                        "}";
                EntradaReporte2p2 Objin = gson.fromJson(json, EntradaReporte2p2.class);

                ModificarReportes service = new ModificarReportes();

                service.modificarReporte(con, Objin);
                con.commit();
            } catch (Exception e) {
                logger.error("Error al modificar el reporte", e);
                con.rollback();
            }
        } catch (Exception e) {
            logger.error("Error al conectar a la base de datos", e);
        }
    }
}