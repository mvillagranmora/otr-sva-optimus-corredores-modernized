package cl.bcs.fondomutuo.normal;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.cierre3b.Guion;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;
import cl.bcs.fondomutuo.manejador.CasoMan;
import cl.bcs.fondomutuo.manejador.ManejadorJavaCallHandler;
import cl.bcs.fondomutuo.manejador.ManejadorSolicitudAporteRescateService;
import cl.bcs.fondomutuo.normal.CasoNormal.Perfil;

/**
 * Step 4B / integration with 4A: runs a Manejador case (oracle format) through the NEW Manejador over the REAL new Normal on the oracle H2 (schema, base
 * seed and HANA emulation of 4A's harness: {@link SesionHana}, {@link SujetoNormal}). The seven other destinations are answered by the case script
 * ({@link Guion}, same matching rules as the oracle's doubles) and recorded in the same call list, so the dump has every key of the golden. In this
 * package only to reuse the package-private wiring of 4A's harness (nothing of 4A is changed).
 */
final class ManejadorSobreNormal {

    private ManejadorSobreNormal() {
    }

    static JsonObject correr(CasoMan c) throws Exception {
        java.util.TimeZone zona = java.util.TimeZone.getDefault();
        java.util.Locale idioma = java.util.Locale.getDefault();
        try {
            java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"));
            java.util.Locale.setDefault(java.util.Locale.ENGLISH);
            org.h2.util.DateTimeUtils.resetCalendar();
            return ejecutar(c);
        } finally {
            java.util.TimeZone.setDefault(zona);
            java.util.Locale.setDefault(idioma);
            org.h2.util.DateTimeUtils.resetCalendar();
        }
    }

    private static String recurso(String ruta) throws Exception {
        try (var in = ManejadorSobreNormal.class.getClassLoader().getResourceAsStream(ruta)) {
            if (in == null) throw new IllegalStateException("resource missing: " + ruta);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static JsonObject ejecutar(CasoMan c) throws Exception {
        String url = "jdbc:h2:mem:man" + UUID.randomUUID().toString().replace("-", "") + ";MODE=LEGACY;DB_CLOSE_DELAY=-1";
        try (Connection raw = DriverManager.getConnection(url, "sa", "")) {
            for (String f : new String[] { "schema-types.sql", "schema-fmm.sql", "schema-keys.sql", "schema-session.sql", "seed/base-fmm.sql" }) {
                script(raw, recurso("fondomutuo-3a/" + f));
            }
            script(raw, recurso("fondomutuo-man/cases/" + c.id + "/seed.sql"));
            raw.commit();
            raw.setAutoCommit(false);
            SesionHana sesion = new SesionHana(raw, url, c.caso.has("reloj") ? c.caso.get("reloj").getAsString() : CasoNormal.RELOJ);
            Connection con = sesion.conexion();
            JsonObject doubles = CasoNormal.leer(java.nio.file.Path.of(ManejadorSobreNormal.class.getClassLoader().getResource("fondomutuo-3a/seed/doubles-base.json").toURI()));
            if (c.caso.has("colaboradores")) {
                for (String k : c.caso.getAsJsonObject("colaboradores").keySet()) doubles.add(k, c.caso.getAsJsonObject("colaboradores").get(k));
            }
            Guion guion = new Guion(doubles);
            JsonArray secuencias = new JsonArray();
            NormalPort normal = SujetoNormal.crear(new ConexionUnidadDeTrabajo(new SingleConnectionDataSource(con, true)), con, guion, secuencias,
                    CasoNormal.compuertas(Perfil.ABIERTAS));
            Compuertas g0 = new Compuertas("NORMAL,PROGRAMADO,OVERNIGHT,REINVERSION_APORTE,CAMBIO_SERIE,RESCATE_REINVERSION,TRANSFERENCIA,TRASPASO",
                    false, false, false, false, false);
            ManejadorJavaCallHandler handler = new ManejadorJavaCallHandler(new ManejadorSolicitudAporteRescateService(normal,
                    CasoMan.destinos(guion::responder), g0::g0ListasConFilas));
            JsonObject res = new JsonObject();
            String decision;
            try {
                JsonObject out = handler.call(con, c.caso.getAsJsonObject("entrada").deepCopy());
                res.add("salida", out);
                res.addProperty("salidaCruda", out.toString());
                JsonElement cnt = out.get("P_OUT_ERRORESCONTADOR");
                boolean commit = cnt != null && !cnt.isJsonNull() && cnt.getAsLong() == 0;
                decision = commit ? "COMMIT" : "ROLLBACK";
                if (commit) raw.commit(); else raw.rollback();
            } catch (Throwable t) {
                raw.rollback();
                decision = t instanceof ServicioNoServidoException ? "ROLLBACK_503" : "ROLLBACK_HTTP500";
                res.addProperty("excepcion", t.getClass().getSimpleName() + ": " + t.getMessage());
            }
            res.addProperty("decisionServlet", decision);
            JsonObject tablas = new JsonObject();
            for (JsonElement el : c.caso.getAsJsonArray("tablas")) {
                JsonObject t = el.getAsJsonObject();
                tablas.add(t.get("nombre").getAsString(), volcar(raw, t.get("nombre").getAsString(), t.get("orden").getAsString()));
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
        JsonArray rows = new JsonArray();
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM \"" + table + "\" ORDER BY " + orderBy)) {
            ResultSetMetaData md = rs.getMetaData();
            while (rs.next()) {
                JsonObject row = new JsonObject();
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    Object v = rs.getObject(i);
                    String col = md.getColumnLabel(i);
                    if (v == null) row.add(col, JsonNull.INSTANCE);
                    else if (v instanceof Number) row.add(col, new JsonPrimitive(new BigDecimal(new BigDecimal(v.toString()).stripTrailingZeros().toPlainString())));
                    else if (v instanceof java.util.Date) row.addProperty(col, rs.getString(i));
                    else row.addProperty(col, v.toString());
                }
                rows.add(row);
            }
        }
        return rows;
    }

    private static void script(Connection conn, String texto) throws Exception {
        List<String> clean = new ArrayList<>();
        for (String line : texto.split("\n")) {
            if (!line.strip().startsWith("--")) clean.add(line.replaceAll(";\\s+--[^']*$", ";"));
        }
        try (Statement st = conn.createStatement()) {
            for (String stmt : String.join("\n", clean).split(";\\s*\n")) {
                if (!stmt.isBlank()) st.execute(stmt);
            }
        }
    }
}
