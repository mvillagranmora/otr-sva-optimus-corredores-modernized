package cl.bcs.valo2.data.consultas;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import cl.bcs.sql.generator.SqlGenerator;
import cl.bcs.sql.generator.SqlGeneratorBuilder;
import cl.bcs.valo2.data.ProveedorDatos;
import cl.bcs.valo2.repositorios.RepositorioException;

public abstract class Consulta<K, T> implements ProveedorDatos<K, T> {
    protected final DataSource dataSource;
    protected final SqlGenerator sqlGenerator;

    public Consulta(
            final DataSource dataSource) throws SQLException {
        this.dataSource = dataSource;
        this.sqlGenerator = SqlGeneratorBuilder.getInstance(dataSource);
    }

    public abstract T buscar(
            final Connection conn,
            final K clave)
                    throws SQLException;

    public T buscar(
            final DataSource dataSource,
            final K clave)
                    throws SQLException {
        try (final Connection conn = dataSource.getConnection()) {
            return buscar(conn, clave);
        }
    }

    @Override
    public T buscar(
            final K clave) {
        try {
            return buscar(dataSource, clave);
        } catch (final SQLException ex) {
            throw new RepositorioException("Error al buscar: " + clave, ex);
        }
    }

}
