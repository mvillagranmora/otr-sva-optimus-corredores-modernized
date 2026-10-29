package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.custodia.utils.GestionMovimientoCarteraEnTransito;
import cl.bcs.custodia.utils.GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito;
import cl.bcs.custodia.utils.GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar;
import cl.bcs.custodia.utils.IngresoCustodia;
import cl.bcs.custodia.utils.RetiroCustodia;
import cl.bcs.custodia.utils.IngresoCustodia.IngresoCustodiaEntrada;
import cl.bcs.custodia.utils.IngresoCustodia.IngresoCustodiaSalida;
import cl.bcs.custodia.utils.IngresoCustodia.ObjDatosEntrada;
import cl.bcs.fondomutuo.ActualizaCarteraValorizadaFFMM.EntradaActulizaCarteraValorizadaFFMM;
import cl.bcs.fondomutuo.ActualizaCarteraValorizadaFFMM.SalidaActulizaCarteraValorizadaFFMM;
import cl.bcs.fondomutuo.ActualizarCarterasFondosMutuos.Cartera;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.pojo.MovimientoCustodiaFondoMutuo;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ProcesarCustodiaFondoMutuo implements JavaCall {
	
	private static final Logger logger = LogManager.getLogger(ProcesarCustodiaFondoMutuo.class);
	private final String SQL_SELECT_CARTERAS = loadResourceToString("cl/bcs/fondomutuo/sql/ProcesarCustodiaFondoMutuo_SelectCarteras.sql");
	private final String SQL_SELECT_CARTERAS_RETIRADAS = loadResourceToString("cl/bcs/fondomutuo/sql/ProcesarCustodiaFondoMutuo_SelectCarterasRetiradas.sql");
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();

	public static class EntradaProcesarCustodiaFondoMutuo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
	    public String p_IdModulo;
	    public String p_simularOperacion;
		public String p_UsaReglasNegocio;
		public List<MovimientoCustodiaFondoMutuo> p_Movimientos;
		
		public EntradaProcesarCustodiaFondoMutuo() {
			this.p_Movimientos = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaMovimientoCustodiaFondoMutuo {
		public Long IdGenerarMovimientoCustodia;
		public String TablaOrigen;
		public Long FolioCustodia;
		public Long IdCustodia;
		public BigDecimal Cantidad;
		public Date FechaOperacion;
		public Date FechaLiquidacion;
		public Date FechaOrigenCompra;
		public BigDecimal CostoInversionEnFactorAjusteARetirar;
		public BigDecimal ValorFactorAjusteaFechaCurseARetirar;
		public BigDecimal CostoInversionEnFactorAjusteOriginal;
		public BigDecimal CostoInversionEnFactorAjusteActual;
		public BigDecimal ValorFactorOriginal;
		public BigDecimal ValorFactorARetirar;
		public BigDecimal MontoCompraFactorARetirar;
		public BigDecimal MontoCompraFactorOriginal;
		
		public SalidaMovimientoCustodiaFondoMutuo(Long IdGenerarMovimientoCustodia, String TablaOrigen, Long FolioCustodia, Long IdCustodia, BigDecimal Cantidad, Date FechaOperacion, Date FechaLiquidacion, Date FechaOrigenCompra
				, BigDecimal CostoInversionEnFactorAjusteARetirar, BigDecimal ValorFactorAjusteaFechaCurseARetirar, BigDecimal CostoInversionEnFactorAjusteOriginal, BigDecimal CostoInversionEnFactorAjusteActual
				, BigDecimal ValorFactorARetirar, BigDecimal ValorFactorOriginal, BigDecimal MontoCompraFactorARetirar, BigDecimal MontoCompraFactorOriginal){
			this.IdGenerarMovimientoCustodia = IdGenerarMovimientoCustodia;
			this.TablaOrigen = TablaOrigen;
			this.FolioCustodia = FolioCustodia;
			this.IdCustodia = IdCustodia;
			this.Cantidad = Cantidad;
			this.FechaOperacion = FechaOperacion;
			this.FechaLiquidacion = FechaLiquidacion;
			this.FechaOrigenCompra = FechaOrigenCompra;
			this.CostoInversionEnFactorAjusteARetirar = CostoInversionEnFactorAjusteARetirar;
			this.ValorFactorAjusteaFechaCurseARetirar = ValorFactorAjusteaFechaCurseARetirar;
			this.CostoInversionEnFactorAjusteOriginal = CostoInversionEnFactorAjusteOriginal;
			this.CostoInversionEnFactorAjusteActual = CostoInversionEnFactorAjusteActual;
			this.ValorFactorOriginal = ValorFactorOriginal;
			this.ValorFactorARetirar = ValorFactorARetirar;
			this.MontoCompraFactorARetirar = MontoCompraFactorARetirar;
			this.MontoCompraFactorOriginal = MontoCompraFactorOriginal;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class SalidaProcesarCustodiaFondoMutuo {
		public Long					P_OUT_ERRORESCONTADOR;
		public List<SalidaMovimientoCustodiaFondoMutuo> P_OUT_MOVIMIENTOS;
		public List<MensajeSalida>	P_OUT_MENSAJES;
		
		public SalidaProcesarCustodiaFondoMutuo() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();	
			this.P_OUT_MOVIMIENTOS = new ArrayList<SalidaMovimientoCustodiaFondoMutuo>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaProcesarCustodiaFondoMutuo jin = gson.fromJson(in.toString(), EntradaProcesarCustodiaFondoMutuo.class);
		SalidaProcesarCustodiaFondoMutuo out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaProcesarCustodiaFondoMutuo logica(Connection con,EntradaProcesarCustodiaFondoMutuo entrada) throws Exception {
		logger.info("INICIO ProcesarCustodiaFondoMutuo");
		
		SalidaProcesarCustodiaFondoMutuo out = new SalidaProcesarCustodiaFondoMutuo();
		List<ObjDatosEntrada> MovimientosTransito  = new ArrayList<ObjDatosEntrada>();
		List<Long> IdMovimientosEnTransito = new ArrayList<Long>();
		HashMap<Long,Long> IdMovimientoTransitoIdGenerarCustodia = new HashMap<Long,Long>();
		//01 - REALIZAR TRANSITOS
		for(MovimientoCustodiaFondoMutuo mov : entrada.p_Movimientos){
			ObjDatosEntrada obj = new ObjDatosEntrada();
			obj.IdGenerarMovimientoCustodia 	= mov.IdGenerarMovimientoCustodia;
			obj.TablaAfectada 					= _c.Transito;
			obj.AccionOperacion					= _c.TipoOperacionAporteFondoMutuo.equalsIgnoreCase(mov.IdTipoOperacion) ? _c.Ingreso : _c.Retiro;
			obj.EsRegistroNuevo					= _c.SI;
			obj.FechaOperacion 					= mov.FechaOperacion;
			obj.IdCliente 						= mov.IdCliente; 
			obj.IdPortafolio 					= mov.IdPortafolio;
			obj.IdTipoProducto 					= mov.IdTipoProducto;
			obj.IdConcepto 						= mov.IdConcepto;
			obj.IdTipoOperacion 				= mov.IdTipoOperacion;
			obj.IdOperacion 					= mov.IdOperacion;
			obj.IdTipoComprobante 				= mov.IdTipoComprobante;
			obj.FolioComprobante 				= mov.FolioComprobante;
			obj.FechaComprobante 				= mov.FechaComprobante;
			obj.IdBolsa 						= mov.IdBolsa;
			obj.IdAgente 						= mov.IdAgente;
			obj.FechaLiquidacion 				= mov.FechaLiquidacion;
			obj.IdUsuario 						= mov.IdUsuario;
			obj.TipoCarteraNormativa 			= mov.TipoCarteraNormativa;
			obj.FechaCurse 						= mov.FechaCurse;
			obj.TipoFactorAjuste 				= mov.TipoFactorAjuste;
			obj.ValorFactorAjusteaFechaCurse 	= mov.ValorFactorAjusteaFechaCurse;
			obj.CostoEnFactorAjuste 			= mov.CostoEnFactorAjuste;
			obj.IdInstrumento 					= mov.IdInstrumento;
			obj.IdEmisor 						= mov.IdEmisor;
			obj.IdMonedaInstrumento 			= mov.IdMonedaInstrumento;
			obj.IdBoveda 						= mov.IdBoveda;
			obj.Cantidad 						= mov.Cantidad;
			obj.ValorRescate 					= mov.ValorRescate;
			obj.PrecioCompra 					= mov.PrecioCompra;
			obj.MontoCompra 					= mov.MontoCompra;
			obj.MontoCompraMonedaInstrumento 	= mov.MontoCompraMonedaInstrumento;
			obj.IdTipoReajusteInstrumento 		= mov.TipoFactorAjuste;
			obj.IdTipoCanalIngreso 				= mov.IdTipoCanalIngreso;
			obj.CorteMinimo						= BigDecimal.ONE;
			obj.IdRegimenFondoMutuo				= mov.IdRegimenTributario;
			obj.FechaOrigenCompra				= mov.FechaOrigenCompra;
			MovimientosTransito.add(obj);
		}
		
		IngresoCustodia ingresoCustodia = new IngresoCustodia();
		IngresoCustodiaEntrada paramInSP = new IngresoCustodiaEntrada();
		paramInSP.p_IdEmpresa			= entrada.p_IdEmpresa;		
		paramInSP.p_IdModulo			= entrada.p_IdModulo;
		paramInSP.p_IdUsuario			= entrada.p_IdUsuario;
		paramInSP.p_SimularOperacion	= "N";
		paramInSP.p_TicketAtencion 		= 0L;
		paramInSP.p_DatosEntrada		= MovimientosTransito;
		paramInSP.p_ValorizacionAutomatica = _c.NO;
		IngresoCustodiaSalida resultIngresoRetiroTransito = ingresoCustodia.logica(con, paramInSP);
		
		out.P_OUT_ERRORESCONTADOR += resultIngresoRetiroTransito.P_OUT_ERRORESCONTADOR;
		
		for (IngresoCustodiaSalida.ObjDatosSalida reg : resultIngresoRetiroTransito.P_OUT_DATOSSALIDA) {
			for (ObjDatosEntrada mov : MovimientosTransito) {
				if (reg.IdGenerarMovimientoCustodia.equals(mov.IdGenerarMovimientoCustodia)) {
					if (reg.RegistroConProblemas.equalsIgnoreCase("S")) {
						out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_AL_PROCESAR_MOVIMIENTO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
					}
					else {
						IdMovimientosEnTransito.add(reg.IdCustodia);
						IdMovimientoTransitoIdGenerarCustodia.put(reg.IdCustodia, reg.IdGenerarMovimientoCustodia);
					}
					break;
				}
			}
		}
		
		//02 - BUSCA TRANSITOS APORTES Y RESCATES
		List<ObjMovimientosEnTransito> MovimientosEnTransito =  GestionMovimientoCarteraEnTransito.ObtenerMovimientoEnTransito(con, IdMovimientosEnTransito);
		List<ObjMovimientosAActualizar> p_Transitos = new ArrayList<ObjMovimientosAActualizar>();
		
		List<IngresoCustodia.ObjDatosEntrada> p_Aportes = new ArrayList<IngresoCustodia.ObjDatosEntrada>();
		List<RetiroCustodia.ObjDatosEntrada> p_Rescates = new ArrayList<RetiroCustodia.ObjDatosEntrada>();
		
		Map<Long,IngresoCustodia.ObjDatosEntrada> MapValorizarCarteraIngreso = new HashMap<>();
		Map<Long,RetiroCustodia.ObjDatosEntrada>  MapValorizarCarteraRetiro  = new HashMap<>();
		List<Cartera> valorizarCarteras = new ArrayList<Cartera>();
		
		
		if (MovimientosEnTransito.size() > 0 ){			
			for (ObjMovimientosEnTransito MovimientoEnTransito : MovimientosEnTransito) {
				if (_c.TipoOperacionAporteFondoMutuo.equalsIgnoreCase(MovimientoEnTransito.IdTipoOperacion)){
					IngresoCustodia.ObjDatosEntrada	obj = new IngresoCustodia.ObjDatosEntrada();
					obj.IdGenerarMovimientoCustodia			= MovimientoEnTransito.IdMovimientoCarteraEnTransito;
					obj.TablaAfectada 						= _c.Cartera;
					obj.EsRegistroNuevo						= _c.SI;						
					obj.IdMovimientoCarteraEnTransitoOrigen	= MovimientoEnTransito.IdMovimientoCarteraEnTransito;
					obj.AccionOperacion 					= _c.Ingreso;		
					obj.IdCliente 							= MovimientoEnTransito.IdCliente;
					obj.IdPortafolio 						= MovimientoEnTransito.IdPortafolio; 
					obj.IdCuentaCustodioPortafolio 			= MovimientoEnTransito.IdCuentaCustodioPortafolio;
					obj.FechaOperacion 						= MovimientoEnTransito.FechaOperacion;
					obj.FechaVencimientoOperacion 			= MovimientoEnTransito.FechaVencimientoOperacion;
					obj.PlazoEnDiasOperacion 				= MovimientoEnTransito.PlazoEnDiasOperacion;
					obj.IdTipoOperacion						= MovimientoEnTransito.IdTipoOperacion;		
					obj.IdConcepto							= MovimientoEnTransito.IdConcepto;
					obj.IdCondicionLiquidacion 				= MovimientoEnTransito.IdCondicionLiquidacion; 
					obj.FechaLiquidacion 					= MovimientoEnTransito.FechaLiquidacion;
					obj.IdTipoCanalIngreso  				= MovimientoEnTransito.IdTipoCanalIngreso; 		
					obj.CodigoIFRS 							= MovimientoEnTransito.CodigoIFRS;		 		 
					obj.IdTipoComprobante 					= MovimientoEnTransito.IdTipoComprobante; 
					obj.FolioComprobante 					= MovimientoEnTransito.FolioComprobante; 
					obj.IdBolsa								= MovimientoEnTransito.IdBolsa;
					obj.EsIntraday 							= MovimientoEnTransito.EsIntraday; 
					obj.TipoCarteraCorredor 				= MovimientoEnTransito.TipoCarteraCorredor;
					obj.IdUsuario 							= MovimientoEnTransito.IdUsuario;
					obj.IdSucursal 							= MovimientoEnTransito.IdSucursal; 
					obj.IdAgente 							= MovimientoEnTransito.IdAgente;
					obj.Glosa 								= MovimientoEnTransito.Glosa;
					obj.IdInstrumento 						= MovimientoEnTransito.IdInstrumento;
					obj.IdMonedaInstrumento					= MovimientoEnTransito.IdMonedaInstrumento;
					obj.IdTipoReajusteInstrumento 			= MovimientoEnTransito.IdTipoReajuste;
					obj.IdEmisor 							= MovimientoEnTransito.IdEmisor;
					obj.EsFungible 							= MovimientoEnTransito.EsFungible;
					obj.IdBoveda 							= MovimientoEnTransito.IdBoveda;
					obj.CuentaBoveda 						= MovimientoEnTransito.CuentaBoveda;
					obj.EsNominal 							= MovimientoEnTransito.EsNominal;
					obj.IdMonedaEmision 					= MovimientoEnTransito.IdMonedaEmision;
					obj.Cantidad 							= MovimientoEnTransito.Cantidad; 
					obj.CorteMinimo 						= MovimientoEnTransito.CorteMinimo; 
					obj.ValorRescate 						= MovimientoEnTransito.ValorRescate; 
					obj.PrecioCompra 						= MovimientoEnTransito.PrecioCompra; 
					obj.TasaCompra							= MovimientoEnTransito.TasaCompra;
					obj.MontoCompra							= MovimientoEnTransito.MontoCompra;
					obj.MontoCompraMonedaInstrumento 		= MovimientoEnTransito.MontoCompraMonedaInstrumento;
					obj.TipoCarteraNormativa 				= MovimientoEnTransito.TipoCarteraNormativa;
					obj.FechaCurse 							= MovimientoEnTransito.FechaCurse;
					obj.TipoFactorAjuste 					= MovimientoEnTransito.TipoFactorAjuste;
					obj.ValorFactorAjusteaFechaCurse 		= MovimientoEnTransito.ValorFactorAjusteaFechaCurse;
					obj.CostoEnFactorAjuste 				= MovimientoEnTransito.CostoEnFactorAjuste;
					obj.IdOperacion							= MovimientoEnTransito.IdOperacion;
					obj.IdRegimenFondoMutuo					= MovimientoEnTransito.IdRegimenFondoMutuo;
					obj.FechaOrigenCompra					= MovimientoEnTransito.FechaOrigenCompra;
					obj.IdTipoProducto                      = MovimientoEnTransito.IdTipoProducto;
					obj.Nemotecnico							= MovimientoEnTransito.Nemotecnico;
					p_Aportes.add(obj);
					MapValorizarCarteraIngreso.put(obj.IdGenerarMovimientoCustodia, obj);
				}
				
				if (_c.TipoOperacionRescateFondoMutuo.equalsIgnoreCase(MovimientoEnTransito.IdTipoOperacion)){
					RetiroCustodia.ObjDatosEntrada	obj = new RetiroCustodia.ObjDatosEntrada(); 
					obj.IdGenerarMovimientoCustodia			= MovimientoEnTransito.IdMovimientoCarteraEnTransito;
					obj.TablaAfectada 						= _c.Cartera;
					obj.IdMovimientoCarteraEnTransitoOrigen	= MovimientoEnTransito.IdMovimientoCarteraEnTransito;
					obj.IdTipoComprobanteBusqueda			= MovimientoEnTransito.IdTipoComprobante;
					obj.FolioComprobanteBusqueda			= MovimientoEnTransito.FolioComprobante;
					obj.FolioCustodia						= MovimientoEnTransito.FolioCustodia == 0 ? null : MovimientoEnTransito.FolioCustodia;
					obj.AccionOperacion						= _c.Retiro;
					obj.IdCliente							= MovimientoEnTransito.IdCliente;
					obj.IdPortafolio						= MovimientoEnTransito.IdPortafolio;	
					obj.FechaOperacion						= MovimientoEnTransito.FechaOperacion;
					obj.IdTipoOperacion						= MovimientoEnTransito.IdTipoOperacion;
					obj.IdConcepto							= MovimientoEnTransito.IdConcepto;
					obj.IdCondicionLiquidacion				= MovimientoEnTransito.IdCondicionLiquidacion; 
					obj.FechaLiquidacion					= MovimientoEnTransito.FechaLiquidacion;
					obj.IdTipoCanalIngreso					= MovimientoEnTransito.IdTipoCanalIngreso; 		 
					obj.IdTipoComprobante					= MovimientoEnTransito.IdTipoComprobante; 
					obj.FolioComprobante					= MovimientoEnTransito.FolioComprobante;
					obj.IdUsuario							= MovimientoEnTransito.IdUsuario;
					obj.IdSucursal							= MovimientoEnTransito.IdSucursal; 
					obj.IdAgente							= MovimientoEnTransito.IdAgente;
					obj.Glosa								= MovimientoEnTransito.Glosa;
					obj.IdInstrumento						= MovimientoEnTransito.IdInstrumento;
					obj.IdBoveda							= MovimientoEnTransito.IdBoveda;
					obj.CuentaBoveda						= MovimientoEnTransito.CuentaBoveda;
					obj.IdEmisor							= MovimientoEnTransito.IdEmisor;
					obj.EsFungible							= MovimientoEnTransito.EsFungible;
					obj.IdMonedaEmision						= MovimientoEnTransito.IdMonedaEmision; 
					obj.Cantidad							= MovimientoEnTransito.Cantidad; 
					obj.CorteMinimo							= MovimientoEnTransito.CorteMinimo; 
					obj.ValorRescate						= MovimientoEnTransito.ValorRescate; 
					obj.PrecioCompra						= MovimientoEnTransito.PrecioCompra; 
					obj.TasaCompra							= MovimientoEnTransito.TasaCompra;
					obj.MontoCompraMonedaInstrumento		= MovimientoEnTransito.MontoCompraMonedaInstrumento;
					obj.IdOperacion							= MovimientoEnTransito.IdOperacion;
					obj.IdRegimenFondoMutuo					= MovimientoEnTransito.IdRegimenFondoMutuo;
					obj.OrdenaFechaOrigenCompra				= _c.SI;
					obj.IdOrigenGarantia					= _c.CUS;
					obj.Nemotecnico							= MovimientoEnTransito.Nemotecnico;
					obj.MontoCompra							= MovimientoEnTransito.MontoCompra;
					p_Rescates.add(obj);
					MapValorizarCarteraRetiro.put(obj.IdGenerarMovimientoCustodia, obj);
				};
				
				ObjMovimientosAActualizar mov = new ObjMovimientosAActualizar();
				mov.IdMovimientoCarteraEnTransito = MovimientoEnTransito.IdMovimientoCarteraEnTransito;
				mov.EsCertificacion = "S";
				mov.IdUsuario = entrada.p_IdUsuario;
				p_Transitos.add(mov);
			}
		}
		
		//03 - REALIZAR APORTES
		if ( p_Aportes.size() > 0 ){
			IngresoCustodia ingresoCustodiaIngreso = new IngresoCustodia();
			IngresoCustodiaEntrada paramInSPIngreso = new IngresoCustodiaEntrada();
			paramInSPIngreso.p_IdEmpresa		= entrada.p_IdEmpresa;		
			paramInSPIngreso.p_IdModulo			= entrada.p_IdModulo;
			paramInSPIngreso.p_IdUsuario = entrada.p_IdUsuario;
			paramInSPIngreso.p_SimularOperacion	= "N";
			paramInSPIngreso.p_TicketAtencion	= 0L;
			paramInSPIngreso.p_DatosEntrada		= p_Aportes;
			paramInSPIngreso.p_ValorizacionAutomatica = _c.NO;
			IngresoCustodiaSalida resultIngresos = ingresoCustodiaIngreso.logica(con, paramInSPIngreso);			
			preparaValorizacionIngreso (valorizarCarteras,MapValorizarCarteraIngreso ,resultIngresos ) ;

			if (!resultIngresos.P_OUT_ERRORESCONTADOR.equals(0L)){
				for (IngresoCustodiaSalida.ObjDatosSalida msg : resultIngresos.P_OUT_DATOSSALIDA){
					if (msg.RegistroConProblemas.equals("N")) continue;
					out.P_OUT_MENSAJES.add(new MensajeSalida(IdMovimientoTransitoIdGenerarCustodia.get(msg.IdGenerarMovimientoCustodia) ,MensajeSalida.MENSAJE_ERROR, msg.Mensaje, new HashMap<>() ,con,entrada.p_IdEmpresa));
				}
				
				out.P_OUT_ERRORESCONTADOR += resultIngresos.P_OUT_ERRORESCONTADOR;
				return out;
			}
			
			for (IngresoCustodiaSalida.ObjDatosSalida result : resultIngresos.P_OUT_DATOSSALIDA){
				HashMap<String,String> valores = new HashMap<String,String>();
				valores.put("Folio", result.FolioCustodia.toString());
				
				if (result.RegistroConProblemas.equalsIgnoreCase("N")) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(IdMovimientoTransitoIdGenerarCustodia.get(result.IdGenerarMovimientoCustodia) ,MensajeSalida.MENSAJE_INFORMACION, "UI_INGRESO_TITULO_OK", valores ,con,entrada.p_IdEmpresa));
					out.P_OUT_MOVIMIENTOS.add(new SalidaMovimientoCustodiaFondoMutuo(IdMovimientoTransitoIdGenerarCustodia.get(result.IdGenerarMovimientoCustodia), result.TablaOrigen, result.FolioCustodia, result.IdCustodia, BigDecimal.ZERO, Fecha.toDate("0001-01-01"), Fecha.toDate("0001-01-01"), Fecha.toDate("0001-01-01"),BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, result.MontoCompraFactorOriginal));
				}
				else {
					out.P_OUT_MENSAJES.add(new MensajeSalida(IdMovimientoTransitoIdGenerarCustodia.get(result.IdGenerarMovimientoCustodia) ,MensajeSalida.MENSAJE_ERROR, "UI_INGRESO_TITULO_ERROR", valores ,con,entrada.p_IdEmpresa));
					out.P_OUT_ERRORESCONTADOR += 1L;
				}
			}
		}
		
		//04 - REALIZAR RETIROS
		if (p_Rescates.size() > 0 ){
			RetiroCustodia retiroCustodia = new RetiroCustodia();
			RetiroCustodia.RetiroCustodiaEntrada paramInSPRetiro = new RetiroCustodia.RetiroCustodiaEntrada();
			paramInSPRetiro.p_IdEmpresa			= entrada.p_IdEmpresa;
			paramInSPRetiro.p_IdModulo	= entrada.p_IdModulo;
			paramInSPRetiro.p_IdUsuario = entrada.p_IdUsuario;
			paramInSPRetiro.p_SimularOperacion	= "N";
			paramInSPRetiro.p_TicketAtencion	= 0L;
			paramInSPRetiro.p_DatosEntrada		= p_Rescates;
			paramInSPRetiro.p_ValorizacionAutomatica = _c.NO;
			logger.debug("Se envia a retirar custodia", paramInSPRetiro);
			RetiroCustodia.RetiroCustodiaSalida resultRetiros = retiroCustodia.logica(con, paramInSPRetiro);
			preparaValorizacionRetiro(con,valorizarCarteras,resultRetiros);
			
			if (!resultRetiros.P_OUT_ERRORESCONTADOR.equals(0L)){
				for (RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida msg : resultRetiros.P_OUT_DATOSSALIDA){
					if (msg.RegistroConProblemas.equals("N")) continue;
					out.P_OUT_MENSAJES.add(new MensajeSalida(IdMovimientoTransitoIdGenerarCustodia.get(msg.IdGenerarMovimientoCustodia) ,MensajeSalida.MENSAJE_ERROR,msg.CodigoMensaje, msg.Mensaje ));
				};				
				out.P_OUT_ERRORESCONTADOR += 1L;
		
			};
			
			for (RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida result : resultRetiros.P_OUT_DATOSSALIDA){
				if (!result.RegistroConProblemas.equals("N")) continue;
				for (RetiroCustodia.ObjDatosEntrada ret : p_Rescates){
					if (!ret.IdGenerarMovimientoCustodia.equals(result.IdGenerarMovimientoCustodia)) continue;

					HashMap<String,String> valores = new HashMap<String,String>();
					valores.put("v_Cantidad", ret.Cantidad.toString());
					valores.put("v_FolioCustodia", result.FolioCustodia.toString());
					
					if (result.RegistroConProblemas.equalsIgnoreCase("N")){
						out.P_OUT_MENSAJES.add(new MensajeSalida(IdMovimientoTransitoIdGenerarCustodia.get(result.IdGenerarMovimientoCustodia) ,MensajeSalida.MENSAJE_INFORMACION, "UI_RETIRO_TITULO_OK", valores ,con,entrada.p_IdEmpresa));
						out.P_OUT_MOVIMIENTOS.add(new SalidaMovimientoCustodiaFondoMutuo(IdMovimientoTransitoIdGenerarCustodia.get(result.IdGenerarMovimientoCustodia), result.TablaOrigen, result.FolioCustodia, result.IdCustodia, result.Cantidad, result.FechaOperacion, result.FechaLiquidacion, result.FechaOrigenCompra, result.CostoInversionEnFactorAjusteARetirar, result.ValorFactorAjusteaFechaCurseARetirar, result.CostoInversionEnFactorAjusteOriginal, result.CostoInversionEnFactorAjusteActual, result.ValorFactorARetirar, result.ValorFactorOriginal, result.MontoCompraFactorARetirar, result.MontoCompraFactorOriginal));
					}
					else {
						out.P_OUT_MENSAJES.add(new MensajeSalida(IdMovimientoTransitoIdGenerarCustodia.get(result.IdGenerarMovimientoCustodia) ,MensajeSalida.MENSAJE_ERROR, "UI_RETIRO_TITULO_ERROR", valores ,con,entrada.p_IdEmpresa));
						out.P_OUT_ERRORESCONTADOR += 1L;
					};
				};						
			};
		};
		valorizarCarteras(con,valorizarCarteras,entrada,out);
		
		//05 - ACTUALIZAR MOVIMIENTOS EN TRANSITO
		if (out.P_OUT_ERRORESCONTADOR.equals(0L)){
			GestionMovimientoCarteraEnTransito.ActualizarMovimientoEnTransito(con, p_Transitos);
		};
		
		logger.info("SALIDA ProcesarCustodiaFondoMutuo");
		return out;
	}
	
	private void preparaValorizacionIngreso (List<Cartera> valorizarCarteras,Map<Long,IngresoCustodia.ObjDatosEntrada> valorizacionCarteras ,IngresoCustodiaSalida salidaCustodia ) {
						
		List<Cartera> carterasIngresoCustodiaParaValorizar = salidaCustodia.P_OUT_DATOSSALIDA.parallelStream().map(cartera->{
			IngresoCustodia.ObjDatosEntrada objSalidaCartera = valorizacionCarteras.get(cartera.IdGenerarMovimientoCustodia);
			Cartera car = new Cartera();
			car.IdCartera= cartera.IdCustodia;
			car.IdMoneda = objSalidaCartera.IdMonedaInstrumento;
			car.IdSerieFondo = objSalidaCartera.Nemotecnico;
			car.INGCantidad = objSalidaCartera.Cantidad;
			car.INGPrecioCompra = objSalidaCartera.PrecioCompra;
			car.INGMontoCompraActualMonedaInstrumento = objSalidaCartera.MontoCompraMonedaInstrumento;
			return car;
		}).collect(Collectors.toList());
		valorizarCarteras.addAll(carterasIngresoCustodiaParaValorizar);
	}
	
	private void preparaValorizacionRetiro (Connection con, List<Cartera> valorizarCarteras,RetiroCustodia.RetiroCustodiaSalida salidaCustodia ) throws SQLException {
		logger.info("INICIO::valorizarCarteras");
		SQL sql=new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		List<Long> folios=  salidaCustodia.P_OUT_DATOSSALIDA.parallelStream().map(car->car.FolioCustodia).distinct().collect(Collectors.toList());
		List<Long> idCarteras =  salidaCustodia.P_OUT_CARTERAS_RETIRADAS.parallelStream().map(car->car.IdCartera).distinct().collect(Collectors.toList());
		int skip = 0;
		int take = 30000;
		int sizeF = folios.size();
		int sizeC = idCarteras.size();
		logger.debug("Busqueda de carteras vigentes");
		if(sizeF >= take) {
			while(skip < folios.size()) {
				List<Long> foliosTemp = new ArrayList<Long>();
				if(sizeF > take) {
					foliosTemp = new ArrayList<Long>(folios.subList(skip, take));
					params.put("FolioCartera", foliosTemp);
					List<Cartera> carterasVigentes = sql.query(con, SQL_SELECT_CARTERAS, params, Cartera.class);
					params.clear();
					valorizarCarteras.addAll(carterasVigentes);
					skip = skip + take;
					sizeF = sizeF - take;
				} else {
					foliosTemp = new ArrayList<Long>(folios.subList(skip, folios.size()));
					params.put("FolioCartera", foliosTemp);
					List<Cartera> carterasVigentes = sql.query(con, SQL_SELECT_CARTERAS, params, Cartera.class);
					params.clear();
					valorizarCarteras.addAll(carterasVigentes);
					skip = skip + take;
				}
			}
			skip = 0;
		} else {
			params.put("FolioCartera", folios);
			List<Cartera> carterasVigentes = sql.query(con, SQL_SELECT_CARTERAS, params, Cartera.class);
			valorizarCarteras.addAll(carterasVigentes);
			params.clear();
		}
		logger.debug("Fin busqueda de carteras vigentes");
		logger.debug("Busqueda de carteras retiradas");
		if(sizeC >= take) {
			while(skip < idCarteras.size()) {
				List<Long> idCarterasTemp = new ArrayList<Long>();
				if(sizeC > take) {
					idCarterasTemp = new ArrayList<Long>(idCarteras.subList(skip, take));
					params.put("IdCartera", idCarterasTemp);
					List<Cartera> carterasRetiradas = sql.query(con, SQL_SELECT_CARTERAS_RETIRADAS, params, Cartera.class);
					params.clear();
					valorizarCarteras.addAll(carterasRetiradas);
					skip = skip + take;
					sizeC = sizeC - take;
				} else {
					idCarterasTemp = new ArrayList<Long>(idCarteras.subList(skip, idCarteras.size()));
					params.put("IdCartera", idCarterasTemp);
					List<Cartera> carterasRetiradas = sql.query(con, SQL_SELECT_CARTERAS_RETIRADAS, params, Cartera.class);
					valorizarCarteras.addAll(carterasRetiradas);
					skip = skip + take;
				}
			}			
		} else {
			params.put("IdCartera", idCarteras);
			List<Cartera> carterasRetiradas = sql.query(con, SQL_SELECT_CARTERAS_RETIRADAS, params, Cartera.class);
			valorizarCarteras.addAll(carterasRetiradas);
		}
		logger.debug("Fin busqueda de carteras retiradas");
		logger.info("FIN::valorizarCarteras");
	}
	
	private void valorizarCarteras (Connection con,List<Cartera> valorizacionCarteras ,EntradaProcesarCustodiaFondoMutuo entrada,SalidaProcesarCustodiaFondoMutuo out) throws Exception {
		ActualizaCarteraValorizadaFFMM valoriza = new ActualizaCarteraValorizadaFFMM();
		EntradaActulizaCarteraValorizadaFFMM inValoriza = new EntradaActulizaCarteraValorizadaFFMM();
		
		inValoriza.p_IdEmpresa= entrada.p_IdEmpresa;
		inValoriza.p_IdModulo = entrada.p_IdModulo;
		inValoriza.p_IdUsuario= entrada.p_IdUsuario;
		inValoriza.carteras   = valorizacionCarteras;
		
		SalidaActulizaCarteraValorizadaFFMM  outValoriza = valoriza.logica(con,inValoriza);
		
		if(outValoriza.P_OUT_ERRORESCONTADOR>0) {
			out.P_OUT_ERRORESCONTADOR= Long.valueOf(outValoriza.P_OUT_MENSAJES.size());
			out.P_OUT_MENSAJES.addAll(outValoriza.P_OUT_MENSAJES);
		}
	}
	
	
}
