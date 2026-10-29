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
import cl.bcs.plataforma.enc.pojo.DefinicionPuntaje;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EliminarDefinicionPuntaje implements JavaCall {

	private final String SQL_DELETE_DEFINICION_PUNTAJE = loadResourceToString("cl/bcs/plataforma/enc/sql/EliminarDefinicionPuntaje_DeleteDefinicionPuntaje.sql");
	private final String SQL_SELECT_DEFINICION_PUNTAJE = loadResourceToString("cl/bcs/plataforma/enc/sql/EliminarDefinicionPuntaje_SelectDefinicionPuntaje.sql");
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EliminarDefinicionPuntaje_Entrada in = gson.fromJson(jin.toString(), EliminarDefinicionPuntaje_Entrada.class);
		EliminarDefinicionPuntaje_Salida out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public EliminarDefinicionPuntaje_Salida logica(Connection conn, EliminarDefinicionPuntaje_Entrada in) throws Exception {
		EliminarDefinicionPuntaje_Salida salida = new EliminarDefinicionPuntaje_Salida();
		
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
		
		DefinicionPuntaje p = new DefinicionPuntaje();
		p.IdCategoriaInversionista = in.p_IdCategoriaInversionista;
		p.IdEmpresa = in.p_IdEmpresa;
		pp.clear();
		pp.add(p);
		_sql.batch(conn, SQL_DELETE_DEFINICION_PUNTAJE, pp, DefinicionPuntaje.class);
		
		String codigo = "UI_DEFINICION_ELIMINADA";
		String descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa, codigo).msg();
		salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion));
		salida.P_OUT_ERRORESCONTADOR = 0L;
		
		return salida;
	}

	public static class EliminarDefinicionPuntaje_Entrada {
		public Long				p_IdEmpresa;
		public Long				p_IdUsuario;
		public String			p_IdModulo;
		public String			p_simularOperacion;
		public String			p_UsaReglasNegocio;
		public String			p_IdCategoriaInversionista;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EliminarDefinicionPuntaje_Salida {

		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public EliminarDefinicionPuntaje_Salida() {
			P_OUT_MENSAJES = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

}
