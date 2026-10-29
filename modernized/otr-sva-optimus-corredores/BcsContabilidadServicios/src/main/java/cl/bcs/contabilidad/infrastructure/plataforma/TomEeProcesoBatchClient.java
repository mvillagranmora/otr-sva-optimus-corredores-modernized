package cl.bcs.contabilidad.infrastructure.plataforma;

import cl.bcs.contabilidad.domain.model.MensajeSalida;
import cl.bcs.contabilidad.domain.port.out.ProcesoBatchPort;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * HTTP adapter for the legacy platform JavaCall {@code cl.bcs.plataforma.bat.InscribirProcesoBatch} (strangler-fig:
 * the platform keeps owning batch programs). Same base URL as {@link TomEePlataformaClient}; no credentials here.
 */
@Component
public class TomEeProcesoBatchClient implements ProcesoBatchPort {

    private static final long ID_INTERNO = 1L;

    private final PlataformaHttp http;
    private final Gson gson = new GsonBuilder().serializeNulls().create();

    public TomEeProcesoBatchClient(
            @Value("${plataforma.base-url:${PLATAFORMA_BASE_URL:http://localhost:8080}}") String baseUrl) {
        this.http = new PlataformaHttp(baseUrl);
    }

    @Override
    public Inscripcion inscribir(Long idEmpresa, String idModulo, Long idUsuario, String idTipoPrograma,
                                 long segundosDesfase, List<Parametro> parametros) {
        JsonObject request = new JsonObject();
        request.addProperty("p_IdEmpresa", idEmpresa);
        request.addProperty("p_IdModulo", idModulo);
        request.addProperty("p_IdUsuario", idUsuario);
        JsonObject tipo = new JsonObject();
        tipo.addProperty("IdInterno", ID_INTERNO);
        tipo.addProperty("IdTipoPrograma", idTipoPrograma);
        tipo.addProperty("SegundosDesfase", segundosDesfase);
        tipo.addProperty("Comentario", "");
        JsonArray tipos = new JsonArray();
        tipos.add(tipo);
        request.add("p_TipoPrograma", tipos);
        JsonArray params = new JsonArray();
        for (Parametro p : parametros) {
            JsonObject o = new JsonObject();
            o.addProperty("IdInterno", ID_INTERNO);
            o.addProperty("NombreParametro", p.nombre());
            o.addProperty("Valor", p.valor());
            o.addProperty("ValorBlob", p.valorBlob());
            params.add(o);
        }
        request.add("p_Parametro", params);

        String body = http.post("/api/plataforma/InscribirProcesoBatch", gson.toJson(request));
        JsonObject out = gson.fromJson(body, JsonObject.class);

        long errores = out.has("P_OUT_ERRORESCONTADOR") ? out.get("P_OUT_ERRORESCONTADOR").getAsLong() : 0L;
        List<MensajeSalida> mensajes = new ArrayList<>();
        if (out.has("P_OUT_MENSAJES") && out.get("P_OUT_MENSAJES").isJsonArray()) {
            for (JsonElement e : out.getAsJsonArray("P_OUT_MENSAJES")) mensajes.add(gson.fromJson(e, MensajeSalida.class));
        }
        Long idPrograma = null;
        if (out.has("P_OUT_PROGRAMA") && out.get("P_OUT_PROGRAMA").isJsonArray() && !out.getAsJsonArray("P_OUT_PROGRAMA").isEmpty()) {
            JsonObject programa = out.getAsJsonArray("P_OUT_PROGRAMA").get(0).getAsJsonObject();
            idPrograma = programa.get("IdPrograma").getAsLong();
        }
        return new Inscripcion(errores, mensajes, idPrograma);
    }
}
