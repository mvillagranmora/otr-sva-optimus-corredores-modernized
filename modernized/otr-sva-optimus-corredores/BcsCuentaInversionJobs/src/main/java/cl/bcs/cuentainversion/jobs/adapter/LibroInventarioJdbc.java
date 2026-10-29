package cl.bcs.cuentainversion.jobs.adapter;

import cl.bcs.cuentainversion.jobs.job.port.LibroInventario;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Types;

/**
 * Production {@link LibroInventario} (legacy LibroInventarioJob.java:305-325): the HANA procedure
 * {@code ActualizarSaldoLibroInventario(FechaMovimiento, IdEjecucion, OUT cuentasAfectadas)} on the connection of the job transaction. The
 * {@link java.sql.SQLException} goes through unchanged (the job swallows it, CIJ-DEF-07).
 */
@Component
public class LibroInventarioJdbc implements LibroInventario {

    static final String LLAMADA = "CALL \"BSCL.CuentaInversion.BD.Servicios::ActualizarSaldoLibroInventario\"(?,?,?);";

    private final DataSource dataSource;

    public LibroInventarioJdbc(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public long actualizarSaldo(String fechaMovimiento, long idEjecucion) throws Exception {
        Connection con = DataSourceUtils.getConnection(dataSource);
        try (CallableStatement sp = con.prepareCall(LLAMADA)) {
            sp.setString(1, fechaMovimiento);
            sp.setLong(2, idEjecucion);
            sp.registerOutParameter(3, Types.BIGINT);
            sp.execute();
            // Review M-8: kept on purpose. The legacy (LibroInventarioJob.java:40) registers Types.BIGINT but reads it with getInt(3);
            // the count is only printed, so the narrowing is bug-for-bug and harmless below 2^31 (driver error above it, as in the legacy).
            return sp.getInt(3);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }
}
