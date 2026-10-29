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
public class ModificarAutorizadoEtapaService implements JavaCallService {

    private final NamedParameterJdbcTemplate jdbc;

    public ModificarAutorizadoEtapaService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.aut.ModificarAutorizadoEtapa"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        JsonArray mensajes = new JsonArray();
        try {
            String sqlSelectEtapa = loadSql("cl/bcs/plataforma/aut/sql/AutorizadoEtapa_SelectEtapa.sql");
            String sqlSelectUsuario = loadSql("cl/bcs/plataforma/aut/sql/AutorizadoEtapa_SelectUsuario.sql");
            String sqlSelectAutorizadoEtapa = loadSql("cl/bcs/plataforma/aut/sql/AutorizadoEtapa_SelectAutorizadoEtapa.sql");
            String sqlSelectRol = loadSql("cl/bcs/plataforma/aut/sql/AutorizadoEtapa_SelectRol.sql");
            String sqlSelectRolUsuario = loadSql("cl/bcs/plataforma/aut/sql/AutorizadoEtapa_SelectRolUsuario.sql");
            String sqlUpdate = loadSql("cl/bcs/plataforma/aut/sql/AutorizadoEtapa_UpdateAutorizadoEtapa.sql");

            Long idEmpresa = params.has("p_IdEmpresa") && !params.get("p_IdEmpresa").isJsonNull() ? params.get("p_IdEmpresa").getAsLong() : null;

            MapSqlParameterSource ps = new MapSqlParameterSource().addValue("IdEmpresa", idEmpresa);
            List<Map<String, Object>> etapas = jdbc.queryForList(sqlSelectEtapa, ps);
            List<Map<String, Object>> usuarios = jdbc.queryForList(sqlSelectUsuario, ps);
            List<Map<String, Object>> autorizados = jdbc.queryForList(sqlSelectAutorizadoEtapa, ps);
            List<Map<String, Object>> roles = jdbc.queryForList(sqlSelectRol, ps);
            List<Map<String, Object>> rolesUsuario = jdbc.queryForList(sqlSelectRolUsuario, ps);

            if (params.has("p_AutorizadoEtapa") && params.get("p_AutorizadoEtapa").isJsonArray()) {
                for (var el : params.getAsJsonArray("p_AutorizadoEtapa")) {
                    JsonObject ae = el.getAsJsonObject();
                    MapSqlParameterSource updatePs = new MapSqlParameterSource()
                            .addValue("IdEmpresa", idEmpresa)
                            .addValue("IdTipoEvento", ae.has("IdTipoEvento") && !ae.get("IdTipoEvento").isJsonNull() ? ae.get("IdTipoEvento").getAsString() : null)
                            .addValue("Etapa", ae.has("Etapa") && !ae.get("Etapa").isJsonNull() ? ae.get("Etapa").getAsLong() : null)
                            .addValue("IdUsuario", ae.has("IdUsuario") && !ae.get("IdUsuario").isJsonNull() ? ae.get("IdUsuario").getAsLong() : null)
                            .addValue("IdRol", ae.has("IdRol") && !ae.get("IdRol").isJsonNull() ? ae.get("IdRol").getAsLong() : null)
                            .addValue("IdEstado", ae.has("IdEstado") && !ae.get("IdEstado").isJsonNull() ? ae.get("IdEstado").getAsString() : "ACT");
                    jdbc.update(sqlUpdate, updatePs);
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
