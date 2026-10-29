package cl.bcs.cuentainversion.jobs.job.port;

/**
 * The HANA stored procedure {@code "BSCL.CuentaInversion.BD.Servicios::ActualizarSaldoLibroInventario"(?,?,?)} called by
 * {@code LibroInventarioJob}: {@code (FechaMovimiento String, IdEjecucion Long, OUT cuentasAfectadas BIGINT)}. One call of
 * {@link #actualizarSaldo} is one legacy {@code CallableStatement.execute()}; the procedure runs in the job transaction.
 */
public interface LibroInventario {

    /** @return the number of affected accounts (the legacy only prints it) */
    long actualizarSaldo(String fechaMovimiento, long idEjecucion) throws Exception;
}
