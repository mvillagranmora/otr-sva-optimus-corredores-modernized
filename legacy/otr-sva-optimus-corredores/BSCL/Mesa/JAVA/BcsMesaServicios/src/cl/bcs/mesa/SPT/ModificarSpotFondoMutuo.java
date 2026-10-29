package cl.bcs.mesa.SPT;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.mesa.SPT.CrearOperacionSpot.FormaPagoObjIn;
import cl.bcs.mesa.SPT.CrearOperacionSpot.OperacionObjIn;
import cl.bcs.mesa.SPT.ModificarOperacionSpot.ModificarOperacionSpotEntrada;
import cl.bcs.mesa.SPT.ModificarOperacionSpot.ModificarOperacionSpotSalida;
import cl.bcs.plataforma.pla.AgregarMensajeWebsocket;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarSpotFondoMutuo implements JavaCall{
	static final Logger logger = LogManager.getLogger(ModificarSpotFondoMutuo.class);
	
	private final String SQL_SELECT_OPERACIONES_SPOT_FFMM = loadResourceToString("cl/bcs/mesa/SPT/sql/ModificarSpotFondoMutuo_SelectOperacionesSpotFondoMutuo.sql");
	private final String SQL_SELECT_FORMAS_PAGO_SPOT_FFMM = loadResourceToString("cl/bcs/mesa/SPT/sql/ModificarSpotFondoMutuo_SelectFomasPagoFondoMutuo.sql");
	
	public static class ModificarSpotFondoMutuoEntrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_FechaOperacion;
		public BigDecimal p_TipoCambioCierre;
		public BigDecimal p_TipoCambioCierreAntiguo;//para el log

		public BigDecimal p_TipoCambioCosto;
		public BigDecimal p_TipoCambioCostoAntiguo;//para el log
		
		
		public String p_TipoOperacion ="";
		public String p_FondoMutuoDolar;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ModificarSpotFondoMutuoSalida {
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;
		
		public ModificarSpotFondoMutuoSalida() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class OperacionSpot {
		public Long IdSolicitudAporteRescate;
		public Long IdOperacionSpot;
		public Long IdInstrumento;
		public String IdTipoOperacion;
		public String IdTipoProducto;
		public Long IdCliente; 
		public Long IdPortafolio;
		public java.sql.Timestamp FechaOperacion;
		public String IdMonedaPrincipal;	
		public String IdMonedaSecundaria;
		public BigDecimal MontoMonedaPrincipal;
		public BigDecimal MontoMonedaSecundaria;
		public BigDecimal TipoCambioCierre;
		public BigDecimal TipoCambioCierreForward;
		public BigDecimal ParidadCierre;
		public BigDecimal ParidadCierreForward;
		public BigDecimal TipoCambioCosto;
		public BigDecimal ParidadCosto;
		public BigDecimal MontoEquivalente;
		public BigDecimal MontoRentabilidad;
		public BigDecimal MontoRentabilidadCanal;
		public BigDecimal Margen;
		public BigDecimal Utilidad;
		public BigDecimal PuntaCompra;
		public BigDecimal PuntaVenta;
		public String SolicitudTipoCambio;
		public Long IdMesa;
		public String IdModulo;
		public Long IdTipoDolar;
		public String TipoComprobanteFacturacion;
		public Long IdAgente;
		public String BenOur;
		public BigDecimal EquivalenteConGasto;
		public BigDecimal TipoCambioCierreConGasto;
		public String Observacion;
		public Long CodigoComercio;
		public Long FolioPlanilla;
		public String FolioInterfaz;
		public String OperacionFicticia;
		public String IdTipoCanalIngreso;
		public String IdDatatec;
		public String Estado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	
	public static class FormaPagoSpot{
		public Long IdComprobantePagoOperacion;
		public String IdTipoMovimiento;
		public String IdFormaDePago;
		public String IdMoneda;
		public String IdMonedaLiquidacion;
		public Long IdOperacion;
		public String RecibePagaPortafolio;
		public BigDecimal MontoFormaDePago;
		public String AfectarTesoreria;
		public Date FechaLiquidacion;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	// -----------------------------------------------------------
	// CALL 	
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ModificarSpotFondoMutuoEntrada in = gson.fromJson(jin.toString(), ModificarSpotFondoMutuoEntrada.class);
		ModificarSpotFondoMutuoSalida out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	// -----------------------------------------------------------
	// LOGICA 
	
	public ModificarSpotFondoMutuoSalida logica(Connection con, ModificarSpotFondoMutuoEntrada entrada) throws Exception {
		logger.info("INICIO :: SPOT.ModificarSpotFondoMutuo");
		SQL _sql = new SQL();
		ModificarSpotFondoMutuoSalida out = new ModificarSpotFondoMutuoSalida();		
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		
		String IdTipoOperacion;
		if (entrada.p_TipoOperacion.isEmpty() || entrada.p_TipoOperacion.equals("0")) {
			IdTipoOperacion="";
		}else {
			IdTipoOperacion =  entrada.p_TipoOperacion.substring(0,3);
			IdTipoOperacion +="SPT";
		}
		
		String FondoNacionalValutaCero = "NACIONALES_VALUTA_CERO".equals(entrada.p_FondoMutuoDolar) ? "S" : "N";
		
		params.put("FechaOperacion", entrada.p_FechaOperacion);
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("IdTipoOperacion", IdTipoOperacion);
		params.put("FondoNacionalValutaCero", FondoNacionalValutaCero);
		List<OperacionSpot> operacionesSpot = _sql.query(con, SQL_SELECT_OPERACIONES_SPOT_FFMM, params, OperacionSpot.class);
		
		
		List<Long> idOperacionesSpot = operacionesSpot.stream().map(operacion -> {
			return operacion.IdOperacionSpot;
		}).collect(Collectors.toList());
		params.clear();
		params.put("IdOperacionSpot", idOperacionesSpot);
		List<FormaPagoSpot> formasPago = _sql.query(con, SQL_SELECT_FORMAS_PAGO_SPOT_FFMM, params, FormaPagoSpot.class);
		
		ModificarOperacionSpot modificarOperacionSpot = new ModificarOperacionSpot();
		
		ModificarOperacionSpotEntrada entradaModificarOperacionSpot = new ModificarOperacionSpotEntrada();
		entradaModificarOperacionSpot.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaModificarOperacionSpot.p_IdModulo = "MSP";
		entradaModificarOperacionSpot.p_IdUsuario = entrada.p_IdUsuario;
		entradaModificarOperacionSpot.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		
		entradaModificarOperacionSpot.p_DatosOperacion = operacionesSpot.stream().map(operacion->{
			OperacionObjIn inOperacion = new OperacionObjIn();
			inOperacion.NumeroOperacion = String.valueOf(operacion.IdOperacionSpot);
			inOperacion.IdInstrumento = operacion.IdInstrumento;
			inOperacion.IdTipoOperacion = operacion.IdTipoOperacion;
			inOperacion.IdTipoProducto = "MEME";
			inOperacion.IdCliente = operacion.IdCliente; 
			inOperacion.IdPortafolio = operacion.IdPortafolio;
			inOperacion.FechaOperacion = operacion.FechaOperacion;
			inOperacion.IdMonedaPrincipal = operacion.IdMonedaPrincipal;	
			inOperacion.IdMonedaSecundaria = operacion.IdMonedaSecundaria;
			inOperacion.MontoMonedaPrincipal = "VENSPT".equals(operacion.IdTipoOperacion) ? operacion.MontoMonedaSecundaria.divide(entrada.p_TipoCambioCierre, MathContext.DECIMAL128) : operacion.MontoMonedaPrincipal;
			inOperacion.MontoMonedaSecundaria = "VENSPT".equals(operacion.IdTipoOperacion) ?  operacion.MontoMonedaSecundaria : operacion.MontoMonedaPrincipal.multiply(entrada.p_TipoCambioCierre, MathContext.DECIMAL128);
			inOperacion.TipoCambioCierre = entrada.p_TipoCambioCierre;
			inOperacion.TipoCambioCierreForward = operacion.TipoCambioCierreForward;
			inOperacion.ParidadCierre = operacion.ParidadCierre;
			inOperacion.ParidadCierreForward = operacion.ParidadCierreForward;
			
			//Logs
			inOperacion.TipoCambioCierreAntiguo = operacion.TipoCambioCierre != null ? operacion.TipoCambioCierre: BigDecimal.ZERO;
			inOperacion.TipoCambioCostoAntiguo = operacion.TipoCambioCosto != null ? operacion.TipoCambioCosto: BigDecimal.ZERO;
			

			inOperacion.TipoCambioCosto = entrada.p_TipoCambioCosto;
			
			inOperacion.ParidadCosto = operacion.ParidadCosto;
			inOperacion.MontoEquivalente = operacion.MontoEquivalente;
			inOperacion.MontoRentabilidad = operacion.MontoRentabilidad;
			inOperacion.MontoRentabilidadCanal = operacion.MontoRentabilidadCanal;
			
			

			if ("COMSPT".equalsIgnoreCase(operacion.IdTipoOperacion) && "CLP".equalsIgnoreCase(operacion.IdMonedaSecundaria) ) 
			{
				inOperacion.Margen = (entrada.p_TipoCambioCosto.subtract(entrada.p_TipoCambioCierre)).multiply(inOperacion.MontoMonedaPrincipal);
			}
			else if ("COMSPT".equalsIgnoreCase(operacion.IdTipoOperacion) && !("CLP".equalsIgnoreCase(operacion.IdMonedaSecundaria))) 
			{
				inOperacion.Margen = (operacion.ParidadCosto.subtract(operacion.ParidadCierre)).multiply(inOperacion.MontoMonedaPrincipal);
			}
			else if ("VENSPT".equalsIgnoreCase(operacion.IdTipoOperacion) && "CLP".equalsIgnoreCase(operacion.IdMonedaSecundaria)) 
			{
				inOperacion.Margen = (entrada.p_TipoCambioCierre.subtract(entrada.p_TipoCambioCosto)).multiply(inOperacion.MontoMonedaPrincipal);
			}
			else 
			{
				inOperacion.Margen = (operacion.ParidadCierre.subtract(operacion.ParidadCosto)).multiply(inOperacion.MontoMonedaPrincipal);
			}

			
			
			
			inOperacion.Utilidad = inOperacion.Margen;
			inOperacion.PuntaCompra = operacion.PuntaCompra;
			inOperacion.PuntaVenta = operacion.PuntaVenta;
			inOperacion.SolicitudTipoCambio = operacion.SolicitudTipoCambio;
			inOperacion.IdMesa = operacion.IdMesa;
			inOperacion.IdModulo = operacion.IdModulo;
			inOperacion.IdTipoDolar = operacion.IdTipoDolar;
			inOperacion.TipoComprobanteFacturacion = operacion.TipoComprobanteFacturacion;
			inOperacion.IdAgente = operacion.IdAgente;
			inOperacion.BenOur = operacion.BenOur;
			inOperacion.EquivalenteConGasto = operacion.EquivalenteConGasto;
			inOperacion.TipoCambioCierreConGasto = operacion.TipoCambioCierreConGasto;
			inOperacion.Observacion = operacion.Observacion;
			inOperacion.CodigoComercio = operacion.CodigoComercio;
			inOperacion.FolioPlanilla = operacion.FolioPlanilla;
			inOperacion.FolioInterfaz = operacion.FolioInterfaz;
			inOperacion.OperacionFicticia = operacion.OperacionFicticia;
			inOperacion.IdTipoCanalIngreso = operacion.IdTipoCanalIngreso;
			inOperacion.IdDatatec = operacion.IdDatatec;
			inOperacion.Estado = "VIG";
			inOperacion.IdSolicitudAporteRescate=operacion.IdSolicitudAporteRescate;
			return inOperacion;
		}).collect(Collectors.toList());
		
		HashMap<String,Object> hOperaciones = Utils.List2Hash(entradaModificarOperacionSpot.p_DatosOperacion, "NumeroOperacion");
		
		AtomicLong index = new AtomicLong();
		entradaModificarOperacionSpot.p_DatosFormaPago = formasPago.stream().map(formaPago -> {
			OperacionObjIn inOperacion = (OperacionObjIn) hOperaciones.get(String.valueOf(formaPago.IdOperacion));
			FormaPagoObjIn inFormaPago = new FormaPagoObjIn();
			inFormaPago.IdInternoPagoOperacion = index.incrementAndGet();
			inFormaPago.IdComprobantePagoOperacion = 0L;
			inFormaPago.IdTipoMovimiento = formaPago.IdTipoMovimiento;
			inFormaPago.IdFormaDePago = formaPago.IdFormaDePago;
			inFormaPago.IdMoneda = formaPago.IdMoneda;
			inFormaPago.IdMonedaLiquidacion = formaPago.IdMonedaLiquidacion;
			inFormaPago.IdOperacion = formaPago.IdOperacion;
			inFormaPago.RecibePagaPortafolio = formaPago.RecibePagaPortafolio;
			inFormaPago.MontoFormaDePago = inFormaPago.IdMoneda.equals(inOperacion.IdMonedaSecundaria) ? inOperacion.MontoMonedaSecundaria : inOperacion.MontoMonedaPrincipal;
			inFormaPago.AfectarTesoreria = formaPago.AfectarTesoreria;
			inFormaPago.FechaLiquidacion = formaPago.FechaLiquidacion;
			return inFormaPago;
		}).collect(Collectors.toList());
		
		ModificarOperacionSpotSalida salidaModificarSpot = modificarOperacionSpot.logica(con, entradaModificarOperacionSpot);
		
		out.P_OUT_ERRORESCONTADOR += MensajeSalida.getNumeroErrores(salidaModificarSpot.P_OUT_MENSAJES);
		List<MensajeSalida> mensajesErroresAdvertencias = salidaModificarSpot.P_OUT_MENSAJES.stream()
				.filter(mensaje -> MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo) || MensajeSalida.MENSAJE_AUTORIZACION.equals(mensaje.tipo))
				.collect(Collectors.toList());
		if(mensajesErroresAdvertencias != null && mensajesErroresAdvertencias.size() == 0) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_CAMBIO_INGRESO_EXITO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
		}else {
			out.P_OUT_MENSAJES = mensajesErroresAdvertencias;
		}
		
		
		/***** WEBSOCKET REFRESCO AUTOMATICO GRILLA MOVIMIENTOS *****/
		AgregarMensajeWebsocket.agregar(con, entrada.p_IdEmpresa, "ACTSPOT", "Refresco Movimientos Spot");
		
		
		logger.info("FIN :: SPOT.ModificarSpotFondoMutuo");
		
		return out;
	}
	
	
	
	
	// -----------------------------------------------------------
	// METODO PRUEBAS LOCALES 
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPRESURAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
						
			//String jsonDesa = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":15,\"p_IdModulo\":\"SPT\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaOperacion\":\"2019-12-11\",\"p_TipoCambioCierre\":790,\"p_TipoOperacion\":\"COMFWD\"}";
			String jsonDesa="{\"p_IdEmpresa\":1,\"p_IdUsuario\":15,\"p_IdModulo\":\"SPT\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaOperacion\":\"2019-12-13\",\"p_TipoCambioCierre\":750,\"p_TipoOperacion\":\"VENFWD\"}";
			ModificarSpotFondoMutuoEntrada Objin = gson.fromJson(jsonDesa, ModificarSpotFondoMutuoEntrada.class);
			
			ModificarSpotFondoMutuo demo = new ModificarSpotFondoMutuo();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				ModificarSpotFondoMutuoSalida ObjOut = demo.logica(con, Objin);
 				con.commit();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
 			//con.commit();
			finally {
				con.close();
			}
	    }	
		System.out.println("== END ==");
	}
}