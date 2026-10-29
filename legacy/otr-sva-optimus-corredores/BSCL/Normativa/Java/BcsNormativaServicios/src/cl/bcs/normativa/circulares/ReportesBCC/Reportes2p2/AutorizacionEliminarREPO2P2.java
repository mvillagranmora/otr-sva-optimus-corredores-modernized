package cl.bcs.normativa.circulares.ReportesBCC.Reportes2p2;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cl.bcs.plataforma.aut.BcsAutorizaciones;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import com.google.gson.JsonObject;
import cl.bcs.utils.*;
import cl.bcs.plataforma.aut.pojo.Evento;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;




public class AutorizacionEliminarREPO2P2 implements JavaCall, BcsAutorizaciones{
	static final Logger logger = LogManager.getLogger(AutorizacionEliminarREPO2P2.class);
	
	private final String  SQL_DATOS_DELETE = loadResourceToString("cl/bcs/normativa/circulares/ReportesBCC/Reportes2p2/sql/AutorizacionEliminarREPO2P2_DatosREPO2P2_delete.sql");
	private final String  SQL_SOLICITUD_UPDATE = loadResourceToString("cl/bcs/normativa/circulares/ReportesBCC/Reportes2p2/sql/AutorizacionEliminarREPO2P2_Solicitud_update.sql");
	private final String  SQL_REGISTRO_SOLICITUD_CAMBIO = loadResourceToString("cl/bcs/normativa/circulares/ReportesBCC/Reportes2p2/sql/AutorizacionEliminarREPO2P2_Registro_Solicitud_Cambio.sql");
	
	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		SalidaAutorizacion s = new SalidaAutorizacion();
		try {
			
			SQL sql = new SQL();
			entrada.p_Lista = entrada.p_Lista; // aqui vienen los eventos por autorizar de la tabla AUT.EVENTO
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema"); 
			Long p_IdLog = 0L;		
			HashMap<String, Object> parametroEntrada = new HashMap<String, Object>();		
		
		 	List<RegistroSolicitud> solicitud = new ArrayList<RegistroSolicitud>();
			
			for(Evento evt : entrada.p_Lista)
			{
				 RegistroSolicitud reg = new RegistroSolicitud();
				 reg.IdSolicitudCambio=Long.valueOf(evt.NumeroOperacionConcepto);
				 reg.IdEstadoAutorizacion="AUT";
				 solicitud.add(reg);
				 parametroEntrada.put("IdSolicitudCambio", Long.valueOf(evt.NumeroOperacionConcepto));
				 p_IdLog = Long.valueOf(evt.NumeroOperacionConcepto);
			}
			List<RegistroSolicitudCambio> registroSolicitudCambio = sql.query(conn, SQL_REGISTRO_SOLICITUD_CAMBIO,parametroEntrada , RegistroSolicitudCambio.class);		
	 
		  	sql.batch(conn, SQL_DATOS_DELETE, solicitud, RegistroSolicitud.class);
		  	sql.batch(conn, SQL_SOLICITUD_UPDATE, solicitud, RegistroSolicitud.class);
		  	
		  	s.P_OUT_ERRORESCONTADOR = 0L;
			s.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"SOLICITUD_AUTORIZADA","Solicitud autorizada con exito"));
			
			ResultadoAutorizacion res = new ResultadoAutorizacion();
			res.evento = 1L;
			res.resultado = EXITO_AUTORIZACION;
			s.P_OUT_RESULTADO.add(res);
			
			 // AGREGAR LOG
            AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
            entradaAgregarLog.p_IdEmpresa = entrada.p_IdEmpresa;
            entradaAgregarLog.p_IdUsuario = entrada.p_IdUsuario;
            entradaAgregarLog.p_IdModulo = "NOR";

            AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();

            log.IdCabeceraLog = p_IdLog;
            log.IdTipoLog = "ELIMINACION";
            log.IdTipoRegistro = "ELIMINAR_REPO2P2";
            log.IdUsuario = entrada.p_IdUsuario; //usuario quien autoriza
            log.IdUsuarioAtendido = registroSolicitudCambio.get(0).IdUsuarioAtendido; //usuario quien modifica
            log.IdEmpresa = entrada.p_IdEmpresa;
            log.IdModulo = "NOR";
            log.FechaSistemaLog = fechaSistema;
            log.NombreProceso = "EliminarRepo2P2";
            entradaAgregarLog.p_Log.add(log);
            
            //Detalle del log
            
            for(RegistroSolicitudCambio solicitudCambio : registroSolicitudCambio) {
            	AgregarLog.InAgregarLog.DetalleLog logDetalle = new AgregarLog.InAgregarLog.DetalleLog();            	

            	logDetalle.IdCabeceraLog = p_IdLog;
            	logDetalle.SubRegistro = "ELIMINA";
            	logDetalle.CampoRegistro = "IdCargoAbonoMovimiento";
            	logDetalle.ValorAntiguo = solicitudCambio.ValorAntiguo;
            	logDetalle.ValorNuevo = solicitudCambio.ValorNuevo;

                entradaAgregarLog.p_LogDetalle.add(logDetalle);
            }
            if(!entradaAgregarLog.p_LogDetalle.isEmpty()) {	                
	                AgregarLog agregarLogService = new AgregarLog();
	                agregarLogService.logica(conn, entradaAgregarLog);	                
	        }
			
			return s;
	
	    } catch (Exception e) {

		  	s.P_OUT_ERRORESCONTADOR = 1L;
		  	s.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"SOLICITUD_ERROR","Solicitud con error"));
			e.printStackTrace();
			return s;
		}
		
		
		
	}

	
	public static class RegistroSolicitud extends BcsPojo{
		public Long IdSolicitudCambio;
		public String IdEstadoAutorizacion;
	}
	
	public static class RegistroSolicitudCambio extends BcsPojo{
		public Long IdSolicitudCambio;
		public String IdEstadoAutorizacion;
		public String IdTipoEntidad;
		public Long IdEntidadOrigen;
		public String CampoModificado;
		public String TipoDato;
		public String ValorAntiguo;
		public String ValorNuevo;
		public Timestamp FechaSolicitud;
		public Timestamp FechaRevision;
		public String IdCargoAbonoMovimiento;
		public Long IdUsuarioAtendido;
		
	}
	
	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		SalidaAutorizacion s = new SalidaAutorizacion();
		try {
		SQL sql = new SQL();
		entrada.p_Lista = entrada.p_Lista; // aqui vienen los eventos por autorizar de la tabla AUT.EVENTO
		
	 	List<RegistroSolicitud> solicitud = new ArrayList<RegistroSolicitud>();
		
		for(Evento evt : entrada.p_Lista)
		{
			 RegistroSolicitud reg = new RegistroSolicitud();
			 reg.IdSolicitudCambio=Long.valueOf(evt.NumeroOperacionConcepto);
			 reg.IdEstadoAutorizacion="RZD";
			 solicitud.add(reg);
		}
		sql.batch(conn, SQL_SOLICITUD_UPDATE, solicitud, RegistroSolicitud.class);
		
		
		s.P_OUT_ERRORESCONTADOR = 0L;
		s.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"SOLICITUD_RECHAZADA","Orden RECHAZADA con exito"));
		
		ResultadoAutorizacion res = new ResultadoAutorizacion();
		res.evento = 1L;
		res.resultado = EXITO_AUTORIZACION;
		s.P_OUT_RESULTADO.add(res);
		
		return s;
		
	    } catch (SQLException e) {

		  	s.P_OUT_ERRORESCONTADOR = 1L;
		  	s.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"SOLICITUD_ERROR","Solicitud con error"));
			e.printStackTrace();
			return s;
		}
	}
	
		
		    
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}
		 
	
	
	public static void main (String [] args) throws SQLException {
		
		
		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			AutorizacionEliminarREPO2P2 e = new AutorizacionEliminarREPO2P2();
			EntradaAutorizacion entrada = new EntradaAutorizacion();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdModulo = "PLA";
			entrada.p_IdUsuario = 0L;
			
			Evento evento = new Evento();
			evento.NumeroOperacionConcepto = "653276";
			entrada.p_Lista.add(evento);
			//e.autorizarEvento(conn, entrada);
			e.rechazarEvento(conn, entrada);
			conn.commit();
			
		}
		
	}

}
