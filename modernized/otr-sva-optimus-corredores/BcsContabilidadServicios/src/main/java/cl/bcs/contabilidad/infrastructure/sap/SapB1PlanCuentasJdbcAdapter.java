package cl.bcs.contabilidad.infrastructure.sap;

import cl.bcs.contabilidad.domain.model.sap.CuentaSap;
import cl.bcs.contabilidad.domain.port.out.PlanCuentasSapPort;
import cl.bcs.contabilidad.infrastructure.persistence.SqlResources;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * {@link PlanCuentasSapPort} over the SAP B1 database: EnvioContabilidad_SelectOACT.sql with {@code :EMPRESA} replaced
 * by the company schema, as legacy did textually (EnvioContabilidad.java:372-373). The schema is validated as a plain
 * identifier before it is placed in the statement. Missing SAP B1 settings (SAP_B1_DB_* or the company) answer
 * {@link PlanCuentasSapPort.ConfiguracionNoEncontradaException}, the counterpart of legacy's missing
 * interfazSAP.properties.
 */
public final class SapB1PlanCuentasJdbcAdapter implements PlanCuentasSapPort {

    static final String SQL_SELECT_OACT = "cl/bcs/contabilidad/sql/EnvioContabilidad_SelectOACT.sql";
    private static final Pattern IDENTIFICADOR = Pattern.compile("[A-Za-z0-9_]+");

    private final Supplier<NamedParameterJdbcTemplate> sapJdbc;
    private final String compania;

    /**
     * @param sapJdbc  template over the SAP B1 database, resolved on first use; it throws
     *                 {@link IllegalStateException} when the SAP_B1_DB_* settings are missing
     * @param compania company schema (sap.b1.company / SAP_B1_COMPANY), may be blank when not configured
     */
    public SapB1PlanCuentasJdbcAdapter(Supplier<NamedParameterJdbcTemplate> sapJdbc, String compania) {
        this.sapJdbc = Objects.requireNonNull(sapJdbc, "sapJdbc");
        this.compania = compania == null ? "" : compania.strip();
    }

    @Override
    public List<CuentaSap> consultarPlanCuentas() {
        if (compania.isEmpty()) throw new ConfiguracionNoEncontradaException("SAP B1 company (sap.b1.company / SAP_B1_COMPANY) not configured");
        if (!IDENTIFICADOR.matcher(compania).matches()) throw new IllegalStateException("Invalid SAP B1 company schema: " + compania);
        NamedParameterJdbcTemplate jdbc;
        try {
            jdbc = sapJdbc.get();
        } catch (IllegalStateException e) {
            throw new ConfiguracionNoEncontradaException(e.getMessage());
        }
        String sql = SqlResources.load(SQL_SELECT_OACT).replace(":EMPRESA", compania).strip().replaceAll(";$", "");
        return jdbc.query(sql, new MapSqlParameterSource(), new DataClassRowMapper<>(CuentaSap.class));
    }
}
