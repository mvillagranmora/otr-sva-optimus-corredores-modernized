package cl.bcs.socionegocio.enr;

import java.sql.Connection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.socionegocio.enr.CrearClienteFull.EntradaCrearClienteFull;
import cl.bcs.socionegocio.enr.pojo.DatosClienteDatosGenerales;
import cl.bcs.socionegocio.enr.pojo.CuentaCustodioPortafolio;
import cl.bcs.socionegocio.por.CrearCuentaCustodioMasivo;
import cl.bcs.socionegocio.por.CrearCuentaCustodioMasivo.EntradaCrearCuentaCustodioMasivo;
import cl.bcs.socionegocio.por.CrearCuentaCustodioMasivo.SalidaCrearCuentaCustodioMasivo;
import cl.bcs.socionegocio.por.pojo.CuentaCustodio;

public class AdministrarEnrolamientoCuentaCustodio {
	
	static final Logger logger = LogManager.getLogger(AdministrarEnrolamientoCuentaCustodio.class);
	
	public List<DatosClienteDatosGenerales> crearCuentaCustodio(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente)  throws Exception {
		logger.debug("INICIO::AdministrarEnrolamientoCuentaCustodio.crearCuentaCustodio");
		long inicio = System.currentTimeMillis();
		
		EntradaCrearCuentaCustodioMasivo entradaCrearCuentaCustodioMasivo = new EntradaCrearCuentaCustodioMasivo();
		entradaCrearCuentaCustodioMasivo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearCuentaCustodioMasivo.p_IdModulo = entrada.p_IdModulo;
		entradaCrearCuentaCustodioMasivo.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearCuentaCustodioMasivo.p_UsaReglasNegocio = "N";
		
		for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
			for(CuentaCustodioPortafolio cuentaCustodio : borradorCliente.CuentasCustodio) {
				CuentaCustodio entradaCuentaCustodio = new CuentaCustodio();
				entradaCuentaCustodio.IdInterno = cuentaCustodio.IdCuentaCustodioPortafolio;
				entradaCuentaCustodio.IdPortafolio = borradorCliente.Portafolio.get(0).IdPortafolio;
				entradaCuentaCustodio.IdBoveda = cuentaCustodio.IdBoveda;
				entradaCuentaCustodio.Codigo = cuentaCustodio.Codigo;
				entradaCuentaCustodio.IdUsuarioCreador = entrada.p_IdUsuario;
				entradaCuentaCustodio.IdEstado = "ACT";
				entradaCuentaCustodio.IdTipoCuentaCustodia = cuentaCustodio.IdTipoCuentaCustodia;
				entradaCuentaCustodio.IdTipoCustodia = cuentaCustodio.IdTipoCustodia;
				entradaCuentaCustodio.IdModeloCustodia = cuentaCustodio.IdModeloCustodia;
				entradaCuentaCustodio.IdPersonaBancoCustodio = cuentaCustodio.IdPersonaBancoCustodio;
				entradaCuentaCustodio.IdPersonaBancoLiquidador = cuentaCustodio.IdPersonaBancoLiquidador;
				entradaCuentaCustodio.LiquidaDividendos = cuentaCustodio.LiquidaDividendos;
				entradaCuentaCustodio.LiquidaCupones = cuentaCustodio.LiquidaCupones;
				entradaCuentaCustodio.LiquidaVencimientos = cuentaCustodio.LiquidaVencimientos;
				entradaCuentaCustodio.LiquidaPrepagos = cuentaCustodio.LiquidaPrepagos;
				
				entradaCrearCuentaCustodioMasivo.p_CuentasCustodio.add(entradaCuentaCustodio);
			}
		}
		
		if(entradaCrearCuentaCustodioMasivo.p_CuentasCustodio.size() > 0) {
			CrearCuentaCustodioMasivo crearCuentaCustodioMasivo = new CrearCuentaCustodioMasivo();
			SalidaCrearCuentaCustodioMasivo salidaCrearCuentaCustodioMasivo = crearCuentaCustodioMasivo.logica(con, entradaCrearCuentaCustodioMasivo);
			
			for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
				borradorCliente.MensajesSalida.addAll(salidaCrearCuentaCustodioMasivo.P_OUT_MENSAJES);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoCuentaCustodio.crearCuentaCustodio", fin - inicio);
		return borradoresCliente;
	}

}
