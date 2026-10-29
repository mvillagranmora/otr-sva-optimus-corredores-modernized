package cl.bcs.normativa.domain;

import java.io.Serializable;

/**
 * Datos relevantes de un usuario.
 * Migrado desde legacy cl.bcs.normativa.esquema.Usuario.
 */
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    private String codigo;
    private String estado;
    private long id;

    public String getCodigo() { return codigo; }
    public String getEstado() { return estado; }
    public long getId() { return id; }

    public void setCodigo(String codigo) { this.codigo = codigo; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setId(long id) { this.id = id; }

    @Override
    public String toString() {
        return "Usuario[id=" + id + ", codigo=" + codigo + "]";
    }
}
