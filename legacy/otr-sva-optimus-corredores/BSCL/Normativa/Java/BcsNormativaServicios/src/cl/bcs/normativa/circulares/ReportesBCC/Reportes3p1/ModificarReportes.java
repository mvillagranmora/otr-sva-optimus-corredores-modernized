package cl.bcs.normativa.circulares.ReportesBCC.Reportes3p1;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
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
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.utils.BcsPojo;

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
            "cl/bcs/normativa/circulares/ReportesBCC/Reportes3p1/sql/ModificarReporte_Reporte_call.sql");
    private final String SQL_CONSULTA_REPORTE = loadResourceToString(
            "cl/bcs/normativa/circulares/ReportesBCC/Reportes3p1/sql/ConsultarReportes_EstadoReporte_select.sql");

    public static class EntradaReporte3p1 extends BcsPojo {
        public Long idEmpresa;
        public String idUsuario;
        public String claveUsuario;
        public String claveReporte;
        public String rutReportante;
        public String fechaOperacion;
        public String horaOperacion;
        public String identificador;
        public String leiContraparte;

        public EntradaReporte3p1() {
            this.idEmpresa = 1L;
            this.claveUsuario = "";
            this.claveReporte = "";
            this.identificador = "";
        }
    }

    public static class SalidaReporte3p1 extends BcsPojo {
        public List<MensajeSalida> P_OUT_MENSAJES;
        public Long P_OUT_ERRORESCONTADOR;
        public String P_OUT_DESCRIPCION;

        public SalidaReporte3p1() {
            this.P_OUT_MENSAJES = new ArrayList<>();
            this.P_OUT_ERRORESCONTADOR = 0L;
            this.P_OUT_DESCRIPCION = "";
        }
    }

    public static class QueryResult extends BcsPojo {
        public Long ID;
        public Long IdDocumento;
        public String Estado;
        public String EstadoSICAM;
        public String SICAM;
        public String FuenteDatos;
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
        public Long IdCliente;
        public Long IdOperacion;
        public String IdTipoOperacion;
        public String Nemo;
        public String IdMonedaPrincipal;
        public String IdMonedaSecundaria;
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

    @Trace
    public SalidaReporte3p1 modificarReporte(Connection con, EntradaReporte3p1 in) throws Exception {
        logger.info("Modificando reporte");
        logger.info("Parametros de entrada: " + in.toString());
        SalidaReporte3p1 out = new SalidaReporte3p1();
        SQL sql = new SQL();

        if (!ValidacionRut(in.rutReportante)) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_RUT_REPORTANTE_INVALIDO",
                    new HashMap<String, String>(), con, in.idEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Rut reportante invalido.";
            logger.error("Rut reportante invalido: " + in.rutReportante);
            return out;
        }

        try {
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate.parse(in.fechaOperacion, df);
        } catch (DateTimeParseException e) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_FECHA_FORMATO",
                    new HashMap<String, String>(), con, in.idEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Fecha de operación inválida, el formato debe ser YYYY-MM-DD.";
            return out;
        }

        try {
            DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm:ss");
            LocalTime.parse(in.horaOperacion, tf);
        } catch (DateTimeParseException e) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_HORA_FORMATO",
                    new HashMap<String, String>(), con, in.idEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Hora de operación inválida, el formato debe ser HH:mm:ss.";
            return out;
        }
        if (in.identificador == null || in.identificador.isEmpty()) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_IDENTIFICADOR_VACIO",
                    new HashMap<String, String>(), con, in.idEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "El identificador del reporte no puede estar vacio.";
            logger.error("El identificador del reporte no puede estar vacio");
            return out;
        }

        HashMap<String, Object> ExistParams = new HashMap<>();
        ExistParams.put("identificador", in.identificador);
        ExistParams.put("rutReportante", in.rutReportante);
        ExistParams.put("fechaOperacion", in.fechaOperacion);
        ExistParams.put("horaOperacion", in.horaOperacion);

        List<QueryResult> rows = sql.query(
                con,
                SQL_CONSULTA_REPORTE,
                ExistParams,
                QueryResult.class);

        if (rows.isEmpty() || rows.get(0).ID == null) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_REPORTE_NO_EXISTE",
                    new HashMap<String, String>(), con, in.idEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "El reporte con el identificador especificado no existe.";
            logger.error("El reporte con el identificador especificado no existe: " + in.identificador);
            return out;
        }

        if (rows.get(0).EstadoSICAM.equals("ENV")) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_REPORTE_ENVIADO",
                    new HashMap<String, String>(), con, in.idEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "El reporte ya ha sido enviado y no puede ser eliminado.";
            logger.error("El reporte ya ha sido enviado y no puede ser eliminado: " + in.identificador);
            return out;
        }

        if (in.leiContraparte == null && !in.leiContraparte.isEmpty() && in.leiContraparte.length() != 20) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
                    "ERROR_LEI_CONTRAPARTE_INVALIDO", new HashMap<String, String>(), con, in.idEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "El LEI de la contraparte debe tener 20 caracteres alfanumericos.";
            logger.error("El LEI de la contraparte debe tener 20 caracteres alfanumericos: " + in.leiContraparte);
            return out;
        }

        HashMap<String, Object> paramsIN = new HashMap<>();
        paramsIN.put("IdEmpresa", in.idEmpresa);
        paramsIN.put("IdUsuario", in.idUsuario);
        paramsIN.put("IdModulo", "NOR");
        paramsIN.put("IdDocumento", rows.get(0).IdDocumento);
        paramsIN.put("Id", rows.get(0).ID);
        paramsIN.put("Estado", rows.get(0).Estado);
        paramsIN.put("EstadoSICAM", rows.get(0).EstadoSICAM);
        paramsIN.put("SICAM", rows.get(0).SICAM);
        paramsIN.put("FuenteDatos", rows.get(0).FuenteDatos);
        paramsIN.put("TipoRegistro", rows.get(0).TipoRegistro);
        paramsIN.put("RUTReportante", rows.get(0).RUTReportante);
        paramsIN.put("FechaOperacion", rows.get(0).FechaOperacion);
        paramsIN.put("HoraOperacion", rows.get(0).HoraOperacion);
        paramsIN.put("TipoOperacion", rows.get(0).TipoOperacion);
        paramsIN.put("EventoReporte", rows.get(0).EventoReporte);
        paramsIN.put("Identificador", in.identificador);
        paramsIN.put("RUTContraparte", rows.get(0).RUTContraparte);
        paramsIN.put("LEIContraparte", in.leiContraparte);
        paramsIN.put("NombreContraparte", rows.get(0).NombreContraparte);
        paramsIN.put("PlataformaNegociacion", rows.get(0).PlataformaNegociacion);
        paramsIN.put("MontoMonedaComprada", rows.get(0).MontoMonedaComprada);
        paramsIN.put("MontoMonedaVendida",  rows.get(0).MontoMonedaVendida);
        paramsIN.put("TipoCambio", rows.get(0).TipoCambio);
        paramsIN.put("IdCliente", rows.get(0).IdCliente);
        paramsIN.put("IdOperacion", rows.get(0).IdOperacion);
        paramsIN.put("IdTipoOperacion", rows.get(0).IdTipoOperacion);
        paramsIN.put("Nemo", rows.get(0).Nemo);
        paramsIN.put("IdMonedaPrincipal", rows.get(0).IdMonedaPrincipal);
        paramsIN.put("IdMonedaSecundaria", rows.get(0).IdMonedaSecundaria);

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
                    new HashMap<String, String>(), con, in.idEmpresa));
            out.P_OUT_ERRORESCONTADOR = 0L;
            out.P_OUT_DESCRIPCION = "La operacion requiere que sea autorizada por un Supervisor.";
            logger.info("La operacion requiere que sea autorizada por un Supervisor.");
        } else if (errList.stream().anyMatch(d -> "Autorizados".equals(d.Codigo))
                || errList.stream().anyMatch(d -> "Usuario".equals(d.Codigo) || errList != null || advList != null)) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "MODIFICACION_NO_AUTORIZADA",
                    new HashMap<String, String>(), con, in.idEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "No existen autorizados para las etapas del tipo evento: MODIFICACION REPORTE SICAM 3.1";
            logger.info("No existen autorizados para las etapas del tipo evento: MODIFICACION REPORTE SICAM 3.1");
        } else if (errList == null || errList.isEmpty() && advList == null || advList.isEmpty()) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "REPORTE_MODIFICADO",
                    new HashMap<String, String>(), con, in.idEmpresa));
            out.P_OUT_ERRORESCONTADOR = 0L;
            out.P_OUT_DESCRIPCION = "El reporte ha sido modificado correctamente.";
            logger.info("Reporte modificado correctamente.");
        } else {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_MODIFICAR_REPORTE",
                    new HashMap<String, String>(), con, in.idEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Se encontraron errores al modificado el reporte.";
            logger.info("Se encontraron errores al modificado el reporte.");
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
        EntradaReporte3p1 entrada = gson.fromJson(in, EntradaReporte3p1.class);

        SalidaReporte3p1 out = modificarReporte(conn, entrada);

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
        try (Connection con = PlataformaUtils.getPropertiesConnection("qa01",
                "C:\\Users\\vaguilera_ext\\Documents\\GIT_OPTIMUS\\optimus-corredores\\connections.properties")) {

            try {
                Gson gson = Utils.getGsonBuilder();
                String json = "{\n" +
                        "    \"idUsuario\": \"1\",\n" +
                        "    \"rutReportante\": \"76011193-7\",\n" +
                        "    \"fechaOperacion\": \"2025-08-04\",\n" +
                        "    \"horaOperacion\": \"17:03:52\",\n" +
                        "    \"identificador\": \"7222221\", \n" +
                        "    \"leiContraparte\": TEST\n" +
                        "}";
                EntradaReporte3p1 Objin = gson.fromJson(json, EntradaReporte3p1.class);

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