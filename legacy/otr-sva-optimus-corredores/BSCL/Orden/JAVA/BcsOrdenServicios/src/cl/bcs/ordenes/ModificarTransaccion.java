package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.CrearTransaccionesHistoricas.EntradaCrearTransaccionesHistoricas;
import cl.bcs.ordenes.CrearTransaccionesHistoricas.SalidaCrearTransaccionesHistoricas;
import cl.bcs.ordenes.ServicioCumpleRequisitosModificarTransaccion.EntradaServicioCumpleRequisitosModificarTransaccion;
import cl.bcs.ordenes.ServicioCumpleRequisitosModificarTransaccion.SalidaServicioCumpleRequisitosModificarTransaccion;
import cl.bcs.ordenes.pojo.Transaccion;
import cl.bcs.ordenes.NotificarCambioTransaccion;
import cl.bcs.ordenes.NotificarCambioTransaccion.NotificarCambioTransaccion_Salida;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.ObtenerPlazo;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.ObtenerPlazo.FechasIn;
import cl.bcs.plataforma.pla.ObtenerPlazo.FechasOut;
import cl.bcs.plataforma.pla.ObtenerPlazo.OutObtenerPlazo;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarTransaccion implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ModificarTransaccion.class);
	
	private final String SQL_SELECT_TRANSACCIONES = loadResourceToString("cl/bcs/ordenes/sql/ModificarTransaccion_SelectTransacciones.sql");
	private final String SQL_UPDATE_TRANSACCIONES = loadResourceToString("cl/bcs/ordenes/sql/ModificarTransaccion_UpdateTransacciones.sql");
	private final String SQL_UPDATE_TRANSACCIONES_PLAZOS = loadResourceToString("cl/bcs/ordenes/sql/ModificarTransaccion_UpdateTransaccionesPlazos.sql");
	private final String SQL_SELECT_TRANSACCIONES_MODIFICADAS = loadResourceToString("cl/bcs/ordenes/sql/ModificarTransaccion_SelectTransaccionesModificadas.sql");
	private final String SQL_SELECT_TRANSACCION_ORIGINAL = loadResourceToString("cl/bcs/ordenes/sql/ModificarTransaccion_SelectTransaccionOriginal.sql");
	private final String SQL_SELECT_DATOS_LOG = loadResourceToString("cl/bcs/ordenes/sql/ModificarTransaccion_SelectDatosLog.sql");
	private final String SQL_SELECT_IDCONDICIONLIQUIDACION = loadResourceToString("cl/bcs/ordenes/sql/ModificarTransaccion_SelectIdCondicionDeLiquidacion.sql");
	
	private final String ORIGEN_MARKET_DATA = "MARKET_DATA";
	
	public static class EntradaModificarTransaccion{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_Reproceso;
		public String p_OrigenTransaccion;
		public List<InTransaccion> p_Transacciones;
		
		public EntradaModificarTransaccion() {
			this.p_Reproceso = "N";
			this.p_OrigenTransaccion = "";
			this.p_Transacciones = new ArrayList<InTransaccion>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InTransaccion{
		public Long IdTransaccion;
	    public BigDecimal Monto;
	    public BigDecimal Precio;
	    public BigDecimal Tasa;
	    public Long IdLibroOrdenes;
	    public BigDecimal ValorRescate;
	    public BigDecimal Cantidad;
	    public String FechaEmision;
	    public String EsBilateral;
	    public String BloqueoLiquidacion;
	    public String FechaLiquidacion;
	    public BigDecimal MontoRetencion;
	    public String AccionTipoOperacion;
		public BigDecimal TipoCambioCorresponsal;
		public Long IdCorredorCorresponsal;
		public BigDecimal MontoLiquidacionCorresponsal;
		public BigDecimal GastosCorresponsal;
		public String IdMonedaLiquidacionCorresponsal;
	    public String IdCamara;
		public String Motivo;
		public String Observaciones;
		public String FechaModificacion;
		public String MarcaNegociacion;
		public Long Plazo;
		public BigDecimal MontoAPlazo;
		public BigDecimal PrecioAPlazo;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public String FechaVencimiento;
		public String IdCondicionDeLiquidacion;
		
		public InTransaccion() {
			this.Motivo = "";
			this.Observaciones = "";
			this.FechaModificacion = "";
			this.MarcaNegociacion = "";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaModificarTransaccion{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaModificarTransaccion() {
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class TransaccionExistente{
		public Long IdTransaccion;
		public String IdEstado;
		public Long IdTransaccionPlazo;
		public String FechaMovimiento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CamposModificados extends BcsPojo{
        public Long IdTransaccion;
        public String NombreCampo;
        public String ValorAntiguo;
        public String ValorNuevo;    
	}
	
	public static class ObjTransaccion extends BcsPojo{
		public Long IdTransaccion;
		public String FolioTransaccion; 
		public String Instrumento; 
		public String IdTipoOperacion;
		public String IdMonedaTransaccion;
		public BigDecimal MontoTransaccion;
		public BigDecimal PrecioTransaccion;
		public BigDecimal CantidadTransaccion;
		public String IdEstadoTransaccion;
	}
	
	public static class IdCondicionDeLiquidacion extends BcsPojo{
		public String	IdCondicionDeLiquidacion;
		public Long		PlazoEnDias;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaModificarTransaccion jin = gson.fromJson(in.toString(), EntradaModificarTransaccion.class);
		SalidaModificarTransaccion out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaModificarTransaccion logica(Connection con, EntradaModificarTransaccion entrada) throws Exception {
		logger.info("INICIO::ModificarTransaccion");
		SalidaModificarTransaccion out = new SalidaModificarTransaccion();
		
		ConsultarEmpresa serviceConsultarEmpresa = new ConsultarEmpresa();
		InformacionEmpresa empresa = serviceConsultarEmpresa.logica(con, entrada.p_IdEmpresa);
		String fechaSistema = obtenerFechaSistema(con,entrada.p_IdEmpresa);
		
		RevisarRequisitosBasicosSalida salidaBasicos = validaRequisitosMinimos(con, entrada);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			return out;
		}
		
		Map<Long,TransaccionExistente> hashTransacciones = obtenerTransacciones(con, entrada);
		
		validarFechaModificacion(con, hashTransacciones, entrada, fechaSistema, out);
		
		if(out.P_OUT_ERRORESCONTADOR > 0L) {
			return out;
		}
		
		SalidaServicioCumpleRequisitosModificarTransaccion salidaServicioCumpleRequisitosModificarTransaccion = validaModificarTransaccion(con, entrada, hashTransacciones);
		
		out.P_OUT_ERRORESCONTADOR += salidaServicioCumpleRequisitosModificarTransaccion.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES.addAll(salidaServicioCumpleRequisitosModificarTransaccion.P_OUT_MENSAJES);
		
		ObtenerPlazo.InObtenerPlazo entradaObtenerPlazoFechaLiquidacion = new ObtenerPlazo.InObtenerPlazo();
		
		if(!ORIGEN_MARKET_DATA.equalsIgnoreCase(entrada.p_OrigenTransaccion)) {
			prepararCalculoCondicionLiquidacion(entradaObtenerPlazoFechaLiquidacion, entrada.p_Transacciones, hashTransacciones, empresa.IdPais);
		}
		
		if(entradaObtenerPlazoFechaLiquidacion.p_Fechas.size() > 0) {
			definirIdCondicionDeLiquidacion(con, entradaObtenerPlazoFechaLiquidacion, entrada, out);
		}
		
		List<InTransaccion> transaccionesPlazos = obtenerTransaccionPlazo(entrada.p_Transacciones, hashTransacciones);
		
		if(transaccionesPlazos.size() > 0 && !ORIGEN_MARKET_DATA.equalsIgnoreCase(entrada.p_OrigenTransaccion)) {
			definirPlazoSIM(con, entrada, hashTransacciones, empresa.IdPais);
		}
		
		if("N".equals(entrada.p_Reproceso)) {
			String estado = out.P_OUT_ERRORESCONTADOR.equals(0L) ? "PRO" : "PEN";
			//Si hay errores al modificar transacciones de MarketData no se crea la transaccion historica
			if(!(ORIGEN_MARKET_DATA.equalsIgnoreCase(entrada.p_OrigenTransaccion) && "PEN".equalsIgnoreCase(estado))) {
				SalidaCrearTransaccionesHistoricas salidaTransaccionHistorica = creacionTransaccionHistorica(con,entrada,transaccionesPlazos,hashTransacciones,estado);
				
				out.P_OUT_ERRORESCONTADOR += salidaTransaccionHistorica.P_OUT_ERRORESCONTADOR;
				out.P_OUT_MENSAJES.addAll(salidaTransaccionHistorica.P_OUT_MENSAJES);
			}
			
		}
		
		if(out.P_OUT_ERRORESCONTADOR.equals(0L)) {
			List<InTransaccion> transaccionesModificar = obtenerTransaccionesCorrectas(entrada, out.P_OUT_MENSAJES);
			List<InTransaccion> transaccionesPlazosModificar = obtenerTransaccionPlazo(entrada.p_Transacciones, hashTransacciones);
			SQL sql=new SQL();
            List<Long> transacciones = entrada.p_Transacciones.stream().map(tran -> {return tran.IdTransaccion;}).distinct().collect(Collectors.toList());
            HashMap<String, Object> params = new HashMap<>();
            params.put("IdTransaccion", transacciones);
            System.out.println(params);
            List<InTransaccion> transaccionOriginal = sql.query(con, SQL_SELECT_TRANSACCION_ORIGINAL, params, InTransaccion.class).parallelStream().collect(Collectors.toList());
            actualizarTransaccion(con,transaccionesModificar);
			actualizarTransaccionPlazo(con,transaccionesPlazosModificar);
	        String notificar = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.NotificarCambioTransaccion");
	        
	        for(InTransaccion transaccion : transaccionesModificar) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(transaccion.IdTransaccion,MensajeSalida.MENSAJE_OK,"TRANSACCION_MODIFICADA",new HashMap<>(),con,entrada.p_IdEmpresa));
			}
	        
	        if (notificar.equals("S")) {notificarModificacion(con, entrada);}
	        
            List<CamposModificados> camposModificados = camposModificados(transaccionOriginal, transaccionesModificar);                        
            
			//log
            if(!camposModificados.isEmpty() && entrada.p_Transacciones != null && !entrada.p_Transacciones.isEmpty()){

				List<Long> lstTransacciones = entrada.p_Transacciones.stream().map(procesamiento ->{
					return procesamiento.IdTransaccion;
				}).distinct().collect(Collectors.toList());
			
				List<ObjTransaccion> listaTransaccionesLog = new ArrayList();
				HashMap<Long,Object> hashTransaccionesLog = new HashMap();
			
				if (!lstTransacciones.isEmpty()) {
					HashMap<String,Object> parmsIn2 = new HashMap<String,Object>();
					parmsIn2.put("IdTransaccion",lstTransacciones);
					listaTransaccionesLog = sql.query(con, SQL_SELECT_DATOS_LOG, parmsIn2, ObjTransaccion.class);	
					hashTransaccionesLog = Utils.List2Hash(listaTransaccionesLog, "IdTransaccion");		
				}	

	            AgregarLog agregarLog = new AgregarLog();
	            InAgregarLog entradaLog = new InAgregarLog();
	            entradaLog.p_IdEmpresa = entrada.p_IdEmpresa;
	            entradaLog.p_IdModulo = entrada.p_IdModulo;
	            entradaLog.p_IdUsuario = entrada.p_IdUsuario;
            
                for(InTransaccion log : entrada.p_Transacciones) {
					ObjTransaccion tranLog = new ObjTransaccion();
					if(hashTransaccionesLog.size()>0) {
						tranLog = (ObjTransaccion) hashTransaccionesLog.get(log.IdTransaccion);
					}	

	                InAgregarLog.Log headLog = new InAgregarLog.Log();
	                headLog.IdEmpresa = entrada.p_IdEmpresa;
	                headLog.IdModulo = entrada.p_IdModulo;
	                headLog.IdUsuario = entrada.p_IdUsuario;
	                headLog.IdTipoLog = "INFORMATIVO";
					headLog.NombreProceso = "Modificar Transaccion";	
	                headLog.IdCabeceraLog = log.IdTransaccion != null ? log.IdTransaccion : 0L;
	                headLog.FechaSistemaLog = fechaSistema;
	                headLog.IdRegistro = log.IdTransaccion != null ? log.IdTransaccion.toString() : "";
	                headLog.IdTipoRegistro = "MODIFICACION_TRANSACCION";
	                headLog.Folio = (tranLog != null && tranLog.FolioTransaccion != null && !tranLog.FolioTransaccion.isEmpty()) ? Long.parseLong(tranLog.FolioTransaccion) : 0L;
	                headLog.IdTipoOperacion = (tranLog != null && tranLog.IdTipoOperacion != null) ? tranLog.IdTipoOperacion : "";
	                headLog.EstadoRegistro = (tranLog != null && tranLog.IdEstadoTransaccion != null) ? tranLog.IdEstadoTransaccion : "";
	                headLog.Nemo = (tranLog != null && tranLog.Instrumento != null) ? tranLog.Instrumento : "";
	                headLog.IdMonedaOrigen = (tranLog != null && tranLog.IdMonedaTransaccion != null) ? tranLog.IdMonedaTransaccion : "";
					headLog.Monto = (tranLog != null && tranLog.MontoTransaccion != null) ? tranLog.MontoTransaccion : BigDecimal.ZERO;
	                headLog.PrecioTasa = (tranLog != null && tranLog.PrecioTransaccion != null) ? tranLog.PrecioTransaccion : BigDecimal.ZERO;
	                headLog.Cantidad = (tranLog != null && tranLog.CantidadTransaccion != null) ? tranLog.CantidadTransaccion : BigDecimal.ZERO;
	                
	                if(log.Monto != null) headLog.Monto = log.Monto;
	                if(log.Precio != null) headLog.PrecioTasa = log.Precio;
	                if(log.Cantidad != null) headLog.Cantidad = log.Cantidad;
	                
	                entradaLog.p_Log.add(headLog);
	                
	                List<CamposModificados> transaccion = camposModificados.stream().filter(tran -> tran.IdTransaccion.compareTo(log.IdTransaccion) == 0L).collect(Collectors.toList());
	                
	                if(!transaccion.isEmpty()) {	                    
	                    InAgregarLog.DetalleLog detLogTransaccion = new InAgregarLog.DetalleLog();
	                    detLogTransaccion.IdCabeceraLog = log.IdTransaccion;
	                    detLogTransaccion.SubRegistro = "INFORMACION";
	                    detLogTransaccion.CampoRegistro = "IdTransaccion: " +  log.IdTransaccion;
	                    entradaLog.p_LogDetalle.add(detLogTransaccion);
	                    
	                    for(CamposModificados detalleLog : transaccion) {
	                            InAgregarLog.DetalleLog detLog = new InAgregarLog.DetalleLog();
	                            detLog.IdCabeceraLog = log.IdTransaccion;
	                            detLog.SubRegistro = "MODIFICACION";
	                            detLog.CampoRegistro = detalleLog.NombreCampo;
	                            detLog.ValorAntiguo = detalleLog.ValorAntiguo;
	                            detLog.ValorNuevo = detalleLog.ValorNuevo;
	                            entradaLog.p_LogDetalle.add(detLog);
	                    }
	                }
                }
                agregarLog.logica(con, entradaLog);
            }
		}
		logger.info("FIN::ModificarTransaccion");
		return out;
	}
	
	private RevisarRequisitosBasicosSalida validaRequisitosMinimos(Connection con, EntradaModificarTransaccion entrada) throws Exception{
		logger.info("INICIO::ModificarTransaccion.validaRequisitosMinimos");
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salida = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
		logger.info("FIN::ModificarTransaccion.validaRequisitosMinimos");
		return salida;
	}
	
	private NotificarCambioTransaccion_Salida notificarModificacion (Connection con, EntradaModificarTransaccion entrada) throws Exception {
		logger.info("INICIO::ModificarTransaccion.notificarModificacion");
		
		NotificarCambioTransaccion.EntradaModificarTransaccion notificar = new NotificarCambioTransaccion.EntradaModificarTransaccion();
		notificar.p_IdEmpresa 		= entrada.p_IdEmpresa;
		notificar.p_IdUsuario		= entrada.p_IdUsuario;
		notificar.p_IdModulo		= entrada.p_IdModulo;
		notificar.p_Accion			= "MODIFICADA";
		notificar.p_InTransacciones = new ArrayList<>();
		
		for (InTransaccion inTran : entrada.p_Transacciones) {
			NotificarCambioTransaccion.InTransaccion p_tran = new NotificarCambioTransaccion.InTransaccion();
			
			p_tran.IdTransaccion = inTran.IdTransaccion;
			
			notificar.p_InTransacciones.add(p_tran);			
		}		
		
		NotificarCambioTransaccion notif = new NotificarCambioTransaccion();
		NotificarCambioTransaccion.NotificarCambioTransaccion_Salida notifOut = notif.logica(con, notificar);
		
		logger.info("FIN::ModificarTransaccion.notificarModificacion"); 
		return notifOut;
	}
	
	private ServicioCumpleRequisitosModificarTransaccion.InTransaccion copiarTransaccionRequisitos(InTransaccion transaccion, Map<Long,TransaccionExistente> hashTransacciones){
		logger.info("INICIO::ModificarTransaccion.copiarTransaccionRequisitos");
		TransaccionExistente transaccionExistente = hashTransacciones.get(transaccion.IdTransaccion);
		ServicioCumpleRequisitosModificarTransaccion.InTransaccion transaccionServicio = new ServicioCumpleRequisitosModificarTransaccion.InTransaccion();
		transaccionServicio.IdInterno = transaccion.IdTransaccion;
		transaccionServicio.IdTransaccion = transaccion.IdTransaccion;
		transaccionServicio.IdEstado = transaccionExistente == null ? "" : transaccionExistente.IdEstado;
		transaccionServicio.Cantidad = transaccion.Cantidad;
		transaccionServicio.Monto = transaccion.Monto;
		transaccionServicio.Precio = transaccion.Precio;
		transaccionServicio.FechaEmision = transaccion.FechaEmision;
		transaccionServicio.Tasa = transaccion.Tasa;
		transaccionServicio.ValorRescate = transaccion.ValorRescate;
		transaccionServicio.AccionTipoOperacion = transaccion.AccionTipoOperacion;
		transaccionServicio.TipoCambioCorresponsal = transaccion.TipoCambioCorresponsal;
		transaccionServicio.IdCorredorCorresponsal = transaccion.IdCorredorCorresponsal;
		transaccionServicio.MontoLiquidacionCorresponsal = transaccion.MontoLiquidacionCorresponsal;
		transaccionServicio.GastosCorresponsal = transaccion.GastosCorresponsal;
		transaccionServicio.IdMonedaLiquidacionCorresponsal = transaccion.IdMonedaLiquidacionCorresponsal;
		

		transaccionServicio.EsBilateral = transaccion.EsBilateral;
		
		transaccionServicio.IdCamara = transaccion.IdCamara;
		
		
		logger.info("FIN::ModificarTransaccion.copiarTransaccionRequisitos");
		return transaccionServicio;
	}
	
	private Map<Long,TransaccionExistente> obtenerTransacciones(Connection con, EntradaModificarTransaccion entrada) throws Exception{
		logger.info("INICIO::ModificarTransaccion.obtenerTransacciones");
		SQL sql=new SQL();
		List<Long> idTransacciones = entrada.p_Transacciones.stream().map(transaccion->transaccion.IdTransaccion).collect(Collectors.toList());
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdTransaccion", idTransacciones);
		Map<Long,TransaccionExistente> salida = sql.query(con, SQL_SELECT_TRANSACCIONES, params, TransaccionExistente.class).stream().collect(Collectors.toMap(transaccion->transaccion.IdTransaccion, transaccion->transaccion));
		logger.info("FIN::ModificarTransaccion.obtenerTransacciones");
		return salida;
	}
	
	private SalidaServicioCumpleRequisitosModificarTransaccion validaModificarTransaccion(Connection con, EntradaModificarTransaccion entrada, Map<Long,TransaccionExistente> hashTransacciones) throws Exception{
		logger.info("INICIO::ModificarTransaccion.validaModificarTransaccion");
		ServicioCumpleRequisitosModificarTransaccion servicioCumpleRequisitosModificarTransaccion = new ServicioCumpleRequisitosModificarTransaccion();
		EntradaServicioCumpleRequisitosModificarTransaccion entradaServicioCumpleRequisitosModificarTransaccion = new EntradaServicioCumpleRequisitosModificarTransaccion();
		entradaServicioCumpleRequisitosModificarTransaccion.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaServicioCumpleRequisitosModificarTransaccion.p_Transacciones = entrada.p_Transacciones.stream().map(transaccion -> copiarTransaccionRequisitos(transaccion,hashTransacciones)).collect(Collectors.toList());
		SalidaServicioCumpleRequisitosModificarTransaccion salida = servicioCumpleRequisitosModificarTransaccion.logica(con, entradaServicioCumpleRequisitosModificarTransaccion);
		logger.info("FIN::ModificarTransaccion.validaModificarTransaccion");
		return salida;
	}
	
	private List<InTransaccion> obtenerTransaccionesCorrectas(EntradaModificarTransaccion entrada, List<MensajeSalida> mensajes){
		logger.info("INICIO::ModificarTransaccion.obtenerTransaccionesCorrectas");
		List<InTransaccion> salida = entrada.p_Transacciones.stream()
				.filter(transaccion-> mensajes.stream().filter(mensaje-> mensaje.id.equals(transaccion.IdTransaccion) && MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)).count() == 0)
				.collect(Collectors.toList());
		logger.info("FIN::ModificarTransaccion.obtenerTransaccionesCorrectas");
		return salida;
	}
	
	private List<InTransaccion> obtenerTransaccionPlazo(List<InTransaccion> transaccionesModificar, Map<Long,TransaccionExistente> hashTransacciones){
		logger.info("INICIO::ModificarTransaccion.obtenerTransaccionPlazo");
		List<InTransaccion> salida = transaccionesModificar.stream().filter(transaccion->{
			TransaccionExistente transaccionExistente = hashTransacciones.get(transaccion.IdTransaccion);
			return tieneTransaccionPlazo(transaccionExistente);
		}).map(transaccion->{
			TransaccionExistente transaccionExistente = hashTransacciones.get(transaccion.IdTransaccion);
			InTransaccion transaccionPlazo = new InTransaccion();
			transaccionPlazo.IdTransaccion = transaccionExistente.IdTransaccionPlazo;
			copiarTransacionATransaccionPlazo(transaccion,transaccionPlazo);
			return transaccionPlazo;
		}).collect(Collectors.toList());
		logger.info("FIN::ModificarTransaccion.obtenerTransaccionPlazo");
		return salida;
	}
	
	private void copiarTransacionATransaccionPlazo(InTransaccion transaccion, InTransaccion transaccionPlazo) {
		logger.info("INICIO::ModificarTransaccion.copiarTransacionATransaccionPlazo");
		transaccionPlazo.Monto = "SIM".equalsIgnoreCase(transaccion.IdTipoInstrumento) ? transaccion.MontoAPlazo : transaccion.Monto;
		transaccionPlazo.Precio = "SIM".equalsIgnoreCase(transaccion.IdTipoInstrumento) ? transaccion.PrecioAPlazo : transaccion.Precio;
		transaccionPlazo.Tasa = transaccion.Tasa;
		transaccionPlazo.IdLibroOrdenes = transaccion.IdLibroOrdenes;
		transaccionPlazo.ValorRescate = transaccion.ValorRescate;
		transaccionPlazo.Cantidad = transaccion.Cantidad;
		transaccionPlazo.FechaEmision = transaccion.FechaEmision;
		transaccionPlazo.EsBilateral = transaccion.EsBilateral;
		transaccionPlazo.BloqueoLiquidacion = transaccion.BloqueoLiquidacion;
		transaccionPlazo.FechaLiquidacion = "SIM".equalsIgnoreCase(transaccion.IdTipoInstrumento) ? transaccion.FechaVencimiento : transaccion.FechaLiquidacion;
		transaccionPlazo.MontoRetencion = transaccion.MontoRetencion;
		transaccionPlazo.AccionTipoOperacion = transaccion.AccionTipoOperacion;
		transaccionPlazo.TipoCambioCorresponsal = transaccion.TipoCambioCorresponsal;
		transaccionPlazo.IdCorredorCorresponsal = transaccion.IdCorredorCorresponsal;
		transaccionPlazo.MontoLiquidacionCorresponsal = transaccion.MontoLiquidacionCorresponsal;
		transaccionPlazo.GastosCorresponsal = transaccion.GastosCorresponsal;
		transaccionPlazo.IdMonedaLiquidacionCorresponsal = transaccion.IdMonedaLiquidacionCorresponsal;
		transaccionPlazo.IdCamara = transaccion.IdCamara;
		transaccionPlazo.Motivo = transaccion.Motivo;
		transaccionPlazo.Observaciones = transaccion.Observaciones;
		transaccionPlazo.FechaModificacion = transaccion.FechaModificacion;
		transaccionPlazo.MarcaNegociacion = transaccion.MarcaNegociacion;
		logger.info("FIN::ModificarTransaccion.copiarTransacionATransaccionPlazo");
	}
	
	private void actualizarTransaccion(Connection con, List<InTransaccion> transaccionesModificar) throws Exception {
		logger.info("INICIO::ModificarTransaccion.actualizarTransaccion");
		SQL sql=new SQL();
		sql.batch(con, SQL_UPDATE_TRANSACCIONES, transaccionesModificar, InTransaccion.class);
		logger.info("FIN::ModificarTransaccion.actualizarTransaccion");
	}
	
	private void actualizarTransaccionPlazo(Connection con, List<InTransaccion> transaccionesPlazosModificar) throws Exception {
		logger.info("INICIO::ModificarTransaccion.actualizarTransaccionPlazo");
		SQL sql=new SQL();
		sql.batch(con, SQL_UPDATE_TRANSACCIONES_PLAZOS, transaccionesPlazosModificar, InTransaccion.class);
		logger.info("FIN::ModificarTransaccion.actualizarTransaccionPlazo");
	}
	
	private SalidaCrearTransaccionesHistoricas creacionTransaccionHistorica(Connection con, EntradaModificarTransaccion entrada, List<InTransaccion> transaccionesPlazosModificar, Map<Long,TransaccionExistente> hashTransacciones, String estado) throws Exception {
		logger.info("INICIO::ModificarTransaccion.creacionTransaccionHistorica");
		
		List<Long> idTransacciones = entrada.p_Transacciones.stream().map(transaccion->transaccion.IdTransaccion).collect(Collectors.toList());
		List<Long> idTransaccionesPlazos = transaccionesPlazosModificar.stream().map(transaccion->transaccion.IdTransaccion).collect(Collectors.toList());
		
		Map<Long, Transaccion> transaccionesActuales = obtenerTransaccionesActuales(con,idTransacciones);
		Map<Long, Transaccion> transaccionesPlazosActuales = obtenerTransaccionesActuales(con,idTransaccionesPlazos);
		
		List<Transaccion> transaccionesFinales = entrada.p_Transacciones.stream()
		.filter(transaccion->hashTransacciones.get(transaccion.IdTransaccion) != null && transaccionesActuales.get(transaccion.IdTransaccion) != null)
		.map(transaccionEntrada->{
			Transaccion transaccionActual = transaccionesActuales.get(transaccionEntrada.IdTransaccion);
			copiarDatosActualizacionTransaccion(transaccionActual,transaccionEntrada);
			return transaccionActual;
		}).collect(Collectors.toList());
		
		List<Transaccion> transaccionesPlazosFinales = entrada.p_Transacciones.stream().filter(transaccion->{
			TransaccionExistente transaccionExistente = hashTransacciones.get(transaccion.IdTransaccion);
			return tieneTransaccionPlazo(transaccionExistente) && transaccionesPlazosActuales.get(transaccionExistente.IdTransaccionPlazo) != null;
		}).map(transaccionEntrada->{
			TransaccionExistente transaccionExistente = hashTransacciones.get(transaccionEntrada.IdTransaccion);
			Transaccion transaccionActual = transaccionesPlazosActuales.get(transaccionExistente.IdTransaccionPlazo);
			copiarDatosActualizacionTransaccionPlazo(transaccionActual,transaccionEntrada);
			return transaccionActual;
		}).collect(Collectors.toList());
		
		transaccionesFinales.addAll(transaccionesPlazosFinales);
		
		CrearTransaccionesHistoricas crearTransaccionesHistoricas = new CrearTransaccionesHistoricas();
		EntradaCrearTransaccionesHistoricas entradaCrearTransaccionesHistoricas = new EntradaCrearTransaccionesHistoricas();
		entradaCrearTransaccionesHistoricas.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearTransaccionesHistoricas.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearTransaccionesHistoricas.p_IdModulo = entrada.p_IdModulo;
		entradaCrearTransaccionesHistoricas.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaCrearTransaccionesHistoricas.p_Transacciones = transaccionesFinales.stream().map(transaccion->{
			CrearTransaccionesHistoricas.InTransaccionHistorica transaccionHistorica = new CrearTransaccionesHistoricas.InTransaccionHistorica();
			transaccionHistorica.TipoRegistro = "MOD";
			transaccionHistorica.IdEstadoRegistro = estado;
			transaccionHistorica.Transaccion = transaccion;
			return transaccionHistorica;
		}).collect(Collectors.toList());
		
		SalidaCrearTransaccionesHistoricas salidaTransaccionHistorica = crearTransaccionesHistoricas.logica(con, entradaCrearTransaccionesHistoricas);
		logger.info("FIN::ModificarTransaccion.creacionTransaccionHistorica");
		return salidaTransaccionHistorica;
	}
	
	private boolean tieneTransaccionPlazo(TransaccionExistente transaccionExistente) {
		logger.info("INICIO::ModificarTransaccion.tieneTransaccionPlazo");
		boolean esPlazo = transaccionExistente != null && transaccionExistente.IdTransaccionPlazo != null && transaccionExistente.IdTransaccionPlazo > 0;
		logger.info("FIN::ModificarTransaccion.tieneTransaccionPlazo");
		return esPlazo;
	}
	
	private Map<Long, Transaccion> obtenerTransaccionesActuales(Connection con, List<Long> idTransacciones) throws Exception{
		logger.info("INICIO::ModificarTransaccion.obtenerTransaccionesActuales");
		SQL sql=new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdTransaccion", idTransacciones);
		Map<Long, Transaccion> transacciones = sql.query(con, SQL_SELECT_TRANSACCIONES_MODIFICADAS, params, Transaccion.class).stream().collect(Collectors.toMap(transaccion->transaccion.IdTransaccion, transaccion->transaccion));
		logger.info("FIN::ModificarTransaccion.obtenerTransaccionesActuales");
		return transacciones;
	}
	
	private void copiarDatosActualizacionTransaccion(Transaccion transaccionActual, InTransaccion transaccionEntrada) {
		logger.info("INICIO::ModificarTransaccion.copiarDatosActualizacionTransaccion");
		transaccionActual.Precio = transaccionEntrada.Precio != null ? transaccionEntrada.Precio : transaccionActual.Precio;
		transaccionActual.FechaEmision = transaccionEntrada.FechaEmision != null ? Fecha.toDate(transaccionEntrada.FechaEmision) : transaccionActual.FechaEmision;
		transaccionActual.Cantidad = transaccionEntrada.Cantidad != null ? transaccionEntrada.Cantidad : transaccionActual.Cantidad;
		transaccionActual.IdLibroOrdenes = transaccionEntrada.IdLibroOrdenes != null ? transaccionEntrada.IdLibroOrdenes : transaccionActual.IdLibroOrdenes;
		transaccionActual.TipoCambioCorresponsal = transaccionEntrada.TipoCambioCorresponsal != null ? transaccionEntrada.TipoCambioCorresponsal : transaccionActual.TipoCambioCorresponsal;
		transaccionActual.IdCorredoraCorresponsal = transaccionEntrada.IdCorredorCorresponsal != null ? transaccionEntrada.IdCorredorCorresponsal : transaccionActual.IdCorredoraCorresponsal;
		transaccionActual.MontoLiquidacionCorresponsal = transaccionEntrada.MontoLiquidacionCorresponsal != null ? transaccionEntrada.MontoLiquidacionCorresponsal : transaccionActual.MontoLiquidacionCorresponsal;
		transaccionActual.GastosCorresponsal = transaccionEntrada.GastosCorresponsal != null ? transaccionEntrada.GastosCorresponsal : transaccionActual.GastosCorresponsal;
		transaccionActual.IdMonedaLiquidacionCorresponsal = transaccionEntrada.IdMonedaLiquidacionCorresponsal != null ? transaccionEntrada.IdMonedaLiquidacionCorresponsal : transaccionActual.IdMonedaLiquidacionCorresponsal;
		transaccionActual.MarcaNegociacion = transaccionEntrada.MarcaNegociacion != null ? transaccionEntrada.MarcaNegociacion : transaccionActual.MarcaNegociacion;
		transaccionActual.Tasa = transaccionEntrada.Tasa != null ? transaccionEntrada.Tasa : transaccionActual.Tasa;
		transaccionActual.EsBilateral = transaccionEntrada.EsBilateral != null ? transaccionEntrada.EsBilateral : transaccionActual.EsBilateral;
		transaccionActual.BloqueoLiquidacion = transaccionEntrada.BloqueoLiquidacion != null ? transaccionEntrada.BloqueoLiquidacion : transaccionActual.BloqueoLiquidacion;
		transaccionActual.FechaLiquidacion = transaccionEntrada.FechaLiquidacion != null ? Fecha.toDate(transaccionEntrada.FechaLiquidacion) : transaccionActual.FechaLiquidacion;
		transaccionActual.MontoRetencion = transaccionEntrada.MontoRetencion != null ? transaccionEntrada.MontoRetencion : transaccionActual.MontoRetencion;
		transaccionActual.IdCamara = transaccionEntrada.IdCamara != null ? transaccionEntrada.IdCamara : transaccionActual.IdCamara;
		BigDecimal monto =  transaccionEntrada.Monto != null ? transaccionEntrada.Monto : transaccionActual.Monto;
		transaccionActual.Monto = "RV".equals(transaccionActual.IdClasificacionTipoInstrumento) ? transaccionActual.Precio.multiply(transaccionActual.Cantidad, MathContext.DECIMAL128) : monto;
		transaccionActual.IdCondicionDeLiquidacion = transaccionEntrada.IdCondicionDeLiquidacion != null ? transaccionEntrada.IdCondicionDeLiquidacion : transaccionActual.IdCondicionDeLiquidacion;
		transaccionActual.Plazo = transaccionEntrada.Plazo != null ? transaccionEntrada.Plazo : transaccionActual.Plazo;
		logger.info("FIN::ModificarTransaccion.copiarDatosActualizacionTransaccion");
	}
	
	private void copiarDatosActualizacionTransaccionPlazo(Transaccion transaccionActual, InTransaccion transaccionEntrada) {
		logger.info("INICIO::ModificarTransaccion.copiarDatosActualizacionTransaccionPlazo");
		transaccionActual.Precio = transaccionEntrada.Precio != null ? transaccionEntrada.Precio : transaccionActual.Precio;
		transaccionActual.Precio = "SIM".equalsIgnoreCase(transaccionEntrada.IdTipoInstrumento) && transaccionEntrada.PrecioAPlazo != null ? transaccionEntrada.PrecioAPlazo : transaccionActual.Precio;
		transaccionActual.FechaEmision = transaccionEntrada.FechaEmision != null ? Fecha.toDate(transaccionEntrada.FechaEmision) : transaccionActual.FechaEmision;
		transaccionActual.Cantidad = transaccionEntrada.Cantidad != null ? transaccionEntrada.Cantidad : transaccionActual.Cantidad;
		transaccionActual.IdLibroOrdenes = transaccionEntrada.IdLibroOrdenes != null ? transaccionEntrada.IdLibroOrdenes : transaccionActual.IdLibroOrdenes;
		transaccionActual.EsBilateral = transaccionEntrada.EsBilateral != null ? transaccionEntrada.EsBilateral : transaccionActual.EsBilateral;
		transaccionActual.BloqueoLiquidacion = transaccionEntrada.BloqueoLiquidacion != null ? transaccionEntrada.BloqueoLiquidacion : transaccionActual.BloqueoLiquidacion;
		transaccionActual.FechaLiquidacion = transaccionEntrada.FechaLiquidacion != null ? Fecha.toDate(transaccionEntrada.FechaLiquidacion) : transaccionActual.FechaLiquidacion;
		transaccionActual.FechaLiquidacion = "SIM".equalsIgnoreCase(transaccionEntrada.IdTipoInstrumento) && transaccionEntrada.FechaVencimiento != null ? Fecha.toDate(transaccionEntrada.FechaVencimiento) : transaccionActual.FechaLiquidacion;
		transaccionActual.MontoRetencion = transaccionEntrada.MontoRetencion != null ? transaccionEntrada.MontoRetencion : transaccionActual.MontoRetencion;
		transaccionActual.Tasa = transaccionEntrada.Tasa != null ? transaccionEntrada.Tasa : transaccionActual.Tasa;
		transaccionActual.TipoCambioCorresponsal = transaccionEntrada.TipoCambioCorresponsal != null ? transaccionEntrada.TipoCambioCorresponsal : transaccionActual.TipoCambioCorresponsal;
		transaccionActual.IdCorredoraCorresponsal = transaccionEntrada.IdCorredorCorresponsal != null ? transaccionEntrada.IdCorredorCorresponsal : transaccionActual.IdCorredoraCorresponsal;
		transaccionActual.IdMonedaLiquidacionCorresponsal = transaccionEntrada.IdMonedaLiquidacionCorresponsal != null ? transaccionEntrada.IdMonedaLiquidacionCorresponsal : transaccionActual.IdMonedaLiquidacionCorresponsal;
		transaccionActual.GastosCorresponsal = transaccionEntrada.GastosCorresponsal != null ? transaccionEntrada.GastosCorresponsal : transaccionActual.GastosCorresponsal;
		transaccionActual.IdMonedaLiquidacionCorresponsal = transaccionEntrada.IdMonedaLiquidacionCorresponsal != null ? transaccionEntrada.IdMonedaLiquidacionCorresponsal : transaccionActual.IdMonedaLiquidacionCorresponsal;
		transaccionActual.IdCamara = transaccionEntrada.IdCamara != null ? transaccionEntrada.IdCamara : transaccionActual.IdCamara;
		BigDecimal monto =  transaccionEntrada.Monto != null ? transaccionEntrada.Monto : transaccionActual.Monto;
		transaccionActual.Monto = "RV".equals(transaccionActual.IdClasificacionTipoInstrumento) ? transaccionActual.Precio.multiply(transaccionActual.Cantidad, MathContext.DECIMAL128) : monto;
		transaccionActual.Plazo = transaccionEntrada.Plazo != null ? transaccionEntrada.Plazo : transaccionActual.Plazo;
		logger.info("FIN::ModificarTransaccion.copiarDatosActualizacionTransaccionPlazo");
	}
	
	private String obtenerFechaSistema(Connection con, Long idEmpresa) throws Exception {
	    logger.trace("INICIO::CertificarSolicitudAporteRescate.obtenerFechaSistema");
	    String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
	    logger.trace("FIN::CertificarSolicitudAporteRescate.obtenerFechaSistema");
	    return fechaSistema;
	}

	private List<CamposModificados> camposModificados(List<InTransaccion> listAntes, List<InTransaccion> listDespues) throws Exception { 
	    List<CamposModificados> listaSalida = new ArrayList();
	    HashMap<Long,Object> hash = Utils.List2Hash(listDespues, "IdTransaccion");
	    for(InTransaccion antes: listAntes){
	        InTransaccion despues = (InTransaccion) hash.get(antes.IdTransaccion);
	        if(despues!= null){ 
                if(antes.Monto.compareTo(despues.Monto) != 0) {
	                CamposModificados aux = new CamposModificados();
	                aux.IdTransaccion = antes.IdTransaccion;
	                aux.NombreCampo = "Monto";
	                aux.ValorAntiguo = antes.Monto.toString();
	                aux.ValorNuevo = despues.Monto.toString();
	                listaSalida.add(aux);
                }
                if(!antes.EsBilateral.equalsIgnoreCase(despues.EsBilateral)){
	                CamposModificados aux = new CamposModificados();
	                aux.IdTransaccion = antes.IdTransaccion;
	                aux.NombreCampo = "EsBilateral";
	                aux.ValorAntiguo = antes.EsBilateral;
	                aux.ValorNuevo = despues.EsBilateral;
	                listaSalida.add(aux);
                }
                if(!antes.BloqueoLiquidacion.equalsIgnoreCase(despues.BloqueoLiquidacion)){
	                CamposModificados aux = new CamposModificados();
	                aux.IdTransaccion = antes.IdTransaccion;
	                aux.NombreCampo = "BloqueoLiquidacion";
	                aux.ValorAntiguo = antes.BloqueoLiquidacion;
	                aux.ValorNuevo = despues.BloqueoLiquidacion;
	                listaSalida.add(aux);
                }
                if(!antes.FechaLiquidacion.equalsIgnoreCase(despues.FechaLiquidacion)){
	                CamposModificados aux = new CamposModificados();
	                aux.IdTransaccion = antes.IdTransaccion;
	                aux.NombreCampo = "FechaLiquidacion";
	                aux.ValorAntiguo = antes.FechaLiquidacion;
	                aux.ValorNuevo = despues.FechaLiquidacion;
	                listaSalida.add(aux);
                }
                if(antes.MontoRetencion.compareTo(despues.MontoRetencion) != 0) {
	                CamposModificados aux = new CamposModificados();
	                aux.IdTransaccion = antes.IdTransaccion;
	                aux.NombreCampo = "MontoRetencion";
	                aux.ValorAntiguo = antes.MontoRetencion.toString();
	                aux.ValorNuevo = despues.MontoRetencion.toString();
	                listaSalida.add(aux);
                }
                if(!antes.AccionTipoOperacion.equalsIgnoreCase(despues.AccionTipoOperacion)){
	                CamposModificados aux = new CamposModificados();
	                aux.IdTransaccion = antes.IdTransaccion;
	                aux.NombreCampo = "AccionTipoOperacion";
	                aux.ValorAntiguo = antes.AccionTipoOperacion;
	                aux.ValorNuevo = despues.AccionTipoOperacion;
	                listaSalida.add(aux);
                }
                if(antes.TipoCambioCorresponsal.compareTo(despues.TipoCambioCorresponsal) != 0) {
	                CamposModificados aux = new CamposModificados();
	                aux.IdTransaccion = antes.IdTransaccion;
	                aux.NombreCampo = "TipoCambioCorresponsal";
	                aux.ValorAntiguo = antes.TipoCambioCorresponsal.toString();
	                aux.ValorNuevo = despues.TipoCambioCorresponsal.toString();
	                listaSalida.add(aux);
                }
                if(antes.IdCorredorCorresponsal.compareTo(despues.IdCorredorCorresponsal) != 0) {
	                CamposModificados aux = new CamposModificados();
	                aux.IdTransaccion = antes.IdTransaccion;
	                aux.NombreCampo = "IdCorredorCorresponsal";
	                aux.ValorAntiguo = antes.IdCorredorCorresponsal.toString();
	                aux.ValorNuevo = despues.IdCorredorCorresponsal.toString();
	                listaSalida.add(aux);
                }
                if(antes.MontoLiquidacionCorresponsal.compareTo(despues.MontoLiquidacionCorresponsal) != 0) {
	                CamposModificados aux = new CamposModificados();
	                aux.IdTransaccion = antes.IdTransaccion;
	                aux.NombreCampo = "MontoLiquidacionCorresponsal";
	                aux.ValorAntiguo = antes.MontoLiquidacionCorresponsal.toString();
	                aux.ValorNuevo = despues.MontoLiquidacionCorresponsal.toString();
	                listaSalida.add(aux);
                }
                if(antes.GastosCorresponsal.compareTo(despues.GastosCorresponsal) != 0) {
	                CamposModificados aux = new CamposModificados();
	                aux.IdTransaccion = antes.IdTransaccion;
	                aux.NombreCampo = "GastosCorresponsal";
	                aux.ValorAntiguo = antes.GastosCorresponsal.toString();
	                aux.ValorNuevo = despues.GastosCorresponsal.toString();
	                listaSalida.add(aux);
                }
                if(!antes.MarcaNegociacion.equalsIgnoreCase(despues.MarcaNegociacion)){
	                CamposModificados aux = new CamposModificados();
	                aux.IdTransaccion = antes.IdTransaccion;
	                aux.NombreCampo = "MarcaNegociacion";
	                aux.ValorAntiguo = antes.MarcaNegociacion;
	                aux.ValorNuevo = despues.MarcaNegociacion;
	                listaSalida.add(aux);
                }
                if(!antes.IdMonedaLiquidacionCorresponsal.equalsIgnoreCase(despues.IdMonedaLiquidacionCorresponsal)){
	                CamposModificados aux = new CamposModificados();
	                aux.IdTransaccion = antes.IdTransaccion;
	                aux.NombreCampo = "IdMonedaLiquidacionCorresponsal";
	                aux.ValorAntiguo = antes.IdMonedaLiquidacionCorresponsal;
	                aux.ValorNuevo = despues.IdMonedaLiquidacionCorresponsal;
	                listaSalida.add(aux);
                }
                if(!antes.IdCamara.equalsIgnoreCase(despues.IdCamara)){
	                CamposModificados aux = new CamposModificados();
	                aux.IdTransaccion = antes.IdTransaccion;
	                aux.NombreCampo = "IdCamara";
	                aux.ValorAntiguo = antes.IdCamara;
	                aux.ValorNuevo = despues.IdCamara;
	                listaSalida.add(aux);
                }
	        }
	    }
	    return listaSalida;
	}
	
	private ObtenerPlazo.InObtenerPlazo prepararCalculoCondicionLiquidacion(ObtenerPlazo.InObtenerPlazo entradaObtenerPlazo, List<InTransaccion> listaTransacciones , Map<Long,TransaccionExistente> hashTransacciones, Long idPais) {
		logger.trace("INICIO::ModificarTransaccion.prepararEntradaCalcularCondicionLiquidacion");
		long inicio = System.currentTimeMillis();
		
		entradaObtenerPlazo.p_IdPais = idPais;
		
		listaTransacciones.stream().forEach(transaccion ->{
			TransaccionExistente tran = hashTransacciones.get(transaccion.IdTransaccion);
			if(tran != null) {
				ObtenerPlazo.FechasIn f = new ObtenerPlazo.FechasIn();
				f.FechaDesde = tran.FechaMovimiento;
				f.FechaHasta = transaccion.FechaLiquidacion;
				f.IdInterno = transaccion.IdTransaccion;
				f.SoloDiasHabiles = "S";
				entradaObtenerPlazo.p_Fechas.add(f);
			}
		});
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarTransaccion.prepararEntradaCalcularCondicionLiquidacion tiempo: " + (fin - inicio));
		return entradaObtenerPlazo;	
	}
	
	private List<IdCondicionDeLiquidacion> buscarIdCondicionDeLiquidacion (Connection con) throws SQLException{
		logger.debug("INICIO::ModificarTransaccion.buscarIdCondicionDeLiquidacion");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		List<IdCondicionDeLiquidacion> listaIdCondicionDeLiquidacion = new ArrayList<>();
		
		HashMap<String,Object> params1 = new HashMap<>();
		listaIdCondicionDeLiquidacion = sql.query(con, SQL_SELECT_IDCONDICIONLIQUIDACION, params1, IdCondicionDeLiquidacion.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarTransaccion.buscarIdCondicionDeLiquidacion tiempo: " + (fin - inicio));
		return listaIdCondicionDeLiquidacion;
	}
	
	private void definirIdCondicionDeLiquidacion (Connection con, ObtenerPlazo.InObtenerPlazo entradaObtenerPlazoFechaLiquidacion, EntradaModificarTransaccion entrada, SalidaModificarTransaccion out) throws Exception {
		logger.trace("INICIO::ModificarTransaccion.definirIdCondicionDeLiquidacion");
		long inicio = System.currentTimeMillis();
		
		List<cl.bcs.plataforma.pla.ObtenerPlazo.FechasOut> PlazosCalculadosFechaliquidacion = new ArrayList<>();
		ObtenerPlazo serviceObtenerPlazo = new ObtenerPlazo();
		OutObtenerPlazo salidaObtenerPlazoFechaLiquidacion = serviceObtenerPlazo.logica(con, entradaObtenerPlazoFechaLiquidacion);
		if(salidaObtenerPlazoFechaLiquidacion.p_out_Fechas.size() > 0) {
			PlazosCalculadosFechaliquidacion = salidaObtenerPlazoFechaLiquidacion.p_out_Fechas;
		}
		if(PlazosCalculadosFechaliquidacion.size() > 0) {
			List<IdCondicionDeLiquidacion> listaIdCondicionDeLiquidacion = buscarIdCondicionDeLiquidacion(con);
			HashMap<Long, Object> hashIdCondicionDeLiquidacion = Utils.List2Hash(listaIdCondicionDeLiquidacion,"PlazoEnDias");
			HashMap<Long, Object> hashPlazoFechaLiquidacion = Utils.List2Hash(PlazosCalculadosFechaliquidacion,"IdInterno");
			if(hashIdCondicionDeLiquidacion.size()>0 && hashPlazoFechaLiquidacion.size() > 0) {
				for(InTransaccion transaccion : entrada.p_Transacciones) {
					cl.bcs.plataforma.pla.ObtenerPlazo.FechasOut plazo = (FechasOut) hashPlazoFechaLiquidacion.get(transaccion.IdTransaccion);
					if(plazo != null) {
						if(Fecha.toDate(plazo.FechaDesde).after(Fecha.toDate(plazo.FechaHasta))) {
							out.P_OUT_ERRORESCONTADOR++;
							out.P_OUT_MENSAJES.add(new MensajeSalida(transaccion.IdTransaccion,MensajeSalida.MENSAJE_ERROR,"FECHA_LIQUIDACION_MAYOR_FECHA_MOVMIENTO",new HashMap<>(),con,entrada.p_IdEmpresa));
							continue;
						}
						IdCondicionDeLiquidacion condicionLiquidacion = (IdCondicionDeLiquidacion) hashIdCondicionDeLiquidacion.get(plazo.Plazo);
						if(condicionLiquidacion != null) {
							transaccion.IdCondicionDeLiquidacion = condicionLiquidacion.IdCondicionDeLiquidacion;
						}else {
							out.P_OUT_ERRORESCONTADOR++;
							out.P_OUT_MENSAJES.add(new MensajeSalida(transaccion.IdTransaccion,MensajeSalida.MENSAJE_ERROR,"CONDICION_LIQUIDACION_NO_VALIDA_PARA_FECHA_LIQUIDACION",new HashMap<>(),con,entrada.p_IdEmpresa));
						}
					}
				}
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarTransaccion.definirIdCondicionDeLiquidacion tiempo: " + (fin - inicio));
	}
	
	private void definirPlazoSIM (Connection con, EntradaModificarTransaccion entrada, Map<Long,TransaccionExistente> hashTransacciones, Long idPais) throws Exception {
		logger.trace("INICIO::ModificarTransaccion.definirPlazoSIM");
		long inicio = System.currentTimeMillis();
		
		ObtenerPlazo.InObtenerPlazo entradaObtenerPlazo = new ObtenerPlazo.InObtenerPlazo();
		entradaObtenerPlazo.p_IdPais = idPais;

		for(InTransaccion transaccion : entrada.p_Transacciones) {
			if("SIM".equalsIgnoreCase(transaccion.IdTipoInstrumento)) {
				TransaccionExistente tran = hashTransacciones.get(transaccion.IdTransaccion);
				if(tran != null) {
					if(tran.IdTransaccionPlazo.compareTo(0L) != 0) {
						if( transaccion.FechaVencimiento != null && !"0001-01-01".equalsIgnoreCase(transaccion.FechaVencimiento)) {
							if(transaccion.FechaLiquidacion != null) {
								ObtenerPlazo.FechasIn f = new ObtenerPlazo.FechasIn();
								f.FechaDesde = transaccion.FechaLiquidacion;
								f.FechaHasta = transaccion.FechaVencimiento;
								f.IdInterno = transaccion.IdTransaccion;
								f.SoloDiasHabiles = "N";
								entradaObtenerPlazo.p_Fechas.add(f);
							}
						}
					}
				}
			}
		}
		
		if(entradaObtenerPlazo.p_Fechas.size() > 0) {
			ObtenerPlazo serviceObtenerPlazo = new ObtenerPlazo();
			OutObtenerPlazo salidaObtenerPlazo = serviceObtenerPlazo.logica(con, entradaObtenerPlazo);
			if(salidaObtenerPlazo.p_out_Fechas.size() > 0) {
				HashMap<Long, Object> hashPlazo = Utils.List2Hash(salidaObtenerPlazo.p_out_Fechas,"IdInterno");
				for(InTransaccion transaccion : entrada.p_Transacciones) {
					cl.bcs.plataforma.pla.ObtenerPlazo.FechasOut plazo = (FechasOut) hashPlazo.get(transaccion.IdTransaccion);
					if(plazo != null) {
						transaccion.Plazo = plazo.Plazo;
					}
				}
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarTransaccion.definirPlazoSIM tiempo: " + (fin - inicio));
	}
	
	private void validarFechaModificacion(Connection con, Map<Long,TransaccionExistente> hashTransacciones, EntradaModificarTransaccion entrada, String fechaSistema, SalidaModificarTransaccion salida) throws Exception {
		logger.trace("INICIO::ModificarTransaccion.validarFechaModificacion");
		long inicio = System.currentTimeMillis();
		
		for(InTransaccion transaccion : entrada.p_Transacciones) {
			TransaccionExistente tran = hashTransacciones.get(transaccion.IdTransaccion);
			if(tran != null) {
				if(!tran.FechaMovimiento.equalsIgnoreCase(fechaSistema)) {
					salida.P_OUT_ERRORESCONTADOR++;
					salida.P_OUT_MENSAJES.add(new MensajeSalida(transaccion.IdTransaccion,MensajeSalida.MENSAJE_ERROR,"FECHA_MODIFICACION_DIFERENTE_FECHA_MOVIMIENTO",new HashMap<>(),con,entrada.p_IdEmpresa));
				}
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarTransaccion.validarFechaModificacion tiempo: " + (fin - inicio));
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01","connections.properties")) { //-->FIJARSE EN QUE ENTORNO SE ESTA PROBANDO<--
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
		String jsonRF = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"ORD\",\"p_UsaReglasNegocio\":\"S\",\"p_Transacciones\":[{\"IdTransaccion\":70289875,\"Monto\":241800,\"Cantidad\":31,\"EsBilateral\":\"N\",\"BloqueoLiquidacion\":\"N\",\"FechaLiquidacion\":\"2019-08-23\",\"MontoRetencion\":0,\"AccionTipoOperacion\":\"COMPRA\",\"TipoCambioCorresponsal\":0,\"IdCorredorCorresponsal\":0,\"MontoLiquidacionCorresponsal\":0,\"GastosCorresponsal\":0,\"MarcaNegociacion\":\"KT\",\"IdMonedaLiquidacionCorresponsal\":\"\",\"IdCamara\":\"SM\"}]}";
		EntradaModificarTransaccion Objin = gson.fromJson(jsonRF, EntradaModificarTransaccion.class);
		ModificarTransaccion demo=new ModificarTransaccion();				
		try {
			SalidaModificarTransaccion ObjOut=demo.logica(con, Objin);
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
