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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.mesa.SPT.CrearOperacionSpot.CrearOperacionSpotEntrada;
import cl.bcs.mesa.SPT.CrearOperacionSpot.CrearOperacionSpotSalida;
import cl.bcs.mesa.SPT.CrearOperacionSpot.FormaPagoObjIn;
import cl.bcs.mesa.SPT.CrearOperacionSpot.ModalidadDePago;
import cl.bcs.mesa.SPT.CrearOperacionSpot.OperacionObjIn;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ProcesarCargaMasivaArchivoOperacionSpot implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ProcesarCargaMasivaArchivoOperacionSpot.class);
	
	private final String SQL_SELECT_OPERACION_DETALLE 	= loadResourceToString("cl/bcs/mesa/SPT/sql/ProcesarCargaMasivaArchivoOperacionSpot_SelectOperacionDetalle.sql");
	private final String SQL_SELECT_CARGA_MASIVA 		= loadResourceToString("cl/bcs/mesa/SPT/sql/ProcesarCargaMasivaArchivoOperacionSpot_SelectCargaMasiva.sql");
	private final String SQL_UPDATE_CARGA_MASIVA 		= loadResourceToString("cl/bcs/mesa/SPT/sql/ProcesarCargaMasivaArchivoOperacionSpot_UpdateOperacionesSpotEstado.sql");
	private final String SQL_UPDATE_CARGA_MASIVA_DETALLE= loadResourceToString("cl/bcs/mesa/SPT/sql/ProcesarCargaMasivaArchivoOperacionSpot_UpdateOperacionesSpotDetalleEstado.sql");
	private final String SELECT_FORMA_PAGO_MESA			= loadResourceToString("cl/bcs/mesa/SPT/sql/ProcesarCargaMasivaArchivoOperacionSpot_SelectFormaPagoMesa.sql");
	private final String SELECT_FORMA_PAGO_CLIENTE		= loadResourceToString("cl/bcs/mesa/SPT/sql/ProcesarCargaMasivaArchivoOperacionSpot_SelectFormaPagoCliente.sql");
	
	public static class EntradaProcesarCargaMasivaArchivoOperacionSpot extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_SimularOperacion;
		public String p_CalculaMargen;
		public String p_IdTipoCanalIngreso;
		public List<OperacionSpot> p_CargaMasivaProcesar;
		
		public EntradaProcesarCargaMasivaArchivoOperacionSpot() {
			this.p_CargaMasivaProcesar = new ArrayList<OperacionSpot>();
            p_IdModulo            = "MSP";
            p_CalculaMargen       = "N";
            p_UsaReglasNegocio    = "S";
            p_IdTipoCanalIngreso  = "AGE";
            p_SimularOperacion 	  = "N";
		}		
	}

	public static class OperacionSpot extends BcsPojo {
		public Long IdCargaMasiva;
	}
	public static class FormasDePagoCliente extends BcsPojo {
		public Long IdPortafolio;
		public Long IdCuentaBancaria;
		public String IdFormaDePago ;
	}
	public static class FormasDePagoMesa extends BcsPojo {
		public Long IdCuentaBancaria = 0L;
		public String IdFormaDePago;
	}
	
	public static class OperacionCargaMasiva extends BcsPojo {
		public Long 	IdCargaMasiva;
		public Date 	FechaCarga;
		public String 	FechaProceso;
		public Long 	CantidadRegistros; 
		public Long 	IdEmpresa;
		public Long 	IdUsuario;
		public String 	IdEstado;
		public String 	NombreArchivo;
		public Long 	IdTipoCargaMasiva;
		public String 	CodigoTipoCargaMasiva;
	}
	
	public static class OperacionSpotDetalle extends BcsPojo {
		public   Long 			IdCargaMasivaDetalle;
		public   String 		DocumentoIdentidad;
		public   String 		CodigoPortafolio;
		public   String 		ProductoAgrupador;
		public   String 		TipoOperacion;
		public   Date 			FechaOperacion;
		public   Time 			HoraOperacion;
		public   String 		CanalIngreso;
		public   BigDecimal 	MontoMonedaPrincipal;
		public   BigDecimal 	TipoCambioCierre;
		public   BigDecimal 	TipoCambioCosto;
		public   BigDecimal 	PuntaCompra;
		public   BigDecimal 	PuntaVenta;
		public   String 		FormaPagoEntregamos;
		public   String	 		FormaPagoRecibimos;
		public   String 		Observacion;
		public   String 		AfectoADerivado;
		public   String 		DolarObservado;
		public   Long 			CodigoComercio;
		public   String 		DetalleValidacion;
		public   Long 			IdCargaMasiva;
		public   Long 			IdTipoCargaMasiva;
		
		public  Long 			IdCliente;
		public  Long 			IdPortafolio;
		public  String 			TipoComprobanteFacturacion;
		

		public   Long 			IdMesa;
		public   Long 			IdCuentaBancariaCorredora;
		public   Long 			IdCuentaBancariaCliente;
		public   Long 			IdInstrumento;
		public   Long 			IdPersonaEjecutivo;
		
		public  OperacionSpotDetalle() {
			

			IdMesa = 0L;
			IdCuentaBancariaCorredora = 0L;
			IdCuentaBancariaCliente = 0L;
			IdInstrumento = 0L;
			IdPersonaEjecutivo = 0L;
			DetalleValidacion = "";
		}
	}
	
	public static class MensajeProcesado extends BcsPojo {
	    private Long idCarga;
	    private String descripcion;

	}
	

	public static class SalidaProcesarCargaMasivaArchivoOperacionSpot extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaProcesarCargaMasivaArchivoOperacionSpot() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaProcesarCargaMasivaArchivoOperacionSpot jin = gson.fromJson(in.toString(), EntradaProcesarCargaMasivaArchivoOperacionSpot.class);
		SalidaProcesarCargaMasivaArchivoOperacionSpot out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaProcesarCargaMasivaArchivoOperacionSpot logica(Connection con, EntradaProcesarCargaMasivaArchivoOperacionSpot entrada) throws Exception{
		logger.info("INICIO::ProcesarCargaMasivaArchivoOperacionSpot");
		SalidaProcesarCargaMasivaArchivoOperacionSpot out = new SalidaProcesarCargaMasivaArchivoOperacionSpot();
		
		List<OperacionCargaMasiva> operacionCargaMasiva = obtenerOperacionCargaMasiva(con, entrada.p_CargaMasivaProcesar);
		List<OperacionSpotDetalle> operacionesDetalle = obtenerOperacionDetalle(con, entrada.p_CargaMasivaProcesar);
		
		if(!operacionCargaMasiva.get(0).IdEstado.equals("ING")) {
			out.P_OUT_MENSAJES.add(new MensajeSalida( operacionCargaMasiva.get(0).IdCargaMasiva,MensajeSalida.MENSAJE_ERROR,"ESATADO_CARGA_MASIVA_DEBE_SER_INGRESADO",null,con,entrada.p_IdEmpresa));
			out.P_OUT_ERRORESCONTADOR= MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			return out;
		}
		

		CrearOperacionSpotSalida CrearOperacionSpotSalida = crearOperaciones(con, operacionesDetalle, operacionCargaMasiva ,entrada);		
		out.P_OUT_MENSAJES.addAll(CrearOperacionSpotSalida.P_OUT_MENSAJES);
		out.P_OUT_ERRORESCONTADOR= MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		
		List<MensajeProcesado> mensajeProcesado = obtenerMensajeProcesado(out.P_OUT_MENSAJES);
		
		
		for(OperacionSpotDetalle opeDetalle : operacionesDetalle) {
			for (MensajeProcesado mensaje : mensajeProcesado) {
				if((mensaje.idCarga.toString().equals(opeDetalle.IdCargaMasivaDetalle.toString()))) {	
					opeDetalle.DetalleValidacion =  mensaje.descripcion;
				}
			}
			if("".equals(opeDetalle.DetalleValidacion) || null == opeDetalle.DetalleValidacion) {
				opeDetalle.DetalleValidacion = "OK";
			}
		}
		

		try {
			
		SQL sql = new SQL();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		operacionCargaMasiva.get(0).IdEstado = out.P_OUT_ERRORESCONTADOR > 0 ? "ERR" : "PRO";
		operacionCargaMasiva.get(0).FechaProceso = fechaSistema+ " 00:00:00.000";
		int[] res = sql.batch(con, SQL_UPDATE_CARGA_MASIVA, operacionCargaMasiva, OperacionCargaMasiva.class);
		int[] res2 = sql.batch(con, SQL_UPDATE_CARGA_MASIVA_DETALLE, operacionesDetalle, OperacionSpotDetalle.class);
		
		
		if (res.length>0 && res.length>0) {
			out.P_OUT_ERRORESCONTADOR = 0L;
		}
		}catch (Exception e) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			logger.error(e);
			e.printStackTrace();
			throw e;
		}
		logger.info("FIN::ProcesarCargaMasivaArchivoOperacionSpot");
		return out;
	}
	
	
	private List<MensajeProcesado> obtenerMensajeProcesado(List<MensajeSalida> p_OUT_MENSAJES) throws Exception {
		logger.trace("INICIO::ProcesarCargaMasivaArchivoOperacionSpot.obtenerMensajeProcesado");
		
		 List<MensajeProcesado> mensajes = new ArrayList<MensajeProcesado>() ; ;
		 Map<Long, String> descripcionPorIdCarga = new HashMap<>();
		 	

        for (MensajeSalida mensaje : p_OUT_MENSAJES) {
            Long idCarga = mensaje.id;
            String descripcion = mensaje.descripcion;

            if(("ERROR").equalsIgnoreCase(mensaje.tipo)) {
            	// Si ya existe un mensaje con ese idcarga, se concatena la descripción
            	if (descripcionPorIdCarga.containsKey(idCarga)) {
            		String descripcionActual = descripcionPorIdCarga.get(idCarga);
            		descripcionPorIdCarga.put(idCarga, descripcionActual + " ; " + descripcion);
            	} else {
            		descripcionPorIdCarga.put(idCarga, descripcion);
            	}
            }
        }
        for (Map.Entry<Long, String> entry : descripcionPorIdCarga.entrySet()) {

            MensajeProcesado mensaje = new MensajeProcesado();
            mensaje.idCarga = entry.getKey();
            mensaje.descripcion = entry.getValue();
            
            mensajes.add(mensaje);
        }
   
		logger.trace("FIN::ProcesarCargaMasivaArchivoOperacionSpot.obtenerMensajeProcesado");
		return mensajes;
	}
	
	
	private List<OperacionCargaMasiva> obtenerOperacionCargaMasiva(Connection con, List<OperacionSpot> operaciones) throws Exception {
		logger.trace("INICIO::ProcesarCargaMasivaArchivoOperacionSpot.obtenerOperacionDetalle");
		
		List<Long> idOperaciones = operaciones
				.stream()
				.map(operacion->operacion.IdCargaMasiva)
				.collect(Collectors.toList());
		HashMap<String, Object> parametros = new HashMap<String, Object>(2);
		parametros.put("IdCargaMasiva", idOperaciones);
		
		SQL sql = new SQL();
		List<OperacionCargaMasiva> operacionCargaMasiva = sql.query(con, SQL_SELECT_CARGA_MASIVA, parametros, OperacionCargaMasiva.class);
		
		logger.trace("FIN::ProcesarCargaMasivaArchivoOperacionSpot.obtenerOperacionDetalle");
		return operacionCargaMasiva;
	}
	
	private List<OperacionSpotDetalle> obtenerOperacionDetalle(Connection con, List<OperacionSpot> operaciones) throws Exception {
		logger.trace("INICIO::ProcesarCargaMasivaArchivoOperacionSpot.obtenerOperacionDetalle");
		
		List<Long> idOperaciones = operaciones
				.stream()
				.map(operacion->operacion.IdCargaMasiva)
				.collect(Collectors.toList());
		HashMap<String, Object> parametros = new HashMap<String, Object>(2);
		parametros.put("IdCargaMasiva", idOperaciones);
		
		SQL sql = new SQL();
		List<OperacionSpotDetalle> operacionesDetalle = sql.query(con, SQL_SELECT_OPERACION_DETALLE, parametros, OperacionSpotDetalle.class);
		
		logger.trace("FIN::ProcesarCargaMasivaArchivoOperacionSpot.obtenerOperacionDetalle");
		return operacionesDetalle;
	}

	private CrearOperacionSpotSalida crearOperaciones(Connection con, List<OperacionSpotDetalle> operacionDetalle,List<OperacionCargaMasiva> operacionCargaMasiva, EntradaProcesarCargaMasivaArchivoOperacionSpot entrada) throws Exception {
		logger.trace("INICIO::ProcesarCargaMasivaArchivoOperacionSpot.crearOperaciones");
		
		SQL sql = new SQL();
		HashMap<String, Object> param = new HashMap<>();
		List<String> IdFormaDePago = new ArrayList<>();
		List<Long> IdPortafolioCli = new ArrayList<>();
        for(OperacionSpotDetalle Detalle : operacionDetalle) {
        	IdFormaDePago.add(Detalle.FormaPagoRecibimos);
        	IdPortafolioCli.add(Detalle.IdPortafolio);
        }
        param.put("IdFormaDePago", IdFormaDePago);	
		List<FormasDePagoMesa> formasPagoMesa = sql.query(con, SELECT_FORMA_PAGO_MESA, param, FormasDePagoMesa.class);
		
		param = new HashMap<>();
        param.put("IdFormaDePago", IdFormaDePago);	
        param.put("IdPortafolio", IdPortafolioCli);	
		List<FormasDePagoCliente> formasDePagoCliente = sql.query(con, SELECT_FORMA_PAGO_CLIENTE, param, FormasDePagoCliente.class);
		

		CrearOperacionSpotEntrada crearOperacionSpotEntrada = new CrearOperacionSpotEntrada();
		crearOperacionSpotEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
		crearOperacionSpotEntrada.p_IdUsuario = entrada.p_IdUsuario;
		crearOperacionSpotEntrada.p_IdModulo = entrada.p_IdModulo;
		crearOperacionSpotEntrada.p_CalculaMargen = entrada.p_CalculaMargen;
		crearOperacionSpotEntrada.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		crearOperacionSpotEntrada.p_SimularOperacion = entrada.p_SimularOperacion;
		crearOperacionSpotEntrada.p_EsCargaMasiva = "S";
		
		List<FormaPagoObjIn> p_DatosFormaPago = new ArrayList<FormaPagoObjIn>() ;
		
		Long IdInternoPagoOperacion = 1L;
		Long IdOperacion = 0L;

		for (OperacionSpotDetalle OperacionSpotDetalle : operacionDetalle) {

			String MonedaPrincipal = OperacionSpotDetalle.FormaPagoEntregamos.substring(11, 14);
			String MonedaSecundaria = OperacionSpotDetalle.FormaPagoRecibimos.substring(11, 14);

			OperacionObjIn objetoOperacionIn = new OperacionObjIn();
			FormaPagoObjIn objetoFormaPagoIn =new FormaPagoObjIn();
			IdOperacion ++;
			
			BigDecimal tipoCambioPrincipal = OperacionSpotDetalle.TipoOperacion.equalsIgnoreCase("COMSPT") ? OperacionSpotDetalle.TipoCambioCosto : OperacionSpotDetalle.TipoCambioCierre;
			BigDecimal tipoCambioSecundario = OperacionSpotDetalle.TipoOperacion.equalsIgnoreCase("COMSPT") ? OperacionSpotDetalle.TipoCambioCierre : OperacionSpotDetalle.TipoCambioCosto;
			
			objetoOperacionIn.NumeroOperacion 			= OperacionSpotDetalle.IdCargaMasivaDetalle.toString();//IdOperacion.toString();
			objetoOperacionIn.IdInstrumento 			= OperacionSpotDetalle.IdInstrumento;
            objetoOperacionIn.IdTipoOperacion			= OperacionSpotDetalle.TipoOperacion;
            objetoOperacionIn.ProductoAgrupador			= OperacionSpotDetalle.ProductoAgrupador;
            objetoOperacionIn.IdTipoProducto			= "MEME";
            objetoOperacionIn.IdCliente					= OperacionSpotDetalle.IdCliente;
            objetoOperacionIn.IdPortafolio				= OperacionSpotDetalle.IdPortafolio;
            objetoOperacionIn.CodigoPortafolio			= OperacionSpotDetalle.CodigoPortafolio;
            objetoOperacionIn.FechaOperacion			= (Timestamp)OperacionSpotDetalle.FechaOperacion;
			objetoOperacionIn.HoraOperacion				= OperacionSpotDetalle.HoraOperacion+"";
            objetoOperacionIn.IdMonedaPrincipal			= MonedaPrincipal;   
            objetoOperacionIn.IdMonedaSecundaria		= MonedaSecundaria;
            objetoOperacionIn.MontoMonedaPrincipal		= OperacionSpotDetalle.MontoMonedaPrincipal;
            objetoOperacionIn.MontoMonedaSecundaria		= OperacionSpotDetalle.MontoMonedaPrincipal.multiply(OperacionSpotDetalle.TipoCambioCierre);
            objetoOperacionIn.TipoCambioCierre			= OperacionSpotDetalle.TipoCambioCierre;
            objetoOperacionIn.TipoCambioCierreForward	= BigDecimal.ZERO;
            objetoOperacionIn.ParidadCierre				= BigDecimal.ZERO;
            objetoOperacionIn.ParidadCierreForward		= BigDecimal.ZERO;
            objetoOperacionIn.TipoCambioCosto			= OperacionSpotDetalle.TipoCambioCosto;
            objetoOperacionIn.ParidadCosto				= BigDecimal.ZERO;
            objetoOperacionIn.MontoEquivalente			= objetoOperacionIn.MontoMonedaSecundaria;
            objetoOperacionIn.MontoRentabilidad			= BigDecimal.ZERO;
            objetoOperacionIn.MontoRentabilidadCanal	= BigDecimal.ZERO;
            objetoOperacionIn.Margen					= ((tipoCambioPrincipal.multiply(OperacionSpotDetalle.MontoMonedaPrincipal)).subtract (tipoCambioSecundario.multiply(OperacionSpotDetalle.MontoMonedaPrincipal)));
            objetoOperacionIn.Utilidad					= ((tipoCambioPrincipal.multiply(OperacionSpotDetalle.MontoMonedaPrincipal)).subtract (tipoCambioSecundario.multiply(OperacionSpotDetalle.MontoMonedaPrincipal)));
            objetoOperacionIn.PuntaCompra				= OperacionSpotDetalle.PuntaCompra;
            objetoOperacionIn.PuntaVenta				= OperacionSpotDetalle.PuntaVenta;
            objetoOperacionIn.SolicitudTipoCambio		= "N";
            objetoOperacionIn.IdMesa					= OperacionSpotDetalle.IdMesa;
           objetoOperacionIn.IdModulo					= entrada.p_IdModulo;
            objetoOperacionIn.IdTipoDolar				= 1L;
            objetoOperacionIn.TipoComprobanteFacturacion= OperacionSpotDetalle.TipoComprobanteFacturacion;
            objetoOperacionIn.IdAgente					= OperacionSpotDetalle.IdPersonaEjecutivo;
            objetoOperacionIn.BenOur					= "BEN";
            objetoOperacionIn.EquivalenteConGasto		= BigDecimal.ZERO;
            objetoOperacionIn.TipoCambioCierreConGasto	= BigDecimal.ZERO;
            objetoOperacionIn.Observacion				= OperacionSpotDetalle.Observacion;
            objetoOperacionIn.CodigoComercio			= OperacionSpotDetalle.CodigoComercio; 
            objetoOperacionIn.FolioPlanilla				= 0L;
            objetoOperacionIn.FolioInterfaz				= "";
            objetoOperacionIn.OperacionFicticia			= "N";
            objetoOperacionIn.IdTipoCanalIngreso		= OperacionSpotDetalle.CanalIngreso;
            objetoOperacionIn.IdDatatec					= "0";
            objetoOperacionIn.Estado					= "VIG";
            objetoOperacionIn.IdSolicitudAporteRescate	= 0L;
            objetoOperacionIn.AfectoADerivado			= OperacionSpotDetalle.AfectoADerivado.trim() == "" ? "N" : OperacionSpotDetalle.AfectoADerivado.trim();
            objetoOperacionIn.DolarObservado			= OperacionSpotDetalle.DolarObservado.trim() == "" ? "N" : OperacionSpotDetalle.DolarObservado.trim();
            crearOperacionSpotEntrada.p_DatosOperacion.add(objetoOperacionIn) ;
			
            
            //MontoMonedaPrincipal
            objetoFormaPagoIn.IdInternoPagoOperacion = IdInternoPagoOperacion++;
            objetoFormaPagoIn.IdComprobantePagoOperacion = 0L;
            objetoFormaPagoIn.IdTipoMovimiento = "MOVEGR";
            objetoFormaPagoIn.IdFormaDePago = OperacionSpotDetalle.FormaPagoEntregamos;
            objetoFormaPagoIn.IdOperacion = (OperacionSpotDetalle.IdCargaMasivaDetalle);
            objetoFormaPagoIn.RecibePagaPortafolio = OperacionSpotDetalle.TipoOperacion.equalsIgnoreCase("COMSPT") ? "R" :"P";
            objetoFormaPagoIn.IdMoneda = MonedaPrincipal;
            objetoFormaPagoIn.IdMonedaLiquidacion = MonedaPrincipal;
            objetoFormaPagoIn.MontoFormaDePago = OperacionSpotDetalle.MontoMonedaPrincipal;
            
            if(OperacionSpotDetalle.FormaPagoEntregamos != null && OperacionSpotDetalle.FormaPagoEntregamos.contains("_CIN_")){
            	objetoFormaPagoIn.AfectarTesoreria = "N";
            }else {
            	objetoFormaPagoIn.AfectarTesoreria = "S";            	
            }
            objetoFormaPagoIn.IdCuentaBancaria = OperacionSpotDetalle.IdCuentaBancariaCliente;
            crearOperacionSpotEntrada.p_DatosFormaPago.add(objetoFormaPagoIn) ;
            p_DatosFormaPago.add(objetoFormaPagoIn);

            
            
            //MontoMonedaSecundaria
            objetoFormaPagoIn = new FormaPagoObjIn();
            objetoFormaPagoIn.IdInternoPagoOperacion = IdInternoPagoOperacion++;
            objetoFormaPagoIn.IdComprobantePagoOperacion = 0L;
            objetoFormaPagoIn.IdTipoMovimiento = "MOVING";
            objetoFormaPagoIn.IdFormaDePago =OperacionSpotDetalle.FormaPagoRecibimos;
            objetoFormaPagoIn.IdOperacion = (OperacionSpotDetalle.IdCargaMasivaDetalle);
            objetoFormaPagoIn.RecibePagaPortafolio = OperacionSpotDetalle.TipoOperacion.equalsIgnoreCase("COMSPT") ? "P": "R";
            objetoFormaPagoIn.IdMoneda = MonedaSecundaria;
            objetoFormaPagoIn.IdMonedaLiquidacion = MonedaSecundaria;
            objetoFormaPagoIn.MontoFormaDePago = objetoOperacionIn.MontoMonedaSecundaria;
            
            if(OperacionSpotDetalle.FormaPagoRecibimos != null && OperacionSpotDetalle.FormaPagoRecibimos.contains("_CIN_")){
            	objetoFormaPagoIn.AfectarTesoreria = "N";
            }else {
            	objetoFormaPagoIn.AfectarTesoreria = "S";            	
            }

            if("VENSPT".equals(OperacionSpotDetalle.TipoOperacion)){
            	objetoFormaPagoIn.IdCuentaBancaria = formasPagoMesa.stream().filter(fpm -> fpm.IdFormaDePago.equals(OperacionSpotDetalle.FormaPagoRecibimos)).findFirst().orElse(new FormasDePagoMesa()).IdCuentaBancaria;
            }
            else {
            	objetoFormaPagoIn.IdCuentaBancaria = formasDePagoCliente.stream().filter(fpc -> (fpc.IdFormaDePago.equals(OperacionSpotDetalle.FormaPagoRecibimos)&&(fpc.IdPortafolio.equals(OperacionSpotDetalle.IdPortafolio)))).findFirst().orElse(new FormasDePagoCliente()).IdCuentaBancaria;
            }
            crearOperacionSpotEntrada.p_DatosFormaPago.add(objetoFormaPagoIn) ;
            p_DatosFormaPago.add(objetoFormaPagoIn);
            
		}
		
		
		CrearOperacionSpot crearOperacionSpot = new CrearOperacionSpot();
		CrearOperacionSpotSalida CrearOperacionSpotSalida = crearOperacionSpot.logica(con, crearOperacionSpotEntrada);

		logger.info("Fin Creacion operaciones Spot + salida :" + CrearOperacionSpotSalida);
		logger.trace("FIN::ProcesarCargaMasivaArchivoOperacionSpot.crearOperaciones");
		return CrearOperacionSpotSalida;
	}
	

	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getQAConnection()) {
//		try (Connection con = PlataformaUtils.getMASTER01Connection()) {
//			try (Connection con = PlataformaUtils.getDESAConnection()) {
		Gson gson = Utils.getGsonBuilder();			
		
		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"MSP\",\"p_CargaMasivaProcesar\":[{\"IdCargaMasiva\":132}]}";
		EntradaProcesarCargaMasivaArchivoOperacionSpot Objin = gson.fromJson(json, EntradaProcesarCargaMasivaArchivoOperacionSpot.class);
		ProcesarCargaMasivaArchivoOperacionSpot demo = new ProcesarCargaMasivaArchivoOperacionSpot();				
		try {
			SalidaProcesarCargaMasivaArchivoOperacionSpot ObjOut = demo.logica(con, Objin);
//			con.commit();
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
