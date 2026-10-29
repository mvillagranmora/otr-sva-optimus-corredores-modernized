package cl.bcs.mesa.SPT;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

import cl.bcs.facturacion.CrearMovimientoComprobante;
import cl.bcs.facturacion.CrearMovimientoComprobante.CrearMovimientoComprobante_Salida;
import cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobante_Entrada;
import cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobante_Salida;
import cl.bcs.mesa.SPT.CrearOperacionSpot.OperacionSpot;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearOperacionSpotFacturacion implements JavaCall{
	private static final Logger logger = LogManager.getLogger(CrearOperacionSpotFacturacion.class);
	
	private final String SQL_UPDATE_OPERACIONES_SPOT_ID_COMPROBANTE_FACTURACION = loadResourceToString("cl/bcs/mesa/SPT/sql/CrearOperacionSpotFacturacion_UpdateIdComprobanteFacturacion.sql");
	
	public static class OperacionSpotFacturacionObjIn{
		public Long IdOperacionSpot;
		public Long IdOperacionSpotOrigen;
		public String IdTipoOperacion;
		public java.sql.Timestamp FechaOperacion;
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdInstrumento;
		public String Nemotecnico;
		public String IdTipoProducto;
		public String IdMonedaPrincipal;
		public String IdMonedaSecundaria;
		public BigDecimal MontoMonedaPrincipal;
		public BigDecimal MontoMonedaSecundaria;
		public BigDecimal TipoCambioCierre;
		public BigDecimal ParidadCierre;
		public String TipoComprobanteFacturacion;
		public String IdBoveda;
		public String IdCondicionLiquidacion;
		public String FechaLiquidacion;
		public String IdTipoConvenio;
		public String MultiplicaODivide;
		public BigDecimal TipoCambioUSD;
		public BigDecimal TipoCambioMonedaSecundaria;
		public String Glosa;
		public Long IdMesa;
		public String CrearComprobanteAutomatico;
		public String IdTipoCanalIngreso; // MGH
		public String Estado;
		public String IdTipoMovimientoFondoMutuo;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class FormaPagoSpotFacturacionObjIn{
		public Long IdOperacion;
		public String IdTipoMovimiento;
		public String IdMedioDePago;
		public String IdModalidadPago;
		public String IdMoneda;
		public BigDecimal Monto;
		public String FechaLiquidacionComprobantePagoOperacion;
		public String AfectarTesoreria;
		public String IdFormaDePago;
		public String IdCondicionDeLiquidacion;
		public Long IdCuentaBancaria = 0L;
		public String IdTipoCuentaBancaria = "";
		public String NumeroCuentaBancaria = "";
        public Long   IdBanco = 0L;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class CrearOperacionSpotFacturacionEntrada{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<OperacionSpotFacturacionObjIn> p_DatosOperacion;
		public List<FormaPagoSpotFacturacionObjIn> p_DatosFormaPago;
		
		public CrearOperacionSpotFacturacionEntrada() {
			this.p_DatosOperacion = new ArrayList<OperacionSpotFacturacionObjIn>();
			this.p_DatosFormaPago = new ArrayList<FormaPagoSpotFacturacionObjIn>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class OperacionSpotFacturacionObjOut{
		public Long IdOperacionSpot;
		public String Estado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CrearOperacionSpotFacturacionSalida {
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;
		public Long						P_OUT_ADVERTENCIASCONTADOR;
		public List<OperacionSpotFacturacionObjOut>	P_OUT_IDOPERACIONSPOT;
		
		public CrearOperacionSpotFacturacionSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_IDOPERACIONSPOT = new ArrayList<OperacionSpotFacturacionObjOut>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		CrearOperacionSpotFacturacionEntrada in = gson.fromJson(jin.toString(), CrearOperacionSpotFacturacionEntrada.class);
		CrearOperacionSpotFacturacionSalida out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		long c = 0;
		for (MensajeSalida s : out.P_OUT_MENSAJES) {
			if (s.tipo.equals("AUTORIZACION"))
				c++;
		}
		out.P_OUT_ADVERTENCIASCONTADOR = c;
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public CrearOperacionSpotFacturacionSalida logica(Connection conn, CrearOperacionSpotFacturacionEntrada in) throws Exception {
		Long init = System.currentTimeMillis();
		logger.info("Inicio servicio CrearOperacionSpotFacturacion");
		
		SQL _sql = new SQL();
		CrearOperacionSpotFacturacionSalida out = new CrearOperacionSpotFacturacionSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		out.P_OUT_ADVERTENCIASCONTADOR = 0L;
		Long Id = 0L;
		HashMap<String,String> mapMsg = new HashMap<>();
		
		try {
			if (in.p_DatosOperacion.size() == 0) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_OPERACIONES",mapMsg,conn,in.p_IdEmpresa));
				Id++;
				return out;
			}
			
			/***** VARIABLES *****/
			String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
			String bolsaDefectoSistema = ConfiguracionSistema.getParametroConfiguracionString(conn,in.p_IdEmpresa, "PLATAFORMA", "PLA.BolsaDefectoSistema");
			long index = 1L;
			
			//Obtiene la secuencia maxima para agregar los arbitrajes en caso que existan
			long vMaxSecuencia = 0L;
			//long vMaxSecuencia = in.p_DatosOperacion.stream().mapToLong(ope -> ope.IdOperacionSpot).max().getAsLong();
			for(OperacionSpotFacturacionObjIn opSp: in.p_DatosOperacion){
				if(opSp.IdOperacionSpot > vMaxSecuencia){
					vMaxSecuencia = opSp.IdOperacionSpot;
				}
			}
			
			
			/***** FACTURACION *****/
			CrearMovimientoComprobante.CrearMovimientoComprobante_Entrada entradaCrearMovimientoComprobante = new CrearMovimientoComprobante.CrearMovimientoComprobante_Entrada();
			entradaCrearMovimientoComprobante.p_IdEmpresa = in.p_IdEmpresa;
			entradaCrearMovimientoComprobante.p_IdUsuario = in.p_IdUsuario;
			entradaCrearMovimientoComprobante.p_IdModulo = in.p_IdModulo;
			entradaCrearMovimientoComprobante.p_simularOperacion = "N";
			entradaCrearMovimientoComprobante.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
			entradaCrearMovimientoComprobante.p_EsIngresoAutomatico = "S";
			
			//Movimientos
			for(OperacionSpotFacturacionObjIn opSp: in.p_DatosOperacion){
				CrearMovimientoComprobante.MovimientoComprobante_Entrada mov = new CrearMovimientoComprobante.MovimientoComprobante_Entrada();
				mov.NumeroMvtoCmbte = opSp.IdOperacionSpot;
				mov.IdEmpresa = in.p_IdEmpresa;
				if(opSp.IdTipoMovimientoFondoMutuo.equals("RESCNN")) {
					mov.IdConcepto = "CMMESP";
				}
				else {
					mov.IdConcepto = opSp.IdTipoOperacion.equals("COMSPT") ? "CMMESP" : "VNMESP";
				}
				mov.SecuenciaAgrupacion = opSp.IdTipoOperacion + "_" + opSp.IdOperacionSpot.toString();
				mov.IdCliente = opSp.IdCliente;
				mov.IdPortafolio = opSp.IdPortafolio;
				mov.ModPrefFact = "";
				mov.IdEmpleadoDigitador = 0L;
				mov.IdInstrumento = opSp.IdInstrumento;
				mov.NemotecnicoInstrumento = opSp.Nemotecnico;
				mov.IdInstrumentoRelacionado = 0L;
				mov.IdInstrumentoSubyacente = 0L;
				mov.IdTipoProducto = opSp.IdTipoProducto;
				//mov.IdMoneda = monedaSistema; Se modifica por el IF que esta a continuacion
				mov.IdMonedaSubyacente = "";
				mov.IdMonedaLiquidacion = monedaSistema;
				mov.IdMonedaSpot= opSp.IdMonedaPrincipal;
				mov.IdTipoComprobante = opSp.TipoComprobanteFacturacion;
				mov.IdTipoOperacion = opSp.IdTipoOperacion;
				mov.IdBoveda = opSp.IdBoveda;
				mov.IdCorredoraContraparte = 0L;
				mov.IdCondicionLiquidacion = opSp.IdCondicionLiquidacion;
				mov.FechaLiquidacion = opSp.FechaLiquidacion;
				mov.IdBolsa = bolsaDefectoSistema;
				mov.IdTipoReajuste = "";
				mov.IdTipoConvenio = opSp.IdTipoConvenio;
				mov.IdFormaCustodia = "";
				mov.IdEmisor = "";
				mov.IdOrden = 0L;
				mov.IdAsignacion = 0L;
				mov.IdAsignacionRef = 0L;
				mov.IdTipoCanalIngreso = opSp.IdTipoCanalIngreso;
				mov.FechaOrden = "0001-01-01";
				mov.IdTransaccion = opSp.IdOperacionSpot;
				mov.FolioTransaccion = opSp.IdOperacionSpotOrigen.toString();
				mov.FechaTransaccion = Fecha.toStringTimeStamp(opSp.FechaOperacion);
				mov.FechaVencimiento = Fecha.toStringDate(opSp.FechaOperacion);
				mov.PrecioAPlazo = BigDecimal.ZERO;
				mov.MontoAPlazo = BigDecimal.ZERO;
				mov.FechaParidad = Fecha.toStringDate(opSp.FechaOperacion);
				mov.ValorParidad = opSp.ParidadCierre;
				//mov.FechaTipoCambio = Fecha.toFormat(opSp.FechaOperacion,"YYYY-MM-DD");
				mov.FechaTipoCambio = Fecha.toStringDate(opSp.FechaOperacion);
				mov.ValorTipoCambio = opSp.TipoCambioCierre;
				mov.ValorCaptacionMonedaOrigen = BigDecimal.ZERO;
				mov.Tasa = BigDecimal.ZERO;
				mov.Cantidad = opSp.MontoMonedaPrincipal;
				if(opSp.Nemotecnico.equals("ARBINT") || opSp.Nemotecnico.equals("ARBDIS")){
					mov.IdMoneda = opSp.IdMonedaPrincipal;//monedaSistema; Se cambia al IdMonedaPrincipal por la Centralizacion
					mov.Precio = opSp.MultiplicaODivide.equals("D") ? opSp.TipoCambioUSD.divide(opSp.ParidadCierre,8,RoundingMode.HALF_UP) : opSp.TipoCambioUSD.multiply(opSp.ParidadCierre);
					//mov.Monto = opSp.MontoMonedaPrincipal.multiply(opSp.TipoCambioMonedaSecundaria).multiply(opSp.ParidadCierre); Se cambia al MontoMonedaPrincipal por la Centralizacion
					mov.Monto = opSp.MontoMonedaPrincipal;
					mov.MontoMovimientoComprobante = opSp.MultiplicaODivide.equals("D") ? opSp.MontoMonedaPrincipal.multiply(opSp.TipoCambioMonedaSecundaria).divide(opSp.ParidadCierre,8,RoundingMode.HALF_UP) : opSp.MontoMonedaPrincipal.multiply(opSp.TipoCambioMonedaSecundaria).multiply(opSp.ParidadCierre);
					//mov.MontoLiquidacion = opSp.MontoMonedaPrincipal.multiply(opSp.TipoCambioMonedaSecundaria).multiply(opSp.ParidadCierre);
					mov.MontoLiquidacion = mov.MontoMovimientoComprobante;
				}
				else{
					mov.IdMoneda = opSp.IdMonedaSecundaria;//monedaSistema; Se cambia al IdMonedaSecundaria por la Centralizacion
					mov.Precio = opSp.TipoCambioCierre;
					mov.Monto = opSp.MontoMonedaSecundaria;
					mov.MontoMovimientoComprobante = opSp.MontoMonedaSecundaria;
					mov.MontoLiquidacion = opSp.MontoMonedaSecundaria;
				}
				mov.MontoDescuento = BigDecimal.ZERO;
				mov.MontoInteres = BigDecimal.ZERO;
				mov.Glosa = opSp.Glosa;
				mov.ValorRescate = BigDecimal.ZERO;
				mov.TasaAgente = BigDecimal.ZERO;
				mov.Spread = BigDecimal.ZERO;
				mov.DiasPlazo = BigDecimal.ZERO;
				mov.FechaMovimientoComprobante = Fecha.toStringTimeStamp(opSp.FechaOperacion);
				mov.IdMesa = opSp.IdMesa;
				mov.FolioCartera = 0L;
				mov.FechaEmision = Fecha.toStringTimeStamp(opSp.FechaOperacion);
				mov.TasaEmision = BigDecimal.ZERO;
				mov.EsComprobIndividual = "S";
				mov.EsCuentaPropia = "N";
				mov.EsCuentaPropia = "N";
				mov.IndicadorOrdenDirecta = "N";
				mov.EsVentaCorta = "N";
				mov.EsPrecioPrima = "N";
				mov.EsEnRueda = "N";
				mov.EsRuteo = "N";
				mov.EsAPlazo = "N";
				mov.EsFungible = "N";
				mov.ActualizarCustodia = "N";
				mov.ActualizarCuentaInversion = "S";
				mov.ActualizarTesoreria = "S";
				mov.ActualizarCustodiaRelacionado = "N";
				mov.CrearComprobanteAutomatico = opSp.CrearComprobanteAutomatico;
				mov.IdUsuario = in.p_IdUsuario;
				entradaCrearMovimientoComprobante.p_Movimientos.add(mov);
				
				//En caso que sea operacion Spot Arbitraje se agrega otro movimiento
				if(opSp.Nemotecnico.equals("ARBINT") || opSp.Nemotecnico.equals("ARBDIS")){
					//Utils.copyPojo(mov, movArb);
					CrearMovimientoComprobante.MovimientoComprobante_Entrada movArb = copyPojoMovimientoComprobanteEntrada(mov);
					
					movArb.NumeroMvtoCmbte = vMaxSecuencia + index;
					movArb.IdConcepto = opSp.IdTipoOperacion.equals("COMSPT") ? "VNMESP" : "CMMESP";
					movArb.IdMoneda = opSp.IdMonedaSecundaria;//Se agrega por la Centralizacion
					movArb.IdMonedaSpot= opSp.IdMonedaSecundaria;
					movArb.IdTipoOperacion = opSp.IdTipoOperacion.equals("COMSPT") ? "VENSPT" : "COMSPT";
					movArb.Cantidad = opSp.MontoMonedaSecundaria;
					movArb.Precio = opSp.TipoCambioUSD;
					movArb.Monto = opSp.MontoMonedaSecundaria;//opSp.MontoMonedaSecundaria.multiply(opSp.TipoCambioMonedaSecundaria); Se hace este cambio por la Centralizacion
					movArb.MontoMovimientoComprobante = opSp.MontoMonedaSecundaria.multiply(opSp.TipoCambioMonedaSecundaria);
					movArb.MontoLiquidacion = opSp.MontoMonedaSecundaria.multiply(opSp.TipoCambioMonedaSecundaria);
					entradaCrearMovimientoComprobante.p_Movimientos.add(movArb);
					index++;
				}
			}
			
			//Formas de Pago
			index = 1L;
			for(FormaPagoSpotFacturacionObjIn fpsf: in.p_DatosFormaPago){
				for(MovimientoComprobante_Entrada mce: entradaCrearMovimientoComprobante.p_Movimientos){
					boolean esArbitraje = "ARBINT".equals(mce.NemotecnicoInstrumento) || "ARBDIS".equals(mce.NemotecnicoInstrumento) ? true : false;
					
					if(fpsf.IdOperacion.equals(mce.IdTransaccion) && (!esArbitraje || (esArbitraje && fpsf.IdMoneda.equals(mce.IdMonedaSpot)))) {
						CrearMovimientoComprobante.MovimientoComprobanteFormaDePago_Entrada mcfp = new CrearMovimientoComprobante.MovimientoComprobanteFormaDePago_Entrada();
						mcfp.NumeroMvtoCmbte = mce.NumeroMvtoCmbte;
						mcfp.NumeroMvtoCmbteFormaDePago = index;
						mcfp.IdMedioDePago = fpsf.IdMedioDePago;
						mcfp.NumeroSerie = "";
						mcfp.NumeroDocumento = 0L;
						mcfp.IdCuentaBancaria = fpsf.IdCuentaBancaria;
						mcfp.IdTipoCuentaBancaria = fpsf.IdTipoCuentaBancaria;
						mcfp.NumeroCuentaBancaria = fpsf.NumeroCuentaBancaria;
						mcfp.IdTarjetaBancaria = 0L;
						mcfp.IdTipoTarjetaBancaria = "";
						mcfp.NumeroTarjetaBancaria = "";
						mcfp.FechaVctoTarjetaBancaria = "0001-01-01";
						mcfp.IdModalidadDePago = fpsf.IdModalidadPago;
						mcfp.IdBanco = fpsf.IdBanco;
						mcfp.IdMoneda = fpsf.IdMoneda;
						mcfp.FechaDocumentoPago = fpsf.FechaLiquidacionComprobantePagoOperacion;
						mcfp.MontoLiquidacionFormaDePago = fpsf.Monto;
						if(esArbitraje){
							if(mce.IdConcepto.equals("CMMESP")){
								mcfp.IdTipoMovimientoSpot = fpsf.IdMoneda.equals(mce.IdMonedaSpot) ? "MOVING" : "MOVEGR";
							}
							else if(mce.IdConcepto.equals("VNMESP")){
								mcfp.IdTipoMovimientoSpot = fpsf.IdMoneda.equals(mce.IdMonedaSpot) ? "MOVEGR" : "MOVING";
							}
							mcfp.AfectarTesoreria = fpsf.IdMoneda.equals(mce.IdMonedaSpot) && "S".equals(fpsf.AfectarTesoreria) ? "S" : "N";
							mcfp.AfectarCuentaInversion = fpsf.IdMoneda.equals(mce.IdMonedaSpot) ? "S" : "N";
						}
						else{
							mcfp.IdTipoMovimientoSpot = fpsf.IdTipoMovimiento;
							mcfp.AfectarTesoreria = fpsf.AfectarTesoreria;
							mcfp.AfectarCuentaInversion = "S";
						}
						entradaCrearMovimientoComprobante.p_MovimientosFormaDePago.add(mcfp);
						index++;
					}
				}
			}
			
			//Cobros
			/*CrearMovimientoComprobante.MovimientoComprobanteCobro_Entrada mcc = new CrearMovimientoComprobante.MovimientoComprobanteCobro_Entrada();
			mcc.NumeroMvtoCmbte = (long) 1;
			mcc.IdCobro = "";
			mcc.IdMonedaCobro = "";
			mcc.IdMonedaLiquidacionCobro = "";
			mcc.MontoCobro = BigDecimal.ZERO;
			mcc.MontoMovimientoComprobanteCobro = BigDecimal.ZERO;
			mcc.MontoLiquidacionCobro = BigDecimal.ZERO;
			mcc.Porcentaje = BigDecimal.ZERO;
			entradaCrearMovimientoComprobante.p_MovimientosCobros.add(mcc);*/
			
			CrearMovimientoComprobante serviceCrearMovimientoComprobante = new CrearMovimientoComprobante();
			CrearMovimientoComprobante_Salida salidaCrearMovimientoComprobante = serviceCrearMovimientoComprobante.logica(conn,entradaCrearMovimientoComprobante);
			for (MensajeSalida msj : salidaCrearMovimientoComprobante.P_OUT_MENSAJES) {
				out.P_OUT_MENSAJES.add(msj);
			}
			
			
			/***** ACTUALIZA CAMPO IdComprobanteFacturacion DE LA TABLA BSCL.Mesa.BD::SPT.OperacionSpot *****/
			List<OperacionSpot> opSpUpd = new ArrayList<>();
			if(salidaCrearMovimientoComprobante.p_out_Movimientos.size() > 0) {
				for(MovimientoComprobante_Salida mcs: salidaCrearMovimientoComprobante.p_out_Movimientos){
					for(OperacionSpotFacturacionObjIn opSp: in.p_DatosOperacion){
						if(mcs.NumeroMvtoCmbte.longValue() == opSp.IdOperacionSpot.longValue()){
							OperacionSpot os = new OperacionSpot();
							os.IdOperacionSpot = opSp.IdOperacionSpot;
							os.IdComprobanteFacturacion = mcs.IdMovimientoComprobante;
							os.Estado = opSp.Estado;
							opSpUpd.add(os);
						}
					}
				}
				List<MensajeSalida> mensajes = out.P_OUT_MENSAJES.stream()
						.map(temp -> { MensajeSalida mensaje = new MensajeSalida ();
							mensaje.codigo = temp.codigo;
							mensaje.descripcion = temp.descripcion;
							mensaje.id = temp.id;
							mensaje.tipo = temp.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ERROR) ? MensajeSalida.MENSAJE_ADVERTENCIA : temp.tipo;
							return mensaje;
						}).collect(Collectors.toList());
				out.P_OUT_MENSAJES = mensajes;
				out.P_OUT_ADVERTENCIASCONTADOR = salidaCrearMovimientoComprobante.P_OUT_MENSAJES.stream().filter(salidaComprobante -> salidaComprobante.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ERROR)).count();
				_sql.batch(conn, SQL_UPDATE_OPERACIONES_SPOT_ID_COMPROBANTE_FACTURACION, opSpUpd, OperacionSpot.class);
			}else {
				out.P_OUT_ERRORESCONTADOR = salidaCrearMovimientoComprobante.P_OUT_MENSAJES.stream().filter(salidaComprobante -> salidaComprobante.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ERROR)).count();
			}
			
			
			/***** SALIDA *****/
			out.P_OUT_IDOPERACIONSPOT = new ArrayList<OperacionSpotFacturacionObjOut>();
			for(OperacionSpot opSp: opSpUpd){
				OperacionSpotFacturacionObjOut opOut = new OperacionSpotFacturacionObjOut();
				opOut.IdOperacionSpot = opSp.IdOperacionSpot;
				opOut.Estado = opSp.Estado;
				out.P_OUT_IDOPERACIONSPOT.add(opOut);
			}
			
		}catch(Exception e){
			out.P_OUT_ERRORESCONTADOR = 1L;
			logger.error(e);
			throw e;
		}
		
		logger.info("Termino del servicio CrearOperacionSpotFacturacion, tiempo:" + (System.currentTimeMillis() - init));
		return out;
	}
	
	private CrearMovimientoComprobante.MovimientoComprobante_Entrada copyPojoMovimientoComprobanteEntrada(CrearMovimientoComprobante.MovimientoComprobante_Entrada mov) {
		CrearMovimientoComprobante.MovimientoComprobante_Entrada movArb = new CrearMovimientoComprobante.MovimientoComprobante_Entrada();
		movArb.ActualizarCustodia = mov.ActualizarCustodia;
		movArb.ActualizarCuentaInversion = mov.ActualizarCuentaInversion;
		movArb.ActualizarTesoreria = mov.ActualizarTesoreria;
		movArb.ActualizarCustodiaRelacionado = mov.ActualizarCustodiaRelacionado;
		movArb.Cantidad = mov.Cantidad;
		movArb.CrearComprobanteAutomatico = mov.CrearComprobanteAutomatico;
		movArb.CodigoBroker = mov.CodigoBroker;
		movArb.CodigoBancoCustodio = mov.CodigoBancoCustodio;
		movArb.CodigoBancoLiquidador = mov.CodigoBancoLiquidador;
		movArb.DiasPlazo = mov.DiasPlazo;
		movArb.EsAPlazo = mov.EsAPlazo;
		movArb.EsComprobIndividual = mov.EsComprobIndividual;
		movArb.EsCuentaPropia = mov.EsCuentaPropia;
		movArb.EsEnRueda = mov.EsEnRueda;
		movArb.EsFungible = mov.EsFungible;
		movArb.EsPrecioPrima = mov.EsPrecioPrima;
		movArb.EsRuteo = mov.EsRuteo;
		movArb.EsVentaCorta = mov.EsVentaCorta;
		movArb.EsClienteFinal = mov.EsClienteFinal;
		movArb.FechaEmision = mov.FechaEmision;
		movArb.FechaLiquidacion = mov.FechaLiquidacion;
		movArb.FechaMovimientoComprobante = mov.FechaMovimientoComprobante;
		movArb.FechaOrden = mov.FechaOrden;
		movArb.FechaParidad = mov.FechaParidad;
		movArb.FechaTipoCambio = mov.FechaTipoCambio;
		movArb.FechaTransaccion = mov.FechaTransaccion;
		movArb.FechaVencimiento = mov.FechaVencimiento;
		movArb.FolioCartera = mov.FolioCartera;
		movArb.FolioTransaccion = mov.FolioTransaccion;
		movArb.Glosa = mov.Glosa;
		movArb.IdAsignacion = mov.IdAsignacion;
		movArb.IdAsignacionRef = mov.IdAsignacionRef;
		movArb.IdBolsa = mov.IdBolsa;
		movArb.IdBoveda = mov.IdBoveda;
		movArb.IdCliente = mov.IdCliente;
		movArb.IdConcepto = mov.IdConcepto;
		movArb.IdComprobanteReferencia = mov.IdComprobanteReferencia;
		movArb.IdCondicionLiquidacion = mov.IdCondicionLiquidacion;
		movArb.IdCorredoraContraparte = mov.IdCorredoraContraparte;
		movArb.IdCuentaCustodioPortafolio = mov.IdCuentaCustodioPortafolio;
		movArb.IdEmisor = mov.IdEmisor;
		movArb.IdEmpleadoDigitador = mov.IdEmpleadoDigitador;                          
		movArb.IdEmpresa = mov.IdEmpresa;
		movArb.IdFormaCustodia = mov.IdFormaCustodia;
		movArb.IdInstrumento = mov.IdInstrumento;
		movArb.IdInstrumentoRelacionado = mov.IdInstrumentoRelacionado;
		movArb.IdInstrumentoSubyacente = mov.IdInstrumentoSubyacente;
		movArb.IdMesa = mov.IdMesa;
		movArb.IdMoneda = mov.IdMoneda;
		movArb.IdMonedaEmision = mov.IdMonedaEmision;
		movArb.IdMonedaLiquidacion = mov.IdMonedaLiquidacion;
		movArb.IdMonedaSubyacente = mov.IdMonedaSubyacente;
		movArb.IdMonedaSpot = mov.IdMonedaSpot;
		movArb.IdMovimientoRelacionado = mov.IdMovimientoRelacionado;
		movArb.IndicadorOrdenDirecta = mov.IndicadorOrdenDirecta;
		movArb.IdOrden = mov.IdOrden;
		movArb.IdPortafolio = mov.IdPortafolio;
		movArb.IdTipoComprobante = mov.IdTipoComprobante;
		movArb.IdTipoOperacion = mov.IdTipoOperacion;
		movArb.IdTipoProducto = mov.IdTipoProducto;
		movArb.IdTipoReajuste = mov.IdTipoReajuste;
		movArb.IdTipoConvenio = mov.IdTipoConvenio;
		movArb.IdTipoCanalIngreso = mov.IdTipoCanalIngreso;
		movArb.IdTipoCliente = mov.IdTipoCliente;
		movArb.IdTransaccion = mov.IdTransaccion;
		movArb.IdUsuario = mov.IdUsuario;
		movArb.IdPersonaBroker = mov.IdPersonaBroker;
		movArb.IdPersonaBancoCustodio = mov.IdPersonaBancoCustodio;
		movArb.IdPersonaBancoLiquidador = mov.IdPersonaBancoLiquidador;
		movArb.Intereses = mov.Intereses;
		movArb.LiquidaBancoCustodio = mov.LiquidaBancoCustodio;
		movArb.MercadoSMV = mov.MercadoSMV;
		movArb.ModalidadSMV = mov.ModalidadSMV;
		movArb.ModPrefFact = mov.ModPrefFact;
		movArb.Monto = mov.Monto;
		movArb.MontoMovimientoComprobante = mov.MontoMovimientoComprobante;
		movArb.MontoLiquidacion = mov.MontoLiquidacion;
		movArb.MontoDescuento = mov.MontoDescuento;
		movArb.MontoInteres = mov.MontoInteres;
		movArb.MontoAPlazo = mov.MontoAPlazo;
		movArb.NumeroMvtoCmbte = mov.NumeroMvtoCmbte;
		movArb.NemotecnicoInstrumento = mov.NemotecnicoInstrumento;
		movArb.NemoSVS = mov.NemoSVS;
		movArb.Precio = mov.Precio;
		movArb.PrecioAPlazo = mov.PrecioAPlazo;
		movArb.ReajustesVAC = mov.ReajustesVAC;
		movArb.SecuenciaAgrupacion = mov.SecuenciaAgrupacion;
		movArb.Spread = mov.Spread;
		movArb.Tasa = mov.Tasa;
		movArb.TasaAgente = mov.TasaAgente;
		movArb.TasaEmision = mov.TasaEmision;
		movArb.TasaOperacion = mov.TasaOperacion;
		movArb.ValorRescate = mov.ValorRescate;
		movArb.ValorParidad = mov.ValorParidad;
		movArb.ValorTipoCambio = mov.ValorTipoCambio;
		movArb.ValorCaptacionMonedaOrigen = mov.ValorCaptacionMonedaOrigen;
		movArb.CodigoIFRS = mov.CodigoIFRS;
		movArb.IdOperacion = mov.IdOperacion;
		return movArb;
	}
	
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String jsonDesa = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":33,\"p_IdModulo\":\"MSP\",\"p_UsaReglasNegocio\":\"S\",\"p_DatosOperacion\":[{\"IdOperacionSpot\":9292,\"IdTipoOperacion\":\"COMSPT\",\"FechaOperacion\":\"2018-10-23 16:09:43.000\",\"IdCliente\":130826,\"IdPortafolio\":34993,\"IdInstrumento\":346391,\"Nemotecnico\":\"ARBDIS\",\"IdTipoProducto\":\"MEME\",\"IdMonedaPrincipal\":\"EUR\",\"IdMonedaSecundaria\":\"USD\",\"MontoMonedaPrincipal\":1000.00000000,\"MontoMonedaSecundaria\":1530.00000000,\"TipoCambioCierre\":0E-8,\"ParidadCierre\":1.53000000,\"TipoComprobanteFacturacion\":\"FEE\",\"IdBoveda\":\"COR\",\"IdCondicionLiquidacion\":\"T0\",\"FechaLiquidacion\":\"2018-10-23\",\"IdTipoConvenio\":\"FAC\",\"MultiplicaODivide\":\"M\",\"TipoCambioUSD\":650.00000000,\"TipoCambioMonedaSecundaria\":650.00000000,\"Glosa\":\"SPOT\",\"IdMesa\":6,\"CrearComprobanteAutomatico\":\"N\",\"Estado\":\"VIG\"}],\"p_DatosFormaPago\":[{\"IdOperacion\":9292,\"IdTipoMovimiento\":\"MOVING\",\"IdMedioDePago\":\"EFE\",\"IdModalidadPago\":\"MODCAJ\",\"IdMoneda\":\"EUR\",\"Monto\":400.00000000,\"FechaLiquidacionComprobantePagoOperacion\":\"2018-10-23\",\"AfectarTesoreria\":\"S\",\"IdFormaDePago\":null,\"IdCondicionDeLiquidacion\":null},{\"IdOperacion\":9292,\"IdTipoMovimiento\":\"MOVING\",\"IdMedioDePago\":\"CHE\",\"IdModalidadPago\":\"MODCAJ\",\"IdMoneda\":\"EUR\",\"Monto\":200.00000000,\"FechaLiquidacionComprobantePagoOperacion\":\"2018-10-23\",\"AfectarTesoreria\":\"S\",\"IdFormaDePago\":null,\"IdCondicionDeLiquidacion\":null},{\"IdOperacion\":9292,\"IdTipoMovimiento\":\"MOVING\",\"IdMedioDePago\":\"TRA\",\"IdModalidadPago\":\"MODDEP\",\"IdMoneda\":\"EUR\",\"Monto\":200.00000000,\"FechaLiquidacionComprobantePagoOperacion\":\"2018-10-23\",\"AfectarTesoreria\":\"S\",\"IdFormaDePago\":null,\"IdCondicionDeLiquidacion\":null},{\"IdOperacion\":9292,\"IdTipoMovimiento\":\"MOVEGR\",\"IdMedioDePago\":\"TRA\",\"IdModalidadPago\":\"MODDEP\",\"IdMoneda\":\"USD\",\"Monto\":330.00000000,\"FechaLiquidacionComprobantePagoOperacion\":\"2018-10-23\",\"AfectarTesoreria\":\"S\",\"IdFormaDePago\":null,\"IdCondicionDeLiquidacion\":null},{\"IdOperacion\":9292,\"IdTipoMovimiento\":\"MOVING\",\"IdMedioDePago\":\"CIN\",\"IdModalidadPago\":\"MODDEP\",\"IdMoneda\":\"EUR\",\"Monto\":200.00000000,\"FechaLiquidacionComprobantePagoOperacion\":\"2018-10-23\",\"AfectarTesoreria\":\"N\",\"IdFormaDePago\":null,\"IdCondicionDeLiquidacion\":null},{\"IdOperacion\":9292,\"IdTipoMovimiento\":\"MOVEGR\",\"IdMedioDePago\":\"CIN\",\"IdModalidadPago\":\"MODDEP\",\"IdMoneda\":\"USD\",\"Monto\":1200.00000000,\"FechaLiquidacionComprobantePagoOperacion\":\"2018-10-23\",\"AfectarTesoreria\":\"N\",\"IdFormaDePago\":null,\"IdCondicionDeLiquidacion\":null}]}";
			//String jsonMaBa = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":25,\"p_IdModulo\":\"MSP\",\"p_UsaReglasNegocio\":\"S\",\"p_DatosOperacion\":[{\"NumeroOperacion\":1,\"IdInstrumento\":133258,\"IdTipoOperacion\":\"COMSPT\",\"IdTipoProducto\":\"MEME\",\"IdCliente\":116825,\"IdPortafolio\":122076,\"FechaOperacion\":\"2018-06-12\",\"IdMonedaPrincipal\":\"USD\",\"IdMonedaSecundaria\":\"CLP\",\"MontoMonedaPrincipal\":1000,\"MontoMonedaSecundaria\":639000,\"TipoCambioCierre\":639,\"TipoCambioCierreForward\":0,\"ParidadCierre\":0,\"ParidadCierreForward\":0,\"TipoCambioCosto\":639,\"ParidadCosto\":0,\"MontoEquivalente\":639000,\"MontoRentabilidad\":0,\"MontoRentabilidadCanal\":0,\"Margen\":0,\"Utilidad\":0,\"PuntaCompra\":639,\"PuntaVenta\":642,\"SolicitudTipoCambio\":\"N\",\"IdMesa\":2,\"IdModulo\":\"SPT\",\"IdTipoDolar\":1,\"TipoComprobanteFacturacion\":\"FAE\",\"IdAgente\":20180,\"BenOur\":\"BEN\",\"EquivalenteConGasto\":0,\"TipoCambioCierreConGasto\":0,\"Observacion\":\"\",\"CodigoComercio\":\"10005\",\"FolioPlanilla\":0,\"FolioInterfaz\":\"\",\"OperacionFicticia\":\"N\",\"IdTipoCanalIngreso\":\"MES\",\"IdDatatec\":0,\"Estado\":\"VIG\"}],\"p_DatosFormaPago\":[{\"IdInternoPagoOperacion\":1,\"IdComprobantePagoOperacion\":0,\"IdTipoMovimiento\":\"MOVING\",\"IdFormaDePago\":\"VENSPT_CHE_CLP_0\",\"IdOperacion\":1,\"RecibePagaPortafolio\":\"P\",\"IdMoneda\":\"CLP\",\"IdMonedaLiquidacion\":\"CLP\",\"MontoFormaDePago\":639000,\"AfectarTesoreria\":\"N\"},{\"IdInternoPagoOperacion\":2,\"IdComprobantePagoOperacion\":0,\"IdTipoMovimiento\":\"MOVEGR\",\"IdFormaDePago\":\"COMSPT_CIN_USD_0\",\"IdOperacion\":1,\"RecibePagaPortafolio\":\"R\",\"IdMoneda\":\"USD\",\"IdMonedaLiquidacion\":\"USD\",\"MontoFormaDePago\":1000,\"AfectarTesoreria\":\"N\"}]}";
			CrearOperacionSpotFacturacionEntrada Objin = gson.fromJson(jsonDesa, CrearOperacionSpotFacturacionEntrada.class);
			
			CrearOperacionSpotFacturacion demo = new CrearOperacionSpotFacturacion();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				CrearOperacionSpotFacturacionSalida ObjOut = demo.logica(con, Objin);
 				//con.commit();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				//er.printStackTrace();	
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