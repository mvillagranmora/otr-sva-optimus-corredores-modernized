package cl.bcs.plataforma.pla;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.pojo.SolicitudCambio;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarRegistroSolicitudCambio implements JavaCall {

    static final Logger logger = LogManager.getLogger(ConsultarRegistroSolicitudCambio.class);

    private String SQL_SELECT_SOLICITUDES_CAMBIO = loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultarRegistroSolicitudCambio_Select.sql");

    public static class EntradaConsultarRegistroSolicitudCambio extends BcsPojo {

	public Long p_IdSolicitudCambio;
	public String p_IdEntidadOrigen;
	public String p_IdTipoEntidad;

	public EntradaConsultarRegistroSolicitudCambio() {
	    this.p_IdSolicitudCambio = 0L;
	    this.p_IdEntidadOrigen = "";
	    this.p_IdTipoEntidad = "";
	}

    }

    public static class SalidaConsultarRegistroSolicitudCambio extends BcsPojo {
	public Long P_OUT_ERRORESCONTADOR;
	public List<MensajeSalida> P_OUT_MENSAJES;
    public List<SolicitudCambio> P_OUT_SOLICITUDES_CAMBIO;
    	
	public SalidaConsultarRegistroSolicitudCambio() {
	    this.P_OUT_ERRORESCONTADOR = 0L;
	    this.P_OUT_MENSAJES = new ArrayList<>();
	    this.P_OUT_SOLICITUDES_CAMBIO= new ArrayList<>();
	}
    }



    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
	Gson gson = Utils.getGsonBuilder();
	EntradaConsultarRegistroSolicitudCambio jin = gson.fromJson(in.toString(), EntradaConsultarRegistroSolicitudCambio.class);
	SalidaConsultarRegistroSolicitudCambio out = logica(conn, jin);
	JsonParser parser = new JsonParser();
	JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
	return dataout;
    }

    public SalidaConsultarRegistroSolicitudCambio logica(Connection con, EntradaConsultarRegistroSolicitudCambio entrada) throws Exception {
	logger.info("INICIO::ConsultarRegistroSolicitudCambio");
	long inicioServicio = System.currentTimeMillis();
	SalidaConsultarRegistroSolicitudCambio out = new SalidaConsultarRegistroSolicitudCambio();
	out.P_OUT_SOLICITUDES_CAMBIO = obtenerSolicitudesCambios(con, entrada.p_IdEntidadOrigen, entrada.p_IdTipoEntidad);
	if(out.P_OUT_SOLICITUDES_CAMBIO.size()== 0) {
	    out.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR,"UI_PROCESO_EJECUTADO_ERROR", "UI_ERROR_CONSULTA_SOLICITUD_CAMBIO"));
	}
	out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
	// TODO: Agregar contadores de errores
	long finServicio = System.currentTimeMillis();
	logger.info("FIN::ConsultarRegistroSolicitudCambio", finServicio - inicioServicio);
	return out;
    }

    private List<SolicitudCambio> obtenerSolicitudesCambios(Connection con, String idEntidadOrigen, String idTipoEntidad) throws Exception {
	logger.info("INICIO::ConsultarRegistroSolicitudCambio.obtenerSolicitudesCambios");
	long inicio = System.currentTimeMillis();

	SQL sql = new SQL();
	HashMap<String, Object> params = new HashMap<String, Object>();

	params.put("IdEntidadOrigen", idEntidadOrigen);
	params.put("IdTipoEntidad", idTipoEntidad);

	List<SolicitudCambio> solicitudesCambios = sql.query(con, SQL_SELECT_SOLICITUDES_CAMBIO, params, SolicitudCambio.class);

	long fin = System.currentTimeMillis();
	logger.info("FIN::ConsultarRegistroSolicitudCambio.obtenerSolicitudesCambios", fin - inicio);
	return solicitudesCambios;
    }

}
