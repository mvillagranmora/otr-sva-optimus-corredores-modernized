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

import cl.bcs.socionegocio.enr.ValidarExistenciaBorradorPreferenciaPagoPortafolio.EntradaValidarExistenciaBorradorPreferenciaPagoPortafolio;
import cl.bcs.socionegocio.enr.pojo.EntradaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.PreferenciaPagoPortafolio;
import cl.bcs.socionegocio.enr.pojo.ResultadoAdministrarBorradorEntidad;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.Validacion;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EliminarBorradorPreferenciaPagoPortafolio implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(EliminarBorradorPreferenciaPagoPortafolio.class);

	private String SQL_DELETE_PREFERENCIA_PAGO_PORTAFOLIO = loadResourceToString("cl/bcs/socionegocio/enr/sql/EliminarBorradorPreferenciaPagoPortafolio_DeletePreferenciaPagoPortafolio.sql");
	
	public static class EntradaEliminarBorradorPreferenciaPagoPortafolio extends EntradaAdministrarBorrador{
		public List<PreferenciaPagoPortafolio> p_PreferenciasPagoPortafolio;
		
		public EntradaEliminarBorradorPreferenciaPagoPortafolio(){
			this.p_PreferenciasPagoPortafolio = new ArrayList<>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaEliminarBorradorPreferenciaPagoPortafolio jin = gson.fromJson(in.toString(), EntradaEliminarBorradorPreferenciaPagoPortafolio.class);
		SalidaAdministrarBorrador out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAdministrarBorrador logica(Connection con, EntradaEliminarBorradorPreferenciaPagoPortafolio entrada) throws Exception {
		logger.info("INICIO::EliminarBorradorPreferenciaPagoPortafolio");
		long inicioServicio = System.currentTimeMillis();
		SalidaAdministrarBorrador out = new SalidaAdministrarBorrador();

		EntradaValidarExistenciaBorradorPreferenciaPagoPortafolio entradaValidarExistenciaBorradorPreferenciaPagoPortafolio = new EntradaValidarExistenciaBorradorPreferenciaPagoPortafolio();
		entradaValidarExistenciaBorradorPreferenciaPagoPortafolio.p_PreferenciasPagoPortafolio = entrada.p_PreferenciasPagoPortafolio;
		entradaValidarExistenciaBorradorPreferenciaPagoPortafolio.p_IdEmpresa = entrada.p_IdEmpresa;

		ValidarExistenciaBorradorPreferenciaPagoPortafolio validarExistenciaBorradorPreferenciaPagoPortafolio = new ValidarExistenciaBorradorPreferenciaPagoPortafolio();
		List<Validacion> validaciones = validarExistenciaBorradorPreferenciaPagoPortafolio.logica(con, entradaValidarExistenciaBorradorPreferenciaPagoPortafolio).validaciones;
		out.agregarValidacionesSalida(validaciones);

		if (out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}

		eliminarPreferenciasPagoPortafolio(con, entrada.p_PreferenciasPagoPortafolio);

		out.P_OUT_REFID = obtenerIdsEliminados(entrada.p_PreferenciasPagoPortafolio);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::EliminarBorradorPreferenciaPagoPortafolio", finServicio - inicioServicio);
		return out;
	}
	
	private void eliminarPreferenciasPagoPortafolio(Connection con, List<PreferenciaPagoPortafolio> preferenciasPagoPortafolio) throws Exception {
		logger.debug("INICIO::EliminarBorradorPreferenciaPagoPortafolio.eliminarPreferenciasPagoPortafolio");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(con, SQL_DELETE_PREFERENCIA_PAGO_PORTAFOLIO, preferenciasPagoPortafolio, PreferenciaPagoPortafolio.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::EliminarBorradorPreferenciaPagoPortafolio.eliminarPreferenciasPagoPortafolio", fin - inicio);
	}
	
	private List<ResultadoAdministrarBorradorEntidad> obtenerIdsEliminados(List<PreferenciaPagoPortafolio> preferenciasPagoPortafolio){
		logger.trace("INICIO::EliminarBorradorPreferenciaPagoPortafolio.obtenerIdsEliminados");
		long inicio = System.currentTimeMillis();
		
		List<ResultadoAdministrarBorradorEntidad> idsEliminados = preferenciasPagoPortafolio.parallelStream().map(preferenciaPagoPortafolio->{
			ResultadoAdministrarBorradorEntidad resultadoCreacionBorradorEntidad = new ResultadoAdministrarBorradorEntidad();
			resultadoCreacionBorradorEntidad.IdInterno = preferenciaPagoPortafolio.IdInterno;
			resultadoCreacionBorradorEntidad.IdEntidad = preferenciaPagoPortafolio.IdPreferenciaPagoPortafolio;
			return resultadoCreacionBorradorEntidad;
		}).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		
		logger.trace("FIN::EliminarBorradorPreferenciaPagoPortafolio.obtenerIdsEliminados", fin - inicio);
		return idsEliminados;
	}
	
}