package cl.bcs.custodia;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.CargaCustodiaCircular1518Utils.CuentaCustodia;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CargarSorteoLetrasArchivoL043Circular1518 implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CargarPosicionesArchivoL02Circular1518.class);

	private final String SQL_SELECT_CUADRATURA = loadResourceToString("cl/bcs/custodia/sql/CargarSorteoLetrasArchivoL043Circular1518_SelectCuadratura.sql");
	private final String SQL_UPDATE_CUADRATURA = loadResourceToString("cl/bcs/custodia/sql/CargarSorteoLetrasArchivoL043Circular1518_UpdateCuadratura.sql");
	
	public static class EntradaSorteoLetrasL043Circular1518 extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_FechaCuadratura;
		public List<Sorteo> p_DatosArchivo;
		
		public EntradaSorteoLetrasL043Circular1518() {
			this.p_DatosArchivo = new ArrayList<>();
		}
	}
	
	public static class Cuadratura{
		public String Nemotecnico;
		public BigDecimal CantidadSorteoLetras; 
	}
	
	public static class Circular1518 extends Cuadratura{
		public Long IdCuadraturaCircular;
		public Date FechaCuadratura;
		public BigDecimal CantidadDCVCuentaRelacionado;
		public BigDecimal CantidadDisponibleDCVCuentaRelacionado;
		public BigDecimal CantidadDCVCuentaPropia;
		public BigDecimal CantidadDisponibleDCVCuentaPropia;
		public BigDecimal CantidadDCVCuentaTercero;
		public BigDecimal CantidadDisponibleDCVCuentaTercero;
	}
	
	public static class Sorteo extends Cuadratura{
		public String Cuenta;
		public String TipoInstrumento;
		public String Unidad;
		public String FechaArchivo;
		public String Validacion;

		public Sorteo() {
			this.CantidadSorteoLetras = BigDecimal.ZERO;
		}
	}
	
	public static class SalidaSorteoLetrasL043Circular1518 extends BcsPojo {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long	P_OUT_ERRORESCONTADOR;
		
		public SalidaSorteoLetrasL043Circular1518() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<>();
		}
	}

	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaSorteoLetrasL043Circular1518 in = gson.fromJson(jin.toString(), EntradaSorteoLetrasL043Circular1518.class);
		SalidaSorteoLetrasL043Circular1518 out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaSorteoLetrasL043Circular1518 logica(Connection con, EntradaSorteoLetrasL043Circular1518 entrada) throws Exception {
		logger.info("INICIO::CargarSorteoLetrasArchivoL043Circular1518");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaSorteoLetrasL043Circular1518 out = new SalidaSorteoLetrasL043Circular1518();
		
		if(entrada.p_DatosArchivo.size() == 0) {
			return out;
		}
		
		List<String> listaNemotecnicoEntrada = entrada.p_DatosArchivo.stream().filter(sorteo->sorteo.Nemotecnico != null && !(sorteo.Nemotecnico.trim().length() == 0))
																			.map(sorteo->sorteo.Nemotecnico)
																			.distinct()
																			.collect(Collectors.toList());
		
		HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("FechaCuadratura", entrada.p_FechaCuadratura);
		params.put("Nemotecnico",listaNemotecnicoEntrada);
		List<Circular1518> cuadraturaCircu1518 = obtenerCuadraturaPorNemotecnico(con, params);
		if(cuadraturaCircu1518.size() == 0) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "SIN_REGISTROS_ASOCIADOS_CUADRATURA", new HashMap<String, String>(), con, entrada.p_IdEmpresa));
			return out;
		}

		try{
			List<Circular1518> cuadraturaActualizada = obtenerCuadraturaActualizadaPorNemo(con, entrada.p_IdEmpresa, entrada.p_DatosArchivo, cuadraturaCircu1518);
			
			actualizarCuadratura(con, cuadraturaActualizada);
			
		}catch(Exception e) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_PROCESO_L043", new HashMap<String, String>(), con, entrada.p_IdEmpresa));
			logger.error(e);
		}
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CargarSorteoLetrasArchivoL043Circular1518", finServicio - inicioServicio);
		return out;
	}
	
	public List<Circular1518> obtenerCuadraturaPorNemotecnico(Connection con, HashMap<String, Object> parametros) throws Exception{
		logger.info("INICIO::CargarSorteoLetrasArchivoL043Circular1518.obtenerCuadraturaPorNemotecnico");
		long inicioServicio = System.currentTimeMillis();
		
		List<Circular1518> listadoBD = new ArrayList<Circular1518>();
		try {
			SQL sql = new SQL();
			listadoBD = sql.query(con, SQL_SELECT_CUADRATURA, parametros, Circular1518.class);
			
		}catch(Exception e) {
			logger.error(e);
		}
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CargarSorteoLetrasArchivoL043Circular1518.obtenerCuadraturaPorNemotecnico", finServicio - inicioServicio);
		return listadoBD;
	}
	
	
	private List<Circular1518> obtenerCuadraturaActualizadaPorNemo(Connection con, Long idEmpresa, List<Sorteo> cargaSorteo, List<Circular1518> listaCuadratura) throws Exception {
		logger.debug("INICIO::CargarSorteoLetrasArchivoL043Circular1518.obtenerCuadraturaActualizadaPorNemo");
		long inicio = System.currentTimeMillis();
		
		CuentaCustodia cuentaCustodia = CargaCustodiaCircular1518Utils.obtenerCuentaCustodia(con, idEmpresa);
		
		Map<String, Circular1518> CuadraturaPorNemo = listaCuadratura.stream().collect(Collectors.toMap(cuadratura->cuadratura.Nemotecnico, cuadratura->cuadratura));
		Map<String, List<Sorteo>> SorteoAgrupadasPorNemo = cargaSorteo.stream().collect(Collectors.groupingBy(sorteo->sorteo.Nemotecnico));
		
		List<Circular1518> cuadraturasActualizada = new ArrayList<Circular1518>();
		
		for(Entry<String, List<Sorteo>> sorteoNemo : SorteoAgrupadasPorNemo.entrySet()) {
			Circular1518 cuadrCircular = CuadraturaPorNemo.get(sorteoNemo.getKey());
			if(cuadrCircular != null) {
				for(Sorteo sorteo : sorteoNemo.getValue()) {
					if(sorteo.Cuenta !=null) {
						if(cuentaCustodia.CuentaRelacionadoDCV.equals(sorteo.Cuenta)) {
							cuadrCircular.CantidadDCVCuentaRelacionado = cuadrCircular.CantidadDCVCuentaRelacionado.subtract(sorteo.CantidadSorteoLetras, MathContext.DECIMAL128);
							cuadrCircular.CantidadDisponibleDCVCuentaRelacionado = cuadrCircular.CantidadDisponibleDCVCuentaRelacionado.subtract(sorteo.CantidadSorteoLetras, MathContext.DECIMAL128);
						}
						else if(cuentaCustodia.CuentaPropiaDCV.equals(sorteo.Cuenta)) {
							cuadrCircular.CantidadDCVCuentaPropia = cuadrCircular.CantidadDCVCuentaPropia.subtract(sorteo.CantidadSorteoLetras, MathContext.DECIMAL128);
							cuadrCircular.CantidadDisponibleDCVCuentaPropia = cuadrCircular.CantidadDisponibleDCVCuentaPropia.subtract(sorteo.CantidadSorteoLetras, MathContext.DECIMAL128);
						}
						else {
							cuadrCircular.CantidadDCVCuentaTercero = cuadrCircular.CantidadDCVCuentaTercero.subtract(sorteo.CantidadSorteoLetras, MathContext.DECIMAL128);
							cuadrCircular.CantidadDisponibleDCVCuentaTercero = cuadrCircular.CantidadDisponibleDCVCuentaTercero.subtract(sorteo.CantidadSorteoLetras, MathContext.DECIMAL128);
						}
						
						cuadrCircular.CantidadSorteoLetras = cuadrCircular.CantidadSorteoLetras.add(sorteo.CantidadSorteoLetras);
					}	
				}
				cuadraturasActualizada.add(cuadrCircular);
			}
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::CargarSorteoLetrasArchivoL043Circular1518.obtenerCuadraturaActualizadaPorNemo", fin - inicio);
		return cuadraturasActualizada;
	}
	
	private void actualizarCuadratura(Connection con, List<Circular1518> listaCuadratura) throws Exception {
		
		logger.debug("INICIO::CargarSorteoLetrasArchivoL043Circular1518.actualizarCargaCuadratura");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_CUADRATURA, listaCuadratura, Circular1518.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CargarSorteoLetrasArchivoL043Circular1518.actualizarCargaCuadratura", fin - inicio);
	}

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			
			EntradaSorteoLetrasL043Circular1518 entradaSorteoLetrasL043Circular1518 = new EntradaSorteoLetrasL043Circular1518();
			
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"CUS\",\"p_FechaCuadratura\":\"2022-01-26\",\"p_DatosArchivo\":[{\"Cuenta\":\"12176\",\"Nemotecnico\":\"FDGDG33\",\"Unidad\":\"UF\",\"TipoInstrumento\":\"LH\",\"FechaArchivo\":\"20220701\",\"CantidadSorteoLetras\":30,\"Validacion\":\"OK\"},{\"Cuenta\":\"12176\",\"Nemotecnico\":\"EST2170110\",\"Unidad\":\"UF\",\"TipoInstrumento\":\"LH\",\"FechaArchivo\":\"20220701\",\"CantidadSorteoLetras\":10,\"Validacion\":\"OK\"},{\"Cuenta\":\"12176\",\"Nemotecnico\":\"EST2050105\",\"Unidad\":\"UF\",\"TipoInstrumento\":\"LH\",\"FechaArchivo\":\"20220701\",\"CantidadSorteoLetras\":20,\"Validacion\":\"OK\"}]}";
			
			Gson gson = Utils.getGsonBuilder();
			entradaSorteoLetrasL043Circular1518 = gson.fromJson(json, EntradaSorteoLetrasL043Circular1518.class);	
			
			CargarSorteoLetrasArchivoL043Circular1518 cargarSorteoLetrasArchivoL043Circular1518 = new CargarSorteoLetrasArchivoL043Circular1518();
			try {
				SalidaSorteoLetrasL043Circular1518 salidaSorteoLetrasL043Circular1518 = cargarSorteoLetrasArchivoL043Circular1518.logica(con, entradaSorteoLetrasL043Circular1518);
				System.out.println(salidaSorteoLetrasL043Circular1518.toString());
				con.commit();
				//con.rollback();
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			} finally {
				con.rollback();
			}

    	}
		System.out.println("== END ==");
	}
	
}