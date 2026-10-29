package cl.bcs.socionegocio.soc;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.CrearDireccionElectronicaMasivo;
import cl.bcs.socionegocio.per.CrearDireccionElectronicaMasivo.EntradaCrearDireccionElectronicaMasivo;
import cl.bcs.socionegocio.per.CrearDireccionElectronicaMasivo.SalidaCrearDireccionElectronicaMasivo;
import cl.bcs.socionegocio.per.ModificarPersonaDireccionElectronicaMasivo;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntradaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.SalidaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EliminarPersonaDireccionElectronicaMasivo;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.socionegocio.soc.pojo.DireccionElectronica;

public class ModificarDireccionElectronica implements EjecutarCambiosSocioNegocio{
	
	static final Logger logger = LogManager.getLogger(ModificarDireccionElectronica.class);
	
	public SalidaModificarSocioNegocio logica(Connection conn, EntradaModificarSocioNegocio<DireccionElectronica> entrada) throws Exception{
		
		logger.info("INICIO::ModificarDireccionElectronica");
		Long inicioServicio = System.currentTimeMillis();
		
		SalidaModificarSocioNegocio out = new SalidaModificarSocioNegocio();
		SalidaCrearDireccionElectronicaMasivo salidaCrearDireccion = new SalidaCrearDireccionElectronicaMasivo();
		
		Map<String, List<DireccionElectronica>> direccionesAgrupadasPorAccion = direccionesAgrupadasPorAccion(entrada.p_datos);
		
		salidaCrearDireccion = crearDireccionElectronica(conn, entrada, direccionesAgrupadasPorAccion.get("AGREGAR"));
		if(!salidaCrearDireccion.P_OUT_MENSAJES.isEmpty()) {
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salidaCrearDireccion.P_OUT_MENSAJES);
			out.P_OUT_MENSAJES = salidaCrearDireccion.P_OUT_MENSAJES;
			return out;
		}
		
		SalidaEjecutarCambiosPersona salidaModificarDireccion = new SalidaEjecutarCambiosPersona();
		salidaModificarDireccion = modificarDireccionElectronica(conn, entrada, direccionesAgrupadasPorAccion.get("MODIFICAR"));
		if(!salidaModificarDireccion.P_OUT_MENSAJES.isEmpty()) {
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salidaModificarDireccion.P_OUT_MENSAJES);
			out.P_OUT_MENSAJES = salidaModificarDireccion.P_OUT_MENSAJES;
			return out;
		}
		
		salidaModificarDireccion = eliminarDireccionElectronica(conn, entrada, direccionesAgrupadasPorAccion.get("ELIMINAR"));
		if(!salidaModificarDireccion.P_OUT_MENSAJES.isEmpty()) {
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salidaModificarDireccion.P_OUT_MENSAJES);
			out.P_OUT_MENSAJES = salidaModificarDireccion.P_OUT_MENSAJES;
			return out;
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarDireccionElectronica", finServicio - inicioServicio);
		
		return out;	
	}
	
	private SalidaCrearDireccionElectronicaMasivo crearDireccionElectronica(Connection conn, EntradaModificarSocioNegocio<DireccionElectronica> in, List<DireccionElectronica> direccionesElectronicas) throws Exception {
		
		logger.info("INICIO::ModificarDireccionElectronica.crearDireccionElectronica");
		Long inicioServicio = System.currentTimeMillis();
		
		SalidaCrearDireccionElectronicaMasivo salida = new SalidaCrearDireccionElectronicaMasivo();
		if(direccionesElectronicas == null) return salida;
		
		List<CrearDireccionElectronicaMasivo.DireccionElectronica> direccionesElectronicasPorCrear = new ArrayList<CrearDireccionElectronicaMasivo.DireccionElectronica>();
		
		direccionesElectronicas.stream()
		.forEach(d -> {
			CrearDireccionElectronicaMasivo.DireccionElectronica direccion = new CrearDireccionElectronicaMasivo.DireccionElectronica();
			direccion.IdInterno 				= 0L;
			direccion.IdDireccionElectronica 	= 0L;
			direccion.CodigoProposito 			= d.IdProposito;
			direccion.Comentario 				= d.Comentario;
			direccion.Direccion 				= d.Direccion;
			direccion.Predeterminado			= "N";
			direccion.IdPersona 				= in.p_IdPersona;
			direccion.IdUsuarioCreador 			= in.p_IdUsuario;
			direccionesElectronicasPorCrear.add(direccion);
		});		
		
		
		if(!direccionesElectronicasPorCrear.isEmpty()) {
			CrearDireccionElectronicaMasivo crearDireccion = new CrearDireccionElectronicaMasivo();
			EntradaCrearDireccionElectronicaMasivo entradaDireccionElectronica = new EntradaCrearDireccionElectronicaMasivo();
			entradaDireccionElectronica.p_IdEmpresa 				= in.p_IdEmpresa;
			entradaDireccionElectronica.p_IdModulo 					= "SOC";
			entradaDireccionElectronica.p_UsaReglasNegocio 			= "N";
			entradaDireccionElectronica.p_IdUsuario 				= in.p_IdUsuario;
			entradaDireccionElectronica.p_DireccionesElectronica 	= direccionesElectronicasPorCrear;
			salida = crearDireccion.logica(conn, entradaDireccionElectronica);
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarDireccionElectronica.crearDireccionElectronica", finServicio - inicioServicio);
		
		return salida;
		
	}
	
	private SalidaEjecutarCambiosPersona modificarDireccionElectronica(Connection conn, EntradaModificarSocioNegocio<DireccionElectronica> in, List<DireccionElectronica> direccionesElectronicas) throws Exception {
		
		logger.info("INICIO::ModificarDireccionElectronica.modificarDireccionElectronica");
		Long inicioServicio = System.currentTimeMillis();
		
		SalidaEjecutarCambiosPersona salida = new SalidaEjecutarCambiosPersona();
		if(direccionesElectronicas == null) return salida;
		
		List<RegistroCambioMasivo> registroCambio = obtenerCambiosDireccionElectronicaPorModificar(direccionesElectronicas);
	
		if(!registroCambio.isEmpty()) {
			ModificarPersonaDireccionElectronicaMasivo modificarDireccion = new ModificarPersonaDireccionElectronicaMasivo();
			EntradaEjecutarCambiosPersona entrada = new EntradaEjecutarCambiosPersona();
			entrada.p_IdEmpresa = in.p_IdEmpresa;
			entrada.p_IdUsuario = in.p_IdUsuario;
			entrada.p_UsaReglasNegocio = "N";
			entrada.p_IdModulo = "SOC";
			entrada.p_RegistroCambio = registroCambio;
			return modificarDireccion.logica(conn, entrada);
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarDireccionElectronica.modificarDireccionElectronica", finServicio - inicioServicio);
		
		return salida;	
	}
	
	private SalidaEjecutarCambiosPersona eliminarDireccionElectronica(Connection conn, EntradaModificarSocioNegocio<DireccionElectronica> in, List<DireccionElectronica> direccionesElectronicas) throws Exception {
		
		logger.info("INICIO::ModificarDireccionElectronica.eliminarDireccionElectronica");
		Long inicioServicio = System.currentTimeMillis();
		
		SalidaEjecutarCambiosPersona salida = new SalidaEjecutarCambiosPersona();
		if(direccionesElectronicas == null) return salida;
		
		List<RegistroCambioMasivo> registroCambio = obtenerCambiosDireccionElectronicaPorEliminar(direccionesElectronicas);
		
		if(!registroCambio.isEmpty()) {
			EliminarPersonaDireccionElectronicaMasivo eliminarDireccion = new EliminarPersonaDireccionElectronicaMasivo();
			EntradaEjecutarCambiosPersona entrada = new EntradaEjecutarCambiosPersona();
			entrada.p_IdEmpresa = in.p_IdEmpresa;
			entrada.p_IdUsuario = in.p_IdUsuario;
			entrada.p_UsaReglasNegocio = "N";
			entrada.p_IdModulo = "SOC";
			entrada.p_RegistroCambio = registroCambio;
			return eliminarDireccion.logica(conn, entrada);
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarDireccionElectronica.eliminarDireccionElectronica", finServicio - inicioServicio);
		
		return salida;		
	}
	
	private List<RegistroCambioMasivo> obtenerCambiosDireccionElectronicaPorModificar(List<DireccionElectronica> direccionElectronica) {
		
		logger.info("INICIO::ModificarDireccionElectronica.obtenerCambiosDireccionElectronicaPorModificar");
		Long inicioServicio = System.currentTimeMillis();
		
		List<RegistroCambioMasivo> listRegistroCambio = new ArrayList<RegistroCambioMasivo>();
		
		for(DireccionElectronica direccion: direccionElectronica) {
			
			if(!direccion.IdProposito.isEmpty()){
				RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
				registroCambio.CampoModificado	= "CodigoProposito";
				registroCambio.ValorNuevo		= direccion.IdProposito.trim();
				registroCambio.IdEntidad		= direccion.IdDireccionElectronica;
				listRegistroCambio.add(registroCambio);
			}
			
			if(!direccion.Comentario.isEmpty()){
				RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
				registroCambio.CampoModificado	= "Comentario";
				registroCambio.ValorNuevo		= direccion.Comentario;
				registroCambio.IdEntidad		= direccion.IdDireccionElectronica;
				listRegistroCambio.add(registroCambio);
			}
			
			if(!direccion.Direccion.isEmpty()){
				RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
				registroCambio.CampoModificado	= "Direccion";
				registroCambio.ValorNuevo		= direccion.Direccion;
				registroCambio.IdEntidad		= direccion.IdDireccionElectronica;
				listRegistroCambio.add(registroCambio);
			}
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarDireccionElectronica.obtenerCambiosDireccionElectronicaPorModificar", finServicio - inicioServicio);
		
		return listRegistroCambio;
	}
	
	private List<RegistroCambioMasivo> obtenerCambiosDireccionElectronicaPorEliminar(List<DireccionElectronica> direccionElectronica) {
		
		logger.info("INICIO::ModificarDireccionElectronica.obtenerCambiosDireccionElectronicaPorEliminar");
		Long inicioServicio = System.currentTimeMillis();
		
		List<RegistroCambioMasivo> listRegistroCambio = new ArrayList<RegistroCambioMasivo>();
		
		for(DireccionElectronica direccion: direccionElectronica) {
			
			if(direccion.IdDireccionElectronica > 0){
				RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
				registroCambio.CampoModificado	= "Borrado";
				registroCambio.ValorNuevo		= "S";
				registroCambio.IdEntidad		= direccion.IdDireccionElectronica;
				listRegistroCambio.add(registroCambio);
			}
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarDireccionElectronica.obtenerCambiosDireccionElectronicaPorEliminar", finServicio - inicioServicio);
		
		return listRegistroCambio;
	}



	private Map<String, List<DireccionElectronica>> direccionesAgrupadasPorAccion(List<DireccionElectronica> direccionesElectronicas) {
		
		return direccionesElectronicas.stream().collect(Collectors.groupingBy(dir -> dir.Accion));
	
	}
}
