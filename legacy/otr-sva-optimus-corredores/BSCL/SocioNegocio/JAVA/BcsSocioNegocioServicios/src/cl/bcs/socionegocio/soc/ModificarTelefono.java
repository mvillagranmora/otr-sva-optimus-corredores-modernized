package cl.bcs.socionegocio.soc;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.CrearTelefonoMasivo;
import cl.bcs.socionegocio.per.CrearTelefonoMasivo.EntradaCrearTelefonoMasivo;
import cl.bcs.socionegocio.per.CrearTelefonoMasivo.SalidaCrearTelefonoMasivo;
import cl.bcs.socionegocio.per.EliminarPersonaTelefonoMasivo;
import cl.bcs.socionegocio.per.ModificarPersonaTelefonoMasivo;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntradaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.SalidaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.socionegocio.soc.pojo.Telefono;

public class ModificarTelefono implements EjecutarCambiosSocioNegocio{
	
	static final Logger logger = LogManager.getLogger(ModificarTelefono.class);
	
	public SalidaModificarSocioNegocio logica(Connection conn, EntradaModificarSocioNegocio<Telefono> entrada) throws Exception {
		
		logger.info("INICIO::ModificarTelefono");
		Long inicioServicio = System.currentTimeMillis();
		
		SalidaModificarSocioNegocio out = new SalidaModificarSocioNegocio();
		SalidaCrearTelefonoMasivo salidaCrearTelefono = new SalidaCrearTelefonoMasivo();
		
		Map<String, List<Telefono>> telefonosAgrupadosPorAccion = telefonosAgrupadosPorAccion(entrada.p_datos);
		
		salidaCrearTelefono = crearTelefono(conn, entrada, telefonosAgrupadosPorAccion.get("AGREGAR"));
		if(!salidaCrearTelefono.P_OUT_MENSAJES.isEmpty()) {
			out.P_OUT_ERRORESCONTADOR = salidaCrearTelefono.P_OUT_ERRORESCONTADOR;
			out.P_OUT_MENSAJES = salidaCrearTelefono.P_OUT_MENSAJES;
			return out;
		}
		
		SalidaEjecutarCambiosPersona salidaModificarTelefono = new SalidaEjecutarCambiosPersona();
		salidaModificarTelefono = modificarTelefono(conn, entrada, telefonosAgrupadosPorAccion.get("MODIFICAR"));
		if(!salidaModificarTelefono.P_OUT_MENSAJES.isEmpty()) {
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salidaModificarTelefono.P_OUT_MENSAJES);
			out.P_OUT_MENSAJES = salidaModificarTelefono.P_OUT_MENSAJES;
			return out;
		}
		
		salidaModificarTelefono = eliminarTelefono(conn, entrada, telefonosAgrupadosPorAccion.get("ELIMINAR"));
		if(!salidaModificarTelefono.P_OUT_MENSAJES.isEmpty()) {
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salidaModificarTelefono.P_OUT_MENSAJES);
			out.P_OUT_MENSAJES = salidaModificarTelefono.P_OUT_MENSAJES;
			return out;
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarTelefono", finServicio - inicioServicio);
		
		return out;
	}
	
	private SalidaCrearTelefonoMasivo crearTelefono(Connection conn, EntradaModificarSocioNegocio<Telefono> in, List<Telefono> telefonos) throws Exception {
		
		logger.info("INICIO::ModificarTelefono.crearTelefono");
		Long inicioServicio = System.currentTimeMillis();
		
		SalidaCrearTelefonoMasivo salida = new SalidaCrearTelefonoMasivo();
		if(telefonos == null) return salida;
		
		List<CrearTelefonoMasivo.Telefono> telefonosPorCrear = new ArrayList<CrearTelefonoMasivo.Telefono>();
		
		telefonos.stream()
		.forEach(t -> {
			CrearTelefonoMasivo.Telefono telefono = new CrearTelefonoMasivo.Telefono();
			telefono.IdInterno 			= 0L;
			telefono.IdTelefono 		= 0L;
			telefono.CodigoProposito 	= t.IdProposito;
			telefono.Numero 			= t.Numero;
			telefono.IdPais 			= t.IdPais;
			telefono.IdTipoTelefono 	= t.Tipo;
			telefono.Anexo 				= t.Anexo;
			telefono.Comentario 		= t.Comentario;
			telefono.Predeterminado		= t.Predeterminado;
			telefono.IdPersona 			= in.p_IdPersona;
			telefono.IdUsuarioCreador 	= in.p_IdUsuario;
			telefonosPorCrear.add(telefono);
		});		
		
		if(!telefonosPorCrear.isEmpty()) {
			CrearTelefonoMasivo crearTelefono = new CrearTelefonoMasivo();
			EntradaCrearTelefonoMasivo entradaCrearTelefono = new EntradaCrearTelefonoMasivo();
			entradaCrearTelefono.p_IdEmpresa 		= in.p_IdEmpresa;
			entradaCrearTelefono.p_IdModulo 		= "SOC";
			entradaCrearTelefono.p_UsaReglasNegocio = "N";
			entradaCrearTelefono.p_IdUsuario 		= in.p_IdUsuario;
			entradaCrearTelefono.p_Telefonos 		= telefonosPorCrear;
			salida = crearTelefono.logica(conn, entradaCrearTelefono);
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarTelefono.crearTelefono", finServicio - inicioServicio);
		
		return salida;
		
	}
	
	private SalidaEjecutarCambiosPersona modificarTelefono(Connection conn, EntradaModificarSocioNegocio<Telefono> in, List<Telefono> telefonos) throws Exception {
		
		logger.info("INICIO::ModificarTelefono.modificarTelefono");
		Long inicioServicio = System.currentTimeMillis();
		
		SalidaEjecutarCambiosPersona salida = new SalidaEjecutarCambiosPersona();
		if(telefonos == null) return salida;
		
		List<RegistroCambioMasivo> registroCambio = obtenerCambiosTelefonoPorModificar(telefonos);
		if(!registroCambio.isEmpty()) {
			ModificarPersonaTelefonoMasivo modificarTelefono = new ModificarPersonaTelefonoMasivo();
			EntradaEjecutarCambiosPersona entrada = new EntradaEjecutarCambiosPersona();
			entrada.p_IdEmpresa 		= in.p_IdEmpresa;
			entrada.p_IdUsuario 		= in.p_IdUsuario;
			entrada.p_UsaReglasNegocio 	= "N";
			entrada.p_IdModulo 			= "SOC";
			entrada.p_RegistroCambio 	= registroCambio;
			return modificarTelefono.logica(conn, entrada);
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarTelefono.modificarTelefono", finServicio - inicioServicio);
		
		return salida;	
	}
	
	private SalidaEjecutarCambiosPersona eliminarTelefono(Connection conn, EntradaModificarSocioNegocio<Telefono> in, List<Telefono> telefonos) throws Exception {
		
		logger.info("INICIO::ModificarTelefono.eliminarTelefono");
		Long inicioServicio = System.currentTimeMillis();
		
		SalidaEjecutarCambiosPersona salida = new SalidaEjecutarCambiosPersona();
		if(telefonos == null) return salida;
		
		List<RegistroCambioMasivo> registroCambio = obtenerCambiosTelefonoPorEliminar(telefonos);
		
		if(!registroCambio.isEmpty()) {
			EliminarPersonaTelefonoMasivo eliminarTelefono = new EliminarPersonaTelefonoMasivo();
			EntradaEjecutarCambiosPersona entrada = new EntradaEjecutarCambiosPersona();
			entrada.p_IdEmpresa 		= in.p_IdEmpresa;
			entrada.p_IdUsuario 		= in.p_IdUsuario;
			entrada.p_UsaReglasNegocio 	= "N";
			entrada.p_IdModulo 			= "SOC";
			entrada.p_RegistroCambio 	= registroCambio;
			return eliminarTelefono.logica(conn, entrada);
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarTelefono.eliminarTelefono", finServicio - inicioServicio);
		
		return salida;		
	}
	
	private List<RegistroCambioMasivo> obtenerCambiosTelefonoPorModificar(List<Telefono> telefonos) {
		
		logger.info("INICIO::ModificarTelefono.obtenerCambiosTelefonoPorModificar");
		Long inicioServicio = System.currentTimeMillis();
		
		List<RegistroCambioMasivo> listRegistroCambio = new ArrayList<RegistroCambioMasivo>();
		
		for(Telefono telefono: telefonos) {
			
			if(!telefono.Numero.isEmpty()){
				RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
				registroCambio.CampoModificado	= "Numero";
				registroCambio.ValorNuevo		= telefono.Numero.trim();
				registroCambio.IdEntidad		= telefono.IdTelefono;
				listRegistroCambio.add(registroCambio);
			}
			
			if(!telefono.IdProposito.isEmpty()){
				RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
				registroCambio.CampoModificado	= "CodigoProposito";
				registroCambio.ValorNuevo		= telefono.IdProposito.trim();
				registroCambio.IdEntidad		= telefono.IdTelefono;
				listRegistroCambio.add(registroCambio);
			}
			
			if(telefono.IdPais > 0L){
				RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
				registroCambio.CampoModificado	= "IdPais";
				registroCambio.ValorNuevo		= String.valueOf(telefono.IdPais);
				registroCambio.IdEntidad		= telefono.IdTelefono;
				listRegistroCambio.add(registroCambio);
			}
			
			if(!telefono.Tipo.isEmpty()){
				RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
				registroCambio.CampoModificado	= "IdTipoTelefono";
				registroCambio.ValorNuevo		= telefono.Tipo;
				registroCambio.IdEntidad		= telefono.IdTelefono;
				listRegistroCambio.add(registroCambio);
			}
			
			if(!telefono.Anexo.isEmpty()){
				RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
				registroCambio.CampoModificado	= "Anexo";
				registroCambio.ValorNuevo		= telefono.Anexo;
				registroCambio.IdEntidad		= telefono.IdTelefono;
				listRegistroCambio.add(registroCambio);
			}
			
			if(!telefono.Comentario.isEmpty()){
				RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
				registroCambio.CampoModificado	= "Comentario";
				registroCambio.ValorNuevo		= telefono.Comentario;
				registroCambio.IdEntidad		= telefono.IdTelefono;
				listRegistroCambio.add(registroCambio);
			}
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarTelefono.obtenerCambiosTelefonoPorModificar", finServicio - inicioServicio);
		
		return listRegistroCambio;
	}
	
	private List<RegistroCambioMasivo> obtenerCambiosTelefonoPorEliminar(List<Telefono> telefonos) {
		
		logger.info("INICIO::ModificarTelefono.obtenerCambiosTelefonoPorEliminar");
		Long inicioServicio = System.currentTimeMillis();
		
		List<RegistroCambioMasivo> listRegistroCambio = new ArrayList<RegistroCambioMasivo>();
		
		for(Telefono telefono: telefonos) {
			
			if(telefono.IdTelefono > 0){
				RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
				registroCambio.CampoModificado	= "Borrado";
				registroCambio.ValorNuevo		= "S";
				registroCambio.IdEntidad		= telefono.IdTelefono;
				listRegistroCambio.add(registroCambio);
			}
			
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarTelefono.obtenerCambiosTelefonoPorEliminar", finServicio - inicioServicio);
		
		return listRegistroCambio;
	}
	
	private Map<String, List<Telefono>> telefonosAgrupadosPorAccion(List<Telefono> telefonos) {
		return telefonos.stream().collect(Collectors.groupingBy(tel -> tel.Accion));
	}

}
