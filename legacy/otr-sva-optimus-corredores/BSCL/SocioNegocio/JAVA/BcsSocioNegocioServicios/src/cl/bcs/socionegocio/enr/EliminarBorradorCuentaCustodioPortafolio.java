package cl.bcs.socionegocio.enr;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.socionegocio.enr.ValidarExistenciaBorradorCuentaCustodioPortafolio.EntradaValidarExistenciaBorradorCuentaCustodioPortafolio;
import cl.bcs.socionegocio.enr.pojo.CuentaCustodioPortafolio;
import cl.bcs.socionegocio.enr.pojo.EntradaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.ResultadoAdministrarBorradorEntidad;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.Validacion;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EliminarBorradorCuentaCustodioPortafolio implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(EliminarBorradorCuentaCustodioPortafolio.class);

	private String SQL_DELETE_CUENTA_CUSTODIO_PORTAFOLIO = loadResourceToString("cl/bcs/socionegocio/enr/sql/EliminarBorradorCuentaCustodioPortafolio_DeleteCuentaCustodioPortafolio.sql");
	
	public static class EntradaEliminarBorradorCuentaCustodioPortafolio extends EntradaAdministrarBorrador{
		public List<CuentaCustodioPortafolio> p_CuentasCustodioPortafolio;
		
		public EntradaEliminarBorradorCuentaCustodioPortafolio(){
			this.p_CuentasCustodioPortafolio = new ArrayList<>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaEliminarBorradorCuentaCustodioPortafolio jin = gson.fromJson(in.toString(), EntradaEliminarBorradorCuentaCustodioPortafolio.class);
		SalidaAdministrarBorrador out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAdministrarBorrador logica(Connection con, EntradaEliminarBorradorCuentaCustodioPortafolio entrada) throws Exception {
		logger.info("INICIO::EliminarBorradorCuentaCustodioPortafolio");
		long inicioServicio = System.currentTimeMillis();
		SalidaAdministrarBorrador out = new SalidaAdministrarBorrador();

		EntradaValidarExistenciaBorradorCuentaCustodioPortafolio entradaValidarExistenciaBorradorCuentaCustodioPortafolio = new EntradaValidarExistenciaBorradorCuentaCustodioPortafolio();
		entradaValidarExistenciaBorradorCuentaCustodioPortafolio.p_CuentasCustodioPortafolio = entrada.p_CuentasCustodioPortafolio;
		entradaValidarExistenciaBorradorCuentaCustodioPortafolio.p_IdEmpresa = entrada.p_IdEmpresa;

		ValidarExistenciaBorradorCuentaCustodioPortafolio validarExistenciaBorradorCuentaCustodioPortafolio = new ValidarExistenciaBorradorCuentaCustodioPortafolio();
		List<Validacion> validaciones = validarExistenciaBorradorCuentaCustodioPortafolio.logica(con, entradaValidarExistenciaBorradorCuentaCustodioPortafolio).validaciones;
		out.agregarValidacionesSalida(validaciones);
		
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		eliminarCuentasCustodioPortafolio(con, entrada.p_CuentasCustodioPortafolio);
		
		out.P_OUT_REFID = obtenerIdsEliminados(entrada.p_CuentasCustodioPortafolio);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::EliminarBorradorCuentaCustodioPortafolio", finServicio - inicioServicio);
		return out;
	}
	
	private void eliminarCuentasCustodioPortafolio(Connection con, List<CuentaCustodioPortafolio> cuentasCustodioPortafolio) throws Exception {
		logger.debug("INICIO::EliminarBorradorCuentaCustodioPortafolio.eliminarCuentasCustodioPortafolio");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(con, SQL_DELETE_CUENTA_CUSTODIO_PORTAFOLIO, cuentasCustodioPortafolio, CuentaCustodioPortafolio.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::EliminarBorradorCuentaCustodioPortafolio.eliminarCuentasCustodioPortafolio", fin - inicio);
	}
	
	private List<ResultadoAdministrarBorradorEntidad> obtenerIdsEliminados(List<CuentaCustodioPortafolio> cuentasCustodioPortafolio){
		logger.trace("INICIO::EliminarBorradorCuentaCustodioPortafolio.obtenerIdsEliminados");
		long inicio = System.currentTimeMillis();
		
		List<ResultadoAdministrarBorradorEntidad> idsEliminados = cuentasCustodioPortafolio.parallelStream().map(cuentaCustodioPortafolio->{
			ResultadoAdministrarBorradorEntidad resultadoCreacionBorradorEntidad = new ResultadoAdministrarBorradorEntidad();
			resultadoCreacionBorradorEntidad.IdInterno = cuentaCustodioPortafolio.IdInterno;
			resultadoCreacionBorradorEntidad.IdEntidad = cuentaCustodioPortafolio.IdCuentaCustodioPortafolio;
			return resultadoCreacionBorradorEntidad;
		}).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		
		logger.trace("FIN::EliminarBorradorCuentaCustodioPortafolio.obtenerIdsEliminados", fin - inicio);
		return idsEliminados;
	}

}