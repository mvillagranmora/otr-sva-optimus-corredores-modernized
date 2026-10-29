package cl.bcs.cuentainversion.jobs.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cl.bcs.cuentainversion.jobs.job.port.LibroInventario;
import cl.bcs.cuentainversion.jobs.runtime.CuentaInversionJob;
import cl.bcs.cuentainversion.jobs.runtime.JobContext;
import java.util.Objects;

/**
 * Legacy {@code cl.bcs.cuentaInversion.jobs.LibroInventarioJob} (95 lines): calls the stored procedure ActualizarSaldoLibroInventario.
 */
public final class LibroInventarioJob implements CuentaInversionJob {

    public static final String CLASE = "cl.bcs.cuentaInversion.jobs.LibroInventarioJob";

    private final LibroInventario libroInventario;

    public LibroInventarioJob(LibroInventario libroInventario) {
        this.libroInventario = Objects.requireNonNull(libroInventario, "libroInventario");
    }

    @Override
    public String clase() {
        return CLASE;
    }

    @Override
    public boolean transaction(JobContext ctx) throws Exception {
        // LibroInventarioJob.java:299-334. CIJ-DEF-07: every exception (also a bad IdEjecucion) is swallowed and answered as false.
        String fechaMovimiento = ctx.getParam("FechaMovimiento");
        String idEjecucion = ctx.getParam("IdEjecucion");
        try {
            long cuentasAfectadas = libroInventario.actualizarSaldo(fechaMovimiento, Long.parseLong(idEjecucion));
            log.info("LibroInventarioJob Cuentas afectadas={}", cuentasAfectadas);
        } catch (Exception e) {
            log.error("LibroInventarioJob error de ejecucion: {}", e.getMessage(), e);
            return false; // CIJ-DEF-01: a rollback that the framework reports like a commit
        }
        return true;
    }

    private static final Logger log = LoggerFactory.getLogger(LibroInventarioJob.class);
}
