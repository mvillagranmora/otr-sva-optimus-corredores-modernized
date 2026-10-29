package cl.bcs.socionegocio.soc;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.CrearDireccionFisicaMasivo;
import cl.bcs.socionegocio.per.ModificarPersonaDireccionFisicaMasivo;
import cl.bcs.socionegocio.per.CrearDireccionFisicaMasivo.EntradaCrearDireccionFisicaMasivo;
import cl.bcs.socionegocio.per.CrearDireccionFisicaMasivo.SalidaCrearDireccionFisicaMasivo;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntradaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.SalidaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EliminarPersonaDireccionFisicaMasivo;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.socionegocio.soc.pojo.DireccionFisica;

public class ModificarDireccionFisica implements EjecutarCambiosSocioNegocio{
	
	static final Logger logger = LogManager.getLogger(ModificarDireccionFisica.class);
	
	public SalidaModificarSocioNegocio logica(Connection conn, EntradaModificarSocioNegocio<DireccionFisica> entrada) throws Exception{
		
		logger.info("INICIO::ModificarDireccionFisica");
		Long inicioServicio = System.currentTimeMillis();
		
		Map<String, List<DireccionFisica>> direccionesAgrupadasPorAccion = direccionesAgrupadasPorAccion(entrada.p_datos);
		
		SalidaModificarSocioNegocio out = new SalidaModificarSocioNegocio();
		SalidaCrearDireccionFisicaMasivo salidaCrearDireccion = new SalidaCrearDireccionFisicaMasivo();
		
		salidaCrearDireccion = crearDireccionFisica(conn, entrada, direccionesAgrupadasPorAccion.get("AGREGAR"));
		if(!salidaCrearDireccion.P_OUT_MENSAJES.isEmpty()) {
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salidaCrearDireccion.P_OUT_MENSAJES);
			out.P_OUT_MENSAJES = salidaCrearDireccion.P_OUT_MENSAJES;
			return out;
		}
		
		SalidaEjecutarCambiosPersona salidaModificarDireccion = new SalidaEjecutarCambiosPersona();
		salidaModificarDireccion = modificarDireccionFisica(conn, entrada, direccionesAgrupadasPorAccion.get("MODIFICAR"));
		if(!salidaModificarDireccion.P_OUT_MENSAJES.isEmpty()) {
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salidaModificarDireccion.P_OUT_MENSAJES);
			out.P_OUT_MENSAJES = salidaModificarDireccion.P_OUT_MENSAJES;
			return out;
		}
		
		salidaModificarDireccion = eliminarDireccionFisica(conn, entrada, direccionesAgrupadasPorAccion.get("ELIMINAR"));
		if(!salidaModificarDireccion.P_OUT_MENSAJES.isEmpty()) {
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salidaModificarDireccion.P_OUT_MENSAJES);
			out.P_OUT_MENSAJES = salidaModificarDireccion.P_OUT_MENSAJES;
			return out;
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarDireccionFisica", finServicio - inicioServicio);
		
		return out;	
	}
	
	private SalidaCrearDireccionFisicaMasivo crearDireccionFisica(Connection conn, EntradaModificarSocioNegocio<DireccionFisica> in, List<DireccionFisica> direccionesFisicas) throws Exception {
		
		logger.info("INICIO::ModificarDireccionFisica.crearDireccionFisica");
		Long inicioServicio = System.currentTimeMillis();
		
		List<CrearDireccionFisicaMasivo.DireccionFisica> direccionesFisicasPorCrear = new ArrayList<CrearDireccionFisicaMasivo.DireccionFisica>();
		SalidaCrearDireccionFisicaMasivo salida = new SalidaCrearDireccionFisicaMasivo();
		
		if(direccionesFisicas == null) return salida;
		
		direccionesFisicas.stream()
		.forEach(d -> {
			CrearDireccionFisicaMasivo.DireccionFisica direccion = new CrearDireccionFisicaMasivo.DireccionFisica();
			direccion.IdInterno 		= 0L;
			direccion.CodigoProposito 	= d.IdProposito;
			direccion.Sector 			= d.Sector;
			direccion.CodigoPostal 		= d.CodigoPostal;
			direccion.IdCiudad 			= d.IdCiudad;
			direccion.IdDivisionPais 	= d.IdComuna;
			direccion.Calle				= d.Calle;
			direccion.Numero 			= d.Numero;
			direccion.Comentario 		= d.Comentario;
			direccion.Departamento 		= d.Complemento;
			direccion.CodigoCompuesto 	= d.CodigoUnicoTerritorial;
			direccion.Predeterminado	= "N";
			direccion.IdPersona 		= in.p_IdPersona;
			direccion.IdUsuarioCreador 	= in.p_IdUsuario;
			direccionesFisicasPorCrear.add(direccion);
		});		
		
		
		if(!direccionesFisicasPorCrear.isEmpty()) {
			CrearDireccionFisicaMasivo crearDireccion = new CrearDireccionFisicaMasivo();
			EntradaCrearDireccionFisicaMasivo entradaDireccionFisica = new EntradaCrearDireccionFisicaMasivo();
			entradaDireccionFisica.p_IdEmpresa 			= in.p_IdEmpresa;
			entradaDireccionFisica.p_IdModulo 			= "SOC";
			entradaDireccionFisica.p_UsaReglasNegocio 	= "N";
			entradaDireccionFisica.p_IdUsuario 			= in.p_IdUsuario;
			entradaDireccionFisica.p_DireccionesFisica 	= direccionesFisicasPorCrear;
			salida = crearDireccion.logica(conn, entradaDireccionFisica);
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarDireccionFisica.crearDireccionFisica", finServicio - inicioServicio);
		
		return salida;
		
	}
	
	private SalidaEjecutarCambiosPersona modificarDireccionFisica(Connection conn, EntradaModificarSocioNegocio<DireccionFisica> in, List<DireccionFisica> direccionesFisicas) throws Exception {
		
		logger.info("INICIO::ModificarDireccionFisica.modificarDireccionFisica");
		Long inicioServicio = System.currentTimeMillis();
		
		SalidaEjecutarCambiosPersona salida = new SalidaEjecutarCambiosPersona();
		if(direccionesFisicas == null) return salida;
		
		List<RegistroCambioMasivo> registroCambio = obtenerCambiosDireccionFisicaPorModificar(direccionesFisicas);
		
		if(!registroCambio.isEmpty()) {
			ModificarPersonaDireccionFisicaMasivo modificarDireccion = new ModificarPersonaDireccionFisicaMasivo();
			EntradaEjecutarCambiosPersona entrada = new EntradaEjecutarCambiosPersona();
			entrada.p_IdEmpresa = in.p_IdEmpresa;
			entrada.p_IdUsuario = in.p_IdUsuario;
			entrada.p_UsaReglasNegocio = "N";
			entrada.p_IdModulo = "SOC";
			entrada.p_RegistroCambio = registroCambio;
			return modificarDireccion.logica(conn, entrada);
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarDireccionFisica.modificarDireccionFisica", finServicio - inicioServicio);
		
		return salida;	
	}
	
	private SalidaEjecutarCambiosPersona eliminarDireccionFisica(Connection conn, EntradaModificarSocioNegocio<DireccionFisica> in, List<DireccionFisica> direccionesFisicas) throws Exception {

		logger.info("INICIO::ModificarDireccionFisica.eliminarDireccionFisica");
		Long inicioServicio = System.currentTimeMillis();
		
		SalidaEjecutarCambiosPersona salida = new SalidaEjecutarCambiosPersona();
		if(direccionesFisicas == null) return salida;
		
		List<RegistroCambioMasivo> registroCambio = obtenerCambiosDireccionFisicaPorEliminar(direccionesFisicas);

		if(!registroCambio.isEmpty()) {
			EliminarPersonaDireccionFisicaMasivo eliminarDireccion = new EliminarPersonaDireccionFisicaMasivo();
			EntradaEjecutarCambiosPersona entrada = new EntradaEjecutarCambiosPersona();
			entrada.p_IdEmpresa = in.p_IdEmpresa;
			entrada.p_IdUsuario = in.p_IdUsuario;
			entrada.p_UsaReglasNegocio = "N";
			entrada.p_IdModulo = "SOC";
			entrada.p_RegistroCambio = registroCambio;
			return eliminarDireccion.logica(conn, entrada);
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarDireccionFisica.eliminarDireccionFisica", finServicio - inicioServicio);
		
		return salida;		
	}
	
	private List<RegistroCambioMasivo> obtenerCambiosDireccionFisicaPorModificar(List<DireccionFisica> direccionFisica) {
		
		logger.info("INICIO::ModificarDireccionFisica.obtenerCambiosDireccionFisicaPorModificar");
		Long inicioServicio = System.currentTimeMillis();
		
		List<RegistroCambioMasivo> listRegistroCambio = new ArrayList<RegistroCambioMasivo>();
		
		for(DireccionFisica direccion: direccionFisica) {
			
			if(!direccion.Calle.isEmpty()){
				RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
				registroCambio.CampoModificado	= "Calle";
				registroCambio.ValorNuevo		= direccion.Calle.trim();
				registroCambio.IdEntidad		= direccion.IdDireccionFisica;
				listRegistroCambio.add(registroCambio);
			}
			
			if(!direccion.IdProposito.isEmpty()){
				RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
				registroCambio.CampoModificado	= "CodigoProposito";
				registroCambio.ValorNuevo		= direccion.IdProposito.trim();
				registroCambio.IdEntidad		= direccion.IdDireccionFisica;
				listRegistroCambio.add(registroCambio);
			}
			
			if(!direccion.Sector.isEmpty()){
				RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
				registroCambio.CampoModificado	= "Sector";
				registroCambio.ValorNuevo		= direccion.Sector.trim();
				registroCambio.IdEntidad		= direccion.IdDireccionFisica;
				listRegistroCambio.add(registroCambio);
			}
			
			if(!direccion.CodigoPostal.isEmpty()){
				RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
				registroCambio.CampoModificado	= "CodigoPostal";
				registroCambio.ValorNuevo		= direccion.CodigoPostal.trim();
				registroCambio.IdEntidad		= direccion.IdDireccionFisica;
				listRegistroCambio.add(registroCambio);
			}
			
			if(direccion.IdCiudad > 0L){
				RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
				registroCambio.CampoModificado	= "IdCiudad";
				registroCambio.ValorNuevo		= String.valueOf(direccion.IdCiudad);
				registroCambio.IdEntidad		= direccion.IdDireccionFisica;
				listRegistroCambio.add(registroCambio);
			}
			
			if(direccion.IdComuna > 0L){
				RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
				registroCambio.CampoModificado	= "IdDivisionPais";
				registroCambio.ValorNuevo		= String.valueOf(direccion.IdComuna);
				registroCambio.IdEntidad		= direccion.IdDireccionFisica;
				listRegistroCambio.add(registroCambio);
			}
			
			if(!direccion.Comentario.isEmpty()){
				RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
				registroCambio.CampoModificado	= "Comentario";
				registroCambio.ValorNuevo		= direccion.Comentario;
				registroCambio.IdEntidad		= direccion.IdDireccionFisica;
				listRegistroCambio.add(registroCambio);
			}
			
			if(!direccion.Complemento.isEmpty()){
				RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
				registroCambio.CampoModificado	= "Departamento";
				registroCambio.ValorNuevo		= direccion.Complemento;
				registroCambio.IdEntidad		= direccion.IdDireccionFisica;
				listRegistroCambio.add(registroCambio);
			}
			
			if(!direccion.Numero.isEmpty()){
				RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
				registroCambio.CampoModificado	= "Numero";
				registroCambio.ValorNuevo		= direccion.Numero;
				registroCambio.IdEntidad		= direccion.IdDireccionFisica;
				listRegistroCambio.add(registroCambio);
			}
			
			if(!direccion.CodigoUnicoTerritorial.isEmpty()){
				RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
				registroCambio.CampoModificado	= "CodigoCompuesto";
				registroCambio.ValorNuevo		= direccion.CodigoUnicoTerritorial;
				registroCambio.IdEntidad		= direccion.IdDireccionFisica;
				listRegistroCambio.add(registroCambio);
			}
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarDireccionFisica.obtenerCambiosDireccionFisicaPorModificar", finServicio - inicioServicio);
		
		return listRegistroCambio;
	}
	
	private List<RegistroCambioMasivo> obtenerCambiosDireccionFisicaPorEliminar(List<DireccionFisica> direccionFisica) {
		
		logger.info("INICIO::ModificarDireccionFisica.obtenerCambiosDireccionFisicaPorEliminar");
		Long inicioServicio = System.currentTimeMillis();
		
		List<RegistroCambioMasivo> listRegistroCambio = new ArrayList<RegistroCambioMasivo>();
		
		for(DireccionFisica direccion: direccionFisica) {
			
			if(direccion.IdDireccionFisica > 0){
				RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
				registroCambio.CampoModificado	= "Borrado";
				registroCambio.ValorNuevo		= "S";
				registroCambio.IdEntidad		= direccion.IdDireccionFisica;
				listRegistroCambio.add(registroCambio);
			}
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarDireccionFisica.obtenerCambiosDireccionFisicaPorEliminar", finServicio - inicioServicio);
		
		return listRegistroCambio;
	}
	
	private Map<String, List<DireccionFisica>> direccionesAgrupadasPorAccion(List<DireccionFisica> direccionesFisicas) {
		return direccionesFisicas.stream().collect(Collectors.groupingBy(dir -> dir.Accion));
	}

}
