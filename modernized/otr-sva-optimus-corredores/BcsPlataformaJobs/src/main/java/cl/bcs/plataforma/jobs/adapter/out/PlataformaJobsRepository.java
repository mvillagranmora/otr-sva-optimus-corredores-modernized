package cl.bcs.plataforma.jobs.adapter.out;

import cl.bcs.plataforma.jobs.domain.DetalleEjecucion;
import cl.bcs.plataforma.jobs.domain.Ejecucion;
import cl.bcs.plataforma.jobs.domain.EstadoModulo;
import cl.bcs.plataforma.jobs.domain.ProcesoPauta;
import cl.bcs.plataforma.jobs.port.out.PautaRepositoryPort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * HANA-backed adapter implementing PautaRepositoryPort.
 *
 * All methods use NamedParameterJdbcTemplate + MapSqlParameterSource
 * per the mandatory pattern.
 *
 * NOTE: These methods throw UnsupportedOperationException until wired
 * to a real HANA datasource.  Integration tests must be
 * {@code @Disabled("requires HANA")}.
 */
@Repository
public class PlataformaJobsRepository implements PautaRepositoryPort {

    private final NamedParameterJdbcTemplate jdbc;

    public PlataformaJobsRepository(NamedParameterJdbcTemplate jdbc) {
        Objects.requireNonNull(jdbc, "jdbc must not be null");
        this.jdbc = jdbc;
    }

    @Override
    public List<EstadoModulo> findEstadoModulos() {
        // Requires live HANA — LaPauta_SelectEstadoModulos.sql
        throw new UnsupportedOperationException(
                "findEstadoModulos requires a live HANA connection");
    }

    @Override
    public List<ProcesoPauta> findProcesosPauta(Long idEmpresa, List<String> codigosProceso,
                                                  int filtroCodigoProceso, String ejecutarDiaInhabil) {
        Objects.requireNonNull(idEmpresa, "idEmpresa must not be null");
        Objects.requireNonNull(codigosProceso, "codigosProceso must not be null");
        throw new UnsupportedOperationException(
                "findProcesosPauta requires a live HANA connection");
    }

    @Override
    public List<Ejecucion> findEjecucion(Long idEmpresa, String fechaSistema) {
        Objects.requireNonNull(idEmpresa, "idEmpresa must not be null");
        Objects.requireNonNull(fechaSistema, "fechaSistema must not be null");
        throw new UnsupportedOperationException(
                "findEjecucion requires a live HANA connection");
    }

    @Override
    public void insertEjecucion(Ejecucion ejecucion) {
        Objects.requireNonNull(ejecucion, "ejecucion must not be null");
        throw new UnsupportedOperationException(
                "insertEjecucion requires a live HANA connection");
    }

    @Override
    public void updateEjecucion(Ejecucion ejecucion) {
        Objects.requireNonNull(ejecucion, "ejecucion must not be null");
        throw new UnsupportedOperationException(
                "updateEjecucion requires a live HANA connection");
    }

    @Override
    public void insertDetalleEjecucion(DetalleEjecucion detalle) {
        Objects.requireNonNull(detalle, "detalle must not be null");
        throw new UnsupportedOperationException(
                "insertDetalleEjecucion requires a live HANA connection");
    }

    @Override
    public void updateDetalleEjecucion(DetalleEjecucion detalle) {
        Objects.requireNonNull(detalle, "detalle must not be null");
        throw new UnsupportedOperationException(
                "updateDetalleEjecucion requires a live HANA connection");
    }

    @Override
    public List<DetalleEjecucion> findDetalleEjecucion(Long idEmpresa, String fechaSistema) {
        Objects.requireNonNull(idEmpresa, "idEmpresa must not be null");
        Objects.requireNonNull(fechaSistema, "fechaSistema must not be null");
        throw new UnsupportedOperationException(
                "findDetalleEjecucion requires a live HANA connection");
    }
}
