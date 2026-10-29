package cl.bcs.sql.generator;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import cl.bcs.sql.metadata.SqlMetadata;

/**
 * Factory para obtener instancia de {@link SqlGenerator}.
 *
 * @author fpino@email.com
 *
 */
public final class SqlGeneratorBuilder {

    private static final Map<String, SqlGenerator> GENERATORS = new ConcurrentHashMap<String, SqlGenerator>();
    private static final String SAP_HANA = "SAP HANA";
    private static final String SQL_SERVER = "SQL SERVER";

    /**
     * Obtiene instancia del generador por defecto (SAP Hana) de sentencias SQL.
     * <p>
     *
     * @return Instancia del generador de sentencias SQL.
     */
    public static SqlGenerator getInstance() {
        return getInstance(SAP_HANA);
    }

    public static SqlGenerator getInstance(
            final DataSource dataSource)
                    throws SQLException {
        if (SqlMetadata.isSqlServer(dataSource)) {
            return getInstance(SQL_SERVER);
        }

        return getInstance(SAP_HANA);
    }

    /**
     * Obtiene instancia del generador de sentencias SQL, seg&uacute;n el tipo
     * solicitado.
     *
     * @param generatorType
     *            Tipo de generador solicitado.
     * @return Instancia del generador de sentencias SQL.
     */
    private static SqlGenerator getInstance(
            final String generatorType) {
        final String type = generatorType.toUpperCase();
        if (!GENERATORS.containsKey(type)) {
            final SqlGenerator generator = newGenerator(type);
            GENERATORS.put(type, generator);
        }

        return GENERATORS.get(type);
    }

    private static SqlGenerator newGenerator(
            final String type) {
        if (SQL_SERVER.equals(type)) {
            return new SqlServerGenerator();
        }

        return new SapHanaGenerator();
    }

    private SqlGeneratorBuilder() {
    }

}
