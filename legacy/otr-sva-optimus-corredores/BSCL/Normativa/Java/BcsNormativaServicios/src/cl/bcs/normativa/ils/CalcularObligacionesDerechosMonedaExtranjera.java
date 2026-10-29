package cl.bcs.normativa.ils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.normativa.ils.ObtenerSaldoCuentaContable.UpdateSaldosCuentasContables;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import java.util.Properties;

import cl.bcs.plataforma.pla.InsertarLogServicio;
import cl.bcs.plataforma.pla.InsertarLogServicio.RegistroLog;

public class CalcularObligacionesDerechosMonedaExtranjera implements JavaCall {
	
	
	public static class ObligacionesDerechos {
		public BigDecimal ObligacionesTasaPapel;
		public BigDecimal DerechosTasaPapel;
		public BigDecimal DerechosTasaPactos;
		public BigDecimal ObligacionesTasaPactos;
		public BigDecimal ObligacionesTasaPapelMenores8Dias;
		public BigDecimal DerechosTasaPapelMenores8Dias;
		public BigDecimal DerechosTasaPactosMenores8Dias;
		public BigDecimal ObligacionesTasaPactosMenores8Dias;
		
	}
	
	public static class UpsertObligacionesDerechos {
		public String FechaContable;
		public BigDecimal ObligacionesTasaPapel;
		public BigDecimal DerechosTasaPapel;
		public BigDecimal DerechosTasaPactos;
		public BigDecimal ObligacionesTasaPactos;
		public BigDecimal ObligacionesTasaPapelMenores8Dias;
		public BigDecimal DerechosTasaPapelMenores8Dias;
		public BigDecimal DerechosTasaPactosMenores8Dias;
		public BigDecimal ObligacionesTasaPactosMenores8Dias;
		public long IdEmpresa;
	}
	
	public static class CalcularObligacionesDerechosMonedaExtranjeraEntrada {
		public Long p_IdEmpresa;
		public String p_fechaContable;
	}
	
	public static class CalcularObligacionesDerechosMonedaExtranjeraSalida {
		public Long P_OUT_ERRORESCONTADOR;
		public String P_OUT_MENSAJE;
	}
	
	private final String SQL_SELECT_OBLIGACIONES_DERECHOS = loadResourceToString("cl/bcs/normativa/ils/sql/CalcularObligacionesDerechosMonedaExtranjera_SelectObligacionesDerechos.sql");
	private final String SQL_UPSERT_OBLIGACIONES_DERECHOS = loadResourceToString("cl/bcs/normativa/ils/sql/CalcularObligacionesDerechosMonedaExtranjera_UpsertObligacionesDerechos.sql");
	
	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		CalcularObligacionesDerechosMonedaExtranjeraEntrada in=gson.fromJson(jin.toString() ,CalcularObligacionesDerechosMonedaExtranjeraEntrada.class);
		CalcularObligacionesDerechosMonedaExtranjeraSalida out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public CalcularObligacionesDerechosMonedaExtranjeraSalida logica(Connection con, CalcularObligacionesDerechosMonedaExtranjeraEntrada entrada) throws Exception {
		CalcularObligacionesDerechosMonedaExtranjeraSalida out = new CalcularObligacionesDerechosMonedaExtranjeraSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		out.P_OUT_MENSAJE = "";
		
        try {
	        String fechaConsulta = entrada.p_fechaContable;
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        	Calendar c = Calendar.getInstance();
        	c.setTime(sdf.parse(fechaConsulta));
        	c.add(Calendar.DATE, 7);
        	String fechaHasta = sdf.format(c.getTime());
	        Long idEmpresa = entrada.p_IdEmpresa;
	        SQL sql = new SQL();
	        HashMap<String,Object> hs = new HashMap<String,Object>();
	        hs.put("FechaContable", fechaConsulta);
	        hs.put("FechaHasta", fechaHasta);
	        List<UpsertObligacionesDerechos> lstUpsertSaldosCuentasContables = new ArrayList<UpsertObligacionesDerechos>();
			List<ObligacionesDerechos> lstDerechosObligaciones = sql.query(con, SQL_SELECT_OBLIGACIONES_DERECHOS, hs, ObligacionesDerechos.class);
	        for (ObligacionesDerechos derechosObligaciones : lstDerechosObligaciones) {
	        	UpsertObligacionesDerechos upsertObligacionesDerechos = new UpsertObligacionesDerechos();
	        	upsertObligacionesDerechos.FechaContable = fechaConsulta;
	        	upsertObligacionesDerechos.ObligacionesTasaPapel = derechosObligaciones.ObligacionesTasaPapel;
	        	upsertObligacionesDerechos.DerechosTasaPapel = derechosObligaciones.DerechosTasaPapel;
	        	upsertObligacionesDerechos.DerechosTasaPactos = derechosObligaciones.DerechosTasaPactos;
	        	upsertObligacionesDerechos.ObligacionesTasaPactos = derechosObligaciones.ObligacionesTasaPactos;
	        	upsertObligacionesDerechos.ObligacionesTasaPapelMenores8Dias = derechosObligaciones.ObligacionesTasaPapelMenores8Dias;
	        	upsertObligacionesDerechos.DerechosTasaPapelMenores8Dias = derechosObligaciones.DerechosTasaPapelMenores8Dias;
	        	upsertObligacionesDerechos.DerechosTasaPactosMenores8Dias = derechosObligaciones.DerechosTasaPactosMenores8Dias;
	        	upsertObligacionesDerechos.ObligacionesTasaPactosMenores8Dias = derechosObligaciones.ObligacionesTasaPactosMenores8Dias;
	        	upsertObligacionesDerechos.IdEmpresa = idEmpresa;
	        	lstUpsertSaldosCuentasContables.add(upsertObligacionesDerechos);
	        }
	        sql.batch(con, SQL_UPSERT_OBLIGACIONES_DERECHOS, lstUpsertSaldosCuentasContables, UpsertObligacionesDerechos.class);
	        
	 		InsertarLogServicio insertarLogService = new InsertarLogServicio();
	 		List<RegistroLog> lstDatosLog = new ArrayList<RegistroLog>();
	 		
	 		String Glosa = "Se ha realizado el cálculo de obligaciones.";
	 		String Valor1 = fechaConsulta;
	 		String Valor2 = "CalcularObligacionesDerechosMonedaExtranjera";
	 		
	 		if(out.P_OUT_ERRORESCONTADOR.equals(0L)) {
	 			lstDatosLog.add(insertarLogService.armaRegistroLog("NORMATIVA",entrada.p_IdEmpresa,"INFORMACION",Glosa,Valor1,Valor2));
	 		}
	 				
	 		if(lstDatosLog.size() >= 1) {
	 			insertarLogService.logica(con,lstDatosLog);
	 		}else {
	 			String GlosaError = "Error en el cálculo.";
	 			String Valor1Error = fechaConsulta;
	 			String Valor2Error = "CalcularObligacionesDerechosMonedaExtranjera";
	 			lstDatosLog.add(insertarLogService.armaRegistroLog("NORMATIVA",entrada.p_IdEmpresa,"ERROR",GlosaError,Valor1Error,Valor2Error));
	 			insertarLogService.logica(con,lstDatosLog);
	 		}	        
	        
        } catch (SQLException e) {
        	out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJE = "";
			e.printStackTrace();
			return out;
		}
        
        con.commit();
		return out;
		 
	}	
	
	public static void main (String [] args) {
		
        //prueba de la clase
  		try {
  			Connection con = PlataformaUtils.getDESAConnection();
  			try{
  				
  				CalcularObligacionesDerechosMonedaExtranjeraEntrada entrada = new CalcularObligacionesDerechosMonedaExtranjeraEntrada();
  				entrada.p_IdEmpresa		= new Long(1);
  				entrada.p_fechaContable	= "2018-04-04";
  				JsonParser parser 		= new JsonParser();
  				JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
  				CalcularObligacionesDerechosMonedaExtranjera demo = new CalcularObligacionesDerechosMonedaExtranjera();
  				System.out.println(demo.call(con, dataJsonIn).toString());
  				con.commit();
  				
  			}catch(Exception e){
  				con.rollback();
  				e.printStackTrace();
  			}finally{
  				con.close();
  			}
  		}catch (Exception e) {
  			e.printStackTrace();
  		}
  		
        System.out.println("== END ==");
	}
	
	
}
