package cl.bcs.facturacion;

import java.io.IOException;


import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.facturacion.CrearCargaMasivaRefacturacion.DetalleCargaMasiva;
import cl.bcs.facturacion.SimulacionPrecioPromedioRefacturacion.Portafolio_Entrada;
import cl.bcs.facturacion.SimulacionPrecioPromedioRefacturacion.SalidaCrearSimulacionAsignacion;
import cl.bcs.facturacion.SimulacionPrecioPromedioRefacturacion.Transaccion_Entrada;
import cl.bcs.facturacion.SimulacionRefacturacionTransaccionesCienPorCiento.ConsultaTransaccion;
import cl.bcs.facturacion.SimulacionRefacturacionTransaccionesCienPorCiento.EntradaSimulacionRefacturacionTransaccionesCienPorCiento;
import cl.bcs.facturacion.SimulacionRefacturacionTransaccionesCienPorCiento.SalidaSimulacionRefacturacionTransaccionesCienPorCiento;
import cl.bcs.facturacion.SimulacionRefacturacionTransaccionesCienPorCiento.EntradaSimulacionRefacturacionTransaccionesCienPorCiento;
import cl.bcs.facturacion.pojo.SalidaCargaMasivaRefacturacion;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearCargaMasivaRefacturacion implements JavaCall	{
	private static final Logger logger = LogManager.getLogger(CrearCargaMasivaRefacturacion.class);
	
	private final String SQL_SELECT_CONSULTA_CLIENTE= loadResourceToString("cl/bcs/facturacion/sql/CrearCargaMasivaRefacturacion_SelectCliente.sql");
	private final String SQL_SELECT_CONSULTA_PORTAFOLIO= loadResourceToString("cl/bcs/facturacion/sql/CrearCargaMasivaRefacturacion_SelectPortafolio.sql");
	private final String SQL_SELECT_CONSULTA_CLIENTE_PORTAFOLIO= loadResourceToString("cl/bcs/facturacion/sql/CrearCargaMasivaRefacturacion_SelectClienteCodigoPortafolio.sql");
	private final String SQL_SELECT_CONSULTA_COMPROBANTES= loadResourceToString("cl/bcs/facturacion/sql/CrearCargaMasivaRefacturacion_SelectComprobantes.sql");
		
	private final String SQL_INSERT_GLOBAL_TRANSACCIONES= loadResourceToString("cl/bcs/facturacion/sql/CrearCargaMasivaRefacturacion_InsertGlobalTransacciones.sql");
	/******************************************************************************
	 * 							CLASES
	 ******************************************************************************/
	public static class EntradaCargaMasivaFacturacion {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public Long p_IdTipoCargaMasiva;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_CodigoArchivo;
		public String p_TipoCarga;  //NEMO - NEMTRA 
		public String p_FechaCarga;
		public List<DetalleCargaMasiva> p_DetalleCargaMasiva;
		public List<DetalleTransancciones>p_DetalleTransacciones;
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class DetalleTransancciones {
		

		

		public String AccionOrden;   //  COMPRA   VENTA
		
		
		public BigDecimal CantidadDisponibleTransaccion;
		public BigDecimal CantidadTransaccionPorAsignar;
		public BigDecimal CantidadTransaccion;
		public BigDecimal CantidadSimulacionTransaccion;
		public String EsFungible;
		public String EsFungibleUI;
		public String EsVentaCorta;
		public String EsVentaCortaUI;
		public String FolioTransaccion;
		public String FechaSistemaCreacion;
		public String FechaOperacionTransaccion;
		public String FechaVencimiento;
		public String HoraSistemaCreacion;
		public String IdBolsa;
		public String IdCondicionDeLiquidacion;
		public String IdClasificacionTipoInstrumento;
		public String IdEmisor;
		public Long IdInstrumento;
		public String IdMonedaTransaccion;
		public String IdTipoOperacion;
		public String IdTipoProducto;
		public String IdTipoReajuste;
		public String IdTipoInstrumento;
		public Long IdTransaccion;
		public String IndicadorOrdenDirecta;
		public String InstrumentoUI;
		public BigDecimal MontoTransaccion;
		public String NemotecnicoSVS;
		public String NombreTipoOperacion;
		public String NombreTipoInstrumento;
		public String NombreClasificacionTipoInstrumento;
		public String NombreCondicionDeLiquidacion;
		public String NombreCorredorContraparte;
		public BigDecimal PrecioTransaccion;
		public BigDecimal Tasa;

		public String getFolioTransaccion() {
			return this.FolioTransaccion;
		}

		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}

	public static class DetalleCargaMasiva {

		public String FechaRegisro;
		public String FechaTransaccion;
		public String FolioTransaccion;
		public Long FolioFactura;
		public String Folio;
		public String Nemotecnico;
		public String Tipo;
		public Long Cantidad;
		public String DocumentoIdentidad;
		public String Portafolio;
		public Long IdPortafolio;
		public BigDecimal Comisiones;
		public BigDecimal Derechos;
		public BigDecimal Gastos;
		public BigDecimal Iva;
		public String Identificador;
		public BigDecimal Precio;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class SalidaCargaMasivaFacturacion extends SalidaCargaMasivaRefacturacion {

		public SalidaCargaMasivaFacturacion() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_SIMASIGNACION = new ArrayList<>();
			P_OUT_COBROSASIGNACION = new ArrayList<>();
			P_OUT_COMPROBANTES = new ArrayList<>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ObjValidarCliente {
		public Long   IdCliente;
		public String IdEstadoCliente;
		public String IdEstadoPersona;
		public String DocumentoIdentidad;
		public String NombreCompleto;

		public Long   IdPortafolio;
		public String CodigoPortafolio;
		public String IdEstadoPortafolio;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}


	public static class ObjValidarPortafolio {
		public String 	IdEstadoPortafolio;
		public String 	CodigoPortafolio;
		public Long 	IdPortafolio;
		public String 	DocumentoIdentidad;
		

		public ObjValidarPortafolio() {
			this.IdEstadoPortafolio = "";
			this.IdPortafolio = 0L;
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ObjCodigoPortafolio{
		public String CodigoPortafolio;
		public Long   IdPortafolio;
		public String DocumentoIdentidad;
	
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Comprobantes{
		public String Comprobante;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaQueryComprobantes{
		public Long SecuenciaComprobante;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaQueryFacturas{
		public Long IdComprobante;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class Transaccion{		 
		 public String FolioTransaccion;
		 public String Nemotecnico;    
	     public String FechaSistemaCreacion;
	     
		 public String toString() {
				return Utils.printJson(this, false);
			}
	}
	
	/******************************************************************************
	 * 							LOGICA
	 ******************************************************************************/
	
	public SalidaCargaMasivaFacturacion logica (Connection con, EntradaCargaMasivaFacturacion entrada)throws Exception {
		Long init = System.currentTimeMillis();
		long startTime = System.nanoTime();
		
		logger.info("== Inicio servicio CrearCargaMasivaRefacturacion ==");
		SQL sql = new SQL();
		SalidaCargaMasivaFacturacion salida = new  SalidaCargaMasivaFacturacion();
		

		logger.debug("[CrearCargaMasivaRefacturacion] Inicio llamada metodo ValidacionCarga");
		startTime = System.nanoTime();
		List<MensajeSalida> validacionDetalle = ValidacionCarga(entrada.p_DetalleCargaMasiva, entrada.p_DetalleTransacciones, con, entrada.p_IdEmpresa);
		logger.debug("[CrearCargaMasivaRefacturacion] (" + (System.nanoTime() - startTime)/1000000 + ") Fin llamada metodo ValidacionCarga");
		salida.P_OUT_MENSAJES.addAll(validacionDetalle);
		
		if (MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES) > 0)
		{
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			return salida;
		}
		if(entrada.p_CodigoArchivo.equalsIgnoreCase("CARGA_REFACTURACION_JPM_2")) {
			
			
			// crear lista para agregar hashmap
			Set<String> foliosTransacciones = new HashSet<>(entrada.p_DetalleCargaMasiva.parallelStream().map(trx -> String.valueOf(trx.Folio)).collect(Collectors.toList()));


			Set<String> foliosTransaccionesSinFactura = new HashSet<>(entrada.p_DetalleCargaMasiva.parallelStream()
					.filter(trx -> trx.FolioFactura == null)
					.map(trx -> String.valueOf(trx.Folio)).collect(Collectors.toList()));
	
			List<Transaccion> listaTrasacciones = new ArrayList<Transaccion>();
			for(DetalleTransancciones tra : entrada.p_DetalleTransacciones) {
				
				if (foliosTransacciones.contains(tra.FolioTransaccion) && (!foliosTransaccionesSinFactura.contains(tra.FolioTransaccion)))
				{
					Transaccion objTrasaccion = new Transaccion();
					objTrasaccion.FolioTransaccion 		= tra.FolioTransaccion;
					objTrasaccion.Nemotecnico 			= tra.InstrumentoUI;
					objTrasaccion.FechaSistemaCreacion 	= tra.FechaSistemaCreacion.substring(0,10);
					
					if (!listaTrasacciones.contains(objTrasaccion))
						listaTrasacciones.add(objTrasaccion);
				}
	
			}
			
			if (listaTrasacciones.size() > 0)
			{
				logger.debug("[CrearCargaMasivaRefacturacion] Inicio llamada SQL_INSERT_GLOBAL_TRANSACCIONES");
				startTime = System.nanoTime();
				sql.batch(con,SQL_INSERT_GLOBAL_TRANSACCIONES, listaTrasacciones,Transaccion.class);
				logger.debug("[CrearCargaMasivaRefacturacion] (" + (System.nanoTime() - startTime)/1000000 + ") Fin llamada SQL_INSERT_GLOBAL_TRANSACCIONES");
				
			}
			
			
			
			
			
			HashMap<String,Object> paramsVacio = new HashMap<>();
			logger.debug("[CrearCargaMasivaRefacturacion] Inicio llamada SQL_SELECT_CONSULTA_COMPROBANTES");
			startTime = System.nanoTime();
			List<SalidaQueryComprobantes> comprobantes = sql.query(con, SQL_SELECT_CONSULTA_COMPROBANTES, paramsVacio, SalidaQueryComprobantes.class);
			logger.debug("[CrearCargaMasivaRefacturacion] (" + (System.nanoTime() - startTime)/1000000 + ") Fin llamada SQL_SELECT_CONSULTA_COMPROBANTES");
			
			
			HashMap<Long, Object> hashComprobantes = Utils.List2Hash(comprobantes, "SecuenciaComprobante");

			List<DetalleCargaMasiva> comprobantesNOK = entrada.p_DetalleCargaMasiva.parallelStream()
					.filter(comp -> hashComprobantes.get(comp.FolioFactura) == null && comp.FolioFactura != null).collect(Collectors.toList());


			
			
			if (comprobantesNOK.size() > 0) {
				for (DetalleCargaMasiva det : comprobantesNOK) {
					salida.P_OUT_ERRORESCONTADOR+=1L;
					salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ELEMENTO_CARGA_ERRONEA","Elemento con folio factura " + det.FolioFactura + " y folio transaccion " + det.Folio + " no encontrado"));
				}
				return salida;
			}
			
			salida.P_OUT_COMPROBANTES = comprobantes.parallelStream().map(comprob->comprob.SecuenciaComprobante).collect(Collectors.toList());
			
			HashMap<String,Object> folioFactura = new HashMap<>();
			folioFactura.put("FolioFactura", entrada.p_DetalleCargaMasiva.parallelStream()
					.map(data -> data.FolioFactura).distinct().collect(Collectors.toList()));


			
			logger.debug("[CrearCargaMasivaRefacturacion] Inicio Filtrar Comprobantes");
			startTime = System.nanoTime();
			List<Long> facts = entrada.p_DetalleCargaMasiva.parallelStream()
											.filter(trx -> trx.FolioFactura != null)
											.map(data -> data.FolioFactura).distinct().collect(Collectors.toList());

			salida.P_OUT_COMPROBANTES.retainAll(facts);
			logger.debug("[CrearCargaMasivaRefacturacion] (" + (System.nanoTime() - startTime)/1000000 + ") Fin Filtrar Comprobantes");
			if (!salida.P_OUT_COMPROBANTES.isEmpty() || foliosTransacciones.size() == foliosTransaccionesSinFactura.size()) 
			{

				SimulacionRefacturacionTransaccionesCienPorCiento simularTransacciones = new SimulacionRefacturacionTransaccionesCienPorCiento();
				EntradaSimulacionRefacturacionTransaccionesCienPorCiento entradaSimulacionTransacciones = new EntradaSimulacionRefacturacionTransaccionesCienPorCiento();
				SalidaSimulacionRefacturacionTransaccionesCienPorCiento salidaSimulacionTransacciones = new SalidaSimulacionRefacturacionTransaccionesCienPorCiento();
				
				entradaSimulacionTransacciones.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaSimulacionTransacciones.p_IdModulo = entrada.p_IdModulo;
				entradaSimulacionTransacciones.p_IdUsuario = entrada.p_IdUsuario;
				entradaSimulacionTransacciones.p_DetalleCargaMasiva = entrada.p_DetalleCargaMasiva;
				entradaSimulacionTransacciones.p_DetalleTransacciones = entrada.p_DetalleTransacciones;
			
				logger.debug("[CrearCargaMasivaRefacturacion] Inicio llamada servicio SimulacionRefacturacionTransaccionesCienPorCiento");
				startTime = System.nanoTime();
				salidaSimulacionTransacciones = simularTransacciones.logica(con, entradaSimulacionTransacciones);
				logger.debug("[CrearCargaMasivaRefacturacion] (" + (System.nanoTime() - startTime)/1000000 + ") Fin llamada servicio SimulacionRefacturacionTransaccionesCienPorCiento");
				if(!salidaSimulacionTransacciones.P_OUT_SIMASIGNACION.isEmpty()){
					salida.P_OUT_SIMASIGNACION.addAll(salidaSimulacionTransacciones.P_OUT_SIMASIGNACION);
					salida.P_OUT_COBROSASIGNACION.addAll(salidaSimulacionTransacciones.P_OUT_COBROSASIGNACION);
				}else{
					salida.P_OUT_MENSAJES.addAll(salidaSimulacionTransacciones.P_OUT_MENSAJES);
					salida.P_OUT_ERRORESCONTADOR=salidaSimulacionTransacciones.P_OUT_ERRORESCONTADOR;
				}
			}
		}else {
		
		List<String> CodPortafolio = new ArrayList<String>();
		List<String> DocumentoIdentidad = new ArrayList<String>();
		
		for (DetalleCargaMasiva carga : entrada.p_DetalleCargaMasiva){
			CodPortafolio.add(carga.Portafolio);
			DocumentoIdentidad.add(carga.DocumentoIdentidad);
		}
		
		
		try {
			
			//cambio CodigoPortafolio por IdPortafolio
			HashMap<String,Object> paramsClientesPortafolio = new HashMap<>();
			paramsClientesPortafolio.put("DocumentoIdentidad",DocumentoIdentidad);
			paramsClientesPortafolio.put("CodigoPortafolio",CodPortafolio);
			
				logger.debug("[CrearCargaMasivaRefacturacion] Inicio llamada SQL_SELECT_CONSULTA_CLIENTE_PORTAFOLIO 222");
				startTime = System.nanoTime();
				List<ObjCodigoPortafolio> ListaIdPortafolio = sql.query(con, SQL_SELECT_CONSULTA_CLIENTE_PORTAFOLIO,
						paramsClientesPortafolio, ObjCodigoPortafolio.class);
				logger.debug("[CrearCargaMasivaRefacturacion] (" + (System.nanoTime() - startTime)/1000000 + ") Fin llamada SQL_SELECT_CONSULTA_CLIENTE_PORTAFOLIO 222");
			List<DetalleCargaMasiva> ListEntCarga = new ArrayList <DetalleCargaMasiva>();
			for (DetalleCargaMasiva carga : entrada.p_DetalleCargaMasiva){
				DetalleCargaMasiva ObjCarga = new DetalleCargaMasiva();
				
				for(ObjCodigoPortafolio portafolio : ListaIdPortafolio) {
					
					if(carga.DocumentoIdentidad.equals(portafolio.DocumentoIdentidad) && carga.Portafolio.equals(portafolio.CodigoPortafolio)) {
						Utils.copyPojo(carga, ObjCarga);
						ObjCarga.IdPortafolio= portafolio.IdPortafolio;
						
						ListEntCarga.add(ObjCarga);
					}
				}
			}
			
			entrada.p_DetalleCargaMasiva.clear();
			entrada.p_DetalleCargaMasiva.addAll(ListEntCarga);	
			
			SimulacionPrecioPromedioRefacturacion Simular = new SimulacionPrecioPromedioRefacturacion();
			SalidaCrearSimulacionAsignacion salidaSimulacion = new SalidaCrearSimulacionAsignacion();
			
			if (entrada.p_DetalleCargaMasiva.size() > 0) {
				SimulacionPrecioPromedioRefacturacion.CrearSimulacionAsignacion_Entrada simulaciones = new SimulacionPrecioPromedioRefacturacion.CrearSimulacionAsignacion_Entrada ();
				simulaciones.p_IdEmpresa=entrada.p_IdEmpresa;
				simulaciones.p_IdUsuario = entrada.p_IdUsuario;
				simulaciones.p_IdModulo="ORD"; //
				simulaciones.p_UsaReglasNegocio="S";
				simulaciones.p_IdTipo="NORMAL";
				simulaciones.p_Origen="ASIG";
				simulaciones.p_IdTipoAsignacion="ASIGNACION_PRECIO_PROMEDIO"; 
				
				
				//Archivo de carga por nemo
				if(entrada.p_TipoCarga.equals("NEMO")){
					for (DetalleCargaMasiva carga : entrada.p_DetalleCargaMasiva){
						List<Transaccion_Entrada>TraTmp = new ArrayList<>();
						Portafolio_Entrada SimPor=new Portafolio_Entrada();						
						
						for(DetalleTransancciones tran : entrada.p_DetalleTransacciones){							
							Transaccion_Entrada SimTran=new Transaccion_Entrada();
							
							if(carga.Nemotecnico.equals(tran.InstrumentoUI) && tran.IdTipoOperacion.equalsIgnoreCase(carga.Tipo)){ //ValidaTipoOperacion(tran.IdTipoOperacion , carga.Tipo)
								//crear transaccion entrada 
								SimTran.IdMovimiento = tran.IdTransaccion; 
								SimTran.IdTransaccion=tran.IdTransaccion;
								SimTran.Cantidad=tran.CantidadDisponibleTransaccion;
								SimTran.Monto= BigDecimal.ZERO;
								SimTran.ValorRescate= BigDecimal.ZERO;
								SimTran.Nemotecnico = tran.InstrumentoUI;
								
								TraTmp.add(SimTran);
								AgregarTransaccion(simulaciones.p_Transacciones,SimTran);
								
							}
						}
						
						if(TraTmp.size()!=0){ 
							
							SimPor.IdPortafolio=carga.IdPortafolio;
							SimPor.Cantidad=new BigDecimal(carga.Cantidad); 
							SimPor.Porcentaje=BigDecimal.ZERO;
							SimPor.Nemotecnico=carga.Nemotecnico;
							SimPor.DocumentoIdentidad=carga.DocumentoIdentidad;
							SimPor.TipoOperacion=carga.Tipo;
							SimPor.cobros.add( new SimulacionPrecioPromedioRefacturacion.Cobros_Archivo ("COFI",carga.Comisiones,"F","COMISION","S"));
							SimPor.cobros.add( new SimulacionPrecioPromedioRefacturacion.Cobros_Archivo ("DEFI",carga.Derechos,"F","DERECHO","S"));
							SimPor.cobros.add( new SimulacionPrecioPromedioRefacturacion.Cobros_Archivo ("GAFI",carga.Gastos,"F","GASTO","S"));
							SimPor.cobros.add( new SimulacionPrecioPromedioRefacturacion.Cobros_Archivo ("IVA" ,carga.Iva,"F","IMPUESTO","S"));
							simulaciones.p_Portafolios.add(SimPor);				
						}
						
					}
				}
				//archivo de carga nemotransaccion
				if(entrada.p_TipoCarga.equals("NEMTRA")){
					for (DetalleCargaMasiva carga : entrada.p_DetalleCargaMasiva){
						
						List<Transaccion_Entrada>TraTmp = new ArrayList<>();
						Portafolio_Entrada SimPor=new Portafolio_Entrada();						
						
						for(DetalleTransancciones tran : entrada.p_DetalleTransacciones){	
							Transaccion_Entrada SimTran=new Transaccion_Entrada();
							String FolioTransaccion= String.valueOf(carga.Folio);
								if (carga.Nemotecnico.equals(tran.InstrumentoUI)
										&& FolioTransaccion.equals(tran.FolioTransaccion)
										

										&& validarTipoOperacion(tran.AccionOrden, carga.Tipo)
										
										) 
								{
								//crear transaccion entrada 
								SimTran.IdMovimiento =tran.IdTransaccion;
								SimTran.IdTransaccion=tran.IdTransaccion;
								SimTran.Cantidad=tran.CantidadDisponibleTransaccion;
								SimTran.Monto= BigDecimal.ZERO;
								SimTran.ValorRescate= BigDecimal.ZERO;
								SimTran.Nemotecnico = tran.InstrumentoUI;
								
								TraTmp.add(SimTran);
								AgregarTransaccion(simulaciones.p_Transacciones,SimTran);
							}
						}
						//CREA PORTAFOLIOS
						
						if(TraTmp.size()!=0){
							
							SimPor.IdPortafolio=carga.IdPortafolio;
							SimPor.Cantidad=new BigDecimal(carga.Cantidad); 
							SimPor.Porcentaje=BigDecimal.ZERO;
							SimPor.Nemotecnico=carga.Nemotecnico;
							SimPor.DocumentoIdentidad=carga.DocumentoIdentidad;
							SimPor.TipoOperacion=carga.Tipo;
							SimPor.cobros.add( new SimulacionPrecioPromedioRefacturacion.Cobros_Archivo ("COFI",carga.Comisiones,"F","COMISION","S"));
							SimPor.cobros.add( new SimulacionPrecioPromedioRefacturacion.Cobros_Archivo ("DEFI",carga.Derechos,"F","DERECHO","S"));
							SimPor.cobros.add( new SimulacionPrecioPromedioRefacturacion.Cobros_Archivo ("GAFI",carga.Gastos,"F","GASTO","S"));
							SimPor.cobros.add( new SimulacionPrecioPromedioRefacturacion.Cobros_Archivo ("IVA" ,carga.Iva,"F","IMPUESTO","S"));
							
							simulaciones.p_Portafolios.add(SimPor);
							
						}else if(TraTmp.size()==0){
							salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA,"ELEMENTO_CARGA_ERRONEA","Elemento con folio:"+carga.Folio+" y Tipo de operacion "+carga.Tipo+" Esta con errores"));
						}
							
					}				
				}
				
				
				//SIMULACIONES
				if(simulaciones.p_Transacciones.size()>0 && simulaciones.p_Portafolios.size()>0)
				{
						logger.debug("[CrearCargaMasivaRefacturacion] Inicio llamada servicio SimulacionPrecioPromedioRefacturacion");
						startTime = System.nanoTime();
					salidaSimulacion = Simular.logica(con,simulaciones);
						logger.debug("[CrearCargaMasivaRefacturacion] (" + (System.nanoTime() - startTime)/1000000 + ") Fin llamada servicio SimulacionPrecioPromedioRefacturacion");
				}else{
					salida.P_OUT_ERRORESCONTADOR=1L;
					salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR,"ARCHIVO_VACIO","Detalle de carga sin datos"));
				}
				
				
				if(salidaSimulacion.P_OUT_SIMASIGNACION.size()>0){
					salida.P_OUT_SIMASIGNACION.addAll(salidaSimulacion.P_OUT_SIMASIGNACION);
					salida.P_OUT_COBROSASIGNACION.addAll(salidaSimulacion.P_OUT_COBROSASIGNACION);
				}else{
					salida.P_OUT_MENSAJES.addAll(salidaSimulacion.P_OUT_MENSAJES);
					salida.P_OUT_ERRORESCONTADOR=salidaSimulacion.P_OUT_ERRORESCONTADOR;
				}

				} 
				else {

						salida.P_OUT_ERRORESCONTADOR = 1L;
						salida.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "ARCHIVO_VACIO",
								"Detalle de carga sin datos"));

				
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		}
		logger.info("== Termino del servicio CrearCargaMasivaRefacturacion, tiempo: " + (System.currentTimeMillis() - init) + " ms - " + (System.currentTimeMillis() - init)/1000 + " seg ==");
		return salida;
	}
	
	//VALIDACION
	
	public void AgregarTransaccion (List<Transaccion_Entrada> ListaTrx, Transaccion_Entrada ObjTrx ){
		
	boolean rep=false;
	
		for (Transaccion_Entrada tmp : ListaTrx){
			if(tmp.IdTransaccion.equals(ObjTrx.IdTransaccion)){
				rep=true;
			}			
		}
		if(!rep){
			ListaTrx.add(ObjTrx);
		}
		
	}

	/**
	 * @param TipoOperacionTrx: COMPRA, VENTA
	 * @param TipoOperacionArchivo: C, V
	 * @return 
	 */
	public boolean validarTipoOperacion(String tipoOperacionTrx, String tipoOperacionArchivo) {
		String tipoOperacionArchivoNew = "";
		
		if (tipoOperacionArchivo.equalsIgnoreCase("C")) 
		{
			tipoOperacionArchivoNew = "COMPRA";
		} 
		else if (tipoOperacionArchivo.equalsIgnoreCase("V")) 
		{
			tipoOperacionArchivoNew = "VENTA";
		}

		if (tipoOperacionTrx.equalsIgnoreCase(tipoOperacionArchivoNew))
			return true;
		else		
			return false;
	}



	public List<MensajeSalida> ValidacionCarga(List<DetalleCargaMasiva> DetalleCarga, List<DetalleTransancciones> DetalleTransacciones, Connection con, Long idEmpresa) throws Exception {
		long startTime = System.nanoTime();
		SQL sql = new SQL();
		List<MensajeSalida> SalidaValidacion = new ArrayList<>();
		List<String> ListaIdClientes = new ArrayList<String>();
		
		HashMap<String, Long> hashCantidadCarga = new HashMap<String, Long>();
		HashMap<String, Long> hashCantidadTransaccion = new HashMap<String, Long>();

		for (DetalleCargaMasiva carga : DetalleCarga) {
			
			if (!ListaIdClientes.contains(carga.DocumentoIdentidad))
				ListaIdClientes.add(carga.DocumentoIdentidad);
			
			Long cantidadCarga = hashCantidadCarga.get(carga.Folio);
			if (cantidadCarga == null)
				hashCantidadCarga.put(carga.Folio, carga.Cantidad);
			else				
				hashCantidadCarga.put(carga.Folio, cantidadCarga + carga.Cantidad);
			
		}
		
		for (DetalleTransancciones transaccion : DetalleTransacciones) {
			Long cantidadTransanccion = hashCantidadTransaccion.get(transaccion.FolioTransaccion);
			if (cantidadTransanccion == null)
				hashCantidadTransaccion.put(transaccion.FolioTransaccion,  transaccion.CantidadTransaccionPorAsignar.longValue());
			else				
				hashCantidadTransaccion.put(transaccion.FolioTransaccion, cantidadTransanccion + transaccion.CantidadTransaccionPorAsignar.longValue());	
		}
		
	
		//Valida que no vengan valores de cobros negativos
		for (DetalleCargaMasiva carga : DetalleCarga) {
			
			if (carga.Comisiones.compareTo(BigDecimal.ZERO) < 0 || carga.Gastos.compareTo(BigDecimal.ZERO) < 0  || carga.Iva.compareTo(BigDecimal.ZERO) < 0)
			{
				HashMap<String,String> paramMsj = new HashMap<String,String>();
				paramMsj.put("DocumentoIdentidad", carga.DocumentoIdentidad);
				paramMsj.put("FolioTransaccion", carga.Folio);
				SalidaValidacion.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "COBROS_DOCUMENTO_IDENTIDAD_NEGATIVOS", paramMsj, con, idEmpresa));
			}
			
		}

		try {

			HashMap<String, Object> paramsClientes = new HashMap<>();
			paramsClientes.put("DocumentoIdentidad", ListaIdClientes);
			logger.debug("[CrearCargaMasivaRefacturacion] Inicio llamada SQL_SELECT_CONSULTA_CLIENTE");
			startTime = System.nanoTime();
			List<ObjValidarCliente> lstClientes = sql.query(con, SQL_SELECT_CONSULTA_CLIENTE, paramsClientes,	ObjValidarCliente.class);
			logger.debug("[CrearCargaMasivaRefacturacion] (" + (System.nanoTime() - startTime)/1000000 + ") Fin llamada SQL_SELECT_CONSULTA_CLIENTE");

			
			//Valida que el cliente exista
			for (DetalleCargaMasiva carga : DetalleCarga) {			

				List<ObjValidarCliente> resultCli = lstClientes.stream().filter(c -> c.DocumentoIdentidad.equals(carga.DocumentoIdentidad)).collect(Collectors.toList());
				
				if (resultCli.size() == 0)
				{
					HashMap<String,String> paramMsj = new HashMap<String,String>();
					paramMsj.put("DocumentoIdentidad", carga.DocumentoIdentidad);	
					paramMsj.put("FolioTransaccion", carga.Folio);					
					SalidaValidacion.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "CLIENTE_DOCUMENTO_IDENTIDAD_NO_EXISTE", paramMsj, con, idEmpresa));
				}
				
			}
	
			//Valida que el cliente no este bloqueado
			for (int i = 0; i < lstClientes.size(); i++) {				
				if (lstClientes.get(i).IdEstadoCliente.equalsIgnoreCase("BLO") || lstClientes.get(i).IdEstadoPersona.equalsIgnoreCase("BLO")) 
				{				
					HashMap<String,String> paramMsj = new HashMap<String,String>();
					paramMsj.put("DocumentoIdentidad", lstClientes.get(i).DocumentoIdentidad);					
					SalidaValidacion.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "CLIENTE_DOCUMENTO_IDENTIDAD_BLOQUEADO", paramMsj, con, idEmpresa));
				}
			}
		
			
			for (DetalleCargaMasiva carga : DetalleCarga) {
				List<ObjValidarCliente> resultPort = lstClientes.stream().filter(c -> c.DocumentoIdentidad.equals(carga.DocumentoIdentidad) && c.CodigoPortafolio.equals(carga.Portafolio)).collect(Collectors.toList());
				
				// Valida que el portafolio exista
				if (resultPort.size() == 0)
				{				
					HashMap<String,String> paramMsj = new HashMap<String,String>();
					paramMsj.put("Portafolio", carga.Portafolio);
					paramMsj.put("DocumentoIdentidad", carga.DocumentoIdentidad);
					paramMsj.put("FolioTransaccion", carga.Folio);					
					SalidaValidacion.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "PORTAFOLIO_DOCUMENTO_IDENTIDAD_NO_EXISTE", paramMsj, con, idEmpresa));
									
					continue;
				}
				
				// Valida que el portafolio no este bloqueado
				if (resultPort.get(0).IdEstadoPortafolio.equalsIgnoreCase("BLO"))
				{				
					HashMap<String,String> paramMsj = new HashMap<String,String>();
					paramMsj.put("Portafolio", carga.Portafolio);
					paramMsj.put("DocumentoIdentidad", carga.DocumentoIdentidad);
					paramMsj.put("FolioTransaccion", carga.Folio);	
					SalidaValidacion.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "PORTAFOLIO_DOCUMENTO_IDENTIDAD_BLOQUEADO", paramMsj, con, idEmpresa));
				}	
			}

			//Valida que la cantidad asignada no exeda el saldo disponible por transaccion
			List<String> foliosTransaccionValidados = new ArrayList<String>();
			for (DetalleCargaMasiva carga : DetalleCarga) {
				Long cantidadCarga 		  = hashCantidadCarga.get(carga.Folio);
				Long cantidadTransanccion = hashCantidadTransaccion.get(carga.Folio);
				
				if (cantidadTransanccion != null)
				{
					if (!foliosTransaccionValidados.contains(carga.Folio))
					{
						if (cantidadCarga.compareTo(cantidadTransanccion) > 0)
						{					
							HashMap<String,String> paramMsj = new HashMap<String,String>();
							paramMsj.put("FolioTransaccion", carga.Folio);				
							SalidaValidacion.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "CANTIDAD_ASINAR_SUPERA_DISPONIBLE", paramMsj, con, idEmpresa));
						}
						foliosTransaccionValidados.add(carga.Folio);
					}
				}

			}
			

		} catch (Exception e) {
			e.printStackTrace();
		}
		return SalidaValidacion;
	}

	/******************************************************************************
	 *                          CALL
	 ******************************************************************************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();
		EntradaCargaMasivaFacturacion in =parse(jin.toString());
		SalidaCargaMasivaFacturacion out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	/******************************************************************************
	 * 							MAIN
	 ******************************************************************************/
	public static void main(String[] args){
		System.out.println("COMIENZA");
		try (Connection con = PlataformaUtils.getDESAConnection())
		{
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String jsonMalo  = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1020,\"p_IdModulo\":\"FAC\",\"p_UsaReglasNegocio\":\"S\",\"p_IdTipoCargaMasiva\":0,\"p_CodigoArchivo\":\"CARGA_REFACTURACION_JPM_2\",\"p_TipoCarga\":\"NEMTRA\",\"p_FechaCarga\":\"2020-07-13\",\"p_DetalleCargaMasiva\":[{\"FechaRegisro\":\"2020-07-13\",\"FechaTransaccion\":\"2020-07-13\",\"Folio\":800201,\"FolioFactura\":174132,\"Nemotecnico\":\"COPEC\",\"Tipo\":\"C\",\"Cantidad\":1500,\"DocumentoIdentidad\":\"6228021-2\",\"Portafolio\":\"0\",\"Comisiones\":\"000000000000000000000000000000006513\",\"Derechos\":\"000000000000000000000000000000000000\",\"Gastos\":\"000000000000000000000000000000000000\",\"Iva\":19,\"Identificador\":\"NEMTRA\",\"Precio\":5000},{\"FechaRegisro\":\"2020-07-13\",\"FechaTransaccion\":\"2020-07-13\",\"Folio\":800200,\"FolioFactura\":174132,\"Nemotecnico\":\"COPEC\",\"Tipo\":\"C\",\"Cantidad\":1000,\"DocumentoIdentidad\":\"10660923-3\",\"Portafolio\":\"0\",\"Comisiones\":\"000000000000000000000000000000006513\",\"Derechos\":\"000000000000000000000000000000000000\",\"Gastos\":\"000000000000000000000000000000000000\",\"Iva\":19,\"Identificador\":\"NEMTRA\",\"Precio\":5000}],\"p_DetalleTransacciones\":[{\"CantidadDisponibleTransaccion\":1500,\"CantidadTransaccion\":1500,\"EsFungible\":\"N\",\"EsFungibleUI\":\"NO\",\"EsVentaCorta\":\"N\",\"EsVentaCortaUI\":\"NO\",\"FolioTransaccion\":\"800201\",\"FechaSistemaCreacion\":\"2020-07-13 00:00:00.000\",\"FechaOperacionTransaccion\":\"2020-07-13 17:01:31.000\",\"FechaVencimiento\":\"0001-01-01 00:00:00.000\",\"HoraSistemaCreacion\":\"05:01:31 PM\",\"IdBolsa\":\"BCS\",\"IdCondicionDeLiquidacion\":\"T0\",\"IdClasificacionTipoInstrumento\":\"RV\",\"IdEmisor\":\"COPEC\",\"IdInstrumento\":502,\"IdMonedaTransaccion\":\"CLP\",\"IdTipoOperacion\":\"COMACN\",\"IdTipoProducto\":\"INAC\",\"IdTipoReajuste\":\"\",\"IdTransaccion\":93013,\"IndicadorOrdenDirecta\":\"\",\"InstrumentoUI\":\"COPEC\",\"MontoTransaccion\":9460500,\"NemotecnicoSVS\":\"\",\"NombreTipoOperacion\":\"COMPRA ACCIONES NACIONALES\",\"NombreTipoInstrumento\":\"ACCIONES NACIONALES\",\"NombreClasificacionTipoInstrumento\":\"RENTA VARIABLE\",\"NombreCondicionDeLiquidacion\":\"Pagadero hoy\",\"NombreCorredorContraparte\":\"SECURITY  \",\"PrecioTransaccion\":6307,\"Tasa\":0},{\"CantidadDisponibleTransaccion\":1000,\"CantidadTransaccion\":1000,\"EsFungible\":\"N\",\"EsFungibleUI\":\"NO\",\"EsVentaCorta\":\"N\",\"EsVentaCortaUI\":\"NO\",\"FolioTransaccion\":\"800200\",\"FechaSistemaCreacion\":\"2020-07-13 00:00:00.000\",\"FechaOperacionTransaccion\":\"2020-07-13 16:58:55.000\",\"FechaVencimiento\":\"0001-01-01 00:00:00.000\",\"HoraSistemaCreacion\":\"04:58:55 PM\",\"IdBolsa\":\"BCS\",\"IdCondicionDeLiquidacion\":\"T0\",\"IdClasificacionTipoInstrumento\":\"RV\",\"IdEmisor\":\"COPEC\",\"IdInstrumento\":502,\"IdMonedaTransaccion\":\"CLP\",\"IdTipoOperacion\":\"COMACN\",\"IdTipoProducto\":\"INAC\",\"IdTipoReajuste\":\"\",\"IdTransaccion\":93012,\"IndicadorOrdenDirecta\":\"\",\"InstrumentoUI\":\"COPEC\",\"MontoTransaccion\":6307000,\"NemotecnicoSVS\":\"\",\"NombreTipoOperacion\":\"COMPRA ACCIONES NACIONALES\",\"NombreTipoInstrumento\":\"ACCIONES NACIONALES\",\"NombreClasificacionTipoInstrumento\":\"RENTA VARIABLE\",\"NombreCondicionDeLiquidacion\":\"Pagadero hoy\",\"NombreCorredorContraparte\":\"SECURITY  \",\"PrecioTransaccion\":6307,\"Tasa\":0}]}";
			String jsonBueno = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1020,\"p_IdModulo\":\"FAC\",\"p_UsaReglasNegocio\":\"S\",\"p_IdTipoCargaMasiva\":0,\"p_CodigoArchivo\":\"CARGA_REFACTURACION_JPM_2\",\"p_TipoCarga\":\"NEMTRA\",\"p_FechaCarga\":\"2020-07-13\",\"p_DetalleCargaMasiva\":[{\"FechaRegisro\":\"2020-07-13\",\"FechaTransaccion\":\"2020-07-13\",\"Folio\":700203,\"FolioFactura\":174126,\"Nemotecnico\":\"ENTEL\",\"Tipo\":\"C\",\"Cantidad\":1000,\"DocumentoIdentidad\":\"6228021-2\",\"Portafolio\":\"0\",\"Comisiones\":\"000000000000000000000000000000006513\",\"Derechos\":\"000000000000000000000000000000000000\",\"Gastos\":\"000000000000000000000000000000000000\",\"Iva\":19,\"Identificador\":\"NEMTRA\",\"Precio\":40400000}],\"p_DetalleTransacciones\":[{\"CantidadDisponibleTransaccion\":1000,\"CantidadTransaccion\":1000,\"EsFungible\":\"N\",\"EsFungibleUI\":\"NO\",\"EsVentaCorta\":\"N\",\"EsVentaCortaUI\":\"NO\",\"FolioTransaccion\":\"700203\",\"FechaSistemaCreacion\":\"2020-07-13 00:00:00.000\",\"FechaOperacionTransaccion\":\"2020-07-13 20:57:22.000\",\"FechaVencimiento\":\"0001-01-01 00:00:00.000\",\"HoraSistemaCreacion\":\"08:57:22 PM\",\"IdBolsa\":\"BCS\",\"IdCondicionDeLiquidacion\":\"T0\",\"IdClasificacionTipoInstrumento\":\"RV\",\"IdEmisor\":\"ENTEL\",\"IdInstrumento\":764,\"IdMonedaTransaccion\":\"CLP\",\"IdTipoOperacion\":\"COMACN\",\"IdTipoProducto\":\"INAC\",\"IdTipoReajuste\":\"\",\"IdTransaccion\":93009,\"IndicadorOrdenDirecta\":\"\",\"InstrumentoUI\":\"ENTEL\",\"MontoTransaccion\":5679000,\"NemotecnicoSVS\":\"\",\"NombreTipoOperacion\":\"COMPRA ACCIONES NACIONALES\",\"NombreTipoInstrumento\":\"ACCIONES NACIONALES\",\"NombreClasificacionTipoInstrumento\":\"RENTA VARIABLE\",\"NombreCondicionDeLiquidacion\":\"Pagadero hoy\",\"NombreCorredorContraparte\":\"SECURITY  \",\"PrecioTransaccion\":5679,\"Tasa\":0}]}";
			EntradaCargaMasivaFacturacion entrada = gson.fromJson(jsonMalo, EntradaCargaMasivaFacturacion.class);
			CrearCargaMasivaRefacturacion demo = new CrearCargaMasivaRefacturacion();
			SalidaCargaMasivaFacturacion salida = demo.logica(con, entrada);
			System.out.println(salida);
			con.rollback();
			System.out.println("FIN");
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	
	public static EntradaCargaMasivaFacturacion parse(String targetStream) throws JSONException{
	        ObjectMapper obje= new ObjectMapper();
	        EntradaCargaMasivaFacturacion map = new EntradaCargaMasivaFacturacion();
			try {
				map = obje.readValue(targetStream, EntradaCargaMasivaFacturacion.class);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	       
	        
		
			return map;
		
		
	}
	
}
