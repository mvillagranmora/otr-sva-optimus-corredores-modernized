package cl.bcs.plataforma.enc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.CrearOrdenes;
import cl.bcs.ordenes.CrearOrdenes.CrearOrdenesEntrada;
import cl.bcs.ordenes.CrearOrdenes.CrearOrdenesSalida;
import cl.bcs.plataforma.enc.pojo.DefinicionEncuesta;
import cl.bcs.plataforma.enc.pojo.DefinicionPregunta;
import cl.bcs.plataforma.enc.pojo.DefinicionRespuesta;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearDefinicionEncuesta implements JavaCall {

	private final String SQL_INSERT_DEFINICION_ENCUESTA = loadResourceToString("cl/bcs/plataforma/enc/sql/CrearDefinicionEncuesta_InsertDefinicionEncuesta.sql");
	private final String SQL_INSERT_DEFINICION_PREGUNTA = loadResourceToString("cl/bcs/plataforma/enc/sql/CrearDefinicionEncuesta_InsertDefinicionPregunta.sql");
	private final String SQL_INSERT_DEFINICION_RESPUESTA = loadResourceToString("cl/bcs/plataforma/enc/sql/CrearDefinicionEncuesta_InsertDefinicionRespuesta.sql");
	private final String SQL_SELECT_DEFINICION_ENCUESTA = loadResourceToString("cl/bcs/plataforma/enc/sql/CrearDefinicionEncuesta_SelectDefinicionEncuesta.sql");
	private final String SQL_UPDATE_DEFINICION_ENCUESTA = loadResourceToString("cl/bcs/plataforma/enc/sql/CrearDefinicionEncuesta_UpdateDefinicionEncuesta.sql");
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		CrearDefinicionEncuesta_Entrada in = gson.fromJson(jin.toString(), CrearDefinicionEncuesta_Entrada.class);
		CrearDefinicionEncuesta_Salida out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public CrearDefinicionEncuesta_Salida logica(Connection conn, CrearDefinicionEncuesta_Entrada in) throws Exception {
		CrearDefinicionEncuesta_Salida salida = new CrearDefinicionEncuesta_Salida();
		
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(conn, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			salida.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			salida.P_OUT_ERRORESCONTADOR = 1L;
			return salida;
		}
		
		SQL _sql = new SQL();
		
		HashMap<String,Object> params = new HashMap<String, Object>();
		params.put("Nombre", in.p_Nombre);
		List<DefinicionEncuesta> existe = _sql.query(conn, SQL_SELECT_DEFINICION_ENCUESTA, params, DefinicionEncuesta.class);
		if (existe.size() > 0) {
			String codigo = "UI_ENCUESTA_YA_EXISTE";
			String descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa, codigo).msg();
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
			salida.P_OUT_ERRORESCONTADOR = 1L;
			return salida;
		}
		
		List<Long> ids = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::IdDefinicionEncuesta",1L);
		List<DefinicionEncuesta> d = new ArrayList<>();
		DefinicionEncuesta def = new DefinicionEncuesta();
		def.IdEmpresa = in.p_IdEmpresa;
		def.IdEstado = in.p_IdEstado;
		def.Nombre = in.p_Nombre;
		def.IdUsuarioCreacion = in.p_IdUsuario;
		def.IdDefinicionEncuesta = ids.get(0);
		def.Borrado = "N";
		
		d.add(def);
		
		_sql.batch(conn, SQL_INSERT_DEFINICION_ENCUESTA, d, DefinicionEncuesta.class);
		
		if (def.IdEstado.equalsIgnoreCase("ACT")) {
			_sql.batch(conn, SQL_UPDATE_DEFINICION_ENCUESTA, d, DefinicionEncuesta.class);
		}
		
		
		HashMap<Long,Long> hashIdPregunta = new HashMap<Long, Long>();
		HashMap<Long,Long> hashIdRespuesta = new HashMap<Long, Long>();
		if (in.p_Preguntas.size() > 0) {
			
			List<DefinicionPregunta> pp = new ArrayList<>();
			
			List<Long> idsPregunta = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::IdDefinicionPregunta",(long)in.p_Preguntas.size());
			int i = 0;
			for (DefinicionPregunta_Entrada pin : in.p_Preguntas) {
				DefinicionPregunta p = new DefinicionPregunta();
				p.IdDefinicionEncuesta = def.IdDefinicionEncuesta;
				p.IdDefinicionPregunta = idsPregunta.get(i++);
				p.IdEmpresa = in.p_IdEmpresa;
				p.IdUsuarioCreacion = in.p_IdUsuario;
				p.TextoPregunta = pin.TextoPregunta;
				p.Borrado = "N";
				pp.add(p);
				hashIdPregunta.put(pin.IdPregunta, p.IdDefinicionPregunta);
			}
			
			_sql.batch(conn, SQL_INSERT_DEFINICION_PREGUNTA, pp, DefinicionPregunta.class);
			
			List<Long> idsRespuesta = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::IdDefinicionRespuesta",(long)in.p_Respuestas.size());
			i = 0;
			if (in.p_Respuestas.size() > 0) {
				
				List<DefinicionRespuesta> rr = new ArrayList<>();
				for (DefinicionRespuesta_Entrada rin : in.p_Respuestas) {
					DefinicionRespuesta r = new DefinicionRespuesta();
					r.IdDefinicionPregunta = hashIdPregunta.get(rin.IdPregunta);
					r.IdDefinicionRespuesta = idsRespuesta.get(i++);
					r.IdEmpresa = in.p_IdEmpresa;
					r.IdUsuarioCreacion = in.p_IdUsuario;
					r.Puntaje = rin.Puntaje;
					r.TextoRespuesta = rin.TextoRespuesta;
					r.Borrado = "N";
					hashIdRespuesta.put(rin.IdRespuesta,r.IdDefinicionRespuesta);
					rr.add(r);
				}
				
				_sql.batch(conn, SQL_INSERT_DEFINICION_RESPUESTA, rr, DefinicionRespuesta.class);
				
			}
			
		}
		
		for (Entry<Long, Long> rr : hashIdRespuesta.entrySet()) {
			DefinicionRespuesta_Salida d1 = new DefinicionRespuesta_Salida();
			d1.IdRespuesta = rr.getKey();
			d1.IdDefinicionRespuesta = rr.getValue();
			salida.p_out_Respuestas.add(d1);
		}
		
		for (Entry<Long, Long> rr : hashIdPregunta.entrySet()) {
			DefinicionPregunta_Salida d2 = new DefinicionPregunta_Salida();
			d2.IdDefinicionPregunta = rr.getValue();
			d2.IdPregunta = rr.getKey();
			salida.p_out_Preguntas.add(d2);
		}
		
		String codigo = "UI_ENCUESTA_CREADA_CORRECTAMENTE";
		String descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa, codigo).msg();
		salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion));
		salida.p_out_IdDefinicionEncuestaCreada = def.IdDefinicionEncuesta;
		
		//TODO LOG

		return salida;
	}

	public static class DefinicionPregunta_Entrada {
		
		public Long IdPregunta;
		public String TextoPregunta;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DefinicionRespuesta_Entrada {
		
		public Long IdPregunta;
		public Long IdRespuesta;
		public String TextoRespuesta;
		public Long Puntaje;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CrearDefinicionEncuesta_Entrada {
		public Long				p_IdEmpresa;
		public Long				p_IdUsuario;
		public String			p_IdModulo;
		public String			p_simularOperacion;
		public String			p_UsaReglasNegocio;
		
		public String			p_Nombre;
		public String			p_IdEstado;
		
		public List<DefinicionPregunta_Entrada> p_Preguntas;
		public List<DefinicionRespuesta_Entrada> p_Respuestas;
		
		public CrearDefinicionEncuesta_Entrada() {
			p_Preguntas = new ArrayList<>();
			p_Respuestas = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class CrearDefinicionEncuesta_Salida {

		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long p_out_IdDefinicionEncuestaCreada;
		public List<DefinicionPregunta_Salida> p_out_Preguntas;
		public List<DefinicionRespuesta_Salida> p_out_Respuestas;
		
		public CrearDefinicionEncuesta_Salida() {
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.p_out_Preguntas = new ArrayList<>();
			this.p_out_Respuestas = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class DefinicionPregunta_Salida {
		public Long IdPregunta;
		public Long IdDefinicionPregunta;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DefinicionRespuesta_Salida {
		public Long IdRespuesta;
		public Long IdDefinicionRespuesta;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static void main(String [] args) throws Exception {
		
		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"S\",\"p_simularOperacion\":\"N\",\"p_Nombre\":\"D\",\"p_IdEstado\":\"ACT\",\"p_Preguntas\":[{\"IdPregunta\":0,\"TextoPregunta\":\"1\"}],\"p_Respuestas\":[{\"IdPregunta\":0,\"TextoRespuesta\":\"1\",\"Puntaje\":2}]}";
		
		Gson gson = Utils.getGsonBuilder();
		CrearDefinicionEncuesta_Entrada Objin = gson.fromJson(json, CrearDefinicionEncuesta_Entrada.class);
		CrearDefinicionEncuesta demo=new CrearDefinicionEncuesta();
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			CrearDefinicionEncuesta_Salida ObjOut=demo.logica(con, Objin);
			con.rollback();
		}
		
		
		
		
		
	}


}
