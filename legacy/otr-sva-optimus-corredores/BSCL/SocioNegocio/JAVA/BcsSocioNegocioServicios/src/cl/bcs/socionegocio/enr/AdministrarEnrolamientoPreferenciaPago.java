package cl.bcs.socionegocio.enr;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.socionegocio.enr.CrearClienteFull.EntradaCrearClienteFull;
import cl.bcs.socionegocio.enr.pojo.DatosClienteDatosGenerales;
import cl.bcs.socionegocio.enr.pojo.PreferenciaPagoPortafolio;
import cl.bcs.socionegocio.por.CrearPreferenciaPagoPortafolioMasivo;
import cl.bcs.socionegocio.por.CrearPreferenciaPagoPortafolioMasivo.EntradaCrearPreferenciaPagoPortafolioMasivo;
import cl.bcs.socionegocio.por.CrearPreferenciaPagoPortafolioMasivo.SalidaCrearPreferenciaPagoPortafolioMasivo;

public class AdministrarEnrolamientoPreferenciaPago {
	
	static final Logger logger = LogManager.getLogger(AdministrarEnrolamientoPreferenciaPago.class);
	
	public List<DatosClienteDatosGenerales> crearPreferenciaPago(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente)  throws Exception {
		logger.debug("INICIO::AdministrarEnrolamientoPreferenciaPago.crearPreferenciaPago");
		long inicio = System.currentTimeMillis();
		
		EntradaCrearPreferenciaPagoPortafolioMasivo entradaCrearPreferenciaPagoPortafolioMasivo = new EntradaCrearPreferenciaPagoPortafolioMasivo();
		entradaCrearPreferenciaPagoPortafolioMasivo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearPreferenciaPagoPortafolioMasivo.p_IdModulo = entrada.p_IdModulo;
		entradaCrearPreferenciaPagoPortafolioMasivo.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearPreferenciaPagoPortafolioMasivo.p_UsaReglasNegocio = "N";
		
		for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
		    	Map<Long, Long> idsCuentasBancariasPorIdsEnrolamiento = borradorCliente.CuentasBancaria
				.parallelStream()
				.collect(Collectors.toMap(cuentaBancaria->cuentaBancaria.IdCuentaBancaria, cuentaBancaria-> cuentaBancaria.IdCuentaBancariaPersona));	
			for(PreferenciaPagoPortafolio preferenciaPago : borradorCliente.PreferenciasPago) {
				cl.bcs.socionegocio.por.pojo.PreferenciaPagoPortafolio entradaPreferenciaPago = new cl.bcs.socionegocio.por.pojo.PreferenciaPagoPortafolio();
				entradaPreferenciaPago.IdInterno = preferenciaPago.IdPreferenciaPagoPortafolio;
				entradaPreferenciaPago.IdPortafolio = borradorCliente.Portafolio.get(0).IdPortafolio;
				entradaPreferenciaPago.IdPreferenciaPago = preferenciaPago.IdPreferenciaPago;
				entradaPreferenciaPago.IdPersonaRelacionada = preferenciaPago.IdPersonaRelacionada;
				entradaPreferenciaPago.IdCuentaBancaria = idsCuentasBancariasPorIdsEnrolamiento.get(preferenciaPago.IdCuentaBancaria)!= null? idsCuentasBancariasPorIdsEnrolamiento.get(preferenciaPago.IdCuentaBancaria):0L;
				entradaPreferenciaPago.IdUsuarioCreador = entradaCrearPreferenciaPagoPortafolioMasivo.p_IdUsuario;
				entradaPreferenciaPago.IdEstado = "ACT";
				entradaPreferenciaPago.IdEmpresa = entradaCrearPreferenciaPagoPortafolioMasivo.p_IdEmpresa;
				entradaPreferenciaPago.IdTarjetaBancaria = preferenciaPago.IdTarjetaBancaria;
				entradaPreferenciaPago.PorDefecto = preferenciaPago.PorDefecto;
				
				entradaCrearPreferenciaPagoPortafolioMasivo.p_PreferenciasPagoPortafolio.add(entradaPreferenciaPago);
			}
		}
		
		if(entradaCrearPreferenciaPagoPortafolioMasivo.p_PreferenciasPagoPortafolio.size() > 0) {
			CrearPreferenciaPagoPortafolioMasivo crearPreferenciaPagoPortafolioMasivo = new CrearPreferenciaPagoPortafolioMasivo();
			SalidaCrearPreferenciaPagoPortafolioMasivo salidaCrearCuentaCustodioMasivo = crearPreferenciaPagoPortafolioMasivo.logica(con, entradaCrearPreferenciaPagoPortafolioMasivo);
			
			for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
				borradorCliente.MensajesSalida.addAll(salidaCrearCuentaCustodioMasivo.P_OUT_MENSAJES);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoPreferenciaPago.crearPreferenciaPago", fin - inicio);
		return borradoresCliente;
	}
	
}
