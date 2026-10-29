package cl.bcs.plataforma.enc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.com.pojo.CategoriaInversionista;
import cl.bcs.plataforma.enc.pojo.DefinicionPuntaje;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ActualizarDefinicionPuntajes implements JavaCall {

	private final String SQL_INSERT_DEFINICION_PUNTAJE = loadResourceToString("cl/bcs/plataforma/enc/sql/ActualizarDefinicionPuntajes_InsertDefinicionPuntaje.sql");
	private final String SQL_SELECT_CATEGORIA_INVERSIONISTA = loadResourceToString("cl/bcs/plataforma/enc/sql/ActualizarDefinicionPuntajes_SelectCategoriaInversionista.sql");
	private final String SQL_DELETE_DEFINICION_PUNTAJE = loadResourceToString("cl/bcs/plataforma/enc/sql/ActualizarDefinicionPuntajes_DeleteDefinicionPuntajes.sql");
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ActualizarDefinicionPuntajes_Entrada in = gson.fromJson(jin.toString(), ActualizarDefinicionPuntajes_Entrada.class);
		ActualizarDefinicionPuntajes_Salida out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public ActualizarDefinicionPuntajes_Salida logica(Connection conn, ActualizarDefinicionPuntajes_Entrada in) throws Exception {
		ActualizarDefinicionPuntajes_Salida salida = new ActualizarDefinicionPuntajes_Salida();
		
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(conn, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			salida.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			salida.P_OUT_ERRORESCONTADOR = 1L;
			return salida;
		}
		
		for (DefinicionPuntaje_Entrada puntaje : in.p_Puntajes) {
			if (puntaje.p_PuntajeHasta == null|| puntaje.p_PuntajeHasta <= 0) {
				String codigo = "UI_PUNTAJE_NO_VALIDO";
				String descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa, codigo).msg();
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
				salida.P_OUT_ERRORESCONTADOR = 1L;
				return salida;
			}
		}
		
		
		SQL _sql = new SQL();
		
		HashMap<String,Object> params = new HashMap<String, Object>();
		params.put("IdEmpresa", in.p_IdEmpresa);
		List<CategoriaInversionista> categorias = _sql.query(conn, SQL_SELECT_CATEGORIA_INVERSIONISTA, params, CategoriaInversionista.class);
		
		for (DefinicionPuntaje_Entrada puntaje : in.p_Puntajes) {
			boolean found = false;
			for (CategoriaInversionista cat : categorias) {
				if (cat.IdCategoriaInversionista.equalsIgnoreCase(puntaje.p_IdCategoriaInversionista)) {
					found = true;
					break;
				}
			}
			
			if (!found) {
				String codigo = "UI_CATEGORIA_NO_EXISTE";
				String descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa, codigo).msg();
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
				salida.P_OUT_ERRORESCONTADOR = 1L;
				return salida;
			}
		}
		
		//todo validacion categoria ya definida
		//todo validacion puntaje ya definido
		
		List<ActualizarDefinicionPuntajes_Entrada> param = new ArrayList<ActualizarDefinicionPuntajes.ActualizarDefinicionPuntajes_Entrada>();
		param.add(in);
		//limpieza
		_sql.batch(conn, SQL_DELETE_DEFINICION_PUNTAJE, param , ActualizarDefinicionPuntajes_Entrada.class);
		
		List<DefinicionPuntaje> puntajes = new ArrayList<>();
		for (DefinicionPuntaje_Entrada puntaje : in.p_Puntajes) {
			DefinicionPuntaje p = new DefinicionPuntaje();
			p.IdCategoriaInversionista = puntaje.p_IdCategoriaInversionista;
			p.IdEmpresa = in.p_IdEmpresa;
			p.PuntajeHasta = puntaje.p_PuntajeHasta;
			puntajes.add(p);
			
		}
		
		
		_sql.batch(conn, SQL_INSERT_DEFINICION_PUNTAJE, puntajes, DefinicionPuntaje.class);
		
		String codigo = "UI_PUNTAJES_ACTUALIZADOS";
		String descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa, codigo).msg();
		salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion));
		salida.P_OUT_ERRORESCONTADOR = 0L;
		
		return salida;
	}

	public static class ActualizarDefinicionPuntajes_Entrada {
		public Long				p_IdEmpresa;
		public Long				p_IdUsuario;
		public String			p_IdModulo;
		public String			p_simularOperacion;
		public String			p_UsaReglasNegocio;
		
		public List<DefinicionPuntaje_Entrada> p_Puntajes;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DefinicionPuntaje_Entrada {
		public Long 			p_PuntajeHasta;
		public String 			p_IdCategoriaInversionista;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ActualizarDefinicionPuntajes_Salida {

		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		
		public ActualizarDefinicionPuntajes_Salida() {
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	
	public static void main(String [] args) throws SQLException {
		
		
		Connection conn = PlataformaUtils.getDESAConnection();
		Gson gson = Utils.getGsonBuilder();
		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"PLA\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_Puntajes\":[{\"p_PuntajeHasta\":1,\"p_IdCategoriaInversionista\":\"ARRIESGADO\"},{\"p_PuntajeHasta\":2,\"p_IdCategoriaInversionista\":\"BALANCEADO\"}]}";
		ActualizarDefinicionPuntajes_Entrada Objin = gson.fromJson(json, ActualizarDefinicionPuntajes_Entrada.class);
		ActualizarDefinicionPuntajes demo=new ActualizarDefinicionPuntajes();				
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			System.out.println("SALIDA");
			try {
				ActualizarDefinicionPuntajes_Salida ObjOut=demo.logica(conn, Objin);
				conn.rollback();
				System.out.println(ObjOut.toString());
			}
			catch(Exception er) {
			er.printStackTrace();	
			conn.rollback();
		}
		
		
		
		
		
	}
	

}
