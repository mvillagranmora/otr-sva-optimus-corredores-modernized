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
import cl.bcs.socionegocio.enr.pojo.DocumentoIdentidad;
import cl.bcs.socionegocio.per.CrearDocumentoIdentidadMasivo;
import cl.bcs.socionegocio.per.ModificarPersonaDocumentoIdentidadMasivo;
import cl.bcs.socionegocio.per.CrearDocumentoIdentidadMasivo.EntradaCrearDocumentoIdentidadMasivo;
import cl.bcs.socionegocio.per.CrearDocumentoIdentidadMasivo.SalidaCrearDocumentoIdentidadMasivo;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntradaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.SalidaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EliminarPersonaDocumentoIdentidadMasivo;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.utils.SQL;

public class AdministrarEnrolamientoDocumentoIdentidad {
	
	static final Logger logger = LogManager.getLogger(AdministrarEnrolamientoDocumentoIdentidad.class);
	
	private static FileLoader loader;
	private static String SQL_SELECT_DOCUMENTO_IDENTIDAD;
	
	static {
		loader = new FileLoader();
		SQL_SELECT_DOCUMENTO_IDENTIDAD = loader.loadResourceToString("cl/bcs/socionegocio/enr/sql/AdministrarEnrolamientoDocumentoIdentidad_SelectDocumentoIdentidad.sql");
	}
	
	public List<DatosClienteDatosGenerales> crearDocumentosIdentidad(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception {
		logger.debug("INICIO::AdministrarEnrolamientoDocumentoIdentidad.crearDocumentosIdentidad");
		long inicio = System.currentTimeMillis();
		
		EntradaCrearDocumentoIdentidadMasivo entradaCrearDocumentoIdentidadMasivo = new EntradaCrearDocumentoIdentidadMasivo();
		entradaCrearDocumentoIdentidadMasivo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearDocumentoIdentidadMasivo.p_IdModulo = entrada.p_IdModulo;
		entradaCrearDocumentoIdentidadMasivo.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearDocumentoIdentidadMasivo.p_UsaReglasNegocio = "N";
		
		for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
			
			List<DocumentoIdentidad> documentosIdentidades = borradorCliente.DocumentosIdentidades
					.parallelStream()
					.filter(documentoIdentidad->esDocumentoIdentidadParaCreacion(documentoIdentidad, borradorCliente.Enrolamiento.IdDocumentoIdentidad, borradorCliente.Enrolamiento.IdDocumentoIdentidadAdicional))
					.collect(Collectors.toList());
			
			for(DocumentoIdentidad documentoIdentidad : documentosIdentidades) {
				CrearDocumentoIdentidadMasivo.DocumentoIdentidad entradaDocumentoIdentidad = new CrearDocumentoIdentidadMasivo.DocumentoIdentidad();
				entradaDocumentoIdentidad.IdInterno = documentoIdentidad.IdDocumentoIdentidad;
				entradaDocumentoIdentidad.IdPersona = borradorCliente.IdPersona;
				entradaDocumentoIdentidad.IdTipoDocumentoIdentidad = documentoIdentidad.IdTipoDocumentoIdentidad;
				entradaDocumentoIdentidad.DocumentoIdentidad = documentoIdentidad.DocumentoIdentidad;
				entradaDocumentoIdentidad.NumeroSerie = documentoIdentidad.NumeroSerie;
				entradaDocumentoIdentidad.FechaEmision = documentoIdentidad.FechaEmision;
				entradaDocumentoIdentidad.FechaVencimiento = documentoIdentidad.FechaVencimiento;
				entradaDocumentoIdentidad.IdTipoDocumentoIdentidadAsociado = documentoIdentidad.IdTipoDocumentoIdentidadAsociado;
				entradaDocumentoIdentidad.TipoPersona = documentoIdentidad.TipoPersona;
				entradaDocumentoIdentidad.DocumentoIdentidadAsociado = documentoIdentidad.DocumentoIdentidadAsociado;
				entradaDocumentoIdentidad.IdUsuarioCreador = entrada.p_IdUsuario;
				entradaDocumentoIdentidad.Predeterminado = documentoIdentidad.Predeterminado;
				entradaDocumentoIdentidad.IdEstado = "ACT";
				entradaDocumentoIdentidad.Borrado = "N";
				entradaCrearDocumentoIdentidadMasivo.p_DocumentosIdentidad.add(entradaDocumentoIdentidad);
			}
		}
		
		if(entradaCrearDocumentoIdentidadMasivo.p_DocumentosIdentidad.size() > 0) {
			CrearDocumentoIdentidadMasivo crearDocumentoIdentidadMasivo = new CrearDocumentoIdentidadMasivo();
			SalidaCrearDocumentoIdentidadMasivo salidaCrearRelacionPersonaMasivo = crearDocumentoIdentidadMasivo.logica(con, entradaCrearDocumentoIdentidadMasivo);
			
			for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
				borradorCliente.MensajesSalida.addAll(salidaCrearRelacionPersonaMasivo.P_OUT_MENSAJES);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoDocumentoIdentidad.crearDocumentosIdentidad", fin - inicio);
		return borradoresCliente;
	}
	
	public List<DatosClienteDatosGenerales> modificarDocumentoIdentidad(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception{
		logger.debug("INICIO::AdministrarEnrolamientoDocumentoIdentidad.modificarDocumentoIdentidad");
		long inicio = System.currentTimeMillis();
		
		EntradaEjecutarCambiosPersona entradaEjecutarCambiosPersona = new EntradaEjecutarCambiosPersona();
		entradaEjecutarCambiosPersona.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaEjecutarCambiosPersona.p_IdModulo = entrada.p_IdModulo;
		entradaEjecutarCambiosPersona.p_IdUsuario = entrada.p_IdUsuario;
		entradaEjecutarCambiosPersona.p_UsaReglasNegocio = "N";
		
		for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
			
			List<DocumentoIdentidad> documentosIdentidades = borradorCliente.DocumentosIdentidades
					.parallelStream()
					.filter(documentoIdentidad->esDocumentoIdentidadValidaParaModificacion(documentoIdentidad))
					.collect(Collectors.toList());
			
			for(DocumentoIdentidad documentoIdentidad : documentosIdentidades) {
				List<RegistroCambioMasivo> registrosCambios = obtenerRegistrosCambioModificarDocumentoIdentidad(documentoIdentidad);
				registrosCambios = registrosCambios
						.parallelStream()
						.filter(registroCambio->registroCambio.ValorNuevo != null)
						.collect(Collectors.toList());
				
				entradaEjecutarCambiosPersona.p_RegistroCambio.addAll(registrosCambios);
			}
		}
		
		if(entradaEjecutarCambiosPersona.p_RegistroCambio.size() > 0) {
			ModificarPersonaDocumentoIdentidadMasivo modificarPersonaDocumentoIdentidadMasivo = new ModificarPersonaDocumentoIdentidadMasivo();
			SalidaEjecutarCambiosPersona salidaEjecutarCambiosPersona = modificarPersonaDocumentoIdentidadMasivo.logica(con, entradaEjecutarCambiosPersona);
			
			for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
				borradorCliente.MensajesSalida.addAll(salidaEjecutarCambiosPersona.P_OUT_MENSAJES);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoDocumentoIdentidad.modificarDocumentoIdentidad", fin - inicio);
		
		return borradoresCliente;
	}
	
	public List<DatosClienteDatosGenerales> eliminarDocumentoIdentidad(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception{
		logger.debug("INICIO::AdministrarEnrolamientoDocumentoIdentidad.eliminarDocumentoIdentidad");
		long inicio = System.currentTimeMillis();
		
		List<DocumentoIdentidad> documentosIdentidadesExistentes = obtenerDocumentosIdentidades(con, borradoresCliente);
		
		EntradaEjecutarCambiosPersona entradaEjecutarCambiosPersona = new EntradaEjecutarCambiosPersona();
		entradaEjecutarCambiosPersona.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaEjecutarCambiosPersona.p_IdModulo = entrada.p_IdModulo;
		entradaEjecutarCambiosPersona.p_IdUsuario = entrada.p_IdUsuario;
		entradaEjecutarCambiosPersona.p_UsaReglasNegocio = "N";
		
		for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
			
			List<DocumentoIdentidad> documentosIdentidades = borradorCliente.DocumentosIdentidades
					.parallelStream()
					.filter(documentoIdentidad->esDocumentoIdentidadValidaParaModificacion(documentoIdentidad))
					.collect(Collectors.toList());
			
			for(DocumentoIdentidad documentoIdentidadExistente : documentosIdentidadesExistentes) {
				DocumentoIdentidad documentoIdentidadEncontrado = documentosIdentidades
						.parallelStream()
						.filter(documentoIdentidad->documentoIdentidadExistente.IdDocumentoIdentidad.equals(documentoIdentidad.IdDocumentoIdentidadPersona))
						.findFirst()
						.orElse(null);
				if(documentoIdentidadEncontrado == null) {
					RegistroCambioMasivo registroCambio = obtenerRegistrosCambioEliminarDocumentoIdentidad(documentoIdentidadExistente);
					entradaEjecutarCambiosPersona.p_RegistroCambio.add(registroCambio);
				}
				
			}
		}
		
		if(entradaEjecutarCambiosPersona.p_RegistroCambio.size() > 0) {
			EliminarPersonaDocumentoIdentidadMasivo eliminarPersonaDocumentoIdentidadMasivo = new EliminarPersonaDocumentoIdentidadMasivo();
			SalidaEjecutarCambiosPersona salidaEjecutarCambiosPersona = eliminarPersonaDocumentoIdentidadMasivo.logica(con, entradaEjecutarCambiosPersona);
			
			for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
				borradorCliente.MensajesSalida.addAll(salidaEjecutarCambiosPersona.P_OUT_MENSAJES);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoDocumentoIdentidad.eliminarDocumentoIdentidad", fin - inicio);
		
		return borradoresCliente;
	}
	
	
	private List<RegistroCambioMasivo> obtenerRegistrosCambioModificarDocumentoIdentidad(DocumentoIdentidad documentoIdentidad){
		logger.trace("INICIO::AdministrarEnrolamientoDocumentoIdentidad.obtenerRegistrosCambioModificarDocumentoIdentidad");
		long inicio = System.currentTimeMillis();
		
		List<RegistroCambioMasivo> registrosCambio = new ArrayList<>();
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(documentoIdentidad.IdDocumentoIdentidadPersona, "TipoPersona", documentoIdentidad.TipoPersona));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(documentoIdentidad.IdDocumentoIdentidadPersona, "NumeroSerie", documentoIdentidad.NumeroSerie));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(documentoIdentidad.IdDocumentoIdentidadPersona, "FechaEmision", documentoIdentidad.FechaEmision));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(documentoIdentidad.IdDocumentoIdentidadPersona, "FechaVencimiento", documentoIdentidad.FechaVencimiento));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(documentoIdentidad.IdDocumentoIdentidadPersona, "Predeterminado", documentoIdentidad.Predeterminado));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(documentoIdentidad.IdDocumentoIdentidadPersona, "DocumentoIdentidad", documentoIdentidad.DocumentoIdentidad));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(documentoIdentidad.IdDocumentoIdentidadPersona, "IdTipoDocumentoIdentidad", documentoIdentidad.IdTipoDocumentoIdentidad));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(documentoIdentidad.IdDocumentoIdentidadPersona, "DocumentoIdentidadAsociado", documentoIdentidad.DocumentoIdentidadAsociado));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(documentoIdentidad.IdDocumentoIdentidadPersona, "IdTipoDocumentoIdentidadAsociado", documentoIdentidad.IdTipoDocumentoIdentidadAsociado));
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AdministrarEnrolamientoDocumentoIdentidad.obtenerRegistrosCambioModificarDocumentoIdentidad", fin - inicio);
		return registrosCambio;
	}
	
	private RegistroCambioMasivo obtenerRegistrosCambioEliminarDocumentoIdentidad(DocumentoIdentidad documentoIdentidad){
		logger.trace("INICIO::AdministrarEnrolamientoDocumentoIdentidad.obtenerRegistrosCambioEliminarDocumentoIdentidad");
		long inicio = System.currentTimeMillis();

		RegistroCambioMasivo registroCambio = EnrolamientoUtils.crearRegistroCambio(documentoIdentidad.IdDocumentoIdentidad, "Borrado", "S");
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AdministrarEnrolamientoDocumentoIdentidad.obtenerRegistrosCambioEliminarDocumentoIdentidad", fin - inicio);
		return registroCambio;
	}
	
	private Boolean esDocumentoIdentidadParaCreacion(DocumentoIdentidad documentoIdentidad, Long idDocumentoIdentidad, Long idDocumentoIdentidadAdicional) {
		logger.trace("INICIO::AdministrarEnrolamientoDocumentoIdentidad.esDocumentoIdentidadParaCreacion");
		long inicio = System.currentTimeMillis();
		Boolean esDocumentoIdentidadNoPrincipal = !documentoIdentidad.IdDocumentoIdentidad.equals(idDocumentoIdentidad) && !documentoIdentidad.IdDocumentoIdentidad.equals(idDocumentoIdentidadAdicional) && documentoIdentidad.IdDocumentoIdentidadPersona.equals(0L);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AdministrarEnrolamientoDocumentoIdentidad.esDocumentoIdentidadParaCreacion", fin - inicio);
		return esDocumentoIdentidadNoPrincipal;
	}
	
	private Boolean esDocumentoIdentidadValidaParaModificacion(DocumentoIdentidad documentoIdentidad) {
		logger.trace("INICIO::AdministrarEnrolamientoDocumentoIdentidad.esDocumentoIdentidadValidaParaModificacion");
		long inicio = System.currentTimeMillis();
		Boolean esDocumentoIdentidadValidaParaModificacion = !documentoIdentidad.IdDocumentoIdentidadPersona.equals(0L);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AdministrarEnrolamientoDocumentoIdentidad.esDocumentoIdentidadValidaParaModificacion", fin - inicio);
		return esDocumentoIdentidadValidaParaModificacion;
	}
	
	private List<DocumentoIdentidad> obtenerDocumentosIdentidades(Connection con, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception {
		logger.debug("INICIO::AdministrarEnrolamientoDocumentoIdentidad.obtenerDocumentosIdentidades");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsPersona = EnrolamientoUtils.obtenerIdsPersona(borradoresCliente);
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("IdPersona", idsPersona);
		
		SQL sql = new SQL();
		List<DocumentoIdentidad> documentosIdentidades = sql.query(con, SQL_SELECT_DOCUMENTO_IDENTIDAD, parametros, DocumentoIdentidad.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoDocumentoIdentidad.obtenerDocumentosIdentidades", fin - inicio);
		return documentosIdentidades;
	}
	
	public List<DatosClienteDatosGenerales> enrolarDocumentoIdentidad(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception{
		logger.debug("INICIO::AdministrarEnrolamientoDocumentoIdentidad.enrolarDocumentoIdentidad");
		long inicio = System.currentTimeMillis();
		
		borradoresCliente = eliminarDocumentoIdentidad(con, entrada, borradoresCliente);
		borradoresCliente = crearDocumentosIdentidad(con, entrada, borradoresCliente);
		borradoresCliente = modificarDocumentoIdentidad(con, entrada, borradoresCliente);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoDocumentoIdentidad.enrolarDocumentoIdentidad", fin - inicio);
		return borradoresCliente;
	}

}
