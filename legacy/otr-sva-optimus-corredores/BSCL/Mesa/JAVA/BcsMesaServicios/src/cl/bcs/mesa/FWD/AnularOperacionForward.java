package cl.bcs.mesa.FWD;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.mesa.MES.CambiarEstadoOperacion;
import cl.bcs.mesa.MES.CambiarEstadoOperacion.CambiarEstadoOperacionEntrada;
import cl.bcs.mesa.MES.CambiarEstadoOperacion.CambiarEstadoOperacionSalida;
import cl.bcs.mesa.MES.CambiarEstadoOperacion.ObjCambiarEstadoOperacionIN;
import cl.bcs.mesa.FWD.CrearOperacionForward.EventoAutorizacion;
import cl.bcs.mesa.FWD.CrearOperacionForward.OperacionForward;
import cl.bcs.mesa.FWD.IngresarForwardCartera.IngresarForwardCarteraEntrada;
import cl.bcs.mesa.FWD.IngresarForwardCartera.IngresarForwardCarteraSalida;
import cl.bcs.mesa.FWD.IngresarForwardCartera.TablaForwardObjIn;
import cl.bcs.mesa.FWD.ModificarOperacionForward.OperacionForwardObjIn;
import cl.bcs.plataforma.lim.pojo.ConstantesLimites;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarMensajeWebsocket;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AnularOperacionForward implements JavaCall{
	
	private final Log log = new Log("FWD.AnularOperacionForward");
	
	private final ConstantesLimites _cl = new ConstantesLimites();
	
	private final String SQL_SELECT_OPERACIONES_FORWARD = loadResourceToString("cl/bcs/mesa/FWD/sql/AnularOperacionForward_ConsultarOperacionesForward.sql");
	private final String SQL_DELETE_RESERVA_ID_OPERACION = loadResourceToString("cl/bcs/mesa/FWD/sql/AnularOperacionForward_DeleteReservaIdOperacion.sql");
	private final String SQL_DELETE_RESERVA_FOLIO_CARTERA_ORIGEN = loadResourceToString("cl/bcs/mesa/FWD/sql/AnularOperacionForward_DeleteReservaFolioCarteraOrigen.sql");
	private final String SQL_UPDATE_ESTADO_CONFIRMACION_PAGO = "UPDATE \"BSCL.Mesa.BD::MES.Operacion\" SET \"EstadoConfirmacion\" = 'RZD' WHERE	\"IdOperacion\" IN ( :IdOperacion );";
	
	public static class AnularOperacionForwardEntrada {
		public Long p_IdEmpresa;
		public Long p_IdPais;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_IdBolsa;
		public Long p_IdSucursal;
		public String p_UsaReglasNegocio;
		public String p_IdEstadoConfirmacion;
		public List<OperacionForwardObjIn> p_tablaAnulacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Operacion{
		public Long IdOperacion;
	}
	
	public static class AnularOperacionForwardSalida {
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;
		
		public AnularOperacionForwardSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		AnularOperacionForwardEntrada in = gson.fromJson(jin.toString(), AnularOperacionForwardEntrada.class);
		AnularOperacionForwardSalida out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public AnularOperacionForwardSalida logica(Connection conn, AnularOperacionForwardEntrada in) throws Exception {
		SQL _sql = new SQL();
		AnularOperacionForwardSalida out = new AnularOperacionForwardSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		Long Id = 0L;
		HashMap<String,String> mapMsg = new HashMap<>();
		Set<String> codigosRechazoLimiteCtaPropia = new HashSet<>();
		
		log.write("INICIO :: FWD.AnularOperacionForward");
		
		try {
			if (in.p_tablaAnulacion.size() == 0) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_OPERACIONES",mapMsg,conn,in.p_IdEmpresa));
				Id++;
				return out;
			}
			
			SocioNegocioUtils.Empleado empleadoUsuario = SocioNegocioUtils.obtenerEmpleadoUsuario(conn, in.p_IdEmpresa, in.p_IdUsuario).orElse(null);
			if (empleadoUsuario == null) {
				out.P_OUT_ERRORESCONTADOR++;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_USUARIO_NO_ES_EMPLEADO", new HashMap<>(), conn, in.p_IdEmpresa));
				return out;
			}
			
			/***** VARIABLES *****/
			Long cantOpeIn = 0L;
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn,in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");

			HashMap<String,Object> params = new HashMap<>();
			
			List<Long> idOperacion = new ArrayList<Long>();
			List<Long> folioCartera = new ArrayList<Long>();
			HashMap<Long,OperacionForwardObjIn> operacionForwardHash = new HashMap<Long,OperacionForwardObjIn>();
			
			List<EventoAutorizacion> evtList = new ArrayList<CrearOperacionForward.EventoAutorizacion>();
			
			for(OperacionForwardObjIn ope: in.p_tablaAnulacion){
				idOperacion.add(ope.IdOperacion);
				folioCartera.add(ope.FolioCartera);
				cantOpeIn++;
				
				operacionForwardHash.put(ope.IdOperacion, ope);
			}
			
			//Obtiene datos de las Operaciones Forward a anular
			params.put("IdEmpresa", in.p_IdEmpresa);
			params.put("IdOperacion", idOperacion);
			params.put("FolioCartera", folioCartera);
			List<OperacionForward> operacionesVigentes = _sql.query(conn, SQL_SELECT_OPERACIONES_FORWARD, params, OperacionForward.class);
			
			
			/***** ANULACION DE FORWARD EN CARTERA *****/
			List<TablaForwardObjIn> operacionesAnular = new ArrayList<TablaForwardObjIn>();
			for(OperacionForward opeVig: operacionesVigentes){
				TablaForwardObjIn tfi = new TablaForwardObjIn();
				tfi.FolioInterno = opeVig.FolioCartera;
				tfi.IdOperacion = opeVig.IdOperacion;
				tfi.FolioCartera = opeVig.FolioCartera;
				tfi.IdTipoOperacionOriginal = opeVig.IdTipoOperacion;
				tfi.IdTipoOperacionNuevo = "ANUFWD";
				tfi.IdTipoInstrumento = opeVig.IdTipoInstrumento;
				tfi.IdClasificacionTipoInstrumento = opeVig.IdClasificacionTipoInstrumento;
				tfi.IdTipoProducto = opeVig.IdTipoProducto;
				tfi.IdInstrumento = opeVig.IdInstrumento;
				tfi.Nemotecnico = opeVig.Nemotecnico;
				tfi.IdClienteOriginal = opeVig.IdCliente;
				tfi.IdClienteNuevo = opeVig.IdCliente;
				tfi.IdPortafolioOriginal = opeVig.IdPortafolio;
				tfi.IdPortafolioNuevo = opeVig.IdPortafolio;
				tfi.IdPersonaEjecutivoOriginal = opeVig.IdPersonaEjecutivo;
				tfi.IdPersonaEjecutivoNuevo = opeVig.IdPersonaEjecutivo;
				tfi.IdTipoCanalIngreso = opeVig.IdTipoCanalIngreso;
				tfi.IdMonedaPrincipal = opeVig.IdMonedaPrincipal;	
				tfi.IdMonedaSecundaria = opeVig.IdMonedaSecundaria;
				tfi.PrecioVencimiento = BigDecimal.ZERO;
				tfi.MontoOperacionOriginal = opeVig.MontoOperacion;
				tfi.MontoOperacionNuevo = opeVig.MontoOperacion;
				tfi.MontoInicialOriginal = opeVig.MontoInicial;
				tfi.MontoInicialNuevo = opeVig.MontoInicial;
				tfi.MontoFinalOriginal = opeVig.MontoFinal;
				tfi.MontoFinalNuevo = opeVig.MontoFinal;
				tfi.FechaOperacion = opeVig.FechaOperacion;
				tfi.TCCierreOriginal = opeVig.TCCierre;
				tfi.TCCierreNuevo = opeVig.TCCierre;
				tfi.ParidadCierreOriginal = opeVig.ParidadCierre;
				tfi.ParidadCierreNuevo = opeVig.ParidadCierre;
				tfi.PlazoOriginal = opeVig.Plazo;
				tfi.PlazoNuevo = opeVig.Plazo;
				tfi.FechaVencimientoOriginal = opeVig.FechaVencimiento;
				tfi.FechaVencimientoNuevo = opeVig.FechaVencimiento;
				tfi.FechaPagoOriginal = opeVig.FechaPago;
				tfi.FechaPagoNuevo = opeVig.FechaPago;
				tfi.IdModalidadLiquidacionOriginal = opeVig.IdModalidadLiquidacion;
				tfi.IdModalidadLiquidacionNuevo = opeVig.IdModalidadLiquidacion;
				tfi.IdReferenciaMercado = opeVig.IdReferenciaMercado;
				tfi.IdPlaza = opeVig.IdPlaza;
				tfi.TCCostoOriginal = opeVig.TCCosto;
				tfi.TCCostoNuevo = opeVig.TCCosto;
				tfi.PuntosTasasForwardOriginal = opeVig.PuntosTasasForward;
				tfi.PuntosTasasForwardNuevo = opeVig.PuntosTasasForward;
				tfi.PuntosForwardOriginal = opeVig.PuntosForward;
				tfi.PuntosForwardNuevo = opeVig.PuntosForward;
				tfi.TCForwardOriginal = opeVig.TCForward;
				tfi.TCForwardNuevo = opeVig.TCForward;
				tfi.TasasMonedaPrincipalOriginal = opeVig.TasasMonedaPrincipal;
				tfi.TasasMonedaPrincipalNuevo = opeVig.TasasMonedaPrincipal;
				tfi.TasasMonedaSecundariaOriginal = opeVig.TasasMonedaSecundaria;
				tfi.TasasMonedaSecundariaNuevo = opeVig.TasasMonedaSecundaria;
				tfi.MontoDiferidoOriginal = opeVig.MontoDiferido;
				tfi.MontoDiferidoNuevo = opeVig.MontoDiferido;
				tfi.MontoMargenOriginal = opeVig.MontoMargen;
				tfi.MontoMargenNuevo = opeVig.MontoMargen;
				tfi.IdFixingOriginal = opeVig.IdFixing;
				tfi.IdFixingNuevo = opeVig.IdFixing;
				tfi.FechaFixingOriginal = opeVig.FechaFixing;
				tfi.FechaFixingNuevo = opeVig.FechaFixing;
				tfi.HoraFixingOriginal = opeVig.HoraFixing;
				tfi.HoraFixingNuevo = opeVig.HoraFixing;
				tfi.IdMesa = opeVig.IdMesa;
				tfi.IdSpotAsociado = opeVig.IdSpotAsociado;
				tfi.Observaciones = opeVig.Observaciones;
				tfi.IdEstado = "MOD";
				tfi.TasaDescuento = BigDecimal.ZERO;
				tfi.TipoCambioAnticipo = BigDecimal.ZERO;
				tfi.MontoARecibirAnticipo = BigDecimal.ZERO;
				tfi.TipoCambioObservado = BigDecimal.ZERO;
				
				operacionesAnular.add(tfi);
			}
			
			/***** LIMITES *****/
			Long autCont = 0L;
			Long idInternoForward = 1L;
			for(TablaForwardObjIn opeFwd : operacionesAnular) {
				opeFwd.IdInterno = idInternoForward++;
				opeFwd.CodigoOperador = empleadoUsuario.CodigoEmpleado;
			}
			
			List<MensajeSalida> mensajesValidarLimitesCuentaPropiaForward = validarLimitesCuentaPropia(conn, in, operacionesAnular);
			out.P_OUT_MENSAJES.addAll(mensajesValidarLimitesCuentaPropiaForward);						
			
			log.write("MESA FORWARD :: Llamada Ingresar Forward Cartera");
			IngresarForwardCartera ingresarForwardCartera = new IngresarForwardCartera();
			IngresarForwardCarteraEntrada ingresarForwardCarteraIn = new IngresarForwardCarteraEntrada();
			ingresarForwardCarteraIn.p_IdEmpresa = in.p_IdEmpresa;
			ingresarForwardCarteraIn.p_IdPais = in.p_IdPais;
			ingresarForwardCarteraIn.p_IdUsuario = in.p_IdUsuario;
			ingresarForwardCarteraIn.p_IdBolsa = in.p_IdBolsa;
			ingresarForwardCarteraIn.p_IdSucursal = in.p_IdSucursal;
			ingresarForwardCarteraIn.p_IdModulo = in.p_IdModulo;
			ingresarForwardCarteraIn.p_UsaReglasNegocio = "N";//En Anulacion FWD no es necesario validar Reglas
			ingresarForwardCarteraIn.p_tablaForward = operacionesAnular;
			IngresarForwardCarteraSalida ingresarForwardCarteraOut = ingresarForwardCartera.logica(conn, ingresarForwardCarteraIn);
			
			out.P_OUT_ERRORESCONTADOR = out.P_OUT_ERRORESCONTADOR + ingresarForwardCarteraOut.P_OUT_ERRORESCONTADOR;
			out.P_OUT_MENSAJES.addAll(ingresarForwardCarteraOut.P_OUT_MENSAJES);
			if (MensajeSalida.getNumeroErrores(ingresarForwardCarteraOut.P_OUT_MENSAJES) > 0){
				return out;
			}
			
			
			/***** CAMBIAR ESTADO *****/
			CambiarEstadoOperacion cambiarEstadoOperacion = new CambiarEstadoOperacion();
			CambiarEstadoOperacionEntrada cambiarEstadoOperacionIn = new CambiarEstadoOperacionEntrada(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "N", in.p_UsaReglasNegocio, in.p_IdPais);
			CambiarEstadoOperacionSalida cambiarEstadoOperacionOut = new CambiarEstadoOperacionSalida();
			List<Operacion> operaciones = new ArrayList<>(); 
			for(OperacionForward opeFwd: operacionesVigentes){
				ObjCambiarEstadoOperacionIN ceoIn = new ObjCambiarEstadoOperacionIN();
				Operacion op = new Operacion();
				ceoIn.IdOperacion = opeFwd.IdOperacion;
				ceoIn.IdTipoProducto = opeFwd.IdTipoProducto;
				ceoIn.IdNuevoEstado = "ANU";
				ceoIn.IdReserva = 0L;
				ceoIn.FolioCarteraCustodia = 0L;
				ceoIn.MontoOperacionAnticipo = BigDecimal.ZERO;
				ceoIn.MontoOperacionInicialAnticipo = BigDecimal.ZERO;
				ceoIn.MontoOperacionFinalAnticipo = BigDecimal.ZERO;
				ceoIn.TasaAnticipo = BigDecimal.ZERO;
				ceoIn.MontoOperacionMonedaAnticipo = BigDecimal.ZERO;
				ceoIn.MontoOperacionInicialMonedaAnticipo = BigDecimal.ZERO;
				ceoIn.MontoOperacionFinalMonedaAnticipo = BigDecimal.ZERO;
				ceoIn.IdMonedaLiquidacionAnticipo = "";
				ceoIn.IdCondicionLiquidacionAnticipo = "";
				ceoIn.FechaCondicionLiquidacionAnticipo = Fecha.toDate("0001-01-01");
				cambiarEstadoOperacionIn.p_DataCambiarEstado.add(ceoIn);
				op.IdOperacion = ceoIn.IdOperacion;
				operaciones.add(op);
			}
			log.write("MESA FORWARD :: Llamada Cambiar Estado Operacion");
			cambiarEstadoOperacionOut = cambiarEstadoOperacion.logica(conn, cambiarEstadoOperacionIn);
			for(MensajeSalida msje : cambiarEstadoOperacionOut.P_OUT_MENSAJES) {
				if(!"".equalsIgnoreCase(msje.codigo) && !"".equalsIgnoreCase(msje.descripcion)) {
					out.P_OUT_MENSAJES.add(msje);
				}
			}
			if (MensajeSalida.getNumeroErrores(cambiarEstadoOperacionOut.P_OUT_MENSAJES) > 0){
				return out;
			}
			
			if(in.p_IdEstadoConfirmacion != null && !in.p_IdEstadoConfirmacion.equals("") ) {
			_sql.batch(conn, SQL_UPDATE_ESTADO_CONFIRMACION_PAGO, operaciones, Operacion.class);									
			}
			
			/***** DELETE CALCE RESERVA *****/
			_sql.batch(conn, SQL_DELETE_RESERVA_ID_OPERACION, operacionesVigentes, OperacionForward.class);
			_sql.batch(conn, SQL_DELETE_RESERVA_FOLIO_CARTERA_ORIGEN, operacionesVigentes, OperacionForward.class);
			
			
			/***** SALIDA *****/
			out.P_OUT_ERRORESCONTADOR = out.P_OUT_ERRORESCONTADOR + MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			
			/***** AGREGAR LOG *****/
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
			for (OperacionForward opeFwd: operacionesVigentes) {
				AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
				log.IdCabeceraLog = opeFwd.IdOperacion;
				log.IdTipoLog = "ANULACION";
				log.IdModulo = in.p_IdModulo;
				log.IdUsuario = in.p_IdUsuario;
				log.IdEmpresa = in.p_IdEmpresa;
				log.FechaSistemaLog = fechaSistema;
				log.IdUsuarioAtendido = in.p_IdUsuario;
				log.IdCliente = opeFwd.IdCliente;
				log.EstadoCliente = "";
				log.NombreCliente = "";
				log.IdTipoDocumentoIdentidad = "";
				log.IdDocumentoIdentidad = "";
				log.IdPortafolio = opeFwd.IdPortafolio;
				log.EstadoPortafolio = "";
				log.IdTipoRegistro = "FORWARD";
				log.EstadoRegistro = opeFwd.IdEstado;
				log.IdTipoOperacion = opeFwd.IdTipoOperacion;
				log.Folio = opeFwd.IdOperacion;
				log.Cantidad = BigDecimal.ZERO;
				log.PrecioTasa = BigDecimal.ZERO;
				log.Monto = opeFwd.MontoOperacion;
				log.IdMonedaOrigen = opeFwd.IdMonedaPrincipal;
				log.IdMonedaLiquidacion = "";
				log.TipoCambio = opeFwd.TCCierre.toString();
				log.Nemo = "Forward";
				log.IdProceso = 0L;
				log.EstadoProceso = opeFwd.IdEstado;
				log.NombreProceso = "AnularOperacionForward";						
				entradaAgregarLog.p_Log.add(log);
			}
			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(conn, entradaAgregarLog);
			
			
			/***** WEBSOCKET REFRESCO AUTOMATICO GRILLA MOVIMIENTOS *****/
			AgregarMensajeWebsocket.agregar(conn, in.p_IdEmpresa, "ACTFWD", "Actualizacion Grilla FWD");
			
		}catch(Exception e){
			out.P_OUT_ERRORESCONTADOR = 1L;
			log.write(e);
			throw e;
		}
		
		return out;
	}
	
	private List<MensajeSalida> validarLimitesCuentaPropia(Connection conn, AnularOperacionForwardEntrada entrada, List<TablaForwardObjIn> operaciones) throws Exception{
		
		log.write("INICIO::AnularOperacionForward.validarLimitesCuentaPropia");
		long inicio = System.currentTimeMillis();
		
		ValidarLimitesCuentaPropiaForward.EntradaValidarLimitesCuentaPropiaForward entradaValidarLimitescuentaPropiaForward = new ValidarLimitesCuentaPropiaForward.EntradaValidarLimitesCuentaPropiaForward();
		entradaValidarLimitescuentaPropiaForward.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaValidarLimitescuentaPropiaForward.p_IdTipoProceso = _cl.PROCESO_ANULAR;
		entradaValidarLimitescuentaPropiaForward.p_IdUsuario = entrada.p_IdUsuario;
		entradaValidarLimitescuentaPropiaForward.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaValidarLimitescuentaPropiaForward.p_OperacionesFwd = operaciones.stream()
				.map(u -> {
					ValidarLimitesCuentaPropiaForward.OperacionForward o = new ValidarLimitesCuentaPropiaForward.OperacionForward();
					o.IdInterno = u.IdInterno;
					o.IdOperacionFwd = u.IdOperacion;
					o.IdCliente = u.IdClienteNuevo;
					o.IdPortafolio = u.IdPortafolioNuevo;
					o.IdEstado = u.IdEstado;
					o.IdInstrumento = u.IdInstrumento;
					o.IdTipoInstrumento = u.IdTipoInstrumento;
					o.NemotecnicoInstrumento = u.Nemotecnico;
					o.CodigoOperador = u.CodigoOperador;
					o.IdTipoCanalIngreso = u.IdTipoCanalIngreso;
					o.IdTipoOperacion = u.IdTipoOperacionNuevo;
					o.IdMonedaPrincipal = u.IdMonedaPrincipal;
					o.MontoOperacion = u.MontoOperacionNuevo;
					o.FechaVencimiento = new java.sql.Timestamp(u.FechaVencimientoNuevo.getTime());
					o.FechaLiquidacion = new java.sql.Timestamp(u.FechaPagoNuevo.getTime());
					o.PlazoDias = u.PlazoNuevo;
					o.Origen = _cl.ORIGEN_ENTRADA;
					return o;
				})
				.collect(Collectors.toList());
		
		ValidarLimitesCuentaPropiaForward.SalidaValidarLimitesCuentaPropiaForward salidaValidarLimitesCuentaPropiaForward = new ValidarLimitesCuentaPropiaForward().logica(conn, entradaValidarLimitescuentaPropiaForward);
		
		List<MensajeSalida> mensajes = salidaValidarLimitesCuentaPropiaForward.P_OUT_MENSAJES;
		for (MensajeSalida mensaje : mensajes) {
			mensaje.tipo = MensajeSalida.MENSAJE_ADVERTENCIA;
		}
		
		long fin = System.currentTimeMillis();
		log.write("FIN::AnularOperacionForward.validarLimitesCuentaPropia {}", fin-inicio);
		return mensajes;
		
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String jsonDesa = "{\"p_IdEmpresa\":1,\"p_IdPais\":44,\"p_IdUsuario\":33,\"p_IdBolsa\":\"BCS\",\"p_IdSucursal\":0,\"p_IdModulo\":\"MFW\",\"p_UsaReglasNegocio\":\"N\",\"p_tablaAnulacion\":[{\"IdOperacion\":1349,\"FolioCartera\":20684108}]}";
			//String jsonMaBa = "{"p_IdEmpresa":1,"p_IdPais":44,"p_IdUsuario":33,"p_IdBolsa":"BCS","p_IdSucursal":0,"p_IdModulo":"MFW","p_UsaReglasNegocio":"N","p_tablaAnulacion":[{"IdOperacion":1307,"FolioCartera":20684053}]}";
			AnularOperacionForwardEntrada Objin = gson.fromJson(jsonDesa, AnularOperacionForwardEntrada.class);
			
			AnularOperacionForward demo = new AnularOperacionForward();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				AnularOperacionForwardSalida ObjOut = demo.logica(con, Objin);
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