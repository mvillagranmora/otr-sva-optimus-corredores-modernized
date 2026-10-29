package cl.bcs.plataforma.pla.utils;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import cl.bcs.utils.Utils;

/**
 * ORACLE SHIM of legacy ConfiguracionSistema (BcsPlataformaServicios/.../pla/utils/ConfiguracionSistema.java). The legacy loads the
 * parameter tables per module; here the values come from the case ("config": "MODULO/Parametro" -> valor, see Utils.CONFIG).
 * A missing parameter throws exactly the legacy message (ConfiguracionSistema.java:70-71).
 */
public class ConfiguracionSistema {

    private static String getParametro(Connection con, Long idEmpresa, String modulo, String nombreParametro) throws Exception {
        String v = Utils.CONFIG.get(modulo + "/" + nombreParametro);
        if (v == null) throw new Exception("Parametro " + nombreParametro + " en modulo " + modulo + " no encontrado");
        return v;
    }

    public static long getParametroConfiguracionLong(Connection con, Long idEmpresa, String modulo, String nombreParametro) throws Exception {
        return Long.parseLong(getParametro(con, idEmpresa, modulo, nombreParametro));
    }

    public static String getParametroConfiguracionString(Connection con, Long idEmpresa, String modulo, String nombreParametro) throws Exception {
        return getParametro(con, idEmpresa, modulo, nombreParametro);
    }

    public static BigDecimal getParametroConfiguracionDecimal(Connection con, Long idEmpresa, String modulo, String nombreParametro) throws Exception {
        return new BigDecimal(getParametro(con, idEmpresa, modulo, nombreParametro));
    }

    public static Set<String> getParametroConfiguracionSet(Connection con, Long idEmpresa, String modulo, String nombreParametro, String delimeter) throws Exception {
        return Arrays.stream(getParametro(con, idEmpresa, modulo, nombreParametro).split(delimeter)).collect(Collectors.toSet());
    }

    public static void limpiarParametros() {
    }
}
