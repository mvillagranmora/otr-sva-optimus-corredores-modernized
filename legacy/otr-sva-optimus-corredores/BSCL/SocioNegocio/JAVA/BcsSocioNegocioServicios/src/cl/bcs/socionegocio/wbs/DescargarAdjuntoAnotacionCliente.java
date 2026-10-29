package cl.bcs.socionegocio.wbs;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.OpenEJBEncryption;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.por.ConsultarAnotacionesClientePortafolio;
import cl.bcs.socionegocio.por.ConsultarAnotacionesClientePortafolio.AtributoAnotacion;
import cl.bcs.socionegocio.por.ConsultarAnotacionesClientePortafolio.ClienteCodigoPortafolio;
import cl.bcs.socionegocio.por.ConsultarAnotacionesClientePortafolio.EntradaConsultarAnotacionesClientePortafolio;
import cl.bcs.socionegocio.por.ConsultarAnotacionesClientePortafolio.SalidaConsultarAnotacionesClientePortafolio;
import cl.bcs.socionegocio.por.pojo.AnotacionClientePortafolio;
import cl.bcs.socionegocio.wbs.pojo.AdjuntoAnotacion;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class DescargarAdjuntoAnotacionCliente implements JavaCall{
	static final Logger logger = LogManager.getLogger(DescargarAdjuntoAnotacionCliente.class);
	
	private final String SQL_SELECT_ARCHIVOS_ANOTACIONES = loadResourceToString("cl/bcs/socionegocio/wbs/sql/DescargarAdjuntoAnotacionCliente_SelectAdjuntoAnotacionesCliente.sql");
	private final String SQL_SELECT_ARCHIVOS_ANOTACIONES_PORTAFOLIO_OPTIMUS = loadResourceToString("cl/bcs/socionegocio/wbs/sql/DescargarAdjuntoAnotacionCliente_SelectAdjuntoAnotacionesPortafolioOptimus.sql");
	private final String SQL_SELECT_ARCHIVOS_ANOTACIONES_PORTAFOLIO_SEBRA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/DescargarAdjuntoAnotacionCliente_SelectAdjuntoAnotacionesPortafolioSebra.sql");
	
	public class EntradaDescargarAdjuntoAnotacionCliente extends BcsPojo {
		public Long p_IdEmpresa;
		public String p_IdUsuario;
		public String p_ClaveUsuario;
		public Long p_IdPortafolio;
		public List<Long> p_Anotaciones;
		
		public EntradaDescargarAdjuntoAnotacionCliente () {
			this.p_IdEmpresa = 0L;
			this.p_IdUsuario = "";
			this.p_ClaveUsuario = "";
			this.p_IdPortafolio = 0L;
			this.p_Anotaciones = new ArrayList<Long>();
		}
	}

	public class SalidaDescargarAdjuntoAnotacionCliente extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<ArchivoCodificado> P_OUT_RESULTADO;
		
		public SalidaDescargarAdjuntoAnotacionCliente (){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<ArchivoCodificado>();
		}
	}
	
	public static class ArchivoBinario extends AdjuntoAnotacion {
		public byte[] Archivo;
	}
	
	public static class ArchivoCodificado extends AdjuntoAnotacion {
		public String Archivo;
	}
	

	public static class ResultadoCodificarArchivoBinario  extends BcsPojo {
		public List<MensajeSalida> Mensajes;
		public List<ArchivoCodificado> ArchivosCodificados;
		
		public ResultadoCodificarArchivoBinario() {
			this.Mensajes = new ArrayList<MensajeSalida>();
			this.ArchivosCodificados = new ArrayList<ArchivoCodificado>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaDescargarAdjuntoAnotacionCliente entrada = gson.fromJson(in, EntradaDescargarAdjuntoAnotacionCliente.class);
		SalidaDescargarAdjuntoAnotacionCliente salida = logica(conn, entrada);
		JsonParser parser = new JsonParser();

		return parser.parse(gson.toJson(salida)).getAsJsonObject();
	}

	public SalidaDescargarAdjuntoAnotacionCliente logica(Connection conn, EntradaDescargarAdjuntoAnotacionCliente entrada)  throws Exception {
		logger.info("INICIO::DescargarAdjuntoAnotacionCliente");
		long inicio = System.currentTimeMillis();
		
		SalidaDescargarAdjuntoAnotacionCliente salida = new SalidaDescargarAdjuntoAnotacionCliente();
		List<ArchivoBinario> archivos = new ArrayList<ArchivoBinario>();
		
		if(entrada.p_IdPortafolio > 0L) {
			archivos = obtenerArchivosAnotacionesPortafolio(conn, entrada);
		}else {
			archivos = obtenerArchivosAnotacionesCliente(conn, entrada.p_Anotaciones);			
		}
		
		HashMap<String, String> params = new HashMap<>(1);
		for(Long idAnotacion : entrada.p_Anotaciones) {
			Boolean anotacionExiste = archivos.parallelStream().anyMatch(archivo -> idAnotacion.equals(archivo.IdAnotacion));
			if(!anotacionExiste) {
				params.put("IdAnotacion", String.valueOf(idAnotacion));
				salida.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(idAnotacion, MensajeSalida.MENSAJE_ERROR, "UI_ANOTACION_NO_EXISTE", params, conn, entrada.p_IdEmpresa));
				params.clear();
			}
		}
		
		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
		if(salida.P_OUT_ERRORESCONTADOR > 0) {
			return salida;
		}
		
		ResultadoCodificarArchivoBinario resultadoCodificarArchivos = codificarAdjuntos(conn, entrada.p_IdEmpresa, entrada.p_Anotaciones, archivos);
		salida.P_OUT_MENSAJES.addAll(resultadoCodificarArchivos.Mensajes);
		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
		if(salida.P_OUT_ERRORESCONTADOR == 0L) {
			salida.P_OUT_RESULTADO.addAll(resultadoCodificarArchivos.ArchivosCodificados);			
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::DescargarAdjuntoAnotacionCliente", fin - inicio);
		return salida;
	}

	private List<ArchivoBinario> obtenerArchivosAnotacionesPortafolio(Connection conn, EntradaDescargarAdjuntoAnotacionCliente entrada) throws Exception {
		logger.info("INICIO::DescargarAdjuntoAnotacionCliente.obtenerArchivosAnotacionesPortafolio");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> paramOptimus = new HashMap<>(1);
		List<ArchivoBinario> archivos = new ArrayList<ArchivoBinario>();
		
		EntradaConsultarAnotacionesClientePortafolio entradaConsulta = new EntradaConsultarAnotacionesClientePortafolio();
		entradaConsulta.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConsulta.p_IdUsuario = Long.parseLong(entrada.p_IdUsuario);
		entradaConsulta.p_IdModulo = "SOC";
		entradaConsulta.p_UsaReglasNegocio = "S";
		entradaConsulta.p_ClienteCodigoPortafolio.add(new ClienteCodigoPortafolio(entrada.p_IdPortafolio));
		ConsultarAnotacionesClientePortafolio servicioConsulta = new ConsultarAnotacionesClientePortafolio();
		SalidaConsultarAnotacionesClientePortafolio salidaConsulta = servicioConsulta.logica(conn,entradaConsulta);
		SQL sql = new SQL();
		
		if(salidaConsulta.P_OUT_ERRORESCONTADOR == 0L) {
			for(Long idAnotacion : entrada.p_Anotaciones) {
				List<AnotacionClientePortafolio> anotacionesPortafolioSEBRA = salidaConsulta.P_OUT_RESULTADO.stream().filter(anot -> idAnotacion.equals(anot.IdAnotacion) && "SEBRA".equals(anot.Origen)).collect(Collectors.toList());
				if(!anotacionesPortafolioSEBRA.isEmpty()) {
					AnotacionClientePortafolio anotacion = anotacionesPortafolioSEBRA.get(0);
					archivos.addAll(consultarArchivosSebra(conn, entrada.p_IdEmpresa, anotacion.IdAnotacion));
				}
				List<AnotacionClientePortafolio> anotacionesPortafolioOptimus = salidaConsulta.P_OUT_RESULTADO.stream().filter(anot -> idAnotacion.equals(anot.IdAnotacion) && "OPTIMUS".equals(anot.Origen)).collect(Collectors.toList());
				if(!anotacionesPortafolioOptimus.isEmpty()) {
					AnotacionClientePortafolio anotacion = anotacionesPortafolioOptimus.get(0);
					paramOptimus.put("p_IdsAnotaciones", anotacion.IdAnotacion);
					
					archivos.addAll(sql.query(conn, SQL_SELECT_ARCHIVOS_ANOTACIONES_PORTAFOLIO_OPTIMUS, paramOptimus, ArchivoBinario.class));
				}
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::DescargarAdjuntoAnotacionCliente.obtenerArchivosAnotacionesPortafolio", fin - inicio);
		return archivos;
	}

	private List<ArchivoBinario> consultarArchivosSebra(Connection conn, Long idEmpresa, Long idAnotacion) {
		logger.info("INICIO::DescargarAdjuntoAnotacionCliente.consultarArchivosSebra");
		long inicio = System.currentTimeMillis();
		
		boolean debug = false;
		HashMap<String, Object> paramSebra = new HashMap<>(1);
		List<ArchivoBinario> archivos = new ArrayList<ArchivoBinario>();
		
		String DBdriverSebra= "";
		String DBuserSebra 	= "";
		String DBpassSebra 	= "";
		String DBurlSebra 	= "";
		String DBpropSebra	= "";
		String DBkeySebra	= "";
		
		Properties prop = new Properties();
		InputStream input;
		SQL sql = new SQL();
		
		try {
        	if( debug ) {
                input = new FileInputStream("C:\\GIT\\OPTIMUS\\workspace\\optimus-corredores-dev-workspace\\OptimusDevJava\\optimus.properties");
            } else {
            	String rutaArchivoProperties = ConfiguracionSistema.getParametroConfiguracionString(conn, idEmpresa, "PLATAFORMA", "PLA.RutaArchivosConfiguracion");        	
            	input = new FileInputStream(rutaArchivoProperties+"optimus.properties");
            	logger.info("Configuracion optimus.properties cargada");
            }
        	
        	prop.load(input);
        	DBdriverSebra=prop.getProperty("DBdriverSebra");
            DBuserSebra=prop.getProperty("DBuserSebra");
            DBpassSebra=prop.getProperty("DBpassSebra");
            DBurlSebra=prop.getProperty("DBurlSebra");
            DBpropSebra=prop.getProperty("DBpropSebra");
            DBkeySebra = prop.getProperty("DBkeySebra");
            input.close();
            logger.info("PROPIEDADES CONEXION SEBRA :::::::::::::");
            logger.info("DBdriverSebra :: "+ DBdriverSebra);
            logger.info("DBuserSebra :: "+DBuserSebra + "( "+OpenEJBEncryption.decrypt(DBuserSebra, DBkeySebra)+" )");
            logger.info("DBpassSebra :: "+DBpassSebra+ "( "+OpenEJBEncryption.decrypt(DBpassSebra, DBkeySebra)+" )");
            logger.info("DBurlSebra :: "+DBurlSebra);
            logger.info("DBpropSebra :: "+DBpropSebra);
            logger.info("DBkeySebra :: "+DBkeySebra);
        } catch(Exception ex) {
        	logger.info("ERROR NO SE PUEDE LEER el archivo optimus.properties", ex);
            logger.info(ex.toString());
        }
        
        BasicDataSource dsTarget = new BasicDataSource();            
        try {                                                
	        dsTarget.setDriverClassName(DBdriverSebra);
	        dsTarget.setUsername(OpenEJBEncryption.decrypt(DBuserSebra, DBkeySebra));
	        dsTarget.setPassword(OpenEJBEncryption.decrypt(DBpassSebra, DBkeySebra));
	        dsTarget.setUrl(DBurlSebra);          
	        dsTarget.setConnectionProperties(DBpropSebra);
	        dsTarget.setDefaultAutoCommit(true);
	        logger.info("Conectado a Sebra");
        } catch( Exception e ){
        	logger.info("ERROR NO SE PUEDE OBTENER CONEXION SEBRA DB", e);
        	logger.info(e.toString());                                            
        }
        
        try( Connection conRU = dsTarget.getConnection() ) {
            logger.info("Consulta de data generada en sebra");
            paramSebra.put("IdAnotacion", idAnotacion);
            
            archivos.addAll(sql.query(conRU, SQL_SELECT_ARCHIVOS_ANOTACIONES_PORTAFOLIO_SEBRA, paramSebra, ArchivoBinario.class));
            
            logger.info("Se agregaron los archivos de las anotaciones de Sebra.");
		} catch( Exception er ) {
			logger.info("ERROR NO SE PUEDE CONECTAR BD EN SEBRA", er);
			logger.info(er.toString());                                                    
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::DescargarAdjuntoAnotacionCliente.consultarArchivosSebra", fin - inicio);
		return archivos;
	}

	private ResultadoCodificarArchivoBinario codificarAdjuntos(Connection conn, Long p_IdEmpresa, List<Long> p_IdsAnotaciones, List<ArchivoBinario> archivos) throws Exception {
		logger.info("INICIO::DescargarAdjuntoAnotacionCliente.codificarAdjuntos");
		long inicio = System.currentTimeMillis();
		ResultadoCodificarArchivoBinario resultado = new ResultadoCodificarArchivoBinario();
		
		Set<Long> idsAnotacionesSinArchivo = new HashSet<>(p_IdsAnotaciones);
		List<ArchivoCodificado> archivosCodificados = archivos.stream().filter(anotacion -> !"".equals(anotacion.NombreArchivo)).map(archivo -> {
			ArchivoCodificado codificado = new ArchivoCodificado();
			codificado.IdAnotacion = archivo.IdAnotacion;
			codificado.NombreArchivo = archivo.NombreArchivo;
			codificado.Archivo = Base64.encodeBase64String(archivo.Archivo);
			idsAnotacionesSinArchivo.remove(archivo.IdAnotacion);
			
			return codificado;
		}).collect(Collectors.toList());
		resultado.ArchivosCodificados.addAll(archivosCodificados);
		
		HashMap<String, String> params = new HashMap<>(1);
		for (Long idAnotacion : idsAnotacionesSinArchivo) {
			params.put("IdAnotacion", String.valueOf(idAnotacion));
			resultado.Mensajes.add(new PlataformaUtils.MensajeSalida(idAnotacion, MensajeSalida.MENSAJE_ERROR, "UI_ARCHIVO_NO_ENCONTRADO_PARA_ANOTACION", params, conn, p_IdEmpresa));
			params.clear();
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::DescargarAdjuntoAnotacionCliente.codificarAdjuntos", fin - inicio);
		return resultado;
	}

	private List<ArchivoBinario> obtenerArchivosAnotacionesCliente(Connection conn, List<Long> p_IdsAnotaciones) throws Exception {
		logger.debug("INICIO::DescargarAdjuntoAnotacionCliente.obtenerArchivosAnotacionesCliente");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> params = new HashMap<>(1);
		params.put("p_IdsAnotaciones", p_IdsAnotaciones);

		SQL sql = new SQL();
		
		List<ArchivoBinario> archivos = sql.query(conn, SQL_SELECT_ARCHIVOS_ANOTACIONES, params, ArchivoBinario.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::DescargarAdjuntoAnotacionCliente.obtenerArchivosAnotacionesCliente", fin - inicio);
		return archivos;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO TEST ==");

		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_Anotaciones\":[25],\"p_IdPortafolio\":6228}";

		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa02")) {
			Gson gson = Utils.getGsonBuilder();
			EntradaDescargarAdjuntoAnotacionCliente entrada = gson.fromJson(json, EntradaDescargarAdjuntoAnotacionCliente.class);
			System.out.println(entrada);

			DescargarAdjuntoAnotacionCliente descargarAdjuntoAnotacionCliente = new DescargarAdjuntoAnotacionCliente();
			try {
				SalidaDescargarAdjuntoAnotacionCliente salida = descargarAdjuntoAnotacionCliente.logica(conn, entrada);
				salida.P_OUT_RESULTADO.forEach(archivo -> archivo.Archivo = "BASE64_FILE_CONTENT");
				System.out.println(salida);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				conn.rollback();
			}
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}

		System.out.println("== FIN TEST ==");
	}
}
