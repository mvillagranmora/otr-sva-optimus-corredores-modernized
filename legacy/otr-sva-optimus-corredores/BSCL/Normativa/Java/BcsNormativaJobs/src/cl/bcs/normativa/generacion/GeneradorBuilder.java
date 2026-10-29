package cl.bcs.normativa.generacion;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import cl.bcs.normativa.procesos.ProcesoException;

/**
 * Constructores asociados a {@link Generador}.
 *
 * @author fpino@email.com
 *
 */
public final class GeneradorBuilder {

    /**
     * Obtiene una instancia de la clase de generaci&oacute;n asociada al nombre
     * del esquema.
     *
     * @param nombreEsquema
     *            El nombre del esquema.
     * @return Una instancia de la clase de generaci&oacute;n asociada al
     *         esquema.
     */
    private static Class<?> getClaseGeneracion(final String nombreEsquema) {
        try {
            final String className = Generador.class.getCanonicalName() + nombreEsquema;
            return Class.forName(className);
        } catch (ClassNotFoundException ex) {
            throw new ProcesoException("Generador no encontrado para: " + nombreEsquema, ex);
        }
    }

    /**
     * Obtiene nueva instancia de {@link Generador} relacionada al esquema.
     *
     * @param contexto
     *            El contexto.
     * @return Una nueva instancia de {@link Generador} relacionada al esquema.
     */
    public static Generador newInstance(final ContextoGeneracion contexto) {
        try {
            final String nombreEsquema = contexto.getEsquema().getNombre();
            final Class<?> clazz = getClaseGeneracion(nombreEsquema);
            final Constructor<?> constructor = clazz
                    .getConstructor(new Class[] { ContextoGeneracion.class });
            return (Generador) constructor.newInstance(contexto);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException
                | NoSuchMethodException | SecurityException ex) {
            throw new ProcesoException("Error en constructor", ex);
        }
    }

    private GeneradorBuilder() {
    }

}
