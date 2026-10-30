import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * ORACLE of the legacy side of BcsAPVApi (modernize-verify, same-behavior and fresh-input evidence). It loads the REAL compiled legacy classes
 * ({@code cl.bcs.APV.*}, compiled from legacy/ into a temporary directory by compilar_legacy.sh) and, for each of the 64 invocable operations:
 * <ul>
 *   <li>{@code defecto &lt;ops.json&gt; &lt;outDir&gt;}: instantiates the Entrada and the Salida with the legacy no-arg constructor and serialises them with the Gson
 *       that the legacy {@code call()} uses (plain {@code new Gson()} or the real {@code Utils.getGsonBuilder()}): {@code outDir/<Caso>/entrada.json, salida.json}.</li>
 *   <li>{@code entradas &lt;ops.json&gt; &lt;inDir&gt;}: builds the fresh INPUTS (profiles P1 populated, P2 limits, P3 partial/extra/null keys) by filling the real legacy
 *       classes by reflection and serialising them with the legacy Gson: {@code inDir/<id>.json} and {@code inDir/index.json}.</li>
 *   <li>{@code ejecutar &lt;ops.json&gt; &lt;inDir&gt; &lt;outDir&gt;}: for every input, {@code gson.fromJson(text, Entrada|Salida.class)} then {@code gson.toJson(obj)}, exactly what
 *       {@code call()} does on its way in and out; {@code outDir/<id>/out.json}; an input the legacy rejects (exception) is listed in {@code outDir/rechazadas.txt}.</li>
 * </ul>
 * Nothing here touches a database, the network or legacy/ (read only classes).
 */
public class LegacyApvOracle {

    static final Gson PARSER = new Gson();

    public static void main(String[] args) throws Exception {
        String modo = args[0];
        JsonObject ops = JsonParser.parseString(Files.readString(Path.of(args[1]), StandardCharsets.UTF_8)).getAsJsonObject();
        switch (modo) {
            case "defecto" -> defecto(ops, Path.of(args[2]));
            case "entradas" -> entradas(ops, Path.of(args[2]));
            case "ejecutar" -> ejecutar(ops, Path.of(args[2]), Path.of(args[3]));
            default -> throw new IllegalArgumentException(modo);
        }
    }

    static Gson gsonDe(JsonObject op) throws Exception {
        String g = op.get("gson").getAsString();
        if (g.equals("plain")) {
            return new Gson();
        }
        // the real thing: cl.bcs.utils.Utils.getGsonBuilder()
        return (Gson) Class.forName("cl.bcs.utils.Utils").getMethod("getGsonBuilder").invoke(null);
    }

    static void escribir(Path p, String texto) throws IOException {
        Files.createDirectories(p.getParent());
        Files.writeString(p, texto, StandardCharsets.UTF_8);
    }

    // ------------------------------------------------------------------------------------------------ defecto
    static void defecto(JsonObject ops, Path out) throws Exception {
        JsonArray index = new JsonArray();
        for (JsonElement e : ops.getAsJsonArray("operaciones")) {
            JsonObject op = e.getAsJsonObject();
            Gson gson = gsonDe(op);
            JsonObject fila = new JsonObject();
            fila.addProperty("caso", op.get("caso").getAsString());
            fila.addProperty("gson", op.get("gson").getAsString());
            for (String lado : new String[] { "entrada", "salida" }) {
                Class<?> c = Class.forName(op.get(lado).getAsString());
                fila.addProperty(lado + "Clase", c.getName());
                Object o;
                String origen;
                try {
                    Constructor<?> k = c.getDeclaredConstructor();
                    k.setAccessible(true);
                    o = k.newInstance();
                    origen = "constructor-sin-argumentos";
                } catch (NoSuchMethodException ex) {
                    // legacy never calls `new` on it: Gson (as call() does with {}) would use Unsafe: record the fact, golden comes from fromJson("{}")
                    o = gson.fromJson("{}", c);
                    origen = "gson-sin-constructor(unsafe)";
                }
                fila.addProperty(lado + "Origen", origen);
                String json = gson.toJson(o);
                // the Entrada as call() builds it from an empty request must be the same object state
                if (lado.equals("entrada") && origen.startsWith("constructor")) {
                    String desdeVacio = gson.toJson(gson.fromJson("{}", c));
                    fila.addProperty("entradaIgualFromJsonVacio", desdeVacio.equals(json));
                }
                escribir(out.resolve(op.get("caso").getAsString()).resolve(lado + ".json"), json);
            }
            index.add(fila);
        }
        JsonObject raiz = new JsonObject();
        raiz.add("operaciones", index);
        escribir(out.resolve("index.json"), new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(raiz) + "\n");
        System.out.println("defecto: " + index.size() + " operaciones -> " + out);
    }

    // ------------------------------------------------------------------------------------------------ populate (reflection over the real legacy classes)
    enum Perfil { P1, P2 }

    static final class Estado {
        final Perfil perfil;
        int n = 0;

        Estado(Perfil p) {
            perfil = p;
        }
    }

    static final long[] LARGOS = { Long.MAX_VALUE, Long.MIN_VALUE, 0L, -1L, 9007199254740993L };
    static final int[] ENTEROS = { Integer.MAX_VALUE, Integer.MIN_VALUE, 0, -1, 2147483646 };
    static final double[] DOBLES = { Double.MIN_VALUE, Double.MAX_VALUE, -0.0, 1.0E-7, 123456789.123456789, 100.0, 1.0E21 };
    static final String[] DECIMALES = { "0.000001", "1E+3", "-0.00", "12345678901234567890.123456789012345", "1.10", "0E-10", "100" };
    static final String[] TEXTOS = { "", " ", "x".repeat(300), "ñandú Ω 😀 \"q\" \\ /<>&='", "l1\nl2\ttab", "0", "null", "  espacios  " };

    static Object poblar(Type t, String campo, Estado st, int prof) throws Exception {
        int n = st.n++;
        boolean p2 = st.perfil == Perfil.P2;
        Class<?> raw = t instanceof Class<?> c ? c : t instanceof ParameterizedType pt ? (Class<?>) pt.getRawType() : null;
        if (raw == null) {
            return null;
        }
        if (raw == Long.class || raw == long.class) {
            return p2 ? LARGOS[n % LARGOS.length] : (Long) (1000L + n);
        }
        if (raw == Integer.class || raw == int.class) {
            return p2 ? ENTEROS[n % ENTEROS.length] : (Integer) (100 + n);
        }
        if (raw == Short.class || raw == short.class) {
            return (short) (p2 ? Short.MAX_VALUE : 7 + n % 10);
        }
        if (raw == Byte.class || raw == byte.class) {
            return (byte) (p2 ? Byte.MIN_VALUE : 3 + n % 10);
        }
        if (raw == Double.class || raw == double.class) {
            return p2 ? (Double) DOBLES[n % DOBLES.length] : (Double) (n + 0.25);
        }
        if (raw == Float.class || raw == float.class) {
            return p2 ? (Float) 1.5E-5f : (Float) (n + 0.5f);
        }
        if (raw == Boolean.class || raw == boolean.class) {
            return p2 ? (n % 2 == 0) : (n % 3 != 0);
        }
        if (raw == Character.class || raw == char.class) {
            return 'Z';
        }
        if (raw == String.class) {
            return p2 ? TEXTOS[n % TEXTOS.length] : "v" + n + "-" + campo;
        }
        if (raw == BigDecimal.class) {
            return p2 ? new BigDecimal(DECIMALES[n % DECIMALES.length]) : new BigDecimal(new String[] { "12345.67", "100", "0.5", "7.250" }[n % 4]);
        }
        if (raw == BigInteger.class) {
            return p2 ? new BigInteger("123456789012345678901234567890") : BigInteger.valueOf(500 + n);
        }
        if (raw == java.sql.Timestamp.class) {
            return new java.sql.Timestamp(p2 ? 0L : 1700000000000L + n * 86400000L);
        }
        if (raw == java.sql.Date.class) {
            return p2 ? java.sql.Date.valueOf(n % 2 == 0 ? "0001-01-01" : "9999-12-31") : java.sql.Date.valueOf("2024-03-" + String.format("%02d", 1 + n % 28));
        }
        if (raw == Date.class) {
            return p2 ? new Date(n % 2 == 0 ? 0L : 253402300799000L) : new Date(1700000000000L + n * 86400000L);
        }
        if (raw == Object.class) {
            return "objeto-" + n;
        }
        if (raw.isArray()) {
            Class<?> el = raw.getComponentType();
            int len = longitudLista(st, prof, n);
            Object arr = Array.newInstance(el, len);
            for (int i = 0; i < len; i++) {
                Array.set(arr, i, poblar(el, campo, st, prof + 1));
            }
            return arr;
        }
        if (Collection.class.isAssignableFrom(raw)) {
            Type el = t instanceof ParameterizedType pt ? pt.getActualTypeArguments()[0] : Object.class;
            int len = longitudLista(st, prof, n);
            List<Object> l = new ArrayList<>();
            for (int i = 0; i < len; i++) {
                l.add(poblar(el, campo, st, prof + 1));
            }
            return l;
        }
        if (Map.class.isAssignableFrom(raw) || raw.isInterface() || Modifier.isAbstract(raw.getModifiers()) || raw.isEnum() || prof > 5) {
            return null;
        }
        if (p2 && n % 7 == 3) {
            return null;     // a null nested object in the limits profile
        }
        Object o;
        try {
            Constructor<?> k = raw.getDeclaredConstructor();
            k.setAccessible(true);
            o = k.newInstance();
        } catch (NoSuchMethodException ex) {
            o = PARSER.fromJson("{}", raw);
        }
        for (Class<?> c = raw; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers()) || Modifier.isTransient(f.getModifiers()) || f.isSynthetic()) {
                    continue;
                }
                f.setAccessible(true);
                f.set(o, poblar(f.getGenericType(), f.getName(), st, prof + 1));
            }
        }
        return o;
    }

    static int longitudLista(Estado st, int prof, int n) {
        if (st.perfil == Perfil.P2) {
            return n % 4 == 0 ? 0 : (prof < 2 ? 3 : 1);
        }
        return prof < 2 ? 2 : 1;
    }

    // ------------------------------------------------------------------------------------------------ entradas
    static void entradas(JsonObject ops, Path dir) throws Exception {
        JsonArray index = new JsonArray();
        for (JsonElement e : ops.getAsJsonArray("operaciones")) {
            JsonObject op = e.getAsJsonObject();
            Gson gson = gsonDe(op);
            String caso = op.get("caso").getAsString();
            for (String lado : new String[] { "entrada", "salida" }) {
                Class<?> c = Class.forName(op.get(lado).getAsString());
                String l = lado.equals("entrada") ? "E" : "S";
                String p1 = null;
                for (Perfil perfil : Perfil.values()) {
                    Object o = poblar(c, c.getSimpleName(), new Estado(perfil), 0);
                    String json = gson.toJson(o);
                    if (perfil == Perfil.P1) {
                        p1 = json;
                    }
                    anotar(index, dir, "F-" + caso + "-" + l + "-" + perfil.name(), caso, lado, perfil.name(), json);
                }
                // P3: the P1 request with every second key removed, every third kept key set to null and two keys the class does not have
                JsonObject src = JsonParser.parseString(p1).getAsJsonObject();
                JsonObject p3 = new JsonObject();
                int i = 0;
                int kept = 0;
                p3.addProperty("campoQueNoExiste", 1);
                for (Map.Entry<String, JsonElement> en : src.entrySet()) {
                    if (i++ % 2 == 1) {
                        continue;
                    }
                    p3.add(en.getKey(), kept++ % 3 == 2 ? JsonNull.INSTANCE : en.getValue());
                }
                JsonObject extra = new JsonObject();
                extra.add("x", JsonParser.parseString("[1,2,{\"y\":null}]"));
                p3.add("otroCampoExtra", extra);
                anotar(index, dir, "F-" + caso + "-" + l + "-P3", caso, lado, "P3", new Gson().toJson(p3));
            }
        }
        JsonObject raiz = new JsonObject();
        raiz.add("entradas", index);
        escribir(dir.resolve("index.json"), new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(raiz) + "\n");
        System.out.println("entradas frescas: " + index.size() + " -> " + dir);
    }

    static void anotar(JsonArray index, Path dir, String id, String caso, String lado, String perfil, String json) throws IOException {
        escribir(dir.resolve(id + ".json"), json);
        JsonObject f = new JsonObject();
        f.addProperty("id", id);
        f.addProperty("caso", caso);
        f.addProperty("lado", lado);
        f.addProperty("perfil", perfil);
        index.add(f);
    }

    // ------------------------------------------------------------------------------------------------ ejecutar
    static void ejecutar(JsonObject ops, Path in, Path out) throws Exception {
        Map<String, JsonObject> porCaso = new TreeMap<>();
        for (JsonElement e : ops.getAsJsonArray("operaciones")) {
            porCaso.put(e.getAsJsonObject().get("caso").getAsString(), e.getAsJsonObject());
        }
        JsonObject index = JsonParser.parseString(Files.readString(in.resolve("index.json"), StandardCharsets.UTF_8)).getAsJsonObject();
        StringBuilder rechazadas = new StringBuilder();
        int ok = 0;
        for (JsonElement e : index.getAsJsonArray("entradas")) {
            JsonObject f = e.getAsJsonObject();
            String id = f.get("id").getAsString();
            JsonObject op = porCaso.get(f.get("caso").getAsString());
            Gson gson = gsonDe(op);
            Class<?> c = Class.forName(op.get(f.get("lado").getAsString()).getAsString());
            String texto = Files.readString(in.resolve(id + ".json"), StandardCharsets.UTF_8);
            try {
                Object o = gson.fromJson(texto, c);
                escribir(out.resolve(id).resolve("out.json"), gson.toJson(o));
                ok++;
            } catch (Exception ex) {
                rechazadas.append(id).append('\t').append(ex.getClass().getSimpleName()).append(": ").append(String.valueOf(ex.getMessage()).replace('\n', ' ')).append('\n');
            }
        }
        escribir(out.resolve("rechazadas.txt"), rechazadas.toString());
        System.out.println("ejecutar: " + ok + " ejecutadas, " + rechazadas.toString().lines().count() + " rechazadas por el legacy -> " + out);
    }
}
