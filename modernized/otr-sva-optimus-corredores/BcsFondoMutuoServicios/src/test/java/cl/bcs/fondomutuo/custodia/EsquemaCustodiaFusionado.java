package cl.bcs.fondomutuo.custodia;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Puts the H2 stand-in schema of Custodia (copy of BcsCustodiaServicios, {@code fondomutuo-custodia-en-proceso/schema-flujo.sql} + {@code schema-session.sql})
 * into a database that ALREADY holds the FondoMutuo schema of the F1/F2 cases, so that the real custody flow and RescateReinversion run in ONE database and
 * ONE transaction (test harness only, H2 only). About 20 tables exist in both stand-ins under the same name (company, configuration, dictionary, holidays,
 * instruments...) with different column sets: for those the columns the custody flow reads are ADDED to the existing table
 * ({@code ALTER TABLE ... ADD COLUMN IF NOT EXISTS}, nullable) and no table is recreated, so the FondoMutuo rows and constraints are untouched. The aliases of the
 * session script that FondoMutuo's schema already defines ({@code TO_BIGINT}) and the {@code DUMMY} table are not created twice.
 */
public final class EsquemaCustodiaFusionado {

    private static final Pattern CREATE_TABLE = Pattern.compile("(?s)^\\s*CREATE TABLE\\s+(\"[^\"]+\")\\s*\\((.*)\\)\\s*$");
    private static final Pattern COLUMNA = Pattern.compile("^\\s*(\"[^\"]+\")\\s+(.+?)\\s*,?\\s*$");

    private EsquemaCustodiaFusionado() {
    }

    public static void aplicar(Connection raw) throws Exception {
        Path base = EntornoCustodia.recursos();
        for (String f : new String[] { "schema-flujo.sql", "schema-session.sql" }) {
            for (String stmt : sentencias(base.resolve(f))) {
                aplicarSentencia(raw, stmt);
            }
        }
    }

    /** The custody rows RescateReinversion needs, on top of the FondoMutuo base seed (custody position of client 100, portfolio 500, instrument 900, regime GEN). */
    public static void semillaRescateReinversion(Connection raw) throws Exception {
        for (String stmt : sentencias(EntornoCustodia.recursos().resolve("custodia-rre-seed.sql"))) {
            try (Statement st = raw.createStatement()) {
                st.execute(stmt);
            }
        }
    }

    private static void aplicarSentencia(Connection raw, String stmt) throws SQLException {
        Matcher m = CREATE_TABLE.matcher(stmt);
        if (m.find() && existe(raw, m.group(1))) {
            for (String linea : m.group(2).split("\n")) {
                Matcher c = COLUMNA.matcher(linea);
                if (c.find()) {
                    try (Statement st = raw.createStatement()) {
                        st.execute("ALTER TABLE " + m.group(1) + " ADD COLUMN IF NOT EXISTS " + c.group(1) + " " + c.group(2));
                    }
                }
            }
            return;
        }
        String u = stmt.strip().toUpperCase();
        if (u.startsWith("CREATE ALIAS TO_BIGINT") || u.startsWith("CREATE TABLE DUMMY") || u.startsWith("INSERT INTO DUMMY")) {
            return;   // already provided by the FondoMutuo schema (same meaning)
        }
        try (Statement st = raw.createStatement()) {
            st.execute(stmt);
        }
    }

    private static boolean existe(Connection raw, String nombreEntreComillas) throws SQLException {
        String nombre = nombreEntreComillas.substring(1, nombreEntreComillas.length() - 1);
        try (java.sql.PreparedStatement ps = raw.prepareStatement("SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?")) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    /** Same splitting as the custody harness ({@code FlujoCaso.runScript}): comment lines out, statements end with ";" and a line break. */
    static List<String> sentencias(Path file) throws Exception {
        List<String> limpias = new ArrayList<>();
        for (String line : new String(Files.readAllBytes(file), StandardCharsets.UTF_8).split("\n")) {
            if (!line.strip().startsWith("--")) limpias.add(line.replaceAll(";\\s+--[^']*$", ";"));
        }
        List<String> r = new ArrayList<>();
        for (String s : String.join("\n", limpias).split(";\\s*\n")) {
            if (!s.isBlank()) r.add(s);
        }
        return r;
    }
}
