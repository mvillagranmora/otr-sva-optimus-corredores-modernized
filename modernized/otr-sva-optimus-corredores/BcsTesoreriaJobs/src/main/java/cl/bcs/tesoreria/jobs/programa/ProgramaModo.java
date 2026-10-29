package cl.bcs.tesoreria.jobs.programa;

/**
 * How {@code POST /internal/job-call} resolves what to run ({@code tesoreria.jobs.programa.modo}).
 *
 * <ul>
 *   <li>{@link #PROGRAMA} (production default, job pattern H4): the body only needs {@code idPrograma}; class and parameters come from
 *       {@code BAT.Programa}; the program is claimed in the database before it runs; the final state is written back.</li>
 *   <li>{@link #PARAMETROS_DEL_LLAMADOR}: the contract that the 508-454 golden and runner tests pin: the caller sends class and
 *       parameters and nothing is read or written in {@code BAT}. For tests and local runs only.</li>
 * </ul>
 */
public enum ProgramaModo {

    PROGRAMA("programa"),
    PARAMETROS_DEL_LLAMADOR("parametros-del-llamador");

    private final String valor;

    ProgramaModo(String valor) {
        this.valor = valor;
    }

    public String valor() {
        return valor;
    }

    /** Parses the property value; an unknown value fails the start (a wrong mode must never fall back silently). */
    public static ProgramaModo desde(String texto) {
        String t = texto == null ? "" : texto.strip();
        for (ProgramaModo m : values()) {
            if (m.valor.equals(t)) {
                return m;
            }
        }
        throw new IllegalArgumentException("tesoreria.jobs.programa.modo must be 'programa' or 'parametros-del-llamador', not '" + t + "'");
    }
}
