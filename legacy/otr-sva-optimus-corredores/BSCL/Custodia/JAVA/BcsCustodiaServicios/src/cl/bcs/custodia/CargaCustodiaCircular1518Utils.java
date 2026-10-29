package cl.bcs.custodia;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.SQL;

public class CargaCustodiaCircular1518Utils {
	
	static final Logger logger = LogManager.getLogger(CargaCustodiaCircular1518Utils.class);
	private static FileLoader loader;
	private static String SQL_SELECT_CUENTAS_CUSTODIA;
	private static String SQL_SELECT_INSTRUMENTOS;
	private static String SQL_SELECT_INSTRUMENTOS_ARCHIVO;
	
	static {
		loader = new FileLoader();
		SQL_SELECT_CUENTAS_CUSTODIA = loader.loadResourceToString("cl/bcs/custodia/sql/CargaCustodiaCircular1518Utils_SelectCuentasCustodia.sql");
		SQL_SELECT_INSTRUMENTOS = loader.loadResourceToString("cl/bcs/custodia/sql/CargaCustodiaCircular1518Utils_SelectInstrumentos.sql");
		SQL_SELECT_INSTRUMENTOS_ARCHIVO = loader.loadResourceToString("cl/bcs/custodia/sql/CargaCustodiaCircular1518Utils_SelectInstrumentosArchivo.sql");
	}
	
	public static class CuentaCustodia extends BcsPojo {
		public String CuentaRelacionadoDCV;
		public String CuentaPropiaDCV;
		public String CuentaRetencionDCV;												/// Linea Ingresada
	}
	
	public static class Instrumento extends BcsPojo {
		public String Nemotecnico;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
	}
	
	public static CuentaCustodia obtenerCuentaCustodia(Connection con, Long idEmpresa) throws Exception {
		logger.debug("INICIO::CargaCustodiaCircular1518Utils.obtenerCuentaCustodia");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("IdEmpresa", idEmpresa);
		
		SQL _sql = new SQL();
		List<CuentaCustodia> cuentasCustodia = _sql.query(con, SQL_SELECT_CUENTAS_CUSTODIA, parametros,  CuentaCustodia.class);
		
		if(cuentasCustodia.isEmpty()) {
			throw new Exception("No existen cuentas custodias configuradas");
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CargaCustodiaCircular1518Utils.obtenerCuentaCustodia", fin - inicio);
		return cuentasCustodia.get(0);
	}
	
	
	public static Map<String, Instrumento> obtenerInstrumentosPorNemo(Connection con, List<String> nemotecnicos, List<String> mercados) throws Exception {
		logger.debug("INICIO::CargaCustodiaCircular1518Utils.obtenerCuentaCustodia");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		int FiltroIdClasificacionTipoInstrumento = 0;
		parametros.put("Nemotecnico", nemotecnicos);
		
		if(mercados.size() > 0) {
			FiltroIdClasificacionTipoInstrumento = 1;
			mercados = mercados.stream().distinct().collect(Collectors.toList());
		}
		
		parametros.put("FiltroIdClasificacionTipoInstrumento", FiltroIdClasificacionTipoInstrumento);
		parametros.put("IdClasificacionTipoInstrumento", mercados);
		
		SQL _sql = new SQL();
		List<Instrumento> instrumentos = _sql.query(con, SQL_SELECT_INSTRUMENTOS, parametros,  Instrumento.class);
		
		Map<String, Instrumento> instrumentosPorNemo = instrumentos.stream().collect(Collectors.toMap(instrumento->instrumento.Nemotecnico + instrumento.IdClasificacionTipoInstrumento, instrumento->instrumento));
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CargaCustodiaCircular1518Utils.obtenerCuentaCustodia", fin - inicio);
		return instrumentosPorNemo;
	}
	
	public static Map<String, Instrumento> obtenerInstrumentosPorNemo(Connection con, List<String> nemotecnicos, String origen) throws Exception {
		logger.debug("INICIO::CargaCustodiaCircular1518Utils.obtenerCuentaCustodia");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("Nemotecnico", nemotecnicos);
		parametros.put("Archivo", origen);
		
		SQL _sql = new SQL();
		List<Instrumento> instrumentos = _sql.query(con, SQL_SELECT_INSTRUMENTOS_ARCHIVO, parametros,  Instrumento.class);
		
		Map<String, Instrumento> instrumentosPorNemo = instrumentos.stream().collect(Collectors.toMap(instrumento->instrumento.Nemotecnico, instrumento->instrumento));
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CargaCustodiaCircular1518Utils.obtenerCuentaCustodia", fin - inicio);
		return instrumentosPorNemo;
	}
}
