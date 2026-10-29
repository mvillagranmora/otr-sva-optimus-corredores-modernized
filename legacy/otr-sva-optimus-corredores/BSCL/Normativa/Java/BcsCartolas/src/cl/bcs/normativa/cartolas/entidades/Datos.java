package cl.bcs.normativa.cartolas.entidades;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Datos implements Serializable {

    public static final class Builder {

        public static Datos newInstance(
                final long idCartola,
                final String nombre,
                final String formato,
                final String valor) {
            final Datos datos = new Datos();
            datos.setIdCartola(idCartola);
            datos.setNombre(nombre);
            datos.setFormato(formato);
            datos.setValor(valor);
            return datos;
        }

        public static Datos newInstance(
                final ResultSet rs)
                        throws SQLException {
            final Datos datos = new Datos();
            datos.setId(rs.getLong("IdDatos"));
            datos.setIdCartola(rs.getLong("IdCartola"));
            datos.setNombre(rs.getString("Nombre"));
            datos.setFormato(rs.getString("Formato"));
            datos.setValor(rs.getString("Valor"));
            return datos;
        }

        private Builder() {
        }

    }

    private static final long serialVersionUID = 1L;

    public static Datos newInstance(
            final Cartola cartola,
            final String nombre,
            final String formato) {
        return newInstance(cartola.getId(), nombre, formato, null);
    }

    public static Datos newInstance(
            final Cartola cartola,
            final String nombre,
            final String formato,
            final String valor) {
        return newInstance(cartola.getId(), nombre, formato, valor);
    }

    public static Datos newInstance(
            final long idCartola,
            final String nombre,
            final String formato) {
        return newInstance(idCartola, nombre, formato, null);
    }

    public static Datos newInstance(
            final long idCartola,
            final String nombre,
            final String formato,
            final String valor) {
        return Builder.newInstance(idCartola, nombre, formato, valor);
    }

    public static Datos newInstance(
            final ResultSet rs)
                    throws SQLException {
        return Builder.newInstance(rs);
    }

    private String formato;
    private long id;
    private long idCartola;
    private String nombre;
    private String valor;

    public String getFormato() {
        return formato;
    }

    public long getId() {
        return id;
    }

    public long getIdCartola() {
        return idCartola;
    }

    public String getNombre() {
        return nombre;
    }

    public String getValor() {
        return valor;
    }

    public void setFormato(
            final String formato) {
        this.formato = formato;
    }

    public void setId(
            final long id) {
        this.id = id;
    }

    public void setIdCartola(
            final long idCartola) {
        this.idCartola = idCartola;
    }

    public void setNombre(
            final String nombre) {
        this.nombre = nombre;
    }

    public void setValor(
            final String valor) {
        this.valor = valor;
    }

    @Override
    public String toString() {
        return "Datos [id=" + id + ", idCartola=" + idCartola + ", nombre=" + nombre + ", formato="
                + formato + "]";
    }

}
