package cl.bcs.mesa.MRF;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.utils.CacheValorizacion;
import cl.bcs.custodia.utils.GestionMovimientoCarteraEnTransito;
import cl.bcs.custodia.utils.IngresoCustodia;
import cl.bcs.custodia.utils.IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida;
import cl.bcs.facturacion.ProcesarFacturacionCustodia.ClienteNeteo;
import cl.bcs.facturacion.ProcesarFacturacionCustodia.EntradaNeteo;
import cl.bcs.custodia.utils.IngresoCustodia.ObjDatosEntrada;
import cl.bcs.custodia.utils.RetiroCustodia;
import cl.bcs.mesa.MRF.IngresarCompraVentaPacto.IngresarCompraVentaPactoUpdate;
import cl.bcs.plataforma.bat.AgregarPrograma;
import cl.bcs.plataforma.bat.AgregarPrograma.InAgregarPrograma.Parametro;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearOperacionMesaCustodia implements JavaCall{
	
	private static final Logger logger = LogManager.getLogger(CrearOperacionMesaCustodia.class);
	
	private final String SQL_SELECT_PARAMETRO_NOMINAL = loadResourceToString("cl/bcs/mesa/sql/ConsultarConfiguracionMesa.sql");	
	private final String SQL_UPDATE_OPERACIONES = loadResourceToString("cl/bcs/mesa/sql/IngresoOperacion_UpdateOperacion.sql");
	private final String SQL_UPDATE_RESERVA = loadResourceToString("cl/bcs/custodia/sql/IngresoReserva_UpdateFolioNominalReserva.sql");
	private final String SQL_UPDATE_RESERVA_SUBYACENTE = loadResourceToString("cl/bcs/custodia/sql/IngresoReserva_UpdateFolioSubyacenteReserva.sql");
	private final String SQL_SELECT_DETALLE_RESERVA = loadResourceToString("cl/bcs/mesa/sql/CrearOperacionMesaCustodia_SelectDetalleReserva.sql");
	
	public final List<String> operacionesTermino = Arrays.asList("COMTER", "VENTER");
	
	public static class OperacionMesaCustodiaObj {
		public Long				IdMovimientoEnTransito;
		public Long				IdReserva;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class OpCertificaCustodia {
		public Long 	IdMovimientoCarteraEnTransito;
		public String	IdTipoOperacion;
	}
	
	
	public static class CrearOperacionMesaCustodia_IN {
		public Long				p_IdEmpresa;
	    public Long				p_IdUsuario;
	    public String			p_FechaSistema;
	    public Long				p_IdSucursal;
	    public String			p_IdModulo;
	    public String			p_UsaReglasNegocio;
	    public Long 			p_TicketAtencion;
	    public String			p_EjecutarValorizador = "S";  // Indica si se ejecuta el valorizador(Si viene desde IngresoCompraVenta (Termino o Pacto) se ejecuta alla por lo que no se debe ejecutar otra vez)
	    public String			p_AfectaCustodia = "S";
	    
	    public BigDecimal		p_TasaOperacion;
	    
		public List<Long>					p_MovimientoEnTransito;
		public List<Long>					p_Reserva;
		public List<OpCertificaCustodia>	P_MOVIMIENTOSCERTIFICAR;
		
		public CrearOperacionMesaCustodia_IN() {
			this.p_MovimientoEnTransito = new ArrayList<Long>();
			this.p_Reserva = new ArrayList<Long>();
			this.P_MOVIMIENTOSCERTIFICAR = new ArrayList<OpCertificaCustodia>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	public static class CrearOperacionMesaCustodia_OUT {
		public List<MensajeSalida>				P_OUT_MENSAJES;
		public Long								P_OUT_ERRORESCONTADOR;
		public List<IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida> 	P_OUT_INGRESOS;
		public List<RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida>		P_OUT_RETIROS;
		
		
		public CrearOperacionMesaCustodia_OUT() {
			P_OUT_MENSAJES 	= new ArrayList<MensajeSalida>();
			P_OUT_INGRESOS 	= new ArrayList<IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida>();
			P_OUT_RETIROS 	= new ArrayList<RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida>();
		}


		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class ComprobantePagoOperacion {
		public Long IdComprobantePagoOperacion;	
		public Long IdEmpresa;
		public Long IdOperacion;
		public Long IdIngresoEgreso;
		public String IdTipoProducto;
		public String IdFormaDePago;
		public String RecibePaga;
		public String IdModalidadPago;
		public String IdMoneda;
		public BigDecimal Monto;
		public Date FechaIngresoComprobantePagoOperacion;
		public Date FechaLiquidacionComprobantePagoOperacion;
		public String IdEstado;
		public Long IdAbono;
		public Long IdCargo;
		public String IdMonedaLiquidacion;
		public BigDecimal MontoLiquidacion;
		public String AfectarTesoreria;
		public Long IdIngresoEgresoAnticipo;
		public Long IdAbonoAnticipo;
		public Long IdCargoAnticipo;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	public static class ReservaUpdate {
		public Long IdReserva;
		public Long IdCartera;
		public Long FolioCartera;
		public String IdEstado;
		public Long FolioSubyacente;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class ConfigMesa {
		public Long 	IdEmpresa;
		public String  	IdTipoProducto;     
		public String 	CodigoParametro; 
		public String 	Descripcion;    
		public String 	Valor;		
	}
	
	public static class SelectDetalleReserva {
		public Long 	IdReserva;
		public Long 	FolioCarteraCustodia;
		public Long 	IdCarteraCustodia;
	}
	
	public CrearOperacionMesaCustodia_OUT logica (Connection conn, CrearOperacionMesaCustodia_IN in) throws Exception {
		
		Long init = System.currentTimeMillis();
		long startTime = System.nanoTime();
		
		SQL _sql = new SQL();
		CrearOperacionMesaCustodia_OUT out = new CrearOperacionMesaCustodia_OUT();
		Long Id = 0L;
		String PRM_DEBE_CERTIFICAR_NOMINALES = "";
		
		HashMap<String,String> mapMsg = new HashMap<>();
		logger.info("Inicio servicio MRF.CrearOperacionMesaCustodia");
		
		//List<Long> IdOperacion = new ArrayList<Long>();
		
		//* 01.0 hago los retiros e ingresos correspondientes
		
		// Son Movimientos por certificar ???
		if( in.p_MovimientoEnTransito.size() <= 0 && in.p_IdModulo.equalsIgnoreCase("CUS") ) {
			// Viene de la certificacion
			for (OpCertificaCustodia OpCertifica: in.P_MOVIMIENTOSCERTIFICAR){
				in.p_MovimientoEnTransito.add(OpCertifica.IdMovimientoCarteraEnTransito);	
			}
		}
		
		
		List<GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito> MovimientosEnTransitoTMP =  GestionMovimientoCarteraEnTransito.ObtenerMovimientoEnTransito(conn, in.p_MovimientoEnTransito);
		List<GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar> p_Transitos = new ArrayList<GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar>();
		List<IngresoCustodia.ObjDatosEntrada> p_IngresoCartera = new ArrayList<IngresoCustodia.ObjDatosEntrada>();
		List<IngresoCustodia.ObjDatosEntrada> p_IngresoCarteraReal = new ArrayList<IngresoCustodia.ObjDatosEntrada>();
		List<IngresoCustodia.ObjDatosEntrada> p_IngresoCarteraDescartada = new ArrayList<IngresoCustodia.ObjDatosEntrada>();		
		List<IngresoCustodia.ObjDatosEntrada> p_IngresoCarteraPacto = new ArrayList<IngresoCustodia.ObjDatosEntrada>();
		List<IngresoCustodia.ObjDatosEntrada> p_IngresoCarteraSubyacentePacto = new ArrayList<IngresoCustodia.ObjDatosEntrada>();
		List<RetiroCustodia.ObjDatosEntrada> p_RetiroCartera = new ArrayList<RetiroCustodia.ObjDatosEntrada>();
		
		List<String> IdTipoOperacion = new ArrayList<String>();
		List<String> EsNominal = new ArrayList<String>();

		HashMap<String,Object> Parms = new HashMap<>();
		Parms.put("IdEmpresa", in.p_IdEmpresa);
		Parms.put("IdTipoProducto", "MERF");
		Parms.put("CodigoParametro", "NOMINAL_EN_TRANSITO_CORREDOR");
	
		logger.debug("[CrearOperacionMesaCustodia] Inicio SQL_SELECT_PARAMETRO_NOMINAL");
		startTime = System.nanoTime();
		List<ConfigMesa> selectParametro = _sql.query(conn, SQL_SELECT_PARAMETRO_NOMINAL, Parms, ConfigMesa.class);
		logger.debug("[CrearOperacionMesaCustodia] (" + (System.nanoTime() - startTime)/1000000 + ") Fin SQL_SELECT_PARAMETRO_NOMINAL");

		
		if ( selectParametro.size() > 0  ) {
			for (ConfigMesa Parametro: selectParametro){
				PRM_DEBE_CERTIFICAR_NOMINALES = Parametro.Valor == null ? "S" : Parametro.Valor;
			}
		} 
		
		
		if (MovimientosEnTransitoTMP.size() <= 0){
			//mapMsg.put("IdOperacion", op.IdOperacion.toString());
			out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"SRV_MRF_SIN_MOVIMIENTO_EN_TRANSITO",mapMsg,conn,in.p_IdEmpresa));
			Id++;
			return out;
		}
		
		
		// lista MovimientosEnTransito se llena con las operaciones que se deben certificar de inmediato en Servicio.
		List<GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito> MovimientosEnTransito = new ArrayList<GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito>();
		for (GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito EnTransitoTMP : MovimientosEnTransitoTMP) {
			// es COMPRA CON PACTO
			if ("COMPAC".equalsIgnoreCase(EnTransitoTMP.IdTipoOperacion)) {
				MovimientosEnTransito.add(EnTransitoTMP);
			}
			// es VENTA CON PACTO o modulo CUSTODIA
			else if (EnTransitoTMP.IdTipoOperacion.equalsIgnoreCase("VENPAC") || in.p_IdModulo.equalsIgnoreCase("CUS")) {
				MovimientosEnTransito.add(EnTransitoTMP);
			}
			// es operacion a TERMINO y no es NOMINAL
			else if (("COMTER".equalsIgnoreCase(EnTransitoTMP.IdTipoOperacion) || "VENTER".equalsIgnoreCase(EnTransitoTMP.IdTipoOperacion)) && "N".equalsIgnoreCase(EnTransitoTMP.EsNominal)) {
				MovimientosEnTransito.add(EnTransitoTMP);
			}else if ("VENTER".equalsIgnoreCase(EnTransitoTMP.IdTipoOperacion) && EnTransitoTMP.IdClienteRelacionado.equals(EnTransitoTMP.IdCliente) 
					&& "S".equals(EnTransitoTMP.EsNominal)) {
				MovimientosEnTransito.add(EnTransitoTMP);
			}
			// si Parametro indica que NO debe quedar por certificar
			else if(!"VENTER".equalsIgnoreCase(EnTransitoTMP.IdTipoOperacion) && PRM_DEBE_CERTIFICAR_NOMINALES.equals("N")) {
				MovimientosEnTransito.add(EnTransitoTMP);
			}
						
			IdTipoOperacion.add(EnTransitoTMP.IdTipoOperacion);
			EsNominal.add(EnTransitoTMP.EsNominal);
		} 
		
		
		if(IdTipoOperacion.get(0).equalsIgnoreCase("VENTER") && in.p_AfectaCustodia.equalsIgnoreCase("N") && EsNominal.get(0).equalsIgnoreCase("S") 
				&& "S".equals(PRM_DEBE_CERTIFICAR_NOMINALES)){
				logger.debug("[CrearOperacionMesaCustodia] Inicio SQL_SELECT_DETALLE_RESERVA");
				
				HashMap<String,Object> ParmsReserva = new HashMap<>();
				ParmsReserva.put("IdReserva", in.p_Reserva);
				List<SelectDetalleReserva> selectDetalleReserva = _sql.query(conn, SQL_SELECT_DETALLE_RESERVA, ParmsReserva, SelectDetalleReserva.class);
				
				startTime = System.nanoTime();
				logger.debug("[CrearOperacionMesaCustodia] (" + (System.nanoTime() - startTime)/1000000 + ") Fin SQL_SELECT_DETALLE_RESERVA");
				
				List<ReservaUpdate> upd = new ArrayList<>();
				for (SelectDetalleReserva detalle: selectDetalleReserva){
					mapMsg.clear();
					mapMsg.put("Folio", detalle.FolioCarteraCustodia.toString());
					
					Id++;
					ReservaUpdate updDat = new ReservaUpdate();
					updDat.IdReserva = detalle.IdReserva;
					updDat.FolioCartera = detalle.FolioCarteraCustodia;
					updDat.IdCartera = detalle.IdCarteraCustodia;
					updDat.IdEstado = "LIQ";
					upd.add(updDat);
				}
				
				logger.debug("MRF.Actualizar Folio Cartera");
				logger.debug("[CrearOperacionMesaCustodia] Inicio SQL_UPDATE_RESERVA");
				
				_sql.batch(conn, SQL_UPDATE_RESERVA, upd, ReservaUpdate.class);
				
				startTime = System.nanoTime();
				logger.debug("[CrearOperacionMesaCustodia] (" + (System.nanoTime() - startTime)/1000000 + ") Fin SQL_UPDATE_RESERVA");
				if("VENTER".equalsIgnoreCase(IdTipoOperacion.get(0)) && "S".equalsIgnoreCase(EsNominal.get(0))) {
					//mapMsg.put("IdOperacion", op.IdOperacion.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_INFORMACION,"SRV_MRF_CARTERAS_POR_CERTIFICAR",mapMsg,conn,in.p_IdEmpresa));
				}
			}else{
				if(!IdTipoOperacion.get(0).equalsIgnoreCase("VENPAC") && !IdTipoOperacion.get(0).equalsIgnoreCase("VENTER") && !IdTipoOperacion.get(0).equalsIgnoreCase("COMPAC") 
						&& in.p_AfectaCustodia.equalsIgnoreCase("N") && "S".equals(PRM_DEBE_CERTIFICAR_NOMINALES)) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_INFORMACION,"SRV_MRF_CARTERAS_POR_CERTIFICAR",mapMsg,conn,in.p_IdEmpresa));
				}
				if(IdTipoOperacion.get(0).equalsIgnoreCase("VENTER") && EsNominal.get(0).equalsIgnoreCase("S") && "N".equals(PRM_DEBE_CERTIFICAR_NOMINALES)){
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_INFORMACION,"SRV_MRF_CARTERAS_POR_CERTIFICAR",mapMsg,conn,in.p_IdEmpresa));
				}
			}
		
		if ( MovimientosEnTransito.size() > 0 ){
			for (GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito MovimientoEnTransito : MovimientosEnTransito) {
				IngresoCustodia.ObjDatosEntrada	ObjEntrada = new IngresoCustodia.ObjDatosEntrada();
				ObjEntrada.IdGenerarMovimientoCustodia			= MovimientoEnTransito.IdMovimientoCarteraEnTransito;
				ObjEntrada.IdMovimientoCarteraEnTransitoOrigen	= MovimientoEnTransito.IdMovimientoCarteraEnTransito;
				ObjEntrada.IdMovimientoCarteraEnTransitoRelacionado = MovimientoEnTransito.IdMovimientoCarteraEnTransitoRelacionado;
				ObjEntrada.IdCuentaCustodioPortafolio			= MovimientoEnTransito.IdCuentaCustodioPortafolio;
				ObjEntrada.FechaOperacion						= MovimientoEnTransito.FechaOperacion;
				ObjEntrada.FechaVencimientoOperacion			= MovimientoEnTransito.FechaVencimientoOperacion;
				ObjEntrada.PlazoEnDiasOperacion					= MovimientoEnTransito.PlazoEnDiasOperacion;
				ObjEntrada.IdTipoOperacion						= MovimientoEnTransito.IdTipoOperacion;
				ObjEntrada.IdTipoProducto						= MovimientoEnTransito.IdTipoProducto;
				ObjEntrada.IdConcepto							= MovimientoEnTransito.IdConcepto;
				ObjEntrada.IdCondicionLiquidacion				= MovimientoEnTransito.IdCondicionLiquidacion; 
				ObjEntrada.FechaLiquidacion						= MovimientoEnTransito.FechaLiquidacion;
				ObjEntrada.IdTipoCanalIngreso					= MovimientoEnTransito.IdTipoCanalIngreso; 		
				ObjEntrada.CodigoIFRS							= MovimientoEnTransito.CodigoIFRS;		 		 
				ObjEntrada.IdTipoComprobante					= MovimientoEnTransito.IdTipoComprobante; 
				ObjEntrada.FolioComprobante						= MovimientoEnTransito.FolioComprobante; 
				ObjEntrada.IdBolsa								= MovimientoEnTransito.IdBolsa;
				ObjEntrada.EsIntraday							= MovimientoEnTransito.EsIntraday; 
				ObjEntrada.TipoCarteraCorredor					= MovimientoEnTransito.TipoCarteraCorredor;
				ObjEntrada.IdUsuario							= MovimientoEnTransito.IdUsuario;
				ObjEntrada.IdSucursal							= MovimientoEnTransito.IdSucursal; 
				ObjEntrada.IdAgente								= MovimientoEnTransito.IdAgente;
				ObjEntrada.Glosa								= MovimientoEnTransito.Glosa;
				ObjEntrada.IdInstrumento						= MovimientoEnTransito.IdInstrumento;
				ObjEntrada.Nemotecnico							= MovimientoEnTransito.Nemotecnico;
				ObjEntrada.IdMonedaInstrumento					= MovimientoEnTransito.IdMonedaInstrumento;
				ObjEntrada.IdMonedaEmision						= MovimientoEnTransito.IdMonedaEmision;
				ObjEntrada.IdTipoReajusteInstrumento			= MovimientoEnTransito.IdTipoReajuste;
				ObjEntrada.IdEmisor								= MovimientoEnTransito.IdEmisor;
				ObjEntrada.EsFungible							= MovimientoEnTransito.EsFungible;
				ObjEntrada.IdBoveda								= MovimientoEnTransito.IdBoveda;
				ObjEntrada.CuentaBoveda							= MovimientoEnTransito.CuentaBoveda;
				ObjEntrada.EsNominal							= MovimientoEnTransito.EsNominal;
				ObjEntrada.IdMonedaEmision						= MovimientoEnTransito.IdMonedaEmision; 
				ObjEntrada.Cantidad								= MovimientoEnTransito.Cantidad; 
				ObjEntrada.CorteMinimo							= MovimientoEnTransito.CorteMinimo; 
				ObjEntrada.ValorRescate							= MovimientoEnTransito.ValorRescate; 
				ObjEntrada.PrecioCompra							= MovimientoEnTransito.PrecioCompra; 
				ObjEntrada.TasaCompra							= MovimientoEnTransito.TasaCompra;
				ObjEntrada.MontoCompra							= MovimientoEnTransito.MontoCompra;
				ObjEntrada.MontoCompraMonedaInstrumento			= MovimientoEnTransito.MontoCompraMonedaInstrumento;
				ObjEntrada.MontoAPlazo							= MovimientoEnTransito.MontoAPlazo; 
				ObjEntrada.MontoAPlazoMonedaInstrumento			= MovimientoEnTransito.MontoAPlazoMonedaInstrumento;
				ObjEntrada.IdOperacion							= MovimientoEnTransito.IdOperacion;
				ObjEntrada.FechaVencimientoInstrumento			= MovimientoEnTransito.FechaVencimientoInstrumento;
				ObjEntrada.IdMesa								= MovimientoEnTransito.IdMesa;
				
				if (MovimientoEnTransito.IdTipoInstrumento.equalsIgnoreCase("PAC")){//cartera pacto
					ObjEntrada.TablaAfectada 						= "CARTERA";
					ObjEntrada.EsRegistroNuevo						= "S";						
					ObjEntrada.AccionOperacion						= "INGRESO";		
					ObjEntrada.IdCliente							= MovimientoEnTransito.IdCliente;
					ObjEntrada.IdPortafolio							= MovimientoEnTransito.IdPortafolio;
					ObjEntrada.IdClienteRelacionado					= MovimientoEnTransito.IdClienteRelacionado;
					ObjEntrada.IdPortafolioRelacionado				= MovimientoEnTransito.IdPortafolioRelacionado;
					p_IngresoCarteraPacto.add(ObjEntrada);
				}
				else if (operacionesTermino.contains(MovimientoEnTransito.IdTipoOperacion) && MovimientoEnTransito.IdMovimientoCarteraEnTransitoRelacionado.compareTo(0L) == 0 && MovimientoEnTransito.EsNominal.equalsIgnoreCase("S") ) {
					ObjEntrada.TablaAfectada 						= "CARTERA";
					ObjEntrada.EsRegistroNuevo						= "S";						
					ObjEntrada.AccionOperacion						= "INGRESO";	
					ObjEntrada.EsNominal                            = "S";
					ObjEntrada.IdCliente							= MovimientoEnTransito.IdTipoOperacion.contains("COM")?MovimientoEnTransito.IdClienteRelacionado:MovimientoEnTransito.IdCliente;
					ObjEntrada.IdPortafolio							= MovimientoEnTransito.IdTipoOperacion.contains("COM")?MovimientoEnTransito.IdPortafolioRelacionado:MovimientoEnTransito.IdPortafolio; 
					ObjEntrada.IdClienteRelacionado					= MovimientoEnTransito.IdTipoOperacion.contains("COM")?MovimientoEnTransito.IdCliente:MovimientoEnTransito.IdClienteRelacionado;
					ObjEntrada.IdPortafolioRelacionado				= MovimientoEnTransito.IdTipoOperacion.contains("COM")?MovimientoEnTransito.IdPortafolio:MovimientoEnTransito.IdPortafolioRelacionado;
					p_IngresoCartera.add(ObjEntrada);
					
					boolean esCertificacionTransitoVenter = "CUS".equals(in.p_IdModulo) && "VENTER".equals(MovimientoEnTransito.IdTipoOperacion);
					
					if(("S").equalsIgnoreCase(MovimientoEnTransito.EsNominal) && MovimientoEnTransito.IdCustodiaOrigen.compareTo(0L) != 0L && !esCertificacionTransitoVenter) {												
						RetiroCustodia.ObjDatosEntrada	ObjEntradaRet = new RetiroCustodia.ObjDatosEntrada();
						ObjEntradaRet = copyPojoRetiroCustodia(ObjEntrada);
						
						ObjEntradaRet.TablaAfectada 						= "CARTERA";
						ObjEntradaRet.IdTipoComprobanteBusqueda				= MovimientoEnTransito.IdTipoComprobante;
						ObjEntradaRet.FolioComprobanteBusqueda				= MovimientoEnTransito.FolioComprobante;
						ObjEntradaRet.FolioCustodia							= MovimientoEnTransito.FolioCustodia == null || MovimientoEnTransito.FolioCustodia == 0 ? null : MovimientoEnTransito.FolioCustodia;
						ObjEntradaRet.IdCustodia 							= ObjEntradaRet.FolioCustodia == null ? MovimientoEnTransito.IdCustodiaOrigen : 0L;
						ObjEntradaRet.EsNominal                        	    = "N";
						ObjEntradaRet.AccionOperacion						= "RETIRO";
						ObjEntradaRet.IdCliente								= MovimientoEnTransito.IdTipoOperacion.contains("COM")?MovimientoEnTransito.IdCliente:MovimientoEnTransito.IdClienteRelacionado;
						ObjEntradaRet.IdPortafolio							= MovimientoEnTransito.IdTipoOperacion.contains("COM")?MovimientoEnTransito.IdPortafolio:MovimientoEnTransito.IdPortafolioRelacionado;
						p_RetiroCartera.add(ObjEntradaRet);
					}
				}
				else if (!MovimientoEnTransito.IdTipoInstrumento.equalsIgnoreCase("PAC") && MovimientoEnTransito.IdMovimientoCarteraEnTransitoRelacionado.compareTo(0L) == 0 && MovimientoEnTransito.EsNominal.equalsIgnoreCase("S") && (!in.p_IdModulo.equalsIgnoreCase("CUS") || "S".equalsIgnoreCase(PRM_DEBE_CERTIFICAR_NOMINALES))) {
					ObjEntrada.TablaAfectada 						= "TRANSITO";
					ObjEntrada.EsRegistroNuevo						= "S";						
					ObjEntrada.AccionOperacion						= "TRANSITO";	
					ObjEntrada.EsNominal                            = "S";
					ObjEntrada.IdCliente							= MovimientoEnTransito.IdTipoOperacion.contains("COM")?MovimientoEnTransito.IdClienteRelacionado:MovimientoEnTransito.IdCliente;
					ObjEntrada.IdPortafolio							= MovimientoEnTransito.IdTipoOperacion.contains("COM")?MovimientoEnTransito.IdPortafolioRelacionado:MovimientoEnTransito.IdPortafolio; 
					ObjEntrada.IdClienteRelacionado					= MovimientoEnTransito.IdTipoOperacion.contains("COM")?MovimientoEnTransito.IdCliente:MovimientoEnTransito.IdClienteRelacionado;
					ObjEntrada.IdPortafolioRelacionado				= MovimientoEnTransito.IdTipoOperacion.contains("COM")?MovimientoEnTransito.IdPortafolio:MovimientoEnTransito.IdPortafolioRelacionado;
					p_IngresoCartera.add(ObjEntrada);
				}				
				else if (!MovimientoEnTransito.IdTipoInstrumento.equalsIgnoreCase("PAC") && MovimientoEnTransito.IdMovimientoCarteraEnTransitoRelacionado.compareTo(0L) == 0 && MovimientoEnTransito.EsNominal.equalsIgnoreCase("N")){
					
					if(MovimientoEnTransito.IdTipoOperacion.contains("VEN")&& in.p_AfectaCustodia.equals("N")) {
						ObjEntrada.TablaAfectada					= "TRANSITO";
						ObjEntrada.AccionOperacion					= "INGRESO";
						ObjEntrada.EsNominal                        = "S";
					}else {
						ObjEntrada.TablaAfectada 					=  "CARTERA";									
						ObjEntrada.AccionOperacion					=  "INGRESO";
					}
					
					ObjEntrada.MueveCarteraEnTransito				=  in.p_AfectaCustodia.equals("N")?"N":"S";					
					ObjEntrada.EsRegistroNuevo						=  "S";							
					ObjEntrada.IdCliente							= MovimientoEnTransito.IdTipoOperacion.contains("COM")?MovimientoEnTransito.IdClienteRelacionado:MovimientoEnTransito.IdCliente;
					ObjEntrada.IdPortafolio							= MovimientoEnTransito.IdTipoOperacion.contains("COM")?MovimientoEnTransito.IdPortafolioRelacionado:MovimientoEnTransito.IdPortafolio; 
					ObjEntrada.IdClienteRelacionado					= MovimientoEnTransito.IdTipoOperacion.contains("COM")?MovimientoEnTransito.IdCliente:MovimientoEnTransito.IdClienteRelacionado;
					ObjEntrada.IdPortafolioRelacionado				= MovimientoEnTransito.IdTipoOperacion.contains("COM")?MovimientoEnTransito.IdPortafolio:MovimientoEnTransito.IdPortafolioRelacionado;
					p_IngresoCartera.add(ObjEntrada);
					
					if (MovimientoEnTransito.EsNominal.equalsIgnoreCase("N") && ("MRF".equals(in.p_IdModulo) && MovimientoEnTransito.FolioCustodia!=0L )){ //FolioCustodia =0 || "MRF".equals(MovimientoEnTransito.IdModulo 
						RetiroCustodia.ObjDatosEntrada	ObjEntradaRet = new RetiroCustodia.ObjDatosEntrada();
						ObjEntradaRet = copyPojoRetiroCustodia(ObjEntrada);
						
						ObjEntradaRet.TablaAfectada 						= "CARTERA";
						ObjEntradaRet.IdTipoComprobanteBusqueda				= MovimientoEnTransito.IdTipoComprobante;
						ObjEntradaRet.FolioComprobanteBusqueda				= MovimientoEnTransito.FolioComprobante;
						ObjEntradaRet.FolioCustodia							= MovimientoEnTransito.FolioCustodia == null || MovimientoEnTransito.FolioCustodia == 0 ? null : MovimientoEnTransito.FolioCustodia;
						ObjEntradaRet.IdCustodia 							= ObjEntradaRet.FolioCustodia == null ? MovimientoEnTransito.IdCustodiaOrigen : 0L;
						ObjEntradaRet.EsNominal                        	    = "N";
						ObjEntradaRet.AccionOperacion						= "RETIRO";
						ObjEntradaRet.IdCliente								= MovimientoEnTransito.IdTipoOperacion.contains("COM")?MovimientoEnTransito.IdCliente:MovimientoEnTransito.IdClienteRelacionado;
						ObjEntradaRet.IdPortafolio							= MovimientoEnTransito.IdTipoOperacion.contains("COM")?MovimientoEnTransito.IdPortafolio:MovimientoEnTransito.IdPortafolioRelacionado;
						p_RetiroCartera.add(ObjEntradaRet);
					}
				}
				else if (MovimientoEnTransito.IdMovimientoCarteraEnTransitoRelacionado.compareTo(0L) > 0){//subyacente del pacto
					
					ObjEntrada.TablaAfectada 						= "CARTERA";
					ObjEntrada.EsRegistroNuevo						= "S";						
					ObjEntrada.AccionOperacion						= "INGRESO";		
					ObjEntrada.IdCliente							= MovimientoEnTransito.IdTipoOperacion.contains("COM")?MovimientoEnTransito.IdClienteRelacionado:MovimientoEnTransito.IdCliente;
					ObjEntrada.IdPortafolio							= MovimientoEnTransito.IdTipoOperacion.contains("COM")?MovimientoEnTransito.IdPortafolioRelacionado:MovimientoEnTransito.IdPortafolio; 
					ObjEntrada.IdClienteRelacionado					= MovimientoEnTransito.IdTipoOperacion.contains("COM")?MovimientoEnTransito.IdCliente:MovimientoEnTransito.IdClienteRelacionado;
					ObjEntrada.IdPortafolioRelacionado				= MovimientoEnTransito.IdTipoOperacion.contains("COM")?MovimientoEnTransito.IdPortafolio:MovimientoEnTransito.IdPortafolioRelacionado;
					p_IngresoCartera.add(ObjEntrada);
					
					IngresoCustodia.ObjDatosEntrada	ObjEntradaSub = new IngresoCustodia.ObjDatosEntrada();

					ObjEntradaSub.TablaAfectada 						= "SUBYACENTE";
					ObjEntradaSub.EsRegistroNuevo						= "S";						
					ObjEntradaSub.AccionOperacion						= "INGRESO";
					ObjEntradaSub.IdCarteraOrigen = MovimientoEnTransito.IdCustodiaOrigen; //IdCarteraRetirada subyacente
					
					ObjEntradaSub.IdCliente								= MovimientoEnTransito.IdCliente;//MovimientoEnTransito.IdTipoOperacion.contains("COM")?MovimientoEnTransito.IdClienteRelacionado:MovimientoEnTransito.IdCliente;
					ObjEntradaSub.IdPortafolio							= MovimientoEnTransito.IdPortafolio;//MovimientoEnTransito.IdTipoOperacion.contains("COM")?MovimientoEnTransito.IdPortafolioRelacionado:MovimientoEnTransito.IdPortafolio;
					ObjEntradaSub.IdClienteRelacionado					= MovimientoEnTransito.IdClienteRelacionado;//MovimientoEnTransito.IdTipoOperacion.contains("COM")?MovimientoEnTransito.IdCliente:MovimientoEnTransito.IdClienteRelacionado;
					ObjEntradaSub.IdPortafolioRelacionado				= MovimientoEnTransito.IdPortafolioRelacionado;//MovimientoEnTransito.IdTipoOperacion.contains("COM")?MovimientoEnTransito.IdPortafolio:MovimientoEnTransito.IdPortafolioRelacionado;

					ObjEntradaSub.IdGenerarMovimientoCustodia = ObjEntrada.IdGenerarMovimientoCustodia;
					ObjEntradaSub.IdMovimientoCarteraEnTransitoOrigen = ObjEntrada.IdMovimientoCarteraEnTransitoOrigen;
					ObjEntradaSub.IdMovimientoCarteraEnTransitoRelacionado = ObjEntrada.IdMovimientoCarteraEnTransitoRelacionado;
					ObjEntradaSub.IdCuentaCustodioPortafolio = ObjEntrada.IdCuentaCustodioPortafolio;
					ObjEntradaSub.FechaOperacion = ObjEntrada.FechaOperacion;
					ObjEntradaSub.FechaVencimientoOperacion = ObjEntrada.FechaVencimientoOperacion;
					ObjEntradaSub.PlazoEnDiasOperacion = ObjEntrada.PlazoEnDiasOperacion;
					ObjEntradaSub.IdTipoOperacion = ObjEntrada.IdTipoOperacion;
					ObjEntradaSub.IdTipoProducto = ObjEntrada.IdTipoProducto;
					ObjEntradaSub.IdConcepto = ObjEntrada.IdConcepto;
					ObjEntradaSub.IdCondicionLiquidacion = ObjEntrada.IdCondicionLiquidacion; 
					ObjEntradaSub.FechaLiquidacion = ObjEntrada.FechaLiquidacion;
					ObjEntradaSub.IdTipoCanalIngreso = ObjEntrada.IdTipoCanalIngreso; 		
					ObjEntradaSub.CodigoIFRS = ObjEntrada.CodigoIFRS;		 		 
					ObjEntradaSub.IdTipoComprobante = ObjEntrada.IdTipoComprobante; 
					ObjEntradaSub.FolioComprobante = ObjEntrada.FolioComprobante; 
					ObjEntradaSub.IdBolsa = ObjEntrada.IdBolsa;
					ObjEntradaSub.EsIntraday = ObjEntrada.EsIntraday; 
					ObjEntradaSub.TipoCarteraCorredor = ObjEntrada.TipoCarteraCorredor;
					ObjEntradaSub.IdUsuario = ObjEntrada.IdUsuario;
					ObjEntradaSub.IdSucursal = ObjEntrada.IdSucursal; 
					ObjEntradaSub.IdAgente = ObjEntrada.IdAgente;
					ObjEntradaSub.Glosa = ObjEntrada.Glosa;
					ObjEntradaSub.IdInstrumento = ObjEntrada.IdInstrumento;
					ObjEntradaSub.Nemotecnico = ObjEntrada.Nemotecnico;
					ObjEntradaSub.IdMonedaInstrumento = ObjEntrada.IdMonedaInstrumento;
					ObjEntradaSub.IdMonedaEmision = ObjEntrada.IdMonedaEmision;
					ObjEntradaSub.IdTipoReajusteInstrumento = ObjEntrada.IdTipoReajusteInstrumento;
					ObjEntradaSub.IdEmisor = ObjEntrada.IdEmisor;
					ObjEntradaSub.EsFungible = ObjEntrada.EsFungible;
					ObjEntradaSub.IdBoveda = ObjEntrada.IdBoveda;
					ObjEntradaSub.CuentaBoveda = ObjEntrada.CuentaBoveda;
					ObjEntradaSub.EsNominal = ObjEntrada.EsNominal;
					ObjEntradaSub.IdMonedaEmision = ObjEntrada.IdMonedaEmision; 
					ObjEntradaSub.Cantidad = ObjEntrada.Cantidad; 
					ObjEntradaSub.CorteMinimo = ObjEntrada.CorteMinimo; 
					ObjEntradaSub.ValorRescate = ObjEntrada.ValorRescate; 
					ObjEntradaSub.PrecioCompra = ObjEntrada.PrecioCompra;
					if("COMPAC".equalsIgnoreCase(ObjEntrada.IdTipoOperacion) || "VENPAC".equalsIgnoreCase(ObjEntrada.IdTipoOperacion)) {
						ObjEntradaSub.TasaCompra = in.p_TasaOperacion;
					}else {
						ObjEntradaSub.TasaCompra = ObjEntrada.TasaCompra;
					}
					ObjEntradaSub.MontoCompra = ObjEntrada.MontoCompra;
					ObjEntradaSub.MontoCompraMonedaInstrumento = ObjEntrada.MontoCompraMonedaInstrumento;
					ObjEntradaSub.MontoAPlazo = ObjEntrada.MontoAPlazo; 
					ObjEntradaSub.MontoAPlazoMonedaInstrumento = ObjEntrada.MontoAPlazoMonedaInstrumento;
					ObjEntradaSub.IdOperacion = ObjEntrada.IdOperacion;
					ObjEntradaSub.FechaVencimientoInstrumento = ObjEntrada.FechaVencimientoInstrumento;
					ObjEntradaSub.IdMesa = ObjEntrada.IdMesa;
					
					
					p_IngresoCarteraSubyacentePacto.add(ObjEntradaSub);
					
					if (MovimientoEnTransito.FolioCustodia!=0){
						RetiroCustodia.ObjDatosEntrada	ObjEntradaRet = new RetiroCustodia.ObjDatosEntrada();
						
						ObjEntradaRet = copyPojoRetiroCustodia(ObjEntradaSub);
						
						ObjEntradaRet.TablaAfectada 						= "CARTERA";
						ObjEntradaRet.IdTipoComprobanteBusqueda				= MovimientoEnTransito.IdTipoComprobante;
						ObjEntradaRet.FolioComprobanteBusqueda				= MovimientoEnTransito.FolioComprobante;
						ObjEntradaRet.FolioCustodia							= MovimientoEnTransito.FolioCustodia == 0 ? null : MovimientoEnTransito.FolioCustodia;
						ObjEntradaRet.IdCustodia 							= MovimientoEnTransito.IdCustodiaOrigen;
						ObjEntradaRet.AccionOperacion						= "RETIRO";
						ObjEntradaRet.IdCliente								= MovimientoEnTransito.IdTipoOperacion.contains("COM")?MovimientoEnTransito.IdCliente:MovimientoEnTransito.IdClienteRelacionado;
						ObjEntradaRet.IdPortafolio							= MovimientoEnTransito.IdTipoOperacion.contains("COM")?MovimientoEnTransito.IdPortafolio:MovimientoEnTransito.IdPortafolioRelacionado;
						if("VENPAC".equalsIgnoreCase(ObjEntrada.IdTipoOperacion) || "VENTER".equalsIgnoreCase(ObjEntrada.IdTipoOperacion)) {
							ObjEntradaRet.TasaCompra							= ObjEntrada.TasaCompra;
						}
						p_RetiroCartera.add(ObjEntradaRet);
					}

				}
				GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar MOV = new GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar();
				MOV.IdMovimientoCarteraEnTransito = MovimientoEnTransito.IdMovimientoCarteraEnTransito;
				MOV.EsCertificacion = "S";
				MOV.IdUsuario = in.p_IdUsuario;
				HashMap<String, Object> p_TransitosHash = Utils.List2Hash(p_Transitos, "IdMovimientoCarteraEnTransito");
				GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar p_tran = (GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar) p_TransitosHash.get(MOV.IdMovimientoCarteraEnTransito);
				if (p_tran == null){
					p_Transitos.add(MOV);
				}
				
			};
			
			//03.0.a - REALIZAR RETIROS
			if ( p_RetiroCartera.size() > 0 ){
				RetiroCustodia retiroCustodia = new RetiroCustodia();
				RetiroCustodia.RetiroCustodiaEntrada paramInSPRetiro = new RetiroCustodia.RetiroCustodiaEntrada();
				paramInSPRetiro.p_IdEmpresa			= in.p_IdEmpresa;
				paramInSPRetiro.p_SimularOperacion	= "N";
				paramInSPRetiro.p_TicketAtencion	= in.p_TicketAtencion;
				paramInSPRetiro.p_ValorizacionAutomatica = "N";
				paramInSPRetiro.p_DatosEntrada		= p_RetiroCartera;		
				
	
				logger.debug("[CrearOperacionMesaCustodia] Inicio llamada a RetiroCustodia");
				startTime = System.nanoTime();
				RetiroCustodia.RetiroCustodiaSalida resultRetiros = retiroCustodia.logica(conn, paramInSPRetiro);
				logger.debug("[CrearOperacionMesaCustodia] (" + (System.nanoTime() - startTime)/1000000 + ") Fin llamada a RetiroCustodia");

				
				if (MensajeSalida.getNumeroErrores(resultRetiros.P_OUT_MENSAJES) > 0){
					out.P_OUT_MENSAJES.addAll(resultRetiros.P_OUT_MENSAJES);
					return out;
				}
				out.P_OUT_RETIROS.addAll(resultRetiros.P_OUT_DATOSSALIDA);
				for (RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida c: resultRetiros.P_OUT_DATOSSALIDA){
					mapMsg.clear();
					mapMsg.put("v_Cantidad", c.Cantidad.toString());
					mapMsg.put("v_FolioCustodia", c.FolioCustodia.toString());
					// out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_OK,"UI_RETIRO_TITULO_OK",mapMsg,conn,in.p_IdEmpresa));
					Id++;
					//validar esto.....ver si queda enganchado a termino con la cartera que la origino
					for(IngresoCustodia.ObjDatosEntrada car: p_IngresoCartera){
						if (car.IdMovimientoCarteraEnTransitoOrigen.equals(c.IdGenerarMovimientoCustodia)){
							car.FolioCarteraRelacionada = c.FolioCustodia;//car.IdCarteraOrigen = c.IdCustodia;
						}
					}
				}
				
			};
			
			//03.0.b - REALIZAR INGRESOS
			if ( p_IngresoCarteraPacto.size() > 0 ){
				IngresoCustodia ingresoCustodiaIngreso = new IngresoCustodia();
				IngresoCustodia.IngresoCustodiaEntrada paramInSPIngreso = new IngresoCustodia.IngresoCustodiaEntrada();
				paramInSPIngreso.p_IdEmpresa		= in.p_IdEmpresa;		
				paramInSPIngreso.p_IdModulo			= in.p_IdModulo;
				paramInSPIngreso.p_SimularOperacion	= "N";
				paramInSPIngreso.p_TicketAtencion	= in.p_TicketAtencion;
				paramInSPIngreso.p_ValorizacionAutomatica = "N";
				paramInSPIngreso.p_DatosEntrada		= p_IngresoCarteraPacto;				
			
			
	
				logger.debug("[CrearOperacionMesaCustodia] Inicio llamada a IngresoCustodia");
				startTime = System.nanoTime();
				IngresoCustodia.IngresoCustodiaSalida resultIngresos = ingresoCustodiaIngreso.logica(conn, paramInSPIngreso);
				logger.debug("[CrearOperacionMesaCustodia] (" + (System.nanoTime() - startTime)/1000000 + ") Fin llamada a IngresoCustodia");
			
			
				if (MensajeSalida.getNumeroErrores(resultIngresos.P_OUT_MENSAJES) > 0){
					out.P_OUT_MENSAJES.addAll(resultIngresos.P_OUT_MENSAJES);
					return out;
				}
				out.P_OUT_INGRESOS.addAll(resultIngresos.P_OUT_DATOSSALIDA);
				List<IngresarCompraVentaPactoUpdate> upd = new ArrayList<>();
				for (IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida c: resultIngresos.P_OUT_DATOSSALIDA){
					mapMsg.clear();
					mapMsg.put("Folio", c.FolioCustodia.toString());
					// out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_OK,"UI_INGRESO_TITULO_OK",mapMsg,conn,in.p_IdEmpresa));
					Id++;
					IngresarCompraVentaPactoUpdate updDat= new IngresarCompraVentaPactoUpdate();
					updDat.IdOperacion = c.IdOperacion;
					updDat.FolioCartera = c.FolioCustodia;
					//ope.FolioCartera = ((IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida)cusOutHash.get(ope.IdOperacion)).FolioCustodia;
					upd.add(updDat);
					for(IngresoCustodia.ObjDatosEntrada car: p_IngresoCartera){
						if (car.IdMovimientoCarteraEnTransitoRelacionado.equals(c.IdGenerarMovimientoCustodia)){
							car.FolioCarteraRelacionada = c.FolioCustodia;//car.IdCarteraOrigen = c.IdCustodia;
						}
					}
					for(IngresoCustodia.ObjDatosEntrada car: p_IngresoCarteraSubyacentePacto){
						if (car.IdMovimientoCarteraEnTransitoRelacionado.equals(c.IdGenerarMovimientoCustodia)){
							car.FolioCarteraRelacionada = c.FolioCustodia;//car.IdCarteraOrigen = c.IdCustodia;
						}
					}
				}
				logger.debug("MRF.Actualizar Folio Cartera");
		
				logger.debug("[CrearOperacionMesaCustodia] Inicio SQL_UPDATE_OPERACIONES");
				startTime = System.nanoTime();
				_sql.batch(conn, SQL_UPDATE_OPERACIONES, upd, IngresarCompraVentaPactoUpdate.class);
				logger.debug("[CrearOperacionMesaCustodia] (" + (System.nanoTime() - startTime)/1000000 + ") Fin SQL_UPDATE_OPERACIONES");
			};
			if ( p_IngresoCarteraSubyacentePacto.size() > 0 ){
				IngresoCustodia ingresoCustodiaIngreso = new IngresoCustodia();
				IngresoCustodia.IngresoCustodiaEntrada paramInSPIngreso = new IngresoCustodia.IngresoCustodiaEntrada();
				paramInSPIngreso.p_IdEmpresa		= in.p_IdEmpresa;		
				paramInSPIngreso.p_IdModulo			= in.p_IdModulo;
				paramInSPIngreso.p_SimularOperacion	= "N";
				paramInSPIngreso.p_TicketAtencion	= in.p_TicketAtencion;
				paramInSPIngreso.p_ValorizacionAutomatica = "N";
				paramInSPIngreso.p_DatosEntrada		= p_IngresoCarteraSubyacentePacto;
					
				logger.debug("[CrearOperacionMesaCustodia] Inicio llamada a IngresoCustodia 2");
				startTime = System.nanoTime();
				IngresoCustodia.IngresoCustodiaSalida resultIngresos = ingresoCustodiaIngreso.logica(conn, paramInSPIngreso);
				logger.debug("[CrearOperacionMesaCustodia] (" + (System.nanoTime() - startTime)/1000000 + ") Fin llamada a IngresoCustodia 2");
				
				
				if (MensajeSalida.getNumeroErrores(resultIngresos.P_OUT_MENSAJES) > 0){
					out.P_OUT_MENSAJES.addAll(resultIngresos.P_OUT_MENSAJES);
					return out;
				}
				out.P_OUT_INGRESOS.addAll(resultIngresos.P_OUT_DATOSSALIDA);
				List<ReservaUpdate> upd = new ArrayList<>();
				HashMap<String, Object> p_IngresoCarteraSubyacentePactoHash = Utils.List2Hash(p_IngresoCarteraSubyacentePacto, "IdGenerarMovimientoCustodia");
				for (IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida c: resultIngresos.P_OUT_DATOSSALIDA){
					mapMsg.clear();
					mapMsg.put("Folio",c.FolioCustodia.toString() );
					//out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_OK,"UI_INGRESO_TITULO_OK",mapMsg,conn,in.p_IdEmpresa));
					Id++;
					ReservaUpdate updDat= new ReservaUpdate();
					updDat.IdReserva = ((IngresoCustodia.ObjDatosEntrada) p_IngresoCarteraSubyacentePactoHash.get(c.IdGenerarMovimientoCustodia)).IdOperacion;//c.IdGenerarMovimientoCustodia;
					updDat.FolioSubyacente = c.FolioCustodia;
					upd.add(updDat);
				}
				
				logger.debug("MRF.Actualizar Folio Subyacente");
		
				logger.debug("[CrearOperacionMesaCustodia] Inicio SQL_UPDATE_RESERVA_SUBYACENTE");
				startTime = System.nanoTime();
				_sql.batch(conn, SQL_UPDATE_RESERVA_SUBYACENTE, upd, ReservaUpdate.class);
				logger.debug("[CrearOperacionMesaCustodia] (" + (System.nanoTime() - startTime)/1000000 + ") Fin SQL_UPDATE_RESERVA_SUBYACENTE");								

				
			};
			
			p_IngresoCarteraReal= this.filtrarNominalesIngresoCustodia(p_IngresoCartera);	
			p_IngresoCarteraDescartada = this.filtrarIngresoCustodiaDescartadas(p_IngresoCartera);
			
			p_IngresoCartera = p_IngresoCarteraReal;			
			
			if ( p_IngresoCartera.size() > 0 ){				
				
				
				IngresoCustodia ingresoCustodiaIngreso = new IngresoCustodia();
				IngresoCustodia.IngresoCustodiaEntrada paramInSPIngreso = new IngresoCustodia.IngresoCustodiaEntrada();
				paramInSPIngreso.p_IdEmpresa		= in.p_IdEmpresa;		
				paramInSPIngreso.p_IdModulo			= in.p_IdModulo;
				paramInSPIngreso.p_SimularOperacion	= "N";
				paramInSPIngreso.p_TicketAtencion	= in.p_TicketAtencion;
				paramInSPIngreso.p_ValorizacionAutomatica = "N";
				paramInSPIngreso.p_DatosEntrada		= p_IngresoCartera;				
							
				logger.debug("[CrearOperacionMesaCustodia] Inicio llamada a IngresoCustodia 3");
				startTime = System.nanoTime();
				IngresoCustodia.IngresoCustodiaSalida resultIngresos = ingresoCustodiaIngreso.logica(conn, paramInSPIngreso);
				logger.debug("[CrearOperacionMesaCustodia] (" + (System.nanoTime() - startTime)/1000000 + ") Fin llamada a IngresoCustodia 3");
					
				if (MensajeSalida.getNumeroErrores(resultIngresos.P_OUT_MENSAJES) > 0){
					out.P_OUT_MENSAJES.addAll(resultIngresos.P_OUT_MENSAJES);
					return out;
				}
				out.P_OUT_INGRESOS.addAll(resultIngresos.P_OUT_DATOSSALIDA);
				
				this.actualizarReservasIngresoCustodia(conn,resultIngresos.P_OUT_DATOSSALIDA,p_IngresoCarteraDescartada);
				
				
			}
			if (!in.p_AfectaCustodia.equalsIgnoreCase("S")){
				HashMap<String,Object> ParmsReserva = new HashMap<>();
				ParmsReserva.put("IdReserva", in.p_Reserva);
				logger.debug("[CrearOperacionMesaCustodia] Inicio SQL_SELECT_DETALLE_RESERVA");
				startTime = System.nanoTime();
				List<SelectDetalleReserva> selectDetalleReserva = _sql.query(conn, SQL_SELECT_DETALLE_RESERVA, ParmsReserva, SelectDetalleReserva.class);
				logger.debug("[CrearOperacionMesaCustodia] (" + (System.nanoTime() - startTime)/1000000 + ") Fin SQL_SELECT_DETALLE_RESERVA");
				
				List<ReservaUpdate> upd = new ArrayList<>();
				for (SelectDetalleReserva detalle: selectDetalleReserva){
					mapMsg.clear();
					mapMsg.put("Folio", detalle.FolioCarteraCustodia.toString());
					
					Id++;
					ReservaUpdate updDat = new ReservaUpdate();
					updDat.IdReserva = detalle.IdReserva;
					updDat.FolioCartera = detalle.FolioCarteraCustodia;
					updDat.IdCartera = detalle.IdCarteraCustodia;
					updDat.IdEstado = "LIQ";
					upd.add(updDat);
				}
				
				logger.debug("MRF.Actualizar Folio Cartera");
				logger.debug("[CrearOperacionMesaCustodia] Inicio SQL_UPDATE_RESERVA");
				
				startTime = System.nanoTime();
				_sql.batch(conn, SQL_UPDATE_RESERVA, upd, ReservaUpdate.class);
				
				logger.debug("[CrearOperacionMesaCustodia] (" + (System.nanoTime() - startTime)/1000000 + ") Fin SQL_UPDATE_RESERVA");
			}
			
			
			//03.0.c - ACTUALIZAR MOVIMIENTOS EN TRANSITO
			if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) == 0L){
				GestionMovimientoCarteraEnTransito.ActualizarMovimientoEnTransito(conn, p_Transitos);
				
				if(in.p_IdModulo.equalsIgnoreCase("MRF")) {
					Set<Long> idsCertificados = p_Transitos.stream().filter(t -> "S".equals(t.EsCertificacion)).map(t -> Long.valueOf(t.IdMovimientoCarteraEnTransito)).collect(Collectors.toSet());
					if(idsCertificados.size() > 0) {
						List<GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito> MovimientosCertificados = MovimientosEnTransitoTMP.stream()
								.filter(m -> idsCertificados.contains(Long.valueOf(m.IdMovimientoCarteraEnTransito)))
						        .collect(Collectors.toList());
						agregarLog(conn, in, MovimientosCertificados);
					}
				}
			}
			
			List<ClienteNeteo> clientesANetear = new ArrayList<>();
			for(GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito movimientoEnTransito : MovimientosEnTransito) {
				if("COMTER".equalsIgnoreCase(movimientoEnTransito.IdTipoOperacion) || "VENTER".equalsIgnoreCase(movimientoEnTransito.IdTipoOperacion)) {
					ClienteNeteo clienteNeteo = new ClienteNeteo();
					clienteNeteo.IdInstrumento = movimientoEnTransito.IdInstrumento;
					clienteNeteo.IdPortafolio = movimientoEnTransito.IdPortafolio;
					clientesANetear.add(clienteNeteo);
					
					ClienteNeteo clienteNeteoRelacionado = new ClienteNeteo();
					clienteNeteoRelacionado.IdInstrumento = movimientoEnTransito.IdInstrumento;
					clienteNeteoRelacionado.IdPortafolio = movimientoEnTransito.IdPortafolioRelacionado;
					clientesANetear.add(clienteNeteoRelacionado);
				}
			}
			
			if(clientesANetear.size() > 0) {
				EntradaNeteo entradaNeteo = new EntradaNeteo();
				entradaNeteo.p_IdEmpresa = in.p_IdEmpresa;
				entradaNeteo.p_IdModulo = "CUS";
				entradaNeteo.p_IdUsuario = in.p_IdUsuario;
				entradaNeteo.p_PortafoliosInstrumento = clientesANetear;
				entradaNeteo.p_SimularOperacion = "N";
				entradaNeteo.p_UsaReglasNegocio = "S";
				
				String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
				AgregarPrograma.InAgregarPrograma paramsIn = new AgregarPrograma.InAgregarPrograma(); 
				paramsIn.p_IdEmpresa		= in.p_IdEmpresa;
				paramsIn.p_IdModulo			= "CUS";
				paramsIn.p_IdUsuario		= in.p_IdUsuario;
				paramsIn.p_IdTipoPrograma	= "NeteoCarterasNegativas";
				paramsIn.p_Segundos			= 0L;
				paramsIn.p_FechaSistema		= fechaSistema;
				List<Parametro> paramsParametros = new ArrayList<>();
				Parametro parametro = new Parametro();
				parametro.NombreParametro = "Entrada";
				Gson gson = Utils.getGsonBuilder();
				parametro.Valor = "";
				parametro.ValorBlob = gson.toJson(entradaNeteo);
				paramsParametros.add(parametro);
				paramsIn.p_Parametros = paramsParametros ;
				AgregarPrograma AP = new AgregarPrograma();
				AP.logica(conn, paramsIn);
			}
		};
		

		if (in.p_EjecutarValorizador.equalsIgnoreCase("S"))
		{
			logger.debug("[CrearOperacionMesaCustodia] Inicio CacheValorizacion.valorizar 2");
			startTime = System.nanoTime();
			CacheValorizacion.valorizar(conn);
			logger.debug("[CrearOperacionMesaCustodia] (" + (System.nanoTime() - startTime)/1000000 + ") Fin CacheValorizacion.valorizar 2");
		}


		logger.info("Termino del servicio CrearOperacionMesaCustodia, tiempo: " + (System.currentTimeMillis() - init) + " ms - " + (System.currentTimeMillis() - init)/1000 + " seg");
		
		return out;
	};
	
	

	
	private void actualizarReservasIngresoCustodia(Connection conn, List<ObjDatosSalida> resultIngresos, List<ObjDatosEntrada> p_IngresoCarteraDescartada) throws SQLException {
		logger.debug("INICIO::CrearOperacionMesaCustodia.filtrarIngresoCustodiaDescartadas");
		long inicioMetodo = System.currentTimeMillis();		
		
		List<ReservaUpdate> listaActualizar = new ArrayList<>();
		SQL sql = new SQL();
		
		if(!resultIngresos.isEmpty()) {
			for (IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida c: resultIngresos){				
				ReservaUpdate updDat= new ReservaUpdate();
				updDat.IdReserva    = c.IdOperacion;
				updDat.FolioCartera = c.FolioCustodia==null?0L:c.FolioCustodia;
				updDat.IdCartera    = c.IdCustodia;
				updDat.IdEstado     = "LIQ";
				listaActualizar.add(updDat);
			}
		}		
		
		if(!p_IngresoCarteraDescartada.isEmpty()) {
			for (IngresoCustodia.ObjDatosEntrada c: p_IngresoCarteraDescartada){				
				ReservaUpdate updDat= new ReservaUpdate();
				updDat.IdReserva    = c.IdOperacion;
				updDat.FolioCartera = 0L;
				updDat.IdCartera    = 0L;
				updDat.IdEstado     = "LIQ";
				listaActualizar.add(updDat);
			}
		}
		
		sql.batch(conn, SQL_UPDATE_RESERVA, listaActualizar, ReservaUpdate.class);
		
		long finMetodo = System.currentTimeMillis();
		logger.debug("FIN::CrearOperacionMesaCustodia.filtrarIngresoCustodiaDescartadas :" , finMetodo-inicioMetodo);
		
	}

	private List<ObjDatosEntrada> filtrarIngresoCustodiaDescartadas(List<ObjDatosEntrada> p_IngresoCartera) {
		logger.trace("INICIO::CrearOperacionMesaCustodia.filtrarIngresoCustodiaDescartadas");
		long inicioMetodo = System.currentTimeMillis();		
		
		 List<ObjDatosEntrada> ingresoCarteraDescartada = p_IngresoCartera.stream().filter(car->      ("VENTER".equals(car.IdTipoOperacion) && "S".equals(car.EsNominal))
																									||("VENPAC".equals(car.IdTipoOperacion) && "S".equals(car.EsNominal))
																									 		 																  
				 ).collect(Collectors.toList());
		 
		long finMetodo = System.currentTimeMillis();
		logger.trace("FIN::CrearOperacionMesaCustodia.filtrarIngresoCustodiaDescartadas :" , finMetodo-inicioMetodo);
		return ingresoCarteraDescartada;
	}
	
	private List<ObjDatosEntrada> filtrarNominalesIngresoCustodia(List<ObjDatosEntrada> p_IngresoCartera) {
		logger.trace("INICIO::CrearOperacionMesaCustodia.filtrarMovimientosIngresoCustodia");
		long inicioMetodo = System.currentTimeMillis();
		
		 List<ObjDatosEntrada> ingresoCartera = p_IngresoCartera.stream().filter(car->    ("VENTER".equals(car.IdTipoOperacion) && "N".equals(car.EsNominal))
				 																		||("VENPAC".equals(car.IdTipoOperacion) && "N".equals(car.EsNominal))
				 																		||"COMTER".equals(car.IdTipoOperacion)
				 																		||"COMPAC".equals(car.IdTipoOperacion) 
				 ).collect(Collectors.toList());
		 
		 
		
		long finMetodo = System.currentTimeMillis();
		logger.trace("FIN::CrearOperacionMesaCustodia.filtrarMovimientosIngresoCustodia :" , finMetodo-inicioMetodo);
		return ingresoCartera;
	}




	private RetiroCustodia.ObjDatosEntrada  copyPojoRetiroCustodia(IngresoCustodia.ObjDatosEntrada entrada) {
		RetiroCustodia.ObjDatosEntrada  salida = new RetiroCustodia.ObjDatosEntrada ();
		
		salida.IdGenerarMovimientoCustodia = entrada.IdGenerarMovimientoCustodia;
		salida.TablaAfectada = entrada.TablaAfectada;
		salida.IdCustodia = entrada.IdCustodia;
		salida.FolioCustodia = entrada.FolioCustodia;
		salida.IdCarteraOrigen = entrada.IdCarteraOrigen;
		salida.IdMovimientoCarteraEnTransitoOrigen = entrada.IdMovimientoCarteraEnTransitoOrigen;
		salida.AccionOperacion = entrada.AccionOperacion;
		salida.IdCliente = entrada.IdCliente; 
		salida.IdPortafolio = entrada.IdPortafolio; 
		salida.FechaOperacion = entrada.FechaOperacion;
		salida.IdTipoOperacion = entrada.IdTipoOperacion;
		salida.IdTipoProducto = entrada.IdTipoProducto;
		salida.PlazoEnDiasOperacion = entrada.PlazoEnDiasOperacion;
		salida.IdOperacion = entrada.IdOperacion;
		salida.IdConcepto = entrada.IdConcepto;
		salida.IdCondicionLiquidacion = entrada.IdCondicionLiquidacion; 
		salida.FechaLiquidacion = entrada.FechaLiquidacion;		
		salida.IdTipoCanalIngreso = entrada.IdTipoCanalIngreso; 	 
		salida.IdTipoComprobante = entrada.IdTipoComprobante; 
		salida.FolioComprobante = entrada.FolioComprobante; 
		salida.FechaComprobante = entrada.FechaComprobante;
		salida.FolioExternoReferencia = entrada.FolioExternoReferencia;
		salida.IdSucursal = entrada.IdSucursal;
		salida.IdAgente = entrada.IdAgente;
		salida.IdUsuario = entrada.IdUsuario;
		salida.Glosa = entrada.Glosa;
		salida.IdMonedaAdicional = entrada.IdMonedaAdicional;
		salida.IdOrigenGarantia = entrada.IdOrigenGarantia;
		salida.IdInstrumento = entrada.IdInstrumento;
		salida.IdMonedaInstrumento = entrada.IdMonedaInstrumento;
		salida.IdMonedaEmision = entrada.IdMonedaEmision;
		salida.IdTipoReajusteInstrumento = entrada.IdTipoReajusteInstrumento;
		salida.IdEmisor = entrada.IdEmisor;
		salida.EsFungible = entrada.EsFungible;
		salida.IdBoveda = entrada.IdBoveda;
		salida.CuentaBoveda = entrada.CuentaBoveda;
		salida.EsNominal = entrada.EsNominal;
		salida.FechaVencimientoInstrumento = entrada.FechaVencimientoInstrumento;
		salida.NemoSVS = entrada.NemoSVS;
		salida.Cantidad = entrada.Cantidad; 
		salida.CorteMinimo = entrada.CorteMinimo; 
		salida.ValorRescate = entrada.ValorRescate;
		salida.PrecioSucio = entrada.PrecioSucio;
		salida.PrecioCompra = entrada.PrecioCompra; 
		salida.TasaCompra = entrada.TasaCompra; 
		salida.MontoCompraMonedaInstrumento = entrada.MontoCompraMonedaInstrumento; 
		salida.MontoCompra = entrada.MontoCompra;
		salida.TasaAgente = entrada.TasaAgente; 
		salida.Nemotecnico = entrada.Nemotecnico;
		salida.PrecioAPlazo = entrada.PrecioAPlazo;
		salida.MontoAPlazo = entrada.MontoAPlazo; 
		salida.MontoAPlazoMonedaInstrumento = entrada.MontoAPlazoMonedaInstrumento;
		salida.ValorFactorAjusteaFechaCurse = entrada.ValorFactorAjusteaFechaCurse;
		salida.IdRegimenFondoMutuo = entrada.IdRegimenFondoMutuo;
		salida.IdSpotAsociado = entrada.IdSpotAsociado; 
		salida.IdModalidadCumplimiento = entrada.IdModalidadCumplimiento; 
		salida.MontoMonedaPrincipal = entrada.MontoMonedaPrincipal; 
		salida.MontoMonedaSecundaria = entrada.MontoMonedaSecundaria; 
		salida.IdMonedaLiquidacion = entrada.IdMonedaLiquidacion; 
		salida.MontoLiquidacion = entrada.MontoLiquidacion; 
		salida.MontoLiquidacionMonedaPrincipal = entrada.MontoLiquidacionMonedaPrincipal; 
		salida.MontoDiferido = entrada.MontoDiferido; 
		salida.MontoUtilidadDiferida = entrada.MontoUtilidadDiferida; 
		salida.MontoPerdidaDiferida = entrada.MontoPerdidaDiferida; 
		salida.MontoMargen = entrada.MontoMargen; 
		salida.MontoValorRazonableActivo = entrada.MontoValorRazonableActivo; 
		salida.MontoValorRazonablePasivo = entrada.MontoValorRazonablePasivo; 
		salida.MontoValorRazonableNeto = entrada.MontoValorRazonableNeto; 
		salida.TasaValorRazonableActivo = entrada.TasaValorRazonableActivo; 
		salida.TasaValorRazonablePasivo = entrada.TasaValorRazonablePasivo; 
		salida.MontoDevengoTipoCambio = entrada.MontoDevengoTipoCambio; 
		salida.MontoDevengoTipoCambioAnterior = entrada.MontoDevengoTipoCambioAnterior; 
		salida.MontoDiferidoDiario = entrada.MontoDiferidoDiario; 
		salida.MontoDiferidoAcumulado = entrada.MontoDiferidoAcumulado; 
		salida.ValorTipoCambioSpot = entrada.ValorTipoCambioSpot; 
		salida.ValorTipoCambioCotizador = entrada.ValorTipoCambioCotizador; 
		salida.EsPuntos = entrada.EsPuntos; 
		salida.TasaPuntosMonedaPrincipal = entrada.TasaPuntosMonedaPrincipal; 
		salida.TasaPuntosMonedaSecundaria = entrada.TasaPuntosMonedaSecundaria; 
		salida.MontoMatriz = entrada.MontoMatriz; 
		salida.TasaDescuentoAnticipo = entrada.TasaDescuentoAnticipo; 
		salida.TipoCambioAnticipo = entrada.TipoCambioAnticipo; 
		salida.MontoARecibirAnticipo = entrada.MontoARecibirAnticipo; 
		salida.TipoCambioObservado = entrada.TipoCambioObservado;		
		salida.FechaEfectiva = entrada.FechaEfectiva;
		salida.IdPlaza = entrada.IdPlaza;
		salida.IdFixing = entrada.IdFixing;
		salida.FechaFixing = entrada.FechaFixing;
		salida.HoraFixing = entrada.HoraFixing;
		salida.IdReferenciaMercado = entrada.IdReferenciaMercado;
		salida.IdMonedaPrincipal = entrada.IdMonedaPrincipal;
		salida.IdMonedaSecundaria = entrada.IdMonedaSecundaria;
		salida.ValorTipoCambioCierre = entrada.ValorTipoCambioCierre;
		salida.ValorParidadCierre = entrada.ValorParidadCierre;
		salida.ValorTipoCambioCosto = entrada.ValorTipoCambioCosto;
		salida.ValorParidadCosto = entrada.ValorParidadCosto;
		salida.IdCarga = entrada.IdCarga; 
		salida.IdSecuenciaCarteraMigracion = entrada.IdSecuenciaCarteraMigracion; 
		salida.CodigoPeriodo = entrada.CodigoPeriodo;
		salida.EsVentaDescubierta = entrada.EsVentaDescubierta;		
	
		return salida;
	}	
	
	private void agregarLog(Connection con, CrearOperacionMesaCustodia_IN in, List<GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito> MovimientosEnTransitoTMP) {
		try {
			logger.debug("INICIO::CertificarMovimientosEnTransito.agregarLog");
			long inicio = System.currentTimeMillis();
			
			String FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
			List<AgregarLog.InAgregarLog.Log> listaLog = new ArrayList<>();
			HashMap<String, Object> hDatosMovimiento = Utils.List2Hash(MovimientosEnTransitoTMP, "IdMovimientoCarteraEnTransito");
			
			for (GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito movimiento : MovimientosEnTransitoTMP) {
				AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
				GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito datosMovimiento = (GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito) hDatosMovimiento.get(movimiento.IdMovimientoCarteraEnTransito);
				
				log.IdTrabajo = 1L; 
				log.IdCabeceraLog = datosMovimiento.IdMovimientoCarteraEnTransito; //
				log.IdTipoLog = "CERTIFICACION";
				log.IdModulo = in.p_IdModulo;
				log.IdUsuario = in.p_IdUsuario;
				log.IdEmpresa = in.p_IdEmpresa;
				log.FechaSistemaLog = FechaSistema;
				log.IdUsuarioAtendido = in.p_IdUsuario;
				log.IdCliente = datosMovimiento.IdCliente; //
				log.EstadoCliente = "";
				log.NombreCliente = "";
				log.IdTipoDocumentoIdentidad = "";
				log.IdDocumentoIdentidad = "";
				log.IdPortafolio = datosMovimiento.IdPortafolio;
				log.EstadoPortafolio = "";
				log.IdTipoRegistro = "CERTIFICARMOVIMIENTOENTRANSITO";
				log.IdRegistro = datosMovimiento.IdMovimientoCarteraEnTransito.toString();
				log.EstadoRegistro = "CRT";
				log.IdTipoOperacion = datosMovimiento.IdTipoOperacion;
				log.Folio = 0L;
				log.Cantidad = datosMovimiento.Cantidad;
				log.PrecioTasa = BigDecimal.ZERO;
				log.Monto = BigDecimal.ZERO;
				log.IdMonedaOrigen = "";
				log.IdMonedaLiquidacion = "";
				log.TipoCambio = "";
				log.Nemo = datosMovimiento.Nemotecnico; //
				log.IdProceso = 0L;
				log.EstadoProceso = "";
				log.NombreProceso = "cl.bcs.custodia.CrearOperacionMesaCustodia.GestionMovimientoCarteraEnTransito";
						
				listaLog.add(log);
			}
			
			//REGISTRO
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
			entradaAgregarLog.p_IdTrabajo = 1L;		
			entradaAgregarLog.p_Log = listaLog;	
			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(con, entradaAgregarLog);
				
			long fin = System.currentTimeMillis();
			logger.debug("FIN::CertificarMovimientosEnTransito.agregarLog:" , fin - inicio);
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}

	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = Utils.getGsonBuilder();
		CrearOperacionMesaCustodia_IN jin = gson.fromJson(in.toString(), CrearOperacionMesaCustodia_IN.class);
		CrearOperacionMesaCustodia_OUT out = logica(conn, jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public static void main(String[] args)  throws Exception,SQLException {
		// TODO Auto-generated method stub
		Connection conn = null;
		Gson gson = new Gson();
		
		//try {
			System.out.println("== BEGIN ==");
			conn = PlataformaUtils.getDESAConnection();
			//datos entrada
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":33,\"p_IdModulo\":\"CUS\",\"P_MOVIMIENTOSCERTIFICAR\":[{\"IdMovimientoCarteraEnTransito\":20327,\"IdTipoOperacion\":\"COMTER\"}],\"p_FechaOperacion\":\"2018-08-03\"}";
			
			CrearOperacionMesaCustodia_IN objIn = gson.fromJson(json,CrearOperacionMesaCustodia_IN.class);
			//mientras pruebo hare el rollback
			CrearOperacionMesaCustodia demo = new CrearOperacionMesaCustodia();
			
			CrearOperacionMesaCustodia_OUT ObjOut=demo.logica(conn, objIn);
				
			System.out.println(ObjOut.toString());
			/*if (MensajeSalida.getNumeroErrores(ObjOut.P_OUT_MENSAJES) > 0)
				conn.rollback();
			else conn.commit();*/
			conn.commit();
		/*}catch (Exception e){
			e.printStackTrace();
		}finally {
			conn.close();
		}*/
			System.out.println("== END ==");

	};
}
