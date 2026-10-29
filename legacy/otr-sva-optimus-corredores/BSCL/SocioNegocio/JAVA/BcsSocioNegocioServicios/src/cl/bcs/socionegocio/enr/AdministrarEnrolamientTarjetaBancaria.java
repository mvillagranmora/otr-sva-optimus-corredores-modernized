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
import cl.bcs.socionegocio.enr.pojo.TarjetaBancaria;
import cl.bcs.socionegocio.per.CrearPersonaTarjetaBancariaMasivo;
import cl.bcs.socionegocio.per.EliminarPersonaTarjetaBancariaMasivo;
import cl.bcs.socionegocio.per.ModificarPersonaTarjetaBancariaMasivo;
import cl.bcs.socionegocio.per.CrearPersonaTarjetaBancariaMasivo.EntradaCrearPersonaTarjetaBancariaMasivo;
import cl.bcs.socionegocio.per.CrearPersonaTarjetaBancariaMasivo.SalidaCrearPersonaTarjetaBancariaMasivo;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntradaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.SalidaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.utils.SQL;

public class AdministrarEnrolamientTarjetaBancaria {
	
	static final Logger logger = LogManager.getLogger(AdministrarEnrolamientTarjetaBancaria.class);
	
	private static FileLoader loader;
	private static String SQL_SELECT_TARJETA_BANCARIA;
	
	static {
		loader = new FileLoader();
		SQL_SELECT_TARJETA_BANCARIA = loader.loadResourceToString("cl/bcs/socionegocio/enr/sql/AdministrarEnrolamientTarjetaBancaria_SelectTarjetaBancaria.sql");
	}
	
	public List<DatosClienteDatosGenerales> crearTarjetasBancaria(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception {
		logger.debug("INICIO::AdministrarEnrolamientTarjetaBancaria.crearTarjetasBancaria");
		long inicio = System.currentTimeMillis();
		
		EntradaCrearPersonaTarjetaBancariaMasivo entradaCrearPersonaTarjetaBancariaMasivo = new EntradaCrearPersonaTarjetaBancariaMasivo();
		entradaCrearPersonaTarjetaBancariaMasivo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearPersonaTarjetaBancariaMasivo.p_IdModulo = entrada.p_IdModulo;
		entradaCrearPersonaTarjetaBancariaMasivo.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearPersonaTarjetaBancariaMasivo.p_UsaReglasNegocio = "N";
		
		for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
			List<TarjetaBancaria> tarjetasBancaria = borradorCliente.TarjetasBancaria
					.parallelStream()
					.filter(tarjetaBancaria->tarjetaBancaria.IdTarjetaBancariaPersona.equals(0L))
					.collect(Collectors.toList());
			for(TarjetaBancaria tarjetaBancaria : tarjetasBancaria) {
				CrearPersonaTarjetaBancariaMasivo.TarjetaBancaria entradaTarjetaBancaria = new CrearPersonaTarjetaBancariaMasivo.TarjetaBancaria();
				entradaTarjetaBancaria.IdInterno = tarjetaBancaria.IdTarjetaBancaria;
				entradaTarjetaBancaria.IdBanco = tarjetaBancaria.IdBanco;
				entradaTarjetaBancaria.IdTipoTarjetaBancaria = tarjetaBancaria.IdTipoTarjetaBancaria;
				entradaTarjetaBancaria.NumeroTarjetaBancaria = tarjetaBancaria.NumeroTarjetaBancaria;
				entradaTarjetaBancaria.IdPersona = borradorCliente.IdPersona;
				entradaTarjetaBancaria.FechaVencimiento = tarjetaBancaria.FechaVencimiento;
				entradaTarjetaBancaria.CodigoVerificacion = tarjetaBancaria.CodigoVerificacion;
				entradaTarjetaBancaria.IdEstado = "ACT";
				entradaTarjetaBancaria.IdUsuarioCreador = entrada.p_IdUsuario;
				entradaCrearPersonaTarjetaBancariaMasivo.p_TarjetasBancarias.add(entradaTarjetaBancaria);
			}
		}
		
		if(entradaCrearPersonaTarjetaBancariaMasivo.p_TarjetasBancarias.size() > 0) {
			CrearPersonaTarjetaBancariaMasivo crearPersonaTarjetaBancariaMasivo = new CrearPersonaTarjetaBancariaMasivo();
			SalidaCrearPersonaTarjetaBancariaMasivo salidaCrearPersonaTarjetaBancariaMasivo = crearPersonaTarjetaBancariaMasivo.logica(con, entradaCrearPersonaTarjetaBancariaMasivo);
			
			for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
				borradorCliente.MensajesSalida.addAll(salidaCrearPersonaTarjetaBancariaMasivo.P_OUT_MENSAJES);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientTarjetaBancaria.crearTarjetasBancaria", fin - inicio);
		return borradoresCliente;
	}
	
	public List<DatosClienteDatosGenerales> modificarTarjetasBancaria(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception{
		logger.debug("INICIO::AdministrarEnrolamientTarjetaBancaria.modificarTarjetasBancaria");
		long inicio = System.currentTimeMillis();
		
		EntradaEjecutarCambiosPersona entradaEjecutarCambiosPersona = new EntradaEjecutarCambiosPersona();
		entradaEjecutarCambiosPersona.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaEjecutarCambiosPersona.p_IdModulo = entrada.p_IdModulo;
		entradaEjecutarCambiosPersona.p_IdUsuario = entrada.p_IdUsuario;
		entradaEjecutarCambiosPersona.p_UsaReglasNegocio = "N";
		
		for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
			
			List<TarjetaBancaria> tarjetasBancarias = borradorCliente.TarjetasBancaria
					.parallelStream()
					.filter(tarjetaBancaria->esTarjetaBancariaValidaParaModificacion(tarjetaBancaria))
					.collect(Collectors.toList());
			
			for(TarjetaBancaria tarjetaBancaria : tarjetasBancarias) {
				List<RegistroCambioMasivo> registrosCambios = obtenerRegistrosCambioModificarTarjetaBancaria(tarjetaBancaria);
				registrosCambios = registrosCambios
						.parallelStream()
						.filter(registroCambio->registroCambio.ValorNuevo != null)
						.collect(Collectors.toList());
				
				entradaEjecutarCambiosPersona.p_RegistroCambio.addAll(registrosCambios);
			}
		}
		
		if(entradaEjecutarCambiosPersona.p_RegistroCambio.size() > 0) {
			ModificarPersonaTarjetaBancariaMasivo modificarPersonaTarjetaBancariaMasivo = new ModificarPersonaTarjetaBancariaMasivo();
			SalidaEjecutarCambiosPersona salidaEjecutarCambiosPersona = modificarPersonaTarjetaBancariaMasivo.logica(con, entradaEjecutarCambiosPersona);
			
			for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
				borradorCliente.MensajesSalida.addAll(salidaEjecutarCambiosPersona.P_OUT_MENSAJES);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientTarjetaBancaria.modificarTarjetasBancaria", fin - inicio);
		
		return borradoresCliente;
	}
	
	public List<DatosClienteDatosGenerales> eliminarTarjetasBancaria(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception{
		logger.debug("INICIO::AdministrarEnrolamientTarjetaBancaria.eliminarTarjetasBancaria");
		long inicio = System.currentTimeMillis();
		
		List<TarjetaBancaria> tarjetasBancariaExistentes = obtenerTarjetasBancaria(con, borradoresCliente);
		
		EntradaEjecutarCambiosPersona entradaEjecutarCambiosPersona = new EntradaEjecutarCambiosPersona();
		entradaEjecutarCambiosPersona.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaEjecutarCambiosPersona.p_IdModulo = entrada.p_IdModulo;
		entradaEjecutarCambiosPersona.p_IdUsuario = entrada.p_IdUsuario;
		entradaEjecutarCambiosPersona.p_UsaReglasNegocio = "N";
		
		for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
			
			List<TarjetaBancaria> tarjetasBancarias = borradorCliente.TarjetasBancaria
					.parallelStream()
					.filter(tarjetaBancaria->esTarjetaBancariaValidaParaModificacion(tarjetaBancaria))
					.collect(Collectors.toList());
			
			for(TarjetaBancaria tarjetaBancariaExistente : tarjetasBancariaExistentes) {
				TarjetaBancaria tarjetaBancariaEncontrado = tarjetasBancarias
						.parallelStream()
						.filter(tarjetaBancaria->tarjetaBancariaExistente.IdTarjetaBancaria.equals(tarjetaBancaria.IdTarjetaBancariaPersona))
						.findFirst()
						.orElse(null);
				if(tarjetaBancariaEncontrado == null) {
					RegistroCambioMasivo registroCambio = obtenerRegistrosCambioEliminarTarjetaBancaria(tarjetaBancariaExistente);
					entradaEjecutarCambiosPersona.p_RegistroCambio.add(registroCambio);
				}
				
			}
		}
		
		if(entradaEjecutarCambiosPersona.p_RegistroCambio.size() > 0) {
			EliminarPersonaTarjetaBancariaMasivo eliminarPersonaTarjetaBancariaMasivo = new EliminarPersonaTarjetaBancariaMasivo();
			SalidaEjecutarCambiosPersona salidaEjecutarCambiosPersona = eliminarPersonaTarjetaBancariaMasivo.logica(con, entradaEjecutarCambiosPersona);
			
			for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
				borradorCliente.MensajesSalida.addAll(salidaEjecutarCambiosPersona.P_OUT_MENSAJES);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientTarjetaBancaria.eliminarTarjetasBancaria", fin - inicio);
		
		return borradoresCliente;
	}
	
	private Boolean esTarjetaBancariaValidaParaModificacion(TarjetaBancaria tarjetaBancaria) {
		logger.trace("INICIO::AdministrarEnrolamientTarjetaBancaria.esTarjetaBancariaValidaParaModificacion");
		long inicio = System.currentTimeMillis();
		Boolean esTarjetaBancariaValidaParaModificacion = !tarjetaBancaria.IdTarjetaBancariaPersona.equals(0L);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientTarjetaBancaria.esTarjetaBancariaValidaParaModificacion", fin - inicio);
		return esTarjetaBancariaValidaParaModificacion;
	}
	
	private List<RegistroCambioMasivo> obtenerRegistrosCambioModificarTarjetaBancaria(TarjetaBancaria tarjetaBancaria){
		logger.trace("INICIO::AdministrarEnrolamientTarjetaBancaria.crearRegistroCambio");
		long inicio = System.currentTimeMillis();
		List<RegistroCambioMasivo> registrosCambio = new ArrayList<>();
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(tarjetaBancaria.IdTarjetaBancariaPersona, "IdBanco", tarjetaBancaria.IdBanco));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(tarjetaBancaria.IdTarjetaBancariaPersona, "IdTipoTarjetaBancaria", tarjetaBancaria.IdTipoTarjetaBancaria));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(tarjetaBancaria.IdTarjetaBancariaPersona, "NumeroTarjetaBancaria", tarjetaBancaria.NumeroTarjetaBancaria));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(tarjetaBancaria.IdTarjetaBancariaPersona, "FechaVencimiento", tarjetaBancaria.FechaVencimiento));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(tarjetaBancaria.IdTarjetaBancariaPersona, "CodigoVerificacion", tarjetaBancaria.CodigoVerificacion));
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AdministrarEnrolamientTarjetaBancaria.crearRegistroCambio", fin - inicio);
		return registrosCambio;
	}
	
	private RegistroCambioMasivo obtenerRegistrosCambioEliminarTarjetaBancaria(TarjetaBancaria tarjetaBancaria){
		logger.trace("INICIO::AdministrarEnrolamientTarjetaBancaria.obtenerRegistrosCambioEliminarTarjetaBancaria");
		long inicio = System.currentTimeMillis();

		RegistroCambioMasivo registroCambio = EnrolamientoUtils.crearRegistroCambio(tarjetaBancaria.IdTarjetaBancaria, "Borrado", "S");
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AdministrarEnrolamientTarjetaBancaria.obtenerRegistrosCambioEliminarTarjetaBancaria", fin - inicio);
		return registroCambio;
	}
	
	private List<TarjetaBancaria> obtenerTarjetasBancaria(Connection con, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception {
		logger.debug("INICIO::AdministrarEnrolamientTarjetaBancaria.obtenerTarjetasBancaria");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsPersona = EnrolamientoUtils.obtenerIdsPersona(borradoresCliente);
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("IdPersona", idsPersona);
		
		SQL sql = new SQL();
		List<TarjetaBancaria> tarjetasBancaria = sql.query(con, SQL_SELECT_TARJETA_BANCARIA, parametros, TarjetaBancaria.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientTarjetaBancaria.obtenerTarjetasBancaria", fin - inicio);
		return tarjetasBancaria;
	}
	
	public List<DatosClienteDatosGenerales> enrolarTarjetaBancaria(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception{
		logger.debug("INICIO::AdministrarEnrolamientTarjetaBancaria.enrolarTarjetaBancaria");
		long inicio = System.currentTimeMillis();
		
		borradoresCliente = eliminarTarjetasBancaria(con, entrada, borradoresCliente);
		borradoresCliente = crearTarjetasBancaria(con, entrada, borradoresCliente);
		borradoresCliente = modificarTarjetasBancaria(con, entrada, borradoresCliente);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientTarjetaBancaria.enrolarTarjetaBancaria", fin - inicio);
		return borradoresCliente;
	}

}
