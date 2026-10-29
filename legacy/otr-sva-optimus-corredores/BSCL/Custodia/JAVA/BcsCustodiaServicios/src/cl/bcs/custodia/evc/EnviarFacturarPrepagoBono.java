/***************************************************************************
* Purpose	: Envia a facturar eventos de capital VEPRBO (venta por prepago bono)
* Author	: JVILLAGRAN - BCS
* Date		: 23-04-2018
**************************************************************************/

package cl.bcs.custodia.evc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.evc.InscribirClientePrepagoBono.CarteraBonoCliente;
import cl.bcs.custodia.evc.InscribirClientePrepagoBono.ClientePrepagoBono_Proceso;
import cl.bcs.custodia.evc.InscribirClientePrepagoBono.EventoCapital;
import cl.bcs.custodia.evc.InscribirClientePrepagoBono.EventoCapitalCliente_Entrada;
import cl.bcs.facturacion.CrearMovimientoComprobante;
import cl.bcs.facturacion.CrearMovimientoComprobante.CrearMovimientoComprobante_Entrada;
import cl.bcs.facturacion.CrearMovimientoComprobante.CrearMovimientoComprobante_Salida;
import cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobanteFormaDePago_Entrada;
import cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobante_Entrada;
import cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobante_Salida;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EnviarFacturarPrepagoBono implements JavaCall {
	
	private final Log log = new Log("CUS.EnviarFacturarPrepagoBono");
	
	private final String SQL_SELECT_EVENTO_CAPITAL_CLIENTE = loadResourceToString("cl/bcs/custodia/sql/EnviarFacturarPrepagoBono_SelectEventoCapitalCliente.sql");	
	private final String SQL_UPDATE_EVENTO_CAPITAL_CLIENTE = loadResourceToString("cl/bcs/custodia/sql/EnviarFacturarPrepagoBono_UpdateEventoCapitalCliente.sql");
	private final String SQL_SELECT_PREFERENCIA_PAGO_PORTAFOLIO = loadResourceToString("cl/bcs/custodia/sql/EnviarFacturarPrepagoBono_SelectPreferenciaPagoPortafolio.sql");
	                                                                                    
	
    //-------------------------------------------------------------------------------------------
	// CLASES QUE REPRESENTAN OBJETOS DE ENTRADA
	
	public static class EnviarFacturarPrepagoBono_Entrada
	{
		public Long 			p_IdEmpresa;
		public Long 			p_IdUsuario;
		public String 			p_IdModulo;
		public Long 			p_IdEventoCapital;
		public List<EventoCapitalCliente_Entrada> p_EventoCapitalCliente;  // EC de clientes a facturar  
		
		
		public EnviarFacturarPrepagoBono_Entrada()
		{
			this.p_EventoCapitalCliente = new ArrayList<EventoCapitalCliente_Entrada>();
		}
	    
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EventoCapitalCliente_Entrada
	{
		public Long p_IdEventoCapitalCliente;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
    //-------------------------------------------------------------------------------------------
	// CLASES QUE REPRESENTAN OBJETOS DE SALIDA
	
    public static class EnviarFacturarPrepagoBono_Salida
    {
    	public List<MensajeSalida> 		P_OUT_MENSAJES;
    	public Long 					P_OUT_ERRORESCONTADOR;
    	   
    	public EnviarFacturarPrepagoBono_Salida()
    	{
    		this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
    	}
    	
    	public String toString() {
    		return Utils.printJson(this, false);
    	}
    }
    
	
    //-------------------------------------------------------------------------------------------
	// CLASES UTILITARIAS DE APOYO	
	                    
	public static class EventoCapitalCliente_Proceso
	{
		public Long 		IdEventoCapital;
		public Long 		IdEventoCapitalCliente;
		public String 		IdTipoEventoCapital;  //Ej.: PB / SU
		public String 		IdEstado;
		public Long     	IdEmpresa;
		public Long     	IdMovimientoComprobanteFac;
		
		public Long     	IdCliente; 	
		public Long     	IdPortafolio;
		public Long     	IdInstrumento;
		public String 		IdEmisor;
		public String 		IdMoneda;
		public String 		IdBoveda;
		public String 		IdBolsa;
		public BigDecimal   CantidadNueva;
		public BigDecimal   PrecioNuevo;
		public BigDecimal   MontoInteres;
		public BigDecimal   Total;
		public Long     	IdCuentaCustodioPortafolio;
	
			
		//Para validaciones
		public long 		errores 		= 0;
		public long 		advertencias 	= 0;
		public long 		autorizaciones 	= 0;
		public long 		ok 				= 0;
	}

   	
	public static class PreferenciaPagoPortafolio
	{
		public String Llave;
		public Long IdPortafolio;
		public String IdTipoOperacion;
		public Long IdCuentaBancaria;
		public Long IdBancoCuentaBancaria;
		public String NumeroCuentaBancaria;
		public String IdTipoCuentaBancaria;
		public String NombreTipoCuentaBancaria;
		public String IdMedioDePago;
		public String IdMoneda;
		public Long IdTarjetaBancaria;
		public String IdTipoTarjetaBancaria;
		public String NumeroTarjetaBancaria;
		
		
    	public String toString() {
    		return Utils.printJson(this, false);
    	}
	}
	
	
	public static class EventoCapitalCliente
	{
		public Long 		IdEventoCapital;
		public Long 		IdEventoCapitalCliente;
		public String 		IdTipoEventoCapital;  //Ej.: PB / SU
		public String 		IdEstado;
		public Long     	IdEmpresa;
		public Long     	IdMovimientoComprobanteFac;
		public Long     	IdCliente;
		public Long     	IdPortafolio;
		public Long     	IdInstrumento;
		public String 		IdEmisor;
		public String 		IdMoneda;
		public String 		IdBoveda;
		public String 		IdBolsa;
		public BigDecimal   CantidadNueva;
		public BigDecimal   PrecioNuevo;
		public BigDecimal   MontoInteres;
		public BigDecimal   Total;
		public Long     	IdCuentaCustodioPortafolio;

	    public String toString() {
	    		return Utils.printJson(this, false);
	    }
	}
	
	

    //-------------------------------------------------------------------------------------------
	// METODOS UTILITARIOS	
	
	//Procesa salida de validaciones
	private void ProcesaRequisito (Connection con, Long idEmpresa, boolean esError, String codMsg, String CodRequisito, EventoCapitalCliente_Proceso fpb, List<MensajeSalida> P_OUT_MENSAJESS)
			throws Exception {
		if (esError) 
		{
			String msg=Diccionario.getMensaje(con, idEmpresa, codMsg)					
					.add("IdEventoCapitalCliente", fpb.IdEventoCapitalCliente)
				    .msg();			
			 
			if (CodRequisito.equals(MensajeSalida.MENSAJE_ERROR)) 
			{
				P_OUT_MENSAJESS.add(new MensajeSalida(fpb.IdEventoCapitalCliente,MensajeSalida.MENSAJE_ERROR,codMsg,msg));
				fpb.errores++;
			}
			else if (CodRequisito.equals(MensajeSalida.MENSAJE_AUTORIZACION)) 
			{
				P_OUT_MENSAJESS.add(new MensajeSalida(fpb.IdEventoCapitalCliente,MensajeSalida.MENSAJE_AUTORIZACION,codMsg,msg));
				fpb.autorizaciones++;						
			}
			else if (CodRequisito.equals(MensajeSalida.MENSAJE_ADVERTENCIA)) 
			{
				P_OUT_MENSAJESS.add(new MensajeSalida(fpb.IdEventoCapitalCliente,MensajeSalida.MENSAJE_ADVERTENCIA,codMsg,msg));
				fpb.advertencias++;
			}
			else 
			{
				fpb.ok++;
			}
		}
		else 
		{
			fpb.ok++;
		}		
	}
	
	

	//-------------------------------------------------------------------------------------------
	// LOGICA DEL SERVICIO
	
	public EnviarFacturarPrepagoBono_Salida logica(Connection conn, EnviarFacturarPrepagoBono_Entrada entrada) throws Exception 
	{
		EnviarFacturarPrepagoBono_Salida salida = new EnviarFacturarPrepagoBono_Salida();
		salida.P_OUT_ERRORESCONTADOR = 0L;
		
		SQL sql = new SQL();		
		log.write("== INICIO ENVIO A FACTURAR PREPAGO BONO ==");
		
		try 
		{
			log.write("PARAMETROS DE ENTRADA : " + entrada.toString());
						
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			
			if (entrada.p_EventoCapitalCliente.size() == 0) 
			{
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_ESPECIFICAN_EVENTOS_CAPITAL_A_PROCESAR",null,conn,entrada.p_IdEmpresa));				
				return salida;
			}
			
			
			//---------------------------------------------------------------------------------------------
			//VALIDA SI CUMPLE REQUISITOS BASICOS
			RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
			RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(conn, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
			if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) 
			{
				log.write("NO CUMPLE REQUISITOS BASICOS");
				salida.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
				return salida;
			}

			
			
			
			//---------------------------------------------------------------------------------------------
			// OBTIENE DATOS DEL EVENTO CAPITAL CLIENTE
			
			HashMap<Long, EventoCapitalCliente> datosEventoCapitalCliente = new HashMap<>();
			{
				HashMap<String,Object> paramSelectEventoCapitalCliente = new HashMap<>();			
				List<Long> _idEventoCapitalCliente = new ArrayList<>();
				
				for (EventoCapitalCliente_Entrada _ecce : entrada.p_EventoCapitalCliente)
				{
					_idEventoCapitalCliente.add(_ecce.p_IdEventoCapitalCliente);
				}
				
				paramSelectEventoCapitalCliente.put("IdEventoCapitalCliente", _idEventoCapitalCliente);
				List<EventoCapitalCliente> resultEventoCapitalCliente = sql.query(conn, SQL_SELECT_EVENTO_CAPITAL_CLIENTE, paramSelectEventoCapitalCliente, EventoCapitalCliente.class);    //Obtiene datos extras del evento de capital
				
								
				HashMap<Object, Object> _datosEventoCapitalCliente = Utils.List2Hash(resultEventoCapitalCliente, "IdEventoCapitalCliente");  //Transforma la lista en Hash
				for(Object key: _datosEventoCapitalCliente.keySet())
				{
					datosEventoCapitalCliente.put((Long)key, (EventoCapitalCliente) _datosEventoCapitalCliente.get(key));
				}
				
				log.write("OBTIENE DEL EVENTO CAPITAL CLIENTE > " + resultEventoCapitalCliente);
			}
			
			
			
			
			//---------------------------------------------------------------------------------------------
			// OBTIENE PREFERENCIAS DE PAGO DEL PORTAFOLIO
			
			HashMap<String, PreferenciaPagoPortafolio> datosPreferenciaPagoPortafolio = new HashMap<>();
			{
				HashMap<String,Object> paramSelectPreferenciaPagoPortafolio = new HashMap<>();
				
				List<String> _llavePrePagPor = new ArrayList<>();
				
				for (EventoCapitalCliente_Entrada ecc_Entrada : entrada.p_EventoCapitalCliente)
				{	
					EventoCapitalCliente infoEventoCapitalCliente = (EventoCapitalCliente) datosEventoCapitalCliente.get(ecc_Entrada.p_IdEventoCapitalCliente);
					String llave = entrada.p_IdEmpresa.toString() + infoEventoCapitalCliente.IdPortafolio.toString() + infoEventoCapitalCliente.IdMoneda;					
					_llavePrePagPor.add(llave);
				}
				paramSelectPreferenciaPagoPortafolio.put("LlavePrePagPor", _llavePrePagPor);
				       
				List<PreferenciaPagoPortafolio> resultPreferenciaPagoPortafolio = sql.query(conn, SQL_SELECT_PREFERENCIA_PAGO_PORTAFOLIO, paramSelectPreferenciaPagoPortafolio, PreferenciaPagoPortafolio.class); 
				
				
				HashMap<Object, Object> _datosPreferenciaPagoPortafolio = Utils.List2Hash(resultPreferenciaPagoPortafolio, "Llave");  //Transforma la lista en Hash
				for(Object key: _datosPreferenciaPagoPortafolio.keySet())
				{
					datosPreferenciaPagoPortafolio.put((String)key, (PreferenciaPagoPortafolio) _datosPreferenciaPagoPortafolio.get(key));
				}
				
				log.write("OBTIENE PREFERENCIA PAGO PORTAFOLIO > " + resultPreferenciaPagoPortafolio);
			}
			
			
			
			//---------------------------------------------------------------------------------------------
			// INCORPORAR DATOS DE ENTRADA A DATOS PROCESO
			
			List<EventoCapitalCliente_Proceso> _EventoCapitalCliente_Proceso  = new ArrayList<EventoCapitalCliente_Proceso>();
			
			
			for (EventoCapitalCliente_Entrada ecc_entrada : entrada.p_EventoCapitalCliente)
			{	
				EventoCapitalCliente_Proceso ecc_Proceso = new EventoCapitalCliente_Proceso();
				
				Utils.copyPojo(ecc_entrada, ecc_Proceso);      //Copia los datos de entrada a proceso
				
				if (datosEventoCapitalCliente.size() > 0)
				{
					//Copia las datos extras de evento capital a proceso
					EventoCapitalCliente infoEventoCapital = (EventoCapitalCliente) datosEventoCapitalCliente.get(ecc_entrada.p_IdEventoCapitalCliente);
					Utils.copyPojo(infoEventoCapital, ecc_Proceso);
				}				
				_EventoCapitalCliente_Proceso.add(ecc_Proceso);
			}
			
			log.write("PROCESO DE DATOS DE ENTRADA: " + _EventoCapitalCliente_Proceso.toString());
			
		
			
			//---------------------------------------------------------------------------------------------			
			// VALIDACIONES
			log.write("VALIDACION DE EVENTOS DE CAPITAL A FACTURAR");
			for (EventoCapitalCliente_Proceso fpb: _EventoCapitalCliente_Proceso)	
			{	 
			    ProcesaRequisito(conn, entrada.p_IdEmpresa, (!fpb.IdEstado.equalsIgnoreCase("CRT") && !fpb.IdEstado.equalsIgnoreCase("ENV")),"EVENTO_CAPITAL_NO_SE_ENCUENTRA_CERTIFICADO", MensajeSalida.MENSAJE_ERROR, fpb, salida.P_OUT_MENSAJES);				
				ProcesaRequisito(conn, entrada.p_IdEmpresa, (fpb.IdEstado.equalsIgnoreCase("ENV")),"EVENTO_CAPITAL_SE_ENCUENTRA_FACTURADO", MensajeSalida.MENSAJE_ERROR, fpb, salida.P_OUT_MENSAJES);				
				ProcesaRequisito(conn, entrada.p_IdEmpresa, (!fpb.IdTipoEventoCapital.equalsIgnoreCase("PB")),"TIPO_EVENTO_CAPITAL_NO_SE_FACTURA", MensajeSalida.MENSAJE_ERROR, fpb, salida.P_OUT_MENSAJES);				
				ProcesaRequisito(conn, entrada.p_IdEmpresa, (fpb.CantidadNueva.compareTo(new BigDecimal(0)) == 0),"CANTIDAD_EVENTO_CAPITAL_INSUFICIENTE_PARA_FACTURAR", MensajeSalida.MENSAJE_ERROR, fpb, salida.P_OUT_MENSAJES);
			}
			
			if (MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES) > 0)
			{
				log.write("ENVIAR A FACTURAR PREPAGO BONO CON ERRORES DE VALIDACION");
				
				for (MensajeSalida mensaje : salida.P_OUT_MENSAJES)
				{
					log.write("ERROR VALIDACION > " + mensaje.descripcion);
				}
				
				return salida;
			}
			

			

			//---------------------------------------------------------------------------------------------			
			// ENVIA A FACTURAR EVENTOS DE CAPITAL DE PREPAGO BONO
			
			log.write("ENVIO DE EVENTO CAPITAL A FACTURACION");
			
			List<MovimientoComprobante_Entrada> MovimientoComprobante_Entrada =  new ArrayList<MovimientoComprobante_Entrada>();
			List<MovimientoComprobanteFormaDePago_Entrada> MovimientoComprobanteFormaDePago_Entrada =  new ArrayList<MovimientoComprobanteFormaDePago_Entrada>();
			
			int cont = 0;
			for (EventoCapitalCliente_Proceso fpb: _EventoCapitalCliente_Proceso)	
			{	
				
				// ----------------------------------------------------------------
				// DATOS GENERALES DE LOS MOVIMIENTOS FACTURACION
				MovimientoComprobante_Entrada _movimientoComprobante_Entrada =  new MovimientoComprobante_Entrada();
				{
					_movimientoComprobante_Entrada.NumeroMvtoCmbte = fpb.IdEventoCapitalCliente; //Se envía IdEventoCapitalCliente ya que después se usa para actualizar el EC facturado
					_movimientoComprobante_Entrada.IdEmpresa = entrada.p_IdEmpresa;
					_movimientoComprobante_Entrada.IdConcepto = "VEPRBO";    //VEPRBO: Venta Prepagpo Bono
					_movimientoComprobante_Entrada.SecuenciaAgrupacion = "0";
					_movimientoComprobante_Entrada.IdCliente = fpb.IdCliente;
					_movimientoComprobante_Entrada.IdPortafolio = fpb.IdPortafolio;
					_movimientoComprobante_Entrada.ModPrefFact = "";
					_movimientoComprobante_Entrada.IdEmpleadoDigitador = 0L;				
					_movimientoComprobante_Entrada.IdInstrumento = fpb.IdInstrumento;
					_movimientoComprobante_Entrada.IdInstrumentoRelacionado = 0L; 
					_movimientoComprobante_Entrada.IdInstrumentoSubyacente = 0L;
					_movimientoComprobante_Entrada.IdTipoProducto = "EVCA"; // EVCA: Evento Capital
					_movimientoComprobante_Entrada.IdMoneda = fpb.IdMoneda;            	
	            	_movimientoComprobante_Entrada.IdMonedaSubyacente = ""; 
	            	_movimientoComprobante_Entrada.IdMonedaLiquidacion = fpb.IdMoneda;
	            	_movimientoComprobante_Entrada.IdTipoComprobante = "POL";  // POL: Poliza
	            	_movimientoComprobante_Entrada.IdTipoOperacion = "VEPRBO";  // VEPRBO: Venta por Prepago Bono
	            	_movimientoComprobante_Entrada.IdBoveda = fpb.IdBoveda;
	            	_movimientoComprobante_Entrada.IdCorredoraContraparte = 0L;
	            	_movimientoComprobante_Entrada.IdCondicionLiquidacion = "T0";
	            	_movimientoComprobante_Entrada.FechaLiquidacion = fechaSistema;
	            	_movimientoComprobante_Entrada.IdBolsa = fpb.IdBolsa;
	            	_movimientoComprobante_Entrada.IdTipoReajuste = fpb.IdMoneda; 
	            	_movimientoComprobante_Entrada.IdTipoConvenio = "SCV";
	            	_movimientoComprobante_Entrada.IdFormaCustodia = "CUS";            	
	            	_movimientoComprobante_Entrada.IdEmisor = fpb.IdEmisor;
	            	_movimientoComprobante_Entrada.IdOrden = 0L;
	            	_movimientoComprobante_Entrada.IdAsignacion = 0L;
	            	_movimientoComprobante_Entrada.IdAsignacionRef = 0L;
	            	_movimientoComprobante_Entrada.IdTipoCanalIngreso = "SIS";
	            	_movimientoComprobante_Entrada.FechaOrden = "0001-01-01";
	            	_movimientoComprobante_Entrada.IdTransaccion = 0L;
	            	_movimientoComprobante_Entrada.FolioTransaccion = "0";
	            	_movimientoComprobante_Entrada.FechaTransaccion = "0001-01-01";
	            	_movimientoComprobante_Entrada.FechaVencimiento = "0001-01-01";
	            	_movimientoComprobante_Entrada.PrecioAPlazo = BigDecimal.ZERO;
	            	_movimientoComprobante_Entrada.MontoAPlazo = BigDecimal.ZERO;
	            	_movimientoComprobante_Entrada.FechaParidad = "0001-01-01";
	            	_movimientoComprobante_Entrada.ValorParidad = BigDecimal.ZERO;
	            	_movimientoComprobante_Entrada.FechaTipoCambio = "0001-01-01";
	            	_movimientoComprobante_Entrada.ValorTipoCambio = BigDecimal.ZERO;
	            	_movimientoComprobante_Entrada.ValorCaptacionMonedaOrigen = BigDecimal.ZERO;
	            	_movimientoComprobante_Entrada.Tasa = BigDecimal.ZERO;
	            	           	
	 				_movimientoComprobante_Entrada.Cantidad = fpb.CantidadNueva; 			////EvCapCli."CantidadNueva"	as "Cantidad"
	 				_movimientoComprobante_Entrada.Precio = fpb.PrecioNuevo; 				////EvCap."Valor" 				as "Precio"
	 				_movimientoComprobante_Entrada.Monto = fpb.Total; 						////(EvCapCli."CantidadNueva" * EvCap."Valor") as "Monto",
	 				_movimientoComprobante_Entrada.MontoMovimientoComprobante = fpb.Total;	////(EvCapCli."CantidadNueva" * EvCap."Valor")	as "MontoMovimientoComprobante",
	 				_movimientoComprobante_Entrada.MontoLiquidacion = fpb.Total; 			////(EvCapCli."CantidadNueva" * EvCap."Valor")	as "MontoLiquidacion",
	 				
	            	_movimientoComprobante_Entrada.MontoDescuento = BigDecimal.ZERO;
	            	_movimientoComprobante_Entrada.MontoInteres = fpb.MontoInteres;            	
	            	_movimientoComprobante_Entrada.Glosa = "";
	            	_movimientoComprobante_Entrada.ValorRescate = BigDecimal.ZERO;
	            	_movimientoComprobante_Entrada.TasaAgente = BigDecimal.ZERO;
	            	_movimientoComprobante_Entrada.Spread = BigDecimal.ZERO;
	            	_movimientoComprobante_Entrada.DiasPlazo = BigDecimal.ZERO;
	            	_movimientoComprobante_Entrada.FechaMovimientoComprobante = fechaSistema;
	            	_movimientoComprobante_Entrada.FolioCartera = 0L;
	            	_movimientoComprobante_Entrada.FechaEmision = fechaSistema;
	            	_movimientoComprobante_Entrada.TasaEmision = BigDecimal.ZERO;
					_movimientoComprobante_Entrada.EsComprobIndividual = "N";
					_movimientoComprobante_Entrada.EsCuentaPropia = "N";
					_movimientoComprobante_Entrada.IndicadorOrdenDirecta = "";
					_movimientoComprobante_Entrada.EsVentaCorta = "N";
					_movimientoComprobante_Entrada.EsPrecioPrima = "N";
					_movimientoComprobante_Entrada.EsEnRueda = "N";
					_movimientoComprobante_Entrada.EsRuteo = "N";
					_movimientoComprobante_Entrada.EsAPlazo = "N";
					_movimientoComprobante_Entrada.EsFungible = "N";
					_movimientoComprobante_Entrada.ActualizarCustodia = "S";
					_movimientoComprobante_Entrada.ActualizarCuentaInversion = "S";
					_movimientoComprobante_Entrada.ActualizarTesoreria = "S";
					_movimientoComprobante_Entrada.ActualizarCustodiaRelacionado = "N";
					_movimientoComprobante_Entrada.CrearComprobanteAutomatico = "N";
	            	_movimientoComprobante_Entrada.IdUsuario = entrada.p_IdUsuario;
	            	_movimientoComprobante_Entrada.IdCuentaCustodioPortafolio = fpb.IdCuentaCustodioPortafolio;
	            	_movimientoComprobante_Entrada.IdOperacion = entrada.p_EventoCapitalCliente.get(cont).p_IdEventoCapitalCliente;
					MovimientoComprobante_Entrada.add(_movimientoComprobante_Entrada);
				}
				cont++;
				

				

				
				// -----------------------------------------------------------------------------------------
				// ASIGNA PREFERENCIAS DE PAGO PORTAFOLIO
				MovimientoComprobanteFormaDePago_Entrada _movimientoComprobanteFormaDePago_Entrada =  new MovimientoComprobanteFormaDePago_Entrada();
				{
					String llavePrePagPor = entrada.p_IdEmpresa.toString() + fpb.IdPortafolio.toString() + fpb.IdMoneda;
					PreferenciaPagoPortafolio infoPreferenciaPagoPortafolio = (PreferenciaPagoPortafolio) datosPreferenciaPagoPortafolio.get(llavePrePagPor);				

					//Si el cliente no tiene preferencia de pago no se asigna nada
					if (infoPreferenciaPagoPortafolio != null)
					{	
						_movimientoComprobanteFormaDePago_Entrada.NumeroMvtoCmbte = fpb.IdEventoCapitalCliente;
						_movimientoComprobanteFormaDePago_Entrada.NumeroMvtoCmbteFormaDePago = 1L;
						_movimientoComprobanteFormaDePago_Entrada.IdMedioDePago = infoPreferenciaPagoPortafolio.IdMedioDePago;
						_movimientoComprobanteFormaDePago_Entrada.NumeroSerie = "";
						_movimientoComprobanteFormaDePago_Entrada.NumeroDocumento = 0L;
						_movimientoComprobanteFormaDePago_Entrada.IdCuentaBancaria = infoPreferenciaPagoPortafolio.IdCuentaBancaria;
						_movimientoComprobanteFormaDePago_Entrada.IdTipoCuentaBancaria = infoPreferenciaPagoPortafolio.IdTipoCuentaBancaria;
						_movimientoComprobanteFormaDePago_Entrada.NumeroCuentaBancaria = infoPreferenciaPagoPortafolio.NumeroCuentaBancaria;
						_movimientoComprobanteFormaDePago_Entrada.IdTarjetaBancaria = infoPreferenciaPagoPortafolio.IdTarjetaBancaria;
						_movimientoComprobanteFormaDePago_Entrada.IdTipoTarjetaBancaria = infoPreferenciaPagoPortafolio.IdTipoTarjetaBancaria;
						_movimientoComprobanteFormaDePago_Entrada.NumeroTarjetaBancaria = infoPreferenciaPagoPortafolio.NumeroTarjetaBancaria;
						_movimientoComprobanteFormaDePago_Entrada.FechaVctoTarjetaBancaria = "0001-01-01";
						_movimientoComprobanteFormaDePago_Entrada.IdModalidadDePago = "MODDEP";
						_movimientoComprobanteFormaDePago_Entrada.IdBanco = 0L;
						_movimientoComprobanteFormaDePago_Entrada.IdMoneda = infoPreferenciaPagoPortafolio.IdMoneda;
						_movimientoComprobanteFormaDePago_Entrada.FechaDocumentoPago = "0001-01-01";
						_movimientoComprobanteFormaDePago_Entrada.MontoLiquidacionFormaDePago = fpb.Total;
						_movimientoComprobanteFormaDePago_Entrada.IdTipoMovimientoSpot = "";
						_movimientoComprobanteFormaDePago_Entrada.AfectarTesoreria = "S";
						
						MovimientoComprobanteFormaDePago_Entrada.add(_movimientoComprobanteFormaDePago_Entrada);
					}
				}
				
				
			}	
			
			
			
			//Define parámetros de entrada(generales) de crearMovimientoComprobante de Facturacion
			CrearMovimientoComprobante_Entrada crearMovimientoComprobante_Entrada = new CrearMovimientoComprobante_Entrada();
			
			crearMovimientoComprobante_Entrada.p_IdEmpresa = entrada.p_IdEmpresa;
			crearMovimientoComprobante_Entrada.p_IdUsuario = entrada.p_IdUsuario;
			crearMovimientoComprobante_Entrada.p_IdModulo = entrada.p_IdModulo;
			crearMovimientoComprobante_Entrada.p_simularOperacion = "N";
			crearMovimientoComprobante_Entrada.p_UsaReglasNegocio = "S";
			crearMovimientoComprobante_Entrada.p_EsIngresoAutomatico = "N";
			crearMovimientoComprobante_Entrada.p_Movimientos = MovimientoComprobante_Entrada;
			crearMovimientoComprobante_Entrada.p_MovimientosFormaDePago = MovimientoComprobanteFormaDePago_Entrada;
			
			
			//Llama a servicio CrearMovimientoComprobante de Facturacion
			CrearMovimientoComprobante servicioCrearMovimientoComprobante = new CrearMovimientoComprobante();
			CrearMovimientoComprobante_Salida salidaCrearMovimientoComprobante = servicioCrearMovimientoComprobante.logica(conn, crearMovimientoComprobante_Entrada);
			
			
			//Recorre y asigna mensajes de salida del crearMovimientoComprobante de facturacion
			for (MensajeSalida msj : salidaCrearMovimientoComprobante.P_OUT_MENSAJES) 
			{		
				salida.P_OUT_MENSAJES.add(msj);
			}

			
			//---------------------------------------------------------------------------------------------			
			// ACTUALIZACION DE EVENTOS DE CAPITAL A FACTURADO
			
			List<EventoCapitalCliente_Proceso> eccFacturado = new ArrayList<EventoCapitalCliente_Proceso>();
			
			//Obtiene los folios de los movimientos creados ok
			for (MovimientoComprobante_Salida movto : salidaCrearMovimientoComprobante.p_out_Movimientos) 
			{	
				EventoCapitalCliente_Proceso _eccFacturado = new EventoCapitalCliente_Proceso();
				_eccFacturado.IdEventoCapitalCliente = movto.NumeroMvtoCmbte;
				_eccFacturado.IdMovimientoComprobanteFac = movto.IdMovimientoComprobante;
				_eccFacturado.IdEstado =  "ENV";    //Enviado a Facturación
				
				eccFacturado.add(_eccFacturado);
			}
			
			if (eccFacturado.size() > 0)
			{
				log.write("ACTUALIZACION DE EVENTOS DE CAPITAL A FACTURADO");
				sql.batch(conn, SQL_UPDATE_EVENTO_CAPITAL_CLIENTE, eccFacturado , EventoCapitalCliente_Proceso.class);
			}
			
			
			
		
		}
		catch(Exception e)
		{	
			log.write("ERROR > ", e);
		}
		
		log.write("SALIDA SERVICIO : " + salida.toString());
		log.write("== TERMINO ENVIO A FACTURAR PREPAGO BONO ==");
		
		return salida;
	}


	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();
		//Entrada in = gson.fromJson(jin.toString(), Entrada.class);
		EnviarFacturarPrepagoBono_Entrada in = gson.fromJson(jin.toString(), EnviarFacturarPrepagoBono_Entrada.class);
				
		EnviarFacturarPrepagoBono_Salida out = logica(conn,in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();	
		return dataout;
	}
	
	
	//-------------------------------------------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	
	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBA LOCAL ==");

		//try (Connection con = PlataformaUtils.getDESAPEConnection())
		try (Connection con = PlataformaUtils.getMBPEConnection())
		{
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":69,\"p_IdModulo\":\"CUS\",\"p_EventoCapitalCliente\": [{\"IdInterno\":1,\"IdEventoCapital\":162,\"IdCliente\":174993,\"IdPortafolio\":40738, \"IdClasificacionTipoInstrumento\": \"RF\", \"IdTipoInstrumento\": \"BON\", \"IdInstrumento\": 177542, \"CantidadNueva\":100.00,\"PrecioNuevo\":200.00,\"MontoCapital\"=111.11,\"MontoInteres\"=222.22,\"MontoReajuste\"=333.33,\"Total\"=444.44}]}";
			String json ="{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CUS\",\"p_IdEventoCapital\":0,\"p_EventoCapitalCliente\":[{\"p_IdEventoCapitalCliente\":35747}]}";

			EnviarFacturarPrepagoBono_Entrada Objin = gson.fromJson(json, EnviarFacturarPrepagoBono_Entrada.class);
			
			EnviarFacturarPrepagoBono demo = new EnviarFacturarPrepagoBono();
			
			System.out.println("ENTRADA:");
			System.out.println(Objin.toString());
			
			
			try 
			{
				EnviarFacturarPrepagoBono_Salida ObjOut= demo.logica(con, Objin);
				//con.commit();
				
				System.out.println("SALIDA:");
				ObjOut.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(ObjOut.P_OUT_MENSAJES);
				System.out.println(ObjOut.toString());
			} catch(Exception er) {
				//er.printStackTrace();	
				con.rollback();
			}
		}	
		System.out.println("== FIN PRUEBA LOCAL ==");
    }
	
	

}
