package cl.bcs.plataforma.service.aut;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CrearEtapaAutorizacionService implements JavaCallService {

    private final NamedParameterJdbcTemplate jdbc;

    public CrearEtapaAutorizacionService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.aut.CrearEtapaAutorizacion"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        JsonArray mensajes = new JsonArray();
        try {
            String sqlSelectTipoEvento = loadSql("cl/bcs/plataforma/aut/sql/CrearEtapaAutorizacion_SelectTipoEvento.sql");
            String sqlSelectEstado = loadSql("cl/bcs/plataforma/aut/sql/CrearEtapaAutorizacion_SelectEstado.sql");
            String sqlSelectEtapa = loadSql("cl/bcs/plataforma/aut/sql/CrearEtapaAutorizacion_SelectEtapa.sql");
            String sqlInsert = loadSql("cl/bcs/plataforma/aut/sql/CrearEtapaAutorizacion_InsertEtapa.sql");

            Long idEmpresa = params.has("p_IdEmpresa") && !params.get("p_IdEmpresa").isJsonNull() ? params.get("p_IdEmpresa").getAsLong() : null;

            MapSqlParameterSource ps = new MapSqlParameterSource().addValue("IdEmpresa", idEmpresa);
            List<Map<String, Object>> estados = jdbc.queryForList(sqlSelectEstado, new MapSqlParameterSource());

            if (params.has("p_EtapaAutorizacion") && params.get("p_EtapaAutorizacion").isJsonArray()) {
                for (var el : params.getAsJsonArray("p_EtapaAutorizacion")) {
                    JsonObject etapa = el.getAsJsonObject();
                    String idTipoEvento = etapa.has("IdTipoEvento") && !etapa.get("IdTipoEvento").isJsonNull() ? etapa.get("IdTipoEvento").getAsString() : null;

                    List<Map<String, Object>> tiposEvento = jdbc.queryForList(sqlSelectTipoEvento,
                            new MapSqlParameterSource().addValue("IdEmpresa", idEmpresa).addValue("IdTipoEvento", idTipoEvento));
                    List<Map<String, Object>> etapasExistentes = jdbc.queryForList(sqlSelectEtapa,
                            new MapSqlParameterSource().addValue("IdEmpresa", idEmpresa).addValue("IdTipoEvento", idTipoEvento));

                    MapSqlParameterSource insertPs = new MapSqlParameterSource()
                            .addValue("IdEmpresa", idEmpresa)
                            .addValue("IdTipoEvento", idTipoEvento)
                            .addValue("Etapa", etapa.has("Etapa") && !etapa.get("Etapa").isJsonNull() ? etapa.get("Etapa").getAsLong() : null)
                            .addValue("TipoAutorizacion", etapa.has("TipoAutorizacion") && !etapa.get("TipoAutorizacion").isJsonNull() ? etapa.get("TipoAutorizacion").getAsString() : null)
                            .addValue("NroAutorizaciones", etapa.has("NroAutorizaciones") && !etapa.get("NroAutorizaciones").isJsonNull() ? etapa.get("NroAutorizaciones").getAsLong() : null)
                            .addValue("IdEstado", etapa.has("IdEstado") && !etapa.get("IdEstado").isJsonNull() ? etapa.get("IdEstado").getAsString() : "ACT");
                    jdbc.update(sqlInsert, insertPs);
                }
            }

            result.addProperty("P_OUT_ERRORESCONTADOR", 0L);
            result.add("P_OUT_MENSAJES", mensajes);
        } catch (Exception e) {
            JsonObject msg = new JsonObject(); msg.addProperty("Tipo", "ERROR"); msg.addProperty("Descripcion", e.getMessage()); mensajes.add(msg);
            result.addProperty("P_OUT_ERRORESCONTADOR", 1L); result.add("P_OUT_MENSAJES", mensajes);
        }
        return result;
    }

    private String loadSql(String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) { return new String(is.readAllBytes()); }
        catch (Exception e) { throw new RuntimeException(e); }
    }
}
