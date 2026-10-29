package cl.bcs.normativa.cartolas.entidades;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import com.opencsv.CSVReader;

public class Recurso implements Serializable {

    public static final class Builder {

        public static Recurso newInstance(
                final long idPlantilla,
                final String grupo,
                final String nombre,
                final String tipo,
                final String valor) {
            final Recurso recurso = new Recurso();
            recurso.setIdPlantilla(idPlantilla);
            recurso.setEditable(Opcion.NO);
            recurso.setGrupo(grupo);
            recurso.setNombre(nombre);
            recurso.setTipo(tipo);
            recurso.setValor(valor);
            return recurso;
        }

        public static Recurso newInstance(
                final ResultSet rs)
                        throws SQLException {
            final Recurso recurso = new Recurso();
            recurso.setId(rs.getLong("IdRecurso"));
            recurso.setIdPlantilla(rs.getLong("IdPlantilla"));
            recurso.setEditable(rs.getString("Editable"));
            recurso.setDescripcion(rs.getString("Descripcion"));
            recurso.setGrupo(rs.getString("Grupo"));
            recurso.setNombre(rs.getString("Nombre"));
            recurso.setTipo(rs.getString("Tipo"));
            recurso.setValor(rs.getString("Valor"));
            return recurso;
        }

        private Builder() {
        }

    }

    private static final long serialVersionUID = 1L;

    public static BufferedImage getBufferedImage(
            final String valor)
                    throws IOException {
        final byte[] bytes = Base64.decodeBase64(valor);
        try (final InputStream in = new ByteArrayInputStream(bytes)) {
            // ADVERTENCIA: Esto no soporta JPG.
            return ImageIO.read(in);
        }
    }

    public static String getEncodedBytes(
            final InputStream is)
                    throws IOException {
        final byte[] bytes = IOUtils.toByteArray(is);
        return Base64.encodeBase64String(bytes);
    }

    public static List<Recurso> loadFromCsv(
            final InputStream is)
                    throws IOException {
        final List<Recurso> recursos = new ArrayList();
        try (final CSVReader reader = new CSVReader(new InputStreamReader(is, "UTF-8"), ';')) {
            reader.readNext(); // Skip header.
            String[] line = reader.readNext();
            while (line != null) {
                final Recurso recurso = new Recurso();
                // Debe mantenerse el orden los campos.
                recurso.setGrupo(line[0]);
                recurso.setNombre(line[1]);
                recurso.setEditable(line[2]);
                recurso.setTipo(line[3]);
                recurso.setValor(line[4]);
                recurso.setDescripcion(line[5]);
                recursos.add(recurso);
                line = reader.readNext();
            }
        }

        return recursos;
    }

    public static Recurso newInstance(
            final long idPlantilla,
            final String grupo,
            final String nombre,
            final String tipo,
            final String valor) {
        return Builder.newInstance(idPlantilla, grupo, nombre, tipo, valor);
    }

    public static Recurso newInstance(
            final Plantilla plantilla,
            final String grupo,
            final String nombre,
            final String tipo,
            final String valor) {
        return newInstance(plantilla.getId(), grupo, nombre, tipo, valor);
    }

    public static Recurso newInstance(
            final ResultSet rs)
                    throws SQLException {
        return Builder.newInstance(rs);
    }

    private String descripcion;
    private String editable;
    private String grupo;
    private long id;
    private long idPlantilla;
    private String nombre;
    private String tipo;
    private String valor;

    public String getDescripcion() {
        return descripcion;
    }

    public String getEditable() {
        return editable;
    }

    public String getGrupo() {
        return grupo;
    }

    public long getId() {
        return id;
    }

    public long getIdPlantilla() {
        return idPlantilla;
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

    public Object getValorAsObject() {
        if (valor == null) {
            return null;
        }

        if (TipoRecurso.OPCION.equals(tipo)) {
            return Opcion.SI.equals(valor);
        }

        if (TipoRecurso.NUMERO.equals(tipo)) {
            return Integer.parseInt(valor);
        }

        if (TipoRecurso.IMAGEN.equals(tipo)) {
            try {
                return getBufferedImage(valor);
            } catch (Exception ex) {
                return null;
            }
        }

        return valor;
    }

    public void setDescripcion(
            final String descripcion) {
        this.descripcion = descripcion;
    }

    public void setEditable(
            final String editable) {
        this.editable = editable;
    }

    public void setGrupo(
            final String grupo) {
        this.grupo = grupo;
    }

    public void setId(
            final long id) {
        this.id = id;
    }

    public void setIdPlantilla(
            final long idPlantilla) {
        this.idPlantilla = idPlantilla;
    }

    public void setNombre(
            final String nombre) {
        this.nombre = nombre;
    }

    public void setTipo(
            final String tipo) {
        this.tipo = tipo;
    }

    public void setValor(
            final String valor) {
        this.valor = valor;
    }

}
