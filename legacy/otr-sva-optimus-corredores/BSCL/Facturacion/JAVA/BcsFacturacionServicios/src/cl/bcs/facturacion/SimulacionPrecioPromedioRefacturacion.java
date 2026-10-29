package cl.bcs.facturacion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.facturacion.pojo.SalidaCargaMasivaRefacturacion;
import cl.bcs.facturacion.pojo.SalidaCobrosSimulacion;
import cl.bcs.facturacion.pojo.SalidaSimulacion;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class SimulacionPrecioPromedioRefacturacion implements JavaCall{
	
	private final String SQL_SELECT_CONSULTA_TRANSACCIONES= loadResourceToString("cl/bcs/facturacion/sql/CrearCargaMasivaRefacturacion_SelectTransacciones.sql");
	private final String SQL_SELECT_CONSULTA_CLIENTE_PORTAFOLIO= loadResourceToString("cl/bcs/facturacion/sql/CrearCargaMasivaRefacturacion_SelectClientePortafolio.sql");
	
	
	
	public static class Transaccion_Entrada {
		public Long IdMovimiento;
		public Long IdTransaccion;
		public Long IdPortafolio;
		public String Nemotecnico;
		public BigDecimal Cantidad;
		public BigDecimal Monto;
		public BigDecimal ValorRescate;
		public BigDecimal Porcentaje;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Portafolio_Entrada {
		public Long IdPortafolio;
		public String Nemotecnico;
		public BigDecimal Cantidad;
		public BigDecimal Porcentaje;
		public String DocumentoIdentidad;
		public String TipoOperacion;
		
		//cobros
		List<Cobros_Archivo> cobros;
		public Portafolio_Entrada(){
			this.cobros= new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Cobros_Archivo{
		public String IdCobro;
		public BigDecimal Monto;
		public String FormaCobro;
		public String Codigo;
		public String PermiteCambio;
		
		public Cobros_Archivo(){}
		
		public Cobros_Archivo(String IdCobro,BigDecimal Monto,String FormaCobro,String Codigo,String PermiteCambio){
			this.IdCobro=IdCobro;
			this.Monto=Monto;
			this.FormaCobro=FormaCobro;
			this.Codigo=Codigo;
			this.PermiteCambio=PermiteCambio;
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class CrearSimulacionAsignacion_Entrada {
		public Long 						p_IdEmpresa;
		public Long 						p_IdUsuario;
		public String						p_IdModulo;
		public String						p_UsaReglasNegocio;
		public String						p_IdTipoAsignacion;
		public String						p_IdTipo;
		public String						p_Origen;
		public List<Transaccion_Entrada> 	p_Transacciones;		
		public List<Portafolio_Entrada> 	p_Portafolios;
		
		public CrearSimulacionAsignacion_Entrada() {
			this.p_Transacciones = new ArrayList<>();			
			this.p_Portafolios = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCrearSimulacionAsignacion extends SalidaCargaMasivaRefacturacion{	
		public SalidaCrearSimulacionAsignacion() {
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_SIMASIGNACION = new ArrayList<>();
			this.P_OUT_COBROSASIGNACION=new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR=0L;
		}
	
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ClientePortafolio{
		public String DocumentoIdentidad;
		public Long IdCliente;
		public Long IdPortafolio;
		public String CodigoPortafolio;
		public String NombreCompleto;
		public String PagoCobroCuentaInversion;
		

		public String OperaCustodiaRV;
		public String OperaCustodiaRFIF;
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ConsultaTransaccion{
		 public java.sql.Timestamp FechaTransaccion;
		 public String FolioTransaccion;
		 public String IdBolsa;
		 public String IdBoveda;
		 public String IdMonedaInstrumento;
		 public String IdMonedaLiquidacionOrden;
		 public Long   IdMovimiento;
		 public String IdTipoInstrumento;
		 public Long   IdTransaccion;
		 public String Instrumento;
		 public String IdTipoOperacion;
		 public String IdTipoProducto;
		 public String  IdFormaCustodia;
		 
		 public String toString() {
				return Utils.printJson(this, false);
			}
	}	
	
	/******************************************************************************
	 * 							LOGICA
	 ******************************************************************************/
	
	
	public SalidaCrearSimulacionAsignacion logica(Connection con,CrearSimulacionAsignacion_Entrada entrada) throws Exception {
		
		SQL sql = new SQL();
		SalidaCrearSimulacionAsignacion Salida = new SalidaCrearSimulacionAsignacion();
	
		List<SalidaSimulacion> SalidaSimulaciones = new ArrayList<>();
		List<SalidaCobrosSimulacion>cobrosSalida = new ArrayList<>();
		List<SalidaSimulacion> Simulaciones = new ArrayList<>();
		
		BigDecimal CantidadTotalTrx = BigDecimal.ZERO;
		for(Transaccion_Entrada trx : entrada.p_Transacciones) {
			CantidadTotalTrx=CantidadTotalTrx.add(trx.Cantidad); 
		}
		
		//calculo porcentaje por transaccion 
		for(Transaccion_Entrada trx : entrada.p_Transacciones) {
			BigDecimal porcentaje = BigDecimal.ZERO;
			
			porcentaje = (new BigDecimal(100).multiply(trx.Cantidad)).divide(CantidadTotalTrx,20,RoundingMode.HALF_UP);
			trx.Porcentaje = porcentaje.divide(new BigDecimal(100),20,RoundingMode.HALF_UP);
			
		}
		
		//Asignaciones por porcentaje por transaccion
		
		List<Long> ListaIdPor = new ArrayList<Long>();
		List<Long> ListaIdTra = new ArrayList<Long>();
		List<String> ListaDocumentoIdentidad = new ArrayList<String>();		
		
		for(Transaccion_Entrada trx : entrada.p_Transacciones) {
			for(Portafolio_Entrada por : entrada.p_Portafolios){
				
				List<Long> secuencias = Utils.getSecuencia(con, "BSCL.Orden.BD.Secuencias::DetalleSimulacionAsignacion", new Long(1));
				BigDecimal  cantidadAsignada = BigDecimal.ZERO;
				cantidadAsignada = new BigDecimal((por.Cantidad.multiply(trx.Porcentaje)).intValue());
					
				SalidaSimulacion simulacion = new SalidaSimulacion(secuencias.get(0),trx.IdTransaccion,trx.IdMovimiento,cantidadAsignada,por.IdPortafolio,por.Nemotecnico,por.DocumentoIdentidad);
				simulacion.IdPortafolio = por.IdPortafolio;
				
				Simulaciones.add(simulacion);
				
				ListaIdPor.add(por.IdPortafolio);
				ListaDocumentoIdentidad.add(por.DocumentoIdentidad);
				
			}
			ListaIdTra.add(trx.IdTransaccion);
		}
	
		
		try {
			//Busqueda de datos para rellenar la simulaciones 
			//Datos Cliente Portafolio
			HashMap<String,Object> paramsPortafoliosCliente = new HashMap<>();
			paramsPortafoliosCliente.put("DocumentoIdentidad",ListaDocumentoIdentidad);
			paramsPortafoliosCliente.put("IdPortafolio",ListaIdPor);
			List<ClientePortafolio> ListaClientePortafolio = sql.query(con, SQL_SELECT_CONSULTA_CLIENTE_PORTAFOLIO, paramsPortafoliosCliente, ClientePortafolio.class);
			
		
			//Dato Transacciones
			HashMap<String,Object> paramsTransacciones = new HashMap<>();
			paramsTransacciones.put("IdTransacciones",ListaIdTra);				
			List<ConsultaTransaccion>  ListaTransacciones = sql.query(con, SQL_SELECT_CONSULTA_TRANSACCIONES, paramsTransacciones, ConsultaTransaccion.class);
			
			
			List<SalidaSimulacion> TmpSalida=new  ArrayList<>();
			
			//asignar valores de portafolio  y cliente y transacciones  
			for (SalidaSimulacion sim :Simulaciones){
				
				SalidaSimulacion TmpSim = new SalidaSimulacion();
				TmpSim=sim;				
				

				
				for(ConsultaTransaccion Tra : ListaTransacciones){
					
					if(sim.IdTransaccion.equals(Tra.IdTransaccion) ){
						TmpSim.IdTipoCanalIngreso= "TRA";
						//sim.CantidadAsignacion=Tra.CantidadAsignacion;
						TmpSim.FechaTransaccion= Tra.FechaTransaccion;
						TmpSim.FolioTransaccion=Tra.FolioTransaccion;
						TmpSim.IdBolsa= Tra.IdBolsa;
						TmpSim.IdBoveda= "DCV";
						TmpSim.IdMonedaInstrumento= Tra.IdMonedaInstrumento;
						TmpSim.IdMonedaLiquidacionOrden= Tra.IdMonedaLiquidacionOrden;
						TmpSim.IdTipoInstrumento = Tra.IdTipoInstrumento;
						//sim.IdOrden= Tra.IdOrden;
						//sim.IdLibroOrdenes= Tra.IdLibroOrdenes;
						TmpSim.Glosa="";
						TmpSim.IdTipoOperacion=Tra.IdTipoOperacion;
						TmpSim.IdTipoProducto=Tra.IdTipoProducto;
						TmpSim.IdFormaCustodia=Tra.IdFormaCustodia;
						break;
					}
				}

				for(ClientePortafolio CliPor : ListaClientePortafolio){
					if(sim.DocumentoIdentidad.equals(CliPor.DocumentoIdentidad)){
						TmpSim.CodigoPortafolio=CliPor.CodigoPortafolio;
						TmpSim.DocumentoIdentidad=CliPor.DocumentoIdentidad;
						TmpSim.NombreCompleto= CliPor.NombreCompleto;
						TmpSim.IdPortafolio=CliPor.IdPortafolio;
						TmpSim.IdCliente=CliPor.IdCliente;
						TmpSim.PagoCobroCuentaInversion=CliPor.PagoCobroCuentaInversion;
						//Si transaccion no esta asignada no tiene forma custodia. En este caso se determina segun indicadores
						TmpSim.IdFormaCustodia= (TmpSim.IdFormaCustodia == null || TmpSim.IdFormaCustodia.equals("")) ? (CliPor.OperaCustodiaRV.equalsIgnoreCase("S") || CliPor.OperaCustodiaRFIF.equalsIgnoreCase("S") ? "CUS" : "NOM") : TmpSim.IdFormaCustodia;
						
						
						break;
					}
				}
				
				TmpSalida.add(TmpSim);
			}
			
			//reajuste de cantidades 			
			HashMap<String,Object> HashPortafolios= Utils.List2Hash(entrada.p_Portafolios, "IdPortafolio");
			HashMap<String,Object> HashTransacciones= Utils.List2Hash(entrada.p_Transacciones, "IdTransaccion");
			
			//sumas simulaciones por portafolio y transacciones
			BigDecimal cantidadProrrateo= ContadorProrrateo(Simulaciones);
			
				while(cantidadProrrateo.compareTo(CantidadTotalTrx)==-1) {
					HashMap<Long,Portafolio_Entrada> HashSimulacionesPor = HashPortafolios(entrada.p_Portafolios,Simulaciones);
					HashMap<Long,Transaccion_Entrada> HashSimulacionesTra = HashTransacciones(entrada.p_Transacciones,Simulaciones);
					
					for(SalidaSimulacion sim :Simulaciones) {
						
						if(HashPortafolios.get(sim.IdPortafolio)!=null && HashSimulacionesPor.get(sim.IdPortafolio)!=null){
							
							Portafolio_Entrada porEntrada= (Portafolio_Entrada)HashPortafolios.get(sim.IdPortafolio);
							Portafolio_Entrada porAsignado= HashSimulacionesPor.get(sim.IdPortafolio);
							
							Transaccion_Entrada traEntrada = (Transaccion_Entrada)HashTransacciones.get(sim.IdTransaccion);
							Transaccion_Entrada traAsignado =  HashSimulacionesTra.get(sim.IdTransaccion);
							
							if(porAsignado.Cantidad.compareTo(porEntrada.Cantidad)==-1  && traAsignado.Cantidad.compareTo(traEntrada.Cantidad)==-1 ) {
								sim.CantidadAsignacion=sim.CantidadAsignacion.add(BigDecimal.ONE);
								break;
							}
						}
					}
					cantidadProrrateo= ContadorProrrateo(Simulaciones);
					
				} 
				
				SalidaSimulaciones.clear();
				SalidaSimulaciones.addAll(Simulaciones);
				Salida.P_OUT_SIMASIGNACION.addAll(Simulaciones);
				
			
				
				/***********************************************************************
				 *    					COBROS 
				 ***********************************************************************/
				Long Correlativo =0L;
				
				HashMap<String,Portafolio_Entrada> HashPortafolioCobros = new HashMap<String,Portafolio_Entrada>();
				
				for (Portafolio_Entrada por : entrada.p_Portafolios){
					String key = por.IdPortafolio+"_"+por.TipoOperacion+"_"+por.Nemotecnico;
					HashPortafolioCobros.put(key, por);
				}
				
				
				
				/* Carga de cobros por cliente */				
				int cont =0;
				BigDecimal divisorMonto = new BigDecimal(SalidaSimulaciones.size() / entrada.p_Portafolios.size());
				for(SalidaSimulacion sim : SalidaSimulaciones){
					SalidaCobrosSimulacion nCobro = new SalidaCobrosSimulacion();
					String key = sim.IdPortafolio+"_"+sim.IdTipoOperacion+"_"+sim.Instrumento;
					
					List<Cobros_Archivo> ListaCobro = HashPortafolioCobros.get(key).cobros;
					if(SalidaSimulaciones.size()==cont) {
						break;
					}

					for(Cobros_Archivo cob : ListaCobro) {
						nCobro = new SalidaCobrosSimulacion();
						nCobro.IdInterno= sim.IdInterno; 
						nCobro.IdBolsa= sim.IdBolsa;
					    nCobro.IdPortafolio= sim.IdPortafolio;
					    nCobro.IdTipoOperacion= sim.IdTipoOperacion;  
					    nCobro.IdTipoProducto= sim.IdTipoProducto;
					    nCobro.IdTipoCanalIngreso=sim.IdTipoCanalIngreso;
					    nCobro.ValorTopeMinimoOrden= BigDecimal.ZERO;
					    nCobro.CodigoCobroClasificacion= cob.Codigo;
					    nCobro.IdMoneda = sim.IdMonedaInstrumento;
					    nCobro.PermiteCambiarValor= cob.PermiteCambio;
					    nCobro.FormaCobro= cob.FormaCobro;
				    	nCobro.IdInternoCobro= Correlativo;
						nCobro.IdCobro= cob.IdCobro;
						nCobro.ValorCobro= cob.Monto;
					    
					    cobrosSalida.add(nCobro);
					    Correlativo++;						
					}
					cont++;
					
				}
				System.out.println(cobrosSalida);
				Salida.P_OUT_COBROSASIGNACION.addAll(cobrosSalida);				
			
		}catch (Exception e){
			System.out.println(e);
			Salida.P_OUT_ERRORESCONTADOR=1L;
			Salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ERROR_ASIGNACION","Error de asignacion"));
			
		}
		
		return Salida;
	}
	
	/********************
	 * 	TEST PORTAFOLIO CANTIDAD
	 *********************/
	
	
	public HashMap<Long,Portafolio_Entrada> HashPortafolios(List<Portafolio_Entrada> ListaPortafolios ,List<SalidaSimulacion>  Simulaciones){
		HashMap<Long,Portafolio_Entrada> HashSimulacionesPor = new HashMap<>();
		BigDecimal sumPor=BigDecimal.ZERO;
		
		//suma simulaciones por portafolio
		
		for(Portafolio_Entrada por :ListaPortafolios) {
			Portafolio_Entrada  objPor = new Portafolio_Entrada();				
			
			for (SalidaSimulacion sim :Simulaciones){
				if(por.IdPortafolio.compareTo(sim.IdPortafolio)==0) {
					sumPor=sumPor.add(sim.CantidadAsignacion);
				}
			}
			objPor.IdPortafolio=por.IdPortafolio;
			objPor.Cantidad=sumPor;	
			if(sumPor.compareTo(BigDecimal.ZERO)!=0) {
				HashSimulacionesPor.put(objPor.IdPortafolio, objPor);
			}
			sumPor=BigDecimal.ZERO;
		}
		
		return HashSimulacionesPor;
	}
	
	public HashMap<Long,Transaccion_Entrada> HashTransacciones(List<Transaccion_Entrada> ListaTransacciones ,List<SalidaSimulacion>  Simulaciones){
		HashMap<Long,Transaccion_Entrada> HashSimulacionesTra = new HashMap<>();
		BigDecimal sumTra=BigDecimal.ZERO;
		
		//suma simulaciones por portafolio
		
		for(Transaccion_Entrada trx : ListaTransacciones) {
			Transaccion_Entrada objTra = new Transaccion_Entrada();
			
			
			for (SalidaSimulacion sim :Simulaciones){
				if(trx.IdTransaccion.compareTo(sim.IdTransaccion)==0) {
					sumTra=sumTra.add(sim.CantidadAsignacion);
				}
			}
			objTra.IdTransaccion=trx.IdTransaccion;
			objTra.Cantidad=sumTra;	
			if(sumTra.compareTo(BigDecimal.ZERO)!=0) {
				HashSimulacionesTra.put(objTra.IdTransaccion, objTra);
			}
			sumTra=BigDecimal.ZERO;
		}
		
		return HashSimulacionesTra;	
	}
	
	public BigDecimal ContadorProrrateo(List<SalidaSimulacion> ListaSim) {
		BigDecimal sum =BigDecimal.ZERO;
		for(SalidaSimulacion sim : ListaSim) {
			sum = sum.add(sim.CantidadAsignacion);
		}
		
		return sum;
	}
	
	/******************************************************************************
	 *                          CALL
	 ******************************************************************************/

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		CrearSimulacionAsignacion_Entrada in = gson.fromJson(jin.toString(), CrearSimulacionAsignacion_Entrada.class);
		SalidaCrearSimulacionAsignacion out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	/******************************************************************************
	 * 							MAIN
	 ******************************************************************************/
	public static void main(String[] args){
		
		try (Connection con = PlataformaUtils.getDESAConnection())
		{
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String jsonTest="{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"ORD\",\"p_UsaReglasNegocio\":\"S\",\"p_IdTipoAsignacion\":\"ASIGNACION_PRECIO_PROMEDIO\",\"p_IdTipo\":\"NORMAL\",\"p_Origen\":\"ASIG\",\"p_Transacciones\":[{\"IdMovimiento\":70289285,\"IdTransaccion\":70289285,\"Nemotecnico\":\"BSANTANDER\",\"Cantidad\":1300,\"Monto\":0,\"ValorRescate\":0,\"Porcentaje\":0.26530612244897959184},{\"IdMovimiento\":70289288,\"IdTransaccion\":70289288,\"Nemotecnico\":\"BSANTANDER\",\"Cantidad\":2600,\"Monto\":0,\"ValorRescate\":0,\"Porcentaje\":0.53061224489795918367},{\"IdMovimiento\":70289290,\"IdTransaccion\":70289290,\"Nemotecnico\":\"BSANTANDER\",\"Cantidad\":1000,\"Monto\":0,\"ValorRescate\":0,\"Porcentaje\":0.20408163265306122449}],\"p_Portafolios\":[{\"IdPortafolio\":512342,\"Nemotecnico\":\"BSANTANDER\",\"Cantidad\":850,\"Porcentaje\":0,\"DocumentoIdentidad\":\"16338637-2\",\"TipoOperacion\":\"COMACN\",\"cobros\":[{\"IdCobro\":\"COFI\",\"Monto\":150,\"FormaCobro\":\"F\",\"Codigo\":\"COMISION\",\"PermiteCambio\":\"S\"},{\"IdCobro\":\"DEFI\",\"Monto\":200,\"FormaCobro\":\"F\",\"Codigo\":\"DERECHO\",\"PermiteCambio\":\"S\"},{\"IdCobro\":\"GAFI\",\"Monto\":300,\"FormaCobro\":\"F\",\"Codigo\":\"GASTO\",\"PermiteCambio\":\"S\"},{\"IdCobro\":\"IVA\",\"Monto\":19,\"FormaCobro\":\"F\",\"Codigo\":\"IMPUESTO\",\"PermiteCambio\":\"S\"}]},{\"IdPortafolio\":20021,\"Nemotecnico\":\"BSANTANDER\",\"Cantidad\":1300,\"Porcentaje\":0,\"DocumentoIdentidad\":\"13260071-6\",\"TipoOperacion\":\"COMACN\",\"cobros\":[{\"IdCobro\":\"COFI\",\"Monto\":400,\"FormaCobro\":\"F\",\"Codigo\":\"COMISION\",\"PermiteCambio\":\"S\"},{\"IdCobro\":\"DEFI\",\"Monto\":50,\"FormaCobro\":\"F\",\"Codigo\":\"DERECHO\",\"PermiteCambio\":\"S\"},{\"IdCobro\":\"GAFI\",\"Monto\":100,\"FormaCobro\":\"F\",\"Codigo\":\"GASTO\",\"PermiteCambio\":\"S\"},{\"IdCobro\":\"IVA\",\"Monto\":19,\"FormaCobro\":\"F\",\"Codigo\":\"IMPUESTO\",\"PermiteCambio\":\"S\"}]},{\"IdPortafolio\":343710,\"Nemotecnico\":\"BSANTANDER\",\"Cantidad\":500,\"Porcentaje\":0,\"DocumentoIdentidad\":\"15731790-3\",\"TipoOperacion\":\"COMACN\",\"cobros\":[{\"IdCobro\":\"COFI\",\"Monto\":250,\"FormaCobro\":\"F\",\"Codigo\":\"COMISION\",\"PermiteCambio\":\"S\"},{\"IdCobro\":\"DEFI\",\"Monto\":280,\"FormaCobro\":\"F\",\"Codigo\":\"DERECHO\",\"PermiteCambio\":\"S\"},{\"IdCobro\":\"GAFI\",\"Monto\":100,\"FormaCobro\":\"F\",\"Codigo\":\"GASTO\",\"PermiteCambio\":\"S\"},{\"IdCobro\":\"IVA\",\"Monto\":19,\"FormaCobro\":\"F\",\"Codigo\":\"IMPUESTO\",\"PermiteCambio\":\"S\"}]},{\"IdPortafolio\":153970,\"Nemotecnico\":\"BSANTANDER\",\"Cantidad\":800,\"Porcentaje\":0,\"DocumentoIdentidad\":\"15345445-0\",\"TipoOperacion\":\"COMACN\",\"cobros\":[{\"IdCobro\":\"COFI\",\"Monto\":320,\"FormaCobro\":\"F\",\"Codigo\":\"COMISION\",\"PermiteCambio\":\"S\"},{\"IdCobro\":\"DEFI\",\"Monto\":100,\"FormaCobro\":\"F\",\"Codigo\":\"DERECHO\",\"PermiteCambio\":\"S\"},{\"IdCobro\":\"GAFI\",\"Monto\":85,\"FormaCobro\":\"F\",\"Codigo\":\"GASTO\",\"PermiteCambio\":\"S\"},{\"IdCobro\":\"IVA\",\"Monto\":19,\"FormaCobro\":\"F\",\"Codigo\":\"IMPUESTO\",\"PermiteCambio\":\"S\"}]},{\"IdPortafolio\":27499,\"Nemotecnico\":\"BSANTANDER\",\"Cantidad\":1450,\"Porcentaje\":0,\"DocumentoIdentidad\":\"333-6\",\"TipoOperacion\":\"COMACN\",\"cobros\":[{\"IdCobro\":\"COFI\",\"Monto\":248,\"FormaCobro\":\"F\",\"Codigo\":\"COMISION\",\"PermiteCambio\":\"S\"},{\"IdCobro\":\"DEFI\",\"Monto\":300,\"FormaCobro\":\"F\",\"Codigo\":\"DERECHO\",\"PermiteCambio\":\"S\"},{\"IdCobro\":\"GAFI\",\"Monto\":340,\"FormaCobro\":\"F\",\"Codigo\":\"GASTO\",\"PermiteCambio\":\"S\"},{\"IdCobro\":\"IVA\",\"Monto\":19,\"FormaCobro\":\"F\",\"Codigo\":\"IMPUESTO\",\"PermiteCambio\":\"S\"}]}]}";
			
			CrearSimulacionAsignacion_Entrada entrada = gson.fromJson(jsonTest, CrearSimulacionAsignacion_Entrada.class);
			SimulacionPrecioPromedioRefacturacion demo = new SimulacionPrecioPromedioRefacturacion();
			
			SalidaCrearSimulacionAsignacion salida = demo.logica(con, entrada);
			System.out.println(salida);
			con.rollback();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		
		
	}

}
