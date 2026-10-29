package cl.bcs.socionegocio.enr;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.socionegocio.enr.CrearClienteFull.EntradaCrearClienteFull;
import cl.bcs.socionegocio.enr.pojo.DatosClienteDatosGenerales;
import cl.bcs.socionegocio.enr.pojo.DireccionElectronicaEnrolamiento;
import cl.bcs.socionegocio.per.CrearDireccionElectronicaMasivo;
import cl.bcs.socionegocio.per.CrearDireccionElectronicaMasivo.EntradaCrearDireccionElectronicaMasivo;
import cl.bcs.socionegocio.per.CrearDireccionElectronicaMasivo.SalidaCrearDireccionElectronicaMasivo;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntradaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.SalidaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EliminarPersonaDireccionElectronicaMasivo;
import cl.bcs.socionegocio.per.ModificarPersonaDireccionElectronicaMasivo;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.utils.SQL;

public class AdministrarEnrolamientoDireccionElectronica {
	
	static final Logger logger = LogManager.getLogger(AdministrarEnrolamientoDireccionElectronica.class);
	
	private static FileLoader loader;
	private static String SQL_SELECT_DIRECCION_ELECTRONICA;
	
	static {
		loader = new FileLoader();
		SQL_SELECT_DIRECCION_ELECTRONICA = loader.loadResourceToString("cl/bcs/socionegocio/enr/sql/AdministrarEnrolamientoDireccionElectronica_SelectDireccionElectronica.sql");
	}
	
	public List<DatosClienteDatosGenerales> crearDireccionesElectronica(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception {
		logger.debug("INICIO::AdministrarEnrolamientoDireccionElectronica.crearDireccionesElectronica");
		long inicio = System.currentTimeMillis();
		
		EntradaCrearDireccionElectronicaMasivo entradaCrearDireccionElectronicaMasivo = new EntradaCrearDireccionElectronicaMasivo();
		entradaCrearDireccionElectronicaMasivo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearDireccionElectronicaMasivo.p_IdModulo = entrada.p_IdModulo;
		entradaCrearDireccionElectronicaMasivo.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearDireccionElectronicaMasivo.p_UsaReglasNegocio = "N";
		
		for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
			List<DireccionElectronicaEnrolamiento> direccionesElectronica = borradorCliente.DireccionesElectronica
					.parallelStream()
					.filter(direccionElectronica->esDireccionElectronicaValidaParaCreacion(direccionElectronica, borradorCliente.Enrolamiento.IdDireccionElectronicaPrincipal))
					.collect(Collectors.toList());
			for(DireccionElectronicaEnrolamiento direccionElectronica : direccionesElectronica) {
				CrearDireccionElectronicaMasivo.DireccionElectronica entradaDireccionElectronica = new CrearDireccionElectronicaMasivo.DireccionElectronica();
				entradaDireccionElectronica.IdInterno = direccionElectronica.IdDireccionElectronica;
				entradaDireccionElectronica.Direccion = direccionElectronica.Direccion;
				entradaDireccionElectronica.CodigoProposito = direccionElectronica.CodigoProposito;
				entradaDireccionElectronica.Comentario = direccionElectronica.Comentario;
				entradaDireccionElectronica.IdEstado = "ACT";
				entradaDireccionElectronica.IdPersona = borradorCliente.IdPersona;
				entradaDireccionElectronica.Predeterminado = direccionElectronica.Predeterminado;
				entradaDireccionElectronica.IdUsuarioCreador = entrada.p_IdUsuario;
				
				entradaCrearDireccionElectronicaMasivo.p_DireccionesElectronica.add(entradaDireccionElectronica);
			}
		}
		
		if(entradaCrearDireccionElectronicaMasivo.p_DireccionesElectronica.size() > 0) {
			CrearDireccionElectronicaMasivo crearDireccionElectronicaMasivo = new CrearDireccionElectronicaMasivo();
			SalidaCrearDireccionElectronicaMasivo salidaCrearDireccionElectronicaMasivo = crearDireccionElectronicaMasivo.logica(con, entradaCrearDireccionElectronicaMasivo);
			
			Map<Long, Long> idsDireccionesCreadasPorIdInterno = salidaCrearDireccionElectronicaMasivo
					.P_OUT_REFID
					.parallelStream()
					.collect(Collectors.toMap(direccion->direccion.IdInterno, direccion->direccion.IdEntidad));
			
			for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
				borradorCliente.MensajesSalida.addAll(salidaCrearDireccionElectronicaMasivo.P_OUT_MENSAJES);
				List<DireccionElectronicaEnrolamiento> direccionesElectronica = borradorCliente.DireccionesElectronica
						.parallelStream()
						.filter(direccionElectronica->esDireccionElectronicaValidaParaCreacion(direccionElectronica, borradorCliente.Enrolamiento.IdDireccionElectronicaPrincipal))
						.collect(Collectors.toList());
				
				for(DireccionElectronicaEnrolamiento direccionElectronica : direccionesElectronica) {
					direccionElectronica.IdDireccionElectronicaReal = idsDireccionesCreadasPorIdInterno.get(direccionElectronica.IdDireccionElectronica);
				}
			}
		}
		
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoDireccionElectronica.crearDireccionesElectronica", fin - inicio);
		return borradoresCliente;
	}
	
	public List<DatosClienteDatosGenerales> modificarDireccionesElectronica(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception{
		logger.debug("INICIO::AdministrarEnrolamientoDireccionElectronica.modificarDireccionesElectronica");
		long inicio = System.currentTimeMillis();
		
		EntradaEjecutarCambiosPersona entradaEjecutarCambiosPersona = new EntradaEjecutarCambiosPersona();
		entradaEjecutarCambiosPersona.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaEjecutarCambiosPersona.p_IdModulo = entrada.p_IdModulo;
		entradaEjecutarCambiosPersona.p_IdUsuario = entrada.p_IdUsuario;
		entradaEjecutarCambiosPersona.p_UsaReglasNegocio = "N";
		
		for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
			
			List<DireccionElectronicaEnrolamiento> direccionesElectronica = borradorCliente.DireccionesElectronica
					.parallelStream()
					.filter(direccionElectronica->esDireccionElectronicaValidaParaModificacion(direccionElectronica))
					.collect(Collectors.toList());
			
			for(DireccionElectronicaEnrolamiento direccionElectronica : direccionesElectronica) {
				List<RegistroCambioMasivo> registrosCambios = obtenerRegistrosCambioModificarDireccionElectronica(direccionElectronica);
				registrosCambios = registrosCambios
						.parallelStream()
						.filter(registroCambio->registroCambio.ValorNuevo != null)
						.collect(Collectors.toList());
				
				entradaEjecutarCambiosPersona.p_RegistroCambio.addAll(registrosCambios);
			}
		}
		
		if(entradaEjecutarCambiosPersona.p_RegistroCambio.size() > 0) {
			ModificarPersonaDireccionElectronicaMasivo modificarPersonaDireccionElectronicaMasivo = new ModificarPersonaDireccionElectronicaMasivo();
			SalidaEjecutarCambiosPersona salidaEjecutarCambiosPersona = modificarPersonaDireccionElectronicaMasivo.logica(con, entradaEjecutarCambiosPersona);
			
			for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
				borradorCliente.MensajesSalida.addAll(salidaEjecutarCambiosPersona.P_OUT_MENSAJES);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoDireccionElectronica.modificarDireccionesElectronica", fin - inicio);
		
		return borradoresCliente;
	}
	
	public List<DatosClienteDatosGenerales> eliminarDireccionesElectronica(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception{
		logger.debug("INICIO::AdministrarEnrolamientoDireccionElectronica.eliminarDireccionesElectronica");
		long inicio = System.currentTimeMillis();
		
		List<DireccionElectronicaEnrolamiento> direccionesElectronicaExistentes = obtenerDireccionesElectronicas(con, borradoresCliente);
		
		EntradaEjecutarCambiosPersona entradaEjecutarCambiosPersona = new EntradaEjecutarCambiosPersona();
		entradaEjecutarCambiosPersona.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaEjecutarCambiosPersona.p_IdModulo = entrada.p_IdModulo;
		entradaEjecutarCambiosPersona.p_IdUsuario = entrada.p_IdUsuario;
		entradaEjecutarCambiosPersona.p_UsaReglasNegocio = "N";
		entradaEjecutarCambiosPersona.p_EsCrearCliente = "S";
		
		for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
			
			List<DireccionElectronicaEnrolamiento> direccionesElectronica = borradorCliente.DireccionesElectronica
					.parallelStream()
					.filter(direccionElectronica->esDireccionElectronicaValidaParaModificacion(direccionElectronica))
					.collect(Collectors.toList());
			
			for(DireccionElectronicaEnrolamiento direccionElectronicaExistente : direccionesElectronicaExistentes) {
				DireccionElectronicaEnrolamiento direccionElectronicaEncontrado = direccionesElectronica
						.parallelStream()
						.filter(direccionElectronica->direccionElectronicaExistente.IdDireccionElectronica.equals(direccionElectronica.IdDireccionElectronicaPersona))
						.findFirst()
						.orElse(null);
				if(direccionElectronicaEncontrado == null) {
					RegistroCambioMasivo registroCambio = obtenerRegistrosCambioEliminarDireccionElectronica(direccionElectronicaExistente);
					entradaEjecutarCambiosPersona.p_RegistroCambio.add(registroCambio);
				}
				
			}
		}
		
		if(entradaEjecutarCambiosPersona.p_RegistroCambio.size() > 0) {
			EliminarPersonaDireccionElectronicaMasivo eliminarPersonaDireccionElectronicaMasivo = new EliminarPersonaDireccionElectronicaMasivo();
			SalidaEjecutarCambiosPersona salidaEjecutarCambiosPersona = eliminarPersonaDireccionElectronicaMasivo.logica(con, entradaEjecutarCambiosPersona);
			
			for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
				borradorCliente.MensajesSalida.addAll(salidaEjecutarCambiosPersona.P_OUT_MENSAJES);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoDireccionElectronica.eliminarDireccionesElectronica", fin - inicio);
		
		return borradoresCliente;
	}
		
	private List<RegistroCambioMasivo> obtenerRegistrosCambioModificarDireccionElectronica(DireccionElectronicaEnrolamiento direccionElectronica){
		logger.trace("INICIO::AdministrarEnrolamientoDireccionElectronica.obtenerRegistrosCambioModificarDireccionElectronica");
		long inicio = System.currentTimeMillis();
		
		List<RegistroCambioMasivo> registrosCambio = new ArrayList<>();
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(direccionElectronica.IdDireccionElectronicaPersona, "Direccion", direccionElectronica.Direccion));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(direccionElectronica.IdDireccionElectronicaPersona, "CodigoProposito", direccionElectronica.CodigoProposito));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(direccionElectronica.IdDireccionElectronicaPersona, "Comentario", direccionElectronica.Comentario));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(direccionElectronica.IdDireccionElectronicaPersona, "Predeterminado", direccionElectronica.Predeterminado));
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AdministrarEnrolamientoDireccionElectronica.obtenerRegistrosCambioModificarDireccionElectronica", fin - inicio);
		return registrosCambio;
	}
	
	private RegistroCambioMasivo obtenerRegistrosCambioEliminarDireccionElectronica(DireccionElectronicaEnrolamiento direccionElectronica){
		logger.trace("INICIO::AdministrarEnrolamientoDireccionElectronica.obtenerRegistrosCambioEliminarDireccionElectronica");
		long inicio = System.currentTimeMillis();

		RegistroCambioMasivo registroCambio = EnrolamientoUtils.crearRegistroCambio(direccionElectronica.IdDireccionElectronica, "Borrado", "S");
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AdministrarEnrolamientoDireccionElectronica.obtenerRegistrosCambioEliminarDireccionElectronica", fin - inicio);
		return registroCambio;
	}
	
	private Boolean esDireccionElectronicaValidaParaCreacion(DireccionElectronicaEnrolamiento direccionElectronica, Long idDireccionElectronicaPrincipal) {
		logger.trace("INICIO::AdministrarEnrolamientoDireccionElectronica.esDireccionElectronicaValidaParaCreacion");
		long inicio = System.currentTimeMillis();
		Boolean esDireccionElectronicaValidaParaCreacion = !direccionElectronica.IdDireccionElectronica.equals(idDireccionElectronicaPrincipal) && direccionElectronica.IdDireccionElectronicaPersona.equals(0L);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AdministrarEnrolamientoDireccionElectronica.esDireccionElectronicaValidaParaCreacion", fin - inicio);
		return esDireccionElectronicaValidaParaCreacion;
	}
	
	private Boolean esDireccionElectronicaValidaParaModificacion(DireccionElectronicaEnrolamiento direccionElectronica) {
		logger.trace("INICIO::AdministrarEnrolamientoDireccionElectronica.esDireccionElectronicaValidaParaModificacion");
		long inicio = System.currentTimeMillis();
		Boolean esDireccionElectronicaValidaParaModificacion = !direccionElectronica.IdDireccionElectronicaPersona.equals(0L);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AdministrarEnrolamientoDireccionElectronica.esDireccionElectronicaValidaParaModificacion", fin - inicio);
		return esDireccionElectronicaValidaParaModificacion;
	}
	
	private List<DireccionElectronicaEnrolamiento> obtenerDireccionesElectronicas(Connection con, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception {
		logger.debug("INICIO::AdministrarEnrolamientoDireccionElectronica.obtenerDireccionesElectronicas");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsPersona = EnrolamientoUtils.obtenerIdsPersona(borradoresCliente);
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("IdPersona", idsPersona);
		
		SQL sql = new SQL();
		List<DireccionElectronicaEnrolamiento> direccionesElectronicas = sql.query(con, SQL_SELECT_DIRECCION_ELECTRONICA, parametros, DireccionElectronicaEnrolamiento.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoDireccionElectronica.obtenerDireccionesElectronicas", fin - inicio);
		return direccionesElectronicas;
	}
	
	public List<DatosClienteDatosGenerales> enrolarDireccionElectronica(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception{
		logger.debug("INICIO::AdministrarEnrolamientoDireccionElectronica.enrolarDireccionElectronica");
		long inicio = System.currentTimeMillis();
		
		borradoresCliente = eliminarDireccionesElectronica(con, entrada, borradoresCliente);
		borradoresCliente = crearDireccionesElectronica(con, entrada, borradoresCliente);
		borradoresCliente = modificarDireccionesElectronica(con, entrada, borradoresCliente);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoDireccionElectronica.enrolarDireccionElectronica", fin - inicio);
		return borradoresCliente;
	}
}
