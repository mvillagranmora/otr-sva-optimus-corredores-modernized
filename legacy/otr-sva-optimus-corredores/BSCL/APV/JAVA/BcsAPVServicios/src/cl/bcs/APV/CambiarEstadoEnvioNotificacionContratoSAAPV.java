 package cl.bcs.APV;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.APV.CambiarEstadoContratosSAAPV.ContratosSAAPV;
import cl.bcs.APV.CambiarEstadoContratosSAAPV.ContratosSAAPV_Entrada;
import cl.bcs.APV.CambiarEstadoContratosSAAPV.ContratosSAAPV_Salida;
import cl.bcs.APV.CargaMasivaPAC.Parametros_Entrada;
import cl.bcs.APV.CargaMasivaPrevired.ExistePersona;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CambiarEstadoEnvioNotificacionContratoSAAPV implements JavaCall{
	
	private final Log log = new Log("EstadoEnvioSolicitudContratosSAAPV");
	
	private final String SQL_UPDATE_ESTADO_NOTIFICACION = loadResourceToString("cl/bcs/APV/sql/CambiarEstadoEnvioNotificacionContratoSAAPV.sql");

	public static class ContratosSAAPV {
		public String			IdFolioContratoSAAPV;
		public String			IdEstadoEnvioNotificacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class Params {
		public String 			FolioContratoSAAPV;
		public String 			IdEstadoEnvioNotificacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ContratosSAAPV_Entrada {
	    public Long				p_IdEmpresa;
	    public Long				p_IdUsuario;
	    public String			p_IdModulo;
	    public String			p_Opcion;
	    public List<ContratosSAAPV>	p_ContratosSAAPV;
	    
	    public ContratosSAAPV_Entrada(){
	    	p_ContratosSAAPV = new ArrayList<ContratosSAAPV>();
		}
	   
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ContratosSAAPV_Salida {		
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ContratosSAAPV_Salida(){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}		
	};
	
	public ContratosSAAPV_Salida logica(Connection con, ContratosSAAPV_Entrada in) throws Exception {
		ContratosSAAPV_Salida out = new ContratosSAAPV_Salida();
		SQL sql = new SQL();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		try {
			List<Params> params = new ArrayList<Params>();
			for (ContratosSAAPV row : in.p_ContratosSAAPV) {
				Params p = new Params();
				p.IdEstadoEnvioNotificacion = "ENV";
				p.FolioContratoSAAPV = row.IdFolioContratoSAAPV;
				params.add(p);
			}
			
			sql.batch(con,SQL_UPDATE_ESTADO_NOTIFICACION, params, Params.class);
			
			log.write(out.P_OUT_MENSAJES);
		} finally {
			
		}
		
		return out;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		ContratosSAAPV_Entrada jin = gson.fromJson(in.toString() ,ContratosSAAPV_Entrada.class);
		ContratosSAAPV_Salida out = logica(conn,jin);		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();				//
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"APV\",\"p_Opcion\":\"VIG\",\"p_ContratosSAAPV\":[{\"IdPersona\":1768423,\"IdCliente\":1423114,\"IdEmpleador\":1773898,\"IdFolioContratoSAAPV\":\"0303669\",\"FechaRevocacion\":\"1900-01-01\",\"IdEstadoEnvioNotificacion\":\"NEV\",\"IdEstadoSolicitud\":\"PDA\",\"IdEstado\":\"VIG\"}]}";
				ContratosSAAPV_Entrada Objin2 = gson.fromJson(json, ContratosSAAPV_Entrada.class);
				
				ContratosSAAPV_Entrada Objin = new ContratosSAAPV_Entrada();
				
				CambiarEstadoEnvioNotificacionContratoSAAPV demo = new CambiarEstadoEnvioNotificacionContratoSAAPV();	
				
	 			System.out.println("ENTRADA");
	 			System.out.println(Objin.toString());
	 			System.out.println("SALIDA");
				
				try {
					ContratosSAAPV_Salida ObjOut = demo.logica(con, Objin2);
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