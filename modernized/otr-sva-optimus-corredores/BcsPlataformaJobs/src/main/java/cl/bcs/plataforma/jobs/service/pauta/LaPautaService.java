package cl.bcs.plataforma.jobs.service.pauta;

import cl.bcs.plataforma.jobs.domain.DetalleEjecucion;
import cl.bcs.plataforma.jobs.domain.Ejecucion;
import cl.bcs.plataforma.jobs.domain.EstadoModulo;
import cl.bcs.plataforma.jobs.domain.ProcesoPauta;
import cl.bcs.plataforma.jobs.domain.SalidaProcesoPauta;
import cl.bcs.plataforma.jobs.port.out.PautaRepositoryPort;
import cl.bcs.plataforma.jobs.port.out.ConfiguracionPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * Service for LaPauta — the main pauta (daily closing) orchestrator.
 *
 * Pure-logic methods are public static so they can be tested without HANA.
 * The full execution requires a live HANA connection and is marked @Disabled in tests.
 *
 * Business rules extracted from:
 *   legacy cl.bcs.plataforma.jobs.LaPauta
 *
 * B-PLJ-10: Date/time formatting with timezone offset (seconds) applied to LocalDateTime.now().
 *            "Fecha"  → "dd-MM-yyyy"
 *            "Hora"   → "HH:mm:ss"
 *            "Ambos"  → "dd-MM-yyyy HH:mm:ss"
 * B-PLJ-11: Module open guard — if any module (other than "SIS") is in state "ABT",
 *            pauta cannot run.
 * B-PLJ-12: Process dependency guard — a process with a non-empty CodigoProcesoDependencia
 *            can only run if its dependency is in state "TER" (terminated).
 * B-PLJ-13: Error propagation — if a process has DetienePauta = "S" and fails,
 *            pauta loop breaks immediately.
 * B-PLJ-14: Message truncation — error/output messages are truncated to 500 characters.
 */
@Service
public class LaPautaService {

    private static final Logger log = LoggerFactory.getLogger(LaPautaService.class);

    public static final String ESTADO_TERMINADO  = "TER";
    public static final String ESTADO_ERROR      = "ERR";
    public static final String ESTADO_PROCESANDO = "PRO";
    public static final String ESTADO_PENDIENTE  = "PEN";
    public static final int    LARGO_MAX_MENSAJE = 500;

    private final PautaRepositoryPort pautaRepo;
    private final ConfiguracionPort   configuracion;

    public LaPautaService(PautaRepositoryPort pautaRepo, ConfiguracionPort configuracion) {
        Objects.requireNonNull(pautaRepo, "pautaRepo must not be null");
        Objects.requireNonNull(configuracion, "configuracion must not be null");
        this.pautaRepo    = pautaRepo;
        this.configuracion = configuracion;
    }

    // -------------------------------------------------------------------------
    // Pure-logic methods (public static — testable without HANA)
    // -------------------------------------------------------------------------

    /**
     * B-PLJ-10: Format a LocalDateTime with an offset in seconds applied.
     *
     * @param peticion  one of "Fecha", "Hora", "Ambos"
     * @param offset    seconds offset to add to LocalDateTime.now()
     * @param baseTime  the base time to use (injectable for tests)
     * @return formatted string, or empty string for unknown peticion
     */
    public static String obtenerFechaHora(String peticion, Long offset, LocalDateTime baseTime) {
        Objects.requireNonNull(peticion, "peticion must not be null");
        Objects.requireNonNull(offset,   "offset must not be null");
        Objects.requireNonNull(baseTime, "baseTime must not be null");

        LocalDateTime ajustado = baseTime.plusSeconds(offset);
        return switch (peticion) {
            case "Fecha" -> ajustado.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            case "Hora"  -> ajustado.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            case "Ambos" -> ajustado.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
            default      -> "";
        };
    }

    /**
     * Convenience overload that uses LocalDateTime.now() as the base time.
     * Legacy-equivalent: LaPauta.obtenerFechaHora(peticion, offset)
     */
    public static String obtenerFechaHora(String peticion, Long offset) {
        return obtenerFechaHora(peticion, offset, LocalDateTime.now());
    }

    /**
     * B-PLJ-11: Determine whether any non-SIS module is open (state "ABT").
     *
     * @param modulos list of module state records
     * @return true if any module (other than "SIS") is in state "ABT"
     */
    public static boolean hayModuloAbierto(List<EstadoModulo> modulos) {
        Objects.requireNonNull(modulos, "modulos must not be null");
        return modulos.stream()
                .anyMatch(m -> !"SIS".equals(m.idModulo) && "ABT".equals(m.idEstado));
    }

    /**
     * B-PLJ-12: Find the dependency process by its code.
     *
     * @param codigoDependencia the dependency code to look up
     * @param procesos          full list of processes
     * @return the matching process, or null if not found
     */
    public static ProcesoPauta obtenerProcesoDependencia(
            String codigoDependencia, List<ProcesoPauta> procesos) {
        Objects.requireNonNull(codigoDependencia, "codigoDependencia must not be null");
        Objects.requireNonNull(procesos,           "procesos must not be null");
        return procesos.stream()
                .filter(p -> codigoDependencia.equalsIgnoreCase(p.codigoProceso))
                .findFirst()
                .orElse(null);
    }

    /**
     * B-PLJ-14: Truncate a message to the maximum allowed length (500 chars).
     *
     * @param mensaje original message
     * @return message truncated to LARGO_MAX_MENSAJE if necessary
     */
    public static String truncarMensaje(String mensaje) {
        Objects.requireNonNull(mensaje, "mensaje must not be null");
        return mensaje.length() > LARGO_MAX_MENSAJE
                ? mensaje.substring(0, LARGO_MAX_MENSAJE)
                : mensaje;
    }

    // -------------------------------------------------------------------------
    // HANA-dependent execution — stubbed
    // -------------------------------------------------------------------------

    /**
     * Execute the full pauta workflow against HANA.
     *
     * @throws UnsupportedOperationException always — requires a live HANA connection
     */
    public boolean ejecutarPauta(Long idEmpresa, Long idUsuario, String fechaSistemaPauta,
                                  String jobsString) {
        throw new UnsupportedOperationException(
                "LaPautaService.ejecutarPauta requires a live HANA connection");
    }
}
