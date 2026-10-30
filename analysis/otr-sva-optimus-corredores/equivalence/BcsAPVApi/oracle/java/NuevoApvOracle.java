import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.apv.adapter.javacall.ApvJavaCallRegistro;
import cl.bcs.apv.adapter.javacall.ApvJavaCallRegistro.OperacionRegistrada;
import cl.bcs.apv.adapter.javacall.MapeoOperacion;
import cl.bcs.apv.adapter.javacall.Sabor;

/**
 * ORACLE of the NEW side of BcsAPVApi: the typed records of BcsAPVApi through the mappers of BcsApvJavaCallAdapter, back to the legacy JSON names, serialised with the
 * Gson flavour the mapper itself declares ({@link Sabor}, read by reflection from its private constant {@code S}).
 * <ul>
 *   <li>{@code defecto <ops.json> <outDir>}: {@code Entrada.porDefecto()} / {@code Resultado.porDefecto()} -> {@code mapeo.entradaAJson / resultadoAJson} ->
 *       {@code outDir/<Caso>/entrada.json, salida.json}.</li>
 *   <li>{@code ejecutar <ops.json> <inDir> <outDir>}: the SAME input text the legacy got -> {@code mapeo.entradaDesdeJson / resultadoDesdeJson} (record) ->
 *       {@code entradaAJson / resultadoAJson} -> {@code outDir/<id>/out.json}; an input the new code rejects (exception) goes to {@code rechazadas.txt}.</li>
 * </ul>
 * The record is the mapper's own: nothing here knows a field name.
 */
public class NuevoApvOracle {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void main(String[] args) throws Exception {
        JsonObject ops = JsonParser.parseString(Files.readString(Path.of(args[1]), StandardCharsets.UTF_8)).getAsJsonObject();
        ApvJavaCallRegistro registro = new ApvJavaCallRegistro();
        Map<String, JsonObject> porCaso = new TreeMap<>();
        for (JsonElement e : ops.getAsJsonArray("operaciones")) {
            porCaso.put(e.getAsJsonObject().get("caso").getAsString(), e.getAsJsonObject());
        }
        switch (args[0]) {
            case "defecto" -> {
                Path out = Path.of(args[2]);
                JsonArray index = new JsonArray();
                for (JsonObject op : porCaso.values()) {
                    OperacionRegistrada reg = registro.buscar(op.get("legacy").getAsString()).orElseThrow();
                    MapeoOperacion mapeo = reg.mapeo();
                    Sabor sabor = sabor(mapeo);
                    Object entrada = porDefecto(reg, true);
                    Object resultado = porDefecto(reg, false);
                    escribir(out.resolve(reg.casoUso()).resolve("entrada.json"), sabor.gson().toJson(mapeo.entradaAJson(entrada)));
                    escribir(out.resolve(reg.casoUso()).resolve("salida.json"), sabor.gson().toJson(mapeo.resultadoAJson(resultado)));
                    JsonObject f = new JsonObject();
                    f.addProperty("caso", reg.casoUso());
                    f.addProperty("sabor", sabor.name());
                    f.addProperty("saborEsperadoPorElLegacy", op.get("gson").getAsString().equals("plain") ? "PLANO" : "CONSTRUCTOR");
                    f.addProperty("entradaRecord", entrada.getClass().getName());
                    f.addProperty("resultadoRecord", resultado.getClass().getName());
                    index.add(f);
                }
                JsonObject raiz = new JsonObject();
                raiz.add("operaciones", index);
                escribir(out.resolve("index.json"), new GsonBuilder().setPrettyPrinting().create().toJson(raiz) + "\n");
                System.out.println("defecto (nuevo): " + index.size() + " operaciones -> " + out);
            }
            case "ejecutar" -> {
                Path in = Path.of(args[2]);
                Path out = Path.of(args[3]);
                JsonObject index = JsonParser.parseString(Files.readString(in.resolve("index.json"), StandardCharsets.UTF_8)).getAsJsonObject();
                StringBuilder rechazadas = new StringBuilder();
                int ok = 0;
                for (JsonElement e : index.getAsJsonArray("entradas")) {
                    JsonObject f = e.getAsJsonObject();
                    String id = f.get("id").getAsString();
                    JsonObject op = porCaso.get(f.get("caso").getAsString());
                    OperacionRegistrada reg = registro.buscar(op.get("legacy").getAsString()).orElseThrow();
                    MapeoOperacion mapeo = reg.mapeo();
                    Sabor sabor = sabor(mapeo);
                    JsonObject json = JsonParser.parseString(Files.readString(in.resolve(id + ".json"), StandardCharsets.UTF_8)).getAsJsonObject();
                    try {
                        JsonObject salida;
                        if (f.get("lado").getAsString().equals("entrada")) {
                            salida = mapeo.entradaAJson(mapeo.entradaDesdeJson(json));
                        } else {
                            salida = mapeo.resultadoAJson(mapeo.resultadoDesdeJson(json));
                        }
                        escribir(out.resolve(id).resolve("out.json"), sabor.gson().toJson(salida));
                        ok++;
                    } catch (Exception ex) {
                        rechazadas.append(id).append('\t').append(ex.getClass().getSimpleName()).append(": ").append(String.valueOf(ex.getMessage()).replace('\n', ' ')).append('\n');
                    }
                }
                escribir(out.resolve("rechazadas.txt"), rechazadas.toString());
                System.out.println("ejecutar (nuevo): " + ok + " ejecutadas, " + rechazadas.toString().lines().count() + " rechazadas -> " + out);
            }
            default -> throw new IllegalArgumentException(args[0]);
        }
    }

    /** the Gson flavour the generated mapper uses: its private static constant {@code S}. */
    static Sabor sabor(MapeoOperacion<?, ?> mapeo) throws Exception {
        Field f = mapeo.getClass().getDeclaredField("S");
        f.setAccessible(true);
        return (Sabor) f.get(null);
    }

    /** {@code Entrada.porDefecto()} / {@code Resultado.porDefecto()} of the use case interface of the operation (types read from {@code ejecutar}). */
    static Object porDefecto(OperacionRegistrada reg, boolean entrada) throws Exception {
        for (Method m : reg.interfaz().getDeclaredMethods()) {
            if (m.getName().equals("ejecutar") && !m.isBridge() && !m.isSynthetic()) {
                Class<?> t = entrada ? m.getParameterTypes()[0] : m.getReturnType();
                return t.getMethod("porDefecto").invoke(null);
            }
        }
        throw new IllegalStateException("ejecutar not found in " + reg.interfaz());
    }

    static void escribir(Path p, String texto) throws IOException {
        Files.createDirectories(p.getParent());
        Files.writeString(p, texto, StandardCharsets.UTF_8);
    }
}
