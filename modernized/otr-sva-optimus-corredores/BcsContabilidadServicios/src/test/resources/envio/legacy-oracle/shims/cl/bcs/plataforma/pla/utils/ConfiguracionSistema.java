package cl.bcs.plataforma.pla.utils;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/** ORACLE SHIM of legacy ConfiguracionSistema: "MODULO/Parametro" -> valor; missing -> Exception (ConfiguracionSistema.java:67-68). */
public class ConfiguracionSistema {
    public static final Map<String, String> VALORES = new HashMap<>();

    public static String getParametroConfiguracionString(Connection con, Long idEmpresa, String modulo, String nombre) throws Exception {
        String k = modulo + "/" + nombre;
        if (!VALORES.containsKey(k)) throw new Exception("Parametro " + nombre + " en modulo " + modulo + " no encontrado");
        return VALORES.get(k);
    }

    public static long getParametroConfiguracionLong(Connection con, Long idEmpresa, String modulo, String nombre) throws Exception {
        return Long.parseLong(getParametroConfiguracionString(con, idEmpresa, modulo, nombre));
    }
}
