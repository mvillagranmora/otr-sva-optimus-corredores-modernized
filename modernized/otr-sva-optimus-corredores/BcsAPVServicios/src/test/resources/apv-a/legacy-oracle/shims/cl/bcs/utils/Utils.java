package cl.bcs.utils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/** ORACLE SHIM of the cl.bcs.utils.Utils members used by the CuentaInversion legacy classes (BcsUtils/src/cl/bcs/utils/Utils.java). */
public class Utils {
    /** Next value per sequence name (legacy: SELECT "seq".NEXTVAL FROM SERIES_GENERATE_BIGINT, HANA-only); calls are recorded. */
    public static final Map<String, Long> SECUENCIAS = new HashMap<>();
    public static final List<String> SECUENCIA_LLAMADAS = new ArrayList<>();
    /** "MODULO/Parametro" -> valor (shared with the ConfiguracionSistema shim). */
    public static Map<String, String> CONFIG = new HashMap<>();

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

    /** Legacy Utils.java:113-115. */
    public static final String getSchema() {
        return "BSCL";
    }

    /**
     * Legacy Utils.java:294-300: the date part of the HANA stored procedure ObtenerFechaHoraSistema, which returns PLA.FechaSistema + the time
     * of the day. The procedure is not reproducible on H2: the date is the configured PLATAFORMA/PLA.FechaSistema (same value FechaDB uses).
     */
    public static final String getFechaSistema(Connection conn, String schema, long idEmpresa) throws Exception {
        String v = CONFIG.get("PLATAFORMA/PLA.FechaSistema");
        if (v == null) throw new Exception("Parametro PLA.FechaSistema en modulo PLATAFORMA no encontrado");
        return v;
    }

    /** Legacy Utils.java:185- configuration of every module for an empresa, as MODULO -> (CodigoParametro -> Valor). */
    public static HashMap<String, HashMap<String, String>> getConfiguraciones(Connection con, long idEmpresa) throws SQLException {
        HashMap<String, HashMap<String, String>> mapa = new HashMap<>();
        for (Map.Entry<String, String> e : CONFIG.entrySet()) {
            int slash = e.getKey().indexOf('/');
            mapa.computeIfAbsent(e.getKey().substring(0, slash), k -> new HashMap<>()).put(e.getKey().substring(slash + 1), e.getValue());
        }
        return mapa;
    }

    /** Legacy Utils.java:750-758. */
    public static String printJson(Object obj, boolean pretty) {
        Gson gson;
        if (pretty) gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").setPrettyPrinting().serializeNulls().create();
        else gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
        String msg = "";
        if (obj == null) msg = "[]";
        else msg = gson.toJson(obj);
        return msg;
    }

    /** Legacy Utils.java:768-783 (throws on duplicate keys, key read through the FIRST element's class). */
    @SuppressWarnings("unchecked")
    public static <T, K> HashMap<T, Object> List2Hash(List<K> list, String keyfield) throws Exception {
        HashMap<T, Object> hobj = new HashMap<T, Object>();
        if (list == null) throw new Exception("List must be not null");
        if (list.size() == 0) return hobj;
        Object lobj = list.get(0);
        Field field = lobj.getClass().getField(keyfield);
        if (field == null) throw new SQLException("Object field unknow for " + keyfield);
        for (Object obj : list) {
            T kf = (T) field.get(obj);
            if (hobj.containsKey(kf)) {
                throw new Exception("key field is not unique");
            }
            hobj.put(kf, obj);
        }
        return hobj;
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

    /** Legacy Utils.java:1018-1024. */
    public static Gson getGsonBuilder() {
        GsonBuilder gbuild = new GsonBuilder();
        gbuild.setDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        gbuild.registerTypeAdapter(java.sql.Timestamp.class, new GsonTimestampDeserializer());
        gbuild.registerTypeAdapter(java.sql.Date.class, new GsonDateDeserializer());
        return gbuild.serializeSpecialFloatingPointValues().serializeNulls().create();
    }

    /** Legacy Utils.java:1422-1443. */
    public static Optional<Long> contarRegistros(Connection conn, String nombreTabla) throws RuntimeException, SQLException {
        Long count = null;
        if (Objects.isNull(conn)) {
            throw new RuntimeException("Connection obligatoria.");
        }
        if (Objects.isNull(nombreTabla) || nombreTabla.isEmpty()) {
            throw new RuntimeException("Nombre Tabla obligatorio.");
        }
        String query = String.format("SELECT COUNT(*) AS \"Cantidad\" FROM \"%s\"", nombreTabla);
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    count = rs.getLong(1);
                }
            }
        }
        return Optional.ofNullable(count);
    }
}
