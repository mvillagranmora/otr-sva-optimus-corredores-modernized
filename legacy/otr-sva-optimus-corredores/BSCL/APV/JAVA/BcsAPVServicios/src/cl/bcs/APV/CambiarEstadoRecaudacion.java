package cl.bcs.APV;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

//Tesoreria
import cl.bcs.tesoreria.CrearIngresoEgreso;
import cl.bcs.APV.ProcesarDistribucion.DetalleCliente_CambiaEstado;
import cl.bcs.APV.ProcesarDistribucion.DetalleEmpleador_CambiaEstado;
import cl.bcs.APV.ProcesarDistribucion.ParametrosRecaudacion;
import cl.bcs.APV.ProcesarDistribucion.Recaudacion_CambiaEstado;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class CambiarEstadoRecaudacion implements JavaCall   {

	/*
	 * El log
	 */	
	private final Log log = new Log("CambiarEstadoRecaudacion");

	/*
	 * Queries que van a ser utilizadas por el servicio. 
	 */	
	public String SQL_SELECT_OrigenDestinoTransicion = loadResourceToString("cl/bcs/APV/sql/ConsultaOrigenDestinoTransicion.sql");
	public String SQL_CAMBIA_ESTADO_Recaudacion = loadResourceToString("cl/bcs/APV/sql/Deposito_Update.sql");
	public String SQL_CAMBIA_ESTADO_Empleador = loadResourceToString("cl/bcs/APV/sql/DetalleEmpleador_CambiaEstado.sql");
	public String SQL_CAMBIA_ESTADO_Cliente = loadResourceToString("cl/bcs/APV/sql/DetalleCliente_CambiaEstado.sql");
	
	/********************
	 * Clases IN/OUT	*
	 ********************/
	
	public static class ParametrosRecaudacionEstado{

		public Long FolioRecaudacion;
		public Long IdRecaudacionEmpleador;
		public Long IdRecaudacionCliente;
		public String IdEstado;
	}
	
	//  SERVICIO ENTRADA
	public static class ParametrosEntradaServicio {
		
		public 	Long									p_IdEmpresa;
		public	Long									p_IdUsuario;
		public 	String									p_IdModulo;
		public  List<ParametrosRecaudacionEstado>		p_Parametros;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}			

	// SERVICIO SALIDA
	public static class ParametrosSalidaServicio {		
		public List<MensajeSalida>									P_OUT_MENSAJES;
		public Long													P_OUT_ERRORESCONTADOR;

		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ParametrosSalidaServicio(){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}		
	};

	public static class ContratoConsultado {
		public  Long		IdContratoSAAPV;
	    public 	String		IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		ParametrosEntradaServicio jin = gson.fromJson(in.toString() ,ParametrosEntradaServicio.class);
		ParametrosSalidaServicio out = logica(conn,jin);		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	/************************
	 * Métodos Básicos	*	
	 ************************/
	
	public ParametrosSalidaServicio logica(Connection con,ParametrosEntradaServicio in)throws Exception {
		ParametrosSalidaServicio out = new ParametrosSalidaServicio();

		SQL sql = new SQL();
		ParametrosSalidaServicio 	salida 			= new ParametrosSalidaServicio ();
		//MensajeSalida               msg             = new MensajeSalida();
		Long contador = 1L;
		
		for (ParametrosRecaudacionEstado fila : in.p_Parametros){

			//Estado Recaudacion
			if(fila.FolioRecaudacion > 0){
				Recaudacion_CambiaEstado Rec_CE = new Recaudacion_CambiaEstado();
				List<Recaudacion_CambiaEstado> UpRec_CE = new ArrayList<Recaudacion_CambiaEstado>();
				Rec_CE.IdRecaudacion = fila.FolioRecaudacion;
				Rec_CE.IdEstado = fila.IdEstado;
				Rec_CE.IdEmpresa = in.p_IdEmpresa; //fila.IdEmpresa;
				UpRec_CE.add(Rec_CE);
				sql.batch(con,SQL_CAMBIA_ESTADO_Recaudacion,UpRec_CE,Recaudacion_CambiaEstado.class);
			}

			//Estado detalle empleador	
			if(fila.IdRecaudacionEmpleador > 0){
				DetalleEmpleador_CambiaEstado DetEmp_CE = new DetalleEmpleador_CambiaEstado();
				List<DetalleEmpleador_CambiaEstado> UpdDetEmp_CE = new ArrayList<DetalleEmpleador_CambiaEstado>();
				DetEmp_CE.IdRecaudacionEmpleador = fila.IdRecaudacionEmpleador;
				DetEmp_CE.IdEstado = fila.IdEstado;
				DetEmp_CE.IdEmpresa = in.p_IdEmpresa; //fila.IdEmpresa;
				UpdDetEmp_CE.add(DetEmp_CE);
				sql.batch(con,SQL_CAMBIA_ESTADO_Empleador,UpdDetEmp_CE,DetalleEmpleador_CambiaEstado.class);
			}
				
				
			//Estado detalle cliente
			if(fila.IdRecaudacionCliente > 0){
				DetalleCliente_CambiaEstado DetCli_CE = new DetalleCliente_CambiaEstado();
				List<DetalleCliente_CambiaEstado> UpdDetCli_CE = new ArrayList<DetalleCliente_CambiaEstado>();
				DetCli_CE.IdRecaudacionCliente = fila.IdRecaudacionCliente;
				DetCli_CE.IdEstado = fila.IdEstado;
				DetCli_CE.IdEmpresa = in.p_IdEmpresa; //fila.IdEmpresa;
				UpdDetCli_CE.add(DetCli_CE);
				sql.batch(con,SQL_CAMBIA_ESTADO_Cliente,UpdDetCli_CE,DetalleCliente_CambiaEstado.class);
			}
		}

		MensajeSalida msg= new MensajeSalida();
		msg.id = 1L;
		msg.tipo = MensajeSalida.MENSAJE_OK;
		msg.codigo = "OK";
		msg.descripcion = "Proceso ejecutado correctamente";
		out.P_OUT_MENSAJES.add(msg);
		return out;
	}
	/********************
	 * Método Principal *
	 ********************/

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				ParametrosEntradaServicio Objin = new ParametrosEntradaServicio();
				CambiarEstadoRecaudacion demo = new CambiarEstadoRecaudacion();	

				Objin.p_IdEmpresa = 1L;
				Objin.p_IdUsuario = 17L;
				Objin.p_IdModulo = "APV";
				
	 			try { 
	 				ParametrosSalidaServicio ObjOut = demo.logica(con, Objin);
	 				con.commit();
	 				//System.out.println(ObjOut.toString());
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