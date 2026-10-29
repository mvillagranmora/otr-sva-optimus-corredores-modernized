package cl.bcs.mesa.SPT;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.facturacion.CrearMovimientoComprobante;
import cl.bcs.facturacion.CrearMovimientoComprobante.CrearMovimientoComprobante_Salida;
import cl.bcs.fondomutuo.CertificarSolicitudAporteRescate.Cobro;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.mesa.SPT.CrearOperacionSpot.OperacionSpot;
import cl.bcs.mesa.SPT.CrearOperacionSpotFacturacion.CrearOperacionSpotFacturacionEntrada;
import cl.bcs.mesa.SPT.CrearOperacionSpotFacturacion.CrearOperacionSpotFacturacionSalida;
import cl.bcs.mesa.SPT.CrearOperacionSpotFacturacion.FormaPagoSpotFacturacionObjIn;
import cl.bcs.mesa.SPT.CrearOperacionSpotFacturacion.OperacionSpotFacturacionObjIn;
import cl.bcs.mesa.SPT.CrearOperacionSpotFacturacion.OperacionSpotFacturacionObjOut;
import cl.bcs.mesa.SPT.CrearOperacionSpotTesoreria.CrearOperacionSpotTesoreriaEntrada;
import cl.bcs.mesa.SPT.CrearOperacionSpotTesoreria.CrearOperacionSpotTesoreriaSalida;
import cl.bcs.mesa.utils.ConsultarConfiguracionMesa;
import cl.bcs.mesa.utils.ConsultarConfiguracionMesa.ConfiguracionMesa;
import cl.bcs.mesa.utils.ConsultarParametroMesa;
import cl.bcs.mesa.utils.ConsultarParametroMesa.ParametroMesa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;


public class ConfirmarLiquidacionSpot implements JavaCall{
	
	private static final Logger logger = LogManager.getLogger(ConfirmarLiquidacionSpot.class);
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	private final String SQL_SELECT_OPERACIONES_SPOT = loadResourceToString("cl/bcs/mesa/SPT/sql/ConfirmarLiquidacionSpot_ConsultarOperacionesSpot.sql");
	private final String SQL_SELECT_FORMAS_DE_PAGO_SPOT = loadResourceToString("cl/bcs/mesa/SPT/sql/ConfirmarLiquidacionSpot_ConsultarFormasDePagoSpot.sql");
	private final String SQL_UPDATE_OPERACIONES_SPOT_ESTADO_CONFIRMACION = loadResourceToString("cl/bcs/mesa/SPT/sql/ConfirmarLiquidacionSpot_UpdateOperacionesSpotEstadoConfirmacion.sql");
	private final String SQL_OPERACIONES_SPOT_ESTADO_BLOQ = loadResourceToString("cl/bcs/mesa/SPT/sql/ConsultarEstadoBloqSpot.sql");
	private final String SQL_SELECT_COBRO_IVA = loadResourceToString("cl/bcs/mesa/SPT/sql/ConfirmarLiquidacionSpot_SelectCobroIva.sql");
	

	private final String SQL_SELECT_CONDICION_DE_LIQUIDACION = loadResourceToString("cl/bcs/mesa/SPT/sql/ConfirmarLiquidacionSpot_SelectCondicionDeLiquidacion.sql");
	
	
	public static class ConfirmarLiquidacionSpotEntrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_ConfirmacionWS = "N";
		public String p_SimularOperacion = "N";
		public List<OperacionSpot> p_DatosOperacion;
		
		public ConfirmarLiquidacionSpotEntrada() {
			this.p_DatosOperacion = new ArrayList<OperacionSpot>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ConfirmarLiquidacionSpotSalida {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public Long					P_OUT_ADVERTENCIASCONTADOR;
		
		public ConfirmarLiquidacionSpotSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class BloqOperacionSpot {
		public String NumeroOperacion;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	
	public static class OperacionFFMM{
		public Long IdSolicitudAporteRescate;
		public String IdTipoMovimientoFondoMutuo;
		public Long   IdOperacionSpot;
		public BigDecimal MontoComision;
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	

	public static class CondicionDeLiquidacion{
		public String 		IdCondicionDeLiquidacion;
		public String 		Codigo;
		public Integer      PlazoEnDias;
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	
	public static class MontoTotalFormaPago{
		public Long 		IdOperacion;
		public String 		IdMoneda;
		public BigDecimal   Monto;
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ConfirmarLiquidacionSpotEntrada in = gson.fromJson(jin.toString(), ConfirmarLiquidacionSpotEntrada.class);
		ConfirmarLiquidacionSpotSalida out = logica(con, in);
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
	
	private void actualizarEstadoConfirmacionOperaciones(
			Connection conn,
			List<OperacionSpot> operacionesSpot,
			List<OperacionSpotFacturacionObjOut> idsFacturados,
			List<MensajeSalida> mensajesSalida,
			Long idEmpresa,
			HashMap<String, String> mapMsg) throws Exception {
		
		if (operacionesSpot == null || operacionesSpot.isEmpty()) {
			return;
		}
		
		SQL _sql = new SQL();
		List<OperacionSpot> opSpUpd = new ArrayList<>();
		List<OperacionSpot> opFMUpd = new ArrayList<>();
		
		for (OperacionSpot opSp : operacionesSpot) {
			if (idsFacturados == null) {
				OperacionSpot os = new OperacionSpot();
				os.IdOperacionSpot = opSp.IdOperacionSpot;
				opSpUpd.add(os);
				if (opSp.IdSolicitudAporteRescate != null && opSp.IdSolicitudAporteRescate > 0) {
					os.IdSolicitudAporteRescate = opSp.IdSolicitudAporteRescate;
					opFMUpd.add(os);
				}
				if (!opSp.IdOperacionSpot.equals(opSp.IdOperacionSpotOrigen)) {
					OperacionSpot osConf = new OperacionSpot();
					osConf.IdOperacionSpot = opSp.IdOperacionSpotOrigen;
					opSpUpd.add(osConf);
				}
			} else {
				for (OperacionSpotFacturacionObjOut osfo : idsFacturados) {
					if (opSp.IdOperacionSpot.longValue() == osfo.IdOperacionSpot.longValue()) {
						OperacionSpot os = new OperacionSpot();
						os.IdOperacionSpot = opSp.IdOperacionSpot;
						if (mensajesSalida.stream().filter(spotFact -> spotFact.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ADVERTENCIA)).count() > 0L) {
							mensajesSalida.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, "OPERACIONES_SPOT_RESTANTES_CERTIFICADAS", mapMsg, conn, idEmpresa));
						}
						opSpUpd.add(os);
						if (opSp.IdSolicitudAporteRescate > 0) {
							os.IdSolicitudAporteRescate = opSp.IdSolicitudAporteRescate;
							opFMUpd.add(os);
						}
						if (!opSp.IdOperacionSpot.equals(opSp.IdOperacionSpotOrigen)) {
							OperacionSpot osConf = new OperacionSpot();
							osConf.IdOperacionSpot = opSp.IdOperacionSpotOrigen;
							opSpUpd.add(osConf);
						}
					}
				}
			}
		}
		
		if (!opSpUpd.isEmpty()) {
			_sql.batch(conn, SQL_UPDATE_OPERACIONES_SPOT_ESTADO_CONFIRMACION, opSpUpd, OperacionSpot.class);
		}
	}
	
//-----------------------------------------------------------------------------
// LOGICA
//-----------------------------------------------------------------------------
	public ConfirmarLiquidacionSpotSalida logica(Connection conn, ConfirmarLiquidacionSpotEntrada in) throws Exception {
		Long init = System.currentTimeMillis();
		logger.info("Inicio servicio ConfirmarLiquidacionSpot");
		
		SQL _sql = new SQL();
		ConfirmarLiquidacionSpotSalida out = new ConfirmarLiquidacionSpotSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		out.P_OUT_ADVERTENCIASCONTADOR = 0L;
		Long Id = 0L;
		Long errCont = 0L;
		HashMap<String,String> mapMsg = new HashMap<>();
		
		HashMap<String,Object> params4 = new HashMap<String,Object>();
		
		for(int i=0;i<in.p_DatosOperacion.size();i++) {
			params4.put("NumeroOperacion", in.p_DatosOperacion.get(i).NumeroOperacion);
		}
		
		try {
			_sql.query(conn, SQL_OPERACIONES_SPOT_ESTADO_BLOQ, params4, BloqOperacionSpot.class);
		}catch(SQLException sqle) {
			if(sqle.getErrorCode()==131) {
				//error especifico porque el movimiento que se esta tratando de obtener ya esta siendo procesado.
				out.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "UI_TRANSACCION_BLOQUEADA_EN_PROCESO", "UI_TRANSACCION_BLOQUEADA_EN_PROCESO"));
				return out;
			}else {
				throw sqle;
			}
		}
		
		try {
			if (in.p_DatosOperacion.size() == 0) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_OPERACIONES",mapMsg,conn,in.p_IdEmpresa));
				Id++;
				return out;
			}
			
			/***** VARIABLES *****/
			ConfiguracionMesa configuracionMesa = new ConsultarConfiguracionMesa().logica(conn, in.p_IdEmpresa, "MEME", "IDMONEDAEQUIVALENTE");
			configuracionMesa = new ConsultarConfiguracionMesa().logica(conn, in.p_IdEmpresa, "MEME", "ENVIA_SPOT_A_CONFIRMAR_LIQUIDACION");
			String liquidacionPorConfirmar = configuracionMesa.Valor;
			
			ParametroMesa parametroMesa = new ConsultarParametroMesa().logica(conn, in.p_IdEmpresa, "FACTURACIONINTERBANCARIOSPOT", "FACTURACIONINTERBANCARIOSPOT", "VIG");
			String parametroFacInterbSpot = parametroMesa.ValorParametro;
			
			// GCMBIDDLDP-5
			String liquidaOpCLI = ConfiguracionSistema.getParametroConfiguracionString(conn,in.p_IdEmpresa, "PLATAFORMA", "PLA.LiquidarOperacionAutomaticoCLI");
			String liquidaOpNEG = ConfiguracionSistema.getParametroConfiguracionString(conn,in.p_IdEmpresa, "PLATAFORMA", "PLA.LiquidarOperacionAutomaticoNEG");
			
			Long cantOpeIn = 0L;
			
			HashMap<String,Object> params = new HashMap<>();
			HashMap<String,Object> params2 = new HashMap<>();
			
			List<String> numeroOperacion = new ArrayList<String>();
			for (OperacionSpot ope: in.p_DatosOperacion){
				numeroOperacion.add(ope.NumeroOperacion);
				cantOpeIn++;
			}
			
			
			/***** VALIDACIONES *****/
			//Retorna si existen errores 
			errCont = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			if (!errCont.equals(0L) ){
				return out;
			}
			
			/***** OBTIENE DATOS DE LAS OPERACIONES SPOT *****/
			params = new HashMap<>();
			params.put("IdEmpresa", in.p_IdEmpresa);
			params.put("NumeroOperacion", numeroOperacion);
			List<OperacionSpot> selectOpeSpot = _sql.query(conn, SQL_SELECT_OPERACIONES_SPOT, params, OperacionSpot.class);
			
			/***VALIDAR QUE EL ESTADO DE LA OPERACION SE ENCUENTRE EN ESTADO PEN*******/
			params2 = new HashMap<>();
			params2.put("IdEmpresa", in.p_IdEmpresa);
			List<Long> numeroOperacion0 = new ArrayList<Long>();
			List<Long> numeroOperacionCNF = new ArrayList<Long>();
			Set<Long> operacionesCajaPEN = new HashSet<>();
			
			HashMap<Long, OperacionSpot> operaciones = new HashMap<>();
			
			for(OperacionSpot opSp0: selectOpeSpot){
				if("PEN".equalsIgnoreCase(opSp0.EstadoCaja)) {
					operacionesCajaPEN.add(opSp0.IdOperacionSpot);
					mapMsg.put("IdOperacion", String.valueOf(opSp0.IdOperacionSpot));
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id++, MensajeSalida.MENSAJE_ADVERTENCIA, "SPOT_ESTADO_CAJA_ERROR",mapMsg,conn,in.p_IdEmpresa));
					mapMsg.clear();
					continue;
				}
				
				if(opSp0.EstadoConfirmacion.equals("PEN")) {
					numeroOperacion0.add(opSp0.IdOperacionSpot);
				}else if (opSp0.EstadoConfirmacion.equals("CNF")) {
					numeroOperacionCNF.add(opSp0.IdOperacionSpot);
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ADVERTENCIA,"La Operacion "+ opSp0.IdOperacionSpotOrigen+" del cliente rut "+ opSp0.DocumentoIdentidad+" "+ opSp0.NombreCompleto + " fue confirmada con anterioridad",mapMsg,conn,in.p_IdEmpresa));
					Id++;
				}
				operaciones.put(opSp0.IdOperacionSpot, opSp0);
			}
			
			selectOpeSpot.removeIf(spot -> operacionesCajaPEN.contains(spot.IdOperacionSpot));
			
			for(int i=0;i<numeroOperacionCNF.size() ; i++) {	
				//if(selectOpeSpot.size()>0) {
					for(int j=0;j<selectOpeSpot.size() ; j++) {	
						if(selectOpeSpot.get(j).IdOperacionSpot.equals(numeroOperacionCNF.get(i))){
							selectOpeSpot.remove(j);	
						}
					}
			//	}
			}
			
			if(numeroOperacion0.size() > 0) {
				params2.put("NumeroOperacion", numeroOperacion0);
			
				/***** OBTIENE DATOS DE LAS FORMAS DE PAGO DE LAS OPERACIONES SPOT *****/
				List<FormaPagoSpotFacturacionObjIn> selectForPagSpotFact = _sql.query(conn, SQL_SELECT_FORMAS_DE_PAGO_SPOT, params2, FormaPagoSpotFacturacionObjIn.class);
				
				if(selectForPagSpotFact.size() == 0) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"OPERACION_NO_TIENE_FORMA_PAGO",mapMsg,conn,in.p_IdEmpresa));
					return out;
				}
				
				//Se calcula el maximo de fecha de las Formas de Pago
				HashMap<Long, Date> maxFechaLiquidacion = new HashMap<Long, Date>();

				HashMap<Long, String> maxIdCondicionLiquidacion = new HashMap<Long, String>();
				HashMap<String, MontoTotalFormaPago> formasDePagoPorMoneda = new HashMap<String, MontoTotalFormaPago>();
				
				for(FormaPagoSpotFacturacionObjIn fpsf: selectForPagSpotFact)
				{
					if (!maxFechaLiquidacion.containsKey(fpsf.IdOperacion)) 
					{
						maxFechaLiquidacion.put(fpsf.IdOperacion, Fecha.toDate(fpsf.FechaLiquidacionComprobantePagoOperacion));
						maxIdCondicionLiquidacion.put(fpsf.IdOperacion, fpsf.IdCondicionDeLiquidacion);
					}
					else 
					{
						Date iFecCondLiq = Fecha.toDate(fpsf.FechaLiquidacionComprobantePagoOperacion);
						if(iFecCondLiq.compareTo(maxFechaLiquidacion.get(fpsf.IdOperacion)) > 0)
						{
							maxFechaLiquidacion.put(fpsf.IdOperacion, iFecCondLiq);
							maxIdCondicionLiquidacion.put(fpsf.IdOperacion, fpsf.IdCondicionDeLiquidacion);
						}
					}
					
					String keys = fpsf.IdOperacion + "_" + fpsf.IdMoneda;

					MontoTotalFormaPago MonTotForPag = new MontoTotalFormaPago();
					
					MonTotForPag.IdOperacion = fpsf.IdOperacion;
					MonTotForPag.IdMoneda = fpsf.IdMoneda;
									
					if(!formasDePagoPorMoneda.containsKey(keys)) {
						MonTotForPag.Monto = fpsf.Monto;
					}else{
						MonTotForPag.Monto = formasDePagoPorMoneda.get(keys).Monto.add(fpsf.Monto);
					}
					
					formasDePagoPorMoneda.put(keys, MonTotForPag);
				}				
				
				for (HashMap.Entry<Long, OperacionSpot> entry : operaciones.entrySet()) {
				    System.out.println(entry.getKey());
				    System.out.println(entry.getValue());
				    OperacionSpot oper = entry.getValue();
				    if(oper.IdTipoOperacion.equals("VENSPT") && oper.IdTipoCanalIngreso.equals("FMM")) {
				    	
				    	String keyMonPrincipal = oper.IdOperacionSpot + "_" + oper.IdMonedaPrincipal;
				    	String keyMonSecundaria = oper.IdOperacionSpot + "_" + oper.IdMonedaSecundaria;
				    	
				    	if(formasDePagoPorMoneda.get(keyMonPrincipal).Monto.compareTo(oper.MontoMonedaPrincipal) > 0) {
				    		out.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "MONTO_FORMAS_PAGO_NO_COINCIDEN_CON_OPERACION", "MONTO_FORMAS_PAGO_NO_COINCIDEN_CON_OPERACION"));
							return out;
				    	}
				    	
				    	if(formasDePagoPorMoneda.get(keyMonSecundaria).Monto.compareTo(oper.MontoMonedaSecundaria) > 0) {
				    		out.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "MONTO_FORMAS_PAGO_NO_COINCIDEN_CON_OPERACION", "MONTO_FORMAS_PAGO_NO_COINCIDEN_CON_OPERACION"));
							return out;
				    	}
				    }
				}
				
				// Obtiene las posibles Condiciones de Liquidacion a partir de la maxFechaLiquidacion calculada
				ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
				InformacionEmpresa empresa = consultarEmpresaService.logica(conn, in.p_IdEmpresa);					
				Long idPaisEmpresa = empresa.IdPais;
				
				List<CondicionDeLiquidacion> lstCondicionDeLiquidacion = _sql.query(conn, SQL_SELECT_CONDICION_DE_LIQUIDACION, new HashMap<>(), CondicionDeLiquidacion.class);
				HashMap<Integer, Object> hashCondicionDeLiquidacion = Utils.List2Hash(lstCondicionDeLiquidacion, "PlazoEnDias");
				
				ObtenerDiaSiguiente servicioObtenerDiaSiguiente = new ObtenerDiaSiguiente();
				InObtenerDiaSiguiente inObtenerDiaSiguiente = new InObtenerDiaSiguiente();
			
				List<FechasIn> lstInFechas = new ArrayList<FechasIn>();
				String fechaInicial = "";
			
				for(OperacionSpot opSp: selectOpeSpot){
					fechaInicial = Fecha.toStringDate(opSp.FechaOperacion);
											
					FechasIn inFechas0 = new FechasIn();
					inFechas0.fechaInicial = fechaInicial;
					inFechas0.numeroDias = 0L; // PH
					inFechas0.pais = idPaisEmpresa;	
					if (!lstInFechas.contains(inFechas0))						
						lstInFechas.add(inFechas0);				
											
					FechasIn inFechas1 = new FechasIn();
					inFechas1.fechaInicial = fechaInicial;
					inFechas1.numeroDias = 1L; // PM
					inFechas1.pais = idPaisEmpresa;	
					if (!lstInFechas.contains(inFechas1))						
						lstInFechas.add(inFechas1);			
					
					FechasIn inFechas2 = new FechasIn();
					inFechas2.fechaInicial = fechaInicial;
					inFechas2.numeroDias = 2L; // CN
					inFechas2.pais = idPaisEmpresa;
					if (!lstInFechas.contains(inFechas2))					
						lstInFechas.add(inFechas2);
					
					
				}
				
				inObtenerDiaSiguiente.p_idPais = idPaisEmpresa;
				inObtenerDiaSiguiente.p_diaHabil = "S";
				inObtenerDiaSiguiente.p_soloCaeDiaHabil = "S";
				inObtenerDiaSiguiente.p_fechas = lstInFechas;

				OutObtenerDiaSiguiente outObtenerDiaSiguiente = servicioObtenerDiaSiguiente.logica(conn, inObtenerDiaSiguiente);
				
				if (outObtenerDiaSiguiente.P_OUT_ERRORESCONTADOR.compareTo(0L) > 0)
				{						
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ERROR_AL_CALCULAR_CONDICION_DE_LIQUIDACION",mapMsg,conn,in.p_IdEmpresa));						
				}
				
				HashMap<Long, Object> operacionesMap = Utils.List2Hash(selectOpeSpot, "IdOperacionSpot");
				
				/***** PROCESO OPERACIONES FICTICIAS *****/
				List<OperacionSpot> operacionesFicticias = selectOpeSpot
						.stream()
						.filter(op -> "S".equalsIgnoreCase(op.OperacionFicticia))
						.collect(Collectors.toList());
				
				if (!operacionesFicticias.isEmpty()) {
					CrearOperacionSpotTesoreria ingresoEgresoServiceFict = new CrearOperacionSpotTesoreria();
					CrearOperacionSpotTesoreriaEntrada entradaCrearIngresoEgresoFict = new CrearOperacionSpotTesoreriaEntrada();

					entradaCrearIngresoEgresoFict.p_IdEmpresa = in.p_IdEmpresa;
					entradaCrearIngresoEgresoFict.p_IdModulo = in.p_IdModulo;
					entradaCrearIngresoEgresoFict.p_IdUsuario = in.p_IdUsuario;
					entradaCrearIngresoEgresoFict.p_SimularOperacion = in.p_SimularOperacion;
					entradaCrearIngresoEgresoFict.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
					entradaCrearIngresoEgresoFict.p_DatosOperacion = operacionesFicticias;
					entradaCrearIngresoEgresoFict.p_MediosDePago = selectForPagSpotFact
							.stream()
							.filter(fpag -> {
								OperacionSpot op = (OperacionSpot) operacionesMap.get(fpag.IdOperacion);
								return op != null && "S".equalsIgnoreCase(op.OperacionFicticia);
							})
							.collect(Collectors.toList());
					
					CrearOperacionSpotTesoreriaSalida salidaIngresoEgresoFict = ingresoEgresoServiceFict.logica(conn, entradaCrearIngresoEgresoFict);
					
					if (salidaIngresoEgresoFict.P_OUT_ERRORESCONTADOR > 0L) {
						out.P_OUT_ERRORESCONTADOR = salidaIngresoEgresoFict.P_OUT_ERRORESCONTADOR;
						out.P_OUT_MENSAJES = salidaIngresoEgresoFict.P_OUT_MENSAJES.stream().filter(m -> m.tipo == MensajeSalida.MENSAJE_ERROR).collect(Collectors.toList());
						return out;
					}
					
					actualizarEstadoConfirmacionOperaciones(conn, operacionesFicticias, null, out.P_OUT_MENSAJES, in.p_IdEmpresa, mapMsg);
				}
				
				/***** FACTURACION *****/
				CrearOperacionSpotFacturacion opSpFact = new CrearOperacionSpotFacturacion();
				CrearOperacionSpotFacturacionEntrada opSpFactIn = new CrearOperacionSpotFacturacionEntrada();
				opSpFactIn.p_IdEmpresa = in.p_IdEmpresa;
				opSpFactIn.p_IdUsuario = in.p_IdUsuario;
				opSpFactIn.p_IdModulo = in.p_IdModulo;
				opSpFactIn.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
				
				for(OperacionSpot opSp: selectOpeSpot) {
					Boolean afectaTesoreria = selectForPagSpotFact.stream().allMatch(forma -> {
						return forma.AfectarTesoreria.equalsIgnoreCase("N");
					});
					
					if ("S".equalsIgnoreCase(opSp.OperacionFicticia)) {
						continue;
					}
					OperacionSpotFacturacionObjIn operacionSpotFactIn = new OperacionSpotFacturacionObjIn();
					operacionSpotFactIn.IdOperacionSpot = opSp.IdOperacionSpot;
					operacionSpotFactIn.IdOperacionSpotOrigen = opSp.IdOperacionSpotOrigen;
					operacionSpotFactIn.IdTipoOperacion = opSp.IdTipoOperacion;
					operacionSpotFactIn.FechaOperacion = opSp.FechaOperacion;
					operacionSpotFactIn.IdCliente = opSp.IdCliente;
					operacionSpotFactIn.IdPortafolio = opSp.IdPortafolio;
					operacionSpotFactIn.IdInstrumento = opSp.IdInstrumento;
					operacionSpotFactIn.Nemotecnico = opSp.Nemotecnico;
					operacionSpotFactIn.IdTipoProducto = "MEME";
					operacionSpotFactIn.IdMonedaPrincipal= opSp.IdMonedaPrincipal;
					operacionSpotFactIn.IdMonedaSecundaria= opSp.IdMonedaSecundaria;
					operacionSpotFactIn.MontoMonedaPrincipal= opSp.MontoMonedaPrincipal;
					operacionSpotFactIn.MontoMonedaSecundaria= opSp.MontoMonedaSecundaria;								
					operacionSpotFactIn.TipoCambioCierre = opSp.TipoCambioCierre;
					operacionSpotFactIn.ParidadCierre = opSp.ParidadCierre;
					operacionSpotFactIn.TipoComprobanteFacturacion = opSp.TipoComprobanteFacturacion;
					operacionSpotFactIn.IdBoveda = "COR";
					
					operacionSpotFactIn.FechaLiquidacion       = Fecha.toStringDate(maxFechaLiquidacion.get(opSp.IdOperacionSpot));
					operacionSpotFactIn.IdTipoMovimientoFondoMutuo = opSp.IdTipoMovimientoFondoMutuo;
					operacionSpotFactIn.IdTipoConvenio = "FAC";
					operacionSpotFactIn.MultiplicaODivide = opSp.MultiplicaODivide;
					operacionSpotFactIn.TipoCambioUSD = opSp.TipoCambioUSD;
					operacionSpotFactIn.TipoCambioMonedaSecundaria = opSp.TipoCambioMonedaSecundaria;
					operacionSpotFactIn.Glosa = opSp.Observacion;
					operacionSpotFactIn.IdTipoCanalIngreso = opSp.IdTipoCanalIngreso;
					operacionSpotFactIn.IdMesa = opSp.IdMesa;
					// GCMBIDDLDP-5
					operacionSpotFactIn.CrearComprobanteAutomatico = (((liquidaOpCLI.equalsIgnoreCase("S") && opSp.IdTipoCanalIngreso.equalsIgnoreCase("CLI")) || (liquidaOpNEG.equalsIgnoreCase("S") && opSp.IdTipoCanalIngreso.equalsIgnoreCase("AGE"))) && afectaTesoreria) ? "S" : opSp.FacturacionAutomatica;
					operacionSpotFactIn.Estado = opSp.Estado;
					
					// Obtiene la Condiciones de Liquidacion a partir de la maxFechaLiquidacion calculada
					String idCondicionLiquidacion = "";
					for (FechasOut fecha: outObtenerDiaSiguiente.p_out_fechas)
					{							
						if (fecha.fechaInicial.equalsIgnoreCase(Fecha.toStringDate(opSp.FechaOperacion)) && fecha.fechaCalculada.equalsIgnoreCase(Fecha.toStringDate(maxFechaLiquidacion.get(opSp.IdOperacionSpot))))
						{									
							idCondicionLiquidacion = ((CondicionDeLiquidacion) hashCondicionDeLiquidacion.get(Math.toIntExact(fecha.numeroDiasInicial))).IdCondicionDeLiquidacion;
							break;
						}										
					}
					operacionSpotFactIn.IdCondicionLiquidacion = (idCondicionLiquidacion != null && !idCondicionLiquidacion.equals("")) ? idCondicionLiquidacion : maxIdCondicionLiquidacion.get(opSp.IdOperacionSpot);
					
					
					opSpFactIn.p_DatosOperacion.add(operacionSpotFactIn);
				}
					
				for(FormaPagoSpotFacturacionObjIn fpsf: selectForPagSpotFact) {
					OperacionSpot op = (OperacionSpot) operacionesMap.get(fpsf.IdOperacion);
					if ("S".equalsIgnoreCase(op.OperacionFicticia)) {
						continue;
					}
					FormaPagoSpotFacturacionObjIn formaPagoSpotFactIn = new FormaPagoSpotFacturacionObjIn();
					formaPagoSpotFactIn.IdOperacion = fpsf.IdOperacion;
					formaPagoSpotFactIn.IdTipoMovimiento = fpsf.IdTipoMovimiento;
					formaPagoSpotFactIn.IdMedioDePago = fpsf.IdMedioDePago;
					formaPagoSpotFactIn.IdModalidadPago = fpsf.IdModalidadPago;
					formaPagoSpotFactIn.IdMoneda = fpsf.IdMoneda;
					formaPagoSpotFactIn.Monto = fpsf.Monto;
					formaPagoSpotFactIn.FechaLiquidacionComprobantePagoOperacion = fpsf.FechaLiquidacionComprobantePagoOperacion;
					formaPagoSpotFactIn.AfectarTesoreria = fpsf.AfectarTesoreria;
					formaPagoSpotFactIn.IdCuentaBancaria = fpsf.IdCuentaBancaria;
					formaPagoSpotFactIn.IdTipoCuentaBancaria = fpsf.IdTipoCuentaBancaria;
					formaPagoSpotFactIn.NumeroCuentaBancaria = fpsf.NumeroCuentaBancaria;
					formaPagoSpotFactIn.IdBanco = fpsf.IdBanco;
					
					opSpFactIn.p_DatosFormaPago.add(formaPagoSpotFactIn);
				}
				
				CrearOperacionSpotFacturacionSalida opSpFactOut = new CrearOperacionSpotFacturacionSalida();
				if (opSpFactIn.p_DatosOperacion.size() > 0) {
					opSpFactOut = opSpFact.logica(conn, opSpFactIn);
					
					//Si hay mensajes de error se agregan a la salida
					if(opSpFactOut.P_OUT_MENSAJES.stream().filter(spotFact -> spotFact.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ADVERTENCIA)).count() > 0L) {
						out.P_OUT_MENSAJES.addAll(opSpFactOut.P_OUT_MENSAJES.stream().filter(spotFact -> spotFact.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ADVERTENCIA)).collect(Collectors.toList()));
					}
							
					if(opSpFactOut.P_OUT_IDOPERACIONSPOT.size() == 0) {
						out.P_OUT_MENSAJES.addAll(opSpFactOut.P_OUT_MENSAJES);
						out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
						return out;
					}
				}
					

				List <OperacionSpot> OperacionSpotSinErrores = new ArrayList<>();
				
				//Se filtran las operaciones spot sin errores
				for(OperacionSpotFacturacionObjOut spot : opSpFactOut.P_OUT_IDOPERACIONSPOT) {
					OperacionSpotSinErrores.addAll(
							selectOpeSpot.stream()
							.filter( selectSpot -> selectSpot.IdOperacionSpot.compareTo(spot.IdOperacionSpot) == 0)
							.collect(Collectors.toList()));
				}
				
				selectOpeSpot = OperacionSpotSinErrores;
				
				if(selectOpeSpot.size() == 0) {
					return out;
				}
				
				//MOVIMIENTO FACTURACION RESCATE POR COMISION						
				CrearMovimientoComprobante.CrearMovimientoComprobante_Entrada entradaCrearMovimientoComprobante = new CrearMovimientoComprobante.CrearMovimientoComprobante_Entrada();
				CrearMovimientoComprobante_Salida salidaCrearMovimientoComprobante = new CrearMovimientoComprobante_Salida();
				
				//Obtiene valor Iva
				List<Cobro> cobros = _sql.query(conn, SQL_SELECT_COBRO_IVA, new HashMap<>(), Cobro.class);
				BigDecimal IVA = new BigDecimal(cobros.get(0).ValorCobro.toString()).divide(BigDecimal.valueOf(100));
				BigDecimal PorcIva = new BigDecimal(cobros.get(0).ValorCobro.toString()); 
				BigDecimal CalculaImporteIva = IVA.add(new BigDecimal(1));						
				
				for(OperacionSpot opComSalFFMM: selectOpeSpot){
					if ("S".equalsIgnoreCase(opComSalFFMM.OperacionFicticia)) {
						continue;
					}
					
					if (opComSalFFMM.MontoComision.compareTo(BigDecimal.ZERO) == 1 && !_c.RescateComisionNN.equals(opComSalFFMM.IdTipoMovimientoFondoMutuo)) {														
						CrearMovimientoComprobante.MovimientoComprobante_Entrada operacionSpotConComSalFFMM = new CrearMovimientoComprobante.MovimientoComprobante_Entrada();
						operacionSpotConComSalFFMM.NumeroMvtoCmbte = opComSalFFMM.IdSolicitudAporteRescate;
						operacionSpotConComSalFFMM.IdEmpresa = in.p_IdEmpresa;
						operacionSpotConComSalFFMM.IdCliente = opComSalFFMM.IdCliente;
						operacionSpotConComSalFFMM.IdPortafolio = opComSalFFMM.IdPortafolio;
						operacionSpotConComSalFFMM.IdInstrumento = opComSalFFMM.IdInstrumentoFFMM;
						operacionSpotConComSalFFMM.IdInstrumentoRelacionado = 0L;
						operacionSpotConComSalFFMM.NemotecnicoInstrumento = opComSalFFMM.NemotecnicoFFMM;
						operacionSpotConComSalFFMM.IdMoneda = opComSalFFMM.IdMonedaSecundaria;
						operacionSpotConComSalFFMM.IdMonedaLiquidacion = opComSalFFMM.IdMonedaSecundaria;
						operacionSpotConComSalFFMM.IdBoveda = "COR";
						operacionSpotConComSalFFMM.IdBolsa = "BCS";
						operacionSpotConComSalFFMM.IdEmisor = opComSalFFMM.IdEmisorFFMM;
						operacionSpotConComSalFFMM.Cantidad = new BigDecimal(1);
						operacionSpotConComSalFFMM.Precio = new BigDecimal(0).equals(opComSalFFMM.MontoComision)? opComSalFFMM.MontoComision: (opComSalFFMM.MontoComision.divide(CalculaImporteIva, MathContext.DECIMAL128).multiply(opComSalFFMM.TipoCambioCierre));							
						operacionSpotConComSalFFMM.FechaLiquidacion = Fecha.stringToSqlDate(opComSalFFMM.FechaLiquidacion);
						operacionSpotConComSalFFMM.FechaOrden = opComSalFFMM.FechaCertificacion;
						operacionSpotConComSalFFMM.FechaTransaccion = opComSalFFMM.FechaCertificacion;
						operacionSpotConComSalFFMM.FechaMovimientoComprobante = opComSalFFMM.FechaCertificacion;
						operacionSpotConComSalFFMM.FechaEmision = opComSalFFMM.FechaCertificacion;
						operacionSpotConComSalFFMM.IdUsuario = in.p_IdUsuario;
						operacionSpotConComSalFFMM.IdTipoProducto = "INFM"; 							
						operacionSpotConComSalFFMM.IdTipoComprobante = "FOE";
						operacionSpotConComSalFFMM.IdConcepto = "COSAFM"; 
						operacionSpotConComSalFFMM.ActualizarCuentaInversion = "S";				
						operacionSpotConComSalFFMM.Monto = new BigDecimal(0).equals(opComSalFFMM.MontoComision)? opComSalFFMM.MontoComision: (opComSalFFMM.MontoComision.divide(CalculaImporteIva, MathContext.DECIMAL128).multiply(opComSalFFMM.TipoCambioCierre)).setScale(0, RoundingMode.FLOOR);
						operacionSpotConComSalFFMM.MontoMovimientoComprobante = new BigDecimal(0).equals(opComSalFFMM.MontoComision)? opComSalFFMM.MontoComision: (opComSalFFMM.MontoComision.divide(CalculaImporteIva, MathContext.DECIMAL128).multiply(opComSalFFMM.TipoCambioCierre)).setScale(0, RoundingMode.FLOOR);
						operacionSpotConComSalFFMM.MontoLiquidacion = new BigDecimal(0).equals(opComSalFFMM.MontoComision)? opComSalFFMM.MontoComision: (opComSalFFMM.MontoComision.divide(CalculaImporteIva, MathContext.DECIMAL128).multiply(opComSalFFMM.TipoCambioCierre)).setScale(0, RoundingMode.FLOOR);
						operacionSpotConComSalFFMM.IdTipoOperacion = "RESCFM"; 
						operacionSpotConComSalFFMM.IdCondicionLiquidacion = "T0";
						operacionSpotConComSalFFMM.IdTipoReajuste = "";
						operacionSpotConComSalFFMM.IdTipoConvenio = "SCV"; 
						operacionSpotConComSalFFMM.IdFormaCustodia = "CUS";
						operacionSpotConComSalFFMM.IdTipoCanalIngreso = "AGE";
						operacionSpotConComSalFFMM.EsComprobIndividual= "N";
						operacionSpotConComSalFFMM.EsCuentaPropia= "N";
						operacionSpotConComSalFFMM.EsVentaCorta= "N";
						operacionSpotConComSalFFMM.EsPrecioPrima= "N";
						operacionSpotConComSalFFMM.EsEnRueda= "N";
						operacionSpotConComSalFFMM.EsRuteo= "N";
						operacionSpotConComSalFFMM.EsAPlazo= "N";
						operacionSpotConComSalFFMM.EsFungible= "N";
						operacionSpotConComSalFFMM.ActualizarCustodia= "N";
						operacionSpotConComSalFFMM.ActualizarTesoreria= "N";
						operacionSpotConComSalFFMM.ActualizarCustodiaRelacionado = "N";
						operacionSpotConComSalFFMM.CrearComprobanteAutomatico = "N";
						operacionSpotConComSalFFMM.SecuenciaAgrupacion = "";
						operacionSpotConComSalFFMM.ModPrefFact = "MODPREFNORM";
						operacionSpotConComSalFFMM.IdEmpleadoDigitador = 0L;
						operacionSpotConComSalFFMM.IdInstrumentoSubyacente = 0L;
						operacionSpotConComSalFFMM.IdMonedaSubyacente = "";
						operacionSpotConComSalFFMM.IdCorredoraContraparte = 0L;
						operacionSpotConComSalFFMM.IdOrden = 0L;
						operacionSpotConComSalFFMM.IdAsignacion = 0L;
						operacionSpotConComSalFFMM.IdAsignacionRef = 0L;
						operacionSpotConComSalFFMM.IdTransaccion = opComSalFFMM.IdOperacionSpot;
						operacionSpotConComSalFFMM.FolioTransaccion = opComSalFFMM.IdOperacionSpotOrigen.toString(); 
						operacionSpotConComSalFFMM.FechaVencimiento = "0001-01-01";
						operacionSpotConComSalFFMM.PrecioAPlazo = BigDecimal.ZERO;
						operacionSpotConComSalFFMM.MontoAPlazo = BigDecimal.ZERO;
						operacionSpotConComSalFFMM.FechaParidad = "0001-01-01";
						operacionSpotConComSalFFMM.ValorParidad = BigDecimal.ZERO;
						operacionSpotConComSalFFMM.FechaTipoCambio = "0001-01-01";
						operacionSpotConComSalFFMM.ValorTipoCambio = BigDecimal.ZERO;
						operacionSpotConComSalFFMM.ValorCaptacionMonedaOrigen = BigDecimal.ZERO;
						operacionSpotConComSalFFMM.Tasa = BigDecimal.ZERO;
						operacionSpotConComSalFFMM.MontoDescuento = BigDecimal.ZERO;
						operacionSpotConComSalFFMM.MontoInteres = BigDecimal.ZERO;
						operacionSpotConComSalFFMM.Glosa = "";
						operacionSpotConComSalFFMM.ValorRescate = BigDecimal.ZERO;
						operacionSpotConComSalFFMM.TasaAgente = BigDecimal.ZERO;
						operacionSpotConComSalFFMM.Spread = BigDecimal.ZERO;
						operacionSpotConComSalFFMM.DiasPlazo = BigDecimal.ZERO;
						operacionSpotConComSalFFMM.FolioCartera = 0L;
						operacionSpotConComSalFFMM.TasaEmision = BigDecimal.ZERO;
						
						entradaCrearMovimientoComprobante.p_Movimientos.add(operacionSpotConComSalFFMM);
						
						//Cobros
						if (opComSalFFMM.MontoComision.compareTo(BigDecimal.ZERO) == 1) {
							CrearMovimientoComprobante.MovimientoComprobanteCobro_Entrada operacionSpotConComSalFFMMCob = new CrearMovimientoComprobante.MovimientoComprobanteCobro_Entrada();
							operacionSpotConComSalFFMMCob.NumeroMvtoCmbte = opComSalFFMM.IdSolicitudAporteRescate;
							operacionSpotConComSalFFMMCob.NumeroMvtoCmbteCobro = opComSalFFMM.IdSolicitudAporteRescate;
							operacionSpotConComSalFFMMCob.IdCobro = "IVA";
							operacionSpotConComSalFFMMCob.IdMonedaCobro = opComSalFFMM.IdMonedaSecundaria;
							operacionSpotConComSalFFMMCob.IdMonedaLiquidacionCobro = opComSalFFMM.IdMonedaSecundaria;
							operacionSpotConComSalFFMMCob.MontoCobro = new BigDecimal(0).equals(opComSalFFMM.MontoComision)? opComSalFFMM.MontoComision: ((opComSalFFMM.MontoComision).subtract(opComSalFFMM.MontoComision.divide(CalculaImporteIva, MathContext.DECIMAL128))).multiply(opComSalFFMM.TipoCambioCierre).setScale(0, RoundingMode.FLOOR);
							operacionSpotConComSalFFMMCob.MontoMovimientoComprobanteCobro = new BigDecimal(0).equals(opComSalFFMM.MontoComision)? opComSalFFMM.MontoComision: ((opComSalFFMM.MontoComision).subtract(opComSalFFMM.MontoComision.divide(CalculaImporteIva, MathContext.DECIMAL128))).multiply(opComSalFFMM.TipoCambioCierre).setScale(0, RoundingMode.FLOOR);
							operacionSpotConComSalFFMMCob.MontoLiquidacionCobro = new BigDecimal(0).equals(opComSalFFMM.MontoComision)? opComSalFFMM.MontoComision: ((opComSalFFMM.MontoComision).subtract(opComSalFFMM.MontoComision.divide(CalculaImporteIva, MathContext.DECIMAL128))).multiply(opComSalFFMM.TipoCambioCierre).setScale(0, RoundingMode.FLOOR);
							operacionSpotConComSalFFMMCob.Porcentaje = PorcIva;
							
							entradaCrearMovimientoComprobante.p_MovimientosCobros.add(operacionSpotConComSalFFMMCob);
						}
						
						//Forma de Pago
						if ("RESNORM".equalsIgnoreCase(opComSalFFMM.IdTipoMovimientoFondoMutuo)) {
							CrearMovimientoComprobante.MovimientoComprobanteFormaDePago_Entrada operacionSpotConComSalFFMMFPago = new CrearMovimientoComprobante.MovimientoComprobanteFormaDePago_Entrada();
							operacionSpotConComSalFFMMFPago.AfectarCuentaInversion = "S";
							operacionSpotConComSalFFMMFPago.AfectarTesoreria = "N";
							operacionSpotConComSalFFMMFPago.IdMedioDePago = "CIN";
							operacionSpotConComSalFFMMFPago.IdMoneda = opComSalFFMM.IdMonedaSecundaria; 
							operacionSpotConComSalFFMMFPago.MontoLiquidacionFormaDePago = new BigDecimal(0).equals(opComSalFFMM.MontoComision)? opComSalFFMM.MontoComision: (opComSalFFMM.MontoComision.divide(CalculaImporteIva, MathContext.DECIMAL128).multiply(opComSalFFMM.TipoCambioCierre)).setScale(0, RoundingMode.FLOOR);
							operacionSpotConComSalFFMMFPago.NumeroMvtoCmbteFormaDePago = opComSalFFMM.IdSolicitudAporteRescate;
							operacionSpotConComSalFFMMFPago.NumeroMvtoCmbte = opComSalFFMM.IdSolicitudAporteRescate;
							operacionSpotConComSalFFMMFPago.FechaVctoTarjetaBancaria = "1900-01-01";
							operacionSpotConComSalFFMMFPago.FechaDocumentoPago = Fecha.stringToSqlDate(opComSalFFMM.FechaOperacion);
							
							entradaCrearMovimientoComprobante.p_MovimientosFormaDePago.add(operacionSpotConComSalFFMMFPago);
						}								
				    }
				}	
				
				if(entradaCrearMovimientoComprobante.p_Movimientos.size() > 0){
					entradaCrearMovimientoComprobante.p_IdEmpresa = in.p_IdEmpresa;
					entradaCrearMovimientoComprobante.p_IdUsuario = in.p_IdUsuario;
					entradaCrearMovimientoComprobante.p_IdModulo = in.p_IdModulo;
					entradaCrearMovimientoComprobante.p_simularOperacion = "N";
					entradaCrearMovimientoComprobante.p_UsaReglasNegocio = "S";
					entradaCrearMovimientoComprobante.p_EsIngresoAutomatico = "N";
					CrearMovimientoComprobante serviceCrearMovimientoComprobante = new CrearMovimientoComprobante();
					salidaCrearMovimientoComprobante = serviceCrearMovimientoComprobante.logica(conn,entradaCrearMovimientoComprobante);
					
					salidaCrearMovimientoComprobante.P_OUT_ERRORESCONTADOR = 0L;
					salidaCrearMovimientoComprobante.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
				}
				
				
				{

					CrearOperacionSpotTesoreria ingresoEgresoService = new CrearOperacionSpotTesoreria();
					CrearOperacionSpotTesoreriaEntrada entradaCrearIngresoEgreso = new CrearOperacionSpotTesoreriaEntrada();

					entradaCrearIngresoEgreso.p_IdEmpresa = in.p_IdEmpresa;
					entradaCrearIngresoEgreso.p_IdModulo = in.p_IdModulo;
					entradaCrearIngresoEgreso.p_IdUsuario = in.p_IdUsuario;
					entradaCrearIngresoEgreso.p_SimularOperacion = in.p_SimularOperacion;
					entradaCrearIngresoEgreso.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
					entradaCrearIngresoEgreso.p_DatosOperacion = selectOpeSpot
							.stream()
							.filter(op -> "S".equalsIgnoreCase(op.OperacionFicticia))
							.collect(Collectors.toList());
					
					entradaCrearIngresoEgreso.p_MediosDePago = selectForPagSpotFact
							.stream()
							.filter(fpag -> {
								OperacionSpot op = (OperacionSpot) operacionesMap.get(fpag.IdOperacion);
								return op != null && "S".equalsIgnoreCase(op.OperacionFicticia);
							})
							.collect(Collectors.toList());
					
					CrearOperacionSpotTesoreriaSalida salidaIngresoEgreso = ingresoEgresoService.logica(conn, entradaCrearIngresoEgreso);
					
					if (salidaIngresoEgreso.P_OUT_ERRORESCONTADOR > 0L) {
						out.P_OUT_ERRORESCONTADOR = salidaIngresoEgreso.P_OUT_ERRORESCONTADOR;
						out.P_OUT_MENSAJES = salidaIngresoEgreso.P_OUT_MENSAJES;
						return out;
					}
				}
				
				
				/***** ACTUALIZA ESTADO CONFIRMACION DE LAS OPERACIONES SPOT *****/
				actualizarEstadoConfirmacionOperaciones(conn, selectOpeSpot, opSpFactOut.P_OUT_IDOPERACIONSPOT, out.P_OUT_MENSAJES, in.p_IdEmpresa, mapMsg);
				
				/***** SALIDA *****/
				out.P_OUT_ADVERTENCIASCONTADOR = out.P_OUT_MENSAJES.stream().filter(spotFact -> spotFact.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ADVERTENCIA)).count();
			}
			

		}
		catch(Exception e){
			out.P_OUT_ERRORESCONTADOR = 1L;
			logger.error(e);
			throw e;
		}
		
		logger.info("Termino del servicio ConfirmarLiquidacionSpot, tiempo:" + (System.currentTimeMillis() - init));
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa2","C:\\Users\\lportales_ext\\OPTIMUS\\workspace\\optimus-corredores-dev-workspace\\OptimusDevJava\\application.properties")) { 
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String jsonDE = "{\"p_DatosOperacion\":[{\"NumeroOperacion\":83488909}],\"p_IdEmpresa\":1,\"p_IdUsuario\":9597,\"p_IdModulo\":\"MSP\",\"p_UsaReglasNegocio\":\"S\"}";
			ConfirmarLiquidacionSpotEntrada Objin = gson.fromJson(jsonDE, ConfirmarLiquidacionSpotEntrada.class);
			
			ConfirmarLiquidacionSpot demo = new ConfirmarLiquidacionSpot();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				ConfirmarLiquidacionSpotSalida ObjOut = demo.logica(con, Objin);
 				//con.commit();
 				con.commit();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				//er.printStackTrace();
 				con.rollback();
			}
			finally {
				con.close();
			}
	    }	
		System.out.println("== END ==");
	}
}