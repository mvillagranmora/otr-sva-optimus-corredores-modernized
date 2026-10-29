package cl.bcs.sql.metadata;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

/**
 * M&eacute;todos utiles para acceder a la metadata de la base de datos.
 *
 * @author fpino@email.com
 *
 */
public final class SqlMetadata {

    private static class Keyword {

        private static final String ASC_OR_DESC = "ASC_OR_DESC";
        private static final String COLUMN_NAME = "COLUMN_NAME";
        private static final String COLUMN_SIZE = "COLUMN_SIZE";
        private static final String DECIMAL_DIGITS = "DECIMAL_DIGITS";
        private static final String DEFAULT_VALUE = "COLUMN_DEF";
        private static final String INDEX_NAME = "INDEX_NAME";
        private static final String KEY_SEQ = "KEY_SEQ";
        private static final String NON_UNIQUE = "NON_UNIQUE";
        private static final String NULLABLE = "NULLABLE";
        private static final String ORDINAL_POSITION = "ORDINAL_POSITION";
        private static final String PK_NAME = "PK_NAME";
        private static final String REMARKS = "REMARKS";
        private static final String TABLE = "TABLE";
        private static final String TABLE_CATALOG = "TABLE_CAT";
        private static final String TABLE_NAME = "TABLE_NAME";
        private static final String TABLE_SCHEMA = "TABLE_SCHEM";
        private static final String TABLE_TYPE = "TABLE_TYPE";
        private static final String TYPE_NAME = "TYPE_NAME";
        private static final String VIEW = "VIEW";
    }

    public static String getDriverName(
            final Connection conn)
                    throws SQLException {
        return conn.getMetaData().getDriverName();
    }

    public static String getDriverName(
            final DataSource dataSource)
                    throws SQLException {
        try (final Connection conn = dataSource.getConnection()) {
            return getDriverName(conn);
        }
    }

    public static boolean isSapHana(
            final DataSource dataSource)
                    throws SQLException {
        // Nombre del driver proporcionado por la Metada JDBC es: "HDB".
        final String driverName = SqlMetadata.getDriverName(dataSource).toUpperCase();
        return driverName.contains("HDB");
    }

    public static boolean isSqlServer(
            final DataSource dataSource)
                    throws SQLException {
        // Nombre del driver proporcionado por la Metada JDBC es:
        // "Microsoft JDBC Driver 4.0 for SQL Server".
        final String driverName = SqlMetadata.getDriverName(dataSource).toUpperCase();
        return driverName.contains("SQL SERVER");
    }

    public static SqlTable newTable(
            final String name) {
        return newTable(null, name);
    }

    /**
     * Obtiene nueva instancia de tabla.
     *
     * @param schema
     *            El schema de la base de datos.
     * @param tableName
     *            El nombre de la tabla.
     * @return Nueva instancia de tabla.
     */
    public static SqlTable newTable(
            final String schema,
            final String tableName) {
        final SqlTable table = new SqlTable(tableName);
        table.setSchema(schema);
        return table;
    }

    private final DatabaseMetaData meta;

    /**
     * Constructor a partir de la conexi&oacute;n.
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public SqlMetadata(
            final Connection conn) throws SQLException {
        meta = conn.getMetaData();
        if (meta == null) {
            throw new SqlMetadataException("Metadata no soportada");
        }
    }

    /**
     * Obtiene la lista de catalogos.
     *
     * @return La lista de catalogos.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public List<String> getCatalogs()
            throws SQLException {
        final List<String> list = new ArrayList<>();

        try (final ResultSet rs = meta.getCatalogs()) {
            while (rs.next()) {
                final String catalog = rs.getString(Keyword.TABLE_CATALOG);
                list.add(catalog);
            }
        }

        return list;
    }

    /**
     * Obtiene una lista de columnas, seg&uacute;n los par&aacute;metros
     * indicados.
     * <p>
     * Los par&aacute;metros de filtro se pasan tal cual, seg&uacute;n se
     * espec&iacute;fica en
     * {@link DatabaseMetaData#getColumns(String, String, String, String)}.
     *
     * @param catalog
     *            El nombre del cat&aacute;logo.
     * @param schemaPattern
     *            El patr&oacute;n del schema de la base de datos.
     * @param tableNamePattern
     *            El patr&oacute;n del nombre de la tabla.
     * @param columnNamePattern
     *            El patr&oacute;n del nombre de la columna.
     * @return Una lista de columnas, seg&uacute;n los par&aacute;metros
     *         indicados.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public List<SqlColumn> getColumns(
            final String catalog,
            final String schemaPattern,
            final String tableNamePattern,
            final String columnNamePattern)
                    throws SQLException {
        final List<SqlColumn> list = new ArrayList<>();

        try (final ResultSet rs = meta.getColumns(catalog, schemaPattern, tableNamePattern,
                columnNamePattern)) {
            while (rs.next()) {
                final SqlColumn column = mapAsColumn(rs);
                list.add(column);
            }
        }

        return list;
    }

    /**
     * Obtiene la lista de &iacute;ndices de la tabla.
     *
     * @param catalog
     *            El nombre del cat&aacute;logo.
     * @param schema
     *            El schema de la base de datos.
     * @param tableName
     *            El nombre de la tabla.
     * @return La lista de &iacute;ndices de la tabla.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public List<SqlIndex> getIndexes(
            final String catalog,
            final String schema,
            final String tableName)
                    throws SQLException {
        final List<SqlIndex> list = new ArrayList<>();
        String previousIndexName = null;
        SqlIndex index = null;

        try (ResultSet rs = meta.getIndexInfo(catalog, schema, tableName, true, true)) {
            while (rs.next()) {
                final String indexName = rs.getString(Keyword.INDEX_NAME);
                if (previousIndexName == null || !indexName.equals(previousIndexName)) {
                    index = new SqlIndex();
                    index.setName(indexName);
                    index.setNonUnique(rs.getString(Keyword.NON_UNIQUE));
                    index.setColumns(new ArrayList<SqlIndexColumn>());
                    list.add(index);

                    previousIndexName = indexName;
                }

                final SqlIndexColumn indexColumn = mapAsIndexColumn(rs);
                index.getColumns().add(indexColumn);
            }
        }

        return list;
    }

    /**
     * Obtiene la llave primaria de una tabla.
     *
     * @param catalog
     *            El nombre del cat&aacute;logo.
     * @param schema
     *            El schema de la base de datos.
     * @param tableName
     *            El nombre de la tabla.
     * @return La llave primaria de una tabla.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public SqlIndex getPrimaryKey(
            final String catalog,
            final String schema,
            final String tableName)
                    throws SQLException {
        SqlIndex pk = null;

        try (final ResultSet rs = meta.getPrimaryKeys(catalog, schema, tableName)) {
            while (rs.next()) {
                final String columnName = rs.getString(Keyword.COLUMN_NAME);
                final SqlIndexColumn pkColumn = new SqlIndexColumn();
                pkColumn.setName(columnName);
                pkColumn.setOrdinalPosition(rs.getShort(Keyword.KEY_SEQ));
                if (pk == null) {
                    pk = new SqlIndex();
                    pk.setName(rs.getString(Keyword.PK_NAME));
                    pk.setColumns(new ArrayList<SqlIndexColumn>());
                }
                pk.getColumns().add(pkColumn);
            }
        }

        return pk;
    }

    /**
     * Obtiene la lista de esquemas de la base de datos.
     *
     * @return La lista de esquemas de la base de datos.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public List<String> getSchemas()
            throws SQLException {
        final List<String> list = new ArrayList<>();

        try (final ResultSet rs = meta.getSchemas()) {
            while (rs.next()) {
                final String catalog = rs.getString(Keyword.TABLE_SCHEMA);
                list.add(catalog);
            }
        }

        return list;
    }

    /**
     * Obtiene la metadata de una tabla.
     *
     * @param schema
     *            El schema de la base de datos.
     * @param tableName
     *            El nombre de la tabla.
     * @return La metadata de una tabla.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public SqlTable getTable(
            final String schema,
            final String tableName)
                    throws SQLException {
        final SqlTable table = getTable(null, schema, tableName);
        if (table == null) {
            final String msg = String.format("No existe tabla \"%s\".\"%s\"", schema, tableName);
            throw new SqlMetadataException(msg);
        }

        return table;
    }

    /**
     * Obtiene la metadata de una tabla.
     *
     * @param catalog
     *            El nombre del cat&aacute;logo.
     * @param schema
     *            El schema de la base de datos.
     * @param tableName
     *            El nombre de la tabla.
     * @return La metadata de una tabla.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public SqlTable getTable(
            final String catalog,
            final String schema,
            final String tableName)
                    throws SQLException {
        try (final ResultSet rs = meta.getTables(catalog, schema, tableName,
                new String[] { Keyword.TABLE, Keyword.VIEW })) {
            if (rs.next()) {
                final SqlTable table = mapAsTable(rs);
                table.setColumns(getColumns(catalog, table.getSchema(), table.getName(), null));
                table.setIndexes(getIndexes(catalog, table.getSchema(), table.getName()));
                table.setPrimaryKey(getPrimaryKey(catalog, table.getSchema(), table.getName()));
                return table;
            }
        }

        return null;
    }

    /**
     * Obtiene una lista de tablas, seg&uacute;n los par&aacute;metros.
     *
     * @param catalog
     *            El nombre del cat&aacute;logo.
     * @param schemaPattern
     *            El patr&oacute;n del schema de la base de datos.
     * @param tableNamePattern
     *            El patr&oacute;n del nombre de la tabla.
     * @return Una lista de tablas, seg&uacute;n los par&aacute;metros.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public List<SqlTable> getTables(
            final String catalog,
            final String schemaPattern,
            final String tableNamePattern)
                    throws SQLException {
        final List<SqlTable> list = new ArrayList<>();

        try (final ResultSet rs = meta.getTables(catalog, schemaPattern, tableNamePattern,
                new String[] { Keyword.TABLE, Keyword.VIEW })) {
            while (rs.next()) {
                final SqlTable table = mapAsTable(rs);
                table.setColumns(getColumns(catalog, table.getSchema(), table.getName(), null));
                table.setIndexes(getIndexes(catalog, table.getSchema(), table.getName()));
                table.setPrimaryKey(getPrimaryKey(catalog, table.getSchema(), table.getName()));
                list.add(table);
            }
        }

        return list;
    }

    /**
     * Convierte {@link ResultSet} a {@link SqlColumn}.
     *
     * @param rs
     *            El {@link ResultSet}.
     * @return {@link SqlColumn} a partir del {@link ResultSet}.
     * @throws SQLException
     *             En caso de error SQL.
     */
    private SqlColumn mapAsColumn(
            final ResultSet rs)
                    throws SQLException {
        final SqlColumn column = new SqlColumn();
        column.setCatalog(rs.getString(Keyword.TABLE_CATALOG));
        column.setSchema(rs.getString(Keyword.TABLE_SCHEMA));
        column.setTableName(rs.getString(Keyword.TABLE_NAME));
        column.setName(rs.getString(Keyword.COLUMN_NAME));
        column.setTypeName(rs.getString(Keyword.TYPE_NAME));
        column.setSize(rs.getInt(Keyword.COLUMN_SIZE));
        column.setScale(rs.getShort(Keyword.DECIMAL_DIGITS));
        column.setNullable(rs.getBoolean(Keyword.NULLABLE));
        column.setRemarks(rs.getString(Keyword.REMARKS));
        column.setDefaultValue(rs.getString(Keyword.DEFAULT_VALUE));
        column.setOrdinalPosition(rs.getShort(Keyword.ORDINAL_POSITION));
        return column;
    }

    /**
     * Convierte {@link ResultSet} a {@link SqlIndexColumn}.
     *
     * @param rs
     *            El {@link ResultSet}.
     * @return {@link SqlIndexColumn} a partir del {@link ResultSet}.
     * @throws SQLException
     *             En caso de error SQL.
     */
    private SqlIndexColumn mapAsIndexColumn(
            final ResultSet rs)
                    throws SQLException {
        final SqlIndexColumn indexColumn = new SqlIndexColumn();
        indexColumn.setName(rs.getString(Keyword.COLUMN_NAME));
        indexColumn.setOrdinalPosition(rs.getShort(Keyword.ORDINAL_POSITION));
        indexColumn.setOrdering(rs.getString(Keyword.ASC_OR_DESC));
        return indexColumn;
    }

    /**
     * Convierte {@link ResultSet} a {@link SqlTable}.
     *
     * @param rs
     *            El {@link ResultSet}.
     * @return {@link SqlTable} a partir del {@link ResultSet}.
     * @throws SQLException
     *             En caso de error SQL.
     */
    private SqlTable mapAsTable(
            final ResultSet rs)
                    throws SQLException {
        final SqlTable table = new SqlTable(rs.getString(Keyword.TABLE_NAME));
        table.setCatalog(rs.getString(Keyword.TABLE_CATALOG));
        table.setSchema(rs.getString(Keyword.TABLE_SCHEMA));
        table.setType(rs.getString(Keyword.TABLE_TYPE));
        table.setRemarks(rs.getString(Keyword.REMARKS));
        return table;
    }
}
