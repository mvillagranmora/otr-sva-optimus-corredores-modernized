package cl.bcs.custodia;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.utils.CacheValorizacion;
import cl.bcs.custodia.utils.GestionMovimientoCarteraEnTransito;
import cl.bcs.custodia.utils.IngresoCustodia;
import cl.bcs.custodia.utils.IngresoCustodia.IngresoCustodiaSalida;
import cl.bcs.custodia.utils.IngresoCustodia.ObjDatosEntrada;
import cl.bcs.custodia.utils.LogCustodia;
import cl.bcs.custodia.utils.RetiroCustodia;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ActualizarCustodiaOSA implements JavaCall{
	
	private final Log log = new Log("CUS.ActualizarCustodiaOSA");
	private final String SQL_SELECT_EVENTO_CAPITAL_CLIENTE_CARTERA			= loadResourceToString("cl/bcs/custodia/sql/ActualizarCustodiaOSA_SelectEventoCapitalClienteDisponible.sql");
	private final String SQL_SELECT_EVENTO_CAPITAL_CLIENTE_GARANTIA			= loadResourceToString("cl/bcs/custodia/sql/ActualizarCustodiaOSA_SelectEventoCapitalClienteGarantia.sql");
	private final String SQL_UPDATE_EVENTO_CAPITAL 							= loadResourceToString("cl/bcs/custodia/sql/ActualizarCustodiaOSA_UpdateEventoCapital.sql");
	private final String SQL_UPDATE_EVENTO_CAPITAL_CLIENTE 					= loadResourceToString("cl/bcs/custodia/sql/ActualizarCustodiaOSA_UpdateEventoCapitalCliente.sql");
	
	
	public static class ActualizarEvento{
		public Long IdEventoCapital;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ActualizarEventoCapitalCliente extends BcsPojo{
		public Long IdEventoCapital;
		public Long IdEventoCapitalCliente;
		public Long IdCartera;
		public Long FolioCartera;
	}
	
	public static class ActualizarCustodiaOSA_Entrada{
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public Long 	p_IdEventoCapital;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ActualizarCustodiaOSA_Salida{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida>	P_OUT_MENSAJES;
		
		public ActualizarCustodiaOSA_Salida(){
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
		public Long 			IdGenerarMovimientoCustodia;
		public java.sql.Date 	FechaOperacion;
		public Long 			IdCliente;
		public Long 			IdPortafolio;
		public String 			IdTipoCanalIngreso;
		public String 			IdMonedaSistema;
		public String 			IdConcepto;
		public String 			IdTipoOperacion;
		public Long 			IdOperacion;
		public String 			IdBolsa;
		public String 			IdBoveda;
		public String 			IdCondicionLiquidacion;
		public java.sql.Date	FechaLiquidacion;
		public String 			Glosa;
		public String 			IdClasificacionTipoInstrumento;
		public String 			IdTipoInstrumento;
		public Long 			IdInstrumento;
		public String 			Nemotecnico;
		public String 			IdEmisor;
		public String 			IdMonedaInstrumento;
		public BigDecimal 		Cantidad;
		public BigDecimal		Precio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public ActualizarCustodiaOSA_Salida logica(Connection con, ActualizarCustodiaOSA_Entrada entrada) throws Exception {
		log.write("INICIO");
		ActualizarCustodiaOSA_Salida out = new ActualizarCustodiaOSA_Salida();
		SQL sql=new SQL();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		List<GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar> p_Transitos = new ArrayList<GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar>();
		List<GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar> p_TransitosGarantia = new ArrayList<GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar>();

		HashMap<Long,Long> transitosGenerados = new HashMap<Long, Long>();
		HashMap<Long,Long> transitosGarantiaGenerados = new HashMap<Long, Long>();


		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEventoCapital", entrada.p_IdEventoCapital);
		List<EventoCapitalCliente> eventoCapitalesDisponible = sql.query(con, SQL_SELECT_EVENTO_CAPITAL_CLIENTE_CARTERA, params, EventoCapitalCliente.class);
				
		HashMap<String,Object> paramsG = new HashMap<String,Object>();
		paramsG.put("IdEventoCapital", entrada.p_IdEventoCapital);
		List<EventoCapitalCliente> eventoCapitalesGarantia = sql.query(con, SQL_SELECT_EVENTO_CAPITAL_CLIENTE_GARANTIA, paramsG, EventoCapitalCliente.class);
		
		if(eventoCapitalesDisponible.size() == 0 && eventoCapitalesGarantia.size() == 0)
		{
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_EVENTO_VIGENTE_CERRAR_ERROR",new HashMap<String,String>(),con,entrada.p_IdEmpresa));	
			out.P_OUT_ERRORESCONTADOR += 1L;
			return out;
		}
		List<ActualizarEventoCapitalCliente> actualizarEvtCapitalCliente = new ArrayList<ActualizarEventoCapitalCliente>();
		
		// --------------------------------------------------------------
		// ACTUALIZA CUSTODIA DISPONIBLE	

		if(eventoCapitalesDisponible.size() > 0){
			
			List<ObjDatosEntrada> MovimientosTransito  = new ArrayList<ObjDatosEntrada>();
			
			for(EventoCapitalCliente evc : eventoCapitalesDisponible){
				ObjDatosEntrada arg0 = new ObjDatosEntrada();
				arg0.IdGenerarMovimientoCustodia		= evc.IdGenerarMovimientoCustodia;
				arg0.TablaAfectada						= "TRANSITO";
				arg0.AccionOperacion					= "INGRESO";
				arg0.EsRegistroNuevo					= "S";
				arg0.FolioCarteraOrigen					= 0L;	//Para caso de retiro desde cartera					
				arg0.IdCliente							= evc.IdCliente;
				arg0.IdPortafolio						= evc.IdPortafolio;
				arg0.IdCuentaCustodioPortafolio			= 0L;
				arg0.IdClienteRelacionado				= null;
				arg0.IdPortafolioRelacionado			= null;
				arg0.FechaOperacion						= Fecha.toDate(fechaSistema);
				arg0.FechaVencimientoOperacion			= Fecha.toDate("0001-01-01");
				arg0.PlazoEnDiasOperacion				= 0L;
				arg0.IdTipoOperacion					= "INGOSA";
				arg0.IdConcepto							= "SUECOS";
				arg0.IdCondicionLiquidacion				= "T0"; 
				arg0.FechaLiquidacion					= Fecha.toDate(fechaSistema);
				arg0.IdTipoCanalIngreso					= "SIS";
				arg0.FolioExternoReferencia				= "";
				arg0.IdBolsa							= evc.IdBolsa;	
				arg0.IdUsuario							= entrada.p_IdUsuario;
				arg0.IdSucursal							= 0L; 
				arg0.IdAgente							= 0L; 
				arg0.Glosa								= evc.Glosa;
				arg0.IdInstrumento						= evc.IdInstrumento;
				arg0.IdMonedaInstrumento				= evc.IdMonedaInstrumento;
				arg0.IdTipoReajusteInstrumento			= evc.IdMonedaInstrumento;
				arg0.IdEmisor							= evc.IdEmisor;
				arg0.EsFungible							= "N";
				arg0.IdBoveda							= evc.IdBoveda;
				arg0.CuentaBoveda						= "";
				arg0.EsNominal							= "N";			
				arg0.Cantidad							= evc.Cantidad.setScale(0,RoundingMode.DOWN); 
				arg0.CorteMinimo						= null; 
				arg0.ValorRescate						= null; 
				arg0.PrecioCompra						= BigDecimal.ZERO; 
				arg0.TasaCompra							= BigDecimal.ZERO;
				arg0.MontoCompra						= evc.Cantidad.multiply(evc.Precio, MathContext.DECIMAL128); 
				arg0.MontoCompraMonedaInstrumento		= evc.Cantidad.multiply(evc.Precio, MathContext.DECIMAL128);
				MovimientosTransito.add(arg0);
			}
			
			IngresoCustodia ingresoTransito = new IngresoCustodia();
			IngresoCustodia.IngresoCustodiaEntrada paramInSP = new IngresoCustodia.IngresoCustodiaEntrada();
			paramInSP.p_IdEmpresa			= entrada.p_IdEmpresa;		
			paramInSP.p_IdModulo			= entrada.p_IdModulo;
			paramInSP.p_SimularOperacion	= "N";
			paramInSP.p_TicketAtencion 		= 0L;
			paramInSP.p_DatosEntrada		= MovimientosTransito;				
			IngresoCustodia.IngresoCustodiaSalida resultIngresoRetiroTransito = ingresoTransito.logica(con, paramInSP);
			
			if (!resultIngresoRetiroTransito.P_OUT_ERRORESCONTADOR.equals(0L)){
				for (IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida msg : resultIngresoRetiroTransito.P_OUT_DATOSSALIDA){
					if (msg.RegistroConProblemas.equals("N")) continue;
					out.P_OUT_MENSAJES.add(new MensajeSalida(msg.IdGenerarMovimientoCustodia ,MensajeSalida.MENSAJE_ERROR, msg.Mensaje, new HashMap<>() ,con,entrada.p_IdEmpresa));
				};					
				out.P_OUT_ERRORESCONTADOR = 1L;
				return out;									
			}
			else
			{
				for (IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida reg : resultIngresoRetiroTransito.P_OUT_DATOSSALIDA){
					if (reg.RegistroConProblemas.equals("S")) continue;
					
					GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar MOV = new GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar();
					MOV.IdMovimientoCarteraEnTransito = reg.IdCustodia;
					MOV.EsCertificacion = "S";
					MOV.IdUsuario = entrada.p_IdUsuario;
					p_Transitos.add(MOV);		
					
					transitosGenerados.put(reg.IdGenerarMovimientoCustodia,reg.IdCustodia);
				};				
			}
			
			//Se actualizan movimientos en transito
			if (p_Transitos.size() > 0 )
			{
				GestionMovimientoCarteraEnTransito.ActualizarMovimientoEnTransito(con, p_Transitos);
			}
			
			List<ObjDatosEntrada> MovimientosEvCliCus  = new ArrayList<ObjDatosEntrada>();
			for(EventoCapitalCliente evc : eventoCapitalesDisponible){
				ObjDatosEntrada arg0 = new ObjDatosEntrada();
				arg0.IdGenerarMovimientoCustodia		= evc.IdGenerarMovimientoCustodia;
				arg0.TablaAfectada						= "CARTERA";
				arg0.AccionOperacion					= "INGRESO";
				arg0.EsRegistroNuevo					= "S";
				arg0.FolioCarteraOrigen					= 0L;	//Para caso de retiro desde cartera					
				arg0.IdCliente							= evc.IdCliente; 
				arg0.IdPortafolio						= evc.IdPortafolio;
				arg0.IdCuentaCustodioPortafolio			= 0L;
				arg0.IdClienteRelacionado				= null;
				arg0.IdPortafolioRelacionado			= null;
				arg0.FechaOperacion						= Fecha.toDate(fechaSistema);
				arg0.FechaVencimientoOperacion			= Fecha.toDate("0001-01-01");
				arg0.PlazoEnDiasOperacion				= 0L;
				arg0.IdTipoOperacion					= "INGOSA";
			
				arg0.IdConcepto							= "SUECOS";
				arg0.IdCondicionLiquidacion				= "T0"; 
				arg0.FechaLiquidacion					= Fecha.toDate(fechaSistema);
				arg0.IdTipoCanalIngreso					= "SIS";
				arg0.FolioExternoReferencia				= "";
				arg0.IdBolsa							= evc.IdBolsa;	
				arg0.IdUsuario							= entrada.p_IdUsuario;
				arg0.IdSucursal							= 0L; 
				arg0.IdAgente							= 0L; 
				arg0.Glosa								= evc.Glosa;
				arg0.IdInstrumento						= evc.IdInstrumento;
				arg0.IdMonedaInstrumento				= evc.IdMonedaInstrumento;
				arg0.IdEmisor							= evc.IdEmisor;
				arg0.EsFungible							= "N";
				arg0.IdBoveda							= evc.IdBoveda;
				arg0.CuentaBoveda						= "";
				arg0.EsNominal							= "N";			
				arg0.Cantidad							= evc.Cantidad.setScale(0, RoundingMode.DOWN);
				arg0.CorteMinimo						= null;
				arg0.ValorRescate						= null;
				arg0.PrecioCompra						= BigDecimal.ZERO; //vPrecio; 
				arg0.TasaCompra							= BigDecimal.ZERO;
				arg0.MontoCompra						= evc.Cantidad.multiply(evc.Precio, MathContext.DECIMAL128); 
				arg0.MontoCompraMonedaInstrumento		= evc.Cantidad.multiply(evc.Precio, MathContext.DECIMAL128);
				arg0.IdMovimientoEnTransito = 0L;
				if(transitosGenerados.get(evc.IdGenerarMovimientoCustodia) != null) {
					arg0.IdMovimientoCarteraEnTransitoOrigen = transitosGenerados.get(evc.IdGenerarMovimientoCustodia);
				}else {
					arg0.IdMovimientoCarteraEnTransitoOrigen = 0L;
				}
				if(arg0.Cantidad.compareTo(BigDecimal.ZERO) > 0) {
					MovimientosEvCliCus.add(arg0);
				}
				
			}
			
			IngresoCustodia ingresoCustodia = new IngresoCustodia();
			IngresoCustodia.IngresoCustodiaEntrada paramInSPIngreso = new IngresoCustodia.IngresoCustodiaEntrada();
			paramInSPIngreso.p_IdEmpresa				= entrada.p_IdEmpresa;		
			paramInSPIngreso.p_IdModulo					= entrada.p_IdModulo;
			paramInSPIngreso.p_SimularOperacion			= "N";
			paramInSPIngreso.p_ValorizacionAutomatica	= "N";
			paramInSPIngreso.p_TicketAtencion			= LogCustodia.logGenerico(con);
			paramInSPIngreso.p_DatosEntrada				= MovimientosEvCliCus;				
			IngresoCustodia.IngresoCustodiaSalida resultIngresoRetiroCartera = ingresoCustodia.logica(con, paramInSPIngreso);
			
			out.P_OUT_ERRORESCONTADOR += resultIngresoRetiroCartera.P_OUT_ERRORESCONTADOR;
			out.P_OUT_MENSAJES.addAll(resultIngresoRetiroCartera.P_OUT_MENSAJES.parallelStream().filter(mje-> mje.tipo.equals(MensajeSalida.MENSAJE_ERROR)).collect(Collectors.toList()));
			resultIngresoRetiroCartera.P_OUT_DATOSSALIDA.forEach(ds -> {
				ActualizarEventoCapitalCliente actualizaEvcCliente = new ActualizarEventoCapitalCliente();
				actualizaEvcCliente.IdEventoCapital = entrada.p_IdEventoCapital;
				actualizaEvcCliente.IdEventoCapitalCliente = ds.IdGenerarMovimientoCustodia;
				actualizaEvcCliente.IdCartera = ds.IdCustodia;
				actualizaEvcCliente.FolioCartera = ds.FolioCustodia;
				actualizarEvtCapitalCliente.add(actualizaEvcCliente);
			});
		}

		
		
		// --------------------------------------------------------------
		// ACTUALIZA GARANTIAS	
		/* GARANTIAS --> SE DEBE HACER:
		 * 								1.- INGRESO DE CARTERAS
		 * 								2.- RETIRO DE ESAS CARTERAS
		 * 								3.- INGRESOS A GARANTIA.
		 */

		if(eventoCapitalesGarantia.size() > 0){
			
			
			List<ObjDatosEntrada> MovimientosTransitoGarantias  = new ArrayList<ObjDatosEntrada>();
			
			for(EventoCapitalCliente evc : eventoCapitalesGarantia){
				ObjDatosEntrada arg0 = new ObjDatosEntrada();
				arg0.IdGenerarMovimientoCustodia		= evc.IdGenerarMovimientoCustodia;
				arg0.TablaAfectada						= "TRANSITO";
				arg0.AccionOperacion					= "INGRESO";
				arg0.EsRegistroNuevo					= "S";
				arg0.FolioCarteraOrigen					= 0L;	//Para caso de retiro desde cartera					
				arg0.IdCliente							= evc.IdCliente;
				arg0.IdPortafolio						= evc.IdPortafolio;
				arg0.IdCuentaCustodioPortafolio			= 0L;
				arg0.IdClienteRelacionado				= null;
				arg0.IdPortafolioRelacionado			= null;
				arg0.FechaOperacion						= Fecha.toDate(fechaSistema);
				arg0.FechaVencimientoOperacion			= Fecha.toDate("0001-01-01");
				arg0.PlazoEnDiasOperacion				= 0L;
				arg0.IdTipoOperacion					= "INGOSA";
				arg0.IdConcepto							= "SUECOS";
				arg0.IdCondicionLiquidacion				= "T0"; 
				arg0.FechaLiquidacion					= Fecha.toDate(fechaSistema);
				arg0.IdTipoCanalIngreso					= "SIS";
				arg0.FolioExternoReferencia				= "";
				arg0.IdBolsa							= evc.IdBolsa;	
				arg0.IdUsuario							= entrada.p_IdUsuario;
				arg0.IdSucursal							= 0L; 
				arg0.IdAgente							= 0L; 
				arg0.Glosa								= evc.Glosa;
				arg0.IdInstrumento						= evc.IdInstrumento;
				arg0.IdMonedaInstrumento				= evc.IdMonedaInstrumento;
				arg0.IdTipoReajusteInstrumento			= evc.IdMonedaInstrumento;
				arg0.IdEmisor							= evc.IdEmisor;
				arg0.EsFungible							= "N";
				arg0.IdBoveda							= evc.IdBoveda;
				arg0.CuentaBoveda						= "";
				arg0.EsNominal							= "N";			
				arg0.Cantidad							= evc.Cantidad.setScale(0,RoundingMode.DOWN); 
				arg0.CorteMinimo						= null; 
				arg0.ValorRescate						= null; 
				arg0.PrecioCompra						= BigDecimal.ZERO; 
				arg0.TasaCompra							= BigDecimal.ZERO;
				arg0.MontoCompra						= evc.Cantidad.multiply(evc.Precio, MathContext.DECIMAL128); 
				arg0.MontoCompraMonedaInstrumento		= evc.Cantidad.multiply(evc.Precio, MathContext.DECIMAL128);
				MovimientosTransitoGarantias.add(arg0);
				
				ObjDatosEntrada arg1 = new ObjDatosEntrada();
				arg1.IdGenerarMovimientoCustodia		= evc.IdGenerarMovimientoCustodia*-1L;
				arg1.TablaAfectada						= "TRANSITO";
				arg1.AccionOperacion					= "INGRESO";
				arg1.EsRegistroNuevo					= "S";
				arg1.FolioCarteraOrigen					= 0L;	//Para caso de retiro desde cartera					
				arg1.IdCliente							= evc.IdCliente;
				arg1.IdPortafolio						= evc.IdPortafolio;
				arg1.IdCuentaCustodioPortafolio			= 0L;
				arg1.IdClienteRelacionado				= null;
				arg1.IdPortafolioRelacionado			= null;
				arg1.FechaOperacion						= Fecha.toDate(fechaSistema);
				arg1.FechaVencimientoOperacion			= Fecha.toDate("0001-01-01");
				arg1.PlazoEnDiasOperacion				= 0L;
				arg1.IdTipoOperacion					= "INGGTA";
				arg1.IdTipoOperacionGestora				= "INGGTA";
				arg1.IdOrigenGarantia					= "CUS";
				arg1.IdConcepto							= "SUECOS";
				arg1.IdCondicionLiquidacion				= "T0"; 
				arg1.FechaLiquidacion					= Fecha.toDate(fechaSistema);
				arg1.IdTipoCanalIngreso					= "SIS";
				arg1.FolioExternoReferencia				= "";
				arg1.IdBolsa							= evc.IdBolsa;	
				arg1.IdUsuario							= entrada.p_IdUsuario;
				arg1.IdSucursal							= 0L; 
				arg1.IdAgente							= 0L; 
				arg1.Glosa								= evc.Glosa;
				arg1.IdInstrumento						= evc.IdInstrumento;
				arg1.IdMonedaInstrumento				= evc.IdMonedaInstrumento;
				arg1.IdTipoReajusteInstrumento			= evc.IdMonedaInstrumento;
				arg1.IdEmisor							= evc.IdEmisor;
				arg1.EsFungible							= "N";
				arg1.IdBoveda							= evc.IdBoveda;
				arg1.CuentaBoveda						= "";
				arg1.EsNominal							= "N";			
				arg1.Cantidad							= evc.Cantidad.setScale(0,RoundingMode.DOWN); 
				arg1.CorteMinimo						= null; 
				arg1.ValorRescate						= null; 
				arg1.PrecioCompra						= BigDecimal.ZERO; 
				arg1.TasaCompra							= BigDecimal.ZERO;
				arg1.MontoCompra						= evc.Cantidad.multiply(evc.Precio, MathContext.DECIMAL128); 
				arg1.MontoCompraMonedaInstrumento		= evc.Cantidad.multiply(evc.Precio, MathContext.DECIMAL128);
				MovimientosTransitoGarantias.add(arg1);
			}
			
			
			
			IngresoCustodia ingresoTransito = new IngresoCustodia();
			IngresoCustodia.IngresoCustodiaEntrada paramInSP = new IngresoCustodia.IngresoCustodiaEntrada();
			paramInSP.p_IdEmpresa			= entrada.p_IdEmpresa;		
			paramInSP.p_IdModulo			= entrada.p_IdModulo;
			paramInSP.p_SimularOperacion	= "N";
			paramInSP.p_TicketAtencion 		= 0L;
			paramInSP.p_DatosEntrada		= MovimientosTransitoGarantias;				
			IngresoCustodia.IngresoCustodiaSalida resultIngresoRetiroTransito = ingresoTransito.logica(con, paramInSP);
			
			if (!resultIngresoRetiroTransito.P_OUT_ERRORESCONTADOR.equals(0L)){
				for (IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida msg : resultIngresoRetiroTransito.P_OUT_DATOSSALIDA){
					if (msg.RegistroConProblemas.equals("N")) continue;
					out.P_OUT_MENSAJES.add(new MensajeSalida(msg.IdGenerarMovimientoCustodia ,MensajeSalida.MENSAJE_ERROR, msg.Mensaje, new HashMap<>() ,con,entrada.p_IdEmpresa));
				};					
				out.P_OUT_ERRORESCONTADOR = 1L;
				return out;									
			}
			else
			{
				for (IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida reg : resultIngresoRetiroTransito.P_OUT_DATOSSALIDA){
					if (reg.RegistroConProblemas.equals("S")) continue;
					
					GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar MOV = new GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar();
					MOV.IdMovimientoCarteraEnTransito = reg.IdCustodia;
					MOV.EsCertificacion = "S";
					MOV.IdUsuario = entrada.p_IdUsuario;
					p_TransitosGarantia.add(MOV);		
					
					transitosGarantiaGenerados.put(reg.IdGenerarMovimientoCustodia,reg.IdCustodia);
				};				
			}
			
			//Se actualizan movimientos en transito
			if (p_TransitosGarantia.size() > 0 )
			{
				GestionMovimientoCarteraEnTransito.ActualizarMovimientoEnTransito(con, p_TransitosGarantia);
			}
			
			
			// INGRESO DE CARTERA
			List<ObjDatosEntrada> MovimientosEvCliGar  = new ArrayList<ObjDatosEntrada>();
			List<RetiroCustodia.ObjDatosEntrada> p_RetiroCartera = new ArrayList<RetiroCustodia.ObjDatosEntrada>();
			List<ObjDatosEntrada> p_IngresoGarantia = new ArrayList<ObjDatosEntrada>();
			for(EventoCapitalCliente evc : eventoCapitalesGarantia){
				ObjDatosEntrada 							arg0 = new ObjDatosEntrada();
				RetiroCustodia.ObjDatosEntrada 				arg1 = new RetiroCustodia.ObjDatosEntrada();
				ObjDatosEntrada							 	arg2 = new ObjDatosEntrada();
				arg0.IdGenerarMovimientoCustodia		= evc.IdGenerarMovimientoCustodia;
				arg0.TablaAfectada						= "CARTERA";
				arg0.AccionOperacion					= "INGRESO";
				arg0.EsRegistroNuevo					= "S";
				arg0.FolioCarteraOrigen					= 0L;	//Para caso de retiro desde cartera					
				arg0.IdCliente							= evc.IdCliente; 
				arg0.IdPortafolio						= evc.IdPortafolio;
				arg0.IdCuentaCustodioPortafolio			= 0L;
				arg0.IdClienteRelacionado				= null;
				arg0.IdPortafolioRelacionado			= null;
				arg0.FechaOperacion						= Fecha.toDate(fechaSistema);
				arg0.FechaVencimientoOperacion			= Fecha.toDate("0001-01-01");
				arg0.PlazoEnDiasOperacion				= 0L;
				arg0.IdTipoOperacion					= "INGOSA";
				//jvillagran
				arg0.IdConcepto							= "SUECOS";
				arg0.IdCondicionLiquidacion				= "T0"; 
				arg0.FechaLiquidacion					= Fecha.toDate(fechaSistema);
				arg0.IdTipoCanalIngreso					= "SIS";
				arg0.FolioExternoReferencia				= "";
				arg0.IdBolsa							= evc.IdBolsa;	
				arg0.IdUsuario							= entrada.p_IdUsuario;
				arg0.IdSucursal							= 0L; 
				arg0.IdAgente							= 0L; 
				arg0.Glosa								= evc.Glosa;
				arg0.IdInstrumento						= evc.IdInstrumento;
				arg0.IdMonedaInstrumento				= evc.IdMonedaInstrumento;
				arg0.IdEmisor							= evc.IdEmisor;
				arg0.EsFungible							= "N";
				arg0.IdBoveda							= evc.IdBoveda;
				arg0.CuentaBoveda						= "";
				arg0.EsNominal							= "N";			
				arg0.Cantidad							= evc.Cantidad; 
				arg0.CorteMinimo						= null;
				arg0.ValorRescate						= null;
				arg0.PrecioCompra						= BigDecimal.ZERO; //vPrecio; 
				arg0.TasaCompra							= BigDecimal.ZERO;
				arg0.MontoCompra						= evc.Cantidad.multiply(evc.Precio, MathContext.DECIMAL128); 
				arg0.MontoCompraMonedaInstrumento		= evc.Cantidad.multiply(evc.Precio, MathContext.DECIMAL128);
				if(transitosGarantiaGenerados.get(evc.IdGenerarMovimientoCustodia) != null) {
					arg0.IdMovimientoCarteraEnTransitoOrigen = transitosGarantiaGenerados.get(evc.IdGenerarMovimientoCustodia);
				}else {
					arg0.IdMovimientoCarteraEnTransitoOrigen = 0L;
				}
				MovimientosEvCliGar.add(arg0);
				//***********************************************************************************************
				arg1.IdGenerarMovimientoCustodia			= evc.IdGenerarMovimientoCustodia;
				arg1.TablaAfectada							= "CARTERA";
				arg1.AccionOperacion						= "RETIRO";
				arg1.IdCliente								= evc.IdCliente; 
				arg1.IdPortafolio							= evc.IdPortafolio;
				arg1.FechaOperacion							= evc.FechaOperacion;
				arg1.IdTipoOperacion						= "INGGTA";		
				arg1.IdConcepto								= evc.IdConcepto;
				arg1.IdCondicionLiquidacion					= evc.IdCondicionLiquidacion; 
				arg1.FechaLiquidacion						= Fecha.toDate(fechaSistema);
				arg1.IdTipoCanalIngreso						= evc.IdTipoCanalIngreso; 		
				arg1.IdUsuario								= entrada.p_IdUsuario;
				arg1.Glosa									= evc.Glosa;
				arg1.IdInstrumento							= evc.IdInstrumento;
				arg1.IdMonedaInstrumento					= evc.IdMonedaInstrumento;
				arg1.IdEmisor								= evc.IdEmisor;
				arg1.EsFungible								= "N";
				arg1.IdBoveda								= evc.IdBoveda;
				arg1.Cantidad								= evc.Cantidad; 
				arg1.CorteMinimo							= BigDecimal.ZERO; 
				arg1.ValorRescate							= BigDecimal.ZERO; 
				if(transitosGarantiaGenerados.get(evc.IdGenerarMovimientoCustodia*-1L) != null) {
					arg0.IdMovimientoCarteraEnTransitoOrigen = transitosGarantiaGenerados.get(evc.IdGenerarMovimientoCustodia*-1L);
				}else {
					arg0.IdMovimientoCarteraEnTransitoOrigen = 0L;
				}
				p_RetiroCartera.add(arg1);
				//***********************************************************************************************
				arg2.IdGenerarMovimientoCustodia		= evc.IdGenerarMovimientoCustodia;
				arg2.TablaAfectada						= "GARANTIA";
				arg2.AccionOperacion					= "INGRESO";
				arg2.EsRegistroNuevo					= "S";
				arg2.FolioCarteraOrigen					= 0L;	//Para caso de retiro desde cartera					
				arg2.IdCliente							= evc.IdCliente; 
				arg2.IdPortafolio						= evc.IdPortafolio;
				arg2.IdCuentaCustodioPortafolio			= 0L;
				arg2.IdClienteRelacionado				= null;
				arg2.IdPortafolioRelacionado			= null;
				arg2.FechaOperacion						= Fecha.toDate(fechaSistema);
				arg2.FechaVencimientoOperacion			= Fecha.toDate("0001-01-01");
				arg2.PlazoEnDiasOperacion				= 0L;
				arg2.IdTipoOperacion					= "INGOSA";
				//jvillagan
				arg2.IdConcepto							= "SUECOS";
				arg2.IdCondicionLiquidacion				= "T0"; 
				arg2.FechaLiquidacion					= Fecha.toDate(fechaSistema);
				arg2.IdTipoCanalIngreso					= "SIS";
				arg2.FolioExternoReferencia				= "";
				arg2.IdBolsa							= evc.IdBolsa;	
				arg2.IdUsuario							= entrada.p_IdUsuario;
				arg2.IdSucursal							= 0L; 
				arg2.IdAgente							= 0L; 
				arg2.Glosa								= evc.Glosa;
				arg2.IdInstrumento						= evc.IdInstrumento;
				arg2.IdMonedaInstrumento				= evc.IdMonedaInstrumento;
				arg2.IdEmisor							= evc.IdEmisor;
				arg2.EsFungible							= "N";
				arg2.IdBoveda							= evc.IdBoveda;
				arg2.CuentaBoveda						= "";
				arg2.EsNominal							= "N";			
				arg2.Cantidad							= evc.Cantidad; 
				arg2.CorteMinimo						= null;
				arg2.ValorRescate						= null;
				arg2.PrecioCompra						= BigDecimal.ZERO; //vPrecio; 
				arg2.TasaCompra							= BigDecimal.ZERO;
				arg2.MontoCompra						= evc.Cantidad.multiply(evc.Precio, MathContext.DECIMAL128); 
				arg2.MontoCompraMonedaInstrumento		= evc.Cantidad.multiply(evc.Precio, MathContext.DECIMAL128);
				if(transitosGarantiaGenerados.get(evc.IdGenerarMovimientoCustodia*-1L) != null) {
					arg0.IdMovimientoCarteraEnTransitoOrigen = transitosGarantiaGenerados.get(evc.IdGenerarMovimientoCustodia*-1L);
				}else {
					arg0.IdMovimientoCarteraEnTransitoOrigen = 0L;
				}
				p_IngresoGarantia.add(arg2);
			}
			
			IngresoCustodia ingresoCustodiaGar = new IngresoCustodia();
			IngresoCustodia.IngresoCustodiaEntrada paramInSPIngresoCusGar = new IngresoCustodia.IngresoCustodiaEntrada();
			
			paramInSPIngresoCusGar.p_IdEmpresa				= entrada.p_IdEmpresa;		
			paramInSPIngresoCusGar.p_IdModulo				= entrada.p_IdModulo;
			paramInSPIngresoCusGar.p_SimularOperacion		= "N";
			paramInSPIngresoCusGar.p_ValorizacionAutomatica	= "N";
			paramInSPIngresoCusGar.p_TicketAtencion			= LogCustodia.logGenerico(con);
			paramInSPIngresoCusGar.p_DatosEntrada			= MovimientosEvCliGar;				
			
			IngresoCustodia.IngresoCustodiaSalida resultIngresoRetiroGarantia = ingresoCustodiaGar.logica(con, paramInSPIngresoCusGar);
			
			out.P_OUT_MENSAJES.addAll(resultIngresoRetiroGarantia.P_OUT_MENSAJES.parallelStream().filter(mje-> mje.tipo.equals(MensajeSalida.MENSAJE_ERROR)).collect(Collectors.toList()));
			out.P_OUT_ERRORESCONTADOR += resultIngresoRetiroGarantia.P_OUT_ERRORESCONTADOR;
			resultIngresoRetiroGarantia.P_OUT_DATOSSALIDA.forEach(ds -> {
				ActualizarEventoCapitalCliente actualizaEvcCliente = new ActualizarEventoCapitalCliente();
				actualizaEvcCliente.IdEventoCapital = entrada.p_IdEventoCapital;
				actualizaEvcCliente.IdEventoCapitalCliente = ds.IdGenerarMovimientoCustodia;
				actualizaEvcCliente.IdCartera = ds.IdCustodia;
				actualizaEvcCliente.FolioCartera = ds.FolioCustodia;
				actualizarEvtCapitalCliente.add(actualizaEvcCliente);
			});
			
			if (out.P_OUT_ERRORESCONTADOR.equals(0L)){
				// RETIRO DE CARTERA
				for (IngresoCustodiaSalida.ObjDatosSalida recorreIngresoCartera :resultIngresoRetiroGarantia.P_OUT_DATOSSALIDA){
					for (RetiroCustodia.ObjDatosEntrada recorrep_RetiroCartera :p_RetiroCartera){
						if (recorreIngresoCartera.IdGenerarMovimientoCustodia.equals(recorrep_RetiroCartera.IdGenerarMovimientoCustodia)){
							recorrep_RetiroCartera.IdCustodia			= recorreIngresoCartera.IdCustodia;									
							recorrep_RetiroCartera.FolioCustodia		= recorreIngresoCartera.FolioCustodia;
						}
					}
				}
				RetiroCustodia retiroCustodia = new RetiroCustodia();
				RetiroCustodia.RetiroCustodiaEntrada paramRetCartera = new RetiroCustodia.RetiroCustodiaEntrada();
				paramRetCartera.p_IdEmpresa					= entrada.p_IdEmpresa;
				paramRetCartera.p_SimularOperacion			= "N";
				paramRetCartera.p_ValorizacionAutomatica	= "N";
				paramRetCartera.p_TicketAtencion			= LogCustodia.logGenerico(con);;
				paramRetCartera.p_DatosEntrada				= p_RetiroCartera;
				RetiroCustodia.RetiroCustodiaSalida resultRetiros = retiroCustodia.logica(con, paramRetCartera);
				out.P_OUT_MENSAJES.addAll(resultRetiros.P_OUT_MENSAJES.parallelStream().filter(mje-> mje.tipo.equals(MensajeSalida.MENSAJE_ERROR)).collect(Collectors.toList()));
				out.P_OUT_ERRORESCONTADOR += resultRetiros.P_OUT_ERRORESCONTADOR;
				if (out.P_OUT_ERRORESCONTADOR.equals(0L)){
					//INGRESO A GARANTIA
					for (RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida recorreresultRetiros: resultRetiros.P_OUT_DATOSSALIDA){
						for(ObjDatosEntrada recorrep_IngresoGarantia: p_IngresoGarantia){
							if (recorrep_IngresoGarantia.IdGenerarMovimientoCustodia.equals(recorreresultRetiros.IdGenerarMovimientoCustodia)){
								recorrep_IngresoGarantia.IdCarteraOrigen			= recorreresultRetiros.IdCustodia;
								recorrep_IngresoGarantia.FolioCarteraOrigen			= recorreresultRetiros.FolioCustodia;
								recorrep_IngresoGarantia.IdOrigenGarantia			= "CUS";
							}
						}
					}
					IngresoCustodia.IngresoCustodiaEntrada paramInGarantia = new IngresoCustodia.IngresoCustodiaEntrada();
					
					paramInGarantia.p_IdEmpresa					= entrada.p_IdEmpresa;		
					paramInGarantia.p_IdModulo					= entrada.p_IdModulo;
					paramInGarantia.p_SimularOperacion			= "N";
					paramRetCartera.p_ValorizacionAutomatica	= "N";
					paramInGarantia.p_TicketAtencion			= LogCustodia.logGenerico(con);;
					paramInGarantia.p_DatosEntrada				= p_IngresoGarantia;
					
					IngresoCustodia ingresoCustodiaGtia = new IngresoCustodia();
					IngresoCustodia.IngresoCustodiaSalida resultIngresosGtia = ingresoCustodiaGtia.logica(con, paramInGarantia);
					out.P_OUT_MENSAJES.addAll(resultIngresosGtia.P_OUT_MENSAJES.parallelStream().filter(mje-> mje.tipo.equals(MensajeSalida.MENSAJE_ERROR)).collect(Collectors.toList()));
					out.P_OUT_ERRORESCONTADOR += resultIngresosGtia.P_OUT_ERRORESCONTADOR;
				}
			}
		}		
		
		
		
		// --------------------------------------------------------------
		// ACTUALIZA ESTADO DE EVENTO CAPITAL 
		
		if(out.P_OUT_ERRORESCONTADOR.equals(0L)){
			ActualizarEvento evento = new ActualizarEvento();
			evento.IdEventoCapital = entrada.p_IdEventoCapital;
			List<ActualizarEvento> actualizar = new ArrayList<ActualizarEvento>();
			actualizar.add(evento);
			sql.batch(con, SQL_UPDATE_EVENTO_CAPITAL, actualizar, ActualizarEvento.class);
			sql.batch(con, SQL_UPDATE_EVENTO_CAPITAL_CLIENTE, actualizarEvtCapitalCliente, ActualizarEventoCapitalCliente.class);
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_OPERACION_ACTUALIZAR_EXITO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
		}		
		
		CacheValorizacion.valorizar(con);
		return out;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		ActualizarCustodiaOSA_Entrada in=gson.fromJson(jin.toString() ,ActualizarCustodiaOSA_Entrada.class);
		ActualizarCustodiaOSA_Salida out= logica(conn,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01","connections.properties")) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":14,\"p_IdModulo\":\"CUS\",\"p_IdEventoCapital\":6798013}";
			ActualizarCustodiaOSA_Entrada Objin = gson.fromJson(json, ActualizarCustodiaOSA_Entrada.class);
			
			ActualizarCustodiaOSA demo=new ActualizarCustodiaOSA();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				ActualizarCustodiaOSA_Salida ObjOut= demo.logica(con, Objin);
 				con.commit();
 				//con.rollback();
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