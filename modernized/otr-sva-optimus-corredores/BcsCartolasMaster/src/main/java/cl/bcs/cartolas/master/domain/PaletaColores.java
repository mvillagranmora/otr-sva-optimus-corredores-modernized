package cl.bcs.cartolas.master.domain;

import java.awt.Color;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Paleta de colores por clase de activo utilizada en los gráficos de la Cartola Master.
 *
 * <p>Equivalente modernizado de {@code config.PaletaColores} legacy.</p>
 *
 * <p>Los métodos son {@code static} para poder ser testeados sin Spring.</p>
 */
public final class PaletaColores {

    private static final Map<String, Color> COLORES;

    static {
        final Map<String, Color> mutable = new ConcurrentHashMap<>();
        mutable.put("CAJA", new Color(0xff0000));
        mutable.put("RV",   new Color(0xcc6600));
        mutable.put("RF",   new Color(0xcccc00));
        mutable.put("DV",   new Color(0x00ff00));
        mutable.put("FWD",  new Color(0xccffcc));
        mutable.put("FM",   new Color(0x00cccc));
        mutable.put("OPL",  new Color(0x006666));
        COLORES = Collections.unmodifiableMap(mutable);
    }

    /**
     * Retorna el color asociado a la clave de producto.
     *
     * @param key clave de producto (e.g. "RV", "RF", "FM")
     * @return {@link Color} o {@code null} si la clave no existe
     */
    public static Color get(final String key) {
        return COLORES.get(key);
    }

    /**
     * Retorna todas las claves de producto configuradas.
     */
    public static Set<String> keySet() {
        return COLORES.keySet();
    }

    /**
     * Retorna el mapa completo de colores (inmutable).
     */
    public static Map<String, Color> getColores() {
        return COLORES;
    }

    private PaletaColores() {
        // utility class — no instances
    }
}
