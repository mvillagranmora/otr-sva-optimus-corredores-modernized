package cl.bcs.plataforma.pla.utils;

import java.sql.Connection;
import java.util.Map;
import java.util.TreeMap;

/**
 * ORACLE SHIM of legacy Diccionario.getMensaje. Deterministic stand-in for the dictionary table:
 * "[CODIGO]{k1=v1, k2=v2}" (keys sorted). The modern tests use the same format in their DiccionarioPort
 * fake, so a golden "descripcion" proves which code and which values were passed.
 */
public class Diccionario {
    public static String getMensaje(Connection con, Long idEmpresa, String codigo, Map<String, String> valores) {
        return formato(codigo, valores);
    }

    public static String formato(String codigo, Map<String, String> valores) {
        return "[" + codigo + "]" + new TreeMap<>(valores == null ? Map.of() : valores);
    }
}
