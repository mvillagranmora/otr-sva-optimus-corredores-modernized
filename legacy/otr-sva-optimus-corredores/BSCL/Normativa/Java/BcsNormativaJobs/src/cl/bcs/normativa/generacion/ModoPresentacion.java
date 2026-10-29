package cl.bcs.normativa.generacion;

/**
 * Modos de presentaci&oacute;n.
 *
 * @author fpino@email.com
 *
 */
public enum ModoPresentacion {

    /**
     * Presentaci&oacute;n de la declaraci&oacute;n por Internet.
     */
    INTERNET("I"),

    /**
     * Presentaci&oacute;n de la declaraci&oacute;n por medio magn&eacute;tico.
     */
    MAGNETICO("M");

    /**
     * C&oacute;digo del modo de presentaci&oacute;n.
     */
    private final String codigo;

    /**
     * Constructor.
     *
     * @param codigo
     *            El c&oacute;digo del modo de presentaci&oacute;n.
     */
    private ModoPresentacion(final String codigo) {
        this.codigo = codigo;
    }

    /**
     * Obtiene el c&oacute;digo del modo de presentaci&oacute;n.
     *
     * @return El c&oacute;digo del modo de presentaci&oacute;n.
     */
    public String getCodigo() {
        return codigo;
    }

}
