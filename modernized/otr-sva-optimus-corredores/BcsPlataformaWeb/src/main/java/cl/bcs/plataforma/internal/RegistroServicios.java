package cl.bcs.plataforma.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Class -> PLA.Servicio id of the J rows of the registry export ({@code IdServicio,RutaServicio,TipoServicio}, header first).
 * Only the TomEE leg needs it: TomEE serves JavaCall by {@code /call?t={IdServicio}}. {@code RutaServicio} is
 * {@code JAR.jar@fully.qualified.Class} (the prefix is stripped) or the bare class. A class with two different ids is ambiguous
 * and resolves to nothing (fail closed). Rows of other types (Q, P, X, O, A) are counted, never resolved.
 */
public class RegistroServicios {

    private final Map<String, Long> ids = new HashMap<>();
    private final Set<String> ambiguas = new HashSet<>();
    private final Map<String, Integer> porTipo = new HashMap<>();

    public static RegistroServicios vacio() {
        return new RegistroServicios();
    }

    public static RegistroServicios desde(InputStream in) throws IOException {
        RegistroServicios r = new RegistroServicios();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            br.readLine(); // header
            for (String l = br.readLine(); l != null; l = br.readLine()) {
                r.agregar(l);
            }
        }
        return r;
    }

    private void agregar(String linea) {
        int a = linea.indexOf(','), z = linea.lastIndexOf(',');
        if (a <= 0 || z <= a) return;
        String tipo = linea.substring(z + 1).trim();
        porTipo.merge(tipo, 1, Integer::sum);
        if (!"J".equals(tipo)) return;
        String ruta = linea.substring(a + 1, z);
        String clase = ruta.substring(ruta.indexOf('@') + 1).trim();
        try {
            long id = Long.parseLong(linea.substring(0, a).trim());
            Long previo = ids.putIfAbsent(clase, id);
            if (previo != null && previo != id) ambiguas.add(clase);
        } catch (NumberFormatException ignorado) {
            // malformed id: the row is ignored
        }
    }

    public Optional<Long> idDe(String clase) {
        return ambiguas.contains(clase) ? Optional.empty() : Optional.ofNullable(ids.get(clase));
    }

    public void clasesJava(Consumer<String> consumidor) {
        ids.keySet().forEach(consumidor);
    }

    public int filas(String tipo) {
        return porTipo.getOrDefault(tipo, 0);
    }
}
