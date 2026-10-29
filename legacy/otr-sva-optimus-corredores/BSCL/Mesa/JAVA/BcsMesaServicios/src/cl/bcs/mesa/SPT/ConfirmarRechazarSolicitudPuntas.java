package cl.bcs.mesa.SPT;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.AgregarMensajeWebsocket;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConfirmarRechazarSolicitudPuntas implements JavaCall {
	
	private static final Logger logger = LogManager.getLogger(ConfirmarRechazarSolicitudPuntas.class);
	
	private final String SQL_SELECT_SOLICITUD_PUNTAS = loadResourceToString("cl/bcs/mesa/SPT/sql/ConfirmarRechazarSolicitudPuntas_SelectSolicitudPuntas.sql");	
	private final String SQL_UPDATE_SOLICITUD_PUNTAS = loadResourceToString("cl/bcs/mesa/SPT/sql/ConfirmarRechazarSolicitudPuntas_UpdateSolicitudPuntas.sql");	
	
	public static class EntradaConfirmarRechazarSolicitudPuntas extends BcsPojo {
		public Long 				p_IdEmpresa;
		public Long 				p_IdSolicitudPuntas;
		public BigDecimal 			p_TipoCambioCostoAprobado;
		public Long 				p_IdUsuarioAutorizador = 0L;
		public String 				p_ObservacionAutorizador;
		public String 				p_IdEstado;					
	}
	
	public static class SolicitudPuntas {
		public Long 				IdEmpresa;
		public Long 				IdSolicitudPuntas;	
		public java.sql.Timestamp 	FechaRegistro;
		public java.sql.Timestamp 	FechaVigencia;
		public Long 				IdOperacion; 
		public String 				IdTipoOperacion;
		public Long 				IdCliente; 
		public BigDecimal 			TipoCambioCostoSolicitado;
		public BigDecimal 			TipoCambioCostoAprobado;
		public BigDecimal 			MontoMonedaPrincipal;
		public String 				IdMonedaPrincipal;	
		public Long 				IdUsuarioSolicitud; 
		public Long 				IdUsuarioAutorizador;
		public String 				ObservacionSolicitud;
		public String 				IdEstado;			
	}
	
	public static class UpdateSolicitudPuntas{
		public Long 				p_IdSolicitudPuntas;
		public Long 				p_IdUsuarioAutorizador = 0L;
		public BigDecimal 			p_TipoCambioCostoAprobado;
		public String 				p_ObservacionAutorizador;		
		public String 				p_IdEstado;
	}
	
	public static class SalidaConfirmarRechazarSolicitudPuntas extends BcsPojo {
		public List<MensajeSalida> 	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		
		public SalidaConfirmarRechazarSolicitudPuntas () {				
			this.P_OUT_MENSAJES 			 = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR 		 = 0L;			
		}		
	}
	

	public SalidaConfirmarRechazarSolicitudPuntas logica(Connection conn, EntradaConfirmarRechazarSolicitudPuntas in) throws Exception {
		logger.info("INIT");
		
		Long init = System.currentTimeMillis();
		
		SalidaConfirmarRechazarSolicitudPuntas salida = new SalidaConfirmarRechazarSolicitudPuntas();		
		SQL sql = new SQL();
		Timestamp fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaTs(conn, in.p_IdEmpresa);

		HashMap<String, Object> params = new HashMap<>(2);
        params.put("IdSolicitudPuntas", in.p_IdSolicitudPuntas);      
        
        List<SolicitudPuntas> solicitudPuntas = sql.query(conn, SQL_SELECT_SOLICITUD_PUNTAS, params, SolicitudPuntas.class);     
        List<UpdateSolicitudPuntas> solicitudesPuntas = new ArrayList<>();
        
        for(SolicitudPuntas sp : solicitudPuntas) {
        	
        	if("PEN".equalsIgnoreCase(sp.IdEstado) && sp.FechaVigencia.getTime() > fechaHoraSistema.getTime())
        	{
        		UpdateSolicitudPuntas usp = new UpdateSolicitudPuntas();        		
        		usp.p_IdEstado = in.p_IdEstado;
        		usp.p_IdSolicitudPuntas = in.p_IdSolicitudPuntas;
        		usp.p_IdUsuarioAutorizador = in.p_IdUsuarioAutorizador;
        		usp.p_ObservacionAutorizador = in.p_ObservacionAutorizador;
        		usp.p_TipoCambioCostoAprobado = in.p_TipoCambioCostoAprobado;
        		
        		solicitudesPuntas = Arrays.asList(usp);
        	}
        	else
        	{
        		String error = "";
        		
	        	if (sp.FechaVigencia.getTime() < fechaHoraSistema.getTime()) {
	                error = "SOLICITUD_NO_ESTA_VIGENTE";
	            } else if (sp.IdEstado.equals("RZD")) {
	                error = "SOLICITUD_SE_ENCUENTRA_RECHAZADA";
	            } else if (sp.IdEstado.equals("APD")) {
	                error = "SOLICITUD_SE_ENCUENTRA_APROBADA";
	            }  	
	        	
	        	
	        	if(!"".equalsIgnoreCase(error))
	        	{
	        		salida.P_OUT_ERRORESCONTADOR += 1L;
	        		salida.P_OUT_MENSAJES.add(new MensajeSalida(sp.IdSolicitudPuntas,MensajeSalida.MENSAJE_ERROR,error, error));
	        	}	            

        	}
        	
        }
        
        if(solicitudesPuntas.size() > 0)
        {
        	sql.batch(conn, SQL_UPDATE_SOLICITUD_PUNTAS, solicitudesPuntas, UpdateSolicitudPuntas.class);
    		
    		String msg = ""; 
    		String code = "";
    		String msgWebsocket = "";        		
    		
    		for(UpdateSolicitudPuntas usp : solicitudesPuntas)
    		{
    			if("APD".equalsIgnoreCase(usp.p_IdEstado))
        		{
        			msg=Diccionario.getMensaje(conn,in.p_IdEmpresa,"SOLICITUD_PUNTAS_APROBADA")
            				  .add("IdSolicitudPuntas", usp.p_IdSolicitudPuntas)      				 
            				  .msg();
        			code = "SOLICITUD_PUNTAS_APROBADA";
        			msgWebsocket = "Punta recibida"
              				+ "\nTC costo Aprobado: "+usp.p_TipoCambioCostoAprobado+""        				
              				+ "\nObservaciones: "+ usp.p_ObservacionAutorizador;              		
        		}
        		else if("RZD".equalsIgnoreCase(usp.p_IdEstado))
        		{
        			msg=Diccionario.getMensaje(conn,in.p_IdEmpresa,"SOLICITUD_PUNTAS_RECHAZADA")
            				  .add("IdSolicitudPuntas", usp.p_IdSolicitudPuntas)      				 
            				  .msg();
        			code = "SOLICITUD_PUNTAS_RECHAZADA";              		
        			msgWebsocket = "Punta rechazada";
        		}    
        		
        		AgregarMensajeWebsocket.agregar(conn, in.p_IdEmpresa, "ENVIOPUNTA_" + usp.p_IdSolicitudPuntas, msgWebsocket);        		
          		salida.P_OUT_MENSAJES.add(new MensajeSalida(usp.p_IdSolicitudPuntas,MensajeSalida.MENSAJE_OK,code,msg));
    		}
    		
        }
		
        logger.info("FIN::EnvioPuntas, " + (System.currentTimeMillis() - init));
		return salida;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = Utils.getGsonBuilder();
		EntradaConfirmarRechazarSolicitudPuntas in = gson.fromJson(jin.toString(), EntradaConfirmarRechazarSolicitudPuntas.class);
		SalidaConfirmarRechazarSolicitudPuntas out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
			//String jsonAprobado = "{\"p_IdEmpresa\":1,\"p_IdUsuarioAutorizador\":9,\"p_IdSolicitudPuntas\":26,\"p_IdEstado\":\"APD\",\"p_TipoCambioCostoAprobado\":869,\"p_ObservacionAutorizador\":\"OBSERVACIO\"}";
			String jsonRechazado = "{\"p_IdEmpresa\":1,\"p_IdUsuarioAutorizador\":9,\"p_IdEstado\":\"RZD\",\"p_TipoCambioCostoAprobado\":0,\"p_ObservacionAutorizador\":\"\"}";
			EntradaConfirmarRechazarSolicitudPuntas Objin = gson.fromJson(jsonRechazado, EntradaConfirmarRechazarSolicitudPuntas.class);
			ConfirmarRechazarSolicitudPuntas demo = new ConfirmarRechazarSolicitudPuntas();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try { 				
 				SalidaConfirmarRechazarSolicitudPuntas ObjOut = demo.logica(con, Objin);
 				//con.commit();
 				con.rollback();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
			finally {
				con.close();
			}
	    }	
		System.out.println("== END ==");
	}
}
