package cl.bcs.normativa.cuadratura;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cl.bcs.normativa.carga.TipoCarga;
import cl.bcs.normativa.dal.SecuenciadorRegistro;
import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.lectores.LectorBuilder;
import cl.bcs.normativa.lectores.LectorSQL;
import cl.bcs.sql.metadata.SqlTable;
import cl.bcs.utils.DateUtil;

/**
 * Implementaci&oacute;n de la cuadratura individual de datos de un documento.
 *
 * @author fpino@email.com
 *
 */
public class ProcesoCuadraturaIndividual extends ProcesoCuadratura {

    /**
     * Constructor.
     *
     * @param contexto
     *            El contexto de cuadratura.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public ProcesoCuadraturaIndividual(final ContextoCuadratura contexto) throws SQLException {
        super(contexto);
    }

    @Override
    protected int compare(final Registro registro0, final Registro registro1) {
        final List<String> nombres = new ArrayList<>();
        final String nombreCampoFecha = registro0.getEsquema().getNombreCampoFecha();
        nombres.add(nombreCampoFecha);
        for (final String nombre : registro0.getEsquema().getNombresCamposClave()) {
            nombres.add(nombre);
        }

        return compare(nombres, registro0, registro1);
    }

    @Override
    protected Date getAnoMes(final Registro registro) {
        final String nombreCampoFecha = getContexto().getEsquema().getNombreCampoFecha();
        final Date fecha = (Date) registro.get(nombreCampoFecha);
        return DateUtil.getStartOfMonth(fecha);
    }

    @Override
    protected List<Long> getIdDetalles(final Registro registroDestino) {
        final long id = (Long) registroDestino.get(SecuenciadorRegistro.ID_COLUMN_NAME);
        final List<Long> list = new ArrayList<>();
        list.add(id);
        return list;
    }

    @Override
    protected LectorSQL getLectorDestino() throws SQLException {
        final ContextoCuadratura contexto = getContexto();
        final SqlTable tablaDestino = contexto.getMetadata().getTable(contexto.getSchema(),
                contexto.getEsquema().getNombreTablaDestino());
        return LectorBuilder.newLectorDestino(contexto, tablaDestino, TipoCarga.INICIAL, false,
                contexto.getClasificadorCuadratura(), true);
    }

    @Override
    protected LectorSQL getLectorOrigen() throws SQLException {
        final ContextoCuadratura contexto = getContexto();
        final SqlTable tablaOrigen = contexto.getMetadata().getTable(contexto.getSchema(),
                contexto.getEsquema().getNombreTablaOrigen());
        return LectorBuilder.newLectorOrigen(contexto, tablaOrigen,
                contexto.getClasificadorCuadratura());
    }

}
