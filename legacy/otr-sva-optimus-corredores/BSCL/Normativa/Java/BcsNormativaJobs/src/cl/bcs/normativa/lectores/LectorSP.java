package cl.bcs.normativa.lectores;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.data.RegistroMapper;
import cl.bcs.normativa.procesos.ContextoProceso;

/**
 * Implementa lector de datos a partir de resulset.
 *
 *
 */
public final class LectorSP extends Lector {

    /**
     * Instancia del Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(LectorSP.class.getName());

    /**
     * {@link ResulSet} resultado de la lectura.
     */
    private final ResultSet resultSet;

    /**
     * Sentencia para obtener los datos.
     */
    private final PreparedStatement statement;

    /**
     * Constructor.
     *
     * @param contexto
     *            El contexto de proceso.
     * @param sql
     *            Comando SELECT.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public LectorSP(final ContextoProceso contexto, final String sql,ResultSet resul ) throws SQLException {
        super(contexto);

        LOGGER.info(sql);
        this.statement = contexto.getConnection().prepareStatement(sql);
        this.resultSet = resul;
    }
    

    @Override
    public void close() throws SQLException {
        statement.close();
    }

    @Override
    public Registro siguiente() throws SQLException {
        if (resultSet.next()) {
            contarRegistrosLeidos();
            final Registro registro = RegistroMapper.map(getContexto().getEsquema(), resultSet);
            registro.setValido(true);
            return registro;
        }

        return null;
    }

}
