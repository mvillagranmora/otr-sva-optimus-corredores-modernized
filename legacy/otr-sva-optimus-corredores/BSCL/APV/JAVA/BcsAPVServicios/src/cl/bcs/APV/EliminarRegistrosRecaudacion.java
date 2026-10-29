package cl.bcs.APV;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.APV.CrearAnotacionRecaudacion.Anotacion_Insert;
import cl.bcs.APV.CrearRecaudacionDeposito.Recaudacion;
import cl.bcs.APV.GrabarContratoSAAPV.ParametrosEntradaServicio;
import cl.bcs.APV.GrabarContratoSAAPV.ParametrosSalidaServicio;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class EliminarRegistrosRecaudacion implements JavaCall   {
	/*
	 * Queries que van a ser utilizadas por el servicio. 
	 */	
	public String SQL_ELIMINA_REGISTRO_CLIENTE 	 = loadResourceToString("cl/bcs/APV/sql/EliminaRegistroRecaudacionCliente.sql");
	public String SQL_ELIMINA_REGISTRO_EMPLEADOR = loadResourceToString("cl/bcs/APV/sql/EliminaRegistroRecaudacionEmpleador.sql");
	
	/********************
	 * Clases IN/OUT	*
	 ********************/
	public static class ParametrosEntradaServicio
	{
		public 	Long	IdEmpresa;
		public  Long	IdRecaudacion;
		public  Long	IdRecaudacionEmpleador;
		public  Long	IdRecaudacionCliente;
		public  String	TipoPersonaEliminar; //C o E
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	// SERVICIO SALIDA
	public static class ParametrosSalidaServicio {		
			public List<MensajeSalida>	P_OUT_MENSAJES;
			public Long					P_OUT_ERRORESCONTADOR;
			
			public String toString() {
				return Utils.printJson(this, false);
			}
			
			public ParametrosSalidaServicio(){
				P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			}		
	};
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		ParametrosEntradaServicio jin = gson.fromJson(in.toString() ,ParametrosEntradaServicio.class);
		ParametrosSalidaServicio out = logica(conn,jin);		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	/********************
	 * Metodo Principal *
	 ********************/
	public ParametrosSalidaServicio logica(Connection con,ParametrosEntradaServicio in)throws Exception {
		SQL sql = new SQL();
		ParametrosSalidaServicio out = new ParametrosSalidaServicio();
		ParametrosEntradaServicio in_s = new ParametrosEntradaServicio();
		List<ParametrosEntradaServicio> List_in = new ArrayList<ParametrosEntradaServicio>();
		in_s.IdEmpresa 					= in.IdEmpresa;
		in_s.IdRecaudacion 				= in.IdRecaudacion;
		in_s.IdRecaudacionEmpleador 	= in.IdRecaudacionEmpleador;
		in_s.IdRecaudacionCliente		= in.IdRecaudacionCliente;
		in_s.TipoPersonaEliminar		= in.TipoPersonaEliminar;
		
		List_in.add(in_s);
		int[] resEmpleador;
		int[] resCliente;
		Long IdRecaudacionEmpleador = 0L;
		
		if(in_s.TipoPersonaEliminar.equals("E")){
			resEmpleador =sql.batch(con,SQL_ELIMINA_REGISTRO_EMPLEADOR,List_in,ParametrosEntradaServicio.class);
			if(resEmpleador!=null && resEmpleador.length>0 && resEmpleador[0]==1){
				MensajeSalida msg = new MensajeSalida();
				msg.id = 1L;//fila.IdRecaudacion;
				msg.tipo = "OK";
				msg.codigo = "UI_DETALLE_EMPLEADOR_ELIMINADO";
				msg.descripcion = "UI_DETALLE_EMPLEADOR_ELIMINADO";
				out.P_OUT_MENSAJES.add(msg);
			}
			resCliente = sql.batch(con,SQL_ELIMINA_REGISTRO_CLIENTE,List_in,ParametrosEntradaServicio.class);
			if(resCliente!=null && resCliente.length>0 && resCliente[0]==1){
				MensajeSalida msg = new MensajeSalida();
				msg.id = 1L;//fila.IdRecaudacion;
				msg.tipo = "OK";
				msg.codigo = "UI_DETALLE_CLIENTE_ASOCIADO_ELIMINADO";
				msg.descripcion = "UI_DETALLE_CLIENTE_ASOCIADO_ELIMINADO";
				out.P_OUT_MENSAJES.add(msg);
			}
		}else{
			resCliente = sql.batch(con,SQL_ELIMINA_REGISTRO_CLIENTE,List_in,ParametrosEntradaServicio.class);
			if(resCliente!=null && resCliente.length>0 && resCliente[0]==1){
				MensajeSalida msg = new MensajeSalida();
				msg.id = 1L;//fila.IdRecaudacion;
				msg.tipo = "OK";
				msg.codigo = "UI_DETALLE_CLIENTE_ELIMINADO";
				msg.descripcion = "UI_DETALLE_CLIENTE_ELIMINADO";
				out.P_OUT_MENSAJES.add(msg);
				IdRecaudacionEmpleador = in.IdRecaudacionEmpleador;
			}
		}
		
		List<CambiarEstadoRecaudacion.ParametrosRecaudacionEstado> paramsCambioEstado = new ArrayList<CambiarEstadoRecaudacion.ParametrosRecaudacionEstado>();

		CambiarEstadoRecaudacion.ParametrosRecaudacionEstado RecaudacionEstado = new CambiarEstadoRecaudacion.ParametrosRecaudacionEstado();

		
		RecaudacionEstado.FolioRecaudacion = in.IdRecaudacion;
		RecaudacionEstado.IdRecaudacionEmpleador = IdRecaudacionEmpleador;
		RecaudacionEstado.IdRecaudacionCliente = 0L;
		RecaudacionEstado.IdEstado = "ING";
		paramsCambioEstado.add(RecaudacionEstado);
		

		CambiarEstadoRecaudacion.ParametrosEntradaServicio paramsCambioEstado_Entrada = new CambiarEstadoRecaudacion.ParametrosEntradaServicio();

	    paramsCambioEstado_Entrada.p_IdEmpresa = in.IdEmpresa;
	    paramsCambioEstado_Entrada.p_IdUsuario = 0L;
	    paramsCambioEstado_Entrada.p_IdModulo = "APV";
	    paramsCambioEstado_Entrada.p_Parametros = paramsCambioEstado;
		
		CambiarEstadoRecaudacion ProcesoCambioEstado = new CambiarEstadoRecaudacion();
		
	    CambiarEstadoRecaudacion.ParametrosSalidaServicio OutCambioEstado = ProcesoCambioEstado.logica(con, paramsCambioEstado_Entrada);

		con.commit();
		
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {				
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();				//

				String json = "{\"IdEmpresa\":1,\"IdRecaudacion\":654,\"IdRecaudacionCliente\":0,\"IdRecaudacionEmpleador\":221,\"TipoPersonaEliminar\":\"E\"}";
				ParametrosEntradaServicio Objin = gson.fromJson(json, ParametrosEntradaServicio.class);

				EliminarRegistrosRecaudacion demo = new EliminarRegistrosRecaudacion();	
	 			try { 
	 				ParametrosSalidaServicio ObjOut = demo.logica(con, Objin);
	 				con.commit();
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
		}


}