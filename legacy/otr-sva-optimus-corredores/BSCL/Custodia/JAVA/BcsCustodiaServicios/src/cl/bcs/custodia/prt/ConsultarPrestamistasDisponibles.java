package cl.bcs.custodia.prt;

/** 
 * Proposito: Consultar los clientes que pueden prestar títulos, entregando también "cuántos de esos títulos puede prestar cada cliente"
 * Autor: ACESPEDES (migración desde SQL del sp "BSCL.Custodia.BD.Servicios::PRT.ConsultarPrestamistasDisponibles")
 * Fecha Creacion: 28-11-2018
 **/ 


import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.ConsultarFlujoCustodia;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.bck.ActualizaRequisitoClienteBlockChain;
import cl.bcs.socionegocio.pojo.ConstantesBCK;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarPrestamistasDisponibles implements JavaCall {
	
	private final String SQL_SELECT_BUSCAR_CLIENTES				= loadResourceToString("cl/bcs/custodia/sql/ConsultarPrestamistasDisponibles_SelectClientesConContrato.sql");
	private final String SQL_SELECT_BUSCAR_CLIENTES_INSTRUMENTO	= loadResourceToString("cl/bcs/custodia/sql/ConsultarPrestamistasDisponibles_SelectClientesPorInstrumento.sql");
	private final String SQL_SELECT_ESCONTRATOBLOCKCHAIN		=	loadResourceToString("cl/bcs/socionegocio/bck/sql/ContratoEsBlockChainFirmado_SelectTipoDocumento.sql");
	
    //-------------------------------------------------------------------------------------------
	// CLASES QUE REPRESENTAN OBJETOS DE ENTRADA
	
	public static class ConsultarPrestamistasDisponibles_IN{
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

		public ConsultarPrestamistasDisponibles_IN(){
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
		public BigDecimal Cantidad;
		public String CodigoPortafolio;
		
    	public String toString() {
    		return Utils.printJson(this, false);
    	}
		
	}
	
    public static class ConsultarPrestamistasDisponibles_OUT{
    	public Long 				P_OUT_ERRORES_CONTADOR;
    	public List<MensajeSalida>	P_OUT_MENSAJES;
    	
    	public List<RegistroSalida> P_OUT_DISPONIBLES;
    	public String 				P_OUT_CLIENTE_VALIDO;
    	public BigDecimal			P_OUT_CLIENTE_VALIDO_CANTIDAD;
    	
    	   
    	public ConsultarPrestamistasDisponibles_OUT(){
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
    
    public static class TipoDocumento {
		Long IdClase = 0L;
	}
    
	//-------------------------------------------------------------------------------------------
	// LOGICA DEL SERVICIO
	
	public ConsultarPrestamistasDisponibles_OUT logica(Connection conn, ConsultarPrestamistasDisponibles_IN paramsIn) throws Exception {
		ConsultarPrestamistasDisponibles_OUT resultado 	= new ConsultarPrestamistasDisponibles_OUT();
    	SQL sql 										= new SQL();
    	HashMap<String,Object> paramDatosCliente 		= new HashMap<String,Object>(); 
    	    	   	
    	paramDatosCliente.put("p_IdTipoDocumento", ConstantesBCK.CONTRATOPRESTAMOANEXO43);
		List<TipoDocumento> arrTipoDocumento = sql.query(conn, SQL_SELECT_ESCONTRATOBLOCKCHAIN, paramDatosCliente, TipoDocumento.class);
		paramDatosCliente.clear();
		
		if ( arrTipoDocumento.size() > 0 ) {
			
			paramDatosCliente.put("p_IdEmpresa"		, paramsIn.p_IdEmpresa);
	    	paramDatosCliente.put("p_IdInstrumento"	, paramsIn.p_IdInstrumento);
			List<Cliente> clientesSegunInstrumento = sql.query(conn, SQL_SELECT_BUSCAR_CLIENTES_INSTRUMENTO, paramDatosCliente, Cliente.class);
			paramDatosCliente.clear();
			
			if ( clientesSegunInstrumento.size() > 0 ) {
				ActualizaRequisitoClienteBlockChain actualizaRequisitoClienteBlockChain 													= new ActualizaRequisitoClienteBlockChain();
		    	ActualizaRequisitoClienteBlockChain.ActualizaRequisitoClienteBlockChain_Entrada actualizaRequisitoClienteBlockChainEntrada 	= new ActualizaRequisitoClienteBlockChain.ActualizaRequisitoClienteBlockChain_Entrada();
		    	actualizaRequisitoClienteBlockChainEntrada.p_IdEmpresa 			= paramsIn.p_IdEmpresa;
		    	actualizaRequisitoClienteBlockChainEntrada.p_IdUsuario 			= paramsIn.p_IdUsuario;
		    	actualizaRequisitoClienteBlockChainEntrada.p_IdTipoDocumento 	= ConstantesBCK.CONTRATOPRESTAMOANEXO43;
		    	actualizaRequisitoClienteBlockChainEntrada.p_Clientes 			= clientesSegunInstrumento.parallelStream().map( cliente -> new ActualizaRequisitoClienteBlockChain.Cliente(cliente.IdCliente) ).collect(Collectors.toList());
		    	ActualizaRequisitoClienteBlockChain.ActualizaRequisitoClienteBlockChain_Salida actualizaRequisitoClienteBlockChainSalida = actualizaRequisitoClienteBlockChain.logica(conn, actualizaRequisitoClienteBlockChainEntrada);
		    	resultado.P_OUT_MENSAJES.addAll(actualizaRequisitoClienteBlockChainSalida.P_OUT_MENSAJES);
			}
		}
		
    	// ---------------------------------------------------------------------------------------------------------------------------------------
    	// -- Primero busco los clientes con contrato de emprestitos y que tengan cartera vigente (no importa cuanta) del instrumento a prestar --
    	// ---------------------------------------------------------------------------------------------------------------------------------------
    	
    	paramDatosCliente.put("p_IdEmpresa", paramsIn.p_IdEmpresa);
    	paramDatosCliente.put("p_IdInstrumento", paramsIn.p_IdInstrumento);
    	paramDatosCliente.put("p_IdClienteExcluido", paramsIn.p_IdClienteExcluido);
    	paramDatosCliente.put("p_IdClienteValidando", paramsIn.p_IdClienteValidando);
    	
		List<Cliente> ListaIdsClientes = sql.query(conn, SQL_SELECT_BUSCAR_CLIENTES, paramDatosCliente, Cliente.class);
		if (ListaIdsClientes.size() == 0){
			return resultado;  // sale si no encuentra clientes
		}
		

		List<ConsultarFlujoCustodia.FlujoFiltrosBusqueda> dato_p_FiltrosBusqueda = new ArrayList<ConsultarFlujoCustodia.FlujoFiltrosBusqueda>();
		ConsultarFlujoCustodia.FlujoFiltrosBusqueda datosFlujoFiltrosBusqueda = new ConsultarFlujoCustodia.FlujoFiltrosBusqueda();
		datosFlujoFiltrosBusqueda.IdClasificacionTipoInstrumento	= paramsIn.p_IdClasificacionTipoInstrumento;
		datosFlujoFiltrosBusqueda.IdTipoInstrumento					= paramsIn.p_IdTipoInstrumento;
		datosFlujoFiltrosBusqueda.IdInstrumento						= paramsIn.p_IdInstrumento;
		datosFlujoFiltrosBusqueda.IdEmisor							= null;
		datosFlujoFiltrosBusqueda.FechaVencimiento					= null;
		datosFlujoFiltrosBusqueda.IdTipoReajuste					= null;
		datosFlujoFiltrosBusqueda.EsFungible						= null;
		datosFlujoFiltrosBusqueda.IdBoveda							= null;
		dato_p_FiltrosBusqueda.add(datosFlujoFiltrosBusqueda);
		
		List<ConsultarFlujoCustodia.FlujoEsAgrupadoPor> dato_p_EsAgrupadoPor = new ArrayList<ConsultarFlujoCustodia.FlujoEsAgrupadoPor>();
		ConsultarFlujoCustodia.FlujoEsAgrupadoPor datosFlujoEsAgrupadoPor = new ConsultarFlujoCustodia.FlujoEsAgrupadoPor();
		datosFlujoEsAgrupadoPor.ClasificacionTipoInstrumento	= "S";
		datosFlujoEsAgrupadoPor.TipoInstrumento					= "S";
		datosFlujoEsAgrupadoPor.Instrumento						= "S";
		datosFlujoEsAgrupadoPor.Emisor							= "N";
		datosFlujoEsAgrupadoPor.Nemotecnico						= "N";
		datosFlujoEsAgrupadoPor.NemoSVS							= "N";
		datosFlujoEsAgrupadoPor.FechaEmisionInstrumento			= "N";
		datosFlujoEsAgrupadoPor.FechaVencimientoInstrumento		= "N";
		datosFlujoEsAgrupadoPor.FechaLiquidacionInstrumento		= "N";
		datosFlujoEsAgrupadoPor.TipoReajuste					= "N";
		datosFlujoEsAgrupadoPor.EsFungible						= "N";
		datosFlujoEsAgrupadoPor.CodigoIFRS						= "N";
		datosFlujoEsAgrupadoPor.Boveda							= "N";
		datosFlujoEsAgrupadoPor.Bolsa							= "N";
		datosFlujoEsAgrupadoPor.TipoOperacion					= "N";
		datosFlujoEsAgrupadoPor.Mesa							= "N";
		datosFlujoEsAgrupadoPor.FolioRegistro					= "N";
		datosFlujoEsAgrupadoPor.ItemOrigen						= "N";
		dato_p_EsAgrupadoPor.add(datosFlujoEsAgrupadoPor);
		
		
		for (Cliente regCliente : ListaIdsClientes){
			List<ConsultarFlujoCustodia.FlujoUso> dato_p_uso = new ArrayList<ConsultarFlujoCustodia.FlujoUso>();
			ConsultarFlujoCustodia.FlujoUso datosFlujoUso = new ConsultarFlujoCustodia.FlujoUso();
			datosFlujoUso.UsoTipoOperacion				= "PREDIS";
			datosFlujoUso.IdCliente						= regCliente.IdCliente;
			datosFlujoUso.IdPortafolio					= regCliente.IdPortafolio;
			datosFlujoUso.BusquedaEspecifica			= "PRESTAMISTAS_DISPONIBLES";
			datosFlujoUso.IdTipoInstrumentoEspecifico	= null;
			dato_p_uso.add(datosFlujoUso);
			
			ConsultarFlujoCustodia llamadoFlujoCustodia = new ConsultarFlujoCustodia();
			ConsultarFlujoCustodia.FlujoCustodiaEntrada paramInSP = new ConsultarFlujoCustodia.FlujoCustodiaEntrada(); 
			paramInSP.p_IdEmpresa			= paramsIn.p_IdEmpresa;
			paramInSP.p_IdUsuario			= paramsIn.p_IdUsuario;
			paramInSP.p_IdModulo			= paramsIn.p_IdModulo;
			paramInSP.p_FechaSaldo			= paramsIn.p_FechaConsulta; //tFechaHoraSistema.substring(0, 10) ;
			paramInSP.p_IdPais				= paramsIn.p_IdPais;
			paramInSP.p_DiasProyeccion		= 0;
			paramInSP.p_DiaHabil			= "S";
			paramInSP.p_ValorizarFlujo		= "N";
			paramInSP.p_Uso					= dato_p_uso;
			paramInSP.p_FiltrosBusqueda		= dato_p_FiltrosBusqueda;
			paramInSP.p_EsAgrupadoPor		= dato_p_EsAgrupadoPor;
			
			
			ConsultarFlujoCustodia.FlujoCustodiaSalida salidaFlujo = llamadoFlujoCustodia.logica(conn, paramInSP);
			if (salidaFlujo.p_out_FlujoTotalizado.size()>0 ){
				if (salidaFlujo.p_out_FlujoTotalizado.get(0).Cantidad.compareTo(BigDecimal.ZERO) > 0){
					
					if (paramsIn.p_IdClienteValidando > 0 && paramsIn.p_IdClienteValidando.equals(regCliente.IdCliente) && paramsIn.p_IdPortafolioValidando.equals(regCliente.IdPortafolio)){
						resultado.P_OUT_CLIENTE_VALIDO = "S";
						resultado.P_OUT_CLIENTE_VALIDO_CANTIDAD = salidaFlujo.p_out_FlujoTotalizado.get(0).Cantidad;
					}
					
					RegistroSalida newReg = new RegistroSalida();
					
					newReg.Cantidad = salidaFlujo.p_out_FlujoTotalizado.get(0).Cantidad;
					newReg.DocumentoIdentidad = regCliente.DocumentoIdentidad;
					newReg.IdCliente = regCliente.IdCliente;
					newReg.IdPortafolio = regCliente.IdPortafolio;
					newReg.NombreCliente = regCliente.Nombre;
					newReg.Proposito = regCliente.Proposito;
					newReg.CodigoPortafolio=regCliente.CodigoPortafolio;
					resultado.P_OUT_DISPONIBLES.add(newReg);
				}
			}
		}
    	
		return resultado;
	}


	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();
		//Entrada in = gson.fromJson(jin.toString(), Entrada.class);
		ConsultarPrestamistasDisponibles_IN in = gson.fromJson(jin.toString(), ConsultarPrestamistasDisponibles_IN.class);
		
		ConsultarPrestamistasDisponibles_OUT out = logica(conn,in);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();	
		return dataout;
	}
	
	//-------------------------------------------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	
	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBA LOCAL ==");

		try (Connection con = PlataformaUtils.getDESAConnection()){
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":17,\"p_IdModulo\":\"CUS\",\"p_IdPais\":44,\"p_IdClasificacionTipoInstrumento\":\"RV\",\"p_IdTipoInstrumento\":\"ACN\",\"p_IdInstrumento\":492,\"p_CantidadMinima\":1,\"p_FechaConsulta\":\"2018-08-06\",\"p_IdClienteExcluido\":116323}";
			
				
			ConsultarPrestamistasDisponibles_IN Objin = gson.fromJson(json, ConsultarPrestamistasDisponibles_IN.class);
			ConsultarPrestamistasDisponibles demo = new ConsultarPrestamistasDisponibles();		
			
			System.out.println("ENTRADA:");
			System.out.println(Objin.toString());
			
			try {
				ConsultarPrestamistasDisponibles_OUT ObjOut= demo.logica(con, Objin);
				
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
