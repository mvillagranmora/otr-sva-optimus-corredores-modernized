package cl.bcs.contabilidad;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.contabilidad.BcsCentralizacion.EntradaCentralizacion;
import cl.bcs.contabilidad.BcsCentralizacion.EntradaCentralizacionInfoAdicional;
import cl.bcs.contabilidad.BcsCentralizacion.SalidaCentralizacion;
import cl.bcs.contabilidad.CentralizaGeneraCabecera.EntradaCentralizaGeneraCabecera;
import cl.bcs.contabilidad.CentralizaGeneraCabecera.SalidaCentralizaGeneraCabecera;
import cl.bcs.contabilidad.CentralizaGeneraCabecera.TipoCentralizacion;
import cl.bcs.contabilidad.CentralizaGenerico.EntradaCentralizaGenerico;
import cl.bcs.contabilidad.CentralizaGenerico.SalidaCentralizaGenerico;
import cl.bcs.contabilidad.pojo.ResultadoCentralizacion;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CentralizaMultiSeleccion implements JavaCall{
	
	private final Logger log = LogManager.getLogger(CentralizaMultiSeleccion.class);
	private final String SQL_SELECT_IDINFORMERANKEADO = loadResourceToString("cl/bcs/contabilidad/sql/Centraliza_SelectInformesValorizados.sql");
	private final String SQL_SELECT_CONFIGURACIONES = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaMultiSeleccion_SelectConfiguraciones.sql");
	private final String SQL_SELECT_CALL_CENTRALIZACION = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaMultiSeleccion_CallCentralizacion.sql");
	private final String SQL_SELECT_MENSAJES = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaMultiSeleccion_SelectMensajes.sql");
	private final String SQL_SELECT_CENTRALIZACIONES_VIGENTES = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaMultiSeleccion_SelectCentralizacionesVigentes.sql");
	private final String SQL_DELETE_DATOS_MOVIMIENTOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaMultiSeleccion_DeleteDatosMovimientos.sql");
	private final String SQL_SELECT_RUTAS_INFORMES =  loadResourceToString("cl/bcs/contabilidad/sql/CentralizaMultiSeleccion_SelectRutaInformes.sql");
	
	public static class EntradaCentralizaMultiSeleccion {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_FechaContable;
		public String p_FechaConsulta;
		public Long p_Ejercicio;
		public String p_SimulaOperacion;
		public List<InTiposCentralizacion> p_in_TiposCentralizacion;
		
		public EntradaCentralizaMultiSeleccion(){
			this.p_SimulaOperacion = "N";
			this.p_in_TiposCentralizacion = new ArrayList<InTiposCentralizacion>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InTiposCentralizacion{
		public Long IdTipoCentralizacion;
		
		public InTiposCentralizacion() {
			
		}

		public InTiposCentralizacion(Long IdTipoCentralizacion) {
			this.IdTipoCentralizacion=IdTipoCentralizacion;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCentralizaMultiSeleccion{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<ResultadoCentralizacion> P_OUT_RESULTADO;
		
		public SalidaCentralizaMultiSeleccion(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<ResultadoCentralizacion>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ConfiguracionCentralizacionTipo{
		public Long IdTipoCentralizacion;
		public String Nombre;
		public String VistaMovimiento;
		public String VistaEvento;
		public String VistaMovimientoDato;
		public String Servicio;
		public String Tipo;
		public String IdInforme;
		public Long IdTipoCentralizacionOrigen;
		public String RequiereFechaConsulta;
		public String InformeAsociado;
		public String LlaveInformeAsociado;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CodigosMensajes{
		public Long IdInterno;
		public Long IdSesion;
		public String Codigo;
		public String CodigoDato1;
		public String ValorDato1;
		public String CodigoDato2;
		public String ValorDato2;
		public String Resultado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CentralizacionesVigentes{
		public String Nombre;
		public Long IdTipoCentralizacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class InformeValorizado{
		public String IdInforme;
	}

	private static Map<Long, String> idsCentralizacion;

	private static String getIdCentralizacion(Long clave) {
		return idsCentralizacion.get(clave);
	}
	   
    private static Map<Long, String> rutasCentralizacion;
    
	private static String getRutaCentralizacion(Long clave) {
		return rutasCentralizacion.get(clave);
	}
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		EntradaCentralizaMultiSeleccion in = gson.fromJson(jin.toString(), EntradaCentralizaMultiSeleccion.class);
		SalidaCentralizaMultiSeleccion out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SalidaCentralizaMultiSeleccion logica(Connection con, EntradaCentralizaMultiSeleccion entrada) throws Exception {
		log.info("INICIO::CentralizaMultiSeleccion");
		SQL sql = new SQL();
		SalidaCentralizaMultiSeleccion out = new SalidaCentralizaMultiSeleccion();
		List<ConfiguracionCentralizacionTipo> configuracionCentralizacionTipo = sql.query(con, SQL_SELECT_RUTAS_INFORMES, new HashMap<>(), ConfiguracionCentralizacionTipo.class);
		
		rutasCentralizacion = Collections.unmodifiableMap(configuracionCentralizacionTipo.stream()
				.collect(Collectors.toMap(data -> data.IdTipoCentralizacion, data -> data.InformeAsociado)));
		
		idsCentralizacion = Collections.unmodifiableMap(configuracionCentralizacionTipo.stream()
				.collect(Collectors.toMap(data -> data.IdTipoCentralizacion, data -> data.LlaveInformeAsociado)));
		
		String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String idMonedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(con, entrada.p_IdEmpresa);
		
		EntradaCentralizacionInfoAdicional infoAdicional = new  EntradaCentralizacionInfoAdicional();
		infoAdicional.p_Empresa = empresa;
		infoAdicional.p_FechaSistema = fechaSistema;
		infoAdicional.p_LocalizacionSistema = localizacionSistema;
		infoAdicional.p_IdMonedaSistema = idMonedaSistema;
		
		List<Long> idTipoCentralizacion = new ArrayList<Long>();
		
		for(InTiposCentralizacion id : entrada.p_in_TiposCentralizacion){
			if(!idTipoCentralizacion.contains(id.IdTipoCentralizacion))  idTipoCentralizacion.add(id.IdTipoCentralizacion);
		}
		
		HashMap<String, Object> params = new HashMap<String,Object>();
		
		params.put("IdTipoCentralizacion", idTipoCentralizacion);
		List<ConfiguracionCentralizacionTipo> configuraciones = sql.query(con, SQL_SELECT_CONFIGURACIONES, params, ConfiguracionCentralizacionTipo.class);
		params.clear();
		HashMap<Long,Object> hConfiguraciones = Utils.List2Hash(configuraciones, "IdTipoCentralizacion");
		
		//Volvemos a recorrer para ver las que se van a ejecutar, dado que pueden ser mas por las relacionadas
		idTipoCentralizacion.clear();
		List<ConfiguracionCentralizacionTipo> configuracionesProceso = new ArrayList<ConfiguracionCentralizacionTipo>();
		for(InTiposCentralizacion id : entrada.p_in_TiposCentralizacion){
			ConfiguracionCentralizacionTipo c = (ConfiguracionCentralizacionTipo) hConfiguraciones.get(id.IdTipoCentralizacion);
			if(c == null){
				HashMap<String,String> valores = new HashMap<String,String>();
				valores.put("Centralizacion", String.valueOf(id.IdTipoCentralizacion));
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA,"UI_CENTRALIZACION_NO_EXISTE",valores,con,entrada.p_IdEmpresa));
			}
			else{
				if(c.Tipo == null){
					HashMap<String,String> valores = new HashMap<String,String>();
					valores.put("Centralizacion", c.Nombre);
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA,"UI_CENTRALIZACION_CONFIGURACION_NO_EXISTE",valores,con,entrada.p_IdEmpresa));
				}else{
					if(!idTipoCentralizacion.contains(id.IdTipoCentralizacion)){
						idTipoCentralizacion.add(id.IdTipoCentralizacion);
						configuracionesProceso.add(c);
						//SE AGREGAN LAS RELACIONADAS
						for(ConfiguracionCentralizacionTipo conf : configuraciones) {
							if(id.IdTipoCentralizacion.equals(conf.IdTipoCentralizacionOrigen) && !idTipoCentralizacion.contains(conf.IdTipoCentralizacion)) {
								idTipoCentralizacion.add(conf.IdTipoCentralizacion);
								configuracionesProceso.add(conf);
							}
						}
					}  
				}
			}
			
		}
		
		if("N".equals(entrada.p_SimulaOperacion) && "CL".equals(localizacionSistema) ){
			params.put("IdTipoCentralizacion", idTipoCentralizacion);
			params.put("FechaContable", entrada.p_FechaContable);
			params.put("IdEmpresa", entrada.p_IdEmpresa);
			List<CentralizacionesVigentes> centralizacionVigentes = sql.query(con, SQL_SELECT_CENTRALIZACIONES_VIGENTES, params, CentralizacionesVigentes.class);
			params.clear();
			
			for(CentralizacionesVigentes centralizacion : centralizacionVigentes){
				if (entrada.p_in_TiposCentralizacion.size()> 1) {
					configuracionesProceso.removeIf(a -> a.IdTipoCentralizacion.longValue() == centralizacion.IdTipoCentralizacion);
					HashMap<String,String> valores = new HashMap<String,String>(1);
					valores.put("Centralizacion", centralizacion.Nombre);
					out.P_OUT_ERRORESCONTADOR++;
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_CENTRALIZACION_CONTABILIZADA_ANULAR",valores,con,entrada.p_IdEmpresa));
				}
				else
				{						
				HashMap<String,String> valores = new HashMap<String,String>();
				valores.put("Centralizacion", centralizacion.Nombre);
				out.P_OUT_ERRORESCONTADOR++;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_CENTRALIZACION_CONTABILIZADA_ANULAR",valores,con,entrada.p_IdEmpresa));
				if(centralizacionVigentes.size() > 0) return out;
				}
			}			
		
		
			if(!validaFechasCentralizacion(configuraciones, entrada.p_FechaConsulta, entrada.p_FechaContable)) {
				configuracionesProceso.removeIf(a -> idsCentralizacion.containsKey(a.Nombre));			
				HashMap<String,String> valores = new HashMap<String,String>(1);
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA,"FECHA_VALORIZACION_INVALIDA",valores,con,entrada.p_IdEmpresa));
			}else if (entrada.p_FechaConsulta != null && !entrada.p_FechaConsulta.isEmpty()) {
			    Iterator<ConfiguracionCentralizacionTipo> iterator = configuracionesProceso.iterator();
			    while (iterator.hasNext()) {
			        ConfiguracionCentralizacionTipo configuracionProceso = iterator.next();
			        
			        if (idsCentralizacion.keySet().stream().anyMatch(key -> key.equals(configuracionProceso.IdTipoCentralizacion))) {
			            HashMap<String, Object> key = new HashMap<>(1);
			            key.put("key", getIdCentralizacion(configuracionProceso.IdTipoCentralizacion)+ entrada.p_FechaConsulta + entrada.p_FechaContable);
			            List<InformeValorizado> informes = sql.query(con, SQL_SELECT_IDINFORMERANKEADO, key, InformeValorizado.class);
			            InformeValorizado idInforme = informes.isEmpty() ? null : informes.get(0);
			            
			            if (idInforme == null) {
			                iterator.remove();		                
			                HashMap<String, String> valores = new HashMap<>(2);
			                valores.put("Centralizacion", configuracionProceso.Nombre);
			                valores.put("Ruta", getRutaCentralizacion(configuracionProceso.IdTipoCentralizacion));
			                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION,
			                        "UI_CENTRALIZACION_INFORME_NO_EXISTE", valores, con, entrada.p_IdEmpresa));
			                out.P_OUT_ERRORESCONTADOR++;
			            }else {
			            	configuracionProceso.IdInforme = idInforme.IdInforme;
			            }
			        }
			    }
			}
		}
		
		EntradaCentralizaGeneraCabecera entradaCentralizaGeneraCabecera = new EntradaCentralizaGeneraCabecera();
		CentralizaGenerico centralizaGenerico = new CentralizaGenerico();
		EntradaCentralizaGenerico entradaCentralizaGenerico = new EntradaCentralizaGenerico();
		
		configuracionesProceso.parallelStream().forEach(conf -> {
			try {
				//PROCEDIMIENTO ALMACENADO
				if("P".equals(conf.Tipo)){
					log.debug("INICIO::EjecutarPorProcedimiento");
					HashMap<String,Object> paramsEntrada = new HashMap<String,Object>();
					paramsEntrada.put("p_IdEmpresa", entrada.p_IdEmpresa);
					paramsEntrada.put("p_IdUsuario", entrada.p_IdUsuario);
					paramsEntrada.put("p_IdModulo", entrada.p_IdModulo);
					paramsEntrada.put("p_SimulaOperacion", entrada.p_SimulaOperacion);
					paramsEntrada.put("p_IdTipoCentralizacion", conf.IdTipoCentralizacion);
					paramsEntrada.put("p_FechaContable", entrada.p_FechaContable);
					paramsEntrada.put("p_Ejercicio", entrada.p_Ejercicio);
					
					HashMap<String,Class> paramsSalida = new HashMap<String,Class>();
					paramsSalida.put("p_out_Resultado", ResultadoCentralizacion.class);
					
					HashMap<String,Object> respuesta = sql.call(con, SQL_SELECT_CALL_CENTRALIZACION.replace(":SP_CENTRALIZACION", conf.Servicio), paramsEntrada, paramsSalida);
					
					List<ResultadoCentralizacion> resultado = (List<ResultadoCentralizacion>) respuesta.get("p_out_Resultado");
					
					out.P_OUT_RESULTADO.addAll(resultado);
					
					SQL.exec(con, SQL_DELETE_DATOS_MOVIMIENTOS);
	
					log.debug("FIN::EjecutarPorProcedimiento");
				}
				
				//JAVA
				if("J".equals(conf.Tipo)){
					log.debug("INICIO::EjecutarJAVA");
					Class<?> tclass = null;
					tclass=Class.forName(conf.Servicio);
					BcsCentralizacion centralizacion = (BcsCentralizacion) tclass.newInstance();
					EntradaCentralizacion entradaCentralizacion = new EntradaCentralizacion();
					entradaCentralizacion.p_IdEmpresa = entrada.p_IdEmpresa;
					entradaCentralizacion.p_IdUsuario = entrada.p_IdUsuario;
					entradaCentralizacion.p_IdModulo = entrada.p_IdModulo;
					entradaCentralizacion.p_IdTipoCentralizacion = conf.IdTipoCentralizacion;
					entradaCentralizacion.p_FechaContable = entrada.p_FechaContable;
					entradaCentralizacion.p_IdInforme = conf.IdInforme;
					entradaCentralizacion.p_Ejercicio = entrada.p_Ejercicio;
					entradaCentralizacion.p_SimulaOperacion = entrada.p_SimulaOperacion;
					
					SalidaCentralizacion salidaCentralizacion = centralizacion.centralizar(con, entradaCentralizacion, infoAdicional);
					
					out.P_OUT_ERRORESCONTADOR = salidaCentralizacion.P_OUT_ERRORESCONTADOR;
					out.P_OUT_MENSAJES.addAll(salidaCentralizacion.P_OUT_MENSAJES);
					out.P_OUT_RESULTADO.addAll(salidaCentralizacion.P_OUT_RESULTADO);
					CentralizaGenerico.InIdTipoCentralizacion inTipoCen = new CentralizaGenerico.InIdTipoCentralizacion();
					inTipoCen.IdTipoCentralizacion = conf.IdTipoCentralizacion;
					inTipoCen.Nombre = conf.Nombre;
					entradaCentralizaGenerico.p_IdTipoCentralizacion.add(inTipoCen);
					entradaCentralizaGenerico.p_TabMovimientos.addAll(salidaCentralizacion.P_TABMOVIMIENTOS);
					entradaCentralizaGenerico.p_TabEventos.addAll(salidaCentralizacion.P_TABEVENTOS);
					entradaCentralizaGenerico.p_TabDatosMovimiento.addAll(salidaCentralizacion.P_TABDATOSMOVIMIENTO);
					entradaCentralizaGenerico.p_TabDatosEvento.addAll(salidaCentralizacion.P_TABDATOSEVENTO);
									
					log.debug("FIN::EjecutarJAVA");
				}
			}catch(Exception e) {
				log.error(e);
				TipoCentralizacion tipoCentralizacion = new TipoCentralizacion();
				tipoCentralizacion.IdTipoCentralizacion = conf.IdTipoCentralizacion;
				entradaCentralizaGeneraCabecera.p_TipoCentralizacion.add(tipoCentralizacion);
			}
		});
		
		// Guardamos cabecera si se generan centralizaciones con error controlado
		if (entradaCentralizaGeneraCabecera.p_TipoCentralizacion.stream().count() > 0) {
			CentralizaGeneraCabecera centralizaGeneraCabecera = new CentralizaGeneraCabecera();
			entradaCentralizaGeneraCabecera.p_CodigoUsuario = entrada.p_IdUsuario;
			entradaCentralizaGeneraCabecera.p_Ejercicio = entrada.p_Ejercicio;
			entradaCentralizaGeneraCabecera.p_FechaContable = entrada.p_FechaContable;
			entradaCentralizaGeneraCabecera.p_IdEmpresa = entrada.p_IdEmpresa;
			SalidaCentralizaGeneraCabecera salidaCentralizaGeneraCabecera = centralizaGeneraCabecera.logica(con, entradaCentralizaGeneraCabecera);
			out.P_OUT_MENSAJES.addAll(salidaCentralizaGeneraCabecera.P_OUT_MENSAJES);
		}
				
		//PARA PROCEDIMIENTOS
		params.put("IdTipoCentralizacion", idTipoCentralizacion);
		List<CodigosMensajes> codigosMensajes = sql.query(con, SQL_SELECT_MENSAJES, params, CodigosMensajes.class);
		params.clear();
		
		for(CodigosMensajes codigo : codigosMensajes){
			ConfiguracionCentralizacionTipo c = (ConfiguracionCentralizacionTipo) hConfiguraciones.get(codigo.IdSesion);
			
			HashMap<String,String> valores = new HashMap<String,String>();
			String tipo = MensajeSalida.MENSAJE_INFORMACION;
			
			if(c != null){
				valores.put("Centralizacion", c.Nombre);
			}
			
			if(!"".equals(codigo.CodigoDato1) && codigo.ValorDato1 != null){
				valores.put(codigo.CodigoDato1, codigo.ValorDato1);
			}
			
			if("ADVE".equals(codigo.Resultado) || "RAUT".equals(codigo.Resultado)) tipo = MensajeSalida.MENSAJE_AUTORIZACION;
			
			if("RECH".equals(codigo.Resultado)){
				out.P_OUT_ERRORESCONTADOR++;
				tipo = MensajeSalida.MENSAJE_ERROR;
			} 
			
			out.P_OUT_MENSAJES.add(new MensajeSalida(codigo.IdInterno,tipo,codigo.Codigo,valores,con,entrada.p_IdEmpresa));
			
		}
		
		//PARA JAVA
		if("N".equals(entrada.p_SimulaOperacion) && entradaCentralizaGenerico.p_IdTipoCentralizacion.size() > 0){
			entradaCentralizaGenerico.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaCentralizaGenerico.p_Ejercicio = entrada.p_Ejercicio;
			entradaCentralizaGenerico.p_FechaContable = entrada.p_FechaContable;
			entradaCentralizaGenerico.p_CodigoUsuario = String.valueOf(entrada.p_IdUsuario);
			log.debug("ANTES CENTRALIZAR GENERICO");
			SalidaCentralizaGenerico salidaCentralizaGenerico = centralizaGenerico.logica(con, entradaCentralizaGenerico);
			log.debug("DESPUES CENTRALIZAR GENERICO");
			out.P_OUT_MENSAJES.addAll(salidaCentralizaGenerico.P_OUT_MENSAJES);
			out.P_OUT_ERRORESCONTADOR += salidaCentralizaGenerico.P_OUT_ERRORESCONTADOR;
		}
		log.info("FIN::CentralizaMultiSeleccion");
		
		return out;
		
	}
	
	private static boolean validaFechasCentralizacion(List<ConfiguracionCentralizacionTipo> inCentralizacionesList, String fechaConsulta, String fechaContable) {
		
		boolean resp = true; //true porque no aplica para todas las centralizaciones
		
		if(esFechaConsultaRequerida(inCentralizacionesList)) {
			resp = validaOrdenFechas(fechaConsulta, fechaContable);
		}
		
		return resp;
	}
	
	private static boolean esFechaConsultaRequerida(List<ConfiguracionCentralizacionTipo> inCentralizacionesList) {
		return inCentralizacionesList.stream().anyMatch( x -> "S".equalsIgnoreCase(x.RequiereFechaConsulta) );
	}
	
	private static boolean validaOrdenFechas(String fechaConsulta, String fechaContable) {
		
		if(fechaConsulta != null) {
			LocalDate dateFechaContable = LocalDate.parse(fechaContable, DateTimeFormatter.ISO_LOCAL_DATE);
			LocalDate dateFechaConsulta = LocalDate.parse(fechaConsulta, DateTimeFormatter.ISO_LOCAL_DATE);
			
			return !dateFechaConsulta.isAfter(dateFechaContable);
		}else {
			return false;
		}
	}
		
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02", "connections.properties")) {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
		String jsonFull = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"CON\",\"p_FechaContable\":\"2025-08-06\",\"p_Ejercicio\":\"2025\",\"p_SimulaOperacion\":\"N\",\"p_in_TiposCentralizacion\":[{\"IdTipoCentralizacion\":371}],\"p_FechaConsulta\":\"2025-08-05\"}";
		String json = "{\n"
				+ "    \"p_IdEmpresa\": 1,\n"
				+ "    \"p_IdUsuario\": 97,\n"
				+ "    \"p_IdModulo\": \"CON\",\n"
				+ "    \"p_FechaContable\": \"2025-03-10\",\n"
				+ "    \"p_FechaConsulta\": \"2025-03-09\",\n"
				+ "    \"p_Ejercicio\": \"2025\",\n"
				+ "    \"p_SimulaOperacion\": \"N\",\n"
				+ "    \"p_in_TiposCentralizacion\": [\n"
				+ "        {\n"
				+ "            \"IdTipoCentralizacion\": 470\n"
				+ "        },\n"
				+ "        {\n"
				+ "            \"IdTipoCentralizacion\": 210\n"
				+ "        }\n"
				+ "    ]\n"
				+ "}";
		EntradaCentralizaMultiSeleccion Objin = gson.fromJson(jsonFull, EntradaCentralizaMultiSeleccion.class);
		CentralizaMultiSeleccion  demo= new CentralizaMultiSeleccion();				
		try {
			long init = System.currentTimeMillis();
			SalidaCentralizaMultiSeleccion ObjOut=demo.logica(con, Objin);
			demo.log.debug(String.format("Termino CentralizacionMultiSeleccion en %d millis", System.currentTimeMillis() - init));
			con.commit();
			//con.rollback();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			con.commit(); 			
    	}
		System.out.println("== END ==");
	}

}
