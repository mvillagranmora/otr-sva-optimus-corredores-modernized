package cl.bcs.plataforma.jobs;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.file.NoSuchFileException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.lang3.math.NumberUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.opencsv.CSVWriter;

import cl.bcs.nuam.StoredProcedure;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.JavaView;
import cl.bcs.utils.PluginLoader;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.kaiser.odata.OdataFilterSql;
import cl.kaiser.odata.OdataImpSql;

public class ExportarGrillaCsv extends BcsJob{
	static final Logger logger = LogManager.getLogger(ExportarGrillaCsv.class);
	
	private final String SQL_UPDATE_ID_PROGRAMA = "UPDATE \"BSCL.Plataforma.BD::BAT.Programa\" SET \"RutaSalida\" = :RutaSalida WHERE \"IdPrograma\" = :IdPrograma;";
	private int offset = 0;
	private boolean DEBUG = false;
	
	public static class ActualizarPrograma{
		public String RutaSalida;
		public Long IdPrograma;
		
		public ActualizarPrograma(String RutaSalida,Long IdPrograma){
			this.RutaSalida = RutaSalida;
			this.IdPrograma = IdPrograma;
		}
	}
	
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		try {
			Long jobId = this.getJobId();
			Long id = Long.parseLong(this.getParam("t"));
			String r = this.getParam("r");
			String callParameters = this.getParam("callParameters");
			String odataParameters = this.getParam("odataParameters");
			String token = this.getParam("token");
			String columnas = this.getParam("columns");
			String nombreArchivo = this.getParam("fileName");
			String gridFilters = this.getParam("gridFilters");
			String gridSort = this.getParam("gridSort");
			logger.debug("odata:" + odataParameters);
			return logica(con, id, callParameters, odataParameters, token, nombreArchivo, columnas, r, jobId, gridFilters, gridSort);
		}catch(Exception e) {
			logger.debug(e);
			throw e;
		}
	}
	
	public boolean logica(Connection conn, Long idServicio, String callParameters, String odataParameters, String token, String fileName, String columnasBase64, String r, Long jobId, String gridFilters, String gridSort) throws Exception {
		logger.info("INIT");
		String schema = Utils.getSchema();
		
		//parse columns base64 to string utf-8.
		String columns = new String(Base64.getDecoder().decode(columnasBase64), "UTF-8");
		
		//obtener configuracion del servicio
		JsonObject dataSRVIn=new JsonObject();
		dataSRVIn.addProperty("p_Token",token);
		dataSRVIn.addProperty("p_IdServicio",idServicio);
		StoredProcedure kHashSrv=new StoredProcedure("BSCL.Plataforma.BD.Servicios::ACC.ProcesaLlamadaServletCall");
		
		JsonObject dataSRVOut=kHashSrv.call(conn, dataSRVIn);
		if(!dataSRVOut.get("p_out_Mensaje".toUpperCase()).getAsString().equals("OK++")) {
			throw new RuntimeException(dataSRVOut.get("p_out_Mensaje".toUpperCase()).getAsString());
		}
		Long SesionIdEmpresa = dataSRVOut.get("p_out_IdEmpresa".toUpperCase()).getAsLong();
		Long SesionIdUsuario = dataSRVOut.get("p_out_IdUsuario".toUpperCase()).getAsLong();
		String SrvNombre = dataSRVOut.get("p_out_NombreServicio".toUpperCase()).getAsString();
		String SrvTipo = dataSRVOut.get("p_out_TipoServicio".toUpperCase()).getAsString();
		String SrvTempIn = dataSRVOut.get("p_out_ServicioTempIn".toUpperCase()).getAsString();
		String SrvTempOUT = dataSRVOut.get("p_out_ServicioTempOut".toUpperCase()).getAsString();
		String 	pResulset= r;

		this.offset = FechaDB.ObtenerSegundosZonaHoraria(conn, SesionIdEmpresa);
		
		JsonParser jsonParser 		= new JsonParser();
        
		//obtener columnas
	    List<String> columnas = new ArrayList<>();
	    HashMap<String, JsonObject> configColumna = new HashMap<>();
	    jsonParser.parse(columns).getAsJsonArray().forEach((json) -> {
	    	JsonElement element = json.getAsJsonObject().get("field");
	    	if(element!=null) {
		    	columnas.add(element.getAsString());
		    	configColumna.putIfAbsent(element.getAsString(), json.getAsJsonObject());
	    	}
	    }); 
	    
	    //exportar archivo en excel
	    Date fechaActual = new Date();			
		DateFormat formatoFecha = new SimpleDateFormat("yyyyMMddHHmmss");
		String fechaHora = formatoFecha.format(fechaActual);
		
	    String archivo = "PLA/"+fechaHora+ "_" + StringUtils.remove(StringUtils.deleteWhitespace(StringUtils.stripAccents(fileName)), "/") + ".csv";
	    String rutaArchivos = ((!DEBUG) ? Utils.getConfiguracion(conn, schema, SesionIdEmpresa, "PLA.RutaBaseArchivos") : "./") + archivo;
	    String codigoPais = Utils.getConfiguracion(conn, schema, SesionIdEmpresa, "PLA.LocalizacionSistema");
	    
		if (SrvTipo.equalsIgnoreCase("p")) {
			
			JsonObject dataJsonIn	= jsonParser.parse(callParameters).getAsJsonObject();
			//call procedure
			JsonObject dataJsonCallIn	= dataJsonIn;
			if (dataJsonCallIn.get("IdEmpresa")!=null)   	dataJsonCallIn.addProperty("IdEmpresa",SesionIdEmpresa); 
			if (dataJsonCallIn.get("p_IdEmpresa")!=null) 	dataJsonCallIn.addProperty("p_IdEmpresa",SesionIdEmpresa); 
			if (dataJsonCallIn.get("IdUsuario")!=null) 		dataJsonCallIn.addProperty("IdUsuario",SesionIdUsuario); 
			if (dataJsonCallIn.get("p_IdUsuario")!=null) 	dataJsonCallIn.addProperty("p_IdUsuario",SesionIdUsuario); 
			
			StoredProcedure kCallSrv=new StoredProcedure(SrvNombre,SrvTempIn,SrvTempOUT);
							
			JsonObject dataJsonCallOut=kCallSrv.call(conn, dataJsonCallIn);
			
			if (pResulset!=null) {
				JsonArray results = dataJsonCallOut.get(pResulset.toUpperCase()).getAsJsonArray();

				if(results != null && results.size() > 0 && gridFilters != null && !gridFilters.equals("{}")) {
						JsonArray filters = jsonParser.parse(gridFilters).getAsJsonArray();
						JsonArray sort = (gridSort != null && !gridSort.equals("{}")) ? jsonParser.parse(gridSort).getAsJsonArray() : null;
						results = aplicarFiltrosGrilla(results, filters, sort);
				}

				//escribir csv
				System.out.println(results);
			    exportarCsv(rutaArchivos, columnas, configColumna, results, codigoPais);
			}else {
				return false;	
			}						
		} else if (SrvTipo.equalsIgnoreCase("q")) {
			//preparar la llamada de la vista
			JsonObject dataJsonIn	= jsonParser.parse(odataParameters).getAsJsonObject();
		    OdataImpSql odata=new OdataImpSql();
		    String 	urlCallback = (dataJsonIn.get("$callback")!=null) ? dataJsonIn.get("$callback").getAsString() : null;
		    String 	urlSelect = (dataJsonIn.get("$select")!=null) ? dataJsonIn.get("$select").getAsString() : null;
		    String 	urlFilter = (dataJsonIn.get("$filter")!=null) ? dataJsonIn.get("$filter").getAsString() : null;
		    String 	urlSort = (dataJsonIn.get("$orderby")!=null) ? dataJsonIn.get("$orderby").getAsString() : null;
		    String 	urlHandler = (dataJsonIn.get("$handler")!=null) ? dataJsonIn.get("$handler").getAsString() : "";
		    String  urlObject = "\""+schema+"\".\""+SrvNombre+"\"" + urlHandler;
		    long 	urlTop = (dataJsonIn.get("$top")!=null) ? Long.parseLong(dataJsonIn.get("$top").getAsString()) : -1;
		    long 	urlSkip = (dataJsonIn.get("$skip")!=null) ? Long.parseLong(dataJsonIn.get("$skip").getAsString()) : -1;
		    boolean urlInline = false;
		    if  (dataJsonIn.get("$inlinecount")!=null && dataJsonIn.get("$inlinecount").getAsString().equalsIgnoreCase("allpages")) {
		    	urlInline=true;
		    }
		    //evaluar si  el resultado del excel es biggie big
		    OdataFilterSql odataFilter = new OdataFilterSql(urlFilter);
		    odataFilter.Parser();
		    Long cantidadTotalRegistros = 0L;
		    try (PreparedStatement stm=odataFilter.getCountStatement(conn, urlObject)) {
				try (ResultSet rs=stm.executeQuery()) {			
					if(rs.next()) {
						cantidadTotalRegistros=rs.getLong(1);
					}
				}
			}
		    Long cantidadMaximaRegistros = 60000L;
		    Long cantidadCiclos = (cantidadTotalRegistros.compareTo(0L)>0) ? cantidadTotalRegistros/cantidadMaximaRegistros : 1L;
		    urlTop = cantidadMaximaRegistros;
		    urlSkip = 0;
		    
		    while(cantidadCiclos>=0) {
		    	logger.debug(String.format("urlSkip: %d, urlTop: %d, cantidadCliclos: %d", urlSkip, urlTop, cantidadCiclos));
		    	
		    	if(cantidadCiclos==0) {
			    	urlTop = cantidadTotalRegistros;
			    	System.out.println("fix");
			    }
		    	
		    	//realizar consulta a la vista y guardar JSON en output stream.
			    try(ByteArrayOutputStream bos = new ByteArrayOutputStream();){
			    	
				    odata.QueryJsonObject(conn, bos, urlCallback, urlSelect, urlObject,urlFilter,urlSort,urlTop,urlSkip,urlInline);
				    
				    //parsear el resultado a objeto json
				    JsonObject data = Utils.getGsonBuilder().fromJson(bos.toString("UTF-8") , JsonObject.class);
				    JsonArray results = data.get("d").getAsJsonObject().get("results").getAsJsonArray();
				    //escribir csv
				    exportarCsv(rutaArchivos, columnas, configColumna, results, codigoPais);
			    }
			    
			    //calcular siguente conjunto de datos
			    urlSkip+=urlTop;
			    cantidadCiclos-=1;			    
		    }

		} else if (SrvTipo.equalsIgnoreCase("j")) {
			
			JsonObject dataJsonIn	= jsonParser.parse(callParameters).getAsJsonObject();
			//call java
			JsonObject dataJsonCallIn	= dataJsonIn;
			if (dataJsonCallIn.get("IdEmpresa")!=null)   	dataJsonCallIn.addProperty("IdEmpresa",SesionIdEmpresa); 
			if (dataJsonCallIn.get("p_IdEmpresa")!=null) 	dataJsonCallIn.addProperty("p_IdEmpresa",SesionIdEmpresa); 
			if (dataJsonCallIn.get("IdUsuario")!=null) 		dataJsonCallIn.addProperty("IdUsuario",SesionIdUsuario); 
			if (dataJsonCallIn.get("p_IdUsuario")!=null) 	dataJsonCallIn.addProperty("p_IdUsuario",SesionIdUsuario); 
				
			String parts[]=SrvNombre.split("@");
			String pathBase=System.getProperty("user.home")+"/bin/";
			JavaCall jcall = null;
			try {
				jcall = (JavaCall)PluginLoader.getObject(pathBase,"PLUGIN_OPTIMUS.jar", parts[1]);
			}
			catch(NoSuchFileException e) {
				System.out.println("PLUGIN_OPTIMUS no encontrado, saltando");
			}
			if (jcall == null) {
				jcall = (JavaCall)PluginLoader.getObject(pathBase,parts[0], parts[1]);
			}
			
		    JsonObject dataJsonCallOut=jcall.call(conn,dataJsonCallIn);						
			if (pResulset!=null) {
				JsonArray results = dataJsonCallOut.get(pResulset).getAsJsonArray();
				
				if(results != null && results.size() > 0 && gridFilters != null && !gridFilters.equals("{}")) {
					JsonArray filters = jsonParser.parse(gridFilters).getAsJsonArray();
					JsonArray sort = (gridSort != null && !gridSort.equals("{}")) ? jsonParser.parse(gridSort).getAsJsonArray() : null;
					results = aplicarFiltrosGrilla(results, filters, sort);
				}
				
				exportarCsv(rutaArchivos, columnas, configColumna, results, codigoPais);								    
			}else {
				return false;	
			}	
					
		} else if( SrvTipo.equalsIgnoreCase("X") ) {
			
			if(odataParameters.equalsIgnoreCase("{}")) odataParameters = "[]";
			JsonObject dataJsonCallIn	= jsonParser.parse(callParameters).getAsJsonObject();
			JsonArray dataJsonIn		= jsonParser.parse(odataParameters).getAsJsonArray();
			if ( dataJsonCallIn.get("IdEmpresa" ) 	!= null )   dataJsonCallIn.addProperty("IdEmpresa",SesionIdEmpresa); 
			if ( dataJsonCallIn.get("p_IdEmpresa" ) != null ) 	dataJsonCallIn.addProperty("p_IdEmpresa",SesionIdEmpresa); 
			if ( dataJsonCallIn.get("IdUsuario" ) 	!= null ) 	dataJsonCallIn.addProperty("IdUsuario",SesionIdUsuario); 
			if ( dataJsonCallIn.get("p_IdUsuario" ) != null ) 	dataJsonCallIn.addProperty("p_IdUsuario",SesionIdUsuario); 
			List<Object> sorts = new ArrayList<>();     
			if ( dataJsonIn.size() > 0 )   sorts.add(dataJsonIn.get(0)); 
			
			String parts[]  = SrvNombre.split("@");			
			String pathBase = System.getProperty("user.home")+"/bin/";
			
			JavaView jcall = null;
			try {
				jcall = (JavaView) PluginLoader.getObject(pathBase,"PLUGIN_OPTIMUS.jar", parts[1]);
			}
			catch(NoSuchFileException e) {
				System.out.println("PLUGIN_OPTIMUS no encontrado, saltando");
			}
			if ( jcall == null ) {
				jcall = (JavaView) PluginLoader.getObject(pathBase,parts[0], parts[1]);
			}

			if ( pResulset != null ) {
				
				int skip 					= 0;
				int take 					= 20000;
				String sort 				= "";
				if(sorts.size() > 0) {
					String field                = ((JsonObject) sorts.get(0)).get("field").getAsString();
					String dir                  = ((JsonObject) sorts.get(0)).get("dir").getAsString();
					sort 						= field+"::"+dir;
				}
				if(gridSort != null && !gridSort.equalsIgnoreCase("{}")) {
					Gson gson = new Gson();
					JsonArray jsonArray = gson.fromJson(gridSort, JsonArray.class);
					JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
					String field                = jsonObject.get("field").getAsString();
					String dir                  = jsonObject.get("dir").getAsString();
					sort 						= field+"::"+dir;
				}
				String filters 				= ""; //por implementar.
				String NameCountError		= "p_out_ErroresContador";
				JsonObject dataJsonCallOut	= new JsonObject();
				
			    for( skip = 0;  ; skip = skip + take ) {
			    	JsonArray datosParaExportar = new JsonArray();
			    	dataJsonCallOut	= jcall.call( conn, dataJsonCallIn, skip, take, sort, filters );
			    	if ( dataJsonCallOut.get("p_out_ErrorCounter".toUpperCase()) != null ) NameCountError = "p_out_ErrorCounter";
			    	if ( dataJsonCallOut.get(NameCountError.toUpperCase() ) == null || dataJsonCallOut.get(NameCountError.toUpperCase()).getAsLong() > 0 ) return false;
			    	if ( dataJsonCallOut.get(pResulset).getAsJsonArray().size() == 0) {
			    		break;
			    	} else {
			    		datosParaExportar.addAll(dataJsonCallOut.get(pResulset).getAsJsonArray());
			    		exportarCsv( rutaArchivos, columnas, configColumna, datosParaExportar, codigoPais );
			    	}
			    }
			} else {
				
				return false;
				
			}
			
		}
		
		SQL sql = new SQL();
		sql.batch(conn,SQL_UPDATE_ID_PROGRAMA , Arrays.asList(new ActualizarPrograma(archivo , jobId)),ActualizarPrograma.class);
		logger.info("TERMINO");
		return true;
	}
	
	private String convertUTCToLocal(String utcdate) throws ParseException {
		ZonedDateTime dd = Instant.parse(utcdate.replace(" ", "T")+"Z").atZone(ZoneId.of("UTC"));
		ZonedDateTime ff = dd.withZoneSameInstant(ZoneId.of("America/Santiago"));
		return ff.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
	}
	
	private void exportarCsv(String fileName, List<String> columnas, HashMap<String, JsonObject> configColumnas,
			JsonArray results, String codigoPais) throws IOException, UnsupportedEncodingException, FileNotFoundException {
		boolean fileExist = new File(fileName).exists();

		SimpleDateFormat formatoTimestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		SimpleDateFormat formatoSoloFecha = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat formatoSoloHora = new SimpleDateFormat("HH:mm:ss");

				
		try(FileOutputStream os = new FileOutputStream(fileName, true);){
			if(!fileExist) {
				os.write(0xef);
				os.write(0xbb);
				os.write(0xbf);	
			}
			try(CSVWriter csv =new CSVWriter(new OutputStreamWriter(os,"UTF-8"),';');){
				if(!fileExist) {
					Set<String> titulosGrupoColumnasUnicos = new HashSet<>();
					List<String> titulosGrupoColumnas = new ArrayList<>();

					List<String> titulos = columnas.stream().map(column -> {
						JsonObject elem = configColumnas.get(column);
						
						if (elem != null) {
							
							if (elem.has("groupTitle")) {
					            String tituloGrupoColumnas = elem.get("groupTitle").getAsString();
					            titulosGrupoColumnas.add(titulosGrupoColumnasUnicos.add(tituloGrupoColumnas) ? tituloGrupoColumnas : "");
					        } else {
					        	titulosGrupoColumnas.add("");
					        }
							
							if (elem.has("title")) {
								return elem.get("title").getAsString();
							} else {
								return elem.get("field").getAsString();
							}
							
						} else {
							titulosGrupoColumnas.add("");
							return "";
						}
					}).collect(Collectors.toList());
					
					if (!titulosGrupoColumnasUnicos.isEmpty()) {
						String [] columnGroupTitle = titulosGrupoColumnas.toArray(new String[0]);
						csv.writeNext(columnGroupTitle);
					}
					
					String [] header = titulos.toArray(new String[0]);
			    	csv.writeNext(header);
				}
				for(JsonElement json : results){
			    	List<Object> columna = new ArrayList<>();
			    	for (String nombreColumna: columnas) {
			    		JsonElement col = json.getAsJsonObject().get(nombreColumna);
			    		if(col!=null && !col.isJsonNull()) {
				    		switch ( configColumnas.get(nombreColumna).get("type").getAsString().toLowerCase() ) {
				    			case "date":
				    				JsonObject configColumna = configColumnas.get(nombreColumna);
				    				String tipoFecha = (configColumna.get("bcstype")!=null) ? configColumna.get("bcstype").getAsString() : "";
				    				String jsDate = col.getAsString();
				    				try {
				    					long millis = 0;
				    					if(jsDate!= null && jsDate.contains("Date")) {
				    						//tiene milisegundos
				    						millis = Long.parseLong(jsDate.replaceAll("\\/Date\\(", "").replaceAll("\\)\\/", ""));
				    						jsDate = formatoTimestamp.format(new Date(millis));
				    					}else {
				    						//debe estar con formato ISO
				    						millis = formatoTimestamp.parse(jsDate).getTime();
				    					}
					    				String fechaFormateada = formatoTimestamp.format(new Date(millis));
					    				if(!fechaFormateada.contains("0001-01") && !fechaFormateada.contains("1900-01")) {
					    					//no es fecha default formatear
					    					
					    					if(!tipoFecha.isEmpty()) {
					    						switch (tipoFecha) {
												case "sysdate":
													fechaFormateada = formatoSoloFecha.format(new Date(millis));
													break;
												case "datetime":
													fechaFormateada = convertUTCToLocal(jsDate);
													break;
												case "sysdatetime":
													fechaFormateada = convertUTCToLocal(jsDate);
													break;
												case "systime":
													fechaFormateada = formatoSoloHora.format(new Date(millis + (this.offset * 1000)));
													break;
												default:
													fechaFormateada = formatoSoloFecha.format(new Date(millis));
													break;
												}
					    					}else {
					    						//fallback calculo de formatos de fechas
						    					if(!fechaFormateada.contains(Fecha.DEFAULT_TIME)) {
						    						fechaFormateada = convertUTCToLocal(jsDate);
						    					}else {
						    						//es hora hora default formatear YYYY-MM-DD
						    						fechaFormateada = formatoSoloFecha.format(new Date(millis));
						    					}
					    					}
					    					
					    				}else {
					    					//es fecha hora default
					    					fechaFormateada = "";
					    				}
					    				columna.add(fechaFormateada);
				    				}catch(Exception e) {
				    					
				    					e.printStackTrace();
				    					columna.add(jsDate);
				    				}
				    				break;
				    			case "number":
				    				//uglyassfix
				    				String separadorMiles = ".";
				    				String separadorDecimales = ",";
				    				if(codigoPais.equals("PE")) {
				    					separadorMiles = ",";
				    					separadorDecimales = ".";
				    				}
				    				String valor = col.getAsString().replace(separadorMiles, separadorDecimales); 
				    				columna.add( ("0E-8".equals(valor) ? "0" : valor) );
				    				break;
				    			default:
				    				//Segun lo conversado, cuando el campo Folio Cartola sea numero y 
				    				//la cantidad de sus caracteres sea superior a 15 se le añade el simbolo = y las comillas dobles.
				    				//Esto es para solucionar el problema detectado en excel, que al leerlo trunca el valor si se cumplen esas condiciones.
				    				
				    				if(nombreColumna.equalsIgnoreCase("FolioCartola")) {
				    					if(col.getAsString().length() > 15 && NumberUtils.isNumber(col.getAsString())) {
				    						columna.add("=\"".concat(col.getAsString()).concat("\""));
				    						break;
				    					}else {
				    						columna.add(col.getAsString());
					    					break;
				    					}
				    				}else {
				    					columna.add(col.getAsString());
				    					break;
				    				}
				    		}
			    		}else {
			    			columna.add("");
			    		}
					}
			    	csv.writeNext(columna.toArray(new String[0]));
			    }
			}
		}
	}
	
	private JsonArray aplicarFiltrosGrilla(JsonArray datos, JsonArray filtros, JsonArray ordenDatos) {
		logger.trace("INICIO::ExportarGrillaCsv.aplicarFiltrosGrilla");
		try {
			JsonArray datosFiltrados = new JsonArray();
			for (JsonElement dato: datos) {
				JsonObject objetoDato = dato.getAsJsonObject();
				boolean cumpleConTodosLosFiltros = true;
				
				for (JsonElement filtro : filtros) {
					JsonObject filtroObj = filtro.getAsJsonObject();
					String nombreCampo = filtroObj.get("field").getAsString();
	                String tipoDato = filtroObj.get("type").getAsString();
	                String operador = filtroObj.get("operator").getAsString();
					
	                JsonElement valorCampoFiltroJ = filtroObj.get("value");
	                JsonElement valorCampoDatoJ = objetoDato.get(nombreCampo);
	                
					if (valorCampoDatoJ != null && valorCampoFiltroJ != null) {
						switch(tipoDato) {
							case "string":
								String valorCampoDatoS = valorCampoDatoJ.getAsString().toLowerCase();
								String valorCampoFiltroS = valorCampoFiltroJ.getAsString().toLowerCase();
								
								if(operador.equals("eq") && valorCampoDatoS.equals(valorCampoFiltroS));
								else if(operador.equals("neq") && !valorCampoDatoS.equals(valorCampoFiltroS));
								else if(operador.equals("startswith") && valorCampoDatoS.startsWith(valorCampoFiltroS));
								else if(operador.equals("contains") && valorCampoDatoS.contains(valorCampoFiltroS));
								else if(operador.equals("doesnotcontain") && !valorCampoDatoS.contains(valorCampoFiltroS));
								else if(operador.equals("endswith") && valorCampoDatoS.endsWith(valorCampoFiltroS));
								else cumpleConTodosLosFiltros = false;
								break;
							case "number":
								BigDecimal valorCampoDatoN = valorCampoDatoJ.getAsBigDecimal();
								BigDecimal valorCampoFiltroN = valorCampoFiltroJ.getAsBigDecimal();
								
								if(operador.equals("eq") && valorCampoDatoN.compareTo(valorCampoFiltroN) == 0);
								else if(operador.equals("neq") && valorCampoDatoN.compareTo(valorCampoFiltroN) != 0);
								else if(operador.equals("gte") && valorCampoDatoN.compareTo(valorCampoFiltroN) >= 0);
								else if(operador.equals("gt") && valorCampoDatoN.compareTo(valorCampoFiltroN) == 1);
								else if(operador.equals("lte") && valorCampoDatoN.compareTo(valorCampoFiltroN) <= 0);
								else if(operador.equals("lt") && valorCampoDatoN.compareTo(valorCampoFiltroN) == -1);
								else cumpleConTodosLosFiltros = false;
								break;
							case "date":
								String bcsType = filtroObj.get("bcsType").getAsString();
								SimpleDateFormat formatoFechaDato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
								SimpleDateFormat formatoFechaFiltro = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
								Date valorCampoDatoD = formatoFechaDato.parse(valorCampoDatoJ.getAsString());
								Date valorCampoFiltroD = formatoFechaFiltro.parse(valorCampoFiltroJ.getAsString());
								
								if(bcsType != null && bcsType.equals("date")) {
									SimpleDateFormat formatoFechaSoloFecha = new SimpleDateFormat("yyyy-MM-dd");
									String valorCampoDatoDS= formatoFechaSoloFecha.format(valorCampoDatoD);
					                String valorCampoFiltroDS = formatoFechaSoloFecha.format(valorCampoFiltroD);
					                
					                valorCampoDatoD = formatoFechaSoloFecha.parse(valorCampoDatoDS);
					                valorCampoFiltroD = formatoFechaSoloFecha.parse(valorCampoFiltroDS);
								} 
								
								if(operador.equals("eq") && valorCampoDatoD.compareTo(valorCampoFiltroD) == 0);
							    else if(operador.equals("neq") && valorCampoDatoD.compareTo(valorCampoFiltroD) != 0);
								else if(operador.equals("gte") && valorCampoDatoD.compareTo(valorCampoFiltroD) >= 0);
								else if(operador.equals("gt") && valorCampoDatoD.compareTo(valorCampoFiltroD) == 1);
								else if(operador.equals("lte") && valorCampoDatoD.compareTo(valorCampoFiltroD) <= 0);
								else if(operador.equals("lt") && valorCampoDatoD.compareTo(valorCampoFiltroD) == -1);
								else cumpleConTodosLosFiltros = false;
								break;
							default:
								break;
						}
					}
				}
				if (cumpleConTodosLosFiltros) {
					datosFiltrados.add(dato);
	            }
			}
			
			if(ordenDatos != null && ordenDatos.size() > 0) {
				datosFiltrados = aplicarOrdenGrilla(datosFiltrados, ordenDatos);
			}
	        
			return (datosFiltrados != null && datosFiltrados.size() > 0) ? datosFiltrados : datos;
		} catch (Exception e) {
			e.printStackTrace();
			return datos;
		} finally {
			logger.trace("FIN::ExportarGrillaCsv.aplicarFiltrosGrilla");
		}
	} 
	
	private JsonArray aplicarOrdenGrilla(JsonArray datosFiltrados, JsonArray ordenDatos) {
        JsonArray arrayOrdenado = new JsonArray();
        JsonElement orden = ordenDatos.get(0);
        String field = orden.getAsJsonObject().get("field").getAsString();
        String dir = orden.getAsJsonObject().get("dir").getAsString();
        ArrayList<JsonObject> listaJsonObj = new ArrayList<>();
        
        for (int i = 0; i < datosFiltrados.size(); i++) {
        	listaJsonObj.add((JsonObject) datosFiltrados.get(i));
        }

        Comparator<JsonObject> comparador = (o1, o2) -> {
            JsonElement e1 = o1.get(field);
            JsonElement e2 = o2.get(field);
            if (e1.isJsonPrimitive() && e2.isJsonPrimitive()) {
                if (e1.getAsJsonPrimitive().isNumber() && e2.getAsJsonPrimitive().isNumber()) {
                    BigDecimal num1 = e1.getAsBigDecimal();
                    BigDecimal num2 = e2.getAsBigDecimal();
                    return num1.compareTo(num2);
                } else {
                    String str1 = e1.getAsString();
                    String str2 = e2.getAsString();
                    return str1.compareToIgnoreCase(str2);
                }
            }
            return 0;
        };
        
        if (dir.equalsIgnoreCase("desc")) {
        	comparador = comparador.reversed();
        }
        
        Collections.sort(listaJsonObj, comparador);

        for (int i = 0; i < datosFiltrados.size(); i++) {
        	arrayOrdenado.add(listaJsonObj.get(i));
        }
        
        return arrayOrdenado;
    }
	
	@Override
	public void onError(Throwable t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}
	
	public static void main(String[] args) {
		ExportarGrillaCsv exportarGrilla = new ExportarGrillaCsv();
//		try(Connection conn = PlataformaUtils.getBOLSAConnection()){
		try(Connection conn = PlataformaUtils.getPropertiesConnection("qa01", "connections.properties")){
			
			 //ODATA gestion tesoreria comprobantes
			/*
			Long jobId = 443173L;
			Long idServicio = 1020L;
			String callParameters = "{}";
			String odataParameters = "{\"$inlinecount\":\"allpages\",\"$format\":\"json\",\"$orderby\":\"IdComprobante desc\",\"$filter\":\"(FechaLiquidacion ge datetime'2020-02-28T03:00:00' and FechaLiquidacion le datetime'2020-07-28T23:59:59')\"}";
			String token = "MTphcm9qYXM6MjAyMC0wOC0xMyAyMjo1NjozNC4yNjgwMDAwOjNFNzMzNTVGRjJFNDc3MzNFMTAwMDAwMEExRUUwRjBF";
			String nombreArchivo = "GestiÃ³n de TesorerÃ­a_Comprobantes_13-08-2020";
			String columnas = "[{\"field\":\"IdComprobante\",\"title\":\"Secuencia Comprobante\",\"type\":\"number\"},{\"field\":\"NombreTipoMovimiento\",\"title\":\"Tipo Comprobante\",\"type\":\"string\"},{\"field\":\"NombreConcepto\",\"title\":\"Concepto\",\"type\":\"string\"},{\"field\":\"IdMoneda\",\"title\":\"Moneda\",\"type\":\"string\"},{\"field\":\"IdComprobanteOrigen\",\"title\":\"Secuencia Comprobante Origen\",\"type\":\"number\"},{\"field\":\"Monto\",\"title\":\"Monto\",\"type\":\"number\"},{\"field\":\"FechaLiquidacion\",\"title\":\"Fecha LiquidaciÃ³n\",\"type\":\"date\",\"bcstype\":\"sysdate\"},{\"field\":\"NombrePersona\",\"title\":\"Socio Negocio\",\"type\":\"string\"},{\"field\":\"DocumentoIdentidad\",\"title\":\"Documento Identidad\",\"type\":\"string\"},{\"field\":\"NombrePortafolio\",\"title\":\"Portafolio\",\"type\":\"string\"},{\"field\":\"NombreTipoSocioDeNegocio\",\"title\":\"Tipo Socio Negocio\",\"type\":\"string\"},{\"field\":\"NombrePersonaTitularPago\",\"title\":\"Persona pago cobro\",\"type\":\"string\"},{\"field\":\"NombrePortafolioTitularPago\",\"title\":\"Portafolio pago cobro\",\"type\":\"string\"},{\"field\":\"MueveCuentaInversion\",\"title\":\"Mueve CTA\",\"type\":\"string\"},{\"field\":\"NombreCaja\",\"title\":\"Caja\",\"type\":\"string\"},{\"field\":\"ComprobanteNeteado\",\"title\":\"Neteado\",\"type\":\"string\"},{\"field\":\"Autorizacion\",\"title\":\"AutorizaciÃ³n\",\"type\":\"string\"},{\"field\":\"NombreEstadoAutorizacion\",\"title\":\"Estado AutorizaciÃ³n\",\"type\":\"string\"},{\"field\":\"NombreUsuarioLiquidacion\",\"title\":\"Liquidado por\",\"type\":\"string\"},{\"field\":\"FechaIngresoRegistro\",\"title\":\"Fecha Ingreso\",\"type\":\"date\",\"bcstype\":\"datetime\"},{\"field\":\"FechaHoraAnulacion\",\"title\":\"Fecha AnulaciÃ³n\",\"type\":\"date\",\"bcstype\":\"datetime\"},{\"field\":\"NombreMedioDePago\",\"title\":\"Medio de Pago\",\"type\":\"string\"},{\"field\":\"NombreBancoCorredora\",\"title\":\"Banco\",\"type\":\"string\"},{\"field\":\"AliasBanco\",\"title\":\"Alias Cuenta Banco\",\"type\":\"string\"},{\"field\":\"NombreTipoCuentaBancariaCorredora\",\"title\":\"Tipo Cuenta Banco\",\"type\":\"string\"},{\"field\":\"NumeroCuentaBancariaCorredora\",\"title\":\"NÃºmero de Cuenta\",\"type\":\"string\"},{\"field\":\"CuentaDCVOrigen\",\"title\":\"Cuenta DCV Origen\",\"type\":\"string\"},{\"field\":\"NombreBancoSocio\",\"title\":\"Banco\",\"type\":\"string\"},{\"field\":\"NombreTipoCuentaBancariaSocio\",\"title\":\"Tipo Cuenta Banco\",\"type\":\"string\"},{\"field\":\"NumeroCuentaBancariaSocio\",\"title\":\"NÃºmero de Cuenta\",\"type\":\"string\"},{\"field\":\"CuentaDCVDestino\",\"title\":\"Cuenta DCV Destino\",\"type\":\"string\"},{\"field\":\"NombreEstado\",\"title\":\"Estado\",\"type\":\"string\"},{\"field\":\"IdTipoCanalIngreso\",\"title\":\"Tipo Canal Ingreso\",\"type\":\"string\"}]";
			String r = "";
			
			//odata gestion operaciones mesa
			Long jobId = 443178L;
			Long idServicio = 700187L;
			String callParameters = "{}";
			String odataParameters = "{\"$inlinecount\":\"allpages\",\"$format\":\"json\",\"$orderby\":\"IdTipoInstrumento desc,IdOperacionSpotVigente desc,IdOperacion desc,IdCartera desc\",\"$filter\":\"(FechaOperacion ge datetime'2020-05-28T00:00:00' and FechaOperacion le datetime'2020-07-28T23:59:59' and (FechaRespaldo eq '0001-01-01' or Mesa eq 'MEME'))\"}";
			String token = "MTphcm9qYXM6MjAyMC0wOC0xNyAxMjozNDowNy43MzEwMDAwOjM5NTQzQTVGOTY5Qjc3MzNFMTAwMDAwMEExRUUwRjBF";
			String columnas = "W3siZmllbGQiOiJJZE9yZGVuIiwidGl0bGUiOiJTZWN1ZW5jaWEgT3JkZW4iLCJ0eXBlIjoibnVtYmVyIn0seyJmaWVsZCI6IkZlY2hhT3BlcmFjaW9uT3JkZW4iLCJ0aXRsZSI6IkZlY2hhIE9yZGVuIiwidHlwZSI6ImRhdGUiLCJiY3N0eXBlIjoiZGF0ZXRpbWUifSx7ImZpZWxkIjoiSWRDbGFzaWZpY2FjaW9uVGlwb0luc3RydW1lbnRvIiwidGl0bGUiOiJNZXJjYWRvIiwidHlwZSI6InN0cmluZyJ9LHsiZmllbGQiOiJBY2Npb25UaXBvT3BlcmFjaW9uIiwidGl0bGUiOiJPcGVyYWNpw7NuIiwidHlwZSI6InN0cmluZyJ9LHsiZmllbGQiOiJOb21icmVUaXBvT3BlcmFjaW9uIiwidGl0bGUiOiJUaXBvIE9wZXJhY2nDs24iLCJ0eXBlIjoic3RyaW5nIn0seyJmaWVsZCI6Ikluc3RydW1lbnRvVUkiLCJ0aXRsZSI6Ik5lbW90w6ljbmljbyIsInR5cGUiOiJzdHJpbmcifSx7ImZpZWxkIjoiUHJlY2lvT3JkZW4iLCJ0aXRsZSI6IlByZWNpbyIsInR5cGUiOiJudW1iZXIifSx7ImZpZWxkIjoiUHJlY2lvU3RyaWtlIiwidGl0bGUiOiJQcmVjaW8gU3RyaWtlIiwidHlwZSI6Im51bWJlciJ9LHsiZmllbGQiOiJUaXBvUHJlY2lvIiwidGl0bGUiOiJUaXBvIFByZWNpbyIsInR5cGUiOiJzdHJpbmcifSx7ImZpZWxkIjoiQ2FudGlkYWRPcmRlbiIsInRpdGxlIjoiQ2FudGlkYWQiLCJ0eXBlIjoibnVtYmVyIn0seyJmaWVsZCI6IkNhbnRpZGFkRGlzcG9uaWJsZU9yZGVuIiwidGl0bGUiOiJTYWxkbyIsInR5cGUiOiJudW1iZXIifSx7ImZpZWxkIjoiQ29kaWdvQ29uZGljaW9uRGVMaXF1aWRhY2lvbiIsInRpdGxlIjoiQ29uZGljacOzbiBkZSBMaXF1aWRhY2nDs24iLCJ0eXBlIjoic3RyaW5nIn0seyJmaWVsZCI6Ik1vbnRvT3JkZW4iLCJ0aXRsZSI6Ik1vbnRvIiwidHlwZSI6Im51bWJlciJ9LHsiZmllbGQiOiJJZE1vbmVkYU9yZGVuIiwidGl0bGUiOiJNb25lZGEiLCJ0eXBlIjoic3RyaW5nIn0seyJmaWVsZCI6IklkTW9uZWRhTGlxdWlkYWNpb25PcmRlbiIsInRpdGxlIjoiTW9uZWRhIExpcXVpZGFjacOzbiIsInR5cGUiOiJzdHJpbmcifSx7ImZpZWxkIjoiRXNWZW50YUNvcnRhVUkiLCJ0aXRsZSI6IlZlbnRhIENvcnRhIiwidHlwZSI6InN0cmluZyJ9LHsiZmllbGQiOiJEb2N1bWVudG9JZGVudGlkYWQiLCJ0aXRsZSI6IkRvY3VtZW50byBJZGVudGlkYWQiLCJ0eXBlIjoic3RyaW5nIn0seyJmaWVsZCI6Ik5vbWJyZUNvbXBsZXRvIiwidGl0bGUiOiJDbGllbnRlIiwidHlwZSI6InN0cmluZyJ9LHsiZmllbGQiOiJDb2RpZ29Qb3J0YWZvbGlvIiwidGl0bGUiOiJQb3J0YWZvbGlvIiwidHlwZSI6InN0cmluZyJ9LHsiZmllbGQiOiJSYXpvblNvY2lhbFBvcnRhZm9saW8iLCJ0aXRsZSI6IlJhesOzbiBTb2NpYWwgUG9ydGFmb2xpbyIsInR5cGUiOiJzdHJpbmcifSx7ImZpZWxkIjoiRXN0YWRvT3JkZW4iLCJ0aXRsZSI6IkVzdGFkbyIsInR5cGUiOiJzdHJpbmcifSx7ImZpZWxkIjoiRmVjaGFWaWdlbmNpYSIsInRpdGxlIjoiRmVjaGEgZGUgVmlnZW5jaWEiLCJ0eXBlIjoiZGF0ZSIsImJjc3R5cGUiOiJzeXNkYXRlIn0seyJmaWVsZCI6IkZlY2hhVmVuY2ltaWVudG8iLCJ0aXRsZSI6IkZlY2hhIFZlbmNpbWllbnRvIiwidHlwZSI6ImRhdGUiLCJiY3N0eXBlIjoic3lzZGF0ZSJ9LHsiZmllbGQiOiJUYXNhIiwidGl0bGUiOiJUYXNhIiwidHlwZSI6Im51bWJlciJ9LHsiZmllbGQiOiJOb21icmVUaXBvVmlnZW5jaWFPcmRlbiIsInRpdGxlIjoiVGlwbyBWaWdlbmNpYSIsInR5cGUiOiJzdHJpbmcifSx7ImZpZWxkIjoiT3JkZW5WaWdlbnRlVUkiLCJ0aXRsZSI6Ik9yZGVuIFZpZ2VudGUiLCJ0eXBlIjoic3RyaW5nIn0seyJmaWVsZCI6IkZlY2hhUmVjZXBjaW9uIiwidGl0bGUiOiJGZWNoYSBSZWNlcGNpw7NuIiwidHlwZSI6ImRhdGUiLCJiY3N0eXBlIjoiZGF0ZXRpbWUifSx7ImZpZWxkIjoiTm9tYnJlRm9ybWFDdXN0b2RpYSIsInRpdGxlIjoiRm9ybWEgQ3VzdG9kaWEiLCJ0eXBlIjoic3RyaW5nIn0seyJmaWVsZCI6IkNvZGlnb0VtcGxlYWRvUG9ydGFmb2xpbyIsInRpdGxlIjoiQ29kaWdvIEVqZWN1dGl2byBQb3J0YWZvbGlvIiwidHlwZSI6InN0cmluZyJ9LHsiZmllbGQiOiJOb21icmVFbXBsZWFkb1BvcnRhZm9saW8iLCJ0aXRsZSI6IkVqZWN1dGl2byBQb3J0YWZvbGlvIiwidHlwZSI6InN0cmluZyJ9LHsiZmllbGQiOiJOb21icmVTdWN1cnNhbEVtcGxlYWRvUG9ydGFmb2xpbyIsInRpdGxlIjoiU3VjdXJzYWwgRWplY3V0aXZvIFBvcnRhZm9saW8iLCJ0eXBlIjoic3RyaW5nIn0seyJmaWVsZCI6IkNvZGlnb0VtcGxlYWRvRGlnaXRhZG9yIiwidGl0bGUiOiJDb2RpZ28gRWplY3V0aXZvIERpZ2l0YWRvciIsInR5cGUiOiJzdHJpbmcifSx7ImZpZWxkIjoiTm9tYnJlRW1wbGVhZG9EaWdpdGFkb3IiLCJ0aXRsZSI6IkVtcGxlYWRvIERpZ2l0YWRvciIsInR5cGUiOiJzdHJpbmcifSx7ImZpZWxkIjoiTm9tYnJlU3VjdXJzYWxFbXBsZWFkb0RpZ2l0YWRvciIsInRpdGxlIjoiU3VjdXJzYWwgRWplY3V0aXZvIERpZ2l0YWRvciIsInR5cGUiOiJzdHJpbmcifSx7ImZpZWxkIjoiQ29kaWdvUHJvY2VkZW5jaWFPcmRlbiIsInRpdGxlIjoiUHJvY2VkZW5jaWEiLCJ0eXBlIjoic3RyaW5nIn0seyJmaWVsZCI6Ik5vbWJyZUNsYXNlT3JkZW4iLCJ0aXRsZSI6Ik1lY2FuaXNtbyBkZSBDYXB0YWNpw7NuIiwidHlwZSI6InN0cmluZyJ9LHsiZmllbGQiOiJOb21icmVDYW5hbCIsInRpdGxlIjoiQ2FuYWwgZGUgSW5ncmVzbyIsInR5cGUiOiJzdHJpbmcifSx7ImZpZWxkIjoiTm9tYnJlQ2FuYWxEaXN0cmlidWNpb24iLCJ0aXRsZSI6IkNhbmFsIGRlIERpc3RyaWJ1Y2nDs24iLCJ0eXBlIjoic3RyaW5nIn0seyJmaWVsZCI6IklkQm9sc2EiLCJ0aXRsZSI6IkJvbHNhIiwidHlwZSI6InN0cmluZyJ9LHsiZmllbGQiOiJFc1Bvck1vbnRvVUkiLCJ0aXRsZSI6IlBvciBNb250byIsInR5cGUiOiJzdHJpbmcifSx7ImZpZWxkIjoiSWRUaXBvTGlicm9PcmRlbiIsInRpdGxlIjoiVGlwbyBkZSBMaWJybyIsInR5cGUiOiJzdHJpbmcifSx7ImZpZWxkIjoiTm9tYnJlTGlicm9PcmRlbmVzIiwidGl0bGUiOiJMaWJybyBkZSBPcmRlbmVzIiwidHlwZSI6InN0cmluZyJ9LHsiZmllbGQiOiJFc3RhZG9PcmRlbkFzaWduYWNpb24iLCJ0aXRsZSI6IkVzdGFkbyBBc2lnbmFjacOzbiIsInR5cGUiOiJzdHJpbmcifSx7ImZpZWxkIjoiSWRUaXBvSW5zdHJ1bWVudG8iLCJ0aXRsZSI6IlRpcG8gSW5zdHJ1bWVudG8iLCJ0eXBlIjoic3RyaW5nIn0seyJmaWVsZCI6IkNhbnRpZGFkQXNpZ25hZGFPcmRlbiIsInRpdGxlIjoiQ2FudGlkYWQgQXNpZ25hZGEiLCJ0eXBlIjoibnVtYmVyIn0seyJmaWVsZCI6IkFzaWduYWRvRGlhIiwidGl0bGUiOiJBc2lnbmFkbyBEw61hIiwidHlwZSI6Im51bWJlciJ9LHsiZmllbGQiOiJGb2xpb09yZGVuIiwidGl0bGUiOiJGb2xpbyBPcmRlbiIsInR5cGUiOiJudW1iZXIifSx7ImZpZWxkIjoiRmVjaGFPZmVydGEiLCJ0aXRsZSI6IkZlY2hhIE9mZXJ0YSIsInR5cGUiOiJkYXRlIiwiYmNzdHlwZSI6InN5c2RhdGV0aW1lIn0seyJmaWVsZCI6IkNvZGlnb0NvcnJlZG9yQ29ycmVzcG9uc2FsIiwidGl0bGUiOiJDb2RpZ28gQ29ycmVkb3IgQ29ycmVzcG9uc2FsIiwidHlwZSI6InN0cmluZyJ9LHsiZmllbGQiOiJHbG9zYU9yZGVuIiwidGl0bGUiOiJHbG9zYSIsInR5cGUiOiJzdHJpbmcifSx7ImZpZWxkIjoiTm9tYnJlQXV0b3JpemFkb3JPcmRlbiIsInRpdGxlIjoiQXV0b3JpemFkbyBPcmRlbiIsInR5cGUiOiJzdHJpbmcifSx7ImZpZWxkIjoiTWVyY2Fkb1NNViIsInRpdGxlIjoiTWVyY2FkbyBTbXYiLCJ0eXBlIjoic3RyaW5nIn0seyJmaWVsZCI6Ik1vZGFsaWRhZFNNViIsInRpdGxlIjoiTW9kYWxpZGFkIFNtdiIsInR5cGUiOiJzdHJpbmcifSx7ImZpZWxkIjoiRXNDbGllbnRlRmluYWwiLCJ0aXRsZSI6IkNsaWVudGUgRmluYWwiLCJ0eXBlIjoic3RyaW5nIn0seyJmaWVsZCI6IkNvZGlnb0Jyb2tlckV4dHJhbmplcm8iLCJ0aXRsZSI6IkPDs2RpZ28gQnJva2VyIiwidHlwZSI6InN0cmluZyJ9LHsiZmllbGQiOiJOb21icmVCb3ZlZGEiLCJ0aXRsZSI6IkLDs3ZlZGEiLCJ0eXBlIjoic3RyaW5nIn0seyJmaWVsZCI6IkNvZGlnb0JvdmVkYSIsInRpdGxlIjoiQ3VlbnRhIELDs3ZlZGEiLCJ0eXBlIjoic3RyaW5nIn0seyJmaWVsZCI6IkluZGljYWRvckJhbmNvQ3VzdG9kaW8iLCJ0aXRsZSI6IkxpcXVpZGEgQmFuY28gQ3VzdG9kaW8iLCJ0eXBlIjoic3RyaW5nIn0seyJmaWVsZCI6IkNvZGlnb0JhbmNvQ3VzdG9kaW8iLCJ0aXRsZSI6IkPDs2RpZ28gQmFuY28gQ3VzdG9kaW8iLCJ0eXBlIjoic3RyaW5nIn0seyJmaWVsZCI6IkNvZGlnb0JhbmNvTGlxdWlkYWRvciIsInRpdGxlIjoiQ8OzZGlnbyBCYW5jbyBMaXF1aWRhZG9yIiwidHlwZSI6InN0cmluZyJ9LHsiZmllbGQiOiJOb21icmVNZWRpb0RlUGFnbyIsInRpdGxlIjoiTWVkaW8gZGUgUGFnbyIsInR5cGUiOiJzdHJpbmcifSx7ImZpZWxkIjoiTW9kYWxpZGFkTGlxdWlkYWNpb24iLCJ0aXRsZSI6Ik1vZGFsaWRhZCBMaXF1aWRhY2nDs24iLCJ0eXBlIjoic3RyaW5nIn0seyJmaWVsZCI6Ik1vZGFsaWRhZEVqZXJjaWNpbyIsInRpdGxlIjoiTW9kYWxpZGFkIEVqZXJjaWNpbyIsInR5cGUiOiJzdHJpbmcifSx7ImZpZWxkIjoiVGlwb09wY2lvbiIsInRpdGxlIjoiVGlwbyBPcGNpw7NuIiwidHlwZSI6InN0cmluZyJ9LHsiZmllbGQiOiJFc3RpbG9PcGNpb24iLCJ0aXRsZSI6IkVzdGlsbyIsInR5cGUiOiJzdHJpbmcifSx7ImZpZWxkIjoiVGFtYW5vIiwidGl0bGUiOiJMb3RlIFBhZHLDs24iLCJ0eXBlIjoibnVtYmVyIn0seyJmaWVsZCI6IklkU2lzdGVtYUJ1cnNhdGlsIiwidGl0bGUiOiJTaXN0ZW1hIEJ1cnPDoXRpbCIsInR5cGUiOiJzdHJpbmcifSx7ImZpZWxkIjoiUHJvZHVjdG8iLCJ0aXRsZSI6IlByb2R1Y3RvIiwidHlwZSI6InN0cmluZyJ9LHsiZmllbGQiOiJGZWNoYUFjZXB0YWNpb24iLCJ0aXRsZSI6IkZlY2hhIEFjZXB0YWNpw7NuIiwidHlwZSI6ImRhdGUiLCJiY3N0eXBlIjoiZGF0ZXRpbWUifSx7ImZpZWxkIjoiTW9udG9PcmRlblBsYXpvIiwidGl0bGUiOiJNb250byBQbGF6byIsInR5cGUiOiJudW1iZXIifSx7ImZpZWxkIjoiSW50ZXJlcyIsInRpdGxlIjoiSW50ZXJlcyIsInR5cGUiOiJudW1iZXIifSx7ImZpZWxkIjoiRW5BbnVsYWNpb24iLCJ0aXRsZSI6IlNvbGljaXR1ZCBkZSBBbnVsYWNpw7NuIiwidHlwZSI6InN0cmluZyJ9XQ==";
			String nombreArchivo = "Consulta Operaciones_Movimientos_13-08-2020";
			String r = "";
			*/
			
			//CALL
			Long jobId = 1444825L;
			Long idServicio = 6024L;
			String callParameters ="";
			String odataParameters = "{\"$inlinecount\":\"allpages\",\"$format\":\"json\",\"$orderby\":\"FechaDeposito\",\"$filter\":\"(FechaDeposito ge '2025-01-02' and FechaDeposito le '2025-02-18')\"}";
			String token = "MTpzaXN0ZW1hOjIwMjUtMDItMjYgMTI6NDk6MzkuODg4MDZmNzMxOC1kNWYyLTRhNWUtODQ3Yi1mMjIzYWQzMTllN2E=";
			String columnas = "W3siZmllbGQiOiJJZENvbXByb2JhbnRlIiwidGl0bGUiOiJTZWN1ZW5jaWEgQ29tcHJvYmFudGUiLCJ0eXBlIjoibnVtYmVyIn0seyJmaWVsZCI6Ik5vbWJyZUJhbmNvQ29ycmVkb3JhIiwidGl0bGUiOiJCYW5jbyIsImhlYWRlclRpdGxlIjoiVUlfQ09SUkVET1JBX1RFU1QiLCJ0eXBlIjoic3RyaW5nIn0seyJmaWVsZCI6IkFsaWFzQmFuY28iLCJ0aXRsZSI6IkFsaWFzIEN1ZW50YSBCYW5jbyIsImhlYWRlclRpdGxlIjoiVUlfQ09SUkVET1JBX1RFU1QiLCJ0eXBlIjoic3RyaW5nIn0seyJmaWVsZCI6Ik5vbWJyZVRpcG9DdWVudGFCYW5jYXJpYUNvcnJlZG9yYSIsInRpdGxlIjoiVGlwbyBDdWVudGEgQmFuY28iLCJoZWFkZXJUaXRsZSI6IlVJX0NPUlJFRE9SQV9URVNUIiwidHlwZSI6InN0cmluZyJ9LHsiZmllbGQiOiJOdW1lcm9DdWVudGFCYW5jYXJpYUNvcnJlZG9yYSIsInRpdGxlIjoiTsO6bWVybyBkZSBDdWVudGEiLCJoZWFkZXJUaXRsZSI6IlVJX0NPUlJFRE9SQV9URVNUIiwidHlwZSI6InN0cmluZyJ9LHsiZmllbGQiOiJDdWVudGFEQ1ZPcmlnZW4iLCJ0aXRsZSI6IkN1ZW50YSBEQ1YgT3JpZ2VuIiwiaGVhZGVyVGl0bGUiOiJVSV9DT1JSRURPUkFfVEVTVCIsInR5cGUiOiJzdHJpbmcifSx7ImZpZWxkIjoiTm9tYnJlVGlwb01vdmltaWVudG8iLCJ0aXRsZSI6IlRpcG8gQ29tcHJvYmFudGUiLCJ0eXBlIjoic3RyaW5nIn0seyJmaWVsZCI6Ik5lbW90ZWNuaWNvIiwidGl0bGUiOiJJbnN0cnVtZW50byIsInR5cGUiOiJzdHJpbmcifSx7ImZpZWxkIjoiSWRDb25jZXB0byIsInRpdGxlIjoiSWQgQ29uY2VwdG8iLCJ0eXBlIjoic3RyaW5nIn0seyJmaWVsZCI6Ik5vbWJyZUNvbmNlcHRvIiwidGl0bGUiOiJDb25jZXB0byIsInR5cGUiOiJzdHJpbmcifSx7ImZpZWxkIjoiSWRNb25lZGEiLCJ0aXRsZSI6Ik1vbmVkYSIsInR5cGUiOiJzdHJpbmcifSx7ImZpZWxkIjoiSWRDb21wcm9iYW50ZU9yaWdlbiIsInRpdGxlIjoiU2VjdWVuY2lhIENvbXByb2JhbnRlIE9yaWdlbiIsInR5cGUiOiJudW1iZXIifSx7ImZpZWxkIjoiRm9saW9Db21wcm9iYW50ZURURSIsInRpdGxlIjoiRm9saW8gRFRFIiwidHlwZSI6Im51bWJlciJ9LHsiZmllbGQiOiJNb250byIsInRpdGxlIjoiTW9udG8iLCJ0eXBlIjoibnVtYmVyIn0seyJmaWVsZCI6IkZlY2hhTGlxdWlkYWNpb24iLCJ0aXRsZSI6IkZlY2hhIExpcXVpZGFjacOzbiIsInR5cGUiOiJkYXRlIiwiYmNzdHlwZSI6InN5c2RhdGUifSx7ImZpZWxkIjoiTm9tYnJlUGVyc29uYSIsInRpdGxlIjoiU29jaW8gTmVnb2NpbyIsInR5cGUiOiJzdHJpbmcifSx7ImZpZWxkIjoiRG9jdW1lbnRvSWRlbnRpZGFkIiwidGl0bGUiOiJEb2N1bWVudG8gSWRlbnRpZGFkIiwidHlwZSI6InN0cmluZyJ9LHsiZmllbGQiOiJOb21icmVQb3J0YWZvbGlvIiwidGl0bGUiOiJQb3J0YWZvbGlvIiwidHlwZSI6InN0cmluZyJ9LHsiZmllbGQiOiJOb21icmVUaXBvU29jaW9EZU5lZ29jaW8iLCJ0aXRsZSI6IlRpcG8gU29jaW8gTmVnb2NpbyIsInR5cGUiOiJzdHJpbmcifSx7ImZpZWxkIjoiTm9tYnJlUGVyc29uYVRpdHVsYXJQYWdvIiwidGl0bGUiOiJQZXJzb25hIHBhZ28gY29icm8iLCJ0eXBlIjoic3RyaW5nIn0seyJmaWVsZCI6Ik5vbWJyZVBvcnRhZm9saW9UaXR1bGFyUGFnbyIsInRpdGxlIjoiUG9ydGFmb2xpbyBwYWdvIGNvYnJvIiwidHlwZSI6InN0cmluZyJ9LHsiZmllbGQiOiJNdWV2ZUN1ZW50YUludmVyc2lvbiIsInRpdGxlIjoiTXVldmUgQ1RBIiwidHlwZSI6InN0cmluZyJ9LHsiZmllbGQiOiJOb21icmVDYWphIiwidGl0bGUiOiJDYWphIiwidHlwZSI6InN0cmluZyJ9LHsiZmllbGQiOiJDb21wcm9iYW50ZU5ldGVhZG8iLCJ0aXRsZSI6Ik5ldGVhZG8iLCJ0eXBlIjoic3RyaW5nIn0seyJmaWVsZCI6IlNvbGljaXR1ZEF1dG9yaXphY2lvbiIsInRpdGxlIjoiU29saWNpdHVkIEF1dG9yaXphY2nDs24iLCJ0eXBlIjoic3RyaW5nIn0seyJmaWVsZCI6Ik5vbWJyZUVzdGFkb0F1dG9yaXphY2lvbiIsInRpdGxlIjoiRXN0YWRvIEF1dG9yaXphY2nDs24iLCJ0eXBlIjoic3RyaW5nIn0seyJmaWVsZCI6Ik5vbWJyZVVzdWFyaW9MaXF1aWRhY2lvbiIsInRpdGxlIjoiTGlxdWlkYWRvIHBvciIsInR5cGUiOiJzdHJpbmcifSx7ImZpZWxkIjoiRmVjaGFJbmdyZXNvUmVnaXN0cm8iLCJ0aXRsZSI6IkZlY2hhIEluZ3Jlc28iLCJ0eXBlIjoiZGF0ZSIsImJjc3R5cGUiOiJkYXRldGltZSJ9LHsiZmllbGQiOiJGZWNoYUhvcmFBbnVsYWNpb24iLCJ0aXRsZSI6IkZlY2hhIEFudWxhY2nDs24iLCJ0eXBlIjoiZGF0ZSIsImJjc3R5cGUiOiJkYXRldGltZSJ9LHsiZmllbGQiOiJOb21icmVNZWRpb0RlUGFnbyIsInRpdGxlIjoiTWVkaW8gZGUgUGFnbyIsInR5cGUiOiJzdHJpbmcifSx7ImZpZWxkIjoiSWRDb21wcm9iYW50ZVBhcnRpZGFFZ3Jlc28iLCJ0aXRsZSI6IlNlY3VlbmNpYSBQYXJ0aWRhIiwidHlwZSI6InN0cmluZyJ9LHsiZmllbGQiOiJOb21icmVFc3RhZG9Ob21pbmEiLCJ0aXRsZSI6IkVzdGFkbyBOb21pbmEiLCJ0eXBlIjoic3RyaW5nIn0seyJmaWVsZCI6Ik5vbWJyZUVzdGFkbyIsInRpdGxlIjoiRXN0YWRvIiwidHlwZSI6InN0cmluZyJ9LHsiZmllbGQiOiJJZFRpcG9DYW5hbEluZ3Jlc28iLCJ0aXRsZSI6IlRpcG8gQ2FuYWwgSW5ncmVzbyIsInR5cGUiOiJzdHJpbmcifSx7ImZpZWxkIjoiQWxpYXMiLCJ0aXRsZSI6IkFsaWFzIC8gUmF6w7NuIFNvY2lhbCIsInR5cGUiOiJzdHJpbmcifSx7ImZpZWxkIjoiQ29uZGljaW9uRGVMaXF1aWRhY2lvbiIsInRpdGxlIjoiQ29uZC4gRGUgTGlxdWlkYWNpb24iLCJ0eXBlIjoic3RyaW5nIn0seyJmaWVsZCI6Ikdsb3NhIiwidGl0bGUiOiJDb21lbnRhcmlvIiwidHlwZSI6InN0cmluZyJ9LHsiZmllbGQiOiJOb21icmVCYW5jb1NvY2lvIiwidGl0bGUiOiJCYW5jbyIsImhlYWRlclRpdGxlIjoiU29jaW8gTmVnb2NpbyIsInR5cGUiOiJzdHJpbmcifSx7ImZpZWxkIjoiTm9tYnJlVGlwb0N1ZW50YUJhbmNhcmlhU29jaW8iLCJ0aXRsZSI6IlRpcG8gQ3VlbnRhIEJhbmNvIiwiaGVhZGVyVGl0bGUiOiJTb2NpbyBOZWdvY2lvIiwidHlwZSI6InN0cmluZyJ9LHsiZmllbGQiOiJOdW1lcm9DdWVudGFCYW5jYXJpYVNvY2lvIiwidGl0bGUiOiJOw7ptZXJvIGRlIEN1ZW50YSIsImhlYWRlclRpdGxlIjoiU29jaW8gTmVnb2NpbyIsInR5cGUiOiJzdHJpbmcifSx7ImZpZWxkIjoiQ3VlbnRhRENWRGVzdGlubyIsInRpdGxlIjoiQ3VlbnRhIERDViBEZXN0aW5vIiwiaGVhZGVyVGl0bGUiOiJTb2NpbyBOZWdvY2lvIiwidHlwZSI6InN0cmluZyJ9XQ==";
			String nombreArchivo = "Gestión De Recaudación_DISTRIBUCIÓN_26-02-2025";
			String r = "";
			String gridFilters = "{}";
			String gridSort = "{}";
			
			exportarGrilla.DEBUG = true;
			exportarGrilla.logica(conn, idServicio, callParameters, odataParameters, token, nombreArchivo, columnas, r, jobId, gridFilters, gridSort);
			conn.rollback();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
