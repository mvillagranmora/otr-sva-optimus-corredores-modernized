package cl.bcs.plataforma.pla.utils;

import java.sql.Connection;

import com.google.gson.JsonElement;

import cl.bcs.oracle.Doubles;

/**
 * ORACLE DOUBLE of the legacy cl.bcs.plataforma.pla.utils.ConfiguracionSistema (not part of the Replica module): the two static readers the module calls, with the real signatures.
 * The answer is the parameter text scripted by the case; getParametroConfiguracionLong parses it like the real one (Long.parseLong of the text).
 */
public class ConfiguracionSistema {
    public static long getParametroConfiguracionLong(Connection con, Long idEmpresa, String modulo, String nombreParametro) throws Exception {
        return Long.parseLong(texto(Doubles.answer("ConfiguracionSistema", "getParametroConfiguracionLong", new Object[] { idEmpresa, modulo, nombreParametro })));
    }

    public static String getParametroConfiguracionString(Connection con, Long idEmpresa, String modulo, String nombreParametro) throws Exception {
        return texto(Doubles.answer("ConfiguracionSistema", "getParametroConfiguracionString", new Object[] { idEmpresa, modulo, nombreParametro }));
    }

    private static String texto(JsonElement r) {
        return r == null || r.isJsonNull() ? null : r.getAsString();
    }
}
