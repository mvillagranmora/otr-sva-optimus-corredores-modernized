package cl.bcs.plataforma.login.dialecto;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The SQL statements of the login, one resource per statement and dialect: {@code /sql/hana/<id>.sql} (the text the login has always sent, byte for byte)
 * and {@code /sql/postgres/<id>.sql}. A statement id is {@code Class.CONSTANT} of the class that uses it, and the list of ids is
 * {@code /sql/indice.txt}.
 *
 * <p>Services keep their HANA text in constants loaded with {@link #hana(String)} and ask {@link #de(String)} for the text of the active dialect, so in HANA
 * mode the text sent is exactly the constant. A statement with no text in the active dialect fails with {@link UnsupportedOperationException} (503) before
 * anything is sent; an object that is not available fails with {@link ObjetoNoDisponibleException}.
 */
public class SentenciasSql {

    private static final String INDICE = "/sql/indice.txt";
    private static final Pattern NOMBRE_ENTRE_COMILLAS = Pattern.compile("\"(BSCL\\.[A-Za-z0-9_.]*::[A-Za-z0-9_.]*)\"");
    private static final Pattern NOMBRE_COMPLETO = Pattern.compile("BSCL\\.[A-Za-z0-9_.]*::[A-Za-z0-9_.]*");

    private final SqlDialecto dialecto;
    private final Set<String> ids = new LinkedHashSet<>();
    private final Map<String, String> idPorTextoHana = new LinkedHashMap<>();
    private final Map<String, String> textos = new ConcurrentHashMap<>();

    public SentenciasSql(SqlDialecto dialecto) {
        this.dialecto = dialecto;
        for (String linea : recurso(INDICE).split("\\R")) {
            if (!linea.isBlank()) {
                ids.add(linea.strip());
            }
        }
        for (String id : ids) {
            String otro = idPorTextoHana.putIfAbsent(hana(id), id);
            if (otro != null) {
                throw new IllegalStateException("Statements " + otro + " and " + id + " have the same HANA text: de(text) could not tell them apart");
            }
        }
    }

    /** The statements of the HANA dialect over the resources' own catalog: what the classes use when nobody gives them another dialect. */
    public static SentenciasSql predeterminadas() {
        return Predeterminadas.HANA;
    }

    private static final class Predeterminadas {
        static final SentenciasSql HANA = new SentenciasSql(new HanaDialecto(CatalogoEquivalencias.desdeRecursos()));
    }

    /** The HANA text of a statement, exactly as the login sends it in HANA mode. */
    public static String hana(String id) {
        return recurso("/sql/hana/" + id + ".sql");
    }

    public SqlDialecto dialecto() {
        return dialecto;
    }

    /** Every statement id. */
    public Set<String> ids() {
        return Set.copyOf(ids);
    }

    /** The full HANA names of the objects a statement touches. */
    public Collection<String> objetosDe(String id) {
        String hana = hanaDe(id);
        List<String> objetos = new ArrayList<>();
        if (NOMBRE_COMPLETO.matcher(hana).matches()) {
            objetos.add(hana);
            return objetos;
        }
        Matcher m = NOMBRE_ENTRE_COMILLAS.matcher(hana);
        while (m.find()) {
            if (!objetos.contains(m.group(1))) {
                objetos.add(m.group(1));
            }
        }
        return objetos;
    }

    /**
     * Pre-flight, before anything is read or written: every statement must have a text in the active dialect and every object it touches must be
     * available. Fails with {@link ObjetoNoDisponibleException} or {@link UnsupportedOperationException} (503).
     */
    public void exigirDisponibles(Collection<String> idsDeSentencias) {
        for (String id : idsDeSentencias) {
            texto(id);
        }
    }

    /** The text of a statement in the active dialect. */
    public String texto(String id) {
        String cacheado = textos.get(id);
        if (cacheado != null) {
            return cacheado;
        }
        String texto = resolver(id);
        textos.put(id, texto);
        return texto;
    }

    /** The text, in the active dialect, of the statement whose HANA text is the given constant. */
    public String de(String textoHana) {
        String id = idPorTextoHana.get(textoHana);
        if (id == null) {
            throw new IllegalArgumentException("not a registered login statement: " + abreviar(textoHana));
        }
        return texto(id);
    }

    private String resolver(String id) {
        Collection<String> objetos = objetosDe(id);
        for (String objeto : objetos) {
            dialecto.exigirDisponible(objeto);
        }
        String hana = hanaDe(id);
        if (NOMBRE_COMPLETO.matcher(hana).matches()) {
            return dialecto.nombreDe(hana);
        }
        String propio = leerOpcional("/sql/" + dialecto.nombre() + "/" + id + ".sql");
        if (propio == null) {
            throw new UnsupportedOperationException("Statement " + id + " has no " + dialecto.nombre() + " text yet");
        }
        return propio;
    }

    private String hanaDe(String id) {
        if (!ids.contains(id)) {
            throw new IllegalArgumentException("unknown statement " + id);
        }
        return hana(id);
    }

    private static String abreviar(String texto) {
        return texto.length() <= 60 ? texto : texto.substring(0, 60) + "...";
    }

    private static String recurso(String ruta) {
        String texto = leerOpcional(ruta);
        if (texto == null) {
            throw new IllegalStateException("missing resource " + ruta);
        }
        return texto;
    }

    private static String leerOpcional(String ruta) {
        try (InputStream in = SentenciasSql.class.getResourceAsStream(ruta)) {
            return in == null ? null : new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("cannot read " + ruta, e);
        }
    }
}
