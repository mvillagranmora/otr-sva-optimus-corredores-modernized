package cl.bcs.plataforma.login.dialecto;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Fixtures and text helpers of the LOGIN-PG tests. A missing fixture is a FAILURE with the word FIXTURE in the message, never a skip: a comparison
 * that cannot run proves nothing.
 */
public final class LoginPgRecursos {

    /** the six classes whose SQL is moved to resources (TODO_MVP.md 10.2); ids are {@code Class.CONSTANT} */
    public static final List<String> CLASES = List.of(
            "cl.bcs.plataforma.login.sesion.CrearSesionServicio",
            "cl.bcs.plataforma.login.sesion.SesionDatos",
            "cl.bcs.plataforma.login.sesion.AuditoriaSesion",
            "cl.bcs.plataforma.login.sesion.FiltroSesion",
            "cl.bcs.plataforma.login.clave.GestionClaveServicio",
            "cl.bcs.plataforma.login.bootstrap.BootstrapServicio");

    /** constants of those classes that are SQL (or object names handed to the call layer): the rest ({@code CANAL_*}, header names) is not */
    public static final Pattern CONSTANTE_SQL = Pattern.compile("^(SQL|QUERY|INSERT|UPDATE|CLOSE|SP|SECUENCIA)_[A-Z0-9_]+$");

    private LoginPgRecursos() {
    }

    // ------------------------------------------------------------------------------------------------ loading

    public static String texto(String ruta) {
        try (InputStream in = LoginPgRecursos.class.getResourceAsStream(ruta)) {
            if (in == null) {
                fail("FIXTURE MISSING: classpath resource " + ruta + " (a comparison that cannot run is a failure, not a skip)");
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new AssertionError("FIXTURE UNREADABLE: " + ruta, e);
        }
    }

    public static boolean hay(String ruta) {
        return LoginPgRecursos.class.getResource(ruta) != null;
    }

    /** the production resource: missing means the production side is not implemented yet (red, with the standard message) */
    public static String textoProduccion(String ruta) {
        try (InputStream in = LoginPgRecursos.class.getResourceAsStream(ruta)) {
            if (in == null) {
                throw new AssertionError("LOGIN-PG not implemented: the production resource " + ruta + " does not exist yet");
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new AssertionError("LOGIN-PG production resource unreadable: " + ruta, e);
        }
    }

    public static List<List<String>> csv(String texto) {
        List<List<String>> filas = new ArrayList<>();
        List<String> fila = new ArrayList<>();
        StringBuilder celda = new StringBuilder();
        boolean comillas = false;
        boolean hayCelda = false;
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (comillas) {
                if (c == '"') {
                    if (i + 1 < texto.length() && texto.charAt(i + 1) == '"') {
                        celda.append('"');
                        i++;
                    } else {
                        comillas = false;
                    }
                } else {
                    celda.append(c);
                }
            } else if (c == '"') {
                comillas = true;
                hayCelda = true;
            } else if (c == ',') {
                fila.add(celda.toString());
                celda.setLength(0);
                hayCelda = true;
            } else if (c == '\n' || c == '\r') {
                if (c == '\r' && i + 1 < texto.length() && texto.charAt(i + 1) == '\n') i++;
                if (hayCelda || celda.length() > 0) {
                    fila.add(celda.toString());
                    filas.add(fila);
                }
                fila = new ArrayList<>();
                celda.setLength(0);
                hayCelda = false;
            } else {
                celda.append(c);
                hayCelda = true;
            }
        }
        if (hayCelda || celda.length() > 0) {
            fila.add(celda.toString());
            filas.add(fila);
        }
        return filas;
    }

    // ------------------------------------------------------------------------------------------------ the independent oracle (from the xlsx)

    public record Objeto(String tipo, String hana, String postgres) {
    }

    public static final class Oraculo {
        public final Map<String, Objeto> objetos = new LinkedHashMap<>();
        /** HANA table -> (HANA column -> Postgres column) */
        public final Map<String, Map<String, String>> columnas = new LinkedHashMap<>();
        /** HANA table -> Postgres table, as the column sheet names it */
        public final Map<String, String> tablaPgDeColumnas = new LinkedHashMap<>();
        /** the names the production code cites (login-alcance.txt) */
        public final List<Objeto> alcance = new ArrayList<>();

        public static Oraculo cargar() {
            Oraculo o = new Oraculo();
            List<List<String>> obj = csv(texto("/loginpg/oraculo-objetos.csv"));
            for (List<String> f : obj.subList(1, obj.size())) {
                o.objetos.put(f.get(1), new Objeto(f.get(0), f.get(1), f.get(2)));
            }
            List<List<String>> col = csv(texto("/loginpg/oraculo-columnas.csv"));
            for (List<String> f : col.subList(1, col.size())) {
                o.columnas.computeIfAbsent(f.get(0), k -> new LinkedHashMap<>()).put(f.get(2), f.get(3));
                o.tablaPgDeColumnas.put(f.get(0), f.get(1));
            }
            for (String linea : texto("/loginpg/login-alcance.txt").split("\n")) {
                if (linea.isBlank()) continue;
                String[] p = linea.split("\t");
                o.alcance.add(new Objeto(p[0], p[1], p[2]));
            }
            return o;
        }

        public Objeto objeto(String hana) {
            Objeto x = objetos.get(hana);
            if (x == null) fail("oracle has no object " + hana);
            return x;
        }

        /** the object of a token {@code ACC.Usuario}: the unique HANA name that ends in {@code ::ACC.Usuario} */
        public String hanaDeFicha(String ficha) {
            String hallado = null;
            for (String n : objetos.keySet()) {
                if (n.endsWith("::" + ficha)) {
                    if (hallado != null) fail("ambiguous token " + ficha);
                    hallado = n;
                }
            }
            if (hallado == null) fail("oracle has no object for token " + ficha);
            return hallado;
        }
    }

    // ------------------------------------------------------------------------------------------------ golden SQL of the HANA code

    private static final String GOLDEN = "/loginpg/golden-hana/";

    /** id -> exact text of the constant, in id order */
    public static Map<String, String> goldenSql() {
        Map<String, String> m = new LinkedHashMap<>();
        for (String id : texto(GOLDEN + "INDEX.txt").split("\n")) {
            if (id.isBlank()) continue;
            m.put(id, texto(GOLDEN + id + ".sql"));
        }
        return m;
    }

    /** the golden of the statements that are SQL (not the SP_ / SECUENCIA_ object names) */
    public static Map<String, String> goldenSentencias() {
        Map<String, String> m = new LinkedHashMap<>();
        goldenSql().forEach((k, v) -> {
            String c = k.substring(k.indexOf('.') + 1);
            if (!c.startsWith("SP_") && !c.startsWith("SECUENCIA_")) m.put(k, v);
        });
        return m;
    }

    public static Map<String, List<String>> goldenFlujos() {
        com.google.gson.JsonObject o = com.google.gson.JsonParser.parseString(texto(GOLDEN + "enviado-por-flujo.json")).getAsJsonObject();
        Map<String, List<String>> m = new LinkedHashMap<>();
        for (String k : o.keySet()) {
            List<String> l = new ArrayList<>();
            o.getAsJsonArray(k).forEach(e -> l.add(e.getAsString()));
            m.put(k, l);
        }
        return m;
    }

    // ------------------------------------------------------------------------------------------------ SQL text helpers

    private static final Pattern NOMBRE_HANA = Pattern.compile("\"(BSCL\\.[A-Za-z.]*::[A-Za-z.]*)\"");

    /** full HANA names between double quotes */
    public static Set<String> objetosHanaDe(String sql) {
        Set<String> s = new LinkedHashSet<>();
        Matcher m = NOMBRE_HANA.matcher(sql);
        while (m.find()) s.add(m.group(1));
        return s;
    }

    /** the {@code :name} parameters outside quotes, in order of appearance, without repeats */
    public static List<String> parametros(String sql) {
        List<String> r = new ArrayList<>();
        boolean simple = false, doble = false;
        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (simple) {
                if (c == '\'') simple = false;
            } else if (doble) {
                if (c == '"') doble = false;
            } else if (c == '\'') {
                simple = true;
            } else if (c == '"') {
                doble = true;
            } else if (c == ':' && i + 1 < sql.length() && Character.isJavaIdentifierStart(sql.charAt(i + 1))) {
                int j = i + 2;
                while (j < sql.length() && Character.isJavaIdentifierPart(sql.charAt(j))) j++;
                String n = sql.substring(i + 1, j);
                if (!r.contains(n)) r.add(n);
                i = j - 1;
            }
        }
        return r;
    }

    /** whitespace collapsed to one space, trimmed: for the literal comparisons of statements that only differ in layout */
    public static String normalizar(String sql) {
        return sql.replaceAll("\\s+", " ").trim();
    }

    private static final Pattern IDENT_ENTRE_COMILLAS = Pattern.compile("\"([^\"]+)\"");

    /** quoted identifiers that are not object names, with the number of times each appears not preceded by AS (a use) and preceded by AS (a definition) */
    public static Map<String, int[]> identificadoresHana(String sql) {
        Map<String, int[]> m = new TreeMap<>();
        Matcher mt = IDENT_ENTRE_COMILLAS.matcher(sql);
        while (mt.find()) {
            String n = mt.group(1);
            if (n.contains("::")) continue;
            String antes = sql.substring(0, mt.start()).stripTrailing();
            boolean alias = antes.length() >= 2 && antes.substring(antes.length() - 2).equalsIgnoreCase("as")
                    && (antes.length() == 2 || !Character.isLetterOrDigit(antes.charAt(antes.length() - 3)));
            // a table alias without AS: it follows a HANA object name ("BSCL...::T" "ALIAS") or a Postgres table name (t_x "ALIAS")
            if (!alias && (antes.matches("(?s).*\"[^\"]*::[^\"]*\"$") || antes.matches("(?s).*\\b[tv]_[a-z0-9_]+$"))) alias = true;
            m.computeIfAbsent(n, k -> new int[2])[alias ? 1 : 0]++;
        }
        return m;
    }

    /** a HANA token that must not survive in Postgres text */
    public static final List<Pattern> FICHAS_HANA = List.of(
            Pattern.compile("::"),
            Pattern.compile("BSCL", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bCURRENT_UTCTIMESTAMP\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bTOP\\s+\\d+", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bDUMMY\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bNEXTVAL\\b\\s*(?!\\()", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bADD_SECONDS\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bTO_VARCHAR\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bTO_INT\\b", Pattern.CASE_INSENSITIVE));

    // ------------------------------------------------------------------------------------------------ module sources (for scope checks)

    public static Path moduloPrincipal() {
        Path p = Path.of("src", "main", "java");
        if (!Files.isDirectory(p)) {
            fail("FIXTURE MISSING: " + p.toAbsolutePath() + " (the tests run with the module as working directory)");
        }
        return p;
    }

    /** The HANA names the production code cites: in its Java sources and, since T-31 moved the statements out of the classes, in the HANA statement resources. */
    public static Set<String> nombresHanaEnProduccion() {
        Set<String> r = new java.util.TreeSet<>();
        Pattern pt = Pattern.compile("BSCL\\.[A-Za-z.]*::[A-Za-z.]*");
        Path recursos = Path.of("src", "main", "resources", "sql", "hana");
        try {
            try (Stream<Path> s = Files.walk(moduloPrincipal())) {
                for (Path f : (Iterable<Path>) s.filter(x -> x.toString().endsWith(".java"))::iterator) {
                    Matcher m = pt.matcher(Files.readString(f, StandardCharsets.UTF_8));
                    while (m.find()) r.add(m.group());
                }
            }
            if (Files.isDirectory(recursos)) {
                try (Stream<Path> s = Files.walk(recursos)) {
                    for (Path f : (Iterable<Path>) s.filter(x -> x.toString().endsWith(".sql"))::iterator) {
                        Matcher m = pt.matcher(Files.readString(f, StandardCharsets.UTF_8));
                        while (m.find()) r.add(m.group());
                    }
                }
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return r;
    }
}
