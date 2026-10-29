package cl.bcs.normativa.validacion;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.esquema.Validacion;
import cl.bcs.normativa.procesos.ProcesoException;

/**
 * Constructores asociados a {@link Validador}.
 *
 * @author fpino@email.com
 *
 */
public final class ValidadorBuilder {

    /**
     * Obtiene una instancia de la clase de validaci&oacute;n asociada al nombre
     * del esquema.
     *
     * @param nombreEsquema
     *            El nombre del esquema.
     * @return Una instancia de la clase de validaci&oacute;n asociada al
     *         esquema.
     */
    private static Class<?> getClaseValidacion(final String nombreEsquema) {
        try {
            final String className = Validador.class.getCanonicalName() + nombreEsquema;
            return Class.forName(className);
        } catch (ClassNotFoundException ex) {
            throw new ProcesoException("Validador no encontrado para: " + nombreEsquema, ex);
        }
    }

    /**
     * Obtiene el m&eacute;todo para la validaci&oacute;n.
     *
     * @param validacion
     *            La validaci&oacute;n.
     * @return El m&eacute;todo para la validaci&oacute;n.
     */
    public static Method getMetodoValidacion(final Validacion validacion) {
        try {
            final Class<?> clazz = getClaseValidacion(validacion.getNombreEsquema());
            final String name = "validar" + validacion.getCodigo();

            return clazz.getMethod(name, new Class[] { Registro.class });
        } catch (NoSuchMethodException | SecurityException ex) {
            return null;
        }
    }

    /**
     * Obtiene nueva instancia de {@link Validador} relacionada al esquema.
     *
     * @param contexto
     *            El contexto de validaci&oacute;n.
     * @return Una nueva instancia de {@link Validador} relacionada al esquema.
     */
    public static Validador newInstance(final ContextoValidacion contexto) {
        try {
            final Class<?> clazz = getClaseValidacion(contexto.getEsquema().getNombre());
            final Constructor<?> constructor = clazz
                    .getConstructor(new Class[] { ContextoValidacion.class });
            return (Validador) constructor.newInstance(contexto);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException
                | NoSuchMethodException | SecurityException ex) {
            throw new ProcesoException("Error en constructor", ex);
        }
    }

    private ValidadorBuilder() {
    }

}
