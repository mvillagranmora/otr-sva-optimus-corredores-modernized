package cl.bcs.apv.api;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import cl.bcs.apv.api.LegacyApv.BloqueAnexo;
import cl.bcs.apv.api.LegacyApv.Campo;
import cl.bcs.apv.api.LegacyApv.Clase;
import cl.bcs.apv.api.LegacyApv.FilaAnexo;
import cl.bcs.apv.api.LegacyApv.Operacion;
import cl.bcs.apv.api.comun.Mensaje;

/**
 * Test support: walks, for every invocable legacy operation, the legacy class graph (read by {@link LegacyApv}) and the record graph (read by
 * reflection) in parallel and records every difference. Type mapping rule checked (documented in CONTRATO.md section 1/annex):
 *
 * <pre>
 *  Long, Integer, Double, Float, Short, Byte, Boolean, String, BigDecimal, JsonObject ... -> same type
 *  long, int, double, boolean ... -> same primitive (boxed inside a List)
 *  java.util.Date -> LocalDateTime ; java.sql.Date -> LocalDate ; Timestamp / Map -> not supported (must not occur)
 *  PlataformaUtils.MensajeSalida -> comun.Mensaje
 *  List/ArrayList/array of X -> List of mapped X
 *  APV class reachable from Entrada/Salida -> one record of the operation (same fields, same order, inherited ones flattened, own first)
 *  any other external class (not an APV class) -> JsonObject (opaque payload)
 * </pre>
 */
final class GrafoApv {

    static final String PAQUETE = "cl.bcs.apv.api";
    static final Set<String> ESCALARES = Set.of("Long", "Integer", "Double", "Float", "Short", "Byte", "Boolean", "String", "BigDecimal", "BigInteger",
            "Object", "JsonObject", "JsonElement", "JsonArray", "LocalDate", "LocalDateTime", "Character");
    static final Map<String, String> PRIMITIVOS = Map.of("long", "Long", "int", "Integer", "double", "Double", "boolean", "Boolean", "float", "Float",
            "short", "Short", "byte", "Byte", "char", "Character");

    /** one legacy class paired with the record of an operation that stands for it. */
    record Par(String operacion, String caso, Clase legacy, Class<?> record, List<Campo> campos) {
    }

    final List<String> errores = new ArrayList<>();
    final List<String> erroresAnexo = new ArrayList<>();
    final List<Par> pares = new ArrayList<>();
    final Map<String, Set<Class<?>>> recordsPorCaso = new TreeMap<>();
    final Map<String, String> externos = new TreeMap<>();
    final Set<String> nombresConVariante = new TreeSet<>();
    int bloquesAnexo;
    int filasAnexo;

    private static GrafoApv cache;

    static synchronized GrafoApv get() {
        if (cache == null) {
            cache = new GrafoApv();
            cache.calcular();
        }
        return cache;
    }

    int totalCampos() {
        return pares.stream().mapToInt(p -> p.campos().size()).sum();
    }

    // ------------------------------------------------------------------------------------------------

    private void calcular() {
        Map<String, String> casos = LegacyApv.catalogoNombres();
        Map<String, BloqueAnexo> anexo = new LinkedHashMap<>();
        List<BloqueAnexo> bloques = LegacyApv.anexo();
        bloquesAnexo = bloques.size();
        for (BloqueAnexo b : bloques) {
            filasAnexo += b.filas().size();
            if (anexo.put(b.caso() + "/" + b.record(), b) != null) {
                erroresAnexo.add("annex: block repeated " + b.caso() + "/" + b.record());
            }
        }
        for (Operacion o : LegacyApv.operaciones()) {
            if (o.callNulo) {
                continue;
            }
            String caso = casos.get(o.clase.nombre);
            if (caso == null) {
                errores.add(o.clase.nombre + ": invocable legacy class without a use case name in tools/catalogo.py");
                continue;
            }
            String pkg = PAQUETE + ".dto." + caso.toLowerCase(Locale.ROOT);
            Clase ent = o.entrada == null ? null : LegacyApv.resolver(o.entrada, o.clase);
            Clase sal = o.salida == null ? null : LegacyApv.resolver(o.salida, o.clase);
            if (ent == null || sal == null) {
                errores.add(o.clase.nombre + ": Entrada/Salida class not found in the legacy source (" + o.entrada + " / " + o.salida + ")");
                continue;
            }
            Map<Clase, Class<?>> vis = new LinkedHashMap<>();
            Map<Class<?>, Clase> inv = new LinkedHashMap<>();
            visitar(o, caso, ent, cargar(pkg + "." + caso + "Entrada", o), vis, inv, anexo);
            visitar(o, caso, sal, cargar(pkg + "." + caso + "Resultado", o), vis, inv, anexo);
            recordsPorCaso.computeIfAbsent(caso, k -> new LinkedHashSet<>()).addAll(inv.keySet());
        }
        // annex blocks that no walk consumed
        Set<String> usados = new TreeSet<>();
        for (Par p : pares) {
            usados.add(p.caso() + "/" + p.record().getSimpleName());
        }
        for (String k : anexo.keySet()) {
            if (!usados.contains(k)) {
                erroresAnexo.add("annex: block " + k + " has no legacy class / record behind it");
            }
        }
    }

    private Class<?> cargar(String fqn, Operacion o) {
        try {
            return Class.forName(fqn);
        } catch (ClassNotFoundException e) {
            errores.add(o.clase.nombre + ": record not found " + fqn);
            return null;
        }
    }

    private void visitar(Operacion o, String caso, Clase lc, Class<?> rc, Map<Clase, Class<?>> vis, Map<Class<?>, Clase> inv,
            Map<String, BloqueAnexo> anexo) {
        if (rc == null) {
            return;
        }
        String ctx = o.clase.nombre + ":" + lc.ruta() + "->" + rc.getSimpleName();
        if (vis.containsKey(lc)) {
            if (vis.get(lc) != rc) {
                errores.add(ctx + ": the same legacy class maps to two records (" + vis.get(lc).getSimpleName() + ")");
            }
            return;
        }
        if (inv.containsKey(rc)) {
            errores.add(ctx + ": two legacy classes map to the same record (" + inv.get(rc) + ")");
            return;
        }
        vis.put(lc, rc);
        inv.put(rc, lc);
        if (!rc.isRecord()) {
            errores.add(ctx + ": not a record");
            return;
        }
        if (!rc.getPackageName().equals(PAQUETE + ".dto." + caso.toLowerCase(Locale.ROOT))) {
            errores.add(ctx + ": record in the wrong package " + rc.getPackageName());
        }
        List<Campo> campos = LegacyApv.camposPlanos(lc);
        RecordComponent[] comps = rc.getRecordComponents();
        pares.add(new Par(o.clase.nombre, caso, lc, rc, campos));
        BloqueAnexo bloque = anexo.get(caso + "/" + rc.getSimpleName());
        if (bloque == null) {
            erroresAnexo.add(ctx + ": no block in CONTRATO_CAMPOS.md");
        } else {
            if (bloque.filas().size() != comps.length) {
                erroresAnexo.add(ctx + ": annex has " + bloque.filas().size() + " rows, record has " + comps.length);
            }
            if (!bloque.operacion().equals(o.clase.nombre)) {
                erroresAnexo.add(ctx + ": annex block belongs to operation " + bloque.operacion());
            }
            if (!bloque.legacyClase().equals((lc.archivo.pojo ? "pojo." : "") + lc.ruta())) {
                erroresAnexo.add(ctx + ": annex names legacy class " + bloque.legacyClase());
            }
        }
        if (campos.size() != comps.length) {
            errores.add(ctx + ": legacy has " + campos.size() + " fields, record has " + comps.length);
            return;
        }
        for (int i = 0; i < comps.length; i++) {
            Campo cl = campos.get(i);
            RecordComponent c = comps[i];
            String at = ctx + "#" + i + " " + cl.nombre() + ":" + cl.tipo();
            if (!nombreCoherente(cl.nombre(), c.getName())) {
                errores.add(at + ": record component name '" + c.getName() + "' is not the legacy name '" + cl.nombre() + "' (or a documented variant)");
            } else if (!nombresBase(cl.nombre()).contains(c.getName())) {
                nombresConVariante.add(cl.nombre() + "->" + c.getName());
            }
            Clase hijo = comprobarTipo(at, cl, c, lc);
            if (bloque != null && i < bloque.filas().size()) {
                FilaAnexo f = bloque.filas().get(i);
                if (!f.legacy().equals(cl.nombre()) || !sinBlancos(f.tipoLegacy()).replace("java.util.", "").equals(cl.tipo().replace("java.util.", ""))
                        || !f.nombre().equals(c.getName()) || !f.tipo().equals(descripcion(c.getGenericType()))
                        || !f.card().equals(esLista(c.getGenericType()) ? "0..n" : "1")) {
                    erroresAnexo.add(at + ": annex row " + f + " differs from legacy/record (" + c.getName() + " " + descripcion(c.getGenericType()) + ")");
                }
            }
            if (hijo != null) {
                visitar(o, caso, hijo, elemento(c.getGenericType()), vis, inv, anexo);
            }
        }
    }

    private static String sinBlancos(String s) {
        return s.replaceAll("\\s+", "");
    }

    // ------------------------------------------------------------------------------------------------ types

    private static boolean esLista(Type t) {
        return t instanceof ParameterizedType p && p.getRawType() == List.class;
    }

    static Class<?> elemento(Type t) {
        if (t instanceof ParameterizedType p) {
            if (p.getRawType() != List.class || !(p.getActualTypeArguments()[0] instanceof Class<?>)) {
                return null;
            }
            return (Class<?>) p.getActualTypeArguments()[0];
        }
        return t instanceof Class<?> c ? c : null;
    }

    static String descripcion(Type t) {
        Class<?> e = elemento(t);
        if (e == null) {
            return t.getTypeName();
        }
        return esLista(t) ? "List<" + e.getSimpleName() + ">" : e.getSimpleName();
    }

    /** returns the legacy class the field points to (to recurse) or null. */
    private Clase comprobarTipo(String at, Campo cl, RecordComponent c, Clase ctx) {
        String t = cl.tipo().replace("java.util.", "").replace("java.math.", "").replace("java.sql.", "").replace("java.lang.", "");
        boolean lista = false;
        Matcher ml = Pattern.compile("^(?:List|ArrayList|Collection|LinkedList|Set|HashSet)<(.+)>$").matcher(t);
        if (ml.matches()) {
            lista = true;
            t = ml.group(1);
        } else if (t.endsWith("[]")) {
            lista = true;
            t = t.substring(0, t.length() - 2);
        }
        if (t.contains("<") || t.matches("^(?:Map|HashMap|LinkedHashMap|TreeMap)<.*")) {
            errores.add(at + ": generic/map legacy type has no record representation");
            return null;
        }
        boolean actualLista = esLista(c.getGenericType());
        Class<?> real = elemento(c.getGenericType());
        if (real == null) {
            errores.add(at + ": unexpected component type " + c.getGenericType().getTypeName());
            return null;
        }
        if (lista != actualLista) {
            errores.add(at + ": cardinality differs (legacy list=" + lista + ", record list=" + actualLista + ")");
            return null;
        }
        String esperado;
        Clase ref = null;
        if (PRIMITIVOS.containsKey(t)) {
            esperado = lista ? PRIMITIVOS.get(t) : t;
        } else if (t.equals("Date")) {
            boolean sql = cl.tipo().startsWith("java.sql.") || (!cl.tipo().startsWith("java.util.") && ctx.archivo.imports.contains("java.sql.Date"));
            esperado = sql ? "LocalDate" : "LocalDateTime";
        } else if (t.equals("Timestamp")) {
            errores.add(at + ": Timestamp has no mapping in the contract");
            return null;
        } else if (ESCALARES.contains(t)) {
            esperado = t;
        } else if (t.equals("MensajeSalida")) {
            esperado = "Mensaje";
            if (real != Mensaje.class) {
                errores.add(at + ": MensajeSalida must map to comun.Mensaje, found " + real.getName());
            }
        } else {
            ref = LegacyApv.resolver(t, ctx);
            if (ref != null) {
                esperado = null;
            } else {
                esperado = "JsonObject";
                externos.put(cl.tipo(), at.substring(0, at.indexOf('#')));
            }
        }
        if (esperado != null && !real.getSimpleName().equals(esperado)) {
            errores.add(at + ": type differs, expected " + (lista ? "List<" + esperado + ">" : esperado) + " found " + descripcion(c.getGenericType()));
            return null;
        }
        if (ref != null && !real.isRecord()) {
            errores.add(at + ": legacy class " + ref + " must be a record, found " + real.getName());
            return null;
        }
        return ref;
    }

    // ------------------------------------------------------------------------------------------------ names

    private static final Set<String> JAVA_RESERVADAS = Set.of("abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float", "for", "goto", "if", "implements", "import",
            "instanceof", "int", "interface", "long", "native", "new", "package", "private", "protected", "public", "return", "short", "static",
            "strictfp", "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while", "record", "var",
            "yield", "null", "true", "false", "clone", "finalize", "getClass", "hashCode", "notify", "notifyAll", "toString", "wait");
    private static final Map<String, String> TOKENS = Map.of("MENSAJES", "mensajes", "ERRORESCONTADOR", "contadorErrores", "ADVERTENCIASCONTADOR",
            "contadorAdvertencias");

    static String quitarPrefijo(String legacy) {
        String b = legacy.replaceFirst("^(?:P_OUT_|p_out_|P_|p_)", "");
        return b.isEmpty() ? legacy : b;
    }

    /** names a legacy field may have as a record component without any collision (camelCase rule, or the letters of an ALLCAPS name). */
    static Set<String> nombresBase(String legacy) {
        String b = quitarPrefijo(legacy);
        Set<String> r = new LinkedHashSet<>();
        String plano = b.replace("_", "");
        boolean mayus = plano.matches(".*[A-Z].*") && plano.equals(plano.toUpperCase(Locale.ROOT));
        if (mayus) {
            if (TOKENS.containsKey(b)) {
                r.add(TOKENS.get(b));
            }
            return r;
        }
        String n = b;
        if (n.contains("_")) {
            StringBuilder sb = new StringBuilder();
            for (String x : n.split("_")) {
                if (!x.isEmpty()) {
                    sb.append(Character.toUpperCase(x.charAt(0))).append(x.substring(1));
                }
            }
            n = sb.toString();
        }
        Matcher m = Pattern.compile("^([A-Z]+)([A-Z][a-z].*)$").matcher(n);
        n = m.matches() ? m.group(1).toLowerCase(Locale.ROOT) + m.group(2) : Character.toLowerCase(n.charAt(0)) + n.substring(1);
        r.add(n);
        if (JAVA_RESERVADAS.contains(n)) {
            r.add(n + "Valor");
        }
        return r;
    }

    static boolean nombreCoherente(String legacy, String tipado) {
        Set<String> candidatos = new LinkedHashSet<>(List.of(tipado));
        for (int vuelta = 0; vuelta < 3; vuelta++) {
            for (String c : new ArrayList<>(candidatos)) {
                if (c.endsWith("2")) {
                    candidatos.add(c.substring(0, c.length() - 1));
                }
                for (String suf : List.of("Valor", "Legacy")) {
                    if (c.endsWith(suf) && c.length() > suf.length()) {
                        candidatos.add(c.substring(0, c.length() - suf.length()));
                    }
                }
                if (c.startsWith("salida") && c.length() > 6) {
                    candidatos.add(Character.toLowerCase(c.charAt(6)) + c.substring(7));
                }
            }
        }
        Set<String> base = nombresBase(legacy);
        String b = quitarPrefijo(legacy);
        String plano = b.replace("_", "");
        boolean mayus = plano.matches(".*[A-Z].*") && plano.equals(plano.toUpperCase(Locale.ROOT));
        for (String c : candidatos) {
            if (base.contains(c)) {
                return true;
            }
            if (mayus && !TOKENS.containsKey(b) && c.toUpperCase(Locale.ROOT).equals(plano)) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------------------------------------ filesystem

    /** every .java file under the dto directory of the module, as a class name. */
    static List<String> recordsEnDisco() {
        Path dto = LegacyApv.modulo().resolve("src/main/java/cl/bcs/apv/api/dto");
        try (Stream<Path> s = Files.walk(dto)) {
            return s.filter(p -> p.toString().endsWith(".java")).map(p -> {
                String rel = dto.relativize(p).toString().replace('\\', '/');
                return PAQUETE + ".dto." + rel.substring(0, rel.length() - 5).replace('/', '.');
            }).sorted(Comparator.naturalOrder()).toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
