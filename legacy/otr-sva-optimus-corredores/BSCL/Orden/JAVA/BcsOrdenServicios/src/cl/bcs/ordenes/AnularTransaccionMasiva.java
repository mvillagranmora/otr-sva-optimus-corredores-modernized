package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.utils.BcsPojo;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.CrearTransaccionesHistoricas.EntradaCrearTransaccionesHistoricas;
import cl.bcs.ordenes.CrearTransaccionesHistoricas.SalidaCrearTransaccionesHistoricas;
import cl.bcs.ordenes.NotificarCambioTransaccion.NotificarCambioTransaccion_Salida;
import cl.bcs.ordenes.pojo.Transaccion;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AnularTransaccionMasiva implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(AnularTransaccionMasiva.class);
	
	private final String SQL_SELECT_TRANSACCIONES = loadResourceToString("cl/bcs/ordenes/sql/AnularTransaccionMasiva_SelectTransacciones.sql");
	private final String SQL_SELECT_ASIGNACIONES = loadResourceToString("cl/bcs/ordenes/sql/AnularTransaccionMasiva_SelectAsignaciones.sql");
	private final String SQL_SELECT_CIERRE_MODULOS = loadResourceToString("cl/bcs/ordenes/sql/AnularTransaccionMasiva_SelectCierreModulos.sql");
	private final String SQL_UPDATE_TRANSACCIONES = loadResourceToString("cl/bcs/ordenes/sql/AnularTransaccionMasiva_UpdateTransacciones.sql");
	private final String SQL_SELECT_TRANSACCIONES_ANULADAS = loadResourceToString("cl/bcs/ordenes/sql/AnularTransaccionMasiva_SelectTransaccionesAnuladas.sql");
	private final String SQL_SELECT_DATOS_LOG = loadResourceToString("cl/bcs/ordenes/sql/AnularTransaccionMasiva_SelectDatosLog.sql");

	public static class EntradaAnularTransaccionMasiva{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_Reproceso;
		public List<InTransaccion> p_transacciones;
		
		public EntradaAnularTransaccionMasiva() {
			this.p_Reproceso = "N";
			this.p_transacciones = new ArrayList<InTransaccion>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InTransaccion{
		public Long IdTransaccion;
		public String Motivo;
		public String Observaciones;
		public String FechaAnulacion;
		public String EsModificacionMarketData;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaAnularTransaccionMasiva{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaAnularTransaccionMasiva() {
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class TransaccionReal{
		public Long IdTransaccion;
		public Long IdTransaccionPlazo;
		public String IdEstado;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Asignacion{
		public Long IdTransaccion;
		public Long Count;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CierreModulo{
		public String CodigoModulo;
		public String IdEstado;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ObjTransaccion extends BcsPojo{
		public Long IdTransaccion;
		public String Folio; 
		public String Nemotecnico;
		public String IdTipoOperacion; 
		public String IdMoneda;
		public BigDecimal Monto;
		public BigDecimal Precio;
		public BigDecimal Cantidad;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaAnularTransaccionMasiva jin = gson.fromJson(in.toString(), EntradaAnularTransaccionMasiva.class);
		SalidaAnularTransaccionMasiva out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAnularTransaccionMasiva logica(Connection con, EntradaAnularTransaccionMasiva entrada) throws Exception {
		logger.info("INICIO::AnularTransaccionMasiva");
		SalidaAnularTransaccionMasiva out = new SalidaAnularTransaccionMasiva();
		
		HashMap<Long,Object> hashTransaccionesReal = obtenerTransacciones(con,entrada);
		HashMap<Long,Object> hashAsignaciones = obtenerAsignaciones(con,entrada);
		HashMap<String,Object> hashCierreModulo = obtenerCierreModulo(con,entrada);
		
		for(InTransaccion transaccion : entrada.p_transacciones) {
			out.P_OUT_MENSAJES.addAll(validaTransaccion(con,transaccion,hashTransaccionesReal,hashAsignaciones,hashCierreModulo,entrada.p_IdEmpresa));
		}
		
		List<InTransaccion> transaccionesAnular = obtenerTransaccionesCorrectas(entrada, out.P_OUT_MENSAJES);
		transaccionesAnular.addAll(obtenerTransaccionPlazo(transaccionesAnular,hashTransaccionesReal));
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		if("N".equals(entrada.p_Reproceso)) {
			String estado = out.P_OUT_ERRORESCONTADOR.equals(0L) ? "PRO" : "PEN";
			SalidaCrearTransaccionesHistoricas salidaTransaccionHistorica = creacionTransaccionHistorica(con,entrada,estado);
			out.P_OUT_ERRORESCONTADOR += salidaTransaccionHistorica.P_OUT_ERRORESCONTADOR;
			out.P_OUT_MENSAJES.addAll(salidaTransaccionHistorica.P_OUT_MENSAJES);
		}
		
		for(InTransaccion transaccion : transaccionesAnular) {
			HashMap<String,String> params = new HashMap<String,String>();
			params.put("IdTransaccion", String.valueOf(transaccion.IdTransaccion));
			out.P_OUT_MENSAJES.add(new MensajeSalida(transaccion.IdTransaccion,MensajeSalida.MENSAJE_OK,"TRANSACCION_ANULADA",params,con,entrada.p_IdEmpresa));
		}
		
		if(out.P_OUT_ERRORESCONTADOR == 0) {
	        String notificar = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.NotificarCambioTransaccion");
	        if (notificar.equals("S")) {notificarModificacion(con, entrada);}
	        actualizarTransaccion(con,transaccionesAnular);
		}
		
		//Agregar Log Anular Transaccion 
		AgregarLog agregarLog = new AgregarLog();
		InAgregarLog entradaLog = new InAgregarLog();
		SQL sql=new SQL();

		if (transaccionesAnular != null && !transaccionesAnular.isEmpty()) {

			List<Long> lstTransacciones = transaccionesAnular.stream().map(procesamiento ->{
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
			
			String fechaAnulacion = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
	
			for(InTransaccion datosLog : transaccionesAnular) {
				ObjTransaccion tranLog = new ObjTransaccion();
				if(hashTransaccionesLog.size()>0) {
					tranLog = (ObjTransaccion) hashTransaccionesLog.get(datosLog.IdTransaccion);
				}
				
				//Construir entradas log
				InAgregarLog.Log headLog = new InAgregarLog.Log();
				headLog.NombreProceso = "Anular Transaccion Masiva";
				headLog.IdCabeceraLog = datosLog.IdTransaccion != null ? datosLog.IdTransaccion : 0L;
				headLog.IdTipoLog = "ANULACION";
				headLog.IdModulo = entrada.p_IdModulo;
				headLog.IdTipoRegistro = "ANULAR_TRANSACCION";
				headLog.IdUsuario = entrada.p_IdUsuario;
				headLog.IdEmpresa = entrada.p_IdEmpresa;
				headLog.FechaSistemaLog = fechaAnulacion != null ? fechaAnulacion : "";
				headLog.IdUsuarioAtendido = entrada.p_IdUsuario;
				headLog.Folio = (tranLog != null && tranLog.Folio != null && !tranLog.Folio.isEmpty()) ? Long.parseLong(tranLog.Folio) : 0L;
                headLog.IdTipoOperacion = (tranLog != null && tranLog.IdTipoOperacion != null) ? tranLog.IdTipoOperacion : "";
                headLog.Nemo = (tranLog != null && tranLog.Nemotecnico != null) ? tranLog.Nemotecnico : "";
                headLog.IdMonedaOrigen = (tranLog != null && tranLog.IdMoneda != null) ? tranLog.IdMoneda: "";
				headLog.Monto = (tranLog != null && tranLog.Monto != null) ? tranLog.Monto : BigDecimal.ZERO;
                headLog.PrecioTasa = (tranLog != null && tranLog.Precio != null) ? tranLog.Precio : BigDecimal.ZERO;
                headLog.Cantidad = (tranLog != null && tranLog.Cantidad != null) ? tranLog.Cantidad : BigDecimal.ZERO;
				
				entradaLog.p_Log.add(headLog);
			}	
			//llamar al log
			agregarLog.logica(con, entradaLog);
		}		
		logger.info("FIN::AnularTransaccionMasiva");
		return out;
	}
	
	private NotificarCambioTransaccion_Salida notificarModificacion (Connection con, EntradaAnularTransaccionMasiva entrada) throws Exception {
		logger.info("INICIO::ModificarTransaccion.notificarModificacion");
		
		NotificarCambioTransaccion.EntradaModificarTransaccion notificar = new NotificarCambioTransaccion.EntradaModificarTransaccion();
		notificar.p_IdEmpresa 		= entrada.p_IdEmpresa;
		notificar.p_IdUsuario		= entrada.p_IdUsuario;
		notificar.p_IdModulo		= entrada.p_IdModulo;
		notificar.p_Accion			= "ANULADA";
		notificar.p_InTransacciones = new ArrayList<>();
		
		for (InTransaccion inTran : entrada.p_transacciones) {
			NotificarCambioTransaccion.InTransaccion p_tran = new NotificarCambioTransaccion.InTransaccion();
			
			p_tran.IdTransaccion = inTran.IdTransaccion;
			
			notificar.p_InTransacciones.add(p_tran);			
		}		
		
		NotificarCambioTransaccion notif = new NotificarCambioTransaccion();
		NotificarCambioTransaccion.NotificarCambioTransaccion_Salida notifOut = notif.logica(con, notificar);
		
		logger.info("FIN::ModificarTransaccion.notificarModificacion"); 
		return notifOut;
	}
	
	private HashMap<Long,Object> obtenerTransacciones(Connection con, EntradaAnularTransaccionMasiva entrada) throws Exception{
		logger.info("INICIO::AnularTransaccionMasiva.obtenerTransacciones");
		SQL sql = new SQL();
		
		List<Long> idTransacciones = entrada.p_transacciones.parallelStream().map(transaccion -> transaccion.IdTransaccion).collect(Collectors.toList());
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdTransaccion", idTransacciones);
		List<TransaccionReal> transaccionesReales = sql.query(con, SQL_SELECT_TRANSACCIONES, params, TransaccionReal.class);
		
		logger.info("FIN::AnularTransaccionMasiva.obtenerTransacciones");
		return Utils.List2Hash(transaccionesReales, "IdTransaccion");
	}
	
	private HashMap<Long,Object> obtenerAsignaciones(Connection con, EntradaAnularTransaccionMasiva entrada) throws Exception{
		logger.info("INICIO::AnularTransaccionMasiva.obtenerAsignaciones");
		SQL sql = new SQL();
		
		List<Long> idTransacciones = entrada.p_transacciones.parallelStream().map(transaccion -> transaccion.IdTransaccion).collect(Collectors.toList());
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdTransaccion", idTransacciones);
		List<Asignacion> asignaciones = sql.query(con, SQL_SELECT_ASIGNACIONES, params, Asignacion.class);
		
		logger.info("FIN::AnularTransaccionMasiva.obtenerAsignaciones");
		return Utils.List2Hash(asignaciones, "IdTransaccion");
	}
	
	private HashMap<String,Object> obtenerCierreModulo(Connection con, EntradaAnularTransaccionMasiva entrada) throws Exception{
		logger.info("INICIO::AnularTransaccionMasiva.obtenerCierreModulo");
		SQL sql = new SQL();
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		List<CierreModulo> cierreModulo = sql.query(con, SQL_SELECT_CIERRE_MODULOS, params, CierreModulo.class);
		
		logger.info("FIN::AnularTransaccionMasiva.obtenerCierreModulo");
		return Utils.List2Hash(cierreModulo, "CodigoModulo");
	}
	
	private List<MensajeSalida> validaTransaccion(Connection con,InTransaccion transaccion,HashMap<Long,Object> hashTransaccionesReal,HashMap<Long,Object> hashAsignaciones,HashMap<String,Object> hashCierreModulo,Long idEmpresa) throws Exception{
		logger.info("INICIO::AnularTransaccionMasiva.validaTransaccion");
		List<MensajeSalida> mensajes = new ArrayList<MensajeSalida>();
		
		TransaccionReal transaccionReal = (TransaccionReal) hashTransaccionesReal.get(transaccion.IdTransaccion);
		Asignacion asignacion = (Asignacion) hashAsignaciones.get(transaccion.IdTransaccion);
		CierreModulo cierreModulo = (CierreModulo) hashCierreModulo.get("ORD");
		
		if(transaccionReal == null) {
			HashMap<String,String> params = new HashMap<String,String>();
			params.put("IdTransaccion", String.valueOf(transaccion.IdTransaccion));
			mensajes.add(new MensajeSalida(transaccion.IdTransaccion,MensajeSalida.MENSAJE_ERROR,"TRANSACCION_NO_EXISTE",params,con,idEmpresa));
		}
		
		if(transaccionReal != null && "ANU".equalsIgnoreCase(transaccionReal.IdEstado)) {
			HashMap<String,String> params = new HashMap<String,String>();
			params.put("IdTransaccion", String.valueOf(transaccion.IdTransaccion));
			mensajes.add(new MensajeSalida(transaccion.IdTransaccion,MensajeSalida.MENSAJE_ERROR,"TRANSACCION_YA_ANULADA",params,con,idEmpresa));
		}
		
		if(asignacion != null && asignacion.Count > 0) {
			HashMap<String,String> params = new HashMap<String,String>();
			params.put("IdTransaccion", String.valueOf(transaccion.IdTransaccion));
			mensajes.add(new MensajeSalida(transaccion.IdTransaccion,MensajeSalida.MENSAJE_ERROR,"TRANSACCION_TIENE_ASIGNACIONES",params,con,idEmpresa));
		}
		
		if(cierreModulo != null && "C".equals(cierreModulo.IdEstado)) {
			HashMap<String,String> params = new HashMap<String,String>();
			params.put("NombreModulo", String.valueOf(cierreModulo.CodigoModulo));
			mensajes.add(new MensajeSalida(transaccion.IdTransaccion,MensajeSalida.MENSAJE_ERROR,"MODULO_CERRADO",params,con,idEmpresa));
		}
		
		logger.info("FIN::AnularTransaccionMasiva.validaTransaccion");
		return mensajes;
	}
	
	private List<InTransaccion> obtenerTransaccionesCorrectas(EntradaAnularTransaccionMasiva entrada, List<MensajeSalida> mensajes){
		logger.info("INICIO::AnularTransaccionMasiva.obtenerTransaccionesCorrectas");
		List<InTransaccion> salida = entrada.p_transacciones.parallelStream()
				.filter(transaccion-> mensajes.parallelStream().filter(mensaje-> mensaje.id.equals(transaccion.IdTransaccion) && MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)).count() == 0)
				.collect(Collectors.toList());
		logger.info("FIN::AnularTransaccionMasiva.obtenerTransaccionesCorrectas");
		return salida;
	}
	
	private List<InTransaccion> obtenerTransaccionPlazo(List<InTransaccion> transaccionesAnular, HashMap<Long,Object> hashTransacciones){
		logger.info("INICIO::AnularTransaccionMasiva.obtenerTransaccionPlazo");
		List<InTransaccion> salida = transaccionesAnular.parallelStream().filter(transaccion->{
			TransaccionReal transaccionExistente = (TransaccionReal) hashTransacciones.get(transaccion.IdTransaccion);
			return transaccionExistente != null && transaccionExistente.IdTransaccionPlazo != null && transaccionExistente.IdTransaccionPlazo > 0;
		}).map(transaccion->{
			InTransaccion transaccionAnular = new InTransaccion();
			transaccionAnular.FechaAnulacion = transaccion.FechaAnulacion;
			transaccionAnular.Motivo = transaccion.Motivo;
			transaccionAnular.Observaciones = transaccion.Observaciones;
			TransaccionReal transaccionExistente = (TransaccionReal) hashTransacciones.get(transaccion.IdTransaccion);
			transaccionAnular.IdTransaccion = transaccionExistente.IdTransaccionPlazo;
			return transaccionAnular;
		}).collect(Collectors.toList());
		logger.info("FIN::AnularTransaccionMasiva.obtenerTransaccionPlazo");
		return salida;
	}
	
	private void actualizarTransaccion(Connection con, List<InTransaccion> transaccionesModificar) throws Exception {
		logger.info("INICIO::AnularTransaccionMasiva.actualizarTransaccion");
		SQL sql=new SQL();
		sql.batch(con, SQL_UPDATE_TRANSACCIONES, transaccionesModificar, InTransaccion.class);
		logger.info("FIN::AnularTransaccionMasiva.actualizarTransaccion");
	}
	
	private SalidaCrearTransaccionesHistoricas creacionTransaccionHistorica(Connection con, EntradaAnularTransaccionMasiva entrada, String estado) throws Exception {
		logger.info("INICIO::AnularTransaccionMasiva.creacionTransaccionHistorica");
		SQL sql=new SQL();
		HashMap<Long, Object> hashTransacciones = Utils.List2Hash(entrada.p_transacciones, "IdTransaccion");
		List<Long> idTransacciones = entrada.p_transacciones.parallelStream().map(transaccion->transaccion.IdTransaccion).collect(Collectors.toList());
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdTransaccion", idTransacciones);
		List<Transaccion> transaccionesFinales = sql.query(con, SQL_SELECT_TRANSACCIONES_ANULADAS, params, Transaccion.class);
		
		CrearTransaccionesHistoricas crearTransaccionesHistoricas = new CrearTransaccionesHistoricas();
		EntradaCrearTransaccionesHistoricas entradaCrearTransaccionesHistoricas = new EntradaCrearTransaccionesHistoricas();
		entradaCrearTransaccionesHistoricas.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearTransaccionesHistoricas.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearTransaccionesHistoricas.p_IdModulo = entrada.p_IdModulo;
		entradaCrearTransaccionesHistoricas.p_UsaReglasNegocio = "S";
		entradaCrearTransaccionesHistoricas.p_Transacciones = transaccionesFinales.parallelStream().map(transaccion->{
			CrearTransaccionesHistoricas.InTransaccionHistorica transaccionHistorica = new CrearTransaccionesHistoricas.InTransaccionHistorica();
			transaccionHistorica.TipoRegistro = "ANU";
			transaccionHistorica.IdEstadoRegistro = estado;
			transaccionHistorica.Transaccion = transaccion;
			InTransaccion transaccionEntrada = (InTransaccion) hashTransacciones.get(transaccion.IdTransaccion);
			transaccionHistorica.EsModificacionMarketData = transaccionEntrada == null ? "N" : transaccionEntrada.EsModificacionMarketData;
			return transaccionHistorica;
		}).collect(Collectors.toList());
		
		SalidaCrearTransaccionesHistoricas salidaTransaccionHistorica = crearTransaccionesHistoricas.logica(con, entradaCrearTransaccionesHistoricas);
		logger.info("FIN::AnularTransaccionMasiva.creacionTransaccionHistorica");
		return salidaTransaccionHistorica;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02", "connections.properties")) { //-->FIJARSE EN QUE ENTORNO SE ESTA PROBANDO<--
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
		String jsonRF = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9565,\"p_IdModulo\":\"ORD\",\"p_transacciones\":[{\"IdTransaccion\":3747463,\"Motivo\":\"\",\"Observaciones\":\"\",\"FechaAnulacion\":null}]}";
		EntradaAnularTransaccionMasiva Objin = gson.fromJson(jsonRF, EntradaAnularTransaccionMasiva.class);
		AnularTransaccionMasiva demo=new AnularTransaccionMasiva();				
		try {
			SalidaAnularTransaccionMasiva ObjOut=demo.logica(con, Objin);
			//con.commit();
			con.rollback();
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
