package cl.bcs.normativa.carga;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.esquema.Campo;
import cl.bcs.normativa.esquema.Esquema;
import cl.bcs.sql.generator.SqlGenerator;
import cl.bcs.sql.generator.SqlGeneratorBuilder;
import cl.bcs.sql.metadata.SqlTable;
import cl.bcs.sql.utils.SqlBulk;
import cl.bcs.utils.StringUtils;

/**
 * Implementa el borrador de datos de la tabla destino de la carga.
 *
 * @author fpino@email.com
 *
 */
final class Borrador extends SqlBulk {

    /**
     * Instancia del generador SQL.
     */
    private static final SqlGenerator GENERATOR = SqlGeneratorBuilder.getInstance();

    /**
     * Instancia del Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(Borrador.class.getName());

    /**
     * Arma el comando DELETE para borrar los datos.
     *
     * @param esquema
     *            El esquema.
     * @param tabla
     *            La tabla.
     * @return El comando DELETE para borrar los datos.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static String armarDelete(
            final Esquema esquema,
            final SqlTable tabla)
                    throws SQLException {
        final List<String> filtros = new ArrayList<>();
        filtros.add(String.format("%s=?", GENERATOR.getSqlText("IdDocumento")));

        for (final Campo campoClave : esquema.getCamposClaves()) {
            filtros.add(String.format("%s=?", GENERATOR.getSqlText(campoClave.getNombre())));
        }

        final String sql = String.format("DELETE FROM %s WHERE %s", GENERATOR.getSqlText(tabla),
                StringUtils.join(" AND ", filtros));
        LOGGER.info(sql);
        return sql;
    }

    /**
     * Contexto de la carga.
     */
    private final ContextoCarga contexto;

    /**
     * Constructor.
     *
     * @param contexto
     *            El contexto de carga.
     * @param tabla
     *            La tabla.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public Borrador(
            final ContextoCarga contexto,
            final SqlTable tabla) throws SQLException {
        super(contexto.getConnection(), armarDelete(contexto.getEsquema(), tabla),
                contexto.getOpciones().getBatchSize());
        this.contexto = contexto;
    }

    /**
     * Recibe un registro para ser borrado.
     * <p>
     * Potencialmente, puede borrar un lote de registros, pues el borrado
     * est&aacute; implementado en modo bulk.
     *
     * @param registro
     *            El registro de datos a grabar.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public void borrar(
            final Registro registro)
                    throws SQLException {
        final PreparedStatement bulkStatement = getBulkStatement();

        int i = 1;
        bulkStatement.setObject(i++, contexto.getDocumento().getId());
        for (final Campo campoClave : contexto.getEsquema().getCamposClaves()) {
            final Object value = registro.get(campoClave.getNombre());
            bulkStatement.setObject(i++, value);
        }

        addBatch();
    }

}
