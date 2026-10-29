package cl.bcs.socionegocio.eje;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ReporteGestionClienteInstrumento implements JavaCall {

	// -------------------------------------------------------------------------------------------
	// SERVICIO DEL REPORTE GESTION CLIENTE INSTRUMENTO
	// SERVICIO No. 2900
	// -------------------------------------------------------------------------------------------	
	
	private final static Logger logger = LogManager.getLogger(ReporteGestionClienteInstrumento.class);
	private final String SQL_SELECT_INTRUMENTOS = loadResourceToString("cl/bcs/socionegocio/eje/sql/ConsultaReporteGestionClienteInstrumento.sql");	
	
	// -------------------------------------------------------------------------------------------
	// CLASE DE ENTRADA DEL SERVICIO
	// -------------------------------------------------------------------------------------------	
	public static class ReporteGestionClienteInstrumento_Entrada {
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
    	public Long 	p_IdInstrumento;
    	public String 	p_FechaConsulta;
    	
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	// -------------------------------------------------------------------------------------------
	// CLASE DE SALIDA DEL SERVICIO
	// -------------------------------------------------------------------------------------------	
	public static class ReporteGestionClienteInstrumento_Salida {
    	public List<Tabla_Salida> P_OUT_TBL;
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
	
		public ReporteGestionClienteInstrumento_Salida(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_TBL = new ArrayList<Tabla_Salida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
	
	// -------------------------------------------------------------------------------------------
	// CLASE DE SALIDA DEL SQL
	// -------------------------------------------------------------------------------------------		
	public static class Tabla_Salida{
		public String 		NombreCliente;
		public String 		Nemotecnico;
		public BigDecimal 	Pesos;
		public BigDecimal 	Nominales;
		public BigDecimal 	Porcentaje;
		public BigDecimal 	TotalPesos;
		public BigDecimal 	TotalNominales;
		public BigDecimal 	TotalPorcentaje;
		
		public Tabla_Salida() {
			this.Pesos 				= BigDecimal.ZERO;
			this.Nominales 			= BigDecimal.ZERO;
			this.Porcentaje 		= BigDecimal.ZERO;
			this.TotalPesos 		= BigDecimal.ZERO;	
			this.TotalNominales 	= BigDecimal.ZERO;
			this.TotalPorcentaje 	= BigDecimal.ZERO;
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	// -------------------------------------------------------------------------------------------
	// LOGICA DEL SERVICIO
	// -------------------------------------------------------------------------------------------		
	public ReporteGestionClienteInstrumento_Salida logica(Connection conn, ReporteGestionClienteInstrumento_Entrada entrada) throws Exception {
		logger.info("INICIO DEL PROCESO...");
		SQL sql = new SQL();
		
		ReporteGestionClienteInstrumento_Salida salida = new ReporteGestionClienteInstrumento_Salida();
		salida.P_OUT_ERRORESCONTADOR= 1L;
		try {		
			List<ReporteGestionClienteInstrumento_Entrada> listEntrada = new ArrayList<ReporteGestionClienteInstrumento_Entrada>();
			listEntrada.add(entrada);			// Get all the parameters
			
			if (listEntrada.get(0).p_IdEmpresa == null) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR, "UI_PROCESO_EJECUTADO_ERROR", "Ingrese el Id de la empresa"));
				
			} else if (listEntrada.get(0).p_IdInstrumento == null) {
				String msg = Diccionario.getMensaje(conn, entrada.p_IdEmpresa, "DEBE_INGRESAR_INSTRUMENTO").msg();					
				salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR, "DEBE_INGRESAR_INSTRUMENTO", msg));
				
			} else if (listEntrada.get(0).p_FechaConsulta == null || listEntrada.get(0).p_FechaConsulta.equals("")) {
				String msg = Diccionario.getMensaje(conn, entrada.p_IdEmpresa,"FECHA_OPERACION_VACIA").msg();						
				salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR, "FECHA_OPERACION_VACIA", msg));
			} 
			else
			{
				List<Tabla_Salida> ConsultarClientesInstrumento = new ArrayList<>();
				
				HashMap<String, Object> params = new HashMap<>();
				// Add parameters
				params.clear();
				params.put("IdInstrumento", entrada.p_IdInstrumento);
				params.put("FechaEntrada", entrada.p_FechaConsulta);
				
				// Select in the table BSCL.Normativa.BD::CAR.CartolaHistoricaInstrumentos
				ConsultarClientesInstrumento = sql.query(conn, SQL_SELECT_INTRUMENTOS, params, Tabla_Salida.class);	
				System.out.println("Consultando clientes por instrumentos");
				salida.P_OUT_ERRORESCONTADOR= 0L;
				salida.P_OUT_TBL.addAll(ConsultarClientesInstrumento);
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
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ReporteGestionClienteInstrumento_Entrada in = gson.fromJson(jin.toString(), ReporteGestionClienteInstrumento_Entrada.class);
		ReporteGestionClienteInstrumento_Salida out = logica(conn, in);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBA LOCAL ==");		
		Connection con = PlataformaUtils.getDESAConnection() ;
		ReporteGestionClienteInstrumento consultar = new ReporteGestionClienteInstrumento();
			Gson gson = consultar.getGsonBuilder();
			String jsonUniverso = "{\"p_IdEmpresa\":\"1\",\"p_IdUsuario\":\"109\",\"p_IdInstrumento\":\"502\",\"p_FechaConsulta\":\"2019-08-23\"}";	 
			ReporteGestionClienteInstrumento_Entrada entrada = gson.fromJson(jsonUniverso,
					ReporteGestionClienteInstrumento_Entrada.class);
			
			System.out.println(consultar.logica(con, entrada));
	 			
			
		System.out.println("== END ==");
	}
}
