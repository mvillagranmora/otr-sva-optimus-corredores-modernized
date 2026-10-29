package cl.bcs.fondomutuo.normal;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.UUID;

import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.cierre3b.Guion;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;

/**
 * verify 2026-10-29 (v29, analysis side, package-private access only): a reduced COPY of CasoNormal.correr (no Tesoreria-real layer, no test hooks) that takes the Compuertas
 * of the run, so the two paths of {@code fondomutuo.custodia.normal-habilitada} (and {@code comisiones-en-proceso}) can be compared on the same case directory. The copy is
 * checked against the harness itself: variant "harness" (= CasoNormal.compuertas(ABIERTAS)) must give the same dump as the reflective call of CasoNormal.correr.
 * Variants (custodia = G-CUS, normal = normal-habilitada, ce = comisiones-en-proceso): harness | cn1 (custodia, normal) | cn0 (custodia, not normal) | c0n1 (not custodia, normal) |
 * cn1ce1 | cn0ce1.
 */
public final class V29Gates {

    private V29Gates() {
    }

    static Compuertas compuertas(String v) {
        return switch (v) {
            case "harness" -> CasoNormal.compuertas(CasoNormal.Perfil.ABIERTAS);
            case "cn1" -> new Compuertas("NORMAL", true, false, true, true, true, true, false);
            case "cn0" -> new Compuertas("NORMAL", true, false, true, true, true, false, false);
            case "c0n1" -> new Compuertas("NORMAL", true, false, true, true, false, true, false);
            case "cn1ce1" -> new Compuertas("NORMAL", true, false, true, true, true, true, true);
            case "cn0ce1" -> new Compuertas("NORMAL", true, false, true, true, true, false, true);
            default -> throw new IllegalArgumentException(v);
        };
    }

    public static JsonObject ejecutar(Path dir, String variante) throws Exception {
        java.util.TimeZone zona = java.util.TimeZone.getDefault();
        java.util.Locale idioma = java.util.Locale.getDefault();
        try {
            java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"));
            java.util.Locale.setDefault(java.util.Locale.ENGLISH);
            org.h2.util.DateTimeUtils.resetCalendar();
            return correr(dir, compuertas(variante));
        } finally {
            java.util.TimeZone.setDefault(zona);
            java.util.Locale.setDefault(idioma);
            org.h2.util.DateTimeUtils.resetCalendar();
        }
    }

    private static JsonObject correr(Path dir, Compuertas compuertas) throws Exception {
        Path base = CasoNormal.recursos();
        JsonObject caso = CasoNormal.leer(dir.resolve("case.json"));
        String url = "jdbc:h2:mem:norv29" + UUID.randomUUID().toString().replace("-", "") + ";MODE=LEGACY;DB_CLOSE_DELAY=-1";
        try (Connection raw = DriverManager.getConnection(url, "sa", "")) {
            for (String f : new String[] { "schema-types.sql", "schema-fmm.sql", "schema-keys.sql", "schema-session.sql", "seed/base-fmm.sql" }) {
                CasoNormal.script(raw, Files.readString(base.getParent().resolve("fondomutuo-3a").resolve(f), StandardCharsets.UTF_8));
            }
            if (Files.exists(dir.resolve("seed.sql"))) CasoNormal.script(raw, Files.readString(dir.resolve("seed.sql"), StandardCharsets.UTF_8));
            raw.commit();
            raw.setAutoCommit(false);
            String reloj = caso.has("reloj") ? caso.get("reloj").getAsString() : CasoNormal.RELOJ;
            SesionHana sesion = new SesionHana(raw, url, reloj);
            Connection con = sesion.conexion();
            JsonObject doubles = CasoNormal.leer(base.getParent().resolve("fondomutuo-3a/seed/doubles-base.json"));
            if (caso.has("colaboradores")) {
                for (String k : caso.getAsJsonObject("colaboradores").keySet()) doubles.add(k, caso.getAsJsonObject("colaboradores").get(k));
            }
            Guion guion = new Guion(doubles);
            JsonArray secuencias = new JsonArray();
            NormalPort normal = SujetoNormal.crear(new ConexionUnidadDeTrabajo(new SingleConnectionDataSource(con, true)), con, guion, secuencias, compuertas);
            NormalJavaCallHandler handler = new NormalJavaCallHandler(normal);
            JsonObject res = new JsonObject();
            String decision;
            try {
                JsonObject out = handler.call(con, caso.getAsJsonObject("entrada").deepCopy());
                res.add("salida", out);
                res.addProperty("salidaCruda", out.toString());
                JsonElement cnt = out.get("P_OUT_ERRORESCONTADOR");
                boolean commit = cnt != null && !cnt.isJsonNull() && cnt.getAsLong() == 0;
                decision = commit ? "COMMIT" : "ROLLBACK";
                if (commit) raw.commit(); else raw.rollback();
            } catch (Throwable t) {
                raw.rollback();
                Throwable causa = t instanceof cl.bcs.fondomutuo.kit.AccesoDatosException && t.getCause() != null ? t.getCause() : t;
                decision = t instanceof ServicioNoServidoException ? "ROLLBACK_503" : "ROLLBACK_HTTP500";
                String clase = causa instanceof java.sql.SQLException ? "SQLException" : causa.getClass().getSimpleName();
                res.addProperty("excepcion", clase + ": " + causa.getMessage());
                if (t instanceof ServicioNoServidoException) res.addProperty("compuerta", ((ServicioNoServidoException) t).compuerta());
            }
            res.addProperty("decisionServlet", decision);
            JsonObject tablas = new JsonObject();
            if (caso.has("tablas")) {
                for (JsonElement el : caso.getAsJsonArray("tablas")) {
                    JsonObject t = el.getAsJsonObject();
                    tablas.add(t.get("nombre").getAsString(), volcar(raw, t.get("nombre").getAsString(), t.get("orden").getAsString()));
                }
            }
            res.add("tablas", tablas);
            res.add("colaboradores", guion.llamadas());
            res.add("secuenciasSolicitadas", secuencias);
            JsonObject ses = new JsonObject();
            JsonArray locales = new JsonArray();
            sesion.tablasLocalesSinDrop().forEach(locales::add);
            ses.add("tablasLocalesSinDrop", locales);
            JsonArray au = new JsonArray();
            sesion.commitsAutonomos().forEach(au::add);
            ses.add("commitsAutonomos", au);
            res.add("sesion", ses);
            return res;
        }
    }

    private static JsonArray volcar(Connection conn, String table, String orderBy) throws Exception {
        java.lang.reflect.Method m = CasoNormal.class.getDeclaredMethod("volcar", Connection.class, String.class, String.class);
        m.setAccessible(true);
        return (JsonArray) m.invoke(null, conn, table, orderBy);
    }
}
