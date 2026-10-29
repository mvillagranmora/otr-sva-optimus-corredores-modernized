package cl.bcs.APV;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.APV.pojo.BancoPrevired;
import cl.bcs.APV.pojo.BancoPreviredEntrada;
import cl.bcs.APV.pojo.ConstantesBancoPrevired;
import cl.bcs.APV.pojo.CuentaBanco;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class ValidarBancoPrevired implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ValidarBancoPrevired.class);
	
	private final ConstantesBancoPrevired _c = new ConstantesBancoPrevired();
	
	public final String SQL_SELECT_BANCOS_PREVIRED = loadResourceToString("cl/bcs/APV/sql/ValidarBancoPrevired_SelectBancosPrevired.sql");
	public final String SQL_SELECT_CUENTAS_BANCO = loadResourceToString("cl/bcs/APV/sql/ValidarBancoPrevired_SelectCuentasBanco.sql");
	
	public static class ResultadoValidacion extends BcsPojo {
		public List<BancoPreviredEntrada> BancosPreviredCorrectos;
		public List<BancoPreviredEntrada> BancosPreviredInCorrectos;
		public List<MensajeSalida> MensajesErrores;
		
		public ResultadoValidacion() {
			this.BancosPreviredCorrectos = new ArrayList<BancoPreviredEntrada>();
			this.BancosPreviredInCorrectos = new ArrayList<BancoPreviredEntrada>();
			this.MensajesErrores = new ArrayList<MensajeSalida>();
		}
	}
	
	public ResultadoValidacion validarCreacionBancoPrevired(Connection con, List<BancoPreviredEntrada> bancosPrevired, Long idEmpresa) throws Exception{
		logger.debug("INICIO::ValidarBancoPrevired.validarCreacionBancoPrevired");
		long inicio = System.currentTimeMillis();
		Map<String, BancoPrevired> bancosPreviredPorCodigoBancoPreviredExistentes = obtenerBancosPreviredPorCodigoBancoPreviredExistentes(con, bancosPrevired);
		Map<Long, CuentaBanco> cuentaBancosPorIdCuentaBancoExistentes = obtenerCuentasBancoPorIdCuentaBancoExistentes(con, bancosPrevired);
		
		ResultadoValidacion resultadoValidacion = validarReglasGenerales(con, bancosPrevired, idEmpresa);
		
		if(noExistenErroresDeReglasGenerales(resultadoValidacion)) {
			for(BancoPreviredEntrada bancoPrevired : bancosPrevired) {
				boolean esCorrecto = true;
				
				if(existeBancoPrevired(bancosPreviredPorCodigoBancoPreviredExistentes, bancoPrevired)) {
					esCorrecto = false;
					HashMap<String, String> parametros = obtenerParametrosMensajeErrores(bancoPrevired);
					resultadoValidacion.MensajesErrores.add(new MensajeSalida(bancoPrevired.IdInterno,MensajeSalida.MENSAJE_ERROR,_c.MENSAJE_BANCO_PREVIRED_YA_EXISTE,parametros,con,idEmpresa));
				}
				if(noExisteCuentaBanco(cuentaBancosPorIdCuentaBancoExistentes, bancoPrevired)) {
					esCorrecto = false;
					HashMap<String, String> parametros = obtenerParametrosMensajeErrores(bancoPrevired);
					resultadoValidacion.MensajesErrores.add(new MensajeSalida(bancoPrevired.IdInterno,MensajeSalida.MENSAJE_ERROR,_c.MENSAJE_CUENTA_BANCO_NO_EXISTE,parametros,con,idEmpresa));
				}
				
				if(esCorrecto) {
					resultadoValidacion.BancosPreviredCorrectos.add(bancoPrevired);
				}else {
					resultadoValidacion.BancosPreviredInCorrectos.add(bancoPrevired);
				}
			}
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ValidarBancoPrevired.validarCreacionBancoPrevired", fin - inicio);
		return resultadoValidacion;
	}
	
	public ResultadoValidacion validarModificacionBancoPrevired(Connection con, List<BancoPreviredEntrada> bancosPrevired, Long idEmpresa) throws Exception{
		logger.debug("INICIO::ValidarBancoPrevired.validarModificacionBancoPrevired");
		long inicio = System.currentTimeMillis();
		Map<String, BancoPrevired> bancosPreviredPorCodigoBancoPreviredExistentes = obtenerBancosPreviredPorCodigoBancoPreviredExistentes(con, bancosPrevired);
		Map<Long, CuentaBanco> cuentaBancosPorIdCuentaBancoExistentes = obtenerCuentasBancoPorIdCuentaBancoExistentes(con, bancosPrevired);
		
		ResultadoValidacion resultadoValidacion = validarReglasGenerales(con, bancosPrevired, idEmpresa);
		
		if(noExistenErroresDeReglasGenerales(resultadoValidacion)) {
			for(BancoPreviredEntrada bancoPrevired : bancosPrevired) {
				boolean esCorrecto = true;
				
				if(noExisteBancoPrevired(bancosPreviredPorCodigoBancoPreviredExistentes, bancoPrevired)) {
					esCorrecto = false;
					HashMap<String, String> parametros = obtenerParametrosMensajeErrores(bancoPrevired);
					resultadoValidacion.MensajesErrores.add(new MensajeSalida(bancoPrevired.IdInterno,MensajeSalida.MENSAJE_ERROR,_c.MENSAJE_NO_EXISTE_BANCO_PREVIRED,parametros,con,idEmpresa));
				}
				if(noExisteCuentaBanco(cuentaBancosPorIdCuentaBancoExistentes, bancoPrevired)) {
					esCorrecto = false;
					HashMap<String, String> parametros = obtenerParametrosMensajeErrores(bancoPrevired);
					resultadoValidacion.MensajesErrores.add(new MensajeSalida(bancoPrevired.IdInterno,MensajeSalida.MENSAJE_ERROR,_c.MENSAJE_CUENTA_BANCO_NO_EXISTE,parametros,con,idEmpresa));
				}
				
				if(esCorrecto) {
					resultadoValidacion.BancosPreviredCorrectos.add(bancoPrevired);
				}else {
					resultadoValidacion.BancosPreviredInCorrectos.add(bancoPrevired);
				}
			}
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ValidarBancoPrevired.validarModificacionBancoPrevired", fin - inicio);
		return resultadoValidacion;
	}
	
	public ResultadoValidacion validarEliminacionBancoPrevired(Connection con, List<BancoPreviredEntrada> bancosPrevired, Long idEmpresa) throws Exception{
		logger.debug("INICIO::ValidarBancoPrevired.validarEliminacionBancoPrevired");
		long inicio = System.currentTimeMillis();
		Map<String, BancoPrevired> bancosPreviredPorCodigoBancoPreviredExistentes = obtenerBancosPreviredPorCodigoBancoPreviredExistentes(con, bancosPrevired);
		ResultadoValidacion resultadoValidacion = validarReglasGenerales(con, bancosPrevired, idEmpresa);
		
		if(noExistenErroresDeReglasGenerales(resultadoValidacion)) {
			for(BancoPreviredEntrada bancoPrevired : bancosPrevired) {
				if(noExisteBancoPrevired(bancosPreviredPorCodigoBancoPreviredExistentes, bancoPrevired)) {
					resultadoValidacion.BancosPreviredInCorrectos.add(bancoPrevired);
					HashMap<String, String> parametros = obtenerParametrosMensajeErrores(bancoPrevired);
					resultadoValidacion.MensajesErrores.add(new MensajeSalida(bancoPrevired.IdInterno,MensajeSalida.MENSAJE_ERROR,_c.MENSAJE_NO_EXISTE_BANCO_PREVIRED,parametros,con,idEmpresa));
				}else {
					resultadoValidacion.BancosPreviredCorrectos.add(bancoPrevired);
				}
			}
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ValidarBancoPrevired.validarEliminacionBancoPrevired", fin - inicio);
		return resultadoValidacion;
	}
	
	private Map<String, BancoPrevired> obtenerBancosPreviredPorCodigoBancoPreviredExistentes(Connection con, List<BancoPreviredEntrada> bancosPrevired) throws Exception{
		logger.debug("INICIO::ValidarBancoPrevired.obtenerBancosPreviredPorCodigoBancoPreviredExistentes");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		List<String> codigosBancoPrevired = obtenerParametroQueryCodigosBanco(bancosPrevired);
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("CodigoBancoPrevired", codigosBancoPrevired);
		List<BancoPrevired> bancosPreviredExistentes = sql.query(con, SQL_SELECT_BANCOS_PREVIRED, parametros, BancoPrevired.class);
		Map<String, BancoPrevired> bancosPreviredExistentesPorCodigoBancoPrevired = bancosPreviredExistentes.parallelStream().collect(Collectors.toMap(bancoPrevired->bancoPrevired.CodigoBancoPrevired, bancoPrevired->bancoPrevired));
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ValidarBancoPrevired.obtenerBancosPreviredPorCodigoBancoPreviredExistentes", fin - inicio);
		return bancosPreviredExistentesPorCodigoBancoPrevired;
	}
	
	private List<Long> obtenerParametroQueryIdsCuentaBanco(List<BancoPreviredEntrada> bancosPrevired){
		logger.debug("INICIO::ValidarBancoPrevired.obtenerParametroQueryIdsCuentaBanco");
		long inicio = System.currentTimeMillis();
		List<Long> idsCuentaBanco = bancosPrevired.parallelStream().map(bancoPrevired->bancoPrevired.IdCuentaBanco).distinct().collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ValidarBancoPrevired.obtenerParametroQueryIdsCuentaBanco", fin - inicio);
		return idsCuentaBanco;
	}
	
	private List<String> obtenerParametroQueryCodigosBanco(List<BancoPreviredEntrada> bancosPrevired){
		logger.debug("INICIO::ValidarBancoPrevired.obtenerParametroQueryCodigosBanco");
		long inicio = System.currentTimeMillis();
		List<String> codigosBanco = bancosPrevired.parallelStream().map(bancoPrevired->bancoPrevired.CodigoBancoPrevired).distinct().collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ValidarBancoPrevired.obtenerParametroQueryCodigosBanco", fin - inicio);
		return codigosBanco;
	}
	
	private Map<Long, CuentaBanco> obtenerCuentasBancoPorIdCuentaBancoExistentes(Connection con, List<BancoPreviredEntrada> bancosPrevired) throws Exception{
		logger.debug("INICIO::ValidarBancoPrevired.obtenerCuentasBancoPorIdCuentaBancoExistentes");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		List<Long> idsCuentaBanco = obtenerParametroQueryIdsCuentaBanco(bancosPrevired);
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("IdCuentaBanco", idsCuentaBanco);
		List<CuentaBanco> bancosExistentes = sql.query(con, SQL_SELECT_CUENTAS_BANCO, parametros, CuentaBanco.class);
		Map<Long, CuentaBanco> cuentasBancoExistentesPorIdCuentasBanco = bancosExistentes.parallelStream().collect(Collectors.toMap(cuenta->cuenta.IdCuentaBanco, cuenta->cuenta));
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ValidarBancoPrevired.obtenerCuentasBancoPorIdCuentaBancoExistentes", fin - inicio);
		return cuentasBancoExistentesPorIdCuentasBanco;
	}
	
	private ResultadoValidacion validarReglasGenerales(Connection con, List<BancoPreviredEntrada> bancosPrevired, Long idEmpresa) throws Exception{
		logger.debug("INICIO::ValidarBancoPrevired.validarReglasGenerales");
		long inicio = System.currentTimeMillis();
		ResultadoValidacion resultadoValidacion = new ResultadoValidacion();
		if(esEntradaVacia(bancosPrevired)) {
			resultadoValidacion.MensajesErrores.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,_c.MENSAJE_NO_SE_INGRESARON_BANCOS_PREVIRED,new HashMap<String,String>(),con,idEmpresa));
		}
		
		Map<Long, List<BancoPreviredEntrada>> bancosPreviredAgrupadosPorIdInterno = bancosPrevired.parallelStream().collect(Collectors.groupingBy(bancoPrevired->bancoPrevired.IdInterno));
		Map<String, List<BancoPreviredEntrada>> bancosPreviredAgrupadosPorCodigoBancoPrevired = bancosPrevired.parallelStream().collect(Collectors.groupingBy(bancoPrevired->bancoPrevired.CodigoBancoPrevired));
		
		for(Entry<Long, List<BancoPreviredEntrada>> agrupacion : bancosPreviredAgrupadosPorIdInterno.entrySet()) {
			Long idInterno = agrupacion.getKey();
			List<BancoPreviredEntrada> bancosPreviredAgrupacion = agrupacion.getValue();
			if(esAgrupacionConRegistrosRepetidos(bancosPreviredAgrupacion)) {
				resultadoValidacion.BancosPreviredInCorrectos.addAll(bancosPrevired);
				HashMap<String,String> parametros = new HashMap<String,String>();
				parametros.put("IdInterno", String.valueOf(idInterno));
				resultadoValidacion.MensajesErrores.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,_c.MENSAJE_ID_INTERNO_REPETIDO_ENTRADA,parametros,con,idEmpresa));
			}
		}
		
		for(Entry<String, List<BancoPreviredEntrada>> agrupacion : bancosPreviredAgrupadosPorCodigoBancoPrevired.entrySet()) {
			String codigoBancoPrevired = agrupacion.getKey();
			List<BancoPreviredEntrada> bancosPreviredAgrupacion = agrupacion.getValue();
			if(esAgrupacionConRegistrosRepetidos(bancosPreviredAgrupacion)) {
				resultadoValidacion.BancosPreviredInCorrectos.addAll(bancosPrevired);
				HashMap<String,String> parametros = new HashMap<String,String>();
				parametros.put("CodigoBancoPrevired", codigoBancoPrevired);
				resultadoValidacion.MensajesErrores.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,_c.MENSAJE_CODIGO_BANCO_PREVIRED_REPETIDO_ENTRADA,new HashMap<String,String>(),con,idEmpresa));
			}
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ValidarBancoPrevired.validarReglasGenerales", fin - inicio);
		return resultadoValidacion;
	}
	
	private Boolean esEntradaVacia(List<BancoPreviredEntrada> bancosPrevired) {
		logger.debug("INICIO::ValidarBancoPrevired.esEntradaVacia");
		long inicio = System.currentTimeMillis();
		Boolean esEntradaVacia = bancosPrevired.size() == 0;
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ValidarBancoPrevired.esEntradaVacia", fin - inicio);
		return esEntradaVacia;
	}
	
	private Boolean esAgrupacionConRegistrosRepetidos(List<BancoPreviredEntrada> bancosPreviredAgrupacion) {
		logger.debug("INICIO::ValidarBancoPrevired.esAgrupacionConRegistrosRepetidos");
		long inicio = System.currentTimeMillis();
		Boolean esAgrupacionConRegistrosRepetidos = bancosPreviredAgrupacion.size() > 1;
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ValidarBancoPrevired.esAgrupacionConRegistrosRepetidos", fin - inicio);
		return esAgrupacionConRegistrosRepetidos;
	}
	
	private Boolean noExistenErroresDeReglasGenerales(ResultadoValidacion resultadoValidacion) {
		logger.debug("INICIO::ValidarBancoPrevired.noExistenErroresDeReglasGenerales");
		long inicio = System.currentTimeMillis();
		Boolean noExistenErroresDeReglasGenerales = resultadoValidacion.MensajesErrores.size() == 0;
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ValidarBancoPrevired.noExistenErroresDeReglasGenerales", fin - inicio);
		return noExistenErroresDeReglasGenerales;
	}
	
	private Boolean existeBancoPrevired(Map<String, BancoPrevired> bancosPreviredPorCodigoBancoPreviredExistentes, BancoPreviredEntrada bancoPrevired) {
		logger.debug("INICIO::ValidarBancoPrevired.existeBancoPrevired");
		long inicio = System.currentTimeMillis();
		BancoPrevired bancoPreviredExistente = bancosPreviredPorCodigoBancoPreviredExistentes.get(bancoPrevired.CodigoBancoPrevired);
		Boolean existeBancoPrevired = bancoPreviredExistente != null;
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ValidarBancoPrevired.existeBancoPrevired", fin - inicio);
		return existeBancoPrevired;
	}
	
	private Boolean noExisteCuentaBanco(Map<Long, CuentaBanco> cuentaBancosPorIdCuentaBancoExistentes, BancoPreviredEntrada bancoPrevired) {
		logger.debug("INICIO::ValidarBancoPrevired.noExisteCuentaBanco");
		long inicio = System.currentTimeMillis();
		CuentaBanco cuentaBancoExistente = cuentaBancosPorIdCuentaBancoExistentes.get(bancoPrevired.IdCuentaBanco);
		Boolean noExisteCuentaBanco = cuentaBancoExistente == null;
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ValidarBancoPrevired.noExisteCuentaBanco", fin - inicio);
		return noExisteCuentaBanco;
	}
	
	private Boolean noExisteBancoPrevired(Map<String, BancoPrevired> bancosPreviredPorCodigoBancoPreviredExistentes, BancoPreviredEntrada bancoPrevired) {
		logger.debug("INICIO::ValidarBancoPrevired.noExisteBancoPrevired");
		long inicio = System.currentTimeMillis();
		BancoPrevired bancoPreviredExistente = bancosPreviredPorCodigoBancoPreviredExistentes.get(bancoPrevired.CodigoBancoPrevired);
		Boolean noExisteBancoPrevired = bancoPreviredExistente == null;
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ValidarBancoPrevired.noExisteBancoPrevired", fin - inicio);
		return noExisteBancoPrevired;
	}
	
	private HashMap<String, String> obtenerParametrosMensajeErrores(BancoPreviredEntrada bancoPrevired) {
		logger.debug("INICIO::ValidarBancoPrevired.obtenerParametrosMensajeErrores");
		long inicio = System.currentTimeMillis();
		HashMap<String, String> parametros = new HashMap<String, String>();
		parametros.put("IdInterno", String.valueOf(bancoPrevired.IdInterno));
		parametros.put("CodigoBancoPrevired", bancoPrevired.CodigoBancoPrevired);
		parametros.put("IdCuentaBanco", String.valueOf(bancoPrevired.IdCuentaBanco));
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ValidarBancoPrevired.obtenerParametrosMensajeErrores", fin - inicio);
		return parametros;
	}
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
