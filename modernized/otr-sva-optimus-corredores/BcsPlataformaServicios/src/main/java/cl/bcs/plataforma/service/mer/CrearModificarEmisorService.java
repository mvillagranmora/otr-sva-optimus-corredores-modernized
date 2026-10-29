package cl.bcs.plataforma.service.mer;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CrearModificarEmisorService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public CrearModificarEmisorService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.mer.CrearModificarEmisor"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        JsonArray erroresDetalle = new JsonArray();
        int errores = 0;

        try {
            String sqlExiste = loadSql("cl/bcs/plataforma/mer/sql/CrearModificarEmisor_Select_Existe.sql");
            String sqlInsert = loadSql("cl/bcs/plataforma/mer/sql/CrearModificarEmisor_Insert.sql");
            String sqlUpdate = loadSql("cl/bcs/plataforma/mer/sql/CrearModificarEmisor_Update.sql");

            String modo = params.has("p_Modo") && !params.get("p_Modo").isJsonNull()
                    ? params.get("p_Modo").getAsString() : "MODIFICAR";
            String idEmisor = params.has("p_IdEmisor") && !params.get("p_IdEmisor").isJsonNull()
                    ? params.get("p_IdEmisor").getAsString() : null;

            MapSqlParameterSource emisorParams = new MapSqlParameterSource()
                    .addValue("p_IdPersona", params.has("p_IdPersona") && !params.get("p_IdPersona").isJsonNull() ? params.get("p_IdPersona").getAsLong() : null)
                    .addValue("p_IdEmisor", idEmisor)
                    .addValue("p_CodigoEmisor", getS(params, "p_CodigoEmisor"))
                    .addValue("p_CodigoRegulador", getS(params, "p_CodigoRegulador"))
                    .addValue("p_Regulador", getS(params, "p_Regulador"))
                    .addValue("p_EmiteIIF", getS(params, "p_EmiteIIF"))
                    .addValue("p_EmiteIRF", getS(params, "p_EmiteIRF"))
                    .addValue("p_EmiteFungible", getS(params, "p_EmiteFungible"))
                    .addValue("p_ValorClasificacionRiesgoIIF", getS(params, "p_ValorClasificacionRiesgoIIF"))
                    .addValue("p_IndicadorAGF", getS(params, "p_IndicadorAGF"))
                    .addValue("p_IdEstado", getS(params, "p_IdEstado"))
                    .addValue("p_IdEntidadClasificadoraRiesgo", getS(params, "p_IdEntidadClasificadoraRiesgo"))
                    .addValue("p_TipoEntidad", params.has("p_TipoEntidad") && !params.get("p_TipoEntidad").isJsonNull() ? params.get("p_TipoEntidad").getAsShort() : null);

            if ("INGRESAR".equalsIgnoreCase(modo)) {
                List<Map<String, Object>> existing = jdbc.queryForList(sqlExiste,
                        new MapSqlParameterSource().addValue("p_IdEmisor", idEmisor));
                long count = existing.isEmpty() ? 0L : (existing.get(0).get("Contador") != null ? Long.parseLong(existing.get(0).get("Contador").toString()) : 0L);
                if (count == 0) {
                    jdbc.update(sqlInsert, emisorParams);
                } else {
                    JsonObject err = new JsonObject();
                    err.addProperty("IdInterno", 0);
                    err.addProperty("Codigo", "UI_EMISOR_YA_AGREGADO");
                    err.addProperty("Descripcion", "Emisor ya agregado");
                    erroresDetalle.add(err);
                    errores++;
                }
            } else {
                jdbc.update(sqlUpdate, emisorParams);
            }
        } catch (Exception e) {
            JsonObject err = new JsonObject();
            err.addProperty("Codigo", "ERROR");
            err.addProperty("Descripcion", e.getMessage());
            erroresDetalle.add(err);
            errores++;
        }

        result.addProperty("P_OUT_ERRORESCONTADOR", errores);
        result.add("P_OUT_ERRORESDETALLE", erroresDetalle);
        result.addProperty("P_OUT_ADVERTENCIASCONTADOR", 0);
        result.add("P_OUT_ADVERTENCIASDETALLE", new JsonArray());
        return result;
    }

    private String getS(JsonObject o, String k) {
        return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : null;
    }

    private String loadSql(String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes());
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
