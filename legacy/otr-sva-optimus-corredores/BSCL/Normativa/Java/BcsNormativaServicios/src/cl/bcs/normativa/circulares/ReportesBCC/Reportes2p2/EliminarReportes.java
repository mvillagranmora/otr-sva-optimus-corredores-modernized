package cl.bcs.normativa.circulares.ReportesBCC.Reportes2p2;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Locale;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
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
import cl.bcs.utils.Log;
import cl.bcs.utils.Utils;
import cl.bcs.utils.BcsPojo;
import static cl.bcs.normativa.circulares.ReportesBCC.Reportes2p2.utils.ReporteUtils.*;

/**
 * Servicio para eliminar reportes SICAM / Merrill Lynch
 * 
 */
public class EliminarReportes implements JavaCall {
    static final Logger logger = LogManager.getLogger(EliminarReportes.class);

    /**
     * Llamadas SQL
     */
    private final String SQL_CONSULTA_REPORTE = loadResourceToString(
            "cl/bcs/normativa/circulares/ReportesBCC/Reportes2p2/sql/ConsultarReportes_Reporte_select.sql");
    private final String SQL_ELIMINAR_REPORTE = loadResourceToString(
            "cl/bcs/normativa/circulares/ReportesBCC/Reportes2p2/sql/EliminarReporte_Reporte_call.sql");
            

    public final String SQL_EXIST_TABLA_ID = "select 1 AS \"Tab\" from m_temporary_tables where table_name = '#ELIMINARREPORTE_ID'";
    private final String SQL_CREATE_TABLA_ID = "CREATE LOCAL TEMPORARY TABLE #ELIMINARREPORTE_ID (\"ID\" BIGINT)";
    private final String SQL_INSERT_TABLA_ID = "INSERT INTO #ELIMINARREPORTE_ID (\"ID\") VALUES (:ID)";
    public final String SQL_DROP_TABLA_ID = "DROP TABLE #ELIMINARREPORTE_ID";

    public static class EntradaReporte2p2 extends BcsPojo {
        public Long IdEmpresa;
        public String IdUsuario;
        public String ClaveUsuario;
        public String ClaveReporte;
        public String RutEntidadMCF;
        public String Fecha;
        public String MonedaSaldo;

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

    public static class Id extends BcsPojo {
        public Long ID;

        public Id() {
            this.ID = 0L;
        }
    }

    public static class ExisteTabId extends BcsPojo {
        public Long count;
    }

    public static class idQueryResult {
        public Long ID;
        public Long IdDocumento;
        public String EstadoSICAM;
    }

    public static class DatosSalidaSP extends BcsPojo {
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

    public static class ListaId extends BcsPojo {
        public List<Id> listaId;

        public ListaId() {
            this.listaId = new ArrayList<Id>();
        }
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
    public SalidaReporte2p2 eliminarReporte(Connection con, EntradaReporte2p2 in) throws Exception {
        logger.info("Eliminando reporte 2.2");
        logger.info("Parametros de entrada: " + in.toString());
        ListaId listaId = new ListaId();
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

            LocalDate ultimoDelMes = fecha.with(TemporalAdjusters.lastDayOfMonth());
            if (!fecha.equals(ultimoDelMes)) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
                    "ERROR_FECHA_NO_ULTIMO_DIA_MES", new HashMap<>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "Fecha debe ser el ultimo dia del mes.";
                logger.error("Fecha no es ultimo dia del mes: {}, esperado: {}", in.Fecha, ultimoDelMes);
                return out;
            }
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

        List<idQueryResult> rows = sql.query(
                con,
                SQL_CONSULTA_REPORTE,
                ExistParams,
                idQueryResult.class);

        if (rows.isEmpty() || rows.get(0).ID == null) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_REPORTE_NO_EXISTE",
                    new HashMap<String, String>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "El reporte con el identificador especificado no existe.";
            logger.error("El reporte con el identificador especificado no existe: ");
            return out;
        }

        if ("ENV".equals(rows.get(0).EstadoSICAM)) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_REPORTE_ENVIADO",
                    new HashMap<String, String>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "El reporte ya ha sido enviado y no puede ser eliminado.";
            logger.error("El reporte ya ha sido enviado y no puede ser eliminado.");
            return out;
        }

        Long idReporte = rows.get(0).ID;
        Long idDocumento = rows.get(0).IdDocumento;
        logger.info("ID del reporte a eliminar: " + idReporte);
        Id id = new Id();
        id.ID = idReporte;
        listaId.listaId.add(id);
        try {
            HashMap<String, Object> params = new HashMap<>(2);
            List<ExisteTabId> TabId = new ArrayList<ExisteTabId>();
            TabId = new SQL().query(con, SQL_EXIST_TABLA_ID, params, ExisteTabId.class);
            if (TabId.size() > 0) {
                try {
                    SQL.exec(con, SQL_DROP_TABLA_ID);
                    logger.info("Tabla temporal #ELIMINARREPORTE_ID eliminada correctamente.");
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("Error al eliminar la tabla temporal #ELIMINARREPORTE_ID: " + e.getMessage());
                }
            } else {
                logger.info("No se encontro la tabla temporal #ELIMINARREPORTE_ID, no es necesario eliminarla.");

            }

            SQL.exec(con, SQL_CREATE_TABLA_ID);
            List<Id> lista = Collections.singletonList(new Id() {
                {
                    ID = idReporte;
                }
            });
            sql.batch(con, SQL_INSERT_TABLA_ID, lista, Id.class);
            

            HashMap<String, Object> paramsIN = new HashMap<>();
            paramsIN.put("IdEmpresa", in.IdEmpresa);
            paramsIN.put("IdUsuario", in.IdUsuario);
            paramsIN.put("IdModulo", "NOR");
            HashMap<String, Class> paramsOut = new HashMap<>();
            paramsOut.put("p_out_AdvertenciasContador", Integer.class);
            paramsOut.put("p_out_AdvertenciasDetalle", Detalle.class);
            paramsOut.put("p_out_ErroresContador", Integer.class);
            paramsOut.put("p_out_ErroresDetalle", Detalle.class);

            HashMap<String, Object> ResultSp = sql.call(con, SQL_ELIMINAR_REPORTE, paramsIN, paramsOut);

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
            } else if (errList.stream().anyMatch(d -> "Autorizados".equals(d.Codigo)) || errList.stream()
                    .anyMatch(d -> "Usuario".equals(d.Codigo) || errList != null || advList != null)) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ELIMINACION_NO_AUTORIZADA",
                        new HashMap<String, String>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "No existen autorizados para las etapas del tipo evento: ELIMINACION REPORTE SICAM 2.2";
                logger.info("No existen autorizados para las etapas del tipo evento: ELIMINACION REPORTE SICAM 2.2");
            } else if (errList == null || errList.isEmpty() && advList == null || advList.isEmpty()) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "REPORTE_ELIMINADO",
                        new HashMap<String, String>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR = 0L;
                out.P_OUT_DESCRIPCION = "El reporte ha sido eliminado correctamente.";
                logger.info("Reporte eliminado correctamente.");
            } else {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_ELIMINAR_REPORTE",
                        new HashMap<String, String>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "Se encontraron errores al eliminar el reporte.";
                logger.info("Se encontraron errores al eliminar el reporte.");
            }
            SQL.exec(con, SQL_DROP_TABLA_ID);
        } catch (Exception e) {
            logger.error("Error al eliminar el reporte: " + e.getMessage());
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_ELIMINAR_REPORTE",
                    new HashMap<String, String>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Error al eliminar el reporte: " + e.getMessage();
            logger.error("Error al eliminar el reporte: ", e);
            e.printStackTrace();
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

        SalidaReporte2p2 out = eliminarReporte(conn, entrada);

        JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
        return dataout;
    }

    /**
     * Metodo principal para probar la eliminacion de un reporte.
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
                        "    \"RutEntidadMCF\": \"76011193-7\",\n" +
                        "    \"Fecha\": \"2025-08-31\",\n" +
                        "    \"MonedaSaldo\": \"USD\"\n" +
                        "}";
                EntradaReporte2p2 Objin = gson.fromJson(json, EntradaReporte2p2.class);

                EliminarReportes service = new EliminarReportes();

                service.eliminarReporte(con, Objin);
                con.commit();
            } catch (Exception e) {
                logger.error("Error al eliminar reporte", e);
                con.rollback();
            }
        } catch (Exception e) {
            logger.error("Error al conectar a la base de datos", e);
        }
    }
}