package cl.bcs.fondomutuo;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.AnulacionSolicitudAporteRescate.EntradaAnulacionSolicitudAporteRescate;
import cl.bcs.fondomutuo.AnulacionSolicitudAporteRescate.ResultadoAnulacion;
import cl.bcs.fondomutuo.AnulacionSolicitudAporteRescate.SalidaAnulacionSolicitudAporteRescate;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AnulacionSolicitudAporteRescateFolioContrato implements JavaCall{
	
	private final Log log = new Log("FM.AnulacionSolicitudAporteRescateFolioContrato");
	
	private final String SQL_SELECT_SOLICITUDES = loadResourceToString("cl/bcs/fondomutuo/sql/AnulacionSolicitudAporteRescateFolioContrato_SelectSolicitudes.sql");
	private final String SQL_SELECT_TRASPASOS_EGRESOS_APV = loadResourceToString("cl/bcs/fondomutuo/sql/ConsultaTraspasoEgresoAPVAsociado.sql");
	private final String SQL_ACTUALIZAR_TRASPASO_EGRESO_APV	= loadResourceToString("cl/bcs/fondomutuo/sql/ActualizarTraspasoEgresoAPVAsociado.sql");
	
	public static class EntradaAnulacionSolicitudAporteRescateFolioContrato {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<InSolicitudes> p_Solicitudes;
		
		public EntradaAnulacionSolicitudAporteRescateFolioContrato(){
			this.p_Solicitudes = new ArrayList<InSolicitudes>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InSolicitudes{
		
		public Long FolioContrato;
		public Long IdSolicitudAporteRescate;
		public Long IdCliente;
		public String Estado;
		
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
	
	public static class Traspaso{
		public Long IdTraspaso;
		public Long FolioContrato;
	
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CambiarEstadoTraspaso {
		public String 		IdEstado;
		public Long			IdEmpresa; 
		public Long			IdTraspaso;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class SalidaAnulacionSolicitudAporteRescateFolioContrato {
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List<ResultadoAnulacion> P_SOLICITUDES;
		
		public SalidaAnulacionSolicitudAporteRescateFolioContrato(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_SOLICITUDES = new ArrayList<ResultadoAnulacion>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaAnulacionSolicitudAporteRescateFolioContrato jin = gson.fromJson(in.toString(), EntradaAnulacionSolicitudAporteRescateFolioContrato.class);
		SalidaAnulacionSolicitudAporteRescateFolioContrato out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAnulacionSolicitudAporteRescateFolioContrato logica (Connection con, EntradaAnulacionSolicitudAporteRescateFolioContrato entrada) throws Exception {
		log.write("INICIO::AnulacionSolicitudAporteRescateFolioContrato");
		SalidaAnulacionSolicitudAporteRescateFolioContrato out = new SalidaAnulacionSolicitudAporteRescateFolioContrato();
		SQL sql=new SQL();
		List<Long> foliosContrato = new ArrayList<Long>();
		List<Long> idCliente = new ArrayList<Long>();
		
		
		EntradaAnulacionSolicitudAporteRescate entradaAnulacionSolicitudAporteRescate = new EntradaAnulacionSolicitudAporteRescate();
		entradaAnulacionSolicitudAporteRescate.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaAnulacionSolicitudAporteRescate.p_IdModulo = entrada.p_IdModulo;
		entradaAnulacionSolicitudAporteRescate.p_IdUsuario = entrada.p_IdUsuario;
		
		for(InSolicitudes solicitud : entrada.p_Solicitudes) {
			if(solicitud.FolioContrato.equals(0L)) {
				AnulacionSolicitudAporteRescate.InSolicitudes inSolicitud = new AnulacionSolicitudAporteRescate.InSolicitudes();
				inSolicitud.IdSolicitudAporteRescate = solicitud.IdSolicitudAporteRescate;
				inSolicitud.Estado = solicitud.Estado;
				entradaAnulacionSolicitudAporteRescate.p_Solicitudes.add(inSolicitud);
			}else {
				foliosContrato.add(solicitud.FolioContrato);
				idCliente.add(solicitud.IdCliente);
			}
		}
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("FolioContrato", foliosContrato);
		params.put("IdCliente", idCliente);
		
		List<Solicitud> solicitudes = sql.query(con, SQL_SELECT_SOLICITUDES, params, Solicitud.class);
		
		for(Solicitud solicitud : solicitudes) {
			AnulacionSolicitudAporteRescate.InSolicitudes inSolicitud = new AnulacionSolicitudAporteRescate.InSolicitudes();
			inSolicitud.IdSolicitudAporteRescate = solicitud.IdSolicitudAporteRescate;
			for(InSolicitudes solicitudEntrada : entrada.p_Solicitudes) {
				if(solicitud.FolioContrato.equals(solicitudEntrada.FolioContrato)) {
					inSolicitud.Estado = solicitudEntrada.Estado;
					break;
				}
			}
			entradaAnulacionSolicitudAporteRescate.p_Solicitudes.add(inSolicitud);
		}
		
		AnulacionSolicitudAporteRescate anulacionSolicitudAporteRescate = new AnulacionSolicitudAporteRescate();
		
		SalidaAnulacionSolicitudAporteRescate salidaAnulacionSolicitudAporteRescate = anulacionSolicitudAporteRescate.logica(con, entradaAnulacionSolicitudAporteRescate);
		
		out.P_OUT_ERRORESCONTADOR = salidaAnulacionSolicitudAporteRescate.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES = salidaAnulacionSolicitudAporteRescate.P_OUT_MENSAJES;
		out.P_SOLICITUDES = salidaAnulacionSolicitudAporteRescate.P_SOLICITUDES;
		
		if(out.P_OUT_ERRORESCONTADOR.equals(0L)) {
			List<Traspaso> ExisteTraspasos = sql.query(con, SQL_SELECT_TRASPASOS_EGRESOS_APV, params, Traspaso.class);
			
			if (ExisteTraspasos.size() > 0) {
				
				CambiarEstadoTraspaso Traspaso = new CambiarEstadoTraspaso();
				List<CambiarEstadoTraspaso> lstTraspaso = new ArrayList<CambiarEstadoTraspaso>();
				
				
				for (Traspaso fila : ExisteTraspasos) {	
					for(InSolicitudes solicitudEntrada : entrada.p_Solicitudes) {	
						if(fila.FolioContrato.equals(solicitudEntrada.FolioContrato)) {
							Traspaso.IdTraspaso = fila.IdTraspaso;
							Traspaso.IdEmpresa =  entrada.p_IdEmpresa;
							Traspaso.IdEstado = solicitudEntrada.Estado;
							lstTraspaso.add(Traspaso);
						}
					}
				}
				
				if (lstTraspaso.size()>0) {
					int[] result = sql.batch(con,SQL_ACTUALIZAR_TRASPASO_EGRESO_APV,lstTraspaso, CambiarEstadoTraspaso.class);
				}
			}
			
		}
		
		
		log.write("FIN::AnulacionSolicitudAporteRescateFolioContrato");
		
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getQAConnection()) {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
		String jsonRv = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":15,\"p_IdModulo\":\"FMM\",\"p_Solicitudes\":[{\"IdSolicitudAporteRescate\":10883696,\"FolioContrato\":5391,\"IdCliente\": 67109,\"Estado\":\"ANU\"}]}";
		EntradaAnulacionSolicitudAporteRescateFolioContrato Objin = gson.fromJson(jsonRv, EntradaAnulacionSolicitudAporteRescateFolioContrato.class);
		AnulacionSolicitudAporteRescateFolioContrato  demo=new AnulacionSolicitudAporteRescateFolioContrato();				
		try {
			SalidaAnulacionSolicitudAporteRescateFolioContrato ObjOut=demo.logica(con, Objin);
			//con.commit();
			con.rollback();
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
