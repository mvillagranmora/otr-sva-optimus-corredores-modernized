 package cl.bcs.plataforma.aut;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.evt.AgregarEvento;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class NotificarAutorizaciones implements JavaCall {
	
	private static final Logger logger = LogManager.getLogger(NotificarAutorizaciones.class);

	private final String SQL_SELECT_AUTORIZADOR_EVENTO_AUTORIZACION_A_NOTIFICAR 	= loadResourceToString("cl/bcs/plataforma/aut/sql/NotificarEventoAutorizacion_SelectAutorizadoresEventoAutorizacion.sql");
	private final String SQL_SELECT_EVENTO_AUTORIZACION						= loadResourceToString("cl/bcs/plataforma/aut/sql/NotificarEventoAutorizacion_SelectEventoAutorizacion.sql");

	//-------------------------------------------------------------------------------------------
	// CLASES QUE REPRESENTAN OBJETOS DE ENTRADA
	
	public static class NotificarAutorizacion_Entrada{ 
		public Long 			p_IdEmpresa;
		public Long 			p_IdUsuario;
		public String 			p_IdModulo;
		public List<ObjEventoAutorizacion_Entrada> 	p_IdsEventoAutorizacion;  
		
		public NotificarAutorizacion_Entrada(){
			this.p_IdsEventoAutorizacion = new ArrayList<>();
		}
	    
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class ObjEventoAutorizacion_Entrada{
		public Long p_IdEventoAutorizacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class ObjEventoAutorizacionAutorizador_Entrada{
		public Long p_IdTipoEvento;
		public Long p_IdEtapa;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public class ObjEventoAutorizacionEvento{
		public String IdEventoAutorizacionEvento;
		public Long IdEventoAutorizacion;
		public Long IdEvento;
		public Long IdSecuencia;
	}
	
	public static class EventoAutorizacion{
		public Long IdEvento;
		public Long IdEtapa;
		public Long IdPersona;
		public String Autorizacion;
		public String Glosa;
		public String HoraVigencia;
		public String Usuario;
		public String FechaHora;
		public String Estado;
		public String TipoEvento;
		public String Corredora;
		public String AtencionCliente;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
		

	}
	
	
	
		
	
    //-------------------------------------------------------------------------------------------
	// CLASES QUE REPRESENTAN OBJETOS DE SALIDA	
	
	
    public static class NotificarEventoAutorizacion_Salida{	
    	public List<MensajeSalida> 		P_OUT_MENSAJES;
    	public Long 					P_OUT_ERRORESCONTADOR;
    	   
    	public NotificarEventoAutorizacion_Salida(){
    		this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
    	}
    	
    	public String toString() {
    		return Utils.printJson(this, false);
    	}
    }
    
	
    //-------------------------------------------------------------------------------------------
	// CLASES UTILITARIAS DE APOYO	
	
	public static class EventoAutorizacionAutorizador_Proceso
	{
		public Long 	IdEventoAutorizacion;
		public Long 	IdEventoAutorizacionAutorizador;
		public String 	IdTipoEventoAutorizacion;  //Ej.: PB / SU
		public String 	IdEstado;
		public Long     IdEmpresa;
		
		//Para validaciones
		public long 	errores 		= 0;
		public long 	advertencias 	= 0;
		public long 	autorizaciones 	= 0;
		public long 	ok 				= 0;
	}
	
	public static class ObjDirs {
		public Long 	  	IdPortafolio;
		public Integer 		IdDireccion;
		public String 	  	TipoDestinatario;
		
		public String toString() {
			return Utils.printJson(this, false);
		}	
	}
	
	public static class ObjDatosRemitente{
		public Long IdPersona;
		public Long IdDireccionElectronica;
		
		public String toString() {
			return Utils.printJson(this, false);
		}	
	}

	
	public static class ObjAutorizadorANotificar{
		public Long IdEventoAutorizacion;
		public Long IdSecuencia;
		public String Nombre;
		public String SocNeg;
		public String Port;
		public String CodigoPortafolio;
		//pu

    	public String toString() {
    		return Utils.printJson(this, false);
    	}
		
	}
	
	public static class AutorizadorANotificar extends BcsPojo{		 
		public Long IdEtapa;
		public Long IdPersona;
		public Long IdDireccionElectronica;
		public String IdTipoEvento;
		public String CodigoUsuario;
		public String SocNeg;
		public String NombreEtapa;
		//public Long IdUsuario;
	}
	
	public static class ObjRemitentePersona {
		public Long IdPersonaRemitente;
		
		public String toString() {
			return Utils.printJson(this, false);
		}	
	}
	
	public static class ControlProceso {
		public String ControlProceso;
		
		public String toString() {
			return Utils.printJson(this, false);
		}	
	}
	
	//-------------------------------------------------------------------------------------------
	// LOGICA DEL SERVICIO
	
	public NotificarEventoAutorizacion_Salida logica(Connection conn,NotificarAutorizacion_Entrada paramsIn) throws Exception 
	{
		String boveda;
		
		NotificarEventoAutorizacion_Salida salida = new NotificarEventoAutorizacion_Salida();
		salida.P_OUT_ERRORESCONTADOR = 0L;
		
		ObjDatosRemitente datosRemitente = new ObjDatosRemitente();
		datosRemitente.IdDireccionElectronica = 0L;
		datosRemitente.IdPersona = 0L;
		
		boolean filtraCliente = false;
		

		logger.info("== INICIO GENERACION NOTIFICACIONES PARA EVENTOS DE AUTORIZACION ==");
		logger.info("PARAMETROS DE ENTRADA : " + paramsIn.toString());
			
		if (paramsIn.p_IdsEventoAutorizacion.size() == 0) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_ESPECIFICAN_EVENTOS_AUTORIZACION_A_PROCESAR",null,conn,paramsIn.p_IdEmpresa));				
			return salida;
		}
		
		String FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn,paramsIn.p_IdEmpresa,"PLATAFORMA","PLA.FechaSistema");
		String HoraSistema = FechaDB.ObtenerHoraSistemaLocal(conn,paramsIn.p_IdEmpresa);
		//System.out.println(" hora"+ HoraSistema);
		String tFechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaUTC(conn, paramsIn.p_IdEmpresa);
		Timestamp vFechaHoraSistema = Fecha.toTimeStamp(tFechaHoraSistema);
		String urlLogoPublico = ConfiguracionSistema.getParametroConfiguracionString(conn, paramsIn.p_IdEmpresa, "PLATAFORMA", "PLA.UrlLogoCorredoraPublic");
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();

		List<Long> p_IdEventoAutorizacion = new ArrayList<Long>();
		
		for (ObjEventoAutorizacion_Entrada val:paramsIn.p_IdsEventoAutorizacion){
			p_IdEventoAutorizacion.add(val.p_IdEventoAutorizacion);
		}
		
        params.clear();
        params.put("p_IdEventoAutorizacion", p_IdEventoAutorizacion);
        params.put("p_Fecha", FechaSistema);
        params.put("p_Hora", HoraSistema);
        params.put("p_IdUsuario", paramsIn.p_IdUsuario);
        params.put("p_IdEmpresa", paramsIn.p_IdEmpresa);
		List<EventoAutorizacion> eventos = sql.query(conn, SQL_SELECT_EVENTO_AUTORIZACION, params, EventoAutorizacion.class);
		
		
		for (EventoAutorizacion evento: eventos){
			AgregarEvento.ServiceEntrada notifIn = new AgregarEvento.ServiceEntrada();
	        notifIn.P_IDEMPRESA = paramsIn.p_IdEmpresa;
	        notifIn.P_IDUSUARIO = paramsIn.p_IdUsuario;
	        notifIn.P_IDMODULO = paramsIn.p_IdModulo;
	        notifIn.P_EVENTOS = new ArrayList<>();
	        notifIn.P_VALORES = new ArrayList<>();
	        notifIn.P_ARCHIVOSADJUNTOS = new ArrayList<>();
	        notifIn.P_IDDIRECCIONES = new ArrayList<>();
			
	        	params.clear();
			 	params.put("p_IdTipoEvento", evento.TipoEvento);
				params.put("p_IdEtapa", evento.IdEtapa);
				
			 	List<AutorizadorANotificar> AutorizadoresANotificar= sql.query(conn, SQL_SELECT_AUTORIZADOR_EVENTO_AUTORIZACION_A_NOTIFICAR, params, AutorizadorANotificar.class);	
			 	
			 	if(AutorizadoresANotificar== null || AutorizadoresANotificar.size()==0) {
			 		salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_HAY_AUTORIZADOR_A_NOTIFICAR",null,conn,paramsIn.p_IdEmpresa));				
					return salida;
			 	}
			 	
			 	AgregarEvento.ObjEVENTOS event = new AgregarEvento.ObjEVENTOS();
	 		 	event.CodigoTipoEvento = "NOTIFICAR_EVENTO_AUTORIZACION";
	 		 	event.IdSecuencia = new Long(evento.IdEtapa);
	 		 	event.FechaCreacionSistema = Fecha.toStringDate(vFechaHoraSistema);
	 		 	event.IdPersonaRemitente = new Long(evento.IdPersona);
	 		 	event.IdPersonaDestinatario =   new Long(evento.IdPersona);
	 		 	event.IdPortafolio = new Long(0);	                
	 		 	notifIn.P_EVENTOS.add(event);
	 		 	
	 		 	List<String> CodigoTipoEventoList = new ArrayList<String>();
	 	        CodigoTipoEventoList.add(event.CodigoTipoEvento);
	 			params.clear();
	 	        params.put("CodigoEvento", CodigoTipoEventoList);	
	 	        
	 	        String SQL_SELECT_CONTROL_PROCESO = "SELECT \"ControlProceso\"\r\n"
	 	        		+ "FROM BSCL.\"BSCL.Plataforma.BD::EVT.ConfiguracionEvento\" ce\r\n"
	 	        		+ "WHERE \"Descripcion\" IN (:CodigoEvento)";
	 	        
	 			List<ControlProceso> estadoControlProceso = sql.query(conn, SQL_SELECT_CONTROL_PROCESO, params, ControlProceso.class);
	 	      	
	 	        notifIn.P_CONTROLPROCESO = estadoControlProceso.get(0).ControlProceso; 
	 	        
	 		 	//List<String> listaUsuarios= new ArrayList<>();
	 	        
			 	AutorizadoresANotificar.stream()
				.forEach(codigoUsuario -> {
					if(codigoUsuario.IdEtapa.equals(evento.IdEtapa) && codigoUsuario.IdTipoEvento.equals(evento.TipoEvento)) {						
						AgregarEvento.ObjIDDIRECCIONES dir = new AgregarEvento.ObjIDDIRECCIONES();
		                dir.IdSecuencia = new Long(evento.IdEtapa); //IdEtapaEvento es unico
		                dir.TipoDestinatario = codigoUsuario.SocNeg;
		                dir.IdDireccion = codigoUsuario.IdDireccionElectronica;        
		                notifIn.P_IDDIRECCIONES.add(dir);
		                //listaUsuarios.add(codigoUsuario.IdUsuario+"");
		                //System.out.println("Direcciones: "+dir);
						
					}
				});	

			 	
	           // Valores para el mensaje
				notifIn.addValor(evento.IdEtapa, "ID_EVENTO_AUT", evento.IdEvento+"")
				       .addValor(evento.IdEtapa, "GLOSA", evento.Glosa.length()>300 ? evento.Glosa.substring(300): evento.Glosa)
				       .addValor(evento.IdEtapa, "AUTORIZACION", evento.Autorizacion+"")
				       .addValor(evento.IdEtapa, "FECHAHORA", evento.FechaHora)
				       .addValor(evento.IdEtapa, "USUARIO", evento.Usuario)
				       .addValor(evento.IdEtapa, "FECHA_VIGENCIA", evento.HoraVigencia)
				       .addValor(evento.IdEtapa, "ID_ETAPAEVENTO_AUT", evento.IdEtapa+"")
				       .addValor(evento.IdEtapa, "TIPO_EVENTO_AUT", evento.TipoEvento+"")
				       //.addValor(evento.IdEtapa, "ID_USUARIOS_DESTINO", String.join(",",listaUsuarios))
				       .addValor(evento.IdEtapa, "ATENCIONCLIENTE", evento.AtencionCliente+"")
				       .addValor(evento.IdEtapa, "CORREDORA", evento.Corredora+"")
				       .addValor(evento.IdEtapa, "LOGO_PUBLICO", urlLogoPublico)
				       .addValor(evento.IdEtapa, "ESTADO",evento.Estado+"");
				
		        if (notifIn.P_EVENTOS.size() > 0) {
		            AgregarEvento notif = new AgregarEvento();
		            AgregarEvento.ServiceSalida notifOut = new AgregarEvento.ServiceSalida(); 
		            notifOut = notif.logica(conn, notifIn);
		            logger.info("RESULTADO DE SOLICITUD DE NOTIFICACION : " + notifOut.toString());
		        }  
	         
	        }
		
	
		/*****/
		
		logger.info("== TERMINO NOTIFICACION EVENTO AUTORIZACION ==");
		
		return salida;
	}

	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		//Entrada in = gson.fromJson(jin.toString(), Entrada.class);
		NotificarAutorizacion_Entrada in = gson.fromJson(jin.toString(), NotificarAutorizacion_Entrada.class);
		
		NotificarEventoAutorizacion_Salida out = logica(conn,in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();	
		return dataout;
	}
	
	
	//-------------------------------------------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	
	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBA LOCAL ==");

		try (Connection con = PlataformaUtils.getDESAConnection() ){
			Gson gson = Utils.getGsonBuilder();
			
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6,\"p_IdModulo\":\"SOC\",\"p_IdsEventoAutorizacion\":[{\"p_IdEventoAutorizacion\":285454}],\"p_IdsEventoAutorizacionAutorizador\":[]}";

			NotificarAutorizacion_Entrada Objin = gson.fromJson(json, NotificarAutorizacion_Entrada.class);
			
			NotificarAutorizaciones demo = new NotificarAutorizaciones();		
			
			System.out.println("ENTRADA:");
			System.out.println(Objin.toString());
			
			
			try {
				NotificarEventoAutorizacion_Salida ObjOut= demo.logica(con, Objin);
				con.rollback();
				//con.commit();
				System.out.println("SALIDA:");
				ObjOut.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(ObjOut.P_OUT_MENSAJES);
				System.out.println(ObjOut.toString());
			} catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== FIN PRUEBA LOCAL ==");
    }
	
	

}
