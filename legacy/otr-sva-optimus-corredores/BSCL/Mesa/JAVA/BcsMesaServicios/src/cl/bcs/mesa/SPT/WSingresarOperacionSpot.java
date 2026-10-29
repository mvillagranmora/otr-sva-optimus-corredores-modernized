package cl.bcs.mesa.SPT;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.mesa.SPT.CrearOperacionSpot.CrearOperacionSpotEntrada;
import cl.bcs.mesa.SPT.CrearOperacionSpot.CrearOperacionSpotSalida;
import cl.bcs.mesa.SPT.CrearOperacionSpot.FormaPagoObjIn;
import cl.bcs.mesa.SPT.CrearOperacionSpot.ModalidadDePago;
import cl.bcs.mesa.SPT.CrearOperacionSpot.OperacionObjIn;
import cl.bcs.mesa.SPT.CrearOperacionSpot.OperacionObjOut;
import cl.bcs.mesa.SPT.CrearOperacionSpot.RequisitosCrearOperacionSpot;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.StatsD;
import cl.bcs.utils.Utils;

public class WSingresarOperacionSpot implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(WSingresarOperacionSpot.class);
	
	private final String SQL_SELECT_CANALES = loadResourceToString("cl/bcs/mesa/SPT/sql/WSingresarOperacionSpot_SelectCanales.sql");
	private final String SQL_SELECT_INSTRUMENTO = loadResourceToString("cl/bcs/mesa/SPT/sql/WSingresarOperacionSpot_SelecInstrumento.sql");
	private final String SQL_SELECT_ESTADO_MESA = loadResourceToString("cl/bcs/mesa/SPT/sql/WSingresarOperacionSpot_ConsultarEstadoMesa.sql");
	private final String SQL_SELECT_EMPLEADO_DIGITADOR = loadResourceToString("cl/bcs/mesa/SPT/sql/WSingresarOperacionSpot_ConsultarEjecutivoDigitador.sql");
	
	public static final String METRIC_PREFIX = "mesa.spt.ws_ingresar_operacion_spot";
	public static final String METRIC_TIEMPO_GENERACION = METRIC_PREFIX + "." + "tiempo_generacion";
	public static final String METRIC_CANTIDAD_OPERACIONES = METRIC_PREFIX + "." + "cantidad_operaciones";
	public static final String METRIC_CANTIDAD_OPERACIONES_CONFIRMACION_AUTOMATICA = METRIC_PREFIX + "." + "cantidad_operaciones_confirmacion_automatica";

	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	public Long CantidadOperaciones = 0L;
	public Long OperacionesConfirmar = 0L;
	
	public static class EntradaWSingresarOperacionSpot extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_CalculaMargen;
		public String p_UsaReglasNegocio;
		public String p_SimularOperacion;
		public List<OperacionObjIn> p_DatosOperacion;
		public List<FormaPagoObjIn> p_DatosFormaPago;
		public String p_PermiteOperarProspectos;
		public String p_EsCargaMasiva;
		
		public EntradaWSingresarOperacionSpot() {
			this.p_DatosOperacion = new ArrayList<OperacionObjIn>();
			this.p_DatosFormaPago = new ArrayList<FormaPagoObjIn>();
			this.p_SimularOperacion = "N";
			this.p_PermiteOperarProspectos = "N";
			this.p_EsCargaMasiva 		= "N";
			this.p_IdModulo            = "MSP";
			this.p_CalculaMargen       = "N";
			this.p_UsaReglasNegocio    = "S";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}	
	}
	public static class Canal extends BcsPojo{
		public String IdTipoCanalIngreso;
		public String getParametroString() {
	        return IdTipoCanalIngreso;
	    }
	}
	public static class Instrumento extends BcsPojo{
		public Long IdInstrumento;
	}
	public static class EstadoMesa extends BcsPojo{
		public Long IdMesa;
		public Long IdEmpresa;
		public String Codigo;
		public String NombreMesa;
		public String IdTipoProducto;
		public String NombreTipoProducto;
		public String IdEstadoMesa;
		public String EstadoMesa;
		public String IdEstadoRegistro;
		public String IdTipoProductoMesaModulo;
		public String IdTipoProductoMesa;
		public String FechaActualSistema;
	}
	
	public static class Empleado extends BcsPojo{
		public Long IdEmpleadoDigitador;
		public String CodigoEmpleado;
	}

	public static class SalidaWSingresarOperacionSpot extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<OperacionObjOut> P_OUT_IDOPERACIONSPOT;
		
		public SalidaWSingresarOperacionSpot() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_IDOPERACIONSPOT = new ArrayList<OperacionObjOut>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaWSingresarOperacionSpot jin = gson.fromJson(in.toString(), EntradaWSingresarOperacionSpot.class);
		SalidaWSingresarOperacionSpot out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaWSingresarOperacionSpot logica(Connection con, EntradaWSingresarOperacionSpot entrada) throws Exception{
		logger.info("INICIO::WSingresarOperacionSpot");
		Long initTime = System.currentTimeMillis();
		
		SalidaWSingresarOperacionSpot out = new SalidaWSingresarOperacionSpot();
		SQL sql = new SQL();
		HashMap<String, Object> param = new HashMap<>();
		String canalPorDefecto = "";
		List<Long> numeroOperaciones = new ArrayList<>();
		String fechaSistema = "";
		String WSingresoMesaCerrada = "";
		List<EstadoMesa> EstadoMesa = new ArrayList<>();
		Timestamp fechaSistemaDate;
		
		SocioNegocioUtils.Empleado empleadoUsuario = SocioNegocioUtils.obtenerEmpleadoUsuario(con, entrada.p_IdEmpresa, entrada.p_IdUsuario).orElse(null);
		try {
			canalPorDefecto = ConfiguracionSistema.getParametroConfiguracionString(con,entrada.p_IdEmpresa, "MESA", "CANALINGRESOSPOTWS");
		} catch (Exception e) {	
			canalPorDefecto = "WBS"; 
			logger.info("Error linea 158 CELLINCECT-736" + e);
		}
		try {
			WSingresoMesaCerrada = ConfiguracionSistema.getParametroConfiguracionString(con,entrada.p_IdEmpresa, "MESA", "WS_INGRESO_MESA_CERRADA");
		} catch (Exception e) { 
			WSingresoMesaCerrada = "N"; 
			logger.info("Error linea 164 CELLINCECT-736" + e);
		}
		try {
			fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			fechaSistemaDate = Timestamp.valueOf(fechaSistema + " 00:00:00");
			param.put("IdEmpresa", entrada.p_IdEmpresa);
			EstadoMesa = sql.query(con, SQL_SELECT_ESTADO_MESA, param, EstadoMesa.class);
		} catch (Exception e) {
			out.P_OUT_ERRORESCONTADOR ++;
			out.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR,"ERROR_CONEXION","Error de conexion, al Obtener Estado de la Mesa"));
			logger.error(e);
			e.printStackTrace();
			logger.info("Error linea 176 CELLINCECT-736" + e);
			throw e;
        }

		if("C".equalsIgnoreCase(EstadoMesa.get(0).IdEstadoMesa) && "S".equals(WSingresoMesaCerrada)) {	
			ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(con, entrada.p_IdEmpresa);
		    Long idPaisSistema = empresa.IdPais;
		    logger.info("Error linea 184 CELLINCECT-736");
			/**SE CALCULA PROXIMO DIA HABIL**/
			ObtenerDiaSiguiente diaHabilSiguiente = new ObtenerDiaSiguiente();
			InObtenerDiaSiguiente diaHabilSiguienteIn = new InObtenerDiaSiguiente();
			diaHabilSiguienteIn.p_idPais = idPaisSistema;
			diaHabilSiguienteIn.p_diaHabil = "S";
			FechasIn fecha = new FechasIn();
			fecha.fechaInicial = fechaSistema;
			fecha.numeroDias = 1L;
			fecha.pais = idPaisSistema;
			diaHabilSiguienteIn.p_fechas.add(fecha);
			OutObtenerDiaSiguiente salDiaSiguiente = diaHabilSiguiente.logica(con, diaHabilSiguienteIn);
			String siguienteDiaHabil = salDiaSiguiente.p_out_fechas.get(0).fechaCalculada.toString();
			logger.info("Error linea 197 CELLINCECT-736");
			for (OperacionObjIn ope: entrada.p_DatosOperacion) {
				if(fechaSistemaDate.equals(ope.FechaOperacion)) {
					ope.FechaOperacion =  Timestamp.valueOf(siguienteDiaHabil + " 00:00:01");	
					logger.info("Error linea 201 CELLINCECT-736");
				}
			}
			logger.info("Error linea 204 CELLINCECT-736");
		}
		logger.info("Error linea 206 CELLINCECT-736");

		try {
			param.clear();
			List<Instrumento> instrumento = sql.query(con, SQL_SELECT_INSTRUMENTO, param, Instrumento.class);
			List<Canal> canales = sql.query(con, SQL_SELECT_CANALES, param, Canal.class);
			List<String> listaCanales = new ArrayList<>();
	        List<String> codigosEmpleado = new ArrayList<>();
	        for (OperacionObjIn ope : entrada.p_DatosOperacion) {
	            if (ope.CodigoEjecutivoDigitador != null && !ope.CodigoEjecutivoDigitador.isEmpty()) {
	                codigosEmpleado.add(ope.CodigoEjecutivoDigitador);
	            }
	        }
	        
			logger.info("Error linea 213 CELLINCECT-736");
	        for (int i = 0; i < canales.size(); i++) {
	        	listaCanales.add(canales.get(i).getParametroString());
	        	logger.info("Error linea 216 CELLINCECT-736");
	        }
	        
	        Map<String, Empleado> empleadosPorCodigo = new HashMap<String, Empleado>();
	        
	        if (!codigosEmpleado.isEmpty()) {
	            HashMap<String, Object> paramsCodigoEmpleado = new HashMap<>();
	            paramsCodigoEmpleado.put("CodigoEjecutivoDigitador", codigosEmpleado);
	            
	            List<Empleado> empleadosValidos = sql.query(con, SQL_SELECT_EMPLEADO_DIGITADOR, paramsCodigoEmpleado, Empleado.class);
	            
	            for (Empleado empleado : empleadosValidos) {
	                empleadosPorCodigo.put(empleado.CodigoEmpleado, empleado);
	            }
	        }

	        for (OperacionObjIn ope: entrada.p_DatosOperacion){
	        	if("S".equalsIgnoreCase(ope.EstadoCaja)) {
	        		ope.EstadoCaja = "PEN";
					ope.FacturacionAutomatica = "N";
					ope.Confirmar = "N";
	        	} else {
	        		ope.EstadoCaja = "NRE";
					ope.Confirmar = "S".equalsIgnoreCase(ope.FacturacionAutomatica) ? "S" : ope.Confirmar;
				}
	        	CantidadOperaciones++;
	        	OperacionesConfirmar = "S".equalsIgnoreCase(ope.Confirmar) ? OperacionesConfirmar+1 : OperacionesConfirmar;
	        	logger.info("Error linea 222 CELLINCECT-736");
				if(ope.IdTipoCanalIngreso.equalsIgnoreCase(null) || "".equalsIgnoreCase(ope.IdTipoCanalIngreso)) {
					ope.IdTipoCanalIngreso = "".equalsIgnoreCase(canalPorDefecto) ? "WBS": canalPorDefecto;
					logger.info("Error linea 225 CELLINCECT-736");
				}else if(!listaCanales.contains(ope.IdTipoCanalIngreso)) {
					out.P_OUT_ERRORESCONTADOR ++;
					out.P_OUT_MENSAJES.add(new MensajeSalida(Long.parseLong(ope.NumeroOperacion),MensajeSalida.MENSAJE_ERROR,"CANAL_ERROR","Canal Ingresado no existe o no esta activo, operacion: "+ ope.NumeroOperacion));
					logger.info("Error linea 229 CELLINCECT-736");
				}
				
				BigDecimal margenUtilidad = BigDecimal.ZERO;
				logger.info("Error linea 233 CELLINCECT-736");
				if (ope.IdInstrumento.equals(instrumento.get(0).IdInstrumento)) {					
					 margenUtilidad ="COMSPT".equals(ope.IdTipoOperacion) ? ((ope.TipoCambioCosto.multiply(ope.MontoMonedaPrincipal)).subtract (ope.TipoCambioCierre.multiply(ope.MontoMonedaPrincipal)))
			              								 				  : ((ope.TipoCambioCierre.multiply(ope.MontoMonedaPrincipal)).subtract(ope.TipoCambioCosto.multiply(ope.MontoMonedaPrincipal)));
					 logger.info("Error linea 237 CELLINCECT-736");
				}
				ope.Margen = margenUtilidad;
				ope.Utilidad = margenUtilidad;
				logger.info("Error linea 241 CELLINCECT-736");
				if("VENSPT".equalsIgnoreCase(ope.IdTipoOperacion)) {
					numeroOperaciones.add(Long.parseLong(ope.NumeroOperacion));
					logger.info("Error linea 244 CELLINCECT-736");
				}
				logger.info("Error linea 246 CELLINCECT-736");
				
				//Normalizacion y validacion PlataformaNegociacion
				String plataforma = (ope.IdPlataformaNegociacion == null) ? "" : ope.IdPlataformaNegociacion.trim().toUpperCase();
				ope.IdPlataformaNegociacion = plataforma;
	            if (ope.CodigoEjecutivoDigitador.isEmpty()) {
	                ope.IdEmpleadoDigitador = empleadoUsuario.IdEmpleado;
	            } else {
	                Empleado empleado = empleadosPorCodigo.get(ope.CodigoEjecutivoDigitador);

	                if (empleado != null) {
	                    ope.IdEmpleadoDigitador = empleado.IdEmpleadoDigitador;
	                } else {
	                	out.P_OUT_ERRORESCONTADOR++;
						HashMap<String, String> parametros = new HashMap<String, String>(2);
						parametros.put("CodigoEjecutivoDigitador", ope.CodigoEjecutivoDigitador);
			            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "CODIGO_EJECUTIVO_DIGITADOR_NO_EXISTE", parametros, con, entrada.p_IdEmpresa));
			            logger.error("Ejecutivo digitador no existe: " + ope.CodigoEjecutivoDigitador + " para operacion: " + ope.NumeroOperacion);
	                }
	            }
	        }
	        
	        if (out.P_OUT_ERRORESCONTADOR > 0) {
	        	return out;
	        }
	        
	        List<FormaPagoObjIn> formasDePago = CorreccionFormaPago(numeroOperaciones,entrada.p_DatosFormaPago);
	        logger.info("Error linea 249 CELLINCECT-736");
			CrearOperacionSpotEntrada crearOperacionSpotEntrada = new CrearOperacionSpotEntrada();
			crearOperacionSpotEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
			crearOperacionSpotEntrada.p_IdUsuario = entrada.p_IdUsuario;
			crearOperacionSpotEntrada.p_IdModulo = entrada.p_IdModulo;
			crearOperacionSpotEntrada.p_CalculaMargen = entrada.p_CalculaMargen;
			crearOperacionSpotEntrada.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
			crearOperacionSpotEntrada.p_SimularOperacion = entrada.p_SimularOperacion;
			crearOperacionSpotEntrada.p_EsCargaMasiva = "N";
			crearOperacionSpotEntrada.p_EsIngresoWS = "S";
			crearOperacionSpotEntrada.p_DatosOperacion = entrada.p_DatosOperacion;
			crearOperacionSpotEntrada.p_DatosFormaPago = formasDePago;
			logger.info("Error linea 261 CELLINCECT-736");
			CrearOperacionSpot crearOperacionSpot = new CrearOperacionSpot();
			CrearOperacionSpotSalida CrearOperacionSpotSalida = crearOperacionSpot.logica(con, crearOperacionSpotEntrada);
			
			out.P_OUT_MENSAJES.addAll(CrearOperacionSpotSalida.P_OUT_MENSAJES);
			out.P_OUT_ERRORESCONTADOR= MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			logger.info("Error linea 267 CELLINCECT-736");
			if(out.P_OUT_ERRORESCONTADOR>0){
				Iterator<MensajeSalida> iterador = out.P_OUT_MENSAJES.iterator();
				logger.info("Error linea 269 CELLINCECT-736");
		        while (iterador.hasNext()) {
		        	MensajeSalida mensaje = iterador.next();
		        	logger.info("Error linea 273 CELLINCECT-736");
		            if (MensajeSalida.MENSAJE_OK.equals(mensaje.tipo)) {
		                iterador.remove();
		                logger.info("Error linea 276 CELLINCECT-736");
		            }
		        }
			}else {
				out.P_OUT_IDOPERACIONSPOT.addAll(CrearOperacionSpotSalida.P_OUT_IDOPERACIONSPOT);	
				logger.info("Error linea 281 CELLINCECT-736");
			}
		}catch (Exception e) {
			out.P_OUT_ERRORESCONTADOR ++;
			out.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR,"ERROR_CONEXION","Error de conexion, al Ingresar la Operacion"));
			logger.error(e);
			logger.info("Error linea 287 CELLINCECT-736");
			e.printStackTrace();
			throw e;
		}
		
		
		if(out.P_OUT_ERRORESCONTADOR==0){
			StatsD.reportExecutionTimeMilis(METRIC_TIEMPO_GENERACION, (System.currentTimeMillis() - initTime));
			StatsD.reportCount(METRIC_CANTIDAD_OPERACIONES, (CantidadOperaciones));
			StatsD.reportCount(METRIC_CANTIDAD_OPERACIONES_CONFIRMACION_AUTOMATICA, (OperacionesConfirmar));
			logger.info("Error linea 297 CELLINCECT-736");
		}

		logger.info("FIN::WSingresarOperacionSpot");
		return out;
	}
	
	public List<FormaPagoObjIn> CorreccionFormaPago(List<Long> numeroOperaciones, List<FormaPagoObjIn> p_DatosFormaPago ) {
		List<FormaPagoObjIn> formasDePago = p_DatosFormaPago;
		
		for (FormaPagoObjIn fp : formasDePago) {
			if (numeroOperaciones.contains(fp.IdOperacion)) {
				
				if("MOVEGR".equals(fp.IdTipoMovimiento)) {
					fp.IdFormaDePago = fp.IdFormaDePago.replace("COMSPT","VENSPT");
					
					
				}else if("MOVING".equals(fp.IdTipoMovimiento)){
					fp.IdFormaDePago = fp.IdFormaDePago.replace("VENSPT","COMSPT");
				}
				
			}
		}
		
		return formasDePago;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {

		Gson gson = Utils.getGsonBuilder();			
		
		
		String jsonDISTRI = "{\r\n"
				+ "  p_IdEmpresa: 1,\r\n"
				+ "  p_IdUsuario: 76522,\r\n"
				+ "  p_ClaveUsuario: \"\",\r\n"
				+ "  p_IdModulo: \"MSP\",\r\n"
				+ "  p_CalculaMargen: \"N\",\r\n"
				+ "  p_UsaReglasNegocio: \"S\",\r\n"
				+ "  p_IdTipoCanalIngreso: \"WBS\",\r\n"
				+ "  p_DatosOperacion: [\r\n"
				+ "    {\r\n"
				+ "      NumeroOperacion: 1,\r\n"
				+ "      IdInstrumento: 133258,\r\n"
				+ "      IdTipoOperacion: \"VENSPT\",\r\n"
				+ "      IdTipoProducto: \"MEME\",\r\n"
				+ "      IdCliente: 16290,\r\n"
				+ "      IdPortafolio: 25254,\r\n"
				+ "      FechaOperacion: \"2026-05-28\",\r\n"
				+ "      HoraOperacion: \"14:11:12\",\r\n"
				+ "      IdMonedaPrincipal: \"USD\",\r\n"
				+ "      IdMonedaSecundaria: \"CLP\",\r\n"
				+ "      MontoMonedaPrincipal: 220,\r\n"
				+ "      MontoMonedaSecundaria: 101,\r\n"
				+ "      TipoCambioCierre: 814.01,\r\n"
				+ "      TipoCambioCierreForward: 0,\r\n"
				+ "      ParidadCierre: 0,\r\n"
				+ "      ParidadCierreForward: 0,\r\n"
				+ "      TipoCambioCosto: 809.01,\r\n"
				+ "      ParidadCosto: 1,\r\n"
				+ "      MontoEquivalente: 101,\r\n"
				+ "      MontoRentabilidad: 0,\r\n"
				+ "      MontoRentabilidadCanal: 0,\r\n"
				+ "      Margen: 900,\r\n"
				+ "      Utilidad: 900,\r\n"
				+ "      PuntaCompra: 909.01,\r\n"
				+ "      PuntaVenta: 909.01,\r\n"
				+ "      SolicitudTipoCambio: \"N\",\r\n"
				+ "      IdMesa: 40,\r\n"
				+ "      IdModulo: \"SPT\",\r\n"
				+ "      IdTipoDolar: 1,\r\n"
				+ "      TipoComprobanteFacturacion: \"FEE\",\r\n"
				+ "      IdAgente: 54447,\r\n"
				+ "      BenOur: \"BEN\",\r\n"
				+ "      EquivalenteConGasto: 0,\r\n"
				+ "      TipoCambioCierreConGasto: 0,\r\n"
				+ "      Observacion: \"Test de White confirma auto, sin requerir autorizacion\",\r\n"
				+ "      CodigoComercio: \"20055\",\r\n"
				+ "      FolioPlanilla: 0,\r\n"
				+ "      FolioInterfaz: \"\",\r\n"
				+ "      OperacionFicticia: \"N\",\r\n"
				+ "      IdTipoCanalIngreso: \"WBS\",\r\n"
				+ "      Confirmar: \"S\",\r\n"
				+ "      IdDatatec: 0,\r\n"
				+ "      Estado: \"VIG\",\r\n"
				+ "      IdSolicitudAporteRescate: 0,\r\n"
				+ "      AfectoADerivado: \"N\",\r\n"
				+ "      DolarObservado: \"N\",\r\n"
				+ "      CodigoEjecutivoDigitador: \"\",\r\n"
				+ "	  	 IdPlataformaNegociacion: \"OTC\"\r\n"
				+ "    }\r\n"
				+ "  ],\r\n"
				+ "  p_DatosFormaPago: [\r\n"
				+ "    {\r\n"
				+ "      IdInternoPagoOperacion: 0,\r\n"
				+ "      IdComprobantePagoOperacion: 0,\r\n"
				+ "      IdTipoMovimiento: \"MOVEGR\",\r\n"
				+ "      IdFormaDePago: \"COMSPT_TRA_USD_1\",\r\n"
				+ "      IdOperacion: 1,\r\n"
				+ "      RecibePagaPortafolio: \"P\",\r\n"
				+ "      IdMoneda: \"USD\",\r\n"
				+ "      IdMonedaLiquidacion: \"USD\",\r\n"
				+ "      MontoFormaDePago: 180,\r\n"
				+ "      AfectarTesoreria: \"S\",\r\n"
				+ "      IdCuentaBancaria: 0\r\n"
				+ "    },\r\n"
				+ "    {\r\n"
				+ "      IdInternoPagoOperacion: 1,\r\n"
				+ "      IdComprobantePagoOperacion: 0,\r\n"
				+ "      IdTipoMovimiento: \"MOVING\",\r\n"
				+ "      IdFormaDePago: \"VENSPT_TRA_CLP_1\",\r\n"
				+ "      IdOperacion: 1,\r\n"
				+ "      RecibePagaPortafolio: \"R\",\r\n"
				+ "      IdMoneda: \"CLP\",\r\n"
				+ "      IdMonedaLiquidacion: \"CLP\",\r\n"
				+ "      MontoFormaDePago: 101,\r\n"
				+ "      AfectarTesoreria: \"S\",\r\n"
				+ "      IdCuentaBancaria: 3057\r\n"
				+ "    }\r\n"
				+ "  ]\r\n"
				+ "}";
		String jsonINTERB = "{\r\n"
				+ "  p_IdEmpresa: 1,\r\n"
				+ "  p_IdUsuario: 0,\r\n"
				+ "  p_ClaveUsuario: \"\",\r\n"
				+ "  p_IdModulo: \"MSP\",\r\n"
				+ "  p_CalculaMargen: \"N\",\r\n"
				+ "  p_UsaReglasNegocio: \"S\",\r\n"
				+ "  p_IdTipoCanalIngreso: \"WBS\",\r\n"
				+ "  p_DatosOperacion: [\r\n"
				+ "    {\r\n"
				+ "      NumeroOperacion: 33,\r\n"
				+ "      IdInstrumento: 133257,\r\n"
				+ "      IdTipoOperacion: \"VENSPT\",\r\n"
				+ "      IdTipoProducto: \"MEME\",\r\n"
				+ "      IdCliente: 16290,\r\n"
				+ "      IdPortafolio: 25254,\r\n"
				+ "      FechaOperacion: \"2023-07-26\",\r\n"
				+ "      HoraOperacion: \"16:50:00\",\r\n"
				+ "      IdMonedaPrincipal: \"USD\",\r\n"
				+ "      IdMonedaSecundaria: \"CLP\",\r\n"
				+ "      MontoMonedaPrincipal: 180,\r\n"
				+ "      MontoMonedaSecundaria: 101,\r\n"
				+ "      TipoCambioCierre: 814.01,\r\n"
				+ "      TipoCambioCierreForward: 0,\r\n"
				+ "      ParidadCierre: 0,\r\n"
				+ "      ParidadCierreForward: 0,\r\n"
				+ "      TipoCambioCosto: 809.01,\r\n"
				+ "      ParidadCosto: 1,\r\n"
				+ "      MontoEquivalente: 101,\r\n"
				+ "      MontoRentabilidad: 0,\r\n"
				+ "      MontoRentabilidadCanal: 0,\r\n"
				+ "      Margen: 900,\r\n"
				+ "      Utilidad: 900,\r\n"
				+ "      PuntaCompra: 909.01,\r\n"
				+ "      PuntaVenta: 909.01,\r\n"
				+ "      SolicitudTipoCambio: \"N\",\r\n"
				+ "      IdMesa: 40,\r\n"
				+ "      IdModulo: \"SPT\",\r\n"
				+ "      IdTipoDolar: 1,\r\n"
				+ "      TipoComprobanteFacturacion: \"FEE\",\r\n"
				+ "      IdAgente: 54447,\r\n"
				+ "      BenOur: \"BEN\",\r\n"
				+ "      EquivalenteConGasto: 0,\r\n"
				+ "      TipoCambioCierreConGasto: 0,\r\n"
				+ "      Observacion: \"Test de White confirma auto, sin requerir autorizacion\",\r\n"
				+ "      CodigoComercio: \"20055\",\r\n"
				+ "      FolioPlanilla: 0,\r\n"
				+ "      FolioInterfaz: \"\",\r\n"
				+ "      OperacionFicticia: \"N\",\r\n"
				+ "      IdTipoCanalIngreso: \"WBS\",\r\n"
				+ "      Confirmar: \"S\",\r\n"
				+ "      IdDatatec: 0,\r\n"
				+ "      Estado: \"VIG\",\r\n"
				+ "      IdSolicitudAporteRescate: 0,\r\n"
				+ "      AfectoADerivado: \"N\",\r\n"
				+ "      DolarObservado: \"N\",\r\n"
				+ "	  	 IdPlataformaNegociacion: \"ZODM\"\r\n"
				+ "    }\r\n"
				+ "  ],\r\n"
				+ "  p_DatosFormaPago: [\r\n"
				+ "    {\r\n"
				+ "      IdInternoPagoOperacion: 0,\r\n"
				+ "      IdComprobantePagoOperacion: 0,\r\n"
				+ "      IdTipoMovimiento: \"MOVEGR\",\r\n"
				+ "      IdFormaDePago: \"COMSPT_TRA_USD_1\",\r\n"
				+ "      IdOperacion: 33,\r\n"
				+ "      RecibePagaPortafolio: \"P\",\r\n"
				+ "      IdMoneda: \"USD\",\r\n"
				+ "      IdMonedaLiquidacion: \"USD\",\r\n"
				+ "      MontoFormaDePago: 180,\r\n"
				+ "      AfectarTesoreria: \"S\",\r\n"
				+ "      IdCuentaBancaria: 0\r\n"
				+ "    },\r\n"
				+ "    {\r\n"
				+ "      IdInternoPagoOperacion: 1,\r\n"
				+ "      IdComprobantePagoOperacion: 0,\r\n"
				+ "      IdTipoMovimiento: \"MOVING\",\r\n"
				+ "      IdFormaDePago: \"VENSPT_TRA_CLP_1\",\r\n"
				+ "      IdOperacion: 33,\r\n"
				+ "      RecibePagaPortafolio: \"R\",\r\n"
				+ "      IdMoneda: \"CLP\",\r\n"
				+ "      IdMonedaLiquidacion: \"CLP\",\r\n"
				+ "      MontoFormaDePago: 101,\r\n"
				+ "      AfectarTesoreria: \"S\",\r\n"
				+ "      IdCuentaBancaria: 3057\r\n"
				+ "    }\r\n"
				+ "  ]\r\n"
				+ "}";



		EntradaWSingresarOperacionSpot Objin = gson.fromJson(jsonDISTRI, EntradaWSingresarOperacionSpot.class);
		WSingresarOperacionSpot demo = new WSingresarOperacionSpot();				
		try {
			SalidaWSingresarOperacionSpot ObjOut = demo.logica(con, Objin);
			// con.commit();
			con.rollback();

			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
						
    	};
		System.out.println("== END ==");
	}
	
}
