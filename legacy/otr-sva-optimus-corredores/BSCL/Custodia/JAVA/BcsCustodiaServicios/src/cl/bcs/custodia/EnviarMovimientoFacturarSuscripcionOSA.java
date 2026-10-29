package cl.bcs.custodia;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.IngresarGarantiaJava.IngresarGarantiaEntrada;
import cl.bcs.custodia.IngresarGarantiaJava.IngresarGarantiaSalida;
import cl.bcs.custodia.IngresarGarantiaJava.ObjGarantiaCustodia;
import cl.bcs.facturacion.CrearMovimientoComprobante;
import cl.bcs.facturacion.CrearMovimientoComprobante.CrearMovimientoComprobante_Salida;
import cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobante_Entrada;
import cl.bcs.plataforma.ins.ConsultarDatosInstrumentos;
import cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.ConsultaInstrumento;
import cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.ConsultarDatosInstrumento_Entrada;
import cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.ConsultarDatosInstrumento_Salida;
import cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.DatosInstrumento;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EnviarMovimientoFacturarSuscripcionOSA implements JavaCall{
	
	private static final Logger logger = LogManager.getLogger(EnviarMovimientoFacturarSuscripcionOSA.class);
	private final String SQL_SELECT_EVENTO_CAPITAL_CLIENTE = loadResourceToString("cl/bcs/custodia/sql/EnviarMovimientoFacturarSuscripcionOSA_SelectEventoCapitalClientes.sql");
	private final String SQL_UPDATE_EVENTO_CAPITAL_CLIENTE = loadResourceToString("cl/bcs/custodia/sql/EnviarMovimientoFacturarSuscripcionOSA_UpdateEventoCapitalClientes.sql");
	private final String SQL_SELECT_EVENTO_CAPITAL_FPAGO = loadResourceToString("cl/bcs/custodia/sql/EnviarMovimientoFacturarSuscripcionOSA_SelectEventoCapitalClientesFormaPago.sql");

	public static class EnviarMovimientoFacturarSuscripcionOSA_Entrada{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public Long p_IdEventoCapitalCliente;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EnviarMovimientoFacturarSuscripcionOSA_Salida{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida>	P_OUT_MENSAJES;
		
		public EnviarMovimientoFacturarSuscripcionOSA_Salida(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class UpdateEventoCapitalCliente{
		public Long IdEventoCapitalCliente;
		public Long IdComprobanteFac;
	}
	
	public static class EventoCapitalClienteFac{
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdInstrumento;
		public Long IdInstrumentoRelacionado;
		public String IdMoneda;
		public String IdBoveda;
		public String IdBolsa;
		public String IdEmisor;
		public BigDecimal CantidadNueva;
		public BigDecimal Precio;
		public String OperaCuentaInversion;
		public String IdPreferenciaPago;
		public String EsPreferenciaPagoPorDefecto;
		public String IdEstadoPreferenciaPago;
		public Long IdEventoCapitalCliente;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EventoCapitalClienteFormaPago{
	
		public Long IdEventoCapitalClienteFormaDePago;
		public Long IdEventoCapitalCliente;
		public String IdTipoMovimiento;
		public String IdModalidadPago;
		public String IdMedioDePago;
		public String NombreMedioDePago;
		public Long IdBanco;
		public String NombreBanco;
		public String IdTipoCuentaBancaria;
		public String NombreTipoCuentaBancaria;
		public Long IdCuentaBancaria;
		public String NombreCuentaBancaria;
		public String NumeroCuentaBancaria;
		public String IdTipoTarjetaBancaria;
		public String NumeroTarjetaBancaria;
		public String NumeroMedioDePago;
		public String Sucursal;
		public String FechaVctoDocumento;
		public BigDecimal Monto;
		public String FechaHoraIngresoRegistro;
		public Long IdUsuarioIngreso;
		public String IdEstado;
		public Long IdPreferenciaPagoPortafolio;
		public String IdMoneda;
		public Long IdTarjetaBancaria;
	
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public EnviarMovimientoFacturarSuscripcionOSA_Salida logica(Connection con, EnviarMovimientoFacturarSuscripcionOSA_Entrada entrada) throws Exception {
		logger.debug("INICIO::EnviarMovimientoFacturarSuscripcionOSA");
		EnviarMovimientoFacturarSuscripcionOSA_Salida out = new EnviarMovimientoFacturarSuscripcionOSA_Salida();
		SQL sql=new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEventoCapitalCliente", entrada.p_IdEventoCapitalCliente);
		String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String idMonedaLocal = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		List<EventoCapitalClienteFac> eventoCapitalClientes = sql.query(con, SQL_SELECT_EVENTO_CAPITAL_CLIENTE, params, EventoCapitalClienteFac.class);
		BigDecimal MontoLiquidacionFormaPago = BigDecimal.ZERO; 
		CrearMovimientoComprobante.CrearMovimientoComprobante_Entrada entradaCrearMovimientoComprobante = new CrearMovimientoComprobante.CrearMovimientoComprobante_Entrada();
		entradaCrearMovimientoComprobante.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearMovimientoComprobante.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearMovimientoComprobante.p_IdModulo = entrada.p_IdModulo;
		entradaCrearMovimientoComprobante.p_simularOperacion = "N";
		entradaCrearMovimientoComprobante.p_UsaReglasNegocio = "S";
		entradaCrearMovimientoComprobante.p_EsIngresoAutomatico = "N";
		int index = 0;
		
		CalcularParidad calcularParidad = new CalcularParidad();
		CalcularParidad.CalcularParidadEntrada calcularParidadEntrada = new CalcularParidad.CalcularParidadEntrada();
		Long idTrabajo = new Double(Math.random()*10000000000L).longValue();
		calcularParidadEntrada.p_IdTrabajo = idTrabajo;
		calcularParidadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;

		calcularParidadEntrada.p_CalculosEntrada = eventoCapitalClientes.stream().map(evt -> {
			CalcularParidad.CalculoEntrada calculoEntrada = new CalcularParidad.CalculoEntrada();
			calculoEntrada.IdTrabajo = idTrabajo;
			calculoEntrada.IdInterno = evt.IdEventoCapitalCliente;
			calculoEntrada.Fecha = fechaSistema;
			calculoEntrada.IdMonedaOrigen = evt.IdMoneda;
			calculoEntrada.IdMonedaDestino = idMonedaLocal;
			calculoEntrada.MontoOrigen = evt.CantidadNueva.multiply(evt.Precio, MathContext.DECIMAL128).setScale(0, BigDecimal.ROUND_UP);
			calculoEntrada.IdUso = "";
			return calculoEntrada;
		}).collect(Collectors.toList());
		
		List<CalcularParidad.CalculoSalida> calculoSalida = calcularParidad.logica(con, calcularParidadEntrada).p_out_CalculosSalida;
		
		out.P_OUT_MENSAJES.addAll(calculoSalida.stream()
				.filter(calculo -> calculo.Fecha.after(calculo.FechaCambioDestino)).map(calculo -> {
					HashMap<String, String> parametros = new HashMap<String, String>(1);
					parametros.put("IdParametroFinanciero", calculo.IdMonedaOrigen);
					MensajeSalida mensajeSalida = null;
					try {
						mensajeSalida = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
								"UI_PARAMETRO_FINANCIERO_NO_EXISTE", parametros, con, entrada.p_IdEmpresa);
					} catch (Exception e) {
						logger.error("ERROR::EnviarMovimientoFacturarSuscripcionOSA:: error al crear mensaje de salida");
					}
					return mensajeSalida;
				}).collect(Collectors.toList()));
		
		if(out.P_OUT_MENSAJES.size() > 0) {
			return out;
		}
		
		HashMap<Long, Object> HashCalculoSalida = Utils.List2Hash(calcularParidad.logica(con, calcularParidadEntrada).p_out_CalculosSalida, "IdInterno");
		
		
		for(EventoCapitalClienteFac ecc : eventoCapitalClientes){
			CrearMovimientoComprobante.MovimientoComprobante_Entrada mov = new CrearMovimientoComprobante.MovimientoComprobante_Entrada();
			CalcularParidad.CalculoSalida calculo = (CalculoSalida) HashCalculoSalida.get(ecc.IdEventoCapitalCliente);
			mov.NumeroMvtoCmbte = new Long(index);
			mov.IdEmpresa = entrada.p_IdEmpresa;
			mov.IdCliente = ecc.IdCliente;
			mov.IdPortafolio = ecc.IdPortafolio;
			mov.IdInstrumento = ecc.IdInstrumento;
			mov.IdInstrumentoRelacionado = ecc.IdInstrumentoRelacionado;
			mov.IdMoneda = ecc.IdMoneda;
			mov.IdMonedaLiquidacion = ecc.IdMoneda;
			mov.IdBoveda = ecc.IdBoveda;
			mov.IdBolsa = ecc.IdBolsa;
			mov.IdEmisor = ecc.IdEmisor;
			mov.Cantidad = ecc.CantidadNueva;
			mov.Precio = ecc.Precio;
			mov.Monto = calculo.MontoOrigen;
			mov.MontoMovimientoComprobante = calculo.MontoLocal.setScale(0, BigDecimal.ROUND_HALF_UP);
			mov.MontoLiquidacion = calculo.MontoOrigen;
			MontoLiquidacionFormaPago= calculo.MontoLocal.setScale(0, BigDecimal.ROUND_HALF_UP);
			mov.FechaLiquidacion = fechaSistema;
			mov.FechaOrden = fechaSistema;
			mov.FechaTransaccion = fechaSistema;
			mov.FechaMovimientoComprobante = fechaSistema;
			mov.FechaEmision = fechaSistema;
			mov.IdUsuario = entrada.p_IdUsuario;
			mov.IdConcepto = "SUECOS";
			mov.IdTipoProducto = "PE".equalsIgnoreCase(localizacionSistema) ? "INRV" : "INAC"; 
			mov.IdTipoComprobante = "PE".equalsIgnoreCase(localizacionSistema) ? "POL" : obtenerTipoComprobante(con, entrada.p_IdEmpresa, "FEE");
			mov.IdTipoOperacion = "SUSOSA";
			mov.IdCondicionLiquidacion = "T0";
			mov.IdTipoReajuste = "";
			mov.IdTipoConvenio = "SCV";
			mov.IdFormaCustodia = "CUS";
			mov.IdTipoCanalIngreso = "AGE";
			mov.EsComprobIndividual= "N";
			mov.EsCuentaPropia= "N";
			mov.EsVentaCorta= "N";
			mov.EsPrecioPrima= "N";
			mov.EsEnRueda= "N";
			mov.EsRuteo= "N";
			mov.EsAPlazo= "N";
			mov.EsFungible= "N";
			mov.ActualizarCustodia= "S";
			mov.ActualizarCuentaInversion="S";
			if (	
				 	!"INGSUS_TRA_CLP".equalsIgnoreCase(ecc.IdPreferenciaPago)
				&& 	!"S".equalsIgnoreCase(ecc.EsPreferenciaPagoPorDefecto)
				&& 	!"TRA".equalsIgnoreCase(ecc.IdEstadoPreferenciaPago)
				&& "S".equalsIgnoreCase(ecc.OperaCuentaInversion) ) {
				
				mov.ActualizarTesoreria= "N";
			}
			else {

				mov.ActualizarTesoreria= "S";
			}
			
			mov.ActualizarCustodiaRelacionado = "N";
			mov.CrearComprobanteAutomatico = "S";
			mov.SecuenciaAgrupacion = "";
			mov.ModPrefFact = "";
			mov.IdEmpleadoDigitador = 0L;
			mov.IdInstrumentoSubyacente = 0L;
			mov.IdMonedaSubyacente = "";
			mov.IdCorredoraContraparte = 0L;
			mov.IdOrden = 0L;
			mov.IdAsignacion = 0L;
			mov.IdAsignacionRef = 0L;
			mov.IdTransaccion = 0L;
			mov.FolioTransaccion = "";
			mov.FechaVencimiento = "0001-01-01";
			mov.PrecioAPlazo = BigDecimal.ZERO;
			mov.MontoAPlazo = BigDecimal.ZERO;
			mov.FechaParidad = "0001-01-01";
			mov.ValorParidad = BigDecimal.ZERO;
			mov.FechaTipoCambio = "0001-01-01";
			mov.ValorTipoCambio = BigDecimal.ZERO;
			mov.ValorCaptacionMonedaOrigen = BigDecimal.ZERO;
			mov.Tasa = BigDecimal.ZERO;
			mov.MontoDescuento = BigDecimal.ZERO;
			mov.MontoInteres = BigDecimal.ZERO;
			mov.Glosa = "";
			mov.ValorRescate = BigDecimal.ZERO;
			mov.TasaAgente = BigDecimal.ZERO;
			mov.Spread = BigDecimal.ZERO;
			mov.DiasPlazo = BigDecimal.ZERO;
			mov.FolioCartera = 0L;
			mov.TasaEmision = BigDecimal.ZERO;
			mov.IdOperacion = entrada.p_IdEventoCapitalCliente;
			index++;
			entradaCrearMovimientoComprobante.p_Movimientos.add(mov);
		}
		
		HashMap<String,Object> param = new HashMap<String,Object>();
		param.put("IdEventoCapitalCliente", entrada.p_IdEventoCapitalCliente);
		
//FORMAS DE PAGOS		
		List<EventoCapitalClienteFormaPago> eventoCapitalClientesFormaPago = sql.query(con, SQL_SELECT_EVENTO_CAPITAL_FPAGO, param, EventoCapitalClienteFormaPago.class);
		for(EventoCapitalClienteFormaPago eccfp : eventoCapitalClientesFormaPago){
			CrearMovimientoComprobante.MovimientoComprobanteFormaDePago_Entrada fp = new CrearMovimientoComprobante.MovimientoComprobanteFormaDePago_Entrada();
			fp.NumeroMvtoCmbte = new Long(index);
			fp.AfectarCuentaInversion = "S";
			fp.AfectarTesoreria = "";
			fp.FechaDocumentoPago= "0001-01-01";
			fp.FechaVctoTarjetaBancaria=  "0001-01-01";
			fp.IdBanco= eccfp.IdBanco;
			fp.IdCuentaBancaria=eccfp.IdCuentaBancaria;
			fp.IdMedioDePago= eccfp.IdMedioDePago;
			fp.IdModalidadDePago = eccfp.IdModalidadPago;
			fp.IdMoneda= eccfp.IdMoneda;
			fp.IdTarjetaBancaria=  eccfp.IdTarjetaBancaria;
			fp.MontoLiquidacionFormaDePago = MontoLiquidacionFormaPago;
			fp.NumeroCuentaBancaria = eccfp.NumeroCuentaBancaria;
			fp.NumeroDocumento = 0L;
			fp.NumeroMvtoCmbte = 0L;
			fp.NumeroMvtoCmbteFormaDePago = 0L;
			fp.NumeroSerie = "";
			fp.NumeroTarjetaBancaria =eccfp.NumeroTarjetaBancaria;
			fp.IdTipoCuentaBancaria = eccfp.IdTipoCuentaBancaria;
			index++;
			entradaCrearMovimientoComprobante.p_MovimientosFormaDePago.add(fp);
		}		
		
		CrearMovimientoComprobante serviceCrearMovimientoComprobante = new CrearMovimientoComprobante();
		CrearMovimientoComprobante_Salida salidaCrearMovimientoComprobante = serviceCrearMovimientoComprobante.logica(con,entradaCrearMovimientoComprobante);
		for (MensajeSalida msj : salidaCrearMovimientoComprobante.P_OUT_MENSAJES) {
			if (msj.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ERROR))
				out.P_OUT_ERRORESCONTADOR++;
			out.P_OUT_MENSAJES.add(msj);
		}
		

	if("PE".equals(localizacionSistema)){
////////////////////////////////////////  INGRESO DE GARANTIAS ///////////////////////////////////////////////////////////////////////		
// INSTRUMENTOS		
		ConsultarDatosInstrumentos obtenerDatosInstrumentos = new ConsultarDatosInstrumentos();
		ConsultarDatosInstrumento_Entrada inObtenerDatosInstrumentos = new ConsultarDatosInstrumento_Entrada();
		
		List<ConsultaInstrumento> instrumentosEntrada = new ArrayList<ConsultaInstrumento>();
		ConsultaInstrumento instruIn = new ConsultaInstrumento();

// GARANTIAS		
		List<ObjGarantiaCustodia> detalleGarantiaCustodia = new ArrayList<ObjGarantiaCustodia>();
		ObjGarantiaCustodia registroInGar = new ObjGarantiaCustodia();
		
		
		for(MovimientoComprobante_Entrada movimiento : entradaCrearMovimientoComprobante.p_Movimientos){
			registroInGar.IdInstrumento 				= movimiento.IdInstrumento;
			registroInGar.IdConcepto 					= "BLOSUS";// BLOQUEO SUSCRIPCION		
			registroInGar.IdMoneda						= movimiento.IdMoneda;
			registroInGar.Cantidad						= movimiento.Cantidad;
			registroInGar.ValorRescate					= movimiento.ValorRescate;
			registroInGar.MontoMonedaInstrumento		= movimiento.Monto;
			registroInGar.MontoMonedaSistema 			= movimiento.Monto;
	 		registroInGar.IdTipoReajuste 				= movimiento.IdTipoReajuste;		
			registroInGar.IdEmisor 						= movimiento.IdEmisor;
			registroInGar.EsFungible 					= movimiento.EsFungible;
			registroInGar.IdCliente 					= movimiento.IdCliente;
			registroInGar.IdPortafolio					= movimiento.IdPortafolio;
			registroInGar.IdBolsa						= movimiento.IdBolsa;
			registroInGar.Precio 						= movimiento.Precio;
			registroInGar.Tasa							= movimiento.Tasa;
			registroInGar.IdContraparteGarantia			= 10184L;	
			registroInGar.IdMonedaEmisionInstrumento	= "";
			registroInGar.IdOperacion					= 0L;
			registroInGar.IdTipoOperacion				= "INGGTA";
			registroInGar.PlazoEnDias					= 0L;
			registroInGar.IdOperacion					= 0L;
			registroInGar.FechaEmisionInstrumento		= "0001-01-01";
			registroInGar.IdAgente						= 310L;
			registroInGar.IdTipoComprobante 			= "CUS";
			registroInGar.FolioCarteraGarantizada		= movimiento.FolioCartera;
		}
		
		instruIn.Correlativo = 1L;
		instruIn.IdInstrumento = entradaCrearMovimientoComprobante.p_Movimientos.get(0).IdInstrumento;
		instruIn.Nemotecnico = null;
		instrumentosEntrada.add(instruIn);
		
		inObtenerDatosInstrumentos.p_IdEmpresa = entrada.p_IdEmpresa;
		inObtenerDatosInstrumentos.p_IdModulo = entrada.p_IdModulo;
		inObtenerDatosInstrumentos.p_IdUsuario = entrada.p_IdUsuario;
		inObtenerDatosInstrumentos.p_CreaOpcionAutomatica = "N";
		inObtenerDatosInstrumentos.p_Instrumentos.addAll(instrumentosEntrada);
		
		
		ConsultarDatosInstrumento_Salida outDatosInstrumentos = obtenerDatosInstrumentos.logica(con, inObtenerDatosInstrumentos);
		
// INSTRUMENTOS		
		for (DatosInstrumento recorreIns : outDatosInstrumentos.P_OUT_INSTRUMENTOS){
				registroInGar.FechaVencimientoInstrumento		= recorreIns.FechaVencimiento.toString();
				registroInGar.NemoSVS 							= ""; // SOLO PARA IF
				registroInGar.IdClasificacionTipoInstrumento	= recorreIns.IdClasificacionTipoInstrumento;
				registroInGar.IdTipoInstrumento					= recorreIns.IdTipoInstrumento;
				registroInGar.IdInstrumentoGarantizado			= entradaCrearMovimientoComprobante.p_Movimientos.get(0).IdInstrumento;
				break;
		}		

		
		detalleGarantiaCustodia.add(registroInGar);
		
		
		
		IngresarGarantiaJava  	Garantia 		= new IngresarGarantiaJava();
		IngresarGarantiaEntrada IngresoGarantia = new IngresarGarantiaEntrada();		
		
		IngresoGarantia.p_IdEmpresa 		= entrada.p_IdEmpresa;
		IngresoGarantia.p_IdUsuario			= entrada.p_IdUsuario;
		IngresoGarantia.p_IdModulo			= entrada.p_IdModulo;
		IngresoGarantia.p_IdEmpleado		= 168L;
		IngresoGarantia.p_UsaReglasNegocio	= "N";
		IngresoGarantia.p_SimulaOperacion	= "N";
		IngresoGarantia.p_Certifica 		= "N";
		IngresoGarantia.p_FechaMovimiento	= fechaSistema;		
		IngresoGarantia.p_GarantiaCustodia.addAll(detalleGarantiaCustodia);


		System.out.println(IngresoGarantia);
		
		IngresarGarantiaSalida GarantiaOut = Garantia.logica(con, IngresoGarantia);
	}
////////////////////////////////////////INGRESO DE GARANTIAS ///////////////////////////////////////////////////////////////////////		
		
		List<UpdateEventoCapitalCliente> upds = new ArrayList<UpdateEventoCapitalCliente>();
		UpdateEventoCapitalCliente upd = new UpdateEventoCapitalCliente();
		upd.IdEventoCapitalCliente = entrada.p_IdEventoCapitalCliente;		
		// Se obtiene el IdComprobanteFacturacion para relacionarlo al EventoCapitalCliente.
		for(MensajeSalida msj : out.P_OUT_MENSAJES) {			
			if(msj.codigo.equalsIgnoreCase("UI_COMPROBANTE_CREADO")) {
				upd.IdComprobanteFac = msj.id;
			}
		}
		upds.add(upd);
		sql.batch(con, SQL_UPDATE_EVENTO_CAPITAL_CLIENTE, upds, UpdateEventoCapitalCliente.class);
		
		logger.debug("FIN::EnviarMovimientoFacturarSuscripcionOSA");
		return out;
	}
	
	public String obtenerTipoComprobante(Connection con, Long idEmpresa, String tipoComprobantePorDefecto) {
		String tipoComprobante = tipoComprobantePorDefecto;
		try {
			tipoComprobante = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "CUSTODIA", "TipoFacturaDefectoEventoCapital");
		} catch(Exception e) {
			logger.error("ERROR::EnviarMovimientoFacturarSuscripcionOSA:: no se ecuentra parametro TipoFacturaDefectoAsignaciones");
		}
		return tipoComprobante;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		EnviarMovimientoFacturarSuscripcionOSA_Entrada in=gson.fromJson(jin.toString() ,EnviarMovimientoFacturarSuscripcionOSA_Entrada.class);
		EnviarMovimientoFacturarSuscripcionOSA_Salida out= logica(conn,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getBCOESTADOConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":106138,\"p_IdModulo\":\"CUS\",\"p_IdEventoCapitalCliente\":149541}";
			EnviarMovimientoFacturarSuscripcionOSA_Entrada Objin = gson.fromJson(json, EnviarMovimientoFacturarSuscripcionOSA_Entrada.class);
			
			EnviarMovimientoFacturarSuscripcionOSA demo=new EnviarMovimientoFacturarSuscripcionOSA();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				EnviarMovimientoFacturarSuscripcionOSA_Salida ObjOut= demo.logica(con, Objin);
// 				con.commit();
 				con.rollback();
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
