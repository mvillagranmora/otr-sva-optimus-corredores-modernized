package cl.bcs.normativa.jobs.service.interfaz;

import cl.bcs.normativa.jobs.domain.ParametrosJob;
import cl.bcs.normativa.jobs.domain.ResultadoJob;
import cl.bcs.normativa.jobs.port.out.ConfiguracionPort;
import cl.bcs.normativa.jobs.port.out.ProgramaLogPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Modernized service for regulatory interface generation jobs.
 *
 * Covers legacy jobs:
 *   BcsJobGenerarInterfazC08CBC/CBI/CBN/Todos
 *   BcsJobGenerarInterfazC40CBC/CBI/CBN/INT/Todos
 *   BcsJobGenerarInterfazP40Aseguradora/Filial/SBIF
 *   BcsJobGenerarInterfazCliente/ClientesEnLinea/ClientesFinales
 *   BcsJobGenerarInterfazDCI / DPD / RCP / SIG
 *   BcsJobGenerarInterfazOrdenes / OrdenesEnLinea
 *   BcsJobGenerarInterfazPolizas / DetalleOperacionesPolizas
 *   BcsJobGenerarInterfazInstrumentosFinancieros
 *   BcsJobGenerarInterfazLavadoDinero
 *   BcsJobGenerarInterfazMontosIntermediados
 *   BcsJobGenerarInterfazAsignaciones
 *   BcsJobGenerarInterfazArchivoCM
 *   BcsJobGenerarC08CBP / BcsJobGenerarC40CBP
 *   BcsJobGenerarP40FilialUnitario
 *
 * B-NJB-INT-01: All interface generation jobs delegate to HANA-specific
 *               service objects (GenerarInterfazXxx.logica()) and update
 *               BAT.Programa with the generated file path.
 *
 * All methods require HANA; integration tests: @Disabled("requires HANA").
 */
@Service
public class InterfazService {

    private static final Logger log = LoggerFactory.getLogger(InterfazService.class);

    private final ConfiguracionPort configuracionPort;
    private final ProgramaLogPort programaLogPort;

    public InterfazService(ConfiguracionPort configuracionPort, ProgramaLogPort programaLogPort) {
        this.configuracionPort = configuracionPort;
        this.programaLogPort = programaLogPort;
    }

    /**
     * Executes a generic interface generation job.
     * The actual HANA logic is delegated; this method provides common
     * parameter extraction and error handling.
     *
     * @param parametros  job parameters
     * @param nombreJob   job identifier for logging
     * @param delegate    HANA-specific delegate (lambda wrapping the legacy .logica() call)
     */
    public ResultadoJob ejecutar(ParametrosJob parametros,
                                 String nombreJob,
                                 InterfazDelegate delegate) {
        Objects.requireNonNull(parametros, "parametros must not be null");
        Objects.requireNonNull(nombreJob, "nombreJob must not be null");
        Objects.requireNonNull(delegate, "delegate must not be null");

        log.info("Interfaz {} activada: jobId={}", nombreJob, parametros.jobId());
        try {
            InterfazResultado resultado = delegate.ejecutar(parametros);
            if (resultado.nombreArchivo() != null) {
                programaLogPort.actualizarPrograma(
                        parametros.jobId(),
                        resultado.nombreArchivo(),
                        resultado.nombreArchivoLog(),
                        resultado.comentario());
                log.info("Interfaz {} finalizada: archivo={}", nombreJob, resultado.nombreArchivo());
                return ResultadoJob.ok("Interfaz " + nombreJob + " generada: " + resultado.nombreArchivo());
            } else {
                log.warn("Interfaz {} sin archivo de salida: jobId={}", nombreJob, parametros.jobId());
                return ResultadoJob.error("Interfaz " + nombreJob + " no generó archivo");
            }
        } catch (Exception e) {
            log.error("Error en interfaz {}: jobId={}", nombreJob, parametros.jobId(), e);
            return ResultadoJob.error("Error en interfaz " + nombreJob + ": " + e.getMessage());
        }
    }

    /**
     * Extracts common parameters for interface jobs.
     * Pure logic — testable without Spring or HANA.
     *
     * @return extracted IdEmpresa (validated as long)
     */
    public static long extraerIdEmpresa(ParametrosJob parametros) {
        Objects.requireNonNull(parametros, "parametros must not be null");
        return parametros.requeridoLong("IdEmpresa");
    }

    /**
     * Extracts IdUsuario from parameters.
     * Pure logic — testable without Spring or HANA.
     */
    public static long extraerIdUsuario(ParametrosJob parametros) {
        Objects.requireNonNull(parametros, "parametros must not be null");
        return parametros.requeridoLong("IdUsuario");
    }

    /** Delegate interface for HANA-specific interface generation logic. */
    @FunctionalInterface
    public interface InterfazDelegate {
        InterfazResultado ejecutar(ParametrosJob parametros) throws Exception;
    }

    /**
     * Result of an interface generation delegate call.
     * Maps to the OUT parameters of legacy GenerarInterfazXxx.logica().
     */
    public record InterfazResultado(String nombreArchivo, String nombreArchivoLog, String comentario) {
        public static InterfazResultado vacio() {
            return new InterfazResultado(null, null, null);
        }
    }
}
