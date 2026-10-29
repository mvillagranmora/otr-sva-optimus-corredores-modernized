package cl.bcs.fondomutuo.kit;

/** Legacy {@code ConfiguracionSistema}: {@code "Parametro X en modulo Y no encontrado"} (also what the legacy NPE of an empresa with no rows amounts to). */
public class ParametroNoEncontradoException extends RuntimeException {

    public ParametroNoEncontradoException(String nombre, String modulo) {
        super("Parametro " + nombre + " en modulo " + modulo + " no encontrado");
    }
}
