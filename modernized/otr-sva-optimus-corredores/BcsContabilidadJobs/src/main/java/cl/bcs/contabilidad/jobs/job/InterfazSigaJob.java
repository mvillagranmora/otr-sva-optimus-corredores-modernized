package cl.bcs.contabilidad.jobs.job;

import cl.bcs.contabilidad.domain.port.out.InterfazContadorSource;
import cl.bcs.contabilidad.domain.port.out.InterfazFileTransfer;
import cl.bcs.contabilidad.domain.port.out.InterfazSapSource;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.infrastructure.sftp.SftpSettings;
import cl.bcs.contabilidad.jobs.runtime.ContabilidadJob;
import cl.bcs.contabilidad.jobs.runtime.JobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * What the two SIGA interface jobs share (CONT-J-20..55).
 *
 * <p>Legacy {@code transaction(con)} of CreaInterfazDiaria.java:255-275 and CreaInterfazDemanda.java:293-314: load
 * the SFTP credentials file from the working directory (now the SFTP settings, DEV-J-01), run {@code logica}, and
 * return false on any exception, true otherwise. Inside {@code logica} only the {@code PLA.RutaBaseArchivos} lookup
 * (empresa 1, module PLATAFORMA) is outside the try (CreaInterfazDiaria.java:74, CreaInterfazDemanda.java:72), so it
 * is the only other way to get false; an empty value is also false (DEV-J-02; the legacy wrote under a literal "null"
 * prefix). Everything else is logged to the run log and the run commits.
 */
abstract sealed class InterfazSigaJob implements ContabilidadJob permits CreaInterfazDiaria, CreaInterfazDemanda {

    static final long ID_EMPRESA = 1L;
    static final String MODULO = "PLATAFORMA";
    static final String PARAMETRO_RUTA_BASE = "PLA.RutaBaseArchivos";
    static final String SUBDIRECTORIO_FI = "Transmisiones/Salida/FI/";
    static final String SUBDIRECTORIO_BPC = "Transmisiones/Salida/BPC/";

    private static final Logger log = LoggerFactory.getLogger(InterfazSigaJob.class);

    private final PlataformaPort plataforma;
    private final InterfazContadorSource contador;
    private final ConexionSap conexionSap;
    private final InterfazFileTransfer transferencia;
    private final Supplier<SftpSettings> sftpSettings;
    final InterfazSapSource sap;
    final Clock clock;

    InterfazSigaJob(PlataformaPort plataforma, InterfazContadorSource contador, InterfazSapSource sap,
                    ConexionSap conexionSap, InterfazFileTransfer transferencia, Supplier<SftpSettings> sftpSettings,
                    Clock clock) {
        this.plataforma = Objects.requireNonNull(plataforma, "plataforma");
        this.contador = Objects.requireNonNull(contador, "contador");
        this.sap = Objects.requireNonNull(sap, "sap");
        this.conexionSap = Objects.requireNonNull(conexionSap, "conexionSap");
        this.transferencia = Objects.requireNonNull(transferencia, "transferencia");
        this.sftpSettings = Objects.requireNonNull(sftpSettings, "sftpSettings");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public final boolean transaction(JobContext ctx) {
        try {
            sftpSettings.get();
            String rutaBase = rutaBase();
            generar(ctx, rutaBase, LocalDate.now(clock));
            return true;
        } catch (RuntimeException e) {
            log.warn("{}: not run ({})", clase(), e.toString());
            return false;
        }
    }

    /** The legacy {@code logica} after the base path lookup. Never throws: failures go to the run log. */
    abstract void generar(JobContext ctx, String rutaBase, LocalDate hoy);

    private String rutaBase() {
        String ruta = plataforma.obtenerParametroConfiguracion(ID_EMPRESA, MODULO, PARAMETRO_RUTA_BASE, null);
        if (ruta == null || ruta.isBlank()) {
            throw new IllegalStateException(PARAMETRO_RUTA_BASE + " is not configured for empresa " + ID_EMPRESA);
        }
        return ruta;
    }

    /** ConsultaContadorInterfazSIGA.sql: the legacy iterated backwards keeping the last value, i.e. the first row. */
    final long contador(LocalDate fechaEjecucion) {
        List<Long> filas = contador.contadoresInterfazSiga(fechaEjecucion);
        return filas == null || filas.isEmpty() || filas.getFirst() == null ? 0L : filas.getFirst();
    }

    /** The legacy "Establecer conexion" step; a failure aborts the whole generation (see {@link ConexionSap}). */
    final void conectarSap(LogServiceBroad bitacora) {
        bitacora.info("Estableciendo conexion a SAP");
        conexionSap.comprobar();
    }

    /**
     * Removes the remote copy, then uploads {@code directorio} to {@code /home/interfaz-bpc/}. Both share one try:
     * a failed removal skips the upload. An IOException is logged with its message; a RuntimeException is left to
     * the caller's outer catch, as the legacy catch list (JSchException, IllegalAccessException, IOException,
     * SftpException) did (CreaInterfazDiaria.java:198-226, CreaInterfazDemanda.java:219-245).
     */
    final void publicar(Path directorio, LogServiceBroad bitacora) {
        String remoto = InterfazFileTransfer.REMOTE_BASE_DIRECTORY;
        try {
            transferencia.removeRemoteCopy(remoto, directorio);
            transferencia.uploadDirectory(remoto, directorio);
        } catch (IOException e) {
            bitacora.error(e.getMessage());
        }
    }
}
