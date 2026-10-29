package cl.bcs.normativa.declaraciones.DJ1889;

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
		public BigDecimal RentabilidadPositiva;
		public BigDecimal RentabilidadNegativa;
	    //Actualizada ($)
		public BigDecimal Factor;
		public BigDecimal RentabilidadActualizadaPositiva;
		public BigDecimal RentabilidadActualizadaNegativa;
	    
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
	
	public static class ReajustarValorIPCEntrada {
		public Long	p_IdEmpresa;
		public Long	p_IdUsuario;
		public String p_IdModulo;
		public Long p_Version;
	}
		
	public static class ReajustarValorIPCSalida {
		public Long P_OUT_ERRORESCONTADOR;
		public String P_OUT_MENSAJE;
	}
	
	private static FileLoader loader = new FileLoader();	
	private static String SQL_SELECT_CONSULTAR_FACTOR_IPC = loader.loadResourceToString("cl/bcs/normativa/declaraciones/DJ1889/sql/ConsultarFactorIpc.sql");
	private static String SQL_SELECT_OBTENER_DETALLE_INICIAL = loader.loadResourceToString("cl/bcs/normativa/declaraciones/DJ1889/sql/ObtenerDetalleIpc.sql");
	private static String SQL_UPDATE_DETALLE = loader.loadResourceToString("cl/bcs/normativa/declaraciones/DJ1889/sql/ActualizarDetalleIpc.sql");
	
	
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
	        
	        HashMap<String,Object> hs = new HashMap<String,Object>();
	        hs.put("Version", entrada.p_Version);
	        List<DetalleInicial> lstDetalleInicial = new ArrayList<DetalleInicial>();
			lstDetalleInicial = sql.query(con, SQL_SELECT_OBTENER_DETALLE_INICIAL, hs, DetalleInicial.class);
			
			
			//Resultado
	        for (DetalleInicial detalleInicial : lstDetalleInicial) {
	        	ValorIPC indiceMesDetalle= (ValorIPC) RCP.get(detalleInicial.AgnoMes);
	        	//System.out.println(indiceMesDetalle.ValorIpc);
	        	detalleInicial.Factor = indiceMesDetalle.Factor; 
	        	detalleInicial.RentabilidadActualizadaPositiva =  detalleInicial.RentabilidadPositiva.multiply(indiceMesDetalle.Factor,MathContext.DECIMAL128).setScale(0,RoundingMode.HALF_UP);
	        	detalleInicial.RentabilidadActualizadaNegativa =  detalleInicial.RentabilidadNegativa.multiply(indiceMesDetalle.Factor,MathContext.DECIMAL128).setScale(0,RoundingMode.HALF_UP);
	        	//System.out.println(detalleInicial.RentabilidadPositiva);
	        	//System.out.println(variacionPorcentual);
	        	//System.out.println(detalleInicial.RentabilidadActualizadaPositiva);

	        }

	        System.out.println(lstDetalleInicial);
		    //actualizacion de registros    
		    sql.batch(con, SQL_UPDATE_DETALLE, lstDetalleInicial, DetalleInicial.class);

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
  				entrada.p_IdUsuario	= 51L;
  				entrada.p_IdModulo	= "NOR";
  				entrada.p_Version	= 2018L;
  				ReajustarValorIPCSalida Salida = new ReajustarValorIPCSalida();
  				ReajustarValorIPC demo = new ReajustarValorIPC();
  				Salida = demo.logica(con,entrada);
  				System.out.println(Salida.P_OUT_MENSAJE);
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