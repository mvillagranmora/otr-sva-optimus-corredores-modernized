package cl.bcs.utils;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/** ORACLE SHIM of the cl.bcs.utils.Utils members used by the four APV jobs and their fakes (BcsUtils/src/cl/bcs/utils/Utils.java). */
public class Utils {
    /** "MODULO/Parametro" -> valor, the configuration of the case (shared with the ConfiguracionSistema shim). */
    public static Map<String, String> CONFIG = new HashMap<>();

    /** Legacy Utils.java:113-115. */
    public static final String getSchema() {
        return "BSCL";
    }

    /**
     * Legacy Utils.java:131-138: the HANA procedure BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracion reads PLA.Configuracion (module PLATAFORMA)
     * and SIGNALs error 10007 when the parameter does not exist (Plataforma/BD/Utilitarios/ObtenerConfiguracion.sql): a StoredProcedureException.
     */
    public final static String getConfiguracion(java.sql.Connection conn, String schema, long IdEmpresa, String CodigoParametro) throws cl.bcs.nuam.StoredProcedureException {
        return getConfiguracion(conn, IdEmpresa, CodigoParametro);
    }

    public final static String getConfiguracion(java.sql.Connection conn, long IdEmpresa, String CodigoParametro) throws cl.bcs.nuam.StoredProcedureException {
        String v = CONFIG.get("PLATAFORMA/" + CodigoParametro);
        if (v == null) {
            throw new cl.bcs.nuam.StoredProcedureException("PARAMETRO_DE_CONFIGURACION_NO_EXISTE (SQL error code 10007)");
        }
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
}
