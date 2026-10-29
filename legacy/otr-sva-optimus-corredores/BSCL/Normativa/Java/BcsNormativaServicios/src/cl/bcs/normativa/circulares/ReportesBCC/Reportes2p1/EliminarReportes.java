package cl.bcs.normativa.circulares.ReportesBCC.Reportes2p1;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Log;
import cl.bcs.utils.Utils;
import cl.bcs.utils.BcsPojo;

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
            "cl/bcs/normativa/circulares/ReportesBCC/Reportes2p1/sql/ConsultarReportes_Reporte_select.sql");
    private final String SQL_ELIMINAR_REPORTE = loadResourceToString(
            "cl/bcs/normativa/circulares/ReportesBCC/Reportes2p1/sql/EliminarReporte_Reporte_call.sql");
            

    public final String SQL_EXIST_TABLA_ID = "select 1 AS \"Tab\" from m_temporary_tables where table_name = '#ELIMINARREPORTE_ID'";
    private final String SQL_CREATE_TABLA_ID = "CREATE LOCAL TEMPORARY TABLE #ELIMINARREPORTE_ID (\"ID\" BIGINT)";
    private final String SQL_INSERT_TABLA_ID = "INSERT INTO #ELIMINARREPORTE_ID (\"ID\") VALUES (:ID)";
    public final String SQL_DROP_TABLA_ID = "DROP TABLE #ELIMINARREPORTE_ID";

    public static class EntradaReporte2p1 extends BcsPojo {
        public Long IdEmpresa;
        public String IdUsuario;
        public String ClaveUsuario;
        public String ClaveReporte;
        public String RutContraparte1;
        public String FechaNegociacion;
        public String HoraNegociacion;
        public String Identificador;

        public EntradaReporte2p1() {
            this.IdEmpresa = 1L;
            this.IdUsuario = "";
            this.ClaveUsuario = "";
            this.ClaveReporte = "";
        }
    }

    public static class SalidaReporte2p1 extends BcsPojo {
        public List<MensajeSalida> P_OUT_MENSAJES;
        public Long P_OUT_ERRORESCONTADOR;
        public String P_OUT_DESCRIPCION;

        public SalidaReporte2p1() {
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

    @Trace
    public SalidaReporte2p1 eliminarReporte(Connection con, EntradaReporte2p1 in) throws Exception {
        logger.info("Eliminando reporte 2.1");
        logger.info("Parametros de entrada: " + in.toString());
        ListaId listaId = new ListaId();
        SalidaReporte2p1 out = new SalidaReporte2p1();
        SQL sql = new SQL();

        if (!ValidacionRut(in.RutContraparte1)) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_RUT_CONTRAPARTE1_INVALIDO",
                    new HashMap<String, String>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Rut contraparte 1 invalido.";
            logger.error("Rut contraparte 1  invalido: " + in.RutContraparte1);
            return out;
        }
        
        try {
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate.parse(in.FechaNegociacion, df);
        } catch (DateTimeParseException e) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_FECHA_NEGOCIACION_FORMATO",
                    new HashMap<String, String>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Fecha de negociacion invalida, el formato debe ser YYYY-MM-DD.";
            return out;
        }

        try {
            LocalTime.parse(in.HoraNegociacion,
                    DateTimeFormatter.ofPattern("HH:mm:ss"));
        } catch (DateTimeParseException e) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_HORA_NEGOCIACION_INVALIDA",
                    new HashMap<String, String>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Formato de hora de negociacion invalido, el formato debe ser HH:MM:SS.";
            logger.error("Hora de negociacion invalida: {}", in.HoraNegociacion);
            return out;
        }

        if (in.Identificador == null || in.Identificador.isEmpty()) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_IDENTIFICADOR_INVALIDO",
                    new HashMap<String, String>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Identificador invalido.";
            logger.error("Identificador invalido: " + in.Identificador);
            return out;
        }
        

        HashMap<String, Object> ExistParams = new HashMap<>();
        ExistParams.put("rutContraparte1", in.RutContraparte1);
        ExistParams.put("fechaNegociacion", in.FechaNegociacion);
        ExistParams.put("horaNegociacion", in.HoraNegociacion);
        ExistParams.put("identificador", in.Identificador);

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
            logger.error("El reporte con el identificador especificado no existe: " + in.Identificador);
            return out;
        }

        if ("ENV".equals(rows.get(0).EstadoSICAM)) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_REPORTE_ENVIADO",
                    new HashMap<String, String>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "El reporte ya ha sido enviado y no puede ser eliminado.";
            logger.error("El reporte ya ha sido enviado y no puede ser eliminado: " + in.Identificador);
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
                out.P_OUT_DESCRIPCION = "No existen autorizados para las etapas del tipo evento: ELIMINACION REPORTE SICAM 2.1";
                logger.info("No existen autorizados para las etapas del tipo evento: ELIMINACION REPORTE SICAM 2.1");
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

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        Gson gson = new GsonBuilder().create();
        EntradaReporte2p1 entrada = gson.fromJson(in, EntradaReporte2p1.class);

        SalidaReporte2p1 out = eliminarReporte(conn, entrada);

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
        try (Connection con = PlataformaUtils.getPropertiesConnection("qa01",
                "C:\\Users\\vaguilera_ext\\Documents\\GIT_OPTIMUS\\optimus-corredores\\connections.properties")) {

            try {
                Gson gson = Utils.getGsonBuilder();
                String json = "{\n" +
                        "    \"IdUsuario\": \"1\",\n" +
                        "    \"RutContraparte1\": \"76011193-7\",\n" +
                        "    \"FechaNegociacion\": \"2025-08-13\",\n" +
                        "    \"HoraNegociacion\": \"10:00:00\",\n" +
                        "    \"Identificador\": \"123456\" \n" +
                        "}";
                EntradaReporte2p1 Objin = gson.fromJson(json, EntradaReporte2p1.class);

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