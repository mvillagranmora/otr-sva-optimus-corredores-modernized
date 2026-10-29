package cl.bcs.normativa.data;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import cl.bcs.normativa.esquema.Campo;
import cl.bcs.normativa.esquema.Esquema;

/**
 * Implementa mapeos para obtener un registro de datos.
 *
 * @author fpino@email.com
 *
 */
public final class RegistroMapper {

    /**
     * Obtiene registro de datos a partir de {@link ResultSet}.
     *
     * @param esquema
     *            El esquema.
     * @param rs
     *            El {@link ResultSet}.
     * @return Registro de datos a partir de {@link ResultSet}.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static Registro map(final Esquema esquema, final ResultSet rs) throws SQLException {
        final Registro registro = new Registro(esquema);
        final ResultSetMetaData rsmeta = rs.getMetaData();
        for (int i = 1; i <= rsmeta.getColumnCount(); i++) {
            final String key = rsmeta.getColumnLabel(i);
            final Comparable<?> value = (Comparable<?>) rs.getObject(i);
            if (value != null) {
                registro.put(key, value);
            }
        }

        return registro;
    }

    /**
     * Obtiene registro de datos a partir del arreglo de valores.
     *
     * @param esquema
     *            El esquema.
     * @param values
     *            El arreglo de valores.
     * @param parser
     *            El parser de la data.
     * @return Registro de datos a partir del arreglo de valores.
     * @throws DataParserException
     *             En caso de error en el parser de datos.
     */
    public static Registro map(final Esquema esquema, final String[] values,
            final DataParser parser) throws DataParserException {
        final Registro registro = new Registro(esquema);
        for (int i = 0; i < values.length; i++) {
            final Campo campo = esquema.buscarCampoCargaExterna(i + 1);
            if (campo != null) {
                registro.put(campo.getNombre(), parser.parse(campo, values[i]));
            }
        }

        return registro;
    }

    private RegistroMapper() {
    }

}
