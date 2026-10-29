package cl.bcs.normativa.esquema;

/**
 * Par de valores resultado de comparar origen y destino.
 *
 * @author fpino@email.com
 */
public class ParComparativo implements Comparable<ParComparativo> {

    /**
     * Valor en el destino.
     */
    private final Comparable<?> valorDestino;

    /**
     * Valor en el origen.
     */
    private final Comparable<?> valorOrigen;

    /**
     * Constructor.
     *
     * @param valorOrigen
     *            El valor en el origen.
     * @param valorDestino
     *            El valor en el destino.
     */
    public ParComparativo(final Comparable<?> valorOrigen, final Comparable<?> valorDestino) {
        this.valorOrigen = valorOrigen;
        this.valorDestino = valorDestino;
    }

    @SuppressWarnings("unchecked")
    @Override
    public int compareTo(final ParComparativo other) {
        if (this == other) {
            return 0;
        }

        final int cmp = this.getValorOrigen().compareTo(other.getValorOrigen());
        if (cmp != 0) {
            return cmp;
        }

        return this.getValorOrigen().compareTo(other.getValorOrigen());
    }

    /**
     * Obtiene el valor en el destino.
     *
     * @return El valor en el destino.
     */
    @SuppressWarnings("rawtypes")
    public Comparable getValorDestino() {
        return valorDestino;
    }

    /**
     * Obtiene el valor en el origen.
     *
     * @return El valor en el origen.
     */
    @SuppressWarnings("rawtypes")
    public Comparable getValorOrigen() {
        return valorOrigen;
    }

    @Override
    public String toString() {
        return "[Origen=" + valorOrigen + ", Destino=" + valorDestino + "]";
    }

}
