package cl.bcs.normativa.dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import cl.bcs.sql.generator.SqlGenerator;
import cl.bcs.sql.generator.SqlGeneratorBuilder;
import cl.bcs.sql.metadata.SqlTable;

/**
 * Secuenciador para generar los id's de los registros de una tabla.
 *
 * @author fpino@email.com
 *
 */
public final class SecuenciadorRegistro {

    /**
     * Instancia del generador SQL.
     */
    private static final SqlGenerator GENERATOR = SqlGeneratorBuilder.getInstance();

    /**
     * Nombre de la columna Id de una tabla.
     */
    public static final String ID_COLUMN_NAME = "ID";

    /**
     * Instancia del Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(SecuenciadorRegistro.class.getName());

    /**
     * Generador de las secuencias.
     */
    private final AtomicLong secuencia;

    /**
     * Constructor dada una tabla.
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param table
     *            La tabla.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public SecuenciadorRegistro(final Connection conn, final SqlTable table) throws SQLException {
        this.secuencia = getAtomicLong(conn, table);
    }

    /**
     * Obtiene {@link AtomicLong} inicializado a partir del m&aacute;ximo id
     * registrado en la tabla.
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param table
     *            La tabla.
     * @return {@link AtomicLong} inicializado a partir del m&aacute;ximo id
     *         registrado en la tabla.
     * @throws SQLException
     *             En caso de error SQL.
     */
    private AtomicLong getAtomicLong(final Connection conn, final SqlTable table)
            throws SQLException {
        final String sql = String.format("SELECT MAX(%s) FROM %s",
                GENERATOR.getSqlText(ID_COLUMN_NAME), GENERATOR.getSqlText(table));
        LOGGER.info(sql);

        try (final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (final ResultSet rs = pstmt.executeQuery()) {
                long inicial = 0l;
                if (rs.next()) {
                    inicial = rs.getLong(1);
                }

                return new AtomicLong(inicial);
            }
        }
    }

    /**
     * Obtiene el siguiente n&uacute;mero de la secuencia.
     *
     * @return El siguiente n&uacute;mero de la secuencia.
     */
    public long getSiguiente() {
        return secuencia.addAndGet(1);
    }

}
