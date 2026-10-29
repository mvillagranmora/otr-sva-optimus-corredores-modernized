package cl.bcs.normativa.lectores;

import java.sql.SQLException;
import java.sql.ResultSet;
import cl.bcs.normativa.carga.TipoCarga;
import cl.bcs.normativa.esquema.Campo;
import cl.bcs.normativa.esquema.CampoClasificador;
import cl.bcs.normativa.esquema.Clasificador;
import cl.bcs.normativa.esquema.Documento;
import cl.bcs.normativa.esquema.Esquema;
import cl.bcs.normativa.procesos.ContextoProceso;
import cl.bcs.sql.generator.SqlGenerator;
import cl.bcs.sql.generator.SqlGeneratorBuilder;
import cl.bcs.sql.metadata.SqlTable;
import cl.bcs.sql.statements.SqlOrderByCondition;
import cl.bcs.sql.statements.SqlSelect;

/**
 * Constructores de {@link Lector}.
 *
 * @author fpino@email.com
 *
 */
public final class LectorBuilder {

    /**
     * Instancia del generador SQL.
     */
    private static final SqlGenerator GENERATOR = SqlGeneratorBuilder.getInstance();

    /**
     * Agrega opciones de ordenamiento a la sentencia SELECT, seg&uacute;n la
     * especificaci&oacute;n de campos claves en el esquema.
     *
     * @param select
     *            La sentencia SELECT.
     * @param esquema
     *            El esquema.
     */
    private static void addOrderBy(final SqlSelect select, final Esquema esquema,
            final boolean ordenarPorFecha) {
        if (ordenarPorFecha) {
            final String nombreCampoFecha = esquema.getNombreCampoFecha();
            select.getOrderBy().add(new SqlOrderByCondition(nombreCampoFecha, "ASC"));
        }

        for (final Campo campoClave : esquema.getCamposClaves()) {
            select.getOrderBy().add(new SqlOrderByCondition(campoClave.getNombre(), "ASC"));
        }
    }

    /**
     * Arma el comando SELECT para obtener los detalles del documento.
     *
     * @param contexto
     *            El contexto de proceso.
     * @param tabla
     *            La tabla.
     * @param tipoCarga
     *            El tipo de carga.
     * @param filtrarPorFecha
     *            Indica si filtrar por fecha.
     * @param clasificador
     *            El clasificador.
     * @return El comando SELECT para obtener los detalles del documento.
     * @throws SQLException
     *             En caso de error SQL.
     */
    private static String armarSelectDestino(final ContextoProceso contexto, final SqlTable tabla,
            final TipoCarga tipoCarga, final boolean filtrarPorFecha,
            final Clasificador clasificador, final boolean ordenarPorFecha) throws SQLException {

        final Documento documento = contexto.getDocumento();
        final Esquema esquema = contexto.getEsquema();

        final SqlSelect select = new SqlSelect(tabla);
        select.getWhere().put("IdDocumento", documento.getId());

        if (tipoCarga != null) {
            select.getWhere().put("FuenteDatos", tipoCarga.toString());
        }

        if (clasificador != null) {
            for (final CampoClasificador campoClasificador : clasificador.getCamposClasificador()) {
                select.getWhere().put(campoClasificador.getNombreCampo(),
                        campoClasificador.getValorAsComparable());
            }
        }

        if (filtrarPorFecha) {
            final String nombreCampoFecha = esquema.getNombreCampoFecha();
            select.getWhere().put(nombreCampoFecha, contexto.getFiltroFechas().getFechaDesde(),
                    contexto.getFiltroFechas().getFechaHasta());
        }

        addOrderBy(select, esquema, ordenarPorFecha);

        return GENERATOR.getSqlText(select);
    }

    /**
     * Arma el comando SELECT para leer los datos desde el origen.
     *
     * @param contexto
     *            El contexto de proceso.
     * @param tablaOrigen
     *            La tabla origen.
     * @param clasificador
     *            El clasificador.
     * @return El comando SELECT para leer los datos desde el origen.
     * @throws SQLException
     *             En caso de error SQL.
     */
    private static String armarSelectOrigen(final ContextoProceso contexto,
            final SqlTable tablaOrigen, final Clasificador clasificador) throws SQLException {
        final SqlSelect select = new SqlSelect(tablaOrigen);
        select.getWhere().put("IdEmpresa", contexto.getDocumento().getIdEmpresa());

        final Esquema esquema = contexto.getEsquema();
        final String nombreCampoFecha = esquema.getNombreCampoFecha();
        select.getWhere().put(nombreCampoFecha, contexto.getFiltroFechas().getFechaDesde(),
                contexto.getFiltroFechas().getFechaHasta());

        if (clasificador != null) {
            for (final CampoClasificador campoClasificador : clasificador.getCamposClasificador()) {
                select.getWhere().put(campoClasificador.getNombreCampo(),
                        campoClasificador.getValorAsComparable());
            }
        }

        addOrderBy(select, esquema, true);

        return GENERATOR.getSqlText(select);
    }

    /**
     * Obtiene nueva instancia de {@link Lector} para leer datos del destino.
     *
     * @param contexto
     *            El contexto de proceso.
     * @param tablaDestino
     *            La tabla destino.
     * @param tipoCarga
     *            El tipo de carga.
     * @param filtrarPorFecha
     *            Indica si filtrar por fecha.
     * @param clasificador
     *            El clasificador.
     * @return Nueva instancia de {@link Lector} para leer datos del destino.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static LectorSQL newLectorDestino(final ContextoProceso contexto,
            final SqlTable tablaDestino, final TipoCarga tipoCarga, final boolean filtrarPorFecha,
            final Clasificador clasificador, final boolean ordenarPorFecha) throws SQLException {
        return new LectorSQL(contexto, armarSelectDestino(contexto, tablaDestino, tipoCarga,
                filtrarPorFecha, clasificador, ordenarPorFecha));
    }
    
    public static LectorSP newLectorServicio(final ContextoProceso contexto,
            final String sql,ResultSet resul ) throws SQLException {
        return new LectorSP(contexto,sql,resul);
    }

    /**
     * Obtiene nueva instancia de {@link Lector} para leer datos del origen.
     *
     * @param contexto
     *            El contexto de proceso.
     * @param tablaOrigen
     *            La tabla origen.
     * @param clasificador
     *            El clasificador.
     * @return Nueva instancia de {@link Lector} para leer datos del origen.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static LectorSQL newLectorOrigen(final ContextoProceso contexto,
            final SqlTable tablaOrigen, final Clasificador clasificador) throws SQLException {

        return new LectorSQL(contexto, armarSelectOrigen(contexto, tablaOrigen, clasificador));
    }

    private LectorBuilder() {
    }
}
