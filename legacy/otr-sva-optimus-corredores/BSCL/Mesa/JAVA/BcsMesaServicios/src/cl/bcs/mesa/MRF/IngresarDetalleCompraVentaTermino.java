package cl.bcs.mesa.MRF;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.IngresoReserva;
import cl.bcs.custodia.IngresoReserva.IngresoReserva_IN;
import cl.bcs.custodia.IngresoReserva.IngresoReserva_OUT;
import cl.bcs.custodia.IngresoReserva.ReservaObj_IN;
import cl.bcs.custodia.IngresoReserva.ReservaObj_OUT;
import cl.bcs.mesa.MES.ConsultarInstrumento;
import cl.bcs.mesa.MES.ConsultarInstrumento.InformacionInstrumento;
import cl.bcs.mesa.MES.ConsultarInstrumentoTipoReajuste;
import cl.bcs.mesa.MES.ConsultarInstrumentoTipoReajuste.InformacionInstrumentoTipoReajuste;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion.ComprobantePagoOperacionObjDel_IN;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion.ComprobantePagoOperacionObj_IN;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion.ComprobantePagoOperacionObj_OUT;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion.CrearComprobantePagoOperacion_IN;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion.CrearComprobantePagoOperacion_OUT;
import cl.bcs.mesa.MES.IngresoOperacion.OperacionObjIn;
import cl.bcs.mesa.MRF.CrearOperacionMesa.Concepto;
import cl.bcs.mesa.MRF.CrearOperacionMesa.CrearOperacionMesa_DetalleOperacion_OUT;
import cl.bcs.mesa.MRF.CrearOperacionMesa.CrearOperacionMesa_FormaPagoOperacion_OUT;
import cl.bcs.mesa.MRF.CrearOperacionMesa.CrearOperacionMesa_Operacion_OUT;
import cl.bcs.mesa.MRF.CrearOperacionMesa.FormaDePagoMesa;
import cl.bcs.mesa.MRF.CrearOperacionMesa.Persona;
import cl.bcs.mesa.MRF.CrearOperacionMesa.PreferenciaPagoPortafolio;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.utils.Fecha;

public class IngresarDetalleCompraVentaTermino implements JavaCall{
	private static final Logger logger = LogManager.getLogger(IngresarDetalleCompraVentaTermino.class);
	SQL _sql = new SQL();
	
	public static class IngresarDetalleCompraVentaTermino_Operacion_IN {
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
		public String		Glosa;
		public String		IdTipoCanalIngreso;
		public Concepto 	Concepto;
		public Persona		Persona;
		public String 		TipoDocumento;
		public Long 		IdContacto;
		public String		DocumentoIdentidadCorredor = "";
		
	 	public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class IngresarDetalleCompraVentaTermino_DetalleOperacion_IN {
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
	
	public static class IngresarDetalleCompraVentaTermino_FormaPagoOperacion_IN {
		public Long 		IdInternoPagoOperacion;
		public Long			IdInternoOperacion;
		public Long 		IdComprobantePagoOperacion;
		public String 		IdFormaDePago;
		public String 		IdMoneda;
		public Long 		IdOperacion;
		public String 		RecibePagaPortafolio;
		public BigDecimal 	MontoFormaDePago;
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
	
	public static class IngresarDetalleCompraVentaTermino_IN {
		public Long												p_IdEmpresa;
	    public Long												p_IdUsuario;
	    public Long												p_IdPais;
	    public String											p_MonedaSistema;
	    public String											p_FechaSistema;
	    public Long												p_IdSucursal;
	    public String											p_IdModulo; 
	    public String											p_UsaReglasNegocio;
	    public String											p_EsDetalle; 
		public List<IngresarDetalleCompraVentaTermino_Operacion_IN>			p_Operacion;
		public List<IngresarDetalleCompraVentaTermino_DetalleOperacion_IN> 	p_DetalleOperacion;
		public List<IngresarDetalleCompraVentaTermino_FormaPagoOperacion_IN> 	p_FormasDePago;
		
		public IngresarDetalleCompraVentaTermino_IN(){
			this.p_EsDetalle ="N";
			this.p_Operacion = new ArrayList<IngresarDetalleCompraVentaTermino_Operacion_IN>();
			this.p_DetalleOperacion = new ArrayList<IngresarDetalleCompraVentaTermino_DetalleOperacion_IN>();
			this.p_FormasDePago = new ArrayList<IngresarDetalleCompraVentaTermino_FormaPagoOperacion_IN>();
		}
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class IngresarDetalleCompraVentaTermino_OUT {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public List<CrearOperacionMesa_Operacion_OUT>			P_OUT_OPERACION;
		public List<CrearOperacionMesa_DetalleOperacion_OUT> 	P_OUT_DETALLEOPERACION;
		public List<CrearOperacionMesa_FormaPagoOperacion_OUT>	P_OUT_FORMASDEPAGO;
		
		public IngresarDetalleCompraVentaTermino_OUT() {
			this.P_OUT_OPERACION 		= new ArrayList<CrearOperacionMesa_Operacion_OUT>();
			this.P_OUT_DETALLEOPERACION = new ArrayList<CrearOperacionMesa_DetalleOperacion_OUT>();
			this.P_OUT_FORMASDEPAGO		= new ArrayList<CrearOperacionMesa_FormaPagoOperacion_OUT>();
			this.P_OUT_MENSAJES 		= new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class IngresarDetalleCompraVentaTerminoUpdate {
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
	
	// ------------------------------------------------------------
	// LOGICA
	// ------------------------------------------------------------
	
	public IngresarDetalleCompraVentaTermino_OUT logica (Connection conn, IngresarDetalleCompraVentaTermino_IN in) throws Exception {
		Long init = System.currentTimeMillis();
		long startTime = System.nanoTime();
		IngresarDetalleCompraVentaTermino_OUT out = new IngresarDetalleCompraVentaTermino_OUT();
		logger.info("Inicio servicio MRF.IngresarDetalleCompraVentaTermino");
		
		List<OperacionObjIn> v_Operacion = new ArrayList<>();
		List<ReservaObj_IN> v_OperacionDetalle = new ArrayList<>();
		Map<Object,Object> v_OperacionHash = new HashMap<>();
		Map<Object,Object> v_OutComprobantePagoHash = new HashMap<>();
		IngresoReserva_OUT resultReserva = new IngresoReserva_OUT();
		
		CrearComprobantePagoOperacion_OUT resultCompPag = new CrearComprobantePagoOperacion_OUT();
		CrearComprobantePagoOperacion_OUT resultCompPagDel = new CrearComprobantePagoOperacion_OUT();
		HashMap<String, Object> infoInsHash = new HashMap<>();
		MultiKeyMap infoInsTipHash = new MultiKeyMap();
		
		for (IngresarDetalleCompraVentaTermino_Operacion_IN operacion : in.p_Operacion){
			OperacionObjIn data = copyPojoIngresoOperacion(operacion);
			
			data.Accion = operacion.Accion;
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
			data.IdInstrumento = 0L;									
			data.IdEmisor = "";
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
			v_Operacion.add(data);										
		}
		//INGRESO RESERVA, detalle Operacion
		{
			//Detalle Operacion
			if (in.p_DetalleOperacion.size() > 0){
				List<Long> paramInst = new ArrayList<>();
				for (IngresarDetalleCompraVentaTermino_DetalleOperacion_IN detalleOperacion : in.p_DetalleOperacion){
					paramInst.add(detalleOperacion.IdInstrumento);
				}
				//consulto info instrumento
				ConsultarInstrumento ins = new ConsultarInstrumento();
				logger.debug("[IngresarDetalleCompraVentaTermino] Inicio llamada a ConsultarInstrumento");
				startTime = System.nanoTime();
				List<InformacionInstrumento> infoIns = ins.logica(conn, paramInst);
				logger.debug("[IngresarDetalleCompraVentaTermino] (" + (System.nanoTime() - startTime)/1000000 + ") Fin llamada a ConsultarInstrumento");		
				infoInsHash = Utils.List2Hash(infoIns, "IdInstrumento");
				//consultar Instrumento tipo reajuste
				ConsultarInstrumentoTipoReajuste insTip = new ConsultarInstrumentoTipoReajuste();
				logger.debug("[IngresarDetalleCompraVentaTermino] Inicio llamada a ConsultarInstrumentoTipoReajuste");
				startTime = System.nanoTime();
				List<InformacionInstrumentoTipoReajuste> infoInsTip = insTip.logica(conn, paramInst);
				logger.debug("[IngresarDetalleCompraVentaTermino] (" + (System.nanoTime() - startTime)/1000000 + ") Fin llamada a ConsultarInstrumentoTipoReajuste");
				for (InformacionInstrumentoTipoReajuste inf: infoInsTip){
					infoInsTipHash.put(inf.IdInstrumento, inf.IdTipoReajuste,inf);
				}
			}
			v_OperacionHash = Utils.List2Hash(v_Operacion,"IdInternoOperacion");
			
			for (IngresarDetalleCompraVentaTermino_DetalleOperacion_IN detalleOperacion : in.p_DetalleOperacion){
				ReservaObj_IN data = new ReservaObj_IN();
				data.IdOperacion 			= ((OperacionObjIn) v_OperacionHash.get(detalleOperacion.IdInternoOperacion)).IdOperacion;
				data.IdRegistro				= detalleOperacion.IdInternoDetalle;
				data.IdReserva				= detalleOperacion.IdReserva;
				data.IdConcepto				= detalleOperacion.Concepto;
				data.FolioOperacion			= ((OperacionObjIn) v_OperacionHash.get(detalleOperacion.IdInternoOperacion)).FolioOperacion;
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
				} else {
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

			for (IngresarDetalleCompraVentaTermino_DetalleOperacion_IN detalleOperacion : in.p_DetalleOperacion){
				for (ReservaObj_OUT re: resultReserva.P_OUT_RESERVA){
					if (re.IdRegistro.equals(detalleOperacion.IdInternoDetalle) && re.Estado.equals("VIG")){
						detalleOperacion.IdReserva = re.IdReserva;
					}
				}
			}
		}			
		
		//INGRESO FORMA DE PAGO MESA, Ingreso las formas de pago con la actualizacion de los id ade cta inv y teso
		if("N".equals(in.p_EsDetalle)){				
			CrearComprobantePagoOperacion callCompPag = new CrearComprobantePagoOperacion();
			CrearComprobantePagoOperacion_IN compPag = new CrearComprobantePagoOperacion_IN();
			ArrayList<ComprobantePagoOperacionObjDel_IN> compPagDel = new ArrayList<>();
			compPag.p_IdEmpresa = in.p_IdEmpresa;
			compPag.p_IdUsuario = in.p_IdUsuario;
			compPag.p_IdModulo = in.p_IdModulo;
			compPag.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
			
			for (IngresarDetalleCompraVentaTermino_Operacion_IN ope: in.p_Operacion){
				boolean sinFP = false;
				for (IngresarDetalleCompraVentaTermino_FormaPagoOperacion_IN inComp : in.p_FormasDePago){
					if (ope.IdInternoOperacion.equals(inComp.IdInternoOperacion)){
						ComprobantePagoOperacionObj_IN data = new ComprobantePagoOperacionObj_IN();
						data.IdEmpresa					= in.p_IdEmpresa;
						data.IdInternoPagoOperacion 	= inComp.IdInternoPagoOperacion;
						data.IdComprobantePagoOperacion = inComp.IdComprobantePagoOperacion;
						data.IdOperacion 				= inComp.IdOperacion;
						data.IdIngresoEgreso			= 0L;
						data.IdAbono 					= 0L;
						data.IdCargo 					= 0L;
						data.IdIngresoEgresoAnticipo	= 0L;
						data.IdAbonoAnticipo			= 0L;
						data.IdCargoAnticipo			= 0L;
						data.IdTipoProducto				= ope.IdTipoProducto;
						data.RecibePagaPortafolio 		= inComp.RecibePagaPortafolio;
						data.IdFormaDePago 				= inComp.IdFormaDePago;
						data.IdModalidadPago			= inComp.IdModalidadDePago;
						data.IdMoneda 					= inComp.IdMoneda;
						data.Monto 						= inComp.MontoFormaDePago;
						data.FechaIngresoComprobantePagoOperacion		= ope.FechaOperacion;//" 		DATE
						data.FechaLiquidacionComprobantePagoOperacion	= inComp.FechaLiquidacionFormaDePago;//"	DATE
						data.IdEstado									= inComp.IdEstado;//" 									NVARCHAR(3)
						data.AfectarTesoreria 							= inComp.AfectarTesoreria;
						data.IdCuentaBanco								= inComp.IdCuentaBancaria;
						compPag.p_ComprobantePagoOperacion.add(data);
						sinFP = false;
					} else {
						sinFP = true;
					}
				}
				if (sinFP || in.p_FormasDePago.size() == 0){
					ComprobantePagoOperacionObjDel_IN data = new ComprobantePagoOperacionObjDel_IN();
					data.IdEmpresa		= in.p_IdEmpresa;
					data.IdOperacion	= ope.IdOperacion;
					compPagDel.add(data);
				}
			}

			logger.debug("MRF.Llamada Ingreso Comprobante Pago Operacion");
			if (compPagDel.size() > 0 ){
				logger.debug("[IngresarDetalleCompraVentaTermino] Inicio llamada a callCompPag.logicaDeleteAllOperacion");
				startTime = System.nanoTime();
				resultCompPagDel = callCompPag.logicaDeleteAllOperacion(conn, compPagDel);
				logger.debug("[IngresarDetalleCompraVentaTermino] (" + (System.nanoTime() - startTime)/1000000 + ") Fin llamada a callCompPag.logicaDeleteAllOperacion");
				
				if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0){
					out.P_OUT_MENSAJES.addAll(resultCompPagDel.P_OUT_MENSAJES);
					return out;
				}
			}
			
			logger.debug("[IngresarDetalleCompraVentaTermino] Inicio llamada a callCompPag.logica");
			startTime = System.nanoTime();
			resultCompPag = callCompPag.logica(conn, compPag);
			logger.debug("[IngresarDetalleCompraVentaTermino] (" + (System.nanoTime() - startTime)/1000000 + ") Fin llamada a callCompPag.logica");
			
			if (MensajeSalida.getNumeroErrores(resultCompPag.P_OUT_MENSAJES) > 0){
				out.P_OUT_MENSAJES.addAll(resultCompPag.P_OUT_MENSAJES);
				return out;
			}
			v_OutComprobantePagoHash = Utils.List2Hash(resultCompPag.P_OUT_COMPRABANTEPAGO,"IdInternoPagoOperacion");
			for (IngresarDetalleCompraVentaTermino_FormaPagoOperacion_IN inComp : in.p_FormasDePago){
				if (v_OutComprobantePagoHash.get(inComp.IdInternoPagoOperacion) != null){
					inComp.IdComprobantePagoOperacion = ((ComprobantePagoOperacionObj_OUT)v_OutComprobantePagoHash.get(inComp.IdInternoPagoOperacion)).IdComprobantePagoOperacion;
				}
			}
		}
		//SALIDAS: prepara tabla salida
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
			opSal.NombreEstado = "";
			opSal.Glosa = ope.Glosa;
			opSal.IdTipoCanalIngreso = ope.IdTipoCanalIngreso;
			out.P_OUT_OPERACION.add(opSal);
		}
		
		for (IngresarDetalleCompraVentaTermino_DetalleOperacion_IN det: in.p_DetalleOperacion){
			CrearOperacionMesa_DetalleOperacion_OUT detOut = new CrearOperacionMesa_DetalleOperacion_OUT();
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
		}
		if("N".equals(in.p_EsDetalle)){		
			for (IngresarDetalleCompraVentaTermino_FormaPagoOperacion_IN ope: in.p_FormasDePago){
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
		}
		
		
		logger.info("Termino del servicio IngresarDetalleCompraVentaTermino, tiempo: " + (System.currentTimeMillis() - init) + " ms - " + (System.currentTimeMillis() - init)/1000 + " seg");
		return out;
	}
	
	// ------------------------------------------------------------
	// METODOS UTILITARIOS 
	// ------------------------------------------------------------
private OperacionObjIn copyPojoIngresoOperacion(IngresarDetalleCompraVentaTermino_Operacion_IN entrada) {
		
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
		IngresarDetalleCompraVentaTermino_IN jin = gson.fromJson(in.toString(), IngresarDetalleCompraVentaTermino_IN.class);
		IngresarDetalleCompraVentaTermino_OUT out = logica(conn, jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};

}
