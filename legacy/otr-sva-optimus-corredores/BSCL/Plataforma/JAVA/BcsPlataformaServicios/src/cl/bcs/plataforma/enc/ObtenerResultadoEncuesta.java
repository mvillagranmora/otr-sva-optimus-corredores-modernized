package cl.bcs.plataforma.enc;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.enc.ResponderEncuesta.RespuestaEncuesta_Entrada;
import cl.bcs.plataforma.enc.pojo.DefinicionEncuesta;
import cl.bcs.plataforma.enc.pojo.DefinicionPregunta;
import cl.bcs.plataforma.enc.pojo.DefinicionPuntaje;
import cl.bcs.plataforma.enc.pojo.DefinicionRespuesta;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.cli.pojo.RegistroPregunta;
import cl.bcs.socionegocio.cli.pojo.RegistroRespuesta;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ObtenerResultadoEncuesta implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ObtenerResultadoEncuesta.class);
	
	private final String SQL_SELECT_ENCUESTA = loadResourceToString("cl/bcs/plataforma/enc/sql/ResponderEncuesta_SelectEncuesta.sql");
	private final String SQL_SELECT_PREGUNTAS = loadResourceToString("cl/bcs/plataforma/enc/sql/ResponderEncuesta_SelectPreguntas.sql");
	private final String SQL_SELECT_RESPUESTAS = loadResourceToString("cl/bcs/plataforma/enc/sql/ResponderEncuesta_SelectRespuestas.sql");
	private final String SQL_SELECT_DEFINICION_PUNTAJE = loadResourceToString("cl/bcs/plataforma/enc/sql/ResponderEncuesta_SelectDefinicionPuntaje.sql");
	
	public static class ObtenerResultadoEncuesta_Entrada {
		public String 	p_IdModulo;
		public Long 	p_IdUsuario;
		public Long 	p_IdEmpresa;
		public String 	p_IdCanal;
		public Long 	p_IdDefinicionEncuesta;
		public List<RespuestaEncuesta_Entrada> p_Respuestas;
		
		public ObtenerResultadoEncuesta_Entrada() {
			p_Respuestas = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ObtenerResultadoEncuesta_Salida {
		public String 				IdCategoriaInversionista;
		public Long 				Puntaje;
		public Long 				P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> 	P_OUT_MENSAJES;
		public List<DefinicionEncuesta> DefinicionEncuestas;
		public List<RegistroPregunta> RegistroPreguntas;
		public List<RegistroRespuesta> RegistroRespuestas;

		public ObtenerResultadoEncuesta_Salida() {
			P_OUT_MENSAJES = new ArrayList<>();
			RegistroPreguntas = new ArrayList<>();
			RegistroRespuestas = new ArrayList<>();
			DefinicionEncuestas = new ArrayList<>();
			P_OUT_ERRORESCONTADOR = 0L;
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public ObtenerResultadoEncuesta_Salida logica (Connection conn, ObtenerResultadoEncuesta_Entrada entrada) throws Exception {
		logger.info("INICIO::ObtenerResultadoEncuesta");
		long inicioServicio = System.currentTimeMillis();		
		ObtenerResultadoEncuesta_Salida salida = new ObtenerResultadoEncuesta_Salida();
		
		SQL _sql = new SQL();
		HashMap<String,Object> params = new HashMap<String, Object>();
		params.put("IdDefinicionEncuesta", entrada.p_IdDefinicionEncuesta);
		List<DefinicionEncuesta> encuesta = _sql.query(conn, SQL_SELECT_ENCUESTA, params , DefinicionEncuesta.class);
		
		if (encuesta.size() == 0) {
			String codigo = "UI_ENCUESTA_NO_EXISTE";
			String desc = Diccionario.getMensaje(conn, entrada.p_IdEmpresa, codigo).msg();
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codigo, new HashMap<>(), conn, entrada.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR = 1L;
			return salida;
		}
		
		//ENCUESTA INACTIVA
		if (!encuesta.get(0).IdEstado.equalsIgnoreCase("ACT")) {
			String codigo = "UI_ENCUESTA_INACTIVA";
			String desc = Diccionario.getMensaje(conn, entrada.p_IdEmpresa, codigo).msg();
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codigo,desc));
			salida.P_OUT_ERRORESCONTADOR = 1L;
			return salida;
		}
		
		params.clear();
		params.put("IdDefinicionEncuesta",encuesta.get(0).IdDefinicionEncuesta);
		List<DefinicionPregunta> preguntas = _sql.query(conn, SQL_SELECT_PREGUNTAS, params , DefinicionPregunta.class);
		List<DefinicionRespuesta> respuestas = _sql.query(conn, SQL_SELECT_RESPUESTAS, params , DefinicionRespuesta.class);
		List<DefinicionPuntaje> puntajes = _sql.query(conn, SQL_SELECT_DEFINICION_PUNTAJE, new HashMap<>() , DefinicionPuntaje.class);
		
		if (puntajes.size() == 0) {
			String codigo = "UI_PUNTAJES_ENCUESTA_NO_DEFINIDOS";
			String desc = Diccionario.getMensaje(conn, entrada.p_IdEmpresa, codigo).msg();
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codigo,desc));
			salida.P_OUT_ERRORESCONTADOR = 1L;
			return salida;
		}

		List<RegistroPregunta> registroPreguntas = new ArrayList<RegistroPregunta>();
		List<RegistroRespuesta> registroRespuestas = new ArrayList<RegistroRespuesta>();
		
		long idPregunta = 0;
		long idRespuesta = 0;
		long idEncuesta = 0;
		
		Long puntajeEncuesta = 0L;
		
		HashMap<Long,Long> respuestasPorPregunta = new HashMap<Long, Long>();
		for (DefinicionPregunta p : preguntas) {
			
			RegistroPregunta registroPregunta = new RegistroPregunta();
			registroPregunta.IdEmpresa = entrada.p_IdEmpresa;
			registroPregunta.IdRegistroEncuesta = idEncuesta;
			registroPregunta.IdRegistroPregunta = idPregunta++;
			registroPregunta.TextoPregunta = p.TextoPregunta;
			registroPreguntas.add(registroPregunta);
			respuestasPorPregunta.put(registroPregunta.IdRegistroPregunta, 0L);
			
			for (DefinicionRespuesta r : respuestas) {
				if (r.IdDefinicionPregunta.equals(p.IdDefinicionPregunta)) {
					
					RegistroRespuesta registroRespuesta = new RegistroRespuesta();
					registroRespuesta.IdEmpresa = entrada.p_IdEmpresa;
					registroRespuesta.IdRegistroPregunta = registroPregunta.IdRegistroPregunta;
					registroRespuesta.IdRegistroRespuesta = idRespuesta++;
					registroRespuesta.Puntaje = r.Puntaje;
					registroRespuesta.Seleccionado = "N";
					registroRespuesta.TextoRespuesta = r.TextoRespuesta;
					registroRespuestas.add(registroRespuesta);
					
					for (RespuestaEncuesta_Entrada rr : entrada.p_Respuestas) {
						if (rr.IdDefinicionRespuesta.equals(r.IdDefinicionRespuesta)) {
							puntajeEncuesta+=registroRespuesta.Puntaje;
							registroRespuesta.Seleccionado = "S";
							Long val = respuestasPorPregunta.get(registroRespuesta.IdRegistroPregunta);
							val+=1;
							respuestasPorPregunta.put(registroRespuesta.IdRegistroPregunta, val);
						}
					}
					
				}
			}
		}
		
		for (Long pregunta : respuestasPorPregunta.keySet()) {
			Long respuesta = respuestasPorPregunta.get(pregunta);
			
			if (respuesta == 0) {
				String codigo = "UI_EXISTEN_PREGUNTAS_SIN_RESPONDER";
				String desc = Diccionario.getMensaje(conn, entrada.p_IdEmpresa, codigo).msg();
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codigo, new HashMap<>(), conn, entrada.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = 1L;
				return salida;
			}
			if (respuesta > 1) {
				String codigo = "UI_EXISTEN_PREGUNTAS_CON_MAS_DE_UNA_RESPUESTA";
				String desc = Diccionario.getMensaje(conn, entrada.p_IdEmpresa, codigo).msg();
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codigo,desc));
				salida.P_OUT_ERRORESCONTADOR = 1L;
				return salida;
			}
		}

		for (int i = 0; i < puntajes.size(); i++) {
			DefinicionPuntaje registroPuntaje = puntajes.get(i);
			boolean calificaEnTramo = puntajeEncuesta < registroPuntaje.PuntajeHasta || i == (puntajes.size() - 1);

			if (calificaEnTramo) {
				salida.IdCategoriaInversionista = registroPuntaje.IdCategoriaInversionista;
				salida.Puntaje = puntajeEncuesta;
				break;
			}
		}
		
		if("".equals(salida.IdCategoriaInversionista) && puntajeEncuesta.compareTo(0L)==0) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_RESPUESTAS_MAL_CONFIGURADAS", new HashMap<>(), conn, entrada.p_IdEmpresa));
			return salida;
		}
		
		String codigo = "UI_ENCUESTA_EXITO_RESULTADO";
		HashMap<String, String> successMsgParams = new HashMap<>(2);
		successMsgParams.put("IdCategoriaInversionista", salida.IdCategoriaInversionista);
		salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,codigo, successMsgParams, conn, entrada.p_IdEmpresa));

		salida.RegistroPreguntas.addAll(registroPreguntas);
		salida.RegistroRespuestas.addAll(registroRespuestas);
		salida.DefinicionEncuestas.addAll(encuesta);

		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ObtenerResultadoEncuesta", finServicio - inicioServicio);		
		return salida;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ObtenerResultadoEncuesta_Entrada in = gson.fromJson(jin.toString(), ObtenerResultadoEncuesta_Entrada.class);
		ObtenerResultadoEncuesta_Salida out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			//String json = "{\"p_IdModulo\":\"SOC\",\"p_IdUsuario\":5,\"p_IdEmpresa\":1,\"p_IdCliente\":1423323,\"p_IdDefinicionEncuesta\":32,\"p_Respuestas\":[{\"IdDefinicionPregunta\":63,\"IdDefinicionRespuesta\":105},{\"IdDefinicionPregunta\":64,\"IdDefinicionRespuesta\":109},{\"IdDefinicionPregunta\":65,\"IdDefinicionRespuesta\":115},{\"IdDefinicionPregunta\":66,\"IdDefinicionRespuesta\":119},{\"IdDefinicionPregunta\":67,\"IdDefinicionRespuesta\":124},{\"IdDefinicionPregunta\":68,\"IdDefinicionRespuesta\":125},{\"IdDefinicionPregunta\":69,\"IdDefinicionRespuesta\":131},{\"IdDefinicionPregunta\":70,\"IdDefinicionRespuesta\":134},{\"IdDefinicionPregunta\":71,\"IdDefinicionRespuesta\":140},{\"IdDefinicionPregunta\":72,\"IdDefinicionRespuesta\":141},{\"IdDefinicionPregunta\":73,\"IdDefinicionRespuesta\":147},{\"IdDefinicionPregunta\":74,\"IdDefinicionRespuesta\":153},{\"IdDefinicionPregunta\":75,\"IdDefinicionRespuesta\":154},{\"IdDefinicionPregunta\":76,\"IdDefinicionRespuesta\":162},{\"IdDefinicionPregunta\":77,\"IdDefinicionRespuesta\":166}],\"p_IdCanal\":\"AGE\"}";
			String json = "{\"p_IdModulo\":\"SOC\",\"p_IdUsuario\":9,\"p_IdEmpresa\":1,\"p_IdDefinicionEncuesta\":32,\"p_IdCanal\":\"AGE\",\"p_Respuestas\":[{\"IdDefinicionPregunta\":63,\"IdDefinicionRespuesta\":105},{\"IdDefinicionPregunta\":64,\"IdDefinicionRespuesta\":109},{\"IdDefinicionPregunta\":65,\"IdDefinicionRespuesta\":112},{\"IdDefinicionPregunta\":66,\"IdDefinicionRespuesta\":116},{\"IdDefinicionPregunta\":67,\"IdDefinicionRespuesta\":121},{\"IdDefinicionPregunta\":68,\"IdDefinicionRespuesta\":125},{\"IdDefinicionPregunta\":69,\"IdDefinicionRespuesta\":129},{\"IdDefinicionPregunta\":70,\"IdDefinicionRespuesta\":132},{\"IdDefinicionPregunta\":71,\"IdDefinicionRespuesta\":136},{\"IdDefinicionPregunta\":72,\"IdDefinicionRespuesta\":141},{\"IdDefinicionPregunta\":73,\"IdDefinicionRespuesta\":146},{\"IdDefinicionPregunta\":74,\"IdDefinicionRespuesta\":150},{\"IdDefinicionPregunta\":75,\"IdDefinicionRespuesta\":154},{\"IdDefinicionPregunta\":76,\"IdDefinicionRespuesta\":159},{\"IdDefinicionPregunta\":77,\"IdDefinicionRespuesta\":163}]}";
			
			ObtenerResultadoEncuesta_Entrada Objin = gson.fromJson(json, ObtenerResultadoEncuesta_Entrada.class);
			ObtenerResultadoEncuesta demo = new ObtenerResultadoEncuesta();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				ObtenerResultadoEncuesta_Salida ObjOut = demo.logica(con, Objin);
 				con.rollback();
 				//con.commit();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				con.rollback();
			}
		}
		System.out.println("== END ==");
	}
}
