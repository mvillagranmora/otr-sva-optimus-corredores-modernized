package cl.bcs.utils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/** ORACLE SHIM of the cl.bcs.utils.Utils members used by the WP-B legacy classes. */
public class Utils {
    /** Values for getConfiguracion (PLA.RutaArchivosConfiguracion, PLA.RutaBaseArchivos). */
    public static final Map<String, String> CONFIGURACION = new HashMap<>();
    /** Next value per sequence name; every call is recorded in SECUENCIA_LLAMADAS. */
    public static final Map<String, Long> SECUENCIAS = new HashMap<>();
    public static final List<String> SECUENCIA_LLAMADAS = new ArrayList<>();
    /** Supplier for the SAP HANA connection used to read OACT (an H2 database in the oracle). */
    public static java.util.function.Supplier<Connection> SAP_CONNECTION;

    public static String getSchema() { return "BSCL"; }

    public static String getConfiguracion(Connection conn, String schema, long idEmpresa, String codigo) throws Exception {
        String v = CONFIGURACION.get(codigo);
        if (v == null) throw new Exception("Configuracion " + codigo + " no encontrada");
        return v;
    }

    /** Legacy Utils.java:985-1016: returns cantidad values; empty list when cantidad <= 0. */
    public static List<Long> getSecuencia(Connection conn, String nombre, Long cantidad) throws SQLException {
        SECUENCIA_LLAMADAS.add(nombre + "#" + cantidad);
        List<Long> out = new ArrayList<>();
        if (cantidad == null || cantidad <= 0) return out;
        long next = SECUENCIAS.getOrDefault(nombre, 1L);
        for (long i = 0; i < cantidad; i++) out.add(next++);
        SECUENCIAS.put(nombre, next);
        return out;
    }

    public static Connection getSAPConnection(String ip, String port, String company, String user, String pass) throws SQLException {
        return SAP_CONNECTION.get();
    }

    /** Legacy Utils.java:768-783 (throws on duplicate keys). */
    @SuppressWarnings("unchecked")
    public static <T, K> HashMap<T, Object> List2Hash(List<K> list, String keyfield) throws Exception {
        HashMap<T, Object> h = new HashMap<>();
        if (list == null) throw new Exception("List must be not null");
        if (list.isEmpty()) return h;
        Field field = list.get(0).getClass().getField(keyfield);
        for (Object o : list) {
            T k = (T) field.get(o);
            if (h.containsKey(k)) throw new Exception("key field is not unique");
            h.put(k, o);
        }
        return h;
    }

    /** Legacy Utils.java:827-880: copy public fields with the same name and type. */
    public static <K, T> T copyPojo(K src, T target) throws Exception {
        if (src == null) throw new Exception("objSrc  must be not null");
        if (target == null) throw new Exception("objTarget  must be not null");
        for (Field f : target.getClass().getFields()) {
            try {
                Field sf = src.getClass().getField(f.getName());
                if (sf.getType().equals(f.getType())) {
                    Object o = sf.get(src);
                    if (o instanceof BigDecimal bd) o = new BigDecimal(bd.toString());
                    f.set(target, o);
                }
            } catch (NoSuchFieldException ignored) {
                // legacy ignores fields missing in the source
            }
        }
        return target;
    }

    /** Legacy Utils.java:750-758. */
    public static String printJson(Object obj, boolean pretty) {
        GsonBuilder b = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        if (pretty) b.setPrettyPrinting();
        Gson gson = b.serializeNulls().create();
        return obj == null ? "[]" : gson.toJson(obj);
    }

    public static Gson getGsonBuilder() {
        return new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                .serializeSpecialFloatingPointValues().serializeNulls().create();
    }
}
