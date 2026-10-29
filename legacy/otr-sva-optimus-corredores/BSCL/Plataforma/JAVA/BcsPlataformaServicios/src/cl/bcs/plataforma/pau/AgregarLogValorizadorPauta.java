package cl.bcs.plataforma.pau;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Timestamp;
import cl.bcs.utils.Fecha;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AgregarLogValorizadorPauta implements JavaCall {

	private final Log log = new Log("AgregarLogValorizadorPauta");
	private static final String CARPETA_LOG_PAUTA = "PAU";
	private final String SQL_SelectLogValorizadorPauta = loadResourceToString("cl/bcs/plataforma/pau/sql/AgregarLogValorizadorPauta_SEL_LogValorizadorPauta.sql");
	private final String SQL_SelectDetalleLogValorizadorPauta = loadResourceToString("cl/bcs/plataforma/pau/sql/AgregarLogValorizadorPauta_SEL_DetalleLogValorizadorPauta.sql");
	private final String SQL_UpdateRutaLog = loadResourceToString("cl/bcs/plataforma/pau/sql/AgregarLogValorizadorPauta_UPD_RutaLog.sql");

	public static class AgregarLogValorizadorPautaEntrada {
		public Long p_IdLogValorizadorPauta;
		public String p_RutaSalidaArchivo;
	}

	public static class AgregarLogValorizadorPautaSalida {
		public Long P_OUT_ERRORESCONTADOR;
		public String P_OUT_MENSAJE;
		public String P_OUT_RUTA_ARCHIVO;

		public AgregarLogValorizadorPautaSalida() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJE = "";
			this.P_OUT_RUTA_ARCHIVO = "";
		}
	}

	public static class ObjLogValorizadorPauta {
		public Long IdLogValorizadorPauta;
		public Timestamp FechaInicio;
		public Timestamp FechaTermino;
		public String Proceso;
		public String Mercado;
	}

	public static class ObjDetalleLogValorizadorPauta {
		public Long IdDetalleLogValorizadorPauta;
		public Long IdLogValorizadorPauta;
		public Long FolioCartera;
		public String Nemotecnico;
		public String Estado;
		public String Observacion;
		public String Origen;
	}
	
	public static class ObjUpdRutaLog {
		public Long IdLogValorizadorPauta;
		public String RutaLog;
	}

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();

		AgregarLogValorizadorPautaEntrada in = gson.fromJson(jin.toString(), AgregarLogValorizadorPautaEntrada.class);
		AgregarLogValorizadorPautaSalida out = logica(con, in);
		JsonParser parser = new JsonParser();
		
		return parser.parse(gson.toJson(out)).getAsJsonObject();
	}

	public AgregarLogValorizadorPautaSalida logica(Connection con, AgregarLogValorizadorPautaEntrada in) throws Exception {

		log.write("Inicio AgregarLogValorizadorPauta");

		AgregarLogValorizadorPautaSalida out = new AgregarLogValorizadorPautaSalida();
		SQL sql = new SQL();

		if (in == null || in.p_IdLogValorizadorPauta == null || in.p_IdLogValorizadorPauta.equals(0L)) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJE = "IdLogValorizadorPauta no informado.";
			return out;
		}

		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("IdLogValorizadorPauta", in.p_IdLogValorizadorPauta);

		List<ObjLogValorizadorPauta> cabeceras = sql.query(con, SQL_SelectLogValorizadorPauta, params, ObjLogValorizadorPauta.class);

		if (cabeceras == null || cabeceras.isEmpty()) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJE = "No se encontro cabecera LogValorizadorPauta para Id: " + in.p_IdLogValorizadorPauta;
			return out;
		}

		ObjLogValorizadorPauta cabecera = cabeceras.get(0);

		List<ObjDetalleLogValorizadorPauta> detalles = sql.query(con, SQL_SelectDetalleLogValorizadorPauta,params,ObjDetalleLogValorizadorPauta.class);

		String contenidoArchivo = construirContenidoArchivo(cabecera, detalles);
		String nombreArchivo = construirNombreArchivo(cabecera);
		Path rutaFisicaArchivo = guardarArchivo(in.p_RutaSalidaArchivo,	nombreArchivo, contenidoArchivo);
		String rutaRelativaLog = CARPETA_LOG_PAUTA + "/" + nombreArchivo;
		actualizarRutaLog(con, sql,	in.p_IdLogValorizadorPauta,	rutaRelativaLog);

		out.P_OUT_RUTA_ARCHIVO = rutaRelativaLog;
		out.P_OUT_ERRORESCONTADOR = 0L;
		out.P_OUT_MENSAJE = "Archivo LogValorizadorPauta generado correctamente.";
		out.P_OUT_RUTA_ARCHIVO = rutaRelativaLog;

		log.write("Archivo generado: " + out.P_OUT_RUTA_ARCHIVO);
		log.write("Fin AgregarLogValorizadorPauta");

		return out;
	}

	private String construirContenidoArchivo(ObjLogValorizadorPauta cabecera, List<ObjDetalleLogValorizadorPauta> detalles) {

		StringBuilder sb = new StringBuilder();

		sb.append("F. Inicio\t: ").append(cabecera.FechaInicio != null ? Fecha.toStringDatetime(cabecera.FechaInicio) : "").append(System.lineSeparator());
		sb.append("> Proceso\t: ").append(cabecera.Proceso != null ? cabecera.Proceso : "").append(System.lineSeparator());
		sb.append("> F. Proceso\t: ").append(cabecera.FechaInicio != null ? Fecha.toStringDate(cabecera.FechaInicio) : "").append(System.lineSeparator());
		sb.append("> Mercado\t: ").append(cabecera.Mercado != null ? cabecera.Mercado : "").append(System.lineSeparator());
		sb.append("F. Termino\t: ").append(cabecera.FechaTermino != null ? Fecha.toStringDatetime(cabecera.FechaTermino) : "").append(System.lineSeparator());
		sb.append("--------------------------------------------------------------------------").append(System.lineSeparator());

		sb.append(String.format("%-7s %-10s %-10s %-16s %s%n", "Estado", "Origen", "Folio", "Nemo", "Observacion"));

		sb.append("--------------------------------------------------------------------------").append(System.lineSeparator());

		if (detalles != null) {
			for (ObjDetalleLogValorizadorPauta detalle : detalles) {
				sb.append(String.format(
						"%-7s %-10s %-10s %-16s %s%n",
						detalle.Estado != null ? detalle.Estado : "",
						detalle.Origen != null ? detalle.Origen : "",
						detalle.FolioCartera != null ? detalle.FolioCartera.toString() : "",
						detalle.Nemotecnico != null ? detalle.Nemotecnico : "",
						detalle.Observacion != null ? detalle.Observacion : ""
					));
			}
		}

		return sb.toString();
	}

	private String construirNombreArchivo(ObjLogValorizadorPauta cabecera) {
		
		String proceso = cabecera.Proceso != null && !cabecera.Proceso.trim().isEmpty() ? cabecera.Proceso.trim() : "LOG_VALORIZADOR_PAUTA";
		proceso = proceso.replaceAll("[^A-Za-z0-9_\\-]", "_");
		Timestamp fechaBase = cabecera.FechaInicio != null ? cabecera.FechaInicio : new Timestamp(System.currentTimeMillis());
		String fecha = Fecha.toFormat(fechaBase, "yyyyMMddHHmmss");

		return proceso + "_" + fecha + ".txt";
	}

	private Path guardarArchivo(String rutaBase, String nombreArchivo,String contenido) throws Exception {

		String rutaBaseArchivo = rutaBase != null && !rutaBase.trim().isEmpty()	? rutaBase.trim() : System.getProperty("java.io.tmpdir");
		Path directorio = Paths.get(rutaBaseArchivo, CARPETA_LOG_PAUTA);

		if (!Files.exists(directorio)) {
			Files.createDirectories(directorio);
		}

		Path archivo = directorio.resolve(nombreArchivo);

		try (BufferedWriter writer = Files.newBufferedWriter(archivo, StandardCharsets.UTF_8)) {
			writer.write(contenido);
		}

		return archivo;
	}
	
	private void actualizarRutaLog(Connection con, SQL sql, Long idLogValorizadorPauta, String rutaLog) throws Exception {

		if (idLogValorizadorPauta == null || idLogValorizadorPauta.equals(0L)) {
			return;
		}

		ObjUpdRutaLog upd = new ObjUpdRutaLog();
		upd.IdLogValorizadorPauta = idLogValorizadorPauta;
		upd.RutaLog = rutaLog;

		List<ObjUpdRutaLog> params = new ArrayList<ObjUpdRutaLog>();
		params.add(upd);

		sql.batch(con, SQL_UpdateRutaLog, params, ObjUpdRutaLog.class);
	}
}