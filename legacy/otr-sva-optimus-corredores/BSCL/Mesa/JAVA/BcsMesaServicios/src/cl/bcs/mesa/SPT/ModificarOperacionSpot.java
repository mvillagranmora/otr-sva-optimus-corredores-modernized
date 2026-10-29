package cl.bcs.mesa.SPT;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import cl.bcs.plataforma.lim.pojo.ConstantesLimites;
import cl.bcs.plataforma.lim.pojo.Validacion;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.mesa.SPT.CrearOperacionSpot.PuntasCalcularMargen;
import cl.bcs.fondomutuo.ModificarMontoNumeroCuotasSolicitud;
import cl.bcs.fondomutuo.ModificarMontoNumeroCuotasSolicitud.EntradaModificarMontoNumeroCuotasSolicitud;
import cl.bcs.fondomutuo.ModificarMontoNumeroCuotasSolicitud.SalidaModificarMontoNumeroCuotasSolicitud;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion.ComprobantePagoOperacionObj_IN;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion.CrearComprobantePagoOperacion_IN;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion.CrearComprobantePagoOperacion_OUT;
import cl.bcs.mesa.SPT.CrearOperacionSpot.FormaDePagoMesa;
import cl.bcs.mesa.SPT.CrearOperacionSpot.FormaPagoObjIn;
import cl.bcs.mesa.SPT.CrearOperacionSpot.LimitesParidadYTipoCambio;
import cl.bcs.mesa.SPT.CrearOperacionSpot.ModalidadDePago;
import cl.bcs.mesa.SPT.CrearOperacionSpot.OperacionObjIn;
import cl.bcs.mesa.SPT.CrearOperacionSpot.OperacionObjOut;
import cl.bcs.mesa.SPT.CrearOperacionSpot.OperacionSpot;
import cl.bcs.mesa.SPT.CrearOperacionSpot.OperacionSpotEventoAutorizacion;
import cl.bcs.mesa.SPT.CrearOperacionSpot.OperacionSpotFormaDePago;
import cl.bcs.mesa.SPT.CrearOperacionSpot.PortafoliosFlujoCaja;
import cl.bcs.mesa.SPT.CrearOperacionSpot.RequisitosCrearOperacionSpot;
import cl.bcs.mesa.SPT.NotificarEstadoSpot.EntradaNotificarEstadoSpot;
import cl.bcs.mesa.utils.ConsultarConfiguracionMesa;
import cl.bcs.mesa.utils.ConsultarConfiguracionMesa.ConfiguracionMesa;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarMensajeWebsocket;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.SaldoFlujoCaja;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjFLUJOCAJA;
import cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModificarOperacionSpot implements JavaCall{
	private static final Logger logger = LogManager.getLogger(ModificarOperacionSpot.class);
	
	private final Log log = new Log("SPT.ModificarOperacionSpot");
	
	private final ConstantesLimites _cl = new ConstantesLimites();
	
	private final String SQL_INSERT_OPERACIONES_SPOT_WRAPPER = loadResourceToString("cl/bcs/mesa/SPT/sql/CrearOperacionSpot_InsertOperacionesSpotWrapper.sql");
	private final String SQL_INSERT_OPERACIONES_SPOT = loadResourceToString("cl/bcs/mesa/SPT/sql/CrearOperacionSpot_InsertOperacionesSpot.sql");
	private final String SQL_SELECT_REQUISITOS_CREAR_OPERACION_SPOT = loadResourceToString("cl/bcs/mesa/SPT/sql/CrearOperacionSpot_ConsultarRequisitosCrearOperacionSpot.sql");
	private final String SQL_SELECT_LIMITES_PARIDAD_Y_TIPO_CAMBIO = loadResourceToString("cl/bcs/mesa/SPT/sql/CrearOperacionSpot_ConsultarLimitesParidadYTipoCambio.sql");


	private final String SQL_SELECT_PORTAFOLIOS_FLUJO_CAJA = loadResourceToString("cl/bcs/mesa/SPT/sql/ModificarOperacionSpot_ConsultarPortafoliosFlujoCaja.sql");
	
	
	private final String SQL_SELECT_FORMAS_DE_PAGO_MESA = loadResourceToString("cl/bcs/mesa/sql/ConsultarFormasDePagoMesa.sql");
	private final String SQL_SELECT_MODALIDAD_DE_PAGO = "SELECT \"IdMedioDePagoYModalidadDePago\",\"IdMedioDePago\",\"IdModalidadDePago\",\"IdEstado\" FROM  \"BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago\" WHERE \"IdEstado\" = 'ACT'";
	private final String SQL_SELECT_OPERACIONES_SPOT_DATOS_ORIGINALES = loadResourceToString("cl/bcs/mesa/SPT/sql/ModificarOperacionSpot_ConsultarOperacionesSpot.sql");
	private final String SQL_UPDATE_OPERACIONES_SPOT_ORIGINALES = loadResourceToString("cl/bcs/mesa/SPT/sql/ModificarOperacionSpot_UpdateOperacionesSpotOriginales.sql");
	private final String SQL_SELECT_OPERACIONES_SPOT_INTERBANCARIO = loadResourceToString("cl/bcs/mesa/SPT/sql/CrearOperacionSpot_ConsultarOperacionesSpotINTERB.sql");
	private final String SQL_SELECT_OPERACIONES_SPOT_ARBITRAJE_INTERBANCARIO = loadResourceToString("cl/bcs/mesa/SPT/sql/CrearOperacionSpot_ConsultarOperacionesSpotARBINT.sql");
	private final String SQL_SELECT_OPERACIONES_SPOT_DISTRIBUCION = loadResourceToString("cl/bcs/mesa/SPT/sql/CrearOperacionSpot_ConsultarOperacionesSpotDISTRI.sql");
	private final String SQL_SELECT_OPERACIONES_SPOT_ARBITRAJE_DISTRIBUCION = loadResourceToString("cl/bcs/mesa/SPT/sql/CrearOperacionSpot_ConsultarOperacionesSpotARBDIS.sql");
	private final String SQL_SELECT_PUNTAS_CALCULAR_MARGEN = loadResourceToString("cl/bcs/mesa/SPT/sql/CrearOperacionSpot_ConsultarPuntasCalcularMargen.sql");	
	
	public static class ModificarOperacionSpotEntrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_CalculaMargen;
		public String p_UsaReglasNegocio;
		public List<OperacionObjIn> p_DatosOperacion;
		public List<FormaPagoObjIn> p_DatosFormaPago;
		
		public ModificarOperacionSpotEntrada() {
			this.p_DatosOperacion = new ArrayList<OperacionObjIn>();
			this.p_DatosFormaPago = new ArrayList<FormaPagoObjIn>();
			this.p_CalculaMargen ="N";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ModificarOperacionSpotSalida {
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;
		public Long						P_OUT_ADVERTENCIASCONTADOR;
		public List<OperacionObjOut>	P_OUT_IDOPERACIONSPOT;
		
		public ModificarOperacionSpotSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_IDOPERACIONSPOT = new ArrayList<OperacionObjOut>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ModificarOperacionSpotEntrada in = gson.fromJson(jin.toString(), ModificarOperacionSpotEntrada.class);
		ModificarOperacionSpotSalida out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		out.P_OUT_ADVERTENCIASCONTADOR = out.P_OUT_MENSAJES.stream().filter(u -> "AUTORIZACION".equals(u.tipo) || MensajeSalida.MENSAJE_ADVERTENCIA.equals(u.tipo)).count();
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public ModificarOperacionSpotSalida logica(Connection conn, ModificarOperacionSpotEntrada in) throws Exception {
		SQL _sql = new SQL();
		ModificarOperacionSpotSalida out = new ModificarOperacionSpotSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		out.P_OUT_ADVERTENCIASCONTADOR = 0L;
		Long Id = 0L;
		Long errCont = 0L;
		Long advCont = 0L;
		HashMap<String,String> mapMsg = new HashMap<>();
		Date fechaDefecto = Fecha.toDate("0001-01-01");
		log.write("INICIO :: SPOT.ModificarOperacionSpot");
		
		try {
			if (in.p_DatosOperacion.size() == 0) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_OPERACIONES",mapMsg,conn,in.p_IdEmpresa));
				Id++;
				return out;
			}
			
			RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
			RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(conn, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
			if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
				out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
				return out;
			}
			
			SocioNegocioUtils.Empleado empleadoUsuario = SocioNegocioUtils.obtenerEmpleadoUsuario(conn, in.p_IdEmpresa, in.p_IdUsuario).orElse(null);
			if (empleadoUsuario == null) {
				out.P_OUT_ERRORESCONTADOR++;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_USUARIO_NO_ES_EMPLEADO", new HashMap<>(), conn, in.p_IdEmpresa));
				return out;
			}
			
			/***** VARIABLES *****/
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn,in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			String fechaHoraSistemaSecondate = FechaDB.ObtenerFechaHoraSistemaUTC(conn,in.p_IdEmpresa);
			java.sql.Timestamp fechaHoraSistemaTimestamp = FechaDB.ObtenerFechaHoraSistemaTs(conn,in.p_IdEmpresa);
			String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
			
			ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(conn, in.p_IdEmpresa);
		    Long idPaisSistema = empresa.IdPais;
			
			ConfiguracionMesa configuracionMesa = new ConsultarConfiguracionMesa().logica(conn, in.p_IdEmpresa, "MEME", "IDMONEDAEQUIVALENTE");
			String monedaEquivalente = configuracionMesa.Valor;
			
			configuracionMesa = new ConsultarConfiguracionMesa().logica(conn, in.p_IdEmpresa, "MEME", "ENVIA_SPOT_A_CONFIRMAR_LIQUIDACION");
			String liquidacionPorConfirmar = configuracionMesa.Valor;
			String estadoConfirmacion = liquidacionPorConfirmar.equals("S") ? "PEN" : "CNF";
			
			Long cantOpeIn = 0L;
			Long cantOpeVig = 0L;
			Long cantOpePen = 0L;
			
			HashMap<String,Object> params = new HashMap<>();
			List<OperacionObjIn> operacionesInUpdateEstado = new ArrayList<>();
			List<OperacionSpot> operacionModificada = new ArrayList<>();
			List<OperacionObjIn> operacionesInUpdateMargen = new ArrayList<>();
			
			List<OperacionSpotEventoAutorizacion> evtList = new ArrayList<>();
			
			HashMap<String,OperacionObjIn> numeroOperacionHash = new HashMap<String,OperacionObjIn>();
			List<String> IdTipoProducto = new ArrayList<String>();
			List<String> numeroOperacion = new ArrayList<String>();
			for (OperacionObjIn ope: in.p_DatosOperacion){
				numeroOperacion.add(ope.NumeroOperacion);
				cantOpeIn++;
				
				numeroOperacionHash.put(ope.NumeroOperacion, ope);
				IdTipoProducto.add(ope.IdTipoProducto);
			}
			
			//Formas de pago Mesa
			HashMap<String, Object> selectFormaPagoMesaIN = new HashMap<>();
			selectFormaPagoMesaIN.put("IdTipoProducto" , IdTipoProducto);
			List<FormaDePagoMesa> formaDePagoMesaList = _sql.query(conn, SQL_SELECT_FORMAS_DE_PAGO_MESA, selectFormaPagoMesaIN, FormaDePagoMesa.class);
			HashMap<String, Object> formaDePagoMesaHash = Utils.List2Hash(formaDePagoMesaList, "IdFormaDePago");
			
			//Modalidad de pago
			List<ModalidadDePago> modalidadPago = _sql.query(conn, SQL_SELECT_MODALIDAD_DE_PAGO, new HashMap<>(), ModalidadDePago.class);
			HashMap<String, Object> modalidadPagoHash = Utils.List2Hash(modalidadPago, "IdMedioDePago");
			
			
			/***** DATOS NUEVA OPERACION SPOT *****/
			//Cruce entre datos originales y datos modificados
			params.put("IdEmpresa", in.p_IdEmpresa);
			params.put("NumeroOperacion", numeroOperacion);
			
			List<OperacionSpot> operacionOriginal = _sql.query(conn, SQL_SELECT_OPERACIONES_SPOT_DATOS_ORIGINALES, params, OperacionSpot.class);
			
			// Las operaciones Vigentes que ya estan confirmadas en blotter Confirmación no se pueden modificar
			// ------------------------------------------------------------------------------------------------
			for(OperacionSpot OperacionesModificadas: operacionOriginal){
				if ( OperacionesModificadas.Estado.equals("VIG") && !OperacionesModificadas.EstadoConfirmacion.equals("PEN") ) {
					mapMsg.put("NumeroOperacion", OperacionesModificadas.NumeroOperacion);
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"SRV_SPT_OPERACION_MOD_CONFIRMADA",mapMsg,conn,in.p_IdEmpresa));
				}
			}
			
			if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0) {
				return out;
			}
			
			
			for(OperacionObjIn datosNuevos: in.p_DatosOperacion){
				for(OperacionSpot datosOriginales: operacionOriginal){
					if(datosNuevos.NumeroOperacion.equals(datosOriginales.NumeroOperacion)){
						OperacionSpot opSpMod = new OperacionSpot();
						opSpMod.NumeroOperacion = datosNuevos.NumeroOperacion;
						opSpMod.IdOperacionSpot = 0L;
						opSpMod.IdEmpresa = datosOriginales.IdEmpresa;
						opSpMod.IdCliente = datosOriginales.IdCliente;
						opSpMod.IdPortafolio = datosOriginales.IdPortafolio;
						opSpMod.IdInstrumento = datosOriginales.IdInstrumento;
						opSpMod.IdTipoOperacion = datosNuevos.IdTipoOperacion;
						opSpMod.IdTipoProducto = datosNuevos.IdTipoProducto;
						opSpMod.FechaOperacion = datosNuevos.FechaOperacion;
						opSpMod.FechaLiquidacion = datosOriginales.FechaLiquidacion;
						opSpMod.IdMonedaPrincipal = datosOriginales.IdMonedaPrincipal;
						opSpMod.IdMonedaSecundaria = datosOriginales.IdMonedaSecundaria;
						opSpMod.IdMonedaLocal = datosOriginales.IdMonedaLocal;
						opSpMod.IdMonedaEquivalente = datosOriginales.IdMonedaEquivalente;
						opSpMod.MontoMonedaPrincipal = datosNuevos.MontoMonedaPrincipal;
						opSpMod.MontoMonedaPrincipalEquivalente = BigDecimal.ZERO;
						opSpMod.MontoMonedaSecundaria = datosNuevos.MontoMonedaSecundaria;
						opSpMod.TipoCambioCierre = datosNuevos.TipoCambioCierre;
						opSpMod.TipoCambioCierreForward = datosOriginales.TipoCambioCierreForward;
						opSpMod.ParidadCierre = datosNuevos.ParidadCierre;
						opSpMod.ParidadCierreForward = datosOriginales.ParidadCierreForward;
						opSpMod.TipoCambioCosto = datosNuevos.IdSolicitudAporteRescate != null && datosNuevos.IdSolicitudAporteRescate > 0 ? datosNuevos.TipoCambioCosto : datosOriginales.TipoCambioCosto;
						opSpMod.ParidadCosto = datosOriginales.ParidadCosto;
						opSpMod.MontoEquivalente = datosNuevos.MontoEquivalente;
						opSpMod.MontoRentabilidad = datosOriginales.MontoRentabilidad;
						opSpMod.MontoRentabilidadCanal = datosOriginales.MontoRentabilidadCanal;
						opSpMod.ResultadoMonedaLocalPuntas = BigDecimal.ZERO;
						opSpMod.ResultadoMonedaLocalBidAsk = BigDecimal.ZERO;
						opSpMod.ResultadoMonedaLocalDO = BigDecimal.ZERO;
						opSpMod.ResultadoMonedaLocalTCSpot = BigDecimal.ZERO;
						opSpMod.ResultadoMonedaLocalTCCierreMesa = BigDecimal.ZERO;
						opSpMod.ResultadoMonedaLocalConsolidado = BigDecimal.ZERO;
						opSpMod.Margen = datosNuevos.Margen;
						opSpMod.MargenBidAsk = BigDecimal.ZERO;
						opSpMod.Utilidad = datosNuevos.Utilidad;
						opSpMod.PuntaCompra = datosOriginales.PuntaCompra;
						opSpMod.PuntaVenta = datosOriginales.PuntaVenta;
						opSpMod.PuntaBid = BigDecimal.ZERO;
						opSpMod.PuntaAsk = BigDecimal.ZERO;
						opSpMod.ParidadBid = BigDecimal.ZERO;
						opSpMod.ParidadAsk = BigDecimal.ZERO;
						opSpMod.ValorDolarObservado = BigDecimal.ZERO;
						opSpMod.ValorTCSpot = BigDecimal.ZERO;
						opSpMod.SolicitudTipoCambio = datosNuevos.SolicitudTipoCambio;
						opSpMod.IdEvento = datosOriginales.IdEvento;
						opSpMod.IdMesa = datosNuevos.IdMesa;
						opSpMod.IdModulo = datosOriginales.IdModulo;
						opSpMod.IdTipoDolar = datosOriginales.IdTipoDolar;
						opSpMod.TipoComprobanteFacturacion = datosNuevos.TipoComprobanteFacturacion;
						opSpMod.IdComprobanteFacturacion = datosOriginales.IdComprobanteFacturacion;
						opSpMod.IdComprobanteTesoreriaIngreso = datosOriginales.IdComprobanteTesoreriaIngreso;
						opSpMod.IdComprobanteTesoreriaEgreso = datosOriginales.IdComprobanteTesoreriaEgreso;
						opSpMod.IdAgente = datosNuevos.IdAgente;
						opSpMod.BenOur = datosOriginales.BenOur;
						opSpMod.EquivalenteConGasto = datosNuevos.EquivalenteConGasto;
						opSpMod.TipoCambioCierreConGasto = datosNuevos.TipoCambioCierreConGasto;
						opSpMod.Observacion = datosNuevos.Observacion;
						opSpMod.CodigoComercio = datosNuevos.CodigoComercio;
						opSpMod.FolioPlanilla = datosOriginales.FolioPlanilla;
						opSpMod.FolioInterfaz = datosOriginales.FolioInterfaz;
						opSpMod.OperacionFicticia = datosNuevos.OperacionFicticia;
						opSpMod.IdTipoCanalIngreso = datosNuevos.IdTipoCanalIngreso;
						opSpMod.IdDatatec = datosOriginales.IdDatatec;
						opSpMod.Estado = datosNuevos.Estado;
						opSpMod.IdUsuario = datosOriginales.IdUsuario;
						opSpMod.IdUsuarioModifica = in.p_IdUsuario;
						opSpMod.FechaModifica = fechaHoraSistemaTimestamp;
						opSpMod.FechaReproceso = datosOriginales.FechaReproceso;
						opSpMod.FechaIngresoRegistro = datosOriginales.FechaIngresoRegistro;
						opSpMod.EstadoConfirmacion = estadoConfirmacion;
						opSpMod.IdOperacionSpotReferencia = datosOriginales.IdOperacionSpot;
						opSpMod.FolioOrigenFW = datosOriginales.FolioOrigenFW;
						opSpMod.IdOperacionSpotOrigen = datosOriginales.IdOperacionSpotOrigen;
						opSpMod.IdOperacionSpotVigente = 0L;
						opSpMod.MultiplicaODivide = "";
						opSpMod.ParidadMercado = BigDecimal.ZERO;
						opSpMod.CierreIngresadoPosMonPri = BigDecimal.ZERO;
						opSpMod.CierreIngresadoPosMonEqui = BigDecimal.ZERO;
						opSpMod.Nemotecnico = "";
						opSpMod.TipoCambioUSD = BigDecimal.ZERO;
						opSpMod.TipoCambioMonedaSecundaria = BigDecimal.ZERO;
						opSpMod.IdSolicitudAporteRescate = datosOriginales.IdSolicitudAporteRescate;
						opSpMod.IdTipoSpot = datosOriginales.IdTipoSpot;
						opSpMod.IdEmpleadoDigitador = datosOriginales.IdEmpleadoDigitador;
						operacionModificada.add(opSpMod);
					}
				}
			}
			
			//Insert a tabla global de las nuevas operaciones para los select con varios join
			log.write("INSERT en tabla BSCL.Mesa.BD::SPT.CrearOperacionSpot_Proceso :: "+operacionModificada);
			_sql.batch(conn, SQL_INSERT_OPERACIONES_SPOT_WRAPPER, operacionModificada, OperacionSpot.class);
			
			
			/***** VALIDACIONES *****/
			params.clear();
			params.put("IdEmpresa", in.p_IdEmpresa);
			params.put("NumeroOperacion", numeroOperacion);
			params.put("FechaSistema", fechaSistema);
			params.put("IdUsuario", in.p_IdUsuario);
			params.put("IdSerieFondo","");
			List<RequisitosCrearOperacionSpot> requisitosCrearOperacionSpot = _sql.query(conn, SQL_SELECT_REQUISITOS_CREAR_OPERACION_SPOT, params, RequisitosCrearOperacionSpot.class);

			String validaPlantillaEmpleado = "N";
			try{ validaPlantillaEmpleado = new ConsultarConfiguracionMesa().logica(conn, in.p_IdEmpresa, "MEME", "VALIDA_PLANTILLA_EMPLEADO").Valor;} catch(Exception er) {};
			
			for (RequisitosCrearOperacionSpot req: requisitosCrearOperacionSpot){
				//Codigos que no se incorporaron : EXCEDE_HORARIO, DOLAR_MENOR_IGUAL_CERO. El codigo OPERACION_SPOT_FACTURADA solo aplica en la Modificacion Spot
				
				if(!req.IdEstadoMesa.equals("A")){
					mapMsg.put("NumeroOperacion", req.NumeroOperacion);
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"MESA_SPOT_CERRADA",mapMsg,conn,in.p_IdEmpresa));
					Id++;
				}
				if(req.IdMesa.longValue() == 0L){
					mapMsg.put("NumeroOperacion", req.NumeroOperacion);
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"NO_EXISTE_MESA_SPOT_POR_DEFECTO",mapMsg,conn,in.p_IdEmpresa));
					Id++;
				}
				
				//Estas validaciones solo se realizan en Spot origen Mesa
				if(in.p_UsaReglasNegocio.equals("S")){
					if(!(req.IdEstadoModulo.equals("ACT") && req.TipoEstadoModulo.equals("A"))){
						mapMsg.put("NumeroOperacion", req.NumeroOperacion);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"UI_MODULO_CERRADO",mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if(!(Fecha.toStringDate(req.FechaOperacion).equals(fechaSistema) || req.FechaOperacion.after(Fecha.toDate(fechaSistema)))){
						mapMsg.put("NumeroOperacion", req.NumeroOperacion);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"FECHA_PROCESO_INVALIDA",mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if(!(req.IdTipoComprobante.contains("FA") || req.IdTipoComprobante.equals("BOE") || req.IdTipoComprobante.equals("BEE")) && req.IdEstadoFacturacion.equals("")){
						mapMsg.put("NumeroOperacion", req.NumeroOperacion);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"TIPO_COMPROBANTE_INVALIDO",mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if(req.TipoCambioUSD.compareTo(BigDecimal.ZERO) <= 0 && !req.IdMonedaPrincipal.equals("USD")){
						mapMsg.put("NumeroOperacion", req.NumeroOperacion);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"VALOR_DOLAR_ACTUAL_IGUAL_CERO",mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if(!"ARBINT".equalsIgnoreCase(req.Nemotecnico) && (req.TipoCambioPrincipal.compareTo(BigDecimal.ZERO) <= 0)){
						mapMsg.put("NumeroOperacion", req.NumeroOperacion);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"TIPO_CAMBIO_PRINCIPAL_IGUAL_CERO",mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if(!"ARBINT".equalsIgnoreCase(req.Nemotecnico) && (req.ParidadPrincipal.compareTo(BigDecimal.ZERO) <= 0)){
						mapMsg.put("NumeroOperacion", req.NumeroOperacion);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"PARIDAD_PRINCIPAL_IGUAL_CERO",mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if(req.IdCliente <= 0L){
						mapMsg.put("NumeroOperacion", req.NumeroOperacion);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"CLIENTE_NO_EXISTE",mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if(!(req.IdCliente > 0L && req.IdEstadoCliente.equals("ACT"))){
						String msjError = "";
						switch(req.IdEstadoCliente){
							case "BLO": msjError = "CLIENTE_BLOQUEADO"; break;
							case "PEN": msjError = "CLIENTE_PENDIENTE_AUTORIZACION"; break;
							default : msjError = "CLIENTE_NO_HABILITADO"; break;
						}
						mapMsg.put("DocumentoIdentidad", req.DocumentoIdentidad);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,msjError,mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if(req.IdPortafolio <= 0L){
						mapMsg.put("NumeroOperacion", req.NumeroOperacion);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"PORTAFOLIO_NO_EXISTE",mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if(!(req.IdPortafolio > 0L && req.IdEstadoPortafolio.equals("ACT"))){
						String msjError = "";
						switch(req.IdEstadoCliente){
							case "BLO": msjError = "PORTAFOLIO_BLOQUEADO"; break;
							case "PEN": msjError = "PORTAFOLIO_PENDIENTE_AUTORIZACION"; break;
							default : msjError = "PORTAFOLIO_NO_HABILITADO"; break;
						}
						mapMsg.put("CodigoPortafolio", req.CodigoPortafolio);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,msjError,mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if(!req.IdEstadoCuentaInversionMonedaPrincipal.equals("ACT")){
						mapMsg.put("NumeroOperacion", req.NumeroOperacion);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"MONEDA_PRINCIPAL_NO_HABILITADA_CUENTA_INVERSION",mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if(!req.IdEstadoCuentaInversionMonedaSecundaria.equals("ACT")){
						mapMsg.put("NumeroOperacion", req.NumeroOperacion);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"MONEDA_SECUNDARIA_NO_HABILITADA_CUENTA_INVERSION",mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if(req.ResultadoPerfilProducto.equals("NOK")){
						mapMsg.put("NumeroOperacion", req.NumeroOperacion);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,req.MensajePerfilProducto,mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if("S".equals(validaPlantillaEmpleado) && "N".equals(req.PlantillaEmpleadoProductoHabilitado) && !"FMM".equals(in.p_IdModulo)){
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"PRODUCTO_NO_HABILITADO_EMPLEADO",mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
				}
			}
			
			//Estas validaciones solo se realizan en Spot origen Mesa
			if(in.p_UsaReglasNegocio.equals("S")){
				params = new HashMap<>();
				params.put("NumeroOperacion", numeroOperacion);
				
				//VALIDACIONES PARIDAD Y TC
				List<LimitesParidadYTipoCambio> limitesParidadYTipoCambio = _sql.query(conn, SQL_SELECT_LIMITES_PARIDAD_Y_TIPO_CAMBIO, params, LimitesParidadYTipoCambio.class);
				
				for (LimitesParidadYTipoCambio ptc: limitesParidadYTipoCambio){
					OperacionObjIn ope = new OperacionObjIn();
					OperacionSpotEventoAutorizacion evt = new OperacionSpotEventoAutorizacion();
					boolean entraIF = false;
					
					if(ptc.IdMonedaSecundaria.equals("CLP") && (ptc.TipoCambioCierre.compareTo(ptc.LimiteSuperiorTC) > 0 || ptc.TipoCambioCierre.compareTo(ptc.LimiteInferiorTC) < 0)){
						mapMsg.put("NumeroOperacion", ptc.NumeroOperacion);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_AUTORIZACION,"TC_FUERA_LIMITES",mapMsg,conn,in.p_IdEmpresa));
						Id++;
						advCont++;
						entraIF = true;
						
						evt.IdTipoEvento = "LITCSP";
						evt.Glosa = "TC_FUERA_LIMITES";
					}
					if(ptc.IdMonedaSecundaria.equals("CLP") && ptc.Nemotecnico.equals("DISTRI") && (ptc.TipoCambioCosto.compareTo(ptc.LimiteSuperiorTC) > 0 || ptc.TipoCambioCosto.compareTo(ptc.LimiteInferiorTC) < 0)){
						mapMsg.put("NumeroOperacion", ptc.NumeroOperacion);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_AUTORIZACION,"TC_COSTO_FUERA_LIMITES",mapMsg,conn,in.p_IdEmpresa));
						Id++;
						advCont++;
						entraIF = true;
						
						evt.IdTipoEvento = "LITCSP";
						evt.Glosa = "TC_FUERA_LIMITES";
					}
					if(!ptc.IdMonedaSecundaria.equals("CLP") && (ptc.ParidadCierre.compareTo(ptc.LimiteSuperiorPAR) > 0 || ptc.ParidadCierre.compareTo(ptc.LimiteInferiorPAR) < 0)){
						mapMsg.put("NumeroOperacion", ptc.NumeroOperacion);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_AUTORIZACION,"PARIDAD_FUERA_LIMITES",mapMsg,conn,in.p_IdEmpresa));
						Id++;
						advCont++;
						entraIF = true;
						
						evt.IdTipoEvento = "LIPASP";
						evt.Glosa = "PARIDAD_FUERA_LIMITES";
					}
					if(!ptc.IdMonedaSecundaria.equals("CLP") && ptc.Nemotecnico.equals("ARBDIS") && (ptc.ParidadCosto.compareTo(ptc.LimiteSuperiorPAR) > 0 || ptc.ParidadCosto.compareTo(ptc.LimiteInferiorPAR) < 0)){
						mapMsg.put("NumeroOperacion", ptc.NumeroOperacion);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_AUTORIZACION,"PARIDAD_COSTO_FUERA_LIMITES",mapMsg,conn,in.p_IdEmpresa));
						Id++;
						advCont++;
						entraIF = true;
						
						evt.IdTipoEvento = "LIPASP";
						evt.Glosa = "PARIDAD_FUERA_LIMITES";
					}
					
					if(entraIF){
						entraIF = false;
						
						ope.NumeroOperacion	= ptc.NumeroOperacion;
						ope.Estado			= "PEN";
						operacionesInUpdateEstado.add(ope);
						
						evt.NumeroOperacion = ptc.NumeroOperacion;
						evtList.add(evt);
					}
				}
		
				
			}
			
			//Retorna si existen errores 
			errCont = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			if (!errCont.equals(0L) ){
				return out;
			}
			
			
			/***** CALCULOS NEGOCIO *****/
			
			//CALCULAR MARGEN
			if(in.p_CalculaMargen.equals("S")){
				String Canal = "Punta";
					for(OperacionObjIn ope : in.p_DatosOperacion)
						if("AGE".equalsIgnoreCase(ope.IdTipoCanalIngreso))
							Canal = "Agente";
						else if("CLI".equalsIgnoreCase(ope.IdTipoCanalIngreso))
							Canal = "Cliente";

				params.clear();
				params.put("IdEmpresa", in.p_IdEmpresa);
				params.put("MonedaLocal", monedaSistema);
				params.put("NumeroOperacion", numeroOperacion);
				params.put("Canal", Canal);
				
				List<PuntasCalcularMargen> puntasCalcularMargen = _sql.query(conn, SQL_SELECT_PUNTAS_CALCULAR_MARGEN, params, PuntasCalcularMargen.class);
				
				for (PuntasCalcularMargen puntas: puntasCalcularMargen){
					OperacionObjIn ope = new OperacionObjIn();
					
					if(puntas.IdTipoOperacion.equals("COMSPT") && puntas.IdMonedaSecundaria.equals(monedaSistema) && !puntas.Nemotecnico.equals("INTERB") && !puntas.Nemotecnico.equals("ARBINT")){
						if(puntas.CheckPrecioPorTramo.equals("S")){
							puntas.Margen = (puntas.ValorCompra.subtract(puntas.TipoCambioCierre)).multiply(puntas.MontoMonedaPrincipal);
						}
						else{
							puntas.Margen = (puntas.PuntaCompra.subtract(puntas.TipoCambioCierre)).multiply(puntas.MontoMonedaPrincipal);
						}
					}
					if(puntas.IdTipoOperacion.equals("COMSPT") && !puntas.IdMonedaSecundaria.equals(monedaSistema) && !puntas.Nemotecnico.equals("INTERB")){
						if(!(puntas.ParidadCosto.compareTo(BigDecimal.ZERO) == 0)){
							puntas.Margen = (puntas.ParidadCosto.subtract(puntas.ParidadCierre)).multiply(puntas.MontoMonedaPrincipal);
						}
						else{
							puntas.Margen = (puntas.ParidadMercado.subtract(puntas.ParidadCierre)).multiply(puntas.MontoMonedaPrincipal);
						}
					}
					if(puntas.IdTipoOperacion.equals("VENSPT") && puntas.IdMonedaSecundaria.equals(monedaSistema) && !puntas.Nemotecnico.equals("INTERB") && !puntas.Nemotecnico.equals("ARBINT")){
						if(puntas.CheckPrecioPorTramo.equals("S")){
							puntas.Margen = (puntas.TipoCambioCierre.subtract(puntas.ValorVenta)).multiply(puntas.MontoMonedaPrincipal);
						}
						else{
							puntas.Margen = (puntas.TipoCambioCierre.subtract(puntas.PuntaVenta)).multiply(puntas.MontoMonedaPrincipal);
						}
					}
					if(puntas.IdTipoOperacion.equals("VENSPT") && !puntas.IdMonedaSecundaria.equals(monedaSistema) && !puntas.Nemotecnico.equals("INTERB")){
						if(!(puntas.ParidadCosto.compareTo(BigDecimal.ZERO) == 0)){
							puntas.Margen = (puntas.ParidadCierre.subtract(puntas.ParidadCosto)).multiply(puntas.MontoMonedaPrincipal);
						}
						else{
							puntas.Margen = (puntas.ParidadCierre.subtract(puntas.ParidadMercado)).multiply(puntas.MontoMonedaPrincipal);
						}
					}
					ope.NumeroOperacion	= puntas.NumeroOperacion;
					ope.Margen			= puntas.Margen;
					ope.Utilidad		= puntas.Margen;
					ope.PuntaCompra		= puntas.PuntaCompra;
					ope.PuntaVenta		= puntas.PuntaVenta;
					operacionesInUpdateMargen.add(ope);
				}
			}
			
			//CALCULO DE CAMPOS POR INSTRUMENTO
			List<Long> secuenciaIdOperacionSpot = Utils.getSecuencia(conn, "BSCL.Mesa.BD.Secuencias::SPT.IdOperacionSpot",cantOpeIn);
			int i = 0;
			
			params = new HashMap<>();
			params.put("IdEmpresa", in.p_IdEmpresa);
			params.put("IdUsuario", in.p_IdUsuario);
			params.put("MonedaLocal", monedaSistema);
			params.put("MonedaEquivalente", monedaEquivalente);
			params.put("FechaHoraSistema", fechaHoraSistemaSecondate);
			params.put("NumeroOperacion", numeroOperacion);
			
			//INTERBANCARIO
			List<OperacionSpot> selectOpeSpotInstrumento = _sql.query(conn, SQL_SELECT_OPERACIONES_SPOT_INTERBANCARIO, params, OperacionSpot.class);
			
			for(OperacionSpot opSpInst: selectOpeSpotInstrumento){
				for(OperacionSpot opSpMod: operacionModificada){
					if(opSpInst.NumeroOperacion.equals(opSpMod.NumeroOperacion)){
						//OperacionSpot opeSpotProceso = new OperacionSpot();
						//Utils.copyPojo(opSpInst, opeSpotProceso);
						
						opSpMod.IdOperacionSpot = secuenciaIdOperacionSpot.get(i);
						opSpMod.Nemotecnico = opSpInst.Nemotecnico;
						if(opSpInst.IdTipoCanalIngreso.equals("CRM")){
							opSpMod.IdTipoOperacion = opSpInst.IdTipoOperacion.equals("COMSPT") ? "VENSPT" : "COMSPT";
						}
						
						if(opSpInst.IdMonedaPrincipal.equals(monedaEquivalente)){
							opSpMod.MontoMonedaPrincipalEquivalente = opSpInst.MontoMonedaPrincipal;
						}
						else if(opSpInst.IdMonedaSecundaria.equals(monedaEquivalente)){
							opSpMod.MontoMonedaPrincipalEquivalente = opSpInst.MontoMonedaSecundaria;
						}
						else if(!opSpInst.IdMonedaPrincipal.equals(monedaEquivalente) && opSpInst.IdMonedaSecundaria.equals(monedaSistema)){
							if(opSpInst.MultiplicaODivide.equals("D") && !(opSpInst.ParidadMercado.compareTo(BigDecimal.ZERO) == 0)){
								opSpMod.MontoMonedaPrincipalEquivalente = opSpInst.MontoMonedaPrincipal.divide(opSpInst.ParidadMercado,8,RoundingMode.HALF_UP);
							}
							else if(opSpInst.MultiplicaODivide.equals("M")){
								opSpMod.MontoMonedaPrincipalEquivalente = opSpInst.MontoMonedaPrincipal.multiply(opSpInst.ParidadMercado);
							}
						}
						
						if(opSpInst.IdTipoOperacion.equals("COMSPT")){
							if(opSpInst.IdMonedaPrincipal.equals(monedaEquivalente)){
								opSpMod.ResultadoMonedaLocalPuntas = (opSpInst.PuntaCompra.subtract(opSpInst.TipoCambioCierre)).multiply(opSpInst.MontoMonedaPrincipal);
								opSpMod.ResultadoMonedaLocalBidAsk = (opSpInst.PuntaBid.subtract(opSpInst.TipoCambioCierre)).multiply(opSpInst.MontoMonedaPrincipal);
								opSpMod.ResultadoMonedaLocalDO = (opSpInst.ValorDolarObservado.subtract(opSpInst.TipoCambioCierre)).multiply(opSpInst.MontoMonedaPrincipal);
								opSpMod.ResultadoMonedaLocalTCSpot = (opSpInst.ValorTCSpot.subtract(opSpInst.TipoCambioCierre)).multiply(opSpInst.MontoMonedaPrincipal);
							}
							else{
								opSpMod.ResultadoMonedaLocalPuntas = (opSpInst.PuntaCompra.subtract(opSpInst.TipoCambioCierre)).multiply(opSpInst.MontoMonedaPrincipal).multiply(opSpInst.ParidadMercado);
								opSpMod.ResultadoMonedaLocalBidAsk = (opSpInst.PuntaBid.subtract(opSpInst.TipoCambioCierre)).multiply(opSpInst.MontoMonedaPrincipal).multiply(opSpInst.ParidadMercado);
								opSpMod.ResultadoMonedaLocalDO = (opSpInst.ValorDolarObservado.subtract(opSpInst.TipoCambioCierre)).multiply(opSpInst.MontoMonedaPrincipal).multiply(opSpInst.ParidadMercado);
								opSpMod.ResultadoMonedaLocalTCSpot = (opSpInst.ValorTCSpot.subtract(opSpInst.TipoCambioCierre)).multiply(opSpInst.MontoMonedaPrincipal).multiply(opSpInst.ParidadMercado);
							}
							
							//Compra && Existe PosicionCierre
							if(!(opSpInst.CierreIngresadoPosMonPri.compareTo(BigDecimal.ZERO) == 0)){
								opSpMod.ResultadoMonedaLocalTCCierreMesa = (opSpInst.CierreIngresadoPosMonPri.subtract(opSpInst.TipoCambioCierre)).multiply(opSpInst.MontoMonedaPrincipal);
							}
							//Compra && NO existe PosicionCierre para la monedaExtranjera
							else if(opSpInst.CierreIngresadoPosMonPri.compareTo(BigDecimal.ZERO) == 0 && !(opSpInst.CierreIngresadoPosMonEqui.compareTo(BigDecimal.ZERO) == 0)){
								if(opSpInst.ParidadBid.compareTo(BigDecimal.ZERO) == 0){
									opSpMod.ResultadoMonedaLocalTCCierreMesa = BigDecimal.ZERO;
								}
								else if(opSpInst.IdMonedaPrincipal.equals(monedaEquivalente)){
									opSpMod.ResultadoMonedaLocalTCCierreMesa = (opSpInst.CierreIngresadoPosMonEqui.subtract(opSpInst.TipoCambioCierre.divide(opSpInst.ParidadBid,8,RoundingMode.HALF_UP))).multiply(opSpInst.MontoMonedaPrincipal);
								}
								else{
									opSpMod.ResultadoMonedaLocalTCCierreMesa = (opSpInst.CierreIngresadoPosMonEqui.subtract(opSpInst.TipoCambioCierre.divide(opSpInst.ParidadBid,8,RoundingMode.HALF_UP))).multiply(opSpInst.MontoMonedaPrincipal).multiply(opSpInst.ParidadMercado);
								}
							}

							if(opSpInst.IdMonedaPrincipal.equals(monedaEquivalente)){
								opSpMod.ResultadoMonedaLocalConsolidado = BigDecimal.ZERO;
							}
							else{
								if(opSpInst.ParidadBid.compareTo(BigDecimal.ZERO) == 0 ){
									opSpMod.ResultadoMonedaLocalConsolidado = opSpInst.TipoCambioCierre.divide(opSpInst.ParidadMercado,8,RoundingMode.HALF_UP).multiply(opSpInst.MontoMonedaPrincipal).multiply(opSpInst.ParidadMercado);								
								}else if(opSpInst.MultiplicaODivide.equals("M")){
									//si se utiliza la paridad BID se debe considerar si moneda fuerte o moneda debil
									//MultiplicaODivide = M significa que es moneda Fuerte y se divide, si MultiplicaODivide = D significa que es debil y se multiplica
									opSpMod.ResultadoMonedaLocalConsolidado = opSpInst.TipoCambioCierre.divide(opSpInst.ParidadBid,8,RoundingMode.HALF_UP).multiply(opSpInst.MontoMonedaPrincipal).multiply(opSpInst.ParidadMercado);

								}else {
									opSpMod.ResultadoMonedaLocalConsolidado = opSpInst.TipoCambioCierre.multiply(opSpInst.ParidadBid).multiply(opSpInst.MontoMonedaPrincipal.divide(opSpInst.ParidadMercado,8,RoundingMode.HALF_UP));
									
								}
							}
						}
						else if(opSpInst.IdTipoOperacion.equals("VENSPT")){
							if(opSpInst.IdMonedaPrincipal.equals(monedaEquivalente)){
								opSpMod.ResultadoMonedaLocalPuntas = (opSpInst.TipoCambioCierre.subtract(opSpInst.PuntaVenta)).multiply(opSpInst.MontoMonedaPrincipal);
								opSpMod.ResultadoMonedaLocalBidAsk = (opSpInst.TipoCambioCierre.subtract(opSpInst.PuntaAsk)).multiply(opSpInst.MontoMonedaPrincipal);
								opSpMod.ResultadoMonedaLocalDO = (opSpInst.TipoCambioCierre.subtract(opSpInst.ValorDolarObservado)).multiply(opSpInst.MontoMonedaPrincipal);
								opSpMod.ResultadoMonedaLocalTCSpot = (opSpInst.TipoCambioCierre.subtract(opSpInst.ValorTCSpot)).multiply(opSpInst.MontoMonedaPrincipal);
								opSpMod.ResultadoMonedaLocalConsolidado = BigDecimal.ZERO;
							}
							else{
								opSpMod.ResultadoMonedaLocalPuntas = (opSpInst.TipoCambioCierre.subtract(opSpInst.PuntaVenta)).multiply(opSpInst.MontoMonedaPrincipal).multiply(opSpInst.ParidadMercado);
								opSpMod.ResultadoMonedaLocalBidAsk = (opSpInst.TipoCambioCierre.subtract(opSpInst.PuntaAsk)).multiply(opSpInst.MontoMonedaPrincipal).multiply(opSpInst.ParidadMercado);
								opSpMod.ResultadoMonedaLocalDO = (opSpInst.TipoCambioCierre.subtract(opSpInst.ValorDolarObservado)).multiply(opSpInst.MontoMonedaPrincipal).multiply(opSpInst.ParidadMercado);
								opSpMod.ResultadoMonedaLocalTCSpot = (opSpInst.TipoCambioCierre.subtract(opSpInst.ValorTCSpot)).multiply(opSpInst.MontoMonedaPrincipal).multiply(opSpInst.ParidadMercado);
								opSpMod.ResultadoMonedaLocalConsolidado = opSpInst.ParidadAsk.divide(opSpInst.TipoCambioCierre,8,RoundingMode.HALF_UP).multiply(opSpInst.MontoMonedaPrincipal).multiply(opSpInst.ParidadMercado);
							}
							
							//Venta && Existe PosicionCierre
							if(!(opSpInst.CierreIngresadoPosMonPri.compareTo(BigDecimal.ZERO) == 0)){
								opSpMod.ResultadoMonedaLocalTCCierreMesa = (opSpInst.TipoCambioCierre.subtract(opSpInst.CierreIngresadoPosMonPri)).multiply(opSpInst.MontoMonedaPrincipal);
							}
							//Venta && NO existe PosicionCierre para la monedaExtranjera
							else if(opSpInst.CierreIngresadoPosMonPri.compareTo(BigDecimal.ZERO) == 0 && !(opSpInst.CierreIngresadoPosMonEqui.compareTo(BigDecimal.ZERO) == 0)){

								if(opSpInst.ParidadAsk.compareTo(BigDecimal.ZERO) == 0 ){
									opSpMod.ResultadoMonedaLocalConsolidado = opSpInst.TipoCambioCierre.divide(opSpInst.ParidadMercado,8,RoundingMode.HALF_UP).multiply(opSpInst.MontoMonedaPrincipal).multiply(opSpInst.ParidadMercado);
									
								}else if(opSpInst.MultiplicaODivide.equals("M")){
									//si se utiliza la paridad ASK se debe considerar si moneda fuerte o moneda debil
									//MultiplicaODivide = M significa que es moneda Fuerte y se divide, si MultiplicaODivide = D significa que es debil y se multiplica
									opSpMod.ResultadoMonedaLocalConsolidado = opSpInst.TipoCambioCierre.divide(opSpInst.ParidadAsk,8,RoundingMode.HALF_UP).multiply(opSpInst.MontoMonedaPrincipal).multiply(opSpInst.ParidadMercado);
									
								}else {
									opSpMod.ResultadoMonedaLocalConsolidado = opSpInst.TipoCambioCierre.multiply(opSpInst.ParidadAsk).multiply(opSpInst.MontoMonedaPrincipal.divide(opSpInst.ParidadMercado,8,RoundingMode.HALF_UP));
									
								}
							}
						}
						opSpMod.IdOperacionSpotVigente = opSpMod.IdOperacionSpot;
						//operacionModificada.add(opeSpotProceso);
						i++;
					}
				}
			}
			
			
			//ARBITRAJE INTERBANCARIO
			selectOpeSpotInstrumento = _sql.query(conn, SQL_SELECT_OPERACIONES_SPOT_ARBITRAJE_INTERBANCARIO, params, OperacionSpot.class);
			
			for(OperacionSpot opSpInst: selectOpeSpotInstrumento){
				for(OperacionSpot opSpMod: operacionModificada){
					if(opSpInst.NumeroOperacion.equals(opSpMod.NumeroOperacion)){
						//OperacionSpot opeSpotProceso = new OperacionSpot();
						//Utils.copyPojo(opSpInst, opeSpotProceso);
						
						opSpMod.IdOperacionSpot = secuenciaIdOperacionSpot.get(i);
						opSpMod.Nemotecnico = opSpInst.Nemotecnico;
						if(opSpInst.IdTipoCanalIngreso.equals("CRM")){
							opSpMod.IdTipoOperacion = opSpInst.IdTipoOperacion.equals("COMSPT") ? "VENSPT" : "COMSPT";
						}
						
						if(opSpInst.IdMonedaPrincipal.equals(monedaEquivalente)){
							opSpMod.MontoMonedaPrincipalEquivalente = opSpInst.MontoMonedaPrincipal;
						}
						else if(opSpInst.IdMonedaSecundaria.equals(monedaEquivalente)){
							opSpMod.MontoMonedaPrincipalEquivalente = opSpInst.MontoMonedaSecundaria;
						}
						else if(!opSpInst.IdMonedaPrincipal.equals(monedaEquivalente) && opSpInst.IdMonedaSecundaria.equals(monedaSistema)){
							if(opSpInst.MultiplicaODivide.equals("D") && !(opSpInst.ParidadMercado.compareTo(BigDecimal.ZERO) == 0)){
								opSpMod.MontoMonedaPrincipalEquivalente = opSpInst.MontoMonedaPrincipal.divide(opSpInst.ParidadMercado,8,RoundingMode.HALF_UP);
							}
							else if(opSpInst.MultiplicaODivide.equals("M")){
								opSpMod.MontoMonedaPrincipalEquivalente = opSpInst.MontoMonedaPrincipal.multiply(opSpInst.ParidadMercado);
							}
						}
						
						if(opSpInst.IdTipoOperacion.equals("COMSPT")){
							opSpMod.ResultadoMonedaLocalPuntas = opSpInst.Margen.multiply(opSpInst.PuntaCompra);
							opSpMod.ResultadoMonedaLocalBidAsk = opSpInst.Margen.multiply(opSpInst.PuntaBid);
							
							opSpMod.ResultadoMonedaLocalTCSpot = opSpInst.Margen.multiply(opSpInst.ValorTCSpot);
							opSpMod.MargenBidAsk = (opSpInst.ParidadBid.subtract(opSpInst.ParidadCierre)).multiply(opSpInst.MontoMonedaPrincipal);
						}
						else if(opSpInst.IdTipoOperacion.equals("VENSPT")){
							opSpMod.ResultadoMonedaLocalPuntas = opSpInst.Margen.multiply(opSpInst.PuntaVenta);
							opSpMod.ResultadoMonedaLocalBidAsk = opSpInst.Margen.multiply(opSpInst.PuntaAsk);
							opSpMod.MargenBidAsk = (opSpInst.ParidadCierre.subtract(opSpInst.ParidadAsk)).multiply(opSpInst.MontoMonedaPrincipal);
						}
						opSpMod.ResultadoMonedaLocalDO = opSpInst.Margen.multiply(opSpInst.ValorDolarObservado);
						opSpMod.ResultadoMonedaLocalTCSpot = opSpInst.Margen.multiply(opSpInst.ValorTCSpot);
						
						opSpMod.IdOperacionSpotVigente = opSpMod.IdOperacionSpot;
						//operacionModificada.add(opeSpotProceso);
						i++;
					}
				}
			}
			
			
			//DISTRIBUCION
			selectOpeSpotInstrumento = _sql.query(conn, SQL_SELECT_OPERACIONES_SPOT_DISTRIBUCION, params, OperacionSpot.class);
			
			for(OperacionSpot opSpInst: selectOpeSpotInstrumento){
				for(OperacionSpot opSpMod: operacionModificada){
					if(opSpInst.NumeroOperacion.equals(opSpMod.NumeroOperacion)){
						//OperacionSpot opeSpotProceso = new OperacionSpot();
						//Utils.copyPojo(opSpInst, opeSpotProceso);
						
						opSpMod.IdOperacionSpot = secuenciaIdOperacionSpot.get(i);
						opSpMod.Nemotecnico = opSpInst.Nemotecnico;
						if(opSpInst.IdTipoCanalIngreso.equals("CRM")){
							opSpMod.IdTipoOperacion = opSpInst.IdTipoOperacion.equals("COMSPT") ? "VENSPT" : "COMSPT";
						}
						
						if(opSpInst.IdMonedaPrincipal.equals(monedaEquivalente)){
							opSpMod.MontoMonedaPrincipalEquivalente = opSpInst.MontoMonedaPrincipal;
						}
						else if(opSpInst.IdMonedaSecundaria.equals(monedaEquivalente)){
							opSpMod.MontoMonedaPrincipalEquivalente = opSpInst.MontoMonedaSecundaria;
						}
						else if(!opSpInst.IdMonedaPrincipal.equals(monedaEquivalente) && opSpInst.IdMonedaSecundaria.equals(monedaSistema)){
							if(opSpInst.MultiplicaODivide.equals("D") && !(opSpInst.ParidadMercado.compareTo(BigDecimal.ZERO) == 0)){
								opSpMod.MontoMonedaPrincipalEquivalente = opSpInst.MontoMonedaPrincipal.divide(opSpInst.ParidadMercado,8,RoundingMode.HALF_UP);
							}
							else if(opSpInst.MultiplicaODivide.equals("M")){
								opSpMod.MontoMonedaPrincipalEquivalente = opSpInst.MontoMonedaPrincipal.multiply(opSpInst.ParidadMercado);
							}
						}
						
						if(opSpInst.IdTipoOperacion.equals("COMSPT")){
							if(opSpInst.ParidadBid.compareTo(BigDecimal.ZERO) == 0){
								opSpMod.ResultadoMonedaLocalConsolidado = BigDecimal.ZERO;
							}
							else if(opSpInst.IdMonedaPrincipal.equals(monedaEquivalente)){
								opSpMod.ResultadoMonedaLocalConsolidado = opSpInst.MontoMonedaPrincipal.multiply(opSpInst.TipoCambioCosto);
							}
							else{
								opSpMod.ResultadoMonedaLocalConsolidado = opSpInst.MontoMonedaPrincipal.multiply(opSpInst.TipoCambioCosto).multiply(opSpInst.ParidadMercado).divide(opSpInst.ParidadBid,8,RoundingMode.HALF_UP);
							}
						}
						else if(opSpInst.IdTipoOperacion.equals("VENSPT")){
							if(opSpInst.ParidadAsk.compareTo(BigDecimal.ZERO) == 0){
								opSpMod.ResultadoMonedaLocalConsolidado = BigDecimal.ZERO;
							}
							else if(opSpInst.IdMonedaPrincipal.equals(monedaEquivalente)){
								opSpMod.ResultadoMonedaLocalConsolidado = opSpInst.MontoMonedaPrincipal.multiply(opSpInst.TipoCambioCosto);
							}
							else{
								opSpMod.ResultadoMonedaLocalConsolidado = opSpInst.MontoMonedaPrincipal.multiply(opSpInst.TipoCambioCosto).multiply(opSpInst.ParidadMercado).divide(opSpInst.ParidadAsk,8,RoundingMode.HALF_UP);
							}
						}
						opSpMod.IdOperacionSpotVigente = opSpMod.IdOperacionSpot;
						//operacionModificada.add(opeSpotProceso);
						i++;
					}
				}
			}
			
			
			//ARBITRAJE DISTRIBUCION
			selectOpeSpotInstrumento = _sql.query(conn, SQL_SELECT_OPERACIONES_SPOT_ARBITRAJE_DISTRIBUCION, params, OperacionSpot.class);
			
			for(OperacionSpot opSpInst: selectOpeSpotInstrumento){
				for(OperacionSpot opSpMod: operacionModificada){
					if(opSpInst.NumeroOperacion.equals(opSpMod.NumeroOperacion)){
						//OperacionSpot opeSpotProceso = new OperacionSpot();
						//Utils.copyPojo(opSpInst, opeSpotProceso);
						
						opSpMod.IdOperacionSpot = secuenciaIdOperacionSpot.get(i);
						opSpMod.Nemotecnico = opSpInst.Nemotecnico;
						if(opSpInst.IdTipoCanalIngreso.equals("CRM")){
							opSpMod.IdTipoOperacion = opSpInst.IdTipoOperacion.equals("COMSPT") ? "VENSPT" : "COMSPT";
						}
						
						if(opSpInst.IdMonedaPrincipal.equals(monedaEquivalente)){
							opSpMod.MontoMonedaPrincipalEquivalente = opSpInst.MontoMonedaPrincipal;
						}
						else if(opSpInst.IdMonedaSecundaria.equals(monedaEquivalente)){
							opSpMod.MontoMonedaPrincipalEquivalente = opSpInst.MontoMonedaSecundaria;
						}
						else if(!opSpInst.IdMonedaPrincipal.equals(monedaEquivalente) && opSpInst.IdMonedaSecundaria.equals(monedaSistema)){
							if(opSpInst.MultiplicaODivide.equals("D") && !(opSpInst.ParidadMercado.compareTo(BigDecimal.ZERO) == 0)){
								opSpMod.MontoMonedaPrincipalEquivalente = opSpInst.MontoMonedaPrincipal.divide(opSpInst.ParidadMercado,8,RoundingMode.HALF_UP);
							}
							else if(opSpInst.MultiplicaODivide.equals("M")){
								opSpMod.MontoMonedaPrincipalEquivalente = opSpInst.MontoMonedaPrincipal.multiply(opSpInst.ParidadMercado);
							}
						}
						
						if(opSpInst.IdTipoOperacion.equals("COMSPT")){
							opSpMod.ResultadoMonedaLocalPuntas = opSpInst.Margen.multiply(opSpInst.PuntaCompra);
							opSpMod.ResultadoMonedaLocalBidAsk = opSpInst.Margen.multiply(opSpInst.PuntaBid);
							opSpMod.MargenBidAsk = (opSpInst.ParidadBid.subtract(opSpInst.ParidadCierre)).multiply(opSpInst.MontoMonedaPrincipal);
						}
						else if(opSpInst.IdTipoOperacion.equals("VENSPT")){
							opSpMod.ResultadoMonedaLocalPuntas = opSpInst.Margen.multiply(opSpInst.PuntaVenta);
							opSpMod.ResultadoMonedaLocalBidAsk = opSpInst.Margen.multiply(opSpInst.PuntaAsk);
							opSpMod.MargenBidAsk = (opSpInst.ParidadCierre.subtract(opSpInst.ParidadAsk)).multiply(opSpInst.MontoMonedaPrincipal);
						}
						opSpMod.ResultadoMonedaLocalDO = opSpInst.Margen.multiply(opSpInst.ValorDolarObservado);
						opSpMod.ResultadoMonedaLocalTCSpot = opSpInst.Margen.multiply(opSpInst.ValorTCSpot);
						
						opSpMod.IdOperacionSpotVigente = opSpMod.IdOperacionSpot;
						//operacionModificada.add(opeSpotProceso);
						i++;
					}
				}
			}
			
			//Modifica el Estado de las operaciones entrantes
			for(OperacionObjIn opeUpd: operacionesInUpdateEstado){
				for(OperacionSpot opSp: operacionModificada){
					if(opSp.NumeroOperacion.equals(opeUpd.NumeroOperacion)){
						opSp.Estado = opeUpd.Estado;
					}
					
					//Asigna los valores calculados del Margen
					for(OperacionObjIn opeMargenUpd: operacionesInUpdateMargen){
						if(opSp.NumeroOperacion.equals(opeMargenUpd.NumeroOperacion)){
							opSp.Margen = opeMargenUpd.Margen;
							opSp.Utilidad = opeMargenUpd.Margen;
							opSp.PuntaCompra = opeMargenUpd.PuntaCompra;
							opSp.PuntaVenta = opeMargenUpd.PuntaVenta;
						}
					}
					
				}
			}

			/***** LIMITES *****/
			for (OperacionSpot operacionSpot : operacionModificada) {
				operacionSpot.IdInterno = Long.valueOf(operacionSpot.NumeroOperacion);
				operacionSpot.CodigoOperador = empleadoUsuario.CodigoEmpleado;
			}
			
			ValidarLimitesCuentaPropiaSpot.SalidaValidarLimitesCuentaPropiaSpot salidaValidarLimitesCuentaPropiaSpot = validarLimitesCuentaPropia(conn, in, operacionModificada);
			if (salidaValidarLimitesCuentaPropiaSpot.P_OUT_ERRORESCONTADOR > 0) {
				out.P_OUT_MENSAJES.addAll(salidaValidarLimitesCuentaPropiaSpot.P_OUT_MENSAJES);
				return out;
			}
			
			if (salidaValidarLimitesCuentaPropiaSpot.P_OUT_VALIDACIONES.size() > 0) {
				AtomicLong totalAutLimCtaPropia = new AtomicLong(0L);
				Map<Long, Validacion> validacionLimCtaPropiaPorIdInterno = salidaValidarLimitesCuentaPropiaSpot.P_OUT_VALIDACIONES.stream().collect(Collectors.toMap(u -> u.IdInternoOperacion, u -> u));
				operacionModificada = operacionModificada.stream()
						.map(operacionSpot -> Pair.of(operacionSpot, validacionLimCtaPropiaPorIdInterno.get(operacionSpot.IdInterno)))
						.peek(parOperacionValidacion -> {
							Validacion validacion = parOperacionValidacion.getRight();
							if (Objects.nonNull(validacion)) {
								if (_cl.TIPO_VALIDACION_OMITIR.equals(validacion.TipoComportamiento) == false) {
									out.P_OUT_MENSAJES.add(validacion.Mensaje);
								}
								if (_cl.TIPO_VALIDACION_AUTORIZACION.equals(validacion.TipoComportamiento) || _cl.TIPO_VALIDACION_ADVERTENCIA.equals(validacion.TipoComportamiento)) {
									out.P_OUT_ADVERTENCIASCONTADOR++;
								}
								if (_cl.TIPO_VALIDACION_RECHAZAR.equals(validacion.TipoComportamiento)) {
									out.P_OUT_ERRORESCONTADOR++;
								}
								if (_cl.TIPO_VALIDACION_AUTORIZACION.equals(validacion.TipoComportamiento)) {
									totalAutLimCtaPropia.getAndIncrement();
									OperacionSpot operacionSpot = parOperacionValidacion.getLeft();
									operacionSpot.Estado = "PEN";
									
									OperacionSpotEventoAutorizacion eventoAutorizacion = new OperacionSpotEventoAutorizacion();
									eventoAutorizacion.IdTipoEvento = "LIMSPT";
									eventoAutorizacion.NumeroOperacion = operacionSpot.NumeroOperacion;
									eventoAutorizacion.Glosa = "FUERA_LIM_CTA_PROPIA";
									evtList.add(eventoAutorizacion);
								}
							}
						})
						.filter(parOperacionValidacion -> parOperacionValidacion.getRight() == null || _cl.TIPO_VALIDACION_RECHAZAR.equals(parOperacionValidacion.getRight().TipoComportamiento) == false)
						.map(Pair::getLeft)
						.collect(Collectors.toList());
				
				Id += totalAutLimCtaPropia.get();
				advCont += totalAutLimCtaPropia.get();
			}
			
			if (operacionModificada.isEmpty()) {
				return out;
			}
			

			/***** OPERACIONES SPOT ORIGINALES QUEDAN ESTADO MOD Y EL CAMPO IdOperacionSpotVigente QUEDA CON LA REFERENCIA DEL NUEVO IdOperacionSpot *****/
			_sql.batch(conn, SQL_UPDATE_OPERACIONES_SPOT_ORIGINALES, operacionModificada, OperacionSpot.class);
			
			
			/***** VALIDACION SALDO FLUJO CAJA (Esta validacion solo se realizan en Spot origen Mesa y debe ir despues de actualizar las operaciones spot originales para que no las considere el flujo caja) *****/
			if(in.p_UsaReglasNegocio.equals("S")){
				params = new HashMap<>();
				params.put("NumeroOperacion", numeroOperacion);
				
				SaldoFlujoCaja.ServiceEntrada entradaFlujoCaja = new SaldoFlujoCaja.ServiceEntrada();
				HashSet<SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR> portafoliosFlujoCaja = new HashSet<>();
				
				List<PortafoliosFlujoCaja> selectPortafoliosFlujoCaja = _sql.query(conn, SQL_SELECT_PORTAFOLIOS_FLUJO_CAJA, params, PortafoliosFlujoCaja.class);
				
				//Obtencion fecha maxima flujo caja
				Date fechaMaximaLiquidacion = Fecha.toDate(fechaSistema);
				for(FormaPagoObjIn frmPago : in.p_DatosFormaPago){
					Date fechaLiqSol = frmPago.FechaLiquidacion == null ? fechaMaximaLiquidacion : frmPago.FechaLiquidacion;//liquidacionPorConfirmar.equals("S") ? "PEN" : "CNF";
					if(fechaLiqSol.compareTo(fechaMaximaLiquidacion) == 1){
						fechaMaximaLiquidacion = fechaLiqSol;
					}
				}
								
								
				for (PortafoliosFlujoCaja pfc: selectPortafoliosFlujoCaja){
					SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR port = new SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR();
					port.IdPortafolio = pfc.IdPortafolio;
					port.IdMoneda = pfc.IdMoneda;
					portafoliosFlujoCaja.add(port);
				}
								

				if (portafoliosFlujoCaja.size() > 0) {
			    	entradaFlujoCaja.P_IDEMPRESA = in.p_IdEmpresa;
					entradaFlujoCaja.P_FECHADESDE = fechaSistema;
					entradaFlujoCaja.P_FECHAHASTA = Fecha.stringToSqlDate(fechaMaximaLiquidacion);
					
					entradaFlujoCaja.P_USO = "INGRESO_OPERACION_SPOT";
					entradaFlujoCaja.P_DESPLEGARENUI = "N";
					entradaFlujoCaja.P_AGRUPARPORORIGEN = "N";
					entradaFlujoCaja.P_PORTAFOLIOACONSULTAR = new ArrayList<ObjPORTAFOLIOACONSULTAR>(portafoliosFlujoCaja);
					
					SaldoFlujoCaja saldoFlujoCajaService = new SaldoFlujoCaja();
					SaldoFlujoCaja.ServiceSalida salidaFlujoCaja = saldoFlujoCajaService.logica(conn,entradaFlujoCaja);
					
					Map<String, List<FormaPagoObjIn>> hashFormaPago = in.p_DatosFormaPago.parallelStream().filter(c -> "P".equalsIgnoreCase(c.RecibePagaPortafolio)).collect(Collectors.groupingBy(ope->ope.IdOperacion.toString()));
					for (ObjFLUJOCAJA flj: salidaFlujoCaja.P_OUT_FLUJOCAJA) {
						for (PortafoliosFlujoCaja pfc: selectPortafoliosFlujoCaja) {
							//Se obtiene fecha liquidacion spot
							String fechaLiqSpotIn = fechaSistema;
							List<FormaPagoObjIn> listNumOperacion = hashFormaPago.get(pfc.NumeroOperacion);
							if(listNumOperacion.size() > 0) {
								if( listNumOperacion.get(0).FechaLiquidacion != null)
								{
									if( listNumOperacion.get(0).FechaLiquidacion.after(Fecha.toDateTime(fechaLiqSpotIn)))
										fechaLiqSpotIn = listNumOperacion.get(0).FechaLiquidacion.toString(); 
								}
							}
				        	if(flj.IdPortafolio.equals(pfc.IdPortafolio) && flj.IdMoneda.equals(pfc.IdMoneda) && flj.FechaSaldo.equals(fechaLiqSpotIn) && pfc.Monto.compareTo(flj.Saldo) > 0){
				        		for(FormaPagoObjIn frmp : in.p_DatosFormaPago){
				        			if(frmp.RecibePagaPortafolio.equalsIgnoreCase("R") && frmp.IdFormaDePago.contains("_CIN_")){
				        				mapMsg.put("NumeroOperacion", pfc.NumeroOperacion);
										out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"CLIENTE SIN SALDO EN CUENTA INVERSION"+ " FOLIO OPERACION: "+ pfc.NumeroOperacion,mapMsg,conn,in.p_IdEmpresa));
										Id++;
				        			}
				        		}

				        	}
				        }
					}
			    }
			
				//Retorna si existen errores 
				errCont = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
				if (!errCont.equals(0L) ){
					return out;
				}
			}
					

			
			
			
			/***** AUTORIZACIONES *****/
			if(advCont > 0L){
				CrearEventoAutorizacionMasivo eventoAutorizacion = new CrearEventoAutorizacionMasivo();
				CrearEventoAutorizacionMasivo.ServiceEntrada entradaEventoAutorizacion = new CrearEventoAutorizacionMasivo.ServiceEntrada();
				entradaEventoAutorizacion.P_IDEMPRESA = in.p_IdEmpresa;
				entradaEventoAutorizacion.P_IDMODULO = in.p_IdModulo;
				entradaEventoAutorizacion.P_IDUSUARIO = in.p_IdUsuario;
				entradaEventoAutorizacion.P_SIMULAROPERACION = "N";
				entradaEventoAutorizacion.P_USAREGLASNEGOCIO = "N";
				
				for(OperacionSpotEventoAutorizacion evt: evtList){
					for(OperacionSpot opSp: operacionModificada){
						if(opSp.NumeroOperacion.equals(evt.NumeroOperacion)){
							String idConcepto = "LIMSPT";
							if(opSp.Nemotecnico.equals("DISTRI") || opSp.Nemotecnico.equals("ARBDIS")){
								idConcepto = "LISDCP";
							}
							else if(opSp.Nemotecnico.equals("INTERB") || opSp.Nemotecnico.equals("ARBINT")){
								idConcepto = "LISICP";
							}
							
							CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
							evento.IdTipoEvento = evt.IdTipoEvento;
							evento.IdModuloSolicita = in.p_IdModulo;
							evento.IdUsuarioSolicita = in.p_IdUsuario;
							evento.Fecha = Fecha.toStringDate(opSp.FechaOperacion);
							evento.IdTipoOperacion = opSp.IdTipoOperacion;
							evento.IdConcepto = idConcepto;
							evento.NumeroOperacionConcepto = opSp.IdOperacionSpot.toString();
							evento.FechaOperacionConcepto = "";
							evento.IdCliente = opSp.IdCliente;
							evento.IdFuncionario = 0L;
							evento.IdProveedor = 0L;
							evento.IdTipoProducto = opSp.IdTipoProducto;
							evento.IdClasificacionTipoInstrumento = "SP";
							evento.IdTipoInstrumento = "SPT";
							evento.IdInstrumento = opSp.IdInstrumento;
							evento.Nemo = opSp.Nemotecnico;
							evento.Cantidad = 0L;
							evento.Monto = opSp.MontoMonedaPrincipal;
							evento.Precio = opSp.Nemotecnico.equals("DISTRI") || opSp.Nemotecnico.equals("INTERB") ? opSp.TipoCambioCierre : opSp.ParidadCierre;
							evento.Tasa = BigDecimal.ZERO;
							evento.IdMoneda = opSp.IdMonedaPrincipal;
							evento.Glosa = evt.Glosa;
							
							entradaEventoAutorizacion.P_TABLAEVENTOS.add(evento);
						}
					}
				}
				CrearEventoAutorizacionMasivo.ServiceSalida salidaAut = eventoAutorizacion.logica(conn, entradaEventoAutorizacion);
				for (CrearEventoAutorizacionMasivo.ObjERRORESDETALLE error: salidaAut.P_OUT_ERRORESDETALLE) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,error.Codigo,mapMsg,conn,in.p_IdEmpresa));
					Id++;
				}
			}
			
			

			//Iniciar Log
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
			
			/***** INSERT OPERACION SPOT *****/
			for(OperacionSpot opSp: operacionModificada){
				opSp.FechaOperacion = Fecha.toDateTime(fechaHoraSistemaSecondate);
				opSp.MontoMonedaPrincipal = MonedaFormato.formatear(conn, opSp.MontoMonedaPrincipal, "MONTO", opSp.IdMonedaPrincipal).valor;
				opSp.MontoMonedaSecundaria = MonedaFormato.formatear(conn, opSp.MontoMonedaSecundaria, "MONTO", opSp.IdMonedaSecundaria).valor;
				opSp.MontoEquivalente = MonedaFormato.formatear(conn, opSp.MontoEquivalente, "MONTO", opSp.IdMonedaEquivalente).valor;
				mapMsg.put("IdOperacionSpot", opSp.IdOperacionSpot.toString());
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_OK,"OPERACION_SPOT_MODIFICADA",mapMsg,conn,in.p_IdEmpresa));
				Id++;
				
				
				//IRIVAS :: Busca entre las operaciones de la entrada el valor antiguo que tienen estas. Una vez encontrados devuelven el objeto completo. Asi poder usas sus atributos TipoCambio*Antiguo
				OperacionObjIn  OperacionSpotEncontrada = new OperacionObjIn();
				try {
					OperacionSpotEncontrada = in.p_DatosOperacion
					.stream() 
					.filter(opIn -> opIn.NumeroOperacion.equals(opSp.NumeroOperacion) )
					.collect(Collectors.toList())
					.stream()
					.findFirst()
					.get();
				} catch (Exception ex) {
					//En caso de fallar
					OperacionSpotEncontrada = new OperacionObjIn();
					OperacionSpotEncontrada.TipoCambioCierreAntiguo =BigDecimal.ZERO;
					OperacionSpotEncontrada.TipoCambioCostoAntiguo=BigDecimal.ZERO;
					ex.printStackTrace();
				}
				
				//Log
				AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
				log.IdCabeceraLog = opSp.IdOperacionSpot;
				log.IdTipoLog = "MODIFICACION";
				log.IdModulo = in.p_IdModulo;
				log.IdUsuario = in.p_IdUsuario;
				log.IdEmpresa = in.p_IdEmpresa;
				log.FechaSistemaLog = fechaSistema;
				log.IdUsuarioAtendido = in.p_IdUsuario;
				log.IdCliente = opSp.IdCliente;
				log.EstadoCliente = "";
				log.NombreCliente = "";
				log.IdTipoDocumentoIdentidad = "";
				log.IdDocumentoIdentidad = "";
				log.IdPortafolio = opSp.IdPortafolio;
				log.EstadoPortafolio = "";
				log.IdTipoRegistro = "MODIFICAROPERACIONSPOT";
				log.EstadoRegistro = opSp.Estado;
				log.IdTipoOperacion = opSp.IdTipoOperacion;
				log.Folio = opSp.IdOperacionSpotOrigen;
				log.Cantidad = BigDecimal.ZERO;
				log.PrecioTasa = BigDecimal.ZERO;
				log.Monto = opSp.MontoMonedaSecundaria;
				log.IdMonedaOrigen = opSp.IdMonedaPrincipal;
				log.IdMonedaLiquidacion = "";
				log.TipoCambio = opSp.TipoCambioCierre != null ? opSp.TipoCambioCierre.toString() : "";
				log.Nemo = opSp.Nemotecnico;
				log.IdProceso = 0L;
				log.EstadoProceso = opSp.Estado;
				log.NombreProceso = "ModificarOperacionSpot";	
				
				//Costo
				InAgregarLog.DetalleLog detLog = new InAgregarLog.DetalleLog();
				detLog.IdCabeceraLog = opSp.IdOperacionSpot;
				detLog.IdTrabajo=0L;
				detLog.SubRegistro = "Operacion ";
				detLog.CampoRegistro = "TCCosto";
				detLog.ValorAntiguo = OperacionSpotEncontrada.TipoCambioCostoAntiguo != null? OperacionSpotEncontrada.TipoCambioCostoAntiguo.toString() : opSp.TipoCambioCosto != null ? opSp.TipoCambioCosto.toString() : "";
				detLog.ValorNuevo = opSp.TipoCambioCosto != null ? opSp.TipoCambioCosto.toString() : "";
				entradaAgregarLog.p_LogDetalle.add(detLog);
				
				//Cierre
				detLog = new InAgregarLog.DetalleLog();
				detLog.IdTrabajo=0L;
				detLog.IdCabeceraLog = opSp.IdOperacionSpot;
				detLog.SubRegistro = "Operacion ";
				detLog.CampoRegistro = "TCCierre";
				detLog.ValorAntiguo = OperacionSpotEncontrada.TipoCambioCierreAntiguo!=null ? OperacionSpotEncontrada.TipoCambioCierreAntiguo.toString() : opSp.TipoCambioCierre != null ? opSp.TipoCambioCierre.toString() : "" ;
				detLog.ValorNuevo = opSp.TipoCambioCierre != null ? opSp.TipoCambioCierre.toString() : "";
				entradaAgregarLog.p_LogDetalle.add(detLog);
				entradaAgregarLog.p_Log.add(log);	
			}
			_sql.batch(conn, SQL_INSERT_OPERACIONES_SPOT, operacionModificada, OperacionSpot.class);
			
			
			/***** AGREGAR LOG *****/
			if (entradaAgregarLog.p_Log.size() > 0) {
				AgregarLog agregarLogService = new AgregarLog();
				agregarLogService.logica(conn, entradaAgregarLog);
			}
						
			
			/***** FORMAS DE PAGO OPERACION SPOT *****/
			ObtenerDiaSiguiente diaSgte = new ObtenerDiaSiguiente();
			InObtenerDiaSiguiente diaSgteIn = new InObtenerDiaSiguiente();
			diaSgteIn.p_idPais = idPaisSistema;
			diaSgteIn.p_diaHabil = "S";
			for(FormaPagoObjIn fpIn : in.p_DatosFormaPago){
				FechasIn fechas = new FechasIn();
				fechas.fechaInicial = Fecha.toStringDate(((OperacionObjIn) numeroOperacionHash.get(fpIn.IdOperacion.toString())).FechaOperacion);
				fechas.numeroDias = ((FormaDePagoMesa) formaDePagoMesaHash.get(fpIn.IdFormaDePago)).Dias;
				diaSgteIn.p_fechas.add(fechas);
			}
			OutObtenerDiaSiguiente salDiaSgte = diaSgte.logica(conn, diaSgteIn);
			
			//Obtiene los datos generales para las formas de pago
			ArrayList<OperacionSpotFormaDePago> operacionesSpotForPag = new ArrayList<>();
			for(OperacionSpot opSp: operacionModificada){
				for(FormaPagoObjIn fpIn: in.p_DatosFormaPago){
					if(opSp.NumeroOperacion.equals(fpIn.IdOperacion.toString())){
						String fechaCondicionLiquidacion = null;
						if(fpIn.FechaLiquidacion == null || fpIn.FechaLiquidacion.equals(fechaDefecto)) {
							for(FechasOut fec: salDiaSgte.p_out_fechas){
								if(fec.fechaInicial.equals(Fecha.toStringDate(opSp.FechaOperacion)) && fec.numeroDiasInicial.equals(((FormaDePagoMesa) formaDePagoMesaHash.get(fpIn.IdFormaDePago)).Dias)){
									fechaCondicionLiquidacion = fec.fechaCalculada;
									break;
								}						
							}
						}else {
							fechaCondicionLiquidacion = Fecha.stringToSqlDate(fpIn.FechaLiquidacion);
						}
						OperacionSpotFormaDePago osfp = new OperacionSpotFormaDePago();
						osfp.IdInternoPagoOperacion = fpIn.IdInternoPagoOperacion;
						osfp.IdComprobantePagoOperacion = fpIn.IdComprobantePagoOperacion;
						osfp.IdEmpresa = in.p_IdEmpresa;
						osfp.IdOperacion = opSp.IdOperacionSpot;
						osfp.IdIngresoEgreso = 0L;
						osfp.IdAbono = 0L;
						osfp.IdCargo = 0L;
						osfp.IdIngresoEgresoAnticipo = 0L;
						osfp.IdAbonoAnticipo = 0L;
						osfp.IdCargoAnticipo = 0L;
						osfp.IdTipoProducto = opSp.IdTipoProducto;
						osfp.IdTipoMovimiento = fpIn.IdTipoMovimiento;
						osfp.RecibePagaPortafolio = fpIn.RecibePagaPortafolio;
						osfp.IdFormaDePago = ((FormaDePagoMesa) formaDePagoMesaHash.get(fpIn.IdFormaDePago)).IdFormaDePago;
						osfp.IdModalidadPago = ((ModalidadDePago) modalidadPagoHash.get(((FormaDePagoMesa) formaDePagoMesaHash.get(fpIn.IdFormaDePago)).IdMedioDePago)).IdModalidadDePago;
						osfp.IdMedioDePago = ((FormaDePagoMesa) formaDePagoMesaHash.get(fpIn.IdFormaDePago)).IdMedioDePago;
						osfp.IdMoneda = fpIn.IdMoneda;
						osfp.IdMonedaLiquidacion = fpIn.IdMonedaLiquidacion;
						osfp.Monto = MonedaFormato.formatear(conn, fpIn.MontoFormaDePago, "MONTO", fpIn.IdMoneda).valor;
						osfp.MontoLiquidacion = MonedaFormato.formatear(conn, fpIn.MontoFormaDePago, "MONTO", fpIn.IdMonedaLiquidacion).valor;
						osfp.FechaIngresoComprobantePagoOperacion = Fecha.toStringDate(opSp.FechaOperacion);
						osfp.FechaLiquidacionComprobantePagoOperacion = fechaCondicionLiquidacion;
						osfp.IdEstado = opSp.Estado;
						osfp.AfectarTesoreria = fpIn.AfectarTesoreria;
						osfp.IdCuentaBancaria = fpIn.IdCuentaBancaria;
						osfp.IdTipoOperacion = opSp.IdTipoOperacion;
						operacionesSpotForPag.add(osfp);
					}
				}
			}
			
			//Comprobante Pago Operacion de Mesa
			CrearComprobantePagoOperacion crearComPag = new CrearComprobantePagoOperacion();
			CrearComprobantePagoOperacion_IN crearComPagIn = new CrearComprobantePagoOperacion_IN();
			CrearComprobantePagoOperacion_OUT crearComPagOut = new CrearComprobantePagoOperacion_OUT();
			crearComPagIn.p_IdEmpresa = in.p_IdEmpresa;
			crearComPagIn.p_IdUsuario = in.p_IdUsuario;
			crearComPagIn.p_IdModulo = in.p_IdModulo;
			crearComPagIn.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
			
			for(OperacionSpotFormaDePago osfp: operacionesSpotForPag){
				ComprobantePagoOperacionObj_IN comPagIn = new ComprobantePagoOperacionObj_IN();
				comPagIn.IdInternoPagoOperacion = osfp.IdInternoPagoOperacion;
				comPagIn.IdComprobantePagoOperacion = osfp.IdComprobantePagoOperacion;
				comPagIn.IdEmpresa = in.p_IdEmpresa;
				comPagIn.IdOperacion = osfp.IdOperacion;
				comPagIn.IdIngresoEgreso = osfp.IdIngresoEgreso;
				comPagIn.IdAbono = osfp.IdAbono;
				comPagIn.IdCargo = osfp.IdCargo;
				comPagIn.IdIngresoEgresoAnticipo = osfp.IdIngresoEgresoAnticipo;
				comPagIn.IdAbonoAnticipo = osfp.IdAbonoAnticipo;
				comPagIn.IdCargoAnticipo = osfp.IdCargoAnticipo;
				comPagIn.IdTipoProducto = osfp.IdTipoProducto;
				comPagIn.RecibePagaPortafolio = osfp.RecibePagaPortafolio;
				comPagIn.IdFormaDePago = osfp.IdFormaDePago;
				comPagIn.IdModalidadPago = osfp.IdModalidadPago;
				comPagIn.IdMoneda = osfp.IdMoneda;
				//comPagIn.IdMonedaLiquidacion = osfp.IdMonedaLiquidacion;
				comPagIn.Monto = osfp.Monto;
				//comPagIn.MontoLiquidacion = osfp.Monto;
				comPagIn.FechaIngresoComprobantePagoOperacion = osfp.FechaIngresoComprobantePagoOperacion;
				comPagIn.FechaLiquidacionComprobantePagoOperacion = osfp.FechaLiquidacionComprobantePagoOperacion;
				comPagIn.IdEstado = osfp.IdEstado;
				comPagIn.AfectarTesoreria = osfp.AfectarTesoreria;
				comPagIn.IdCuentaBanco = osfp.IdCuentaBancaria;
				comPagIn.IdTipoOperacion = osfp.IdTipoOperacion;
				crearComPagIn.p_ComprobantePagoOperacion.add(comPagIn);
			}
			
			log.write("MESA SPOT :: Llamada Ingreso Comprobante Pago Operación");
			crearComPagOut = crearComPag.logica(conn, crearComPagIn);
			if (MensajeSalida.getNumeroErrores(crearComPagOut.P_OUT_MENSAJES) > 0){
				out.P_OUT_MENSAJES.addAll(crearComPagOut.P_OUT_MENSAJES);
				return out;
			}
			
			//FFMM
			ModificarMontoNumeroCuotasSolicitud modificarMontoNumeroCuotasSolicitud = new ModificarMontoNumeroCuotasSolicitud();
			
			EntradaModificarMontoNumeroCuotasSolicitud entradaModificarMontoNumeroCuotasSolicitud = new EntradaModificarMontoNumeroCuotasSolicitud();
			
			entradaModificarMontoNumeroCuotasSolicitud.p_IdEmpresa = in.p_IdEmpresa;
			entradaModificarMontoNumeroCuotasSolicitud.p_IdUsuario = in.p_IdUsuario;
			entradaModificarMontoNumeroCuotasSolicitud.p_IdModulo = in.p_IdModulo;
			
			entradaModificarMontoNumeroCuotasSolicitud.p_Solicitudes = operacionModificada.stream()
					.filter(operacionSpot -> operacionSpot.IdSolicitudAporteRescate != null && operacionSpot.IdSolicitudAporteRescate > 0)
					.map(operacionSpot -> {
				ModificarMontoNumeroCuotasSolicitud.InSolicitud solicitud = new ModificarMontoNumeroCuotasSolicitud.InSolicitud();
				solicitud.IdInterno = operacionSpot.IdSolicitudAporteRescate;
				solicitud.IdSolicitudAporteRescate = operacionSpot.IdSolicitudAporteRescate;
				solicitud.CalculoPorMontoCuota = "M";
				solicitud.IdMonedaPrincipal = operacionSpot.IdMonedaPrincipal;
				solicitud.MontoPrincipal = operacionSpot.MontoMonedaPrincipal;
				solicitud.IdMonedaSecundaria = operacionSpot.IdMonedaSecundaria;
				solicitud.MontoSecundario = operacionSpot.MontoMonedaSecundaria;
				return solicitud;
			}).collect(Collectors.toList());
			
			if(entradaModificarMontoNumeroCuotasSolicitud.p_Solicitudes != null && entradaModificarMontoNumeroCuotasSolicitud.p_Solicitudes.size() > 0) {
				SalidaModificarMontoNumeroCuotasSolicitud salidaModificarMontoNumeroCuotasSolicitud = modificarMontoNumeroCuotasSolicitud.logica(conn, entradaModificarMontoNumeroCuotasSolicitud);
				out.P_OUT_MENSAJES.addAll(salidaModificarMontoNumeroCuotasSolicitud.P_OUT_MENSAJES);
			}
			
			/***** SALIDA *****/
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			for(OperacionSpot opSp: operacionModificada){
				OperacionObjOut opOut = new OperacionObjOut();
				opOut.IdInterno = opSp.NumeroOperacion;
				opOut.IdOperacionSpot = opSp.IdOperacionSpot;
				opOut.IdDatatec = opSp.IdDatatec;
				opOut.Estado = opSp.Estado;
				
				out.P_OUT_IDOPERACIONSPOT.add(opOut);
				
				if(opSp.Estado.equals("VIG"))cantOpeVig++;
				else if(opSp.Estado.equals("PEN"))cantOpePen++;
			}
			
			
			/***** WEBSOCKET REFRESCO AUTOMATICO GRILLA MOVIMIENTOS *****/
			AgregarMensajeWebsocket.agregar(conn, in.p_IdEmpresa, "ACTSPOT", "Refresco Movimientos Spot");
			
			
			/***** NOTIFICACIONES *****/
			EntradaNotificarEstadoSpot entradaNotificarEstadoSpot = new EntradaNotificarEstadoSpot();
			for(OperacionSpot opSp : operacionModificada){
				NotificarEstadoSpot.OperacionSpot operacionSpot = new NotificarEstadoSpot.OperacionSpot();
				operacionSpot.IdOperacionSpot = opSp.IdOperacionSpot;
				entradaNotificarEstadoSpot.p_IdOperacionesSpot.add(operacionSpot);
			}
			
			if(entradaNotificarEstadoSpot.p_IdOperacionesSpot.size() > 0){
				entradaNotificarEstadoSpot.p_IdEmpresa = in.p_IdEmpresa;
				entradaNotificarEstadoSpot.p_IdUsuario = in.p_IdUsuario;
				entradaNotificarEstadoSpot.p_IdModulo = in.p_IdModulo;
				NotificarEstadoSpot notificarEstadoSpot = new NotificarEstadoSpot();
				notificarEstadoSpot.logica(conn, entradaNotificarEstadoSpot);
			}
		}
		catch(Exception e){
			out.P_OUT_ERRORESCONTADOR = 1L;
			log.write(e);
			throw e;
		}
		
		return out;
	}

	private ValidarLimitesCuentaPropiaSpot.SalidaValidarLimitesCuentaPropiaSpot validarLimitesCuentaPropia(Connection conn, ModificarOperacionSpotEntrada entrada, List<OperacionSpot> operacionesSpot) throws Exception {
		logger.debug("INICIO::ModificarOperacionSpot.validarLimitesCuentaPropia");
		long inicio = System.currentTimeMillis();
		
		ValidarLimitesCuentaPropiaSpot.EntradaValidarLimitesCuentaPropiaSpot entradaValidarLimitesCuentaPropiaSpot = new ValidarLimitesCuentaPropiaSpot.EntradaValidarLimitesCuentaPropiaSpot();
		entradaValidarLimitesCuentaPropiaSpot.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaValidarLimitesCuentaPropiaSpot.p_IdUsuario = entrada.p_IdUsuario;
		entradaValidarLimitesCuentaPropiaSpot.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaValidarLimitesCuentaPropiaSpot.p_IdTipoProceso = _cl.PROCESO_MODIFICACION;
		entradaValidarLimitesCuentaPropiaSpot.p_OperacionesSpot = operacionesSpot.stream()
				.map(u -> {
					ValidarLimitesCuentaPropiaSpot.OperacionSpot o = new ValidarLimitesCuentaPropiaSpot.OperacionSpot();
					o.IdInterno = u.IdInterno;
					o.IdOperacionSpot = u.IdOperacionSpot;
					o.IdCliente = u.IdCliente;
					o.IdPortafolio = u.IdPortafolio;
					o.IdMesa = u.IdMesa;
					o.IdAgenteOperacion = u.IdAgente;
					o.IdTipoSpot = u.IdTipoSpot;
					o.Estado = u.Estado;
					o.EstadoConfirmacion = u.EstadoConfirmacion;
					o.IdInstrumento = u.IdInstrumento;
					o.IdTipoInstrumento = _cl.TIPO_INSTRUMENTO_SPOT;
					o.NemotecnicoInstrumento = u.Nemotecnico;
					o.CodigoOperador = u.CodigoOperador;
					o.IdTipoCanalIngreso = u.IdTipoCanalIngreso;
					o.IdTipoOperacion = u.IdTipoOperacion;
					o.IdMonedaPrincipal = u.IdMonedaPrincipal;
					o.MontoPrincipal = u.MontoMonedaPrincipal;
					return o;
				})
				.collect(Collectors.toList());
		
		ValidarLimitesCuentaPropiaSpot.SalidaValidarLimitesCuentaPropiaSpot salidaValidarLimitesCuentaPropiaSpot = new ValidarLimitesCuentaPropiaSpot().logica(conn, entradaValidarLimitesCuentaPropiaSpot);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarOperacionSpot.validarLimitesCuentaPropia {}", fin - inicio);
		
		return salidaValidarLimitesCuentaPropiaSpot;
	}
	
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
		//	String jsonDesa = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":15,\"p_IdModulo\":\"SPT\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaOperacion\":\"2019-12-16\",\"p_TipoCambioCierre\":778,\"p_TipoOperacion\":\"VENFWD\"}";
			String jsonDesa= "{'p_IdEmpresa':1,'p_IdUsuario':1006,'p_IdModulo':'SPT','p_UsaReglasNegocio':'S','p_FechaOperacion':'2021-06-28','p_TipoCambioCierre':780,'p_TipoCambioCosto':779,'p_TipoOperacion':'VENFWD'}";
			//String jsonDesaARBDIS = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":33,\"p_IdModulo\":\"MSP\",\"p_CalculaMargen\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_DatosOperacion\":[{\"NumeroOperacion\":1,\"IdInstrumento\":346391,\"IdTipoOperacion\":\"COMSPT\",\"IdTipoProducto\":\"MEME\",\"IdCliente\":123569,\"IdPortafolio\":27499,\"FechaOperacion\":\"2018-06-15\",\"IdMonedaPrincipal\":\"EUR\",\"IdMonedaSecundaria\":\"USD\",\"MontoMonedaPrincipal\":1000,\"MontoMonedaSecundaria\":1500,\"TipoCambioCierre\":0,\"TipoCambioCierreForward\":0,\"ParidadCierre\":1.5,\"ParidadCierreForward\":0,\"TipoCambioCosto\":600,\"ParidadCosto\":1.18110236,\"MontoEquivalente\":1500,\"MontoRentabilidad\":0,\"MontoRentabilidadCanal\":0,\"Margen\":-318.8976400000001,\"Utilidad\":-318.8976400000001,\"PuntaCompra\":600,\"PuntaVenta\":601,\"SolicitudTipoCambio\":\"N\",\"IdMesa\":6,\"IdModulo\":\"SPT\",\"IdTipoDolar\":1,\"TipoComprobanteFacturacion\":\"FAE\",\"IdAgente\":10003,\"BenOur\":\"BEN\",\"EquivalenteConGasto\":0,\"TipoCambioCierreConGasto\":0,\"Observacion\":\"\",\"CodigoComercio\":\"10005\",\"FolioPlanilla\":0,\"FolioInterfaz\":\"\",\"OperacionFicticia\":\"N\",\"IdTipoCanalIngreso\":\"MES\",\"IdDatatec\":0,\"Estado\":\"VIG\"}],\"p_DatosFormaPago\":[{\"IdInternoPagoOperacion\":1,\"IdComprobantePagoOperacion\":0,\"IdTipoMovimiento\":\"MOVING\",\"IdFormaDePago\":\"VENSPT_CIN_USD_0\",\"IdOperacion\":1,\"RecibePagaPortafolio\":\"P\",\"IdMoneda\":\"USD\",\"IdMonedaLiquidacion\":\"USD\",\"MontoFormaDePago\":1500,\"AfectarTesoreria\":\"S\"},{\"IdInternoPagoOperacion\":2,\"IdComprobantePagoOperacion\":0,\"IdTipoMovimiento\":\"MOVEGR\",\"IdFormaDePago\":\"COMSPT_CIN_EUR_0\",\"IdOperacion\":1,\"RecibePagaPortafolio\":\"R\",\"IdMoneda\":\"EUR\",\"IdMonedaLiquidacion\":\"EUR\",\"MontoFormaDePago\":1000,\"AfectarTesoreria\":\"S\"}]}";
			//String jsonMaBa = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":25,\"p_IdModulo\":\"MSP\",\"p_CalculaMargen\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_DatosOperacion\":[{\"NumeroOperacion\":1,\"IdInstrumento\":133258,\"IdTipoOperacion\":\"COMSPT\",\"IdTipoProducto\":\"MEME\",\"IdCliente\":116825,\"IdPortafolio\":122076,\"FechaOperacion\":\"2018-06-12\",\"IdMonedaPrincipal\":\"USD\",\"IdMonedaSecundaria\":\"CLP\",\"MontoMonedaPrincipal\":1000,\"MontoMonedaSecundaria\":639000,\"TipoCambioCierre\":639,\"TipoCambioCierreForward\":0,\"ParidadCierre\":0,\"ParidadCierreForward\":0,\"TipoCambioCosto\":639,\"ParidadCosto\":0,\"MontoEquivalente\":639000,\"MontoRentabilidad\":0,\"MontoRentabilidadCanal\":0,\"Margen\":0,\"Utilidad\":0,\"PuntaCompra\":639,\"PuntaVenta\":642,\"SolicitudTipoCambio\":\"N\",\"IdMesa\":2,\"IdModulo\":\"SPT\",\"IdTipoDolar\":1,\"TipoComprobanteFacturacion\":\"FAE\",\"IdAgente\":20180,\"BenOur\":\"BEN\",\"EquivalenteConGasto\":0,\"TipoCambioCierreConGasto\":0,\"Observacion\":\"\",\"CodigoComercio\":\"10005\",\"FolioPlanilla\":0,\"FolioInterfaz\":\"\",\"OperacionFicticia\":\"N\",\"IdTipoCanalIngreso\":\"MES\",\"IdDatatec\":0,\"Estado\":\"VIG\"}],\"p_DatosFormaPago\":[{\"IdInternoPagoOperacion\":1,\"IdComprobantePagoOperacion\":0,\"IdTipoMovimiento\":\"MOVING\",\"IdFormaDePago\":\"VENSPT_CHE_CLP_0\",\"IdOperacion\":1,\"RecibePagaPortafolio\":\"P\",\"IdMoneda\":\"CLP\",\"IdMonedaLiquidacion\":\"CLP\",\"MontoFormaDePago\":639000,\"AfectarTesoreria\":\"N\"},{\"IdInternoPagoOperacion\":2,\"IdComprobantePagoOperacion\":0,\"IdTipoMovimiento\":\"MOVEGR\",\"IdFormaDePago\":\"COMSPT_CIN_USD_0\",\"IdOperacion\":1,\"RecibePagaPortafolio\":\"R\",\"IdMoneda\":\"USD\",\"IdMonedaLiquidacion\":\"USD\",\"MontoFormaDePago\":1000,\"AfectarTesoreria\":\"N\"}]}";
			ModificarOperacionSpotEntrada Objin = gson.fromJson(jsonDesa, ModificarOperacionSpotEntrada.class);
			
			ModificarOperacionSpot demo = new ModificarOperacionSpot();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				ModificarOperacionSpotSalida ObjOut = demo.logica(con, Objin);
 				//con.commit();
 				con.rollback();
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