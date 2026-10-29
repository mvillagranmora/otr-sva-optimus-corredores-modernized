package cl.bcs.custodia;

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

import cl.bcs.custodia.ConsultarCustodiaPortafolio.Tabla_Salida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class ConsultarBalanceCustodia implements JavaCall{
	
	private final String SQL_SELECT_BALANCE = loadResourceToString("cl/bcs/custodia/sql/ConsultarBalanceCustodia_Select.sql");
	
	private final static Logger logger = LogManager.getLogger(ConsultarBalanceCustodia.class);

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ConsultarBalance_Entrada in = gson.fromJson(jin.toString(), ConsultarBalance_Entrada.class);
		ConsultarBalance_Salida out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = 0L;
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;	
	}
	
	public static class ConsultarBalance_Entrada{
		//Parametros Fijos
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		
		//Parametros de ventana
		public String 	p_FechaConsulta;
		public Long		p_IdPortafolio;
		public Long 	p_IdCliente;
		public String 	p_IncluyeCuentaInversion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	//Estructura de datos para la Grilla
	public static class Cartera_Salida{
		
		/*
		public String 		IdClasificacionTipoInstrumento;
		public String 		NombreClasificacionTipoInstrumento;
		public String 		IdTipoInstrumento;
		public String 		NombreTipoInstrumento;
		public BigDecimal 	MontoCompraMonedaInstrumento;
		*/
		
		public String 		IdClasificacionTipoInstrumentoCar;
		public String 		NombreClasificacionTipoInstrumentoCar;
		public String 		IdClasificacionTipoInstrumentoSub;
		public String 		NombreClasificacionTipoInstrumentoSub;
		public String 		IdTipoInstrumentoCar;
		public String 		NombreTipoInstrumentoCar;
		public String 		IdTipoInstrumentoSub;
		public String 		NombreTipoInstrumentoSub;
		public BigDecimal 	MontoATasaCompraActualCar;
		public BigDecimal 	MontoATasaCompraActualSub;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	//Estructura de datos para la Grilla
		public static class Balance_Salida{
			
			public String 		IdClasificacionTipoInstrumento;
			public String 		NombreClasificacionTipoInstrumento;
			public String 		IdTipoInstrumento;
			public String 		NombreTipoInstrumento;
			public BigDecimal 	Activo;
			public BigDecimal 	Pasivo;
			public BigDecimal 	Saldo;
			public BigDecimal 	Rentabilidad;
			
			public String toString() {
				return Utils.printJson(this, false);
			}
		}
	
	public static class Portafolio {
		public Long IdPortafolio;
	}
	
	//Salida para la grilla
	public static class ConsultarBalance_Salida{
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;
		
		public List<Balance_Salida> 	P_OUT_BALANCE;
		
		public ConsultarBalance_Salida() {
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_BALANCE = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, true);
		}	
	}
	
	public ConsultarBalance_Salida logica(Connection conn, ConsultarBalance_Entrada entrada) throws Exception{
		logger.info("Inicio de servicio ConsultarBalance");
		SQL _sql = new SQL();
		ConsultarBalance_Salida salida = new ConsultarBalance_Salida();
		
		int varIdPortafolio = 0;
		int varIdCliente = 0;
		
		if(entrada.p_IdPortafolio == 0) {
			varIdPortafolio = 1;
		}
		
		if(entrada.p_IdCliente == 0) {
			varIdCliente = 1;
		}
		
		HashMap<String,Object> params =  new HashMap<>();
		params.put("p_IdEmpresa", entrada.p_IdEmpresa);
		params.put("p_FechaConsulta", entrada.p_FechaConsulta);
		params.put("p_IdPortafolio", entrada.p_IdPortafolio);
		params.put("p_IdCliente", entrada.p_IdCliente);
		//params.put("p_IncluyeCuentaInversion", entrada.p_IncluyeCuentaInversion);

		List<Balance_Salida> balances = new ArrayList<>();
		logger.debug("INICIO - Select ObtenerCarteras");
		List<Cartera_Salida> carteras = _sql.query(conn, SQL_SELECT_BALANCE, params, Cartera_Salida.class);
		logger.debug("FIN - Select ObtenerCarteras"); 
		
		logger.debug("INICIO - Ciclo construye Balance");
		
		if(carteras != null && carteras.size() > 0) {
			
			ConsultarCustodiaPortafolio ConsultaCustodia = new ConsultarCustodiaPortafolio();
			
			ConsultarCustodiaPortafolio.Entrada_ConsultaCustodiaPortafolio entradaConsulta= new  ConsultarCustodiaPortafolio.Entrada_ConsultaCustodiaPortafolio();
			entradaConsulta.p_IdEmpresa=entrada.p_IdEmpresa;
			entradaConsulta.p_IdUsuario=entrada.p_IdUsuario;
			entradaConsulta.p_IdModulo=entrada.p_IdModulo;
			
			entradaConsulta.p_IdCliente=entrada.p_IdCliente;
			entradaConsulta.p_IdPortafolio=entrada.p_IdPortafolio;
			entradaConsulta.p_IdInstrumento=0L;
			entradaConsulta.p_IdTipoInstrumento="";
			entradaConsulta.p_IdClasificacionTipoInstrumento="";
			entradaConsulta.p_IdProductoAgrupador=null;
			entradaConsulta.p_AgrupaPorBoveda="S";
			entradaConsulta.p_AgrupaPorRelacion="S";
			entradaConsulta.p_TipoFechaConsulta="ADQ";
			entradaConsulta.p_FechaConsulta=entrada.p_FechaConsulta;
		
			ConsultarCustodiaPortafolio.Salida_ConsultaCustodiaPortafolio salidaConsulta = new ConsultarCustodiaPortafolio.Salida_ConsultaCustodiaPortafolio();		
			salidaConsulta = ConsultaCustodia.logica(conn, entradaConsulta);
			
			
			for(Cartera_Salida cartera : carteras) {
				Balance_Salida balance = new Balance_Salida();
				if(cartera.IdClasificacionTipoInstrumentoSub != null) {
					balance.IdClasificacionTipoInstrumento = cartera.IdClasificacionTipoInstrumentoSub;
					balance.NombreClasificacionTipoInstrumento = cartera.NombreClasificacionTipoInstrumentoSub;
					balance.IdTipoInstrumento = cartera.IdTipoInstrumentoSub;
					balance.NombreTipoInstrumento = cartera.NombreTipoInstrumentoSub;
					switch(cartera.IdTipoInstrumentoCar) {
						case "EMP": 
							balance.Activo = cartera.MontoATasaCompraActualSub;
							balance.Pasivo = new BigDecimal(0);
							break;
						default:
							balance.Activo = new BigDecimal(0);
							balance.Pasivo = cartera.MontoATasaCompraActualSub;
							break;
					}
				}
				else {
					balance.IdClasificacionTipoInstrumento = cartera.IdClasificacionTipoInstrumentoCar;
					balance.NombreClasificacionTipoInstrumento = cartera.NombreClasificacionTipoInstrumentoCar;
					balance.IdTipoInstrumento = cartera.IdTipoInstrumentoCar;
					balance.NombreTipoInstrumento = cartera.NombreTipoInstrumentoCar;
					switch(cartera.IdTipoInstrumentoCar) {
						case "PAC": 
							balance.Activo = new BigDecimal(0);
							balance.Pasivo = cartera.MontoATasaCompraActualCar;
							break;
						default:
							balance.Activo = cartera.MontoATasaCompraActualCar;
							balance.Pasivo = new BigDecimal(0);
							break;
					}
					
				}
				balances.add(balance);
			}
			
			for(int j =0 ; j<balances.size(); j++) {
				
				BigDecimal acum=  BigDecimal.ZERO;
				
				for(int i =0 ; i<salidaConsulta.P_OUT_TBL.size(); i++) {
					if(salidaConsulta.P_OUT_TBL.get(i).IdClasificacionTipoInstrumento.equals(balances.get(j).IdClasificacionTipoInstrumento) 
							&& salidaConsulta.P_OUT_TBL.get(i).IdTipoInstrumento.equals(balances.get(j).IdTipoInstrumento)) {
						acum = acum.add(salidaConsulta.P_OUT_TBL.get(i).MontoMercadoCustodia);
						
					}
				}
				
				
				
				if (balances.get(j).Activo.compareTo(BigDecimal.ZERO)>0 && acum.compareTo(BigDecimal.ZERO)>0) {
					
					balances.get(j).Activo = acum;
					balances.get(j).Rentabilidad= ((acum.subtract(balances.get(j).Activo)).divide(balances.get(j).Activo, MathContext.DECIMAL128)).multiply(new BigDecimal("100"));
				}else if (balances.get(j).Pasivo.compareTo(BigDecimal.ZERO)>0 && acum.compareTo(BigDecimal.ZERO)>0) {
					
					balances.get(j).Pasivo = acum;
					balances.get(j).Rentabilidad= ((acum.subtract(balances.get(j).Pasivo)).divide(balances.get(j).Pasivo, MathContext.DECIMAL128)).multiply(new BigDecimal("100"));
				}else {
					
					balances.get(j).Rentabilidad= BigDecimal.ZERO;
				}
			}
			
		}
		
		salida.P_OUT_BALANCE = balances;
		
		logger.debug("FIN - Ciclo construye Balance");

		logger.info("Fin de servicio ConsultarBalance");
		return salida;
	}
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection())
		{
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			
			//String json = "{\"p_IdCliente\":\"524891\",\"p_IdPortafolio\":\"0\",\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"CUS\",\"p_FechaConsulta\":\"2021-04-25\"}";
			
			String json = "{\"p_IdEmpresa\":1,\"p_IdModulo\":\"CUS\",\"p_IdCliente\":524891,\"p_IdPortafolio\":1451531,\"p_FechaConsulta\":\"2021-04-25\"}";
			String jsonMaty = "{'p_IdEmpresa':1,'p_IdUsuario':'1646','p_IdModulo':'CUS','p_IdCliente':9033,'p_IdPortafolio':9193,'p_FechaConsulta':'2022-05-31'}";
			ConsultarBalance_Entrada Objin = gson.fromJson(jsonMaty, ConsultarBalance_Entrada.class);
			ConsultarBalanceCustodia demo = new ConsultarBalanceCustodia();
			try 
			{
				ConsultarBalance_Salida ObjOut = demo.logica(con, Objin);
				ObjOut.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(ObjOut.P_OUT_MENSAJES);
				System.out.println(ObjOut);
			}catch(Exception er) {
				er.printStackTrace();
				logger.error(er.getMessage());
				con.rollback();
			}
		}	
    }
		
}
