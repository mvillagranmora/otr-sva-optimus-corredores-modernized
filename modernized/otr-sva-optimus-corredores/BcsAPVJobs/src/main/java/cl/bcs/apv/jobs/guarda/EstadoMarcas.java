package cl.bcs.apv.jobs.guarda;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * What the marks of ONE program say, over all its attempts.
 *
 * @param sinCierre INTENTO marks that have neither HECHO nor FALLO: a run died (or lost its connection) after the call started, so the outcome
 *                  of the TomEE write is UNKNOWN
 * @param hechas    INTENTO marks (with their HECHO) of calls that answered 2xx: the TomEE write is committed and cannot be undone
 * @param ilegibles rows that start with {@code H4G|} but do not parse: they cannot be trusted, so they count as unknown outcomes
 */
public record EstadoMarcas(List<MarcaGuarda> sinCierre, List<MarcaGuarda> hechas, int ilegibles) {

    public EstadoMarcas {
        sinCierre = List.copyOf(sinCierre);
        hechas = List.copyOf(hechas);
    }

    /** True when the program has nothing the guard refuses to run over. A FALLO alone does not block (see the notes). */
    public boolean limpio() {
        return sinCierre.isEmpty() && hechas.isEmpty() && ilegibles == 0;
    }

    /** Pairs the marks by {@link MarcaGuarda#llave()}. {@code marcas} may be in any order. */
    public static EstadoMarcas deMarcas(List<MarcaGuarda> marcas, int ilegibles) {
        Map<String, MarcaGuarda> abiertas = new LinkedHashMap<>();
        Map<String, MarcaGuarda> hechas = new LinkedHashMap<>();
        for (MarcaGuarda m : marcas) {
            if (m.fase() == MarcaGuarda.Fase.INTENTO) {
                abiertas.putIfAbsent(m.llave(), m);
            }
        }
        for (MarcaGuarda m : marcas) {
            if (m.fase() == MarcaGuarda.Fase.HECHO) {
                hechas.put(m.llave(), m.en(MarcaGuarda.Fase.INTENTO));
                abiertas.remove(m.llave());
            } else if (m.fase() == MarcaGuarda.Fase.FALLO) {
                abiertas.remove(m.llave());
            }
        }
        return new EstadoMarcas(new ArrayList<>(abiertas.values()), new ArrayList<>(hechas.values()), ilegibles);
    }

    /** Short description for {@code Resultado} and logs: {@code clase#n(intento i)}, at most {@code max} items. */
    public static String describir(List<MarcaGuarda> marcas, int max) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < marcas.size() && i < max; i++) {
            MarcaGuarda m = marcas.get(i);
            sb.append(i == 0 ? "" : ", ").append(m.clase()).append('#').append(m.n()).append("(attempt ").append(m.intento()).append(')');
        }
        if (marcas.size() > max) {
            sb.append(", ... (").append(marcas.size() - max).append(" more)");
        }
        return sb.toString();
    }
}
