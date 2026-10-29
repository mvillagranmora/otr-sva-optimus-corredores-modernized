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

import cl.bcs.custodia.CargaCustodiaCircular1518Utils.Instrumento;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CargarPosicionesOtrosSaldosCircular1518 implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CargarPosicionesOtrosSaldosCircular1518.class);
	
	private final String SQL_SELECT_CUADRATURA = loadResourceToString("cl/bcs/custodia/sql/CargarPosicionesOtrosSaldosCircular1518_SelectCuadratura.sql");
	private final String SQL_UPDATE_CUADRATURA = loadResourceToString("cl/bcs/custodia/sql/CargarPosicionesOtrosSaldosCircular1518_UpdateCuadratura.sql");
	private final String SQL_INSERT_CUADRATURA = loadResourceToString("cl/bcs/custodia/sql/CargarPosicionesOtrosSaldosCircular1518_InsertCuadratura.sql");
	private final String SQL_DELETE_CUADRATURA = loadResourceToString("cl/bcs/custodia/sql/CargarPosicionesOtrosSaldosCircular1518_DeleteCuadratura.sql");
	
	private final String SQL_SELECT_CUADRATURA1720 = loadResourceToString("cl/bcs/custodia/sql/CargarPosicionesOtrosSaldosCircular1720_SelectCuadratura.sql");
	private final String SQL_UPDATE_CUADRATURA1720 = loadResourceToString("cl/bcs/custodia/sql/CargarPosicionesOtrosSaldosCircular1720_UpdateCuadratura.sql");
	private final String SQL_INSERT_CUADRATURA1720 = loadResourceToString("cl/bcs/custodia/sql/CargarPosicionesOtrosSaldosCircular1720_InsertCuadratura.sql");
	private final String SQL_DELETE_CUADRATURA1720 = loadResourceToString("cl/bcs/custodia/sql/CargarPosicionesOtrosSaldosCircular1720_DeleteCuadratura.sql");
	
	public static class EntradaCargarPosicionesOtrosSaldosCircular1518 extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_FechaCuadratura;
		public String p_SobreEscribeArchivo;
		public List<Posicion> p_DatosArchivo;
		
		public EntradaCargarPosicionesOtrosSaldosCircular1518() {
			this.p_SobreEscribeArchivo = "N";
			this.p_DatosArchivo = new ArrayList<>();
		}
	}
	
	public static class Posicion extends BcsPojo {
		public String Boveda;
		public String Nemotecnico;
		public BigDecimal PosicionDisponible;
		public String Mercado;
		public String Validacion;
		
		public Posicion() {
			this.PosicionDisponible = BigDecimal.ZERO;
		}
	}
	
	public static class SalidaCargarPosicionesOtrosSaldosCircular1518 extends BcsPojo {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long	P_OUT_ERRORESCONTADOR;
		
		public SalidaCargarPosicionesOtrosSaldosCircular1518() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<>();
		}
	}
	
	public static class PosicionPorNemo extends BcsPojo {
		public String Nemotecnico;
		public BigDecimal CantidadBCS;
		public BigDecimal CantidadBEC;
		public BigDecimal CantidadBOV;
		public BigDecimal CantidadEmisores;
		public BigDecimal TotalPosicion;
		public String NemoExisteEnOptimus;
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
		public BigDecimal CantidadSorteoLetras;
		public String Mercado;

		public PosicionPorNemo() {
			this.CantidadBCS = BigDecimal.ZERO;
			this.CantidadBEC = BigDecimal.ZERO;
			this.CantidadBOV = BigDecimal.ZERO;
			this.CantidadEmisores = BigDecimal.ZERO;
			this.TotalPosicion = BigDecimal.ZERO;
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
			this.CantidadSorteoLetras = BigDecimal.ZERO;
			this.Mercado = "";
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
		/*******Relacionado a sobreescribir informacion*************/
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
			this.IdClasificacionTipoInstrumento = "";
			this.IdTipoInstrumento = "";
		}
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCargarPosicionesOtrosSaldosCircular1518 in = gson.fromJson(jin.toString(), EntradaCargarPosicionesOtrosSaldosCircular1518.class);
		SalidaCargarPosicionesOtrosSaldosCircular1518 out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCargarPosicionesOtrosSaldosCircular1518 logica(Connection con, EntradaCargarPosicionesOtrosSaldosCircular1518 entrada) throws Exception {
		logger.info("INICIO::CargarPosicionesOtrosSaldosCircular1518");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaCargarPosicionesOtrosSaldosCircular1518 out = new SalidaCargarPosicionesOtrosSaldosCircular1518();
		
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
			
			Map<String, PosicionPorNemo> posicionesPorNemo = obtenerPosicionesPorNemo(entrada.p_DatosArchivo);
			custodiasCarga1518 = obtenerCargasActualizadas(custodiasCarga1518, posicionesPorNemo);
			custodiasCarga1720 = obtenerCargasActualizadas(custodiasCarga1720, posicionesPorNemo);
			List<CustodiaInsertar> custodiasInsertar1518 = obtenerCargasInsertar(con, custodiasCarga1518, posicionesPorNemo, entrada.p_DatosArchivo, entrada.p_FechaCuadratura);
			List<CustodiaInsertar> custodiasInsertar1720 = obtenerCargasInsertar(con, custodiasCarga1720, posicionesPorNemo, entrada.p_DatosArchivo, entrada.p_FechaCuadratura);
			
			actualizarCargaCuadratura(con, custodiasCarga1518, SQL_UPDATE_CUADRATURA);
			actualizarCargaCuadratura(con, custodiasCarga1720, SQL_UPDATE_CUADRATURA1720);
			
			insertarCargaCuadratura(con, custodiasInsertar1518, SQL_INSERT_CUADRATURA);
			insertarCargaCuadratura(con, custodiasInsertar1720, SQL_INSERT_CUADRATURA1720);
			
		}catch(SQLException SQLe) {
			logger.error(SQLe);
			
		}catch(Exception e ) {
			logger.error(e);
		}

		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CargarPosicionesOtrosSaldosCircular1518", finServicio - inicioServicio);
		return out;
	}
	
	private List<CustodiaActualizar> obtenerCustodiaCarga(Connection con, String fechaCuadratura, String Query) throws Exception {
		logger.debug("INICIO::CargarPosicionesOtrosSaldosCircular.obtenerCustodiaCarga");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> parametro = new HashMap<String, Object>();
		parametro.put("FechaCuadratura", fechaCuadratura);
		
		SQL _sql = new SQL();
		List<CustodiaActualizar> custodiasCarga = _sql.query(con, Query, parametro, CustodiaActualizar.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CargarPosicionesOtrosSaldosCircular.obtenerCustodiaCarga", fin - inicio);
		return custodiasCarga;
	}
	
	private Map<String, PosicionPorNemo> obtenerPosicionesPorNemo(List<Posicion> posiciones) throws Exception {
		logger.debug("INICIO::CargarPosicionesOtrosSaldosCircular1518.obtenerPosicionesPorNemo");
		long inicio = System.currentTimeMillis();
		
		Map<String, List<Posicion>> posicionesAgrupadasPorNemo = posiciones.stream().collect(Collectors.groupingBy(posicion->posicion.Nemotecnico + posicion.Mercado.trim()));
		
		HashMap<String, PosicionPorNemo> posicionesPorNemo = new HashMap<String, PosicionPorNemo>();
		for(Entry<String, List<Posicion>> posicionesNemo : posicionesAgrupadasPorNemo.entrySet()) {
			PosicionPorNemo posicionPorNemo = new PosicionPorNemo();
			posicionPorNemo.Nemotecnico = posicionesNemo.getValue().get(0).Nemotecnico;
			posicionPorNemo.Mercado = posicionesNemo.getValue().get(0).Mercado.trim();
			for(Posicion posicion : posicionesNemo.getValue()) {
				
				if("BCS".equals(posicion.Boveda)) {
					posicionPorNemo.CantidadBCS  = posicionPorNemo.CantidadBCS.add(posicion.PosicionDisponible, MathContext.DECIMAL128);
					continue;
				}
				
				if("BEC".equals(posicion.Boveda)) {
					posicionPorNemo.CantidadBEC  = posicionPorNemo.CantidadBEC.add(posicion.PosicionDisponible, MathContext.DECIMAL128);
					continue;
				}
				
				if("BOV".equals(posicion.Boveda)) {
					posicionPorNemo.CantidadBOV  = posicionPorNemo.CantidadBOV.add(posicion.PosicionDisponible, MathContext.DECIMAL128);
					continue;
				}
				
				posicionPorNemo.CantidadEmisores = posicionPorNemo.CantidadEmisores.add(posicion.PosicionDisponible);
				
			}
			posicionPorNemo.TotalPosicion = posicionPorNemo.CantidadBCS.add(posicionPorNemo.CantidadBEC, MathContext.DECIMAL128).add(posicionPorNemo.CantidadBOV, MathContext.DECIMAL128).add(posicionPorNemo.CantidadEmisores, MathContext.DECIMAL128);
			posicionesPorNemo.put(posicionesNemo.getKey(), posicionPorNemo);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CargarPosicionesOtrosSaldosCircular1518.obtenerPosicionesPorNemo", fin - inicio);
		return posicionesPorNemo;
	}
	
	private List<CustodiaActualizar> obtenerCargasActualizadas(List<CustodiaActualizar> custodiasCarga, Map<String, PosicionPorNemo> posicionesPorNemo) throws Exception {
		logger.debug("INICIO::CargarPosicionesOtrosSaldosCircular1518.obtenerCargasActualizadas");
		long inicio = System.currentTimeMillis();
		
		Map<String, CustodiaActualizar> custodiasCargaPorNemo = custodiasCarga.stream().collect(Collectors.toMap(custodia->custodia.Nemotecnico + custodia.IdClasificacionTipoInstrumento, custodia->custodia));
		
		for(Entry<String, PosicionPorNemo> posicionNemo : posicionesPorNemo.entrySet()) {
			CustodiaActualizar custodiaActualizar = custodiasCargaPorNemo.get(posicionNemo.getKey());
			if(custodiaActualizar != null) {
				PosicionPorNemo posicion = posicionNemo.getValue();
				custodiaActualizar.CantidadBCS = custodiaActualizar.CantidadBCS.add(posicion.CantidadBCS, MathContext.DECIMAL128);
				custodiaActualizar.CantidadBEC  = custodiaActualizar.CantidadBEC.add(posicion.CantidadBEC, MathContext.DECIMAL128);
				custodiaActualizar.CantidadBOV = custodiaActualizar.CantidadBOV.add(posicion.CantidadBOV, MathContext.DECIMAL128);
				custodiaActualizar.CantidadEmisores = custodiaActualizar.CantidadEmisores.add(posicion.CantidadEmisores, MathContext.DECIMAL128);
				
				custodiaActualizar.TotalCustodioEmisores = custodiaActualizar.TotalCustodioEmisores.add(posicion.TotalPosicion, MathContext.DECIMAL128);
				custodiaActualizar.Diferencia = custodiaActualizar.TotalCustodioEmisores.subtract(custodiaActualizar.TotalCustodia, MathContext.DECIMAL128);
				custodiaActualizar.DiferenciaValorizada = "RV".equals(custodiaActualizar.IdClasificacionTipoInstrumento) || "FM".equals(custodiaActualizar.IdClasificacionTipoInstrumento) ? custodiaActualizar.Diferencia.multiply(custodiaActualizar.Precio, MathContext.DECIMAL128).abs(MathContext.DECIMAL128) : custodiaActualizar.Diferencia.abs(MathContext.DECIMAL128);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CargarPosicionesOtrosSaldosCircular1518.obtenerCargasActualizadas", fin - inicio);
		return custodiasCarga;
	}
	
	private List<CustodiaInsertar> obtenerCargasInsertar(Connection con, List<CustodiaActualizar> custodiasCarga, Map<String, PosicionPorNemo> posicionesPorNemo, List<Posicion> posiciones, String fechaCuadratura) throws Exception {
		logger.debug("INICIO::CargarPosicionesOtrosSaldosCircular1518.obtenerCargasInsertar");
		long inicio = System.currentTimeMillis();
		
		Map<String, CustodiaActualizar> custodiasCargaPorNemo = custodiasCarga.stream().collect(Collectors.toMap(custodia->custodia.Nemotecnico + custodia.IdClasificacionTipoInstrumento, custodia->custodia));
		
		List<CustodiaInsertar> custodiasInsertar = new ArrayList<CustodiaInsertar>();
		
		for(Entry<String, PosicionPorNemo> posicionNemo : posicionesPorNemo.entrySet()) {
			CustodiaActualizar custodiaActualizar = custodiasCargaPorNemo.get(posicionNemo.getKey());
			if(custodiaActualizar == null) {
				CustodiaInsertar custodiaInsertar = new CustodiaInsertar();
				PosicionPorNemo posicion = posicionNemo.getValue();
				custodiaInsertar.IdCarga = custodiasCarga.get(0).IdCarga;
				custodiaInsertar.Nemotecnico = posicion.Nemotecnico;
				custodiaInsertar.FechaCuadratura = fechaCuadratura;
				custodiaInsertar.CantidadBCS = posicion.CantidadBCS;
				custodiaInsertar.CantidadBEC  = posicion.CantidadBEC;
				custodiaInsertar.CantidadBOV = posicion.CantidadBOV;
				custodiaInsertar.CantidadEmisores = posicion.CantidadEmisores;
				custodiaInsertar.TotalCustodioEmisores = posicion.TotalPosicion;
				custodiaInsertar.Diferencia = posicion.TotalPosicion;
				custodiaInsertar.NemoExisteEnOptimus = "N";
				custodiaInsertar.Mercado = posicion.Mercado.trim();
				custodiasInsertar.add(custodiaInsertar);
			}
		}
		
		List<String> nemotecnicos = custodiasInsertar.stream().map(custodia->custodia.Nemotecnico).collect(Collectors.toList());
		List<String> mercados = posiciones.stream().map(posicion -> posicion.Mercado).collect(Collectors.toList());
		Map<String, Instrumento> instrumentosPorNemo = CargaCustodiaCircular1518Utils.obtenerInstrumentosPorNemo(con, nemotecnicos, mercados);
		
		custodiasInsertar.stream().forEach(custodia->{
			Instrumento instrumento = instrumentosPorNemo.get(custodia.Nemotecnico + custodia.Mercado);
			if(instrumento != null) {
				custodia.IdClasificacionTipoInstrumento = instrumento.IdClasificacionTipoInstrumento;
				custodia.IdTipoInstrumento = instrumento.IdTipoInstrumento;
			} else {
				custodia.IdClasificacionTipoInstrumento = custodia.Mercado.trim();
			}
		});
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CargarPosicionesOtrosSaldosCircular1518.obtenerCargasInsertar", fin - inicio);
		return custodiasInsertar;
	}
	
	private Map<String, List<?>> restaurarCamposDeCuadratura(Connection con, List<CustodiaActualizar> listadoCustodia) throws Exception{
		logger.debug("INICIO::CargarPosicionesOtrosSaldosCircular1518.restaurarCamposDeCuadratura");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, List<?>> cuadraturas = new HashMap<>(2);
		List<CustodiaActualizar> listadoCuadraturaRestaurada = new ArrayList<>();
		List<CuadraturaEliminar> listadoCuadraturaEliminar = new ArrayList<>();
		
		for(CustodiaActualizar cust : listadoCustodia) {
			if(cust.NemoExisteEnOptimus==null || cust.NemoExisteEnOptimus.equals("N")) {
				cust.TotalCustodioEmisores = BigDecimal.ZERO;
				cust.Diferencia = BigDecimal.ZERO;
				cust.DiferenciaValorizada = BigDecimal.ZERO;
				cust.CantidadBCS = BigDecimal.ZERO;
				cust.CantidadBEC = BigDecimal.ZERO;
				cust.CantidadBOV = BigDecimal.ZERO;
				cust.CantidadEmisores = BigDecimal.ZERO;
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
				cust.CantidadSorteoLetras = BigDecimal.ZERO;
				listadoCuadraturaRestaurada.add(cust);
			}
			else{
				cust.TotalCustodioEmisores = cust.ReplicaBaseTotalCustodioEmisores;
				cust.Diferencia = cust.ReplicaBaseDiferencia;
				cust.DiferenciaValorizada = cust.ReplicaBaseDiferenciaValorizada;
				cust.CantidadBCS = BigDecimal.ZERO;
				cust.CantidadBEC = BigDecimal.ZERO;
				cust.CantidadBOV = BigDecimal.ZERO;
				cust.CantidadEmisores = BigDecimal.ZERO;
				//Posiciones referente a archivo L02
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
				//Posiciones referente a archivo L43
				cust.CantidadSorteoLetras = BigDecimal.ZERO;
				
				listadoCuadraturaRestaurada.add(cust);	
			}
		}
		
		cuadraturas.put("CuadraturaEliminar", listadoCuadraturaEliminar);
		cuadraturas.put("CuadraturaRestaurada", listadoCuadraturaRestaurada);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CargarPosicionesOtrosSaldosCircular1518.restaurarCamposDeCuadratura", fin - inicio);
		return cuadraturas;
	}
	
	private void actualizarCargaCuadratura(Connection con, List<CustodiaActualizar> custodias, String Query)  throws Exception {
		logger.debug("INICIO::CargarPosicionesOtrosSaldosCircular.actualizarCargaCuadratura");
		long inicio = System.currentTimeMillis();
		
		SQL _sql = new SQL();
		_sql.batch(con, Query, custodias, CustodiaActualizar.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CargarPosicionesArchivoL02Circular.actualizarCargaCuadratura", fin - inicio);
	}
	
	private void insertarCargaCuadratura(Connection con, List<CustodiaInsertar> custodias, String Query)  throws Exception {
		logger.debug("INICIO::CargarPosicionesOtrosSaldosCircular.insertarCargaCuadratura");
		long inicio = System.currentTimeMillis();
		
		SQL _sql = new SQL();
		_sql.batch(con, Query, custodias, CustodiaInsertar.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CargarPosicionesOtrosSaldosCircular.insertarCargaCuadratura", fin - inicio);
	}
	
	private void eliminarCargaCuadratura(Connection con, List<CuadraturaEliminar> custodia, String Query)  throws SQLException, Exception {
		logger.debug("INICIO::CargarPosicionesOtrosSaldosCircular.eliminarCargaCuadratura");
		long inicio = System.currentTimeMillis();
		SQL _sql = new SQL();
		
		_sql.batch(con, Query, custodia, CuadraturaEliminar.class);
	
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CargarPosicionesOtrosSaldosCircular.eliminarCargaCuadratura", fin - inicio);
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02", "connections.properties")) {
			
			EntradaCargarPosicionesOtrosSaldosCircular1518 entradaCargarPosicionesOtrosSaldosCircular1518 = new EntradaCargarPosicionesOtrosSaldosCircular1518();
			
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"CUS\",\"p_SobreEscribeArchivo\":\"N\",\"p_FechaCuadratura\":\"2025-04-17\",\"p_DatosArchivo\":[{\"Boveda\":\"BCS\",\"Nemotecnico\":\"PRUEBITA\",\"PosicionDisponible\":747,\"Mercado\":\"  \",\"Validacion\":\"OK\"}]}";
			Gson gson = Utils.getGsonBuilder();
			entradaCargarPosicionesOtrosSaldosCircular1518 = gson.fromJson(json, EntradaCargarPosicionesOtrosSaldosCircular1518.class);	
			CargarPosicionesOtrosSaldosCircular1518 cargarPosicionesOtrosSaldosCircular1518 = new CargarPosicionesOtrosSaldosCircular1518();
			
			try {
				SalidaCargarPosicionesOtrosSaldosCircular1518 salidaCargarPosicionesOtrosSaldosCircular1518 = cargarPosicionesOtrosSaldosCircular1518.logica(con, entradaCargarPosicionesOtrosSaldosCircular1518);
				System.out.println(salidaCargarPosicionesOtrosSaldosCircular1518.toString());
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
