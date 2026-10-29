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
import cl.bcs.socionegocio.enr.pojo.DireccionFisica;
import cl.bcs.socionegocio.enr.pojo.Portafolio;
import cl.bcs.socionegocio.per.CrearDireccionFisicaMasivo;
import cl.bcs.socionegocio.per.CrearDireccionFisicaMasivo.EntradaCrearDireccionFisicaMasivo;
import cl.bcs.socionegocio.per.CrearDireccionFisicaMasivo.SalidaCrearDireccionFisicaMasivo;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntradaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.SalidaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EliminarPersonaDireccionFisicaMasivo;
import cl.bcs.socionegocio.per.ModificarPersonaDireccionFisicaMasivo;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.socionegocio.wbs.CrearClienteFullWS.DireccionElectronica;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AdministrarEnrolamientoDireccionFisica {
	
	static final Logger logger = LogManager.getLogger(AdministrarEnrolamientoDireccionFisica.class);
	
	private static FileLoader loader;
	private static final String SQL_SELECT_DIRECCION_FISICA;
	private static final String SQL_UPDATE_DIRECCION_FISICA_PREDETERMINADA_PORTAFOLIO;
	private static final String SQL_SELECT_DIRECCION_FISICA_PREDETERMINADA;
	static {
		loader = new FileLoader();
		SQL_SELECT_DIRECCION_FISICA = loader.loadResourceToString("cl/bcs/socionegocio/enr/sql/AdministrarEnrolamientoDireccionFisica_SelectDireccionFisica.sql");
		SQL_UPDATE_DIRECCION_FISICA_PREDETERMINADA_PORTAFOLIO = loader.loadResourceToString("cl/bcs/socionegocio/enr/sql/AdministrarEnrolamientoDireccionFisica_UpdatetDireccionFisicaPredeterminadaPortafolio.sql");
		SQL_SELECT_DIRECCION_FISICA_PREDETERMINADA = loader.loadResourceToString("cl/bcs/socionegocio/enr/sql/AdministrarEnrolamientoDireccionFisica_SelectDireccionFisicaPredeterminada.sql");
	}
	public static class DireccionFisicaPredeterminada {
		public Long IdDireccionFisica;
	}
	
	public List<DatosClienteDatosGenerales> crearDireccionesFisica(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception {
		logger.debug("INICIO::AdministrarEnrolamientoDireccionFisica.crearDireccionesFisica");
		long inicio = System.currentTimeMillis();
		
		EntradaCrearDireccionFisicaMasivo entradaDireccionFisicaMasivo = new EntradaCrearDireccionFisicaMasivo();
		entradaDireccionFisicaMasivo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaDireccionFisicaMasivo.p_IdModulo = entrada.p_IdModulo;
		entradaDireccionFisicaMasivo.p_IdUsuario = entrada.p_IdUsuario;
		entradaDireccionFisicaMasivo.p_UsaReglasNegocio = "N";
		
		for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
			List<DireccionFisica> direccionesFisicas = borradorCliente.DireccionesFisica
					.stream()
					.filter(direccionFisica->esDireccionFisicaValidaParaCreacion(direccionFisica, borradorCliente.Enrolamiento.IdDireccionFisicaPrincipal, entrada.p_IdTipoCanalIngreso))
					.collect(Collectors.toList());
			for(DireccionFisica direccionFisica : direccionesFisicas) {
				CrearDireccionFisicaMasivo.DireccionFisica entradaDireccionFisica = new CrearDireccionFisicaMasivo.DireccionFisica();
				entradaDireccionFisica.IdDireccionFisica = direccionFisica.IdDireccionFisica;
				entradaDireccionFisica.IdInterno = direccionFisica.IdDireccionFisica;
				entradaDireccionFisica.Calle = direccionFisica.Calle;
				entradaDireccionFisica.Numero = direccionFisica.Numero;
				entradaDireccionFisica.Departamento = direccionFisica.Departamento;
				entradaDireccionFisica.Sector = direccionFisica.Sector;
				entradaDireccionFisica.CodigoPostal = direccionFisica.CodigoPostal;
				entradaDireccionFisica.IdCiudad = direccionFisica.IdCiudad;
				entradaDireccionFisica.IdDivisionPais = direccionFisica.IdDivisionPais;
				entradaDireccionFisica.CodigoProposito = direccionFisica.CodigoProposito;
				entradaDireccionFisica.Comentario = direccionFisica.Comentario;
				entradaDireccionFisica.IdEstado = "ACT";
				entradaDireccionFisica.IdPersona = borradorCliente.IdPersona;
				entradaDireccionFisica.CodigoCompuesto = direccionFisica.CodigoCompuesto;
				entradaDireccionFisica.Predeterminado = direccionFisica.Predeterminado;
				entradaDireccionFisica.IdUsuarioCreador = entrada.p_IdUsuario;
				
				entradaDireccionFisicaMasivo.p_DireccionesFisica.add(entradaDireccionFisica);
				
			}
		}
		
		if(entradaDireccionFisicaMasivo.p_DireccionesFisica.size() > 0) {
			CrearDireccionFisicaMasivo crearDireccionFisicaMasivo = new CrearDireccionFisicaMasivo();
			SalidaCrearDireccionFisicaMasivo salidaDireccionFisicaMasivo = crearDireccionFisicaMasivo.logica(con, entradaDireccionFisicaMasivo);
			
			for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
				borradorCliente.MensajesSalida.addAll(salidaDireccionFisicaMasivo.P_OUT_MENSAJES);
			}
		}
		
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoDireccionFisica.crearDireccionesFisica", fin - inicio);
		return borradoresCliente;
	}
	
	public List<DatosClienteDatosGenerales> modificarDireccionesFisica(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception{
		logger.debug("INICIO::AdministrarEnrolamientoDireccionFisica.modificarDireccionesFisica");
		long inicio = System.currentTimeMillis();
		
		EntradaEjecutarCambiosPersona entradaEjecutarCambiosPersona = new EntradaEjecutarCambiosPersona();
		entradaEjecutarCambiosPersona.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaEjecutarCambiosPersona.p_IdModulo = entrada.p_IdModulo;
		entradaEjecutarCambiosPersona.p_IdUsuario = entrada.p_IdUsuario;
		entradaEjecutarCambiosPersona.p_UsaReglasNegocio = "N";
		
		for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
			
			List<DireccionFisica> direccionesFisica = borradorCliente.DireccionesFisica
					.stream()
					.filter(direccionFisica->esDireccionFisicaValidaParaModificacion(direccionFisica))
					.collect(Collectors.toList());
			
			for(DireccionFisica direccionFisica : direccionesFisica) {
				List<RegistroCambioMasivo> registrosCambios = obtenerRegistrosCambioModificarDireccionFisica(direccionFisica);
				registrosCambios = registrosCambios
						.stream()
						.filter(registroCambio->registroCambio.ValorNuevo != null)
						.collect(Collectors.toList());
				
				entradaEjecutarCambiosPersona.p_RegistroCambio.addAll(registrosCambios);
			}
		}
		
		if(entradaEjecutarCambiosPersona.p_RegistroCambio.size() > 0) {
			ModificarPersonaDireccionFisicaMasivo modificarPersonaDireccionFisicaMasivo = new ModificarPersonaDireccionFisicaMasivo();
			SalidaEjecutarCambiosPersona salidaEjecutarCambiosPersona = modificarPersonaDireccionFisicaMasivo.logica(con, entradaEjecutarCambiosPersona);
			
			for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
				borradorCliente.MensajesSalida.addAll(salidaEjecutarCambiosPersona.P_OUT_MENSAJES);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoDireccionFisica.modificarDireccionesFisica", fin - inicio);
		
		return borradoresCliente;
	}
	
	public List<DatosClienteDatosGenerales> eliminarDireccionFisica(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception{
		logger.debug("INICIO::AdministrarEnrolamientoDireccionFisica.eliminarDireccionFisica");
		long inicio = System.currentTimeMillis();
		
		List<DireccionFisica> direccionesFisicaExistentes = obtenerDireccionesFisicas(con, borradoresCliente);
		
		EntradaEjecutarCambiosPersona entradaEjecutarCambiosPersona = new EntradaEjecutarCambiosPersona();
		entradaEjecutarCambiosPersona.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaEjecutarCambiosPersona.p_IdModulo = entrada.p_IdModulo;
		entradaEjecutarCambiosPersona.p_IdUsuario = entrada.p_IdUsuario;
		entradaEjecutarCambiosPersona.p_UsaReglasNegocio = "N";
		entradaEjecutarCambiosPersona.p_EsCrearCliente = "S";
		
		for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
			
			List<DireccionFisica> direccionesFisica = borradorCliente.DireccionesFisica
					.stream()
					.filter(direccionFisica->esDireccionFisicaValidaParaModificacion(direccionFisica))
					.collect(Collectors.toList());
			
			for(DireccionFisica direccionFisicaExistente : direccionesFisicaExistentes) {
				DireccionFisica direccionFisicaEncontrado = direccionesFisica
						.stream()
						.filter(direccionFisica->direccionFisicaExistente.IdDireccionFisica.equals(direccionFisica.IdDireccionFisicaPersona))
						.findFirst()
						.orElse(null);
				if(direccionFisicaEncontrado == null) {
					RegistroCambioMasivo registroCambio = obtenerRegistrosCambioEliminarDireccionFisica(direccionFisicaExistente);
					entradaEjecutarCambiosPersona.p_RegistroCambio.add(registroCambio);
				}
				
			}
		}
		
		if(entradaEjecutarCambiosPersona.p_RegistroCambio.size() > 0) {
			EliminarPersonaDireccionFisicaMasivo eliminarPersonaDireccionFisicaMasivo = new EliminarPersonaDireccionFisicaMasivo();
			SalidaEjecutarCambiosPersona salidaEjecutarCambiosPersona = eliminarPersonaDireccionFisicaMasivo.logica(con, entradaEjecutarCambiosPersona);
			
			for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
				borradorCliente.MensajesSalida.addAll(salidaEjecutarCambiosPersona.P_OUT_MENSAJES);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoDireccionFisica.eliminarDireccionFisica", fin - inicio);
		
		return borradoresCliente;
	}
		
	private List<RegistroCambioMasivo> obtenerRegistrosCambioModificarDireccionFisica(DireccionFisica direccionFisica){
		logger.trace("INICIO::AdministrarEnrolamientoDireccionFisica.obtenerRegistrosCambioModificarDireccionFisica");
		long inicio = System.currentTimeMillis();
		
		List<RegistroCambioMasivo> registrosCambio = new ArrayList<>();
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(direccionFisica.IdDireccionFisicaPersona, "Calle", direccionFisica.Calle));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(direccionFisica.IdDireccionFisicaPersona, "Numero", direccionFisica.Numero));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(direccionFisica.IdDireccionFisicaPersona, "Departamento", direccionFisica.Departamento));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(direccionFisica.IdDireccionFisicaPersona, "CodigoPostal", direccionFisica.CodigoPostal));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(direccionFisica.IdDireccionFisicaPersona, "IdCiudad", direccionFisica.IdCiudad));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(direccionFisica.IdDireccionFisicaPersona, "CodigoProposito", direccionFisica.CodigoProposito));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(direccionFisica.IdDireccionFisicaPersona, "Comentario", direccionFisica.Comentario));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(direccionFisica.IdDireccionFisicaPersona, "IdDivisionPais", direccionFisica.IdDivisionPais));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(direccionFisica.IdDireccionFisicaPersona, "CodigoCompuesto", direccionFisica.CodigoCompuesto));
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AdministrarEnrolamientoDireccionFisica.obtenerRegistrosCambioModificarDireccionFisica", fin - inicio);
		return registrosCambio;
	}
	
	private RegistroCambioMasivo obtenerRegistrosCambioEliminarDireccionFisica(DireccionFisica direccionFisica){
		logger.trace("INICIO::AdministrarEnrolamientoDireccionFisica.obtenerRegistrosCambioEliminarDireccionFisica");
		long inicio = System.currentTimeMillis();

		RegistroCambioMasivo registroCambio = EnrolamientoUtils.crearRegistroCambio(direccionFisica.IdDireccionFisica, "Borrado", "S");
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AdministrarEnrolamientoDireccionFisica.obtenerRegistrosCambioEliminarDireccionFisica", fin - inicio);
		return registroCambio;
	}
	
	private Boolean esDireccionFisicaValidaParaCreacion(DireccionFisica direccionFisica, Long idDireccionFisicaPrincipal, String idTipoCanalIngreso) {
		logger.trace("INICIO::AdministrarEnrolamientoDireccionFisica.esDireccionFisicaValidaParaCreacion");
		long inicio = System.currentTimeMillis();
		Boolean esDireccionFisicaValidaParaCreacion = false;
		
		esDireccionFisicaValidaParaCreacion = !direccionFisica.IdDireccionFisica.equals(idDireccionFisicaPrincipal) && direccionFisica.IdDireccionFisicaPersona.equals(0L);
		 
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AdministrarEnrolamientoDireccionFisica.esDireccionFisicaValidaParaCreacion", fin - inicio);
		return esDireccionFisicaValidaParaCreacion;
	}
	
	private Boolean esDireccionFisicaValidaParaModificacion(DireccionFisica direccionFisica) {
		logger.trace("INICIO::AdministrarEnrolamientoDireccionFisica.esDireccionFisicaValidaParaModificacion");
		long inicio = System.currentTimeMillis();
		Boolean esDireccionFisicaValidaParaModificacion = !direccionFisica.IdDireccionFisicaPersona.equals(0L);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AdministrarEnrolamientoDireccionFisica.esDireccionFisicaValidaParaModificacion", fin - inicio);
		return esDireccionFisicaValidaParaModificacion;
	}
	
	private List<DireccionFisica> obtenerDireccionesFisicas(Connection con, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception {
		logger.debug("INICIO::AdministrarEnrolamientoDireccionFisica.obtenerDireccionesFisicas");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsPersona = EnrolamientoUtils.obtenerIdsPersona(borradoresCliente);
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("IdPersona", idsPersona);
		
		SQL sql = new SQL();
		List<DireccionFisica> direccionesFisicas = sql.query(con, SQL_SELECT_DIRECCION_FISICA, parametros, DireccionFisica.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoDireccionFisica.obtenerDireccionesFisicas", fin - inicio);
		return direccionesFisicas;
	}
	
	public List<DatosClienteDatosGenerales> enrolarDireccionFisica(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception{
		logger.debug("INICIO::AdministrarEnrolamientoDireccionFisica.enrolarDireccionFisica");
		long inicio = System.currentTimeMillis();
		
		borradoresCliente = eliminarDireccionFisica(con, entrada, borradoresCliente);
		borradoresCliente = crearDireccionesFisica(con, entrada, borradoresCliente);
		borradoresCliente = modificarDireccionesFisica(con, entrada, borradoresCliente);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoDireccionFisica.enrolarDireccionFisica", fin - inicio);
		return borradoresCliente;
	}
	
	public List<DatosClienteDatosGenerales> updateDireccionFisicaPredeterminada(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception{
		logger.debug("INICIO::AdministrarEnrolamientoDireccionFisica.updateDireccionFisicaPredeterminada");
		long inicio = System.currentTimeMillis();
		
		if(borradoresCliente.get(0).DireccionesFisica.size() >0 ) {
			for(Portafolio portafolio : borradoresCliente.get(0).Portafolio) {
				Long idPortafolio = portafolio.IdPortafolio;
				HashMap<String, Object> parametros = new HashMap<String, Object>();
				
				SQL sql = new SQL();
				parametros.put("IdPersona", borradoresCliente.get(0).IdPersona);
				List<DireccionFisicaPredeterminada> idDireccionFisicaPredeterminada = sql.query(con, SQL_SELECT_DIRECCION_FISICA_PREDETERMINADA, parametros,DireccionFisicaPredeterminada.class);
				
				if (idDireccionFisicaPredeterminada.size()>0) {
					SQL.exec(con, SQL_UPDATE_DIRECCION_FISICA_PREDETERMINADA_PORTAFOLIO.replace("(:IdPortafolio)", Long.toString(idPortafolio)).replace("(:IdDireccionEnvioCorrespondencia)",Long.toString(idDireccionFisicaPredeterminada.get(0).IdDireccionFisica)));
				}
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoDireccionFisica.updateDireccionFisicaPredeterminada", fin - inicio);
		return borradoresCliente;
	}

}
