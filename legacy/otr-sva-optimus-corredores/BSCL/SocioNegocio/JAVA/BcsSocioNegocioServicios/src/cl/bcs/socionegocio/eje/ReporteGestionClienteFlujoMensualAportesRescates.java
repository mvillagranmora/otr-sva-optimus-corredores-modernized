package cl.bcs.socionegocio.eje;

import java.math.BigDecimal;
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

import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ReporteGestionClienteFlujoMensualAportesRescates implements JavaCall {

	private final static Logger logger = LogManager.getLogger(ReporteGestionClienteFlujoMensualAportesRescates.class);
	private final String SQL_SELECT_FLUJO_MENSUAL_APORTES_RESCATES = loadResourceToString("cl/bcs/socionegocio/eje/sql/ConsultaFlujoMensualAportesRescates.sql");
	
    public static class EntradaParametros{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;		
    	public Long p_IdAgente;
    	public String p_agno;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
    public static class SalidaProcesamiento {
    	public List<Tabla_Salida> P_OUT_TBL;
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
	
		public SalidaProcesamiento(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_TBL = new ArrayList<Tabla_Salida>();
		} 
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
    
	public static class Tabla_Salida{
		public String 		NombreAgente;
		public String 		NombreCliente;
		public BigDecimal 	Monto_01;
		public BigDecimal 	Monto_02;
		public BigDecimal 	Monto_03;		
		public BigDecimal 	Monto_04;
		public BigDecimal 	Monto_05;
		public BigDecimal 	Monto_06;		
		public BigDecimal 	Monto_07;
		public BigDecimal 	Monto_08;
		public BigDecimal 	Monto_09;		
		public BigDecimal 	Monto_10;
		public BigDecimal 	Monto_11;
		public BigDecimal 	Monto_12;
		
		public BigDecimal 	TotalPeriodo;
		public Tabla_Salida() {
			this.Monto_01 = BigDecimal.ZERO;
			this.Monto_02 = BigDecimal.ZERO;
			this.Monto_03 = BigDecimal.ZERO;
			this.Monto_04 = BigDecimal.ZERO;	
			this.Monto_05 = BigDecimal.ZERO;
			this.Monto_06 = BigDecimal.ZERO;
			this.Monto_07 = BigDecimal.ZERO;
			this.Monto_08 = BigDecimal.ZERO;
			this.Monto_09 = BigDecimal.ZERO;
			this.Monto_10 = BigDecimal.ZERO;
			this.Monto_11 = BigDecimal.ZERO;
			this.Monto_12 = BigDecimal.ZERO;				
			this.TotalPeriodo = BigDecimal.ZERO;
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}    
    
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();		
		EntradaParametros in = gson.fromJson(jin.toString() ,EntradaParametros.class);
		
		SalidaProcesamiento out = logica(conn, in);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	private SalidaProcesamiento logica(Connection conn, EntradaParametros entrada) throws Exception {
		logger.info("INICIO DEL PROCESO...");
		SQL sql = new SQL();
		SalidaProcesamiento salida = new  SalidaProcesamiento();
		
		salida.P_OUT_ERRORESCONTADOR= 1L;
		try {		
			List<EntradaParametros> listEntrada = new ArrayList<EntradaParametros>();
			listEntrada.add(entrada);			// Get all the parameters
			
			if (listEntrada.get(0).p_IdEmpresa == null) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR, "UI_PROCESO_EJECUTADO_ERROR", "Ingrese el Id de la empresa"));
				
			} else if (listEntrada.get(0).p_IdAgente == null) {
				String msg = Diccionario.getMensaje(conn, entrada.p_IdEmpresa, "CODIGO_AGENTE_VACIO").msg();					
				salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR, "CODIGO_AGENTE_VACIO", msg));
				
			} else if (listEntrada.get(0).p_agno == null || listEntrada.get(0).p_agno.equals("")) {
				String msg = Diccionario.getMensaje(conn, entrada.p_IdEmpresa,"FECHA_OPERACION_VACIA").msg();						
				salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR, "FECHA_OPERACION_VACIA", msg));
			} 
			else
			{
				List<Tabla_Salida> ConsultarFlujoMensual = new ArrayList<>();
				
				HashMap<String, Object> params = new HashMap<>();
				// Add parameters
				params.clear();
				params.put("IdAgente", entrada.p_IdAgente);
				params.put("Agno", entrada.p_agno);
				
				// Select in the table BSCL.Normativa.BD::CAR.CartolaHistorica
				ConsultarFlujoMensual = sql.query(conn, SQL_SELECT_FLUJO_MENSUAL_APORTES_RESCATES, params, Tabla_Salida.class);	
				System.out.println("Consultando flujo mensual de aportes y rescates");
				salida.P_OUT_ERRORESCONTADOR= 0L;
				salida.P_OUT_TBL.addAll(ConsultarFlujoMensual);
			}
		}
				
		catch (Exception e)
		{
			salida.P_OUT_ERRORESCONTADOR= 1L;
			String msg= Diccionario.getMensaje(conn, entrada.p_IdEmpresa,"UI_PROCESO_EJECUTADO_ERROR").msg();					
			salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR,"UI_PROCESO_EJECUTADO_ERROR", msg));
			e.printStackTrace();
		}
		logger.info("PROCESO CULIMINADO");
		return salida;
	}	

	public static void main(String[] args) {
		try (Connection conn = PlataformaUtils.getDESAConnection())
		{
			System.out.println("== INICIO PRUEBA LOCAL ==");
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			//String jsonUniverso = "{\"p_IdEmpresa\":\"1\",\"p_IdUsuario\":\"109\",\"p_IdAgente\":\"11497\",\"p_agno\":\"2018\"}";			
			String jsonUniverso = "{\"p_IdEmpresa\":\"1\",\"p_IdUsuario\":\"109\",\"p_IdAgente\":\"0\",\"p_agno\":\"2019\"}";			
			EntradaParametros entrada = gson.fromJson(jsonUniverso, EntradaParametros.class);
			ReporteGestionClienteFlujoMensualAportesRescates demo = new ReporteGestionClienteFlujoMensualAportesRescates();				
			SalidaProcesamiento salida = demo.logica(conn, entrada);
			
			System.out.println("Salida --> " + salida.toString());
			
		}catch(Exception e){ 
			e.printStackTrace();
		}
		System.out.println("== FIN PRUEBA LOCAL ==");
	}
}

