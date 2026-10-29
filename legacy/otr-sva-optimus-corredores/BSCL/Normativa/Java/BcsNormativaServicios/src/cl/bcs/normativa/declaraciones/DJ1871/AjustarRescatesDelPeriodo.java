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

public class AjustarRescatesDelPeriodo implements JavaCall {
	
	public static class DetalleInicial {
		public Long				ID;
		public Long				Numero;
		public String			RutCliente;
		public Long				IdCliente;
		public java.sql.Date	FechaOperacion;
		public Long				AnoAhorro;
		// Ahorros
		public BigDecimal		AhorrosDeposito;
		public BigDecimal		AhorrosDepositoActualizado;
		// Retiros
		public BigDecimal		RetirosDeposito;
		public BigDecimal		RetirosDepositoActualizado;
		
	    public String toString() {
			return Utils.printJson(this, true);
		}
	}
	
	public static class SaldoPeriodoAnterior {
		public String			RutCliente;
		public Long				IdCliente;
		public BigDecimal		AhorrosDeposito;
		public Long				AnoAhorro;
		
		public String toString() {
			return Utils.printJson(this, true);
		}
	}
	
	public static class ParametroUpdateDelete {
		public Long				p_ID;
		public Long				p_Numero;
		public Long				p_IdCliente;
		public BigDecimal		p_RetirosDeposito;
		
		public String toString() {
			return Utils.printJson(this, true);
		}
	}
	
	public static class AjustarRescatesDelPeriodoEntrada {
		public Long				p_IdEmpresa;
		public Long				p_IdUsuario;
		public String			p_IdModulo;
		public Long				p_Version;
	}
		
	public static class AjustarRescatesDelPeriodoSalida {
		public Long				P_OUT_ERRORESCONTADOR;
		public String			P_OUT_MENSAJE;
	}
	
	private static FileLoader loader = new FileLoader();	
	private static String SQL_SELECT_DETALLE_INICIAL			= loader.loadResourceToString("cl/bcs/normativa/declaraciones/DJ1871/sql/AjustarRescatesDelPeriodo_SelectDetalleInicial.sql");
	private static String SQL_SELECT_SALDO_PERIODO_ANTERIOR		= loader.loadResourceToString("cl/bcs/normativa/declaraciones/DJ1871/sql/AjustarRescatesDelPeriodo_SelectSaldoPeriodoAnterior.sql");
	private static String SQL_UPDATE_SALDO						= loader.loadResourceToString("cl/bcs/normativa/declaraciones/DJ1871/sql/AjustarRescatesDelPeriodo_UpdateSaldo.sql");
	private static String SQL_DELETE_DETALLE					= loader.loadResourceToString("cl/bcs/normativa/declaraciones/DJ1871/sql/AjustarRescatesDelPeriodo_DeleteDetalle.sql");
	private static String SQL_UPDATE_DETALLE					= loader.loadResourceToString("cl/bcs/normativa/declaraciones/DJ1871/sql/AjustarRescatesDelPeriodo_UpdateDetalle.sql");
	
	/**********************/
	/** Método Principal **/
	/**********************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		AjustarRescatesDelPeriodoEntrada in=gson.fromJson(jin.toString() ,AjustarRescatesDelPeriodoEntrada.class);
		AjustarRescatesDelPeriodoSalida out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	/*************************/
	/** Lógica del Programa **/
	/*************************/
	public AjustarRescatesDelPeriodoSalida logica(Connection con, AjustarRescatesDelPeriodoEntrada entrada) throws Exception {
		AjustarRescatesDelPeriodoSalida out = new AjustarRescatesDelPeriodoSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		out.P_OUT_MENSAJE = "";
		
        try {
	        SQL sql = new SQL();
	        BigDecimal nuevoSaldo = new BigDecimal(0);
	        HashMap<String,Object> hashMap = new HashMap<String,Object>();
	        hashMap.put("p_AnoAhorro", entrada.p_Version);
	        
	        List<DetalleInicial> listaDetalleInicial = new ArrayList<DetalleInicial>();
	        listaDetalleInicial = sql.query(con, SQL_SELECT_DETALLE_INICIAL, hashMap, DetalleInicial.class);
	        
	        List<SaldoPeriodoAnterior> listaSaldoPeriodoAnterior = new ArrayList<SaldoPeriodoAnterior>();
	        listaSaldoPeriodoAnterior = sql.query(con, SQL_SELECT_SALDO_PERIODO_ANTERIOR, hashMap, SaldoPeriodoAnterior.class);

	        for (SaldoPeriodoAnterior saldoPeriodoAnterior : listaSaldoPeriodoAnterior) {
	        	if (saldoPeriodoAnterior.AhorrosDeposito.compareTo(BigDecimal.ZERO) == 1) {
	        		nuevoSaldo = saldoPeriodoAnterior.AhorrosDeposito;
	        		for (DetalleInicial detalleInicial : listaDetalleInicial) {
	        			if (detalleInicial.IdCliente.equals(saldoPeriodoAnterior.IdCliente)) {
	        				if (nuevoSaldo.compareTo(BigDecimal.ZERO) == 1) {
		        				nuevoSaldo = nuevoSaldo.subtract(detalleInicial.RetirosDeposito);
		        				if (detalleInicial.RetirosDeposito.intValue() != 0) {
		        					List<ParametroUpdateDelete> updateDelete = new ArrayList<ParametroUpdateDelete>();
		        					ParametroUpdateDelete parametroSaldo = new ParametroUpdateDelete();
		        					parametroSaldo.p_ID = detalleInicial.ID;
		        					parametroSaldo.p_Numero = detalleInicial.Numero;
		        					parametroSaldo.p_IdCliente = detalleInicial.IdCliente;
		        					parametroSaldo.p_RetirosDeposito = nuevoSaldo.abs();
		        					updateDelete.add(parametroSaldo);
		        					
		        					if (nuevoSaldo.compareTo(BigDecimal.ZERO) == 1) {
		        						sql.batch(con, SQL_DELETE_DETALLE, updateDelete, ParametroUpdateDelete.class);
		        					} else {
		        						sql.batch(con, SQL_UPDATE_SALDO, updateDelete, ParametroUpdateDelete.class);
		        					}
		        				}
	        				}
	        			}
			        }
	        	}
	        }
	        
	        List<ParametroUpdateDelete> updateDelete = new ArrayList<ParametroUpdateDelete>();
	        sql.batch(con, SQL_UPDATE_DETALLE, updateDelete, ParametroUpdateDelete.class);

        } catch (SQLException e) {
        	out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJE = "Error : " + e;
			e.printStackTrace();
			return out;
		}
		
        con.commit();
		return out;
	}
	
	public static void main (String [] args) throws Exception
	{
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
  		try {
  			//Connection con = PlataformaUtils.getDESAConnection();
  			Connection con = PlataformaUtils.getQAConnection();
  			try{
  				
  				AjustarRescatesDelPeriodoEntrada entrada = new AjustarRescatesDelPeriodoEntrada();
  				entrada.p_IdEmpresa	= 1L;
  				entrada.p_IdUsuario	= 1L;
  				entrada.p_IdModulo	= "NOR";
  				entrada.p_Version	= 2018L;
  				
  				AjustarRescatesDelPeriodoSalida Salida = new AjustarRescatesDelPeriodoSalida();
  				AjustarRescatesDelPeriodo demo = new AjustarRescatesDelPeriodo();
  				
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