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
import cl.bcs.socionegocio.enr.pojo.Portafolio;
import cl.bcs.socionegocio.enr.pojo.Telefono;
import cl.bcs.socionegocio.per.CrearTelefonoMasivo;
import cl.bcs.socionegocio.per.EliminarPersonaTelefonoMasivo;
import cl.bcs.socionegocio.per.ModificarPersonaTelefonoMasivo;
import cl.bcs.socionegocio.per.CrearTelefonoMasivo.EntradaCrearTelefonoMasivo;
import cl.bcs.socionegocio.per.CrearTelefonoMasivo.SalidaCrearTelefonoMasivo;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntradaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.SalidaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.utils.SQL;

public class AdministrarEnrolamientoTelefono {
	
	static final Logger logger = LogManager.getLogger(AdministrarEnrolamientoTelefono.class);
	
	private static FileLoader loader;
	private static final String SQL_SELECT_TELEFONO;
	private static final String SQL_UPDATE_TELEFONO_PREDETERMINADO_PORTAFOLIO;
	private static final String SQL_SELECT_TELEFONO_PREDETERMINADO;
	
	static {
		loader = new FileLoader();
		SQL_SELECT_TELEFONO = loader.loadResourceToString("cl/bcs/socionegocio/enr/sql/AdministrarEnrolamientoTelefono_SelectTelefono.sql");
		SQL_UPDATE_TELEFONO_PREDETERMINADO_PORTAFOLIO = loader.loadResourceToString("cl/bcs/socionegocio/enr/sql/AdministrarEnrolamientoTelefono_UpdatetTelefonoPredeterminado.sql");
		SQL_SELECT_TELEFONO_PREDETERMINADO = loader.loadResourceToString("cl/bcs/socionegocio/enr/sql/AdministrarEnrolamientoTelefono_SelectTelefonoPredeterminado.sql");
	}
	
	public static class TelefonoPredeterminado {
		public Long IdTelefonoContacto;
	}
	
	public List<DatosClienteDatosGenerales> crearTelefonos(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception {
		logger.debug("INICIO::AdministrarEnrolamientoTelefono.crearTelefonos");
		long inicio = System.currentTimeMillis();
		
		EntradaCrearTelefonoMasivo entradaCrearTelefonoMasivo = new EntradaCrearTelefonoMasivo();
		entradaCrearTelefonoMasivo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearTelefonoMasivo.p_IdModulo = entrada.p_IdModulo;
		entradaCrearTelefonoMasivo.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearTelefonoMasivo.p_UsaReglasNegocio = "N";
		
		for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
			List<Telefono> telefonos = borradorCliente.Telefonos
					.stream()
					.filter(telefono->esTelefonoValidoParaCreacion(telefono, borradorCliente.Enrolamiento.IdTelefonoPrincipal))
					.collect(Collectors.toList());
			for(Telefono telefono : telefonos) {
				CrearTelefonoMasivo.Telefono entradaTelefono = new CrearTelefonoMasivo.Telefono();
				entradaTelefono.IdInterno = telefono.IdTelefono;
				entradaTelefono.IdPais = telefono.IdPais;
				entradaTelefono.IdTipoTelefono = telefono.IdTipoTelefono;
				entradaTelefono.Numero = telefono.Numero;
				entradaTelefono.Anexo = telefono.Anexo;
				entradaTelefono.CodigoProposito = telefono.CodigoProposito;
				entradaTelefono.Comentario = telefono.Comentario;
				entradaTelefono.IdEstado = "ACT";
				entradaTelefono.IdPersona = borradorCliente.IdPersona;
				entradaTelefono.Predeterminado = telefono.Predeterminado;
				entradaTelefono.IdUsuarioCreador = entrada.p_IdUsuario;
				
				entradaCrearTelefonoMasivo.p_Telefonos.add(entradaTelefono);
			}
		}
		
		if(entradaCrearTelefonoMasivo.p_Telefonos.size() > 0) {
			CrearTelefonoMasivo crearTelefonoMasivo = new CrearTelefonoMasivo();
			SalidaCrearTelefonoMasivo salidaCrearTelefonoMasivo = crearTelefonoMasivo.logica(con, entradaCrearTelefonoMasivo);
			
			for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
				borradorCliente.MensajesSalida.addAll(salidaCrearTelefonoMasivo.P_OUT_MENSAJES);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoTelefono.crearTelefonos", fin - inicio);
		return borradoresCliente;
	}
	
	public List<DatosClienteDatosGenerales> modificarTelefonos(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception{
		logger.debug("INICIO::AdministrarEnrolamientoTelefono.modificarTelefonos");
		long inicio = System.currentTimeMillis();
		
		EntradaEjecutarCambiosPersona entradaEjecutarCambiosPersona = new EntradaEjecutarCambiosPersona();
		entradaEjecutarCambiosPersona.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaEjecutarCambiosPersona.p_IdModulo = entrada.p_IdModulo;
		entradaEjecutarCambiosPersona.p_IdUsuario = entrada.p_IdUsuario;
		entradaEjecutarCambiosPersona.p_UsaReglasNegocio = "N";
		
		for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
			
			List<Telefono> telefonos = borradorCliente.Telefonos
					.stream()
					.filter(telefono->esTelefonoValidaParaModificacion(telefono))
					.collect(Collectors.toList());
			
			for(Telefono telefono : telefonos) {
				List<RegistroCambioMasivo> registrosCambios = obtenerRegistrosCambioModificarTelefono(telefono);
				registrosCambios = registrosCambios
						.stream()
						.filter(registroCambio->registroCambio.ValorNuevo != null)
						.collect(Collectors.toList());
				
				entradaEjecutarCambiosPersona.p_RegistroCambio.addAll(registrosCambios);
			}
		}
		
		if(entradaEjecutarCambiosPersona.p_RegistroCambio.size() > 0) {
			ModificarPersonaTelefonoMasivo modificarPersonaTelefonoMasivo = new ModificarPersonaTelefonoMasivo();
			SalidaEjecutarCambiosPersona salidaEjecutarCambiosPersona = modificarPersonaTelefonoMasivo.logica(con, entradaEjecutarCambiosPersona);
			
			for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
				borradorCliente.MensajesSalida.addAll(salidaEjecutarCambiosPersona.P_OUT_MENSAJES);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoTelefono.modificarTelefonos", fin - inicio);
		
		return borradoresCliente;
	}
	
	public List<DatosClienteDatosGenerales> eliminarTelefonos(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception{
		logger.debug("INICIO::AdministrarEnrolamientoTelefono.eliminarTelefonos");
		long inicio = System.currentTimeMillis();
		
		List<Telefono> telefonosExistentes = obtenerTelefonos(con, borradoresCliente);
		
		EntradaEjecutarCambiosPersona entradaEjecutarCambiosPersona = new EntradaEjecutarCambiosPersona();
		entradaEjecutarCambiosPersona.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaEjecutarCambiosPersona.p_IdModulo = entrada.p_IdModulo;
		entradaEjecutarCambiosPersona.p_IdUsuario = entrada.p_IdUsuario;
		entradaEjecutarCambiosPersona.p_UsaReglasNegocio = "N";
		entradaEjecutarCambiosPersona.p_EsCrearCliente = "S";
		
		for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
			
			List<Telefono> telefonos = borradorCliente.Telefonos
					.stream()
					.filter(telefono->esTelefonoValidaParaModificacion(telefono))
					.collect(Collectors.toList());
			
			for(Telefono telefonoExistente : telefonosExistentes) {
				Telefono telefonoEncontrado = telefonos
						.stream()
						.filter(telefono->telefonoExistente.IdTelefono.equals(telefono.IdTelefonoPersona))
						.findFirst()
						.orElse(null);
				if(telefonoEncontrado == null) {
					RegistroCambioMasivo registroCambio = obtenerRegistrosCambioEliminarTelefono(telefonoExistente);
					entradaEjecutarCambiosPersona.p_RegistroCambio.add(registroCambio);
				}
				
			}
		}
		
		if(entradaEjecutarCambiosPersona.p_RegistroCambio.size() > 0) {
			EliminarPersonaTelefonoMasivo eliminarPersonaTelefonoMasivo = new EliminarPersonaTelefonoMasivo();
			SalidaEjecutarCambiosPersona salidaEjecutarCambiosPersona = eliminarPersonaTelefonoMasivo.logica(con, entradaEjecutarCambiosPersona);
			
			for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
				borradorCliente.MensajesSalida.addAll(salidaEjecutarCambiosPersona.P_OUT_MENSAJES);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoTelefono.eliminarTelefonos", fin - inicio);
		
		return borradoresCliente;
	}
	
	private List<RegistroCambioMasivo> obtenerRegistrosCambioModificarTelefono(Telefono telefono){
		logger.trace("INICIO::AdministrarEnrolamientoTelefono.obtenerRegistrosCambioModificarTelefono");
		long inicio = System.currentTimeMillis();
		
		List<RegistroCambioMasivo> registrosCambio = new ArrayList<>();
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(telefono.IdTelefonoPersona, "IdPais", telefono.IdPais));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(telefono.IdTelefonoPersona, "IdTipoTelefono", telefono.IdTipoTelefono));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(telefono.IdTelefonoPersona, "Numero", telefono.Numero));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(telefono.IdTelefonoPersona, "Anexo", telefono.Anexo));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(telefono.IdTelefonoPersona, "CodigoProposito", telefono.CodigoProposito));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(telefono.IdTelefonoPersona, "Comentario", telefono.Comentario));
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AdministrarEnrolamientoTelefono.obtenerRegistrosCambioModificarTelefono", fin - inicio);
		return registrosCambio;
	}
	
	private RegistroCambioMasivo obtenerRegistrosCambioEliminarTelefono(Telefono telefono){
		logger.trace("INICIO::AdministrarEnrolamientoTelefono.obtenerRegistrosCambioEliminarTelefono");
		long inicio = System.currentTimeMillis();

		RegistroCambioMasivo registroCambio = EnrolamientoUtils.crearRegistroCambio(telefono.IdTelefono, "Borrado", "S");
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AdministrarEnrolamientoTelefono.obtenerRegistrosCambioEliminarTelefono", fin - inicio);
		return registroCambio;
	}
	
	private Boolean esTelefonoValidoParaCreacion(Telefono telefono, Long idTelefonoPrincipal) {
		logger.trace("INICIO::AdministrarEnrolamientoTelefono.esTelefonoValidoParaCreacion");
		long inicio = System.currentTimeMillis();
		Boolean esDireccionElectronicaValidaParaCreacion = !telefono.IdTelefono.equals(idTelefonoPrincipal) && telefono.IdTelefonoPersona.equals(0L);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AdministrarEnrolamientoTelefono.esTelefonoValidoParaCreacion", fin - inicio);
		return esDireccionElectronicaValidaParaCreacion;
	}
	
	private Boolean esTelefonoValidaParaModificacion(Telefono telefono) {
		logger.trace("INICIO::AdministrarEnrolamientoTelefono.esTelefonoValidaParaModificacion");
		long inicio = System.currentTimeMillis();
		Boolean esTelefonoValidaParaModificacion = !telefono.IdTelefonoPersona.equals(0L);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AdministrarEnrolamientoTelefono.esTelefonoValidaParaModificacion", fin - inicio);
		return esTelefonoValidaParaModificacion;
	}
	
	private List<Telefono> obtenerTelefonos(Connection con, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception {
		logger.debug("INICIO::AdministrarEnrolamientoTelefono.obtenerTelefonos");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsPersona = EnrolamientoUtils.obtenerIdsPersona(borradoresCliente);
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("IdPersona", idsPersona);
		
		SQL sql = new SQL();
		List<Telefono> telefonos = sql.query(con, SQL_SELECT_TELEFONO, parametros, Telefono.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoTelefono.obtenerTelefonos", fin - inicio);
		return telefonos;
	}
	
	public List<DatosClienteDatosGenerales> enrolarTelefono(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception{
		logger.debug("INICIO::AdministrarEnrolamientoTelefono.enrolarTelefono");
		long inicio = System.currentTimeMillis();
		
		borradoresCliente = eliminarTelefonos(con, entrada, borradoresCliente);
		borradoresCliente = crearTelefonos(con, entrada, borradoresCliente);
		borradoresCliente = modificarTelefonos(con, entrada, borradoresCliente);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoTelefono.enrolarTelefono", fin - inicio);
		return borradoresCliente;
	}
	
	public List<DatosClienteDatosGenerales> updateTerlefonoPredeterminado(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception{
		logger.debug("INICIO::AdministrarEnrolamientoTelefono.updateDireccionFisicaPredeterminada");
		long inicio = System.currentTimeMillis();
		
		if(borradoresCliente.get(0).DireccionesFisica.size() >0 ) {
			for(Portafolio portafolio : borradoresCliente.get(0).Portafolio) {
				Long idPortafolio = portafolio.IdPortafolio;
				HashMap<String, Object> parametros = new HashMap<String, Object>();
				
				SQL sql = new SQL();
				parametros.put("IdPersona", borradoresCliente.get(0).IdPersona);
				List<TelefonoPredeterminado> IdTelefonoContacto = sql.query(con, SQL_SELECT_TELEFONO_PREDETERMINADO, parametros,TelefonoPredeterminado.class);
				
				if (IdTelefonoContacto.size()>0) {
					SQL.exec(con, SQL_UPDATE_TELEFONO_PREDETERMINADO_PORTAFOLIO.replace("(:IdPortafolio)", Long.toString(idPortafolio)).replace("(:IdTelefonoContacto)",Long.toString(IdTelefonoContacto.get(0).IdTelefonoContacto)));
				}
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoTelefono.updateDireccionFisicaPredeterminada", fin - inicio);
		return borradoresCliente;
	}

}
