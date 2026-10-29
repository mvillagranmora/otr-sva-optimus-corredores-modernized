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
public class ConsultarDatosInstrumentosService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public ConsultarDatosInstrumentosService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.ins.ConsultarDatosInstrumentos"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        try {
            String sqlSelect = loadSql("cl/bcs/plataforma/ins/sql/ConsultarDatosInstrumentos_SelectInstrumento.sql");

            List<Long> idInstrumento = new ArrayList<>();
            List<String> nemotecnico = new ArrayList<>();
            List<String> idsTipo = new ArrayList<>();
            List<String> idsClasificacion = new ArrayList<>();
            List<String> estadoInstrumento = new ArrayList<>();
            int filtroPorTipo = 1;
            int filtroPorClasificacion = 1;
            long filtroInstrumentoNegociacion = 0L;

            JsonArray instrumentos = params.has("p_Instrumentos") && params.get("p_Instrumentos").isJsonArray()
                    ? params.getAsJsonArray("p_Instrumentos") : new JsonArray();
            for (JsonElement el : instrumentos) {
                JsonObject item = el.getAsJsonObject();
                if (item.has("IdInstrumento") && !item.get("IdInstrumento").isJsonNull()) {
                    idInstrumento.add(item.get("IdInstrumento").getAsLong());
                } else if (item.has("Nemotecnico") && !item.get("Nemotecnico").isJsonNull()
                        && !item.get("Nemotecnico").getAsString().isEmpty()) {
                    nemotecnico.add(item.get("Nemotecnico").getAsString());
                }
            }

            JsonArray tipos = params.has("p_IdTipoInstrumentos") && params.get("p_IdTipoInstrumentos").isJsonArray()
                    ? params.getAsJsonArray("p_IdTipoInstrumentos") : new JsonArray();
            for (JsonElement el : tipos) {
                JsonObject item = el.getAsJsonObject();
                if (item.has("IdTipoInstrumento") && !item.get("IdTipoInstrumento").isJsonNull()) {
                    idsTipo.add(item.get("IdTipoInstrumento").getAsString());
                    filtroPorTipo = 0;
                }
            }

            JsonArray clasif = params.has("p_IdClasificacionTipoInstrumentos") && params.get("p_IdClasificacionTipoInstrumentos").isJsonArray()
                    ? params.getAsJsonArray("p_IdClasificacionTipoInstrumentos") : new JsonArray();
            for (JsonElement el : clasif) {
                JsonObject item = el.getAsJsonObject();
                if (item.has("IdClasificacionTipoInstrumento") && !item.get("IdClasificacionTipoInstrumento").isJsonNull()) {
                    idsClasificacion.add(item.get("IdClasificacionTipoInstrumento").getAsString());
                    filtroPorClasificacion = 0;
                }
            }

            JsonArray estados = params.has("p_filtroEstado") && params.get("p_filtroEstado").isJsonArray()
                    ? params.getAsJsonArray("p_filtroEstado") : new JsonArray();
            if (estados.size() == 0) {
                estadoInstrumento.add("ACT");
            } else {
                for (JsonElement el : estados) {
                    estadoInstrumento.add(el.getAsString());
                }
            }

            if (idInstrumento.isEmpty()) idInstrumento.add(-1L);
            if (nemotecnico.isEmpty()) nemotecnico.add("");

            String creaOpcion = params.has("p_CreaOpcionAutomatica") && !params.get("p_CreaOpcionAutomatica").isJsonNull()
                    ? params.get("p_CreaOpcionAutomatica").getAsString() : "N";
            if ("S".equalsIgnoreCase(creaOpcion)) {
                idsTipo.add("OPC");
                filtroPorTipo = 0;
            }

            int filtroNegociacion = params.has("p_filtroInstrumentoNegociacion")
                    ? params.get("p_filtroInstrumentoNegociacion").getAsInt() : 0;
            filtroInstrumentoNegociacion = filtroNegociacion == 0 ? 0L : 1L;

            MapSqlParameterSource sqlParams = new MapSqlParameterSource()
                    .addValue("IdInstrumento", idInstrumento)
                    .addValue("Nemotecnico", nemotecnico)
                    .addValue("idsTipoInstrumento", idsTipo)
                    .addValue("idsClasificacionTipoInstrumento", idsClasificacion)
                    .addValue("filtroPorTipo", filtroPorTipo)
                    .addValue("filtroPorClasificacion", filtroPorClasificacion)
                    .addValue("estadoInstrumento", estadoInstrumento)
                    .addValue("filtroInstrumentoNegociacion", filtroInstrumentoNegociacion);

            List<Map<String, Object>> rows = jdbc.queryForList(sqlSelect, sqlParams);

            JsonArray arr = new JsonArray();
            rows.forEach(row -> {
                JsonObject o = new JsonObject();
                row.forEach((k, v) -> o.addProperty(k, v != null ? v.toString() : null));
                arr.add(o);
            });
            result.add("P_OUT_INSTRUMENTOS", arr);
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
