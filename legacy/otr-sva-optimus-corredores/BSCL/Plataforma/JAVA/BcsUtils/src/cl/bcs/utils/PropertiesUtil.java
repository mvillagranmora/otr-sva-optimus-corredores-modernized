package cl.bcs.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

/**
 * M&eacute;todos utiles para el manejo de {@link Properties}.
 *
 * @author fpino@email.com
 *
 */
public final class PropertiesUtil {

    /**
     * Obtiene un valor <code>boolean</code> opcional desde las
     * {@link Properties}, utilizando la llave indicada.
     *
     * @param props
     *            Las {@link Properties}.
     * @param key
     *            La llave de b&uacute;squeda.
     * @param defaultValue
     *            El valor por defecto a utilizar en caso de que la propiedad no
     *            exista.
     * @return Un valor <code>boolean</code> obtenido desde las propiedades o,
     *         el valor por defecto en el caso de que la propiedad no exista.
     */
    public static boolean getOptional(
            final Properties props,
            final String key,
            final boolean defaultValue) {
        boolean ret;
        if (props.containsKey(key)) {
            ret = Boolean.parseBoolean(props.getProperty(key));
        } else {
            ret = defaultValue;
        }

        return ret;
    }

    /**
     * Obtiene un valor <code>int</code> opcional desde las {@link Properties},
     * utilizando la llave indicada.
     *
     * @param props
     *            Las {@link Properties}.
     * @param key
     *            La llave de b&uacute;squeda.
     * @param defaultValue
     *            El valor por defecto a utilizar en caso de que la propiedad no
     *            exista.
     * @return Un valor <code>int</code> obtenido desde las propiedades o, el
     *         valor por defecto en el caso de que la propiedad no exista.
     */
    public static int getOptional(
            final Properties props,
            final String key,
            final int defaultValue) {
        int ret;
        if (props.containsKey(key)) {
            ret = Integer.parseInt(props.getProperty(key));
        } else {
            ret = defaultValue;
        }

        return ret;
    }

    /**
     * Obtiene un valor <code>String</code> opcional desde las
     * {@link Properties}, utilizando la llave indicada.
     *
     * @param props
     *            Las {@link Properties}.
     * @param key
     *            La llave de b&uacute;squeda.
     * @param defaultValue
     *            El valor por defecto a utilizar en caso de que la propiedad no
     *            exista.
     * @return Un valor <code>boolean</code> obtenido desde las propiedades o,
     *         el valor por defecto en el caso de que la propiedad no exista.
     */
    public static String getOptional(
            final Properties props,
            final String key,
            final String defaultValue) {
        String ret;
        if (props.containsKey(key)) {
            ret = props.getProperty(key);
        } else {
            ret = defaultValue;
        }

        return ret;
    }

    /**
     * Obtiene un objeto {@link Date} opcional desde las {@link Properties},
     * utilizando la llave indicada.
     *
     * @param props
     *            Las {@link Properties}.
     * @param key
     *            La llave de b&uacute;squeda.
     * @return Un objeto {@link Date} obtenido desde las propiedades o,
     *         <code>null</code> en el caso de que la propiedad no exista.
     */
    public static Date getOptionalDate(
            final Properties props,
            final String key) {
        Date ret = null;
        if (props.containsKey(key)) {
            ret = DateUtil.getDate(props.getProperty(key));
        }

        return ret;
    }

    /**
     * Obtiene un valor <code>int</code> obligatorio desde las
     * {@link Properties}, utilizando la llave indicada.
     *
     * @param props
     *            Las {@link Properties}.
     * @param key
     *            La llave de b&uacute;squeda.
     * @return Un valor <code>int</code> obtenido desde las propiedades.
     * @throws NumberFormatException
     *             En el caso de que haya un error al interpretar el
     *             n&uacute;mero.
     * @throws PropertyNotFoundException
     *             En el caso de que la propiedad no exista.
     */
    public static int getRequiredInt(
            final Properties props,
            final String key) {
        final String val = props.getProperty(key);
        if (val == null) {
            throw new PropertyNotFoundException("Falta configurar " + key);
        }

        return Integer.valueOf(val);
    }

    /**
     * Obtiene un valor <code>long</code> obligatorio desde las
     * {@link Properties}, utilizando la llave indicada.
     *
     * @param props
     *            Las {@link Properties}.
     * @param key
     *            La llave de b&uacute;squeda.
     * @return Un valor <code>long</code> obtenido desde las propiedades.
     * @throws NumberFormatException
     *             En el caso de que haya un error al interpretar el
     *             n&uacute;mero.
     * @throws PropertyNotFoundException
     *             En el caso de que la propiedad no exista.
     */
    public static long getRequiredLong(
            final Properties props,
            final String key) {
        final String val = props.getProperty(key);
        if (val == null) {
            throw new PropertyNotFoundException("Falta configurar " + key);
        }

        return Long.valueOf(val);
    }

    /**
     * Obtiene un objeto {@link String} obligatorio desde las {@link Properties}
     * , utilizando la llave indicada.
     *
     * @param props
     *            Las {@link Properties}.
     * @param key
     *            La llave de b&uacute;squeda.
     * @return Un objeto {@link String} obtenido desde las propiedades.
     * @throws PropertyNotFoundException
     *             En el caso de que la propiedad no exista.
     */
    public static String getRequiredString(
            final Properties props,
            final String key) {
        final String val = props.getProperty(key);
        if (val == null) {
            throw new PropertyNotFoundException("Falta configurar " + key);
        }

        return val;
    }

    /**
     * Obtiene una nueva instancia de {@link Properties} a partir del
     * {@link Map} otorgado.
     *
     * @param map
     *            El {@link Map} a convertir a {@link Properties}.
     * @return Instancia de {@link Properties} a partir del {@link Map}
     *         otorgado.
     */
    public static Properties newProperties(
            final Map<String, String> map) {
        final Properties props = new Properties();
        props.putAll(map);
        return props;
    }

    /**
     * Obtiene una nueva instancia de {@link Properties} cargadas desde el
     * archivo indicado en la ruta.
     *
     * @param filepath
     *            Ruta del archivo.
     * @return Instancia de {@link Properties} cargadas desde el archivo
     *         indicado en la ruta.
     * @throws IOException
     *             En caso de error de I/O sobre el archivo.
     */
    public static Properties newProperties(
            final String filepath)
                    throws IOException {
        try (InputStream is = new FileInputStream(filepath)) {
            final Properties props = new Properties();
            props.load(is);
            return props;
        }
    }

    private PropertiesUtil() {
    }

}
