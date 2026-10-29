package cl.bcs.normativa.exportacion;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import cl.bcs.normativa.procesos.ContextoProceso;

/**
 * Contexto del exportador.
 *
 * @author fpino@email.com
 *
 */
public class ContextoExportacion extends ContextoProceso {

    /**
     * Opciones de exportaci&oacute;n.
     */
    private final OpcionesExportacion opciones;

    /**
     * Constructor.
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param props
     *            Las {@link Properties}.
     * @param idProceso
     *            El id de proceso.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public ContextoExportacion(final Connection conn, final Properties props, final long idProceso, final DataSource ds)
            throws SQLException {
        super(conn, props, idProceso, ds);
        this.opciones = new OpcionesExportacion(props);
    }

    /**
     * Obtiene las opciones de exportaci&oacute;n.
     * 
     * @return Las opciones de exportaci&oacute;n.
     */
    public OpcionesExportacion getOpciones() {
        return opciones;
    }

}
