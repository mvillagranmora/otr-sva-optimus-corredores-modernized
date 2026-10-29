package cl.bcs.contabilidad.jobs.guarda;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * One mark of the fail-closed guard (job pattern, step 6), stored as the {@code Mensaje} of a row of {@code BAT.LogPrograma}:
 * <pre>H4G|&lt;FASE&gt;|&lt;idPrograma&gt;|&lt;intento&gt;|&lt;clase&gt;|&lt;n&gt;</pre>
 * {@code FASE} is {@link Fase#INTENTO} (written before the HTTP call), {@link Fase#HECHO} (the call answered 2xx) or {@link Fase#FALLO}
 * (the call failed). {@code intento} is {@code BAT.Programa.Intento} of the run (it grows with every claim), {@code clase} the simple name of
 * the collaborator (for example {@code CrearOrdenes}) and {@code n} the 1-based ordinal of the guarded call in that run. The three phases of
 * one call share the same (idPrograma, intento, clase, n): that is how an INTENTO is matched with its close.
 *
 * <p>The text is plain ASCII, far below the 5000 characters of {@code LogPrograma.Mensaje}, and starts with {@link #PREFIJO} so a
 * {@code LIKE 'H4G|%'} finds the marks among the other log lines of the program.
 */
public record MarcaGuarda(Fase fase, long idPrograma, int intento, String clase, int n) {

    public enum Fase { INTENTO, HECHO, FALLO }

    public static final String PREFIJO = "H4G|";
    private static final char SEPARADOR = '|';
    private static final Pattern CLASE_VALIDA = Pattern.compile("[A-Za-z0-9_.]{1,100}");

    public MarcaGuarda {
        if (fase == null || clase == null || !CLASE_VALIDA.matcher(clase).matches()) {
            throw new IllegalArgumentException("invalid guard mark: phase and collaborator name ([A-Za-z0-9_.]{1,100}) are required");
        }
        if (idPrograma < 0 || intento < 0 || n < 1) {
            throw new IllegalArgumentException("invalid guard mark: idPrograma and intento must be >= 0 and n >= 1");
        }
    }

    /** The same call in another phase. */
    public MarcaGuarda en(Fase otra) {
        return new MarcaGuarda(otra, idPrograma, intento, clase, n);
    }

    /** The text stored in {@code LogPrograma.Mensaje}. */
    public String formatear() {
        return PREFIJO + fase + SEPARADOR + idPrograma + SEPARADOR + intento + SEPARADOR + clase + SEPARADOR + n;
    }

    /** The key shared by the INTENTO of a call and its HECHO or FALLO. */
    public String llave() {
        return idPrograma + "|" + intento + "|" + clase + "|" + n;
    }

    /** Parses a {@code Mensaje}; empty when it is not a well formed mark (the caller treats a {@code H4G|} text that does not parse as unreadable). */
    public static Optional<MarcaGuarda> parsear(String mensaje) {
        if (mensaje == null || !mensaje.startsWith(PREFIJO)) {
            return Optional.empty();
        }
        String[] partes = mensaje.split("\\|", -1);
        if (partes.length != 6) {
            return Optional.empty();
        }
        try {
            return Optional.of(new MarcaGuarda(Fase.valueOf(partes[1]), Long.parseLong(partes[2]), Integer.parseInt(partes[3]),
                    partes[4], Integer.parseInt(partes[5])));
        } catch (IllegalArgumentException e) { // bad phase, number or name (NumberFormatException is one)
            return Optional.empty();
        }
    }
}
