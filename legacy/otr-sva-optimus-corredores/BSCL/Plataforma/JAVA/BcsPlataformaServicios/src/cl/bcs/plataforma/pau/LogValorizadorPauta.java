package cl.bcs.plataforma.pau;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class LogValorizadorPauta implements JavaCall {

	private final Log log = new Log("LogValorizadorPauta");

	private final String SQL_InsertLogValorizadorPauta = loadResourceToString("cl/bcs/plataforma/pau/sql/LogValorizadorPauta_INS_LogValorizadorPauta.sql");
	private final String SQL_UpdateTerminoLogValorizadorPauta =	loadResourceToString("cl/bcs/plataforma/pau/sql/LogValorizadorPauta_UPD_LogValorizadorPauta_Termino.sql");
	private final String SQL_SelectIdEjecucion = loadResourceToString("cl/bcs/plataforma/pau/sql/LogValorizadorPauta_SEL_IdEjecucion.sql");
	private final String SQL_DeleteDetalleLogValorizadorPautaAntiguo = loadResourceToString("cl/bcs/plataforma/pau/sql/LogValorizadorPauta_DEL_DetalleLogValorizadorPauta_Antiguo.sql");

	public static class ObjLogValorizadorPauta {
		public Long IdLogValorizadorPauta;
		public Long IdEjecucion;
		public String Proceso;
		public String Mercado;
	}

	public static class ObjUpdLogValorizadorPauta {
		public Long IdLogValorizadorPauta;
	}

	public static class ObjIdEjecucion {
		public Long IdEjecucion;
	}
	
	public static class ObjDelDetalleLogValorizadorPauta {
		public String FechaSistema;
		public String Proceso;
	}

	@Override
	public JsonObject call(Connection con, JsonObject in) throws Exception {
		throw new UnsupportedOperationException("LogValorizadorPauta es una clase de apoyo interno, no debe ejecutarse mediante call().");
	}

	public Long obtenerIdEjecucionPauta(Connection con, Long idEmpresa,	String fechaSistema, String codigoProceso) throws Exception {
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("IdEmpresa", idEmpresa);
		params.put("FechaSistema", fechaSistema);
		params.put("CodigoProceso", codigoProceso);

		List<ObjIdEjecucion> ids = sql.query( con, SQL_SelectIdEjecucion, params, ObjIdEjecucion.class);

		if (ids == null || ids.isEmpty()) {
			return null;
		}

		return ids.get(0).IdEjecucion;
	}

	public Long crearCabeceraLogPauta(Connection con, Long idEjecucion, String proceso, String mercado) throws Exception {
		SQL sql = new SQL();
		Long idLogValorizadorPauta = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::IdLogValorizadorPauta", 1L).get(0);
		ObjLogValorizadorPauta logPauta = new ObjLogValorizadorPauta();
		logPauta.IdLogValorizadorPauta = idLogValorizadorPauta;
		logPauta.IdEjecucion = idEjecucion;
		logPauta.Proceso = proceso;
		logPauta.Mercado = mercado;
		List<ObjLogValorizadorPauta> params = new ArrayList<ObjLogValorizadorPauta>();
		params.add(logPauta);
		sql.batch(con, SQL_InsertLogValorizadorPauta, params, ObjLogValorizadorPauta.class);

		return idLogValorizadorPauta;
	}

	public void cerrarCabeceraLogPauta(Connection con, Long idLogValorizadorPauta) throws Exception {
		if (idLogValorizadorPauta == null || idLogValorizadorPauta.equals(0L)) {
			return;
		}

		SQL sql = new SQL();
		ObjUpdLogValorizadorPauta upd = new ObjUpdLogValorizadorPauta();
		upd.IdLogValorizadorPauta = idLogValorizadorPauta;
		List<ObjUpdLogValorizadorPauta> params = new ArrayList<ObjUpdLogValorizadorPauta>();
		params.add(upd);
		sql.batch(con, SQL_UpdateTerminoLogValorizadorPauta, params, ObjUpdLogValorizadorPauta.class);
	}
	
	public void generarArchivoLogValorizadorPauta(Connection con, Long idEmpresa, Long idLogValorizadorPauta) throws Exception {
		generarArchivoLogValorizadorPauta(con, idEmpresa, idLogValorizadorPauta, null);
	}

	public void generarArchivoLogValorizadorPauta(Connection con, Long idEmpresa, Long idLogValorizadorPauta, String rutaSalidaArchivo) throws Exception {
		if (idLogValorizadorPauta == null || idLogValorizadorPauta.equals(0L)) {
			return;
		}

		AgregarLogValorizadorPauta.AgregarLogValorizadorPautaEntrada entradaLog = new AgregarLogValorizadorPauta.AgregarLogValorizadorPautaEntrada();
		entradaLog.p_IdLogValorizadorPauta = idLogValorizadorPauta;
		entradaLog.p_RutaSalidaArchivo = rutaSalidaArchivo != null && !rutaSalidaArchivo.trim().isEmpty() ? rutaSalidaArchivo : ConfiguracionSistema.getParametroConfiguracionString(con,idEmpresa,"PLATAFORMA","PLA.RutaBaseArchivos");
		AgregarLogValorizadorPauta agregarLogValorizadorPauta = new AgregarLogValorizadorPauta();
		AgregarLogValorizadorPauta.AgregarLogValorizadorPautaSalida salidaLog = agregarLogValorizadorPauta.logica(con, entradaLog);

		if (salidaLog.P_OUT_ERRORESCONTADOR != null	&& !salidaLog.P_OUT_ERRORESCONTADOR.equals(0L)) {
			log.write("Error generando archivo LogValorizadorPauta: " + salidaLog.P_OUT_MENSAJE);

		} else {
			log.write("Archivo LogValorizadorPauta generado: " + salidaLog.P_OUT_RUTA_ARCHIVO);
		}
	}
	
	public void limpiarDetalleLogPautaAntiguo(Connection con, String fechaSistema, String proceso) throws Exception {

		if (fechaSistema == null || fechaSistema.trim().isEmpty()) {
			log.write("No se ejecuta limpieza de detalle LogValorizadorPauta: FechaSistema no informada.");
			return;
		}

		String fechaSistemaNormalizada = fechaSistema.trim();

		if (fechaSistemaNormalizada.length() >= 10) {
			fechaSistemaNormalizada = fechaSistemaNormalizada.substring(0, 10);
		}

		ObjDelDetalleLogValorizadorPauta param = new ObjDelDetalleLogValorizadorPauta();
		param.FechaSistema = fechaSistemaNormalizada;
		param.Proceso = proceso != null ? proceso.trim() : "";
		List<ObjDelDetalleLogValorizadorPauta> params = new ArrayList<ObjDelDetalleLogValorizadorPauta>();
		params.add(param);

		SQL sql = new SQL();

		log.write("Inicio limpieza detalle LogValorizadorPauta. " + "FechaSistema=" + param.FechaSistema + ", Proceso=" + param.Proceso);

		sql.batch(con, SQL_DeleteDetalleLogValorizadorPautaAntiguo,	params, ObjDelDetalleLogValorizadorPauta.class);

		log.write("Fin limpieza detalle LogValorizadorPauta.");
	}
	
}