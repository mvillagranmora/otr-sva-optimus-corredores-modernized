package cl.bcs.mesa.MRF;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.CrearCargoAbono;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoEntrada;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoSalida;
import cl.bcs.cuentaInversion.CrearCargoAbono.ObjCargoAbonoAGenerar;
import cl.bcs.cuentaInversion.CrearCargoAbono.ObjCargoAbonoGenerado;
import cl.bcs.custodia.IngresoReserva;
import cl.bcs.custodia.IngresoReserva.IngresoReserva_IN;
import cl.bcs.custodia.IngresoReserva.IngresoReserva_OUT;
import cl.bcs.custodia.IngresoReserva.ReservaObj_IN;
import cl.bcs.custodia.IngresoReserva.ReservaObj_OUT;
import cl.bcs.custodia.utils.CacheValorizacion;
import cl.bcs.custodia.utils.IngresoCustodia;
import cl.bcs.custodia.utils.LogCustodia;
import cl.bcs.custodia.utils.LogCustodia.ObjMovimiento;
import cl.bcs.custodia.utils.RetiroCustodia;
import cl.bcs.mesa.MES.ConsultarInstrumento;
import cl.bcs.mesa.MES.ConsultarInstrumento.InformacionInstrumento;
import cl.bcs.mesa.MES.ConsultarInstrumentoTipoReajuste;
import cl.bcs.mesa.MES.ConsultarInstrumentoTipoReajuste.InformacionInstrumentoTipoReajuste;
import cl.bcs.mesa.MES.ConsultarTipoInstrumento.InformacionTipoInstrumento;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion.ComprobantePagoOperacionObjDel_IN;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion.ComprobantePagoOperacionObj_IN;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion.ComprobantePagoOperacionObj_OUT;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion.CrearComprobantePagoOperacion_IN;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion.CrearComprobantePagoOperacion_OUT;
import cl.bcs.mesa.MES.IngresoOperacion;
import cl.bcs.mesa.MES.IngresoOperacion.IngresoOperacion_IN;
import cl.bcs.mesa.MES.IngresoOperacion.IngresoOperacion_OUT;
import cl.bcs.mesa.MES.IngresoOperacion.OperacionObjIn;
import cl.bcs.mesa.MES.IngresoOperacion.OperacionObjOut;
import cl.bcs.mesa.MRF.CrearOperacionMesa.Concepto;
import cl.bcs.mesa.MRF.CrearOperacionMesa.CrearOperacionMesa_DetalleOperacion_OUT;
import cl.bcs.mesa.MRF.CrearOperacionMesa.CrearOperacionMesa_FormaPagoOperacion_OUT;
import cl.bcs.mesa.MRF.CrearOperacionMesa.CrearOperacionMesa_Operacion_OUT;
import cl.bcs.mesa.MRF.CrearOperacionMesa.FormaDePagoMesa;
import cl.bcs.mesa.MRF.CrearOperacionMesa.Persona;
import cl.bcs.mesa.MRF.CrearOperacionMesa.PreferenciaPagoPortafolio;
import cl.bcs.mesa.MRF.CrearOperacionMesaCustodia.CrearOperacionMesaCustodia_IN;
import cl.bcs.mesa.MRF.CrearOperacionMesaCustodia.CrearOperacionMesaCustodia_OUT;
import cl.bcs.mesa.MRF.CrearOperacionMesaCustodia.OperacionMesaCustodiaObj;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.CrearIngresoEgreso;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Salida;
import cl.bcs.tesoreria.CrearIngresoEgreso.FormaDePago_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgresoGenerado_Salida;
import cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgreso_Entrada;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


import cl.bcs.plataforma.pla.SaldoFlujoCaja;
import cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjFLUJOCAJA;
import cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR;
import cl.bcs.plataforma.pla.utils.Diccionario;

public class IngresarCompraVentaTermino implements JavaCall{
	private static final Logger logger = LogManager.getLogger(IngresarCompraVentaTermino.class);

	

	private final String SQL_SELECT_INDICADORES_PORTAFOLIO = loadResourceToString("cl/bcs/mesa/MRF/sql/Globales_SelectIndicadoresPortafolio.sql");
	SQL _sql = new SQL();
	
	public static class IngresarCompraVentaTermino_Operacion_IN {
		public String 		Accion;
		public Long 		IdInternoOperacion;
		public Long 		IdOperacion;
		public Long			FolioOperacion;
		public Long			FolioCartera;//para pactos y depositos y desembolso
		public Long			IdCliente;
		public Long			IdPortafolio;
		public Long			IdClienteCorredor;
		public Long			IdPortafolioCorredor;
		public String		Intermesa;
		public Long			IdMesaOrigen;
		public Long			IdMesaDestino;
		public String		AfectaCustodia;
		public String		IdTipoProducto;//'MERF'
		public String		IdTipoOperacion;//COMTER,VENTER,COMPAC,VENPAC
		public String		IdTipoReajuste;//"USD","UF"
		public Long			Plazo;
		public BigDecimal	Monto;
		public BigDecimal	MontoMoneda;
		public String		IdMoneda;//CLP "USD","UF" moneda local
		public String		IdMonedaLiquidacion; //CLP "USD","UF"
		public BigDecimal	Tasa;
		public BigDecimal	ValorFinal;
		public BigDecimal	ValorFinalMoneda;
		public BigDecimal	TasaAgente;
		public String 		FechaOperacion;
		public String		FechaVencimiento;
		public String		IdCondicionLiquidacion;
		public String		FechaLiquidacion;
		public Long			IdAgente;
		public Long			IdOperador;
		public Long			IdEmpleado;
		public String		IdEstado;
		public String		Glosa = "";
		public String		IdTipoCanalIngreso;
		public Concepto 	Concepto;
		public Persona		Persona;
		public String 		TipoDocumento;
		public Long 		IdContacto;
		public String		RequiereAutorizacion ="N";
		//Nuevo deposito desembolso
		//public String		IdEmisor;
		
 
		public String		DocumentoIdentidadCorredor = "";
		public String 		ValorizacionManual="N";
		
		
	 	public String toString() {
			return Utils.printJson(this, false);
		};
	}
	public static class IngresarCompraVentaTermino_DetalleOperacion_IN {
		public Long			IdInternoDetalle;
		public Long			IdInternoOperacion;
		public Long			IdReserva;
		public Long			IdCliente;
		public Long			IdPortafolio;
		public Long			IdClienteCorredor;
		public Long			IdPortafolioCorredor;
		public Long			IdMesa;
		public String 		IdTipoProducto; //'MERF'
		public String 		IdTipoOperacion; //COMTER,VENTER,COMPAC,VENPAC
		public Long			IdInstrumento;
		public String		Nemotecnico;
		public String 		EsFungible;
		public String 		IdBolsa;
		public String 		IdBoveda;
		public Long			IdCartera;
		public Long			FolioCartera;
		public String 		CodigoIFRS;
		public String 		IdEmisor;
		public String		IdMoneda;
		public String 		IdTipoReajuste;
		public String 		IdMonedaEmisionInstrumento;
		public String 		IdMonedaLiquidacionInstrumento;
		public String 		IdMonedaLiquidacionOperacion;
		public BigDecimal 	CorteMinimo;	
		public BigDecimal 	Cantidad;
		public BigDecimal 	Precio;
		public BigDecimal 	PrecioMercado;
		public BigDecimal 	Tasa;
		public BigDecimal 	TasaMercado;
		public BigDecimal 	Monto;
		public BigDecimal 	MontoLiquidacion;
		public BigDecimal 	MontoLiquidacionOperacion;
		public BigDecimal 	ValorRescate;
		public String 		FechaVencimiento;
		
		public String		Concepto;
		public BigDecimal 	MontoValorizadoReserva;
		
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	public static class IngresarCompraVentaTermino_FormaPagoOperacion_IN {
		public Long 		IdInternoPagoOperacion;
		public Long			IdInternoOperacion;
		public Long 		IdComprobantePagoOperacion;
		public String 		IdFormaDePago;
		public String 		IdMoneda;
		//public String 		IdMonedaLiquidacion;
		public Long 		IdOperacion;
		public String 		RecibePagaPortafolio;
		public BigDecimal 	MontoFormaDePago;
		//public BigDecimal 	MontoLiquidacionFormaDePago;
		public String		FechaLiquidacionFormaDePago;
		public String 		AfectarTesoreria;
		public String		IdTipoMovimientoTes;
		public String		IdTipoMovimientoCtaCli;
		public String		IdTipoMovimientoCtaCorr;
		public String		IdModalidadDePago;
		public FormaDePagoMesa FormaDePago;
		public PreferenciaPagoPortafolio PreferenciaDePago;
		public String		IdEstado;
		public Long 		IdCuentaBancaria;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	public static class IngresarCompraVentaTermino_IN {
		public Long												p_IdEmpresa;
	    public Long												p_IdUsuario;
	    public Long												p_IdPais;
	    public String											p_MonedaSistema;
	    public String											p_FechaSistema;
	    public Long												p_IdSucursal;
	    public String											p_IdModulo; 
	    public String											p_UsaReglasNegocio;
		public List<IngresarCompraVentaTermino_Operacion_IN>			p_Operacion;
		public List<IngresarCompraVentaTermino_DetalleOperacion_IN> 	p_DetalleOperacion;
		public List<IngresarCompraVentaTermino_FormaPagoOperacion_IN> 	p_FormasDePago;
		
		public IngresarCompraVentaTermino_IN(){
			this.p_Operacion = new ArrayList<IngresarCompraVentaTermino_Operacion_IN>();
			this.p_DetalleOperacion = new ArrayList<IngresarCompraVentaTermino_DetalleOperacion_IN>();
			this.p_FormasDePago = new ArrayList<IngresarCompraVentaTermino_FormaPagoOperacion_IN>();
		}
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	public static class IngresarCompraVentaTermino_OUT {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;

		public List<CrearOperacionMesa_Operacion_OUT>			P_OUT_OPERACION;
		public List<CrearOperacionMesa_DetalleOperacion_OUT> 	P_OUT_DETALLEOPERACION;
		public List<CrearOperacionMesa_FormaPagoOperacion_OUT>	P_OUT_FORMASDEPAGO;
		
		public IngresarCompraVentaTermino_OUT() {
			this.P_OUT_OPERACION 		= new ArrayList<CrearOperacionMesa_Operacion_OUT>();
			this.P_OUT_DETALLEOPERACION = new ArrayList<CrearOperacionMesa_DetalleOperacion_OUT>();
			this.P_OUT_FORMASDEPAGO		= new ArrayList<CrearOperacionMesa_FormaPagoOperacion_OUT>();
			this.P_OUT_MENSAJES 		= new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class IngresarCompraVentaTerminoUpdate {
		public Long IdOperacion;
		public Long FolioCartera;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	

	public static class IndicadoresPortafolio {
		public Long IdPortafolio;
		public String OperaCuentaInversion;
		public String OperaLineaSobregiro;		

		public String toString() {
			return cl.bcs.utils.Utils.printJson(this,true);		
		}
	}
	
	
	private List<ObjMovimiento>  cargaLogDatosCustodia(Connection conn, IngresarCompraVentaTermino_IN in, HashMap<String, Object> tipoInst, HashMap<String, Object> inst) throws Exception{
		List<ObjMovimiento> salida = new ArrayList<>();
		try {
			
			
			for (IngresarCompraVentaTermino_Operacion_IN ent : in.p_Operacion){
				ObjMovimiento objCus = new ObjMovimiento();
				objCus.IdGenerarMovimientoCustodia 		= ent.IdOperacion;
				objCus.EstaCertificando = "N";
				//--FolioComprobanteBusqueda--FolioCustodia--FolioCartera
				objCus.FolioCustodiaBusqueda 			= ent.IdOperacion;
				objCus.FolioComprobante 				= ent.IdOperacion;
				objCus.IdTipoComprobante				= "MES";
			//--OPERACION
				objCus.IdEmpresa						= in.p_IdEmpresa;
				objCus.IdPais							= in.p_IdPais;
				objCus.FechaOperacion					= Fecha.toDate(ent.FechaOperacion);
				objCus.FechaVencimientoOperacion		= Fecha.toDate(ent.FechaVencimiento);
				objCus.PlazoEnDiasOperacion				= ent.Plazo;
				objCus.EsNominalIngreso					= "S";
				objCus.IdClienteIngreso					= ent.IdCliente;
				objCus.IdPortafolioIngreso				= ent.IdPortafolio;
				objCus.EsNominalRetiro					= "S";
				//objCus.IdClienteRetiro					= ;
				//objCus.IdPortafolioRetiro;
				objCus.IdTipoProducto					= ent.IdTipoProducto;
				objCus.IdTipoCanalIngreso				= ent.IdTipoCanalIngreso;
				objCus.IdMonedaSistema					= in.p_MonedaSistema;
				//--CodigoIFRS
				objCus.IdConcepto						= ent.Concepto.IdConcepto;
				objCus.IdTipoOperacion					= ent.IdTipoOperacion;
				objCus.IdGrupoOperacion					= ent.IdOperacion;
				objCus.IdOperacion						= ent.IdOperacion;
				objCus.IdMesa							= ent.IdMesaOrigen;
				objCus.IdBolsa							= ((InformacionTipoInstrumento) tipoInst.get(ent.IdTipoOperacion)).IdBolsa;
				objCus.IdSucursal						= in.p_IdSucursal;
				objCus.IdAgente							= ent.IdAgente;
				objCus.IdCondicionLiquidacion			= ent.IdCondicionLiquidacion;
				objCus.FechaLiquidacion					= Fecha.toDate(ent.FechaLiquidacion);
				objCus.Glosa							= ent.Glosa;
				objCus.IdUsuario						= in.p_IdUsuario;
				objCus.IdModulo							= in.p_IdModulo;	
				objCus.TipoCarteraCorredor				= "";
			//--INSTRUMENTO
				objCus.IdClasificacionTipoInstrumento	= ((InformacionInstrumento) inst.get(((InformacionTipoInstrumento) tipoInst.get(ent.IdTipoOperacion)).IdInstrumento)).IdClasificacionTipoInstrumento;
				objCus.IdTipoInstrumento				= ((InformacionInstrumento) inst.get(((InformacionTipoInstrumento) tipoInst.get(ent.IdTipoOperacion)).IdInstrumento)).IdTipoInstrumento;
				objCus.IdInstrumento					= ((InformacionInstrumento) inst.get(((InformacionTipoInstrumento) tipoInst.get(ent.IdTipoOperacion)).IdInstrumento)).IdInstrumento;
				objCus.Nemotecnico						= ((InformacionInstrumento) inst.get(((InformacionTipoInstrumento) tipoInst.get(ent.IdTipoOperacion)).IdInstrumento)).Nemotecnico;
				objCus.IdEmisor							= "";
				objCus.IdTipoReajuste					= ent.IdTipoReajuste;
				objCus.FechaEmisionInstrumento			= Fecha.toDate(ent.FechaOperacion);
				objCus.FechaVencimientoInstrumento		= Fecha.toDate(ent.FechaVencimiento);
				objCus.EsFungible						= ((InformacionInstrumento) inst.get(((InformacionTipoInstrumento) tipoInst.get(ent.IdTipoOperacion)).IdInstrumento)).EsDivisible;
				objCus.IdMonedaInstrumento				= ent.IdTipoReajuste;
				objCus.IdBoveda							= "";
			//--CANTIDADES Y MONTOS
				objCus.Cantidad							= BigDecimal.ONE;
				objCus.CorteMinimo						= BigDecimal.ZERO;
				objCus.PrecioCompra						= BigDecimal.ZERO;
				objCus.TasaCompra						= ent.Tasa;
				objCus.MontoCompra						= ent.Monto;
				objCus.MontoCompraMonedaInstrumento		= ent.MontoMoneda;
				objCus.MontoAPlazo						= ent.ValorFinal;
				objCus.MontoAPlazoMonedaInstrumento		= ent.ValorFinalMoneda;
				objCus.ValorRescate						= ent.ValorFinal;
				salida.add(objCus);
			}
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return salida;
	}
	
	

	
	
	// ------------------------------------------------------------
	// LOGICA
	// ------------------------------------------------------------
	
	public IngresarCompraVentaTermino_OUT logica (Connection conn, IngresarCompraVentaTermino_IN in) throws Exception {
		
		Long init = System.currentTimeMillis();
		long startTime = System.nanoTime();
		
		IngresarCompraVentaTermino_OUT out = new IngresarCompraVentaTermino_OUT();
		Long Id = 0L,errTot = 0L;
		Long TicketAtencion = 0L;
		HashMap<String,String> mapMsg = new HashMap<>();
		logger.info("Inicio servicio MRF.IngresarCompraVentaTermino");
		
		//try {
			//CUSTODIA
			//INGRESO OPERACION
			//FORMA DE PAGO
			//TESORERIA
			//CUENTA INVERSION
			List<OperacionObjIn> v_Operacion = new ArrayList<>();
			List<ReservaObj_IN> v_OperacionDetalle = new ArrayList<>();
			Map<Object,Object> v_OperacionHash = new HashMap<>();
			Map<Object,Object> v_OutOperacionHash = new HashMap<>();			
			Map<Object,Object> v_OutComprobantePagoHash = new HashMap<>();
			IngresoOperacion_OUT resultOperacion = new IngresoOperacion_OUT();
			IngresoReserva_OUT resultReserva = new IngresoReserva_OUT();
			//MultiKeyMap resultReservaHash = new MultiKeyMap();
			IngresarCompraVentaTermino_IN operacionCerrar = new IngresarCompraVentaTermino_IN();
			
			Long cantCerrar = 0L;
			IngresoCustodia.IngresoCustodiaSalida cusTranOut = new IngresoCustodia.IngresoCustodiaSalida();
			CrearCargoAbonoSalida ctaOut = new CrearCargoAbonoSalida();
			CrearIngresoEgreso_Salida tesOut = new CrearIngresoEgreso_Salida();
			CrearComprobantePagoOperacion_OUT resultCompPag = new CrearComprobantePagoOperacion_OUT();
			CrearComprobantePagoOperacion_OUT resultCompPagDel = new CrearComprobantePagoOperacion_OUT();
			HashMap<String, Object> infoInsHash = new HashMap<>();
			MultiKeyMap infoInsTipHash = new MultiKeyMap();
			List<ObjMovimiento>  entLogCus = new ArrayList<>();
			
			
			

			//INDICADORES PORTAFOLIO
			HashMap<Long, IndicadoresPortafolio> indicadoresPortafolio = new HashMap<>();
			{
				HashMap<String,Object> paramsInIndicadorPortafolio = new HashMap<>();
				ArrayList<Long> idsPortafolio = new ArrayList<>();
				
				
				for (IngresarCompraVentaTermino_Operacion_IN operacion : in.p_Operacion) {
					idsPortafolio.add(operacion.IdPortafolio);
					idsPortafolio.add(operacion.IdPortafolioCorredor);				
				}
				
				paramsInIndicadorPortafolio.put("idsPortafolio", idsPortafolio);
		
				List<IndicadoresPortafolio> _indicadoresPortafolio = _sql.query(conn, SQL_SELECT_INDICADORES_PORTAFOLIO, paramsInIndicadorPortafolio, IndicadoresPortafolio.class);
				
				
				HashMap<Object, Object>__indicadoresPortafolio = Utils.List2Hash(_indicadoresPortafolio, "IdPortafolio");
				
				for(Object key: __indicadoresPortafolio.keySet()) {
					indicadoresPortafolio.put((Long)key,(IndicadoresPortafolio) __indicadoresPortafolio.get(key));
				}
			}
			
			
			//* 01: INGRESO OPERACION
			{
				for (IngresarCompraVentaTermino_Operacion_IN operacion : in.p_Operacion){
					OperacionObjIn data = copyPojoIngresoOperacion(operacion);
					
					data.Accion = operacion.Accion;//operacion.IdOperacion.equals(0L)? "CREAR":"MODIFICAR";
					data.IdInternoOperacion = operacion.IdInternoOperacion;
					data.MontoOperacion = operacion.Monto;
					data.MontoOperacionInicial = operacion.Monto;					
					data.MontoOperacionFinal = operacion.ValorFinal;			
					data.MontoOperacionMoneda = operacion.MontoMoneda;			
					data.MontoOperacionInicialMoneda = operacion.MontoMoneda;
					data.MontoOperacionFinalMoneda = operacion.ValorFinalMoneda;	
					data.PlazoEnDias = operacion.Plazo;
					data.FechaCondicionLiquidacion = Fecha.toDate(operacion.FechaLiquidacion);	
					data.IdUsuarioCreacion = operacion.IdOperador;
					data.IdEmpresa = in.p_IdEmpresa;							
					data.UsuarioEdicion = in.p_IdUsuario;
					data.FolioOperacion = 0L;									
					data.IdOperacionRelacionada = 0L;						
					data.IdInstrumento = 0L;//((InformacionInstrumento) instOutHash.get(((InformacionTipoInstrumento) tipInstHash.get(operacion.IdTipoOperacion)).IdInstrumento)).IdInstrumento;									
					data.IdEmisor		= "";
					data.IdMonedaPrincipal = operacion.IdMoneda;							
					data.IdMonedaSecundaria = operacion.IdMonedaLiquidacion;
					data.IdModalidadLiquidacion = 0L;							
					data.IdReferenciaMercado = 0L;
					data.IdPlaza = 0L;
					data.TCCierre = BigDecimal.ZERO;
					data.ParidadCierre = BigDecimal.ZERO;									
					data.TCCosto = BigDecimal.ZERO;
					data.PuntosTasasForward  = "";
					data.PuntosForward = BigDecimal.ZERO;
					data.TCForward = BigDecimal.ZERO;
					data.TasasMonedaPrincipal = BigDecimal.ZERO;
					data.TasasMonedaSecundaria = BigDecimal.ZERO;
					data.MontoDiferido = BigDecimal.ZERO;
					data.MontoMargen = BigDecimal.ZERO;
					data.IdFixing = 0L;
					data.FechaFixing = Fecha.toDate("0001-01-01");						
					data.HoraFixing = "00:00:00";
					data.IdSpotAsociado = 0L;									
					data.IdClasificacionCartera = 0L;						
					data.FechaLiquidacion = Fecha.toDate("0001-01-01");	
					data.AccionEdicion = "B";
					data.FechaOperacion = operacion.FechaOperacion;
					data.FechaVencimiento = Fecha.toDate(operacion.FechaVencimiento);	
					data.TipoDocumento = operacion.TipoDocumento;
					data.IdContacto = operacion.IdContacto;
					data.RequiereAutorizacion = operacion.RequiereAutorizacion;
					data.ValorizacionManual = operacion.ValorizacionManual;
										
					v_Operacion.add(data);										
				}
				IngresoOperacion callOpe = new IngresoOperacion();
				IngresoOperacion_IN inOpe = new IngresoOperacion_IN();
				inOpe.p_IdEmpresa = in.p_IdEmpresa;
				inOpe.p_IdUsuario = in.p_IdUsuario;
				inOpe.p_IdModulo = in.p_IdModulo;
				inOpe.p_simularOperacion = "N"; 
				inOpe.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
				inOpe.p_IdPais = in.p_IdPais;
				inOpe.p_Operaciones = v_Operacion;

				logger.debug("MRF.Llamada Ingreso Operacion");
				logger.debug("[IngresarCompraVentaTermino] Inicio llamada a IngresoOperacion");
				startTime = System.nanoTime();
				resultOperacion = callOpe.logica(conn, inOpe);
				logger.debug("[IngresarCompraVentaTermino] (" + (System.nanoTime() - startTime)/1000000 + ") Fin llamada a IngresoOperacion");				
			
				
				if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0){
					out.P_OUT_MENSAJES.addAll(resultOperacion.P_OUT_MENSAJES);
					return out;
				}
				v_OutOperacionHash = Utils.List2Hash(resultOperacion.P_OUT_OPERACION,"IdAplicacion");
			}
			//* 02: INGRESO RESERVA, detalle Operacion
			{
				//Detalle Operacion
				if (in.p_DetalleOperacion.size() > 0){
					List<Long> paramInst = new ArrayList<>();
					for (IngresarCompraVentaTermino_DetalleOperacion_IN detalleOperacion : in.p_DetalleOperacion){
						paramInst.add(detalleOperacion.IdInstrumento);
					}
					//consulto info instrumento
					ConsultarInstrumento ins = new ConsultarInstrumento();
					logger.debug("[IngresarCompraVentaTermino] Inicio llamada a ConsultarInstrumento");
					startTime = System.nanoTime();
					List<InformacionInstrumento> infoIns = ins.logica(conn, paramInst);
					logger.debug("[IngresarCompraVentaTermino] (" + (System.nanoTime() - startTime)/1000000 + ") Fin llamada a ConsultarInstrumento");		
					infoInsHash = Utils.List2Hash(infoIns, "IdInstrumento");
					//consultar Instrumento tipo reajuste
					ConsultarInstrumentoTipoReajuste insTip = new ConsultarInstrumentoTipoReajuste();
					logger.debug("[IngresarCompraVentaTermino] Inicio llamada a ConsultarInstrumentoTipoReajuste");
					startTime = System.nanoTime();
					List<InformacionInstrumentoTipoReajuste> infoInsTip = insTip.logica(conn, paramInst);
					logger.debug("[IngresarCompraVentaTermino] (" + (System.nanoTime() - startTime)/1000000 + ") Fin llamada a ConsultarInstrumentoTipoReajuste");
					for (InformacionInstrumentoTipoReajuste inf: infoInsTip){
						infoInsTipHash.put(inf.IdInstrumento, inf.IdTipoReajuste,inf);
					}
				}
				v_OperacionHash = Utils.List2Hash(v_Operacion,"IdInternoOperacion");
				
				for (IngresarCompraVentaTermino_DetalleOperacion_IN detalleOperacion : in.p_DetalleOperacion){
					String concepto = "";
					
					ReservaObj_IN data = new ReservaObj_IN();
					data.IdOperacion 			= ((OperacionObjOut) v_OutOperacionHash.get(detalleOperacion.IdInternoOperacion)).IdOperacion;
					data.IdRegistro				= detalleOperacion.IdInternoDetalle;
					data.IdReserva				= detalleOperacion.IdReserva;
					data.IdConcepto				= detalleOperacion.Concepto;//concepto;//			INGRES / RETRES / INGDET / RETDET / LIMDET
					data.FolioOperacion			= ((OperacionObjOut) v_OutOperacionHash.get(detalleOperacion.IdInternoOperacion)).FolioOperacion;
					data.FolioCartera			= detalleOperacion.FolioCartera; 
					data.FolioCarteraCustodia	= detalleOperacion.FolioCartera;     									
					data.IdCartera				= detalleOperacion.IdCartera; 
					data.IdCarteraCustodia		= detalleOperacion.IdCartera;
					data.IdTipoProducto			= detalleOperacion.IdTipoProducto;
					data.Cantidad				= detalleOperacion.Cantidad;
					data.Tasa					= detalleOperacion.Tasa;
					data.TasaMercado			= detalleOperacion.TasaMercado;
					data.Precio					= detalleOperacion.Precio;
					data.PrecioMercado			= detalleOperacion.PrecioMercado;
					data.ValorRescate			= detalleOperacion.ValorRescate;
					data.MontoValorizado		= detalleOperacion.Monto;
					data.MontoLiquidacion		= detalleOperacion.MontoLiquidacion;
					data.MontoLiquidacionOperacion= detalleOperacion.MontoLiquidacionOperacion;
					data.IdClasificacionTipoInstrumento = ((InformacionInstrumento) infoInsHash.get(detalleOperacion.IdInstrumento)).IdClasificacionTipoInstrumento;
					data.IdTipoInstrumento		= ((InformacionInstrumento) infoInsHash.get(detalleOperacion.IdInstrumento)).IdTipoInstrumento;
					data.IdInstrumento			= ((InformacionInstrumento) infoInsHash.get(detalleOperacion.IdInstrumento)).IdInstrumento;
					data.Nemotecnico			= detalleOperacion.Nemotecnico;
					data.IdMoneda				= detalleOperacion.IdMoneda;
					data.IdTipoReajuste			= detalleOperacion.IdTipoReajuste;
					data.IdMonedaEmisionInstrumento			= detalleOperacion.IdMonedaEmisionInstrumento;
					data.IdMonedaLiquidacionInstrumento		= detalleOperacion.IdMonedaLiquidacionInstrumento;
					data.IdMonedaLiquidacionOperacion		= detalleOperacion.IdMonedaLiquidacionOperacion;
					data.MontoValorizadoReserva = detalleOperacion.MontoValorizadoReserva;
					data.IdEmisor				= detalleOperacion.IdEmisor;
					if ( "N".equalsIgnoreCase(detalleOperacion.EsFungible) && "IF".equalsIgnoreCase(data.IdClasificacionTipoInstrumento)){
						data.CorteMinimo		= detalleOperacion.CorteMinimo;
					}
					else {
						data.CorteMinimo		= ((InformacionInstrumentoTipoReajuste) infoInsTipHash.get(detalleOperacion.IdInstrumento,detalleOperacion.IdTipoReajuste)).CorteMinimo != null || !((InformacionInstrumentoTipoReajuste) infoInsTipHash.get(detalleOperacion.IdInstrumento,detalleOperacion.IdTipoReajuste)).CorteMinimo.equals(BigDecimal.ZERO) ? ((InformacionInstrumentoTipoReajuste) infoInsTipHash.get(detalleOperacion.IdInstrumento,detalleOperacion.IdTipoReajuste)).CorteMinimo : ((InformacionInstrumento) infoInsHash.get(detalleOperacion.IdInstrumento)).CorteMinimo;
					}
					data.FechaVencimiento		= detalleOperacion.FechaVencimiento;
					data.EsFungible				= detalleOperacion.EsFungible;
					data.Glosa					= "";
					data.IdBolsa				= detalleOperacion.IdBolsa;
					data.IdBoveda				= detalleOperacion.IdBoveda;	
					data.CodigoIFRS				= detalleOperacion.CodigoIFRS;
					data.FechaOperacion			= ((OperacionObjIn) v_OperacionHash.get(detalleOperacion.IdInternoOperacion)).FechaOperacion;
					
					v_OperacionDetalle.add(data);
				}
				IngresoReserva callReserva = new IngresoReserva();
				IngresoReserva_IN inRes = new IngresoReserva_IN();
				inRes.p_IdEmpresa = in.p_IdEmpresa;
				inRes.p_IdUsuario = in.p_IdUsuario;
				inRes.p_IdModulo = in.p_IdModulo;
				inRes.p_simularOperacion = "N"; 
				inRes.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
				inRes.p_IdPais = in.p_IdPais;
				inRes.p_TblCarteraReserva = v_OperacionDetalle;
				
				
				logger.debug("[IngresarCompraVentaTermino] Inicio llamada a IngresoReserva");
				startTime = System.nanoTime();
				resultReserva = callReserva.logica(conn, inRes);
				logger.debug("[IngresarCompraVentaTermino] (" + (System.nanoTime() - startTime)/1000000 + ") Fin llamada a IngresoReserva");
				
				
				if (MensajeSalida.getNumeroErrores(resultReserva.P_OUT_MENSAJES) > 0){
					out.P_OUT_MENSAJES.addAll(resultReserva.P_OUT_MENSAJES);
					return out;
				}
	
				for (IngresarCompraVentaTermino_DetalleOperacion_IN detalleOperacion : in.p_DetalleOperacion){
					for (ReservaObj_OUT re: resultReserva.P_OUT_RESERVA){
						if (re.IdRegistro.equals(detalleOperacion.IdInternoDetalle) && re.Estado.equals("VIG")){
							detalleOperacion.IdReserva = re.IdReserva;
						}
					}
				}
			}			
			
			//* 03: INGRESO CUSTODIA, TESO, CTA INV 
			{
				String AfectaCustodia = "S";
				for (IngresarCompraVentaTermino_Operacion_IN ope: in.p_Operacion){
					ope.IdOperacion = ((OperacionObjOut)v_OutOperacionHash.get(ope.IdInternoOperacion)).IdOperacion;
					ope.FolioOperacion = ((OperacionObjOut)v_OutOperacionHash.get(ope.IdInternoOperacion)).FolioOperacion;
					ope.FolioCartera = 0L;
					//ope.IdOperacionRelacionada = ((OperacionObjOut)v_OutOperacionHash.get(ope.IdInternoOperacion)).IdOperacionRelacionada;
					ope.IdEstado = ((OperacionObjOut)v_OutOperacionHash.get(ope.IdInternoOperacion)).IdEstado;
					for (IngresarCompraVentaTermino_FormaPagoOperacion_IN fPag :in.p_FormasDePago){
						fPag.IdOperacion = ((OperacionObjOut)v_OutOperacionHash.get(ope.IdInternoOperacion)).IdOperacion;
						fPag.IdEstado = ope.IdEstado;
					}
					if ("CERRAR".equalsIgnoreCase(ope.Accion)){
						operacionCerrar.p_IdEmpresa = in.p_IdEmpresa;
						operacionCerrar.p_IdUsuario = in.p_IdUsuario;
						operacionCerrar.p_IdPais = in.p_IdPais;
						operacionCerrar.p_MonedaSistema = in.p_MonedaSistema;
						operacionCerrar.p_IdSucursal = in.p_IdSucursal;
						operacionCerrar.p_IdModulo = in.p_IdModulo; 
						operacionCerrar.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
						operacionCerrar.p_Operacion.add(ope);
						for (IngresarCompraVentaTermino_DetalleOperacion_IN det: in.p_DetalleOperacion){
							if (det.IdInternoOperacion.equals(ope.IdInternoOperacion))
								operacionCerrar.p_DetalleOperacion.add(det);
						}
						for (IngresarCompraVentaTermino_FormaPagoOperacion_IN pag: in.p_FormasDePago){
							if (pag.IdInternoOperacion.equals(ope.IdInternoOperacion))
								operacionCerrar.p_FormasDePago.add(pag);
						}
						cantCerrar++;
					}
					AfectaCustodia = ope.AfectaCustodia;
				}
				if (cantCerrar.compareTo(0L) > 0){
					//01 - LOG (Requerido)
					//entLogCus = cargaLogDatosCustodia(conn, operacionCerrar,tipInstHash, instOutHash);
					TicketAtencion = LogCustodia.logCustodia(conn,entLogCus);
					if (TicketAtencion.compareTo(0L) <= 0){
						mapMsg.clear();
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"SRV_MRF_SIN_TICKET_ATENCION",mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
				}
				errTot = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
				if (!errTot.equals(0L) ){
					return out;
				}
				//Ingreso servicio Custodia
				if (cantCerrar.compareTo(0L) > 0){
					List<IngresoCustodia.ObjDatosEntrada> ingresoOperacionPendiente = new ArrayList<>();
					//List<RetiroCustodia.ObjDatosEntrada> retiroOperacionPendiente = new ArrayList<>();
					List<Long> IdMovimientosEnTransito = new ArrayList<Long>();
					List<Long> IdReserva = new ArrayList<Long>();
					long contadorIdGenerarMovimientoCustodia = 0L;
					for (IngresarCompraVentaTermino_Operacion_IN ope: operacionCerrar.p_Operacion){
				    	for (IngresarCompraVentaTermino_DetalleOperacion_IN det: operacionCerrar.p_DetalleOperacion){
				    		if (ope.IdInternoOperacion.equals(det.IdInternoOperacion)){
						    	//CUSTODIA, puede ir a quedar operaciones pendientes
					    		//INGRESO
					    		IngresoCustodia.ObjDatosEntrada objIng = new IngresoCustodia.ObjDatosEntrada();
					    		objIng.IdGenerarMovimientoCustodia = det.IdReserva;	//ID Interno del proceso
					    		objIng.TablaAfectada				= "TRANSITO"; 					//CARTERA - SUBYACENTE - TRANSITO - GARANTIA
					    		objIng.EsRegistroNuevo				= "S";				//S o N
					    		//////////////////////////	SALDOS CARTERAS
					    		objIng.IdCustodia 				= det.IdCartera;						//Cuando es registro de saldo  - en caso de registro en tabla transito indica la informacion de la custodia a retirar
					    		objIng.FolioCustodia 			= det.FolioCartera;					//Cuando es resgitro de saldo
					    		//public String				GenerarNuevoFolioCustodia = "N";//S o N ,Cuando se le quiere asignar forsosamente un nuevo folio  
					    		/////////////////////////// OPERACION
					    		objIng.AccionOperacion					= "INGRESO";				//INGRESO - CERTIFICACION		
					    		objIng.IdCliente						= det.IdCliente;//det.IdTipoOperacion.contains("COM")?det.IdClienteCorredor : det.IdCliente; 
					    		objIng.IdPortafolio						= det.IdPortafolio;//det.IdTipoOperacion.contains("COM")?det.IdPortafolioCorredor : det.IdPortafolio; 
					    		//public Long					IdCuentaCustodioPortafolio;
					    		objIng.IdClienteRelacionado				= det.IdClienteCorredor;//det.IdTipoOperacion.contains("COM")?det.IdCliente : det.IdClienteCorredor; 					//----------------------------------------------------
					    		objIng.IdPortafolioRelacionado			= det.IdPortafolioCorredor;//det.IdTipoOperacion.contains("COM")?det.IdPortafolio : det.IdPortafolioCorredor; 				//----------------------------------------------------
					    		//public Long					IdCuentaCustodioPortafolioRelacionado;	//----------------------------------------------------	
					    		objIng.FechaOperacion					= Fecha.toDate(ope.FechaOperacion);
					    		objIng.FechaVencimientoOperacion 		= Fecha.toDate(ope.FechaVencimiento);;
					    		objIng.PlazoEnDiasOperacion			= ope.Plazo;
					    		objIng.IdTipoOperacion					= ope.IdTipoOperacion;
					    		objIng.IdGrupoOperacion				= ope.IdOperacion; 
					    		objIng.IdOperacion						= det.IdReserva;//ope.IdOperacion;
					    		objIng.IdConcepto						= ope.Concepto.IdConcepto;
					    		//public Long					FolioOperacionRelacionada;
					    		objIng.IdCondicionLiquidacion			= ope.IdCondicionLiquidacion; 
					    		objIng.FechaLiquidacion				= Fecha.toDate(ope.FechaLiquidacion);		
					    		objIng.IdTipoProducto					= ope.IdTipoProducto; 
					    		objIng.IdTipoCanalIngreso				= ope.IdTipoCanalIngreso;
					    		objIng.CodigoIFRS						= det.CodigoIFRS;
					    		objIng.IdTipoComprobante				= "MES";
					    		objIng.FolioComprobante				= ope.IdOperacion; 
					    		objIng.FechaComprobante				= Fecha.toDate(ope.FechaOperacion); 
					    		//public String				FolioExternoReferencia; 
					    		objIng.IdMesa							= det.IdMesa;//ope.IdMesaOrigen; 
					    		objIng.IdBolsa							= det.IdBolsa;
					    		objIng.IdUsuario						= in.p_IdUsuario;
					    		objIng.IdSucursal						= in.p_IdSucursal; 
					    		objIng.IdAgente						= ope.IdAgente; 
					    		objIng.Glosa							= ope.Glosa;
					    		objIng.PermiteCertificacionPorUsuario 	= "S";		//Mov en transito
					    		/////////////////////////// INSTRUMENTO
					    		objIng.IdInstrumento					= det.IdInstrumento;
					    		objIng.Nemotecnico						= det.Nemotecnico;
					    		//public Long					IdInstrumentoAnterior;
					    		objIng.IdMonedaInstrumento				= det.IdMonedaLiquidacionInstrumento;//((InformacionInstrumento) infoInsHash.get(det.IdInstrumento)).IdMoneda;
					    		objIng.IdMonedaEmision					= det.IdMonedaEmisionInstrumento;
					    		objIng.IdTipoReajusteInstrumento		= det.IdTipoReajuste;//((InformacionInstrumento) infoInsHash.get(det.IdInstrumento)).IdTipoReajuste;
					    		objIng.IdEmisor							= det.IdEmisor;//((InformacionInstrumento) infoInsHash.get(det.IdInstrumento)).IdEmisor;
					    		objIng.EsFungible						= det.EsFungible;//((InformacionInstrumento) infoInsHash.get(det.IdInstrumento)).EsDivisible;
					    		objIng.IdBoveda						= det.IdBoveda;		
					    		//public String				CuentaBoveda;			//????
					    		objIng.EsNominal						= "S".equals(ope.AfectaCustodia)?"N":"S";// det.FolioCartera.equals(0L)?"S":"N";
					    		objIng.FechaVencimientoInstrumento		= Fecha.toDate(det.FechaVencimiento);		//SOLO PARA IF
					    		objIng.FechaEmisionInstrumento			= ((InformacionInstrumento) infoInsHash.get(det.IdInstrumento)).FechaEmision;			//SOLO PARA IF
					    		/////////////////////////// CANTIDADES Y MONTOS
					    		//public String				EsPagaderoEn; 
					    		objIng.Cantidad						= det.Cantidad; 
					    		objIng.CorteMinimo						= det.CorteMinimo; 
					    		objIng.ValorRescate					= det.ValorRescate; 
					    		objIng.PrecioCompra					= det.Precio; 
					    		objIng.TasaCompra						= det.Tasa;
					    		objIng.MontoCompra						= det.Monto; 
					    		objIng.MontoCompraMonedaInstrumento		= det.MontoLiquidacion; 
					    		objIng.TasaAgente						= ope.TasaAgente; 

					    		ingresoOperacionPendiente.add(objIng);
					    		
					    		if("VENTER".equals(det.IdTipoOperacion) && "S".equals(objIng.EsNominal) && objIng.FolioCustodia.compareTo((long) 0) > 0) {
					    			IngresoCustodia.ObjDatosEntrada objIngCorredora = new IngresoCustodia.ObjDatosEntrada();
					    			objIngCorredora = Utils.copyPojo(objIng, objIngCorredora);
					    			objIngCorredora.IdGenerarMovimientoCustodia = ++contadorIdGenerarMovimientoCustodia;
					    			objIngCorredora.IdCliente = objIng.IdClienteRelacionado;
					    			objIngCorredora.IdPortafolio = objIng.IdPortafolioRelacionado;
					    			objIngCorredora.PermiteCertificacionPorUsuario = "N";
					    			objIngCorredora.EsNominal = "N";
					    			ingresoOperacionPendiente.add(objIngCorredora);
					    		}
					    		
				    		}
				    		
				    		IdReserva.add(det.IdReserva);
				    	}
				    }
					
					IngresoCustodia.IngresoCustodiaEntrada cusIn = new IngresoCustodia.IngresoCustodiaEntrada();
					cusIn.p_IdEmpresa 		= in.p_IdEmpresa;
				    cusIn.p_IdModulo 		= in.p_IdModulo;
				    cusIn.p_SimularOperacion= "N";
				    cusIn.p_TicketAtencion 	= TicketAtencion;
				    cusIn.p_ValorizacionAutomatica = "N";
					cusIn.p_DatosEntrada 	= ingresoOperacionPendiente;//agregar los datos de custodia
					HashMap<String, Object> ingresoOperacionPendienteMap =Utils.List2Hash(ingresoOperacionPendiente, "IdGenerarMovimientoCustodia");						
					IngresoCustodia cus = new IngresoCustodia();
									
					logger.debug("[IngresarCompraVentaTermino] Inicio llamada a IngresoCustodia");
					startTime = System.nanoTime();
					cusTranOut = cus.logica(conn, cusIn);
					logger.debug("[IngresarCompraVentaTermino] (" + (System.nanoTime() - startTime)/1000000 + ") Fin llamada a IngresoCustodia");					

					
					
					out.P_OUT_MENSAJES.addAll(cusTranOut.P_OUT_MENSAJES);
					if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0){
						return out;
					}
					for (IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida c: cusTranOut.P_OUT_DATOSSALIDA){
						mapMsg.clear();
						mapMsg.put("IdOperacion",c.IdGenerarMovimientoCustodia.toString());
						//out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_OK,"INGRESO_TRANSITO_CUSTODIA_OK",mapMsg,conn,in.p_IdEmpresa));
						Id++;
						OperacionMesaCustodiaObj tra = new OperacionMesaCustodiaObj();
						tra.IdMovimientoEnTransito 	= c.IdCustodia;
						tra.IdReserva				= c.IdGenerarMovimientoCustodia;
						
						// ---------------------------------------------------------------------------------------
						// SI es nominal queda pendiente....
						// ---------------------------------------------------------------------------------------						
						//IngresoCustodia.ObjDatosEntrada obj = (IngresoCustodia.ObjDatosEntrada) ingresoOperacionPendienteMap.get(c.IdGenerarMovimientoCustodia);
						//if (!obj.EsNominal.equals("S")){
						IdMovimientosEnTransito.add(c.IdCustodia);
						//}
					}
					
					//* 03.0 hago los retiros e ingresos correspondientes
					CrearOperacionMesaCustodia transitoCus = new CrearOperacionMesaCustodia ();
					CrearOperacionMesaCustodia_IN inTransitoCus = new CrearOperacionMesaCustodia_IN ();
					inTransitoCus.p_IdEmpresa 			= in.p_IdEmpresa;
					inTransitoCus.p_IdUsuario			= in.p_IdUsuario;
					inTransitoCus.p_FechaSistema		= in.p_FechaSistema;
					inTransitoCus.p_IdSucursal			= in.p_IdSucursal;
					inTransitoCus.p_IdModulo			= in.p_IdModulo;
					inTransitoCus.p_UsaReglasNegocio	= in.p_UsaReglasNegocio;
					inTransitoCus.p_TicketAtencion		= TicketAtencion;
					inTransitoCus.p_EjecutarValorizador = "N";  // No volver a ejecutar el valorizador ya que se va a ejecutase al final de este java
					inTransitoCus.p_AfectaCustodia		= AfectaCustodia;
					
					inTransitoCus.p_MovimientoEnTransito.addAll(IdMovimientosEnTransito);
					inTransitoCus.p_Reserva.addAll(IdReserva);
				
					logger.debug("[IngresarCompraVentaTermino] Inicio llamada a CrearOperacionMesaCustodia");
					startTime = System.nanoTime();
					CrearOperacionMesaCustodia_OUT transitoCusOut = transitoCus.logica(conn, inTransitoCus);
					logger.debug("[IngresarCompraVentaTermino] (" + (System.nanoTime() - startTime)/1000000 + ") Fin llamada a CrearOperacionMesaCustodia");
					
					//Se deja esto antes de ejecutar CacheValorizacion.valorizar ya que no se realizan ingresos o retiros en Cartera
					out.P_OUT_MENSAJES.addAll(transitoCusOut.P_OUT_MENSAJES);
					if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0){
						return out;
					}
					
					logger.debug("[IngresarCompraVentaTermino] Inicio CacheValorizacion.valorizar");
					startTime = System.nanoTime();
					//El valorizador debe ejecutar en este punto ya que valoriza la custodia ingresada en este java, mas la que movió en CrearOperacionMesaCustodia
					CacheValorizacion.valorizar(conn);
					logger.debug("[IngresarCompraVentaTermino] (" + (System.nanoTime() - startTime)/1000000 + ") Fin CacheValorizacion.valorizar");
					
					for (IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida c: transitoCusOut.P_OUT_INGRESOS){
						mapMsg.clear();
						mapMsg.put("IdOperacion",c.IdGenerarMovimientoCustodia.toString());
						//out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_OK,"INGRESO_CUSTODIA_OK",mapMsg,conn,in.p_IdEmpresa));
						Id++;
						OperacionMesaCustodiaObj tra = new OperacionMesaCustodiaObj();
						tra.IdMovimientoEnTransito 	= c.IdCustodia;
						tra.IdReserva				= c.IdGenerarMovimientoCustodia;
						IdMovimientosEnTransito.add(c.IdCustodia);
					}
					for (RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida c: transitoCusOut.P_OUT_RETIROS){
						mapMsg.clear();
						mapMsg.put("IdOperacion",c.IdGenerarMovimientoCustodia.toString());
						//out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_OK,"RETIRO_CUSTODIA_OK",mapMsg,conn,in.p_IdEmpresa));
						Id++;
						OperacionMesaCustodiaObj tra = new OperacionMesaCustodiaObj();
						tra.IdMovimientoEnTransito 	= c.IdCustodia;
						tra.IdReserva				= c.IdGenerarMovimientoCustodia;
						IdMovimientosEnTransito.add(c.IdCustodia);
					}
					
					{
						//genero INGRESO EGRESO a Tesoreria
						CrearIngresoEgreso tes = new CrearIngresoEgreso();
						CrearIngresoEgreso_Entrada entTes = new CrearIngresoEgreso_Entrada();
						entTes.p_IdEmpresa = in.p_IdEmpresa;
						entTes.p_IdUsuario = in.p_IdUsuario;
						entTes.p_IdModulo = in.p_IdModulo;
						entTes.p_SimularOperacion = "N";
						entTes.p_UsaReglasNegocio = in.p_UsaReglasNegocio;		
						entTes.p_EsIngresoAutomatico = "S";        //S: Ingreso automático   N: Ingreso Manual
						entTes.p_FechaMovimiento = in.p_FechaSistema;   // Fecha de Sistema(si no viene le asigna fecha sistema)  //////// nuevo jv
						
						for (IngresarCompraVentaTermino_Operacion_IN ope: operacionCerrar.p_Operacion){
							IngresoEgreso_Entrada IEGen = new IngresoEgreso_Entrada();
							for (IngresarCompraVentaTermino_FormaPagoOperacion_IN fPag :operacionCerrar.p_FormasDePago){
								if (ope.IdOperacion.equals(fPag.IdOperacion) &&  fPag.AfectarTesoreria.equals("S")){									
									IEGen.NumeroReferencia 				= fPag.IdOperacion;  				//	BIGINT 
									IEGen.IdTipoMovimiento 				= fPag.IdTipoMovimientoTes;  		 		//	NVARCHAR(6)    -- MOVING, MOVEGR
									IEGen.IdTipoPersona 				= "CLI";  		 		//	NVARCHAR(3)    -- Es el IdTipoSocioDeNegocio (CLI / EMP / PRO / OPE)
									IEGen.IdPersona						= ope.Persona.IdPersona;  		 			//	BIGINT
									IEGen.IdPortafolio 					= ope.IdPortafolio;  		 			//	BIGINT 
									IEGen.IdMoneda 						= fPag.IdMoneda;//Liquidacion;  		 				//	NVARCHAR(3)
									IEGen.Monto 						= IEGen.Monto.add(fPag.MontoFormaDePago);//LiquidacionFormaDePago;//fPag.MontoFormaDePago;  		 				//	DECIMAL(26,8)
									IEGen.FechaLiquidacion 				= fPag.FechaLiquidacionFormaDePago;  		 		//	NVARCHAR(10)                                                                      
									IEGen.IdConcepto 					= ope.Concepto.IdConcepto;  		 			//	NVARCHAR(6)
									IEGen.IdTipoPersonaTitularPago 		= "CLI";  		//	NVARCHAR(3)    -- Es el IdTipoSocioDeNegocio (CLI / EMP / PRO / OPE)
									IEGen.IdPersonaTitularPago 			= ope.Persona.IdPersona;  		 	//	BIGINT
									IEGen.IdPortafolioTitularPago 		= ope.IdPortafolio;  		//	BIGINT
									IEGen.IdCaja 						= 0L;  		 				//	BIGINT
									IEGen.SecuenciaAgrupacion 			= ""; 		//	NVARCHAR(40)  -- Movimientos con una misma secuencia se agrupan en un mismo comprobante(también influyen parámetros de agrupación)
									IEGen.SecuenciaRelacionada 			= 0L;	 	//	BIGINT		  -- Permite relacionar distintos mov y generar una orden de liquidación entre ellos
									IEGen.Glosa 						= ope.Glosa;  		 				//	NVARCHAR(260)														   	
								   	IEGen.RequiereAutorizacion 			= ope.RequiereAutorizacion;  
									
									//Parámetros Referenciales(Opcionales)
									// IEGen.IdEmpleadoDigitador			= operacionCerrar.p_IdUsuario;  		 	//	BIGINT
									IEGen.IdEmpleadoDigitador			= ope.IdEmpleado;  		 	//	BIGINT									
									IEGen.IdSucursalEmpleadoDigitador	= operacionCerrar.p_IdSucursal;  	//	BIGINT
									IEGen.IdTipoProducto				= ope.IdTipoProducto;  		 		//	NVARCHAR(4)
									IEGen.IdTipoOperacion				= ope.IdTipoOperacion;  		 		//	NVARCHAR(6)									    
									IEGen.IdTipoComprobante				= "MES";  		 	//	NVARCHAR(3)
									IEGen.IdComprobante					= ope.FolioOperacion;  		 		//	BIGINT
									IEGen.FechaComprobante				= Fecha.toFormat(Fecha.toDate(ope.FechaOperacion),"YYYY-MM-dd hh:mm:ss");  		 		//	NVARCHAR(40)
									IEGen.FechaLiquidacionComprobante	= ope.FechaLiquidacion;  	//	NVARCHAR(40)
									
									
									IEGen.IdOperacion					= fPag.IdOperacion;  		 			//	BIGINT			
									IEGen.FechaOperacion				= Fecha.toFormat(Fecha.toDate(ope.FechaOperacion),"YYYY-MM-dd hh:mm:ss");  		 		//	NVARCHAR(40)
									IEGen.IdCondicionLiquidacion		= ope.IdCondicionLiquidacion;  		//	NVARCHAR(2)
									IEGen.IdBolsa						= "";  		 				//	NVARCHAR(3)
									IEGen.IdMonedaOperacion				= ope.IdMonedaLiquidacion;  		 	//	NVARCHAR(3)
									IEGen.MontoOperacion				= IEGen.MontoOperacion.add(fPag.MontoFormaDePago);//LiquidacionFormaDePago;   				//	DECIMAL(26,8)
									IEGen.IdTipoReajuste				= ope.IdTipoReajuste;  		 		//	NVARCHAR(3)
									IEGen.FechaParidad					= "0001-01-01";  		 			//	NVARCHAR(40)
									IEGen.ValorParidad					= BigDecimal.ZERO;   				// 	DECIMAL(26,8)
									IEGen.FolioCartera					= ope.FolioCartera != null ? ope.FolioCartera:0;  		 			//	BIGINT
									IEGen.IdInstrumento					= 0L;  		 		//	BIGINT
									IEGen.IdTipoInstrumento				= "";  		 	//	NVARCHAR(3)
									IEGen.IdClasificacionTipoInstrumento= ""; //	NVARCHAR(3)
								    //Indicadores
									IEGen.Bilateral 					= "N";		        //	NVARCHAR(1)  -- Liquidación bilateral entre corredores, no pasa por el CCLV
									IEGen.AgruparEnComprobante 			= "N"; 	//	NVARCHAR(1)  -- S/N: permite agrupar varios mov. en un comprobante
									IEGen.MueveCuentaInversion			= "N";  			//	NVARCHAR(1)  -- S/N: mueve cuenta inversion // *** No se usa el campo, se debe borrar ? ***
									IEGen.TitularPago					= "S";  		 			//	NVARCHAR(1)  -- S/N: Socio de negocio que genera el movimiento es quién cobra o paga
									IEGen.GeneraComprobante 			= "S"; 		//	NVARCHAR(1)  -- S/N: genera comprobante automático									
									
								}
							}
							entTes.p_IngresoEgresoGenerar.add(IEGen);
							for (IngresarCompraVentaTermino_FormaPagoOperacion_IN fPag :operacionCerrar.p_FormasDePago){
								if (ope.IdOperacion.equals(fPag.IdOperacion) &&  fPag.AfectarTesoreria.equals("S")){	
									FormaDePago_Entrada FPagTes = new FormaDePago_Entrada();
									FPagTes.NumeroReferencia			= fPag.IdOperacion;				//  BIGINT        Id Referencial (para unir el movimiento con su forma de pago)
									FPagTes.NumeroReferenciaFormaDePago	= fPag.IdInternoPagoOperacion;	//  BIGINT        Id Referencia para FP (1,2,3,..) debe ser único por FP aunque sean de distintos movimientos
									FPagTes.IdMedioDePago				= fPag.FormaDePago.IdMedioDePago;          		//	NVARCHAR(3)   Ej.: CHE, EFE, TRA, VVI, CIN
									FPagTes.IdModalidadDePago			= fPag.IdModalidadDePago;      		//	NVARCHAR(6)   Ej.: MODCAJ: Por Caja   MODDEP: Depósito
									FPagTes.IdMoneda					= fPag.IdMoneda;//Liquidacion;               		//	NVARCHAR(3)
									FPagTes.Monto						= fPag.MontoFormaDePago;//LiquidacionFormaDePago; 							//	DECIMAL(26,8)
									FPagTes.IdCuentaBancariaCorredora	= 0L; 		//	BIGINT
									FPagTes.IdTarjetaBancariaCorredora	= 0L; 	//	BIGINT
									FPagTes.IdChequera					= 0L; 					//	BIGINT
									FPagTes.NumeroDocumentoCorredora	= 0L; 		//	BIGINT
									if ( ope.IdTipoOperacion.equals("COMTER") && fPag.PreferenciaDePago != null) {
										FPagTes.IdCuentaBancariaSocio		= fPag.PreferenciaDePago.IdCuentaBancaria; 			//	BIGINT
										FPagTes.IdBancoSocio				= fPag.PreferenciaDePago.IdBancoCuentaBancaria; 					//	BIGINT
										FPagTes.NombreCuentaBancariaSocio	= fPag.PreferenciaDePago.NombreBancoCuentaBancaria;		//	NVARCHAR(100)
										FPagTes.NumeroCuentaBancariaSocio	= fPag.PreferenciaDePago.NumeroCuentaBancaria;		//	NVARCHAR(100)
										FPagTes.IdTipoCuentaBancariaSocio	= fPag.PreferenciaDePago.IdTipoCuentaBancaria;		//	NVARCHAR(4)
										FPagTes.NombreTipoCuentaBancariaSocio = fPag.PreferenciaDePago.NombreTipoCuentaBancaria; 	//  NVARCHAR(100)
										FPagTes.IdTarjetaBancariaSocio		= fPag.PreferenciaDePago.IdTarjetaBancaria;			//	BIGINT
										FPagTes.IdTipoTarjetaBancariaSocio	= fPag.PreferenciaDePago.IdTipoTarjetaBancaria;  	//	NVARCHAR(40)
										FPagTes.NombreTipoTarjetaBancariaSocio = fPag.PreferenciaDePago.NombreBancoTarjetaBancaria; //	NVARCHAR(100)
										FPagTes.NumeroTarjetaBancariaSocio	= fPag.PreferenciaDePago.NumeroTarjetaBancaria;  	//	NVARCHAR(100)
										FPagTes.IdCuentaBancariaCorredora	= 0L; 		//	BIGINT
										FPagTes.IdTarjetaBancariaCorredora	= 0L; 	//	BIGINT
										FPagTes.FechaDocumentoPagoCorredora	= "0001-01-01 00:00:00";	//	NVARCHAR(40)
									}
									else if ( ope.IdTipoOperacion.equals("VENTER") && fPag.PreferenciaDePago != null) {
										FPagTes.IdCuentaBancariaCorredora	= fPag.PreferenciaDePago.IdCuentaBancaria; 			//	BIGINT
										FPagTes.IdTarjetaBancariaCorredora 	= fPag.PreferenciaDePago.IdTarjetaBancaria;
										FPagTes.IdCuentaBancariaSocio		= 0L; 			//	BIGINT
										FPagTes.IdTarjetaBancariaSocio		= 0L;
										FPagTes.NumeroTarjetaBancariaSocio	= "";  	//	NVARCHAR(100)
										FPagTes.FechaDocumentoPagoCorredora	= Fecha.toFormat(Fecha.toDate(fPag.FechaLiquidacionFormaDePago),"YYYY-MM-dd hh:mm:ss");
										FPagTes.IdCuentaBancariaSocio		= 0L; 			//	BIGINT
										FPagTes.IdBancoSocio				= 0L; 					//	BIGINT
										FPagTes.NombreCuentaBancariaSocio	= "";		//	NVARCHAR(100)
										FPagTes.NumeroCuentaBancariaSocio	= "";		//	NVARCHAR(100)
										FPagTes.IdTipoCuentaBancariaSocio	= "";		//	NVARCHAR(4)
										FPagTes.NombreTipoCuentaBancariaSocio = ""; 	//  NVARCHAR(100)
										FPagTes.IdTarjetaBancariaSocio		= 0L;			//	BIGINT
										FPagTes.IdTipoTarjetaBancariaSocio	= "";  	//	NVARCHAR(40)
										FPagTes.NombreTipoTarjetaBancariaSocio = ""; //	NVARCHAR(100)
										FPagTes.NumeroTarjetaBancariaSocio	= "";
									}
									else {
										FPagTes.IdCuentaBancariaSocio		= 0L; 			//	BIGINT
										FPagTes.IdBancoSocio				= 0L; 					//	BIGINT
										FPagTes.NombreCuentaBancariaSocio	= "";		//	NVARCHAR(100)
										FPagTes.NumeroCuentaBancariaSocio	= "";		//	NVARCHAR(100)
										FPagTes.IdTipoCuentaBancariaSocio	= "";		//	NVARCHAR(4)
										FPagTes.NombreTipoCuentaBancariaSocio = ""; 	//  NVARCHAR(100)
										FPagTes.IdTarjetaBancariaSocio		= 0L;			//	BIGINT
										FPagTes.IdTipoTarjetaBancariaSocio	= "";  	//	NVARCHAR(40)
										FPagTes.NombreTipoTarjetaBancariaSocio = ""; //	NVARCHAR(100)
										FPagTes.NumeroTarjetaBancariaSocio	= "";
										FPagTes.IdCuentaBancariaCorredora	= 0L; 		//	BIGINT
										FPagTes.IdTarjetaBancariaCorredora	= 0L; 	//	BIGINT
										FPagTes.FechaDocumentoPagoCorredora	= "0001-01-01 00:00:00";	//	NVARCHAR(40)
									}
									FPagTes.NumeroSerieSocio			= "";  				//	NVARCHAR(40)
									FPagTes.NumeroDocumentoSocio		= 0L;			//	BIGINT
									FPagTes.FechaDocumentoPagoSocio		= Fecha.toFormat(Fecha.toDate(fPag.FechaLiquidacionFormaDePago),"YYYY-MM-dd hh:mm:ss");  		//	NVARCHAR(40)
									entTes.p_FormaDePago.add(FPagTes);
								}
							}
							
						}

						logger.debug("MRF.Llamada Ingreso Tesoreria");
						if (entTes.p_IngresoEgresoGenerar.size() > 0 && entTes.p_FormaDePago.size() > 0){
							
							logger.debug("[IngresarCompraVentaTermino] Inicio llamada a CrearIngresoEgreso");
							startTime = System.nanoTime();
							tesOut = tes.logica(conn, entTes);
							logger.debug("[IngresarCompraVentaTermino] (" + (System.nanoTime() - startTime)/1000000 + ") Fin llamada a CrearIngresoEgreso");
							
							Long mensajesOk = 0L;
							for (MensajeSalida s : tesOut.P_OUT_MENSAJES) {
								if (s.tipo == "OK") {
									mensajesOk++;
									out.P_OUT_MENSAJES.add(s);
								}
							}
							
							if(mensajesOk.compareTo(0L) == 0) {
								if (MensajeSalida.getNumeroErrores(tesOut.P_OUT_MENSAJES) > 0){
									out.P_OUT_MENSAJES.addAll(tesOut.P_OUT_MENSAJES);								
									return out;
								}
							}
						}
					}
					
					

					// --------------------------------------------------------------------------------------
					// VALIDA FLUJO CAJA (Cliente y Corredor)(Solo cuando la forma de pago es cuenta inversion(CIN) y si el portafolio opera con cuenta inversion)
					
					{
						class FlujoCajaElem {
							String 	IdMoneda;
							String 	Fecha;
							Long 	IdPortafolio;
							
							public FlujoCajaElem(String moneda,String fecha,Long idPortafolio) {
								this.IdMoneda 		= moneda;
								this.Fecha 			= fecha;
								this.IdPortafolio 	= idPortafolio;
							}
							
							@Override
							public boolean equals(Object o) {
								FlujoCajaElem e = (FlujoCajaElem)o;
								return this.IdMoneda.equalsIgnoreCase(e.IdMoneda) && this.Fecha.equalsIgnoreCase(e.Fecha) && this.IdPortafolio.equals(e.IdPortafolio);
							}
							
							@Override
							public int hashCode() {
								return (this.IdMoneda + this.Fecha + this.IdPortafolio).hashCode();
							}
						}
							
						
						SaldoFlujoCaja.ServiceEntrada entradaFlujoCaja = new SaldoFlujoCaja.ServiceEntrada();					
						HashSet<SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR> portafoliosFlujoCaja = new HashSet<>();						
						HashMap<FlujoCajaElem,BigDecimal> totalOperacionesPortafolio = new HashMap<>();
						HashMap<FlujoCajaElem,BigDecimal> totalOperacionesCorredora = new HashMap<>();
						java.sql.Date fechaLiquidacionMinima = Fecha.toDate(in.p_FechaSistema);
						java.sql.Date fechaLiquidacionMaxima = Fecha.toDate(in.p_FechaSistema);	
						

						
						
						// Prepara datos para llamada flujo caja
						for (IngresarCompraVentaTermino_Operacion_IN ope: operacionCerrar.p_Operacion){
							for (IngresarCompraVentaTermino_FormaPagoOperacion_IN fPag :operacionCerrar.p_FormasDePago){
								
								
								if (fPag.FormaDePago.IdMedioDePago.equalsIgnoreCase("CIN"))
								{
									if (ope.IdOperacion.equals(fPag.IdOperacion)){
										
										// Para validar flujo caja Cliente
										if (fPag.IdTipoMovimientoCtaCli.equalsIgnoreCase("MOVCAR") && indicadoresPortafolio.get(ope.IdPortafolio).OperaCuentaInversion.equalsIgnoreCase("S"))
										{		
											// Portafolios a consultar flujo caja
											SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR port = new SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR();
											port.IdMoneda 		= fPag.IdMoneda;
											port.IdPortafolio 	= ope.IdPortafolio; //portafolio cliente
											portafoliosFlujoCaja.add(port);
											
											// Calcula fecha minima y maxima
											if (Fecha.toDate(fPag.FechaLiquidacionFormaDePago).before(fechaLiquidacionMinima)) {
												fechaLiquidacionMinima = Fecha.toDate(fPag.FechaLiquidacionFormaDePago);
											}
											
											if (Fecha.toDate(fPag.FechaLiquidacionFormaDePago).after(fechaLiquidacionMaxima)) {
												fechaLiquidacionMaxima = Fecha.toDate(fPag.FechaLiquidacionFormaDePago);
											}
											
											
											// Obtiene total de todas las operaciones por portafolio Cliente
											FlujoCajaElem llaveCli = new FlujoCajaElem(fPag.IdMoneda, fPag.FechaLiquidacionFormaDePago, ope.IdPortafolio);
											if (!totalOperacionesPortafolio.containsKey(llaveCli)) {
												totalOperacionesPortafolio.put(llaveCli, BigDecimal.ZERO);
											}
											
											BigDecimal totalOperaciones = totalOperacionesPortafolio.get(llaveCli);									
											totalOperaciones = totalOperaciones.add(fPag.MontoFormaDePago);
											totalOperacionesPortafolio.put(llaveCli, totalOperaciones);
											
										}
										
										// Para validar Flujo Caja Corredora
										if (fPag.IdTipoMovimientoCtaCorr.equalsIgnoreCase("MOVCAR") && indicadoresPortafolio.get(ope.IdPortafolioCorredor).OperaCuentaInversion.equalsIgnoreCase("S"))
										{	
											//Portafolios a consuktar flujo caja
											SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR port2 = new SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR();
											port2.IdMoneda 		= fPag.IdMoneda;
											port2.IdPortafolio 	= ope.IdPortafolioCorredor; //portafolio corredora
											portafoliosFlujoCaja.add(port2);
											
											// Calcula fecha minima y maxima
											if (Fecha.toDate(fPag.FechaLiquidacionFormaDePago).before(fechaLiquidacionMinima)) {
												fechaLiquidacionMinima = Fecha.toDate(fPag.FechaLiquidacionFormaDePago);
											}
											
											if (Fecha.toDate(fPag.FechaLiquidacionFormaDePago).after(fechaLiquidacionMaxima)) {
												fechaLiquidacionMaxima = Fecha.toDate(fPag.FechaLiquidacionFormaDePago);
											}
											
											// Obtiene total de todas las operaciones por portafolio Corredora
											FlujoCajaElem llaveCor = new FlujoCajaElem(fPag.IdMoneda, fPag.FechaLiquidacionFormaDePago, ope.IdPortafolioCorredor);
											if (!totalOperacionesCorredora.containsKey(llaveCor)) {
												totalOperacionesCorredora.put(llaveCor, BigDecimal.ZERO);
											}
											
											BigDecimal totalOperacionesCor = totalOperacionesCorredora.get(llaveCor);													
											totalOperacionesCor = totalOperacionesCor.add(fPag.MontoFormaDePago);
											totalOperacionesCorredora.put(llaveCor, totalOperacionesCor);
										}

									}
								}
							}
						}
				
						// Llamada flujo caja
					    if (portafoliosFlujoCaja.size() > 0)
					    {
					    	entradaFlujoCaja.P_IDEMPRESA 	= in.p_IdEmpresa;
					    	entradaFlujoCaja.P_FECHADESDE 	= Fecha.toStringDate(fechaLiquidacionMinima);
							entradaFlujoCaja.P_FECHAHASTA 	= Fecha.toStringDate(fechaLiquidacionMaxima);
							entradaFlujoCaja.P_USO 			= "MESA_RF";
							entradaFlujoCaja.P_PORTAFOLIOACONSULTAR = new ArrayList<ObjPORTAFOLIOACONSULTAR>(portafoliosFlujoCaja);
							
							SaldoFlujoCaja saldoFlujoCajaService = new SaldoFlujoCaja();
							SaldoFlujoCaja.ServiceSalida salidaFlujoCaja = saldoFlujoCajaService.logica(conn,entradaFlujoCaja);
							

							//Validacion flujo de Caja
							List<ObjFLUJOCAJA> asdf = salidaFlujoCaja.P_OUT_FLUJOCAJA;
							for (ObjFLUJOCAJA flj : asdf) {
								
								for (IngresarCompraVentaTermino_Operacion_IN ope: operacionCerrar.p_Operacion){
									for (IngresarCompraVentaTermino_FormaPagoOperacion_IN fPag :operacionCerrar.p_FormasDePago){

										if (fPag.FormaDePago.IdMedioDePago.equalsIgnoreCase("CIN"))
										{
											if (ope.IdOperacion.equals(fPag.IdOperacion))
											{
									        		// Valida Flujo Caja Cliente
													if (fPag.IdTipoMovimientoCtaCli.equalsIgnoreCase("MOVCAR") && indicadoresPortafolio.get(ope.IdPortafolio).OperaCuentaInversion.equalsIgnoreCase("S"))
													{	
														if (fPag.IdMoneda.equalsIgnoreCase(flj.IdMoneda) &&	fPag.FechaLiquidacionFormaDePago.equalsIgnoreCase(flj.FechaSaldo) && ope.IdPortafolio.equals(flj.IdPortafolio)) 
											        	{																		
											        		FlujoCajaElem llaveCli = new FlujoCajaElem(fPag.IdMoneda, fPag.FechaLiquidacionFormaDePago, ope.IdPortafolio);													
															BigDecimal totalOperacionesCli = totalOperacionesPortafolio.get(llaveCli);
															
															if (totalOperacionesCli.compareTo(flj.Saldo) > 0)
															{															
																String msg=Diccionario.getMensaje(conn,in.p_IdEmpresa,"FLUJO_CAJA_INSUFICIENTE_PORTAFOLIO").add("DocumentoIdentidad", ope.Persona.DocumentoIdentidad).msg();
																out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_ERROR, "FLUJO_CAJA_INSUFICIENTE_PORTAFOLIO", msg));
																return out;
															}
											        	}
													}
									        		
													// Valida Flujo Caja Corredora
													if (fPag.IdTipoMovimientoCtaCorr.equalsIgnoreCase("MOVCAR") && indicadoresPortafolio.get(ope.IdPortafolioCorredor).OperaCuentaInversion.equalsIgnoreCase("S"))
													{
														if (fPag.IdMoneda.equalsIgnoreCase(flj.IdMoneda) &&	fPag.FechaLiquidacionFormaDePago.equalsIgnoreCase(flj.FechaSaldo) && ope.IdPortafolioCorredor.equals(flj.IdPortafolio)) 
											        	{															
															
											        		FlujoCajaElem llaveCor = new FlujoCajaElem(fPag.IdMoneda, fPag.FechaLiquidacionFormaDePago, ope.IdPortafolioCorredor);												
															BigDecimal totalOperacionesCor = totalOperacionesCorredora.get(llaveCor);
															
															if (totalOperacionesCor.compareTo(flj.Saldo) > 0)
															{
																String msg=Diccionario.getMensaje(conn,in.p_IdEmpresa,"FLUJO_CAJA_INSUFICIENTE_PORTAFOLIO").add("DocumentoIdentidad", ope.DocumentoIdentidadCorredor).msg();
																out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_ERROR, "FLUJO_CAJA_INSUFICIENTE_PORTAFOLIO", msg));
																return out;
															}
											        	}
													}
											}
										}
									}	
								}
							}
					    }
					}
		
					
					
					{
						//genero CARGO ABONO cta inversion
						CrearCargoAbonoEntrada entCta = new CrearCargoAbonoEntrada();
						entCta.p_IdEmpresa = in.p_IdEmpresa;
						entCta.p_IdUsuario = in.p_IdUsuario;
						entCta.p_IdModulo = in.p_IdModulo;
						entCta.p_simularOperacion = "N";
						entCta.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
						entCta.p_FechaSistema = in.p_FechaSistema;
						entCta.p_EsIngresoManual	= "N";
						for (IngresarCompraVentaTermino_Operacion_IN ope: operacionCerrar.p_Operacion){
							
							//obtener monto total de las formas de pago
							BigDecimal monto = operacionCerrar.p_FormasDePago.stream()
									.filter(fp -> fp.RecibePagaPortafolio.equals("P") && fp.IdInternoOperacion.equals(ope.IdInternoOperacion))
									.map(fp -> fp.MontoFormaDePago)
									.reduce(BigDecimal.ZERO, BigDecimal::add);
							
							for (IngresarCompraVentaTermino_FormaPagoOperacion_IN fPag :operacionCerrar.p_FormasDePago){
								if (ope.IdOperacion.equals(fPag.IdOperacion)){
									Long IdMovtoTeso = 0L;
									for (IngresoEgresoGenerado_Salida tesOutGen : tesOut.P_OUT_INGRESOEGRESOGENERADO){
										if (fPag.IdOperacion.equals(tesOutGen.NumeroReferencia)){
											IdMovtoTeso = tesOutGen.IdIngresoEgreso;
										}
									}
									ObjCargoAbonoAGenerar objCtaInCli = new ObjCargoAbonoAGenerar();
									ObjCargoAbonoAGenerar objCtaInCorr = new ObjCargoAbonoAGenerar();
									objCtaInCli.NumeroCargoAbono 				= fPag.IdOperacion + fPag.IdInternoPagoOperacion;  //NumeroCargoAbono Para Cliente
									objCtaInCli.IdPortafolio 					= ope.IdPortafolio; 
									objCtaInCli.IdMoneda 						= fPag.IdMoneda;//Liquidacion; 
									objCtaInCli.IdTipoMovimiento 				= fPag.IdTipoMovimientoCtaCli; 
									objCtaInCli.IdConcepto						= ope.Concepto.IdConcepto; //validar si hay intermesa para el concepto relacionado
									objCtaInCli.Descripcion						= ope.Concepto.NombreConcepto; 
									objCtaInCli.IdCondicionDeLiquidacion		= ope.IdCondicionLiquidacion; 
									objCtaInCli.FechaCondicionDeLiquidacion		= fPag.FechaLiquidacionFormaDePago; 
									objCtaInCli.Monto							= monto;//LiquidacionFormaDePago;//fPag.MontoFormaDePago; 
									objCtaInCli.IdEjecutivo						= operacionCerrar.p_IdUsuario; 
									objCtaInCli.IdSucursal						= operacionCerrar.p_IdSucursal; 
									objCtaInCli.IdTipoProducto					= ope.IdTipoProducto; 
									objCtaInCli.IdTipoOperacion					= ope.IdTipoOperacion; 
									objCtaInCli.IdOperacion						= fPag.IdOperacion; 
									objCtaInCli.FechaOperacion					= Fecha.toFormat(Fecha.toDate(ope.FechaOperacion),"YYYY-MM-dd"); 
									objCtaInCli.FechaLiquidacionOperacion		= ope.FechaLiquidacion; 
									objCtaInCli.FechaVencimientoOperacion		= ope.FechaVencimiento; 
									objCtaInCli.IdMonedaOperacion				= fPag.IdMoneda;//Liquidacion; 
									objCtaInCli.MontoOperacion					= monto;//LiquidacionFormaDePago;//fPag.MontoFormaDePago; 
									objCtaInCli.TipoCambioOperacion				= BigDecimal.ZERO; 
									objCtaInCli.IdContraparte					= 0L; 
									objCtaInCli.IdTipoComprobante				= "MES"; 
									objCtaInCli.IdComprobante					= fPag.IdOperacion; 
									objCtaInCli.FolioComprobante				= ope.FolioOperacion.toString(); 
									objCtaInCli.IdComprobanteDetalle			= 0L; 
									objCtaInCli.IdComprobanteFormaDePago		= 0L; 
									objCtaInCli.IdTipoMovimientoAsociado		= fPag.IdTipoMovimientoTes; 
									objCtaInCli.IdMovimientoAsociado			= IdMovtoTeso; 
									objCtaInCli.FechaComprobante				= Fecha.toFormat(Fecha.toDate(ope.FechaOperacion),"YYYY-MM-dd"); 
									objCtaInCli.IdPersonaPagoCobro				= ope.Persona.IdPersona; 
									objCtaInCli.IdInstrumento					= 0L; 
									objCtaInCli.IdTipoInstrumento				= ""; 
									objCtaInCli.Nemo							= ""; 
									objCtaInCli.EsIndicadorBilateral			= "N"; 
									objCtaInCli.EsCargoAbonoPorTraspaso			= "N"; 
									objCtaInCli.IdPortafolioTraspaso			= 0L; 
									objCtaInCli.GeneroContraparteEnTesoreria	= "S"; 
									objCtaInCli.CertificadoAutomatico			= "S";
									
									objCtaInCorr.NumeroCargoAbono = -1 * (fPag.IdOperacion + fPag.IdInternoPagoOperacion);   //NumeroCargoAbono Para Corredor
									objCtaInCorr.IdMoneda = objCtaInCli.IdMoneda;
									objCtaInCorr.IdConcepto = objCtaInCli.IdConcepto;
									objCtaInCorr.Descripcion = objCtaInCli.Descripcion; 
									objCtaInCorr.IdCondicionDeLiquidacion = objCtaInCli.IdCondicionDeLiquidacion; 
									objCtaInCorr.FechaCondicionDeLiquidacion = objCtaInCli.FechaCondicionDeLiquidacion; 
									objCtaInCorr.Monto = objCtaInCli.Monto; 
									objCtaInCorr.IdEjecutivo = objCtaInCli.IdEjecutivo; 
									objCtaInCorr.IdSucursal = objCtaInCli.IdSucursal; 
									objCtaInCorr.IdTipoProducto = objCtaInCli.IdTipoProducto; 
									objCtaInCorr.IdTipoOperacion = objCtaInCli.IdTipoOperacion; 
									objCtaInCorr.IdOperacion = objCtaInCli.IdOperacion; 
									objCtaInCorr.FechaOperacion = objCtaInCli.FechaOperacion; 
									objCtaInCorr.FechaLiquidacionOperacion = objCtaInCli.FechaLiquidacionOperacion; 
									objCtaInCorr.FechaVencimientoOperacion = objCtaInCli.FechaVencimientoOperacion; 
									objCtaInCorr.IdMonedaOperacion = objCtaInCli.IdMonedaOperacion; 
									objCtaInCorr.MontoOperacion = objCtaInCli.MontoOperacion; 
									objCtaInCorr.TipoCambioOperacion = objCtaInCli.TipoCambioOperacion; 
									objCtaInCorr.IdContraparte = objCtaInCli.IdContraparte; 
									objCtaInCorr.IdTipoComprobante = objCtaInCli.IdTipoComprobante; 
									objCtaInCorr.IdComprobante = objCtaInCli.IdComprobante; 
									objCtaInCorr.FolioComprobante = objCtaInCli.FolioComprobante; 
									objCtaInCorr.IdComprobanteDetalle = objCtaInCli.IdComprobanteDetalle; 
									objCtaInCorr.IdComprobanteFormaDePago = objCtaInCli.IdComprobanteFormaDePago; 
									objCtaInCorr.IdTipoMovimientoAsociado = objCtaInCli.IdTipoMovimientoAsociado; 
									objCtaInCorr.IdMovimientoAsociado = objCtaInCli.IdMovimientoAsociado; 
									objCtaInCorr.FechaComprobante = objCtaInCli.FechaComprobante; 
									objCtaInCorr.IdPersonaPagoCobro = objCtaInCli.IdPersonaPagoCobro; 
									objCtaInCorr.IdInstrumento = objCtaInCli.IdInstrumento; 
									objCtaInCorr.IdTipoInstrumento = objCtaInCli.IdTipoInstrumento; 
									objCtaInCorr.Nemo = objCtaInCli.Nemo; 
									objCtaInCorr.EsIndicadorBilateral = objCtaInCli.EsIndicadorBilateral; 
									objCtaInCorr.EsCargoAbonoPorTraspaso = objCtaInCli.EsCargoAbonoPorTraspaso; 
									objCtaInCorr.IdPortafolioTraspaso = objCtaInCli.IdPortafolioTraspaso; 
									objCtaInCorr.GeneroContraparteEnTesoreria = objCtaInCli.GeneroContraparteEnTesoreria; 
									objCtaInCorr.CertificadoAutomatico = objCtaInCli.CertificadoAutomatico;									
									
									objCtaInCorr.IdPortafolio					= ope.IdPortafolioCorredor;
									objCtaInCorr.IdTipoMovimiento				= fPag.IdTipoMovimientoCtaCorr; 
									entCta.p_CargoAbonoAGenerar.add(objCtaInCli);
									entCta.p_CargoAbonoAGenerar.add(objCtaInCorr);
									
									if(objCtaInCli.Monto.compareTo(objCtaInCorr.Monto) == 0 && objCtaInCli.Monto.compareTo(monto) == 0) {
										break;
									}
								}
							}
						}
						CrearCargoAbono cta = new CrearCargoAbono();
						logger.debug("MRF.Llamada Ingreso Cuenta Inversion");
						
						logger.debug("[IngresarCompraVentaTermino] Inicio llamada a CrearCargoAbono");
						startTime = System.nanoTime();
						ctaOut = cta.logica(conn, entCta);
						logger.debug("[IngresarCompraVentaTermino] (" + (System.nanoTime() - startTime)/1000000 + ") Fin llamada a CrearCargoAbono");

						
						if (MensajeSalida.getNumeroErrores(ctaOut.P_OUT_MENSAJES) > 0){
							out.P_OUT_MENSAJES.addAll(ctaOut.P_OUT_MENSAJES);
							return out;
						}
					}
				}
			}//*end 03: INGRESO CUSTODIA, TESO, CTA INV
			
			//* 04: INGRESO FORMA DE PAGO MESA, Ingreso las formas de pago con la actualizacion de los id ade cta inv y teso
			{				
				CrearComprobantePagoOperacion callCompPag = new CrearComprobantePagoOperacion();
				CrearComprobantePagoOperacion_IN compPag = new CrearComprobantePagoOperacion_IN();
				ArrayList<ComprobantePagoOperacionObjDel_IN> compPagDel = new ArrayList<>();
				compPag.p_IdEmpresa = in.p_IdEmpresa;
				compPag.p_IdUsuario = in.p_IdUsuario;
				compPag.p_IdModulo = in.p_IdModulo;
				compPag.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
				
				HashMap<String, Object> tesOutMap = Utils.List2Hash(tesOut.P_OUT_INGRESOEGRESOGENERADO, "NumeroReferencia");				
				for (IngresarCompraVentaTermino_Operacion_IN ope: in.p_Operacion){
					boolean sinFP = false;
					for (IngresarCompraVentaTermino_FormaPagoOperacion_IN inComp : in.p_FormasDePago){
						if (ope.IdInternoOperacion.equals(inComp.IdInternoOperacion)){
							ComprobantePagoOperacionObj_IN data = new ComprobantePagoOperacionObj_IN();
							data.IdEmpresa					= in.p_IdEmpresa;
							data.IdInternoPagoOperacion 	= inComp.IdInternoPagoOperacion;
							data.IdComprobantePagoOperacion = inComp.IdComprobantePagoOperacion;
							data.IdOperacion 				= inComp.IdOperacion;
							data.IdIngresoEgreso			= tesOutMap.size() > 0?((IngresoEgresoGenerado_Salida) tesOutMap.get(inComp.IdOperacion)).IdIngresoEgreso:0L;
								data.IdAbono = 0L;
								data.IdCargo = 0L;
							for (ObjCargoAbonoGenerado ctaOutCAAB: ctaOut.P_OUT_CARGOABONOGENERADO){
								Long numeroCargoAbono = inComp.IdOperacion + inComp.IdInternoPagoOperacion;
							
								if ((numeroCargoAbono.compareTo(ctaOutCAAB.NumeroCargoAbono) == 0 || numeroCargoAbono.compareTo(-1*ctaOutCAAB.NumeroCargoAbono) == 0) && ctaOutCAAB.IdTipoMovimiento.equals("MOVABN") ){
									data.IdAbono			= ctaOutCAAB.IdCargoAbonoMovimiento;
								}
															
								if ((numeroCargoAbono.compareTo(ctaOutCAAB.NumeroCargoAbono) == 0 || numeroCargoAbono.compareTo(-1*ctaOutCAAB.NumeroCargoAbono) == 0) && ctaOutCAAB.IdTipoMovimiento.equals("MOVCAR")){	
									data.IdCargo			= ctaOutCAAB.IdCargoAbonoMovimiento;
								}
							}
							data.IdIngresoEgresoAnticipo	= 0L;
							data.IdAbonoAnticipo			= 0L;
							data.IdCargoAnticipo			= 0L;
							data.IdTipoProducto				= ope.IdTipoProducto;
							data.RecibePagaPortafolio 		= inComp.RecibePagaPortafolio;
							data.IdFormaDePago 				= inComp.IdFormaDePago;
							data.IdModalidadPago			= inComp.IdModalidadDePago;
							data.IdMoneda 					= inComp.IdMoneda;
							//data.IdMonedaLiquidacion 		= inComp.IdMonedaLiquidacion;
							data.Monto 						= inComp.MontoFormaDePago;
							//data.MontoLiquidacion			= inComp.MontoLiquidacionFormaDePago;
							data.FechaIngresoComprobantePagoOperacion		= ope.FechaOperacion;//" 		DATE
							data.FechaLiquidacionComprobantePagoOperacion	= inComp.FechaLiquidacionFormaDePago;//"	DATE
							data.IdEstado									= inComp.IdEstado;//" 									NVARCHAR(3)
							data.AfectarTesoreria 							= inComp.AfectarTesoreria;
							data.IdCuentaBanco								= inComp.IdCuentaBancaria;
							
							compPag.p_ComprobantePagoOperacion.add(data);
							sinFP = false;
						}
						else {
							sinFP = true;
						}
					}
					if (sinFP || in.p_FormasDePago.size() == 0){
						ComprobantePagoOperacionObjDel_IN data = new ComprobantePagoOperacionObjDel_IN();
						data.IdEmpresa					= in.p_IdEmpresa;
						data.IdOperacion 				= ope.IdOperacion;
						compPagDel.add(data);
					}
				}

				logger.debug("MRF.Llamada Ingreso Comprobante Pago Operacion");
				if (compPagDel.size() > 0 ){

					logger.debug("[IngresarCompraVentaTermino] Inicio llamada a callCompPag.logicaDeleteAllOperacion");
					startTime = System.nanoTime();
					resultCompPagDel = callCompPag.logicaDeleteAllOperacion(conn, compPagDel);
					logger.debug("[IngresarCompraVentaTermino] (" + (System.nanoTime() - startTime)/1000000 + ") Fin llamada a callCompPag.logicaDeleteAllOperacion");

					
					if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0){
						out.P_OUT_MENSAJES.addAll(resultCompPagDel.P_OUT_MENSAJES);
						return out;
					}
				}
				

				logger.debug("[IngresarCompraVentaTermino] Inicio llamada a callCompPag.logica");
				startTime = System.nanoTime();
				resultCompPag = callCompPag.logica(conn, compPag);
				logger.debug("[IngresarCompraVentaTermino] (" + (System.nanoTime() - startTime)/1000000 + ") Fin llamada a callCompPag.logica");

				
				if (MensajeSalida.getNumeroErrores(resultCompPag.P_OUT_MENSAJES) > 0){
					out.P_OUT_MENSAJES.addAll(resultCompPag.P_OUT_MENSAJES);
					return out;
				}
				v_OutComprobantePagoHash = Utils.List2Hash(resultCompPag.P_OUT_COMPRABANTEPAGO,"IdInternoPagoOperacion");
				for (IngresarCompraVentaTermino_FormaPagoOperacion_IN inComp : in.p_FormasDePago){
					if (v_OutComprobantePagoHash.get(inComp.IdInternoPagoOperacion) != null){
						inComp.IdComprobantePagoOperacion = ((ComprobantePagoOperacionObj_OUT)v_OutComprobantePagoHash.get(inComp.IdInternoPagoOperacion)).IdComprobantePagoOperacion;
					}
				}
			}

			//* 05 SALIDAS: prepara tabla salida
			for (OperacionObjIn ope: v_Operacion){

				CrearOperacionMesa_Operacion_OUT opSal= new CrearOperacionMesa_Operacion_OUT();

				opSal.IdEmisor = ope.IdEmisor;
				opSal.Accion = ope.Accion;
				opSal.IdInternoOperacion = ope.IdInternoOperacion;
				opSal.IdOperacion = ope.IdOperacion;
				opSal.FolioOperacion = ope.FolioOperacion;
				opSal.IdCliente = ope.IdCliente;
				opSal.IdPortafolio = ope.IdPortafolio;
				opSal.IdClienteCorredor = ope.IdClienteCorredor;
				opSal.IdPortafolioCorredor = ope.IdPortafolioCorredor;
				opSal.IdOperacionRelacionada = ope.IdOperacionRelacionada;
				opSal.Intermesa = ope.Intermesa;
				opSal.IdMesaOrigen = ope.IdMesaOrigen;
				opSal.IdMesaDestino = ope.IdMesaDestino;
				opSal.AfectaCustodia = ope.AfectaCustodia;
				opSal.IdTipoProducto = ope.IdTipoProducto; //'MERF'
				opSal.IdTipoOperacion = ope.IdTipoOperacion; //COMTER,VENTER,COMPAC,VENPAC
				opSal.IdTipoReajuste = ope.IdTipoReajuste; //"USD","UF"
				opSal.Plazo = ope.PlazoEnDias;
				opSal.Monto = ope.MontoOperacion;
				opSal.MontoMoneda = ope.MontoOperacionMoneda;
				opSal.IdMoneda = ope.IdMoneda;//CLP "USD","UF" moneda local
				opSal.IdMonedaLiquidacion = ope.IdMonedaLiquidacion; //CLP "USD","UF"
				opSal.Tasa = ope.Tasa;
				opSal.ValorFinal = ope.MontoOperacionFinal;
				opSal.ValorFinalMoneda = ope.MontoOperacionFinalMoneda;
				opSal.TasaAgente = ope.TasaAgente;
				opSal.FechaOperacion =  Fecha.toDate(ope.FechaOperacion);
				opSal.FechaVencimiento = ope.FechaVencimiento;
				opSal.IdCondicionLiquidacion = ope.IdCondicionLiquidacion;
				opSal.FechaLiquidacion = ope.FechaCondicionLiquidacion;
				opSal.IdAgente = ope.IdAgente;
				opSal.IdOperador = ope.UsuarioEdicion;
				opSal.IdEstado = ope.IdEstado;
				opSal.NombreEstado = ((OperacionObjOut)v_OutOperacionHash.get(ope.IdInternoOperacion)).NombreEstado;
				opSal.Glosa = ope.Glosa;
				opSal.IdTipoCanalIngreso = ope.IdTipoCanalIngreso;
				out.P_OUT_OPERACION.add(opSal);
			}
			for (IngresarCompraVentaTermino_DetalleOperacion_IN det: in.p_DetalleOperacion){
				CrearOperacionMesa_DetalleOperacion_OUT detOut = new CrearOperacionMesa_DetalleOperacion_OUT();
				//ReservaObj_OUT resOut = (ReservaObj_OUT) resultReservaHash.get("VIG",det.IdInternoDetalle);
				//if (resOut != null){
					
					detOut.IdInternoDetalle = det.IdInternoDetalle;
					detOut.IdInternoOperacion = det.IdInternoOperacion;
					detOut.IdReserva = det.IdReserva;
					detOut.IdCliente = det.IdCliente;
					detOut.IdPortafolio = det.IdPortafolio;
					detOut.IdPortafolio = det.IdPortafolio;
					detOut.IdPortafolioCorredor = det.IdPortafolioCorredor;
					detOut.IdMesa = det.IdMesa;
					detOut.IdTipoProducto = det.IdTipoProducto;
					detOut.IdTipoOperacion = det.IdTipoOperacion;
					detOut.IdInstrumento = det.IdInstrumento;
					detOut.EsFungible = det.EsFungible;
					detOut.IdBolsa = det.IdBolsa;
					detOut.IdBoveda = det.IdBoveda;
					detOut.FolioCartera = det.FolioCartera;
					detOut.CodigoIFRS = det.CodigoIFRS;
					detOut.IdEmisor = det.IdEmisor;
					detOut.IdTipoReajuste = det.IdTipoReajuste;
					detOut.IdMonedaEmisionInstrumento = det.IdMonedaEmisionInstrumento;
					detOut.IdMonedaLiquidacionInstrumento = det.IdMonedaLiquidacionInstrumento;
					detOut.IdMonedaLiquidacionOperacion = det.IdMonedaLiquidacionOperacion;
					detOut.Cantidad = det.Cantidad;
					detOut.Precio = det.Precio;
					detOut.Tasa = det.Tasa;
					detOut.Monto = det.Monto;
					detOut.MontoLiquidacion = det.MontoLiquidacion;
					detOut.MontoLiquidacionOperacion = det.MontoLiquidacionOperacion;
					detOut.ValorRescate = det.ValorRescate;
					detOut.FechaVencimiento = Fecha.toDate(det.FechaVencimiento);

					out.P_OUT_DETALLEOPERACION.add(detOut);	
				//}
			}
			for (IngresarCompraVentaTermino_FormaPagoOperacion_IN ope: in.p_FormasDePago){
				CrearOperacionMesa_FormaPagoOperacion_OUT opSal= new CrearOperacionMesa_FormaPagoOperacion_OUT();
				
				opSal.IdInternoPagoOperacion = ope.IdInternoPagoOperacion;
				opSal.IdComprobantePagoOperacion = ope.IdComprobantePagoOperacion;
				opSal.IdFormaDePago = ope.IdFormaDePago;
				opSal.IdMoneda = ope.IdMoneda;
				opSal.IdOperacion = ope.IdOperacion;
				opSal.RecibePagaPortafolio = ope.RecibePagaPortafolio;
				opSal.MontoFormaDePago = ope.MontoFormaDePago;
				
				out.P_OUT_FORMASDEPAGO.add(opSal);
			}
		
		logger.info("Termino del servicio IngresarCompraVentaTermino, tiempo: " + (System.currentTimeMillis() - init) + " ms - " + (System.currentTimeMillis() - init)/1000 + " seg");
		
		return out;
	}
	
	
	// ------------------------------------------------------------
	// METODOS UTILITARIOS 
	// ------------------------------------------------------------

	private OperacionObjIn copyPojoIngresoOperacion(IngresarCompraVentaTermino_Operacion_IN entrada) {
		
		OperacionObjIn salida = new OperacionObjIn();
		
		salida.Accion = entrada.Accion;
		salida.IdInternoOperacion = entrada.IdInternoOperacion;
		salida.IdOperacion = entrada.IdOperacion;
		salida.FolioOperacion = entrada.FolioOperacion;
		salida.IdCliente = entrada.IdCliente;
		salida.IdPortafolio = entrada.IdPortafolio;
		salida.IdClienteCorredor = entrada.IdClienteCorredor;
		salida.IdPortafolioCorredor = entrada.IdPortafolioCorredor;
		salida.Intermesa = entrada.Intermesa;
		salida.IdMesaOrigen = entrada.IdMesaOrigen;
		salida.IdMesaDestino = entrada.IdMesaDestino;
		salida.AfectaCustodia = entrada.AfectaCustodia;
		salida.IdTipoProducto = entrada.IdTipoProducto;
		salida.IdTipoOperacion = entrada.IdTipoOperacion;
		salida.IdTipoReajuste = entrada.IdTipoReajuste;
		salida.IdMoneda = entrada.IdMoneda;
		salida.IdMonedaLiquidacion = entrada.IdMonedaLiquidacion;
		salida.Tasa = entrada.Tasa;
		salida.TasaAgente = entrada.TasaAgente;
		salida.FechaOperacion = entrada.FechaOperacion;
		salida.IdCondicionLiquidacion = entrada.IdCondicionLiquidacion;
		salida.IdAgente = entrada.IdAgente;
		salida.IdEstado = entrada.IdEstado;
		salida.Glosa = entrada.Glosa;
		salida.IdTipoCanalIngreso = entrada.IdTipoCanalIngreso;
		
		return salida;
	}
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = Utils.getGsonBuilder();
		IngresarCompraVentaTermino_IN jin = gson.fromJson(in.toString(), IngresarCompraVentaTermino_IN.class);
		IngresarCompraVentaTermino_OUT out = logica(conn, jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};

}
