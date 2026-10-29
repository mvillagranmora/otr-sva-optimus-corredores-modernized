package cl.bcs.normativa.cartola.utils;

import java.awt.Color;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class PaletaColores {

    private static final Map<String, Color> COLORES;

    // TODO Extraer desde los style de ItemResInversionClasico.jrxml

    static {
        COLORES = new ConcurrentHashMap();
        COLORES.put("CAJA", new Color(0x006A8F));
        COLORES.put("RV", new Color(0xD9993D));
        COLORES.put("RF", new Color(0x006A4D));
        COLORES.put("IF", new Color(0xD4D43D));
        COLORES.put("SIM", new Color(0x00ff00));
        //Operaciones por liquidar
        //Pagos por recibir / entregar
        COLORES.put("DV", new Color(0x02BF28));               
        COLORES.put("FWD", new Color(0xA18660));
        COLORES.put("FM", new Color(0x00CCCC));
        COLORES.put("OPL", new Color(0xBD4B4B));
        
        /*COLORES.put("CAJA", new Color(0xE1CFCF));
        COLORES.put("RV", new Color(0xBC8F8F));
        COLORES.put("RF", new Color(0x86ADAD));
        COLORES.put("IF", new Color(0xFFFFFF));
        COLORES.put("SIM", new Color(0x330817));
        //Operaciones por liquidar
        //Pagos por recibir / entregar
        COLORES.put("DV", new Color(0xFF9933));               
        COLORES.put("FWD", new Color(0xccffcc));
        COLORES.put("FM", new Color(0x00CCCC));
        COLORES.put("OPL", new Color(0x006666));*/
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
