package cl.bcs.socionegocio.enr;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.socionegocio.enr.CrearClienteFull.EntradaCrearClienteFull;
import cl.bcs.socionegocio.enr.pojo.DatosClienteDatosGenerales;
import cl.bcs.socionegocio.enr.pojo.CuentaBancaria;
import cl.bcs.socionegocio.per.CrearPersonaCuentaBancaria;
import cl.bcs.socionegocio.per.CrearPersonaCuentaBancaria.EntradaCrearPersonaCuentaBancaria;
import cl.bcs.socionegocio.per.CrearPersonaCuentaBancaria.InCuentaBancaria;
import cl.bcs.socionegocio.per.CrearPersonaCuentaBancaria.OutCuentaBancaria;
import cl.bcs.socionegocio.per.CrearPersonaCuentaBancaria.SalidaCrearPersonaCuentaBancaria;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntradaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.SalidaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EliminarPersonaCuentaBancariaMasivo;
import cl.bcs.socionegocio.per.ModificarPersonaCuentaBancariaMasivo;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.utils.SQL;

public class AdministrarEnrolamientoCuentaBancaria {
	
    static final Logger logger = LogManager.getLogger(AdministrarEnrolamientoCuentaBancaria.class);
	
	private static FileLoader loader;
	private static String SQL_SELECT_CUENTA_BANCARIA;
	private static String SQL_SELECT_CUENTABANCARIA_PREDETERMINADO;
	private static String SQL_UPDATE_CUENTABANCARIA_PRINCIPAL_PREDETERMINADO;
	static {
		loader = new FileLoader();
		SQL_SELECT_CUENTA_BANCARIA = loader.loadResourceToString("cl/bcs/socionegocio/enr/sql/AdministrarEnrolamientoCuentaBancaria_SelectCuentaBancaria.sql");
		SQL_SELECT_CUENTABANCARIA_PREDETERMINADO = loader.loadResourceToString("cl/bcs/socionegocio/enr/sql/AdministrarEnrolamientoCuentaBancaria_SelectCuentaBancariaPredeterminado.sql");
		SQL_UPDATE_CUENTABANCARIA_PRINCIPAL_PREDETERMINADO = loader.loadResourceToString("cl/bcs/socionegocio/enr/sql/AdministrarEnrolamientoCuentaBancaria_UpdateCuentaBancariaPrincipal.sql");

	}
	public List<DatosClienteDatosGenerales> crearCuentasBancaria(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception {
		logger.debug("INICIO::AdministrarEnrolamientoCuentaBancaria.crearCuentasBancaria");
		long inicio = System.currentTimeMillis();
		
		EntradaCrearPersonaCuentaBancaria entradaCrearCuentaBancaria = new EntradaCrearPersonaCuentaBancaria();
		entradaCrearCuentaBancaria.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearCuentaBancaria.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearCuentaBancaria.p_IdModulo = entrada.p_IdModulo;
		entradaCrearCuentaBancaria.p_UsaReglasNegocio = "N";
		
		for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
			List<CuentaBancaria> cuentasBancaria = borradorCliente.CuentasBancaria
					.parallelStream()
					.filter(cuentaBancaria->cuentaBancaria.IdCuentaBancariaPersona.equals(0L))
					.collect(Collectors.toList());
			for(CuentaBancaria cuentaBancaria : cuentasBancaria) {
				InCuentaBancaria inCuentaBancaria = new InCuentaBancaria();
				inCuentaBancaria.IdInterno = cuentaBancaria.IdCuentaBancaria;
				inCuentaBancaria.IdBanco = cuentaBancaria.IdBanco;
				inCuentaBancaria.IdTipoCuentaBancaria = cuentaBancaria.IdTipoCuentaBancaria;
				inCuentaBancaria.IdMoneda = cuentaBancaria.IdMoneda;
				inCuentaBancaria.NumeroCuentaBancaria = cuentaBancaria.NumeroCuentaBancaria;
				inCuentaBancaria.IdPersona = borradorCliente.IdPersona;
				inCuentaBancaria.IdEstado = "ACT";
				inCuentaBancaria.IdPlaza = cuentaBancaria.IdPlaza;
				inCuentaBancaria.CrearPreferenciaPago = cuentaBancaria.CrearPreferenciaPago;
				inCuentaBancaria.IdCliente = 0L;
				inCuentaBancaria.EsPac = cuentaBancaria.EsPac;
				inCuentaBancaria.Predeterminado = cuentaBancaria.Predeterminado;
				inCuentaBancaria.IdTipoIdentificadorCuenta = cuentaBancaria.IdTipoIdentificadorCuenta;
				inCuentaBancaria.IdentificadorCuenta = cuentaBancaria.IdentificadorCuenta;
				inCuentaBancaria.IdPersonaRelacionada = cuentaBancaria.IdPersonaRelacionada;
				inCuentaBancaria.CodigoCuentaInterbancario = cuentaBancaria.CodigoCuentaInterbancario;
				inCuentaBancaria.CodigoAba = cuentaBancaria.CodigoAba;
				inCuentaBancaria.OtroCodigoBancoExtranjero = cuentaBancaria.OtroCodigoBancoExtranjero;
				inCuentaBancaria.CodigoIban = cuentaBancaria.CodigoIban;
				inCuentaBancaria.OtroCodigoCuentaExtranjero = cuentaBancaria.OtroCodigoCuentaExtranjero;
				entradaCrearCuentaBancaria.p_InCuentaBancaria.add(inCuentaBancaria);
			}
		}
		
		if(entradaCrearCuentaBancaria.p_InCuentaBancaria.size() > 0) {
			CrearPersonaCuentaBancaria crearPersonaCuentaBancaria = new CrearPersonaCuentaBancaria();
			SalidaCrearPersonaCuentaBancaria salidaCrearPersonaCuentaBancaria = crearPersonaCuentaBancaria.logica(con, entradaCrearCuentaBancaria);
			
			for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
				borradorCliente.MensajesSalida.addAll(salidaCrearPersonaCuentaBancaria.P_OUT_MENSAJES);
				for(CuentaBancaria cuenta: borradorCliente.CuentasBancaria) {
				    cuenta.IdCuentaBancariaPersona = buscarIdCuentaBancaria(cuenta.IdCuentaBancaria ,salidaCrearPersonaCuentaBancaria.P_OUT_CUENTABANCARIA);
				}
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoCuentaBancaria.crearCuentasBancaria", fin - inicio);
		return borradoresCliente;
	}
	
	private Long buscarIdCuentaBancaria(Long idCuentaEnrolamiento, List<OutCuentaBancaria> cuentasCreadas) {
	    for (OutCuentaBancaria cuentaCreada : cuentasCreadas) {
		if (idCuentaEnrolamiento.equals(cuentaCreada.IdInterno)) {
		    return cuentaCreada.IdCuentaBancaria;
		}
	    }
	    return 0L;
	}
	public List<DatosClienteDatosGenerales> modificarCuentasBancaria(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception{
		logger.debug("INICIO::AdministrarEnrolamientoCuentaBancaria.modificarCuentasBancaria");
		long inicio = System.currentTimeMillis();
		
		EntradaEjecutarCambiosPersona entradaEjecutarCambiosPersona = new EntradaEjecutarCambiosPersona();
		entradaEjecutarCambiosPersona.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaEjecutarCambiosPersona.p_IdModulo = entrada.p_IdModulo;
		entradaEjecutarCambiosPersona.p_IdUsuario = entrada.p_IdUsuario;
		entradaEjecutarCambiosPersona.p_UsaReglasNegocio = "N";
		
		for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
			
			List<CuentaBancaria> cuentasBancarias = borradorCliente.CuentasBancaria
					.parallelStream()
					.filter(cuentaBancaria->esCuentaBancariaValidaParaModificacion(cuentaBancaria))
					.collect(Collectors.toList());
			
			for(CuentaBancaria cuentaBancaria : cuentasBancarias) {
				List<RegistroCambioMasivo> registrosCambios = obtenerRegistrosCambioModificarCuentaBancaria(cuentaBancaria);
				registrosCambios = registrosCambios
						.parallelStream()
						.filter(registroCambio->registroCambio.ValorNuevo != null)
						.collect(Collectors.toList());
				
				entradaEjecutarCambiosPersona.p_RegistroCambio.addAll(registrosCambios);
			}
		}
		
		if(entradaEjecutarCambiosPersona.p_RegistroCambio.size() > 0) {
			ModificarPersonaCuentaBancariaMasivo modificarPersonaCuentaBancariaMasivo = new ModificarPersonaCuentaBancariaMasivo();
			SalidaEjecutarCambiosPersona salidaEjecutarCambiosPersona = modificarPersonaCuentaBancariaMasivo.logica(con, entradaEjecutarCambiosPersona);
			
			for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
				borradorCliente.MensajesSalida.addAll(salidaEjecutarCambiosPersona.P_OUT_MENSAJES);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoCuentaBancaria.modificarCuentasBancaria", fin - inicio);
		
		return borradoresCliente;
	}
	
	public List<DatosClienteDatosGenerales> eliminarCuentasBancaria(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception{
		logger.debug("INICIO::AdministrarEnrolamientoCuentaBancaria.eliminarCuentasBancaria");
		long inicio = System.currentTimeMillis();
		
		List<CuentaBancaria> cuentasBancariaExistentes = obtenerCuentasBancaria(con, borradoresCliente);
		
		EntradaEjecutarCambiosPersona entradaEjecutarCambiosPersona = new EntradaEjecutarCambiosPersona();
		entradaEjecutarCambiosPersona.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaEjecutarCambiosPersona.p_IdModulo = entrada.p_IdModulo;
		entradaEjecutarCambiosPersona.p_IdUsuario = entrada.p_IdUsuario;
		entradaEjecutarCambiosPersona.p_UsaReglasNegocio = "N";
		entradaEjecutarCambiosPersona.p_EsCrearCliente = "S";
		
		for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
			
			List<CuentaBancaria> cuentasBancarias = borradorCliente.CuentasBancaria
					.parallelStream()
					.filter(cuentaBancaria->esCuentaBancariaValidaParaModificacion(cuentaBancaria))
					.collect(Collectors.toList());
			
			for(CuentaBancaria cuentaBancariaExistente : cuentasBancariaExistentes) {
				CuentaBancaria cuentaBancariaEncontrado = cuentasBancarias
						.parallelStream()
						.filter(cuentaBancaria->cuentaBancariaExistente.IdCuentaBancaria.equals(cuentaBancaria.IdCuentaBancariaPersona))
						.findFirst()
						.orElse(null);
				if(cuentaBancariaEncontrado == null) {
					RegistroCambioMasivo registroCambio = obtenerRegistrosCambioEliminarCuentaBancaria(cuentaBancariaExistente);
					entradaEjecutarCambiosPersona.p_RegistroCambio.add(registroCambio);
				}
				
			}
		}
		
		if(entradaEjecutarCambiosPersona.p_RegistroCambio.size() > 0) {
			EliminarPersonaCuentaBancariaMasivo eliminarPersonaCuentaBancariaMasivo = new EliminarPersonaCuentaBancariaMasivo();
			SalidaEjecutarCambiosPersona salidaEjecutarCambiosPersona = eliminarPersonaCuentaBancariaMasivo.logica(con, entradaEjecutarCambiosPersona);
			
			for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
				borradorCliente.MensajesSalida.addAll(salidaEjecutarCambiosPersona.P_OUT_MENSAJES);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoCuentaBancaria.eliminarCuentasBancaria", fin - inicio);
		
		return borradoresCliente;
	}
	
	private Boolean esCuentaBancariaValidaParaModificacion(CuentaBancaria cuentaBancaria) {
		logger.trace("INICIO::AdministrarEnrolamientoCuentaBancaria.esCuentaBancariaValidaParaModificacion");
		long inicio = System.currentTimeMillis();
		Boolean esCuentaBancariaValidaParaModificacion = !cuentaBancaria.IdCuentaBancariaPersona.equals(0L);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AdministrarEnrolamientoCuentaBancaria.esCuentaBancariaValidaParaModificacion", fin - inicio);
		return esCuentaBancariaValidaParaModificacion;
	}
	
	private List<RegistroCambioMasivo> obtenerRegistrosCambioModificarCuentaBancaria(CuentaBancaria cuentaBancaria){
		logger.trace("INICIO::AdministrarEnrolamientoCuentaBancaria.crearRegistroCambio");
		long inicio = System.currentTimeMillis();
		List<RegistroCambioMasivo> registrosCambio = new ArrayList<>();
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(cuentaBancaria.IdCuentaBancariaPersona, "EsPac", cuentaBancaria.EsPac));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(cuentaBancaria.IdCuentaBancariaPersona, "Predeterminado", cuentaBancaria.Predeterminado));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(cuentaBancaria.IdCuentaBancariaPersona, "IdBanco", cuentaBancaria.IdBanco));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(cuentaBancaria.IdCuentaBancariaPersona, "IdPlaza", cuentaBancaria.IdPlaza));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(cuentaBancaria.IdCuentaBancariaPersona, "IdMoneda", cuentaBancaria.IdMoneda));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(cuentaBancaria.IdCuentaBancariaPersona, "CodigoAba", cuentaBancaria.CodigoAba));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(cuentaBancaria.IdCuentaBancariaPersona, "CodigoIban", cuentaBancaria.CodigoIban));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(cuentaBancaria.IdCuentaBancariaPersona, "IdentificadorCuenta", cuentaBancaria.IdentificadorCuenta));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(cuentaBancaria.IdCuentaBancariaPersona, "IdPersonaRelacionada", cuentaBancaria.IdPersonaRelacionada));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(cuentaBancaria.IdCuentaBancariaPersona, "IdTipoCuentaBancaria", cuentaBancaria.IdTipoCuentaBancaria));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(cuentaBancaria.IdCuentaBancariaPersona, "NumeroCuentaBancaria", cuentaBancaria.NumeroCuentaBancaria));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(cuentaBancaria.IdCuentaBancariaPersona, "CodigoCuentaInterbancario", cuentaBancaria.CodigoCuentaInterbancario));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(cuentaBancaria.IdCuentaBancariaPersona, "IdTipoIdentificadorCuenta", cuentaBancaria.IdTipoIdentificadorCuenta));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(cuentaBancaria.IdCuentaBancariaPersona, "OtroCodigoBancoExtranjero", cuentaBancaria.OtroCodigoBancoExtranjero));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(cuentaBancaria.IdCuentaBancariaPersona, "OtroCodigoCuentaExtranjero", cuentaBancaria.OtroCodigoCuentaExtranjero));
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AdministrarEnrolamientoCuentaBancaria.crearRegistroCambio", fin - inicio);
		return registrosCambio;
	}
	
	private RegistroCambioMasivo obtenerRegistrosCambioEliminarCuentaBancaria(CuentaBancaria cuentaBancaria){
		logger.trace("INICIO::AdministrarEnrolamientoTelefono.obtenerRegistrosCambioEliminarCuentaBancaria");
		long inicio = System.currentTimeMillis();

		RegistroCambioMasivo registroCambio = EnrolamientoUtils.crearRegistroCambio(cuentaBancaria.IdCuentaBancaria, "Borrado", "S");
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AdministrarEnrolamientoTelefono.obtenerRegistrosCambioEliminarCuentaBancaria", fin - inicio);
		return registroCambio;
	}
	
	private List<CuentaBancaria> obtenerCuentasBancaria(Connection con, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception {
		logger.debug("INICIO::AdministrarEnrolamientoCuentaBancaria.obtenerCuentasBancaria");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsPersona = EnrolamientoUtils.obtenerIdsPersona(borradoresCliente);
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("IdPersona", idsPersona);
		
		SQL sql = new SQL();
		List<CuentaBancaria> cuentasBancaria = sql.query(con, SQL_SELECT_CUENTA_BANCARIA, parametros, CuentaBancaria.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoCuentaBancaria.obtenerCuentasBancaria", fin - inicio);
		return cuentasBancaria;
	}
	
	public List<DatosClienteDatosGenerales> enrolarCuentaBancaria(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception{
		logger.debug("INICIO::AdministrarEnrolamientoCuentaBancaria.enrolarCuentaBancaria");
		long inicio = System.currentTimeMillis();
		
		borradoresCliente = eliminarCuentasBancaria(con, entrada, borradoresCliente);
		borradoresCliente = crearCuentasBancaria(con, entrada, borradoresCliente);
		borradoresCliente = modificarCuentasBancaria(con, entrada, borradoresCliente);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoCuentaBancaria.enrolarCuentaBancaria", fin - inicio);
		return borradoresCliente;
	}

	public List<DatosClienteDatosGenerales> actualizarCuentaBancariaPrincipalPredeterminado(Connection con,EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception {
		logger.debug("INICIO::CrearPersonaCuentaBancaria.actualizarCuentaBancariaPrincipalPredeterminado");
		long inicio = System.currentTimeMillis();

		if(borradoresCliente.get(0).CuentasBancaria.size() >0 ) {
			HashMap<String, Object> parametros = new HashMap<String, Object>();

			SQL sql = new SQL();
			parametros.put("IdPersona", borradoresCliente.get(0).IdPersona);
			List<CuentaBancaria> cuentaBancariaPredeterminada = sql.query(con, SQL_SELECT_CUENTABANCARIA_PREDETERMINADO, parametros,CuentaBancaria.class);
			
			if (cuentaBancariaPredeterminada.size()>0) {
				SQL_UPDATE_CUENTABANCARIA_PRINCIPAL_PREDETERMINADO = SQL_UPDATE_CUENTABANCARIA_PRINCIPAL_PREDETERMINADO
																		.replace("(:IdPersona)", Long.toString(borradoresCliente.get(0).IdPersona))
																		.replace("(:IdCuentaBancariaPrincipal)",Long.toString(cuentaBancariaPredeterminada.get(0).IdCuentaBancaria));
				SQL.exec(con, SQL_UPDATE_CUENTABANCARIA_PRINCIPAL_PREDETERMINADO);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearPersonaCuentaBancaria.actualizarCuentaBancariaPrincipalPredeterminado", fin - inicio);
		return borradoresCliente;
	}
}
