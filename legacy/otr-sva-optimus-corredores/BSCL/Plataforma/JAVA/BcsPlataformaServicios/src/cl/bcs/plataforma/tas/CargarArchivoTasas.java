package cl.bcs.plataforma.tas;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.InformacionDetalle;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class CargarArchivoTasas implements JavaCall {
	private final String SQL_SELECT_PARAMETRO_TIPO_INSTRUMENTO = loadResourceToString("cl/bcs/plataforma/tas/sql/CargarArchivoTasas_SelectParametroTipoInstrumento.sql");
	private final String SQL_SELECT_PARAMETRO_TIPO_TASA = loadResourceToString("cl/bcs/plataforma/tas/sql/CargarArchivoTasas_SelectParametroTipoTasa.sql");
	private final String SQL_SELECT_PARAMETRO_FUENTE = loadResourceToString("cl/bcs/plataforma/tas/sql/CargarArchivoTasas_SelectParametroFuente.sql");
	private final String SQL_SELECT_NOMBRE_ARCHIVO = loadResourceToString("cl/bcs/plataforma/tas/sql/CargarArchivoTasas_SelectNombreArchivo.sql");
	private final String SQL_SELECT_TASAS_ARCHIVO = loadResourceToString("cl/bcs/plataforma/tas/sql/CargarArchivoTasas_SelectTasasArchivo.sql");
	private final String SQL_SELECT_INSTRUMENTOS_DV = loadResourceToString("cl/bcs/plataforma/tas/sql/CargarArchivoTasas_SelectInstrumentoDV.sql");
	private final String SQL_SELECT_CARGA_CURVA_PUNTO = loadResourceToString("cl/bcs/plataforma/tas/sql/CargarArchivoTasas_SelectCargaCurvaPunto.sql");
	private final String SQL_SELECT_CARGA_TASA = loadResourceToString("cl/bcs/plataforma/tas/sql/CargarArchivoTasas_SelectCargaTasa.sql");
	private final String SQL_SELECT_DATOS_GENERALES = loadResourceToString("cl/bcs/plataforma/tas/sql/CargarArchivoTasas_SelectDatosGenerales.sql");
	private final String SQL_SELECT_ID_FUENTE = loadResourceToString("cl/bcs/plataforma/tas/sql/CargarArchivoTasas_SelectIdFuente.sql");
	private final String SQL_INSERT_GBL_DATOS_TASAS = loadResourceToString("cl/bcs/plataforma/tas/sql/CargarArchivoTasas_InsertDatosTasas.sql");
	private final String INSERT_NUEVAS_TASAS = loadResourceToString("cl/bcs/plataforma/tas/sql/CargarArchivoTasas_SelectNuevasTasas.sql");
	private final String SQL_INSERT_GBL_DATOS_NUEVAS_TASAS = loadResourceToString("cl/bcs/plataforma/tas/sql/CargarArchivoTasas_InsertDatosNuevasTasas.sql");
	private final String SQL_UPDATE_TASAS_CARGADAS = loadResourceToString("cl/bcs/plataforma/tas/sql/CargarArchivoTasas_UpdateTasasCargadas.sql");
	private static final Logger logger = LogManager.getLogger(CargarArchivoTasas.class);
	
	/***********************************************************************************************
	 * CLASES
	 ***********************************************************************************************/
	
	public static class Entrada_CargarArchivoTasas {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_Archivo;
		public List<DataTasas> p_DataTasas;
		public String p_Fecha;
		public String p_FechaCreSol;
		public String p_Fuente;
		public String p_Hora;
		public String p_IdClasificacionTipoInstrumento;
		public String p_IdTipoTasa;
		public String p_Rut;
		public String p_TipoArchivo;
		
		public Entrada_CargarArchivoTasas() {
			p_DataTasas = new ArrayList<DataTasas>();
		} 

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DataTasas{
		public String IdMoneda;
		public String Nemo;
		public String Plazo;
		public String Tasa;
		public String TasaEstimada;
	}
	
	public static class DatosGeneralesCarga{
		public Integer IdPrecioTasa;
		public Long IdInstrumento;
		public String Nemotecnico;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public BigDecimal Tasa;
		public Integer TasaMod;
		public BigDecimal TasaEstimada;
		public Integer TasaEstimadaMod;
		public Long Plazo;
		public Integer PlazoMod;
		public String IdEstado;
		public String IdMoneda;
		public Long p_IdUsuario;
	}
	
	public static class DatosGeneralesCargaUpdate{
		public Integer IdPrecioTasa;
		public Long IdInstrumento;
		public String Nemotecnico;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public BigDecimal Tasa;
		public BigDecimal TasaMod;
		public BigDecimal TasaEstimada;
		public BigDecimal TasaEstimadaMod;
		public Long Plazo;
		public Long PlazoMod;
		public String IdEstado;
		public String IdMoneda;
		public Long p_IdUsuario;
	}
	
	public static class NuevasTasas{
		public Integer IdPrecioTasa;
		public Long IdFuente;
		public Long IdInstrumento;
		public String Nemotecnico;
		public String IdTipoTasa;
		public BigDecimal Tasa;
		public BigDecimal TasaEstimada;
		public Long Plazo;
		public String IdEstado;
		public Long IdEmpresa;
		public Long IdUsuarioCreador;
		public String FechaSolicitud;
		public String FechaIngreso;
		public String FechaCreacion;
		public String FechaModificacion;
		public String NombreArchivo;
		public String TipoRegistro;
		public String IdMoneda;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
	}
		
	public static class ClasificacionInstrumento{
		public String Nombre;
	}
	
	public static class Fuente{
		public Long IdFuente;
	}
	
	public static class Salida_CargarArchivoTasas {
	    public int p_out_advertenciascontador;
	    public List<InformacionDetalle> P_OUT_ERRORESDETALLE;
	    public Long P_OUT_ERRORESCONTADOR;
	    public List<Long> P_OUT_SECUENCIA;
	    private List<MensajeSalida> P_OUT_MENSAJES;
	    public List<String> nombreArchivo;
	    
	    public Salida_CargarArchivoTasas() {
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
	

	/***********************************************************************************************
	 * CALL
	 ***********************************************************************************************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		Entrada_CargarArchivoTasas in = gson.fromJson(jin.toString(),
				Entrada_CargarArchivoTasas.class);
		Salida_CargarArchivoTasas out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();

		return dataout;
	}

	/***********************************************************************************************
	 * LOGICA
	 ***********************************************************************************************/
	@Trace
	public Salida_CargarArchivoTasas logica(Connection con,	Entrada_CargarArchivoTasas entrada) throws Exception {

		SQL sql = new SQL();
		Salida_CargarArchivoTasas salida = new Salida_CargarArchivoTasas();
		logger.info("Inicio servicio ");
		MensajeSalida msg = new MensajeSalida();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		//OBTENEMOS LOS DATOS QUE SERÁN CARGARDOS:
		HashMap<String, Object> params = new HashMap<>();
		List<DatosGeneralesCarga> DatosGeneralesCarga = new ArrayList<DatosGeneralesCarga>();
		List<DatosGeneralesCargaUpdate> DatosGeneralesCargaUpdate = new ArrayList<DatosGeneralesCargaUpdate>();
		int[] InsertarDatosTasas = sql.batch(con, SQL_INSERT_GBL_DATOS_TASAS, entrada.p_DataTasas, DataTasas.class);
		
		// VALIDACIÓN REQUISITOS BASICOS
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
        RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
        if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
            logger.debug("NO CUMPLE REQUISITOS BASICOS");
            salida.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
            salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
        }
        
		logger.debug("INICIO Validacion para parametro ClasificacionTipoInstrumento incorrecto");
		params.clear();
		params.put("p_IdClasificacionTipoInstrumento", entrada.p_IdClasificacionTipoInstrumento);
		Long validacionParametro = sql.countQuery(con, SQL_SELECT_PARAMETRO_TIPO_INSTRUMENTO, params);
		if(validacionParametro == 0) {
			msg.id = 0L;
        	msg.tipo = "ERROR";
        	msg.codigo = "ERROR_PARAMETRO_INCORRECTO";
        	salida.P_OUT_MENSAJES.add(msg);
        	salida.P_OUT_ERRORESCONTADOR++;
        	return salida;
		}
		logger.debug("FIN Validacion para parametro ClasificacionTipoInstrumento incorrecto");
		
		logger.debug("INICIO Validacion para parametro TipoTasa incorrecto");
		params.clear();
		params.put("p_IdTipoTasa", entrada.p_IdTipoTasa);
		Long validacionTipoTasa = sql.countQuery(con, SQL_SELECT_PARAMETRO_TIPO_TASA, params);
		if(validacionTipoTasa == 0) {
			msg.id = 0L;
        	msg.tipo = "ERROR";
        	msg.codigo = "ERROR_PARAMETRO_TASA";
        	salida.P_OUT_MENSAJES.add(msg);
        	salida.P_OUT_ERRORESCONTADOR++;
        	return salida;
		}
		logger.debug("FIN Validacion para parametro TipoTasa incorrecto");
		
		logger.debug("INICIO  Validacion para parametro Fuente incorrecto");
		List<Fuente> fuente = new ArrayList<Fuente>();
		params.clear();
		params.put("p_Fuente", entrada.p_Fuente);
		Long validacionFuente = sql.countQuery(con, SQL_SELECT_PARAMETRO_FUENTE, params);
		if(validacionFuente == 0) {
			msg.id = 0L;
        	msg.tipo = "ERROR";
        	msg.codigo = "ERROR_PARAMETRO_FUENTE";
        	salida.P_OUT_MENSAJES.add(msg);
        	salida.P_OUT_ERRORESCONTADOR++;
        	return salida;
		}else {
			params.clear();
			params.put("p_Fuente", entrada.p_Fuente);
			fuente = sql.query(con, SQL_SELECT_ID_FUENTE, params, Fuente.class);
			if(fuente.isEmpty()) {
				msg.id = 0L;
	        	msg.tipo = "ERROR";
	        	msg.codigo = "ERROR_SELECCIONAR_FUENTE";
	        	salida.P_OUT_MENSAJES.add(msg);
	        	salida.P_OUT_ERRORESCONTADOR++;
	        	return salida;
			}
		}
		logger.debug("FIN  Validacion para parametro Fuente incorrecto");
		
		//FIN VALIDACIONES BASICAS
		
		//CARGAMOS LOS DATOS SEGÚN EL TIPO:
		if(entrada.p_IdTipoTasa.equalsIgnoreCase("CURVA") || entrada.p_IdTipoTasa.equalsIgnoreCase("PUNTO")) {
			params.clear();
        	params.put("p_IdClasificacionTipoInstrumento", entrada.p_IdClasificacionTipoInstrumento);
    		params.put("p_TipoArchivo", entrada.p_TipoArchivo);
    		DatosGeneralesCarga = sql.query(con, SQL_SELECT_CARGA_CURVA_PUNTO, params, DatosGeneralesCarga.class);
    		if(DatosGeneralesCarga.isEmpty()) {
    			msg.id = 0L;
            	msg.tipo = "ERROR";
            	msg.codigo = "ERROR_DATOS_CARGA_INCORRECTO";
            	salida.P_OUT_MENSAJES.add(msg);
            	salida.P_OUT_ERRORESCONTADOR++;
            	return salida;
    		}
    	}else if(entrada.p_IdTipoTasa.equalsIgnoreCase("TASA") && entrada.p_TipoArchivo.equalsIgnoreCase("CSV")) { 
			params.clear();
    		DatosGeneralesCarga = sql.query(con, SQL_SELECT_CARGA_TASA, params, DatosGeneralesCarga.class);
    		if(DatosGeneralesCarga.isEmpty()) {
    			msg.id = 0L;
            	msg.tipo = "ERROR";
            	msg.codigo = "ERROR_DATOS_CARGA_INCORRECTO";
            	salida.P_OUT_MENSAJES.add(msg);
            	salida.P_OUT_ERRORESCONTADOR++;
            	return salida;
    		}
		}else {
			params.clear();
    		params.put("p_Archivo", entrada.p_Archivo);
    		params.put("p_IdEmpresa", entrada.p_IdEmpresa);
    		params.put("p_Fuente", entrada.p_Fuente);
    		params.put("FechaDesde", entrada.p_FechaCreSol+" 00:00:00.000");
    		params.put("FechaHasta", entrada.p_FechaCreSol+" 23:59:59.999");
    		DatosGeneralesCargaUpdate = sql.query(con, SQL_SELECT_DATOS_GENERALES, params, DatosGeneralesCargaUpdate.class);
    		if(DatosGeneralesCargaUpdate.isEmpty()) {
    			msg.id = 0L;
            	msg.tipo = "ERROR";
            	msg.codigo = "ERROR_DATOS_GENERALES_INCORRECTO";
            	salida.P_OUT_MENSAJES.add(msg);
            	salida.P_OUT_ERRORESCONTADOR++;
            	return salida;
    		}
		}
		//FIN CARGA DE DATA SEGÚN TASA
		
		//VALIDAMOS SEGÚN NECESIDAD DE LA TASA:
		if(entrada.p_IdTipoTasa.equalsIgnoreCase("CURVA") || entrada.p_IdTipoTasa.equalsIgnoreCase("PUNTO") || (entrada.p_IdTipoTasa.equalsIgnoreCase("TASA") && entrada.p_TipoArchivo.equalsIgnoreCase("CSV")) ) {
			params.clear();
	        params.put("IdTipoTasa", entrada.p_IdTipoTasa);
	        params.put("IdFuente", fuente.get(0).IdFuente);
			params.put("FechaDesde", entrada.p_FechaCreSol+" 00:00:00.000");
			params.put("FechaHasta", entrada.p_FechaCreSol+" 23:59:59.999");
	        params.put("IdEmpresa", entrada.p_IdEmpresa);
	        params.put("IdClasificacionTipoInstrumento", entrada.p_IdClasificacionTipoInstrumento);
	        Long validacionTasas = sql.countQuery(con, SQL_SELECT_TASAS_ARCHIVO, params);
	        if(validacionTasas > 0) {
	        	msg.id = 0L;
            	msg.tipo = "ERROR";
            	msg.codigo = "ERROR_YA_EXISTEN_TASAS_CARGADAS_PARA_LA_FUENTE";
            	salida.P_OUT_MENSAJES.add(msg);
            	salida.P_OUT_ERRORESCONTADOR++;
            	return salida;
	        }
	        
	        logger.debug("INICIO validacion carga de Curva FWD y Punto FWD aplica solo con clasificacion tipo instrumento DV");
    		params.clear();
    		params.put("p_IdClasificacionTipoInstrumento", entrada.p_IdClasificacionTipoInstrumento);
    		if((!entrada.p_IdClasificacionTipoInstrumento.equalsIgnoreCase("DV") && !entrada.p_IdClasificacionTipoInstrumento.equalsIgnoreCase("IF"))  && (entrada.p_IdTipoTasa.equalsIgnoreCase("CURVA") || entrada.p_IdTipoTasa.equalsIgnoreCase("PUNTO") )) {
    			msg.id = 0L;
            	msg.tipo = "ERROR";
            	msg.codigo = "ERROR_INSTRUMENTOS_DV";
            	salida.P_OUT_MENSAJES.add(msg);
            	salida.P_OUT_ERRORESCONTADOR++;
            	return salida;
    		}
    		logger.debug("FIN validacion carga de Curva FWD y Punto FWD aplica solo con clasificacion tipo instrumento DV");
		} else {
			logger.debug("INICIO validacion Tasas que ya esten Cargadas, Oficializadas o Publicadas");   	
        	int contador = 0;
    		for(DatosGeneralesCargaUpdate Datos : DatosGeneralesCargaUpdate) {
    			if(Datos.IdEstado.compareToIgnoreCase("OFI") == 0 || Datos.IdEstado.compareToIgnoreCase("PUB") == 0) {
    				contador++;
    			}
    		}
    		if(contador > 0) {
    			msg.id = 0L;
            	msg.tipo = "ERROR";
            	msg.codigo = "ERROR_TASAS_PUBLICADAS";
            	salida.P_OUT_MENSAJES.add(msg);
            	salida.P_OUT_ERRORESCONTADOR++;
            	return salida;
    		}
    		logger.debug("FIN validacion Tasas que ya esten Cargadas, Oficializadas o Publicadas");
    		
        	logger.debug("INICIO validacion Tasas que no han sido Solicitadas");
    		for(DatosGeneralesCargaUpdate Datos : DatosGeneralesCargaUpdate) {
    			if(Datos.IdEstado.compareToIgnoreCase("CRA") == 0) {
    				contador++;
    			}
    		}
    		if(contador > 0) {
    			msg.id = 0L;
            	msg.tipo = "ERROR";
            	msg.codigo = "ERROR_TASAS_NO_SOLICITADAS";
            	salida.P_OUT_MENSAJES.add(msg);
            	salida.P_OUT_ERRORESCONTADOR++;
            	return salida;
    		}
    		logger.debug("FIN validacion Tasas que no han sido Solicitadas");
		}
		
		//POR ULTIMO CARGAMOS LA DATA O UPDATEAMOS LAS DE SOLICITUD DE TASAS:
		if(entrada.p_IdTipoTasa.equalsIgnoreCase("CURVA") || entrada.p_IdTipoTasa.equalsIgnoreCase("PUNTO") || (entrada.p_IdTipoTasa.equalsIgnoreCase("TASA") && entrada.p_TipoArchivo.equalsIgnoreCase("CSV")) ) {
			List<NuevasTasas> lstNuevasTasas = new ArrayList<NuevasTasas>();
            for(DatosGeneralesCarga datos: DatosGeneralesCarga) {
                NuevasTasas nuevasTasas = new NuevasTasas();
                nuevasTasas.IdFuente = fuente.get(0).IdFuente;
                nuevasTasas.IdInstrumento = datos.IdInstrumento;
                nuevasTasas.Nemotecnico = datos.Nemotecnico;
                nuevasTasas.IdTipoTasa = entrada.p_IdTipoTasa;
                nuevasTasas.Tasa = datos.Tasa;
                nuevasTasas.TasaEstimada = datos.TasaEstimada;
                nuevasTasas.Plazo = datos.Plazo;
                nuevasTasas.IdEstado = datos.IdEstado;
                nuevasTasas.IdEmpresa = entrada.p_IdEmpresa;
                nuevasTasas.IdUsuarioCreador = entrada.p_IdUsuario;
                nuevasTasas.FechaSolicitud = entrada.p_FechaCreSol;
                nuevasTasas.FechaIngreso = fechaSistema;
                nuevasTasas.NombreArchivo = entrada.p_Archivo;
                nuevasTasas.TipoRegistro = entrada.p_IdTipoTasa;
                nuevasTasas.IdMoneda = datos.IdMoneda;
                nuevasTasas.IdClasificacionTipoInstrumento = datos.IdClasificacionTipoInstrumento;
                nuevasTasas.IdTipoInstrumento = datos.IdTipoInstrumento;
                lstNuevasTasas.add(nuevasTasas);
            }
            int[] NuevasTasas = sql.batch(con, INSERT_NUEVAS_TASAS, lstNuevasTasas, NuevasTasas.class);
            msg.id = 1L;
            msg.tipo = "INFO";
            msg.codigo = "TASAS_INGRESADAS_CORRECTAMENTE";
            salida.P_OUT_MENSAJES.add(msg);
            return salida;
		} else {
			for(DatosGeneralesCargaUpdate datos: DatosGeneralesCargaUpdate) {
                datos.p_IdUsuario = entrada.p_IdUsuario;
            }
            int[] updateTasas = sql.batch(con, SQL_UPDATE_TASAS_CARGADAS, DatosGeneralesCargaUpdate, DatosGeneralesCargaUpdate.class);
            msg.id = 1L;
            msg.tipo = "INFO";
            msg.codigo = "TASAS_UPDATE_CORRECTAMENTE";
            salida.P_OUT_MENSAJES.add(msg);
            
            AgregarLog agregarLog = new AgregarLog();
    		InAgregarLog entradaLog = new InAgregarLog();
            for(DatosGeneralesCargaUpdate datos: DatosGeneralesCargaUpdate) {
            	 InAgregarLog.Log headLog = new InAgregarLog.Log();
            	 headLog.IdTipoLog = "MODIFICACION";
            	 headLog.IdModulo = entrada.p_IdModulo;
            	 headLog.IdUsuario = entrada.p_IdUsuario;
            	 headLog.IdEmpresa = entrada.p_IdEmpresa;
            	 headLog.FechaSistemaLog = entrada.p_FechaCreSol;
            	 headLog.IdUsuarioAtendido = entrada.p_IdUsuario;
            	 headLog.IdTipoRegistro = "CARGATASAS";
            	 headLog.NombreProceso = "CARGA ARCHIVO TASAS";
            	 entradaLog.p_Log.add(headLog);
            	 
            	 InAgregarLog.DetalleLog detLog = new InAgregarLog.DetalleLog();
            	 detLog.SubRegistro = datos.Nemotecnico;
            	 detLog.CampoRegistro = "Tasa / TasaEstimada / Plazo";
            	 detLog.ValorAntiguo = datos.Tasa.toString().concat(" / ").concat(datos.TasaEstimada.toString().concat(" / ").concat(datos.Plazo.toString()));
            	 detLog.ValorNuevo = datos.TasaMod.toString().concat(" / ").concat(datos.TasaEstimadaMod.toString().concat(" / ").concat(datos.PlazoMod.toString()));
            	 entradaLog.p_LogDetalle.add(detLog);
            }
            
            agregarLog.logica(con, entradaLog);
            
            return salida;
                       
		}
        
   
	}

	/***********************************************************************************************
	 * MAIN
	 ***********************************************************************************************/
	public static void main(String[] args) throws Exception { 
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
					.serializeNulls().create();
			
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":74246,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"N\",\"p_Archivo\":\"RISK_7601119372023-06-12_99.xml\",\"p_IdClasificacionTipoInstrumento\":\"\",\"p_FechaCreSol\":\"2023-06-12\",\"p_IdTipoTasa\":\"\",\"p_Fuente\":\"TASASMERCADO\",\"p_Rut\":\"76011193-7\",\"p_Fecha\":\"12-06-2023\",\"p_Hora\":\"21:06:23\",\"p_TipoArchivo\":\"XML\",\"p_DataTasas\":[{\"Nemo\":\"BNPDBC100424\",\"IdMoneda\":\"\",\"Tasa\":\"3.1111\",\"TasaEstimada\":\"0\",\"Plazo\":\"3\"},{\"Nemo\":\"BNPDBC170424\",\"IdMoneda\":\"\",\"Tasa\":\"2\",\"TasaEstimada\":\"0\",\"Plazo\":\"5\"},{\"Nemo\":\"FNBIC-181023\",\"IdMoneda\":\"\",\"Tasa\":\"2.1111\",\"TasaEstimada\":\"0\",\"Plazo\":\"1\"},{\"Nemo\":\"ONC18-021023\",\"IdMoneda\":\"\",\"Tasa\":\"3.2222\",\"TasaEstimada\":\"0\",\"Plazo\":\"2\"},{\"Nemo\":\"ONC18-200923\",\"IdMoneda\":\"\",\"Tasa\":\"5\",\"TasaEstimada\":\"0\",\"Plazo\":\"20\"},{\"Nemo\":\"SEAGUA181023\",\"IdMoneda\":\"\",\"Tasa\":\"10\",\"TasaEstimada\":\"0\",\"Plazo\":\"2\"},{\"Nemo\":\"SNANTA181023\",\"IdMoneda\":\"\",\"Tasa\":\"20.1\",\"TasaEstimada\":\"0\",\"Plazo\":\"0\"},{\"Nemo\":\"SUADRT210423\",\"IdMoneda\":\"\",\"Tasa\":\"0\",\"TasaEstimada\":\"0\",\"Plazo\":\"0\"},{\"Nemo\":\"ABNA150513\",\"IdMoneda\":\"\",\"Tasa\":\"0\",\"TasaEstimada\":\"0\",\"Plazo\":\"0\"},{\"Nemo\":\"ADMINISTRATIVO RENTA FIJA\",\"IdMoneda\":\"\",\"Tasa\":\"0\",\"TasaEstimada\":\"0\",\"Plazo\":\"0\"},{\"Nemo\":\"ADMINISTRATIVO RF\",\"IdMoneda\":\"\",\"Tasa\":\"0\",\"TasaEstimada\":\"0\",\"Plazo\":\"0\"},{\"Nemo\":\"BACEN-A1\",\"IdMoneda\":\"\",\"Tasa\":\"0\",\"TasaEstimada\":\"0\",\"Plazo\":\"0\"},{\"Nemo\":\"BAGUA-AA\",\"IdMoneda\":\"\",\"Tasa\":\"0\",\"TasaEstimada\":\"0\",\"Plazo\":\"0\"},{\"Nemo\":\"BAGUA-H\",\"IdMoneda\":\"\",\"Tasa\":\"0\",\"TasaEstimada\":\"0\",\"Plazo\":\"0\"},{\"Nemo\":\"BBIC850522\",\"IdMoneda\":\"\",\"Tasa\":\"0\",\"TasaEstimada\":\"0\",\"Plazo\":\"0\"},{\"Nemo\":\"BBICS-FF\",\"IdMoneda\":\"\",\"Tasa\":\"0\",\"TasaEstimada\":\"0\",\"Plazo\":\"0\"},{\"Nemo\":\"BCMPC-F\",\"IdMoneda\":\"\",\"Tasa\":\"0\",\"TasaEstimada\":\"0\",\"Plazo\":\"0\"},{\"Nemo\":\"BCP0600323\",\"IdMoneda\":\"\",\"Tasa\":\"0\",\"TasaEstimada\":\"0\",\"Plazo\":\"0\"},{\"Nemo\":\"BCTOR-L\",\"IdMoneda\":\"\",\"Tasa\":\"0\",\"TasaEstimada\":\"0\",\"Plazo\":\"0\"},{\"Nemo\":\"BECOP-H\",\"IdMoneda\":\"\",\"Tasa\":\"0\",\"TasaEstimada\":\"0\",\"Plazo\":\"0\"},{\"Nemo\":\"BFALA-AC\",\"IdMoneda\":\"\",\"Tasa\":\"0\",\"TasaEstimada\":\"0\",\"Plazo\":\"0\"},{\"Nemo\":\"BFALA-AD\",\"IdMoneda\":\"\",\"Tasa\":\"0\",\"TasaEstimada\":\"0\",\"Plazo\":\"0\"},{\"Nemo\":\"BLAPO-E\",\"IdMoneda\":\"\",\"Tasa\":\"0\",\"TasaEstimada\":\"0\",\"Plazo\":\"0\"},{\"Nemo\":\"BLATM-H\",\"IdMoneda\":\"\",\"Tasa\":\"0\",\"TasaEstimada\":\"0\",\"Plazo\":\"0\"},{\"Nemo\":\"BRLOA-C\",\"IdMoneda\":\"\",\"Tasa\":\"0\",\"TasaEstimada\":\"0\",\"Plazo\":\"0\"},{\"Nemo\":\"BTP0000324\",\"IdMoneda\":\"\",\"Tasa\":\"0\",\"TasaEstimada\":\"0\",\"Plazo\":\"0\"},{\"Nemo\":\"BTP0450326\",\"IdMoneda\":\"\",\"Tasa\":\"0\",\"TasaEstimada\":\"0\",\"Plazo\":\"0\"},{\"Nemo\":\"BTU0300124\",\"IdMoneda\":\"\",\"Tasa\":\"0\",\"TasaEstimada\":\"0\",\"Plazo\":\"0\"},{\"Nemo\":\"BTU0300327\",\"IdMoneda\":\"\",\"Tasa\":\"0\",\"TasaEstimada\":\"0\",\"Plazo\":\"0\"},{\"Nemo\":\"CFIRCA-OSA\",\"IdMoneda\":\"\",\"Tasa\":\"0\",\"TasaEstimada\":\"0\",\"Plazo\":\"0\"},{\"Nemo\":\"CFPRI1-OSA\",\"IdMoneda\":\"\",\"Tasa\":\"0\",\"TasaEstimada\":\"0\",\"Plazo\":\"0\"},{\"Nemo\":\"EMPRESTITO RENTA FIJA\",\"IdMoneda\":\"\",\"Tasa\":\"0\",\"TasaEstimada\":\"0\",\"Plazo\":\"0\"},{\"Nemo\":\"FINES GENERALES RF\",\"IdMoneda\":\"\",\"Tasa\":\"0\",\"TasaEstimada\":\"0\",\"Plazo\":\"0\"},{\"Nemo\":\"SQMB-OSA\",\"IdMoneda\":\"\",\"Tasa\":\"0\",\"TasaEstimada\":\"0\",\"Plazo\":\"0\"}]}";
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"N\",\"p_Archivo\":\"RISK-SCOTIA3.xml\",\"p_IdClasificacionTipoInstrumento\":\"DV\",\"p_FechaCreSol\":\"2023-08-02\",\"p_IdTipoTasa\":\"TASA\",\"p_Fuente\":\"RISK\",\"p_Rut\":\"76011193-7\",\"p_Fecha\":\"02-08-2023\",\"p_Hora\":\"11:09:21\",\"p_TipoArchivo\":\"XML\",\"p_DataTasas\":[{\"Nemo\":\"BNPDBC100424\",\"IdMoneda\":\"998\",\"Tasa\":\"3.5\",\"TasaEstimada\":\"0\",\"Plazo\":\"1\"},{\"Nemo\":\"BNPDBC170424\",\"IdMoneda\":\"998\",\"Tasa\":\"5.1\",\"TasaEstimada\":\"0\",\"Plazo\":\"5\"},{\"Nemo\":\"BUPRBC051023\",\"IdMoneda\":\"998\",\"Tasa\":\"3.2785\",\"TasaEstimada\":\"0\",\"Plazo\":\"3\"}]}";
			Entrada_CargarArchivoTasas Objin = gson.fromJson(json,Entrada_CargarArchivoTasas.class);
			CargarArchivoTasas demo = new CargarArchivoTasas();

			try {
				Salida_CargarArchivoTasas out = demo.logica(con, Objin);
				System.out.println(out);
				con.rollback();
				//con.commit();
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		}
		System.out.println("== END ==");
	}
	
}