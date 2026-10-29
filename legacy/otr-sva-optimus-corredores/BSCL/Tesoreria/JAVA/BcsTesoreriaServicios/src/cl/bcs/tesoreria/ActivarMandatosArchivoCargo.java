package cl.bcs.tesoreria;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgresoGenerado_Salida;
import cl.bcs.tesoreria.CrearIngresoEgreso.ResultadoProceso_Salida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ActivarMandatosArchivoCargo implements JavaCall{
	
	// Log
	private final Log log = new Log("TES.ActivarMandatos");
	
	// Queries
	private final String SQL_SELECT_MANDATOS = loadResourceToString("cl/bcs/tesoreria/sql/ActivarMandatosArchivoCargo_SelectMandatos.sql");
	private final String SQL_UPDATE_MANDATOS = loadResourceToString("cl/bcs/tesoreria/sql/ActivarMandatosArchivoCargo_UpdateMandatos.sql");

	
	public static class MandatosPAC{
		public Long		IdMandatoPAC;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class UpdateMandatos_Salida {

		public List<MensajeSalida>					P_OUT_MENSAJES;
		public Long									P_OUT_ERRORESCONTADOR;
		public List<IngresoEgresoGenerado_Salida>	P_OUT_INGRESOEGRESOGENERADO;
		public List<ResultadoProceso_Salida>        P_OUT_RESULTADOPROCESO;
		
		public UpdateMandatos_Salida()
		{	
			this.P_OUT_MENSAJES 			 = new ArrayList<MensajeSalida>();
			this.P_OUT_INGRESOEGRESOGENERADO = new ArrayList<IngresoEgresoGenerado_Salida>();
			this.P_OUT_RESULTADOPROCESO		 = new ArrayList<ResultadoProceso_Salida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public UpdateMandatos_Salida logica(Connection con, Long DiasDescuento) {
		log.write("INICIO ACTIVAR MANDATOS::");
		
		SQL sql 							= new SQL();
		UpdateMandatos_Salida out 			= new UpdateMandatos_Salida();
		List<MandatosPAC> MandatosUpdate 	= new ArrayList<MandatosPAC>();
		
		try {
			HashMap<String,Object> diasDescuento = 	new HashMap<String,Object>();
			diasDescuento.put("DiaDescuento", DiasDescuento);
			MandatosUpdate = sql.query(con, SQL_SELECT_MANDATOS, diasDescuento, MandatosPAC.class);
			
			if (MandatosUpdate.size() > 0) {
				sql.batch(con, SQL_UPDATE_MANDATOS, MandatosUpdate, MandatosPAC.class);	
			}
			
			out.P_OUT_ERRORESCONTADOR 	= 0L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "Resultado", ""+ MandatosUpdate.size() +" Mandatos actualizados Correctamente."));
			
		} catch (Exception e) {
			out.P_OUT_ERRORESCONTADOR 	= 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "Resultado", "Error al actualizar Mandatos."));
		}
		
		log.write("FIN ACTIVAR MANDATOS::");

		return out;
	}
	
	
	public static void main(String[] args) throws Exception 
	{
		System.out.println("== INICIO LOCAL ==");
		
		try (Connection con = PlataformaUtils.getBOLSAConnection()) {
			try {
				
				Long DiasDescuento = 25L;
				
				ActivarMandatosArchivoCargo demo = new ActivarMandatosArchivoCargo();	
	 			System.out.println("ENTRADA");
	 			System.out.println("Dias Descuento: "+DiasDescuento);
	 			System.out.println("SALIDA");

	 			try { 
	 				UpdateMandatos_Salida ObjOut = demo.logica(con, DiasDescuento);
	 				//con.commit();
	 				con.rollback();
	 				System.out.println(ObjOut.toString());
	 			} 
	 			catch(Exception er) {
					er.printStackTrace();
					con.rollback();
				}
	 			
		} catch (Exception e) {

			e.printStackTrace();			
		}	
		System.out.println("== END ==");
		};
		
		 	
		System.out.println("== FIN PRUEBA LOCAL ==");
	}


	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
		
}
