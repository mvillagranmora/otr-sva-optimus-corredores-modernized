package cl.bcs.replica.caracterizacion;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;

/**
 * Lado NUEVO de las entradas frescas (verificacion independiente 2026-10-30): ejecuta el codigo de produccion de BcsReplica con el mismo arnes que las pruebas (CasoReplica.ejecutar:
 * H2 en memoria, conexion registradora, colaboradores guionados por el caso, todas las compuertas abiertas) sobre case.json + seed.sql de cada caso de &lt;entradas&gt; y escribe
 * &lt;salida&gt;/&lt;caso&gt;/expected.json con el mismo formato que el oraculo del legacy. Solo lee entradas; solo escribe en &lt;salida&gt;. No toca HANA ni la red.
 * Uso: java -cp ... cl.bcs.replica.caracterizacion.FrescoNuevo &lt;entradas&gt; &lt;salida&gt;
 */
public final class FrescoNuevo {
    private FrescoNuevo() {
    }

    public static void main(String[] args) throws Exception {
        Path entradas = Path.of(args[0]);
        Path salida = Path.of(args[1]);
        List<Path> dirs;
        try (var s = Files.list(entradas)) {
            dirs = s.filter(p -> Files.isDirectory(p) && Files.exists(p.resolve("case.json"))).sorted().collect(Collectors.toList());
        }
        int n = 0;
        for (Path d : dirs) {
            JsonObject caso = CasoReplica.leer(d.resolve("case.json"));
            Path semilla = d.resolve("seed.sql");
            JsonObject corrida = CasoReplica.ejecutar(caso, Files.exists(semilla) ? Files.readString(semilla, StandardCharsets.UTF_8) : null, null);
            JsonObject r = new JsonObject();
            r.addProperty("_provenance", "Written by fresh/driver FrescoNuevo executing the modernized BcsReplica (src/main) through CasoReplica.ejecutar on H2 in memory; same sections as the legacy oracle.");
            for (var e : corrida.entrySet()) r.add(e.getKey(), e.getValue());
            Path dest = salida.resolve(d.getFileName().toString());
            Files.createDirectories(dest);
            Files.write(dest.resolve("expected.json"), (CasoReplica.pretty(r) + "\n").getBytes(StandardCharsets.UTF_8));
            n++;
        }
        System.out.println("fresh new executed: " + n);
    }
}
