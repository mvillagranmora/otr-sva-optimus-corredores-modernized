package cl.bcs.contabilidad.servicios;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Oracle copies of the legacy SQL statements.
 *
 * <p>The files under {@code src/test/resources/servicios/sql/} are verbatim copies of
 * {@code legacy/otr-sva-optimus-corredores/BSCL/Contabilidad/JAVA/BcsContabilidadServicios/src/cl/bcs/contabilidad/sql/}.
 * They contain no credentials.
 */
final class LegacySql {

    private LegacySql() {}

    static final String MODERN_RESOURCE_PREFIX = "cl/bcs/contabilidad/sql/";

    /** Text of a legacy SQL file (oracle copy). Fails loudly if the oracle copy is missing. */
    static String text(String file) {
        try (InputStream in = LegacySql.class.getClassLoader().getResourceAsStream("servicios/sql/" + file)) {
            if (in == null) throw new IllegalStateException("legacy oracle SQL missing from test resources: " + file);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Text of the SQL resource the modern module ships at the legacy path, or null. */
    static String modernResource(String file) {
        try (InputStream in = LegacySql.class.getClassLoader().getResourceAsStream(MODERN_RESOURCE_PREFIX + file)) {
            return in == null ? null : new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Whitespace/comment/trailing-semicolon tolerant normal form. */
    static String norm(String sql) {
        if (sql == null) return "";
        String s = sql.replaceAll("(?s)/\\*.*?\\*/", " ").replaceAll("--[^\\n]*", " ");
        s = s.replaceAll("\\s+", " ").trim();
        s = s.replaceAll("\\s*([(),=])\\s*", "$1");
        while (s.endsWith(";")) s = s.substring(0, s.length() - 1).trim();
        return s;
    }

    private static final Pattern QUOTED = Pattern.compile("'[^']*'|\"[^\"]*\"");
    private static final Pattern PARAM = Pattern.compile("(?<![:\\w]):([A-Za-z_][A-Za-z0-9_]*)");

    /** Named parameters (":Name") of a statement, ignoring quoted identifiers/literals. */
    static Set<String> paramNames(String sql) {
        Set<String> names = new LinkedHashSet<>();
        if (sql == null) return names;
        String s = sql.replaceAll("(?s)/\\*.*?\\*/", " ").replaceAll("--[^\\n]*", " ");
        s = QUOTED.matcher(s).replaceAll(" ");
        Matcher m = PARAM.matcher(s);
        while (m.find()) names.add(m.group(1));
        return names;
    }

    /**
     * The modern services load their SQL from {@code cl/bcs/contabilidad/sql/*.sql}; those resources
     * are missing today, so the fields hold "". To exercise the service LOGIC independently of that
     * defect (which is pinned separately by {@link SqlResourcesTest}), this sets each listed private
     * SQL field to the legacy statement ONLY when the field is blank. Static finals and absent fields
     * are left untouched. Pairs: "FIELD_NAME=LegacyFile.sql".
     */
    static <T> T injectBlank(T service, String... fieldEqFile) {
        for (String pair : fieldEqFile) {
            String[] p = pair.split("=", 2);
            try {
                Field f = service.getClass().getDeclaredField(p[0]);
                if (f.getType() != String.class || Modifier.isStatic(f.getModifiers())) continue;
                f.setAccessible(true);
                Object v = f.get(service);
                if (v == null || v.toString().isBlank()) f.set(service, text(p[1]));
            } catch (NoSuchFieldException e) {
                // renamed field: the implementer presumably loads the resource another way
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        return service;
    }

    static List<String> missingOrDifferent(String... files) {
        List<String> bad = new ArrayList<>();
        for (String f : files) {
            String modern = modernResource(f);
            if (modern == null) bad.add(f + " (missing)");
            else if (!norm(modern).equals(norm(text(f)))) bad.add(f + " (differs from legacy)");
        }
        return bad;
    }
}
