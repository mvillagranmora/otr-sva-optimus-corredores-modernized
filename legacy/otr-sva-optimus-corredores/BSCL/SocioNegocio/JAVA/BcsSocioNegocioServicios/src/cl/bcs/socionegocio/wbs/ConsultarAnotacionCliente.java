package cl.bcs.socionegocio.wbs;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.cli.ConsultarAnotacionesCliente;
import cl.bcs.socionegocio.cli.ConsultarAnotacionesCliente.EntradaConsultarAnotacionesCliente;
import cl.bcs.socionegocio.cli.ConsultarAnotacionesCliente.SalidaConsultarAnotacionesCliente;
import cl.bcs.socionegocio.por.ConsultarAnotacionesClientePortafolio;
import cl.bcs.socionegocio.por.ConsultarAnotacionesClientePortafolio.ClienteCodigoPortafolio;
import cl.bcs.socionegocio.por.ConsultarAnotacionesClientePortafolio.EntradaConsultarAnotacionesClientePortafolio;
import cl.bcs.socionegocio.por.ConsultarAnotacionesClientePortafolio.SalidaConsultarAnotacionesClientePortafolio;
import cl.bcs.socionegocio.por.pojo.AnotacionClientePortafolio;
import cl.bcs.socionegocio.wbs.pojo.AnotacionCliente;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;


public class ConsultarAnotacionCliente implements JavaCall{

	static final Logger logger = LogManager.getLogger(ConsultarAnotacionCliente.class);
	
	private final String SQL_SELECT_IDCLIENTES = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ConsultarAnotacionCliente_SelectIdClientes.sql");
	private final String SQL_SELECT_ATRIBUTO_ANOTACIONES = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ConsultarAnotacionCliente_SelectAtributoAnotaciones.sql");
	
	
	public class EntradaConsultarAnotacionCliente extends BcsPojo {
		public Long p_IdEmpresa;
		public String p_IdUsuario;
		public String p_ClaveUsuario;
		public List<FiltroAnotacion> p_Anotaciones;
		
		public EntradaConsultarAnotacionCliente () {
			this.p_IdEmpresa = 0L;
			this.p_IdUsuario = "";
			this.p_ClaveUsuario = "";
			this.p_Anotaciones = new ArrayList<FiltroAnotacion>();
		}
	}
	
	public class SalidaConsultarAnotacionCliente extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<AnotacionCliente> P_OUT_RESULTADO;
		
		public SalidaConsultarAnotacionCliente () {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<AnotacionCliente>();
		}
	}
	
	public class FiltroAnotacion extends BcsPojo {
		public Long IdInterno;
		public String Filtro;
		public String Valor;
	}
	
	public static class Cliente extends BcsPojo {
        public Long IdCliente;
        public Long IdPortafolio;
        
        public Cliente () {
        	this.IdCliente = 0L;
        	this.IdPortafolio = 0L;
        }
    }
	
	public static class AtributoAnotacion extends BcsPojo {
		public String IdAtributoAnotacion;
		public String TipoAtributoAnotacion;
		
		public AtributoAnotacion () {
			this.IdAtributoAnotacion = "";
			this.TipoAtributoAnotacion = "";
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaConsultarAnotacionCliente jin=gson.fromJson(in.toString() ,EntradaConsultarAnotacionCliente.class);
		SalidaConsultarAnotacionCliente out=logica(conn,jin);		
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConsultarAnotacionCliente logica(Connection conn, EntradaConsultarAnotacionCliente entrada) throws Exception {
		logger.info("INICIO::ConsultarAnotacionCliente");
		long inicio = System.currentTimeMillis();
		
		SalidaConsultarAnotacionCliente salida = new SalidaConsultarAnotacionCliente();
		
		List<String> documentosIdentidad = entrada.p_Anotaciones.parallelStream()
				.filter(Filtro -> "DocumentoIdentidad".equals(Filtro.Filtro))
				.map(Filtro -> Filtro.Valor).collect(Collectors.toList());
		
		if(documentosIdentidad.isEmpty()) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_ENTRADA_SIN_DOCUMENTO_IDENTIDAD",new HashMap<String,String>(0),conn, entrada.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			long fin = System.currentTimeMillis();
			logger.info("FIN::ConsultarAnotacionCliente", fin - inicio);
			return salida;
		}
		
		List<String> motivos = entrada.p_Anotaciones.parallelStream()
				.filter(Filtro -> "Motivo".equalsIgnoreCase(Filtro.Filtro))
				.map(Filtro -> Filtro.Valor.toUpperCase())
				.collect(Collectors.toList());
		
		List<String> ambitos = entrada.p_Anotaciones.parallelStream()
				.filter(Filtro -> "Ambito".equalsIgnoreCase(Filtro.Filtro))
				.map(Filtro -> Filtro.Valor.toUpperCase())
				.collect(Collectors.toList());
		
		List<String> relevancias = entrada.p_Anotaciones.parallelStream()
				.filter(Filtro -> "Relevancia".equalsIgnoreCase(Filtro.Filtro))
				.map(Filtro -> Filtro.Valor.toUpperCase())
				.collect(Collectors.toList());
		
		List<String> tieneAdjuntos = entrada.p_Anotaciones.parallelStream()
				.filter(Filtro -> "TieneAdjunto".equalsIgnoreCase(Filtro.Filtro))
				.map(Filtro -> Filtro.Valor.toUpperCase())
				.collect(Collectors.toList());
		
		String codigoPortafolio = entrada.p_Anotaciones.stream().filter(Filtro -> "CodigoPortafolio".equals(Filtro.Filtro)).count() > 0 ? 
				entrada.p_Anotaciones.stream().filter(Filtro -> "CodigoPortafolio".equals(Filtro.Filtro)).map(Filtro -> Filtro.Valor).collect(Collectors.toList()).get(0) : "";
		
		List<Cliente> datosCliente = obtenerIdClientes(conn, documentosIdentidad, codigoPortafolio);
		
		if(datosCliente.isEmpty()) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_DOCUMENTO_IDENTIDAD_NO_ES_CLIENTE",new HashMap<String,String>(0),conn, entrada.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			long fin = System.currentTimeMillis();
			logger.info("FIN::ConsultarAnotacionCliente", fin - inicio);
			return salida;
		}
		
		List<AtributoAnotacion> atributos = obtenerAtributos(conn,entrada.p_IdEmpresa);
		
		salida.P_OUT_MENSAJES.addAll(verificarAtributosEntrada(conn,entrada.p_IdEmpresa, atributos,motivos,ambitos,relevancias,tieneAdjuntos));
		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
		if(salida.P_OUT_ERRORESCONTADOR > 0L) {
			long fin = System.currentTimeMillis();
			logger.info("FIN::ConsultarAnotacionCliente", fin - inicio);
			return salida;
		}
		
		Cliente cliente = datosCliente.get(0);
		if(!"".equals(codigoPortafolio) && cliente.IdPortafolio == 0L) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_CLIENTE_NO_TIENE_CODIGO_PORTAFOLIO",new HashMap<String,String>(0),conn, entrada.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			long fin = System.currentTimeMillis();
			logger.info("FIN::ConsultarAnotacionCliente", fin - inicio);
			return salida;
		}
		if(!"".equals(codigoPortafolio) && cliente.IdPortafolio > 0L) {
			EntradaConsultarAnotacionesClientePortafolio entradaConsultarAnotacionPortafolio = new EntradaConsultarAnotacionesClientePortafolio();
			ConsultarAnotacionesClientePortafolio servicio = new ConsultarAnotacionesClientePortafolio();
			entradaConsultarAnotacionPortafolio.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaConsultarAnotacionPortafolio.p_IdModulo = "SOC";
			entradaConsultarAnotacionPortafolio.p_UsaReglasNegocio = "";
			entradaConsultarAnotacionPortafolio.p_IdUsuario = Long.parseLong(entrada.p_IdUsuario);
			entradaConsultarAnotacionPortafolio.p_ClienteCodigoPortafolio.add(new ClienteCodigoPortafolio(cliente.IdPortafolio));
			SalidaConsultarAnotacionesClientePortafolio salidaConsultarAnotacionesClientePortafolio = servicio.logica(conn, entradaConsultarAnotacionPortafolio);
			if(salidaConsultarAnotacionesClientePortafolio.P_OUT_ERRORESCONTADOR > 0L) {
				salida.P_OUT_MENSAJES.addAll(salidaConsultarAnotacionesClientePortafolio.P_OUT_MENSAJES);
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
				long fin = System.currentTimeMillis();
				logger.info("FIN::ConsultarAnotacionCliente", fin - inicio);
				return salida;
			}
			salida.P_OUT_RESULTADO = filtrarResultadosAnotacionesClientePortafolio(salidaConsultarAnotacionesClientePortafolio.P_OUT_RESULTADO, motivos, ambitos, relevancias, tieneAdjuntos);
		}
		
		if("".equals(codigoPortafolio) && cliente.IdCliente > 0L){
			EntradaConsultarAnotacionesCliente entradaConsultarAnotacionCliente = new EntradaConsultarAnotacionesCliente();
			ConsultarAnotacionesCliente servicio = new ConsultarAnotacionesCliente();
			entradaConsultarAnotacionCliente.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaConsultarAnotacionCliente.p_IdModulo = "SOC";
			entradaConsultarAnotacionCliente.p_UsaReglasNegocio = "";
			entradaConsultarAnotacionCliente.p_IdUsuario = Long.parseLong(entrada.p_IdUsuario);
			entradaConsultarAnotacionCliente.p_Clientes.add(new ConsultarAnotacionesCliente.Cliente(cliente.IdCliente));
			SalidaConsultarAnotacionesCliente salidaConsultarAnotacionesCliente = servicio.logica(conn, entradaConsultarAnotacionCliente);
			if(salidaConsultarAnotacionesCliente.P_OUT_ERRORESCONTADOR > 0L) {
				salida.P_OUT_MENSAJES.addAll(salidaConsultarAnotacionesCliente.P_OUT_MENSAJES);
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
				long fin = System.currentTimeMillis();
				logger.info("FIN::ConsultarAnotacionCliente", fin - inicio);
				return salida;
			}
			salida.P_OUT_RESULTADO = filtrarResultadosAnotacionesCliente(salidaConsultarAnotacionesCliente.P_OUT_RESULTADO, motivos, ambitos, relevancias, tieneAdjuntos);
		}
		
		if(salida.P_OUT_RESULTADO.isEmpty()) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"UI_SIN_ANOTACIONES",new HashMap<String,String>(0),conn, entrada.p_IdEmpresa));
		} else {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_CONSULTA_ANOTACIONES_OK",new HashMap<String,String>(0),conn, entrada.p_IdEmpresa));
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::ConsultarAnotacionCliente", fin - inicio);
		return salida;
	}
	
	private List<AnotacionCliente> filtrarResultadosAnotacionesCliente(List<cl.bcs.socionegocio.cli.pojo.AnotacionCliente> p_OUT_RESULTADO, List<String> motivos, List<String> ambitos, List<String> relevancias, List<String> tieneAdjuntos) {
		logger.debug("INICIO::ConsultarAnotacionCliente.filtrarResultadosAnotacionesCliente");
        long inicio = System.currentTimeMillis();
        
		List<AnotacionCliente> filtrados = p_OUT_RESULTADO.stream()
				.filter( res -> (motivos.isEmpty() || motivos.contains(res.IdMotivo)) && (ambitos.isEmpty() || ambitos.contains(res.IdAmbito)) && (relevancias.isEmpty() || relevancias.contains(res.IdRelevancia)) && (tieneAdjuntos.isEmpty() || tieneAdjuntos.contains(res.ExisteArchivo))).map( res-> {
			AnotacionCliente anotacion = new AnotacionCliente();
			anotacion.IdAnotacion = res.IdAnotacion;
			anotacion.IdCliente = res.IdCliente;
			anotacion.Titulo = res.Titulo;
			anotacion.IdAmbito = res.IdAmbito;
			anotacion.IdMotivo = res.IdMotivo;
			anotacion.IdRelevancia = res.IdRelevancia;
			anotacion.TieneAdjunto = res.ExisteArchivo;
			anotacion.NombreArchivo = res.NombreArchivo;
			anotacion.Ambito = res.NombreAmbito;
			anotacion.Motivo = res.NombreMotivo;
			anotacion.Relevancia = res.NombreRelevancia;
			anotacion.FechaIngreso = res.FechaIngreso;
			anotacion.ValidacionDesde = res.ValidoDesde;
			anotacion.ValidacionHasta = res.ValidoHasta;
			anotacion.Origen = res.Origen;
			return anotacion;
		}).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::ConsultarAnotacionCliente.filtrarResultadosAnotacionesCliente", fin - inicio);
		return filtrados;
	}

	private List<AnotacionCliente> filtrarResultadosAnotacionesClientePortafolio(List<AnotacionClientePortafolio> p_OUT_RESULTADO, List<String> motivos, List<String> ambitos, List<String> relevancias, List<String> tieneAdjuntos ) {
		logger.debug("INICIO::ConsultarAnotacionCliente.filtrarResultadosAnotacionesClientePortafolio");
        long inicio = System.currentTimeMillis();
        
		List<AnotacionCliente> filtrados = p_OUT_RESULTADO.stream()
				.filter( res -> (motivos.isEmpty() || motivos.contains(res.IdMotivo)) && (ambitos.isEmpty() || ambitos.contains(res.IdAmbito)) && (relevancias.isEmpty() || relevancias.contains(res.IdRelevancia)) && (tieneAdjuntos.isEmpty() || tieneAdjuntos.contains(res.ExisteArchivo))).map( res-> {
			AnotacionCliente anotacion = new AnotacionCliente();
			anotacion.IdAnotacion = res.IdAnotacion;
			anotacion.IdPortafolio = res.IdPortafolio;
			anotacion.Titulo = res.Titulo;
			anotacion.IdAmbito = res.IdAmbito;
			anotacion.IdMotivo = res.IdMotivo;
			anotacion.IdRelevancia = res.IdRelevancia;
			anotacion.TieneAdjunto = res.ExisteArchivo;
			anotacion.NombreArchivo = res.NombreArchivo;
			anotacion.Ambito = res.NombreAmbito;
			anotacion.Motivo = res.NombreMotivo;
			anotacion.Relevancia = res.NombreRelevancia;
			anotacion.FechaIngreso = res.FechaIngreso;
			anotacion.ValidacionDesde = res.ValidoDesde;
			anotacion.ValidacionHasta = res.ValidoHasta;
			anotacion.Origen = res.Origen;
			return anotacion;
		}).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::ConsultarAnotacionCliente.filtrarResultadosAnotacionesClientePortafolio", fin - inicio);
		return filtrados;
	}

	public List<MensajeSalida> verificarAtributosEntrada(Connection conn, Long idEmpresa, List<AtributoAnotacion> atributos, List<String> motivos, List<String> ambitos, List<String> relevancias, List<String> tieneAdjuntos) throws Exception {
		logger.debug("INICIO::ConsultarAnotacionCliente.verificarAtributosEntrada");
        long inicio = System.currentTimeMillis();
        
        List<MensajeSalida> mensajes = new ArrayList<>();
        HashMap<String,String> params = new HashMap<String,String>(1);
        for(String motivo : motivos) {
        	if(atributos.parallelStream().filter(atr -> "MOTIVO".equals(atr.TipoAtributoAnotacion) && motivo.equalsIgnoreCase(atr.IdAtributoAnotacion)).count() == 0 ) {
        		params.put("Motivo", motivo);
        		mensajes.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_MOTIVO_NO_EXISTE",params,conn, idEmpresa));
        		params.clear();
        	}
        }

        for(String ambito : ambitos) {
        	if(atributos.parallelStream().filter(atr -> "AMBITO".equals(atr.TipoAtributoAnotacion) && ambito.equalsIgnoreCase(atr.IdAtributoAnotacion)).count() == 0 ) {
        		params.put("Ambito", ambito );
        		mensajes.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_AMBITO_NO_EXISTE",params,conn, idEmpresa));
        		params.clear();
        	}
        }

        for(String relevancia : relevancias) {
        	if(atributos.parallelStream().filter(atr -> "RELEVANCIA".equals(atr.TipoAtributoAnotacion) && relevancia.equalsIgnoreCase(atr.IdAtributoAnotacion)).count() == 0 ) {
        		params.put("Relevancia",relevancia);
        		mensajes.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_RELEVANCIA_NO_EXISTE",params,conn, idEmpresa));
        		params.clear();
        	}
        }
        for(String tieneAdjunto : tieneAdjuntos) {
        	if(!("S".equalsIgnoreCase(tieneAdjunto) || "N".equalsIgnoreCase(tieneAdjunto))) {
        		params.put("TieneAdjunto",tieneAdjunto);
        		mensajes.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_TIENE_ADJUNTO_NO_ES_VALIDO",params,conn, idEmpresa));
        		params.clear();
        	}
        }
        
        long fin = System.currentTimeMillis();
        logger.debug("FIN::ConsultarAnotacionCliente.verificarAtributosEntrada", fin - inicio);
		return mensajes;
	}

	public List<AtributoAnotacion> obtenerAtributos(Connection conn, Long idEmpresa) throws Exception {
		logger.debug("INICIO::ConsultarAnotacionCliente.obtenerAtributos");
        long inicio = System.currentTimeMillis();
        
        SQL sql = new SQL();
        HashMap<String, Object> parametros = new HashMap<>(1);
        parametros.put("IdEmpresa", idEmpresa);
        
        List<AtributoAnotacion> atributos = sql.query(conn, SQL_SELECT_ATRIBUTO_ANOTACIONES, parametros, AtributoAnotacion.class);
        
        long fin = System.currentTimeMillis();
        logger.debug("FIN::ConsultarAnotacionCliente.obtenerAtributos", fin - inicio);
		return atributos;
	}

	public List<Cliente> obtenerIdClientes(Connection conn, List<String> documentosIdentidad, String codigoPortafolio) throws Exception {
		logger.debug("INICIO::ConsultarAnotacionCliente.obtenerIdClientes");
        long inicio = System.currentTimeMillis();
        
        SQL sql = new SQL();
        HashMap<String, Object> parametros = new HashMap<>(2);
        parametros.put("DocumentoIdentidad", documentosIdentidad);
        parametros.put("CodigoPortafolio", codigoPortafolio);

        List<Cliente> resultadoClientes = sql.query(conn, SQL_SELECT_IDCLIENTES, parametros, Cliente.class).stream().filter(cli -> cli.IdCliente != 0L).collect(Collectors.toList());
        
        long fin = System.currentTimeMillis();
        logger.debug("FIN::ConsultarAnotacionCliente.obtenerIdClientes", fin - inicio);
		return resultadoClientes;
	}

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01")) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd")
						.serializeNulls().create();

				String jsonCL = "{\"p_IdEmpresa\":1,\r\n"
						+ "\"p_IdUsuario\":1,\r\n"
						+ "\"p_ClaveUsuario\":\"\",\r\n"
						+ "\"p_Anotaciones\":[\r\n"
						+ "		{ \"IdInterno\": 0, \"Filtro\": \"DocumentoIdentidad\", \"Valor\": \"10111045-1\" },\r\n"
						+ "		{ \"IdInterno\": 0, \"Filtro\": \"CodigoPortafolio\", \"Valor\": \"350\" },\r\n"
//						+ "		{ \"IdInterno\": 0, \"Filtro\": \"Ambito\", \"Valor\": \"PRIVAD\" },\r\n"
						+ "		{ \"IdInterno\": 0, \"Filtro\": \"Relevancia\", \"Valor\": \"NORMAL\" }\r\n"
//						+ "		{ \"IdInterno\": 0, \"Filtro\": \"TieneAdjunto\", \"Valor\": \"S\" },\r\n"
//						+ "		{ \"IdInterno\": 0, \"Filtro\": \"Motivo\", \"Valor\": \"RECORD\" }\r\n"
						+ "    ]}";
				EntradaConsultarAnotacionCliente in = gson.fromJson(jsonCL, EntradaConsultarAnotacionCliente.class);

				ConsultarAnotacionCliente demo = new ConsultarAnotacionCliente();
				System.out.println("ENTRADA");
				System.out.println(in);
				
				try {
					SalidaConsultarAnotacionCliente out = demo.logica(con, in);
					con.rollback();
					System.out.println("SALIDA");
					System.out.println(out);
				} catch (Exception er) {
					er.printStackTrace();
					con.rollback();
				}
				
			}catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
		} catch (Exception e) {
			e.printStackTrace();			
		}
	}
}

	
