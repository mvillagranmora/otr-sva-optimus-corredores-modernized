package cl.bcs.mesa.jobs;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.mesa.FWD.ValorizacionForward;
import cl.bcs.mesa.FWD.ValorizacionForward.ValorizacionForwardEntrada;
import cl.bcs.mesa.FWD.ValorizacionForward.ValorizacionForwardSalida;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.BcsPojo;

public class ValorizacionForwardJob extends BcsJob {
	
	private static final Logger logger = LogManager.getLogger(ValorizacionForwardJob.class);
	
	public static class ParametroValorizacionForward extends BcsPojo {
		public Long IdEmpresa;
		public Long IdPais;
		public Long IdUsuario;
		public String IdModulo;
		public String UsaReglasNegocio;
		public String FechaProceso; 
		public String IdTipoCanalIngreso;
	}
	
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica(con,false);
	}
	
	public boolean logica(Connection con, boolean debug) throws InterruptedException, Throwable {
		logger.info("INICIO::ValorizacionForwardJob");
		
		ParametroValorizacionForward parametroValorizacionForward = debug ? obtenerParametrosDebug(con) : obtenerParametros(con);
		
		ValorizacionForwardSalida resultadoValorizacion = valorizar(con, parametroValorizacionForward);
		
		if(resultadoValorizacion.P_OUT_ERRORESCONTADOR > 0) {
			String mensajeSalida = resultadoValorizacion.P_OUT_MENSAJES
					.stream()
					.filter(mensaje->MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo))
					.map(mensaje->mensaje.descripcion)
					.collect(Collectors.joining(", "));
			throw new Exception(mensajeSalida);
		}
		
		agregarLog(con, parametroValorizacionForward);
		
		logger.info("FIN::ValorizacionForwardJob");
		return true;
	}
	
	private ParametroValorizacionForward obtenerParametrosDebug(Connection con) throws Exception {
		logger.trace("INICIO::ValorizacionForwardJob.obtenerParametrosDebug");

		ParametroValorizacionForward parametroProcesarCargaOperacionTermino = new ParametroValorizacionForward();
		parametroProcesarCargaOperacionTermino.IdEmpresa = 1L;
		parametroProcesarCargaOperacionTermino.IdUsuario = 9L;
		parametroProcesarCargaOperacionTermino.IdModulo = "MFW";
		parametroProcesarCargaOperacionTermino.UsaReglasNegocio = "S";
		parametroProcesarCargaOperacionTermino.FechaProceso = "2023-01-17";
		parametroProcesarCargaOperacionTermino.IdTipoCanalIngreso = "WBS";
		
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(con, 1L);
		parametroProcesarCargaOperacionTermino.IdPais = empresa.IdPais;
		
		logger.trace("FIN::ValorizacionForwardJob.obtenerParametrosDebug");
		return parametroProcesarCargaOperacionTermino;
	}
	
	private ParametroValorizacionForward obtenerParametros(Connection con) throws Exception {
		logger.trace("INICIO::ValorizacionForwardJob.obtenerParametros");

		ParametroValorizacionForward parametroProcesarCargaOperacionTermino = new ParametroValorizacionForward();
		parametroProcesarCargaOperacionTermino.IdEmpresa = Long.valueOf(this.getParam("IdEmpresa"));
		parametroProcesarCargaOperacionTermino.IdUsuario = Long.valueOf(this.getParam("IdUsuario"));
		parametroProcesarCargaOperacionTermino.IdModulo = this.getParam("IdModulo");
		parametroProcesarCargaOperacionTermino.UsaReglasNegocio = this.getParam("UsaReglasNegocio");
		parametroProcesarCargaOperacionTermino.FechaProceso = this.getParam("Fecha");
		parametroProcesarCargaOperacionTermino.IdTipoCanalIngreso = this.getParam("IdTipoCanalIngreso");
		
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(con, parametroProcesarCargaOperacionTermino.IdEmpresa);
		parametroProcesarCargaOperacionTermino.IdPais = empresa.IdPais;
		
		logger.trace("FIN::ValorizacionForwardJob.obtenerParametros");
		return parametroProcesarCargaOperacionTermino;
	}
	
	private ValorizacionForwardSalida valorizar(Connection con, ParametroValorizacionForward parametroValorizacionForward) throws Exception {
		logger.trace("INICIO::ValorizacionForwardJob.valorizar");
		
		ValorizacionForwardEntrada entradaValorizacionForward = new ValorizacionForwardEntrada();
		entradaValorizacionForward.p_IdEmpresa = parametroValorizacionForward.IdEmpresa;
		entradaValorizacionForward.p_IdPais = parametroValorizacionForward.IdPais;
		entradaValorizacionForward.p_IdUsuario = parametroValorizacionForward.IdUsuario;
		entradaValorizacionForward.p_IdModulo = parametroValorizacionForward.IdModulo;
		entradaValorizacionForward.p_UsaReglasNegocio = parametroValorizacionForward.UsaReglasNegocio;
		entradaValorizacionForward.p_FechaProceso = parametroValorizacionForward.FechaProceso;
		entradaValorizacionForward.p_IdTipoCanalIngreso = parametroValorizacionForward.IdTipoCanalIngreso;
		
		ValorizacionForward valorizacionForward = new ValorizacionForward();
		ValorizacionForwardSalida resultadoValorizacion = valorizacionForward.logica(con, entradaValorizacionForward);
		resultadoValorizacion.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(resultadoValorizacion.P_OUT_MENSAJES);
		
		logger.trace("FIN::ValorizacionForwardJob.valorizar");
		return resultadoValorizacion;
	}
	
	private void agregarLog(Connection con, ParametroValorizacionForward entrada) throws Exception {
		logger.trace("INICIO::ValorizacionForwardJob.agregarLog");
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
		
		log.IdCabeceraLog = 1L;
		log.IdTipoLog = "MODIFICACION";
		log.IdModulo = entrada.IdModulo;
		log.IdUsuario = entrada.IdUsuario;
		log.IdEmpresa = entrada.IdEmpresa;
		log.FechaSistemaLog = fechaSistema;
		log.IdUsuarioAtendido = entrada.IdUsuario;
		log.IdCliente = 0L;
		log.EstadoCliente = "";
		log.NombreCliente = "";
		log.IdTipoDocumentoIdentidad = "";
		log.IdDocumentoIdentidad = "";
		log.IdPortafolio = 0L;
		log.EstadoPortafolio = "";
		log.IdTipoRegistro = "CARTERA";
		log.EstadoRegistro = "VIG";
		log.IdTipoOperacion = "";
		log.Folio = 0L;
		log.Cantidad = BigDecimal.ZERO;
		log.PrecioTasa = BigDecimal.ZERO;
		log.Monto = BigDecimal.ZERO;
		log.IdMonedaOrigen = "";
		log.IdMonedaLiquidacion = "";
		log.TipoCambio = "";
		log.Nemo = "";
		log.IdProceso = 0L;
		log.EstadoProceso = "VIG";
		log.NombreProceso = "Valorizar Forward Por Web Services";						
		entradaAgregarLog.p_Log.add(log);
		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(con, entradaAgregarLog);
		
		logger.trace("FIN::ValorizacionForwardJob.agregarLog");
	}

	@Override
	public void onError(Throwable t) {}

	@Override
	public void pause() {}
	
	public static void main(String [] args) throws InterruptedException, Throwable {
		Connection c = PlataformaUtils.getDESAConnection();
		ValorizacionForwardJob a = new ValorizacionForwardJob();
		a.logica(c, true);
		c.commit();
	}
	
}
