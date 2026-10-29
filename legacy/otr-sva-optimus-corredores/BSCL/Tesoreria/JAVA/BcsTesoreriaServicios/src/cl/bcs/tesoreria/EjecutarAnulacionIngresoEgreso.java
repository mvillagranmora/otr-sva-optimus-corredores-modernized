package cl.bcs.tesoreria;


import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EjecutarAnulacionIngresoEgreso implements JavaCall {

	private final Log log = new Log("TES.EjecutarAnulacionIngresoEgreso");

	/*
	 * Queries que van a ser utilizadas por el servicio. Deben usar : para
	 * optimizar con los PreparedStatement
	 */
	private final String SQL_SELECT_MOVIMIENTOS = loadResourceToString("cl/bcs/tesoreria/sql/EjecutarAnulacionIngresoEgreso_SelectMovimientos.sql");
	private final String SQL_UPDATE_MOVIMIENTOS = loadResourceToString("cl/bcs/tesoreria/sql/EjecutarAnulacionIngresoEgreso_UpdateMovimientos.sql");
	private final String SQL_UPDATE_MOVIMIENTOS_FORMA_DE_PAGO = loadResourceToString("cl/bcs/tesoreria/sql/EjecutarAnulacionIngresoEgreso_UpdateMovimientosFormaDePago.sql");
	
	
	public static class Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_Fecha;
		public String p_SimularOperacion;
				
		public String toString() {
			return Utils.printJson(this, false);
		}
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
		
	public static class movimientoTesoreria_Proceso{
		public Long IdIngresoEgreso;
		public Date FechaMovimiento;
		public Long IdUsuarioAnulacion;		
		
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
		log.write("INICIO::EjecutarAnulacionIngresoEgreso");
		SQL sql = new SQL();
		Salida out = new Salida();
		
		out.P_OUT_ERRORESCONTADOR = 0L;
		
		List<movimientoTesoreria_Proceso> movimientos = null;
		ArrayList<Long> correlativos = new ArrayList<Long>();
		List<movimientoTesoreria_Proceso> anularMovimientos = new ArrayList<movimientoTesoreria_Proceso>();
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
	    log.write("Inicia ejecucion de vencimiento con datos de entrada: " + entrada);
	    params.put("p_IdEmpresa", entrada.p_IdEmpresa);
	    params.put("p_Fecha", entrada.p_Fecha);
    
	    movimientos = sql.query(con, SQL_SELECT_MOVIMIENTOS, params, movimientoTesoreria_Proceso.class);
	    if (movimientos.size() == 0) {
			log.write("Sin movimientos para anular");
			out.P_OUT_DESCRIPCION = "NO HAY REGISTROS PARA ANULAR";
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION, MensajeSalida.MENSAJE_INFORMACION, out.P_OUT_DESCRIPCION));
			out.P_OUT_ERRORESCONTADOR = 0L;
			return out;
		}
	        
	    for (movimientoTesoreria_Proceso mov: movimientos) {
	    	correlativos.add(mov.IdIngresoEgreso);
		}
	    
	    for(Long procesar : correlativos ){
	    	movimientoTesoreria_Proceso reg =new movimientoTesoreria_Proceso();
			reg.IdIngresoEgreso = procesar;
			reg.IdUsuarioAnulacion = entrada.p_IdUsuario;
			reg.FechaMovimiento = dateFormat.parse(entrada.p_Fecha);
			
			anularMovimientos.add(reg);	
		}
	    
	    if(anularMovimientos.size() > 0){
	    	sql.batch(con, SQL_UPDATE_MOVIMIENTOS, anularMovimientos, movimientoTesoreria_Proceso.class);
	    	sql.batch(con, SQL_UPDATE_MOVIMIENTOS_FORMA_DE_PAGO, anularMovimientos, movimientoTesoreria_Proceso.class);
	    	
	    	
	    	HashMap<String,String> parametrosMensaje = new HashMap<String,String>();
	    	parametrosMensaje.put("Cantidad", String.valueOf(anularMovimientos.size()));
	    	out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"MOVIMIENTOS_ANULADOS",parametrosMensaje,con,entrada.p_IdEmpresa));	    	
		}
	    	    	    				
		log.write("FIN::EjecutarAnulacionIngresoEgreso");
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getBOLSAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":288,\"p_IdModulo\":\"TES\",\"p_UsaReglasNegocio\":\"S\",\"p_SimularOperacion\":\"N\",\"p_Fecha\":\"2022-02-03\"}";
			
			Entrada Objin = gson.fromJson(json, Entrada.class);

			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			System.out.println("SALIDA");

			EjecutarAnulacionIngresoEgreso ven = new EjecutarAnulacionIngresoEgreso();
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
