package cl.bcs.cartolas.domain;

/**
 * Constantes booleanas legacy SI/NO.
 */
public final class Opcion {

    public static final String NO = "NO";
    public static final String SI = "SI";

    /**
     * Convierte un booleano a la representación legacy SI/NO.
     */
    public static String of(final boolean value) {
        return value ? SI : NO;
    }

    /**
     * Convierte una cadena SI/NO a booleano.
     */
    public static boolean isTrue(final String valor) {
        return SI.equalsIgnoreCase(valor);
    }

    private Opcion() {
    }
}
