package cl.bcs.plataforma.cre;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.port.out.ModuloRepository;

/**
 * Use case of the JavaCall {@code cl.bcs.plataforma.cre.ConsultarEstadoModulo} (registry id 600017, 1.777.141 uses).
 * Port of legacy {@code BcsPlataformaServicios/src/cl/bcs/plataforma/cre/ConsultarEstadoModulo.java} (124 LOC): {@code call} (:60-69)
 * and {@code logica} (:109-122). No Spring, no static state: one instance (with one repository) per call, as the legacy
 * {@code PluginLoader} did with {@code newInstance()}.
 *
 * <p>The wire format is the legacy one, bug for bug (rules PLA-ESM-01..11, defects D-ESM-1..7):
 * <ul>
 *   <li>the input is read with a plain {@code new Gson()} into public fields named like the keys (case sensitive; a text
 *       {@code "1"} or {@code 1.0} is accepted as a {@code Long}; {@code 1.5} or {@code "abc"} throw {@code JsonSyntaxException});
 *       {@code p_IdModulo} and {@code p_IdUsuario} are parsed (so they can still break the call) and then ignored (D-ESM-1, D-ESM-7);</li>
 *   <li>the output is written with a plain {@code new Gson()} (no {@code serializeNulls}); the legacy class also declared
 *       {@code p_out_AdvertenciasDetalle} and {@code p_out_ErroresDetalle}, which were always {@code null} and therefore never
 *       emitted, so they are not declared here: the text is identical. Key order = component order (PLA-ESM-06);</li>
 *   <li>errors are NOT counted: a failure propagates as an exception (D-ESM-5). The HTTP layer turns it into a generic 500
 *       (approved deviation DEV-A3).</li>
 * </ul>
 */
public class ConsultarEstadoModuloService {

    /** Legacy {@code InConsultarEstadoModulo} (ESM:46-50). Only {@code p_IdEmpresa} is used. */
    public record Entrada(Long p_IdEmpresa, String p_IdModulo, Long p_IdUsuario) { }

    /** Legacy {@code Modulo} (ESM:28-32). {@code NombreModulo} is the column {@code CodigoModulo} (D-ESM-2). */
    public record Modulo(String IdEstadoModulo, String NombreModulo) { }

    /** Legacy {@code OutConsultarEstadoModulo} (ESM:51-57), without the two null-always lists (see class comment). */
    public record Salida(Long P_OUT_ADVERTENCIASCONTADOR, Long P_OUT_ERRORESCONTADOR, List<Modulo> p_out_ListaModulos) { }

    private final ModuloRepository modulos;

    public ConsultarEstadoModuloService(ModuloRepository modulos) {
        this.modulos = modulos;
    }

    /** Legacy {@code call(con, jin)}: JSON in, JSON out, same key order and same exceptions as the legacy. */
    public JsonObject call(JsonObject jin) {
        Gson gson = new Gson();
        Entrada in = gson.fromJson(jin.toString(), Entrada.class);
        Salida out = logica(in);
        return JsonParser.parseString(gson.toJson(out)).getAsJsonObject();
    }

    /** Legacy {@code logica} (ESM:109-122): counters fixed at 0, the module list never {@code null}. */
    public Salida logica(Entrada in) {
        List<Modulo> lista = modulos.listarPorEmpresa(in.p_IdEmpresa());
        return new Salida(0L, 0L, lista);
    }
}
