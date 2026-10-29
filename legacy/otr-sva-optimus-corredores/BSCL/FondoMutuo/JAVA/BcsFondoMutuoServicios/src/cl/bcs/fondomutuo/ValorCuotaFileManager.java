package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
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

import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.pojo.Emisor;
import cl.bcs.plataforma.ftp.FileManager.Cabecera;
import cl.bcs.plataforma.ftp.FileManager.Detalle;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ValorCuotaFileManager implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ValorCuotaFileManager.class);
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	private final String SQL_SELECT_EMISOR = loadResourceToString("cl/bcs/fondomutuo/sql/Comunes_SelectEmisor.sql");
	private final String SQL_SELECT_CABECERA = loadResourceToString("cl/bcs/fondomutuo/sql/Comunes_SelectCabeceras.sql");
	private final String SQL_SELECT_DETALLE = loadResourceToString("cl/bcs/fondomutuo/sql/ValorCuotaFileManager_SelectDetalle.sql");
	private final String SQL_SELECT_IDSERIEFONDO = loadResourceToString("cl/bcs/fondomutuo/sql/ValorCuotaFileManager_SelectIdSerieFondo.sql");
	private final String SQL_UPDATE_ESTADO_CABECERA = loadResourceToString("cl/bcs/fondomutuo/sql/TrxFileManager_UpdateEstadoCabecera.sql");
	
	private static final String VALOR_CUOTA_TIPO = "VALCUO";
	private static final Integer LARGO_LINEA = 84;
	
	public static class EntradaValorCuotaFileManager {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InValorCuota {
		public String IdSerieFondo;
		public String IdMoneda;
		public String FechaCierre;
		public BigDecimal ValorCuota;
		public String Observacion;
		public String MensajeValidacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaValorCuotaFileManager {
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_ERRORESDETALLE;
		
		public SalidaValorCuotaFileManager(){
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_ERRORESDETALLE = new ArrayList<MensajeSalida>(); 
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class IdSerieFondo {
		public String IdSerieFondo;
		public String CodigoSVS;
		public String CodigoSerie;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public class UpdateEstadoCabecera {
		public Long idCabecera;
		public String idEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();		
		EntradaValorCuotaFileManager in=gson.fromJson(jin.toString() ,EntradaValorCuotaFileManager.class);
		SalidaValorCuotaFileManager out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaValorCuotaFileManager logica(Connection conn, EntradaValorCuotaFileManager entrada) throws Exception {
		logger.info("INICIO:ValorCuotaFileManager");
		SalidaValorCuotaFileManager out = new SalidaValorCuotaFileManager();
		try {
			List<Emisor> emisores = getEmisores(conn);
			List<Cabecera> cabeceraList = getCabeceras(conn, emisores);
			if(cabeceraList.size() > 0) {
				List<Detalle> detalleList = getDetalles(conn, cabeceraList);
				List<IdSerieFondo> serieList = getSeriesFondo(conn, detalleList);				
				CargarValorCuota.SalidaCargarValorCuota salidaCargarValorCuota = cargarValorCuota(conn, entrada, detalleList, serieList);
								
				out.P_OUT_ERRORESCONTADOR = salidaCargarValorCuota.P_OUT_ERRORESCONTADOR;
				out.P_OUT_ERRORESDETALLE = obtenerMensajeErrores(salidaCargarValorCuota);
				
				if(null != out && out.P_OUT_ERRORESCONTADOR < 1) {
					cabeceraList.parallelStream().forEach(cabecera->{
						cabecera.IdEstado = _c.EstadoProcesado;
					});
				} else {
					cabeceraList.parallelStream().forEach(cabecera->{
						cabecera.IdEstado = _c.EstadoError;
					});
				}
				
				actualizarEstadoCabecera(conn, cabeceraList);
			}
		} catch (SQLException e) {
			logger.error("ERROR:ValorCuotaFileManager::", e);
			throw e;
		} catch (Exception e) {
			logger.error("ERROR:ValorCuotaFileManager::", e);
			throw e;
		}
		logger.info("FIN:ValorCuotaFileManager");
		return out;
	}
	
	
	private List<Emisor> getEmisores(Connection conn) throws SQLException{
		logger.info("INICIO::ValorCuotaFileManager.getEmisores");
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		List<Emisor> emisores = sql.query(conn, SQL_SELECT_EMISOR, params, Emisor.class);
		logger.info("FIN::ValorCuotaFileManager.getEmisores");
		return emisores;
	}
	
	private List<Cabecera> getCabeceras(Connection conn,List<Emisor> emisores) throws SQLException{
		logger.info("INICIO::ValorCuotaFileManager.getCabeceras");
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		List<String> codigosTipo = emisores.parallelStream().map(emisor->VALOR_CUOTA_TIPO + "_" + emisor.IdEmisor).collect(Collectors.toList());
		params.put("IdCodigoTipo", codigosTipo);
		List<Cabecera> cabeceras = sql.query(conn, SQL_SELECT_CABECERA, params, Cabecera.class);
		logger.info("FIN::ValorCuotaFileManager.getCabeceras");
		return cabeceras;
		
	}
	
	private List<Detalle> getDetalles(Connection conn, List<Cabecera> cabeceras) throws SQLException{
		logger.info("INICIO::ValorCuotaFileManager.getDetalles");
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		List<Long> idCabeceras = cabeceras.stream().map(cabecera -> cabecera.IdCabecera).collect(Collectors.toList());
		params.clear();
		params.put("IdCabecera", idCabeceras);
		List<Detalle> detalles = sql.query(conn, SQL_SELECT_DETALLE, params, Detalle.class);
		logger.info("FIN::ValorCuotaFileManager.getDetalles");
		return detalles;
	}
	
	private List<IdSerieFondo> getSeriesFondo(Connection conn, List<Detalle> detalles) throws SQLException{
		logger.info("INICIO::ValorCuotaFileManager.getSeriesFondo");
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		List<String> codigosFondos = detalles.parallelStream().filter(detalle->esLineaValida(detalle)).map(detalle -> {
			return calcularCodigoFondo(detalle);
		}).collect(Collectors.toList());
		
		List<String> seriesFondos = detalles.parallelStream().filter(detalle->esLineaValida(detalle)).map(detalle -> {
			return calcularCodigoSerie(detalle);
		}).collect(Collectors.toList());
		
		params.put("IdCodigoFondo", codigosFondos);
		params.put("CodigoSerie", seriesFondos);
		List<IdSerieFondo> series = sql.query(conn, SQL_SELECT_IDSERIEFONDO, params, IdSerieFondo.class);
		logger.info("FIN::ValorCuotaFileManager.getSeriesFondo");
		return series;
	}
	
	private Boolean esLineaValida(Detalle detalle) {
		logger.info("INICIO::ValorCuotaFileManager.esLineaValida");
		Boolean esLineaValida = null != detalle && null != detalle.Linea && detalle.Linea.length() >= LARGO_LINEA;
		logger.info("INICIO::ValorCuotaFileManager.esLineaValida");
		return esLineaValida;
	}
	
	private String calcularCodigoFondo(Detalle detalle) {
		logger.info("INICIO::ValorCuotaFileManager.calcularCodigoFondo");
		String codigoFondo = detalle.Linea.substring(10, 18);
		codigoFondo = (null != codigoFondo) ? codigoFondo.replaceFirst ("^0*", "") : "";
		logger.info("FIN::ValorCuotaFileManager.calcularCodigoFondo");
		return codigoFondo;
	}
	
	private String calcularCodigoSerie(Detalle detalle) {
		logger.info("INICIO::ValorCuotaFileManager.calcularCodigoFondo");
		String codigoSerie = detalle.Linea.substring(83, detalle.Linea.length()).trim();
		logger.info("FIN::ValorCuotaFileManager.calcularCodigoFondo");
		return codigoSerie;
	}
	
	private CargarValorCuota.SalidaCargarValorCuota cargarValorCuota(Connection con, EntradaValorCuotaFileManager entrada, List<Detalle> detalles, List<IdSerieFondo> series) throws Exception {
		logger.info("INICIO::ValorCuotaFileManager.cargarValorCuota");
		CargarValorCuota cargarValorCuota = new CargarValorCuota();
		CargarValorCuota.EntradaCargarValorCuota entradaCargarValorCuota =  new CargarValorCuota.EntradaCargarValorCuota();
		entradaCargarValorCuota.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCargarValorCuota.p_IdUsuario = entrada.p_IdUsuario;
		entradaCargarValorCuota.p_IdModulo = _c.ModuloFondoMutuo;
		entradaCargarValorCuota.p_UsaReglasNegocio = _c.NO;
		entradaCargarValorCuota.p_SimularOperacion = _c.SI;
		
		entradaCargarValorCuota.p_ValorCuota = detalles.parallelStream().filter(detalle->esLineaValida(detalle)).filter(detalle->{
			String codigoFondo = calcularCodigoFondo(detalle);
			String codigoSerie = calcularCodigoSerie(detalle);
			return series.parallelStream().filter(serie-> serie.CodigoSerie.equals(codigoSerie) && serie.CodigoSVS.equals(codigoFondo)).collect(Collectors.toList()).size() > 0;
		}).map(detalle->{
			CargarValorCuota.ValorCuota inValorCuota = new CargarValorCuota.ValorCuota();
			inValorCuota.VALORCUOTA = new BigDecimal(getValorCuota(detalle));
			inValorCuota.IDMONEDA = detalle.Linea.substring(36, 39);
			inValorCuota.FECHACIERRE = getFechaCierre(detalle);
			String codigoFondo = calcularCodigoFondo(detalle);
			String codigoSerie =  calcularCodigoSerie(detalle);
			IdSerieFondo serieFondo = series.parallelStream().filter(serie-> serie.CodigoSerie.equals(codigoSerie) && serie.CodigoSVS.equals(codigoFondo)).collect(Collectors.toList()).get(0);
			inValorCuota.IDSERIEFONDO = serieFondo.IdSerieFondo;
			inValorCuota.OBSERVACION = "";
			inValorCuota.MENSAJEVALIDACION = _c.OK;
			return inValorCuota;
		}).collect(Collectors.toList());
		
		CargarValorCuota.SalidaCargarValorCuota salidaCargarValorCuota = cargarValorCuota.logica(con, entradaCargarValorCuota);
		logger.info("FIN::ValorCuotaFileManager.cargarValorCuota");
		return salidaCargarValorCuota;
	}
	
	private String getFechaCierre(Detalle detalle) {
		logger.info("INICIO::ValorCuotaFileManager.getFechaCierre");
		String fechaCierre = "";
		fechaCierre = detalle.Linea.substring(57, 65);
		fechaCierre = (null != fechaCierre) ? fechaCierre.substring(0, 4) + "-" + 
				fechaCierre.substring(4, 6) + "-" + fechaCierre.substring(6, fechaCierre.length()) : "";
		logger.info("FIN::ValorCuotaFileManager.getFechaCierre");
		return fechaCierre;
	}
	
	private String getValorCuota(Detalle detalle) {
		logger.info("INICIO::ValorCuotaFileManager.getValorCuota");
		String valorCuota = "";
		valorCuota = detalle.Linea.substring(18, 36);
		valorCuota = valorCuota.substring(0, 9) + "." + valorCuota.substring(9, 18);
		valorCuota = (null != valorCuota) ? valorCuota.replaceFirst ("^0*", "") : "";
		logger.info("FIN::ValorCuotaFileManager.getValorCuota");
		return valorCuota;
	}
	
	private List<MensajeSalida> obtenerMensajeErrores(CargarValorCuota.SalidaCargarValorCuota salidaCargarValorCuota){
		logger.info("INICIO::ValorCuotaFileManager.obtenerMensajeErrores");
		List<MensajeSalida> mensajesErrores = salidaCargarValorCuota.P_OUT_MENSAJES.parallelStream().filter(mensaje->MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)).collect(Collectors.toList());
		logger.info("FIN::ValorCuotaFileManager.obtenerMensajeErrores");
		return mensajesErrores;
	}
	
	private void actualizarEstadoCabecera(Connection conn, List<Cabecera> cabeceraList) throws SQLException {
		logger.info("INICIO::ValorCuotaFileManager.actualizarEstadoCabecera");
		SQL sql = new SQL();
		sql.batch(conn, SQL_UPDATE_ESTADO_CABECERA, cabeceraList, Cabecera.class);
		logger.info("FIN::ValorCuotaFileManager.actualizarEstadoCabecera");
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		
		try (Connection con = PlataformaUtils.getPRESURAConnection()) {
		
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
			String jsonRF = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0}";
					
			EntradaValorCuotaFileManager Objin = gson.fromJson(jsonRF, EntradaValorCuotaFileManager.class);
			
			ValorCuotaFileManager  demo= new ValorCuotaFileManager();				
 			
 			try {
 				SalidaValorCuotaFileManager ObjOut=demo.logica(con, Objin);
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
