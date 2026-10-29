package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
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

public class ValorCuotaInternacionalFileManager implements JavaCall{

	static final Logger logger = LogManager.getLogger(ValorCuotaInternacionalFileManager.class);
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	private final String SQL_SELECT_EMISOR = loadResourceToString("cl/bcs/fondomutuo/sql/Comunes_SelectEmisor.sql");
	private final String SQL_SELECT_CABECERA = loadResourceToString("cl/bcs/fondomutuo/sql/Comunes_SelectCabeceras.sql");
	private final String SQL_SELECT_DETALLE = loadResourceToString("cl/bcs/fondomutuo/sql/ValorCuotaFileManager_SelectDetalle.sql");
	private final String SQL_SELECT_IDSERIEFONDO = loadResourceToString("cl/bcs/fondomutuo/sql/ValorCuotaInternacionalFileManager_SelectIdSerieFondo.sql");
	private final String SQL_UPDATE_ESTADO_CABECERA = loadResourceToString("cl/bcs/fondomutuo/sql/TrxFileManager_UpdateEstadoCabecera.sql");
	
	private static final String VALOR_CUOTA_TIPO_I = "VALCUI";
	private static final String CABECERA_ISIN = "ISIN";
	
	public static class EntradaValorCuotaInternacionalFileManager {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaValorCuotaInternacionalFileManager {
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_ERRORESDETALLE;
		
		public SalidaValorCuotaInternacionalFileManager(){
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_ERRORESDETALLE = new ArrayList<MensajeSalida>(); 
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class IdSerieFondo {
		public String ISIN;
		public String IdSerieFondo;
		public String IdMoneda;
		
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
		EntradaValorCuotaInternacionalFileManager in=gson.fromJson(jin.toString() ,EntradaValorCuotaInternacionalFileManager.class);
		SalidaValorCuotaInternacionalFileManager out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaValorCuotaInternacionalFileManager logica(Connection conn, EntradaValorCuotaInternacionalFileManager entrada) throws Exception {
		logger.info("INICIO::ValorCuotaInternacionalFileManager");
		SalidaValorCuotaInternacionalFileManager out = new SalidaValorCuotaInternacionalFileManager();
		
		try {
			List<Emisor> emisores = getEmisores(conn);
			List<Cabecera> cabeceraList = getCabeceras(conn, emisores);
			
			if(cabeceraList.size() > 0) {
				List<Detalle> detalleList = getDetalles(conn, cabeceraList);
				List<IdSerieFondo> serieList = getSeriesFondo(conn, detalleList);
				CargarValorCuota.SalidaCargarValorCuota salidaCargarValorCuota = cargarValorCuota(conn, entrada, detalleList, serieList);
				out.P_OUT_ERRORESCONTADOR = salidaCargarValorCuota.P_OUT_ERRORESCONTADOR;
				out.P_OUT_ERRORESDETALLE  = obtenerMensajeErrores(salidaCargarValorCuota);
				
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
			logger.error("ERROR::ValorCuotaInternacionalFileManager::", e);
			throw e;
		} catch (Exception e) {
			logger.error("ERROR::ValorCuotaInternacionalFileManager::", e);
			throw e;
		}
		
		logger.info("FIN::ValorCuotaInternacionalFileManager");
		return out;
	}
	
	private List<Emisor> getEmisores(Connection conn) throws SQLException{
		logger.info("INICIO::ValorCuotaInternacionalFileManager.getEmisores");
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		List<Emisor> emisores = sql.query(conn, SQL_SELECT_EMISOR, params, Emisor.class);
		logger.info("FIN::ValorCuotaInternacionalFileManager.getEmisores");
		return emisores;
	}
	
	private List<Cabecera> getCabeceras(Connection conn,List<Emisor> emisores) throws SQLException{
		logger.info("INICIO::ValorCuotaInternacionalFileManager.getCabeceras");
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		List<String> codigosTipo = emisores.parallelStream().map(emisor->VALOR_CUOTA_TIPO_I + "_" + emisor.IdEmisor).collect(Collectors.toList());
		params.put("IdCodigoTipo", codigosTipo);
		List<Cabecera> cabeceras = sql.query(conn, SQL_SELECT_CABECERA, params, Cabecera.class);
		logger.info("FIN::ValorCuotaInternacionalFileManager.getCabeceras");
		return cabeceras;
		
	}
	
	private List<Detalle> getDetalles(Connection conn, List<Cabecera> cabeceras) throws SQLException{
		logger.info("INICIO::ValorCuotaInternacionalFileManager.getDetalles");
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		List<Long> idCabeceras = cabeceras.stream().map(cabecera -> cabecera.IdCabecera).collect(Collectors.toList());
		params.clear();
		params.put("IdCabecera", idCabeceras);
		List<Detalle> detalles = sql.query(conn, SQL_SELECT_DETALLE, params, Detalle.class);
		logger.info("FIN::ValorCuotaInternacionalFileManager.getDetalles");
		return detalles;
	}
	
	private List<IdSerieFondo> getSeriesFondo(Connection conn, List<Detalle> detalles) throws SQLException{
		logger.info("INICIO::ValorCuotaInternacionalFileManager.getSeriesFondo");
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		List<String> isins = detalles.parallelStream().filter(detalle->esLineaValida(detalle))
		.map(detalle -> {
			return calcularISIN(detalle);
		}).collect(Collectors.toList());
				
		params.put("isin", isins);
		List<IdSerieFondo> series = sql.query(conn, SQL_SELECT_IDSERIEFONDO, params, IdSerieFondo.class);
		logger.info("FIN::ValorCuotaInternacionalFileManager.getSeriesFondo");
		return series;
	}
	
	private Boolean esLineaValida(Detalle detalle) {
		logger.info("INICIO::ValorCuotaInternacionalFileManager.esLineaValida");
		List<String> lineaList = new ArrayList<String>(Arrays.asList(detalle.Linea.split(",")));
		Boolean esLineaValida = null != lineaList && !CABECERA_ISIN.equals(lineaList.get(0)) && lineaList.size() == 6;
		logger.info("FIN::ValorCuotaInternacionalFileManager.esLineaValida");
		return esLineaValida;
	}
	
	private String calcularISIN(Detalle detalle) {
		logger.info("INICIO::ValorCuotaInternacionalFileManager.calcularISIN");
		List<String> lineaList = new ArrayList<String>(Arrays.asList(detalle.Linea.split(",")));
		String isin = lineaList.get(0);
		isin = (null != isin) ? isin.trim().replaceFirst ("^0*", "") : "";
		logger.info("FIN::ValorCuotaInternacionalFileManager.calcularISIN");
		return isin;
	}
	
	private CargarValorCuota.SalidaCargarValorCuota cargarValorCuota(Connection con, EntradaValorCuotaInternacionalFileManager entrada, List<Detalle> detalles, List<IdSerieFondo> series) throws Exception {
		logger.info("INICIO::ValorCuotaInternacionalFileManager.cargarValorCuota");
		CargarValorCuota cargarValorCuota = new CargarValorCuota();
		CargarValorCuota.EntradaCargarValorCuota entradaCargarValorCuota =  new CargarValorCuota.EntradaCargarValorCuota();
		entradaCargarValorCuota.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCargarValorCuota.p_IdUsuario = entrada.p_IdUsuario;
		entradaCargarValorCuota.p_IdModulo = _c.ModuloFondoMutuo;
		entradaCargarValorCuota.p_UsaReglasNegocio = _c.NO;
		entradaCargarValorCuota.p_SimularOperacion = _c.SI;
		
		entradaCargarValorCuota.p_ValorCuota = detalles.parallelStream().filter(detalle->esLineaValida(detalle))
		.filter(detalle->{
			String isin = calcularISIN(detalle);
			return series.parallelStream().filter(serie-> serie.ISIN.equals(isin)).collect(Collectors.toList()).size() > 0;
		})
		.map(detalle->{
			List<String> lineaList = new ArrayList<String>(Arrays.asList(detalle.Linea.split(",")));
			String isin = calcularISIN(detalle);
			IdSerieFondo serieFondo = series.parallelStream().filter(serie-> serie.ISIN.equals(isin)).collect(Collectors.toList()).get(0);
			CargarValorCuota.ValorCuota inValorCuota = new CargarValorCuota.ValorCuota();
			inValorCuota.IDSERIEFONDO = serieFondo.IdSerieFondo;
			inValorCuota.VALORCUOTA = new BigDecimal(lineaList.get(4));
			inValorCuota.FECHACIERRE = getFechaCierre(lineaList, 3);
			inValorCuota.IDMONEDA = serieFondo.IdMoneda;
			inValorCuota.OBSERVACION = "";
			inValorCuota.MENSAJEVALIDACION = "OK";
			return inValorCuota;
		}).collect(Collectors.toList());
		
		CargarValorCuota.SalidaCargarValorCuota salidaCargarValorCuota = cargarValorCuota.logica(con, entradaCargarValorCuota);
		logger.info("FIN::ValorCuotaInternacionalFileManager.cargarValorCuota");
		return salidaCargarValorCuota;
	}
	
	private String getFechaCierre(List<String> lineaList, int position) {
		logger.info("INICIO::ValorCuotaInternacionalFileManager.getFechaCierre");
		String fechaCierre = "";
		fechaCierre = lineaList.get(position);
		List<String> fechaList = new ArrayList<String>(Arrays.asList(fechaCierre.split(" ")));
		String fecha = fechaList.get(0);
		String dia = fecha.substring(0, 2);
		String mes = fecha.substring(3, 5);
		String anio = fecha.substring(6, fecha.length());
		String hora = fechaList.get(1);
		fechaCierre = anio + "-" + mes + "-" + dia + " " + hora;
		logger.info("FIN::ValorCuotaInternacionalFileManager.getFechaCierre");
		return fechaCierre;
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
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
			String jsonRF = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21}";
					
			EntradaValorCuotaInternacionalFileManager Objin = gson.fromJson(jsonRF, EntradaValorCuotaInternacionalFileManager.class);
			
			ValorCuotaInternacionalFileManager  demo= new ValorCuotaInternacionalFileManager();				
 			
 			try {
 				SalidaValorCuotaInternacionalFileManager ObjOut=demo.logica(con, Objin);
 				//con.rollback();
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
