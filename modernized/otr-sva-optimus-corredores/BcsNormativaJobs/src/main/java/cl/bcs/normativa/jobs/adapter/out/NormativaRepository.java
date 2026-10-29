package cl.bcs.normativa.jobs.adapter.out;

import cl.bcs.normativa.jobs.domain.ParametrosJob;
import cl.bcs.normativa.jobs.domain.ResultadoJob;
import cl.bcs.normativa.jobs.port.out.ConfiguracionPort;
import cl.bcs.normativa.jobs.port.out.NormativaHanaPort;
import cl.bcs.normativa.jobs.port.out.ProgramaLogPort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Objects;

/**
 * HANA-backed implementation of all Normativa output ports.
 *
 * The stored-procedure calls (NormativaHanaPort) require a live SAP HANA
 * connection and are therefore NOT runnable in the H2 test profile.
 * Integration tests for those methods must use @Disabled("requires HANA").
 *
 * ConfiguracionPort and ProgramaLogPort are table-based and CAN run
 * against H2 when the schema is seeded via schema-test.sql.
 */
@Repository
public class NormativaRepository implements NormativaHanaPort, ConfiguracionPort, ProgramaLogPort {

    private static final String CONFIG_TABLE = "\"BSCL.Plataforma.BD::PLA.Configuracion\"";

    private final NamedParameterJdbcTemplate jdbc;

    NormativaRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ─── ConfiguracionPort ────────────────────────────────────────────────────

    @Override
    public String getFechaSistema(Long idEmpresa) {
        Objects.requireNonNull(idEmpresa, "idEmpresa must not be null");
        return getParametro(idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
    }

    @Override
    public String getRutaBaseArchivos(Long idEmpresa) {
        Objects.requireNonNull(idEmpresa, "idEmpresa must not be null");
        return getParametro(idEmpresa, "PLATAFORMA", "PLA.RutaBaseArchivos");
    }

    @Override
    public String getParametro(Long idEmpresa, String modulo, String codigoParametro) {
        Objects.requireNonNull(idEmpresa, "idEmpresa must not be null");
        Objects.requireNonNull(modulo, "modulo must not be null");
        Objects.requireNonNull(codigoParametro, "codigoParametro must not be null");
        var sql = """
                SELECT "Valor"
                FROM "BSCL.Plataforma.BD::PLA.Configuracion"
                WHERE "IdEmpresa" = :idEmpresa
                  AND "CodigoParametro" = :codigoParametro
                """;
        return jdbc.queryForObject(sql,
                new MapSqlParameterSource("idEmpresa", idEmpresa)
                        .addValue("codigoParametro", codigoParametro),
                String.class);
    }

    // ─── ProgramaLogPort ──────────────────────────────────────────────────────

    @Override
    public void actualizarRutaSalida(Long idPrograma, String rutaSalida) {
        Objects.requireNonNull(idPrograma, "idPrograma must not be null");
        Objects.requireNonNull(rutaSalida, "rutaSalida must not be null");
        var sql = """
                UPDATE "BSCL.Plataforma.BD::BAT.Programa"
                SET "RutaSalida" = :RutaSalida
                WHERE "IdPrograma" = :IdPrograma
                """;
        jdbc.update(sql, new MapSqlParameterSource("RutaSalida", rutaSalida)
                .addValue("IdPrograma", idPrograma));
    }

    @Override
    public void actualizarPrograma(Long idPrograma, String rutaArchivo, String rutaLog, String comentario) {
        Objects.requireNonNull(idPrograma, "idPrograma must not be null");
        var sql = """
                UPDATE "BSCL.Plataforma.BD::BAT.Programa"
                SET "RutaSalida"    = :RutaArchivo,
                    "RutaLog"       = :RutaLog,
                    "Comentario"    = :Comentario
                WHERE "IdPrograma" = :IdPrograma
                """;
        jdbc.update(sql, new MapSqlParameterSource("RutaArchivo", rutaArchivo)
                .addValue("RutaLog", rutaLog)
                .addValue("Comentario", comentario)
                .addValue("IdPrograma", idPrograma));
    }

    // ─── NormativaHanaPort — HANA SP delegates ────────────────────────────────

    @Override
    public ResultadoJob procesarTarea(Long idEmpresa, String idTipoTarea,
                                      Long idSolicitudCircular, Long jobId) {
        throw new UnsupportedOperationException(
                "requires HANA: BSCL.Normativa.BD.Servicios::NOR.ProcesarTarea");
    }

    @Override
    public ResultadoJob ejecutarGeneracion(ParametrosJob parametros) {
        throw new UnsupportedOperationException(
                "requires HANA: ProcesoGeneracion (complex HANA table scan + file generation)");
    }

    @Override
    public ResultadoJob ejecutarValidacion(ParametrosJob parametros) {
        throw new UnsupportedOperationException(
                "requires HANA: ProcesoValidacion (HANA SP delegation)");
    }

    @Override
    public ResultadoJob ejecutarCarga(ParametrosJob parametros) {
        throw new UnsupportedOperationException(
                "requires HANA: ProcesoCarga (HANA table-based carga process)");
    }

    @Override
    public ResultadoJob ejecutarCuadratura(ParametrosJob parametros) {
        throw new UnsupportedOperationException(
                "requires HANA: ProcesoCuadratura (HANA SP delegation)");
    }

    @Override
    public ResultadoJob ejecutarExportacion(ParametrosJob parametros) {
        throw new UnsupportedOperationException(
                "requires HANA: ProcesoExportacion (HANA table scan + file export)");
    }

    @Override
    public ResultadoJob ejecutarExportacionServ(ParametrosJob parametros) {
        throw new UnsupportedOperationException(
                "requires HANA: ProcesoExportacionServ (HANA NCG380 SP delegation)");
    }
}
