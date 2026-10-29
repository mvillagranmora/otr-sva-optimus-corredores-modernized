package cl.bcs.tesoreria;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.AnularRegistros.AnularRegistros_Entrada;
import cl.bcs.tesoreria.AnularRegistros.AnularRegistros_Salida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EjecutarAnulacionComprobantes implements JavaCall {
	private final Log log = new Log("TES.EjecutarAnulacionComprobantes");

	private final String SQL_SELECT_COMPROBANTES = loadResourceToString("cl/bcs/tesoreria/sql/EjecutarAnulacionComprobantes_SelectComprobantes.sql");
	
	public static class Entrada {
		public Long							p_IdEmpresa;
		public Long							p_IdUsuario;
		public String						p_FechaMovimiento;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class Salida {
		public Long P_OUT_ERRORESCONTADOR;
		public Long P_OUT_ADVERTENCIASCONTADOR;
		public String P_OUT_DESCRIPCION;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public Salida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}		
	}
		
	public static class comprobante_registro{
		public Long IdComprobante;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	

	/*******************
	 * metodo principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		log.write("*** Inicio de la ejecucion ***");
		Gson gson = new Gson();
		Entrada in = gson.fromJson(jin.toString(), Entrada.class);
		Salida out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		log.write("*** Fin de la ejecucion ***");
		return dataout;
	}

	
	/**********************
	 * logica del programa
	 **********************/
	public Salida logica(Connection con, Entrada entrada) throws Exception {
		log.write("INICIO::EjecutarAnulacionComprobantes");
		SQL sql = new SQL();
		Salida out = new Salida();
		
		out.P_OUT_ERRORESCONTADOR = 0L;
		
		List<comprobante_registro> comprobantes = null;
		
		HashMap<String,Object> params = new HashMap<String,Object>();
	    params.put("p_IdEmpresa", entrada.p_IdEmpresa);
	    params.put("p_Fecha", entrada.p_FechaMovimiento);
	    
		comprobantes = sql.query(con, SQL_SELECT_COMPROBANTES, params, comprobante_registro.class);
		if (comprobantes.size() == 0) {
			log.write("Sin comprobantes para anular");
			out.P_OUT_DESCRIPCION = "No hay registros para anular. ";
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION, MensajeSalida.MENSAJE_INFORMACION, out.P_OUT_DESCRIPCION));
			out.P_OUT_ERRORESCONTADOR = 0L;
			return out;
		}
		
		// Estos seran los comprobantes a anular
		List<AnularRegistros.Registro_Entrada> lista = new ArrayList<AnularRegistros.Registro_Entrada>();
		for (comprobante_registro com : comprobantes) {
			AnularRegistros.Registro_Entrada registro = new AnularRegistros.Registro_Entrada();
			registro.Id = com.IdComprobante;
			registro.Entidad = "TES.Comprobante";
			lista.add(registro);
		}
		
		AnularRegistros_Entrada in_anular_registros = new AnularRegistros_Entrada();
		in_anular_registros.p_IdEmpresa = entrada.p_IdEmpresa;
		in_anular_registros.p_IdUsuario = entrada.p_IdUsuario;
		in_anular_registros.p_IdModulo  = "TES";
		in_anular_registros.p_UsaReglasNegocio = "S";
		in_anular_registros.p_FechaMovimiento = entrada.p_FechaMovimiento;
		in_anular_registros.p_RegistrosAProcesar = lista;
		
		AnularRegistros anularRegistros = new AnularRegistros();
		AnularRegistros_Salida outAnularRegistros = anularRegistros.logica(con, in_anular_registros);
		    				
		out.P_OUT_DESCRIPCION = String.valueOf(outAnularRegistros.P_OUT_MENSAJES.size()) + " comprobantes anulados. ";
		out.P_OUT_MENSAJES = outAnularRegistros.P_OUT_MENSAJES;
		out.P_OUT_ERRORESCONTADOR = outAnularRegistros.P_OUT_ERRORESCONTADOR;
		log.write("FIN::EjecutarAnulacionComprobantes");
		
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02","connections.properties")) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_FechaMovimiento\":\"2024-06-04\"}";
			
			Entrada Objin = gson.fromJson(json, Entrada.class);

			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			System.out.println("SALIDA");

			EjecutarAnulacionComprobantes ven = new EjecutarAnulacionComprobantes();
			try {
				Salida resp = ven.logica(con, Objin);
				if(resp.P_OUT_ERRORESCONTADOR == 0) {
					con.rollback();
					//con.commit();
				} else {					
					con.rollback();
				}
				con.close();
				System.out.println(resp);
			} catch (Exception e) {
				e.printStackTrace();
				con.rollback();
			}
			
			System.out.println("== END ==");
		}
	}
}
