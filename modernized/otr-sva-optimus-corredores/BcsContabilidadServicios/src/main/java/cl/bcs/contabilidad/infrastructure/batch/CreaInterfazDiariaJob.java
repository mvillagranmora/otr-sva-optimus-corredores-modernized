package cl.bcs.contabilidad.infrastructure.batch;

import cl.bcs.contabilidad.domain.port.out.InterfazContadorSource;
import cl.bcs.contabilidad.domain.port.out.InterfazFileTransfer;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.infrastructure.interfaz.InterfazContext;
import cl.bcs.contabilidad.infrastructure.interfaz.InterfazGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Daily SIGA interface: 9 FI + 4 BPC files, then the SFTP publication (CONT-A-01..09, CONT-A-30, CONT-A-31).
 *
 * <p>Legacy: {@code CreaInterfazDiaria.logica()} (BcsContabilidadJobs).
 * <ol>
 *   <li>{@code PLA.RutaBaseArchivos} (empresa 1, module PLATAFORMA) is read before the legacy try, so a failure
 *       there propagates (CONT-A-09).</li>
 *   <li>fechaEjecucion = local date of the run minus one calendar day (CONT-A-01).</li>
 *   <li>contador = first row of ConsultaContadorInterfazSIGA.sql, 0 without rows (CONT-A-03). A failure here ends
 *       the run: nothing is generated or uploaded and nothing is thrown (CONT-A-08).</li>
 *   <li>Each generator runs in order; a failing one is logged and the next one still runs (CONT-A-07).</li>
 *   <li>The remote copy is removed and the whole base path is uploaded to {@code /home/interfaz-bpc/}. Both share
 *       one guard: a failed removal skips the upload; any SFTP failure is logged, never thrown (CONT-A-30/31).</li>
 * </ol>
 * The output directories are the base path plus {@code Transmisiones/Salida/FI/} and {@code .../BPC/}, joined by
 * plain concatenation as the legacy did (CONT-A-05).
 */
public class CreaInterfazDiariaJob {

    static final long ID_EMPRESA = 1L;
    static final String MODULO = "PLATAFORMA";
    static final String PARAMETRO_RUTA_BASE = "PLA.RutaBaseArchivos";
    static final String SUBDIRECTORIO_FI = "Transmisiones/Salida/FI/";
    static final String SUBDIRECTORIO_BPC = "Transmisiones/Salida/BPC/";

    private static final Logger log = LoggerFactory.getLogger(CreaInterfazDiariaJob.class);

    private final PlataformaPort plataforma;
    private final InterfazContadorSource contadorSource;
    private final List<InterfazGenerator> generadores;
    private final InterfazFileTransfer transferencia;
    private final Clock clock;

    public CreaInterfazDiariaJob(PlataformaPort plataforma,
                                 InterfazContadorSource contadorSource,
                                 List<InterfazGenerator> generadores,
                                 InterfazFileTransfer transferencia,
                                 Clock clock) {
        this.plataforma = Objects.requireNonNull(plataforma, "plataforma");
        this.contadorSource = Objects.requireNonNull(contadorSource, "contadorSource");
        this.generadores = List.copyOf(generadores);
        this.transferencia = Objects.requireNonNull(transferencia, "transferencia");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /** Runs for today in the clock's zone. */
    public void ejecutar() {
        ejecutar(LocalDate.now(clock));
    }

    /**
     * Runs as if today were {@code hoy}; fechaEjecucion is {@code hoy} minus one calendar day.
     *
     * @throws InterfazConfiguracionException when {@code PLA.RutaBaseArchivos} is empty; a failure of the platform
     *                                        lookup itself propagates unchanged
     */
    public void ejecutar(LocalDate hoy) {
        String rutaBase = rutaBase();
        try {
            LocalDate fechaEjecucion = hoy.minusDays(1);
            long contador = primerContador(contadorSource.contadoresInterfazSiga(fechaEjecucion));
            InterfazContext contexto = new InterfazContext(fechaEjecucion, contador,
                    Path.of(rutaBase + SUBDIRECTORIO_FI), Path.of(rutaBase + SUBDIRECTORIO_BPC));
            log.info("Interfaz diaria: fechaEjecucion={} contador={}", fechaEjecucion, contador);

            for (InterfazGenerator generador : generadores) {
                generar(generador, contexto);
            }
            publicar(Path.of(rutaBase));
        } catch (RuntimeException e) {
            log.error("Interfaz diaria aborted: {}", e.toString());
        }
    }

    private String rutaBase() {
        String ruta = plataforma.obtenerParametroConfiguracion(ID_EMPRESA, MODULO, PARAMETRO_RUTA_BASE, null);
        if (ruta == null || ruta.isBlank()) {
            throw new InterfazConfiguracionException(PARAMETRO_RUTA_BASE + " is not configured for empresa "
                    + ID_EMPRESA);
        }
        return ruta;
    }

    /** Legacy iterated the rows backwards keeping the last assignment, i.e. the first row. */
    private static long primerContador(List<Long> filas) {
        if (filas == null || filas.isEmpty() || filas.getFirst() == null) {
            return 0L;
        }
        return filas.getFirst();
    }

    private static void generar(InterfazGenerator generador, InterfazContext contexto) {
        try {
            generador.generar(contexto);
        } catch (IOException | RuntimeException e) {
            log.error("Interfaz step {} failed: {}", generador.getClass().getSimpleName(), e.toString());
        }
    }

    private void publicar(Path rutaLocal) {
        String remoto = InterfazFileTransfer.REMOTE_BASE_DIRECTORY;
        try {
            transferencia.removeRemoteCopy(remoto, rutaLocal);
            transferencia.uploadDirectory(remoto, rutaLocal);
            log.info("Interfaz uploaded to {}", InterfazFileTransfer.remoteCopyPath(remoto, rutaLocal));
        } catch (IOException | RuntimeException e) {
            log.error("Interfaz SFTP publication failed: {}", e.toString());
        }
    }

    /** {@code PLA.RutaBaseArchivos} is missing (legacy concatenated {@code "null"} into every path). */
    public static class InterfazConfiguracionException extends IllegalStateException {
        public InterfazConfiguracionException(String message) {
            super(message);
        }
    }
}
