package cl.bcs.normativa.cartolas.master.config;

import java.awt.Color;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class PaletaColores {

    private static final Map<String, Color> COLORES;

    // TODO Extraer desde los style de ItemResInversionClasico.jrxml

    static {
        COLORES = new ConcurrentHashMap();
        COLORES.put("CAJA", new Color(0xff0000));
        COLORES.put("RV", new Color(0xcc6600));
        COLORES.put("RF", new Color(0xcccc00));
        COLORES.put("DV", new Color(0x00ff00));
        COLORES.put("FWD", new Color(0xccffcc));
        COLORES.put("FM", new Color(0x00cccc));
        COLORES.put("OPL", new Color(0x006666));
    }

    public static Color get(
            final String key) {
        return COLORES.get(key);
    }

    public static Set<String> keySet() {
        return COLORES.keySet();
    }

    private PaletaColores() {
    }

}
