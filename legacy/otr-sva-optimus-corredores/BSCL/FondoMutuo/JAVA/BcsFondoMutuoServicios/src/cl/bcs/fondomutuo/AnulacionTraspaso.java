package cl.bcs.fondomutuo;

import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.utils.Log;

import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.pojo.ResultadoSolicitud;


import cl.bcs.fondomutuo.pojo.Traspaso.RegistroTraspaso;






public class AnulacionTraspaso implements JavaCall   {

	private final Log log = new Log("FM.AnulacionTraspaso");
	
	public String SQL_TRASPASO_SELECT = loadResourceToString("cl/bcs/fondomutuo/sql/Traspaso_Anulacion_SelectTraspaso.sql");
	
	public String SQL_TRASPASO_UPDATE = loadResourceToString("cl/bcs/fondomutuo/sql/Traspaso_Anulacion_UpdateTraspaso.sql");
	

	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	public static class InTraspaso{
		public Long 	IdTraspaso;
		public String 	IdMotivoAnulacion;
		public String 	FechaAnulacion;
		
		private String  EstadoProceso ="";
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	};
	
	public static class UpdTraspaso{
		public Long 	IdTraspaso;
		public String 	IdMotivoAnulacion;
		public String 	FechaAnulacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	};
	

	
	//  ENTRADA
	public static class EntradaAnulacionTraspaso {
		
		public 	Long						p_IdEmpresa;
		public	Long						p_IdUsuario;
		public 	String						p_IdModulo;
		public	List<InTraspaso>			p_Traspaso;
		
		
		public EntradaAnulacionTraspaso(){
			p_Traspaso 					= new ArrayList<InTraspaso>();
			
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
	
	// SALIDA

	public static class SalidaAnulacionTraspaso{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List <ResultadoSolicitud> P_OUT_RESULTADO;
		
		public SalidaAnulacionTraspaso(){
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<ResultadoSolicitud>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaAnulacionTraspaso jin = gson.fromJson(in.toString(), EntradaAnulacionTraspaso.class);
		SalidaAnulacionTraspaso out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAnulacionTraspaso logica(Connection con, EntradaAnulacionTraspaso entrada) throws Exception{
	
		log.write("INICIO SalidaAnulacionTraspaso");
		SalidaAnulacionTraspaso out = new SalidaAnulacionTraspaso();
		SQL sql=new SQL();
		
		//String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			return out;
		}
		
		
		if(entrada.p_Traspaso.size() == 0){
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_MODIFICACIONES_DE_TRASPASO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			return out;
		}
		
		ArrayList<Long> correlativos = new ArrayList<Long>();
		for (InTraspaso mov: entrada.p_Traspaso) {
			if(mov.IdTraspaso.longValue() == 0L){
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ID_INTERNO_NO_CERO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				return out;
			}
			if (correlativos.contains(mov.IdTraspaso)) {
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CORRELATIVO_INCORRECTO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				return out;
			} else {
				correlativos.add(mov.IdTraspaso);
			}
		}
		
		List<Long> idTraspaso = new ArrayList<Long>();
		for(InTraspaso in : entrada.p_Traspaso){
			if(!idTraspaso.contains(in.IdTraspaso)) idTraspaso.add(in.IdTraspaso);
		}
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("IdTraspaso", idTraspaso);
		
		List<RegistroTraspaso> traspasos = sql.query(con, SQL_TRASPASO_SELECT, params, RegistroTraspaso.class);
		
		List<InTraspaso> solicitudProcesar = new ArrayList<InTraspaso>();
		List<InTraspaso> solicitudInvalida = new ArrayList<InTraspaso>();
		//validaciones del Traspaso
		for(InTraspaso in : entrada.p_Traspaso){
		
				for(RegistroTraspaso solicitud : traspasos){
					in.EstadoProceso = _c.EstadoPendiente;
				
					
					if( !(_c.TraspasoEstadoIngresado.equalsIgnoreCase(solicitud.IdEstado))){
						in.EstadoProceso = _c.EstadoRechazado;
						out.P_OUT_ERRORESCONTADOR++;
						out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdTraspaso,MensajeSalida.MENSAJE_ERROR,"UI_ESTADO_TRASPASO_INVALIDO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
						continue;
					}
					
				}
				if (_c.EstadoRechazado.equalsIgnoreCase(in.EstadoProceso)){
					solicitudInvalida.add(in);
				}else{
					
					solicitudProcesar.add(in);	
				};	
				
		}//fin Validaiones
		
		
		List<UpdTraspaso> actualizarTraspaso = new ArrayList<UpdTraspaso>();
		//Procesar traspasos
		for(InTraspaso procesar : solicitudProcesar){
			UpdTraspaso reg =new UpdTraspaso();
			reg.FechaAnulacion=procesar.FechaAnulacion;
			reg.IdMotivoAnulacion=procesar.IdMotivoAnulacion;
			reg.IdTraspaso=procesar.IdTraspaso;
			actualizarTraspaso.add(reg);	
		}
		
		
		if(actualizarTraspaso.size() > 0){
			sql.batch(con, SQL_TRASPASO_UPDATE, actualizarTraspaso, UpdTraspaso.class);
			
		}
	

		return out;
	}
	/********************
	 * Metodo Principal *
	 ********************/

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
				
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6,\"p_IdModulo\":\"FMM\",\"p_Traspaso\":[{\"IdTraspaso\":41,\"IdMotivoAnulacion\":\"CANADM\",\"FechaAnulacion\":\"2018-03-08\"}]}";
				
				EntradaAnulacionTraspaso Objin_json = gson.fromJson(json, EntradaAnulacionTraspaso.class);
				
				AnulacionTraspaso demo = new AnulacionTraspaso();	
				
	 			System.out.println("ENTRADA");
	 			System.out.println(Objin_json.toString());
	 			System.out.println("SALIDA");

	 			try { 
	 				SalidaAnulacionTraspaso ObjOut = demo.logica(con, Objin_json);
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