package cl.bcs.normativa.validacion;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;

import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.esquema.Esquema;
import cl.bcs.normativa.esquema.RangoFechas;
import cl.bcs.normativa.esquema.Validacion;
import cl.bcs.normativa.procesos.ProcesoException;

/**
 * Implementa m&eacute;todos de validaci&oacute;n general de los documentos.
 *
 * @author fpino@email.com
 *
 */
public class Validador {

    /**
     * Validaci&oacute;n de la fecha de un registro.
     *
     * @param esquema
     *            El esquema.
     * @param registro
     *            El registro de datos.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n,
     *         <code>false</code> en caso contrario.
     */
    public static boolean validarFecha(final Esquema esquema, final Registro registro) {
        final String nombreCampoFecha = esquema.getNombreCampoFecha();
        final Object obj = registro.get(nombreCampoFecha);
        if (obj == null) {
            return false;
        }

        Date fecha;
        try {
            fecha = (Date) obj;
        } catch (ClassCastException ex) {
            return false;
        }

        final RangoFechas rangoFechas = esquema.getRangoFechas();
        if (rangoFechas != null) {
            final Date fechaDesde = rangoFechas.getFechaDesde();
            if (fechaDesde != null && fecha.compareTo(fechaDesde) < 0) {
                return false;
            }

            final Date fechaHasta = rangoFechas.getFechaHasta();
            if (fechaHasta != null && fecha.compareTo(fechaHasta) > 0) {
                return false;
            }
        }

        return true;
    }

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
    public Validador(final ContextoValidacion contexto) {
        this.contexto = contexto;
    }

    /**
     * Obtiene el contexto de validaci&oacute;n.
     * 
     * @return El contexto de validaci&oacute;n.
     */
    public ContextoValidacion getContexto() {
        return contexto;
    }

    /**
     * Implementaci&oacute;n del validador gen&eacute;rico.
     *
     * @param validacion
     *            La validaci&oacute;n.
     * @param registro
     *            El registro de datos a validar.
     * @return Una lista de los resultados de aplicar las reglas de
     *         validaci&oacute;n.
     */
    public boolean validar(final Validacion validacion, final Registro registro) {
        final Method method = validacion.getMetodo();
        if (method == null) {
            throw new ProcesoException(
                    "Metodo de validacion no implementado para: " + validacion.getCodigo());
        }

        try {
            return (boolean) method.invoke(this, registro);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new ProcesoException("Error al invocar metodo de validacion", ex);
        }
    }

}
