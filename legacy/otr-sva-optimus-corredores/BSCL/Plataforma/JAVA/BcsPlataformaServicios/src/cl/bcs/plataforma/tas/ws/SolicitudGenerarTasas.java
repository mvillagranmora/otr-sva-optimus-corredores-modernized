package cl.bcs.plataforma.tas.ws;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.AdvertenciaDetalle;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.ErrorDetalle;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.StatsD;
import cl.bcs.utils.Utils;

public class SolicitudGenerarTasas implements JavaCall{
	
	private static final Logger logger = LogManager.getLogger(SolicitudGenerarTasas.class);
	private final String SQL_OBTENER_FUENTE_ACTIVA = loadResourceToString("cl/bcs/plataforma/tas/sql/SolicitudGenerarTasas_SelectCodigoFuenteActiva.sql");
	private final String SQL_CALL_GENERAR_ARCHIVO_TASAS = loadResourceToString("cl/bcs/plataforma/tas/sql/SolicitudGenerarTasas_GenerarArchivoTasas_Call.sql");	
	
	public static final String METRIC_PREFIX = "plataforma.tas.ws.solicitud_generar_tasas";
	public static final String METRIC_TIEMPO_EJECUCION = METRIC_PREFIX + "." + "tiempo_ejecucion";
	public static final String METRIC_VOLUMEN_DATOS = METRIC_PREFIX + "." + "volumen_datos";
	
	public static class Entrada_SolicitudGenerarTasas extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_Fecha;
		public String p_Fuente;
		public String p_Mercado = "";
	}
	
	public static class Salida_SolicitudGenerarTasas extends BcsPojo{
		public List<ResultadoProceso> P_OUT_RESULTADOPROCESO;
		public Long P_OUT_ADVERTENCIASCONTADOR;
	    public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
	    public Long P_OUT_ERRORESCONTADOR;
	    public List<ErrorDetalle> P_OUT_ERRORESDETALLE;
	    private List<MensajeSalida> P_OUT_MENSAJES;
	    
	    public Salida_SolicitudGenerarTasas() {
	    	this.P_OUT_RESULTADOPROCESO = new ArrayList<ResultadoProceso>();
	    	this.P_OUT_ADVERTENCIASCONTADOR = 0L;
	    	this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<AdvertenciaDetalle>(); 
	        this.P_OUT_ERRORESCONTADOR = 0L;
	        this.P_OUT_ERRORESDETALLE = new ArrayList<ErrorDetalle>();
	        this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
	    }

	}
	
	public static class ResultadoProceso extends BcsPojo{
		public String NombreParametro;
		public String Valor;
	}
	
	public static class CodigoFuente extends BcsPojo{
		public String Codigo;
		public String Descripcion;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		Entrada_SolicitudGenerarTasas in = gson.fromJson(jin.toString(),
				Entrada_SolicitudGenerarTasas.class);
		Salida_SolicitudGenerarTasas out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();

		return dataout;
	}
	
	@SuppressWarnings("unchecked")
	public Salida_SolicitudGenerarTasas logica(Connection con, Entrada_SolicitudGenerarTasas entrada) throws Exception {
		logger.info("INICIO::SolicitudGenerarTasas.logica");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		
		String tagMercado = entrada.p_Mercado.isEmpty() ? "Todos":  entrada.p_Mercado;
		
		Salida_SolicitudGenerarTasas salida = new Salida_SolicitudGenerarTasas();
		
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
		
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			salida.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
		}
		
		entrada.p_Fuente = obtenerCodigoFuenteActiva(con, entrada);
		
		if(entrada.p_Fuente.isEmpty()) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_FUENTE_NO_ENCONTRADA", null, con, entrada.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR++;
			return salida;
		}
		
		try {
			HashMap<String, Object> paramsIn = new HashMap<String, Object>(7);

			paramsIn.put("p_IdEmpresa", entrada.p_IdEmpresa);
			paramsIn.put("p_IdUsuario", entrada.p_IdUsuario);
			paramsIn.put("p_IdModulo", entrada.p_IdModulo.toString());
			paramsIn.put("p_UsaReglasNegocio", entrada.p_UsaReglasNegocio.toString());
			paramsIn.put("p_Fecha", entrada.p_Fecha.toString());
			paramsIn.put("p_Fuente", entrada.p_Fuente);
			paramsIn.put("p_Mercado", entrada.p_Mercado);

			HashMap<String, Class> paramsOut = new HashMap<String, Class>(5);

			paramsOut.put("p_out_ResultadoProceso", ResultadoProceso.class);
			paramsOut.put("p_out_AdvertenciasContador", Long.class);
			paramsOut.put("p_out_AdvertenciasDetalle", AdvertenciaDetalle.class);
			paramsOut.put("p_out_ErroresContador", Long.class);
			paramsOut.put("p_out_ErroresDetalle", ErrorDetalle.class);

			HashMap<String, Object> respuestaGenerarArchivoTasas = sql.call(con, SQL_CALL_GENERAR_ARCHIVO_TASAS, paramsIn, paramsOut);
			
			List<ResultadoProceso> resultadosProceso = (List<ResultadoProceso>)respuestaGenerarArchivoTasas.get("p_out_ResultadoProceso");
			long cantidadRegistros = resultadosProceso.stream().filter(x -> "CantidadRegistros".equals(x.NombreParametro)).mapToLong(x -> Long.parseLong(x.Valor)).findFirst().orElse(0L); 
			StatsD.reportCount(METRIC_VOLUMEN_DATOS, cantidadRegistros, "fecha:"+ entrada.p_Fecha, "fuente:"+entrada.p_Fuente, "mercado:" + tagMercado);
					
			if (!respuestaGenerarArchivoTasas.isEmpty()) {
				salida.P_OUT_RESULTADOPROCESO = (List<ResultadoProceso>) respuestaGenerarArchivoTasas.get("p_out_ResultadoProceso");
				salida.P_OUT_ADVERTENCIASCONTADOR = (Long) respuestaGenerarArchivoTasas.get("p_out_AdvertenciasContador");
				salida.P_OUT_ADVERTENCIASDETALLE = (List<AdvertenciaDetalle>) respuestaGenerarArchivoTasas.get("p_out_AdvertenciasDetalle");
				salida.P_OUT_ERRORESCONTADOR = (Long) respuestaGenerarArchivoTasas.get("p_out_ErroresContador");
				salida.P_OUT_ERRORESDETALLE = (List<ErrorDetalle>) respuestaGenerarArchivoTasas.get("p_out_ErroresDetalle");
					
				if(salida.P_OUT_ERRORESCONTADOR > 0) {
					for(ErrorDetalle error : salida.P_OUT_ERRORESDETALLE) {
						salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, error.Codigo, error.Descripcion));
					}
				}else {				
					salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "UI_PROCESO_EJECUTADO_EXITO", null, con, entrada.p_IdEmpresa));
				}	
			} else {
				salida.P_OUT_ERRORESCONTADOR++;
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_SOLICITUD_TASAS", null, con, entrada.p_IdEmpresa));
			}
		} catch (Exception e) {
			salida.P_OUT_ERRORESCONTADOR++;
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_PROCESO_EJECUTADO_ERROR", null, con, entrada.p_IdEmpresa));
			e.printStackTrace();
		}
		
	    long fin = System.currentTimeMillis();
		logger.info("FIN::SolicitudGenerarTasas.logica", fin - inicio);
		
		String mercado = entrada.p_Mercado.isEmpty() ? "Todos":  entrada.p_Mercado;
		StatsD.reportExecutionTimeMilis(METRIC_TIEMPO_EJECUCION, (fin - inicio), "fecha:"+ entrada.p_Fecha, "fuente:"+entrada.p_Fuente, "mercado:" + tagMercado);
	    
		return salida;
	}
	
	private String obtenerCodigoFuenteActiva(Connection con, Entrada_SolicitudGenerarTasas entrada) throws Exception{
		logger.info("INICIO::SolicitudGenerarTasas.obtenerCodigoFuenteActiva");
		long inicio = System.currentTimeMillis();

		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<String, Object>(2);
		
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("Fuente", entrada.p_Fuente);
		
		List<CodigoFuente> codigos = sql.query(con, SQL_OBTENER_FUENTE_ACTIVA, params, CodigoFuente.class);
		
		String codigo = codigos.parallelStream().findFirst().map(x -> x.Codigo).orElse("");
		long fin = System.currentTimeMillis();
		logger.info("FIN::SolicitudGenerarTasas.obtenerCodigoFuenteActiva", fin - inicio);
		
		return codigo;
	}

	public static void main(String[] args)  throws Exception{
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa","connections.properties")) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"N\",\"p_Fecha\":\"2023-07-07\",\"p_Fuente\":\"RISK - TASASMERCADO\",\"p_Mercado\":\"\"}";
			
			Entrada_SolicitudGenerarTasas Objin = gson.fromJson(json,Entrada_SolicitudGenerarTasas.class);
			SolicitudGenerarTasas demo = new SolicitudGenerarTasas();

			try {
				Salida_SolicitudGenerarTasas out = demo.logica(con, Objin);
				System.out.println(out);
				con.rollback();
				//con.commit();
			} catch (Exception er) {
				//er.printStackTrace();
				con.rollback();
			}
		}
		System.out.println("== END ==");

	}
}
