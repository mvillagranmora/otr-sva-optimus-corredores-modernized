package cl.bcs.fondomutuo;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.GenerarDataReporteTRXI.EntradaReporteTRXI;
import cl.bcs.fondomutuo.GenerarDataReporteTRXI.SalidaReporteTRXI;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarDataReporteTRXIFolioContrato implements JavaCall{
	
	private final Log log = new Log("FM.GenerarDataReporteTRXIFolioContrato");
	
	private final String SQL_SELECT_SOLICITUDES = loadResourceToString("cl/bcs/fondomutuo/sql/GenerarDataReporteTRXIFolioContrato_SelectSolicitudes.sql");
	
	public static class EntradaGenerarDataReporteTRXIFolioContrato {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<InSolicitudes> p_Solicitudes;
		
		public EntradaGenerarDataReporteTRXIFolioContrato(){
			this.p_Solicitudes = new ArrayList<InSolicitudes>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InSolicitudes{
		
		public Long FolioContrato;
		public Long IdSolicitudAporteRescate;
		
		public InSolicitudes() {
			this.FolioContrato = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Solicitud{
		public Long IdSolicitudAporteRescate;
		public Long FolioContrato;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaGenerarDataReporteTRXIFolioContrato {
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaGenerarDataReporteTRXIFolioContrato(){
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>(); 
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaGenerarDataReporteTRXIFolioContrato jin = gson.fromJson(in.toString(), EntradaGenerarDataReporteTRXIFolioContrato.class);
		SalidaGenerarDataReporteTRXIFolioContrato out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaGenerarDataReporteTRXIFolioContrato logica (Connection con, EntradaGenerarDataReporteTRXIFolioContrato entrada) throws Exception {
		log.write("INICIO::GenerarDataReporteTRXIFolioContrato");
		
		SalidaGenerarDataReporteTRXIFolioContrato out = new SalidaGenerarDataReporteTRXIFolioContrato();
		
		SQL sql=new SQL();
		List<Long> foliosContrato = new ArrayList<Long>();
		
		EntradaReporteTRXI entradaReporteTRXI = new EntradaReporteTRXI();
		entradaReporteTRXI.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaReporteTRXI.p_IdUsuario = entrada.p_IdUsuario;
		
		for(InSolicitudes solicitud : entrada.p_Solicitudes) {
			if(solicitud.FolioContrato.equals(0L)) {
				entradaReporteTRXI.p_IdSolicitudAporteRescate.add(solicitud.IdSolicitudAporteRescate);
			}else {
				foliosContrato.add(solicitud.FolioContrato);
			}
		}
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("FolioContrato", foliosContrato);
		List<Solicitud> solicitudes = sql.query(con, SQL_SELECT_SOLICITUDES, params, Solicitud.class);
		
		for(Solicitud solicitud : solicitudes) {
			entradaReporteTRXI.p_IdSolicitudAporteRescate.add(solicitud.IdSolicitudAporteRescate);
		}
		
		GenerarDataReporteTRXI generarDataReporteTRXI = new GenerarDataReporteTRXI();
		
		SalidaReporteTRXI salidaReporteTRXI = generarDataReporteTRXI.logica(con, entradaReporteTRXI);
		
		out.P_OUT_ERRORESCONTADOR = salidaReporteTRXI.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES = salidaReporteTRXI.P_OUT_MENSAJES;
		
		log.write("FIN::GenerarDataReporteTRXIFolioContrato");
		
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getSURAConnection()) {
		
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
			String jsonRF = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":35,\"p_IdModulo\":\"FMM\",\"p_Solicitudes\":[{\"IdSolicitudAporteRescate\":1671751935,\"FolioContrato\":0},{\"IdSolicitudAporteRescate\":1671751926,\"FolioContrato\":0},{\"IdSolicitudAporteRescate\":1671751883,\"FolioContrato\":0},{\"IdSolicitudAporteRescate\":1671751912,\"FolioContrato\":0},{\"IdSolicitudAporteRescate\":1671751930,\"FolioContrato\":0},{\"IdSolicitudAporteRescate\":1671751921,\"FolioContrato\":0},{\"IdSolicitudAporteRescate\":1671751920,\"FolioContrato\":0},{\"IdSolicitudAporteRescate\":1671751887,\"FolioContrato\":0},{\"IdSolicitudAporteRescate\":1671751929,\"FolioContrato\":0},{\"IdSolicitudAporteRescate\":1671751902,\"FolioContrato\":0},{\"IdSolicitudAporteRescate\":1671751908,\"FolioContrato\":0},{\"IdSolicitudAporteRescate\":1671751927,\"FolioContrato\":0},{\"IdSolicitudAporteRescate\":1671751910,\"FolioContrato\":0}]}";
					
			EntradaGenerarDataReporteTRXIFolioContrato Objin = gson.fromJson(jsonRF, EntradaGenerarDataReporteTRXIFolioContrato.class);
			
			GenerarDataReporteTRXIFolioContrato  demo=new GenerarDataReporteTRXIFolioContrato();				
 			
 			try {
 				SalidaGenerarDataReporteTRXIFolioContrato ObjOut=demo.logica(con, Objin);
 				con.commit();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
 			con.rollback(); 			
	    	}
		System.out.println("== END ==");
	}

}
