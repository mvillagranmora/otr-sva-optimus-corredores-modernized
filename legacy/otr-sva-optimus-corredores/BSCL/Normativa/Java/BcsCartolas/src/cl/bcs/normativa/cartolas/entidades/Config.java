package cl.bcs.normativa.cartolas.entidades;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVReader;

public class Config implements Serializable {

    public static final class Builder {

        public static Config newInstance(
                final ResultSet rs)
                        throws SQLException {
            final Config config = new Config();
            config.setId(rs.getLong("IdConfig"));
            config.setIdEmpresa(rs.getLong("IdEmpresa"));
            config.setDescripcion(rs.getString("Descripcion"));
            config.setTipo(rs.getString("Tipo"));
            config.setNombre(rs.getString("Nombre"));
            config.setValor(rs.getString("Valor"));
            return config;
        }

        private Builder() {
        }

    }

    private static final long serialVersionUID = 1L;

    public static List<Config> loadFromCsv(
            final InputStream is)
                    throws UnsupportedEncodingException,
                    IOException {
        final List<Config> configs = new ArrayList();
        try (final CSVReader reader = new CSVReader(new InputStreamReader(is, "UTF-8"), ';')) {
            reader.readNext(); // Skip header.
            String[] line = reader.readNext();
            while (line != null) {
                final Config config = new Config();
                // Debe mantenerse el orden los campos.
                config.setTipo(line[0]);
                config.setNombre(line[1]);
                config.setDescripcion(line[2]);
                config.setValor(line[3]);
                configs.add(config);
                line = reader.readNext();
            }
        }

        return configs;
    }

    public static Config newInstance(
            final ResultSet rs)
                    throws SQLException {
        return Builder.newInstance(rs);
    }

    private String descripcion;
    private long id;
    private long idEmpresa;
    private String nombre;
    private String tipo;
    private String valor;

    public String getDescripcion() {
        return descripcion;
    }

    public long getId() {
        return id;
    }

    public long getIdEmpresa() {
        return idEmpresa;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public String getValor() {
        return valor;
    }

    public void setDescripcion(
            String descripcion) {
        this.descripcion = descripcion;
    }

    public void setId(
            final long id) {
        this.id = id;
    }

    public void setIdEmpresa(
            final long idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public void setNombre(
            final String nombre) {
        this.nombre = nombre;
    }

    public void setTipo(
            String tipo) {
        this.tipo = tipo;
    }

    public void setValor(
            final String valor) {
        this.valor = valor;
    }
}
