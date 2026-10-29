package cl.bcs.mesa.jobs;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.mesa.utils.ConsultarConfiguracionMesa;
import cl.bcs.mesa.utils.ConsultarConfiguracionMesa.ConfiguracionMesa;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class LiberaBloqueoEdicionPactos extends BcsJob implements JavaCall{

	private static final Logger logger = LogManager.getLogger(LiberaBloqueoEdicionPactos.class);

	private final String SQL_SELECT_OPERACIONES_BLOQUEADAS = loadResourceToString("cl/bcs/mesa/jobs/sql/LiberaBloqueoEdicionPactos_SelectOperacionesBloqueadas.sql");
	private final String SQL_UPDATE_LIBERAR_OPERACIONES = loadResourceToString("cl/bcs/mesa/jobs/sql/LiberaBloqueoEdicionPactos_UpdateLiberarOperaciones.sql");
	private final String SQL_SELECT_ID_USUARIO_POR_CODIGO = loadResourceToString("cl/bcs/mesa/jobs/sql/LiberaBloqueoEdicionPactos_SelectIdUsuarioPorCodigo.sql");

	public static class OperacionBloqueada {
		public Long IdOperacion;
		public Long FolioOperacion;
		public Timestamp FechaHoraInicioEdicion;
		public String BloqueoPorEdicion;
		public String CodigoUsuarioBloqueoEdicion;
		public String IdTipoOperacion;
		public Long IdCliente;
	}

	public static class UsuarioPorCodigo {
		public Long IdUsuario;
	}

	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica(con, false);
	}

	private Long resolverIdUsuarioPorCodigo(Connection con, Long idEmpresa, String codigoUsuario) throws Exception {
		if (codigoUsuario == null || codigoUsuario.trim().isEmpty()) {
			return 0L;
		}
		SQL sqlUsuario = new SQL();
		HashMap<String,Object> params = new HashMap<>();
		params.put("CodigoUsuario", codigoUsuario);
		params.put("IdEmpresa", idEmpresa);
		List<UsuarioPorCodigo> resultado = sqlUsuario.query(con, SQL_SELECT_ID_USUARIO_POR_CODIGO, params, UsuarioPorCodigo.class);
		return (!resultado.isEmpty() && resultado.get(0).IdUsuario != null) ? resultado.get(0).IdUsuario : 0L;
	}

	private void registrarLog(Connection con, Long idEmpresa, String fechaHoraSistema, OperacionBloqueada ope, long idProceso) throws Exception {
		Long idUsuarioBloqueo = resolverIdUsuarioPorCodigo(con, idEmpresa, ope.CodigoUsuarioBloqueoEdicion);

		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();

		log.IdCabeceraLog = ope.IdOperacion;
		log.IdTipoLog = "INFORMATIVO";
		log.IdModulo = "MRF";
		log.IdUsuario = idUsuarioBloqueo;
		log.IdEmpresa = idEmpresa;
		log.FechaSistemaLog = fechaHoraSistema.substring(0, 10);
		log.IdUsuarioAtendido = idUsuarioBloqueo;
		log.IdCliente = ope.IdCliente != null ? ope.IdCliente : 0L;
		log.EstadoCliente = "";
		log.NombreCliente = "";
		log.IdTipoDocumentoIdentidad = "";
		log.IdDocumentoIdentidad = "";
		log.IdPortafolio = 0L;
		log.EstadoPortafolio = "";
		log.IdTipoRegistro = "LIBERACIONPACTOBLOQUEADO";
		log.EstadoRegistro = "VIG";
		log.IdTipoOperacion = "";
		log.Folio = ope.FolioOperacion != null ? ope.FolioOperacion : 0L;
		log.Cantidad = BigDecimal.ZERO;
		log.PrecioTasa = BigDecimal.ZERO;
		log.Monto = BigDecimal.ZERO;
		log.IdMonedaOrigen = "";
		log.IdMonedaLiquidacion = "";
		log.TipoCambio = "";
		log.Nemo = "";
		log.IdProceso = idProceso;
		log.EstadoProceso = "VIG";
		log.NombreProceso = "LiberaBloqueoEdicionPactos";
		entradaAgregarLog.p_Log.add(log);
		entradaAgregarLog.agregarDetalleLogBasico(
				ope.IdOperacion, 0L, "BloqueoPorEdicion", "S", "N", ope.IdTipoOperacion);
		entradaAgregarLog.agregarDetalleLogBasico(
				ope.IdOperacion, 0L, "IdOperacion", ope.IdOperacion.toString(), "", ope.IdTipoOperacion);

		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(con, entradaAgregarLog);
	}

	public boolean logica(Connection con, boolean debug) throws InterruptedException, Throwable {
		logger.info("INICIO::LiberaBloqueoEdicionPactos");

		Long idEmpresa = debug ? 1L : Long.valueOf(this.getParam("IdEmpresa"));

		ConfiguracionMesa configuracion;
		try {
			configuracion = new ConsultarConfiguracionMesa().logica(con, idEmpresa, "MERF", "TIEMPOMAXEDICIONPACTO");
		} catch (Exception e) {
			logger.warn("LiberaBloqueoEdicionPactos > logica > no existe configuracion TIEMPOMAXEDICIONPACTO para IdEmpresa=" + idEmpresa + ", no se libera ninguna operacion");
			logger.info("FIN::LiberaBloqueoEdicionPactos");
			return true;
		}

		int tiempoMaximoMinutos;
		try {
			tiempoMaximoMinutos = Integer.parseInt(configuracion.Valor.trim());
		} catch (Exception e) {
			logger.warn("LiberaBloqueoEdicionPactos > logica > TIEMPOMAXEDICIONPACTO invalido ('" + configuracion.Valor + "') para IdEmpresa=" + idEmpresa + ", no se libera ninguna operacion");
			logger.info("FIN::LiberaBloqueoEdicionPactos");
			return true;
		}

		String fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaUTC(con, idEmpresa);
		Timestamp fechaHoraActual = Timestamp.valueOf(fechaHoraSistema);

		SQL _sql = new SQL();
		HashMap<String,Object> paramsSelect = new HashMap<>();
		paramsSelect.put("IdEmpresa", idEmpresa);
		List<OperacionBloqueada> operacionesBloqueadas = _sql.query(con, SQL_SELECT_OPERACIONES_BLOQUEADAS, paramsSelect, OperacionBloqueada.class);

		long idProceso = this.getJobId();

		List<OperacionBloqueada> operacionesALiberar = new ArrayList<>();
		for (OperacionBloqueada ope : operacionesBloqueadas) {
			if (ope.FechaHoraInicioEdicion == null) {
				continue;
			}
			long minutosTranscurridos = (fechaHoraActual.getTime() - ope.FechaHoraInicioEdicion.getTime()) / 60000L;
			if (minutosTranscurridos > tiempoMaximoMinutos) {
				registrarLog(con, idEmpresa, fechaHoraSistema, ope, idProceso);
				ope.BloqueoPorEdicion = "N";
				ope.CodigoUsuarioBloqueoEdicion = "";
				operacionesALiberar.add(ope);
			}
		}

		if (!operacionesALiberar.isEmpty()) {
			_sql.batch(con, SQL_UPDATE_LIBERAR_OPERACIONES, operacionesALiberar, OperacionBloqueada.class);
		}

		logger.info("LiberaBloqueoEdicionPactos > logica > liberadas " + operacionesALiberar.size()
				+ " de " + operacionesBloqueadas.size() + " operaciones bloqueadas (IdEmpresa=" + idEmpresa
				+ ", limite=" + tiempoMaximoMinutos + " min)");

		logger.info("FIN::LiberaBloqueoEdicionPactos");
		return true;
	}

	@Override
	public void onError(Throwable t) {}

	@Override
	public void pause() {}

	public static void main(String[] args) throws InterruptedException, Throwable {
		Connection c = PlataformaUtils.getPropertiesConnection("qa02","connections.properties");
		LiberaBloqueoEdicionPactos job = new LiberaBloqueoEdicionPactos();
		job.logica(c, true);
		c.commit();
		//c.rollback();
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
