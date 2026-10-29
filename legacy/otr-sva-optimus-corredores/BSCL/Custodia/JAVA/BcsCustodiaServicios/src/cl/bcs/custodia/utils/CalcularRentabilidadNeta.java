package cl.bcs.custodia.utils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

import java.text.SimpleDateFormat;

public class CalcularRentabilidadNeta implements JavaCall {

	//private final Log log = new Log("CUS.CalcularRentabilidadNeta");
	
	public static class ObjEntrada {		
		public Long			 IdInterno;//IdCartera
		public Long			 IdCliente;
		public Long			 IdPortafolio;
		public java.sql.Date FechaRetiro;
		public BigDecimal	 MontoSaldo;//monto aporte utm
		public BigDecimal	 MontoRetiro; 
		public BigDecimal	 Cantidad;
				
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	public static class ObjSalida {
		public Long				IdInterno;
		public String			PortafolioTienePropositoAPV;
		public String			TipoFactorRentabilidadNeta;
		public java.sql.Date	FechaFactorRentabilidadNeta;
		public BigDecimal		ValorFactorRentabilidadNeta;
		public BigDecimal		MontoRentabilidadNeta;
		public BigDecimal		MontoRentabilidadNetaActualizada;
		public String			Resultado;
		public String			CodigoMensaje;

		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class CalcularRentabilidadNetaEntrada {
		public Long				p_IdEmpresa; 
		public List<ObjEntrada>	p_Entrada;
		public java.sql.Date	p_FechaRetiro;
		public CalcularRentabilidadNetaEntrada (){
			p_Entrada		= new ArrayList<ObjEntrada>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class CarteraFactor {
		public BigDecimal		Cantidad;
		public String			TipoFactor; 
		public java.sql.Date	FechaFactor;
		public BigDecimal		ValorFactor; 
		public BigDecimal		MontoCompraFactor;
	};
	
	public static class CalcularRentabilidadNetaSalida {
		public List<ObjSalida>	p_Salida;
		public Long				P_OUT_ERRORESCONTADOR;
		
		public CalcularRentabilidadNetaSalida (){
			p_Salida			= new ArrayList<ObjSalida>();			
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ConsultarPortafolio {
		public Long				IdPortafolio;
		public String			TienePropositoAPV;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	private static FileLoader loader = new FileLoader();
	private static String SQL_CONSULTAR_PORTAFOLIO = loader.loadResourceToString("cl/bcs/custodia/sql/CalcularRentabilidadNeta_SEL_Portafolio.sql");
	private static String SQL_CONSULTAR_CARTERA_FACTOR = loader.loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaPortafolio_ConsultaCarteraFactor.sql");
	
	@Trace
	public CalcularRentabilidadNetaSalida logica(Connection con, CalcularRentabilidadNetaEntrada in) throws Exception {		
		CalcularRentabilidadNetaSalida out = new CalcularRentabilidadNetaSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		
		SQL sql = new SQL();
		String MonedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa,"PLATAFORMA", "PLA.MonedaSistema");
		List<Long> IdPortafolios = new ArrayList<Long>();
		
		for (ObjEntrada RP: in.p_Entrada){
			if (RP.IdPortafolio != null && !RP.IdPortafolio.equals(0L))	IdPortafolios.add( RP.IdPortafolio );
		};
		
		HashMap<String,Object> hs = new HashMap<String,Object>();
        hs.put("IdPortafolio", IdPortafolios);
        List<ConsultarPortafolio> lstConsultarPortafolio = new ArrayList<ConsultarPortafolio>();
        lstConsultarPortafolio = sql.query(con, SQL_CONSULTAR_PORTAFOLIO, hs, ConsultarPortafolio.class);
        
        HashMap<Long,Object> RCP2 = Utils.List2Hash(lstConsultarPortafolio, "IdPortafolio");
        HashMap<String,Boolean> fechas = new HashMap<String,Boolean>();
        CalcularParidad.CalcularParidadEntrada CalcParidad = new CalcularParidad.CalcularParidadEntrada();
        for (ObjEntrada entrada: in.p_Entrada){
        	java.sql.Date fecha = entrada.FechaRetiro;
			String strFecha = "0001-01-01";
			if(fecha != null) {
				strFecha = Fecha.toStringDate(fecha);
			}
			if(fechas.get(strFecha) == null) {
				CalcularParidad.CalculoEntrada calculoEntrada = new CalcularParidad.CalculoEntrada();
				
				calculoEntrada.IdTrabajo 			= -5998L;
				calculoEntrada.IdInterno 			= Long.valueOf(strFecha.replaceAll("\\-", ""));
				calculoEntrada.Fecha				= strFecha;
				calculoEntrada.IdMonedaOrigen 		= "UTM";
				calculoEntrada.MontoOrigen			= BigDecimal.ONE;
				calculoEntrada.IdMonedaDestino 		= MonedaSistema;
				calculoEntrada.IdUso				= "CARTERA";
				CalcParidad.p_CalculosEntrada.add(calculoEntrada);
				fechas.put(strFecha, true);
			}
			
			
        }
		
		CalcularParidad.CalcularParidadSalida ResultParidad = new CalcularParidadSalida();
		if (CalcParidad.p_CalculosEntrada != null){
			CalcParidad.p_IdEmpresa 		= in.p_IdEmpresa; 
			CalcParidad.p_IdTrabajo 		= -5998L;
			
			CalcularParidad CP = new CalcularParidad();				
			ResultParidad = CP.logica(con, CalcParidad);
		};
		
        //Resultado
        for (ObjEntrada entrada: in.p_Entrada){
        	
				
			HashMap<Long,Object> RCP = Utils.List2Hash(ResultParidad.p_out_CalculosSalida, "IdInterno");
			String strFecha = Fecha.toStringDate(entrada.FechaRetiro);
			Long longFecha = Long.parseLong(strFecha.replaceAll("\\-", ""));	
        	CalcularParidad.CalculoSalida resultadoParidad = (CalcularParidad.CalculoSalida) RCP.get(longFecha);
			
        	if (resultadoParidad != null){
				ConsultarPortafolio consultaPortafolio = (ConsultarPortafolio) RCP2.get(entrada.IdPortafolio);

    			if (consultaPortafolio.TienePropositoAPV.equals("S")){
		        	
	        		HashMap<String,Object> hs2 = new HashMap<String,Object>();
	    	        hs2.put("IdEmpresa", in.p_IdEmpresa);
	    	        hs2.put("IdCartera", entrada.IdInterno);
	    	        
	    	        List<CarteraFactor> lstConsultarCarteraFactor = new ArrayList<CarteraFactor>();
	    	        lstConsultarCarteraFactor = sql.query(con, SQL_CONSULTAR_CARTERA_FACTOR, hs2, CarteraFactor.class);
	    	        
	    	        BigDecimal cantidadPorUtmOrigen = entrada.Cantidad.multiply(lstConsultarCarteraFactor.get(0).MontoCompraFactor);    	        
	    	        BigDecimal EGRCantidadEnUtmCompra = cantidadPorUtmOrigen.divide(lstConsultarCarteraFactor.get(0).Cantidad,MathContext.DECIMAL128).setScale(4, BigDecimal.ROUND_HALF_UP);
	    	        	    	        
		        	BigDecimal utmRetiro = entrada.MontoRetiro.divide(resultadoParidad.FactorOrigenDestino,MathContext.DECIMAL128).setScale(4, BigDecimal.ROUND_HALF_UP);
		        	BigDecimal diferenciaUtm = utmRetiro.subtract(EGRCantidadEnUtmCompra);
		        	BigDecimal montoActualizado = diferenciaUtm.multiply(resultadoParidad.FactorOrigenDestino).setScale(0, BigDecimal.ROUND_HALF_UP);

					ObjSalida obj = new ObjSalida ();
					obj.IdInterno = entrada.IdInterno;
					obj.PortafolioTienePropositoAPV = "S";
					obj.TipoFactorRentabilidadNeta = "UTM";
					obj.FechaFactorRentabilidadNeta = resultadoParidad.FechaParametroFinanciero;
					obj.ValorFactorRentabilidadNeta = resultadoParidad.FactorOrigenDestino;
					obj.MontoRentabilidadNeta = diferenciaUtm;
					obj.MontoRentabilidadNetaActualizada = montoActualizado;
					obj.Resultado = resultadoParidad.Resultado;
					obj.CodigoMensaje = resultadoParidad.CodigoMensaje;
					out.p_Salida.add(obj);
    			}
    			else
				{
        			ObjSalida obj = new ObjSalida ();
        			obj.IdInterno = entrada.IdInterno;
        			obj.PortafolioTienePropositoAPV = "N";
        			obj.TipoFactorRentabilidadNeta = "";
        			obj.FechaFactorRentabilidadNeta = new java.sql.Date(df.parse("0001-01-01").getTime());
        			obj.ValorFactorRentabilidadNeta = BigDecimal.ZERO;
        			obj.MontoRentabilidadNeta = BigDecimal.ZERO;
        			obj.MontoRentabilidadNetaActualizada = BigDecimal.ZERO;
        			obj.Resultado = "OK";
        			obj.CodigoMensaje = "PORTAFOLIO_SIN_PROPOSITO_APV";
        			out.p_Salida.add(obj);
				};
        	};
		
        };
			
		return out;
	};
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();
		CalcularRentabilidadNetaEntrada jin=gson.fromJson(in.toString() ,CalcularRentabilidadNetaEntrada.class);
		CalcularRentabilidadNetaSalida out=logica(conn,jin);
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
				String json = "{\"p_IdEmpresa\":1,\"p_Entrada\":[{\"IdInterno\":1523918,\"IdCliente\":123570,\"IdPortafolio\":1450868,\"FechaRetiro\":\"2018-07-31\",\"MontoRetiro\":900000}]}";
				CalcularRentabilidadNetaEntrada Objin = gson.fromJson(json, CalcularRentabilidadNetaEntrada.class);
							
				CalcularRentabilidadNeta  kk = new CalcularRentabilidadNeta();
				CalcularRentabilidadNetaSalida Objout = kk.logica(con, Objin);
				System.out.println(Objout.p_Salida);
				//con.commit();
				
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
		} catch (Exception e) {
			e.printStackTrace();			
		}
	};
};