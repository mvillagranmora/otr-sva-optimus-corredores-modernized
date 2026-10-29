package cl.bcs.utils;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Date;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * ORACLE SHIM of the cl.bcs.utils.Utils members used by the reporting classes. Bodies are the legacy bodies
 * (legacy/.../Plataforma/JAVA/BcsUtils/src/cl/bcs/utils/Utils.java) except where marked "DEVIATION".
 */
public class Utils {
    /** DEVIATION: calls recorded as "name#cantidad" (the real one only issues the SQL). */
    public static final List<String> SECUENCIA_LLAMADAS = new ArrayList<>();

    /** Utils.java:113-115 */
    public final static String getSchema() {
        return "BSCL";
    }

    /**
     * Utils.java:121-132 calls the stored procedure BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracion. DEVIATION: the
     * procedure source is not available; the shim answers with PLA.Configuracion.Valor for (IdEmpresa, CodigoParametro)
     * and fails when the parameter does not exist (assumption, flagged in the README).
     */
    public static String getConfiguracion(Connection conn, String schema, long idEmpresa, String codigo) throws Exception {
        try (java.sql.PreparedStatement ps = conn.prepareStatement(
                "SELECT \"Valor\" FROM \"BSCL.Plataforma.BD::PLA.Configuracion\" WHERE \"IdEmpresa\" = ? AND \"CodigoParametro\" = ?")) {
            ps.setLong(1, idEmpresa);
            ps.setString(2, codigo);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new Exception("Configuracion " + codigo + " no encontrada");
                return rs.getString(1);
            }
        }
    }

    /**
     * Utils.java:985-1016: cantidad values from the HANA sequence, empty list when cantidad is null or &lt;= 0.
     * DEVIATION: H2 syntax for the same call ("name".NEXTVAL, SQL.java real query is
     * SELECT "name".NEXTVAL FROM SERIES_GENERATE_BIGINT(1,0,:cantidad)); the sequences live in schema-reportes.sql.
     */
    public static List<Long> getSecuencia(Connection conn, String nombre, Long cantidad) throws SQLException {
        SECUENCIA_LLAMADAS.add(nombre + "#" + cantidad);
        List<Long> out = new ArrayList<>();
        if (cantidad == null || cantidad <= 0) return out;
        for (long i = 0; i < cantidad; i++) {
            try (java.sql.Statement st = conn.createStatement();
                 java.sql.ResultSet rs = st.executeQuery("SELECT \"" + nombre + "\".NEXTVAL")) {
                if (rs.next()) out.add(rs.getLong(1));
            }
        }
        return out;
    }

    /** Utils.java:750-758 */
    public static String printJson(Object obj, boolean pretty) {
        Gson gson;
        if (pretty) gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").setPrettyPrinting().serializeNulls().create();
        else gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
        String msg = "";
        if (obj == null) msg = "[]";
        else msg = gson.toJson(obj);
        return msg;
    }

    /** Utils.java:768-783 (throws on duplicate keys) */
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

    /** Utils.java:1385-1398 */
    public static <K> List<HashMap<String, Object>> List2ListHashGenericObject(List<K> list) throws Exception {
        List<HashMap<String, Object>> listObj = new ArrayList<HashMap<String, Object>>();
        if (list == null) throw new Exception("List must be not null");
        if (list.size() == 0) return listObj;
        for (Object obj : list) {
            HashMap<String, Object> hObj = new HashMap<String, Object>();
            Field[] fields = obj.getClass().getFields();
            for (Field field : fields) {
                hObj.put(field.getName(), field.get(obj));
            }
            listObj.add(hObj);
        }
        return listObj;
    }

    /** Utils.java:1379-1383 */
    public static int diferenciaEnDias(Date fechaMayor, Date fechaMenor) {
        long diferenciaEn_ms = fechaMayor.getTime() - fechaMenor.getTime();
        long dias = diferenciaEn_ms / (1000 * 60 * 60 * 24);
        return (int) dias;
    }

    /** Utils.java:1018-1025 */
    public static Gson getGsonBuilder() {
        GsonBuilder gbuild = new GsonBuilder();
        gbuild.setDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        gbuild.registerTypeAdapter(java.sql.Timestamp.class, new GsonTimestampDeserializer());
        gbuild.registerTypeAdapter(java.sql.Date.class, new GsonDateDeserializer());
        Gson gson = gbuild.serializeSpecialFloatingPointValues().serializeNulls().create();
        return gson;
    }
}
