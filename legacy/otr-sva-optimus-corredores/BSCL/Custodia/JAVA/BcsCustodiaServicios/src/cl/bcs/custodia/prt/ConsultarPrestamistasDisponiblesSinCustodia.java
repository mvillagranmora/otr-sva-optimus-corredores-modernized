package cl.bcs.custodia.prt;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.math.BigDecimal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarPrestamistasDisponiblesSinCustodia implements JavaCall {
	
	private final String SQL_SELECT_CLIENTES_EXTERNOS = loadResourceToString("cl/bcs/custodia/sql/ConsultarPrestamistasDisponiblesSinCustodia_SelectClientesExternos.sql");
	
    //-------------------------------------------------------------------------------------------
	// CLASES QUE REPRESENTAN OBJETOS DE ENTRADA
	
	public static class ConsultarPrestamistasDisponiblesSinCustodia_IN{
		public Long 		p_IdEmpresa;
		public Long 		p_IdUsuario;
		public String 		p_IdModulo;
		public Long 		p_IdPais;
		
		public String 		p_IdClasificacionTipoInstrumento;
		public String 		p_IdTipoInstrumento;
		public Long 		p_IdInstrumento;
		
		public String 		p_IdEmisor;
		public String 		p_FechaVencimiento;
		public String 		p_IdTipoReajuste;
		public String 		p_EsFungible;
		public String 		p_IdBoveda;
		public Long 		p_IdClienteExcluido;
		public BigDecimal	p_CantidadMinima;
		public String 		p_FechaConsulta;
		
		public Long 		p_IdClienteValidando;
		public Long 		p_IdPortafolioValidando;

		public ConsultarPrestamistasDisponiblesSinCustodia_IN(){
			this.p_IdClasificacionTipoInstrumento = "";
			this.p_IdTipoInstrumento = "";
			this.p_IdInstrumento = 0L;
			this.p_CantidadMinima = BigDecimal.ZERO;
			this.p_IdClienteExcluido = 0L;
			this.p_IdClienteValidando = 0L;
			this.p_IdPortafolioValidando = 0L;
		}
	    
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
    //-------------------------------------------------------------------------------------------
	// CLASES QUE REPRESENTAN OBJETOS DE SALIDA	
	
	public static class RegistroSalida{
		public Long IdCliente; 
		public Long IdPortafolio; 
		public String NombreCliente; 
		public String Proposito; 
		public String DocumentoIdentidad;
		public String CodigoPortafolio;
		
    	public String toString() {
    		return Utils.printJson(this, false);
    	}
		
	}
	
    public static class ConsultarPrestamistasDisponiblesSinCustodia_OUT{
    	public Long 				P_OUT_ERRORES_CONTADOR;
    	public List<MensajeSalida>	P_OUT_MENSAJES;
    	
    	public List<RegistroSalida> P_OUT_DISPONIBLES;
    	public String 				P_OUT_CLIENTE_VALIDO;
    	public BigDecimal			P_OUT_CLIENTE_VALIDO_CANTIDAD;
    
    	   
    	public ConsultarPrestamistasDisponiblesSinCustodia_OUT(){
    		this.P_OUT_ERRORES_CONTADOR = 0L;
    		this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
    		this.P_OUT_DISPONIBLES = new ArrayList<RegistroSalida>();
    		this.P_OUT_CLIENTE_VALIDO = "N";
    		this.P_OUT_CLIENTE_VALIDO_CANTIDAD = BigDecimal.ZERO;
    	
    	}
    	
    	public String toString() {
    		return Utils.printJson(this, false);
    	}
    }
    
    
    //---------------------------------------------------------------------------------------------
    // CLASES DE APOYO
    
    public static class Cliente{
    	public Long 	IdCliente;
    	public String 	Nombre;
    	public String 	DocumentoIdentidad;
    	public Long 	IdPortafolio;
    	public String 	Proposito;
    	public String CodigoPortafolio;
    	
    	public String toString() {
    		return Utils.printJson(this, false);
    	}
    }
    
	//-------------------------------------------------------------------------------------------
	// LOGICA DEL SERVICIO
	
	public ConsultarPrestamistasDisponiblesSinCustodia_OUT logica(Connection conn, ConsultarPrestamistasDisponiblesSinCustodia_IN paramsIn) throws Exception {
		ConsultarPrestamistasDisponiblesSinCustodia_OUT resultado = new ConsultarPrestamistasDisponiblesSinCustodia_OUT();
		
    	SQL sql=new SQL();
    	
    	
    	HashMap<String,Object> paramDatosCliente = new HashMap<String,Object>(); 			// parametros para consulta de cliente
    	paramDatosCliente.put("p_IdEmpresa", paramsIn.p_IdEmpresa);
    	paramDatosCliente.put("p_IdClienteExcluido", paramsIn.p_IdClienteExcluido);
    	paramDatosCliente.put("p_IdClienteValidando", paramsIn.p_IdClienteValidando);
    	
		List<Cliente> ListaIdsClientes = sql.query(conn, SQL_SELECT_CLIENTES_EXTERNOS, paramDatosCliente, Cliente.class);
		if (ListaIdsClientes.size() == 0){
			return resultado;  // sale si no encuentra clientes
		}
		

		for (Cliente regCliente : ListaIdsClientes){
			
					if (paramsIn.p_IdClienteValidando > 0 && paramsIn.p_IdClienteValidando.equals(regCliente.IdCliente) && paramsIn.p_IdPortafolioValidando.equals(regCliente.IdPortafolio)){
						resultado.P_OUT_CLIENTE_VALIDO = "S";	
					}
					
					RegistroSalida newReg = new RegistroSalida();
					
					
					newReg.DocumentoIdentidad = regCliente.DocumentoIdentidad;
					newReg.IdCliente = regCliente.IdCliente;
					newReg.IdPortafolio = regCliente.IdPortafolio;
					newReg.NombreCliente = regCliente.Nombre;
					newReg.Proposito = regCliente.Proposito;
					newReg.CodigoPortafolio=regCliente.CodigoPortafolio;
					resultado.P_OUT_DISPONIBLES.add(newReg);
				
		}
    	
		return resultado;
	}


	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();
		//Entrada in = gson.fromJson(jin.toString(), Entrada.class);
		ConsultarPrestamistasDisponiblesSinCustodia_IN in = gson.fromJson(jin.toString(), ConsultarPrestamistasDisponiblesSinCustodia_IN.class);
		
		ConsultarPrestamistasDisponiblesSinCustodia_OUT out = logica(conn,in);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();	
		return dataout;
	}
	
	//-------------------------------------------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	
	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBA LOCAL !==");

		try (Connection con = PlataformaUtils.getDESAConnection()){
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"CUS\",\"p_IdPais\":44,\"p_IdClasificacionTipoInstrumento\":\"RV\",\"p_IdTipoInstrumento\":\"ACN\",\"p_IdInstrumento\":308,\"p_CantidadMinima\":1,\"p_FechaConsulta\":\"2020-08-21\",\"p_IdClienteExcluido\":524893}";
			String json ="{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"CUS\",\"p_IdPais\":44,\"p_IdClasificacionTipoInstrumento\":\"RV\",\"p_IdTipoInstrumento\":\"ACN\",\"p_IdInstrumento\":1845,\"p_CantidadMinima\":1,\"p_FechaConsulta\":\"2020-08-21\",\"p_IdClienteExcluido\":524893}";
				
			ConsultarPrestamistasDisponiblesSinCustodia_IN Objin = gson.fromJson(json, ConsultarPrestamistasDisponiblesSinCustodia_IN.class);
			ConsultarPrestamistasDisponiblesSinCustodia demo = new ConsultarPrestamistasDisponiblesSinCustodia();		
			
			System.out.println("ENTRADA:");
			System.out.println(Objin.toString());
			
			try {
				ConsultarPrestamistasDisponiblesSinCustodia_OUT ObjOut= demo.logica(con, Objin);
				
				System.out.println("SALIDA:");
				System.out.println(ObjOut.toString());
			} catch(Exception er) {
				con.rollback();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== FIN PRUEBA LOCAL ==");
    }
}

