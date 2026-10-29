package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.pojo.SolicitudCambio;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.socionegocio.per.ConfirmarEjecutarCambiosPersona.EntradaConfirmarEjecutarCambiosPersona;

public class AgregarLogPersona {

	static final Logger logger = LogManager.getLogger(AgregarLogPersona.class);
	
	public void agregarLog(Connection con, EntradaConfirmarEjecutarCambiosPersona entrada, List<SolicitudCambio> solicitudCambio, String tipoEntidad, String tipoLog,  String nombreProceso) throws Exception {
		logger.debug("INICIO::agregarLog");
		long inicio = System.currentTimeMillis();
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		entradaAgregarLog.p_Log = entrada.p_Entidad.parallelStream().map(datoGeneral->{
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdCabeceraLog = datoGeneral.IdEntidad;
			log.IdTipoLog = tipoLog;
			log.IdModulo = entrada.p_IdModulo;
			log.IdUsuario = entrada.p_IdUsuario;
			log.IdEmpresa = entrada.p_IdEmpresa;
			log.FechaSistemaLog = fechaSistema;
			log.IdUsuarioAtendido = entrada.p_IdUsuario;
			log.IdRegistro = datoGeneral.IdEntidad.toString();
			log.IdTipoRegistro = tipoEntidad;
			log.EstadoRegistro = entrada.p_IdEstadoAutorizacion;
			log.IdProceso = 0L;
			log.EstadoProceso = entrada.p_IdEstadoAutorizacion;
			log.IdPersona = datoGeneral.IdPersona;
			try {
				log.NombreProceso = Diccionario.getMensaje(con, entrada.p_IdEmpresa, nombreProceso).msg();
			} catch (Exception e) {
				log.NombreProceso = nombreProceso;
			}
			return log;
		}).collect(Collectors.toList());
		
		entradaAgregarLog.p_LogDetalle = solicitudCambio.parallelStream().map(solicitud->{
			AgregarLog.InAgregarLog.DetalleLog detalleLog = new AgregarLog.InAgregarLog.DetalleLog();			
			detalleLog.IdTrabajo = 0L; 
			detalleLog.IdCabeceraLog = solicitud.IdEntidadOrigenLong;			
			detalleLog.SubRegistro = solicitud.IdTipoEntidad;
			detalleLog.CampoRegistro = solicitud.CampoModificado;
			detalleLog.ValorAntiguo = solicitud.ValorAntiguo; 
			detalleLog.ValorNuevo = solicitud.ValorNuevo;	
			return detalleLog;
		}).collect(Collectors.toList());
		
		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(con, entradaAgregarLog);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::agregarLog", fin - inicio);
	}	
}
