package cl.bcs.plataforma.service.ins;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ConsultarInstrumentoWSService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public ConsultarInstrumentoWSService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.ins.ConsultarInstrumentoWS"; }

    @Override
    public JsonObject execute(JsonObject params) {
        // TODO: requires pla.* utility (ObtenerDiaSiguiente, ConfiguracionSistema) — complete after pla subpackage migration
        JsonObject result = new JsonObject();
        try {
            String sqlWS = loadSql("cl/bcs/plataforma/ins/sql/ConsultaInstrumentoWS.sql");
            String sqlAtributos = loadSql("cl/bcs/plataforma/ins/sql/ConsultarInstrumentoWS_ConsultaAtributoInstrumento.sql");

            List<Long> idInstrumento = new ArrayList<>();
            List<String> idClasificacion = new ArrayList<>();
            List<String> idTipo = new ArrayList<>();
            List<String> nemotecnico = new ArrayList<>();
            List<String> estaInscrito = new ArrayList<>();
            List<String> esAutorizado = new ArrayList<>();
            List<String> esCalificado = new ArrayList<>();
            List<String> idEstado = new ArrayList<>();

            String filtraId = "N", filtraClasif = "N", filtraTipo = "N", filtraNemo = "N";
            String filtraInscrito = "N", filtraAutorizado = "N", filtraCalificado = "N", filtraEstado = "N";
            String incluyeNemo = params.has("p_IncluyeNemoNegociacion") && !params.get("p_IncluyeNemoNegociacion").isJsonNull()
                    ? params.get("p_IncluyeNemoNegociacion").getAsString() : "N";

            JsonArray datos = params.has("p_Datos") && params.get("p_Datos").isJsonArray()
                    ? params.getAsJsonArray("p_Datos") : new JsonArray();
            for (JsonElement el : datos) {
                JsonObject d = el.getAsJsonObject();
                if (d.has("IdInstrumento") && !d.get("IdInstrumento").isJsonNull()) {
                    idInstrumento.add(d.get("IdInstrumento").getAsLong()); filtraId = "S";
                }
                if (d.has("IdClasificacionTipoInstrumento") && !d.get("IdClasificacionTipoInstrumento").isJsonNull()) {
                    idClasificacion.add(d.get("IdClasificacionTipoInstrumento").getAsString()); filtraClasif = "S";
                }
                if (d.has("IdTipoInstrumento") && !d.get("IdTipoInstrumento").isJsonNull()) {
                    idTipo.add(d.get("IdTipoInstrumento").getAsString()); filtraTipo = "S";
                }
                if (d.has("Nemotecnico") && !d.get("Nemotecnico").isJsonNull()) {
                    nemotecnico.add(d.get("Nemotecnico").getAsString()); filtraNemo = "S";
                }
                if (d.has("EstaInscrito") && !d.get("EstaInscrito").isJsonNull()) {
                    estaInscrito.add(d.get("EstaInscrito").getAsString()); filtraInscrito = "S";
                }
                if (d.has("EsAutorizado") && !d.get("EsAutorizado").isJsonNull()) {
                    esAutorizado.add(d.get("EsAutorizado").getAsString()); filtraAutorizado = "S";
                }
                if (d.has("EsInstrumentoCalificado") && !d.get("EsInstrumentoCalificado").isJsonNull()) {
                    esCalificado.add(d.get("EsInstrumentoCalificado").getAsString()); filtraCalificado = "S";
                }
                if (d.has("IdEstado") && !d.get("IdEstado").isJsonNull()) {
                    idEstado.add(d.get("IdEstado").getAsString()); filtraEstado = "S";
                }
            }

            if (idInstrumento.isEmpty()) idInstrumento.add(-1L);
            if (idClasificacion.isEmpty()) idClasificacion.add("");
            if (idTipo.isEmpty()) idTipo.add("");
            if (nemotecnico.isEmpty()) nemotecnico.add("");
            if (estaInscrito.isEmpty()) estaInscrito.add("");
            if (esAutorizado.isEmpty()) esAutorizado.add("");
            if (esCalificado.isEmpty()) esCalificado.add("");
            if (idEstado.isEmpty()) idEstado.add("");

            List<Map<String, Object>> rows = jdbc.queryForList(sqlWS, new MapSqlParameterSource()
                    .addValue("IdInstrumento", idInstrumento)
                    .addValue("IdClasificacionTipoInstrumento", idClasificacion)
                    .addValue("IdTipoInstrumento", idTipo)
                    .addValue("Nemotecnico", nemotecnico)
                    .addValue("EstaInscrito", estaInscrito)
                    .addValue("EsAutorizado", esAutorizado)
                    .addValue("EsInstrumentoCalificado", esCalificado)
                    .addValue("IdEstado", idEstado)
                    .addValue("FiltraIdInstrumento", filtraId)
                    .addValue("FiltraIdClasificacionTipoInstrumento", filtraClasif)
                    .addValue("FiltraIdTipoInstrumento", filtraTipo)
                    .addValue("FiltraNemotecnico", filtraNemo)
                    .addValue("FiltraEsInscrito", filtraInscrito)
                    .addValue("FiltraEsAutorizado", filtraAutorizado)
                    .addValue("FiltraEsInstrumentoCalificado", filtraCalificado)
                    .addValue("FiltraIdEstado", filtraEstado)
                    .addValue("IncluyeNemoNegociacion", incluyeNemo));

            JsonArray arr = new JsonArray();
            rows.forEach(row -> {
                JsonObject o = new JsonObject();
                row.forEach((k, v) -> o.addProperty(k, v != null ? v.toString() : null));
                arr.add(o);
            });
            result.add("P_OUT_INSTRUMENTO", arr);
            result.addProperty("P_OUT_ERRORESCONTADOR", 0);
            result.add("P_OUT_MENSAJES", new JsonArray());
        } catch (Exception e) {
            result.addProperty("P_OUT_ERRORESCONTADOR", 1);
            JsonArray msgs = new JsonArray();
            JsonObject msg = new JsonObject();
            msg.addProperty("Tipo", "ERROR");
            msg.addProperty("Descripcion", e.getMessage());
            msgs.add(msg);
            result.add("P_OUT_MENSAJES", msgs);
        }
        return result;
    }

    private String loadSql(String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes());
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
