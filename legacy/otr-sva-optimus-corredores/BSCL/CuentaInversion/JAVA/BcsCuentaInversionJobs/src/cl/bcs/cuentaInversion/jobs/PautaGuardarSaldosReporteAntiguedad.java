package cl.bcs.cuentaInversion.jobs;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.cuentaInversion.GuardarSaldosReporteAntiguedad;
import cl.bcs.cuentaInversion.GuardarSaldosReporteAntiguedad.EntradaGuardarSaldosReporteAntiguedad;
import cl.bcs.cuentaInversion.GuardarSaldosReporteAntiguedad.SalidaGuardarSaldosReporteAntiguedad;
import cl.bcs.plataforma.jobs.BcsProcesoPauta;
import cl.bcs.plataforma.jobs.BcsProcesoPauta.SalidaProcesoPauta;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;

public class PautaGuardarSaldosReporteAntiguedad implements BcsProcesoPauta {
	private final static Logger logger = LogManager.getLogger(PautaGuardarSaldosReporteAntiguedad.class);

	@Override
	public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws Throwable {
		logger.info("INICIO::PautaGuardarSaldosReporteAntiguedad");
		SalidaProcesoPauta salida = new SalidaProcesoPauta();
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,(Long)parameters.get("IdEmpresa"),"PLATAFORMA","PLA.FechaSistema");
		
		GuardarSaldosReporteAntiguedad guardarSaldosReporteAntiguedad = new GuardarSaldosReporteAntiguedad();
		EntradaGuardarSaldosReporteAntiguedad entradaGuardarSaldosReporteAntiguedad = new EntradaGuardarSaldosReporteAntiguedad();
		entradaGuardarSaldosReporteAntiguedad.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
		entradaGuardarSaldosReporteAntiguedad.p_IdUsuario = (Long)parameters.get("IdUsuario");
		entradaGuardarSaldosReporteAntiguedad.p_FechaSistema = fechaSistema;
		SalidaGuardarSaldosReporteAntiguedad salidaGuardarSaldosReporteAntiguedad = guardarSaldosReporteAntiguedad.logica(con, entradaGuardarSaldosReporteAntiguedad);
		
		salida.P_OUT_MENSAJE = PlataformaUtils.concatenarMensajesSalida(salidaGuardarSaldosReporteAntiguedad.P_OUT_MENSAJES);
		salida.P_OUT_ERRORESCONTADOR = salidaGuardarSaldosReporteAntiguedad.P_OUT_ERRORESCONTADOR;
		
		logger.info("Errores: " + salida.P_OUT_ERRORESCONTADOR);
		
		logger.info("FIN::PautaGuardarSaldosReporteAntiguedad");
		return salida;
	}
}
