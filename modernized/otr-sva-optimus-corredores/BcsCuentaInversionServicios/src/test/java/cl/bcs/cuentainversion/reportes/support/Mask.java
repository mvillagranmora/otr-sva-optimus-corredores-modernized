package cl.bcs.cuentainversion.reportes.support;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Masks the wall-clock part of generated text (file names and file contents) so recorded and re-computed output compare.
 * The legacy code stamps files with the current date and time (new Date(), System.currentTimeMillis()). Rule:
 * <ol>
 *   <li>today's date (local or UTC) written as yyyyMMdd, yyyy-MM-dd, dd-MM-yyyy or dd/MM/yyyy becomes {@code <HOY>};</li>
 *   <li>a time of day right after {@code <HOY>} (optionally separated by space, '_', '-', 'T') as HHmmss, HH:mm:ss or
 *       HH:mm:ss.SSS becomes {@code <AHORA>} (replacing the whole stamp).</li>
 * </ol>
 * Business dates used in the cases are never today's date. If a case is recorded on a day that equals a business date of
 * its seed, pick other dates.
 */
public final class Mask {

    private static final Pattern HORA = Pattern.compile("<HOY>[ _T-]?\\d{2}:?\\d{2}:?\\d{2}(?:\\.\\d{3})?");

    private Mask() {
    }

    public static String texto(String s) {
        if (s == null) {
            return null;
        }
        Set<LocalDate> dias = new LinkedHashSet<>();
        dias.add(LocalDate.now());
        dias.add(LocalDate.now(ZoneOffset.UTC));
        String r = s;
        for (LocalDate d : dias) {
            for (String f : new String[]{"yyyyMMdd", "yyyy-MM-dd", "dd-MM-yyyy", "dd/MM/yyyy"}) {
                r = r.replace(d.format(DateTimeFormatter.ofPattern(f)), "<HOY>");
            }
        }
        return HORA.matcher(r).replaceAll("<AHORA>");
    }

    /**
     * Collaborator request logged under llamadasColaboradores: the legacy classes draw a random IdTrabajo (Math.random) for their work
     * tables and pass it along; any non zero "IdTrabajo" / "p_IdTrabajo" number becomes "<random>" (0 stays 0, it is a constant).
     */
    public static com.google.gson.JsonElement llamada(com.google.gson.JsonElement e) {
        if (e == null) {
            return null;
        }
        if (e.isJsonObject()) {
            com.google.gson.JsonObject o = e.getAsJsonObject();
            for (String k : new java.util.ArrayList<>(o.keySet())) {
                com.google.gson.JsonElement v = o.get(k);
                if ((k.equals("IdTrabajo") || k.equals("p_IdTrabajo")) && v.isJsonPrimitive() && v.getAsJsonPrimitive().isNumber() && v.getAsLong() != 0L) {
                    o.addProperty(k, "<random>");
                } else {
                    llamada(v);
                }
            }
        } else if (e.isJsonArray()) {
            e.getAsJsonArray().forEach(Mask::llamada);
        }
        return e;
    }
}
