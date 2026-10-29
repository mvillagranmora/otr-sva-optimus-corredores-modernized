package cl.bcs.utils.sesion;

import java.util.Optional;

/**
 * RULE-024 / RULE-025: idle timeout per channel (legacy {@code SesionUtils.GetParametro}, {@code SesionUtils.java:42-81},
 * and its use in {@code ProcesaLlamadaServletCall.java:92-95, 123, 170}). Enforcement (reading PLA.Configuracion,
 * updating ACC.Sesion) moves to the Platform Service in Phase 5.
 *
 * <ul>
 *   <li>A {@code null} channel is {@code COR} ({@link #canalEfectivo}).</li>
 *   <li>{@code WS}, {@code CLI}, {@code AGE} and {@code COR} read {@code PLA.IdleMaxTime<channel>}. The match is
 *       case-SENSITIVE. Any other channel uses 1 800 000 ms.</li>
 *   <li>A session is expired iff {@code now - lastUse > idleMax} (strict).</li>
 *   <li>RULE-025 (approved deviation, 2026-10-16): COR is no longer exempt. It has its own parameter
 *       {@code PLA.IdleMaxTimeCOR}, the idle check applies to it, and its last use is updated, like any other channel.</li>
 * </ul>
 */
public final class PoliticaInactividad {

    public static final long IDLE_MAX_POR_DEFECTO_MS = 1_800_000L;
    private static final String CANAL_POR_DEFECTO = "COR";

    private PoliticaInactividad() {
    }

    /** The channel used for the call: {@code COR} when none is given. */
    public static String canalEfectivo(String canal) {
        return canal == null ? CANAL_POR_DEFECTO : canal;
    }

    /** The PLA.Configuracion parameter of the channel, if it has one; {@code null} throws NPE. */
    public static Optional<String> codigoParametro(String canal) {
        return switch (canal) {
            case "WS", "CLI", "AGE", "COR" -> Optional.of("PLA.IdleMaxTime" + canal);
            default -> Optional.empty();
        };
    }

    /**
     * The idle maximum in ms: {@link Long#parseLong} of the configured value for a channel with a parameter (a
     * {@code null} or non-numeric value throws {@link NumberFormatException}, as the legacy caller did), else the
     * default.
     */
    public static long idleMaxMs(String canal, String valorConfigurado) {
        return codigoParametro(canal).isPresent() ? Long.parseLong(valorConfigurado) : IDLE_MAX_POR_DEFECTO_MS;
    }

    /** Strict: expired only when more than {@code idleMaxMs} elapsed. Every channel is checked (RULE-025). */
    public static boolean sesionExpirada(String canal, long ahoraUtcMs, long ultimoUsoUtcMs, long idleMaxMs) {
        return ahoraUtcMs - ultimoUsoUtcMs > idleMaxMs;
    }

    /** Whether a call updates the session's last use: always, since RULE-025 (the legacy skipped COR). */
    public static boolean actualizaUltimoUso(String canal) {
        return true;
    }
}
