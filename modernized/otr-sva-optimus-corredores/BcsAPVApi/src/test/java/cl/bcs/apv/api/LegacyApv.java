package cl.bcs.apv.api;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Test support: INDEPENDENT reader of the legacy {@code BcsAPVServicios} sources (and of the module annexes). It shares no code with
 * {@code tools/legacy_parser.py}: it is a second, much smaller reading of the same {@code .java} files, so that a defect of the generator's
 * parser (or of the generator's type rules) shows up as a difference instead of being reproduced by the test.
 *
 * <p>What it reads: classes (nested, any depth), their instance fields (type text and name, declaration order), the superclass, the imports and,
 * for the {@code JavaCall} classes, the Entrada class used by {@code call()} ({@code fromJson(..., X.class)}) and the Salida class (the type
 * returned by {@code logica(...)}). Comments and string/char literals are blanked first. It does NOT interpret initialisers or constructors
 * (defaults are not part of what these tests pin).
 */
final class LegacyApv {

    static final Pattern CLASE = Pattern.compile(
            "(?:(?:public|private|protected|static|abstract|final)\\s+)*class\\s+(\\w+)(?:\\s+extends\\s+([\\w.]+))?(?:\\s+implements\\s+([^{]+?))?");

    private LegacyApv() {
    }

    /** One instance field: type text without blanks and name. */
    record Campo(String tipo, String nombre) {
    }

    static final class Archivo {
        final String nombre;
        final boolean pojo;
        final String texto;
        final List<String> imports = new ArrayList<>();
        final Clase raiz = new Clase("", null, null);

        Archivo(String nombre, boolean pojo, String texto) {
            this.nombre = nombre;
            this.pojo = pojo;
            this.texto = texto;
            raiz.archivo = this;
        }
    }

    static final class Clase {
        final String nombre;
        final String extiende;
        final String implementa;
        Archivo archivo;
        Clase padre;
        final List<Campo> campos = new ArrayList<>();
        final List<Clase> anidadas = new ArrayList<>();

        Clase(String nombre, String extiende, String implementa) {
            this.nombre = nombre;
            this.extiende = extiende;
            this.implementa = implementa == null ? "" : implementa;
        }

        /** Path of nested names, e.g. {@code AsignarContratoARecaudacion.ParametrosEntradaServicio}. */
        String ruta() {
            return padre == null || padre.nombre.isEmpty() ? nombre : padre.ruta() + "." + nombre;
        }

        @Override
        public String toString() {
            return ruta();
        }
    }

    /** A legacy operation: top-level class that implements JavaCall. */
    static final class Operacion {
        final Clase clase;
        boolean callNulo;
        String entrada;
        String salida;

        Operacion(Clase clase) {
            this.clase = clase;
        }
    }

    // ------------------------------------------------------------------------------------------------ locating the sources

    static Path modulo() {
        String base = System.getProperty("basedir");
        return Path.of(base != null ? base : System.getProperty("user.dir")).toAbsolutePath().normalize();
    }

    /** {@code APV_LEGACY_SRC} or the repository layout; FAILS (never skips) when the legacy sources are not there. */
    static Path fuentesLegacy() {
        String env = System.getenv("APV_LEGACY_SRC");
        Path p = env != null && !env.isBlank() ? Path.of(env)
                : modulo().resolve("../../../legacy/otr-sva-optimus-corredores/BSCL/APV/JAVA/BcsAPVServicios/src/cl/bcs/APV").normalize();
        if (!Files.isDirectory(p)) {
            throw new AssertionError("legacy sources not found (a test of the contract cannot be skipped): " + p);
        }
        return p;
    }

    static String leer(Path p) {
        try {
            return Files.readString(p, StandardCharsets.UTF_8).replace("\r\n", "\n");
        } catch (IOException e) {
            throw new UncheckedIOException(p.toString(), e);
        }
    }

    // ------------------------------------------------------------------------------------------------ reading

    private static Map<String, Archivo> cache;

    /** simple name (or {@code pojo/Name}) to parsed file, in alphabetical order. */
    static synchronized Map<String, Archivo> archivos() {
        if (cache != null) {
            return cache;
        }
        Path dir = fuentesLegacy();
        Map<String, Archivo> r = new TreeMap<>();
        try (Stream<Path> s = Files.list(dir)) {
            for (Path f : (Iterable<Path>) s.filter(x -> x.getFileName().toString().endsWith(".java")).sorted()::iterator) {
                String n = f.getFileName().toString();
                r.put(n.substring(0, n.length() - 5), leerArchivo(n.substring(0, n.length() - 5), false, leer(f)));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        try (Stream<Path> s = Files.list(dir.resolve("pojo"))) {
            for (Path f : (Iterable<Path>) s.filter(x -> x.getFileName().toString().endsWith(".java")).sorted()::iterator) {
                String n = f.getFileName().toString();
                r.put("pojo/" + n.substring(0, n.length() - 5), leerArchivo(n.substring(0, n.length() - 5), true, leer(f)));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        cache = r;
        return r;
    }

    static Archivo leerArchivo(String nombre, boolean pojo, String original) {
        String m = enmascarar(original);
        Archivo a = new Archivo(nombre, pojo, m);
        Matcher im = Pattern.compile("(?m)^\\s*import\\s+(?:static\\s+)?([\\w.*]+)\\s*;").matcher(m);
        while (im.find()) {
            a.imports.add(im.group(1));
        }
        cuerpo(m, 0, m.length(), a.raiz);
        return a;
    }

    /** blanks comments and string/char literals (newlines kept). */
    static String enmascarar(String s) {
        char[] o = s.toCharArray();
        int n = o.length;
        int i = 0;
        while (i < n) {
            char c = o[i];
            if (c == '/' && i + 1 < n && o[i + 1] == '/') {
                while (i < n && o[i] != '\n') {
                    o[i++] = ' ';
                }
            } else if (c == '/' && i + 1 < n && o[i + 1] == '*') {
                int j = i + 2;
                while (j + 1 < n && !(o[j] == '*' && o[j + 1] == '/')) {
                    j++;
                }
                for (int k = i; k < Math.min(n, j + 2); k++) {
                    if (o[k] != '\n') {
                        o[k] = ' ';
                    }
                }
                i = j + 2;
            } else if (c == '"' || c == '\'') {
                int j = i + 1;
                while (j < n && o[j] != c) {
                    if (o[j] == '\\') {
                        j++;
                    }
                    j++;
                }
                for (int k = i + 1; k < Math.min(n, j); k++) {
                    if (o[k] != '\n') {
                        o[k] = ' ';
                    }
                }
                i = j + 1;
            } else {
                i++;
            }
        }
        return new String(o);
    }

    private static int cierre(String m, int abre) {
        int d = 0;
        for (int i = abre; i < m.length(); i++) {
            char c = m.charAt(i);
            if (c == '{') {
                d++;
            } else if (c == '}' && --d == 0) {
                return i;
            }
        }
        return m.length() - 1;
    }

    private static void cuerpo(String m, int lo, int hi, Clase c) {
        StringBuilder cab = new StringBuilder();
        int i = lo;
        while (i < hi) {
            char ch = m.charAt(i);
            if (ch == ';') {
                declaracion(cab.toString(), c);
                cab.setLength(0);
                i++;
            } else if (ch == '{') {
                int e = cierre(m, i);
                String h = cab.toString().trim();
                if (h.contains("=")) {
                    cab.append(" {} ");
                } else {
                    Matcher mm = CLASE.matcher(sinAnotaciones(h));
                    if (mm.matches()) {
                        Clase n = new Clase(mm.group(1), mm.group(2), mm.group(3));
                        n.archivo = c.archivo;
                        n.padre = c;
                        c.anidadas.add(n);
                        cuerpo(m, i + 1, e, n);
                    }
                    cab.setLength(0);
                }
                i = e + 1;
            } else {
                cab.append(ch);
                i++;
            }
        }
    }

    private static String sinAnotaciones(String h) {
        return h.replaceAll("@\\w+(?:\\([^)]*\\))?", " ").trim();
    }

    private static int igualTope(String s) {
        int d = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '<') {
                d++;
            } else if (c == '>') {
                d--;
            } else if (c == '=' && d == 0) {
                return i;
            }
        }
        return -1;
    }

    private static void declaracion(String cabecera, Clase c) {
        String h = sinAnotaciones(cabecera);
        if (h.isEmpty()) {
            return;
        }
        int eq = igualTope(h);
        String decl = (eq < 0 ? h : h.substring(0, eq)).trim();
        if (decl.contains("(") || decl.startsWith("package ") || decl.startsWith("import ")) {
            return;
        }
        List<String> toks = new ArrayList<>(List.of(decl.split("\\s+")));
        boolean estatico = false;
        boolean transitorio = false;
        while (!toks.isEmpty() && List.of("public", "private", "protected", "static", "final", "transient", "volatile", "abstract").contains(toks.get(0))) {
            String t = toks.remove(0);
            estatico |= t.equals("static");
            transitorio |= t.equals("transient");
        }
        if (estatico || transitorio || toks.size() < 2) {
            return;
        }
        String resto = String.join(" ", toks);
        List<String> partes = new ArrayList<>();
        int d = 0;
        StringBuilder cur = new StringBuilder();
        for (char ch : resto.toCharArray()) {
            if (ch == '<') {
                d++;
            } else if (ch == '>') {
                d--;
            }
            if (ch == ',' && d == 0) {
                partes.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(ch);
            }
        }
        partes.add(cur.toString());
        Matcher t0 = Pattern.compile("^(.*?)\\s*(\\w+)\\s*$", Pattern.DOTALL).matcher(partes.get(0).trim());
        if (!t0.matches() || t0.group(1).isBlank()) {
            return;
        }
        String tipo = t0.group(1).replaceAll("\\s+", "");
        c.campos.add(new Campo(tipo, t0.group(2)));
        for (int k = 1; k < partes.size(); k++) {
            c.campos.add(new Campo(tipo, partes.get(k).trim()));
        }
    }

    // ------------------------------------------------------------------------------------------------ queries

    static List<Clase> todasLasClases(Archivo a) {
        List<Clase> r = new ArrayList<>();
        rec(a.raiz, r);
        return r;
    }

    private static void rec(Clase c, List<Clase> acc) {
        for (Clase n : c.anidadas) {
            acc.add(n);
            rec(n, acc);
        }
    }

    /** Top-level classes (one per file in the legacy). */
    static Clase superior(String simple) {
        Archivo a = archivos().get(simple);
        if (a == null) {
            return null;
        }
        for (Clase c : a.raiz.anidadas) {
            if (c.nombre.equals(simple)) {
                return c;
            }
        }
        return null;
    }

    /** Every JavaCall class, invocable or not, alphabetical. */
    static List<Operacion> operaciones() {
        List<Operacion> r = new ArrayList<>();
        for (Map.Entry<String, Archivo> e : archivos().entrySet()) {
            if (e.getValue().pojo) {
                continue;
            }
            Clase c = superior(e.getKey());
            if (c == null || !Pattern.compile("\\bJavaCall\\b").matcher(c.implementa).find()) {
                continue;
            }
            Operacion o = new Operacion(c);
            String m = e.getValue().texto;
            Matcher mc = Pattern.compile("JsonObject\\s+call\\s*\\(").matcher(m);
            if (!mc.find()) {
                throw new AssertionError("no call() in " + c.nombre);
            }
            int ab = m.indexOf('{', mc.end());
            String cuerpo = m.substring(ab + 1, cierre(m, ab));
            o.callNulo = cuerpo.replaceAll("\\s+", "").equals("returnnull;");
            if (!o.callNulo) {
                Matcher f = Pattern.compile("fromJson\\s*\\([^;]*?,\\s*([\\w.]+?)\\.class").matcher(cuerpo);
                o.entrada = f.find() ? f.group(1) : null;
                Matcher l = Pattern.compile("([\\w.]+)\\s+\\w+\\s*=\\s*logica\\s*\\(").matcher(cuerpo);
                o.salida = l.find() ? l.group(1) : null;
            }
            r.add(o);
        }
        return r;
    }

    /** Resolves a (possibly qualified) type name used inside {@code ctx}; null when it is not a legacy APV class. */
    static Clase resolver(String nombre, Clase ctx) {
        String simple = nombre.substring(nombre.lastIndexOf('.') + 1);
        // 1. same file: the closest class of that name (siblings and enclosing classes first)
        List<Clase> locales = new ArrayList<>();
        for (Clase c : todasLasClases(ctx.archivo)) {
            if (c.nombre.equals(simple)) {
                locales.add(c);
            }
        }
        if (!locales.isEmpty()) {
            Clase mejor = locales.get(0);
            int mejorPuntos = -1;
            for (Clase c : locales) {
                int pts = 0;
                for (Clase p = ctx; p != null; p = p.padre) {
                    if (c.padre == p) {
                        pts = 10 - depth(ctx, p);
                        break;
                    }
                }
                if (pts > mejorPuntos) {
                    mejorPuntos = pts;
                    mejor = c;
                }
            }
            return mejor;
        }
        // 2. imports of cl.bcs.APV.* (pojo or other operation class)
        for (String imp : ctx.archivo.imports) {
            if (imp.startsWith("cl.bcs.APV.") && imp.endsWith("." + simple)) {
                String[] p = imp.substring("cl.bcs.APV.".length()).split("\\.");
                String archivo = p[0].equals("pojo") ? "pojo/" + p[1] : p[0];
                Archivo a = archivos().get(archivo);
                if (a != null) {
                    for (Clase c : todasLasClases(a)) {
                        if (c.nombre.equals(simple)) {
                            return c;
                        }
                    }
                }
            }
        }
        // 3. a top-level class of the package with that name
        Clase top = superior(simple);
        if (top != null) {
            return top;
        }
        Archivo pojo = archivos().get("pojo/" + simple);
        if (pojo != null) {
            for (Clase c : pojo.raiz.anidadas) {
                if (c.nombre.equals(simple)) {
                    return c;
                }
            }
        }
        // 4. qualified Outer.Inner
        if (nombre.contains(".")) {
            String[] p = nombre.split("\\.");
            Archivo a = archivos().get(p[0]);
            if (a != null) {
                for (Clase c : todasLasClases(a)) {
                    if (c.nombre.equals(simple)) {
                        return c;
                    }
                }
            }
        }
        return null;
    }

    private static int depth(Clase desde, Clase hasta) {
        int d = 0;
        for (Clase p = desde; p != null && p != hasta; p = p.padre) {
            d++;
        }
        return d;
    }

    /** Gson-visible fields: own first, then the superclass chain (BcsPojo carries none). */
    static List<Campo> camposPlanos(Clase c) {
        List<Campo> r = new ArrayList<>(c.campos);
        if (c.extiende != null && !c.extiende.equals("BcsPojo")) {
            Clase sup = resolver(c.extiende, c);
            if (sup == null) {
                throw new AssertionError("superclass not found: " + c.extiende + " of " + c);
            }
            r.addAll(camposPlanos(sup));
        }
        return r;
    }

    // ------------------------------------------------------------------------------------------------ annex reader (CONTRATO_CAMPOS.md)

    /** one table of the annex. */
    record BloqueAnexo(String operacion, String caso, String rol, String legacyClase, String record, List<FilaAnexo> filas) {
    }

    record FilaAnexo(String legacy, String tipoLegacy, String card, String nombre, String tipo) {
    }

    static List<BloqueAnexo> anexo() {
        List<BloqueAnexo> r = new ArrayList<>();
        String op = null;
        String caso = null;
        BloqueAnexo actual = null;
        Pattern pOp = Pattern.compile("^### `cl\\.bcs\\.APV\\.(\\w+)` to `(\\w+)` \\(.*$");
        Pattern pBl = Pattern.compile("^\\*\\*(Entrada|Salida|nested)\\*\\* `([^`]+)` to record `([^`]+)`\\s*$");
        Pattern pFila = Pattern.compile("^\\| `([^`]+)` \\| `([^`]+)` \\| (\\S+) \\| (?:`.*?`|\\S*) \\| `([^`]+)` \\| `([^`]+)` \\|.*\\|\\s*$");
        for (String l : leer(modulo().resolve("CONTRATO_CAMPOS.md")).split("\n")) {
            Matcher m = pOp.matcher(l);
            if (m.matches()) {
                op = m.group(1);
                caso = m.group(2);
                continue;
            }
            m = pBl.matcher(l);
            if (m.matches()) {
                actual = new BloqueAnexo(op, caso, m.group(1), m.group(2), m.group(3), new ArrayList<>());
                r.add(actual);
                continue;
            }
            m = pFila.matcher(l);
            if (m.matches() && actual != null) {
                actual.filas().add(new FilaAnexo(m.group(1), m.group(2), m.group(3), m.group(4), m.group(5)));
            }
        }
        return r;
    }

    static Map<String, String> catalogoNombres() {
        Map<String, String> r = new LinkedHashMap<>();
        Matcher m = Pattern.compile("(?m)^\\s*'(\\w+)': \\('([ABC])', ('?\\w+'?),").matcher(leer(modulo().resolve("tools/catalogo.py")));
        while (m.find()) {
            r.put(m.group(1), m.group(3).equals("None") ? null : m.group(3).replace("'", ""));
        }
        return r;
    }
}
