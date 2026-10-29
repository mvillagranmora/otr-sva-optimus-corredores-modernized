package cl.bcs.facturacion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.facturacion.CrearCargaMasivaRefacturacion.DetalleCargaMasiva;
import cl.bcs.facturacion.CrearCargaMasivaRefacturacion.DetalleTransancciones;
import cl.bcs.facturacion.pojo.SalidaCargaMasivaRefacturacion;
import cl.bcs.facturacion.pojo.SalidaCobrosSimulacion;
import cl.bcs.facturacion.pojo.SalidaSimulacion;
import cl.bcs.ordenes.CrearSimulacionAsignacion.PreferenciaPagoDefectoPortafolio;
import cl.bcs.ordenes.pojo.ConstantesOrdenes;
import cl.bcs.ordenes.pojo.IndicadoresPortafolio;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class SimulacionRefacturacionTransaccionesCienPorCiento implements JavaCall{
	private static final Logger logger = LogManager.getLogger(SimulacionRefacturacionTransaccionesCienPorCiento.class);

	
	private final String SQL_SELECT_CONSULTA_CLIENTE_SELECT= loadResourceToString("cl/bcs/facturacion/sql/SimulacionRefacturacionJPM_SelectClient.sql");

	private final String SQL_INSERT_GLOBAL_TRANSACCIONES= loadResourceToString("cl/bcs/facturacion/sql/SimulacionRefacturacionTransaccionesCienPorCiento_InsertGlobalTransacciones.sql");
	
	
	private final String SQL_SELECT_CONSULTA_TRANSACCIONES= loadResourceToString("cl/bcs/facturacion/sql/CrearCargaMasivaRefacturacion_SelectTransaccionesPorFolio.sql");
	private final String SQL_SELECT_PREFERENCIA_PAGO_DEFECTO = loadResourceToString("cl/bcs/ordenes/sql/CrearSimulacionAsignacion_SelectPreferenciaPagoDefecto.sql");
	private final String SQL_SELECT_INDICADORES_PORTAFOLIO = loadResourceToString("cl/bcs/ordenes/sql/Globales_SelectIndicadoresPortafolio.sql");
	private ConstantesOrdenes _c = new ConstantesOrdenes();
	public static class EntradaSimulacionRefacturacionTransaccionesCienPorCiento {
		public Long   p_IdEmpresa;
		public Long   p_IdUsuario;
		public String p_IdModulo;
		public List<DetalleCargaMasiva> p_DetalleCargaMasiva;
		public List<DetalleTransancciones>p_DetalleTransacciones;
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
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class SalidaSimulacionRefacturacionTransaccionesCienPorCiento extends SalidaCargaMasivaRefacturacion{
		public SalidaSimulacionRefacturacionTransaccionesCienPorCiento() {
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_SIMASIGNACION = new ArrayList<>();
			this.P_OUT_COBROSASIGNACION=new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR=0L;
		}
	
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
		 public String AccionOperacion;
	     public java.sql.Date FechaCreacionTransaccion;
	     public String  InstrumentoNegociacionTran;
	     public String FechaSistemaCreacion;
	     
		 public String toString() {
				return Utils.printJson(this, false);
			}
	}	
	public class EntradaCobros {
		public Long IdPortafolio;
		public String Nemotecnico;
		public BigDecimal Cantidad;
		public BigDecimal Porcentaje;
		public String DocumentoIdentidad;
		public String TipoOperacion;
		public String FolioTransaccion;
		
		//cobros
		List<CobrosArchivo> cobros;
		public EntradaCobros(){
			this.cobros= new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public class CobrosArchivo{
		public String IdCobro;
		public BigDecimal Monto;
		public String FormaCobro;
		public String Codigo;
		public String PermiteCambio;
		
		public CobrosArchivo(){}
		
		public CobrosArchivo(String IdCobro,BigDecimal Monto,String FormaCobro,String Codigo,String PermiteCambio){
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
	/******************************************************************************
	 * 							LOGICA
	 ******************************************************************************/
	public SalidaSimulacionRefacturacionTransaccionesCienPorCiento logica(Connection con, EntradaSimulacionRefacturacionTransaccionesCienPorCiento entrada) throws Exception {

		Long init = System.currentTimeMillis();
		long startTime = System.nanoTime();
		
		logger.info("== Inicio SimulacionRefacturacionTransaccionesCienPorCiento ==");
		
		
		SQL sql = new SQL();
		SalidaSimulacionRefacturacionTransaccionesCienPorCiento salida = new SalidaSimulacionRefacturacionTransaccionesCienPorCiento();
		List<SalidaSimulacion> salidaSimulaciones = new ArrayList<>();
		
		HashMap<String, DetalleCargaMasiva> cargaMasiva = new HashMap<>();
		List<EntradaCobros> listaEntradaCobros = new ArrayList<EntradaCobros>();
		
		List<String> listaDocumentoIdentidad = new ArrayList<>();

		
		List<ConsultaTransaccion> listaTrasacciones = new ArrayList<ConsultaTransaccion>();
		
		
		List<Long> listaIdPortafolios = new ArrayList<Long>();
		List<PreferenciaPagoDefectoPortafolio> preferenciaPagoDefecto = new ArrayList<>();
		HashMap<Long,Object> indicadoresPortafolios = new HashMap<>();
		//Prepara datos 
		for(DetalleCargaMasiva carga : entrada.p_DetalleCargaMasiva) {
			cargaMasiva.put(carga.Folio.toString(), carga);
			
			if (!listaDocumentoIdentidad.contains(carga.DocumentoIdentidad))
				listaDocumentoIdentidad.add(carga.DocumentoIdentidad);
			
			ConsultaTransaccion objTrasaccion = new ConsultaTransaccion();
			objTrasaccion.FolioTransaccion 		= carga.Folio;
			objTrasaccion.Instrumento 			= carga.Nemotecnico;
			objTrasaccion.FechaSistemaCreacion 	= carga.FechaTransaccion;
			
			if (!listaTrasacciones.contains(objTrasaccion))
				listaTrasacciones.add(objTrasaccion);
			
		}
		
		if (listaTrasacciones.size() > 0)
		{
			logger.debug("[SimulacionRefacturacionTransaccionesCienPorCiento] Inicio llamada SQL_INSERT_GLOBAL_TRANSACCIONES");
			startTime = System.nanoTime();
			sql.batch(con,SQL_INSERT_GLOBAL_TRANSACCIONES, listaTrasacciones,ConsultaTransaccion.class);
			logger.debug("[SimulacionRefacturacionTransaccionesCienPorCiento] (" + (System.nanoTime() - startTime)/1000000 + ") Fin llamada SQL_INSERT_GLOBAL_TRANSACCIONES");
			
		}
		
		HashMap<String,Object> paramsVacio = new HashMap<>();
		
		logger.debug("[SimulacionRefacturacionTransaccionesCienPorCiento] Inicio llamada SQL_SELECT_CONSULTA_TRANSACCIONES");
		startTime = System.nanoTime();
		List<ConsultaTransaccion>  listaTransacciones = sql.query(con, SQL_SELECT_CONSULTA_TRANSACCIONES, paramsVacio, ConsultaTransaccion.class);
		logger.debug("[SimulacionRefacturacionTransaccionesCienPorCiento] (" + (System.nanoTime() - startTime)/1000000 + ") Fin llamada SQL_SELECT_CONSULTA_TRANSACCIONES");
		
		if(listaTransacciones.isEmpty()) {
			salida.P_OUT_ERRORESCONTADOR = 1L;
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ERROR_TRANSACCIONES_NO_ENCONTRADAS","Transacciones no encontradas"));
			return salida;
		}
		
		HashMap<String, ConsultaTransaccion> datosTransacciones = new HashMap<String, ConsultaTransaccion>();
		String accionOperacionCarga = "";
		//Prepara datos de las transacciones consultadas
		for(ConsultaTransaccion data : listaTransacciones) {
			for(DetalleCargaMasiva cargaDetalle : entrada.p_DetalleCargaMasiva) {
				accionOperacionCarga = (cargaDetalle.Tipo.equalsIgnoreCase("C")) ? "COMPRA" : "VENTA";
				if (cargaDetalle.FechaTransaccion.equalsIgnoreCase(Fecha.toStringDate(data.FechaCreacionTransaccion)) && data.AccionOperacion.equalsIgnoreCase(accionOperacionCarga)) {
					datosTransacciones.put(data.FolioTransaccion, data);
				}
			}
		}
		
		List<DetalleCargaMasiva> transaccionesNOK = entrada.p_DetalleCargaMasiva.parallelStream().filter(tran->datosTransacciones.get(tran.Folio) == null).collect(Collectors.toList());
		
		if (transaccionesNOK.size() > 0) {
			for (DetalleCargaMasiva det : transaccionesNOK) {
				salida.P_OUT_ERRORESCONTADOR+=1L;
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ELEMENTO_CARGA_ERRONEA","Elemento con folio factura " + det.FolioFactura + " y folio transaccion " + det.Folio + " no encontrado"));
			}
			return salida;
		}
		
		HashMap<String,Object> paramsPortafoliosCliente = new HashMap<>();
		paramsPortafoliosCliente.put("DocumentoIdentidad",listaDocumentoIdentidad);

		logger.debug("[SimulacionRefacturacionTransaccionesCienPorCiento] Inicio llamada SQL_SELECT_CONSULTA_CLIENTE_SELECT");
		startTime = System.nanoTime();
		List<ClientePortafolio> listaClientePortafolio = sql.query(con, SQL_SELECT_CONSULTA_CLIENTE_SELECT, paramsPortafoliosCliente, ClientePortafolio.class);
		logger.debug("[SimulacionRefacturacionTransaccionesCienPorCiento] (" + (System.nanoTime() - startTime)/1000000 + ") Fin llamada SQL_SELECT_CONSULTA_CLIENTE_SELECT");
		
		if(!listaClientePortafolio.isEmpty()) {
			HashMap<String, ClientePortafolio> hashCliente = new HashMap<>();
			
			for(ClientePortafolio cliente : listaClientePortafolio) {
				
				hashCliente.put(cliente.DocumentoIdentidad+"_"+cliente.CodigoPortafolio, cliente);
				
				if (!listaIdPortafolios.contains(cliente.IdPortafolio))
					listaIdPortafolios.add(cliente.IdPortafolio);
			}
			
			logger.debug("[SimulacionRefacturacionTransaccionesCienPorCiento] Inicio asignacion de secuencia: BSCL.Orden.BD.Secuencias::DetalleSimulacionAsignacion");
			startTime = System.nanoTime();
			List<Long> secuencias = Utils.getSecuencia(con, "BSCL.Orden.BD.Secuencias::DetalleSimulacionAsignacion", (long) entrada.p_DetalleCargaMasiva.size());
			logger.debug("[SimulacionRefacturacionTransaccionesCienPorCiento] (" + (System.nanoTime() - startTime)/1000000 + ") Fin asignacion de secuencia: BSCL.Orden.BD.Secuencias::DetalleSimulacionAsignacion");
			int cont = 0;
			HashMap<String,Object> paramsPortafolios = new HashMap<>();
			
			paramsPortafolios.put("idsPortafolio", listaIdPortafolios);
			logger.debug("[SimulacionRefacturacionTransaccionesCienPorCiento] Inicio SQL_SELECT_INDICADORES_PORTAFOLIO");
			startTime = System.nanoTime();
			List<IndicadoresPortafolio> _indicadoresPortafolios = sql.query(con, SQL_SELECT_INDICADORES_PORTAFOLIO, paramsPortafolios, IndicadoresPortafolio.class);
			logger.debug("[SimulacionRefacturacionTransaccionesCienPorCiento] (" + (System.nanoTime() - startTime)/1000000 + ") Fin SQL_SELECT_INDICADORES_PORTAFOLIO");
			indicadoresPortafolios = Utils.List2Hash(_indicadoresPortafolios, "IdPortafolio");
			logger.debug("[SimulacionRefacturacionTransaccionesCienPorCiento] Inicio SQL_SELECT_PREFERENCIA_PAGO_DEFECTO");
			startTime = System.nanoTime();
			preferenciaPagoDefecto = sql.query(con, SQL_SELECT_PREFERENCIA_PAGO_DEFECTO, paramsPortafolios, PreferenciaPagoDefectoPortafolio.class);
			logger.debug("[SimulacionRefacturacionTransaccionesCienPorCiento] (" + (System.nanoTime() - startTime)/1000000 + ") Fin SQL_SELECT_PREFERENCIA_PAGO_DEFECTO");
			for(DetalleCargaMasiva cargaDetalle : entrada.p_DetalleCargaMasiva) {
				
				for(DetalleTransancciones transaccion : entrada.p_DetalleTransacciones) {
					if 	(cargaDetalle.Folio.toString().equalsIgnoreCase(transaccion.FolioTransaccion)  && transaccion.InstrumentoUI.equalsIgnoreCase(cargaDetalle.Nemotecnico))
					{
						if(cargaMasiva.containsKey(transaccion.FolioTransaccion)  && transaccion.CantidadTransaccion.compareTo(new BigDecimal(cargaMasiva.get(transaccion.FolioTransaccion).Cantidad)) != -1 && datosTransacciones.get(transaccion.FolioTransaccion) != null && cont < secuencias.size())
						{
							EntradaCobros entradaCobros = new EntradaCobros();
							String key = cargaDetalle.DocumentoIdentidad+"_"+cargaDetalle.Portafolio;
							if(hashCliente.containsKey(key)) {
								entradaCobros.IdPortafolio = hashCliente.get(key).IdPortafolio;
								entradaCobros.FolioTransaccion = transaccion.FolioTransaccion;
								entradaCobros.Cantidad=new BigDecimal(cargaDetalle.Cantidad); 
								entradaCobros.Porcentaje=BigDecimal.ZERO;
								entradaCobros.Nemotecnico=cargaDetalle.Nemotecnico;
								entradaCobros.DocumentoIdentidad=cargaDetalle.DocumentoIdentidad;
								entradaCobros.TipoOperacion=datosTransacciones.get(transaccion.FolioTransaccion).IdTipoOperacion;
								entradaCobros.cobros.add( new CobrosArchivo ("COFI",cargaDetalle.Comisiones,"F","COMISION","S"));
								entradaCobros.cobros.add( new CobrosArchivo ("DEFI",cargaDetalle.Derechos,"F","DERECHO","S"));
								entradaCobros.cobros.add( new CobrosArchivo ("GAFI",cargaDetalle.Gastos,"F","GASTO","S"));
								entradaCobros.cobros.add( new CobrosArchivo ("IVA" ,cargaDetalle.Iva,"F","IMPUESTO","S"));
								listaEntradaCobros.add(entradaCobros);
								SalidaSimulacion simulacionSalida = new SalidaSimulacion();
																
								simulacionSalida.IdInterno = secuencias.get(cont);
								simulacionSalida.FolioTransaccion = transaccion.FolioTransaccion;
								simulacionSalida.Instrumento = cargaDetalle.Nemotecnico;
								simulacionSalida.FechaTransaccion = Timestamp.valueOf(transaccion.FechaOperacionTransaccion);
								simulacionSalida.DocumentoIdentidad = cargaDetalle.DocumentoIdentidad;
								simulacionSalida.NombreCompleto = hashCliente.get(key).NombreCompleto;
								simulacionSalida.IdPortafolio = hashCliente.get(key).IdPortafolio;
								simulacionSalida.CodigoPortafolio = cargaDetalle.Portafolio;
								simulacionSalida.CantidadAsignacion = entradaCobros.Cantidad;
								simulacionSalida.IdMonedaInstrumento = datosTransacciones.get(transaccion.FolioTransaccion).IdMonedaInstrumento;
								simulacionSalida.IdMonedaLiquidacionOrden = datosTransacciones.get(transaccion.FolioTransaccion).IdMonedaLiquidacionOrden;
								simulacionSalida.IdTransaccion = datosTransacciones.get(transaccion.FolioTransaccion).IdTransaccion;
								simulacionSalida.IdBolsa = datosTransacciones.get(transaccion.FolioTransaccion).IdBolsa;
								simulacionSalida.IdBoveda= "DCV";
								simulacionSalida.IdTipoCanalIngreso= "TRA";
								simulacionSalida.IdTipoOperacion=datosTransacciones.get(transaccion.FolioTransaccion).IdTipoOperacion;
								simulacionSalida.IdTipoProducto=datosTransacciones.get(transaccion.FolioTransaccion).IdTipoProducto;
								simulacionSalida.IdFormaCustodia=datosTransacciones.get(transaccion.FolioTransaccion).IdFormaCustodia;
								simulacionSalida.InstrumentoNegociacionTran=datosTransacciones.get(transaccion.FolioTransaccion).InstrumentoNegociacionTran;


								if (indicadoresPortafolios.containsKey(hashCliente.get(key).IdPortafolio)) {
									IndicadoresPortafolio indPortafolios = (IndicadoresPortafolio) indicadoresPortafolios.get(hashCliente.get(key).IdPortafolio);
									if (indPortafolios.OperaCuentaInversion.equalsIgnoreCase(_c.SI)) {
										simulacionSalida.PagoCobroCuentaInversion = _c.SI;
									}
									else {
										simulacionSalida.PagoCobroCuentaInversion = _c.NO;
										
										for (PreferenciaPagoDefectoPortafolio pref : preferenciaPagoDefecto) {
											if (pref.IdPortafolio.equals(hashCliente.get(key).IdPortafolio) && 
													pref.IdMoneda.equalsIgnoreCase(datosTransacciones.get(transaccion.FolioTransaccion).IdMonedaLiquidacionOrden) &&
													pref.IdTipoOperacion.equalsIgnoreCase(datosTransacciones.get(transaccion.FolioTransaccion).IdTipoOperacion)) {
												simulacionSalida.IdPreferenciaPagoPortafolio = pref.IdPreferenciaPagoPortafolio;
												break;
											}		
										}
									}

									simulacionSalida.IdFormaCustodia = (simulacionSalida.IdFormaCustodia == null || simulacionSalida.IdFormaCustodia.equals("")) ? ((indPortafolios.OperaCustodiaRV.equalsIgnoreCase(_c.SI) || indPortafolios.OperaCustodiaRFIF.equalsIgnoreCase(_c.SI)) ? "CUS" : "NOM") : simulacionSalida.IdFormaCustodia;

								}
								
								
								simulacionSalida.PrecioPromedio = cargaDetalle.Precio;
								
								cont++;
								salidaSimulaciones.add(simulacionSalida);
							}else {
								salida.P_OUT_ERRORESCONTADOR+= 1L;
								salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ERROR_ASIGNACION_CLIENTE","Documento Identidad: "+ cargaMasiva.get(transaccion.FolioTransaccion).DocumentoIdentidad +" Portafolio: "+ cargaMasiva.get(transaccion.FolioTransaccion).Portafolio +" no existe"));
							}
					}
				  }	
						
						
				}//detalle transaccion
			}//detalle carga	
			if (salida.P_OUT_ERRORESCONTADOR.compareTo(0L) > 0) return salida;
			salida.P_OUT_SIMASIGNACION = salidaSimulaciones;
			/***********************************************************************
			 *    					COBROS 
			 ***********************************************************************/
			List<SalidaCobrosSimulacion>cobrosSalida = new ArrayList<>();
			Long Correlativo =0L;
			
			HashMap<String,EntradaCobros> HashPortafolioCobros = new HashMap<String,EntradaCobros>();
			
			for (EntradaCobros por : listaEntradaCobros){
				String key = por.FolioTransaccion+"_"+por.IdPortafolio+"_"+por.TipoOperacion+"_"+por.Nemotecnico;
				HashPortafolioCobros.put(key, por);
			}
			
			/* Carga de cobros por cliente */				
			cont =0;
			for(SalidaSimulacion sim : salidaSimulaciones){
				SalidaCobrosSimulacion nCobro = new SalidaCobrosSimulacion();
				String key =  sim.FolioTransaccion+"_"+sim.IdPortafolio+"_"+sim.IdTipoOperacion+"_"+sim.Instrumento;
				
				List<CobrosArchivo> ListaCobro = HashPortafolioCobros.get(key).cobros;
				if(salidaSimulaciones.size()==cont) {
					break;
				}
	
				for(CobrosArchivo cob : ListaCobro) {
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
			
			
			salida.P_OUT_COBROSASIGNACION.addAll(cobrosSalida);	
		}else {
			salida.P_OUT_ERRORESCONTADOR = 1L;
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ERROR_ASIGNACION_CLIENTE","Cliente no se encuentra"));
		}
		
		logger.info("== Termino del servicio SimulacionRefacturacionTransaccionesCienPorCiento, tiempo: " + (System.currentTimeMillis() - init) + " ms - " + (System.currentTimeMillis() - init)/1000 + " seg ==");
		return salida;
	}
	
	/******************************************************************************
	 *                          CALL
	 ******************************************************************************/

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		EntradaSimulacionRefacturacionTransaccionesCienPorCiento in = gson.fromJson(jin.toString(), EntradaSimulacionRefacturacionTransaccionesCienPorCiento.class);
		SalidaSimulacionRefacturacionTransaccionesCienPorCiento out = logica(conn, in);
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
			
			EntradaSimulacionRefacturacionTransaccionesCienPorCiento entrada = gson.fromJson(jsonTest, EntradaSimulacionRefacturacionTransaccionesCienPorCiento.class);
			SimulacionRefacturacionTransaccionesCienPorCiento demo = new SimulacionRefacturacionTransaccionesCienPorCiento();
			
			SalidaSimulacionRefacturacionTransaccionesCienPorCiento salida = demo.logica(con, entrada);
			System.out.println(salida);
			con.rollback();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		
		
	}

}
