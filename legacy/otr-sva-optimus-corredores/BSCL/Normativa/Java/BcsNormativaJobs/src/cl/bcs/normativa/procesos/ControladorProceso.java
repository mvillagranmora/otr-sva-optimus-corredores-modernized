package cl.bcs.normativa.procesos;

import java.sql.SQLException;

import cl.bcs.normativa.dal.DocumentoDAO;
import cl.bcs.normativa.esquema.Documento;

/**
 * Controlador del estado de un proceso.
 * <p>
 * El objetivo es que no haya m&aacute;s de un proceso ejecut&aacute;ndose a la
 * vez sobre un documento.
 *
 * @author fpino@email.com
 *
 */
public class ControladorProceso implements AutoCloseable {

    /**
     * Contexto de proceso.
     */
    private final ContextoProceso contexto;

    /**
     * Constructor.
     *
     * @param contexto
     *            El contexto de proceso.
     * @throws ProcesoException
     *             En caso de error de proceso.
     */
    public ControladorProceso(final ContextoProceso contexto) throws ProcesoException {

        final Documento documento = contexto.getDocumento();
        if (documento.isEnProceso()) {
            throw new ProcesoException("El documento esta en proceso");
        }

        this.contexto = contexto;

        try {
            DocumentoDAO.setEnProceso(contexto.getConnection(), contexto.getSchema(), documento,
                    true);
        } catch (SQLException ex) {
            throw new ProcesoException("Error al abrir proceso", ex);
        }
    }

    @Override
    public void close() throws ProcesoException {
        try {
            DocumentoDAO.setEnProceso(contexto.getConnection(), contexto.getSchema(),
                    contexto.getDocumento(), false);
            //contexto.getConnection().commit();
        } catch (SQLException ex) {
            throw new ProcesoException("Error al cerrar proceso ...", ex);
        }
    }

}
