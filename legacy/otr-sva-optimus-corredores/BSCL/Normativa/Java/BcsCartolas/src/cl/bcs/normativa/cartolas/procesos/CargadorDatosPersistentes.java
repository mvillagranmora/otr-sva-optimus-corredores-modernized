package cl.bcs.normativa.cartolas.procesos;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import cl.bcs.normativa.cartolas.entidades.Cartola;
import cl.bcs.normativa.cartolas.entidades.Datos;
import cl.bcs.normativa.cartolas.entidades.FormatoDatos;

public class CargadorDatosPersistentes extends CargadorDatos {

    /**
     * Instancia del Logger.
     */
    private static final Logger LOGGER = Logger
            .getLogger(CargadorDatosPersistentes.class.getName());

    private final ContextoCartolas contexto;
    private final GrabadorDatosBulk grabador;

    public CargadorDatosPersistentes(
            final ContextoCartolas contexto,
            final GrabadorDatosBulk grabador) {
        this.contexto = contexto;
        this.grabador = grabador;
    }

    @Override
    public void load(
            final String nombre,
            final ResultSet resultSet)
                    throws SQLException,
                    IOException {
        final Map<Long, List<String[]>> map = buildMap(resultSet);
        for (final Long idPortafolio : map.keySet()) {
            final Cartola cartola = Cartola.buscarCartolaPorIdPortafolio(contexto.getCartolas(),
                    idPortafolio);
            final List<String[]> list = map.get(idPortafolio);
            final String valor = serialize(list);
            final Datos datos = Datos.newInstance(cartola, nombre, FormatoDatos.CSV, valor);
            LOGGER.info(datos.toString());
            grabador.grabar(datos);
        }
    }

}
