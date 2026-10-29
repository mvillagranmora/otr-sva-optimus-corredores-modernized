package cl.bcs.normativa.declaraciones.DJ1871;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import java.math.MathContext;
import java.math.RoundingMode;

public class ReajustarValorIPC implements JavaCall {
	
	public static class DetalleInicial {
		public Long IdDocumento;
		public Long ID;
		public Long Numero;
		public String FuenteDatos;
		public java.sql.Date FechaOperacion;
		public String AgnoMes;
		//
		public BigDecimal AhorrosTrabajador;
		public BigDecimal AhorrosTrabajadorActualizado;
		public BigDecimal AhorrosDeposito;
		public BigDecimal AhorrosDepositoActualizado;
		public BigDecimal AhorrosCotizaciones;
		public BigDecimal AhorrosCotizacionesActualizado;
		//Retiros
		public BigDecimal RetirosTrabajador;
		public BigDecimal RetirosTrabajadorActualizado;
		public BigDecimal RetirosDeposito;
		public BigDecimal RetirosDepositoActualizado;
		public BigDecimal RetirosCotizaciones;
		public BigDecimal RetirosCotizacionesActualizado;
	    public String toString() {
			return Utils.printJson(this, true);
		}
	}
	
	public static class ValorIPC {
		public String Agno;
		public String AgnoMes;
		public String AgnoTributario;
	    public String DescMes;
	    public java.sql.Date Fecha;
	    public BigDecimal Factor;
	    public BigDecimal ValorIpc;
		
		public String toString() {
			return Utils.printJson(this, true);
		}
	}
	
	public static class Esquema {
		public java.sql.Date FechaDatosHasta;
		public String AgnoMes;
		
		public String toString() {
			return Utils.printJson(this, true);
		}
	}
	
	public static class ReajustarValorIPCEntrada {
		public Long	p_IdEmpresa;
		public Long	p_IdUsuario;
		public String p_IdModulo;
		public String p_Version;
	}
		
	public static class ReajustarValorIPCSalida {
		public Long P_OUT_ERRORESCONTADOR;
		public String P_OUT_MENSAJE;
	}
	
	private static FileLoader loader = new FileLoader();	
	private static String SQL_SELECT_CONSULTAR_FACTOR_IPC = loader.loadResourceToString("cl/bcs/normativa/declaraciones/DJ1871/sql/ConsultarFactorIpc.sql");
	private static String SQL_SELECT_OBTENER_DETALLE_INICIAL = loader.loadResourceToString("cl/bcs/normativa/declaraciones/DJ1871/sql/ObtenerDetalleIpc.sql");
	private static String SQL_UPDATE_DETALLE = loader.loadResourceToString("cl/bcs/normativa/declaraciones/DJ1871/sql/ActualizarDetalleIpc.sql");
	private static String SQL_SELECT_ESQUEMA = loader.loadResourceToString("cl/bcs/normativa/declaraciones/DJ1871/sql/ObtenerEsquema.sql");
	
	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		ReajustarValorIPCEntrada in=gson.fromJson(jin.toString() ,ReajustarValorIPCEntrada.class);
		ReajustarValorIPCSalida out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public ReajustarValorIPCSalida logica(Connection con, ReajustarValorIPCEntrada entrada) throws Exception {
		ReajustarValorIPCSalida out = new ReajustarValorIPCSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		out.P_OUT_MENSAJE = "";
		
        try {
	        SQL sql = new SQL();
	        HashMap<String,Object> hi = new HashMap<String,Object>();
	        List<ValorIPC> lstValorIPC = new ArrayList<ValorIPC>();
			lstValorIPC = sql.query(con, SQL_SELECT_CONSULTAR_FACTOR_IPC, hi, ValorIPC.class);
			
			HashMap<String,Object> RCP = Utils.List2Hash(lstValorIPC, "AgnoMes");
	        
			HashMap<String,Object> he = new HashMap<String,Object>();
			he.put("Version", entrada.p_Version);
	        List<Esquema> lstEsquema = new ArrayList<Esquema>();
	        lstEsquema = sql.query(con, SQL_SELECT_ESQUEMA, he, Esquema.class);
						
	        HashMap<String,Object> hs = new HashMap<String,Object>();
	        hs.put("Version", entrada.p_Version);
	        List<DetalleInicial> lstDetalleInicial = new ArrayList<DetalleInicial>();
			lstDetalleInicial = sql.query(con, SQL_SELECT_OBTENER_DETALLE_INICIAL, hs, DetalleInicial.class);
			
			ValorIPC indiceMesActual= (ValorIPC) RCP.get(lstEsquema.get(0).AgnoMes);
			//System.out.println(lstEsquema.get(0).AgnoMes);
			//System.out.println(indiceMesActual.ValorIpc);
			//Resultado
	        for (DetalleInicial detalleInicial : lstDetalleInicial) {
	        	ValorIPC indiceMesDetalle= (ValorIPC) RCP.get(detalleInicial.AgnoMes);
	        	//System.out.println(indiceMesDetalle.ValorIpc);
	        	BigDecimal variacionPorcentual = indiceMesActual.ValorIpc.divide(indiceMesDetalle.ValorIpc,MathContext.DECIMAL128).multiply(new BigDecimal(100)).subtract(new BigDecimal(100));
	        	
	        	detalleInicial.AhorrosTrabajadorActualizado =  detalleInicial.AhorrosTrabajador.multiply(variacionPorcentual,MathContext.DECIMAL128).divide(new BigDecimal(100)).add(detalleInicial.AhorrosTrabajador).setScale(0,RoundingMode.HALF_UP);
	        	detalleInicial.AhorrosDepositoActualizado =  detalleInicial.AhorrosDeposito.multiply(variacionPorcentual,MathContext.DECIMAL128).divide(new BigDecimal(100)).add(detalleInicial.AhorrosDeposito).setScale(0,RoundingMode.HALF_UP);
	        	detalleInicial.AhorrosCotizacionesActualizado =  detalleInicial.AhorrosCotizaciones.multiply(variacionPorcentual,MathContext.DECIMAL128).divide(new BigDecimal(100)).add(detalleInicial.AhorrosCotizaciones).setScale(0,RoundingMode.HALF_UP);
	        	//Retiros
	        	detalleInicial.RetirosTrabajadorActualizado =  detalleInicial.RetirosTrabajador.multiply(variacionPorcentual,MathContext.DECIMAL128).divide(new BigDecimal(100)).add(detalleInicial.RetirosTrabajador).setScale(0,RoundingMode.HALF_UP);
	        	detalleInicial.RetirosDepositoActualizado =  detalleInicial.RetirosDeposito.multiply(variacionPorcentual,MathContext.DECIMAL128).divide(new BigDecimal(100)).add(detalleInicial.RetirosDeposito).setScale(0,RoundingMode.HALF_UP);
	        	detalleInicial.RetirosCotizacionesActualizado =  detalleInicial.RetirosCotizaciones.multiply(variacionPorcentual,MathContext.DECIMAL128).divide(new BigDecimal(100)).add(detalleInicial.RetirosCotizaciones).setScale(0,RoundingMode.HALF_UP);
	        	//System.out.println(detalleInicial.MontoRetiroPorAhorrosIncisoPrimero);
	        	//System.out.println(variacionPorcentual);
	        	//System.out.println(detalleInicial.MontoRetiroPorAhorrosIncisoPrimeroIPC);

	        	//System.out.println(lstDetalleInicial);
	    	    //actualizacion de registros    
	    	    sql.batch(con, SQL_UPDATE_DETALLE, lstDetalleInicial, DetalleInicial.class);
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
	
	public static void main (String [] args) throws Exception{
		
        //prueba de la clase
  		try {
  			//Connection con = PlataformaUtils.getDESAConnection();
  			Connection con = PlataformaUtils.getQAConnection();
  			try{
  				
  				ReajustarValorIPCEntrada entrada = new ReajustarValorIPCEntrada();
  				entrada.p_IdEmpresa	= 1L;
  				entrada.p_IdUsuario	= 25L;
  				entrada.p_IdModulo	= "NOR";
  				entrada.p_Version	= "2018";
  				ReajustarValorIPCSalida Salida = new ReajustarValorIPCSalida();
  				ReajustarValorIPC demo = new ReajustarValorIPC();
  				Salida = demo.logica(con,entrada);
  				System.out.println(Salida.P_OUT_MENSAJE);
  				con.commit();
  				
  				//{"p_IdEmpresa":1,"p_IdUsuario":51,"p_IdModulo":"NOR","p_Version":"2018"}
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