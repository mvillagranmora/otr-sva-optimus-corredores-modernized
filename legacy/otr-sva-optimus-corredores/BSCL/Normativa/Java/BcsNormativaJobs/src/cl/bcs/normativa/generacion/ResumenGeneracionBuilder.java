package cl.bcs.normativa.generacion;

import java.sql.SQLException;
import java.util.Date;

import org.apache.commons.io.FilenameUtils;

import cl.bcs.normativa.dal.ResumenGeneracionDAO;
import cl.bcs.normativa.esquema.Documento;
import cl.bcs.normativa.esquema.ResumenGeneracion;
import cl.bcs.normativa.procesos.ContextoProceso;

/**
 * M&eacute;todos convenientes para crear y actualizar {@link ResumenGeneracion}
 * .
 *
 * @author fpino@email.com
 *
 */
public class ResumenGeneracionBuilder {

    /**
     * Actualiza el resumen de la generaci&oacute;n.
     *
     * @param contexto
     *            El contexto.
     * @param resumen
     *            El resumen.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static void actualizarResumen(final ContextoProceso contexto,
            final ResumenGeneracion resumen) throws SQLException {
        contexto.getConnection().setAutoCommit(true);
        ResumenGeneracionDAO.actualizar(contexto.getConnection(), contexto.getSchema(), resumen);
    }

    /**
     * Crea resumen.
     *
     * @param contexto
     *            El contexto.
     * @param rutaArchivoGenerado
     *            La ruta del archivo generado.
     * @return Nueva instancia de {@link ResumenGeneracion}.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static ResumenGeneracion crearResumen( ContextoProceso contexto,
             String rutaArchivoGenerado,  int numeroPresentacion) throws SQLException {
         ResumenGeneracion resumen = newResumenGeneracion(contexto, rutaArchivoGenerado, numeroPresentacion);
        contexto.getConnection().setAutoCommit(true);
        ResumenGeneracionDAO.insertar(contexto.getConnection(), contexto.getSchema(), resumen);
        return resumen;
    }

    /**
     * Obtiene nueva instancia de {@link ResumenGeneracion}.
     *
     * @param contexto
     *            El contexto.
     * @param rutaArchivoGenerado
     *            La ruta del archivo generado.
     * @return Una nueva instancia de {@link ResumenGeneracion}.
     */
    private static ResumenGeneracion newResumenGeneracion(final ContextoProceso contexto,
            final String rutaArchivoGenerado, final int numeroPresentacion) {
        final Documento documento = contexto.getDocumento();
        final ResumenGeneracion resumen = new ResumenGeneracion();
        resumen.setFecha(new Date());
        resumen.setIdProceso(contexto.getIdProceso());
        resumen.setIdDocumento(documento.getId());
        resumen.setIdUsuario(contexto.getUsuario().getId());
        resumen.setGlosa("Iniciada");

        final String nombreArchivo = FilenameUtils.getName(rutaArchivoGenerado);
        resumen.setNombreArchivo(nombreArchivo);
        resumen.setUltimoCorrelativo(numeroPresentacion);
        return resumen;
    }

    private ResumenGeneracionBuilder() {
    }

}
