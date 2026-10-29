package cl.bcs.plataforma.tas;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.InformacionDetalle;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.bat.AgregarPrograma;
import cl.bcs.plataforma.bat.AgregarPrograma.InAgregarPrograma;
import cl.bcs.plataforma.bat.AgregarPrograma.OutAgregarPrograma;
import cl.bcs.plataforma.bat.AgregarPrograma.InAgregarPrograma.Parametro;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class SolicitudArchivoTasas implements JavaCall {
	private final String SQL_SELECT_CONFIGURACION_TASAS = loadResourceToString("cl/bcs/plataforma/tas/sql/SolicitudArchivoTasas_SelectConfiguracionTasas.sql");
	private final String SQL_SELECT_ESTADO_MESARF = loadResourceToString("cl/bcs/plataforma/tas/sql/SolicitudArchivoTasas_SelectEstadoMesaRF.sql");
	private final String SQL_CONSULTAR_FUENTE = loadResourceToString("cl/bcs/plataforma/tas/sql/SolicitudArchivoTasas_SelectNombreFuente.sql");
	private final String SQL_SELECT_DATOS_EMPRESA = loadResourceToString("cl/bcs/plataforma/tas/sql/SolicitudArchivoTasas_SelectDocumentoIdentidad.sql");
	private final String SQL_SELECT_NEMOTECNICOS_REPETIDOS = loadResourceToString("cl/bcs/plataforma/tas/sql/SolicitudArchivoTasas_SelectNemotecnicosRepetidos.sql");
	private final String SQL_SELECT_NEMOTECNICOS_NO_REPETIDOS = loadResourceToString("cl/bcs/plataforma/tas/sql/SolicitudArchivoTasas_SelectNemotecnicosNoRepetidos.sql");
	private final String SQL_SELECT_INSTRUMENTOS_ARCHIVOS = loadResourceToString("cl/bcs/plataforma/tas/sql/SolicitudArchivoTasas_SelectInstrumentosArchivos.sql");
	private final String SQL_DELETE_PRECIO_TASA = loadResourceToString("cl/bcs/plataforma/tas/sql/SolicitudArchivoTasas_DeletePrecioTasa.sql");
	private final String SQL_INSERT_PRECIO_TASA = loadResourceToString("cl/bcs/plataforma/tas/sql/SolicitudArchivoTasas_InsertPrecioTasa.sql");
	private final String SQL_SELECT_INSERT_PRECIO_TASAS = loadResourceToString("cl/bcs/plataforma/tas/sql/SolicitudArchivoTasas_SelectInsertPrecioTasa.sql");
	private final String SQL_CREAR_TEMP_INSTRUMENTOS_SELECT = "CREATE LOCAL TEMPORARY TABLE #InstrumentosSelect(\"Nemotecnico\" NVARCHAR(100), \"IdInstrumento\" BIGINT, \"IdClasificacionTipoInstrumento\" NVARCHAR(100), \"IdTipoInstrumento\" NVARCHAR(100), \"IdMoneda\" NVARCHAR(100))";
    private final String SQL_CREAR_TEMP_INSTRUMENTOS_INSERT = "INSERT INTO #InstrumentosSelect (\"Nemotecnico\",\"IdInstrumento\",\"IdClasificacionTipoInstrumento\",\"IdTipoInstrumento\",\"IdMoneda\") VALUES(:Nemotecnico,:IdInstrumento,:IdClasificacionTipoInstrumento,:IdTipoInstrumento,:IdMoneda)";
	private static final Logger logger = LogManager.getLogger(SolicitudArchivoTasas.class);
	
	/***********************************************************************************************
	 * CLASES
	 ***********************************************************************************************/
	
	public static class Entrada_SolicitudArchivoTasas {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_Fecha;
		public List<Long> p_Fuentes;
		public Entrada_SolicitudArchivoTasas() {
			p_Fuentes = new ArrayList<Long>();
		} 

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Salida_SolicitudArchivoTasas {
	    public int p_out_advertenciascontador;
	    public List<InformacionDetalle> P_OUT_ERRORESDETALLE;
	    public Long P_OUT_ERRORESCONTADOR;
	    public List<Long> P_OUT_SECUENCIA;
	    private List<MensajeSalida> P_OUT_MENSAJES;
	    public List<String> nombreArchivo;
	    
	    public Salida_SolicitudArchivoTasas() {
	        this.P_OUT_ERRORESDETALLE = new ArrayList<InformacionDetalle>();
	        this.P_OUT_SECUENCIA = new ArrayList<Long>();
	        this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
	        this.P_OUT_ERRORESCONTADOR = 0L;
	        this.nombreArchivo = new ArrayList<String>();
	    }

	    public String toString() {
	        return Utils.printJson(this, false);
	    }
	}
	
	public static class ParametrosArchivos {
		public String nombreArchivo;
		public String codigoFuente;
		public String documentoIdentidad;
		public String codigo;
		public Long idFuente;
		public String toString() {
			return Utils.printJson(this, false);
		}		
	}
	
	public static class ConfiguracionTasas{
		public String nombreEntidad;
		public String valor;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Nemotecnicos{
		public String nemotecnico;
	}
	
	public static class Instrumentos{
		public String Nemotecnico;
		public int IdInstrumento;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public String IdMoneda;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InsertInstrumentosTasas{
		public String Nemotecnico;
		public int IdInstrumento;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public String IdMoneda;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class PrecioTasaEliminar{
		public List<String> p_CodigosFuentes;
		public String p_Fecha;
		public Long p_IdEmpresa;
		public PrecioTasaEliminar(){
			this.p_CodigosFuentes = new ArrayList<>();
		}
	}
	
	public static class PrecioTasaInsertar{
		public Long IdFuente;
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String RutaArchivo;
		public String NombreArchivo;
		public String FechaSistema;
		public String FechaHoraUI;
		public List<String> instrumentos;
	}
	

	/***********************************************************************************************
	 * CALL
	 ***********************************************************************************************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		Entrada_SolicitudArchivoTasas in = gson.fromJson(jin.toString(),
				Entrada_SolicitudArchivoTasas.class);
		Salida_SolicitudArchivoTasas out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();

		return dataout;
	}

	/***********************************************************************************************
	 * LOGICA
	 ***********************************************************************************************/
	@Trace
	public Salida_SolicitudArchivoTasas logica(Connection con,	Entrada_SolicitudArchivoTasas entrada) throws Exception {

		SQL sql = new SQL();
		Salida_SolicitudArchivoTasas salida = new Salida_SolicitudArchivoTasas();
		logger.info("Inicio servicio ");
		MensajeSalida msg = new MensajeSalida();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		// VALIDACIÓN REQUISITOS BASICOS
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
        RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
        if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
            logger.debug("NO CUMPLE REQUISITOS BASICOS");
            salida.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
            salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
        }
        
        // VALIDACIÓN MESA RF CERRADAS.
        logger.debug("INICIO CONSULTAR ESTADO MESARF");
        HashMap<String, Object> params = new HashMap<>();
        params.put("p_IdEmpresa", entrada.p_IdEmpresa);
        Long validacionMesa = sql.countQuery(con, SQL_SELECT_ESTADO_MESARF, params);
        if(validacionMesa > 0) {
        	msg.id = 0L;
        	msg.tipo = "ERROR";
        	msg.codigo = "ERROR_NO_CIERRE_MESARF";
        	salida.P_OUT_MENSAJES.add(msg);
        	salida.P_OUT_ERRORESCONTADOR++;
        	return salida;
        }
        logger.debug("FIN CONSULTAR ESTADO MESARF");
        
        logger.debug("INICIO CONSULTAR NOMBRE FUENTE");
        params.clear();
    	params.put("IdFuente", entrada.p_Fuentes);
    	List<ParametrosArchivos> datosArchivos = sql.query(con, SQL_CONSULTAR_FUENTE, params, ParametrosArchivos.class);
    	if(datosArchivos.isEmpty()) {
			msg.id = 0L;
        	msg.tipo = "ERROR";
        	msg.codigo = "ERROR_CONSULTAR_FUENTE";
        	salida.P_OUT_MENSAJES.add(msg);
        	salida.P_OUT_ERRORESCONTADOR++;
			return salida;
    	}   	
    	logger.debug("FIN CONSULTAR NOMBRE FUENTE");    
    	
        // CONSEGUIMOS LA INFORMACIÓN DE LAS FUENTES.
    	// PRIMERO LA RUTA DEL ARCHIVO Y EL SERVICIO PARA LA FUENTE.
		logger.debug("INICIO CONSULTAR CONFIGURACION TASAS");
    	params.clear();
		params.put("p_IdEmpresa", entrada.p_IdEmpresa);
		List<ConfiguracionTasas> configuracionTasas = sql.query(con, SQL_SELECT_CONFIGURACION_TASAS, params, ConfiguracionTasas.class);
		if(configuracionTasas.isEmpty()){
			msg.id = 0L;
        	msg.tipo = "ERROR";
        	msg.codigo = "ERROR_NO_CONFIGURACION_TASAS";
        	salida.P_OUT_MENSAJES.add(msg);
        	salida.P_OUT_ERRORESCONTADOR++;
			return salida;
		}
		String rutaArchivo = "";
		String servicio = "";
		for(ConfiguracionTasas entidad : configuracionTasas) {
			if(entidad.nombreEntidad.equals("RUTAARCHIVOTASAS")) {
				rutaArchivo = entidad.valor;
			}
			if(entidad.nombreEntidad.equals("SERVICIOCONSULTARTASAS")) {
				servicio = entidad.valor;
			}
		}
		logger.debug("FIN CONSULTAR CONFIGURACION TASAS");
	
    	// SEGUNDO: NOMBRE DEL ARCHIVO PARA LA FUENTE
		logger.debug("INICIO CONSULTAR DOCUMENTO IDENTIDAD EMPRESA");
		params.clear();
		params.put("p_IdEmpresa", entrada.p_IdEmpresa);
		List<ParametrosArchivos> documentoIdentidad = sql.query(con, SQL_SELECT_DATOS_EMPRESA, params, ParametrosArchivos.class);
		if(documentoIdentidad.isEmpty()) {
			msg.id = 0L;
        	msg.tipo = "ERROR";
        	msg.codigo = "ERROR_DOCUMENTO_IDENTIDAD";
        	salida.P_OUT_MENSAJES.add(msg);
        	salida.P_OUT_ERRORESCONTADOR++;
			return salida;
		}
		String docIdentidad = documentoIdentidad.get(0).documentoIdentidad;
		Random random = new Random();
		for(ParametrosArchivos codigosFuente:datosArchivos) {
			int numAleatorio = random.nextInt(101);
    		codigosFuente.nombreArchivo = codigosFuente.codigo+"_"+docIdentidad+entrada.p_Fecha+".xml"; //nombreArchivo
    	}
		logger.debug("FIN CONSULTAR DOCUMENTO IDENTIDAD EMPRESA");
		
		List<String> lstNombresArchivos = new ArrayList<>();
		for(ParametrosArchivos nombre:datosArchivos) {
			lstNombresArchivos.add(nombre.nombreArchivo);
		}
		salida.nombreArchivo.addAll(lstNombresArchivos);
		
		// VERIFICAMOS LOS INSTRUMENTOS
		logger.debug("INICIO CONSULTAR NEMOTECNICOS REPETIDOS");
		List<Nemotecnicos> nemotecnicosRepetidos = sql.query(con, SQL_SELECT_NEMOTECNICOS_REPETIDOS, new HashMap<>(), Nemotecnicos.class);
		
		List<String> nemotecnicosRepetidosValues = nemotecnicosRepetidos.stream()
		        .map(nemotecnicos -> nemotecnicos.nemotecnico)
		        .collect(Collectors.toList());
		logger.debug("FIN CONSULTAR NEMOTECNICOS REPETIDOS");
		
		logger.debug("INICIO CONSULTAR NEMOTECNICOS NO REPETIDOS");
		List<Nemotecnicos> nemotecnicosNoRepetidos = sql.query(con, SQL_SELECT_NEMOTECNICOS_NO_REPETIDOS, new HashMap<>(), Nemotecnicos.class);
		
		List<String> nemotecnicosNoRepetidosValues = nemotecnicosNoRepetidos.stream()
		        .map(nemotecnicos -> nemotecnicos.nemotecnico)
		        .collect(Collectors.toList());
		logger.debug("FIN CONSULTAR NEMOTECNICOS NO REPETIDOS");
		
		logger.debug("INICIO SELECCIONAR INSTRUMENTOS");
		params.clear();
		params.put("nemotecnicosRepetidos", nemotecnicosRepetidosValues);
		params.put("nemotecnicosNoRepetidos", nemotecnicosNoRepetidosValues);
		params.put("p_IdEmpresa", entrada.p_IdEmpresa);
		List<Instrumentos> instrumentosList = sql.query(con, SQL_SELECT_INSTRUMENTOS_ARCHIVOS, params, Instrumentos.class);
		if(instrumentosList.isEmpty()) {
			msg.id = 0L;
        	msg.tipo = "ERROR";
        	msg.codigo = "ERROR_SELECCIONAR_INSTRUMENTO";
        	salida.P_OUT_MENSAJES.add(msg);
        	salida.P_OUT_ERRORESCONTADOR++;
			return salida;
		}
		List<String> instrumentosValues = instrumentosList.stream()
		        .map(instrumentos -> instrumentos.Nemotecnico)
		        .collect(Collectors.toList());
		logger.debug("FIN SELECCIONAR INSTRUMENTOS");
		
		// SE ELIMINAN TODAS LAS TASAS DE LA MISMA FECHA Y FUENTE QUE NO ESTEN OFICIALIZADAS NI PUBLICADAS
		logger.debug("INICIO ELIMINAR FECHA Y FUENTE NO OFICIALIZADAS");
		List<String> lstCodigosFuentes = new ArrayList<>();
		for(ParametrosArchivos codigo:datosArchivos) {
			lstCodigosFuentes.add(codigo.codigo);
		}
		PrecioTasaEliminar eliminarPreciosTasas = new PrecioTasaEliminar();
		List<PrecioTasaEliminar> lstEliminarPreciosTasas = new ArrayList<PrecioTasaEliminar>();
		eliminarPreciosTasas.p_CodigosFuentes = lstCodigosFuentes;
		eliminarPreciosTasas.p_Fecha = entrada.p_Fecha;
		eliminarPreciosTasas.p_IdEmpresa = entrada.p_IdEmpresa;
		lstEliminarPreciosTasas.add(eliminarPreciosTasas);
		int[] deletePrecioTasa = sql.batch(con, SQL_DELETE_PRECIO_TASA, lstEliminarPreciosTasas, PrecioTasaEliminar.class);
		logger.debug("FIN ELIMINAR FECHA Y FUENTE NO OFICIALIZADAS");
		
		// SE CREAN LOS REGISTROS TABLA PRECIO TASA
		//SELECCIONO LOS REGISTROS PARA LA TABLA:
		logger.debug("INICIO SELECCIONAR INSTRUMENTOS");
		params.clear();
		params.put("p_IdEmpresa", entrada.p_IdEmpresa);
		params.put("instrumentos", instrumentosValues);
		List<InsertInstrumentosTasas> listInstrumentosSelect = sql.query(con, SQL_SELECT_INSERT_PRECIO_TASAS, params, InsertInstrumentosTasas.class);
		if(listInstrumentosSelect.isEmpty()) {
			msg.id = 0L;
        	msg.tipo = "ERROR";
        	msg.codigo = "ERROR_SELECT_INSERT_INSTRUMENTOS_TASAS";
        	salida.P_OUT_MENSAJES.add(msg);
        	salida.P_OUT_ERRORESCONTADOR++;
			return salida;
		}
		
		try {
			sql.exec(con, SQL_CREAR_TEMP_INSTRUMENTOS_SELECT);
			int[] insertarDatosTemporales = sql.batch(con, SQL_CREAR_TEMP_INSTRUMENTOS_INSERT, listInstrumentosSelect, InsertInstrumentosTasas.class);
		}catch(Exception err) {
			System.out.println("Error:"+err);
		}
		logger.debug("FIN SELECCIONAR INSTRUMENTOS");
		
		logger.debug("INICIO INSERTAR REGISTROS PRECIO TASA");
		List<PrecioTasaInsertar> lstInsertarPreciosTasas = new ArrayList<PrecioTasaInsertar>();
		for(ParametrosArchivos datos:datosArchivos) {
			PrecioTasaInsertar insertarPreciosTasas = new PrecioTasaInsertar();
			insertarPreciosTasas.IdFuente = datos.idFuente;
			insertarPreciosTasas.NombreArchivo = datos.nombreArchivo;
			insertarPreciosTasas.p_IdEmpresa = entrada.p_IdEmpresa;
			insertarPreciosTasas.p_IdUsuario = entrada.p_IdUsuario;
			insertarPreciosTasas.RutaArchivo = rutaArchivo;
			insertarPreciosTasas.FechaSistema = fechaSistema;
			insertarPreciosTasas.FechaHoraUI = entrada.p_Fecha;
			//insertarPreciosTasas.instrumentos = instrumentosValues;
			lstInsertarPreciosTasas.add(insertarPreciosTasas);
		}
		
		//VERIFICACIÓN DE NEMOTECNICOS PARA EVITAR DUPLICIDAD:
		logger.debug("INICIO NEMOS DUPLICADOS");
		params.clear();
		//sql.countQuery(con, SQL_SELECT_DUPLICADOS, params);
		int[] insertPrecioTasa = sql.batch(con, SQL_INSERT_PRECIO_TASA, lstInsertarPreciosTasas, PrecioTasaInsertar.class);
		try {
			sql.exec(con, "DROP TABLE #InstrumentosSelect");
		}catch(Exception err) {
			System.out.println("Error:"+err);
		}
		
		System.out.println(insertPrecioTasa);
		logger.debug("FIN INSERTAR REGISTROS PRECIO TASA");
		
		// INSCRIPCION DE TAREA EN JOB MANAGER
		for(ParametrosArchivos datos:datosArchivos) {
			AgregarPrograma agregarPrograma = new AgregarPrograma();	
    		List<Parametro> lstParametros = new ArrayList<Parametro>();
    		Parametro objParametro = new Parametro();
		    objParametro.NombreParametro = "IdEmpresa";
		    objParametro.Valor = entrada.p_IdEmpresa.toString();
		    lstParametros.add(objParametro);

		    objParametro = new Parametro();
		    objParametro.NombreParametro = "Fecha";
		    objParametro.Valor = entrada.p_Fecha;
		    lstParametros.add(objParametro);

		    objParametro = new Parametro();
		    objParametro.NombreParametro = "Fuente";
		    objParametro.Valor = datos.codigo;
		    lstParametros.add(objParametro);

		    objParametro = new Parametro();
		    objParametro.NombreParametro = "RutaArchivo";
		    objParametro.Valor = rutaArchivo;
		    lstParametros.add(objParametro);

		    objParametro = new Parametro();
		    objParametro.NombreParametro = "NombreArchivo";
		    objParametro.Valor = datos.nombreArchivo;
		    lstParametros.add(objParametro);

		    objParametro = new Parametro();
		    objParametro.NombreParametro = "Servicio";
		    objParametro.Valor = servicio;
		    lstParametros.add(objParametro);
    		
    		InAgregarPrograma inAgregarPrograma = new InAgregarPrograma();
    		OutAgregarPrograma outAgregarPrograma = new OutAgregarPrograma();
    		
    		inAgregarPrograma.p_IdEmpresa = entrada.p_IdEmpresa;
    		inAgregarPrograma.p_IdUsuario = entrada.p_IdUsuario;
    		inAgregarPrograma.p_IdModulo = entrada.p_IdModulo;
    		inAgregarPrograma.p_IdTipoPrograma = "GENERAR_ARCHIVO_TASAS";
    		inAgregarPrograma.p_Segundos = 0L;
    		inAgregarPrograma.p_Parametros = lstParametros;
    		outAgregarPrograma = agregarPrograma.logica(con, inAgregarPrograma);
		}
		
		return salida; 
	}

	/***********************************************************************************************
	 * MAIN
	 ***********************************************************************************************/
	public static void main(String[] args) throws Exception { 
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
					.serializeNulls().create();

			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"N\",\"p_Fecha\":\"2023-08-16\",\"p_Fuentes\":[13]}";
	
			Entrada_SolicitudArchivoTasas Objin = gson.fromJson(json,Entrada_SolicitudArchivoTasas.class);
			SolicitudArchivoTasas demo = new SolicitudArchivoTasas();

			try {
				Salida_SolicitudArchivoTasas out = demo.logica(con, Objin);
				System.out.println(out);
				//con.rollback();
				con.commit();
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		}
		System.out.println("== END ==");
	}
	
}