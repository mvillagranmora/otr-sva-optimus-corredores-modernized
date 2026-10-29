package cl.bcs.normativa.dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import cl.bcs.normativa.esquema.ResumenValidacion;
import cl.bcs.sql.generator.SqlGenerator;
import cl.bcs.sql.generator.SqlGeneratorBuilder;
import cl.bcs.sql.metadata.SqlMetadata;
import cl.bcs.sql.metadata.SqlTable;
import cl.bcs.sql.utils.SqlBulk;
import cl.bcs.utils.StringUtils;

/**
 * Inserta masivamente registros ResumenValidacion.
 *
 * @author fpino@email.com
 */
public class ResumenValidacionInserter extends SqlBulk {

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
            .getLogger(ResumenValidacionInserter.class.getName());

    /**
     * Nombres de las columnas de la tabla ResumenValidacion.
     */
    private static final List<String> NOMBRES_COLUMNAS = Arrays.asList(
            new String[] { "ID", "IdProceso", "IdDocumento", "IdUsuario", "CodigoValidacion",
                    "DescripcionValidacion", "CantidadCasos", "FechaValidacion" });

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
        final SqlTable tabla = metadata.getTable(schema, Nombres.NOMBRE_TABLA_RESUMEN_VALIDACION);
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
     * Obtiene nuevo secuenciador para {@link ResumenValidacion}.
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param schema
     *            El schema de la base de datos.
     * @return Nuevo secuenciador para {@link ResumenValidacion}.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static SecuenciadorRegistro getSecuenciador(
            final Connection conn,
            final String schema)
                    throws SQLException {
        return new SecuenciadorRegistro(conn,
                SqlMetadata.newTable(schema, Nombres.NOMBRE_TABLA_RESUMEN_VALIDACION));
    }

    /**
     * Secuenciador de los resumenes de validaci&oacute;n.
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
    public ResumenValidacionInserter(
            final Connection connection,
            final SqlMetadata metadata,
            final String schema) throws SQLException {
        super(connection, armarInsert(metadata, schema), BATCH_SIZE);
        this.secuenciador = getSecuenciador(connection, schema);
    }

    /**
     * Inserta nuevo resumen (modo bulk).
     *
     * @param resumen
     *            El resumen.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public void insert(
            final ResumenValidacion resumen)
                    throws SQLException {
        resumen.setId(secuenciador.getSiguiente());

        final PreparedStatement bulkStatement = getBulkStatement();
        bulkStatement.clearParameters();
        bulkStatement.setObject(1, resumen.getId());
        bulkStatement.setObject(2, resumen.getIdProceso());
        bulkStatement.setObject(3, resumen.getIdDocumento());
        bulkStatement.setObject(4, resumen.getIdUsuario());
        bulkStatement.setObject(5, resumen.getCodigoValidacion());
        bulkStatement.setObject(6, resumen.getDescripcionValidacion());
        bulkStatement.setObject(7, resumen.getCantidadCasos());
        bulkStatement.setObject(8, resumen.getFechaValidacion());

        addBatch();
    }

}
