package cl.bcs.normativa.esquema;

import java.io.Serializable;

/**
 * Datos relevantes de un usuario.
 *
 * @author fpino@email.com
 *
 */
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * C&oacute;digo del usuario.
     */
    private String codigo;

    /**
     * Estado del usuario.
     */
    private String estado;

    /**
     * Id del usuario.
     */
    private long id;

    /**
     * Obtiene el c&oacute;digo del usuario.
     *
     * @return El c&oacute;digo del usuario.
     */
    public String getCodigo() {
        return codigo;
    }

    /**
     * Obtiene el estado del usuario.
     *
     * @return El estado del usuario.
     */
    public String getEstado() {
        return estado;
    }

    /**
     * Obtiene el id del usuario.
     *
     * @return El id del usuario.
     */
    public long getId() {
        return id;
    }

    /**
     * Asigna el c&oacute;digo del usuario.
     *
     * @param codigo
     *            El c&oacute;digo del usuario.
     */
    public void setCodigo(final String codigo) {
        this.codigo = codigo;
    }

    /**
     * Asigna el estado del usuario.
     *
     * @param estado
     *            El estado del usuario.
     */
    public void setEstado(final String estado) {
        this.estado = estado;
    }

    /**
     * Asigna el id del usuario.
     *
     * @param id
     *            El id del usuario.
     */
    public void setId(final long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Usuario [codigo=" + codigo + ", estado=" + estado + ", id=" + id + "]";
    }

}
