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
import cl.bcs.socionegocio.enr.pojo.GrupoDireccion;
import cl.bcs.socionegocio.enr.pojo.GrupoDireccionDetalleEnrolamiento;
import cl.bcs.socionegocio.enr.pojo.GrupoDireccionEnrolamiento;
import cl.bcs.socionegocio.per.CrearPersonaGrupoDireccionElectronicaConDetalle;
import cl.bcs.socionegocio.per.CrearPersonaGrupoDireccionElectronicaConDetalle.EntradaCrearGrupoDireccionElectronicaConDetalle;
import cl.bcs.socionegocio.per.CrearPersonaGrupoDireccionElectronicaConDetalle.SalidaCrearGrupoDireccionElectronicaConDetalle;
import cl.bcs.socionegocio.per.CrearPersonaGrupoDireccionElectronicaDetalleMasivo;
import cl.bcs.socionegocio.per.CrearPersonaGrupoDireccionElectronicaDetalleMasivo.EntradaCrearPersonaGrupoDireccionElectronicaDetalleMasivo;
import cl.bcs.socionegocio.per.CrearPersonaGrupoDireccionElectronicaDetalleMasivo.SalidaCrearPersonaGrupoDireccionElectronicaDetalleMasivo;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntradaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.SalidaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EliminarPersonaGrupoDireccionElectronicaMasivo;
import cl.bcs.socionegocio.per.ModificarPersonaGrupoDireccionElectronicaDetalleMasivo;
import cl.bcs.socionegocio.per.ModificarPersonaGrupoDireccionElectronicaMasivo;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.utils.SQL;

public class AdministrarEnrolamientoGrupoDireccionElectronica {
	
	static final Logger logger = LogManager.getLogger(AdministrarEnrolamientoGrupoDireccionElectronica.class);
	
	private static FileLoader loader;
	private static String SQL_SELECT_GRUPO_DIRECCION;
	private static String SQL_SELECT_GRUPO_DIRECCION_DETALLE;
	
	static {
		loader = new FileLoader();
		SQL_SELECT_GRUPO_DIRECCION = loader.loadResourceToString("cl/bcs/socionegocio/enr/sql/AdministrarEnrolamientoGrupoDireccionElectronica_SelectGrupoDireccion.sql");
		SQL_SELECT_GRUPO_DIRECCION_DETALLE = loader.loadResourceToString("cl/bcs/socionegocio/enr/sql/AdministrarEnrolamientoGrupoDireccionElectronica_SelectGrupoDireccionDetalle.sql");
	}
	
	public List<DatosClienteDatosGenerales> crearGrupoDireccionElectronica(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception{
		logger.debug("INICIO::AdministrarEnrolamientoGrupoDireccionElectronica.crearGrupoDireccionElectronica");
		long inicio = System.currentTimeMillis();
		
		for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
			List<GrupoDireccionEnrolamiento> gruposDirecciones = borradorCliente.GruposDireccion
					.parallelStream()
					.filter(grupoDireccion->grupoDireccion.IdGrupoDireccionPersona.equals(0L))
					.collect(Collectors.toList());
			
			Map<Long, Long> idsDireccionesElectronicasRealesPorId = borradorCliente.DireccionesElectronica
					.parallelStream()
					.collect(Collectors.toMap(direccion->direccion.IdDireccionElectronica, direccion-> direccion.IdDireccionElectronicaReal));			
			for(GrupoDireccionEnrolamiento grupoDireccion : gruposDirecciones) {
				EntradaCrearGrupoDireccionElectronicaConDetalle entradaCrearGrupoDireccionElectronicaConDetalle = new EntradaCrearGrupoDireccionElectronicaConDetalle();
				entradaCrearGrupoDireccionElectronicaConDetalle.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaCrearGrupoDireccionElectronicaConDetalle.p_IdUsuario = entrada.p_IdUsuario;
				entradaCrearGrupoDireccionElectronicaConDetalle.p_IdModulo = entrada.p_IdModulo;
				entradaCrearGrupoDireccionElectronicaConDetalle.p_UsaReglasNegocio = "N";
				entradaCrearGrupoDireccionElectronicaConDetalle.p_GrupoDireccionElectronica.IdInterno = grupoDireccion.IdGrupoDireccion;
				entradaCrearGrupoDireccionElectronicaConDetalle.p_GrupoDireccionElectronica.Grupo = grupoDireccion.Grupo;
				entradaCrearGrupoDireccionElectronicaConDetalle.p_GrupoDireccionElectronica.Comentario = grupoDireccion.Comentario;
				entradaCrearGrupoDireccionElectronicaConDetalle.p_GrupoDireccionElectronica.IdPersona = borradorCliente.IdPersona;
				entradaCrearGrupoDireccionElectronicaConDetalle.p_GrupoDireccionElectronica.IdEstado = "ACT";
				entradaCrearGrupoDireccionElectronicaConDetalle.p_GrupoDireccionElectronica.IdUsuarioCreador = entrada.p_IdUsuario;
				entradaCrearGrupoDireccionElectronicaConDetalle.p_GrupoDireccionElectronica.Detalles = borradorCliente.GruposDireccionDetalle
						.parallelStream()
						.filter(grupoDireccionDetalle->grupoDireccionDetalle.IdGrupoDireccion.equals(grupoDireccion.IdGrupoDireccion))
						.map(grupoDireccionDetalle->{
								cl.bcs.socionegocio.per.pojo.GrupoDireccionElectronica.Detalle detalle = new cl.bcs.socionegocio.per.pojo.GrupoDireccionElectronica.Detalle();
								detalle.IdInterno = grupoDireccionDetalle.IdGrupoDireccionDetalle;
								detalle.IdDireccionElectronica = idsDireccionesElectronicasRealesPorId.get(grupoDireccionDetalle.IdDireccionElectronica);
								detalle.IdTipoDestinatario = grupoDireccionDetalle.IdTipoDestinatario;
								detalle.IdEstado = "ACT";
								detalle.IdUsuarioCreador = entrada.p_IdUsuario;
								return detalle;
						})
						.collect(Collectors.toList());
				
				CrearPersonaGrupoDireccionElectronicaConDetalle crearPersonaGrupoDireccionElectronicaConDetalle = new CrearPersonaGrupoDireccionElectronicaConDetalle();
				SalidaCrearGrupoDireccionElectronicaConDetalle salidaCrearGrupoDireccionElectronicaConDetalle = crearPersonaGrupoDireccionElectronicaConDetalle.logica(con, entradaCrearGrupoDireccionElectronicaConDetalle);
				borradorCliente.MensajesSalida.addAll(salidaCrearGrupoDireccionElectronicaConDetalle.P_OUT_MENSAJES);
				
				grupoDireccion.IdGrupoDireccionReal = salidaCrearGrupoDireccionElectronicaConDetalle.P_OUT_REFID.IdEntidad;
				
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoGrupoDireccionElectronica.crearGrupoDireccionElectronica", fin - inicio);
		return borradoresCliente;
	}
	
	public List<DatosClienteDatosGenerales> modificarGrupoDireccionElectronica(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception{
		logger.debug("INICIO::AdministrarEnrolamientoGrupoDireccionElectronica.modificarGrupoDireccionElectronica");
		long inicio = System.currentTimeMillis();

		EntradaEjecutarCambiosPersona entradaEjecutarCambiosPersona = new EntradaEjecutarCambiosPersona();
		entradaEjecutarCambiosPersona.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaEjecutarCambiosPersona.p_IdModulo = entrada.p_IdModulo;
		entradaEjecutarCambiosPersona.p_IdUsuario = entrada.p_IdUsuario;
		entradaEjecutarCambiosPersona.p_UsaReglasNegocio = "N";
		
		for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
			
			List<GrupoDireccionEnrolamiento> gruposDirecciones = borradorCliente.GruposDireccion
					.parallelStream()
					.filter(grupoDireccion->esGrupoDireccionValidaParaModificacion(grupoDireccion))
					.collect(Collectors.toList());
			
			for(GrupoDireccion grupoDireccion : gruposDirecciones) {
				List<RegistroCambioMasivo> registrosCambios = obtenerRegistrosCambioModificarGrupoDireccion(grupoDireccion);
				registrosCambios = registrosCambios
						.parallelStream()
						.filter(registroCambio->registroCambio.ValorNuevo != null)
						.collect(Collectors.toList());
				
				entradaEjecutarCambiosPersona.p_RegistroCambio.addAll(registrosCambios);
			}
		}
		
		if(entradaEjecutarCambiosPersona.p_RegistroCambio.size() > 0) {
			ModificarPersonaGrupoDireccionElectronicaMasivo modificarPersonaGrupoDireccionElectronicaMasivo = new ModificarPersonaGrupoDireccionElectronicaMasivo();
			SalidaEjecutarCambiosPersona salidaEjecutarCambiosPersona = modificarPersonaGrupoDireccionElectronicaMasivo.logica(con, entradaEjecutarCambiosPersona);
			
			for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
				borradorCliente.MensajesSalida.addAll(salidaEjecutarCambiosPersona.P_OUT_MENSAJES);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoGrupoDireccionElectronica.modificarGrupoDireccionElectronica", fin - inicio);
		
		return borradoresCliente;		
	}
	
	public List<DatosClienteDatosGenerales> eliminarGrupoDireccionElectronica(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception{
		logger.debug("INICIO::AdministrarEnrolamientoGrupoDireccionElectronica.eliminarGrupoDireccionElectronica");
		long inicio = System.currentTimeMillis();
		
		List<GrupoDireccionEnrolamiento> gruposDireccionesExistentes = obtenerGruposDireccion(con, borradoresCliente);
		
		EntradaEjecutarCambiosPersona entradaEjecutarCambiosPersona = new EntradaEjecutarCambiosPersona();
		entradaEjecutarCambiosPersona.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaEjecutarCambiosPersona.p_IdModulo = entrada.p_IdModulo;
		entradaEjecutarCambiosPersona.p_IdUsuario = entrada.p_IdUsuario;
		entradaEjecutarCambiosPersona.p_UsaReglasNegocio = "N";
		
		for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
			
			List<GrupoDireccionEnrolamiento> gruposDirecciones = borradorCliente.GruposDireccion
					.parallelStream()
					.filter(grupoDireccion->esGrupoDireccionValidaParaModificacion(grupoDireccion))
					.collect(Collectors.toList());
			
			for(GrupoDireccion grupoDireccionExistente : gruposDireccionesExistentes) {
				GrupoDireccion grupoDireccionEncontrado = gruposDirecciones
						.parallelStream()
						.filter(grupoDireccion->grupoDireccionExistente.IdGrupoDireccion.equals(grupoDireccion.IdGrupoDireccionPersona))
						.findFirst()
						.orElse(null);
				if(grupoDireccionEncontrado == null) {
					RegistroCambioMasivo registroCambio = obtenerRegistrosCambioEliminarGrupoDireccion(grupoDireccionExistente);
					entradaEjecutarCambiosPersona.p_RegistroCambio.add(registroCambio);
				}
				
			}
		}
		
		if(entradaEjecutarCambiosPersona.p_RegistroCambio.size() > 0) {
			EliminarPersonaGrupoDireccionElectronicaMasivo eliminarPersonaGrupoDireccionElectronicaMasivo = new EliminarPersonaGrupoDireccionElectronicaMasivo();
			SalidaEjecutarCambiosPersona salidaEjecutarCambiosPersona = eliminarPersonaGrupoDireccionElectronicaMasivo.logica(con, entradaEjecutarCambiosPersona);
			
			for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
				borradorCliente.MensajesSalida.addAll(salidaEjecutarCambiosPersona.P_OUT_MENSAJES);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoGrupoDireccionElectronica.eliminarGrupoDireccionElectronica", fin - inicio);
		
		return borradoresCliente;
	}
	
	public List<DatosClienteDatosGenerales> agregarDetalleGrupoDireccionExistente(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception{
		logger.debug("INICIO::AdministrarEnrolamientoGrupoDireccionElectronica.agregarDetalleGrupoDireccionExistente");
		long inicio = System.currentTimeMillis();
		
		EntradaCrearPersonaGrupoDireccionElectronicaDetalleMasivo entradaCrearPersonaGrupoDireccionElectronicaDetalleMasivo = new EntradaCrearPersonaGrupoDireccionElectronicaDetalleMasivo();
		entradaCrearPersonaGrupoDireccionElectronicaDetalleMasivo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearPersonaGrupoDireccionElectronicaDetalleMasivo.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearPersonaGrupoDireccionElectronicaDetalleMasivo.p_IdModulo = entrada.p_IdModulo;
		entradaCrearPersonaGrupoDireccionElectronicaDetalleMasivo.p_UsaReglasNegocio = "N";
		
		for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
			List<GrupoDireccionEnrolamiento> gruposDirecciones = borradorCliente.GruposDireccion
					.parallelStream()
					.filter(grupoDireccion->esGrupoDireccionValidaParaModificacion(grupoDireccion))
					.collect(Collectors.toList());
			
			List<GrupoDireccionDetalleEnrolamiento> gruposDireccionesDetalleEnrolamiento = borradorCliente.GruposDireccionDetalle
			.parallelStream()
			.filter(grupoDetalle->esGrupoDireccionDetalleValidaParaModificacion(grupoDetalle))
			.collect(Collectors.toList());
			
			Map<Long, Long> idsDireccionesElectronicasRealesPorId = borradorCliente.DireccionesElectronica
					.parallelStream()
					.collect(Collectors.toMap(direccion->direccion.IdDireccionElectronica, direccion-> direccion.IdDireccionElectronicaReal));
						
			for(GrupoDireccionEnrolamiento grupoDireccion : gruposDirecciones) {
				List<cl.bcs.socionegocio.per.pojo.GrupoDireccionElectronica.Detalle> detalles = gruposDireccionesDetalleEnrolamiento
						.parallelStream()
						.filter(grupoDireccionDetalle->grupoDireccionDetalle.IdGrupoDireccion.equals(grupoDireccion.IdGrupoDireccion))
						.map(grupoDetalle->{
							cl.bcs.socionegocio.per.pojo.GrupoDireccionElectronica.Detalle detalle = new cl.bcs.socionegocio.per.pojo.GrupoDireccionElectronica.Detalle();
							detalle.IdInterno = grupoDetalle.IdGrupoDireccionDetalle;
							detalle.IdGrupoDireccion = grupoDireccion.IdGrupoDireccionPersona;
							detalle.IdDireccionElectronica = idsDireccionesElectronicasRealesPorId.get(grupoDetalle.IdDireccionElectronica);
							detalle.IdTipoDestinatario = grupoDetalle.IdTipoDestinatario;
							detalle.IdEstado = "ACT";
							detalle.IdUsuarioCreador = entrada.p_IdUsuario;
							return detalle;
						})
						.collect(Collectors.toList());
				entradaCrearPersonaGrupoDireccionElectronicaDetalleMasivo.p_Detalles.addAll(detalles);
			}
		}
		
		CrearPersonaGrupoDireccionElectronicaDetalleMasivo crearPersonaGrupoDireccionElectronicaDetalleMasivo = new CrearPersonaGrupoDireccionElectronicaDetalleMasivo();
		SalidaCrearPersonaGrupoDireccionElectronicaDetalleMasivo salidaCrearPersonaGrupoDireccionElectronicaDetalleMasivo = crearPersonaGrupoDireccionElectronicaDetalleMasivo.logica(con, entradaCrearPersonaGrupoDireccionElectronicaDetalleMasivo, true);
		
		if(entradaCrearPersonaGrupoDireccionElectronicaDetalleMasivo.p_Detalles.size() > 0) {
			for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
				borradorCliente.MensajesSalida.addAll(salidaCrearPersonaGrupoDireccionElectronicaDetalleMasivo.P_OUT_MENSAJES);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoGrupoDireccionElectronica.agregarDetalleGrupoDireccionExistente", fin - inicio);
		return borradoresCliente;
	}
	
	public List<DatosClienteDatosGenerales> eliminarGrupoDireccionElectronicaDetalle(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception{
		logger.debug("INICIO::AdministrarEnrolamientoGrupoDireccionElectronica.eliminarGrupoDireccionElectronicaDetalle");
		long inicio = System.currentTimeMillis();
		
		List<GrupoDireccionDetalleEnrolamiento> gruposDireccionesDetalleExistentes = obtenerGruposDireccionDetalle(con, borradoresCliente);
		
		EntradaEjecutarCambiosPersona entradaEjecutarCambiosPersona = new EntradaEjecutarCambiosPersona();
		entradaEjecutarCambiosPersona.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaEjecutarCambiosPersona.p_IdModulo = entrada.p_IdModulo;
		entradaEjecutarCambiosPersona.p_IdUsuario = entrada.p_IdUsuario;
		entradaEjecutarCambiosPersona.p_UsaReglasNegocio = "N";
		
		for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
			
			List<GrupoDireccionDetalleEnrolamiento> gruposDireccionesDetalle = borradorCliente.GruposDireccionDetalle
					.parallelStream()
					.filter(grupoDireccionDetalle->esGrupoDireccionDetalleValidaParaModificacion(grupoDireccionDetalle))
					.collect(Collectors.toList());
			
			for(GrupoDireccionDetalleEnrolamiento grupoDireccionDetalleExistente : gruposDireccionesDetalleExistentes) {
				GrupoDireccionDetalleEnrolamiento grupoDireccionDetalleEncontrado = gruposDireccionesDetalle
						.parallelStream()
						.filter(grupoDireccionDetalle->grupoDireccionDetalleExistente.IdGrupoDireccionDetalle.equals(grupoDireccionDetalle.IdGrupoDireccionDetallePersona))
						.findFirst()
						.orElse(null);
				if(grupoDireccionDetalleEncontrado == null) {
					RegistroCambioMasivo registroCambio = obtenerRegistrosCambioEliminarGrupoDireccionDetalle(grupoDireccionDetalleExistente);
					entradaEjecutarCambiosPersona.p_RegistroCambio.add(registroCambio);
				}
				
			}
		}
		
		if(entradaEjecutarCambiosPersona.p_RegistroCambio.size() > 0) {
			ModificarPersonaGrupoDireccionElectronicaDetalleMasivo modificarPersonaGrupoDireccionElectronicaDetalleMasivo = new ModificarPersonaGrupoDireccionElectronicaDetalleMasivo();
			SalidaEjecutarCambiosPersona salidaEjecutarCambiosPersona = modificarPersonaGrupoDireccionElectronicaDetalleMasivo.logica(con, entradaEjecutarCambiosPersona);
			
			for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
				borradorCliente.MensajesSalida.addAll(salidaEjecutarCambiosPersona.P_OUT_MENSAJES);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoGrupoDireccionElectronica.eliminarGrupoDireccionElectronicaDetalle", fin - inicio);
		
		return borradoresCliente;
	}
	
	private Boolean esGrupoDireccionValidaParaModificacion(GrupoDireccion grupoDireccion) {
		logger.trace("INICIO::AdministrarEnrolamientoGrupoDireccionElectronica.esGrupoDireccionValidaParaModificacion");
		long inicio = System.currentTimeMillis();
		Boolean esGrupoDireccionValidaParaModificacion = !grupoDireccion.IdGrupoDireccionPersona.equals(0L);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AdministrarEnrolamientoGrupoDireccionElectronica.esGrupoDireccionValidaParaModificacion", fin - inicio);
		return esGrupoDireccionValidaParaModificacion;
	}
	
	private Boolean esGrupoDireccionDetalleValidaParaModificacion(GrupoDireccionDetalleEnrolamiento grupoDireccionDetalle) {
		logger.trace("INICIO::AdministrarEnrolamientoGrupoDireccionElectronica.esGrupoDireccionDetalleValidaParaModificacion");
		long inicio = System.currentTimeMillis();
		Boolean esGrupoDireccionDetalleValidaParaModificacion = !grupoDireccionDetalle.IdGrupoDireccionPersona.equals(0L);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AdministrarEnrolamientoGrupoDireccionElectronica.esGrupoDireccionDetalleValidaParaModificacion", fin - inicio);
		return esGrupoDireccionDetalleValidaParaModificacion;
	}
	
	private List<RegistroCambioMasivo> obtenerRegistrosCambioModificarGrupoDireccion(GrupoDireccion grupoDireccion){
		logger.trace("INICIO::AdministrarEnrolamientoGrupoDireccionElectronica.obtenerRegistrosCambioModificarGrupoDireccion");
		long inicio = System.currentTimeMillis();
		List<RegistroCambioMasivo> registrosCambio = new ArrayList<>();
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(grupoDireccion.IdGrupoDireccionPersona, "Grupo", grupoDireccion.Grupo));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(grupoDireccion.IdGrupoDireccionPersona, "Comentario", grupoDireccion.Comentario));
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AdministrarEnrolamientoGrupoDireccionElectronica.obtenerRegistrosCambioModificarGrupoDireccion", fin - inicio);
		return registrosCambio;
	}
	
	private RegistroCambioMasivo obtenerRegistrosCambioEliminarGrupoDireccion(GrupoDireccion grupoDireccion){
		logger.trace("INICIO::AdministrarEnrolamientoGrupoDireccionElectronica.obtenerRegistrosCambioEliminarGrupoDireccion");
		long inicio = System.currentTimeMillis();

		RegistroCambioMasivo registroCambio = EnrolamientoUtils.crearRegistroCambio(grupoDireccion.IdGrupoDireccion, "Borrado", "S");
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AdministrarEnrolamientoGrupoDireccionElectronica.obtenerRegistrosCambioEliminarGrupoDireccion", fin - inicio);
		return registroCambio;
	}
	
	private RegistroCambioMasivo obtenerRegistrosCambioEliminarGrupoDireccionDetalle(GrupoDireccionDetalleEnrolamiento grupoDireccion){
		logger.trace("INICIO::AdministrarEnrolamientoGrupoDireccionElectronica.obtenerRegistrosCambioEliminarGrupoDireccionDetalle");
		long inicio = System.currentTimeMillis();

		RegistroCambioMasivo registroCambio = EnrolamientoUtils.crearRegistroCambio(grupoDireccion.IdGrupoDireccionDetalle, "IdEstado", "INA");
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AdministrarEnrolamientoGrupoDireccionElectronica.obtenerRegistrosCambioEliminarGrupoDireccionDetalle", fin - inicio);
		return registroCambio;
	}
	
	private List<GrupoDireccionEnrolamiento> obtenerGruposDireccion(Connection con, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception {
		logger.debug("INICIO::AdministrarEnrolamientoGrupoDireccionElectronica.obtenerGruposDireccion");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsPersona = EnrolamientoUtils.obtenerIdsPersona(borradoresCliente);
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("IdPersona", idsPersona);
		
		SQL sql = new SQL();
		List<GrupoDireccionEnrolamiento> gruposDirecciones = sql.query(con, SQL_SELECT_GRUPO_DIRECCION, parametros, GrupoDireccionEnrolamiento.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoGrupoDireccionElectronica.obtenerGruposDireccion", fin - inicio);
		return gruposDirecciones;
	}
	
	private List<GrupoDireccionDetalleEnrolamiento> obtenerGruposDireccionDetalle(Connection con, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception {
		logger.debug("INICIO::AdministrarEnrolamientoGrupoDireccionElectronica.obtenerGruposDireccionDetalle");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsPersona = EnrolamientoUtils.obtenerIdsPersona(borradoresCliente);
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("IdPersona", idsPersona);
		
		SQL sql = new SQL();
		List<GrupoDireccionDetalleEnrolamiento> gruposDireccionesDetalle = sql.query(con, SQL_SELECT_GRUPO_DIRECCION_DETALLE, parametros, GrupoDireccionDetalleEnrolamiento.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoGrupoDireccionElectronica.obtenerGruposDireccionDetalle", fin - inicio);
		return gruposDireccionesDetalle;
	}
	
	public List<DatosClienteDatosGenerales> enrolarGrupoDireccion(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception{
		logger.debug("INICIO::AdministrarEnrolamientoGrupoDireccionElectronica.enrolarGrupoDireccion");
		long inicio = System.currentTimeMillis();
		borradoresCliente = eliminarGrupoDireccionElectronica(con, entrada, borradoresCliente);
		borradoresCliente = eliminarGrupoDireccionElectronicaDetalle(con, entrada, borradoresCliente);
		borradoresCliente = crearGrupoDireccionElectronica(con, entrada, borradoresCliente);
		borradoresCliente = modificarGrupoDireccionElectronica(con, entrada, borradoresCliente);
		borradoresCliente = agregarDetalleGrupoDireccionExistente(con, entrada, borradoresCliente);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoGrupoDireccionElectronica.enrolarGrupoDireccion", fin - inicio);
		return borradoresCliente;
	}
	
}
