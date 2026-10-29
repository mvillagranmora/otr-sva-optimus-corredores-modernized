package cl.bcs.normativa.generacion;

/**
 * Tipos de declaraciones.
 *
 * @author fpino@email.com
 *
 */
public enum TipoDeclaracion {

    /**
     * Declaraci&oacute;n original.
     */
    ORIGINAL("O"),

    /**
     * Rectificatoria de declaraci&oacute;n.
     */
    RECTIFICATORIA("R");

    /**
     * C&oacute;digo del tipo de declaraci&oacute;n.
     */
    private final String codigo;

    /**
     * Constructor.
     *
     * @param codigo
     *            El c&oacute;digo del tipo de declaraci&oacute;n.
     */
    private TipoDeclaracion(final String codigo) {
        this.codigo = codigo;
    }

    /**
     * Obtiene el c&oacute;digo del tipo de declaraci&oacute;n.
     *
     * @return El c&oacute;digo del tipo de declaraci&oacute;n.
     */
    public String getCodigo() {
        return codigo;
    }

}
