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
public class ConsultarInstrumentoPrecioBolsaService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public ConsultarInstrumentoPrecioBolsaService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.ins.ConsultarInstrumentoPrecioBolsa"; }

    @Override
    public JsonObject execute(JsonObject params) {
        // TODO: requires pla.* utility (ConfiguracionSistema, ConsultarEmpresa) — complete after pla subpackage migration
        JsonObject result = new JsonObject();
        try {
            String sqlSelectInstrumentos = loadSql("cl/bcs/plataforma/ins/sql/ConsultarInstrumentoPrecioBolsa_Select_Instrumentos.sql");
            String sqlDeleteTable = loadSql("cl/bcs/plataforma/ins/sql/ConsultarInstrumentoPrecioBolsa_Delete_Table.sql");
            String sqlDeletePrecios = loadSql("cl/bcs/plataforma/ins/sql/ConsultarInstrumentoPrecioBolsa_Delete_Table_Precios.sql");
            String sqlInsertFecha = loadSql("cl/bcs/plataforma/ins/sql/ConsultarInstrumentoPrecioBolsa_Insert_Instrumentos_Fecha.sql");
            String sqlInsertPrecios = loadSql("cl/bcs/plataforma/ins/sql/ConsultarInstrumentoPrecioBolsa_Insert_Instrumentos_Precios.sql");
            String sqlSelectPrecios = loadSql("cl/bcs/plataforma/ins/sql/ConsultarInstrumentoPrecioBolsa_Select_Precios_Bolsa.sql");

            String soloInscritos = params.has("p_SoloInscritos") && !params.get("p_SoloInscritos").isJsonNull()
                    ? params.get("p_SoloInscritos").getAsString() : "N";
            String incluyeNemo = params.has("p_IncluyeNemoNegociacion") && !params.get("p_IncluyeNemoNegociacion").isJsonNull()
                    ? params.get("p_IncluyeNemoNegociacion").getAsString() : "N";

            List<Long> idInstrumentos = new ArrayList<>();
            List<String> idTipos = new ArrayList<>();
            List<String> idBolsas = new ArrayList<>();
            List<String> nemos = new ArrayList<>();
            List<String> idClasificaciones = new ArrayList<>();
            int filtroPorId = 1, filtroPorTipo = 1, filtroPorBolsa = 1, filtroPorNemo = 1, filtroPorClasificacion = 1;

            JsonArray instrumentos = params.has("p_Instrumentos") && params.get("p_Instrumentos").isJsonArray()
                    ? params.getAsJsonArray("p_Instrumentos") : new JsonArray();
            for (JsonElement el : instrumentos) {
                JsonObject item = el.getAsJsonObject();
                if (item.has("IdInstrumento") && !item.get("IdInstrumento").isJsonNull()) {
                    idInstrumentos.add(item.get("IdInstrumento").getAsLong());
                    filtroPorId = 0;
                }
                if (item.has("IdTipoInstrumento") && !item.get("IdTipoInstrumento").isJsonNull()) {
                    idTipos.add(item.get("IdTipoInstrumento").getAsString());
                    filtroPorTipo = 0;
                }
                if (item.has("IdBolsa") && !item.get("IdBolsa").isJsonNull()) {
                    idBolsas.add(item.get("IdBolsa").getAsString());
                    filtroPorBolsa = 0;
                }
                if (item.has("Nemotecnico") && !item.get("Nemotecnico").isJsonNull()) {
                    nemos.add(item.get("Nemotecnico").getAsString());
                    filtroPorNemo = 0;
                }
                if (item.has("IdClasificacionTipoInstrumento") && !item.get("IdClasificacionTipoInstrumento").isJsonNull()) {
                    idClasificaciones.add(item.get("IdClasificacionTipoInstrumento").getAsString());
                    filtroPorClasificacion = 0;
                }
            }

            if (idInstrumentos.isEmpty()) idInstrumentos.add(-1L);
            if (idTipos.isEmpty()) idTipos.add("");
            if (idBolsas.isEmpty()) idBolsas.add("");
            if (nemos.isEmpty()) nemos.add("");
            if (idClasificaciones.isEmpty()) idClasificaciones.add("");

            MapSqlParameterSource selectParams = new MapSqlParameterSource()
                    .addValue("idInstrumentos", idInstrumentos)
                    .addValue("idTiposInstrumentos", idTipos)
                    .addValue("idBolsas", idBolsas)
                    .addValue("nemos", nemos)
                    .addValue("idClasificaciones", idClasificaciones)
                    .addValue("filtroPorId", filtroPorId)
                    .addValue("filtroPorTipo", filtroPorTipo)
                    .addValue("filtroPorBolsa", filtroPorBolsa)
                    .addValue("filtroPorNemo", filtroPorNemo)
                    .addValue("filtroPorClasificacion", filtroPorClasificacion)
                    .addValue("soloInscritos", soloInscritos)
                    .addValue("incluyeNemoNegociacion", incluyeNemo);

            List<Map<String, Object>> rows = jdbc.queryForList(sqlSelectInstrumentos, selectParams);

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
