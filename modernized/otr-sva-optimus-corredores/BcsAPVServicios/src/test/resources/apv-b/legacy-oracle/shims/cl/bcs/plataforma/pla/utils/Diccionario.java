package cl.bcs.plataforma.pla.utils;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * ORACLE SHIM of legacy Diccionario (BcsPlataformaServicios/.../pla/utils/Diccionario.java). The legacy loads the translation table
 * and substitutes {@code ::Key::} placeholders. Here a deterministic stand-in is used so a golden "descripcion" proves WHICH code and
 * WHICH values were passed: {@code "[CODIGO]{k1=v1, k2=v2}"} (keys sorted, null value -> empty, as legacy line 61).
 * Reproduced legacy behaviour: {@code valores == null} -> {@code "CODIGO sin traduccion"} (Diccionario.java:55-57);
 * fluent {@code add(k, v)} ignores a null value (Diccionario.java:97-101).
 */
public class Diccionario {
    private static final String SIN_TRADUCCION = "sin traduccion";

    public static String getMensaje(Connection con, Long idEmpresa, String codigo, Map<String, String> valores) throws Exception {
        if (valores == null) {
            return codigo + " " + SIN_TRADUCCION;
        }
        return formato(codigo, valores);
    }

    public static String formato(String codigo, Map<String, String> valores) {
        Map<String, String> sorted = new TreeMap<>();
        for (Map.Entry<String, String> e : valores.entrySet()) {
            sorted.put(e.getKey(), e.getValue() == null ? "" : e.getValue());
        }
        return "[" + codigo + "]" + sorted;
    }

    private Long idEmpresa;
    private String objCodigo = "";
    private Connection conn;
    private final Map<String, String> objValores = new HashMap<String, String>();

    public Diccionario add(String llave, Object valor) {
        if (valor != null)
            objValores.put(llave, valor.toString());
        return this;
    }

    public String msg() throws Exception {
        return getMensaje(conn, idEmpresa, objCodigo, objValores);
    }

    public static Diccionario getMensaje(Connection con, Long idEmpresa, String codigo) throws Exception {
        Diccionario dicc = new Diccionario();
        dicc.objCodigo = codigo;
        dicc.idEmpresa = idEmpresa;
        dicc.conn = con;
        return dicc;
    }
}
