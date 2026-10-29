package cl.bcs.normativa.cartolas.entidades;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Cliente {
    public static Cliente newInstance(
            final ResultSet rs)
                    throws SQLException {
        final Cliente cliente = new Cliente();
        cliente.setId(rs.getLong("IdCliente"));
        cliente.setDocumentoIdentidad(rs.getString("DocumentoIdentidad"));
        cliente.setEmail(rs.getString("Direccion"));

        final String razonSocial = rs.getString("RazonSocial");
        final String nombres = rs.getString("Nombres");
        final String paterno = rs.getString("ApellidoPaterno");
        final String materno = rs.getString("ApellidoMaterno");

        String nombre = "";
        if (!razonSocial.isEmpty()) {
            nombre = razonSocial;
        } else {
            nombre = nombres + " " + paterno + " " + materno;
        }
        cliente.setNombre(nombre);
        return cliente;
    }

    private long id;
    private String nombre;
    private String documentoIdentidad;
    private String email;

    public String getDocumentoIdentidad() {
        return documentoIdentidad;
    }

    public String getEmail() {
        return email;
    }

    public long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setDocumentoIdentidad(
            final String documentoIdentidad) {
        this.documentoIdentidad = documentoIdentidad;
    }

    public void setEmail(
            String email) {
        this.email = email;
    }

    public void setId(
            final long id) {
        this.id = id;
    }

    public void setNombre(
            final String nombre) {
        this.nombre = nombre;
    }
}
