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
import cl.bcs.socionegocio.enr.pojo.RelacionPersona;
import cl.bcs.socionegocio.per.ModificarRelacionPersonaMasivo;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntradaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.SalidaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.socionegocio.rel.CrearRelacionPersonaMasivo;
import cl.bcs.socionegocio.rel.CrearRelacionPersonaMasivo.EntradaCrearRelacionPersonaMasivo;
import cl.bcs.socionegocio.rel.CrearRelacionPersonaMasivo.SalidaCrearRelacionPersonaMasivo;
import cl.bcs.utils.SQL;

public class AdministrarEnrolamientoRelacionPersona {
	
	static final Logger logger = LogManager.getLogger(AdministrarEnrolamientoRelacionPersona.class);
	
	private static FileLoader loader;
	private static String SQL_SELECT_RELACION_PERSONA;
	
	static {
		loader = new FileLoader();
		SQL_SELECT_RELACION_PERSONA = loader.loadResourceToString("cl/bcs/socionegocio/enr/sql/AdministrarEnrolamientoRelacionPersona_SelectRelacionPersona.sql");
	}
	
	public List<DatosClienteDatosGenerales> crearRelacionesPersona(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception {
		logger.debug("INICIO::AdministrarEnrolamientoRelacionPersona.crearRelacionesPersona");
		long inicio = System.currentTimeMillis();
		
		EntradaCrearRelacionPersonaMasivo entradaCrearRelacionPersonaMasivo = new EntradaCrearRelacionPersonaMasivo();
		entradaCrearRelacionPersonaMasivo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearRelacionPersonaMasivo.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearRelacionPersonaMasivo.p_IdModulo = entrada.p_IdModulo;
		entradaCrearRelacionPersonaMasivo.p_UsaReglasNegocio = "N";
		
		for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
			List<RelacionPersona> relaciones = borradorCliente.Relaciones
					.parallelStream()
					.filter(relacion->relacion.IdRelacionPersonaPersona.equals(0L))
					.collect(Collectors.toList());
			for(RelacionPersona relacion : relaciones) {
				CrearRelacionPersonaMasivo.RelacionPersona entradaRelacionPersona = new CrearRelacionPersonaMasivo.RelacionPersona();
				entradaRelacionPersona.IdInterno = relacion.IdRelacionPersona;
				entradaRelacionPersona.IdTipoRelacion = relacion.IdTipoRelacion;
				entradaRelacionPersona.IdPersonaOrigen = borradorCliente.IdPersona;
				entradaRelacionPersona.IdPersonaDestino = relacion.IdPersonaDestino;
				entradaRelacionPersona.IdEstado = "ACT";
				entradaRelacionPersona.FechaInicioRelacion = relacion.FechaInicioRelacion;
				entradaRelacionPersona.FechaFinRelacion = relacion.FechaFinRelacion;
				entradaRelacionPersona.PorcentajeParticipacion = relacion.PorcentajeParticipacion;
				entradaRelacionPersona.RetiraDocumentos = relacion.RetiraDocumentos;
				entradaRelacionPersona.FinalidadRelacion = relacion.FinalidadRelacion;
				entradaRelacionPersona.CondicionesRelacion = relacion.CondicionesRelacion;
				entradaRelacionPersona.IdGradoVinculacionParental = relacion.IdGradoVinculacionParental;
				entradaRelacionPersona.Ciudad = relacion.Ciudad;
				entradaRelacionPersona.Calle = relacion.Calle;
				entradaRelacionPersona.Numero = relacion.Numero;
				entradaRelacionPersona.Telefono = relacion.Telefono;
				entradaRelacionPersona.Email = relacion.Email;
				entradaRelacionPersona.Complemento = relacion.Complemento;
				entradaRelacionPersona.IdUsuarioCreador = entrada.p_IdUsuario;
				entradaCrearRelacionPersonaMasivo.p_RelacionesPersona.add(entradaRelacionPersona);
			}
		}
		
		if(entradaCrearRelacionPersonaMasivo.p_RelacionesPersona.size() > 0) {
			CrearRelacionPersonaMasivo crearRelacionPersonaMasivo = new CrearRelacionPersonaMasivo();
			SalidaCrearRelacionPersonaMasivo salidaCrearRelacionPersonaMasivo = crearRelacionPersonaMasivo.logica(con, entradaCrearRelacionPersonaMasivo);
			
			for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
				borradorCliente.MensajesSalida.addAll(salidaCrearRelacionPersonaMasivo.P_OUT_MENSAJES);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoRelacionPersona.crearRelacionesPersona", fin - inicio);
		return borradoresCliente;
	}
	
	public List<DatosClienteDatosGenerales> modificarRelacionesPersona(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception{
		logger.debug("INICIO::AdministrarEnrolamientoRelacionPersona.modificarRelacionesPersona");
		long inicio = System.currentTimeMillis();
		
		EntradaEjecutarCambiosPersona entradaEjecutarCambiosPersona = new EntradaEjecutarCambiosPersona();
		entradaEjecutarCambiosPersona.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaEjecutarCambiosPersona.p_IdModulo = entrada.p_IdModulo;
		entradaEjecutarCambiosPersona.p_IdUsuario = entrada.p_IdUsuario;
		entradaEjecutarCambiosPersona.p_UsaReglasNegocio = "N";
		
		for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
			
			List<RelacionPersona> relacionesPersona = borradorCliente.Relaciones
					.parallelStream()
					.filter(relacionPersona->esRelacionPersonaValidaParaModificacion(relacionPersona))
					.collect(Collectors.toList());
			
			for(RelacionPersona relacion : relacionesPersona) {
				List<RegistroCambioMasivo> registrosCambios = obtenerRegistrosCambioModificarRelacionPersona(relacion);
				registrosCambios = registrosCambios
						.parallelStream()
						.filter(registroCambio->registroCambio.ValorNuevo != null)
						.collect(Collectors.toList());
				
				entradaEjecutarCambiosPersona.p_RegistroCambio.addAll(registrosCambios);
			}
		}
		
		if(entradaEjecutarCambiosPersona.p_RegistroCambio.size() > 0) {
			ModificarRelacionPersonaMasivo modificarRelacionPersonaMasivo = new ModificarRelacionPersonaMasivo();
			SalidaEjecutarCambiosPersona salidaEjecutarCambiosPersona = modificarRelacionPersonaMasivo.logica(con, entradaEjecutarCambiosPersona);
			
			for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
				borradorCliente.MensajesSalida.addAll(salidaEjecutarCambiosPersona.P_OUT_MENSAJES);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoRelacionPersona.modificarRelacionesPersona", fin - inicio);
		
		return borradoresCliente;
	}
	
	public List<DatosClienteDatosGenerales> eliminarRelacionesPersona(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception{
		logger.debug("INICIO::AdministrarEnrolamientoRelacionPersona.eliminarRelacionesPersona");
		long inicio = System.currentTimeMillis();
		
		List<RelacionPersona> relacionesPersonaExistentes = obtenerRelacionesPersona(con, borradoresCliente);
		
		EntradaEjecutarCambiosPersona entradaEjecutarCambiosPersona = new EntradaEjecutarCambiosPersona();
		entradaEjecutarCambiosPersona.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaEjecutarCambiosPersona.p_IdModulo = entrada.p_IdModulo;
		entradaEjecutarCambiosPersona.p_IdUsuario = entrada.p_IdUsuario;
		entradaEjecutarCambiosPersona.p_UsaReglasNegocio = "N";
		
		for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
			
			List<RelacionPersona> relacionesPersona = borradorCliente.Relaciones
					.parallelStream()
					.filter(relacionPersona->esRelacionPersonaValidaParaModificacion(relacionPersona))
					.collect(Collectors.toList());
			
			for(RelacionPersona relacionPersonaExistente : relacionesPersonaExistentes) {
				RelacionPersona relacionPersonaEncontrado = relacionesPersona
						.parallelStream()
						.filter(relacionPersona->relacionPersonaExistente.IdRelacionPersona.equals(relacionPersona.IdRelacionPersonaPersona))
						.findFirst()
						.orElse(null);
				if(relacionPersonaEncontrado == null) {
					RegistroCambioMasivo registroCambio = obtenerRegistrosCambioEliminarTarjetaBancaria(relacionPersonaExistente);
					entradaEjecutarCambiosPersona.p_RegistroCambio.add(registroCambio);
				}
				
			}
		}
		
		if(entradaEjecutarCambiosPersona.p_RegistroCambio.size() > 0) {
			ModificarRelacionPersonaMasivo modificarRelacionPersonaMasivo = new ModificarRelacionPersonaMasivo();
			SalidaEjecutarCambiosPersona salidaEjecutarCambiosPersona = modificarRelacionPersonaMasivo.logica(con, entradaEjecutarCambiosPersona);
			
			for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
				borradorCliente.MensajesSalida.addAll(salidaEjecutarCambiosPersona.P_OUT_MENSAJES);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoRelacionPersona.eliminarRelacionesPersona", fin - inicio);
		
		return borradoresCliente;
	}
	
	private List<RegistroCambioMasivo> obtenerRegistrosCambioModificarRelacionPersona(RelacionPersona relacionPersona){
		logger.trace("INICIO::AdministrarEnrolamientoRelacionPersona.obtenerRegistrosCambioModificarRelacionPersona");
		long inicio = System.currentTimeMillis();
		List<RegistroCambioMasivo> registrosCambio = new ArrayList<>();
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(relacionPersona.IdRelacionPersonaPersona, "FechaInicioRelacion", relacionPersona.FechaInicioRelacion));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(relacionPersona.IdRelacionPersonaPersona, "FechaFinRelacion", relacionPersona.FechaFinRelacion));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(relacionPersona.IdRelacionPersonaPersona, "IdCiudad", relacionPersona.IdCiudad));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(relacionPersona.IdRelacionPersonaPersona, "Ciudad", relacionPersona.Ciudad));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(relacionPersona.IdRelacionPersonaPersona, "PorcentajeParticipacion", relacionPersona.PorcentajeParticipacion));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(relacionPersona.IdRelacionPersonaPersona, "RetiraDocumentos", relacionPersona.RetiraDocumentos));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(relacionPersona.IdRelacionPersonaPersona, "FinalidadRelacion", relacionPersona.FinalidadRelacion));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(relacionPersona.IdRelacionPersonaPersona, "CondicionesRelacion", relacionPersona.CondicionesRelacion));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(relacionPersona.IdRelacionPersonaPersona, "Calle", relacionPersona.Calle));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(relacionPersona.IdRelacionPersonaPersona, "Numero", relacionPersona.Numero));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(relacionPersona.IdRelacionPersonaPersona, "Email", relacionPersona.Email));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(relacionPersona.IdRelacionPersonaPersona, "Telefono", relacionPersona.Telefono));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(relacionPersona.IdRelacionPersonaPersona, "Complemento", relacionPersona.Complemento));
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AdministrarEnrolamientoRelacionPersona.obtenerRegistrosCambioModificarRelacionPersona", fin - inicio);
		return registrosCambio;
	}
	
	private RegistroCambioMasivo obtenerRegistrosCambioEliminarTarjetaBancaria(RelacionPersona relacionPersona){
		logger.trace("INICIO::AdministrarEnrolamientoRelacionPersona.obtenerRegistrosCambioEliminarTarjetaBancaria");
		long inicio = System.currentTimeMillis();

		RegistroCambioMasivo registroCambio = EnrolamientoUtils.crearRegistroCambio(relacionPersona.IdRelacionPersona, "IdEstado", "BLO");
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AdministrarEnrolamientoRelacionPersona.obtenerRegistrosCambioEliminarTarjetaBancaria", fin - inicio);
		return registroCambio;
	}
	
	private Boolean esRelacionPersonaValidaParaModificacion(RelacionPersona relacionPersona) {
		logger.trace("INICIO::AdministrarEnrolamientoRelacionPersona.esRelacionPersonaValidaParaModificacion");
		long inicio = System.currentTimeMillis();
		Boolean esRelacionPersonaValidaParaModificacion = !relacionPersona.IdRelacionPersonaPersona.equals(0L);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AdministrarEnrolamientoRelacionPersona.esRelacionPersonaValidaParaModificacion", fin - inicio);
		return esRelacionPersonaValidaParaModificacion;
	}
	
	private List<RelacionPersona> obtenerRelacionesPersona(Connection con, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception {
		logger.debug("INICIO::AdministrarEnrolamientoRelacionPersona.obtenerRelacionesPersona");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsPersona = EnrolamientoUtils.obtenerIdsPersona(borradoresCliente);
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("IdPersona", idsPersona);
		
		SQL sql = new SQL();
		List<RelacionPersona> relacionesPersona = sql.query(con, SQL_SELECT_RELACION_PERSONA, parametros, RelacionPersona.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoRelacionPersona.obtenerRelacionesPersona", fin - inicio);
		return relacionesPersona;
	}
	
	public List<DatosClienteDatosGenerales> enrolarRelacionPersona(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception{
		logger.debug("INICIO::AdministrarEnrolamientoRelacionPersona.enrolarRelacionPersona");
		long inicio = System.currentTimeMillis();
		
		borradoresCliente = eliminarRelacionesPersona(con, entrada, borradoresCliente);
		borradoresCliente = crearRelacionesPersona(con, entrada, borradoresCliente);
		borradoresCliente = modificarRelacionesPersona(con, entrada, borradoresCliente);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoRelacionPersona.enrolarRelacionPersona", fin - inicio);
		return borradoresCliente;
	}
	
}
