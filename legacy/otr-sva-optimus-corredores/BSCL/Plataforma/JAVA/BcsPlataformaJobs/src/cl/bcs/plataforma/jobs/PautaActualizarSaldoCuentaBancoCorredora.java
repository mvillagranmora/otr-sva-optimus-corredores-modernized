package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.tesoreria.ActualizarSaldosCuentaBancoCorredora;
import cl.bcs.tesoreria.ActualizarSaldosCuentaBancoCorredora.EntradaActualizarSaldosCuentaBancoCorredora;
import cl.bcs.tesoreria.ActualizarSaldosCuentaBancoCorredora.SalidaActualizarSaldosCuentaBancoCorredora;

public class PautaActualizarSaldoCuentaBancoCorredora implements BcsProcesoPauta {
	
	static final Logger logger = LogManager.getLogger(PautaActualizarSaldoCuentaBancoCorredora.class);
	
	@Override
    public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
		logger.debug("***** Inicio PautaActualizarSaldoCuentaBancoCorredora *****");
		SalidaProcesoPauta salida = new SalidaProcesoPauta();
		try {
			
			Long idEmpresa =  (Long) parameters.get("IdEmpresa");
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			
			EntradaActualizarSaldosCuentaBancoCorredora entradaActualizarSaldosCuentaBancoCorredora = new EntradaActualizarSaldosCuentaBancoCorredora();
			entradaActualizarSaldosCuentaBancoCorredora.p_IdEmpresa = idEmpresa;
			entradaActualizarSaldosCuentaBancoCorredora.p_IdUsuario = (Long) parameters.get("IdUsuario");
			entradaActualizarSaldosCuentaBancoCorredora.p_IdModulo = "PLA";
			entradaActualizarSaldosCuentaBancoCorredora.p_UsaReglasNegocio = "S";
			entradaActualizarSaldosCuentaBancoCorredora.p_FechaMovimiento = fechaSistema;
			
			ActualizarSaldosCuentaBancoCorredora actualizarSaldosCuentaBancoCorredora = new ActualizarSaldosCuentaBancoCorredora();
			SalidaActualizarSaldosCuentaBancoCorredora salidaActualizarSaldosCuentaBancoCorredora = actualizarSaldosCuentaBancoCorredora.logica(con, entradaActualizarSaldosCuentaBancoCorredora);
			
			salida.P_OUT_MENSAJE = "";
			salidaActualizarSaldosCuentaBancoCorredora.P_OUT_MENSAJES.stream().forEach(mensaje->{
				salida.P_OUT_MENSAJE += mensaje.descripcion + "\n";
			});
		} catch (Exception e) {
    		logger.error(e);
    		logger.error("***** Error PautaActualizarSaldoCuentaBancoCorredora *****");
    		throw e;
    	}
		logger.debug("***** Fin PautaActualizarSaldoCuentaBancoCorredora *****");
		return salida;
	}

}
