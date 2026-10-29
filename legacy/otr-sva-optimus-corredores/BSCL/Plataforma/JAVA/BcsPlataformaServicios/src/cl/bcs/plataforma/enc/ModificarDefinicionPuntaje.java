package cl.bcs.plataforma.enc;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.enc.pojo.DefinicionPuntaje;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarDefinicionPuntaje implements JavaCall {

	private final String SQL_SELECT_DEFINICION_PUNTAJE = loadResourceToString("cl/bcs/plataforma/enc/sql/ModificarDefinicionPuntaje_SelectDefinicionPuntaje.sql");
	private final String SQL_UPDATE_DEFINICION_PUNTAJE = loadResourceToString("cl/bcs/plataforma/enc/sql/ModificarDefinicionPuntaje_UpdateDefinicionPuntaje.sql");
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ModificarDefinicionPuntaje_Entrada in = gson.fromJson(jin.toString(), ModificarDefinicionPuntaje_Entrada.class);
		ModificarDefinicionPuntaje_Salida out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public ModificarDefinicionPuntaje_Salida logica(Connection conn, ModificarDefinicionPuntaje_Entrada in) throws Exception {
		ModificarDefinicionPuntaje_Salida salida = new ModificarDefinicionPuntaje_Salida();
		
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(conn, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			salida.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			salida.P_OUT_ERRORESCONTADOR = 1L;
			return salida;
		}
		
		SQL _sql = new SQL();
		
		HashMap<String,Object> params = new HashMap<String, Object>();
		params.put("IdEmpresa",in.p_IdEmpresa);
		params.put("IdCategoriaInversionista",in.p_IdCategoriaInversionista);
		List<DefinicionPuntaje> pp = _sql.query(conn, SQL_SELECT_DEFINICION_PUNTAJE, params , DefinicionPuntaje.class);
		if (pp.size() == 0) {
			String codigo = "UI_DEFINICION_NO_EXISTE";
			String descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa, codigo).msg();
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
			salida.P_OUT_ERRORESCONTADOR = 1L;
			return salida;
		}
		
		if (in.p_PuntajeHasta == null|| in.p_PuntajeHasta <= 0) {
			String codigo = "UI_PUNTAJE_NO_VALIDO";
			String descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa, codigo).msg();
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
			salida.P_OUT_ERRORESCONTADOR = 1L;
			return salida;
		}
		
		DefinicionPuntaje p = new DefinicionPuntaje();
		p.IdCategoriaInversionista = in.p_IdCategoriaInversionista;
		p.IdEmpresa = in.p_IdEmpresa;
		pp.clear();
		pp.add(p);
		
		_sql.batch(conn, SQL_UPDATE_DEFINICION_PUNTAJE, pp, DefinicionPuntaje.class);
		
		String codigo = "UI_DEFINICION_MODIFICADA";
		String descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa, codigo).msg();
		salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion));
		salida.P_OUT_ERRORESCONTADOR = 0L;
		
		return salida;
	}

	public static class ModificarDefinicionPuntaje_Entrada {
		public Long				p_IdEmpresa;
		public Long				p_IdUsuario;
		public String			p_IdModulo;
		public String			p_simularOperacion;
		public String			p_UsaReglasNegocio;
		public String			p_IdCategoriaInversionista;
		public Long				p_PuntajeHasta;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ModificarDefinicionPuntaje_Salida {

		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public ModificarDefinicionPuntaje_Salida() {
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
}
