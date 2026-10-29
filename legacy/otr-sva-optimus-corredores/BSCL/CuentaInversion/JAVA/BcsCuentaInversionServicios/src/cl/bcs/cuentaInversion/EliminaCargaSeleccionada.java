package cl.bcs.cuentaInversion;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public  class EliminaCargaSeleccionada implements JavaCall{
	private final String SQL_ELIMINA_DETALLE       = loadResourceToString("cl/bcs/cuentaInversion/sql/CrearCargaMasiva_EliminaDetalle.sql");
	private final String SQL_ELIMINA_CABECERA      = loadResourceToString("cl/bcs/cuentaInversion/sql/CrearCargaMasiva_EliminaCabecera.sql");
		
	public static class Carga {
		public Long p_IdCarga;
		
	}
	public static class ParametrosEntradaServicio{
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;		 
		public List<Carga>  p_IdCargaMasiva;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ParametrosSalidaServicio{
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		
		public ParametrosSalidaServicio() {
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_ERRORESCONTADOR = 0L;
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();
		ParametrosEntradaServicio in = gson.fromJson(jin.toString(), ParametrosEntradaServicio.class);
		ParametrosSalidaServicio out = logica(con,in);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	/**********************
	 * logica del programa
	 **********************/
	public  ParametrosSalidaServicio logica(Connection con, ParametrosEntradaServicio entrada) throws Exception {
		ParametrosSalidaServicio out = new ParametrosSalidaServicio();
		
		SQL sql = new SQL();			
		HashMap<String,String> mapMsg = new HashMap<>();
		ParametrosEntradaServicio in_s = new ParametrosEntradaServicio();
		List<Long> List_in = new ArrayList<>();
		in_s.p_IdEmpresa  		  = entrada.p_IdEmpresa;
		in_s.p_IdUsuario 		  = entrada.p_IdUsuario;
		in_s.p_IdModulo 	      = entrada.p_IdModulo;
		in_s.p_IdCargaMasiva	  = entrada.p_IdCargaMasiva;
		
								
		sql.batch(con, SQL_ELIMINA_DETALLE, entrada.p_IdCargaMasiva , Carga.class);
		sql.batch(con, SQL_ELIMINA_CABECERA, entrada.p_IdCargaMasiva, Carga.class);
		
		mapMsg.clear();
		out.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_OK,"UI_PROCESO_COMPLETADO", mapMsg, con, entrada.p_IdEmpresa));
					
		return out;
	}
	

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {				
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();				//

				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"CTA\",\"p_UsaReglasNegocio\":\"S\",\"p_IdCargaMasiva\":[{\"p_IdCarga\":95}]}";
				ParametrosEntradaServicio Objin = gson.fromJson(json, ParametrosEntradaServicio.class);

				EliminaCargaSeleccionada demo = new EliminaCargaSeleccionada();	
	 			try { 
	 				ParametrosSalidaServicio ObjOut = demo.logica(con, Objin);
	 				con.commit();
	 				System.out.println(ObjOut.toString());
	 			} 
	 			catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
			}
				
				
			catch (Exception e) {
				e.printStackTrace();			
			}	
			System.out.println("== END ==");
			};
		}

}
