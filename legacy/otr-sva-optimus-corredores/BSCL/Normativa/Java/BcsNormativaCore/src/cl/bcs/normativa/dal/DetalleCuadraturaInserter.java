package cl.bcs.normativa.dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import cl.bcs.normativa.esquema.DetalleCuadratura;
import cl.bcs.sql.generator.SqlGenerator;
import cl.bcs.sql.generator.SqlGeneratorBuilder;
import cl.bcs.sql.metadata.SqlMetadata;
import cl.bcs.sql.metadata.SqlTable;
import cl.bcs.sql.utils.SqlBulk;
import cl.bcs.utils.StringUtils;

/**
 * Inserta masivamente registros DetalleCuadratura.
 *
 * @author fpino@email.com
 */
public class DetalleCuadraturaInserter extends SqlBulk {

    /**
     * Tama&ntilde;o del lote batch.
     */
    private final static int BATCH_SIZE = 1000;

    /**
     * Instancia del generador SQL.
     */
    private static final SqlGenerator GENERATOR = SqlGeneratorBuilder.getInstance();

    /**
     * Instancia del Logger.
     */
    private static final Logger LOGGER = Logger
            .getLogger(DetalleCuadraturaInserter.class.getName());

    /**
     * Nombres de las columnas de la tabla DetalleCuadratura.
     */
    private static final List<String> NOMBRES_COLUMNAS = Arrays
            .asList(new String[] { "ID", "IdResumenCuadratura", "IdDetalleDocumento" });

    /**
     * Arma el comando INSERT.
     *
     * @param metadata
     *            La metadata.
     * @param schema
     *            El schema de la base de datos.
     * @return El comando INSERT.
     * @throws SQLException
     *             En caso de error SQL.
     */
    private static String armarInsert(
            final SqlMetadata metadata,
            final String schema)
                    throws SQLException {
        final SqlTable tabla = metadata.getTable(schema, Nombres.NOMBRE_TABLA_DETALLE_CUADRATURA);
        final List<String> names = new ArrayList<>();
        final List<String> values = new ArrayList<>();

        for (final String nombre : NOMBRES_COLUMNAS) {
            names.add(GENERATOR.getSqlText(nombre));
            values.add("?");
        }

        final String sql = String.format("INSERT INTO %s(%s) VALUES(%s)",
                GENERATOR.getSqlText(tabla), StringUtils.join(",", names),
                StringUtils.join(",", values));
        LOGGER.info(sql);
        return sql;
    }

    /**
     * Obtiene nuevo secuenciador para {@link DetalleCuadratura}.
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param schema
     *            El schema de la base de datos.
     * @return Nuevo secuenciador para {@link DetalleCuadratura}.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static SecuenciadorRegistro getSecuenciador(
            final Connection conn,
            final String schema)
                    throws SQLException {
        return new SecuenciadorRegistro(conn,
                SqlMetadata.newTable(schema, Nombres.NOMBRE_TABLA_DETALLE_CUADRATURA));
    }

    /**
     * Secuenciador de los detalles de cuadratura.
     */
    private final SecuenciadorRegistro secuenciador;

    /**
     * Constructor.
     *
     * @param connection
     *            La conexi&oacute;n a la base de datos.
     * @param metadata
     *            La metadata.
     * @param schema
     *            El schema de la base de datos.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public DetalleCuadraturaInserter(
            final Connection connection,
            final SqlMetadata metadata,
            final String schema) throws SQLException {
        super(connection, armarInsert(metadata, schema), BATCH_SIZE);
        this.secuenciador = getSecuenciador(connection, schema);
    }

    /**
     * Inserta nuevo detalle (modo bulk).
     *
     * @param detalle
     *            El detalle.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public void insert(
            final DetalleCuadratura detalle)
                    throws SQLException {
        detalle.setId(secuenciador.getSiguiente());

        final PreparedStatement bulkStatement = getBulkStatement();
        bulkStatement.clearParameters();
        bulkStatement.setObject(1, detalle.getId());
        bulkStatement.setObject(2, detalle.getIdResumenCuadratura());
        bulkStatement.setObject(3, detalle.getIdDetalleDocumento());

        addBatch();
    }

}
