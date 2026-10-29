package cl.bcs.plataforma.enc;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.enc.CrearDefinicionEncuesta.CrearDefinicionEncuesta_Entrada;
import cl.bcs.plataforma.enc.CrearDefinicionEncuesta.CrearDefinicionEncuesta_Salida;
import cl.bcs.plataforma.enc.pojo.DefinicionEncuesta;
import cl.bcs.plataforma.enc.pojo.DefinicionPregunta;
import cl.bcs.plataforma.enc.pojo.DefinicionRespuesta;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarEncuestaVigente implements JavaCall {

	private final String SQL_SELECT_ENCUESTA = loadResourceToString("cl/bcs/plataforma/enc/sql/ConsultarEncuestaVigente_SelectEncuesta.sql");
	private final String SQL_SELECT_PREGUNTAS = loadResourceToString("cl/bcs/plataforma/enc/sql/ConsultarEncuestaVigente_SelectPreguntas.sql");
	private final String SQL_SELECT_RESPUESTAS = loadResourceToString("cl/bcs/plataforma/enc/sql/ConsultarEncuestaVigente_SelectRespuestas.sql");
	
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ConsultarEncuestaVigente_Entrada in = gson.fromJson(jin.toString(), ConsultarEncuestaVigente_Entrada.class);
		ConsultarEncuestaVigente_Salida out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static class PreguntaEncuesta_Salida {
		
		public Long IdDefinicionPregunta;
		public String TextoPregunta;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class RespuestaEncuesta_Salida {
		
		public Long IdDefinicionPregunta;
		public Long IdDefinicionRespuesta;
		public String TextoRespuesta;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ConsultarEncuestaVigente_Salida {
		
		public Long p_out_IdDefinicionEncuesta;
		public String p_out_NombreEncuesta;
		
		public List<PreguntaEncuesta_Salida> p_out_Preguntas;
		public List<RespuestaEncuesta_Salida> p_out_Respuestas;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		
		public ConsultarEncuestaVigente_Salida() {
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.p_out_Preguntas = new ArrayList<>();
			this.p_out_Respuestas = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}	
	}
	
	public static class ConsultarEncuestaVigente_Entrada {
		public String p_IdModulo;
		public Long p_IdUsuario;
		public Long p_IdEmpresa;

		public String toString() {
			return Utils.printJson(this, false);
		}	
	}
	
	public ConsultarEncuestaVigente_Salida logica(Connection conn,ConsultarEncuestaVigente_Entrada entrada) throws Exception {
		ConsultarEncuestaVigente_Salida rs = new ConsultarEncuestaVigente_Salida();
		
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(conn, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			rs.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			return rs;
		}
		
		SQL _sql = new SQL();
		
		List<DefinicionEncuesta> encuestaVigente = _sql.query(conn, SQL_SELECT_ENCUESTA, new HashMap<>(), DefinicionEncuesta.class);
		if (encuestaVigente.size() == 0) {
			String codigo = "ENCUESTA_ACTIVA_NO_DEFINIDA";
			String desc = Diccionario.getMensaje(conn, entrada.p_IdEmpresa, codigo).msg();
			rs.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codigo,desc));
			return rs;
		}
		
		rs.p_out_IdDefinicionEncuesta = encuestaVigente.get(0).IdDefinicionEncuesta;
		rs.p_out_NombreEncuesta = encuestaVigente.get(0).Nombre;
		
		HashMap<String,Object> params = new HashMap<String, Object>();
		params.put("IdDefinicionEncuesta",encuestaVigente.get(0).IdDefinicionEncuesta);
		List<DefinicionPregunta> preguntas = _sql.query(conn, SQL_SELECT_PREGUNTAS, params , DefinicionPregunta.class);
		List<DefinicionRespuesta> respuestas = _sql.query(conn, SQL_SELECT_RESPUESTAS, params , DefinicionRespuesta.class);
		
		
		for (DefinicionPregunta p : preguntas) {
			PreguntaEncuesta_Salida pp = new PreguntaEncuesta_Salida();
			pp.IdDefinicionPregunta = p.IdDefinicionPregunta;
			pp.TextoPregunta = p.TextoPregunta;
			rs.p_out_Preguntas.add(pp);
		}
		
		for (DefinicionRespuesta r : respuestas) {
			RespuestaEncuesta_Salida rr = new RespuestaEncuesta_Salida();
			rr.IdDefinicionPregunta = r.IdDefinicionPregunta;
			rr.IdDefinicionRespuesta = r.IdDefinicionRespuesta;
			rr.TextoRespuesta = r.TextoRespuesta;			
			rs.p_out_Respuestas.add(rr);
		}
		
		return rs;
		
	}

}
