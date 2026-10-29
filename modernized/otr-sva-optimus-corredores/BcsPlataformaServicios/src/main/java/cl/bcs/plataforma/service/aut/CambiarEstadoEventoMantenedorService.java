package cl.bcs.plataforma.service.aut;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class CambiarEstadoEventoMantenedorService implements JavaCallService {

    private final NamedParameterJdbcTemplate jdbc;

    public CambiarEstadoEventoMantenedorService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.aut.CambiarEstadoEventoMantenedor"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        try {
            String sql = loadSql("cl/bcs/plataforma/aut/sql/ModificaEstadoTipoEvento.sql");
            String idTipoEvento = params.has("p_IdTipoEvento") && !params.get("p_IdTipoEvento").isJsonNull() ? params.get("p_IdTipoEvento").getAsString() : null;
            Long idEmpresa = params.has("p_IdEmpresa") && !params.get("p_IdEmpresa").isJsonNull() ? params.get("p_IdEmpresa").getAsLong() : null;
            String idEstado = params.has("p_IdEstado") && !params.get("p_IdEstado").isJsonNull() ? params.get("p_IdEstado").getAsString() : null;

            jdbc.update(sql, new MapSqlParameterSource()
                    .addValue("IdTipoEvento", idTipoEvento)
                    .addValue("IdEmpresa", idEmpresa)
                    .addValue("IdEstado", idEstado));

            result.addProperty("P_OUT_ERRORESCONTADOR", 0L);
            result.addProperty("P_OUT_ADVERTENCIASCONTADOR", 0L);
            result.add("P_OUT_ADVERTENCIADETALLE", new JsonArray());
            result.add("P_OUT_ERRORESDETALLE", new JsonArray());
        } catch (Exception e) {
            result.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            result.addProperty("P_OUT_ADVERTENCIASCONTADOR", 0L);
            result.add("P_OUT_ADVERTENCIADETALLE", new JsonArray());
            JsonArray errs = new JsonArray(); JsonObject err = new JsonObject(); err.addProperty("Codigo", "ERROR"); err.addProperty("Descripcion", e.getMessage()); errs.add(err); result.add("P_OUT_ERRORESDETALLE", errs);
        }
        return result;
    }

    private String loadSql(String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) { return new String(is.readAllBytes()); }
        catch (Exception e) { throw new RuntimeException(e); }
    }
}
