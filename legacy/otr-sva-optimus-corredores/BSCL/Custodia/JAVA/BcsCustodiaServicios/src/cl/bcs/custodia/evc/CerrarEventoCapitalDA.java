package cl.bcs.custodia.evc;
/**** esta clase fue movida a evc, desde la raíz del directorio custodia
 *  ACespedes 19-11-2018
 */


import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.evc.NotificarEventoCapital.NotificarEventoCapital_Entrada;
import cl.bcs.custodia.evc.NotificarEventoCapital.NotificarEventoCapital_Salida;
import cl.bcs.custodia.evc.NotificarEventoCapital.ObjEventoCapital_Entrada;
import cl.bcs.facturacion.CrearMovimientoComprobante;
import cl.bcs.facturacion.CrearMovimientoComprobante.Comprobante_Salida;
import cl.bcs.facturacion.CrearMovimientoComprobante.CrearMovimientoComprobante_Salida;
import cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobante_Salida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.tas.EntregarPreciosTasas;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Entrada;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Entrada_DatosInstrumentos;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida_PreciosTasas;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CerrarEventoCapitalDA implements JavaCall{
	
	private final Log log = new Log("CUS.CerrarEventoCapitalDA");
	
	private final String SQL_SELECT_EVENTO_CAPITAL = loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalDA_SelectEventoCapital.sql");
	private final String SQL_SELECT_EVENTO_CAPITAL_CLIENTE = loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalDA_SelectEventoCapitalCliente.sql");
	private final String SQL_SELECT_EVENTO_CAPITAL_CLIENTE_PENDIENTE = loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalDA_SelectEventoCapitalClientePendiente.sql");
	private final String SQL_UPDATE_EVENTO_CAPITAL = loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalDA_UpdateEventoCapital.sql");
	private final String SQL_UPDATE_EVENTO_CAPITAL_CLIENTE = loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalDA_UpdateEventoCapitalCliente.sql");
	
	public static class EntradaCerrarEventoCapitalDA{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public Long p_IdEventoCapital;
		public String p_IdTipoEventoCapital;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCerrarEventoCapitalDA{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaCerrarEventoCapitalDA(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class EventoCapital{
		public Long IdEventoCapital;
		public Date FechaPago;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public Long IdInstrumento;
		public String Nemo;
		public String IdMoneda;
		public String IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EventoCapitalCliente{
		public Long IdEventoCapital;
		public Long IdEventoCapitalCliente;
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdInstrumento;
		public String IdMoneda;
		public String TipoCartera;
		public String IdBoveda;
		public String IdBolsa;
		public String IdEmisor;
		public BigDecimal CantidadNueva;
		public String Nemo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ActualizarEventoCapitalCliente {
		public Long IdEventoCapitalCliente;
		public Long IdEventoCapital;
		public Long IdMovimientoComprobanteFac;
		public Long IdComprobanteFac;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaCerrarEventoCapitalDA jin = gson.fromJson(in.toString(), EntradaCerrarEventoCapitalDA.class);
		SalidaCerrarEventoCapitalDA out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	//TODO Lógica
	public SalidaCerrarEventoCapitalDA logica(Connection con, EntradaCerrarEventoCapitalDA entrada) throws Exception{
		log.write("INICIO:: CerrarEventoCapitalDA");
		SalidaCerrarEventoCapitalDA out = new SalidaCerrarEventoCapitalDA();
		SQL sql=new SQL();
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEventoCapital", entrada.p_IdEventoCapital);
		List<EventoCapital> eventoCapital = sql.query(con, SQL_SELECT_EVENTO_CAPITAL, params, EventoCapital.class);
		params.clear();
		
		if(eventoCapital.size() == 0){
			out.P_OUT_ERRORESCONTADOR += 1;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_EVENTO_VIGENTE_CERRAR",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			return out;
		}
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(con, entrada.p_IdEmpresa);
		Long idPaisEmpresa = empresa.IdPais;
		
		EntregarPreciosTasas entregarPreciosTasas = new EntregarPreciosTasas();
		EntregarPreciosTasas_Entrada entradaEntregarPreciosTasas = new EntregarPreciosTasas_Entrada();
		
		entradaEntregarPreciosTasas.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaEntregarPreciosTasas.p_IdUsuario = entrada.p_IdUsuario;
		entradaEntregarPreciosTasas.p_IdModulo = entrada.p_IdModulo;
		entradaEntregarPreciosTasas.p_IdPais = idPaisEmpresa;
		
		
		for(EventoCapital evento : eventoCapital){
			EntregarPreciosTasas_Entrada_DatosInstrumentos tasa = new EntregarPreciosTasas_Entrada_DatosInstrumentos();
			tasa.IdRegistro = evento.IdEventoCapital;
			tasa.TipoCartera = "0";
			tasa.IdUso = "CARTERA";
			tasa.IdTipoCriterioTasa = "0";
			tasa.IdClasificacionTipoInstrumento = evento.IdClasificacionTipoInstrumento;
			tasa.IdTipoInstrumento = evento.IdTipoInstrumento;
			tasa.IdInstrumento = evento.IdInstrumento;
			tasa.Nemotecnico = evento.Nemo;
			tasa.IdMoneda = evento.IdMoneda;
			tasa.FechaConsulta = evento.FechaPago;
			tasa.PlazoResidual = 0L;
			entradaEntregarPreciosTasas.p_DatosInstrumentos.add(tasa);
		}

		EntregarPreciosTasas_Salida salida = entregarPreciosTasas.logica(con, entradaEntregarPreciosTasas);
		
		HashMap<Long,Object> hPrecios = Utils.List2Hash(salida.P_OUT_PRECIOSTASAS, "IdRegistro");
		
		params.put("IdEventoCapital", entrada.p_IdEventoCapital);
		List<EventoCapitalCliente> eventoClienteCapital = sql.query(con, SQL_SELECT_EVENTO_CAPITAL_CLIENTE, params, EventoCapitalCliente.class);
		params.clear();
		
		CrearMovimientoComprobante_Salida salidaCrearMovimientoComprobante = new CrearMovimientoComprobante_Salida();
		salidaCrearMovimientoComprobante.P_OUT_ERRORESCONTADOR = 0L;
		salidaCrearMovimientoComprobante.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		
		Long i = 1L;
		CrearMovimientoComprobante.CrearMovimientoComprobante_Entrada entradaCrearMovimientoComprobante = new CrearMovimientoComprobante.CrearMovimientoComprobante_Entrada();
		for(EventoCapitalCliente ec : eventoClienteCapital){
			EntregarPreciosTasas_Salida_PreciosTasas tasa = (EntregarPreciosTasas_Salida_PreciosTasas) hPrecios.get(ec.IdEventoCapital);
			
			BigDecimal precio = tasa != null && tasa.Precio !=null ? tasa.Precio : BigDecimal.ZERO;
			BigDecimal monto = ec.CantidadNueva.multiply(precio, MathContext.DECIMAL128);
			String fecha = fechaSistema;
			
			CrearMovimientoComprobante.MovimientoComprobante_Entrada mov = new CrearMovimientoComprobante.MovimientoComprobante_Entrada();
			mov.NumeroMvtoCmbte 					= ec.IdEventoCapitalCliente;
			mov.IdEmpresa 							= entrada.p_IdEmpresa;
			mov.IdConcepto 							="GAR".equals(ec.TipoCartera)?"DIACGS":"DIEVAC";
			mov.SecuenciaAgrupacion 				= "";
			mov.IdCliente 							= ec.IdCliente;
			mov.IdPortafolio 						= ec.IdPortafolio;
			mov.ModPrefFact 						= "";
			mov.IdEmpleadoDigitador 				= 0L;
			mov.IdInstrumento 						= ec.IdInstrumento;
			mov.IdInstrumentoRelacionado 			= 0L;
			mov.IdInstrumentoSubyacente 			= 0L;
			mov.IdTipoProducto 						= "INAC";
			mov.IdMoneda 							= ec.IdMoneda;
			mov.IdMonedaSubyacente 					= "";
			mov.IdMonedaLiquidacion 				= ec.IdMoneda;
			mov.IdTipoComprobante 					= "FEE";
			mov.IdTipoOperacion 					= "GAR".equals(ec.TipoCartera) ? "DIVACG" : "NEG".equalsIgnoreCase(ec.TipoCartera) ? "DVACCN" : "DIVACC";
			mov.IdBoveda 							= ec.IdBoveda;
			mov.IdCorredoraContraparte 				= 0L;
			mov.IdCondicionLiquidacion 				= "T0";
			mov.FechaLiquidacion 					= fecha;
			mov.IdBolsa 							= ec.IdBolsa;
			mov.IdTipoReajuste 						= "CLP";
			mov.IdTipoConvenio 						= "SCV";
			mov.IdFormaCustodia 					= "CUS";
			mov.IdEmisor 							= ec.IdEmisor;
			mov.IdOrden 							= 0L;
			mov.IdAsignacion 						= 0L;
			mov.IdAsignacionRef 					= 0L;
			mov.IdTipoCanalIngreso 					= "SIS";
			mov.FechaOrden 							= fechaSistema;
			mov.IdTransaccion 						= 0L;
			mov.FolioTransaccion 					= "";
			mov.FechaTransaccion 					= fecha;
			mov.FechaVencimiento 					= "0001-01-01";
			mov.MontoAPlazo 						= BigDecimal.ZERO;
			mov.FechaParidad 						= "0001-01-01";
			mov.ValorParidad 						= BigDecimal.ZERO;
			mov.FechaTipoCambio 					= "0001-01-01";
			mov.ValorTipoCambio 					= BigDecimal.ZERO;
			mov.ValorCaptacionMonedaOrigen 			= BigDecimal.ZERO;
			mov.Tasa 								= BigDecimal.ZERO;
			mov.Cantidad 							= ("NEG".equals(ec.TipoCartera)) ? ec.CantidadNueva.abs() : ec.CantidadNueva;
			mov.Precio								= BigDecimal.ZERO;
			mov.Monto 								= BigDecimal.ZERO;
			mov.MontoMovimientoComprobante 			= BigDecimal.ZERO;
			mov.MontoLiquidacion 					= BigDecimal.ZERO;
			mov.MontoDescuento 						= BigDecimal.ZERO;
			mov.MontoInteres 						= BigDecimal.ZERO;
			mov.Glosa 								= "Div Acc N°: " + ec.IdEventoCapital + " " + ec.Nemo + " Fecha:" + fecha;
			mov.ValorRescate 						= BigDecimal.ZERO;
			mov.TasaAgente 							= BigDecimal.ZERO;
			mov.Spread 								= BigDecimal.ZERO;
			mov.DiasPlazo 							= BigDecimal.ZERO;
			mov.FechaMovimientoComprobante 			= fecha;
			mov.FolioCartera 						= 0L;
			mov.FechaEmision 						= fecha;
			mov.TasaEmision 						= BigDecimal.ZERO;
			mov.EsComprobIndividual 				= "N";
			mov.EsCuentaPropia 						= "N";
			mov.IndicadorOrdenDirecta 				= "";
			mov.EsVentaCorta 						= "N";
			mov.EsPrecioPrima 						= "N";
			mov.EsEnRueda 							= "N";
			mov.EsRuteo 							= "N";
			mov.EsAPlazo 							= "N";
			mov.EsFungible 							= "N";
			mov.ActualizarCustodia 					= "S";
			mov.ActualizarCuentaInversion 			= "N";
			mov.ActualizarTesoreria 				= "N";
			mov.ActualizarCustodiaRelacionado		= "N";
			mov.CrearComprobanteAutomatico 			= "S";
			mov.IdUsuario 							= entrada.p_IdUsuario;
			mov.CodigoIFRS 							= "";
			mov.CodigoBroker 						= "";
			mov.CodigoBancoLiquidador 				= "";
			mov.CodigoBancoCustodio 				= "";
			mov.IdMonedaSpot 						= "";
			mov.IdMovimientoRelacionado 			= 0L;
			mov.IdPersonaBroker 					= 0L;
			mov.IdPersonaBancoCustodio 				= 0L;
			mov.IdPersonaBancoLiquidador 			= 0L;
			mov.MercadoSMV 							= "";
			mov.ModalidadSMV 						= "";
			mov.PrecioAPlazo 						= BigDecimal.ZERO;
			mov.TasaOperacion 						= BigDecimal.ZERO;
			mov.NemotecnicoInstrumento              = ec.Nemo != null ? ec.Nemo : "";
			
			entradaCrearMovimientoComprobante.p_Movimientos.add(mov);
			i++;
		}
		
		if(entradaCrearMovimientoComprobante.p_Movimientos.size() > 0){
			entradaCrearMovimientoComprobante.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaCrearMovimientoComprobante.p_IdUsuario = entrada.p_IdUsuario;
			entradaCrearMovimientoComprobante.p_IdModulo = entrada.p_IdModulo;
			entradaCrearMovimientoComprobante.p_simularOperacion = "N";
			entradaCrearMovimientoComprobante.p_UsaReglasNegocio = "S";
			entradaCrearMovimientoComprobante.p_EsIngresoAutomatico = "N";
			CrearMovimientoComprobante serviceCrearMovimientoComprobante = new CrearMovimientoComprobante();
			salidaCrearMovimientoComprobante = serviceCrearMovimientoComprobante.logica(con,entradaCrearMovimientoComprobante);
			out.P_OUT_ERRORESCONTADOR += salidaCrearMovimientoComprobante.P_OUT_ERRORESCONTADOR == null ? 0 : salidaCrearMovimientoComprobante.P_OUT_ERRORESCONTADOR;
			out.P_OUT_MENSAJES.addAll(salidaCrearMovimientoComprobante.P_OUT_MENSAJES);
		}
		
		params.put("IdEventoCapital", entrada.p_IdEventoCapital);
		List<EventoCapitalCliente> eventoClienteCapitalPendiente = sql.query(con, SQL_SELECT_EVENTO_CAPITAL_CLIENTE_PENDIENTE, params, EventoCapitalCliente.class);
		
		if(eventoClienteCapitalPendiente.size() > 0){
			for(EventoCapital evento : eventoCapital){
				evento.IdEstado = "PEN";
			}
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA,"UI_OPERACION_PENDIENTES",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
		}
		
		if(out.P_OUT_ERRORESCONTADOR.longValue() == 0){
			for(EventoCapital evento : eventoCapital){
				evento.IdEstado = "CER";
			}
			
			HashMap<Long,Object> hMovimientos = Utils.List2Hash(salidaCrearMovimientoComprobante.p_out_Movimientos, "NumeroMvtoCmbte");
			HashMap<Long,Object> hComprobante = Utils.List2Hash(salidaCrearMovimientoComprobante.p_out_Comprobantes, "IdMovimientoComprobante");
			
			List<ActualizarEventoCapitalCliente> actualizarECCliente = eventoClienteCapital.stream().map(data -> {
				ActualizarEventoCapitalCliente ecc = new ActualizarEventoCapitalCliente();
				ecc.IdEventoCapital            = data.IdEventoCapital;
				ecc.IdEventoCapitalCliente     = data.IdEventoCapitalCliente;
				
				MovimientoComprobante_Salida datosMovimiento  = (MovimientoComprobante_Salida) hMovimientos.get(data.IdEventoCapitalCliente);
				Comprobante_Salida           datosFacturacion = (Comprobante_Salida) hComprobante.get(datosMovimiento.IdMovimientoComprobante);
				
				ecc.IdMovimientoComprobanteFac = datosMovimiento.IdMovimientoComprobante;
				ecc.IdComprobanteFac           = datosFacturacion.IdComprobante;
				return ecc;
			}).collect(Collectors.toList());
			
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"UI_OPER_CERRAR_EXITO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			sql.batch(con, SQL_UPDATE_EVENTO_CAPITAL, eventoCapital, EventoCapital.class);
			sql.batch(con, SQL_UPDATE_EVENTO_CAPITAL_CLIENTE, actualizarECCliente, ActualizarEventoCapitalCliente.class);
		}
		
		
		// crear objeto de tipo noticacion
		
		if(out.P_OUT_ERRORESCONTADOR==0) {
			NotificarEventoCapital notificarEventoCierre = new NotificarEventoCapital();	
			NotificarEventoCapital_Entrada NotifIN = new NotificarEventoCapital_Entrada();
			NotifIN.p_IdUsuario = entrada.p_IdUsuario;
			NotifIN.p_IdEmpresa = entrada.p_IdEmpresa;
			NotifIN.p_IdModulo = entrada.p_IdModulo;
			NotifIN.p_EstadoEventoCapital = "CER";
			ObjEventoCapital_Entrada objIdsCapital = new ObjEventoCapital_Entrada();
			objIdsCapital.p_IdEventoCapital= entrada.p_IdEventoCapital;
			NotifIN.p_IdsEventoCapital.add(objIdsCapital);
			
			try {
				NotificarEventoCapital_Salida ObjOut= notificarEventoCierre.logica(con, NotifIN);
				List<MensajeSalida> mensajeAdvertencia = ObjOut.P_OUT_MENSAJES.stream().filter(s -> s.tipo.equalsIgnoreCase("ADVERTENCIA")).collect(Collectors.toList());	
				if(ObjOut.P_OUT_ERRORESCONTADOR>0) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA,"UI_NOTIFICACION_EVENTO_CAPITAL_NO_SE_REALIZO",new HashMap<String,String>(),con,entrada.p_IdEmpresa) );								
				}else if(mensajeAdvertencia.size()>0) {
					out.P_OUT_MENSAJES=ObjOut.P_OUT_MENSAJES.stream().filter(s -> s.tipo.equalsIgnoreCase("ADVERTENCIA")).collect(Collectors.toList());	
				}
			}
			catch(Exception e) {				
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA,"UI_NOTIFICACION_EVENTO_CAPITAL_NO_SE_REALIZO",new HashMap<String,String>(),con,entrada.p_IdEmpresa) );				
			}
		}
		
		
		log.write("FIN:: CerrarEventoCapitalDA");
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
		
		String jsonRF = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CUS\",\"p_IdEventoCapital\":366526,\"p_IdTipoEventoCapital\":\"DA\"}";
		
		EntradaCerrarEventoCapitalDA Objin = gson.fromJson(jsonRF, EntradaCerrarEventoCapitalDA.class);
		CerrarEventoCapitalDA  demo=new CerrarEventoCapitalDA();				
		try {
			SalidaCerrarEventoCapitalDA ObjOut=demo.logica(con, Objin);
			con.commit();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			con.rollback(); 			
    	}
		System.out.println("== END ==");
	}
	
}
