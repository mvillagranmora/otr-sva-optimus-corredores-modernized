package cl.bcs.normativa.cartolas.entidades;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Parametro implements Serializable {

    public static final class Builder {

        public static Parametro newInstance(
                final ResultSet rs)
                        throws SQLException {
            final Parametro parametro = new Parametro();
            parametro.setId(rs.getLong("IdParametro"));
            parametro.setIdNomina(rs.getLong("IdNomina"));
            parametro.setNombre(rs.getString("Nombre"));
            parametro.setValor(rs.getString("Valor"));
            return parametro;
        }

        private Builder() {
        }

    }

    private static final long serialVersionUID = 1L;

    public static Parametro newInstance(
            final ResultSet rs)
                    throws SQLException {
        return Builder.newInstance(rs);
    }

    private long id;
    private long idNomina;
    private String nombre;
    private String valor;

    public long getId() {
        return id;
    }

    public long getIdNomina() {
        return idNomina;
    }

    public String getNombre() {
        return nombre;
    }

    public String getValor() {
        return valor;
    }

    public void setId(
            final long id) {
        this.id = id;
    }

    public void setIdNomina(
            final long idNomina) {
        this.idNomina = idNomina;
    }

    public void setNombre(
            final String nombre) {
        this.nombre = nombre;
    }

    public void setValor(
            final String valor) {
        this.valor = valor;
    }
}
