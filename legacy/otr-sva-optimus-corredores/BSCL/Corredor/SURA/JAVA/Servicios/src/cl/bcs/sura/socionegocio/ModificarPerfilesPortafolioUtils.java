package cl.bcs.sura.socionegocio;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.soc.CrearSocioNegocio.EntidadCreada;
import cl.bcs.socionegocio.soc.CrearSocioNegocio.Indicador;
import cl.bcs.socionegocio.soc.CrearSocioNegocio.MensajesErrores;
import cl.bcs.socionegocio.soc.CrearSocioNegocio.SocioNegocio;
import cl.bcs.socionegocio.soc.ModificarSocioNegocio.EntidadModificada;
import cl.bcs.socionegocio.wbs.CrearClienteExpress.Mensaje;
import cl.bcs.socionegocio.wbs.WebServiceUtils.EntidadManipulada;
import cl.bcs.sura.socionegocio.pojo.IdsEntidadYPerfiles;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.SQL;

public class ModificarPerfilesPortafolioUtils  {
	
	private static FileLoader loader;
	static final Logger logger = LogManager.getLogger(ModificarPerfilesPortafolioUtils.class);
	private static String SQL_SELECT_PERFILES_COMERCIALES_POR_DOCUMENTOS_IDENTIDAD;
	private static String SQL_SELECT_IDSPORTAFOLIO_IDCLIENTE_POR_DOCUMENTO_IDENTIDAD;
	private static String SQL_UPDATE_PERFILES_PORTAFOLIO;
	private static String SQL_INSERT_INDICADOR_COBRO_CUSTODIA;
	private static String SQL_UPDATE_INDICADOR_COBRO_CUSTODIA;
	private static String SQL_DELETE_TABLA_TEMPORAL;
	private static String SQL_INSERT_TABLA_TEMPORAL;
	private static String SQL_CONSULTAR_PERFILES_FILTRADOS_POR_TABLA_TEMPORAL;
	
	static {
		loader = new FileLoader();
		SQL_SELECT_PERFILES_COMERCIALES_POR_DOCUMENTOS_IDENTIDAD = loader.loadResourceToString("cl/bcs/sura/socionegocio/sql/ModificarPerfilesPortafolioUtils_ConsultarPerfilesComercialesPorDocumentosIdentidad.sql");
		SQL_SELECT_IDSPORTAFOLIO_IDCLIENTE_POR_DOCUMENTO_IDENTIDAD = loader.loadResourceToString("cl/bcs/sura/socionegocio/sql/ModificarPerfilesPortafolioUtils_ConsultarIdsPortafolioIdClientePorDocumentoIdentidad.sql");
		SQL_UPDATE_PERFILES_PORTAFOLIO = loader.loadResourceToString("cl/bcs/sura/socionegocio/sql/ModificarPerfilesPortafolioUtils_ModificarPerfilesPortafolio.sql");
		SQL_INSERT_INDICADOR_COBRO_CUSTODIA = loader.loadResourceToString("cl/bcs/sura/socionegocio/sql/ModificarPerfilesPortafolioUtils_InsertIndicadorCobroCustodia.sql");
		SQL_UPDATE_INDICADOR_COBRO_CUSTODIA = loader.loadResourceToString("cl/bcs/sura/socionegocio/sql/ModificarPerfilesPortafolioUtils_UpdateIndicadorCobroCustodia.sql");
		SQL_DELETE_TABLA_TEMPORAL = loader.loadResourceToString("cl/bcs/sura/socionegocio/sql/ModificarPerfilesPortafolioUtils_BorrarDataTablaTemporalClientesFormasCobro.sql");
		SQL_INSERT_TABLA_TEMPORAL = loader.loadResourceToString("cl/bcs/sura/socionegocio/sql/ModificarPerfilesPortafolioUtils_InsertDataTablaTemporalClientesFormasCobro.sql");		
		SQL_CONSULTAR_PERFILES_FILTRADOS_POR_TABLA_TEMPORAL = loader.loadResourceToString("cl/bcs/sura/socionegocio/sql/ModificarPerfilesPortafolioUtils_ConsultarPerfilesComercialesFiltradosPorTablaTemporal.sql");
	
	
	}
	
	public static class ObjetoIndicador extends BcsPojo
	{
        public String	ValorIndicador;
		public Long 	IdEntidad;
		
		public ObjetoIndicador() {
			this.ValorIndicador = "";
			this.IdEntidad = 0L;
		}
	}
	
	public List<IdsEntidadYPerfiles> consultarIdsPortafolioIdClientePorDocumentoIdentidad(Connection conn, List<String> documentosIdentidad) throws Exception {
		logger.trace("INICIO::ModificarPerfilesPortafolioLogicaSuraUtils.consultarIdPortafolioIdClientePorDocumentoIdentidad");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
	    parametros.put("DocumentoIdentidad", documentosIdentidad);
	    
	    SQL sql = new SQL();
	    List<IdsEntidadYPerfiles> ids = sql.query(conn, SQL_SELECT_IDSPORTAFOLIO_IDCLIENTE_POR_DOCUMENTO_IDENTIDAD, parametros, IdsEntidadYPerfiles.class);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarPerfilesPortafolioLogicaSuraUtils.consultarIdPortafolioIdClientePorDocumentoIdentidad", fin - inicio);
		return ids;
	}
	
	public void modificarPerfilesPortafolio(Connection conn, List<IdsEntidadYPerfiles> perfiles) throws Exception {
		logger.trace("INICIO::ModificarPerfilesPortafolioLogicaSuraUtils.modificarPerfilesPortafolio");
		long inicio = System.currentTimeMillis();
		
	    SQL sql = new SQL();
	    sql.batch(conn, SQL_UPDATE_PERFILES_PORTAFOLIO, perfiles, IdsEntidadYPerfiles.class);	
	    
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarPerfilesPortafolioLogicaSuraUtils.modificarPerfilesPortafolio", fin - inicio);
	}
	
	public void ingresarIndicadoresCobroCustodia(Connection conn, Indicador indicador, List<IdsEntidadYPerfiles> ids) throws Exception {
		logger.trace("INICIO::ModificarPerfilesPortafolioLogicaSuraUtils.ingresarIndicadoresCobroCustodia");
		long inicio = System.currentTimeMillis();
		
		List<ObjetoIndicador> listObj = ids.parallelStream().filter(id-> id.IdIndicador == 0 ).map(cliente->{
			ObjetoIndicador objetoIndicador = new ObjetoIndicador();
			objetoIndicador.IdEntidad = cliente.IdPortafolio;
			objetoIndicador.ValorIndicador = indicador.ValorIndicador;
			return objetoIndicador;
		}).collect(Collectors.toList());
		
	    SQL sql = new SQL();
	    sql.batch(conn, SQL_INSERT_INDICADOR_COBRO_CUSTODIA, listObj, ObjetoIndicador.class);	
	    
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarPerfilesPortafolioLogicaSuraUtils.ingresarIndicadoresCobroCustodia", fin - inicio);
	}
	
	public void modificarIndicadoresCobroCustodia(Connection conn, List<IdsEntidadYPerfiles> ids) throws Exception {
		logger.info("INICIO::ModificarPerfilesPortafolioLogicaSuraUtils.modificarIndicadoresCobroCustodia");
		long inicioServicio = System.currentTimeMillis();
		
		List<ObjetoIndicador> listObj = ids.parallelStream().filter(id-> id.IdIndicador != 0 && "S".equals(id.ActualizarFormaCobro) ).map(cliente->{
			ObjetoIndicador objetoIndicador = new ObjetoIndicador();
			objetoIndicador.IdEntidad = cliente.IdIndicador;
			objetoIndicador.ValorIndicador = cliente.IdFormaCobro;
			return objetoIndicador;
		}).collect(Collectors.toList());
		
		SQL sql = new SQL();
	    sql.batch(conn, SQL_UPDATE_INDICADOR_COBRO_CUSTODIA, listObj, ObjetoIndicador.class);	
	    
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarPerfilesPortafolioLogicaSuraUtils.modificarIndicadoresCobroCustodia", finServicio - inicioServicio);
	}
	
	public List<IdsEntidadYPerfiles> validarIndicadorFormaCobroPortafolioApv(Connection con, List<IdsEntidadYPerfiles> idsEntidadYPerfiles) throws Exception {
		
		logger.debug("INICIO::ModificarPerfilesPortafolioLogicaSuraUtils.validarIndicadorFormaCobroPortafolioApv");
		long inicio = System.currentTimeMillis();
			
		
		for( IdsEntidadYPerfiles idEntidadYPerfiles:  idsEntidadYPerfiles) {
			if( "PROPOSITOAPV".equals(idEntidadYPerfiles.Proposito) )
				idEntidadYPerfiles.ActualizarFormaCobro = "N";
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarPerfilesPortafolioLogicaSuraUtils.validarIndicadorFormaCobroPortafolioApv", fin - inicio);
		return idsEntidadYPerfiles;
		
	}
	
	public EntidadCreada agregarMensajeError(Connection conn, Long idCliente, Long idEmpresa, HashMap<String, String> parametrosMensaje, String codigoMensaje) throws Exception{
		logger.info("INICIO::ModificarPerfilesPortafolioLogicaSuraUtils.agregarMensajeError");
		long inicioServicio = System.currentTimeMillis();
		
		List<MensajesErrores> listMensajesErrores = new ArrayList<MensajesErrores>();
		EntidadCreada entidadCreada = new EntidadCreada();
		
		PlataformaUtils.MensajeSalida ms = new PlataformaUtils.MensajeSalida(idCliente, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, codigoMensaje, parametrosMensaje, conn, idEmpresa);
		MensajesErrores mensaje = new MensajesErrores();
		mensaje.IdInterno = 0L;
		mensaje.Codigo = ms.codigo;
		mensaje.Mensaje = ms.descripcion;
		mensaje.TipoMensaje = ms.tipo;
		
		listMensajesErrores.add(mensaje);
		entidadCreada.TipoEntidad = parametrosMensaje.get("TipoEntidad");
		entidadCreada.IdEntidad = Long.parseLong(parametrosMensaje.get("IdEntidad"));
		entidadCreada.Estado = "";
		entidadCreada.Nuevo = "";
		entidadCreada.Mensajes = listMensajesErrores;
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarPerfilesPortafolioLogicaSuraUtils.agregarMensajeError", finServicio - inicioServicio);
		return entidadCreada;
	}
	
	public cl.bcs.socionegocio.wbs.CrearClienteFullWS.EntidadCreada agregarMensajeErrorWSBase(Connection conn, Long idCliente, Long idEmpresa, HashMap<String, String> parametrosMensaje, String codigoMensaje) throws Exception{
		logger.info("INICIO::ModificarPerfilesPortafolioLogicaSuraUtils.agregarMensajeErrorWSBase");
		long inicioServicio = System.currentTimeMillis();
		
		List<Mensaje> listMensajesErrores = new ArrayList<Mensaje>();
		cl.bcs.socionegocio.wbs.CrearClienteFullWS.EntidadCreada entidadCreada = new cl.bcs.socionegocio.wbs.CrearClienteFullWS.EntidadCreada();
		
		PlataformaUtils.MensajeSalida ms = new PlataformaUtils.MensajeSalida(idCliente, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, codigoMensaje, parametrosMensaje, conn, idEmpresa);
		Mensaje mensaje = new Mensaje();
		mensaje.IdInterno = 0L;
		mensaje.Codigo = ms.codigo;
		mensaje.Mensaje = ms.descripcion;
		mensaje.TipoMensaje = ms.tipo;
		
		listMensajesErrores.add(mensaje);
		entidadCreada.TipoEntidad = parametrosMensaje.get("TipoEntidad");
		entidadCreada.IdEntidad = Long.parseLong(parametrosMensaje.get("IdEntidad"));
		entidadCreada.Mensajes = listMensajesErrores;
		entidadCreada.IdInterno = 0L;
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarPerfilesPortafolioLogicaSuraUtils.agregarMensajeErrorWSBase", finServicio - inicioServicio);
		return entidadCreada;
	}
	
	public EntidadModificada agregarMensajeErrorModificacion(Connection conn, Long idCliente, Long idEmpresa, HashMap<String, String> parametrosMensaje, String codigoMensaje) throws Exception{
		logger.info("INICIO::ModificarPerfilesPortafolioLogicaSuraUtils.agregarMensajeErrorModificacion");
		long inicioServicio = System.currentTimeMillis();
		
		List<cl.bcs.socionegocio.soc.ModificarSocioNegocio.MensajesErrores> listMensajesErrores = new ArrayList<cl.bcs.socionegocio.soc.ModificarSocioNegocio.MensajesErrores>();
		EntidadModificada entidadModificada = new EntidadModificada();
		
		PlataformaUtils.MensajeSalida ms = new PlataformaUtils.MensajeSalida(idCliente, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, codigoMensaje, parametrosMensaje, conn, idEmpresa);
		cl.bcs.socionegocio.soc.ModificarSocioNegocio.MensajesErrores mensaje = new cl.bcs.socionegocio.soc.ModificarSocioNegocio.MensajesErrores();
		mensaje.IdInterno = 0L;
		mensaje.Codigo = ms.codigo;
		mensaje.Mensaje = ms.descripcion;
		mensaje.TipoMensaje = ms.tipo;
		
		listMensajesErrores.add(mensaje);
		entidadModificada.TipoEntidad = parametrosMensaje.get("TipoEntidad");
		entidadModificada.IdEntidad = Long.parseLong(parametrosMensaje.get("IdEntidad"));
		entidadModificada.Mensajes = listMensajesErrores;
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarPerfilesPortafolioLogicaSuraUtils.agregarMensajeErrorModificacion", finServicio - inicioServicio);
		return entidadModificada;
	}

	public EntidadManipulada agregarMensajeErrorModificacionCliente(Connection conn, Long idCliente, Long idEmpresa, HashMap<String, String> parametrosMensaje, String codigoMensaje) throws Exception{
		logger.info("INICIO::ModificarPerfilesPortafolioLogicaSuraUtils.agregarMensajeErrorModificacionCliente");
		long inicioServicio = System.currentTimeMillis();
		
		List<MensajeSalida> listMensajesErrores = new ArrayList<MensajeSalida>();
		EntidadManipulada entidadManipulada = new EntidadManipulada();
		
		MensajeSalida mensaje = new PlataformaUtils.MensajeSalida(idCliente, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, codigoMensaje, parametrosMensaje, conn, idEmpresa);
		
		listMensajesErrores.add(mensaje);
		entidadManipulada.TipoEntidad = parametrosMensaje.get("TipoEntidad");
		entidadManipulada.IdEntidad = Long.parseLong(parametrosMensaje.get("IdEntidad"));
		entidadManipulada.Mensajes = listMensajesErrores;
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarPerfilesPortafolioLogicaSuraUtils.agregarMensajeErrorModificacionCliente", finServicio - inicioServicio);
		return entidadManipulada;
	}
			
	public List<IdsEntidadYPerfiles> obtenerEntidadesYPerfilesCompletas(Connection conn, List<IdsEntidadYPerfiles> ids) throws Exception {
		logger.info("INICIO::ModificarPerfilesPortafolioLogicaSuraUtils.obtenerEntidadesYPerfilesCompletas");
		long inicioServicio = System.currentTimeMillis();
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		
		List<String> documentos = ids.parallelStream().map(id -> {
			return id.DocumentoIdentidad;
		}).collect(Collectors.toList());
		
	    parametros.put("DocumentoIdentidad", documentos);
	    
		SQL sql = new SQL();
		List<IdsEntidadYPerfiles> entidadesPerfiles = sql.query(conn, SQL_SELECT_PERFILES_COMERCIALES_POR_DOCUMENTOS_IDENTIDAD, parametros, IdsEntidadYPerfiles.class);
		
		entidadesPerfiles.parallelStream().forEach(entidad -> {
			String formaCobro = ids.parallelStream().filter( id -> id.IdPortafolio.equals(entidad.IdPortafolio)).findFirst().get().IdFormaCobro;
			entidad.IdFormaCobro = entidad.IdFormaCobro.isEmpty() ? formaCobro : entidad.IdFormaCobro;
		});
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarPerfilesPortafolioLogicaSuraUtils.obtenerEntidadesYPerfilesCompletas", finServicio - inicioServicio);
		return entidadesPerfiles;
	}
	
	public List<IdsEntidadYPerfiles> obtenerEntidadesParaInsertarYModificarEnWSSociosNegocio(Connection conn, List<SocioNegocio> sociosNegocio, List<IdsEntidadYPerfiles> ids) throws Exception {
		logger.info("INICIO::ModificarPerfilesPortafolioLogicaSuraUtils.obtenerEntidadesParaInsertarYModificarEnWSSociosNegocio");
		long inicioServicio = System.currentTimeMillis();
		
		List<IdsEntidadYPerfiles> listaPerfiles = new ArrayList<IdsEntidadYPerfiles>();
		
		for(SocioNegocio socio : sociosNegocio) {
			List<String> listadoDocumentos = new ArrayList<String>();
			listadoDocumentos.add(socio.DocumentoIdentidad);
			
			List<IdsEntidadYPerfiles> perfiles = ids.parallelStream().filter(id -> listadoDocumentos.contains(id.DocumentoIdentidad)).collect(Collectors.toList());
			if(!socio.IndicadorPortafolio.parallelStream().filter(ind -> ind.CodigoTipoIndicador.equals("COBROCUST")).collect(Collectors.toList()).isEmpty() && !perfiles.isEmpty()) {
				
				Indicador indicadorFormaCobro = new Indicador();
				indicadorFormaCobro.ValorIndicador = socio.IndicadorPortafolio.parallelStream().filter(ind -> ind.CodigoTipoIndicador.equals("COBROCUST")).findFirst().get().ValorIndicador;
				indicadorFormaCobro.CodigoTipoIndicador = socio.IndicadorPortafolio.parallelStream().filter(ind -> ind.CodigoTipoIndicador.equals("COBROCUST")).findFirst().get().CodigoTipoIndicador;
				
				perfiles.parallelStream().forEach(perfil ->{ perfil.IdFormaCobro = indicadorFormaCobro.ValorIndicador;});
				
			}
			listaPerfiles.addAll(perfiles);
		}
		
		generarTablaTemporal(conn, listaPerfiles);
		
		listaPerfiles = obtenerPerfilesFiltradosPorTablaTemporal(conn,sociosNegocio).parallelStream().filter(perf -> !perf.IdFormaCobro.isEmpty()).collect(Collectors.toList());
			    
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarPerfilesPortafolioLogicaSuraUtils.obtenerEntidadesParaInsertarYModificarEnWSSociosNegocio", finServicio - inicioServicio);
		return listaPerfiles;
	}
	
	public void generarTablaTemporal(Connection conn, List<IdsEntidadYPerfiles> listaPerfiles) throws Exception {
		logger.info("INICIO::ModificarPerfilesPortafolioLogicaSuraUtils.generarTablaTemporal");
		long inicioServicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(conn, SQL_INSERT_TABLA_TEMPORAL, listaPerfiles, IdsEntidadYPerfiles.class);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarPerfilesPortafolioLogicaSuraUtils.generarTablaTemporal", finServicio - inicioServicio);
	}
	
	public List<IdsEntidadYPerfiles> obtenerPerfilesFiltradosPorTablaTemporal(Connection conn, List<SocioNegocio> sociosNegocio) throws Exception{
		logger.info("INICIO::ModificarPerfilesPortafolioLogicaSuraUtils.obtenerPerfilesFiltradosPorTablaTemporal");
		long inicioServicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		List<IdsEntidadYPerfiles> listaPerfiles = new ArrayList<IdsEntidadYPerfiles>();
		List<String> listadoDocumentos = new ArrayList<String>();
		
		sociosNegocio.parallelStream().forEach(soc -> {
			listadoDocumentos.add(soc.DocumentoIdentidad);
		});
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		
	    listaPerfiles = sql.query(conn, SQL_CONSULTAR_PERFILES_FILTRADOS_POR_TABLA_TEMPORAL, parametros,IdsEntidadYPerfiles.class);
		
	    sql.batch(conn, SQL_DELETE_TABLA_TEMPORAL, listaPerfiles, IdsEntidadYPerfiles.class);
	    
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarPerfilesPortafolioLogicaSuraUtils.obtenerPerfilesFiltradosPorTablaTemporal", finServicio - inicioServicio);
		return listaPerfiles;
	}
}
