package cl.bcs.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/** ORACLE SHIM of the cl.bcs.utils.Utils members used by BcsCuentaInversionJobs (BcsUtils/src/cl/bcs/utils/Utils.java). */
public class Utils {
    /** Next value per sequence name (legacy: SELECT "seq".NEXTVAL FROM SERIES_GENERATE_BIGINT, HANA-only); calls are recorded as "name#cantidad". */
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

    /**
     * Legacy Utils.java:126-138: the platform parameter by code through the HANA procedure
     * "BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracion" (it takes only the code: the module is not an argument).
     * Here the value comes from the case "config" as "PLATAFORMA/<code>". A missing parameter has an UNKNOWN legacy behavior
     * (it depends on the HANA procedure); the shim throws, and no golden depends on that message.
     */
    public static String getConfiguracion(Connection conn, String schema, long idEmpresa, String codigoParametro) throws Exception {
        return getConfiguracion(conn, idEmpresa, codigoParametro);
    }

    public static String getConfiguracion(Connection conn, long idEmpresa, String codigoParametro) throws Exception {
        String v = CONFIG.get("PLATAFORMA/" + codigoParametro);
        if (v == null) throw new Exception("ORACLE-SHIM: parametro " + codigoParametro + " no configurado (comportamiento legacy desconocido)");
        return v;
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

    /** Legacy Utils.java:1018-1024. */
    public static Gson getGsonBuilder() {
        GsonBuilder gbuild = new GsonBuilder();
        gbuild.setDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        gbuild.registerTypeAdapter(java.sql.Timestamp.class, new GsonTimestampDeserializer());
        gbuild.registerTypeAdapter(java.sql.Date.class, new GsonDateDeserializer());
        return gbuild.serializeSpecialFloatingPointValues().serializeNulls().create();
    }

    /** Legacy Utils.java:113-115. */
    public static String getSchema() {
        return "BSCL";
    }

    /**
     * Legacy Utils.java:294-300 (date part of the system date-time). The stored procedure ObtenerFechaHoraSistema is replaced by
     * PLATAFORMA/PLA.FechaSistema, which is what FechaDB.ObtenerFechaHoraSistemaUTC uses for the date part (ASSUMPTION).
     */
    public static String getFechaSistema(Connection conn, String schema, long idEmpresa) throws Exception {
        return getFechaSistema(conn, idEmpresa);
    }

    public static String getFechaSistema(Connection conn, long idEmpresa) throws Exception {
        String f = CONFIG.get("PLATAFORMA/PLA.FechaSistema");
        if (f == null) throw new Exception("Parametro PLA.FechaSistema en modulo PLATAFORMA no encontrado");
        return f;
    }
}
