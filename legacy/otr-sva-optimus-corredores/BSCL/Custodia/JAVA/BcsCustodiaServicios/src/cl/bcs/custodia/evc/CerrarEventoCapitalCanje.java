package cl.bcs.custodia.evc;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.AnularCarteraNegativa;
import cl.bcs.custodia.utils.GestionMovimientoCarteraEnTransito;
import cl.bcs.custodia.utils.IngresoCustodia;
import cl.bcs.custodia.utils.IngresoCustodia.ObjDatosEntrada;
import cl.bcs.custodia.utils.RetiroCustodia;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CerrarEventoCapitalCanje implements JavaCall{
	
	private final Log log = new Log("CUS.CerrarEventoCapitalCanje");
	private final String SQL_SELECT_EVENTO_CAPITAL = loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalCanje_SelectEventoCapital.sql");
	private final String SQL_SELECT_EVENTO_CAPITAL_CLIENTE_RETIRO = loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalCanje_SelectEventoCapitalClienteRetiro.sql");
	private final String SQL_SELECT_EVENTO_CAPITAL_CLIENTE_INGRESO = loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalCanje_SelectEventoCapitalClienteIngreso.sql");
	private final String SQL_UPDATE_EVENTO_CAPITAL = loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalCanje_UpdateEventoCapital.sql");
	private final String SQL_UPDATE_EVENTO_CAPITAL_CLIENTE = loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalCanje_UpdateEventoCapitalCliente.sql");
	
	public static class CerrarEventoCapitalCanje_Entrada{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public Long p_IdEventoCapital;
		public Long p_IdInstrumento;
		public Long p_IdPais;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CerrarEventoCapitalCanje_Salida{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida>	P_OUT_MENSAJES;
		
		public CerrarEventoCapitalCanje_Salida(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EventoCapital{
		public java.sql.Date FechaLimite;
		public java.sql.Date FechaPago;
		public Long IdEventoCapital;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EventoCapitalCliente{
		public Long IdGenerarMovimientoCustodia;
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdBolsa;
		public java.sql.Date FechaPago;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public Long IdInstrumento;
		public String Nemotecnico;
		public String IdEmisor;
		public String IdTipoReajuste;
		public java.sql.Date FechaEmision;
		public String IdMoneda;
		public String IdBoveda;
		public BigDecimal Cantidad;
		public BigDecimal PrecioCompra;
		public String Glosa;
		public Long FolioCustodiaBusqueda;
		public Long IdCartera;
		public String TablaAfectada;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	//TODO: Lógica
	public CerrarEventoCapitalCanje_Salida logica(Connection con, CerrarEventoCapitalCanje_Entrada entrada) throws Exception {
		log.write("INICIO");
		CerrarEventoCapitalCanje_Salida out = new CerrarEventoCapitalCanje_Salida();
		SQL sql=new SQL();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEventoCapital", entrada.p_IdEventoCapital);
		List<EventoCapital> eventoCapitales = sql.query(con, SQL_SELECT_EVENTO_CAPITAL, params, EventoCapital.class);
		
		AnularCarteraNegativa anulaCartera = new AnularCarteraNegativa();
		AnularCarteraNegativa.AnularCarteraNegativa_Entrada entNeg = new AnularCarteraNegativa.AnularCarteraNegativa_Entrada();
		entNeg.p_IdEmpresa = entrada.p_IdEmpresa;
		entNeg.p_IdModulo = entrada.p_IdModulo;
		entNeg.p_IdUsuario = entrada.p_IdUsuario;
		if(eventoCapitales.size() > 0){
			List<Long> IdMovimientosEnTransito = new ArrayList<Long>();
			params.clear();
			params.put("IdEventoCapital", entrada.p_IdEventoCapital);
			params.put("IdEmpresa", entrada.p_IdEmpresa);
			params.put("FechaLimite", eventoCapitales.get(0).FechaLimite);
			params.put("FechaPago", eventoCapitales.get(0).FechaPago);
			List<EventoCapitalCliente> eventoCapitalClienteRetiro = sql.query(con, SQL_SELECT_EVENTO_CAPITAL_CLIENTE_RETIRO, params, EventoCapitalCliente.class);
			params.clear();
			params.put("IdEventoCapital", entrada.p_IdEventoCapital);
			List<EventoCapitalCliente> eventoCapitalClienteIngreso = sql.query(con, SQL_SELECT_EVENTO_CAPITAL_CLIENTE_INGRESO, params, EventoCapitalCliente.class);
			List<ObjDatosEntrada> MovimientosTransito  = new ArrayList<ObjDatosEntrada>();
			for(EventoCapitalCliente evc : eventoCapitalClienteRetiro){
				ObjDatosEntrada arg0 = new ObjDatosEntrada();
				arg0.IdGenerarMovimientoCustodia		= evc.IdGenerarMovimientoCustodia;
				arg0.TablaAfectada						= "TRANSITO";
				arg0.AccionOperacion					= "RETIRO";
				arg0.EsRegistroNuevo					= "S";
				arg0.FolioCarteraOrigen					= evc.FolioCustodiaBusqueda;	//Para caso de retiro desde cartera					
				arg0.IdCliente							= evc.IdCliente; 
				arg0.IdPortafolio						= evc.IdPortafolio;
				arg0.IdCuentaCustodioPortafolio			= 0L;
				arg0.IdClienteRelacionado				= null;
				arg0.IdPortafolioRelacionado			= null;
				arg0.FechaOperacion						= Fecha.toDate(fechaSistema);
				arg0.FechaVencimientoOperacion			= Fecha.toDate("0001-01-01");
				arg0.PlazoEnDiasOperacion				= 0L;
				arg0.IdTipoOperacion					= "RETTIT";
				arg0.IdConcepto							= "CIECCA";
				arg0.IdCondicionLiquidacion				= "T0"; 
				arg0.FechaLiquidacion					= evc.FechaPago;
				arg0.IdTipoCanalIngreso					= "SIS";
				arg0.FolioExternoReferencia				= "";
				arg0.IdBolsa							= evc.IdBolsa;	
				arg0.IdUsuario							= entrada.p_IdUsuario;
				arg0.IdSucursal							= 0L; 
				arg0.IdAgente							= 0L; 
				arg0.Glosa								= evc.Glosa;
				arg0.IdInstrumento						= evc.IdInstrumento;
				arg0.IdMonedaInstrumento				= evc.IdMoneda;
				arg0.IdTipoReajusteInstrumento			= evc.IdTipoReajuste;
				arg0.IdEmisor							= evc.IdEmisor;
				arg0.EsFungible							= "N";
				arg0.IdBoveda							= evc.IdBoveda;
				arg0.CuentaBoveda						= "";
				arg0.EsNominal							= "N";			
				arg0.Cantidad							= evc.Cantidad; 
				arg0.CorteMinimo						= null; 
				arg0.ValorRescate						= null; 
				arg0.PrecioCompra						= evc.PrecioCompra; 
				arg0.TasaCompra							= BigDecimal.ZERO;
				arg0.MontoCompra						= evc.Cantidad.multiply(evc.PrecioCompra,MathContext.DECIMAL128); 
				arg0.MontoCompraMonedaInstrumento		= evc.Cantidad.multiply(evc.PrecioCompra,MathContext.DECIMAL128);
				if(!evc.TablaAfectada.equals("Negativa")) {
					MovimientosTransito.add(arg0);
				}else {
					AnularCarteraNegativa.CarteraNegativa_Entrada cn = new AnularCarteraNegativa.CarteraNegativa_Entrada();
					cn.IdCarteraNegativa = evc.IdCartera;
					entNeg.p_CarterasNegativas.add(cn);
				}
				
			}
			
			for(EventoCapitalCliente evc : eventoCapitalClienteIngreso){
				ObjDatosEntrada arg0 = new ObjDatosEntrada();
				arg0.IdGenerarMovimientoCustodia		= -1*evc.IdGenerarMovimientoCustodia;
				arg0.TablaAfectada						= "TRANSITO";
				arg0.AccionOperacion					= "INGRESO";
				arg0.EsRegistroNuevo					= "S";
				arg0.FolioCarteraOrigen					= evc.FolioCustodiaBusqueda;	//Para caso de retiro desde cartera					
				arg0.IdCliente							= evc.IdCliente; 
				arg0.IdPortafolio						= evc.IdPortafolio;
				arg0.IdCuentaCustodioPortafolio			= 0L;
				arg0.IdClienteRelacionado				= null;
				arg0.IdPortafolioRelacionado			= null;
				arg0.FechaOperacion						= Fecha.toDate(fechaSistema);
				arg0.FechaVencimientoOperacion			= Fecha.toDate("0001-01-01");
				arg0.PlazoEnDiasOperacion				= 0L;
				arg0.IdTipoOperacion					= "INGTIT";
				arg0.IdConcepto							= "CIECCA";
				arg0.IdCondicionLiquidacion				= "T0"; 
				arg0.FechaLiquidacion					= evc.FechaPago;
				arg0.IdTipoCanalIngreso					= "SIS";
				arg0.FolioExternoReferencia				= "";
				arg0.IdBolsa							= evc.IdBolsa;	
				arg0.IdUsuario							= entrada.p_IdUsuario;
				arg0.IdSucursal							= 0L; 
				arg0.IdAgente							= 0L; 
				arg0.Glosa								= evc.Glosa;
				arg0.IdInstrumento						= evc.IdInstrumento;
				arg0.IdMonedaInstrumento				= evc.IdMoneda;
				arg0.IdTipoReajusteInstrumento			= evc.IdTipoReajuste;
				arg0.IdEmisor							= evc.IdEmisor;
				arg0.EsFungible							= "N";
				arg0.IdBoveda							= evc.IdBoveda;
				arg0.CuentaBoveda						= "";
				arg0.EsNominal							= "N";			
				arg0.Cantidad							= evc.Cantidad; 
				arg0.CorteMinimo						= null; 
				arg0.ValorRescate						= null; 
				arg0.PrecioCompra						= evc.PrecioCompra; 
				arg0.TasaCompra							= BigDecimal.ZERO;
				arg0.MontoCompra						= evc.Cantidad.multiply(evc.PrecioCompra,MathContext.DECIMAL128); 
				arg0.MontoCompraMonedaInstrumento		= evc.Cantidad.multiply(evc.PrecioCompra,MathContext.DECIMAL128); 
				MovimientosTransito.add(arg0);
			}
		
			IngresoCustodia ingresoCustodia = new IngresoCustodia();
			IngresoCustodia.IngresoCustodiaEntrada paramInSP = new IngresoCustodia.IngresoCustodiaEntrada();
			paramInSP.p_IdEmpresa			= entrada.p_IdEmpresa;		
			paramInSP.p_IdModulo			= entrada.p_IdModulo;
			paramInSP.p_SimularOperacion	= "N";
			paramInSP.p_TicketAtencion 		= 0L;
			paramInSP.p_DatosEntrada		= MovimientosTransito;				
			IngresoCustodia.IngresoCustodiaSalida resultIngresoRetiroTransito = ingresoCustodia.logica(con, paramInSP);
			for (IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida reg : resultIngresoRetiroTransito.P_OUT_DATOSSALIDA) {
				for (ObjDatosEntrada mov : MovimientosTransito) {
					if (reg.IdGenerarMovimientoCustodia.equals(mov.IdGenerarMovimientoCustodia)) {
						if (reg.RegistroConProblemas.equalsIgnoreCase("S")) {
							out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_AL_PROCESAR_MOVIMIENTO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
						}
						else {
							IdMovimientosEnTransito.add(reg.IdCustodia);	
						}
						break;
					}
				}
			}
			
			if(entNeg.p_CarterasNegativas.size() > 0) {
				anulaCartera.logica(con, entNeg);
			}
			
			List<GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito> MovimientosEnTransito =  GestionMovimientoCarteraEnTransito.ObtenerMovimientoEnTransito(con, IdMovimientosEnTransito);
			List<GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar> p_Transitos = new ArrayList<GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar>();
			List<IngresoCustodia.ObjDatosEntrada> p_IngresoCartera = new ArrayList<IngresoCustodia.ObjDatosEntrada>();
			List<RetiroCustodia.ObjDatosEntrada> p_RetiroCartera = new ArrayList<RetiroCustodia.ObjDatosEntrada>();
			if ( MovimientosEnTransito.size() > 0 ){
								
				for (GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito MovimientoEnTransito : MovimientosEnTransito) {
					if (MovimientoEnTransito.IdTipoOperacion.equalsIgnoreCase("INGTIT")){
						IngresoCustodia.ObjDatosEntrada	ObjEntrada = new IngresoCustodia.ObjDatosEntrada();
						ObjEntrada.IdGenerarMovimientoCustodia			= MovimientoEnTransito.IdMovimientoCarteraEnTransito;
						ObjEntrada.TablaAfectada 						= "CARTERA";
						ObjEntrada.EsRegistroNuevo						= "S";						
						ObjEntrada.IdMovimientoCarteraEnTransitoOrigen	= MovimientoEnTransito.IdMovimientoCarteraEnTransito;
						ObjEntrada.AccionOperacion						= "INGRESO";		
						ObjEntrada.IdCliente							= MovimientoEnTransito.IdCliente;
						ObjEntrada.IdPortafolio							= MovimientoEnTransito.IdPortafolio; 
						ObjEntrada.IdCuentaCustodioPortafolio			= MovimientoEnTransito.IdCuentaCustodioPortafolio;
						ObjEntrada.FechaOperacion						= MovimientoEnTransito.FechaOperacion;
						ObjEntrada.FechaVencimientoOperacion			= MovimientoEnTransito.FechaVencimientoOperacion;
						ObjEntrada.PlazoEnDiasOperacion					= MovimientoEnTransito.PlazoEnDiasOperacion;
						ObjEntrada.IdTipoOperacion						= MovimientoEnTransito.IdTipoOperacion;		
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
						ObjEntrada.IdMonedaInstrumento					= MovimientoEnTransito.IdMonedaInstrumento;
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
						if(ObjEntrada.Cantidad.longValue() > 0) {
							p_IngresoCartera.add(ObjEntrada);
						}else {
							ObjEntrada.TablaAfectada 					= "NEGATIVA";
							ObjEntrada.Cantidad							= MovimientoEnTransito.Cantidad.multiply(BigDecimal.valueOf(-1));
							ObjEntrada.MontoCompraMonedaInstrumento		= MovimientoEnTransito.MontoCompraMonedaInstrumento.multiply(BigDecimal.valueOf(-1));
							ObjEntrada.Nemotecnico 						= MovimientoEnTransito.Nemotecnico;
							ObjEntrada.NemoSVS							= MovimientoEnTransito.NemoSVS;	
							ObjEntrada.FolioCustodia					= MovimientoEnTransito.FolioCustodia;
							ObjEntrada.FolioExternoReferencia			= MovimientoEnTransito.FolioExternoReferencia;
							ObjEntrada.IdTipoProducto					= MovimientoEnTransito.IdTipoProducto;
							ObjEntrada.IdMovimientoComprobante			= 0L;								
							p_IngresoCartera.add(ObjEntrada);
						}
					}; // END-IF INGTIT
						
					
					if (MovimientoEnTransito.IdTipoOperacion.equalsIgnoreCase("RETTIT")){
						RetiroCustodia.ObjDatosEntrada	ObjEntrada = new RetiroCustodia.ObjDatosEntrada(); 
						ObjEntrada.IdGenerarMovimientoCustodia			= MovimientoEnTransito.IdMovimientoCarteraEnTransito;
						ObjEntrada.TablaAfectada 						= "CARTERA";
						ObjEntrada.IdMovimientoCarteraEnTransitoOrigen	= MovimientoEnTransito.IdMovimientoCarteraEnTransito;
						ObjEntrada.IdTipoComprobanteBusqueda			= MovimientoEnTransito.IdTipoComprobante;
						ObjEntrada.FolioComprobanteBusqueda				=  MovimientoEnTransito.FolioComprobante;
						ObjEntrada.FolioCustodia						= MovimientoEnTransito.FolioCustodia == 0 ? null : MovimientoEnTransito.FolioCustodia;
						ObjEntrada.AccionOperacion						= "RETIRO";
						ObjEntrada.IdCliente							= MovimientoEnTransito.IdCliente;
						ObjEntrada.IdPortafolio							= MovimientoEnTransito.IdPortafolio;	
						ObjEntrada.FechaOperacion						= MovimientoEnTransito.FechaOperacion;
						ObjEntrada.IdTipoOperacion						= MovimientoEnTransito.IdTipoOperacion;
						ObjEntrada.IdConcepto							= MovimientoEnTransito.IdConcepto;
						ObjEntrada.IdCondicionLiquidacion				= MovimientoEnTransito.IdCondicionLiquidacion; 
						ObjEntrada.FechaLiquidacion						= MovimientoEnTransito.FechaLiquidacion;
						ObjEntrada.IdTipoCanalIngreso					= MovimientoEnTransito.IdTipoCanalIngreso; 		 
						ObjEntrada.IdTipoComprobante					= MovimientoEnTransito.IdTipoComprobante; 
						ObjEntrada.FolioComprobante						= MovimientoEnTransito.FolioComprobante;
						ObjEntrada.IdUsuario							= MovimientoEnTransito.IdUsuario;
						ObjEntrada.IdSucursal							= MovimientoEnTransito.IdSucursal; 
						ObjEntrada.IdAgente								= MovimientoEnTransito.IdAgente;
						ObjEntrada.Glosa								= MovimientoEnTransito.Glosa;
						ObjEntrada.IdInstrumento						= MovimientoEnTransito.IdInstrumento;
						ObjEntrada.IdBoveda								= MovimientoEnTransito.IdBoveda;
						ObjEntrada.CuentaBoveda							= MovimientoEnTransito.CuentaBoveda;
						ObjEntrada.IdEmisor								= MovimientoEnTransito.IdEmisor;
						ObjEntrada.EsFungible							= MovimientoEnTransito.EsFungible;
						ObjEntrada.IdMonedaEmision						= MovimientoEnTransito.IdMonedaEmision; 
						ObjEntrada.Cantidad								= MovimientoEnTransito.Cantidad; 
						ObjEntrada.CorteMinimo							= MovimientoEnTransito.CorteMinimo; 
						ObjEntrada.ValorRescate							= MovimientoEnTransito.ValorRescate; 
						ObjEntrada.PrecioCompra							= MovimientoEnTransito.PrecioCompra; 
						ObjEntrada.TasaCompra							= MovimientoEnTransito.TasaCompra;
						ObjEntrada.MontoCompraMonedaInstrumento			= MovimientoEnTransito.MontoCompraMonedaInstrumento;
						p_RetiroCartera.add(ObjEntrada);
					}; // END-IF RETTIT
					
										
					GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar MOV = new GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar();
					MOV.IdMovimientoCarteraEnTransito = MovimientoEnTransito.IdMovimientoCarteraEnTransito;
					MOV.EsCertificacion = "S";
					MOV.IdUsuario = entrada.p_IdUsuario;
					p_Transitos.add(MOV);
				};
				
				//03 - REALIZAR RETIROS
				if ( p_RetiroCartera.size() > 0 ){
					RetiroCustodia retiroCustodia = new RetiroCustodia();
					RetiroCustodia.RetiroCustodiaEntrada paramInSPRetiro = new RetiroCustodia.RetiroCustodiaEntrada();
					paramInSPRetiro.p_IdEmpresa			= entrada.p_IdEmpresa;
					paramInSPRetiro.p_SimularOperacion	= "N";
					paramInSPRetiro.p_TicketAtencion		= 0L;
					paramInSPRetiro.p_DatosEntrada		= p_RetiroCartera;				
					RetiroCustodia.RetiroCustodiaSalida resultRetiros = retiroCustodia.logica(con, paramInSPRetiro);	
					
					if (!resultRetiros.P_OUT_ERRORESCONTADOR.equals(0L)){
						for (RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida msg : resultRetiros.P_OUT_DATOSSALIDA){
							if (msg.RegistroConProblemas.equals("N")) continue;
							out.P_OUT_MENSAJES.add(new MensajeSalida(msg.IdGenerarMovimientoCustodia ,MensajeSalida.MENSAJE_ERROR,"_", msg.Mensaje ));
						};
						
						out.P_OUT_ERRORESCONTADOR = 1L;
						return out;
					};
					
					for (RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida result : resultRetiros.P_OUT_DATOSSALIDA){
						for (RetiroCustodia.ObjDatosEntrada ret : p_RetiroCartera){
							if (!ret.IdGenerarMovimientoCustodia.equals(result.IdGenerarMovimientoCustodia)) continue;

							HashMap<String,String> valores = new HashMap<String,String>();
							valores.put("v_Cantidad", ret.Cantidad.toString());
							valores.put("v_FolioCustodia", result.FolioCustodia.toString());
							
							if (result.RegistroConProblemas.equalsIgnoreCase("S")){
								out.P_OUT_MENSAJES.add(new MensajeSalida(result.IdGenerarMovimientoCustodia ,MensajeSalida.MENSAJE_ERROR, "UI_RETIRO_TITULO_ERROR", valores ,con,entrada.p_IdEmpresa));
								out.P_OUT_ERRORESCONTADOR = 1L;
							};
						};						
					};
				};
				
				
				//04 - REALIZAR INGRESOS
				if ( p_IngresoCartera.size() > 0 ){
					IngresoCustodia ingresoCustodiaIngreso = new IngresoCustodia();
					IngresoCustodia.IngresoCustodiaEntrada paramInSPIngreso = new IngresoCustodia.IngresoCustodiaEntrada();
					paramInSPIngreso.p_IdEmpresa			= entrada.p_IdEmpresa;		
					paramInSPIngreso.p_IdModulo			= entrada.p_IdModulo;
					paramInSPIngreso.p_SimularOperacion	= "N";
					paramInSPIngreso.p_TicketAtencion		= 0L;
					paramInSPIngreso.p_DatosEntrada		= p_IngresoCartera;				
					IngresoCustodia.IngresoCustodiaSalida resultIngresos = ingresoCustodiaIngreso.logica(con, paramInSPIngreso);				

					if (!resultIngresos.P_OUT_ERRORESCONTADOR.equals(0L)){
						for (IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida msg : resultIngresos.P_OUT_DATOSSALIDA){
							if (msg.RegistroConProblemas.equals("N")) continue;
							out.P_OUT_MENSAJES.add(new MensajeSalida(msg.IdGenerarMovimientoCustodia ,MensajeSalida.MENSAJE_ERROR, msg.Mensaje, new HashMap<>() ,con,entrada.p_IdEmpresa));
						};
						
						out.P_OUT_ERRORESCONTADOR = 1L;
						return out;
					};
					
					for (IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida result : resultIngresos.P_OUT_DATOSSALIDA){
						HashMap<String,String> valores = new HashMap<String,String>();
						valores.put("Folio", result.FolioCustodia.toString());
						
						if (result.RegistroConProblemas.equalsIgnoreCase("S")){
							out.P_OUT_MENSAJES.add(new MensajeSalida(result.IdGenerarMovimientoCustodia ,MensajeSalida.MENSAJE_ERROR, "UI_INGRESO_TITULO_ERROR", valores ,con,entrada.p_IdEmpresa));
							out.P_OUT_ERRORESCONTADOR = 1L;
						};
					};
				};
				
				
				
				//05 - ACTUALIZAR MOVIMIENTOS EN TRANSITO
				if (out.P_OUT_ERRORESCONTADOR.equals(0L)){
					GestionMovimientoCarteraEnTransito.ActualizarMovimientoEnTransito(con, p_Transitos);
					sql.batch(con, SQL_UPDATE_EVENTO_CAPITAL, eventoCapitales, EventoCapital.class);
					sql.batch(con, SQL_UPDATE_EVENTO_CAPITAL_CLIENTE, eventoCapitales, EventoCapital.class);
				};
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"UI_OPER_CERRAR_EXITO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			};
			
		}else{
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_EVENTO_VIGENTE_CERRAR",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
		}
		
		
		return out;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		CerrarEventoCapitalCanje_Entrada in=gson.fromJson(jin.toString() ,CerrarEventoCapitalCanje_Entrada.class);
		CerrarEventoCapitalCanje_Salida out= logica(conn,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":16,\"p_IdModulo\":\"CUS\",\"p_IdEventoCapital\":366304,\"p_IdInstrumento\":682,\"p_IdPais\":44}";
			CerrarEventoCapitalCanje_Entrada Objin = gson.fromJson(json, CerrarEventoCapitalCanje_Entrada.class);
			
			CerrarEventoCapitalCanje demo=new CerrarEventoCapitalCanje();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				CerrarEventoCapitalCanje_Salida ObjOut= demo.logica(con, Objin);
 				//con.commit();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
 			//con.commit();
 			
	    	}
		 	
		System.out.println("== END ==");
	}
}
