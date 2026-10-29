package cl.bcs.normativa.data;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import cl.bcs.normativa.esquema.Campo;
import cl.bcs.normativa.esquema.Esquema;
import cl.bcs.sql.generator.SqlFormatter;

/**
 * Registro de datos.
 *
 * @author fpino@email.com
 *
 */
public final class Registro {

    /**
     * Esquema del registro.
     */
    private final Esquema esquema;

    /**
     * Map que contiene los datos.
     */
    private final Map<String, Comparable<?>> map;

    /**
     * Raz&oacute;n de la invalidez del registro.
     */
    private String razonInvalidez;

    /**
     * Indica si el registro es valido.
     */
    private boolean valido;

    /**
     * Constructor dado el esquema.
     *
     * @param esquema
     *            El esquema.
     */
    public Registro(final Esquema esquema) {
        this.esquema = esquema;
        this.map = new ConcurrentHashMap<>();
    }

    /**
     * Obtiene el valor asociado a la llave.
     *
     * @param key
     *            La llave.
     * @return El valor asociado a la llave o, <code>null</code> si no hay valor
     *         asociado a la llave.
     */
    public Comparable<?> get(final String key) {
        return map.get(key);
    }

    /**
     * Obtiene la clave serializada.
     *
     * @return La clave serializada.
     * @throws IOException
     *             En caso de error de I/O.
     */
    public String getClave() throws IOException {
        return serialize(esquema.getNombresCamposClave());
    }

    /**
     * Obtiene el esquema al que corresponde el registro.
     * 
     * @return El esquema al que corresponde el registro.
     */
    public Esquema getEsquema() {
        return esquema;
    }

    /**
     * Obtiene el campo dado el c&oacute;digo SII del mismo.
     *
     * @param codigoSII
     *            El c&oacute;digo SII.
     * @return El campo dado el c&oacute;digo SII del mismo.
     */
    public Comparable<?> getPorCodigoSII(String codigoSII) {
        final Campo campo = esquema.buscarCampoPorCodigoSII(codigoSII);
        if (campo == null) {
            return null;
        }

        return get(campo.getNombre());
    }

    /**
     * Obtiene el campo dado el nombre del mismo.
     *
     * @param nombreCampo
     *            El nombre del campo
     * @return El campo dado del mismo.
     */
    public Comparable<?> getPorNombre(String nombreCampo) {
        final Campo campo = esquema.buscarCampoPorNombre(nombreCampo);
        if (campo == null) {
            return null;
        }

        return get(campo.getNombre());
    }

    

    /**
     * Obtiene la raz&oacute;n de la invalidez del registro.
     *
     * @return La raz&oacute;n de la invalidez del registro.
     */
    public String getRazonInvalidez() {
        return razonInvalidez;
    }

    /**
     * Obtiene el RUT del cliente.
     *
     * @return El RUT del cliente.
     */
    public String getRutCliente() {
        final String nombreCampoRutCliente = esquema.getNombreCampoRutCliente();
        return (String) get(nombreCampoRutCliente);
    }

    /**
     * Indica si el registro es valido.
     *
     * @return <code>true</code> si el registro es valido, <code>false</code> en
     *         caso contrario.
     */
    public boolean isValido() {
        return valido;
    }

    /**
     * Obtiene el conjunto de claves de los campos del registro de datos.
     *
     * @return El conjunto de claves de los campos del registro de datos.
     */
    public Set<String> keySet() {
        return map.keySet();
    }

    /**
     * Asigna el valor asociado a la llave.
     *
     * @param key
     *            La llave.
     * @param value
     *            El valor.
     */
    public void put(final String key, final Comparable<?> value) {
        map.put(key, value);
    }

    /**
     * Serializa el registro de datos, usando los nombres de campos indicados.
     *
     * @param nombres
     *            Lista de los nombres de campo.
     * @return {@link String} de la serializaci&oacute;n de la porci&oacute;n
     *         del registro de datos.
     * @throws IOException
     *             En caso de error de I/O.
     */
    public String serialize(final List<String> nombres) throws IOException {
        String ret = "";

        for (final String nombre : nombres) {
            if (!ret.isEmpty()) {
                ret += "&";
            }

            Comparable<?> cmp = this.get(nombre);
            if (cmp == null) {
                ret += nombre + "=";
            } else {
                final String valor = SqlFormatter.format(this.get(nombre));
                ret += nombre + "=" + URLEncoder.encode(valor, "UTF-8");

            }
        }

        return ret;
    }

    /**
     * Asigna la raz&oacute;n de la invalidez del registro.
     *
     * @param razonInvalidez
     *            La raz&oacute;n de la invalidez del registro.
     */
    public void setRazonInvalidez(final String razonInvalidez) {
        this.razonInvalidez = razonInvalidez;
    }

    /**
     * Asigna si el registro es valido.
     *
     * @param valido
     *            Si el registro es valido.
     */
    public void setValido(final boolean valido) {
        this.valido = valido;
    }

    @Override
    public String toString() {
        return "Registro " + map;
    }

}
