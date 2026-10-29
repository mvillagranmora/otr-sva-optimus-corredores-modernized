package cl.bcs.plataforma.enc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;

import cl.bcs.plataforma.enc.CrearDefinicionEncuesta.CrearDefinicionEncuesta_Entrada;
import cl.bcs.plataforma.enc.CrearDefinicionEncuesta.CrearDefinicionEncuesta_Salida;
import cl.bcs.plataforma.enc.CrearDefinicionEncuesta.DefinicionPregunta_Entrada;
import cl.bcs.plataforma.enc.CrearDefinicionEncuesta.DefinicionRespuesta_Entrada;
import cl.bcs.plataforma.enc.pojo.DefinicionEncuesta;
import cl.bcs.plataforma.enc.pojo.DefinicionPregunta;
import cl.bcs.plataforma.enc.pojo.DefinicionRespuesta;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarDefinicionEncuesta implements JavaCall {

	private final String SQL_SELECT_DEFINICION_ENCUESTA = loadResourceToString("cl/bcs/plataforma/enc/sql/ModificarDefinicionEncuesta_SelectDefinicionEncuesta.sql");
	private final String SQL_UPDATE_DEFINICION_ENCUESTA = loadResourceToString("cl/bcs/plataforma/enc/sql/ModificarDefinicionEncuesta_UpdateDefinicionEncuesta.sql");
	private final String SQL_UPDATE_OTRAS_ENCUESTAS = loadResourceToString("cl/bcs/plataforma/enc/sql/ModificarDefinicionEncuesta_UpdateOtrasEncuestas.sql");
	private final String SQL_SELECT_RESPUESTAS_ELIMINADAS = loadResourceToString("cl/bcs/plataforma/enc/sql/ModificarDefinicionEncuesta_SelectRespuestasEliminadas.sql");
	private final String SQL_UPDATE_RESPUESTAS_ELIMINADAS = loadResourceToString("cl/bcs/plataforma/enc/sql/ModificarDefinicionEncuesta_UpdateRespuestasEliminadas.sql");
	private final String SQL_UPDATE_RESPUESTAS_MODIFICADAS = loadResourceToString("cl/bcs/plataforma/enc/sql/ModificarDefinicionEncuesta_UpdateRespuestasModificadas.sql");
	
	private final String SQL_UPDATE_PREGUNTAS_ELIMINADAS = loadResourceToString("cl/bcs/plataforma/enc/sql/ModificarDefinicionEncuesta_UpdatePreguntasEliminadas.sql");
	private final String SQL_UPDATE_PREGUNTAS_MODIFICADAS = loadResourceToString("cl/bcs/plataforma/enc/sql/ModificarDefinicionEncuesta_UpdatePreguntasModificadas.sql");
	
	
	private final String SQL_INSERT_DEFINICION_PREGUNTA = loadResourceToString("cl/bcs/plataforma/enc/sql/ModificarDefinicionEncuesta_InsertDefinicionPregunta.sql");
	private final String SQL_INSERT_DEFINICION_RESPUESTA = loadResourceToString("cl/bcs/plataforma/enc/sql/ModificarDefinicionEncuesta_InsertDefinicionRespuesta.sql");
	
	

	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ModificarDefinicionEncuesta_Entrada in = gson.fromJson(jin.toString(), ModificarDefinicionEncuesta_Entrada.class);
		ModificarDefinicionEncuesta_Salida out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public ModificarDefinicionEncuesta_Salida logica(Connection conn, ModificarDefinicionEncuesta_Entrada in) throws Exception {
		ModificarDefinicionEncuesta_Salida salida = new ModificarDefinicionEncuesta_Salida();
		
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(conn, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			salida.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			salida.P_OUT_ERRORESCONTADOR = 1L;
			return salida;
		}
		
		SQL _sql = new SQL();
		
		HashMap<String,Object> params = new HashMap<String, Object>();
		params.put("IdDefinicionEncuesta", in.p_IdDefinicionEncuesta);
		List<DefinicionEncuesta> existe = _sql.query(conn, SQL_SELECT_DEFINICION_ENCUESTA, params, DefinicionEncuesta.class);
		if (existe.size() == 0) {
			String codigo = "UI_ENCUESTA_NO_EXISTE";
			String descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa, codigo).msg();
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
			salida.P_OUT_ERRORESCONTADOR = 1L;
			return salida;
		}
		
		DefinicionEncuesta d = new DefinicionEncuesta();
		d.IdDefinicionEncuesta = in.p_IdDefinicionEncuesta;
		d.Nombre = in.p_Nombre;
		d.IdEstado = in.p_IdEstado;
		
		if (d.Nombre != null || d.IdEstado != null) {
			List<DefinicionEncuesta> dd = new ArrayList<>();
			dd.add(d);
			_sql.batch(conn, SQL_UPDATE_DEFINICION_ENCUESTA, dd, DefinicionEncuesta.class);
			
			if (d.IdEstado.equalsIgnoreCase("ACT")) {
				_sql.batch(conn, SQL_UPDATE_OTRAS_ENCUESTAS, dd, DefinicionEncuesta.class);
			}
		}
		
		List<DefinicionRespuesta> respuestasEliminar = new ArrayList<>();
		List<DefinicionRespuesta> respuestasModificar = new ArrayList<>();
		List<DefinicionRespuesta> respuestasCrear = new ArrayList<>();
		for (ModificarDefinicionRespuesta_Entrada resp : in.p_Respuestas) {
			if (resp.Accion.equalsIgnoreCase("E" ) && resp.IdDefinicionRespuesta != null) {
				DefinicionRespuesta def = new DefinicionRespuesta();
				def.IdDefinicionRespuesta = resp.IdDefinicionRespuesta;
				def.Borrado = "S";
				respuestasEliminar.add(def);
			}
			if (resp.Accion.equalsIgnoreCase("M" ) && resp.IdDefinicionRespuesta != null) {
				DefinicionRespuesta def = new DefinicionRespuesta();
				def.IdDefinicionRespuesta = resp.IdDefinicionRespuesta;
				def.Puntaje = resp.Puntaje;
				def.TextoRespuesta = resp.TextoRespuesta;
				def.Borrado = "N";
				respuestasModificar.add(def);
			}
			if (resp.Accion.equalsIgnoreCase("C") && resp.IdDefinicionPregunta != null) {
				DefinicionRespuesta def = new DefinicionRespuesta();
				def.IdDefinicionRespuesta = null;
				def.IdDefinicionPregunta = resp.IdDefinicionPregunta;
				def.Puntaje = resp.Puntaje;
				def.TextoRespuesta = resp.TextoRespuesta;
				def.Borrado = "N";
				def.IdEmpresa = in.p_IdEmpresa;
				def.IdUsuarioCreacion = in.p_IdUsuario;
				respuestasCrear.add(def);
			}
		}
		
		//select respuestas de preguntas eliminadas
		List<DefinicionPregunta> preguntasEliminar = new ArrayList<>();
		List<DefinicionPregunta> preguntasModificar = new ArrayList<>();
		List<DefinicionPregunta> preguntasCrear = new ArrayList<>();
		for (ModificarDefinicionPregunta_Entrada preg : in.p_Preguntas) {
			if (preg.Accion.equalsIgnoreCase("E") && preg.IdDefinicionPregunta != null) {
				DefinicionPregunta e = new DefinicionPregunta();
				e.IdDefinicionPregunta = preg.IdDefinicionPregunta;
				e.Borrado = "S";
				preguntasEliminar.add(e);
			}
			if (preg.Accion.equalsIgnoreCase("M") && preg.IdDefinicionPregunta != null) {
				DefinicionPregunta e = new DefinicionPregunta();
				e.IdDefinicionPregunta = preg.IdDefinicionPregunta;
				e.TextoPregunta = preg.TextoPregunta;
				preguntasModificar.add(e);
			}
			if (preg.Accion.equalsIgnoreCase("C")) {
				DefinicionPregunta e = new DefinicionPregunta();
				e.IdDefinicionEncuesta = in.p_IdDefinicionEncuesta;
				e.IdDefinicionPregunta = preg.IdDefinicionPregunta;
				e.IdEmpresa = in.p_IdEmpresa;
				e.IdUsuarioCreacion = in.p_IdUsuario;
				e.TextoPregunta = preg.TextoPregunta;
				e.Borrado = "N";
				preguntasCrear.add(e);
			}
		}
		
		if (preguntasCrear.size() > 0) {
			List<Long> idsPregunta = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::IdDefinicionPregunta",(long)preguntasCrear.size());
			int ii = 0;
			for (DefinicionPregunta preg : preguntasCrear) {
				
				Long idPregunta = idsPregunta.get(ii++);
				
				for (DefinicionRespuesta resp : respuestasCrear) {
					
					if (preg.IdDefinicionPregunta.compareTo(resp.IdDefinicionPregunta) == 0) {
						resp.IdDefinicionPregunta = idPregunta; 
					}
					
					 
				}
				
				preg.IdDefinicionPregunta = idPregunta;  
			}
		}
		
		if (respuestasCrear.size() > 0) {
			List<Long> idsRespuesta = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::IdDefinicionRespuesta",(long)respuestasCrear.size());
			int ii = 0;
			for (DefinicionRespuesta resp : respuestasCrear) {
				resp.IdDefinicionRespuesta = idsRespuesta.get(ii++);  
			}
		}
		
		if (preguntasEliminar.size() > 0) {
			params.clear();
			List<Long> idsPreguntaEliminar = new ArrayList<>();
			for (DefinicionPregunta preg : preguntasEliminar) {
				idsPreguntaEliminar.add(preg.IdDefinicionPregunta);
			}
			params.put("idsDefinicionPregunta",idsPreguntaEliminar );
			List<DefinicionRespuesta> respuestasPreguntasEliminadas = _sql.query(conn, SQL_SELECT_RESPUESTAS_ELIMINADAS, params, DefinicionRespuesta.class);
			for (DefinicionRespuesta resp : respuestasPreguntasEliminadas) {
				DefinicionRespuesta r1 = new DefinicionRespuesta();
				r1.IdDefinicionRespuesta = resp.IdDefinicionRespuesta;
				r1.Borrado = "S";
				respuestasEliminar.add(r1);
			}
		}
		
		if (respuestasEliminar.size() > 0) {
			_sql.batch(conn, SQL_UPDATE_RESPUESTAS_ELIMINADAS, respuestasEliminar, DefinicionRespuesta.class);
		}
		
		if (respuestasModificar.size() > 0) {
			_sql.batch(conn, SQL_UPDATE_RESPUESTAS_MODIFICADAS, respuestasModificar, DefinicionRespuesta.class);
		}
		
		if (preguntasEliminar.size() > 0) {
			_sql.batch(conn, SQL_UPDATE_PREGUNTAS_ELIMINADAS, preguntasEliminar, DefinicionPregunta.class);
		}
		
		if (preguntasModificar.size() > 0) {
			_sql.batch(conn, SQL_UPDATE_PREGUNTAS_MODIFICADAS, preguntasModificar, DefinicionPregunta.class);
		}
		
		if (preguntasCrear.size() > 0) {
			_sql.batch(conn, SQL_INSERT_DEFINICION_PREGUNTA, preguntasCrear, DefinicionPregunta.class);
		}
		
		if (respuestasCrear.size() > 0) {
			_sql.batch(conn, SQL_INSERT_DEFINICION_RESPUESTA, respuestasCrear, DefinicionRespuesta.class);
		}
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		//Agregar log
		AgregarLog agregarLog = new AgregarLog();
		InAgregarLog entradaLog = new InAgregarLog();
						
		//Cabecera log
		InAgregarLog.Log headLog = new InAgregarLog.Log();
		headLog.IdCabeceraLog = 1L;
		headLog.IdTipoLog = "MODIFICACION";
		headLog.IdModulo = "PLA";
		headLog.IdUsuarioAtendido = in.p_IdUsuario;
		headLog.IdTipoRegistro = "MODIFICAR_ENCUESTA";
		headLog.IdUsuario = in.p_IdUsuario;
		headLog.IdEmpresa = in.p_IdEmpresa;
		headLog.NombreProceso = "Modificar Definicion Encuesta";
		headLog.EstadoRegistro = in.p_IdEstado != null ? in.p_IdEstado : "";
		headLog.FechaSistemaLog = fechaSistema != null ? fechaSistema : "";

		entradaLog.p_Log.add(headLog);

		//Detalle log
		if (preguntasCrear.size() > 0 && preguntasCrear != null) {
			for(DefinicionPregunta datosLogPC : preguntasCrear){
				InAgregarLog.DetalleLog detLog1 = new InAgregarLog.DetalleLog();
				detLog1.IdCabeceraLog = 1L;
				detLog1.SubRegistro = "Crear Pregunta";
				detLog1.CampoRegistro = "TextoPregunta";
				detLog1.ValorAntiguo = "";
				detLog1.ValorNuevo = datosLogPC.TextoPregunta != null ? datosLogPC.TextoPregunta : "";
		
				entradaLog.p_LogDetalle.add(detLog1);
			}
		}
		
		if (preguntasModificar.size() > 0 && preguntasModificar != null) {
			for(DefinicionPregunta datosLogPM : preguntasModificar){
				InAgregarLog.DetalleLog detLog2 = new InAgregarLog.DetalleLog();
				detLog2.IdCabeceraLog = 1L;
				detLog2.SubRegistro = "Modificar Pregunta";
				detLog2.CampoRegistro = "TextoPregunta";
				detLog2.ValorAntiguo = "";
				detLog2.ValorNuevo = datosLogPM.TextoPregunta != null ? datosLogPM.TextoPregunta : "";
		
				entradaLog.p_LogDetalle.add(detLog2);
			}
		}
		
		if (preguntasEliminar.size() > 0 && preguntasEliminar != null) {
			for(DefinicionPregunta datosLogPE : preguntasEliminar){
				InAgregarLog.DetalleLog detLog3 = new InAgregarLog.DetalleLog();
				detLog3.IdCabeceraLog = 1L;
				detLog3.SubRegistro = "Eliminar Pregunta";
				detLog3.CampoRegistro = "IdPregunta";
				detLog3.ValorNuevo = "";
				if (datosLogPE.IdDefinicionPregunta != null) {
					detLog3.ValorAntiguo = datosLogPE.IdDefinicionPregunta.toString();
				} else {
					detLog3.ValorAntiguo = "";
				}
				entradaLog.p_LogDetalle.add(detLog3);
			}
		}
		
		if (respuestasCrear.size() > 0 && respuestasCrear != null) {
			for(DefinicionRespuesta datosLogRC : respuestasCrear){
				InAgregarLog.DetalleLog detLog4 = new InAgregarLog.DetalleLog();
				detLog4.IdCabeceraLog = 1L;
				detLog4.SubRegistro = "Crear Respuesta";
				detLog4.CampoRegistro = "TextoRespuesta";
				detLog4.ValorAntiguo = "";
				detLog4.ValorNuevo = datosLogRC.TextoRespuesta != null ? datosLogRC.TextoRespuesta : "";
		
				entradaLog.p_LogDetalle.add(detLog4);
			}
		}
		
		if (respuestasModificar.size() > 0 && respuestasModificar != null) {
			for(DefinicionRespuesta datosLogRM : respuestasModificar){
				InAgregarLog.DetalleLog detLog5 = new InAgregarLog.DetalleLog();
				detLog5.IdCabeceraLog = 1L;
				detLog5.SubRegistro = "Modificar Respuesta";
				detLog5.CampoRegistro = "TextoRespuesta";
				detLog5.ValorAntiguo = "";
				detLog5.ValorNuevo = datosLogRM.TextoRespuesta != null ? datosLogRM.TextoRespuesta : "";
		
				entradaLog.p_LogDetalle.add(detLog5);
			}
		}
		
		//llamar al log
		agregarLog.logica(conn, entradaLog);
				
		String codigo = "UI_ENCUESTA_MODIFICADA_CORRECTAMENTE";
		String descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa, codigo).msg();
		salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion));
		
		return salida;
	}

	public static class ModificarDefinicionEncuesta_Entrada {
		
		public Long				p_IdEmpresa;
		public Long				p_IdUsuario;
		public String			p_IdModulo;
		public String			p_simularOperacion;
		public String			p_UsaReglasNegocio;
		
		public Long 			p_IdDefinicionEncuesta;
		public String			p_Nombre;
		public String			p_IdEstado;
		
		public List<ModificarDefinicionPregunta_Entrada> p_Preguntas;
		public List<ModificarDefinicionRespuesta_Entrada> p_Respuestas;
		
		public ModificarDefinicionEncuesta_Entrada() {
			p_Preguntas = new ArrayList<>();
			p_Respuestas = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ModificarDefinicionPregunta_Entrada {
		
		public Long IdDefinicionPregunta;
		public String TextoPregunta;
		public String Accion; //Crear,Modificar,Eliminar
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ModificarDefinicionRespuesta_Entrada {
		
		public Long IdDefinicionRespuesta; //viene para modificaciones, eliminaciones
		public Long IdDefinicionPregunta;  //viene para los nuevos
		public String TextoRespuesta;
		public Long Puntaje;
		public String Accion; //Crear,Modificar,Eliminar
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ModificarDefinicionEncuesta_Salida {

		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		
		public ModificarDefinicionEncuesta_Salida() {
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static void main(String [] args) throws Exception {
		
		
		Connection conn = PlataformaUtils.getDESAConnection();
		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"S\",\"p_simularOperacion\":\"N\",\"p_IdDefinicionEncuesta\":24,\"p_Nombre\":\"D CAMBIADA\",\"p_IdEstado\":\"ACT\",\"p_Preguntas\":[{\"Accion\":\"C\",\"IdDefinicionPregunta\":-2,\"TextoPregunta\":\"3\"}],\"p_Respuestas\":[{\"Accion\":\"C\",\"IdDefinicionPregunta\":-2,\"IdDefinicionRespuesta\":0,\"TextoRespuesta\":\"33\",\"Puntaje\":4},{\"IdDefinicionPregunta\":50,\"IdDefinicionRespuesta\":78,\"TextoRespuesta\":\"2\",\"Puntaje\":2,\"Accion\":\"M\"}]}";
		Gson gson = Utils.getGsonBuilder();
		ModificarDefinicionEncuesta_Entrada in = gson.fromJson(json, ModificarDefinicionEncuesta_Entrada.class);
		ModificarDefinicionEncuesta m = new ModificarDefinicionEncuesta();
		ModificarDefinicionEncuesta_Salida asd = m.logica(conn, in);
		System.out.println(asd);
		
	}
	
}
