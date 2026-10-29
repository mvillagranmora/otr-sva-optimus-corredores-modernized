package cl.bcs.custodia.sim;


import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.CrearCargoAbono;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoEntrada;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoSalida;
import cl.bcs.cuentaInversion.CrearCargoAbono.ObjCargoAbonoAGenerar;
import cl.bcs.custodia.sim.NotificarAnticipoSimultanea.EntradaNotificarAnticipoSimultanea;
import cl.bcs.custodia.utils.CacheValorizacion;
import cl.bcs.custodia.utils.IngresoCustodia.ObjTablaCartera;
import cl.bcs.custodia.utils.ValorizarCustodia;
import cl.bcs.facturacion.CrearMovimientoComprobante;
import cl.bcs.facturacion.CrearMovimientoComprobante.CrearMovimientoComprobante_Entrada;
import cl.bcs.facturacion.CrearMovimientoComprobante.CrearMovimientoComprobante_Salida;
import cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobante_Entrada;
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

public class AnticipoSimultanea implements JavaCall{
	
	// Log
	private final Log log = new Log("CUS.AnticipoSimultanea");

	// Queries
	private final String SQL_INSERT_ANTICIPO_SIMUTANEA_PROCESO     = loadResourceToString("cl/bcs/custodia/sql/AnticipoSimultanea_InsertAnticipoSimultaneaProceso.sql");
	private final String SQL_INSERT_ANTICIPO_SIMUTANEA_OD_PROCESO  = loadResourceToString("cl/bcs/custodia/sql/AnticipoSimultanea_InsertAnticipoSimultaneaODProceso.sql");
	private final String SQL_SELECT_ANTICIPO_SIMUTANEA_PROCESO     = loadResourceToString("cl/bcs/custodia/sql/AnticipoSimultanea_SelectAnticipoSimultaneaProceso.sql");
	private final String SQL_SELECT_CARTERA_SIMUTANEA			   = loadResourceToString("cl/bcs/custodia/sql/AnticipoSimultanea_SelectCarteraSimultanea.sql");
	

	//-------------------------------------------------
	// CLASES DE ENTRADA 
	//-------------------------------------------------

	public static class AnticipoSimultanea_Entrada
	{
		public Long       		p_IdEmpresa;
		public Long       		p_IdUsuario;
		public String     		p_IdModulo;
		public String     		p_UsaReglasNegocio;
		public Long     		p_FolioCartera;
		public Long     		p_IdPais;
		public String     		p_FechaSistema;
		public String     		p_IdCondicionLiquidacion; 
		public String     		p_FechaCondicionLiquidacion;
	    public BigDecimal 		p_CantidadAnticipo;
		public BigDecimal 		p_TasaAnticipo;
		public BigDecimal 		p_MontoAnticipo;
		public Long     		p_PlazoOriginal = 0L;
		public Long     		p_PlazoResidual = 0L;
		public Long     		p_IdEmpleado = 0L;
				
		public String toString() {
			return Utils.printJson(this, false);
		}
	} 
	
	
	
	//-------------------------------------------------
	// CLASES DE SALIDA 
	//-------------------------------------------------

	public static class AnticipoSimultanea_Salida
	{	
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		
		public AnticipoSimultanea_Salida()
		{
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES	 =  new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	
	
	//-------------------------------------------------
	// CLASES UTILITARIAS 
	//-----------------------------------------------
	

	
	public static class AnticipoSimultanea_Proceso
	{				
		public String     		EsSimultaneaEnviada; 
		public Long     		IdCartera;
		public Long     		FolioCartera;
		public Long     		FolioCarteraOD;
		public Long     		IdCliente;
		public Long     		IdPortafolio;
		public Long     		IdInstrumento;
		public String     		IdTipoProducto;
		public String     		IdMonedaSistema;
		public String     		IdBoveda;
		public String     		IdBolsa;
		public java.sql.Date    FechaOperacion;
		public String     		FolioExternoReferencia;
		public String     		IdConcepto;
		public String     		IdTipoOperacion;
		public String     		IndicadorOrdenDirecta;
		
		public java.sql.Date    FechaVencimientoOperacion;
		public java.sql.Date    FechaLiquidacion;
		public BigDecimal       INGCantidad;
		public String           IdEmisor;
		public BigDecimal		INGMontoAPlazo;
		
		public BigDecimal		PrecioAPlazo;
		
		
		public Long				IdAgente;
		public Long				IdSucursal;
		public String			IdTipoInstrumento;
		public String			Nemotecnico;
		//Datos Subyecente
		public Long     		IdInstrumentoSubyacente;
		public String     		IdMonedaInstrumentoSubyacente;
		public String     		IdTipoReajusteSubyacente;
		public String           IdEmisorSubyacente;
		public String           NemotecnicoSubyacente;
		

		public Long				IdComprobanteReferencia;
		
		
		//Para validaciones
		public long 				errores 		= 0;
		public long 				advertencias 	= 0;
		public long 				autorizaciones 	= 0;
		public long 				ok 				= 0;		
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	

	
	//-------------------------------------------------
	// LOGICA DEL SERVICIO 
	//-------------------------------------------------	
	public AnticipoSimultanea_Salida logica(Connection con, AnticipoSimultanea_Entrada entrada) throws Exception {
		
		SQL sql = new SQL();
		AnticipoSimultanea_Salida salida = new AnticipoSimultanea_Salida();
		
		ArrayList<AnticipoSimultanea_Proceso> datosProcesoAnticipoSimultaneaConErrores = new ArrayList<AnticipoSimultanea_Proceso>();
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		String IdCondicionParam = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "CUSTODIA", "TipoComprobanteAnticipoSimultanea");
		log.write("== INICIO EJECUCION ANTICIPO SIMULTANEA ==");
		
			
		//---------------------------------------------------------------------------------------------
		//VALIDA SI CUMPLE REQUISITOS BASICOS			
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) 
		{
			log.write("NO CUMPLE REQUISITOS BASICOS");
			salida.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			return salida;
		}
		
		
		
		//---------------------------------------------------------------------------------------------
		// INSERTA DATOS DE CARTERA A TABLA GLOBAL
		
		List<AnticipoSimultanea_Proceso> paramsProceso = new ArrayList<>();
		AnticipoSimultanea_Proceso arg1 = new AnticipoSimultanea_Proceso();
		arg1.FolioCartera = entrada.p_FolioCartera;
		paramsProceso.add(arg1);
		
	    sql.batch(con, SQL_INSERT_ANTICIPO_SIMUTANEA_PROCESO, paramsProceso , AnticipoSimultanea_Proceso.class);
	    
		
		//---------------------------------------------------------------------------------------------
		// INSERTA DATOS DE CARTERA(OD) A TABLA GLOBAL
		//List<AnticipoSimultanea_Proceso> paramsVacio = new ArrayList<>();
	    // *** Se solicita excluir la OD del proceso de anticipo ***
		//sql.batch(con, SQL_INSERT_ANTICIPO_SIMUTANEA_OD_PROCESO, paramsVacio , AnticipoSimultanea_Proceso.class);

		
		
		//---------------------------------------------------------------------------------------------
		// OBTIENE DATOS DE PROCESO
		HashMap<String,Object> paramsVacioHash = new HashMap<String,Object>();
		
		List<AnticipoSimultanea_Proceso> datosProcesoAnticipoSimultanea = sql.query(con, SQL_SELECT_ANTICIPO_SIMUTANEA_PROCESO, paramsVacioHash, AnticipoSimultanea_Proceso.class);
		
		if (datosProcesoAnticipoSimultanea.size() == 0)
		{
			log.write("NO SE ENCONTRARON DATOS PARA PROCESAR");
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "NO_SE_ENCONTRARON_DATOS_PARA_PROCESAR", null, con, entrada.p_IdEmpresa));				
			return salida;
		}
		else
		{	
			log.write("DATOS PROCESO > " + datosProcesoAnticipoSimultanea);
		}

 
		
		//---------------------------------------------------------------------------------------
		//VALIDACIONES DE DATOS 
		{	
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); /*111*/
			java.sql.Date sqlFechaSistema = new java.sql.Date(dateFormat.parse(entrada.p_FechaSistema).getTime());			
			java.sql.Date sqlFechaCondicionLiquidacion = new java.sql.Date(dateFormat.parse(entrada.p_FechaCondicionLiquidacion).getTime());
			
			for (AnticipoSimultanea_Proceso proAntSim: datosProcesoAnticipoSimultanea)	
			{				
				Calendar c = Calendar.getInstance();
			    c.setTime(proAntSim.FechaLiquidacion);
			    c.add(Calendar.DATE, 1); // suma 1 día			        
			    java.sql.Date sqlFechaAnticipoPermitida = new java.sql.Date(c.getTimeInMillis());			        
			        
			        
				ProcesaRequisito(con, entrada.p_IdEmpresa, (entrada.p_CantidadAnticipo.compareTo(new BigDecimal(1)) < 0), "CANTIDAD_MENOR_IGUAL_A_CERO", MensajeSalida.MENSAJE_ERROR, proAntSim, salida.P_OUT_MENSAJES);
				ProcesaRequisito(con, entrada.p_IdEmpresa, (entrada.p_MontoAnticipo.compareTo(new BigDecimal(1)) < 0), "MONTO_MENOR_IGUAL_A_CERO", MensajeSalida.MENSAJE_ERROR, proAntSim, salida.P_OUT_MENSAJES);
				ProcesaRequisito(con, entrada.p_IdEmpresa, (entrada.p_IdCondicionLiquidacion.equalsIgnoreCase("")), "CONDICION_LIQUIDACION_INVALIDO", MensajeSalida.MENSAJE_ERROR, proAntSim, salida.P_OUT_MENSAJES);								
				ProcesaRequisito(con, entrada.p_IdEmpresa, (sqlFechaSistema.compareTo(proAntSim.FechaVencimientoOperacion)) > 0, "FECHA_SISTEMA_MAYOR_VCTO_OPERACION", MensajeSalida.MENSAJE_ERROR, proAntSim, salida.P_OUT_MENSAJES);
				ProcesaRequisito(con, entrada.p_IdEmpresa, (sqlFechaSistema.compareTo(sqlFechaAnticipoPermitida)) < 0, "FECHA_SISTEMA_MENOR_ANTICIPO", MensajeSalida.MENSAJE_ERROR, proAntSim, salida.P_OUT_MENSAJES);
				ProcesaRequisito(con, entrada.p_IdEmpresa, (sqlFechaCondicionLiquidacion.compareTo(sqlFechaSistema)) < 0, "FECHA_LIQUIDACION_INVALIDA", MensajeSalida.MENSAJE_ERROR, proAntSim, salida.P_OUT_MENSAJES);
				ProcesaRequisito(con, entrada.p_IdEmpresa, (proAntSim.FechaVencimientoOperacion.compareTo(sqlFechaCondicionLiquidacion)) < 0, "FECHA_VCTO_MENOR_FECHA_LIQUIDACION", MensajeSalida.MENSAJE_ERROR, proAntSim, salida.P_OUT_MENSAJES);
				ProcesaRequisito(con, entrada.p_IdEmpresa, (entrada.p_CantidadAnticipo.compareTo(proAntSim.INGCantidad)>0) , "CANTIDAD_ANTICIPAR", MensajeSalida.MENSAJE_ERROR, proAntSim, salida.P_OUT_MENSAJES);
				
			}

			
			limpiarProceso(datosProcesoAnticipoSimultanea, datosProcesoAnticipoSimultaneaConErrores);
			
			if (datosProcesoAnticipoSimultanea.size() == 0) 
			{
				log.write("DATOS INVALIDALIDOS > " + datosProcesoAnticipoSimultaneaConErrores.toString());
				return salida;
			}
			else
			{	
				log.write("VALIDACION DE DATOS DE MOVIMIENTOS REALIZADA");
			}
			

		}
		
		// TODO .. FALTA VALIDAR QUE NO EXISTAN MOVIMIENTOS EN TRANSITO SIN CERTIFICAR (revisar servicio sql)
		
		// ---------------------------------------------------------------------------------------------
		// GENERACION NOTA DE CREDITO 
			   
	    log.write("GENERANDO NOTA DE CREDITO");
	    
	    {
	    	CrearMovimientoComprobante ComprobantePrestamo = new CrearMovimientoComprobante();				
			CrearMovimientoComprobante_Entrada entradaComprobantePrestamo = new CrearMovimientoComprobante_Entrada();
			
			entradaComprobantePrestamo.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaComprobantePrestamo.p_IdUsuario = entrada.p_IdUsuario;
			entradaComprobantePrestamo.p_IdModulo = entrada.p_IdModulo;
			entradaComprobantePrestamo.p_simularOperacion = "N";
			entradaComprobantePrestamo.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
			entradaComprobantePrestamo.p_EsIngresoAutomatico = "S";
			
			entradaComprobantePrestamo.p_Movimientos = datosProcesoAnticipoSimultanea.stream().map(datSimPro -> {
				MovimientoComprobante_Entrada entradaMovimientoComprobante = new MovimientoComprobante_Entrada();
				BigDecimal precioAnticipo = BigDecimal.ZERO;
				//CALCULO DE INTERESES DE SIMULTANEA
				if (entrada.p_CantidadAnticipo.compareTo(BigDecimal.ZERO) != 0)
					precioAnticipo = (entrada.p_MontoAnticipo.divide(entrada.p_CantidadAnticipo, MathContext.DECIMAL32));
				
				///BigDecimal precioAPlazo = datSimPro.INGMontoAPlazo.divide(datSimPro.INGCantidad, MathContext.DECIMAL128);
				///BigDecimal montoAPlazo = entrada.p_CantidadAnticipo.multiply(precioAPlazo, MathContext.DECIMAL128);				
				BigDecimal montoAPlazo = entrada.p_CantidadAnticipo.multiply(datSimPro.PrecioAPlazo, MathContext.DECIMAL128);				
				BigDecimal v_Monto =  montoAPlazo.subtract(entrada.p_MontoAnticipo, MathContext.DECIMAL128);
				
				
				
				
				
				BigDecimal v_TasaPorDiasTranscurrido = (entrada.p_TasaAnticipo.divide(new BigDecimal(100)));
				BigDecimal v_InteresAcumulado 	= (v_TasaPorDiasTranscurrido.add(new BigDecimal(1)));
				BigDecimal v_InteresAcumulado2 	= v_InteresAcumulado.multiply(v_Monto);
				BigDecimal v_InteresAcumulado3 	= v_InteresAcumulado2.subtract(v_Monto);
				BigDecimal v_TasaPorPlazo = entrada.p_TasaAnticipo.divide(new BigDecimal(100));
				Long v_TasaPorPlazo2 = entrada.p_PlazoOriginal/30;
				BigDecimal v_TasaPorPlazo3 = v_TasaPorPlazo.multiply(new BigDecimal(v_TasaPorPlazo2));
				BigDecimal v_MontoAPlazo = v_TasaPorPlazo3.add(new BigDecimal(1));
				BigDecimal v_MontoAPlazo2 = v_MontoAPlazo.multiply(v_Monto);
				
				entradaMovimientoComprobante.NumeroMvtoCmbte = datSimPro.IdCartera;
				entradaMovimientoComprobante.IdEmpresa = entrada.p_IdEmpresa;
				entradaMovimientoComprobante.IdConcepto = datSimPro.IdConcepto;				
				entradaMovimientoComprobante.SecuenciaAgrupacion = "";				
				entradaMovimientoComprobante.IdCliente = datSimPro.IdCliente;
				entradaMovimientoComprobante.IdPortafolio = datSimPro.IdPortafolio;				
				entradaMovimientoComprobante.ModPrefFact = "";				
				entradaMovimientoComprobante.IdEmpleadoDigitador = entrada.p_IdEmpleado;
				entradaMovimientoComprobante.IdInstrumento = datSimPro.IdInstrumento;
				entradaMovimientoComprobante.IdInstrumentoRelacionado = 0L;				
				entradaMovimientoComprobante.IdInstrumentoSubyacente = datSimPro.IdInstrumentoSubyacente;			
				entradaMovimientoComprobante.IdTipoProducto = datSimPro.IdTipoProducto;
				entradaMovimientoComprobante.IdMoneda = datSimPro.IdMonedaSistema;				
				entradaMovimientoComprobante.IdMonedaSubyacente = datSimPro.IdMonedaInstrumentoSubyacente;
				entradaMovimientoComprobante.IdMonedaLiquidacion = datSimPro.IdMonedaSistema;
				entradaMovimientoComprobante.IdTipoComprobante = IdCondicionParam;     // Nota de Credito
				entradaMovimientoComprobante.IdTipoOperacion = datSimPro.IdTipoOperacion;
				entradaMovimientoComprobante.IdBoveda = datSimPro.IdBoveda;
				entradaMovimientoComprobante.IdCorredoraContraparte = 0L;
				entradaMovimientoComprobante.IdCondicionLiquidacion = entrada.p_IdCondicionLiquidacion;				
				entradaMovimientoComprobante.FechaLiquidacion = entrada.p_FechaCondicionLiquidacion;
				entradaMovimientoComprobante.IdBolsa = datSimPro.IdBolsa;
				entradaMovimientoComprobante.IdTipoReajuste = datSimPro.IdTipoReajusteSubyacente;
				entradaMovimientoComprobante.IdTipoConvenio = "SCV"; // Sin convenio				
				entradaMovimientoComprobante.IdFormaCustodia = "";		
				entradaMovimientoComprobante.IdEmisor = datSimPro.IdEmisor;  //El IdEmisor de SIM es OPTIMUS ya no usa el mismo IdEmisor que subyacente
				entradaMovimientoComprobante.IdOrden = 0L;
				entradaMovimientoComprobante.IdAsignacion = 0L;
				entradaMovimientoComprobante.IdAsignacionRef = 0L;
				entradaMovimientoComprobante.IdTipoCanalIngreso = "";
				entradaMovimientoComprobante.FechaOrden = "0001-01-01";
				entradaMovimientoComprobante.IdTransaccion = 0L;
				entradaMovimientoComprobante.FolioTransaccion = "0";				
				entradaMovimientoComprobante.FechaTransaccion = "0001-01-01";
				entradaMovimientoComprobante.FechaVencimiento = entrada.p_FechaCondicionLiquidacion;
				entradaMovimientoComprobante.PrecioAPlazo = precioAnticipo;
				entradaMovimientoComprobante.MontoAPlazo = v_MontoAPlazo2;		
				entradaMovimientoComprobante.FechaParidad = "0001-01-01";
				entradaMovimientoComprobante.ValorParidad = BigDecimal.ZERO;
				entradaMovimientoComprobante.FechaTipoCambio = "0001-01-01";
				entradaMovimientoComprobante.ValorTipoCambio = BigDecimal.ZERO; 
				entradaMovimientoComprobante.ValorCaptacionMonedaOrigen = BigDecimal.ZERO;
				entradaMovimientoComprobante.TasaOperacion = entrada.p_TasaAnticipo;
				entradaMovimientoComprobante.Tasa = entrada.p_TasaAnticipo;
				entradaMovimientoComprobante.Cantidad = entrada.p_CantidadAnticipo; 
				entradaMovimientoComprobante.Precio = precioAnticipo;
				entradaMovimientoComprobante.Monto = v_Monto;
				entradaMovimientoComprobante.MontoMovimientoComprobante = v_Monto;
				entradaMovimientoComprobante.MontoLiquidacion = v_Monto;
				entradaMovimientoComprobante.MontoDescuento = BigDecimal.ZERO; 
				entradaMovimientoComprobante.MontoInteres = v_InteresAcumulado3;			
				//La glosa anticipo simultanea se usa tambien para detalle de factura (no cambiar)
				String glosaAnticipoSimmultaneas = "Liquidacion Anticipada de Operacion Simultanea de "+ datSimPro.NemotecnicoSubyacente  +" "+ entrada.p_CantidadAnticipo.toString() + "  Pagadera el dia "+ datSimPro.FechaVencimientoOperacion.toString() +" Tasa "+ entrada.p_TasaAnticipo +" Boveda "+ datSimPro.IdBoveda +" Valor al "+ datSimPro.FechaVencimientoOperacion.toString() + " " + datSimPro.INGMontoAPlazo.setScale(2, BigDecimal.ROUND_UP).toString() + " Valor Pagado al "+ entrada.p_FechaCondicionLiquidacion.toString() +" "+ v_MontoAPlazo2.setScale(2, BigDecimal.ROUND_UP).toString();
				entradaMovimientoComprobante.Glosa = glosaAnticipoSimmultaneas;
				entradaMovimientoComprobante.ValorRescate = BigDecimal.ZERO;
				entradaMovimientoComprobante.TasaAgente = BigDecimal.ZERO; 
				entradaMovimientoComprobante.Spread = BigDecimal.ZERO; 
				entradaMovimientoComprobante.DiasPlazo = BigDecimal.valueOf(entrada.p_PlazoResidual);
				entradaMovimientoComprobante.FechaMovimientoComprobante = entrada.p_FechaSistema; 				
				entradaMovimientoComprobante.FolioCartera = datSimPro.FolioCartera;
				entradaMovimientoComprobante.FechaEmision = entrada.p_FechaSistema; 
				entradaMovimientoComprobante.TasaEmision = BigDecimal.ZERO; 
				entradaMovimientoComprobante.EsComprobIndividual = "S";
				entradaMovimientoComprobante.EsCuentaPropia = "N"; 
				entradaMovimientoComprobante.IndicadorOrdenDirecta = datSimPro.IndicadorOrdenDirecta; 
				entradaMovimientoComprobante.EsVentaCorta = "N"; 
				entradaMovimientoComprobante.EsPrecioPrima = "N";				
				entradaMovimientoComprobante.EsEnRueda = "N"; 
				entradaMovimientoComprobante.EsRuteo = "N"; 
				entradaMovimientoComprobante.EsAPlazo = "N"; 
				entradaMovimientoComprobante.EsFungible = "N"; 
				entradaMovimientoComprobante.ActualizarCustodia = "S";
				entradaMovimientoComprobante.ActualizarCuentaInversion = "N"; 
				entradaMovimientoComprobante.ActualizarTesoreria = "N"; 
				entradaMovimientoComprobante.ActualizarCustodiaRelacionado = "N";
				entradaMovimientoComprobante.CrearComprobanteAutomatico = "S";     //S: Al generar comprobante automático mueve las custodias en ProcesarFacturacionCustodia.java
				entradaMovimientoComprobante.IdUsuario = entrada.p_IdUsuario;
				
		
				entradaMovimientoComprobante.IdComprobanteReferencia = datSimPro.IdComprobanteReferencia;
				
				
				return entradaMovimientoComprobante;
			}).collect(Collectors.toList());
			
			CrearMovimientoComprobante_Salida salidaCrearMovimientoComprobante = ComprobantePrestamo.logica(con, entradaComprobantePrestamo);
					
			//Recorre y asigna mensajes de salida del crearMovimientoComprobante a la salida de AnticipoSimultanea
			for (MensajeSalida msj : salidaCrearMovimientoComprobante.P_OUT_MENSAJES)
			{		
				salida.P_OUT_MENSAJES.add(msj);
			}
			
			if (MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES) > 0)
			{
				log.write("ERROR AL GENERAR NOTA DE CREDITO");									
				return salida;					
			}
			
			
					
			//Obtiene Id del movimiento de facturación creado 
			for (cl.bcs.facturacion.CrearMovimientoComprobante.ResultadoProceso_Salida result : salidaCrearMovimientoComprobante.p_out_ResultadoProceso) 
			{
				if ("IdComprobante".equalsIgnoreCase(result.Campo) && "ING".equalsIgnoreCase(result.Resultado))
				{
					datosProcesoAnticipoSimultanea = datosProcesoAnticipoSimultanea.stream().map(sim -> {
						sim.IdComprobanteReferencia = result.Id;
						return sim;
					}).collect(Collectors.toList());
				}
			}				
			
			log.write("NOTA DE CREDITO GENERADO > " + salidaCrearMovimientoComprobante.P_OUT_MENSAJES);
	    }
	    
	    // ---------------------------------------------------------------------------------------------
	 	// GENERACION CARGO/ABONO CUENTA INVERSION
	    {
	    	CrearCargoAbono crearCargoAbono = new CrearCargoAbono();
			CrearCargoAbonoEntrada entradaCrearCargoAbono = new CrearCargoAbonoEntrada();
			entradaCrearCargoAbono.p_CargoAbonoAGenerar = datosProcesoAnticipoSimultanea.stream().map(datSimPro -> {
				ObjCargoAbonoAGenerar obj = new ObjCargoAbonoAGenerar();
				obj.NumeroCargoAbono = datSimPro.IdCartera;
				obj.IdPortafolio = datSimPro.IdPortafolio;
				obj.IdMoneda = datSimPro.IdMonedaSistema;
				obj.IdTipoMovimiento = "ANSIMC".equals(datSimPro.IdTipoOperacion) ? "MOVABN" : "MOVCAR";
				obj.IdConcepto = datSimPro.IdConcepto;
				obj.Descripcion = "";
				obj.IdCondicionDeLiquidacion = "T0";
				obj.FechaCondicionDeLiquidacion = entrada.p_FechaCondicionLiquidacion;
				obj.Monto = entrada.p_MontoAnticipo;
				obj.IdEjecutivo = datSimPro.IdAgente;
				obj.IdSucursal = datSimPro.IdSucursal;
				obj.IdTipoProducto = datSimPro.IdTipoProducto;
				obj.IdTipoOperacion = datSimPro.IdTipoOperacion;
				obj.IdOperacion = datSimPro.FolioCartera;
				obj.FechaOperacion = fechaSistema;
				obj.FechaLiquidacionOperacion = entrada.p_FechaCondicionLiquidacion;
				obj.FechaVencimientoOperacion = entrada.p_FechaCondicionLiquidacion;
				obj.IdMonedaOperacion = datSimPro.IdMonedaSistema;
				obj.MontoOperacion = entrada.p_MontoAnticipo;
				obj.TipoCambioOperacion = BigDecimal.ZERO;
				obj.IdContraparte = datSimPro.IdCliente;
				obj.IdTipoComprobante = IdCondicionParam;
				obj.IdComprobante = datSimPro.IdComprobanteReferencia;
				obj.FolioComprobante = "";
				obj.IdComprobanteDetalle = 0L;
				obj.IdComprobanteFormaDePago = 0L;
				obj.IdTipoMovimientoAsociado = "";
				obj.IdMovimientoAsociado = 0L;
				obj.FechaComprobante = fechaSistema;
				obj.IdPersonaPagoCobro = 0L;
				obj.IdInstrumento = datSimPro.IdInstrumento;
				obj.IdTipoInstrumento = datSimPro.IdTipoInstrumento;
				obj.Nemo = datSimPro.Nemotecnico;
				obj.EsIndicadorBilateral = "N";
				obj.EsCargoAbonoPorTraspaso = "N";
				obj.IdPortafolioTraspaso = 0L;
				obj.GeneroContraparteEnTesoreria = "N";
				obj.CertificadoAutomatico = "S";
				return obj;
			}).collect(Collectors.toList());
			
			entradaCrearCargoAbono.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaCrearCargoAbono.p_IdUsuario = entrada.p_IdUsuario;
			entradaCrearCargoAbono.p_IdModulo = entrada.p_IdModulo;
			entradaCrearCargoAbono.p_simularOperacion = "N";
			entradaCrearCargoAbono.p_UsaReglasNegocio = "S";
			entradaCrearCargoAbono.p_FechaSistema = fechaSistema;
			entradaCrearCargoAbono.p_EsIngresoManual	= "N";
			CrearCargoAbonoSalida salidaCrearCargoAbono = crearCargoAbono.logica(con, entradaCrearCargoAbono);
			salida.P_OUT_ERRORESCONTADOR += MensajeSalida.getNumeroErrores(salidaCrearCargoAbono.P_OUT_MENSAJES);
			
			if(salida.P_OUT_ERRORESCONTADOR > 0) {
				salida.P_OUT_MENSAJES.addAll(salidaCrearCargoAbono.P_OUT_MENSAJES);
				return salida;
			}
	    }
		
	    

		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
		
		if(salida.P_OUT_ERRORESCONTADOR.equals(0L)){
			
			HashMap<String,Object> paramsCartera = new HashMap<>();
			paramsCartera.put("FolioCartera", entrada.p_FolioCartera);
			List<ObjTablaCartera> CarteraSimultanea = sql.query(con, SQL_SELECT_CARTERA_SIMUTANEA, paramsCartera, ObjTablaCartera.class);
			List<ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada> paramsValorizarCustodia = new ArrayList<ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada>();
			ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada ValorizarCustodia = new ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada();
			
			for (ObjTablaCartera Cartera : CarteraSimultanea) 
			{
				ValorizarCustodia.TablaOrigen					= "CARTERA";
				ValorizarCustodia.IdCustodia					= Cartera.IdCartera;
				ValorizarCustodia.IdCliente						= Cartera.IdCliente;
				ValorizarCustodia.IdPortafolio					= Cartera.IdPortafolio;
				ValorizarCustodia.EsRegistroNuevo				= "N";
				ValorizarCustodia.GenerarNuevoFolioCustodia		= "N";
				ValorizarCustodia.OrigenProcesamiento			= "VALORIZACION_CARTERASIM";
				ValorizarCustodia.FechaOperacion 				= Cartera.FechaOperacion;
				ValorizarCustodia.FechaVencimientoOperacion		= Cartera.FechaVencimientoOperacion;
				ValorizarCustodia.PlazoEnDiasOperacion			= entrada.p_PlazoOriginal;
				ValorizarCustodia.IdCondicionLiquidacion		= Cartera.IdCondicionLiquidacion;
				ValorizarCustodia.FechaLiquidacion				= Cartera.FechaLiquidacion;
				ValorizarCustodia.IdClasificacionTipoInstrumento= Cartera.IdClasificacionTipoInstrumento;
				ValorizarCustodia.IdTipoInstrumento 			= Cartera.IdTipoInstrumento;
				ValorizarCustodia.IdInstrumento 				= Cartera.IdInstrumento;
				ValorizarCustodia.Nemotecnico 					= Cartera.Nemotecnico;
				ValorizarCustodia.FechaVencimientoInstrumento 	= Cartera.FechaVencimientoInstrumento;
				ValorizarCustodia.FechaEmisionInstrumento 		= Cartera.FechaEmisionInstrumento;
				ValorizarCustodia.TieneCorteDeCupon 			= Cartera.TieneCorteDeCupon;
				ValorizarCustodia.IdMonedaInstrumento 			= Cartera.IdMonedaInstrumento;
				ValorizarCustodia.IdMonedaEmisionInstrumento 	= Cartera.IdMonedaEmision;
				ValorizarCustodia.IdTipoReajusteInstrumento 	= Cartera.IdTipoReajuste;
				ValorizarCustodia.Cantidad						= Cartera.INGCantidad;
				ValorizarCustodia.ValorRescate					= Cartera.INGValorRescate;
				ValorizarCustodia.MontoCompra 					= Cartera.INGMontoCompraActualMonedaInstrumento;
				ValorizarCustodia.MontoCompraMonedaSistema 		= Cartera.INGMontoCompraActualMonedaInstrumento;
				ValorizarCustodia.PrecioCompra 					= Cartera.INGPrecioCompra;
				ValorizarCustodia.TasaCompra 					= Cartera.INGTasaCompra;
				ValorizarCustodia.IdTipoOperacion 				= Cartera.IdTipoOperacion;
				paramsValorizarCustodia.add(ValorizarCustodia);
			}
			
			paramsValorizarCustodia.stream().forEach(t -> t.OrigenProcesamiento = "VALORIZACION_CARTERASIM");
			ValorizarCustodia.ValorizarCustodiaEntrada VIE = new ValorizarCustodia.ValorizarCustodiaEntrada();				
			VIE.p_IdEmpresa					= entrada.p_IdEmpresa;	
			VIE.p_IdUsuario					= entrada.p_IdUsuario;
			VIE.p_IdModulo					= "CUS";
			VIE.p_IdPais 					= entrada.p_IdPais;
			VIE.p_OrigenProcesamiento 		= "VALORIZACION_CARTERASIM";
			VIE.p_InstrumentosAValorizar 	= paramsValorizarCustodia;	
			CacheValorizacion.agregar(VIE);
			CacheValorizacion.valorizar(con);
			
			
			EntradaNotificarAnticipoSimultanea entradaNotificarAnticipoSimultanea = new EntradaNotificarAnticipoSimultanea();
			for(AnticipoSimultanea_Proceso datSimPro : datosProcesoAnticipoSimultanea){
				NotificarAnticipoSimultanea.Cartera cartera = new NotificarAnticipoSimultanea.Cartera();
				cartera.IdCartera = datSimPro.IdCartera;
				entradaNotificarAnticipoSimultanea.p_IdCarteras.add(cartera);
			}
			
			if(entradaNotificarAnticipoSimultanea.p_IdCarteras.size() > 0){
				entradaNotificarAnticipoSimultanea.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaNotificarAnticipoSimultanea.p_IdUsuario = entrada.p_IdUsuario;
				entradaNotificarAnticipoSimultanea.p_IdModulo = entrada.p_IdModulo;
				NotificarAnticipoSimultanea notificarAnticiposSimultanea = new NotificarAnticipoSimultanea();
				notificarAnticiposSimultanea.logica(con, entradaNotificarAnticipoSimultanea);
			}
		}
		
		log.write("PARAMETROS DE SALIDA : " + salida.toString());		
		log.write("== TERMINO ANTICIPO SIMULTANEA ==");
		
		return salida;
	}
	
	
	

	//-------------------------------------------------------------------------------------------
	// METODOS UTILITARIOS
	//-------------------------------------------------------------------------------------------	
	 
	//Procesa salida de validaciones
	private void ProcesaRequisito(Connection con, Long idEmpresa, boolean esError, String codMsg, String CodRequisito, AnticipoSimultanea_Proceso antSim, List<MensajeSalida> P_OUT_MENSAJES)
			throws Exception {
		Calendar c = Calendar.getInstance();
	    c.setTime(antSim.FechaLiquidacion);
	    c.add(Calendar.DATE, 1);	
		if (esError) 
		{
			String msg=Diccionario.getMensaje(con, idEmpresa, codMsg)
					.add("NumeroReferencia", antSim.FolioCartera)
				    .msg()
					.replace("::FechaAnticipoPermitida::", new SimpleDateFormat("dd-MM-yyyy").format(c.getTimeInMillis()));			
			 
			if (CodRequisito.equals(MensajeSalida.MENSAJE_ERROR)) 
			{
				P_OUT_MENSAJES.add(new MensajeSalida(antSim.FolioCartera, MensajeSalida.MENSAJE_ERROR, codMsg,msg));
				antSim.errores++;
			}
			else if (CodRequisito.equals(MensajeSalida.MENSAJE_AUTORIZACION)) 
			{
				P_OUT_MENSAJES.add(new MensajeSalida(antSim.FolioCartera, MensajeSalida.MENSAJE_AUTORIZACION, codMsg,msg));
				antSim.autorizaciones++;						
			}
			else if (CodRequisito.equals(MensajeSalida.MENSAJE_ADVERTENCIA)) 
			{
				P_OUT_MENSAJES.add(new MensajeSalida(antSim.FolioCartera, MensajeSalida.MENSAJE_ADVERTENCIA, codMsg,msg));
				antSim.advertencias++;
			}
			else 
			{
				antSim.ok++;
			}
		}
		else 
		{
			antSim.ok++;
		}		
	}
	
	
	//Quita del proceso los registros invalidos
	public void limpiarProceso(List<AnticipoSimultanea_Proceso> anticipoSimultaneaProceso, List<AnticipoSimultanea_Proceso> anticipoSimultaneaProcesoConErrores) 
	{
		for (AnticipoSimultanea_Proceso asp : anticipoSimultaneaProceso) 
		{
			if (asp.errores > 0) {
				anticipoSimultaneaProcesoConErrores.add(asp);
			}
		}
		
		for (AnticipoSimultanea_Proceso asp : anticipoSimultaneaProcesoConErrores) 
		{
			//Remueve el registro
			if (anticipoSimultaneaProceso.contains(asp))
				anticipoSimultaneaProceso.remove(asp);
			
		}
	}
	
	
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	//-------------------------------------------------------------------------------------------
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();
		AnticipoSimultanea_Entrada in = gson.fromJson(jin.toString(), AnticipoSimultanea_Entrada.class);
		AnticipoSimultanea_Salida out = logica(con,in);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	
	
	//-------------------------------------------------
	// METODO PARA PRUEBAS LOCALES 
	//-------------------------------------------------
 
	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
			try (Connection con = PlataformaUtils.getDESAConnection())
		{
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();		

			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":64,\"p_IdEmpleado\":229,\"p_IdModulo\":\"CUS\",\"p_UsaReglasNegocio\":\"S\",\"p_FolioCartera\":12089,\"p_IdPais\":44,\"p_FechaSistema\":\"2018-07-24\",\"p_IdCondicionLiquidacion\":\"T0\",\"p_FechaCondicionLiquidacion\":\"2018-07-24\",\"p_CantidadAnticipo\":100,\"p_TasaAnticipo\":0.7,\"p_MontoAnticipo\":1000233.33,\"p_PlazoOriginal\":3,\"p_PlazoResidual\":2}";			
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":64,\"p_IdEmpleado\":229,\"p_IdModulo\":\"CUS\",\"p_UsaReglasNegocio\":\"S\",\"p_FolioCartera\":12192,\"p_IdPais\":44,\"p_FechaSistema\":\"2018-08-08\",\"p_IdCondicionLiquidacion\":\"T0\",\"p_FechaCondicionLiquidacion\":\"2018-08-08\",\"p_CantidadAnticipo\":300,\"p_TasaAnticipo\":0.6,\"p_MontoAnticipo\":3000600,\"p_PlazoOriginal\":7,\"p_PlazoResidual\":6}";
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdEmpleado\":7889,\"p_IdModulo\":\"CUS\",\"p_UsaReglasNegocio\":\"S\",\"p_FolioCartera\":20689102,\"p_IdPais\":44,\"p_FechaSistema\":\"2019-05-17\",\"p_IdCondicionLiquidacion\":\"T0\",\"p_FechaCondicionLiquidacion\":\"2019-05-17\",\"p_CantidadAnticipo\":29000,\"p_TasaAnticipo\":1.6,\"p_MontoAnticipo\":169699302,\"p_PlazoOriginal\":7,\"p_PlazoResidual\":7}";
		
			AnticipoSimultanea_Entrada Objin = gson.fromJson(json, AnticipoSimultanea_Entrada.class);
			AnticipoSimultanea demo = new AnticipoSimultanea();
 			
			System.out.println("ENTRADA:" + Objin.toString());
			
 			try
 			{
 				AnticipoSimultanea_Salida ObjOut = demo.logica(con, Objin);
 				System.out.println("SALIDA:" + ObjOut);
 				//con.commit();
 			}
 			catch(Exception er) {
				er.printStackTrace();
				con.rollback();
			}

	    }

		System.out.println("== FIN PRUEBA LOCAL ==");
	}
	
	
	
}
