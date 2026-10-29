package cl.bcs.custodia.adapter.out;

import cl.bcs.custodia.domain.PosicionCustodia;
import cl.bcs.custodia.port.out.CustodiaServiciosDataPort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JDBC adapter implementing CustodiaServiciosDataPort.
 *
 * H2-compatible SQL (plain queries).
 * HANA stored-procedure methods throw UnsupportedOperationException with a clear message.
 */
@Repository
public class CustodiaServiciosRepositoryAdapter implements CustodiaServiciosDataPort {

    private final NamedParameterJdbcTemplate jdbc;

    public CustodiaServiciosRepositoryAdapter(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // -----------------------------------------------------------------------
    // Plain-SQL methods (H2-testable)
    // -----------------------------------------------------------------------

    /**
     * Returns the current business date configured in the system.
     *
     * Real HANA table: BSCL."BSCL.Plataforma.BD::PLA.Configuracion"
     * Column mapping: IdEmpresa, CodigoParametro='PLA.FechaSistema', Valor
     * H2 test table: CONFIGURACION_PLA (same column names, different table name)
     *
     * HANA QA equivalence confirmed 2026-10-08:
     *   GWJAVA t=2 → P_OUT_FECHAHORASISTEMA="2026-09-14 18:24:47.000"
     *   HANA direct → Valor="2026-09-14"  (same date, no time component)
     */
    @Override
    public String obtenerFechaSistema(Long idEmpresa) {
        var params = new MapSqlParameterSource()
                .addValue("idEmpresa", idEmpresa)
                .addValue("clave", "PLA.FechaSistema");
        return jdbc.queryForObject(
                "SELECT \"Valor\" FROM CONFIGURACION_PLA" +
                " WHERE \"IdEmpresa\" = :idEmpresa AND \"CodigoParametro\" = :clave",
                params,
                String.class);
        // HANA deployment: replace CONFIGURACION_PLA with "BSCL.Plataforma.BD::PLA.Configuracion"
    }

    /**
     * Returns custody positions for a given portfolio.
     *
     * Real HANA view: BSCL."BSCL.Custodia.BD::CUS.CustodiaPortafolio"
     * Note: This view uses HANA user-defined types and cannot be queried via plain JDBC.
     * Full implementation requires the HANA stored-procedure path or a flattened view.
     * H2 test table: CUSTODIA_PORTAFOLIO (simplified schema for unit testing)
     *
     * HANA QA: BSCL_VIRTUAL.CUSTODIA_OPTIMUS is queryable but keyed by RUT_CLI not IdPortafolio.
     * A portfolio-to-RUT join table is needed; this method is a stub until the SP path is available.
     */
    @Override
    public List<PosicionCustodia> consultarPosicion(Long idPortafolio) {
        var params = new MapSqlParameterSource()
                .addValue("idPortafolio", idPortafolio);
        return jdbc.query(
                "SELECT \"IdPortafolio\", \"IdInstrumento\", \"Nemotecnico\", \"CantidadCustodia\"" +
                " FROM CUSTODIA_PORTAFOLIO" +
                " WHERE \"IdPortafolio\" = :idPortafolio",
                params,
                (rs, rowNum) -> new PosicionCustodia(
                        rs.getLong("IdPortafolio"),
                        rs.getLong("IdInstrumento"),
                        rs.getString("Nemotecnico"),
                        rs.getBigDecimal("CantidadCustodia")));
        // HANA deployment: replace CUSTODIA_PORTAFOLIO with "BSCL.Custodia.BD::CUS.CustodiaPortafolio"
        // (requires HANA SP or flattened view — user-defined types block plain JDBC)
    }

    // -----------------------------------------------------------------------
    // HANA stored-procedure methods — not H2-testable
    // -----------------------------------------------------------------------

    /** Legacy: LiquidarCustodiaEmprestito.java — requires HANA SP */
    @Override
    public void liquidarEmprestito(Long idEmpresa, Long idPortafolio, Long idCliente) {
        throw new UnsupportedOperationException("requires HANA SP: SP_LIQUIDAR_CUSTODIA_EMPRESTITO");
    }

    /** Legacy: AnticiparPrestamo.java — requires HANA SP */
    @Override
    public void anticiparPrestamo(Long idEmpresa, Long idPortafolio, String tipoPrestamo) {
        throw new UnsupportedOperationException("requires HANA SP: SP_ANTICIPAR_PRESTAMO");
    }

    /** Legacy: GuardarPatrimonioRetroactivo.java — requires HANA SP */
    @Override
    public void guardarPatrimonio(Long idEmpresa, String fechaInicio, String fechaTermino) {
        throw new UnsupportedOperationException("requires HANA SP: SP_GUARDAR_PATRIMONIO_RETROACTIVO");
    }
}
