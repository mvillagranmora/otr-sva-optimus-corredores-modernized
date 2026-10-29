package cl.bcs.cuentainversion.reportes;

import cl.bcs.cuentainversion.port.out.ColaboradoresReportes;
import cl.bcs.utils.Utils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * JSON edge of the reporting services: the JavaCall input/output are the JSON form of the legacy Entrada/Salida POJOs, read and
 * written with the platform Gson configuration ({@link Utils#getGsonBuilder()}: lenient date deserializers, nulls serialized).
 */
public final class JsonReportes {

    private JsonReportes() {
    }

    public static <T> T leer(JsonObject entrada, Class<T> tipo) {
        return Utils.getGsonBuilder().fromJson(entrada.toString(), tipo);
    }

    public static JsonObject escribir(Object salida) {
        return JsonParser.parseString(Utils.getGsonBuilder().toJson(salida)).getAsJsonObject();
    }

    /**
     * {@code ConsultarEmpresa}: the collaborator port is unchecked, the legacy service threw a plain {@link Exception}
     * ("Empresa no existe") that the reporting classes let propagate, so it is rethrown as such.
     */
    public static JsonObject empresa(ColaboradoresReportes colaboradores, long idEmpresa) throws Exception {
        try {
            return colaboradores.consultarEmpresa(idEmpresa);
        } catch (RuntimeException e) {
            throw new Exception(e.getMessage(), e);
        }
    }
}
