package cl.bcs.socionegocio.eje;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import java.math.MathContext;



public class ReporteGestionCliente implements JavaCall {
	private final Log log = new Log("PLA.ReporteGestionCliente");
	private final String SQL_CONSULTA_CLIENTE_INSTRUMENTO 			= loadResourceToString("cl/bcs/socionegocio/eje/sql/ConsultaClienteInstrumentos.sql");
	private final String SQL_CONSULTA_CLIENTE_INSTRUMENTO_HISTORICO = loadResourceToString("cl/bcs/socionegocio/eje/sql/ConsultaClienteInstrumentosHistorico.sql");


	public static class ReporteGestionCliente_IN {
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_FechaConsulta;		
		public String 	p_IdClasificacionTipoInstrumento = "";
		public String 	p_IdTipoInstrumento =  "";        
        
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ReporteGestionCliente_OUT {
		public List<Tabla_Salida> P_OUT_TBL;
		public Long P_OUT_ERRORESCONTADOR = 0L;
		
		public ReporteGestionCliente_OUT(){
			this.P_OUT_TBL=new ArrayList<Tabla_Salida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Tabla_Salida{
		public Long 		IdCliente;
		public String 		NombreCliente;
		public String		Nemotecnico;
		public BigDecimal 	Pesos;
		public BigDecimal 	Nominales;
		public BigDecimal 	Porcentaje;
		public BigDecimal 	TotalPesos;
		public BigDecimal 	TotalNominales;
		

		public Tabla_Salida() {
			this.Pesos = BigDecimal.ZERO;
			this.Nominales = BigDecimal.ZERO;
			this.TotalPesos = BigDecimal.ZERO;
			this.TotalNominales = BigDecimal.ZERO;
			this.Porcentaje = BigDecimal.ZERO;
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	
	}

	
	// -------------------------------------------------------------------
	// LOGICA
	// -------------------------------------------------------------------
	
	
	public ReporteGestionCliente_OUT logica(Connection conn, ReporteGestionCliente_IN in) throws Exception {
		SQL _sql = new SQL();
		Long Id = 0L;
		HashMap<String, String> mapMsg = new HashMap<>();
		log.write("INICIO::PLA.ReporteGestionCliente");
		ReporteGestionCliente_OUT out = new ReporteGestionCliente_OUT();
		HashMap<String, Object> params = new HashMap<>();
		List<Tabla_Salida> salida = new ArrayList<>();
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA","PLA.FechaSistema");
		params.clear();
		params.put("FechaConsulta",in.p_FechaConsulta);		
		params.put("IdClasificacionTipoInstrumento",in.p_IdClasificacionTipoInstrumento);
		params.put("IdTipoInstrumento",in.p_IdTipoInstrumento);
		

		if(in.p_FechaConsulta.equals(fechaSistema)){
			//Consulta Cartera
			salida = _sql.query(conn,SQL_CONSULTA_CLIENTE_INSTRUMENTO,params,Tabla_Salida.class);
			
		}else if (!(in.p_FechaConsulta.equals(fechaSistema))){
			//Consulta Cartera Historica
			salida = _sql.query(conn,SQL_CONSULTA_CLIENTE_INSTRUMENTO_HISTORICO,params,Tabla_Salida.class);
		}
		

		// CALCULO DE TOTALES
		for(Tabla_Salida x: salida)
		{			
			for(Tabla_Salida y: salida)
			{
				if (x.IdCliente.compareTo(y.IdCliente) == 0)
				{
					x.TotalPesos = x.TotalPesos.add(y.Pesos);
					x.TotalNominales = x.TotalNominales.add(y.Nominales);					
				}
			}
		}
		
		
		// CALCULO DE PORCENTAJE
		for(Tabla_Salida x: salida)
		{
			x.Porcentaje = ((x.TotalPesos.compareTo(new BigDecimal(0L)) == 0 ?  new BigDecimal(0L) : x.Pesos.divide(x.TotalPesos, MathContext.DECIMAL128)).multiply(new BigDecimal(100L)));
		}
		
		
		out.P_OUT_TBL.addAll(salida);
		log.write("FIN::PLA.ReporteGestionCliente");
		return out;

	}

	
	
	// -------------------------------------------------------------------
	// CALL 
	// -------------------------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = Utils.getGsonBuilder();
		ReporteGestionCliente_IN jin = gson.fromJson(in.toString(), ReporteGestionCliente_IN.class);
		ReporteGestionCliente_OUT out = logica(conn, jin);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};

	
	// -------------------------------------------------------------------
	// PARA PRUEBAS LOCALES
	// -------------------------------------------------------------------
	public static void main(String[] args) throws Exception, SQLException {
		// TODO Auto-generated method stub
		Connection conn = null;
		Gson gson = new Gson();

		try {
			System.out.println("== BEGIN ==");
			conn = PlataformaUtils.getDESAConnection();

			//String json = "{p_IdEmpresa: 1, p_IdUsuario: 5, p_IdClasificacionTipoInstrumento: \"RV\", p_IdTipoInstrumento: \"ACN\", p_FechaConsulta: \"2021-07-22\"}";
			
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":5,\"p_FechaConsulta\":\"2021-07-22\",\"p_IdClasificacionTipoInstrumento\":\"RV\",\"p_IdTipoInstrumento\":\"\"}";
			
			
			
			ReporteGestionCliente_IN objIn = gson.fromJson(json, ReporteGestionCliente_IN.class);

			ReporteGestionCliente demo = new ReporteGestionCliente();

			ReporteGestionCliente_OUT ObjOut = demo.logica(conn, objIn);

			System.out.println("SALIDA : " + ObjOut.toString());

			conn.rollback();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
		System.out.println("== END ==");

	};
}
