package cl.bcs.fondomutuo.jobs.adapter.out;

import cl.bcs.fondomutuo.jobs.domain.*;
import cl.bcs.fondomutuo.jobs.port.out.HanaFondoMutuoPort;
import cl.bcs.fondomutuo.jobs.port.out.ParametrosProgramaPort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * HANA-backed adapter implementing ParametrosProgramaPort and HanaFondoMutuoPort.
 *
 * All methods use NamedParameterJdbcTemplate + MapSqlParameterSource.
 * Methods that require HANA SP or #temp_table syntax throw UnsupportedOperationException
 * in Phase 2 and are annotated with the HANA-requires note.
 *
 * SQL mirrors the legacy .sql resource files found in the legacy source tree.
 */
@Repository
public class FondoMutuoJobsRepository implements ParametrosProgramaPort, HanaFondoMutuoPort {

    private final NamedParameterJdbcTemplate jdbc;

    public FondoMutuoJobsRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ── ParametrosProgramaPort ───────────────────────────────────────────────

    @Override
    public List<ParametrosPrograma> consultarParametros(Long jobId) {
        // IngresarOrdenFMBonificacionFiscal_SelectParametros.sql
        var sql = """
                SELECT "IdSecuencia", "NombreParametro", "Valor"
                FROM "BSCL.Plataforma.BD::BAT.ParametrosPrograma"
                WHERE "IdPrograma" = :IdPrograma
                ORDER BY "IdSecuencia" ASC
                """;
        return jdbc.query(sql,
                new MapSqlParameterSource("IdPrograma", jobId),
                (rs, n) -> new ParametrosPrograma(
                        rs.getLong("IdSecuencia"),
                        rs.getString("NombreParametro"),
                        rs.getString("Valor")));
    }

    // ── HanaFondoMutuoPort ───────────────────────────────────────────────────

    @Override
    public List<RegistroTraspaso> consultarTraspasosPendientes(Long idEmpresa, Long diasTranscurridos) {
        // ConfirmarTraspaso_SelectTraspaso.sql (adapted)
        var sql = """
                SELECT TRASPASO."IdTraspaso", TRASPASO."IdEmpresa", TRASPASO."IdEstado",
                       TO_NVARCHAR(TRASPASO."FechaIngreso") AS "FechaIngreso",
                       DAYS_BETWEEN(TO_DATE(TRASPASO."FechaIngreso", 'YYYY-MM-DD'),
                                    TO_DATE(CONFIG."FechaSistema", 'YYYY-MM-DD')) AS "DiasTranscurridos"
                FROM "BSCL.FondoMutuo.BD::TRA.Traspaso" TRASPASO
                INNER JOIN (
                    SELECT "Valor" AS "FechaSistema"
                    FROM "BSCL.Plataforma.BD::PLA.Configuracion"
                    WHERE "IdEmpresa" = :IdEmpresa AND "CodigoParametro" = 'PLA.FechaSistema'
                ) CONFIG ON (1=1)
                WHERE TRASPASO."IdEmpresa" = :IdEmpresa AND TRASPASO."IdEstado" = 'ING'
                  AND DAYS_BETWEEN(TO_DATE(TRASPASO."FechaIngreso", 'YYYY-MM-DD'),
                                   TO_DATE(CONFIG."FechaSistema", 'YYYY-MM-DD')) = :DiasTranscurridos
                """;
        return jdbc.query(sql,
                new MapSqlParameterSource("IdEmpresa", idEmpresa)
                        .addValue("DiasTranscurridos", diasTranscurridos),
                (rs, n) -> new RegistroTraspaso(
                        rs.getLong("IdTraspaso"),
                        rs.getLong("IdEmpresa"),
                        rs.getString("IdEstado"),
                        rs.getString("FechaIngreso"),
                        rs.getLong("DiasTranscurridos")));
    }

    @Override
    public List<ApoResFFMM> consultarAportesRescateBF(List<Long> idsAporteRescate) {
        // IngresarOrdenFMBonificacionFiscal_SelectFFMMBF.sql
        var sql = """
                SELECT ApoRes."IdSolicitudAporteRescate", ApoRes."IdCliente",
                       PortaBonif."IdPortafolio",
                       ApoRes."IdPortafolio" AS "IdPortafolioOrigen",
                       ApoRes."TipoSolicitud", ApoRes."IdSerieFondo",
                       ApoRes."CalculoPorMontoCuota", ApoRes."Monto", ApoRes."NumeroCuotas",
                       ApoRes."IdMoneda", ApoRes."IdMedioDePago",
                       ApoRes."Visado", ApoRes."SolicitudBatch", ApoRes."OrigenOperacion",
                       ApoRes."IdRegimenTributario", ApoRes."IndicadorMLT",
                       ApoRes."IdSerieFondoDestino", ApoRes."IdEmisorDestino",
                       ApoRes."IdAutorizadoOrigen", ApoRes."Porcentaje",
                       COALESCE(ApoRes."FolioContrato", 0) AS "FolioContrato",
                       TO_DECIMAL(0, 26, 8) AS "PorcentajeCalculado"
                FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" ApoRes
                INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" PortaBonif
                    ON ApoRes."IdCliente" = PortaBonif."IdCliente"
                   AND PortaBonif."TienePropositoAPV" = 'S'
                   AND PortaBonif."IdEstado" = 'ACT'
                INNER JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" MatrizBonif
                    ON PortaBonif."IdMatrizAgrupadorAPV" = MatrizBonif."IdMatrizAgrupadorAPV"
                INNER JOIN "BSCL.APV.BD::REC.RegimenTributario" RegimenBonif
                    ON RegimenBonif."IdRegimenTributario" = MatrizBonif."IdRegimenTributario"
                   AND RegimenBonif."Codigo" = 'BF'
                WHERE ApoRes."IdSolicitudAporteRescate" IN (:IdsAporteRescate)
                """;
        return jdbc.query(sql,
                new MapSqlParameterSource("IdsAporteRescate", idsAporteRescate),
                (rs, n) -> new ApoResFFMM(
                        rs.getLong("IdSolicitudAporteRescate"),
                        rs.getLong("IdCliente"),
                        rs.getLong("IdPortafolio"),
                        rs.getLong("IdPortafolioOrigen"),
                        rs.getString("TipoSolicitud"),
                        rs.getString("IdSerieFondo"),
                        rs.getString("CalculoPorMontoCuota"),
                        rs.getBigDecimal("Monto"),
                        rs.getBigDecimal("NumeroCuotas"),
                        rs.getString("IdMoneda"),
                        rs.getString("IdMedioDePago"),
                        rs.getString("Visado"),
                        rs.getString("SolicitudBatch"),
                        rs.getString("OrigenOperacion"),
                        rs.getString("IdRegimenTributario"),
                        rs.getString("IndicadorMLT"),
                        rs.getString("IdSerieFondoDestino"),
                        rs.getString("IdEmisorDestino"),
                        rs.getObject("IdAutorizadoOrigen", Long.class),
                        rs.getBigDecimal("Porcentaje"),
                        rs.getObject("FolioContrato", Long.class),
                        rs.getBigDecimal("PorcentajeCalculado")));
    }

    @Override
    public List<ParametrosPrograma> consultarParametrosPrograma(Long idPrograma) {
        return consultarParametros(idPrograma);
    }

    @Override
    public List<SerieFondoSolicitudes> consultarSeriesFondo(Long idEmpresa, List<String> idsSerieFondo) {
        // IngresarOrdenFMBonificacionFiscal_SelectSerieFondo.sql
        var sql = """
                SELECT "IdSerieFondo", "DecimalesCuota"
                FROM "BSCL.FondoMutuo.BD::FMM.SerieDeFondo"
                WHERE "IdEmpresa" = :IdEmpresa AND "IdSerieFondo" IN (:IdSerieFondo)
                """;
        return jdbc.query(sql,
                new MapSqlParameterSource("IdEmpresa", idEmpresa)
                        .addValue("IdSerieFondo", idsSerieFondo),
                (rs, n) -> new SerieFondoSolicitudes(
                        rs.getString("IdSerieFondo"),
                        rs.getBigDecimal("DecimalesCuota")));
    }

    @Override
    public List<ReinversionSerieFondoDestino> consultarReinversionSerieFondoDestino(List<Long> idsAporteRescate) {
        // ComunesSolicitudes_SelectReinversionSerieFondoDestino.sql (legacy reference)
        var sql = """
                SELECT "IdSolicitudAporteRescate", "IdSerieFondoDestino", "Porcentaje"
                FROM "BSCL.FondoMutuo.BD::FMM.ReinversionSerieFondoDestino"
                WHERE "IdSolicitudAporteRescate" IN (:IdSolicitudAporteRescate)
                """;
        return jdbc.query(sql,
                new MapSqlParameterSource("IdSolicitudAporteRescate", idsAporteRescate),
                (rs, n) -> new ReinversionSerieFondoDestino(
                        rs.getLong("IdSolicitudAporteRescate"),
                        rs.getString("IdSerieFondoDestino"),
                        rs.getBigDecimal("Porcentaje")));
    }

    @Override
    public String consultarParametroConfiguracion(Long idEmpresa, String codigoParametro) {
        var sql = """
                SELECT "Valor"
                FROM "BSCL.Plataforma.BD::PLA.Configuracion"
                WHERE "IdEmpresa" = :IdEmpresa AND "CodigoParametro" = :CodigoParametro
                """;
        return jdbc.queryForObject(sql,
                new MapSqlParameterSource("IdEmpresa", idEmpresa)
                        .addValue("CodigoParametro", codigoParametro),
                String.class);
    }

    @Override
    public void actualizarRutaSalidaPrograma(Long idPrograma, String rutaSalida) {
        var sql = """
                UPDATE "BSCL.Plataforma.BD::BAT.Programa"
                SET "RutaSalida" = :RutaSalida
                WHERE "IdPrograma" = :IdPrograma
                """;
        jdbc.update(sql, new MapSqlParameterSource("RutaSalida", rutaSalida)
                .addValue("IdPrograma", idPrograma));
    }

    @Override
    public void agregarLogPrograma(Long idPrograma, String idTipoPrograma, String mensaje) {
        var sql = """
                INSERT INTO "BSCL.Plataforma.BD::BAT.LogPrograma"
                    ("IdPrograma", "IdTipoPrograma", "Descripcion", "FechaHora")
                VALUES (:IdPrograma, :IdTipoPrograma, :Descripcion, CURRENT_TIMESTAMP)
                """;
        jdbc.update(sql, new MapSqlParameterSource("IdPrograma", idPrograma)
                .addValue("IdTipoPrograma", idTipoPrograma)
                .addValue("Descripcion", mensaje));
    }
}
