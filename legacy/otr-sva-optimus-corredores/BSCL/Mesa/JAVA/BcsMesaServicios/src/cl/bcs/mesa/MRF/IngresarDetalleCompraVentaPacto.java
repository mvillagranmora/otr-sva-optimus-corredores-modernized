package cl.bcs.mesa.MRF;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
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
import cl.bcs.mesa.MES.ConsultarTipoInstrumento;
import cl.bcs.mesa.MES.ConsultarTipoInstrumento.ConsultarTipoInstrumentoIN;
import cl.bcs.mesa.MES.ConsultarTipoInstrumento.InformacionTipoInstrumento;
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

public class IngresarDetalleCompraVentaPacto implements JavaCall{

	private static final Logger logger = LogManager.getLogger(IngresarDetalleCompraVentaPacto.class);
	SQL _sql = new SQL();
		
	public static class IngresarDetalleCompraVentaPacto_Operacion_IN {
		public BigDecimal 	TasaAnualizada;
		public BigDecimal 	CostoFondo;
		public BigDecimal 	UtilidadPerdida;
		public BigDecimal 	UtilidadFinal;
		public String 		Accion;
		public Long 		IdInternoOperacion;
		public Long 		IdOperacion;
		public Long			FolioOperacion;
		public Long			FolioCartera;
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
	
	public static class IngresarDetalleCompraVentaPacto_DetalleOperacion_IN {
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
		public BigDecimal	ValorFinal;
		public String       TipoCarteraCorredor;
		public BigDecimal MontoFinalCarteraMonedaSistema;
		public BigDecimal MontoFinalCarteraMonedaInstrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	public static class IngresarDetalleCompraVentaPacto_FormaPagoOperacion_IN {
		public Long 		IdInternoPagoOperacion;
		public Long			IdInternoOperacion;
		public Long 		IdComprobantePagoOperacion;
		public String 		IdFormaDePago;
		public String 		IdMoneda;
		public Long 		IdOperacion;
		public String 		RecibePagaPortafolio;
		public BigDecimal 	MontoFormaDePago;
		public String 		AfectarTesoreria;
		public String		IdTipoMovimientoTes;
		public String		IdTipoMovimientoCtaCli;
		public String		IdTipoMovimientoCtaCorr;
		public String		IdModalidadDePago;
		public FormaDePagoMesa FormaDePago;
		public PreferenciaPagoPortafolio PreferenciaDePago;
		public String		IdEstado;
		public Long 		IdCuentaBancaria;
		public String 		FechaLiquidacionFormaDePago;
		public String NombreBanco;
		public String NombreBancoTarjeta;
		public String NumeroTarjeta;
		public String CuentaBancaria;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	public static class IngresarDetalleCompraVentaPacto_IN {
		public Long												p_IdEmpresa;
	    public Long												p_IdUsuario;
	    public Long												p_IdPais;
	    public String											p_MonedaSistema;
	    public String											p_FechaSistema;
	    public Long												p_IdSucursal;
	    public String											p_IdModulo; 
	    public String											p_UsaReglasNegocio;
	    public String											p_EsDetalle;
		public List<IngresarDetalleCompraVentaPacto_Operacion_IN>			p_Operacion;
		public List<IngresarDetalleCompraVentaPacto_DetalleOperacion_IN> 	p_DetalleOperacion;
		public List<IngresarDetalleCompraVentaPacto_FormaPagoOperacion_IN> 	p_FormasDePago;
		
		public IngresarDetalleCompraVentaPacto_IN(){
			this.p_EsDetalle = "N";
			this.p_Operacion = new ArrayList<IngresarDetalleCompraVentaPacto_Operacion_IN>();
			this.p_DetalleOperacion = new ArrayList<IngresarDetalleCompraVentaPacto_DetalleOperacion_IN>();
			this.p_FormasDePago = new ArrayList<IngresarDetalleCompraVentaPacto_FormaPagoOperacion_IN>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class IngresarDetalleCompraVentaPacto_OUT {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public List<CrearOperacionMesa_Operacion_OUT>			P_OUT_OPERACION;
		public List<CrearOperacionMesa_DetalleOperacion_OUT> 	P_OUT_DETALLEOPERACION;
		public List<CrearOperacionMesa_FormaPagoOperacion_OUT>	P_OUT_FORMASDEPAGO;
		
		public IngresarDetalleCompraVentaPacto_OUT() {
			this.P_OUT_OPERACION 		= new ArrayList<CrearOperacionMesa_Operacion_OUT>();
			this.P_OUT_DETALLEOPERACION = new ArrayList<CrearOperacionMesa_DetalleOperacion_OUT>();
			this.P_OUT_FORMASDEPAGO		= new ArrayList<CrearOperacionMesa_FormaPagoOperacion_OUT>();
			this.P_OUT_MENSAJES 		= new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class IngresarCompraVentaPactoUpdate {
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
	
	public IngresarDetalleCompraVentaPacto_OUT logica (Connection conn, IngresarDetalleCompraVentaPacto_IN in) throws Exception {
		
		Long init = System.currentTimeMillis();
		long startTime = System.nanoTime();
		IngresarDetalleCompraVentaPacto_OUT out = new IngresarDetalleCompraVentaPacto_OUT();
		HashMap<String, Long> cantFormasPago = new HashMap<String, Long>();
		logger.info("Inicio servicio MRF.IngresarDetalleCompraVentaPacto");
		
		List<OperacionObjIn> v_Operacion = new ArrayList<>();
		List<ReservaObj_IN> v_OperacionDetalle = new ArrayList<>();
		Map<Object,Object> v_OperacionHash = new HashMap<>();
		Map<Object,Object> v_OutComprobantePagoHash = new HashMap<>();
		IngresoReserva_OUT resultReserva = new IngresoReserva_OUT();
		
		CrearComprobantePagoOperacion_OUT resultCompPag = new CrearComprobantePagoOperacion_OUT();
		CrearComprobantePagoOperacion_OUT resultCompPagDel = new CrearComprobantePagoOperacion_OUT();
		HashMap<String, Object> infoInsHash = new HashMap<>();
		MultiKeyMap infoInsTipHash = new MultiKeyMap();
		
		List<Long> instIn = new ArrayList<>();
		ConsultarTipoInstrumento tipInst = new ConsultarTipoInstrumento();
		ConsultarTipoInstrumentoIN tipInstIN = new ConsultarTipoInstrumentoIN();
		for (IngresarDetalleCompraVentaPacto_Operacion_IN ent : in.p_Operacion){
			tipInstIN.IdTipoOperacion.add(ent.IdTipoOperacion);
		}		
		List<InformacionTipoInstrumento> tipInstOut = tipInst.logica(conn, tipInstIN);
		MultiKeyMap tipInstHash = new MultiKeyMap();
		
		for(InformacionTipoInstrumento tipIns: tipInstOut){
			instIn.add(tipIns.IdInstrumento);
			tipInstHash.put(tipIns.IdTipoOperacion, tipIns.IdTipoInstrumento,tipIns);
		}
		
		ConsultarInstrumento inst = new ConsultarInstrumento();
		List<InformacionInstrumento> instOut = inst.logica(conn, instIn);
		HashMap<String, Object> instOutHash = Utils.List2Hash(instOut, "IdInstrumento");
		
		for (IngresarDetalleCompraVentaPacto_Operacion_IN operacion : in.p_Operacion){					
			OperacionObjIn data = new OperacionObjIn();
			data.IdOperacion = operacion.IdOperacion;
			data.IdCliente = operacion.IdCliente;
			data.IdPortafolio = operacion.IdPortafolio;
			data.IdClienteCorredor = operacion.IdClienteCorredor;
			data.IdPortafolioCorredor = operacion.IdPortafolioCorredor;
			data.Intermesa = operacion.Intermesa;
			data.IdMesaOrigen = operacion.IdMesaOrigen;
			data.IdMesaDestino = operacion.IdMesaDestino;
			data.AfectaCustodia = operacion.AfectaCustodia;
			data.IdTipoProducto = operacion.IdTipoProducto;
			data.IdTipoOperacion = operacion.IdTipoOperacion;
			data.IdTipoReajuste = operacion.IdTipoReajuste;
			data.IdMoneda = operacion.IdMoneda;
			data.IdMonedaLiquidacion = operacion.IdMonedaLiquidacion;
			data.Tasa = operacion.Tasa;
			data.TasaAgente = operacion.TasaAgente;					 
			data.IdCondicionLiquidacion = operacion.IdCondicionLiquidacion;
			data.IdAgente = operacion.IdAgente;
			data.IdEstado = operacion.IdEstado;
			data.Glosa = operacion.Glosa;
			data.IdTipoCanalIngreso = operacion.IdTipoCanalIngreso;
			data.Accion = operacion.Accion;
			data.IdInternoOperacion = operacion.IdInternoOperacion;
			data.MontoOperacion = operacion.Monto;
			data.MontoOperacionInicial = operacion.Monto;					
			data.MontoOperacionFinal = operacion.ValorFinal;			
			data.MontoOperacionMoneda = operacion.MontoMoneda;			
			data.MontoOperacionInicialMoneda = operacion.MontoMoneda;
			data.MontoOperacionFinalMoneda = operacion.ValorFinalMoneda;	
			data.PlazoEnDias = operacion.Plazo;
			if( operacion.FechaLiquidacion == null || operacion.FechaLiquidacion.equalsIgnoreCase("") ) {
				data.FechaCondicionLiquidacion = Fecha.toDate("0001-01-01");
				data.FechaLiquidacion =Fecha.toDate("0001-01-01");
			}else {
				data.FechaCondicionLiquidacion = Fecha.toDate(operacion.FechaLiquidacion);
				data.FechaLiquidacion =Fecha.toDate(operacion.FechaLiquidacion);
			}
			data.IdUsuarioCreacion = operacion.IdOperador;
			data.IdEmpresa = in.p_IdEmpresa;							
			data.UsuarioEdicion = in.p_IdUsuario;
			data.FolioOperacion = 0L;									
			data.IdOperacionRelacionada = 0L;						
			data.IdInstrumento 		= ((InformacionInstrumento) instOutHash.get(((InformacionTipoInstrumento) tipInstHash.get(operacion.IdTipoOperacion,"PAC")).IdInstrumento)).IdInstrumento;									
			data.IdEmisor		 	= ((InformacionInstrumento) instOutHash.get(((InformacionTipoInstrumento) tipInstHash.get(operacion.IdTipoOperacion,"PAC")).IdInstrumento)).IdEmisor;
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
			data.AccionEdicion = "B";
			data.FechaOperacion = operacion.FechaOperacion;
			data.FechaVencimiento = Fecha.toDate(operacion.FechaVencimiento);
			data.TipoDocumento = operacion.TipoDocumento;
			data.IdContacto = operacion.IdContacto;
			data.TasaAnualizada 	= operacion.TasaAnualizada;
			data.CostoFondo	  		= operacion.CostoFondo;
			data.UtilidadPerdida	= operacion.UtilidadPerdida;
			data.UtilidadFinal		= operacion.UtilidadFinal;
			v_Operacion.add(data);										
		}
		//INGRESO RESERVA, detalle Operacion
		{
			//Detalle Operacion
			if (in.p_DetalleOperacion.size() > 0){
				List<Long> paramInst = new ArrayList<>();
				for (IngresarDetalleCompraVentaPacto_DetalleOperacion_IN detalleOperacion : in.p_DetalleOperacion){
					paramInst.add(detalleOperacion.IdInstrumento);
				}
				//consulto info instrumento
				ConsultarInstrumento ins = new ConsultarInstrumento();
				logger.debug("[IngresarCompraVentaPacto] Inicio llamada a ConsultarInstrumento");
				startTime = System.nanoTime();
				List<InformacionInstrumento> infoIns = ins.logica(conn, paramInst);
				logger.debug("[IngresarCompraVentaPacto] (" + (System.nanoTime() - startTime)/1000000 + ") Fin llamada a ConsultarInstrumento");
				infoInsHash = Utils.List2Hash(infoIns, "IdInstrumento");
				//consultar Instrumento tipo reajuste
				ConsultarInstrumentoTipoReajuste insTip = new ConsultarInstrumentoTipoReajuste();
				logger.debug("[IngresarCompraVentaPacto] Inicio llamada a ConsultarInstrumentoTipoReajuste");
				startTime = System.nanoTime();
				List<InformacionInstrumentoTipoReajuste> infoInsTip = insTip.logica(conn, paramInst);
				logger.debug("[IngresarCompraVentaPacto] (" + (System.nanoTime() - startTime)/1000000 + ") Fin llamada a ConsultarInstrumentoTipoReajuste");
				for (InformacionInstrumentoTipoReajuste inf: infoInsTip){
					infoInsTipHash.put(inf.IdInstrumento, inf.IdTipoReajuste,inf);
				}
			}
			v_OperacionHash = Utils.List2Hash(v_Operacion,"IdInternoOperacion");
			
			for (IngresarDetalleCompraVentaPacto_DetalleOperacion_IN detalleOperacion : in.p_DetalleOperacion){
				
				OperacionObjIn Operacion = (OperacionObjIn) v_OperacionHash.get(detalleOperacion.IdInternoOperacion);
				
				if(Fecha.toDate(detalleOperacion.FechaVencimiento).compareTo(Operacion.FechaVencimiento)<0) {
					
					out.P_OUT_MENSAJES.add( new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "FECHA_DETALLE_MENOR_AL_PACTO","Fecha de vencimiento del instrumento menor a la del pacto"));
					out.P_OUT_ERRORESCONTADOR= 1L;
					return out;					
					
				}else {
					
					ReservaObj_IN data = new ReservaObj_IN();
					data.IdOperacion 			= Operacion.IdOperacion;
					data.IdRegistro				= detalleOperacion.IdInternoDetalle;
					data.IdReserva				= detalleOperacion.IdReserva;
					data.IdConcepto				= detalleOperacion.Concepto;//concepto;//			INGRES / RETRES / INGDET / RETDET / LIMDET
					data.FolioOperacion			= Operacion.FolioOperacion;
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
					data.FechaOperacion			= Operacion.FechaOperacion;
					data.MontoValorizadoReserva = detalleOperacion.MontoValorizadoReserva;
					data.MontoFinalCarteraMonedaSistema=detalleOperacion.MontoFinalCarteraMonedaSistema;
					data.MontoFinalCarteraMonedaInstrumento=detalleOperacion.MontoFinalCarteraMonedaInstrumento;
					
					v_OperacionDetalle.add(data);
					
				}
					
				
				
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
			logger.debug("[IngresarDetalleCompraVentaPacto] Inicio llamada a IngresoReserva");
			startTime = System.nanoTime();
			resultReserva = callReserva.logica(conn, inRes);
			logger.debug("[IngresarDetalleCompraVentaPacto] (" + (System.nanoTime() - startTime)/1000000 + ") Fin llamada a IngresoReserva");
			
			if (MensajeSalida.getNumeroErrores(resultReserva.P_OUT_MENSAJES) > 0){
				out.P_OUT_MENSAJES.addAll(resultReserva.P_OUT_MENSAJES);
				return out;
			}
			
			HashMap<Long, Object> reserva = Utils.List2Hash(resultReserva.P_OUT_RESERVA, "IdReserva");
			for (IngresarDetalleCompraVentaPacto_DetalleOperacion_IN detalleOperacion : in.p_DetalleOperacion){
				ReservaObj_OUT res = (ReservaObj_OUT) reserva.get(detalleOperacion.IdReserva);
				if(res == null) {
					for (ReservaObj_OUT re: resultReserva.P_OUT_RESERVA){
						if (re.IdRegistro.equals(detalleOperacion.IdInternoDetalle) && re.Estado.equals("VIG")){
							detalleOperacion.IdReserva = re.IdReserva;
						}
					}
				} else {
					if (res.IdRegistro.equals(detalleOperacion.IdInternoDetalle) && res.Estado.equals("VIG")){
						detalleOperacion.IdReserva = res.IdReserva;
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
			cantFormasPago = cuentaCantidadFormaDePago(in.p_FormasDePago);
			for (IngresarDetalleCompraVentaPacto_Operacion_IN ope: in.p_Operacion) {
				boolean sinFP = false;
				for (IngresarDetalleCompraVentaPacto_FormaPagoOperacion_IN inComp : in.p_FormasDePago) {
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
						if(cantFormasPago.get(inComp.RecibePagaPortafolio) > 1) {
							data.Monto = BigDecimal.ZERO;
							data.Monto = data.Monto.add(inComp.MontoFormaDePago);
						} else {
							data.Monto = inComp.IdMoneda.equals("CLP") ? ope.Monto : ope.MontoMoneda;
						}	
						data.FechaIngresoComprobantePagoOperacion		= ope.FechaOperacion;//" 		DATE
						data.FechaLiquidacionComprobantePagoOperacion	= ope.FechaLiquidacion;//"	DATE
						data.IdEstado									= inComp.IdEstado;//" 									NVARCHAR(3)
						data.AfectarTesoreria 							= inComp.AfectarTesoreria;
						data.IdCuentaBanco								= inComp.IdCuentaBancaria;
						compPag.p_ComprobantePagoOperacion.add(data);
						sinFP = false;
					} else {
						sinFP = true;
					}
				}
				if (sinFP || in.p_FormasDePago.size() == 0) {
					ComprobantePagoOperacionObjDel_IN data = new ComprobantePagoOperacionObjDel_IN();
					data.IdEmpresa					= in.p_IdEmpresa;
					data.IdOperacion 				= ope.IdOperacion;
					compPagDel.add(data);
				}
			}

			logger.debug("MRF.Llamada Ingreso Comprobante Pago Operación");
			if (compPagDel.size() > 0 ) {
				logger.debug("[IngresarDetalleCompraVentaPacto] Inicio llamada a callCompPag.logicaDeleteAllOperacion");
				startTime = System.nanoTime();
				resultCompPagDel = callCompPag.logicaDeleteAllOperacion(conn, compPagDel);
				logger.debug("[IngresarDetalleCompraVentaPacto] (" + (System.nanoTime() - startTime)/1000000 + ") Fin llamada a callCompPag.logicaDeleteAllOperacion");
				if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0) {
					out.P_OUT_MENSAJES.addAll(resultCompPagDel.P_OUT_MENSAJES);						
					return out;
				}
			}
			logger.debug("[IngresarDetalleCompraVentaPacto] Inicio llamada a CrearComprobantePagoOperacion");
			startTime = System.nanoTime();
			resultCompPag = callCompPag.logica(conn, compPag);
			logger.debug("[IngresarDetalleCompraVentaPacto] (" + (System.nanoTime() - startTime)/1000000 + ") Fin llamada a CrearComprobantePagoOperacion");
			if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0){
				out.P_OUT_MENSAJES.addAll(resultCompPag.P_OUT_MENSAJES);
				return out;
			}
			v_OutComprobantePagoHash = Utils.List2Hash(resultCompPag.P_OUT_COMPRABANTEPAGO,"IdInternoPagoOperacion");
			for (IngresarDetalleCompraVentaPacto_FormaPagoOperacion_IN inComp : in.p_FormasDePago){
				if (v_OutComprobantePagoHash.get(inComp.IdInternoOperacion) != null){
					inComp.IdComprobantePagoOperacion = ((ComprobantePagoOperacionObj_OUT)v_OutComprobantePagoHash.get(inComp.IdInternoOperacion)).IdComprobantePagoOperacion;
				}
			}
		}
		
	

		// 04 SALIDAS: prepara tabla salida
		for (OperacionObjIn ope: v_Operacion) {
			CrearOperacionMesa_Operacion_OUT opSal= new CrearOperacionMesa_Operacion_OUT();
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
			opSal.IdTipoProducto = ope.IdTipoProducto;
			opSal.IdTipoOperacion = ope.IdTipoOperacion;
			opSal.IdTipoReajuste = ope.IdTipoReajuste;
			opSal.Plazo = ope.PlazoEnDias;
			opSal.Monto = ope.MontoOperacion;
			opSal.MontoMoneda = ope.MontoOperacionMoneda;
			opSal.IdMoneda = ope.IdMoneda;
			opSal.IdMonedaLiquidacion = ope.IdMonedaLiquidacion;
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
			opSal.NombreEstado = ""; //((OperacionObjIn)v_OperacionHash.get(ope.IdInternoOperacion)).NombreEstado;
			opSal.Glosa = ope.Glosa;
			opSal.IdTipoCanalIngreso = ope.IdTipoCanalIngreso;
			opSal.IdEmisor = ope.IdEmisor;
			out.P_OUT_OPERACION.add(opSal);
		}
		
		for (IngresarDetalleCompraVentaPacto_DetalleOperacion_IN det: in.p_DetalleOperacion){
			CrearOperacionMesa_DetalleOperacion_OUT detOut = new CrearOperacionMesa_DetalleOperacion_OUT();
			detOut.IdInternoDetalle = det.IdInternoDetalle;
			detOut.IdInternoOperacion = det.IdInternoOperacion;
			detOut.IdReserva = det.IdReserva;
			detOut.IdCliente = det.IdCliente;
			detOut.IdPortafolio = det.IdPortafolio;
			detOut.IdClienteCorredor = det.IdClienteCorredor;
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
		
		if("N".equals(in.p_EsDetalle)) {
			for (IngresarDetalleCompraVentaPacto_FormaPagoOperacion_IN ope: in.p_FormasDePago){
				CrearOperacionMesa_FormaPagoOperacion_OUT opSal= new CrearOperacionMesa_FormaPagoOperacion_OUT();
				opSal.IdInternoPagoOperacion = ope.IdInternoPagoOperacion;
				opSal.IdComprobantePagoOperacion = ope.IdComprobantePagoOperacion;
				opSal.IdFormaDePago = ope.IdFormaDePago;
				opSal.IdMoneda = ope.IdMoneda;
				opSal.IdOperacion = ope.IdOperacion;
				opSal.RecibePagaPortafolio = ope.RecibePagaPortafolio;
				opSal.MontoFormaDePago = ope.MontoFormaDePago;
				opSal.NombreFormaDePago =  ope.FormaDePago.NombreFormaDePago;
				opSal.NombreMoneda = ope.FormaDePago.IdMoneda;
				opSal.IdMedioDePago = ope.FormaDePago.IdMedioDePago;
				
				out.P_OUT_FORMASDEPAGO.add(opSal);
			}
			
		}
		
		
		
			
		logger.info("Termino del servicio MRF.IngresarDetalleCompraVentaPacto, tiempo: " + (System.currentTimeMillis() - init) + " ms - " + (System.currentTimeMillis() - init)/1000 + " seg");
		return out;
	}
	
	private HashMap<String, Long> cuentaCantidadFormaDePago(List<IngresarDetalleCompraVentaPacto_FormaPagoOperacion_IN> fpago) {
		Long cantidadFormaPagoEntregamos = 0L;
		Long cantidadFormaPagoRecibimos = 0L;
		HashMap<String, Long> formasPago = new HashMap<String,Long>();
		for (IngresarDetalleCompraVentaPacto_FormaPagoOperacion_IN fPag :fpago){
			if(fPag.RecibePagaPortafolio.equals("R")) {
				cantidadFormaPagoRecibimos ++;
			}else {
				cantidadFormaPagoEntregamos ++;
			}
		}
		formasPago.put("R", cantidadFormaPagoRecibimos);
		formasPago.put("P", cantidadFormaPagoEntregamos);
		return formasPago;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = Utils.getGsonBuilder();
		IngresarDetalleCompraVentaPacto_IN jin = gson.fromJson(in.toString(), IngresarDetalleCompraVentaPacto_IN.class);
		IngresarDetalleCompraVentaPacto_OUT out = logica(conn, jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
}
