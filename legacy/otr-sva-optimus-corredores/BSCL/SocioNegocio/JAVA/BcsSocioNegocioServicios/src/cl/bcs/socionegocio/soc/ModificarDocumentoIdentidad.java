package cl.bcs.socionegocio.soc;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.CrearDocumentoIdentidadMasivo;
import cl.bcs.socionegocio.per.EliminarPersonaDocumentoIdentidadMasivo;
import cl.bcs.socionegocio.per.ModificarPersonaDocumentoIdentidadMasivo;
import cl.bcs.socionegocio.per.CrearDocumentoIdentidadMasivo.EntradaCrearDocumentoIdentidadMasivo;
import cl.bcs.socionegocio.per.CrearDocumentoIdentidadMasivo.SalidaCrearDocumentoIdentidadMasivo;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntradaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.SalidaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.socionegocio.soc.pojo.DocumentoIdentidad;

public class ModificarDocumentoIdentidad implements EjecutarCambiosSocioNegocio{
	
	static final Logger logger = LogManager.getLogger(ModificarDocumentoIdentidad.class);
	
	public SalidaModificarSocioNegocio logica(Connection conn, EntradaModificarSocioNegocio<DocumentoIdentidad> entrada) throws Exception {
		
		logger.info("INICIO::ModificarDocumentoIdentidad");
		Long inicioServicio = System.currentTimeMillis();
		
		SalidaModificarSocioNegocio out = new SalidaModificarSocioNegocio();
		SalidaCrearDocumentoIdentidadMasivo salidaCrearDocumento = new SalidaCrearDocumentoIdentidadMasivo();
		
		Map<String, List<DocumentoIdentidad>> documentosAgrupadosPorAccion = documentosAgrupadosPorAccion(entrada.p_datos);
		
		salidaCrearDocumento = Modificar_CrearDocumentoIdentidad(conn, entrada, documentosAgrupadosPorAccion.get("AGREGAR"));
		if(salidaCrearDocumento.P_OUT_ERRORESCONTADOR > 0) {
			out.P_OUT_ERRORESCONTADOR = salidaCrearDocumento.P_OUT_ERRORESCONTADOR;
			out.P_OUT_MENSAJES = salidaCrearDocumento.P_OUT_MENSAJES;
			return out;
		}
		
		SalidaEjecutarCambiosPersona salidaModificarDocumento = new SalidaEjecutarCambiosPersona();
		salidaModificarDocumento = modificarDocumentoIdentidad(conn, entrada, documentosAgrupadosPorAccion.get("MODIFICAR"));
		if(!salidaModificarDocumento.P_OUT_MENSAJES.isEmpty()) {
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salidaModificarDocumento.P_OUT_MENSAJES);
			out.P_OUT_MENSAJES = salidaModificarDocumento.P_OUT_MENSAJES;
			return out;
		}
		
		salidaModificarDocumento = eliminarDocumentoIdentidad(conn, entrada, documentosAgrupadosPorAccion.get("ELIMINAR"));
		if(!salidaModificarDocumento.P_OUT_MENSAJES.isEmpty()) {
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salidaModificarDocumento.P_OUT_MENSAJES);
			out.P_OUT_MENSAJES = salidaModificarDocumento.P_OUT_MENSAJES;
			return out;
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarDocumentoIdentidad", finServicio - inicioServicio);
		
		return out;
		
	}
	
	private SalidaCrearDocumentoIdentidadMasivo Modificar_CrearDocumentoIdentidad(Connection conn, EntradaModificarSocioNegocio<DocumentoIdentidad> in, List<DocumentoIdentidad> documentosIdentidad) throws Exception {
		
		logger.info("INICIO::ModificarDocumentoIdentidad.Modificar_CrearDocumentoIdentidad");
		Long inicioServicio = System.currentTimeMillis();
		
		SalidaCrearDocumentoIdentidadMasivo salida = new SalidaCrearDocumentoIdentidadMasivo();
		if(documentosIdentidad == null) return salida;
		
		List<CrearDocumentoIdentidadMasivo.DocumentoIdentidad> documentosIdentidadPorCrear = new ArrayList<CrearDocumentoIdentidadMasivo.DocumentoIdentidad>();
		
		documentosIdentidad.stream()
		.forEach(d -> {
			CrearDocumentoIdentidadMasivo.DocumentoIdentidad documento = new CrearDocumentoIdentidadMasivo.DocumentoIdentidad();
			documento.IdInterno 						= 0L;
			documento.IdPersona 						= in.p_IdPersona;
			documento.IdTipoDocumentoIdentidad 			= d.TipoDocumentoIdentidad;
			documento.DocumentoIdentidad 				= d.DocumentoIdentidad;
			documento.NumeroSerie 						= d.NumeroSerie;
			documento.FechaEmision 						= d.FechaEmision;
			documento.FechaVencimiento 					= d.FechaVencimiento;
			documento.IdTipoDocumentoIdentidadAsociado  = "";
			documento.DocumentoIdentidadAsociado 		= "";
			documento.Predeterminado 					= "N";
			documento.TipoPersona 						= in.p_IdTipoPersona;
			documento.IdUsuarioCreador 					= in.p_IdUsuario;
			documentosIdentidadPorCrear.add(documento);
		});		
		
		if(!documentosIdentidadPorCrear.isEmpty()) {
			CrearDocumentoIdentidadMasivo crearDocumento = new CrearDocumentoIdentidadMasivo();
			EntradaCrearDocumentoIdentidadMasivo entradaCrearDocumento = new EntradaCrearDocumentoIdentidadMasivo();
			entradaCrearDocumento.p_IdEmpresa = in.p_IdEmpresa;
			entradaCrearDocumento.p_IdModulo = "SOC";
			entradaCrearDocumento.p_UsaReglasNegocio = "N";
			entradaCrearDocumento.p_IdUsuario = in.p_IdUsuario;
			entradaCrearDocumento.p_DocumentosIdentidad = documentosIdentidadPorCrear;
			salida = crearDocumento.logica(conn, entradaCrearDocumento);
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarDocumentoIdentidad.Modificar_CrearDocumentoIdentidad", finServicio - inicioServicio);
		
		return salida;
		
	}
	
	private SalidaEjecutarCambiosPersona modificarDocumentoIdentidad(Connection conn, EntradaModificarSocioNegocio<DocumentoIdentidad> in, List<DocumentoIdentidad> documentosIdentidad) throws Exception {
		
		logger.info("INICIO::ModificarDocumentoIdentidad.modificarDocumentoIdentidad");
		Long inicioServicio = System.currentTimeMillis();
		
		SalidaEjecutarCambiosPersona salida = new SalidaEjecutarCambiosPersona();
		if(documentosIdentidad == null) return salida;
		
		List<RegistroCambioMasivo> registroCambio = obtenerCambiosDocumentoIdentidadPorModificar(documentosIdentidad);
		
		if(!registroCambio.isEmpty()) {
			ModificarPersonaDocumentoIdentidadMasivo modificarDocumento = new ModificarPersonaDocumentoIdentidadMasivo();
			EntradaEjecutarCambiosPersona entrada = new EntradaEjecutarCambiosPersona();
			entrada.p_IdEmpresa = in.p_IdEmpresa;
			entrada.p_IdUsuario = in.p_IdUsuario;
			entrada.p_UsaReglasNegocio = "N";
			entrada.p_IdModulo = "SOC";
			entrada.p_RegistroCambio = registroCambio;
			return modificarDocumento.logica(conn, entrada);
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarDocumentoIdentidad.modificarDocumentoIdentidad", finServicio - inicioServicio);
		
		return salida;	
	}
	
	private SalidaEjecutarCambiosPersona eliminarDocumentoIdentidad(Connection conn, EntradaModificarSocioNegocio<DocumentoIdentidad> in, List<DocumentoIdentidad> documentosIdentidad) throws Exception {
		
		logger.info("INICIO::ModificarDocumentoIdentidad.eliminarDocumentoIdentidad");
		Long inicioServicio = System.currentTimeMillis();
		
		SalidaEjecutarCambiosPersona salida = new SalidaEjecutarCambiosPersona();
		if(documentosIdentidad == null) return salida;
		
		List<RegistroCambioMasivo> registroCambio = obtenerCambiosDocumentoIdentidadPorEliminar(documentosIdentidad);
		
		if(!registroCambio.isEmpty()) {
			EliminarPersonaDocumentoIdentidadMasivo eliminarDocumento = new EliminarPersonaDocumentoIdentidadMasivo();
			EntradaEjecutarCambiosPersona entrada = new EntradaEjecutarCambiosPersona();
			entrada.p_IdEmpresa = in.p_IdEmpresa;
			entrada.p_IdUsuario = in.p_IdUsuario;
			entrada.p_UsaReglasNegocio = "N";
			entrada.p_IdModulo = "SOC";
			entrada.p_RegistroCambio = registroCambio;
			return eliminarDocumento.logica(conn, entrada);
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarDocumentoIdentidad.eliminarDocumentoIdentidad", finServicio - inicioServicio);
		
		return salida;		
	}
	
	private List<RegistroCambioMasivo> obtenerCambiosDocumentoIdentidadPorModificar(List<DocumentoIdentidad> documentosIdentidad) {
		
		logger.info("INICIO::ModificarDocumentoIdentidad.obtenerCambiosDocumentoIdentidadPorModificar");
		Long inicioServicio = System.currentTimeMillis();
		
		List<RegistroCambioMasivo> listRegistroCambio = new ArrayList<RegistroCambioMasivo>();
		for(DocumentoIdentidad documento: documentosIdentidad) {
			
			if(!documento.TipoDocumentoIdentidad.isEmpty()){
				RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
				registroCambio.CampoModificado	= "IdTipoDocumentoIdentidad";
				registroCambio.ValorNuevo		= documento.TipoDocumentoIdentidad.trim();
				registroCambio.IdEntidad		= documento.IdDocumentoIdentidad;
				listRegistroCambio.add(registroCambio);
			}
			
			if(!documento.DocumentoIdentidad.isEmpty()){
				RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
				registroCambio.CampoModificado	= "DocumentoIdentidad";
				registroCambio.ValorNuevo		= documento.DocumentoIdentidad.trim();
				registroCambio.IdEntidad		= documento.IdDocumentoIdentidad;
				listRegistroCambio.add(registroCambio);
			}
			
			if(!documento.NumeroSerie.isEmpty()){
				RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
				registroCambio.CampoModificado	= "NumeroSerie";
				registroCambio.ValorNuevo		= documento.NumeroSerie.trim();
				registroCambio.IdEntidad		= documento.IdDocumentoIdentidad;
				listRegistroCambio.add(registroCambio);
			}
			
			if(!documento.FechaEmision.isEmpty()){
				RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
				registroCambio.CampoModificado	= "FechaEmision";
				registroCambio.ValorNuevo		= documento.FechaEmision.trim();
				registroCambio.IdEntidad		= documento.IdDocumentoIdentidad;
				listRegistroCambio.add(registroCambio);
			}
			
			if(!documento.FechaVencimiento.isEmpty()){
				RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
				registroCambio.CampoModificado	= "FechaVencimiento";
				registroCambio.ValorNuevo		= documento.FechaVencimiento.trim();
				registroCambio.IdEntidad		= documento.IdDocumentoIdentidad;
				listRegistroCambio.add(registroCambio);
			}	

		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarDocumentoIdentidad.obtenerCambiosDocumentoIdentidadPorModificar", finServicio - inicioServicio);
		
		return listRegistroCambio;
	}
	
	private List<RegistroCambioMasivo> obtenerCambiosDocumentoIdentidadPorEliminar(List<DocumentoIdentidad> documentosIdentidad) {
		
		logger.info("INICIO::ModificarDocumentoIdentidad.obtenerCambiosDocumentoIdentidadPorEliminar");
		Long inicioServicio = System.currentTimeMillis();
		
		List<RegistroCambioMasivo> listRegistroCambio = new ArrayList<RegistroCambioMasivo>();
		for(DocumentoIdentidad documento: documentosIdentidad) {
			
			if(documento.IdDocumentoIdentidad > 0){
				RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
				registroCambio.CampoModificado	= "Borrado";
				registroCambio.ValorNuevo		= "S";
				registroCambio.IdEntidad		= documento.IdDocumentoIdentidad;
				listRegistroCambio.add(registroCambio);
			}
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarDocumentoIdentidad.obtenerCambiosDocumentoIdentidadPorEliminar", finServicio - inicioServicio);
		
		return listRegistroCambio;
	}
	
	private Map<String, List<DocumentoIdentidad>> documentosAgrupadosPorAccion(List<DocumentoIdentidad> documentosIdentidad) {
			
		return documentosIdentidad.stream().collect(Collectors.groupingBy(doc -> doc.Accion));
	
	}
	
}
