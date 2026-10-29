package cl.bcs.socionegocio.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.doc.GenerarDocumentos;
import cl.bcs.socionegocio.doc.GenerarDocumentos.EntradaGenerarDocumentos;
import cl.bcs.socionegocio.doc.GenerarDocumentos.SalidaGenerarDocumentos;
import cl.bcs.socionegocio.pojo.RequisitoEnrolamientoFull;
import cl.bcs.socionegocio.pojo.Validacion;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class RegenerarDocumentosCliente implements JavaCall {
	
	static final Logger logger	 = LogManager.getLogger(RegenerarDocumentosCliente.class);
	public String SQL_SELECT_LOG = loadResourceToString("cl/bcs/socionegocio/soc/sql/RegenerarDocumentosCliente_SelectLog.sql");
	public String SQL_SELECT_PERSONA = loadResourceToString("cl/bcs/socionegocio/soc/sql/RegenerarDocumentosCliente_SelectPersona.sql");
	public String SQL_SELECT_CLIENTE = loadResourceToString("cl/bcs/socionegocio/soc/sql/RegenerarDocumentosCliente_SelectCliente.sql");	
	public String SQL_SELECT_CAMPOSFORMULARIOS = loadResourceToString("cl/bcs/socionegocio/soc/sql/RegenerarDocumentosCliente_SelectCamposFormulario.sql");
	public String SQL_SELECT_INDICADORES_CLIENTE = loadResourceToString("cl/bcs/socionegocio/soc/sql/RegenerarDocumentosCliente_SelectoIndicadoresCliente.sql");
	public String SQL_SELECT_INDICADORES_PERSONA = loadResourceToString("cl/bcs/socionegocio/soc/sql/RegenerarDocumentosCliente_SelectoIndicadoresPersona.sql");
	public String SQL_CREATE_TABLE = "CREATE LOCAL TEMPORARY TABLE #TablaLog ( \"IdPersona\" BIGINT ,\"IdCliente\" BIGINT ,\"CodigoFormulario\"  nvarchar(200) ,\"CampoBackend\" nvarchar(100))";
	public String SQL_CREATE_TABLE_ACTUALIZAR = "CREATE LOCAL TEMPORARY TABLE #TablaLogActulizar ( \"IdLog\" BIGINT ,\"IdPrograma\" BIGINT ,\"Estado\"  nvarchar(3) )";
	public String SQL_DROP_TABLE   = "DROP TABLE  #TablaLog";
	public String SQL_INSERT_TABLE = "INSERT INTO #TablaLog VALUES (:IdPersona,:IdCliente,:CodigoFormulario,:CampoBackend)";
	public String SQL_INSERT_TABLE_ACTUALIZAR = "INSERT INTO  #TablaLogActulizar VALUES (:Idlog,:IdPrograma,:Estado)";
	public String SQL_DROP_TABLE_ACTUALIZAR = "DROP TABLE  #TablaLogActulizar";
	public String SQL_UPDATE_LOG   = loadResourceToString("cl/bcs/socionegocio/soc/sql/RegenerarDocumentosCliente_UpdateLog.sql");
	
	
	public static class EntradaRegenerarDocumentosCliente extends BcsPojo{
		public Long p_IdEmpresa;
		public String p_IdModulo;
		public Long p_IdUsuario;
		public Long p_IdCliente;
		public String p_UsaReglasNegocio;
		public String p_IdTipoCanalIngresoCorredor;
		
		public EntradaRegenerarDocumentosCliente() {
			this.p_IdCliente = 0L;
			this.p_UsaReglasNegocio = "";
			this.p_IdTipoCanalIngresoCorredor = "";
		}
	}	
	
	public static class SalidaRegenerarDocumentosCliente extends BcsPojo{
		
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<RequisitoEnrolamientoFull> P_OUT_REQUISITOS_ENR_FULL;
		
		public SalidaRegenerarDocumentosCliente() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_REQUISITOS_ENR_FULL = new ArrayList<>();
		}
		
		public void agregarValidacionesSalida(List<Validacion> validaciones) {
			for (Validacion validacion : validaciones) {
				agregarValidacionSalida(validacion);
			}
		}
		
		public void agregarValidacionSalida(Validacion validacion) {
			if (validacion.EsError) {
				this.P_OUT_ERRORESCONTADOR += 1L;
			}

			this.P_OUT_MENSAJES.add(validacion.MensajeError);
		}
	}
	
	
	public static class Log extends BcsPojo {
		 public Long Idlog;
	     public String CodigoFormulario; 
		 public String CampoBackend;  
		 public Long IdEmpresa; 		
		 public Long IdCliente;			
		 public Long IdPersona;			
		 public Long IdUsuario; 		
		 public String FechaSistema; 		
		 public Long IdPrograma; 		
		 public String Estado;			
		 public String Mensaje;		
		 
		 public Log () {
			 this.IdPrograma =0L;
			 this.Mensaje ="";			 
		 }
	}
	
	public static class Persona extends BcsPojo {
		
		public Long IdPersona;
		public String Tipo;
		public String IdEstado;
		public String IdTipoCanalIngresoCorredor;
		
	}
	
	public static class Cliente extends BcsPojo {		
		public Long IdCliente;
		public Long IdPersona;
		public String IdEstado;
		public String TipoEnrolamiento;
	}
	
	public static class GenerarDocumento extends BcsPojo {		
		public Long IdCliente;
		public String IdCanal;
	}
	
	public static class ActualizaLog extends BcsPojo {		
		public Long IdCliente;
		public Long IdPrograma;
	}
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaRegenerarDocumentosCliente jin = gson.fromJson(in.toString(), EntradaRegenerarDocumentosCliente.class);
		SalidaRegenerarDocumentosCliente out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public void imprimirLog(List<Log> listaLog ) {
		for (Log log : listaLog) {
			System.out.println("IdCliente: "+log.IdCliente +" | IdPersona: " +log.IdPersona +" | "+log.CodigoFormulario);
		}
	}


	public SalidaRegenerarDocumentosCliente logica(Connection con, EntradaRegenerarDocumentosCliente entrada) throws Exception {
		logger.info("INICIO::SalidaRegenerarDocumentosCliente");
		long inicioServicio = System.currentTimeMillis();
	
		SalidaRegenerarDocumentosCliente salida = new SalidaRegenerarDocumentosCliente();		
		List<Log> listaLog = this.obtenerLog(con,entrada.p_IdCliente);
		
		Map<Long,Cliente> mapClienteLog = this.obtnerClienteLog(con, listaLog);
		Map<Long,Cliente> mapCLienteKeyPersona = this.cambiarKeyMap(mapClienteLog);		
		
		//Actualiza IdPersona a los registro que no lo tenian y descarte las que no encuentra informacion
		
		List<Log> listaLogVerificada = new ArrayList<>();
		List<Log> listaLogDescartada = new ArrayList<>();
		
		//Si el servicio se esta llamando desde un boton o desde WBS para un cliente en especifico entonces:
		if (entrada.p_IdCliente != 0 && entrada.p_IdCliente != null) {
			for (Log log : listaLog) {	
				if(log.IdPersona.compareTo(0L)==0 && mapClienteLog.get(log.IdCliente)!=null) {
					log.IdPersona= mapClienteLog.get(log.IdCliente).IdPersona;
				}else if(log.IdCliente.compareTo(0L)==0 && mapCLienteKeyPersona.get(log.IdPersona)!=null) {			
					log.IdCliente= mapCLienteKeyPersona.get(log.IdPersona).IdCliente;//revisar aqui
				}

				//DESCARTE 
				if(log.IdPersona.compareTo(0L)!=0 && log.IdCliente.compareTo(0L)!=0 && log.IdCliente.compareTo(entrada.p_IdCliente)==0) {
					listaLogVerificada.add(log);
				}else if(log.IdCliente.compareTo(entrada.p_IdCliente)==0){
					listaLogDescartada.add(log);
				}else {
					
				}
			}
		}else { //Caso contrario, si se esta llamando por pauta entonces:
			for (Log log : listaLog) {
				
				if(log.IdPersona.compareTo(0L)==0 && mapClienteLog.get(log.IdCliente)!=null) {
					log.IdPersona= mapClienteLog.get(log.IdCliente).IdPersona;
				}else if(log.IdCliente.compareTo(0L)==0 && mapCLienteKeyPersona.get(log.IdPersona)!=null) {			
					log.IdCliente= mapCLienteKeyPersona.get(log.IdPersona).IdCliente;//revisar aqui
				}

				//DESCARTE 
				if(log.IdPersona.compareTo(0L)!=0 && log.IdCliente.compareTo(0L)!=0 ) {
					listaLogVerificada.add(log);
				}else {
					listaLogDescartada.add(log);
				}
			}
		}
		
				
		//informacion persona 
		Map<Long,Persona> mapPersonasLog = this.obtenerPersonasLog(con ,listaLog);
		
		//Completar tipo formulario
		this.completarFormulario(listaLogVerificada, mapPersonasLog ,mapCLienteKeyPersona);
		
		//filtrar indicador - accion - campo
		
		List<Log> listaLogIndicadores = this.obtenerLogIndicadores(listaLogVerificada);
		List<Log> listaLogCampos=this.obtenerLogCampos(listaLogVerificada);
		List<Log> listaLogAcciones= this.obtenerLogAcciones(listaLogVerificada);
		
		List<GenerarDocumento> listaClienteGenerarDocumentos= new ArrayList<GenerarDocumento>();  
		List<GenerarDocumento> GenerarDocumentos= new ArrayList<GenerarDocumento>();  
		
		if(!listaLogAcciones.isEmpty()) {
			listaClienteGenerarDocumentos.addAll(this.generarDocumentoAcciones(listaLogAcciones,mapPersonasLog));
		}
		if(!listaLogCampos.isEmpty()) {
			listaClienteGenerarDocumentos.addAll(this.generarDocumentoCampos(con,listaLogCampos,mapPersonasLog));
		}
		if(!listaLogIndicadores.isEmpty()) {
			listaClienteGenerarDocumentos.addAll(this.generarDocumentoIndicadores(con,listaLogIndicadores));
		}

		
		//filtra idcliente repetid
		Map<Long,ActualizaLog> mapActualizarLog = new HashMap<Long,ActualizaLog>();
		List<Long> idClientes = new ArrayList<>();
		idClientes.add(entrada.p_IdCliente);
		
		SocioNegocioUtils.ResultadoValidacionRequisitosEnrolamientoFull resultado = SocioNegocioUtils.validarEnrolamientoFull(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo, entrada.p_UsaReglasNegocio, entrada.p_IdTipoCanalIngresoCorredor, idClientes);
		salida.agregarValidacionesSalida(resultado.validaciones);
		salida.P_OUT_REQUISITOS_ENR_FULL.addAll(resultado.requisitos);
	
		if (salida.P_OUT_ERRORESCONTADOR > 0) {
			return salida;
		}
		
		if(!listaClienteGenerarDocumentos.isEmpty()) {
			GenerarDocumentos.addAll(this.filtrarEjecuciones(listaClienteGenerarDocumentos));
			mapActualizarLog = this.regenerarDocumentos(con, entrada,salida, GenerarDocumentos);
		}	
		
		//Actualizar
		if(!mapActualizarLog.isEmpty() || !listaLogVerificada.isEmpty() || !listaLogDescartada.isEmpty()) {
			this.actualizarTablaLog(con,mapActualizarLog,listaLogVerificada,listaLogDescartada);
		}
				
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::SalidaRegenerarDocumentosCliente :" , finServicio- inicioServicio);		
		
		return salida;
	}
	
	

	public Map<Long,ActualizaLog> regenerarDocumentos(Connection con,EntradaRegenerarDocumentosCliente entrada,SalidaRegenerarDocumentosCliente salida,List<GenerarDocumento> GenerarDocumentos) throws Exception {
		logger.trace("INICIO::SalidaRegenerarDocumentosCliente.generarDocumentos");
		long inicioServicio = System.currentTimeMillis();
		
	
		Map<Long,ActualizaLog> mapActualizarLog = new HashMap<Long,ActualizaLog>();
			
		
		for (GenerarDocumento doc : GenerarDocumentos){
			ActualizaLog actualizaLog = new ActualizaLog();
			actualizaLog.IdCliente =doc.IdCliente;
			
			EntradaGenerarDocumentos entradaGenerarDocumentos = new EntradaGenerarDocumentos();
			entradaGenerarDocumentos.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaGenerarDocumentos.p_IdCliente = doc.IdCliente;
			entradaGenerarDocumentos.p_IdTipoCanal = doc.IdCanal;
			
			GenerarDocumentos generarDocumentos = new GenerarDocumentos();
			SalidaGenerarDocumentos salidaGenerarDocumentos = generarDocumentos.logica(con, entradaGenerarDocumentos);
			
			if(salidaGenerarDocumentos.P_OUT_ERRORESCONTADOR>1) {
				salida.P_OUT_ERRORESCONTADOR += salidaGenerarDocumentos.P_OUT_ERRORESCONTADOR;
				for(MensajeSalida msgSalida: salidaGenerarDocumentos.P_OUT_MENSAJES) {
					salida.P_OUT_MENSAJES.add(new MensajeSalida(2L, MensajeSalida.MENSAJE_ERROR, "ERROR_AL_EJECUTAR_PROGRAMA",msgSalida.descripcion));
				}
			}else {
				actualizaLog.IdPrograma = 0L;
				mapActualizarLog.put(actualizaLog.IdCliente,actualizaLog);
			}			
		};
		
		
		long finServicio = System.currentTimeMillis();
		logger.trace("FIN::SalidaRegenerarDocumentosCliente.generarDocumentos :" , finServicio- inicioServicio);
		
		return mapActualizarLog;
	}
	
	public void actualizarTablaLog (Connection con,Map<Long,ActualizaLog> mapActualizarLog, List<Log> listaLogVerificada, List<Log> listaLogDescartada) throws SQLException {
		logger.debug("INICIO::SalidaRegenerarDocumentosCliente.generarDocumentos");
		long inicioServicio = System.currentTimeMillis();
		
		List<Log> listaUpdateLog = new ArrayList<Log>();
		
		for (Log log : listaLogVerificada) {			
			log.Estado = "TER";
			log.IdPrograma = mapActualizarLog.get(log.IdCliente)!=null?mapActualizarLog.get(log.IdCliente).IdPrograma:0L;
		}
		
		listaUpdateLog.addAll(listaLogVerificada);		
		for (Log log : listaLogDescartada) {			
			log.Estado = "ERR";
			log.IdPrograma = 0L;
		}
		listaUpdateLog.addAll(listaLogDescartada);
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		SQL sql = new SQL();
		
		SQL.exec(con, SQL_CREATE_TABLE_ACTUALIZAR);		
		sql.batch(con,SQL_INSERT_TABLE_ACTUALIZAR ,listaUpdateLog, Log.class);
		SQL.exec(con, SQL_UPDATE_LOG);	
		SQL.exec(con, SQL_DROP_TABLE_ACTUALIZAR);
				
		long finServicio = System.currentTimeMillis();
		logger.debug("FIN::SalidaRegenerarDocumentosCliente.actualizarTablaLog :" , finServicio- inicioServicio);
	}
	
	
	public List<GenerarDocumento> filtrarEjecuciones (List<GenerarDocumento> listaClienteGenerarDocumentos){
		logger.debug("INICIO::SalidaRegenerarDocumentosCliente.filtrarEjecuciones");
		long inicioServicio = System.currentTimeMillis();
		
		Map<Long,GenerarDocumento> GenerarDocumentos = new HashMap<Long,GenerarDocumento>();
		List<GenerarDocumento> listaClientes = new ArrayList<>();
		
		listaClienteGenerarDocumentos.forEach(cli ->{			
			if(GenerarDocumentos.get(cli.IdCliente)==null) {
				GenerarDocumentos.put(cli.IdCliente,cli);
			}
		});
		
		for(Map.Entry<Long, GenerarDocumento> obj : GenerarDocumentos.entrySet()){			
			listaClientes.add(obj.getValue());
		}
		
		long finServicio = System.currentTimeMillis();
		logger.debug("FIN::SalidaRegenerarDocumentosCliente.filtrarEjecuciones :" , finServicio- inicioServicio);		
		return listaClientes;		
	}

	private  List<GenerarDocumento> generarDocumentoIndicadores(Connection con, List<Log> listaLogIndicadores) throws Exception {
		logger.trace("INICIO::SalidaRegenerarDocumentosCliente.generarDocumentoIndicadores");
		long inicioServicio = System.currentTimeMillis();
		
		List<GenerarDocumento> listaIndicadores =new  ArrayList<GenerarDocumento>();
		HashMap<String,Object> params = new HashMap<String,Object>();
		SQL sql = new SQL();
		
		SQL.exec(con, SQL_CREATE_TABLE);		
		sql.batch(con,SQL_INSERT_TABLE ,listaLogIndicadores, Log.class);
		
		List<GenerarDocumento> clienteIndicadores = sql.query(con, SQL_SELECT_INDICADORES_CLIENTE, params, GenerarDocumento.class);
		listaIndicadores.addAll(clienteIndicadores);
		List<GenerarDocumento> personaIndicadores = sql.query(con, SQL_SELECT_INDICADORES_PERSONA, params, GenerarDocumento.class);
		listaIndicadores.addAll(personaIndicadores);
				
		SQL.exec(con, SQL_DROP_TABLE);
		
		long finServicio = System.currentTimeMillis();
		logger.trace("FIN::SalidaRegenerarDocumentosCliente.generarDocumentoIndicadores :" , finServicio- inicioServicio);
		
		return listaIndicadores;
	}
	
	public List<GenerarDocumento> generarDocumentoCampos(Connection con, List<Log> listaLogCampos , Map<Long,Persona> mapPersonasLog) throws Exception{
		logger.debug("INICIO::SalidaRegenerarDocumentosCliente.generarDocumentoCampos");
		long inicioServicio = System.currentTimeMillis();
		HashMap<String,Object> params = new HashMap<String,Object>();
		SQL sql = new SQL();
		
		SQL.exec(con, SQL_CREATE_TABLE);		
		sql.batch(con,SQL_INSERT_TABLE ,listaLogCampos, Log.class);
		
		List<GenerarDocumento> clienteCampos = sql.query(con, SQL_SELECT_CAMPOSFORMULARIOS, params, GenerarDocumento.class);
		
		SQL.exec(con, SQL_DROP_TABLE);
		
		long finServicio = System.currentTimeMillis();
		logger.debug("FIN::SalidaRegenerarDocumentosCliente.generarDocumentoCampos :" , finServicio- inicioServicio);
		return clienteCampos;
	}
	
	public List<GenerarDocumento> generarDocumentoAcciones(List<Log> listaLogAcciones , Map<Long,Persona> mapPersonasLog){
		logger.trace("INICIO::SalidaRegenerarDocumentosCliente.generarDocumentoAcciones");
		long inicioServicio = System.currentTimeMillis();
		List<String> seccionRegeneraDocs = new ArrayList<String>();
		seccionRegeneraDocs.add("CUENTA_BANCARIA_");seccionRegeneraDocs.add("RELACIONES_");seccionRegeneraDocs.add("DOCUMENTO_IDENTIDAD_");
		List<Log> listaLogAccionesRegeneran = listaLogAcciones.stream().filter(log -> {
			for(String seccion: seccionRegeneraDocs) {
				if(log.CodigoFormulario.contains(seccion)) {
					return true;
				}
			}
			return false;

		}).collect(Collectors.toList());
			                			
		List<GenerarDocumento> clienteAcciones  = listaLogAccionesRegeneran.stream().map(log -> {
			GenerarDocumento documento  = new GenerarDocumento ();
			documento.IdCliente = log.IdCliente;
			documento.IdCanal = mapPersonasLog.get(log.IdPersona).IdTipoCanalIngresoCorredor;
			return documento;
		}).collect(Collectors.toList());
		
		long finServicio = System.currentTimeMillis();
		logger.trace("FIN::SalidaRegenerarDocumentosCliente.generarDocumentoAcciones :" , finServicio- inicioServicio);
		return clienteAcciones;
	}
	
	public List<Log> obtenerLogCampos(List<Log> listaLog ){
		logger.trace("INICIO::SalidaRegenerarDocumentosCliente.obtenerLogCampos");
		long inicioServicio = System.currentTimeMillis();
		
		List<Log> listaLogCampos = listaLog.stream()
				.filter(log ->
					!("Borrado".equals(log.CampoBackend))&&
				    !("Creado".equals(log.CampoBackend)) &&
				    !("Bloqueado".equals(log.CampoBackend))&&
				    !("Desbloqueado".equals(log.CampoBackend))&&
				    !(log.CampoBackend.matches("[+-]?\\d*(\\.\\d+)?"))
				).collect(Collectors.toList());
		
		long finServicio = System.currentTimeMillis();
		logger.trace("FIN::SalidaRegenerarDocumentosCliente.obtenerLogCampos :" , finServicio- inicioServicio);
		return listaLogCampos;			
	}
	
	public List<Log> obtenerLogAcciones(List<Log> listaLog ){
		logger.trace("INICIO::SalidaRegenerarDocumentosCliente.obtenerLogAcciones");
		long inicioServicio = System.currentTimeMillis();
		
		List<Log> listaLogAcciones = listaLog.stream()
				.filter(log ->
					"Borrado".equals(log.CampoBackend)||
				    "Creado".equals(log.CampoBackend) ||
				    "Bloqueado".equals(log.CampoBackend)||
				    "Desbloqueado".equals(log.CampoBackend)
				).collect(Collectors.toList());
		
		long finServicio = System.currentTimeMillis();
		logger.trace("FIN::SalidaRegenerarDocumentosCliente.obtenerLogAcciones :" , finServicio- inicioServicio);
		return listaLogAcciones;			
	}
	
	public List<Log> obtenerLogIndicadores(List<Log> listaLog ){
		logger.trace("INICIO::SalidaRegenerarDocumentosCliente.obtenerIndicadores");
		long inicioServicio = System.currentTimeMillis();
		
		List<Log> listaLogIndicadores = listaLog.stream().filter(log ->log.CampoBackend.matches("[+-]?\\d*(\\.\\d+)?")).collect(Collectors.toList());
		
		long finServicio = System.currentTimeMillis();
		logger.trace("FIN::SalidaRegenerarDocumentosCliente.obtenerIndicadores :" , finServicio- inicioServicio);
		return listaLogIndicadores;	
		
	}
	
	public void completarFormulario (List<Log> listaLog , Map<Long,Persona> mapPersonasLog,Map<Long,Cliente> mapClienteLog) {
		logger.trace("INICIO::SalidaRegenerarDocumentosCliente.cambiarKeyMap");
		long inicioServicio = System.currentTimeMillis();
		
		for (Log log : listaLog) {
			
			
			String tipoEnrolamiento = mapClienteLog.get(log.IdPersona)!=null? mapClienteLog.get(log.IdPersona).TipoEnrolamiento:"FULL";
			String tipoPersona = mapPersonasLog.get(log.IdPersona).Tipo;
			
			if("J".equals(tipoPersona)) {
				log.CodigoFormulario = log.CodigoFormulario+(("FULL").equals(tipoEnrolamiento)?"FULL_":"EXPRESS_")+"JURIDICA";
			}else {
				log.CodigoFormulario = log.CodigoFormulario+(("FULL").equals(tipoEnrolamiento)?"FULL_":"EXPRESS_")+"NATURAL";
			}
		}
		
		long finServicio = System.currentTimeMillis();
		logger.trace("FIN::SalidaRegenerarDocumentosCliente.cambiarKeyMap :" , finServicio- inicioServicio);
	}
	
	
	public Map<Long,Cliente>  cambiarKeyMap(Map<Long,Cliente>  mapClienteLog) {
		logger.trace("INICIO::SalidaRegenerarDocumentosCliente.cambiarKeyMap");
		long inicioServicio = System.currentTimeMillis();
		Map<Long,Cliente> mapCLienteKeyPersona = new HashMap<Long,Cliente>();
		
		for(Map.Entry<Long, Cliente> obj : mapClienteLog.entrySet()){			
			mapCLienteKeyPersona.put(obj.getValue().IdPersona,obj.getValue());
		}
		long finServicio = System.currentTimeMillis();
		logger.trace("FIN::SalidaRegenerarDocumentosCliente.cambiarKeyMap :" , finServicio- inicioServicio);
		return mapCLienteKeyPersona;
	}
	
	
	private Map<Long, Cliente> obtnerClienteLog(Connection con, List<Log> listaLog) throws Exception {
		logger.debug("INICIO::SalidaRegenerarDocumentosCliente.obtnerClienteLog");
		long inicioServicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String , Object>();
		List<Long> idCliente = listaLog.stream().filter(log -> log.IdCliente.compareTo(0L)!=0)
				.collect(Collectors.toList()).stream().map(log->log.IdCliente).collect(Collectors.toList())
				.stream().distinct().collect(Collectors.toList());	
		
		List<Long> idPersona = listaLog.stream().map(log->log.IdPersona).collect(Collectors.toList())
				   .stream().distinct().collect(Collectors.toList());
		
		params.put("idCliente", idCliente);
		params.put("idPersona", idPersona);
		
		Map<Long, Cliente> mapClientes = sql.query(con, SQL_SELECT_CLIENTE, params, Cliente.class)
				.stream().collect(Collectors.toMap(cli -> cli.IdCliente , cli -> cli));

		long finServicio = System.currentTimeMillis();
		logger.debug("FIN::SalidaRegenerarDocumentosCliente.obtnerClienteLog :" , finServicio- inicioServicio);
		return mapClientes;
	}

	private Map<Long,Persona> obtenerPersonasLog(Connection con, List<Log> listaLog) throws Exception {
		logger.debug("INICIO::SalidaRegenerarDocumentosCliente.obtenerPersonasLog");
		long inicioServicio = System.currentTimeMillis();
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String , Object>();
		
		List<Long> idPersona = listaLog.stream().map(log->log.IdPersona).collect(Collectors.toList())
							   .stream().distinct().collect(Collectors.toList());
		
		params.put("idPersona", idPersona);
		
		Map<Long, Persona> mapPersonas = sql.query(con, SQL_SELECT_PERSONA, params, Persona.class)
				.stream().collect(Collectors.toMap(per -> per.IdPersona , per -> per));		
		
		long finServicio = System.currentTimeMillis();
		logger.debug("FIN::SalidaRegenerarDocumentosCliente.obtenerPersonasLog :" , finServicio- inicioServicio);	
		return mapPersonas;
	}

	public List<Log> obtenerLog(Connection con, Long p_IdCliente) throws Exception{
		logger.debug("INICIO::SalidaRegenerarDocumentosCliente.obtenerLog");
		long inicioServicio = System.currentTimeMillis();
		HashMap<String,Object> params = new HashMap<String , Object>();
		SQL sql = new SQL();		
		params.put("IdCliente", p_IdCliente);
		List<Log> listaLog = sql.query(con, SQL_SELECT_LOG, params, Log.class);		
		
		long finServicio = System.currentTimeMillis();
		logger.debug("FIN::SalidaRegenerarDocumentosCliente.obtenerLog :" , finServicio- inicioServicio);	
		return listaLog;
	}
	
	
	public static void main(String[] args) throws Throwable {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("prebe","connections.properties")) { 
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
					
		
		String jsonCliente = "{\"p_IdEmpresa\":1,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_IdUsuario\":1646,\"p_IdCliente\":134979,\"p_IdTipoCanalIngresoCorredor\":\"OPT\"}";
		
		String jsonEmpresa = "{\"p_IdEmpresa\":1,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_IdUsuario\":1646,\"p_IdCliente\":161092,\"p_IdTipoCanalIngresoCorredor\":\"OPT\"}";
		
		EntradaRegenerarDocumentosCliente Objin = gson.fromJson(jsonEmpresa, EntradaRegenerarDocumentosCliente.class);
		RegenerarDocumentosCliente demo = new RegenerarDocumentosCliente();				
		try {
			SalidaRegenerarDocumentosCliente ObjOut = demo.logica(con, Objin);			
			System.out.println(ObjOut.toString());
			//con.commit();
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			
    	}
		System.out.println("== END ==");
	}


}
