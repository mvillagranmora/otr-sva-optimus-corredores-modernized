package cl.bcs.normativa.carga;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import cl.bcs.normativa.procesos.ContextoProceso;
import cl.bcs.utils.PropertiesUtil;

/**
 * Contexto de la carga de documentos.
 *
 * @author fpino@email.com
 *
 */
public final class ContextoCarga extends ContextoProceso {

    /**
     * Opciones de carga.
     */
    private final OpcionesCarga opciones;

    /**
     * Ruta del archivo para carga externa.
     */
    private String rutaArchivo;

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
    public ContextoCarga(final Connection conn, final Properties props, final long idProceso, final DataSource ds)
            throws SQLException {
        super(conn, props, idProceso, ds);

        this.opciones = new OpcionesCarga(props);
        if (opciones.getTipoCarga().equals(TipoCarga.EXTERNA)) {
            this.rutaArchivo = PropertiesUtil.getRequiredString(props, "rutaArchivo");
        }
    }

    /**
     * Obtiene las opciones de carga.
     *
     * @return Las opciones de carga.
     */
    public OpcionesCarga getOpciones() {
        return opciones;
    }

    /**
     * Obtiene la ruta del archivo.
     *
     * @return La ruta del archivo.
     */
    public String getRutaArchivo() {
        return rutaArchivo;
    }

}
