package cl.bcs.socionegocio.enr;

import java.sql.Connection;
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

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.enr.EliminarBorradorPreferenciaPagoPortafolio.EntradaEliminarBorradorPreferenciaPagoPortafolio;
import cl.bcs.socionegocio.enr.ValidarExistenciaCuentaBancaria.EntradaValidarExistenciaBorradorCuentaBancaria;
import cl.bcs.socionegocio.enr.pojo.CuentaBancaria;
import cl.bcs.socionegocio.enr.pojo.EntradaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.PreferenciaPagoPortafolio;
import cl.bcs.socionegocio.enr.pojo.ResultadoAdministrarBorradorEntidad;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.Validacion;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EliminarBorradorCuentaBancaria implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(EliminarBorradorCuentaBancaria.class);

	private String SQL_DELETE_CUENTA_BANCARIA = loadResourceToString("cl/bcs/socionegocio/enr/sql/EliminarBorradorCuentaBancaria_DeleteCuentaBancaria.sql");
    private final String SQL_SELECT_PREFERENCIA_PAGO_PORTAFOLIO = loadResourceToString("cl/bcs/socionegocio/enr/sql/EliminarBorradorCuentaBancaria_SelectPreferenciaPago.sql");
	
	public static class EntradaEliminarBorradorCuentaBancaria extends EntradaAdministrarBorrador{
		public List<CuentaBancaria> p_CuentasBancarias;
		
		public EntradaEliminarBorradorCuentaBancaria(){
			this.p_CuentasBancarias = new ArrayList<CuentaBancaria>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaEliminarBorradorCuentaBancaria jin = gson.fromJson(in.toString(), EntradaEliminarBorradorCuentaBancaria.class);
		SalidaAdministrarBorrador out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAdministrarBorrador logica(Connection con, EntradaEliminarBorradorCuentaBancaria entrada) throws Exception {
		logger.info("INICIO::EliminarBorradorCuentaBancaria");
		long inicioServicio = System.currentTimeMillis();
		SalidaAdministrarBorrador out = new SalidaAdministrarBorrador();
		
		ValidarExistenciaCuentaBancaria validarExistenciaCuentaBancaria = new ValidarExistenciaCuentaBancaria();
		EntradaValidarExistenciaBorradorCuentaBancaria entradaValidarExistenciaBorradorCuentaBancaria = new EntradaValidarExistenciaBorradorCuentaBancaria();
		entradaValidarExistenciaBorradorCuentaBancaria.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaValidarExistenciaBorradorCuentaBancaria.p_CuentasBancarias = entrada.p_CuentasBancarias;
		
		List<Validacion> validacionesExistenciaCuentasBancarias = validarExistenciaCuentaBancaria.logica(con, entradaValidarExistenciaBorradorCuentaBancaria).validaciones;
		out.agregarValidacionesSalida(validacionesExistenciaCuentasBancarias);
		
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		eliminarCuentasBancarias(con, entrada.p_CuentasBancarias);
		
		out.P_OUT_REFID = obtenerIdsEliminados(entrada.p_CuentasBancarias);
		
	SalidaAdministrarBorrador outPreferenciaPago = eliminarPreferenciasPago(con, entrada);
	if (outPreferenciaPago.P_OUT_ERRORESCONTADOR > 0) {
	    out.agregarErroresMensajesSalida(outPreferenciaPago);
	}
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::EliminarBorradorCuentaBancaria", finServicio - inicioServicio);
		return out;
	}
    private SalidaAdministrarBorrador eliminarPreferenciasPago(Connection con, EntradaEliminarBorradorCuentaBancaria entrada) throws Exception {
	logger.debug("INICIO::EliminarBorradorCuentaBancaria.eliminarPreferenciasPago");
	long inicio = System.currentTimeMillis();
	EntradaEliminarBorradorPreferenciaPagoPortafolio in = new EntradaEliminarBorradorPreferenciaPagoPortafolio();
	in.p_IdEmpresa = entrada.p_IdEmpresa;
	in.p_IdUsuario = entrada.p_IdUsuario;
	in.p_IdModulo = entrada.p_IdModulo;
	in.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
	in.p_CodigoFormulario = entrada.p_CodigoFormulario;
	in.p_IdTipoCanal = entrada.p_IdTipoCanal;
	in.p_PreferenciasPagoPortafolio = obtenerPreferenciasPagoPortafolio(con, entrada);
	EliminarBorradorPreferenciaPagoPortafolio eliminarBorrador = new EliminarBorradorPreferenciaPagoPortafolio();
	SalidaAdministrarBorrador out = eliminarBorrador.logica(con, in);
	long fin = System.currentTimeMillis();
	logger.info("FIN::EliminarBorradorCuentaBancaria.eliminarPreferenciasPago", fin - inicio);
	return out;
    }
    private List<PreferenciaPagoPortafolio> obtenerPreferenciasPagoPortafolio(Connection con, EntradaEliminarBorradorCuentaBancaria entrada) throws Exception {
	logger.debug("INICIO::EliminarBorradorCuentaBancaria.obtenerPreferenciasPagoPortafolio");
	long inicio = System.currentTimeMillis();
	
	List<Long> idsCuentasBancarias = entrada.p_CuentasBancarias.parallelStream().map(cuentaBancaria -> {
	    return cuentaBancaria.IdCuentaBancaria;
	}).collect(Collectors.toList());
	HashMap<String, Object> parametros = new HashMap<>(1);
	parametros.put("idsCuentasBancarias", idsCuentasBancarias);
	SQL sql = new SQL();
	List<PreferenciaPagoPortafolio> preferenciasPagoPortafolioExistentes = sql.query(con, SQL_SELECT_PREFERENCIA_PAGO_PORTAFOLIO, parametros, PreferenciaPagoPortafolio.class);
	long fin = System.currentTimeMillis();
	logger.info("FIN::EliminarBorradorCuentaBancaria.obtenerPreferenciasPagoPortafolio", fin - inicio);
	return preferenciasPagoPortafolioExistentes;
    }
	private void eliminarCuentasBancarias(Connection con, List<CuentaBancaria> cuentasBancarias) throws Exception {
		logger.debug("INICIO::EliminarBorradorCuentaBancaria.eliminarCuentasBancarias");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(con, SQL_DELETE_CUENTA_BANCARIA, cuentasBancarias, CuentaBancaria.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::EliminarBorradorCuentaBancaria.eliminarCuentasBancarias", fin - inicio);
	}
	
	private List<ResultadoAdministrarBorradorEntidad> obtenerIdsEliminados(List<CuentaBancaria> cuentasBancarias){
		logger.trace("INICIO::EliminarBorradorCuentaBancaria.obtenerIdsEliminados");
		long inicio = System.currentTimeMillis();
		
		List<ResultadoAdministrarBorradorEntidad> idsInsertados = cuentasBancarias.parallelStream().map(cuentaBancaria->{
			ResultadoAdministrarBorradorEntidad resultadoCreacionBorradorEntidad = new ResultadoAdministrarBorradorEntidad();
			resultadoCreacionBorradorEntidad.IdInterno = cuentaBancaria.IdInterno;
			resultadoCreacionBorradorEntidad.IdEntidad = cuentaBancaria.IdCuentaBancaria;
			return resultadoCreacionBorradorEntidad;
		}).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		
		logger.trace("FIN::EliminarBorradorCuentaBancaria.obtenerIdsEliminados", fin - inicio);
		return idsInsertados;
    }
	
}
