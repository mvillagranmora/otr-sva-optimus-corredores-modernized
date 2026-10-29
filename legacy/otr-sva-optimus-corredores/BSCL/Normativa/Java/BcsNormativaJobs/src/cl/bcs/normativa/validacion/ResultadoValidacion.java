package cl.bcs.normativa.validacion;

import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

import cl.bcs.normativa.dal.DetalleValidacionInserter;
import cl.bcs.normativa.dal.ResumenValidacionDAO;
import cl.bcs.normativa.dal.ResumenValidacionInserter;
import cl.bcs.normativa.dal.SecuenciadorRegistro;
import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.esquema.DetalleValidacion;
import cl.bcs.normativa.esquema.ResumenValidacion;
import cl.bcs.normativa.interfaces.CambiarEstadoDJValidacion;
import cl.bcs.normativa.interfaces.CambiarEstadoDJCargar;
import cl.bcs.normativa.procesos.ProcesoException;

/**
 * Resultado de validaci&oacute;n.
 *
 * @author fpino@email.com
 *
 */
public class ResultadoValidacion {

    /**
     * Contenedor de los resumenes de validaci&oacute;n.
     */
    private final Map<String, ResumenValidacion> contenedor;

    /**
     * Contexto de validaci&oacute;n.
     */
    private final ContextoValidacion contexto;

    /**
     * Constructor.
     *
     * @param contexto
     *            El contexto de validaci&oacute;n.
     */
    public ResultadoValidacion(final ContextoValidacion contexto) {
        this.contexto = contexto;
        this.contenedor = new TreeMap<>();
    }

    /**
     * Contabiliza nuevo caso detectado en la validaci&oacute;n.
     *
     * @param codigo
     *            C&oacute;digo de la regla de validaci&oacute;n.
     * @param descripcion
     *            Descripci&oacute;n de la regla de validaci&oacute;n.
     * @param registro
     *            El registro de datos.
     */
    public void contar(final String codigo, final String descripcion, final Registro registro) {
        ResumenValidacion resumen;

        if (contenedor.containsKey(codigo)) {
            resumen = contenedor.get(codigo);
        } else {
            resumen = newResumenValidacion(codigo, descripcion);
            contenedor.put(codigo, resumen);
        }

        resumen.contarCasos();

        final long id = (Long) registro.get(SecuenciadorRegistro.ID_COLUMN_NAME);
        resumen.getIdDetalles().add(id);
    }

    /**
     * Elimina los resumenes de validaci&oacute;n generados en proceso previo.
     *
     * @throws SQLException
     *             En caso de error SQL.
     */
    public void limpiar() throws SQLException {
        ResumenValidacionDAO.eliminarPorIdDocumento(contexto.getConnection(), contexto.getSchema(),
                contexto.getDocumento().getId());
        
    }

    /**
     * Obtiene nueva instancia de {@link ResumenValidacion}.
     *
     * @param codigo
     *            C&oacute;digo de la regla de validaci&oacute;n.
     * @param descripcion
     *            Descripci&oacute;n de la regla de validaci&oacute;n.
     * @return Nueva instancia de {@link ResumenValidacion}.
     */
    private ResumenValidacion newResumenValidacion(final String codigo, final String descripcion) {
        final ResumenValidacion resumen = new ResumenValidacion();
        resumen.setIdProceso(contexto.getIdProceso());
        resumen.setIdDocumento(contexto.getDocumento().getId());
        resumen.setIdUsuario(contexto.getUsuario().getId());
        resumen.setCodigoValidacion(codigo);
        resumen.setDescripcionValidacion(descripcion);
        resumen.setCantidadCasos(0);
        resumen.setFechaValidacion(contexto.getFechaValidacion());
        return resumen;
    }

    /**
     * Registra los resumenes y detalles resultado de la validaci&oacute;n.
     *
     * @throws SQLException
     *             En caso de error SQL.
     */
    public void registrar() throws SQLException {

        if (contenedor.isEmpty()) {
            final String codigo = "OK";
            ResumenValidacion resumen = newResumenValidacion(codigo, "No hay registros invalidos");
            contenedor.put(codigo, resumen);
            //Se actualiza estado de proceso de la DJ
            CambiarEstadoDJValidacion cambiarEstadoDJValidacion = new CambiarEstadoDJValidacion();
            CambiarEstadoDJValidacion.InCambiarEstadoDJ inCambiarEstadoDJ = new CambiarEstadoDJValidacion.InCambiarEstadoDJ();
            inCambiarEstadoDJ.Esquema = contexto.getEsquema().getNombre();
            inCambiarEstadoDJ.Version = contexto.getEsquema().getVersion();
            try {
            	cambiarEstadoDJValidacion.logica(contexto.getConnection(), inCambiarEstadoDJ);
			} catch (Exception e) {
				final String msg = "Error durante proceso de validacion, no es posible actualizar el estado de la DJ a validada";
                throw new ProcesoException(msg, e);
			}
        }else{
				CambiarEstadoDJCargar cambiarEstadoDJCargar = new CambiarEstadoDJCargar();
                CambiarEstadoDJCargar.InCambiarEstadoDJ inCambiarEstadoDJ = new CambiarEstadoDJCargar.InCambiarEstadoDJ();
                inCambiarEstadoDJ.Esquema = contexto.getEsquema().getNombre();
                inCambiarEstadoDJ.Version = contexto.getEsquema().getVersion();
			try {
				cambiarEstadoDJCargar.logica(contexto.getConnection(), inCambiarEstadoDJ);
			} catch (Exception e) {
				final String msg = "Error durante proceso de validacion, no es posible actualizar el estado de la DJ a validada";
                throw new ProcesoException(msg, e);
			}
			
		}

        //contexto.getConnection().setAutoCommit(false);

        final ResumenValidacionInserter resumenInserter = new ResumenValidacionInserter(
                contexto.getConnection(), contexto.getMetadata(), contexto.getSchema());
        for (final ResumenValidacion resumen : contenedor.values()) {
            resumenInserter.insert(resumen);
        }
        resumenInserter.endBatch();

        final DetalleValidacionInserter detalleInserter = new DetalleValidacionInserter(
                contexto.getConnection(), contexto.getMetadata(), contexto.getSchema());
        for (final ResumenValidacion resumen : contenedor.values()) {
            for (final long idDetalle : resumen.getIdDetalles()) {
                final DetalleValidacion detalle = new DetalleValidacion();
                detalle.setIdDetalleDocumento(idDetalle);
                detalle.setIdResumenValidacion(resumen.getId());
                detalleInserter.insert(detalle);
            }
        }
        detalleInserter.endBatch();
    }

}
