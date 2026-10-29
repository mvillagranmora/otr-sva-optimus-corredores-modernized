package cl.bcs.mesa.MRF;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.mesa.MRF.CrearOperacionesTerminoCargaMasiva.EntradaCrearOperacionesTerminoCargaMasiva;
import cl.bcs.mesa.MRF.CrearOperacionesTerminoCargaMasiva.SalidaCrearOperacionesTerminoCargaMasiva;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ProcesarCargaMasivaArchivoOperacionTermino implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ProcesarCargaMasivaArchivoOperacionTermino.class);
	
	private final String SQL_SELECT_OPERACION_CABECERA = loadResourceToString("cl/bcs/mesa/MRF/sql/ProcesarCargaMasivaArchivoOperacionTermino_SelectOperacionCabecera.sql");
	private final String SQL_SELECT_OPERACION_DETALLE = loadResourceToString("cl/bcs/mesa/MRF/sql/ProcesarCargaMasivaArchivoOperacionTermino_SelectOperacionDetalle.sql");
	private final String SQL_SELECT_CARTERAS = loadResourceToString("cl/bcs/mesa/MRF/sql/ProcesarCargaMasivaArchivoOperacionTermino_SelectCarteras.sql");
	
	public static class EntradaProcesarCargaMasivaArchivoOperacionTermino extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_SimularOperacion;
		public List<OperacionTermino> p_Operaciones;
		
		public EntradaProcesarCargaMasivaArchivoOperacionTermino() {
			this.p_Operaciones = new ArrayList<OperacionTermino>();
		}
		
	}

	public static class OperacionTermino extends BcsPojo {
		public Long IdCargaMasivaDetalle;
	}
	
	public static class OperacionTerminoCabecera extends BcsPojo {
		public Long IdCargaMasivaDetalle;
		public Long IdPortafolio;
		public String TipoOperacion;
		public String TipoReajuste;
		public BigDecimal ValorInicial;
		public String CondicionLiquidacion;
	}
	
	public static class OperacionTerminoDetalle extends BcsPojo {
		public Long IdCargaMasivaDetalle;
		public Long IdCargaMasivaDetalleCabecera;
		public String TipoOperacion;
		public String CondicionLiquidacion;
		public String TipoReajuste;
		public BigDecimal ValorInicial;
		public String Mercado;
		public Long SecuenciaCartera;
		public String Nemotecnico;
		public String Emisor;
		public String Boveda;
		public String ReajusteCartera;
		public String MonedaLiquidacion;
		public String FechaVencimiento;
		public BigDecimal Cantidad;
		public BigDecimal ValorRescate;
		public String Fungible;
		public BigDecimal Monto;
		public Long IdPortafolio;
		public BigDecimal Tasa;
	}
	
	public static class Cartera extends BcsPojo {
		public Long FolioCartera; 
	}
	
	public static class SalidaProcesarCargaMasivaArchivoOperacionTermino extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaProcesarCargaMasivaArchivoOperacionTermino() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaProcesarCargaMasivaArchivoOperacionTermino jin = gson.fromJson(in.toString(), EntradaProcesarCargaMasivaArchivoOperacionTermino.class);
		SalidaProcesarCargaMasivaArchivoOperacionTermino out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaProcesarCargaMasivaArchivoOperacionTermino logica(Connection con, EntradaProcesarCargaMasivaArchivoOperacionTermino entrada) throws Exception{
		logger.info("INICIO::ProcesarCargaMasivaArchivoOperacionTermino");
		SalidaProcesarCargaMasivaArchivoOperacionTermino out = new SalidaProcesarCargaMasivaArchivoOperacionTermino();
		
		out = validarCabecera(con, entrada);
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		List<OperacionTerminoDetalle> operacionesDetalle = obtenerOperacionDetalle(con, entrada.p_Operaciones);
		
		out = validarDetalle(con, operacionesDetalle, entrada.p_IdEmpresa);
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		SalidaCrearOperacionesTerminoCargaMasiva salidaCrearOperacionesTerminoCargaMasiva = crearOperaciones(con, entrada);		
		out.P_OUT_MENSAJES.addAll(salidaCrearOperacionesTerminoCargaMasiva.P_OUT_MENSAJES);
		out.P_OUT_ERRORESCONTADOR = salidaCrearOperacionesTerminoCargaMasiva.P_OUT_ERRORESCONTADOR;
		
		logger.info("FIN::ProcesarCargaMasivaArchivoOperacionTermino");
		return out;
	}
	
	private SalidaProcesarCargaMasivaArchivoOperacionTermino validarCabecera(Connection con, EntradaProcesarCargaMasivaArchivoOperacionTermino entrada) throws Exception{
		logger.trace("INICIO::ProcesarCargaMasivaArchivoOperacionTermino.validarCabecera");
		SalidaProcesarCargaMasivaArchivoOperacionTermino out = new SalidaProcesarCargaMasivaArchivoOperacionTermino();
		
		List<Long> idOperaciones = entrada.p_Operaciones
				.stream()
				.map(operacion->operacion.IdCargaMasivaDetalle)
				.collect(Collectors.toList());
		HashMap<String, Object> parametros = new HashMap<String, Object>(2);
		parametros.put("IdCargaMasivaDetalle", idOperaciones);
		
		SQL sql = new SQL();
		List<OperacionTerminoCabecera> operacionesEncontradas = sql.query(con, SQL_SELECT_OPERACION_CABECERA, parametros, OperacionTerminoCabecera.class);
		Map<Long, OperacionTerminoCabecera> operacionesEncontradasPorId = operacionesEncontradas
				.stream()
				.collect(Collectors.toMap(operacion->operacion.IdCargaMasivaDetalle, operacion->operacion));
		
		for(OperacionTermino operacion : entrada.p_Operaciones) {
			OperacionTerminoCabecera operacionEncontrada = operacionesEncontradasPorId.get(operacion.IdCargaMasivaDetalle);
			if(operacionEncontrada == null) {
				HashMap<String,String> parametrosMensaje = new HashMap<String,String>(2);
				parametrosMensaje.put("IdCargaMasivaDetalle", String.valueOf(operacion.IdCargaMasivaDetalle));
				out.P_OUT_MENSAJES.add(new MensajeSalida(operacion.IdCargaMasivaDetalle,MensajeSalida.MENSAJE_ERROR,"OPERACION_TERMINO_CARGA_NO_ENCONTRADA",parametrosMensaje,con,entrada.p_IdEmpresa));
				continue;
			}
			if(operacionEncontrada.IdPortafolio == 0) {
				HashMap<String,String> parametrosMensaje = new HashMap<String,String>(2);
				parametrosMensaje.put("IdCargaMasivaDetalle", String.valueOf(operacion.IdCargaMasivaDetalle));
				out.P_OUT_MENSAJES.add(new MensajeSalida(operacion.IdCargaMasivaDetalle,MensajeSalida.MENSAJE_ERROR,"OPERACION_TERMINO_CARGA_NO_EXISTE_CLIENTE",parametrosMensaje,con,entrada.p_IdEmpresa));
			}
			if(operacionEncontrada.TipoOperacion.isEmpty()) {
				HashMap<String,String> parametrosMensaje = new HashMap<String,String>(2);
				parametrosMensaje.put("IdCargaMasivaDetalle", String.valueOf(operacion.IdCargaMasivaDetalle));
				out.P_OUT_MENSAJES.add(new MensajeSalida(operacion.IdCargaMasivaDetalle,MensajeSalida.MENSAJE_ERROR,"OPERACION_TERMINO_CARGA_TIPO_OPERACION_INVALIDA",parametrosMensaje,con,entrada.p_IdEmpresa));
			}
			if(operacionEncontrada.TipoReajuste.isEmpty()) {
				HashMap<String,String> parametrosMensaje = new HashMap<String,String>(2);
				parametrosMensaje.put("IdCargaMasivaDetalle", String.valueOf(operacion.IdCargaMasivaDetalle));
				out.P_OUT_MENSAJES.add(new MensajeSalida(operacion.IdCargaMasivaDetalle,MensajeSalida.MENSAJE_ERROR,"OPERACION_TERMINO_CARGA_TIPO_REAJUSTE_INVALIDO",parametrosMensaje,con,entrada.p_IdEmpresa));
			}
			if(operacionEncontrada.CondicionLiquidacion.isEmpty()) {
				HashMap<String,String> parametrosMensaje = new HashMap<String,String>(2);
				parametrosMensaje.put("IdCargaMasivaDetalle", String.valueOf(operacion.IdCargaMasivaDetalle));
				out.P_OUT_MENSAJES.add(new MensajeSalida(operacion.IdCargaMasivaDetalle,MensajeSalida.MENSAJE_ERROR,"OPERACION_TERMINO_CARGA_CONDICION_LIQUIDACION_INVALIDA",parametrosMensaje,con,entrada.p_IdEmpresa));
			}
		}
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		logger.trace("FIN::ProcesarCargaMasivaArchivoOperacionTermino.validarCabecera");
		return out;
	}
	
	private List<OperacionTerminoDetalle> obtenerOperacionDetalle(Connection con, List<OperacionTermino> operaciones) throws Exception {
		logger.trace("INICIO::ProcesarCargaMasivaArchivoOperacionTermino.obtenerOperacionDetalle");
		
		List<Long> idOperaciones = operaciones
				.stream()
				.map(operacion->operacion.IdCargaMasivaDetalle)
				.collect(Collectors.toList());
		HashMap<String, Object> parametros = new HashMap<String, Object>(2);
		parametros.put("IdCargaMasivaDetalle", idOperaciones);
		
		SQL sql = new SQL();
		List<OperacionTerminoDetalle> operacionesDetalle = sql.query(con, SQL_SELECT_OPERACION_DETALLE, parametros, OperacionTerminoDetalle.class);
		
		logger.trace("FIN::ProcesarCargaMasivaArchivoOperacionTermino.obtenerOperacionDetalle");
		return operacionesDetalle;
	}
	
	private SalidaProcesarCargaMasivaArchivoOperacionTermino validarDetalle(Connection con, List<OperacionTerminoDetalle> operaciones, Long idEmpresa) throws Exception {
		logger.trace("INICIO::ProcesarCargaMasivaArchivoOperacionTermino.validarDetalle");
		
		SalidaProcesarCargaMasivaArchivoOperacionTermino out = new SalidaProcesarCargaMasivaArchivoOperacionTermino();
		
		List<Cartera> carteras = obtenerCarteras(con, operaciones);
		Map<Long, Cartera> carterasPorFolioCartera = carteras
				.stream()
				.collect(Collectors.toMap(cartera->cartera.FolioCartera, cartera->cartera));
				
		for(OperacionTerminoDetalle operacionDetalle : operaciones) {
			if(operacionDetalle.SecuenciaCartera > 0) {
				Cartera cartera = carterasPorFolioCartera.get(operacionDetalle.SecuenciaCartera);
				if(cartera == null) {
					HashMap<String,String> parametrosMensaje = new HashMap<String,String>(3);
					parametrosMensaje.put("IdCargaMasivaDetalle", String.valueOf(operacionDetalle.IdCargaMasivaDetalleCabecera));
					parametrosMensaje.put("FolioCartera", String.valueOf(operacionDetalle.SecuenciaCartera));
					out.P_OUT_MENSAJES.add(new MensajeSalida(operacionDetalle.IdCargaMasivaDetalleCabecera,MensajeSalida.MENSAJE_ERROR,"OPERACION_TERMINO_CARGA_CARTERA_NO_ENCONTRADA",parametrosMensaje,con,idEmpresa));
					continue;
				}
			}
			if("RF".equals(operacionDetalle.Mercado)) {
				List<MensajeSalida> mensajes = validarOperacionRF(con, operacionDetalle, idEmpresa);
				out.P_OUT_MENSAJES.addAll(mensajes);
				continue;
			}
			if("IF".equals(operacionDetalle.Mercado)) {
				List<MensajeSalida> mensajes = validarOperacionIF(con, operacionDetalle, idEmpresa);
				out.P_OUT_MENSAJES.addAll(mensajes);
				continue;
			}
		}
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		logger.trace("FIN::ProcesarCargaMasivaArchivoOperacionTermino.validarDetalle");
		return out;
	}
		
	private List<Cartera> obtenerCarteras(Connection con, List<OperacionTerminoDetalle> operaciones) throws Exception {
		logger.trace("INICIO::ProcesarCargaMasivaArchivoOperacionTermino.obtenerCarteras");
		
		List<Long> secuenciasCartera = operaciones
				.stream()
				.filter(operacion->operacion.SecuenciaCartera > 0)
				.map(operacion->operacion.SecuenciaCartera)
				.distinct()
				.collect(Collectors.toList());
		HashMap<String, Object> parametros = new HashMap<String, Object>(2);
		parametros.put("SecuenciaCartera", secuenciasCartera);
		
		SQL sql = new SQL();
		List<Cartera> carteras = sql.query(con, SQL_SELECT_CARTERAS, parametros, Cartera.class);
		
		logger.trace("FIN::ProcesarCargaMasivaArchivoOperacionTermino.obtenerCarteras");
		return carteras;
	}
	
	private List<MensajeSalida> validarOperacionRF(Connection con, OperacionTerminoDetalle operacionDetalle, Long idEmpresa) throws Exception {
		logger.trace("INICIO::ProcesarCargaMasivaArchivoOperacionTermino.validarOperacionRF");
		List<MensajeSalida> mensajes = new ArrayList<MensajeSalida>();
		if(operacionDetalle.SecuenciaCartera == 0 && operacionDetalle.Nemotecnico.isEmpty()) {
			HashMap<String,String> parametrosMensaje = new HashMap<String,String>(2);
			parametrosMensaje.put("IdCargaMasivaDetalle", String.valueOf(operacionDetalle.IdCargaMasivaDetalle));
			mensajes.add(new MensajeSalida(operacionDetalle.IdCargaMasivaDetalleCabecera,MensajeSalida.MENSAJE_ERROR,"OPERACION_TERMINO_CARGA_NEMOTECNICO_INVALIDA",parametrosMensaje,con,idEmpresa));
		}
		if(operacionDetalle.SecuenciaCartera == 0 && operacionDetalle.Boveda.isEmpty()) {
			HashMap<String,String> parametrosMensaje = new HashMap<String,String>(2);
			parametrosMensaje.put("IdCargaMasivaDetalle", String.valueOf(operacionDetalle.IdCargaMasivaDetalle));
			mensajes.add(new MensajeSalida(operacionDetalle.IdCargaMasivaDetalleCabecera,MensajeSalida.MENSAJE_ERROR,"OPERACION_TERMINO_CARGA_BOVEDA_INVALIDA",parametrosMensaje,con,idEmpresa));
		}
		if(operacionDetalle.Cantidad.compareTo(BigDecimal.ZERO) != 1 && operacionDetalle.Monto.compareTo(BigDecimal.ZERO) != 1) {
			HashMap<String,String> parametrosMensaje = new HashMap<String,String>(2);
			parametrosMensaje.put("IdCargaMasivaDetalle", String.valueOf(operacionDetalle.IdCargaMasivaDetalle));
			mensajes.add(new MensajeSalida(operacionDetalle.IdCargaMasivaDetalleCabecera,MensajeSalida.MENSAJE_ERROR,"OPERACION_TERMINO_CARGA_CANTIDAD_MONTO_INVALIDA",parametrosMensaje,con,idEmpresa));
		}
		
		logger.trace("FIN::ProcesarCargaMasivaArchivoOperacionTermino.validarOperacionRF");
		return mensajes;
	}
	
	private List<MensajeSalida> validarOperacionIF(Connection con, OperacionTerminoDetalle operacionDetalle, Long idEmpresa) throws Exception {
		logger.trace("INICIO::ProcesarCargaMasivaArchivoOperacionTermino.validarOperacionIF");
		List<MensajeSalida> mensajes = new ArrayList<MensajeSalida>();
		if(operacionDetalle.SecuenciaCartera == 0 && operacionDetalle.Nemotecnico.isEmpty()) {
			HashMap<String,String> parametrosMensaje = new HashMap<String,String>(2);
			parametrosMensaje.put("IdCargaMasivaDetalle", String.valueOf(operacionDetalle.IdCargaMasivaDetalle));
			mensajes.add(new MensajeSalida(operacionDetalle.IdCargaMasivaDetalleCabecera,MensajeSalida.MENSAJE_ERROR,"OPERACION_TERMINO_CARGA_NEMOTECNICO_INVALIDA",parametrosMensaje,con,idEmpresa));
		}
		if(operacionDetalle.SecuenciaCartera == 0 && operacionDetalle.Boveda.isEmpty()) {
			HashMap<String,String> parametrosMensaje = new HashMap<String,String>(2);
			parametrosMensaje.put("IdCargaMasivaDetalle", String.valueOf(operacionDetalle.IdCargaMasivaDetalle));
			mensajes.add(new MensajeSalida(operacionDetalle.IdCargaMasivaDetalleCabecera,MensajeSalida.MENSAJE_ERROR,"OPERACION_TERMINO_CARGA_BOVEDA_INVALIDA",parametrosMensaje,con,idEmpresa));
		}
		if(operacionDetalle.SecuenciaCartera == 0 && operacionDetalle.Emisor.isEmpty() && ("PAGARE R".equals(operacionDetalle.Nemotecnico) || "PAGARE NR".equals(operacionDetalle.Nemotecnico))) {
			HashMap<String,String> parametrosMensaje = new HashMap<String,String>(2);
			parametrosMensaje.put("IdCargaMasivaDetalle", String.valueOf(operacionDetalle.IdCargaMasivaDetalle));
			mensajes.add(new MensajeSalida(operacionDetalle.IdCargaMasivaDetalleCabecera,MensajeSalida.MENSAJE_ERROR,"OPERACION_TERMINO_CARGA_EMISOR_INVALIDO",parametrosMensaje,con,idEmpresa));
		}
		if(operacionDetalle.SecuenciaCartera == 0 && operacionDetalle.TipoReajuste.isEmpty() && "PAGARE R".equals(operacionDetalle.Nemotecnico)) {
			HashMap<String,String> parametrosMensaje = new HashMap<String,String>(2);
			parametrosMensaje.put("IdCargaMasivaDetalle", String.valueOf(operacionDetalle.IdCargaMasivaDetalle));
			mensajes.add(new MensajeSalida(operacionDetalle.IdCargaMasivaDetalleCabecera,MensajeSalida.MENSAJE_ERROR,"OPERACION_TERMINO_CARGA_TIPO_REAJUSTE_INVALIDO",parametrosMensaje,con,idEmpresa));
		}
		if(operacionDetalle.SecuenciaCartera == 0 && operacionDetalle.MonedaLiquidacion.isEmpty() && "PAGARE R".equals(operacionDetalle.Nemotecnico)) {
			HashMap<String,String> parametrosMensaje = new HashMap<String,String>(2);
			parametrosMensaje.put("IdCargaMasivaDetalle", String.valueOf(operacionDetalle.IdCargaMasivaDetalle));
			mensajes.add(new MensajeSalida(operacionDetalle.IdCargaMasivaDetalleCabecera,MensajeSalida.MENSAJE_ERROR,"OPERACION_TERMINO_CARGA_MONEDA_LIQUIDACION_INVALIDA",parametrosMensaje,con,idEmpresa));
		}
		if(operacionDetalle.SecuenciaCartera == 0 && "0001-01-01".equals(operacionDetalle.FechaVencimiento)) {
			HashMap<String,String> parametrosMensaje = new HashMap<String,String>(2);
			parametrosMensaje.put("IdCargaMasivaDetalle", String.valueOf(operacionDetalle.IdCargaMasivaDetalle));
			mensajes.add(new MensajeSalida(operacionDetalle.IdCargaMasivaDetalleCabecera,MensajeSalida.MENSAJE_ERROR,"OPERACION_TERMINO_CARGA_FECHA_VENCIMIENTO_INVALIDA",parametrosMensaje,con,idEmpresa));
		}
		if(operacionDetalle.Cantidad.compareTo(BigDecimal.ZERO) != 1 && "N".equals(operacionDetalle.Fungible)) {
			HashMap<String,String> parametrosMensaje = new HashMap<String,String>(2);
			parametrosMensaje.put("IdCargaMasivaDetalle", String.valueOf(operacionDetalle.IdCargaMasivaDetalle));
			mensajes.add(new MensajeSalida(operacionDetalle.IdCargaMasivaDetalleCabecera,MensajeSalida.MENSAJE_ERROR,"OPERACION_TERMINO_CARGA_CANTIDAD_INVALIDA",parametrosMensaje,con,idEmpresa));
		}
		if(operacionDetalle.ValorRescate.compareTo(BigDecimal.ZERO) != 1) {
			HashMap<String,String> parametrosMensaje = new HashMap<String,String>(2);
			parametrosMensaje.put("IdCargaMasivaDetalle", String.valueOf(operacionDetalle.IdCargaMasivaDetalle));
			mensajes.add(new MensajeSalida(operacionDetalle.IdCargaMasivaDetalleCabecera,MensajeSalida.MENSAJE_ERROR,"OPERACION_TERMINO_CARGA_VALOR_RESCATE_INVALIDA",parametrosMensaje,con,idEmpresa));
		}
		if(operacionDetalle.Monto.compareTo(BigDecimal.ZERO) != 1) {
			HashMap<String,String> parametrosMensaje = new HashMap<String,String>(2);
			parametrosMensaje.put("IdCargaMasivaDetalle", String.valueOf(operacionDetalle.IdCargaMasivaDetalle));
			mensajes.add(new MensajeSalida(operacionDetalle.IdCargaMasivaDetalleCabecera,MensajeSalida.MENSAJE_ERROR,"OPERACION_TERMINO_CARGA_MONTO_INVALIDA",parametrosMensaje,con,idEmpresa));
		}
		
		logger.trace("FIN::ProcesarCargaMasivaArchivoOperacionTermino.validarOperacionIF");
		return mensajes;
	}
	
	private SalidaCrearOperacionesTerminoCargaMasiva crearOperaciones(Connection con, EntradaProcesarCargaMasivaArchivoOperacionTermino entrada) throws Exception {
		logger.trace("INICIO::ProcesarCargaMasivaArchivoOperacionTermino.crearOperaciones");
		EntradaCrearOperacionesTerminoCargaMasiva entradaCrearOperacionesTerminoCargaMasiva = new EntradaCrearOperacionesTerminoCargaMasiva();
		entradaCrearOperacionesTerminoCargaMasiva.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearOperacionesTerminoCargaMasiva.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearOperacionesTerminoCargaMasiva.p_IdModulo = entrada.p_IdModulo;
		entradaCrearOperacionesTerminoCargaMasiva.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaCrearOperacionesTerminoCargaMasiva.p_SimularOperacion = entrada.p_SimularOperacion;
		entradaCrearOperacionesTerminoCargaMasiva.p_Operaciones = entrada.p_Operaciones
				.stream()
				.map(operacion->{
					CrearOperacionesTerminoCargaMasiva.OperacionTermino operacionTermino = new CrearOperacionesTerminoCargaMasiva.OperacionTermino();
					operacionTermino.IdCargaMasivaDetalle = operacion.IdCargaMasivaDetalle;
					return operacionTermino;
				})
				.collect(Collectors.toList());
		
		CrearOperacionesTerminoCargaMasiva crearOperacionesTerminoCargaMasiva = new CrearOperacionesTerminoCargaMasiva();
		SalidaCrearOperacionesTerminoCargaMasiva salidaCrearOperacionesTerminoCargaMasiva = crearOperacionesTerminoCargaMasiva.logica(con, entradaCrearOperacionesTerminoCargaMasiva);
		
		logger.trace("FIN::ProcesarCargaMasivaArchivoOperacionTermino.crearOperaciones");
		return salidaCrearOperacionesTerminoCargaMasiva;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		Gson gson = Utils.getGsonBuilder();			
		
		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"MRF\",\"p_UsaReglasNegocio\":\"S\",\"p_SimularOperacion\":\"N\",\"p_Operaciones\":[{\"IdCargaMasivaDetalle\":245}]}";
		EntradaProcesarCargaMasivaArchivoOperacionTermino Objin = gson.fromJson(json, EntradaProcesarCargaMasivaArchivoOperacionTermino.class);
		ProcesarCargaMasivaArchivoOperacionTermino demo = new ProcesarCargaMasivaArchivoOperacionTermino();				
		try {
			SalidaProcesarCargaMasivaArchivoOperacionTermino ObjOut = demo.logica(con, Objin);
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
