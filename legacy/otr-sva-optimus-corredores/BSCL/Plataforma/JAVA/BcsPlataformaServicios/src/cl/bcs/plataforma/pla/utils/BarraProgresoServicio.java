package cl.bcs.plataforma.pla.utils;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class BarraProgresoServicio implements JavaCall{
		private static final Logger logger = LogManager.getLogger(BarraProgresoServicio.class);
		private final String SQL_InsertTablaPaso = loadResourceToString("cl/bcs/plataforma/pla/sql/BarraProgresoServicio_InsertTablaPaso.sql");
		private final String SQL_DeleteTablaPaso = loadResourceToString("cl/bcs/plataforma/pla/sql/BarraProgresoServicio_DeleteTablaPaso.sql");
		private final String SQL_FinalizarProgreso = loadResourceToString("cl/bcs/plataforma/pla/sql/BarraProgresoServicio_UpdateProgresoFinalizado.sql");

		private final String SQL_CallRegistrarProgreso = loadResourceToString("cl/bcs/plataforma/pla/sql/BarraProgresoServicio_CallServicioRegistrarProgreso.sql");
		private final String SQL_SelectProgresoServicio = loadResourceToString("cl/bcs/plataforma/pla/sql/BarraProgresoServicio_SelectProgresos.sql");
		
		
	// ENTRADA SERVICIO
		public static class EntradaServicio {
			public Long						p_IdEmpresa;
			public Long						p_IdUsuario;
			public String					p_IdModulo;
			public String 					p_Accion; //CREAR,ACTUALIZAR,CONSULTAR,FINALIZAR
			//
			public DetalleProgreso	p_DetalleProgreso;
			//
			public String toString() {
				return Utils.printJson(this, false);
			}
		}		
		public static class DetalleProgreso {
			public Long 	IdTrabajo; //ID_JOB
			public String	Servicio; //SERVICIO AL CUAL SE LE QUIERE REGISTRAR EL PROGRESO
			public Long		PorcentajeProgreso;
			public String	JsonData; //ALGUN MENSAJE, YA SEA LA RESPUESTA DEL SERVICIO O DETALLE DEL PROCENTAJE DE AVANCE
			public String 	Estado; //PROCESANDO,ERROR,TERMINADO
			public Long     IdUsuario;
			
			public DetalleProgreso() {
				this.IdTrabajo = 0L;
				this.PorcentajeProgreso = 0L;
				this.Servicio = "";
				this.JsonData = "";
				this.Estado = "";
				this.IdUsuario = 0L;
			}
		}
		public static class DetalleSalidaServicio{
			public Long IdTrabajo;
			public Long PorcentajeProgreso;
			public String JsonData;
			public String Estado;
			public String Servicio;
			public Long IdUsuario;
			
			public DetalleSalidaServicio() {
				this.IdTrabajo = 0L;
			}
		}
		//SALIDA SERVICIO
		public static class SalidaServicio {
			public Long P_OUT_ERRORESCONTADOR;
			public List<DetalleSalidaServicio> P_OUT_PROGRESOS;
		}
		
		public static class ProgresoCaducado{
			public Timestamp FechaHoraProgresoCaducado;
		}
	
		public SalidaServicio logica(Connection con, EntradaServicio entrada) throws Exception {
			SalidaServicio salida = new SalidaServicio();
			salida.P_OUT_ERRORESCONTADOR = 0L;
			SQL sql=new SQL();	
			List<DetalleProgreso> listDetalles = new ArrayList<>();
			//CREAR o ACTUALIZAR
			//Registra los datos en la BD
			try {
				if("CREAR".equals(entrada.p_Accion) || "ACTUALIZAR".equals(entrada.p_Accion)) {
					
				
					if(entrada.p_DetalleProgreso != null){
						
						listDetalles.add(entrada.p_DetalleProgreso);
						
						logger.debug("INICIO::SQL_CallRegistrarProgreso");
						
						sql.batch(con,SQL_InsertTablaPaso, listDetalles, DetalleProgreso.class);
						
						logger.debug("FIN::SQL_CallRegistrarProgreso");
						try {
							HashMap<String,Object> inParametros = new HashMap<String,Object>();
							inParametros.put("p_IdTrabajo", entrada.p_DetalleProgreso.IdTrabajo);
							inParametros.put("p_Accion", entrada.p_Accion);
	
							HashMap<String,Class> outParametros = new HashMap<String,Class>();
							logger.debug("INICIO::SQL_CallRegistrarProgreso");
							
							HashMap<String,Object> salidaBloqueoOrden = sql.call(con,SQL_CallRegistrarProgreso,inParametros,outParametros);
							
							logger.debug("FIN::SQL_CallRegistrarProgreso");
							
							logger.debug("INICIO::SQL_DeleteTablaPaso");
							
							sql.batch(con,SQL_DeleteTablaPaso, listDetalles, DetalleProgreso.class);
							
							logger.debug("FIN::SQL_DeleteTablaPaso");
							
						}
						catch(Exception e2) {
							//devolver error
							salida.P_OUT_ERRORESCONTADOR++;
						}
					}
				}
				
			}
			catch(SQLException e) {
				//devolver error
				salida.P_OUT_ERRORESCONTADOR++;
			}
			
			//CONSULTAR O FINALIZAR
			try {
				if("CONSULTAR".equals(entrada.p_Accion)) {
					HashMap<String,Object> paramsIn1 = new HashMap<>();
					paramsIn1.put("p_IdUsuario", entrada.p_IdUsuario);
					
					logger.debug("INICIO::SQL_SelectProgresoServicio");
					
					List<DetalleSalidaServicio> detalleProgreso = sql.query(con, SQL_SelectProgresoServicio, paramsIn1, DetalleSalidaServicio.class);
					salida.P_OUT_PROGRESOS = detalleProgreso;
					
					logger.debug("FIN::SQL_SelectProgresoServicio");
				}
				if("FINALIZAR".equals(entrada.p_Accion)) {
					listDetalles.add(entrada.p_DetalleProgreso);
					
					logger.debug("INICIO::SQL_FinalizarProgreso");
					sql.batch(con,SQL_FinalizarProgreso, listDetalles, DetalleProgreso.class);
					logger.debug("FIN::SQL_FinalizarProgreso");
				}
			}
			catch(Exception e) {
				logger.debug(e);
				salida.P_OUT_ERRORESCONTADOR++;
			}
			
			
			
			
			return salida;
		}
		
		public static void iniciar(Connection con,Long	IdEmpresa, String Servicio,Long IdTrabajo,Long IdUsuario) throws Exception {
			EntradaServicio entrada=new EntradaServicio();
			entrada.p_IdEmpresa=IdEmpresa;
			entrada.p_IdUsuario=IdUsuario;
			entrada.p_IdModulo="PLA";
			entrada.p_Accion = "CREAR";
			
			DetalleProgreso progreso=new DetalleProgreso();
			progreso.IdTrabajo=IdTrabajo;
			progreso.Servicio=Servicio;
			progreso.IdUsuario = IdUsuario;
			progreso.PorcentajeProgreso= 0L;
			entrada.p_DetalleProgreso = progreso;
			
			BarraProgresoServicio obj=new BarraProgresoServicio();
			obj.logica(con,entrada);		
		}
		
		public static void actualizar(Connection con,Long	IdEmpresa, String Servicio,String Estado,String jsonData,Long IdTrabajo,Long porcentajeProgreso) throws Exception {
			EntradaServicio entrada=new EntradaServicio();
			entrada.p_IdEmpresa=IdEmpresa;
			entrada.p_IdUsuario=0L;
			entrada.p_IdModulo="PLA";
			entrada.p_Accion = "ACTUALIZAR";
			
			DetalleProgreso progreso=new DetalleProgreso();
			progreso.IdTrabajo=IdTrabajo;
			progreso.Servicio=Servicio;
			progreso.JsonData=jsonData;
			progreso.Estado = Estado;
			progreso.PorcentajeProgreso= porcentajeProgreso;
			entrada.p_DetalleProgreso = progreso;
			BarraProgresoServicio obj=new BarraProgresoServicio();
			obj.logica(con,entrada);		
					
		}
		
		public static void actualizarConJSON(Connection con,Long	IdEmpresa, String Servicio,String Estado,JsonObject jsonData,Long IdTrabajo,Long porcentajeProgreso) throws Exception {
			
			String jsonToString = jsonData.toString();
			actualizar(con,IdEmpresa, Servicio,Estado,jsonToString, IdTrabajo,porcentajeProgreso);	
					
		}
	
		@Override
		public JsonObject call(Connection con, JsonObject jin) throws Exception {
			Gson gson = new Gson();		
			EntradaServicio in=gson.fromJson(jin.toString() ,EntradaServicio.class);
			SalidaServicio out=logica(con,in);
			JsonParser parser 	= new JsonParser();
	        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
			return dataout;
		}
		
		
		public static void main(String[] args) throws Exception {
			System.out.println("== TEST ==");
			try (Connection con = PlataformaUtils.getDESAConnection()) {
				Gson gson = Utils.getGsonBuilder();
				String json = "{\"p_IdEmpresa\": 1,\"p_IdUsuario\": 97,\"p_IdModulo\": \"ORD\",\"p_Accion\": \"CONSULTAR\"}";
				EntradaServicio Objin = gson.fromJson(json, EntradaServicio.class);
				
				BarraProgresoServicio demo=new BarraProgresoServicio();				
	 			System.out.println("ENTRADA");
	 			System.out.println(Objin.toString());
	 			
	 			try {
	 				SalidaServicio ObjOut = demo.logica(con, Objin);
	 				System.out.println(ObjOut.toString());
	 				con.commit();
	 				//con.rollback();
	 			}
	 			catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
	 			//con.commit();
	 			con.rollback();
		    	}
			 	
			System.out.println("== END ==");
		}
	
}
