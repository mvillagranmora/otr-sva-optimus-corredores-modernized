package cl.bcs.cuentainversion.jobs.job;

import cl.bcs.cuentainversion.jobs.job.port.ConfiguracionJobs;
import cl.bcs.cuentainversion.reportes.ReporteXlsxCuentas;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Types;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

/**
 * What the legacy jobs did with {@code new Servicio().logica(con, entrada)}: here the service is called in process through its JSON entry
 * {@code execute(JsonObject)} (decision 2026-10-15), with the typed {@code Entrada} of the Servicios class serialised by the platform
 * Gson ({@link Utils#getGsonBuilder()}: nulls kept, dates {@code yyyy-MM-dd HH:mm:ss.SSS}) and the answer decoded into its {@code Salida}.
 */
public final class Llamada {

    /** A service of BcsCuentaInversionServicios: {@code execute(JsonObject)}. */
    @FunctionalInterface
    public interface Servicio {
        JsonObject execute(JsonObject entrada) throws Exception;
    }

    private Llamada() {
    }

    public static <S> S de(Servicio servicio, Object entrada, Class<S> salida) throws Exception {
        Gson gson = Utils.getGsonBuilder();
        return gson.fromJson(servicio.execute(gson.toJsonTree(entrada).getAsJsonObject()), salida);
    }

    /**
     * Legacy {@code ConfiguracionSistema.getParametroConfiguracionString} (ConfiguracionSistema.java:70-71): a parameter that is not
     * configured is an {@link Exception} with the legacy message.
     */
    public static String parametro(ConfiguracionJobs configuracion, Long idEmpresa, String modulo, String nombre) throws Exception {
        String valor = configuracion.parametro(idEmpresa, modulo, nombre);
        if (valor == null) {
            throw new Exception("Parametro " + nombre + " en modulo " + modulo + " no encontrado");
        }
        return valor;
    }

    /** The legacy SQL resource {@code cl/bcs/cuentaInversion/.../<nombre>.sql}, copied to {@code sql/jobs/} (the named parameters are kept). */
    public static String sql(String nombre) {
        try (var in = new ClassPathResource("sql/jobs/" + nombre + ".sql").getInputStream()) {
            return StreamUtils.copyToString(in, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("SQL resource not found: " + nombre, e);
        }
    }

    /**
     * Legacy {@code SQL.batch(con, sql, List.of(one row), Row.class)}: one UPDATE with named parameters on the transaction of the run.
     * {@code pares} is {@code name, value, name, value...}; a {@code null} value is bound as a VARCHAR NULL (every nullable column the
     * jobs write is text: file, log, comment).
     */
    public static void actualizar(JdbcTemplate jdbc, String sql, Object... pares) {
        MapSqlParameterSource parametros = new MapSqlParameterSource();
        for (int i = 0; i < pares.length; i += 2) {
            Object valor = pares[i + 1];
            parametros.addValue((String) pares[i], valor == null ? new SqlParameterValue(Types.VARCHAR, null) : valor);
        }
        new NamedParameterJdbcTemplate(jdbc).update(sql, parametros);
    }

    /**
     * The update the two patrimony report jobs share (legacy {@code InterfacesJob_update.sql} of BcsNormativaJobs, batch of one
     * {@code ActualizarPrograma}): output file, log file and comment of the program.
     */
    public static void actualizarPrograma(JdbcTemplate jdbc, String rutaArchivo, String rutaLog, Long idPrograma, String comentario) {
        actualizar(jdbc, sql("InterfacesJob_update"), "RutaArchivo", rutaArchivo, "RutaLog", rutaLog,
                "IdPrograma", idPrograma, "Comentario", comentario);
    }

    /** {@code UPDATE BAT.Programa SET RutaSalida = :RutaArchivo WHERE IdPrograma = :IdPrograma} (inline SQL of several legacy jobs). */
    public static final String SQL_RUTA_SALIDA =
            "UPDATE \"BSCL.Plataforma.BD::BAT.Programa\" SET \"RutaSalida\" = :RutaArchivo  WHERE \"IdPrograma\" = :IdPrograma ";

    /**
     * Tail shared by the three aging / creditors report jobs (GenerarReporteAcreedoresJob.java:78-92, ...BolsaCTAJob, ...CTAJob): a clean
     * answer stores the file and the broker code; anything else stores the first message as the comment (CIJ-DEF-03 and CIJ-DEF-06:
     * the counter is not a failure, an empty answer without messages is an IndexOutOfBoundsException).
     */
    public static void guardarRutaReporte(JdbcTemplate jdbc, String sql, ReporteXlsxCuentas.Salida salida, Long idPrograma) {
        if (salida.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0 && salida.P_OUT_RUTAARCHIVO.trim().length() != 0
                && salida.P_OUT_CODIGOCORREDOR.trim().length() != 0) {
            actualizar(jdbc, sql, "IdPrograma", idPrograma, "RutaSalida", salida.P_OUT_RUTAARCHIVO, "Comentario", salida.P_OUT_CODIGOCORREDOR);
        } else {
            actualizar(jdbc, sql, "IdPrograma", idPrograma, "RutaSalida", "", "Comentario", salida.P_OUT_MENSAJES.get(0).descripcion());
        }
    }

    /** {@code UPDATE BAT.Programa SET RutaSalida, Comentario} of the aging reports (inline SQL of GenerarReporteAntiguedadDeuda*CTAJob). */
    public static final String SQL_RUTA_SALIDA_COMENTARIO = "UPDATE \"BSCL.Plataforma.BD::BAT.Programa\" SET \"RutaSalida\" = :RutaSalida , "
            + "\"Comentario\" = :Comentario WHERE \"IdPrograma\" = :IdPrograma ";
}
