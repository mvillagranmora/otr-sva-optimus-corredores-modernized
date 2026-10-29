package cl.bcs.custodia;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.SQLException;
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
import cl.bcs.custodia.CargaCustodiaCircular1518Utils.Instrumento;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CargarPosicionesArchivoL02Circular1518 implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CargarPosicionesArchivoL02Circular1518.class);

	private final String SQL_SELECT_CUADRATURA = loadResourceToString("cl/bcs/custodia/sql/CargarPosicionesArchivoL02Circular1518_SelectCuadratura.sql");
	private final String SQL_UPDATE_CUADRATURA = loadResourceToString("cl/bcs/custodia/sql/CargarPosicionesArchivoL02Circular1518_UpdateCuadratura.sql");
	private final String SQL_INSERT_CUADRATURA = loadResourceToString("cl/bcs/custodia/sql/CargarPosicionesArchivoL02Circular1518_InsertCuadratura.sql");
	private final String SQL_DELETE_CUADRATURA = loadResourceToString("cl/bcs/custodia/sql/CargarPosicionesArchivoL02Circular1518_DeleteCuadratura.sql");
	
	private final String SQL_SELECT_CUADRATURA1720 = loadResourceToString("cl/bcs/custodia/sql/CargarPosicionesArchivoL02Circular1720_SelectCuadratura.sql");
	private final String SQL_UPDATE_CUADRATURA1720 = loadResourceToString("cl/bcs/custodia/sql/CargarPosicionesArchivoL02Circular1720_UpdateCuadratura.sql");
	private final String SQL_INSERT_CUADRATURA1720 = loadResourceToString("cl/bcs/custodia/sql/CargarPosicionesArchivoL02Circular1720_InsertCuadratura.sql");
	private final String SQL_DELETE_CUADRATURA1720 = loadResourceToString("cl/bcs/custodia/sql/CargarPosicionesArchivoL02Circular1720_DeleteCuadratura.sql");
	
	private final String ORIGEN = "ARCHIVO_L02";
	
	public static class EntradaCargarPosicionesArchivoL02Circular1518 extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_FechaCuadratura;
		public String p_SobreEscribeArchivo;
		public List<Posicion> p_DatosArchivo;
		
		public EntradaCargarPosicionesArchivoL02Circular1518() {
			this.p_SobreEscribeArchivo = "N";
			this.p_DatosArchivo = new ArrayList<>();
		}
	}
	
	public static class Posicion extends BcsPojo {
		public String Cuenta;
		public String Nemotecnico;
		public BigDecimal PosicionDisponible;
		public BigDecimal PosicionBloqueda;
		public BigDecimal PosicionReventa;
		public BigDecimal PosicionRecompra;
		public String Validacion;
		
		public Posicion() {
			this.PosicionDisponible = BigDecimal.ZERO;
			this.PosicionBloqueda = BigDecimal.ZERO;
			this.PosicionReventa = BigDecimal.ZERO;
			this.PosicionRecompra = BigDecimal.ZERO;
		}
	}
	
	public static class SalidaCargarPosicionesArchivoL02Circular1518 extends BcsPojo {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long	P_OUT_ERRORESCONTADOR;
		
		public SalidaCargarPosicionesArchivoL02Circular1518() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<>();
		}
	}
	
	public static class PosicionPorNemo extends BcsPojo {
		public String Nemotecnico;
		public BigDecimal CantidadDCVCuentaRelacionado;
		public BigDecimal CantidadDisponibleDCVCuentaRelacionado;
		public BigDecimal CantidadBloqueadaDCVCuentaRelacionado;
		public BigDecimal CantidadReventaDCVCuentaRelacionado;
		public BigDecimal CantidadRecompraDCVCuentaRelacionado;
		public BigDecimal CantidadValorizadaUFDCVCuentaRelacionado;
		public BigDecimal CantidadDCVCuentaPropia;
		public BigDecimal CantidadDisponibleDCVCuentaPropia;
		public BigDecimal CantidadBloqueadaDCVCuentaPropia;
		public BigDecimal CantidadReventaDCVCuentaPropia;
		public BigDecimal CantidadRecompraDCVCuentaPropia;
		public BigDecimal CantidadValorizadaUFDCVCuentaPropia;
		public BigDecimal CantidadDCVCuentaTercero;
		public BigDecimal CantidadDisponibleDCVCuentaTercero;
		public BigDecimal CantidadBloqueadaDCVCuentaTercero;
		public BigDecimal CantidadReventaDCVCuentaTercero;
		public BigDecimal CantidadRecompraDCVCuentaTercero;
		public BigDecimal CantidadValorizadaUFDCVCuentaTercero;
		public BigDecimal TotalPosicion;
		public BigDecimal CantidadSorteoLetras;
		public BigDecimal CantidadBCS;
		public BigDecimal CantidadBEC;
		public BigDecimal CantidadBOV;
		public BigDecimal CantidadEmisores;
		public String NemoExisteEnOptimus;
		
		public PosicionPorNemo() {
			this.CantidadDCVCuentaRelacionado = BigDecimal.ZERO;
			this.CantidadDisponibleDCVCuentaRelacionado  = BigDecimal.ZERO;
			this.CantidadBloqueadaDCVCuentaRelacionado = BigDecimal.ZERO;
			this.CantidadReventaDCVCuentaRelacionado = BigDecimal.ZERO;
			this.CantidadRecompraDCVCuentaRelacionado = BigDecimal.ZERO;
			this.CantidadValorizadaUFDCVCuentaRelacionado = BigDecimal.ZERO;
			this.CantidadDCVCuentaPropia = BigDecimal.ZERO;
			this.CantidadDisponibleDCVCuentaPropia = BigDecimal.ZERO;
			this.CantidadBloqueadaDCVCuentaPropia = BigDecimal.ZERO;
			this.CantidadReventaDCVCuentaPropia = BigDecimal.ZERO;
			this.CantidadRecompraDCVCuentaPropia = BigDecimal.ZERO;
			this.CantidadValorizadaUFDCVCuentaPropia = BigDecimal.ZERO;
			this.CantidadDCVCuentaTercero = BigDecimal.ZERO;
			this.CantidadDisponibleDCVCuentaTercero = BigDecimal.ZERO;
			this.CantidadBloqueadaDCVCuentaTercero = BigDecimal.ZERO;
			this.CantidadReventaDCVCuentaTercero = BigDecimal.ZERO;
			this.CantidadRecompraDCVCuentaTercero = BigDecimal.ZERO;
			this.CantidadValorizadaUFDCVCuentaTercero = BigDecimal.ZERO;
			this.TotalPosicion = BigDecimal.ZERO;
			this.CantidadSorteoLetras = BigDecimal.ZERO;
			this.CantidadBCS = BigDecimal.ZERO;
			this.CantidadBEC = BigDecimal.ZERO;
			this.CantidadBOV = BigDecimal.ZERO;
			this.CantidadEmisores = BigDecimal.ZERO;
		}
	}
	
	public static class CustodiaActualizar extends PosicionPorNemo {
		public Long IdCarga;
		public Long IdCuadraturaCircular;
		public BigDecimal Diferencia;
		public BigDecimal Precio;
		public BigDecimal DiferenciaValorizada;
		public BigDecimal TotalCustodioEmisores;
		public BigDecimal TotalCustodia;
		public String IdClasificacionTipoInstrumento;
		/*******Relacionado a sobreescribir informacion*******/
		public BigDecimal ReplicaBaseDiferencia;
		public BigDecimal ReplicaBaseTotalCustodioEmisores;
		public BigDecimal ReplicaBaseDiferenciaValorizada;
	}
	
	public static class CuadraturaEliminar extends BcsPojo{
		public Long IdCuadraturaCircular;
	}
	
	public static class CustodiaInsertar extends PosicionPorNemo {
		public Long IdCarga;
		public BigDecimal Diferencia;
		public BigDecimal TotalCustodioEmisores;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public String FechaCuadratura;
		
		public CustodiaInsertar() {
			super();
			this.IdClasificacionTipoInstrumento = "";
			this.IdTipoInstrumento = "";
		}
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCargarPosicionesArchivoL02Circular1518 in = gson.fromJson(jin.toString(), EntradaCargarPosicionesArchivoL02Circular1518.class);
		SalidaCargarPosicionesArchivoL02Circular1518 out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCargarPosicionesArchivoL02Circular1518 logica(Connection con, EntradaCargarPosicionesArchivoL02Circular1518 entrada) throws Exception {
		logger.info("INICIO::CargarPosicionesArchivoL02Circular1518");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaCargarPosicionesArchivoL02Circular1518 out = new SalidaCargarPosicionesArchivoL02Circular1518();

		List<CustodiaActualizar> custodiasCarga1518 = obtenerCustodiaCarga(con, entrada.p_FechaCuadratura, SQL_SELECT_CUADRATURA);
		List<CustodiaActualizar> custodiasCarga1720 = obtenerCustodiaCarga(con, entrada.p_FechaCuadratura, SQL_SELECT_CUADRATURA1720);
		
		if(custodiasCarga1518.isEmpty() || custodiasCarga1720.isEmpty()){
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "CARGA_DE_CUADRATURA_CUSTODIA_NO_REALIZADA", new HashMap<String, String>(), con, entrada.p_IdEmpresa));
			return out;
		}

		try {
			
			if(entrada.p_SobreEscribeArchivo != null && entrada.p_SobreEscribeArchivo.equalsIgnoreCase("S") == true) {
				
				Map<String, List<?>> hcustodiasCarga1518 = restaurarCamposDeCuadratura(con, custodiasCarga1518);
				Map<String, List<?>> hcustodiasCarga1720 = restaurarCamposDeCuadratura(con, custodiasCarga1720);
				
				List<CuadraturaEliminar> listadoCustodiaEliminar1518 = (List<CuadraturaEliminar>) hcustodiasCarga1518.get("CuadraturaEliminar");
				custodiasCarga1518 = (List<CustodiaActualizar>) hcustodiasCarga1518.get("CuadraturaRestaurada");

				List<CuadraturaEliminar> listadoCustodiaEliminar1720 = (List<CuadraturaEliminar>) hcustodiasCarga1720.get("CuadraturaEliminar");
				custodiasCarga1720 = (List<CustodiaActualizar>) hcustodiasCarga1720.get("CuadraturaRestaurada");
				
				eliminarCargaCuadratura(con, listadoCustodiaEliminar1518, SQL_DELETE_CUADRATURA);
				eliminarCargaCuadratura(con, listadoCustodiaEliminar1720, SQL_DELETE_CUADRATURA1720);					
			}
			
			Map<String, PosicionPorNemo> posicionesPorNemo1518 = obtenerPosicionesPorNemo(con, entrada.p_DatosArchivo, entrada.p_IdEmpresa);
			Map<String, PosicionPorNemo> posicionesPorNemo1720 = obtenerPosicionesPorNemo(con, entrada.p_DatosArchivo, entrada.p_IdEmpresa);
		
			custodiasCarga1518 = obtenerCargasActualizadas(custodiasCarga1518, posicionesPorNemo1518);
			custodiasCarga1720 = obtenerCargasActualizadas(custodiasCarga1720, posicionesPorNemo1720);
			
			List<CustodiaInsertar> custodiasInsertar1518 = obtenerCargasInsertar(con, custodiasCarga1518, posicionesPorNemo1518, entrada.p_FechaCuadratura);
			List<CustodiaInsertar> custodiasInsertar1720 = obtenerCargasInsertar(con, custodiasCarga1720, posicionesPorNemo1720, entrada.p_FechaCuadratura);
		
			actualizarCargaCuadratura(con, custodiasCarga1518, SQL_UPDATE_CUADRATURA);
			actualizarCargaCuadratura(con, custodiasCarga1720, SQL_UPDATE_CUADRATURA1720);
			
			insertarCargaCuadratura(con, custodiasInsertar1518, SQL_INSERT_CUADRATURA);
			insertarCargaCuadratura(con, custodiasInsertar1720, SQL_INSERT_CUADRATURA1720);
			
		}catch(SQLException SQLe) {
			SQLe.printStackTrace();
			out.P_OUT_ERRORESCONTADOR = 1L;
		}catch(Exception e) {
			e.printStackTrace();
			out.P_OUT_ERRORESCONTADOR = 1L;
		}
		
		if(out.P_OUT_ERRORESCONTADOR.equals(1L) == true) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_AL_CARGAR_ARCHIVO", new HashMap<String, String>(), con, entrada.p_IdEmpresa));
		}
	
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CargarPosicionesArchivoL02Circular1518", finServicio - inicioServicio);
		return out;
	}
	
	private List<CustodiaActualizar> obtenerCustodiaCarga(Connection con, String fechaCuadratura, String Query) throws Exception {
		logger.debug("INICIO::CargarPosicionesArchivoL02Circular.obtenerCustodiaCarga");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> parametro = new HashMap<String, Object>();
		parametro.put("FechaCuadratura", fechaCuadratura);
		
		SQL _sql = new SQL();
		List<CustodiaActualizar> custodiasCarga = _sql.query(con, Query, parametro, CustodiaActualizar.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CargarPosicionesArchivoL02Circular.obtenerCustodiaCarga", fin - inicio);
		return custodiasCarga;
	}
	
	private Map<String, PosicionPorNemo> obtenerPosicionesPorNemo(Connection con, List<Posicion> posiciones, Long idEmpresa) throws Exception {
		logger.debug("INICIO::CargarPosicionesArchivoL02Circular1518.obtenerPosicionesPorNemo");
		long inicio = System.currentTimeMillis();
		
		CuentaCustodia cuentaCustodia = CargaCustodiaCircular1518Utils.obtenerCuentaCustodia(con, idEmpresa);
		Map<String, List<Posicion>> posicionesAgrupadasPorNemo = posiciones.stream().collect(Collectors.groupingBy(posicion->posicion.Nemotecnico));
		
		HashMap<String, PosicionPorNemo> posicionesPorNemo = new HashMap<String, PosicionPorNemo>();
		for(Entry<String, List<Posicion>> posicionesNemo : posicionesAgrupadasPorNemo.entrySet()) {
			PosicionPorNemo posicionPorNemo = new PosicionPorNemo();
			posicionPorNemo.Nemotecnico = posicionesNemo.getKey();
			for(Posicion posicion : posicionesNemo.getValue()) {
				
				if(cuentaCustodia.CuentaRelacionadoDCV.equals(posicion.Cuenta)) {
					posicionPorNemo.CantidadDisponibleDCVCuentaRelacionado  = posicionPorNemo.CantidadDisponibleDCVCuentaRelacionado.add(posicion.PosicionDisponible, MathContext.DECIMAL128);
					posicionPorNemo.CantidadBloqueadaDCVCuentaRelacionado = posicionPorNemo.CantidadBloqueadaDCVCuentaRelacionado.add(posicion.PosicionBloqueda, MathContext.DECIMAL128);
					posicionPorNemo.CantidadReventaDCVCuentaRelacionado = posicionPorNemo.CantidadReventaDCVCuentaRelacionado.add(posicion.PosicionReventa, MathContext.DECIMAL128);
					posicionPorNemo.CantidadRecompraDCVCuentaRelacionado = posicionPorNemo.CantidadRecompraDCVCuentaRelacionado.add(posicion.PosicionRecompra, MathContext.DECIMAL128);
					posicionPorNemo.CantidadDCVCuentaRelacionado = posicionPorNemo.CantidadDisponibleDCVCuentaRelacionado
							.add(posicionPorNemo.CantidadBloqueadaDCVCuentaRelacionado, MathContext.DECIMAL128)
							.add(posicionPorNemo.CantidadReventaDCVCuentaRelacionado, MathContext.DECIMAL128)
							.add(posicionPorNemo.CantidadRecompraDCVCuentaRelacionado, MathContext.DECIMAL128);
					continue;
				}
				
				if(cuentaCustodia.CuentaPropiaDCV.equals(posicion.Cuenta)) {
					posicionPorNemo.CantidadDisponibleDCVCuentaPropia  = posicionPorNemo.CantidadDisponibleDCVCuentaPropia.add(posicion.PosicionDisponible, MathContext.DECIMAL128);
					posicionPorNemo.CantidadBloqueadaDCVCuentaPropia = posicionPorNemo.CantidadBloqueadaDCVCuentaPropia.add(posicion.PosicionBloqueda, MathContext.DECIMAL128);
					posicionPorNemo.CantidadReventaDCVCuentaPropia = posicionPorNemo.CantidadReventaDCVCuentaPropia.add(posicion.PosicionReventa, MathContext.DECIMAL128);
					posicionPorNemo.CantidadRecompraDCVCuentaPropia = posicionPorNemo.CantidadRecompraDCVCuentaPropia.add(posicion.PosicionRecompra, MathContext.DECIMAL128);
					posicionPorNemo.CantidadDCVCuentaPropia = posicionPorNemo.CantidadDisponibleDCVCuentaPropia
							.add(posicionPorNemo.CantidadBloqueadaDCVCuentaPropia, MathContext.DECIMAL128)
							.add(posicionPorNemo.CantidadReventaDCVCuentaPropia, MathContext.DECIMAL128)
							.add(posicionPorNemo.CantidadRecompraDCVCuentaPropia, MathContext.DECIMAL128);
					continue;
				}
				
				posicionPorNemo.CantidadDisponibleDCVCuentaTercero  = posicionPorNemo.CantidadDisponibleDCVCuentaTercero.add(posicion.PosicionDisponible, MathContext.DECIMAL128);
				posicionPorNemo.CantidadBloqueadaDCVCuentaTercero = posicionPorNemo.CantidadBloqueadaDCVCuentaTercero.add(posicion.PosicionBloqueda, MathContext.DECIMAL128);
				posicionPorNemo.CantidadReventaDCVCuentaTercero = posicionPorNemo.CantidadReventaDCVCuentaTercero.add(posicion.PosicionReventa, MathContext.DECIMAL128);
				posicionPorNemo.CantidadRecompraDCVCuentaTercero = posicionPorNemo.CantidadRecompraDCVCuentaTercero.add(posicion.PosicionRecompra, MathContext.DECIMAL128);
				posicionPorNemo.CantidadDCVCuentaTercero = posicionPorNemo.CantidadDisponibleDCVCuentaTercero
						.add(posicionPorNemo.CantidadBloqueadaDCVCuentaTercero, MathContext.DECIMAL128)
						.add(posicionPorNemo.CantidadReventaDCVCuentaTercero, MathContext.DECIMAL128)
						.add(posicionPorNemo.CantidadRecompraDCVCuentaTercero, MathContext.DECIMAL128);
				
			}
			posicionPorNemo.TotalPosicion = posicionPorNemo.CantidadDCVCuentaRelacionado.add(posicionPorNemo.CantidadDCVCuentaPropia, MathContext.DECIMAL128).add(posicionPorNemo.CantidadDCVCuentaTercero, MathContext.DECIMAL128);
			posicionesPorNemo.put(posicionPorNemo.Nemotecnico, posicionPorNemo);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CargarPosicionesArchivoL02Circular1518.obtenerPosicionesPorNemo", fin - inicio);
		return posicionesPorNemo;
	}

	private List<CustodiaActualizar> obtenerCargasActualizadas(List<CustodiaActualizar> custodiasCarga, Map<String, PosicionPorNemo> posicionesPorNemo) throws Exception {
		logger.debug("INICIO::CargarPosicionesArchivoL02Circular1518.obtenerCargasActualizadas");
		long inicio = System.currentTimeMillis();
		
		Map<String, CustodiaActualizar> custodiasCargaPorNemo = custodiasCarga.stream().collect(Collectors.toMap(custodia->custodia.Nemotecnico, custodia->custodia));
		
		for(Entry<String, PosicionPorNemo> posicionNemo : posicionesPorNemo.entrySet()) {
			CustodiaActualizar custodiaActualizar = custodiasCargaPorNemo.get(posicionNemo.getKey());
			if(custodiaActualizar != null) {
				PosicionPorNemo posicion = posicionNemo.getValue();
				custodiaActualizar.CantidadDCVCuentaRelacionado = custodiaActualizar.CantidadDCVCuentaRelacionado.add(posicion.CantidadDCVCuentaRelacionado, MathContext.DECIMAL128);
				custodiaActualizar.CantidadDisponibleDCVCuentaRelacionado  = custodiaActualizar.CantidadDisponibleDCVCuentaRelacionado.add(posicion.CantidadDisponibleDCVCuentaRelacionado, MathContext.DECIMAL128);
				custodiaActualizar.CantidadBloqueadaDCVCuentaRelacionado = custodiaActualizar.CantidadBloqueadaDCVCuentaRelacionado.add(posicion.CantidadBloqueadaDCVCuentaRelacionado, MathContext.DECIMAL128);
				custodiaActualizar.CantidadReventaDCVCuentaRelacionado = custodiaActualizar.CantidadReventaDCVCuentaRelacionado.add(posicion.CantidadReventaDCVCuentaRelacionado, MathContext.DECIMAL128);
				custodiaActualizar.CantidadRecompraDCVCuentaRelacionado = custodiaActualizar.CantidadRecompraDCVCuentaRelacionado.add(posicion.CantidadRecompraDCVCuentaRelacionado, MathContext.DECIMAL128);
				custodiaActualizar.CantidadValorizadaUFDCVCuentaRelacionado = custodiaActualizar.CantidadValorizadaUFDCVCuentaRelacionado.add(posicion.CantidadValorizadaUFDCVCuentaRelacionado, MathContext.DECIMAL128);
				custodiaActualizar.CantidadDCVCuentaPropia = custodiaActualizar.CantidadDCVCuentaPropia.add(posicion.CantidadDCVCuentaPropia, MathContext.DECIMAL128);
				custodiaActualizar.CantidadDisponibleDCVCuentaPropia = custodiaActualizar.CantidadDisponibleDCVCuentaPropia.add(posicion.CantidadDisponibleDCVCuentaPropia, MathContext.DECIMAL128);
				custodiaActualizar.CantidadBloqueadaDCVCuentaPropia = custodiaActualizar.CantidadBloqueadaDCVCuentaPropia.add(posicion.CantidadBloqueadaDCVCuentaPropia, MathContext.DECIMAL128);
				custodiaActualizar.CantidadReventaDCVCuentaPropia = custodiaActualizar.CantidadReventaDCVCuentaPropia.add(posicion.CantidadReventaDCVCuentaPropia, MathContext.DECIMAL128);
				custodiaActualizar.CantidadRecompraDCVCuentaPropia = custodiaActualizar.CantidadRecompraDCVCuentaPropia.add(posicion.CantidadRecompraDCVCuentaPropia, MathContext.DECIMAL128);
				custodiaActualizar.CantidadValorizadaUFDCVCuentaPropia = custodiaActualizar.CantidadValorizadaUFDCVCuentaPropia.add(posicion.CantidadValorizadaUFDCVCuentaPropia, MathContext.DECIMAL128);
				custodiaActualizar.CantidadDCVCuentaTercero = custodiaActualizar.CantidadDCVCuentaTercero.add(posicion.CantidadDCVCuentaTercero, MathContext.DECIMAL128);
				custodiaActualizar.CantidadDisponibleDCVCuentaTercero = custodiaActualizar.CantidadDisponibleDCVCuentaTercero.add(posicion.CantidadDisponibleDCVCuentaTercero, MathContext.DECIMAL128);
				custodiaActualizar.CantidadBloqueadaDCVCuentaTercero = custodiaActualizar.CantidadBloqueadaDCVCuentaTercero.add(posicion.CantidadBloqueadaDCVCuentaTercero, MathContext.DECIMAL128);
				custodiaActualizar.CantidadReventaDCVCuentaTercero = custodiaActualizar.CantidadReventaDCVCuentaTercero.add(posicion.CantidadReventaDCVCuentaTercero, MathContext.DECIMAL128);
				custodiaActualizar.CantidadRecompraDCVCuentaTercero = custodiaActualizar.CantidadRecompraDCVCuentaTercero.add(posicion.CantidadRecompraDCVCuentaTercero, MathContext.DECIMAL128);
				custodiaActualizar.CantidadValorizadaUFDCVCuentaTercero = custodiaActualizar.CantidadValorizadaUFDCVCuentaTercero.add(posicion.CantidadValorizadaUFDCVCuentaTercero, MathContext.DECIMAL128);
				custodiaActualizar.TotalCustodioEmisores = custodiaActualizar.TotalCustodioEmisores.add(posicion.TotalPosicion, MathContext.DECIMAL128);
				custodiaActualizar.Diferencia = custodiaActualizar.TotalCustodioEmisores.subtract(custodiaActualizar.TotalCustodia, MathContext.DECIMAL128);
				custodiaActualizar.DiferenciaValorizada = "RV".equals(custodiaActualizar.IdClasificacionTipoInstrumento) || "FM".equals(custodiaActualizar.IdClasificacionTipoInstrumento) ? custodiaActualizar.Diferencia.multiply(custodiaActualizar.Precio, MathContext.DECIMAL128).abs(MathContext.DECIMAL128) : custodiaActualizar.Diferencia.abs(MathContext.DECIMAL128);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CargarPosicionesArchivoL02Circular1518.obtenerCargasActualizadas", fin - inicio);
		return custodiasCarga;
	}
	
	private List<CustodiaInsertar> obtenerCargasInsertar(Connection con, List<CustodiaActualizar> custodiasCarga, Map<String, PosicionPorNemo> posicionesPorNemo, String fechaCuadratura) throws Exception {
		logger.debug("INICIO::CargarPosicionesArchivoL02Circular1518.obtenerCargasInsertar");
		long inicio = System.currentTimeMillis();
		
		Map<String, CustodiaActualizar> custodiasCargaPorNemo = custodiasCarga.stream().collect(Collectors.toMap(custodia->custodia.Nemotecnico, custodia->custodia));
		
		List<CustodiaInsertar> custodiasInsertar = new ArrayList<CustodiaInsertar>();
		
		for(Entry<String, PosicionPorNemo> posicionNemo : posicionesPorNemo.entrySet()) {
			CustodiaActualizar custodiaActualizar = custodiasCargaPorNemo.get(posicionNemo.getKey());
			if(custodiaActualizar == null) {
				CustodiaInsertar custodiaInsertar = new CustodiaInsertar();
				PosicionPorNemo posicion = posicionNemo.getValue();
				custodiaInsertar.IdCarga = custodiasCarga.get(0).IdCarga;
				custodiaInsertar.Nemotecnico = posicion.Nemotecnico;
				custodiaInsertar.FechaCuadratura = fechaCuadratura;
				custodiaInsertar.CantidadDCVCuentaRelacionado = posicion.CantidadDCVCuentaRelacionado;
				custodiaInsertar.CantidadDisponibleDCVCuentaRelacionado  = posicion.CantidadDisponibleDCVCuentaRelacionado;
				custodiaInsertar.CantidadBloqueadaDCVCuentaRelacionado = posicion.CantidadBloqueadaDCVCuentaRelacionado;
				custodiaInsertar.CantidadReventaDCVCuentaRelacionado = posicion.CantidadReventaDCVCuentaRelacionado;
				custodiaInsertar.CantidadRecompraDCVCuentaRelacionado = posicion.CantidadRecompraDCVCuentaRelacionado;
				custodiaInsertar.CantidadValorizadaUFDCVCuentaRelacionado = posicion.CantidadValorizadaUFDCVCuentaRelacionado;
				custodiaInsertar.CantidadDCVCuentaPropia = posicion.CantidadDCVCuentaPropia;
				custodiaInsertar.CantidadDisponibleDCVCuentaPropia = posicion.CantidadDisponibleDCVCuentaPropia;
				custodiaInsertar.CantidadBloqueadaDCVCuentaPropia = posicion.CantidadBloqueadaDCVCuentaPropia;
				custodiaInsertar.CantidadReventaDCVCuentaPropia = posicion.CantidadReventaDCVCuentaPropia;
				custodiaInsertar.CantidadRecompraDCVCuentaPropia = posicion.CantidadRecompraDCVCuentaPropia;
				custodiaInsertar.CantidadValorizadaUFDCVCuentaPropia = posicion.CantidadValorizadaUFDCVCuentaPropia;
				custodiaInsertar.CantidadDCVCuentaTercero = posicion.CantidadDCVCuentaTercero;
				custodiaInsertar.CantidadDisponibleDCVCuentaTercero = posicion.CantidadDisponibleDCVCuentaTercero;
				custodiaInsertar.CantidadBloqueadaDCVCuentaTercero = posicion.CantidadBloqueadaDCVCuentaTercero;
				custodiaInsertar.CantidadReventaDCVCuentaTercero = posicion.CantidadReventaDCVCuentaTercero;
				custodiaInsertar.CantidadRecompraDCVCuentaTercero = posicion.CantidadRecompraDCVCuentaTercero;
				custodiaInsertar.CantidadValorizadaUFDCVCuentaTercero = posicion.CantidadValorizadaUFDCVCuentaTercero;
				custodiaInsertar.TotalCustodioEmisores = posicion.TotalPosicion;
				custodiaInsertar.Diferencia = posicion.TotalPosicion;
				custodiaInsertar.NemoExisteEnOptimus = "N";
				custodiasInsertar.add(custodiaInsertar);
			}
		}
		
		List<String> nemotecnicos = custodiasInsertar.stream().map(custodia->custodia.Nemotecnico).collect(Collectors.toList());
		Map<String, Instrumento> instrumentosPorNemo = CargaCustodiaCircular1518Utils.obtenerInstrumentosPorNemo(con, nemotecnicos, ORIGEN);
		
		custodiasInsertar.stream().forEach(custodia->{
			Instrumento instrumento = instrumentosPorNemo.get(custodia.Nemotecnico);
			if(instrumento != null) {
				custodia.IdClasificacionTipoInstrumento = instrumento.IdClasificacionTipoInstrumento;
				custodia.IdTipoInstrumento = instrumento.IdTipoInstrumento;
			}
		});
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CargarPosicionesArchivoL02Circular1518.obtenerCargasInsertar", fin - inicio);
		return custodiasInsertar;
	}

	private Map<String, List<?>> restaurarCamposDeCuadratura(Connection con, List<CustodiaActualizar> listadoCustodia) throws Exception{
		logger.debug("INICIO::CargarPosicionesArchivoL02Circular.restaurarCamposDeCuadratura");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, List<?>> cuadraturas = new HashMap<>(2);
		List<CustodiaActualizar> listadoCuadraturaRestaurada = new ArrayList<>();
		List<CuadraturaEliminar> listadoCuadraturaEliminar = new ArrayList<>();
		
		for(CustodiaActualizar cust : listadoCustodia) {
			if(cust.NemoExisteEnOptimus==null || cust.NemoExisteEnOptimus.equals("N")) {
				cust.TotalCustodioEmisores = BigDecimal.ZERO;
				cust.Diferencia = BigDecimal.ZERO;
				cust.DiferenciaValorizada = BigDecimal.ZERO;
				cust.CantidadDCVCuentaRelacionado = BigDecimal.ZERO;
				cust.CantidadDisponibleDCVCuentaRelacionado  = BigDecimal.ZERO;
				cust.CantidadBloqueadaDCVCuentaRelacionado = BigDecimal.ZERO;
				cust.CantidadReventaDCVCuentaRelacionado = BigDecimal.ZERO;
				cust.CantidadRecompraDCVCuentaRelacionado = BigDecimal.ZERO;
				cust.CantidadValorizadaUFDCVCuentaRelacionado = BigDecimal.ZERO;
				cust.CantidadDCVCuentaPropia = BigDecimal.ZERO;
				cust.CantidadDisponibleDCVCuentaPropia = BigDecimal.ZERO;
				cust.CantidadBloqueadaDCVCuentaPropia = BigDecimal.ZERO;
				cust.CantidadReventaDCVCuentaPropia = BigDecimal.ZERO;
				cust.CantidadRecompraDCVCuentaPropia = BigDecimal.ZERO;
				cust.CantidadValorizadaUFDCVCuentaPropia = BigDecimal.ZERO;
				cust.CantidadDCVCuentaTercero = BigDecimal.ZERO;
				cust.CantidadDisponibleDCVCuentaTercero = BigDecimal.ZERO;
				cust.CantidadBloqueadaDCVCuentaTercero = BigDecimal.ZERO;
				cust.CantidadReventaDCVCuentaTercero = BigDecimal.ZERO;
				cust.CantidadRecompraDCVCuentaTercero = BigDecimal.ZERO;
				cust.CantidadValorizadaUFDCVCuentaTercero = BigDecimal.ZERO;
				cust.CantidadBCS = BigDecimal.ZERO;
				cust.CantidadBEC = BigDecimal.ZERO;
				cust.CantidadBOV = BigDecimal.ZERO;
				cust.CantidadEmisores = BigDecimal.ZERO;
				cust.CantidadSorteoLetras = BigDecimal.ZERO;
				listadoCuadraturaRestaurada.add(cust);
			}
			else{
				cust.TotalCustodioEmisores = cust.ReplicaBaseTotalCustodioEmisores;
				cust.Diferencia = cust.ReplicaBaseDiferencia;
				cust.DiferenciaValorizada = cust.ReplicaBaseDiferenciaValorizada;
				cust.CantidadDCVCuentaRelacionado = BigDecimal.ZERO;
				cust.CantidadDisponibleDCVCuentaRelacionado  = BigDecimal.ZERO;
				cust.CantidadBloqueadaDCVCuentaRelacionado = BigDecimal.ZERO;
				cust.CantidadReventaDCVCuentaRelacionado = BigDecimal.ZERO;
				cust.CantidadRecompraDCVCuentaRelacionado = BigDecimal.ZERO;
				cust.CantidadValorizadaUFDCVCuentaRelacionado = BigDecimal.ZERO;
				cust.CantidadDCVCuentaPropia = BigDecimal.ZERO;
				cust.CantidadDisponibleDCVCuentaPropia = BigDecimal.ZERO;
				cust.CantidadBloqueadaDCVCuentaPropia = BigDecimal.ZERO;
				cust.CantidadReventaDCVCuentaPropia = BigDecimal.ZERO;
				cust.CantidadRecompraDCVCuentaPropia = BigDecimal.ZERO;
				cust.CantidadValorizadaUFDCVCuentaPropia = BigDecimal.ZERO;
				cust.CantidadDCVCuentaTercero = BigDecimal.ZERO;
				cust.CantidadDisponibleDCVCuentaTercero = BigDecimal.ZERO;
				cust.CantidadBloqueadaDCVCuentaTercero = BigDecimal.ZERO;
				cust.CantidadReventaDCVCuentaTercero = BigDecimal.ZERO;
				cust.CantidadRecompraDCVCuentaTercero = BigDecimal.ZERO;
				cust.CantidadValorizadaUFDCVCuentaTercero = BigDecimal.ZERO;
				//Posiciones referente a archivo Otros Saldos
				cust.CantidadBCS = BigDecimal.ZERO;
				cust.CantidadBEC = BigDecimal.ZERO;
				cust.CantidadBOV = BigDecimal.ZERO;
				cust.CantidadEmisores = BigDecimal.ZERO;
				//Posiciones referente a archivo L43
				cust.CantidadSorteoLetras = BigDecimal.ZERO;
				listadoCuadraturaRestaurada.add(cust);	
			}
		}

		cuadraturas.put("CuadraturaEliminar", listadoCuadraturaEliminar);
		cuadraturas.put("CuadraturaRestaurada", listadoCuadraturaRestaurada);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CargarPosicionesArchivoL02Circular.restaurarCamposDeCuadratura", fin - inicio);
		return cuadraturas;
	}


	private void actualizarCargaCuadratura(Connection con, List<CustodiaActualizar> custodias, String Query)  throws Exception {
		logger.debug("INICIO::CargarPosicionesArchivoL02Circular.actualizarCargaCuadratura");
		long inicio = System.currentTimeMillis();
		
		SQL _sql = new SQL();
		_sql.batch(con, Query, custodias, CustodiaActualizar.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CargarPosicionesArchivoL02Circular.actualizarCargaCuadratura", fin - inicio);
	}

	private void insertarCargaCuadratura(Connection con, List<CustodiaInsertar> custodias, String Query)  throws Exception {
		logger.debug("INICIO::CargarPosicionesArchivoL02Circular.insertarCargaCuadratura");
		long inicio = System.currentTimeMillis();
		
		SQL _sql = new SQL();
		_sql.batch(con, Query, custodias, CustodiaInsertar.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CargarPosicionesArchivoL02Circular.insertarCargaCuadratura", fin - inicio);
	}

	private void eliminarCargaCuadratura(Connection con, List<CuadraturaEliminar> custodia, String Query)  throws SQLException, Exception {
		logger.debug("INICIO::CargarPosicionesArchivoL02Circular.eliminarCargaCuadratura");
		long inicio = System.currentTimeMillis();
		SQL _sql = new SQL();
		
		_sql.batch(con, Query, custodia, CuadraturaEliminar.class);
	
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CargarPosicionesArchivoL02Circular.eliminarCargaCuadratura", fin - inicio);
	}

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02", "connections.properties")) {
			
			EntradaCargarPosicionesArchivoL02Circular1518 entradaCargarPosicionesArchivoL02Circular1518 = new EntradaCargarPosicionesArchivoL02Circular1518();
			
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"CUS\",\"p_SobreEscribeArchivo\":\"N\",\"p_FechaCuadratura\":\"2025-04-11\",\"p_DatosArchivo\":[{\"Cuenta\":\"12176007\",\"Nemotecnico\":\"COPEC\",\"PosicionDisponible\":0,\"PosicionBloqueda\":4000,\"PosicionReventa\":0,\"PosicionRecompra\":0,\"Validacion\":\"OK\"},{\"Cuenta\":\"12176007\",\"Nemotecnico\":\"FDSTD-080925\",\"PosicionDisponible\":0,\"PosicionBloqueda\":4000,\"PosicionReventa\":0,\"PosicionRecompra\":0,\"Validacion\":\"OK\"}]}";
			Gson gson = Utils.getGsonBuilder();
			entradaCargarPosicionesArchivoL02Circular1518 = gson.fromJson(json, EntradaCargarPosicionesArchivoL02Circular1518.class);	
			CargarPosicionesArchivoL02Circular1518 cargarPosicionesArchivoL02Circular1518 = new CargarPosicionesArchivoL02Circular1518();
			
			try {
				SalidaCargarPosicionesArchivoL02Circular1518 salidaCargarPosicionesArchivoL02Circular1518 = cargarPosicionesArchivoL02Circular1518.logica(con, entradaCargarPosicionesArchivoL02Circular1518);
				System.out.println(salidaCargarPosicionesArchivoL02Circular1518.toString());
				con.rollback();
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
