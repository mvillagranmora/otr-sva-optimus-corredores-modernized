package cl.bcs.plataforma.service.evt;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AgregarEmailService implements JavaCallService {

    private final NamedParameterJdbcTemplate jdbc;

    public AgregarEmailService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.evt.AgregarEmail"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        try {
            String sqlObtenerIdEmail = loadSql("cl/bcs/plataforma/evt/sql/ObtenerIdEmail.sql");
            String sqlInsertaEmail = loadSql("cl/bcs/plataforma/evt/sql/InsertaEmail.sql");
            String sqlInsertaDestinatario = loadSql("cl/bcs/plataforma/evt/sql/InsertaDestinatario.sql");
            String sqlInsertaAdjunto = loadSql("cl/bcs/plataforma/evt/sql/InsertaArchivoAdjunto.sql");

            // TODO: requires pla.* utility — complete after pla subpackage migration
            // ConfiguracionSistema.getParametroConfiguracionString needed for PLA.ValorTandaArchivoAdjunto parameter

            Long idEmpresa = params.has("p_IdEmpresa") && !params.get("p_IdEmpresa").isJsonNull() ? params.get("p_IdEmpresa").getAsLong() : null;

            List<Map<String, Object>> idEmailList = jdbc.queryForList(sqlObtenerIdEmail,
                    new MapSqlParameterSource().addValue("IdEmpresa", idEmpresa));
            Long idEmail = idEmailList.isEmpty() ? null : Long.parseLong(idEmailList.get(0).values().iterator().next().toString());

            MapSqlParameterSource ps = new MapSqlParameterSource()
                    .addValue("IdEmail", idEmail)
                    .addValue("IdEmpresa", idEmpresa)
                    .addValue("Asunto", params.has("p_Asunto") && !params.get("p_Asunto").isJsonNull() ? params.get("p_Asunto").getAsString() : null)
                    .addValue("Cuerpo", params.has("p_Cuerpo") && !params.get("p_Cuerpo").isJsonNull() ? params.get("p_Cuerpo").getAsString() : null)
                    .addValue("IdTanda", params.has("p_IdTanda") && !params.get("p_IdTanda").isJsonNull() ? params.get("p_IdTanda").getAsLong() : null)
                    .addValue("IdTipoEmail", params.has("p_IdTipoEmail") && !params.get("p_IdTipoEmail").isJsonNull() ? params.get("p_IdTipoEmail").getAsString() : null);
            jdbc.update(sqlInsertaEmail, ps);

            if (params.has("p_Destinatarios") && params.get("p_Destinatarios").isJsonArray()) {
                for (var el : params.getAsJsonArray("p_Destinatarios")) {
                    JsonObject dest = el.getAsJsonObject();
                    MapSqlParameterSource destPs = new MapSqlParameterSource()
                            .addValue("IdEmail", idEmail)
                            .addValue("Email", dest.has("Email") && !dest.get("Email").isJsonNull() ? dest.get("Email").getAsString() : null)
                            .addValue("TipoDestinatario", dest.has("TipoDestinatario") && !dest.get("TipoDestinatario").isJsonNull() ? dest.get("TipoDestinatario").getAsString() : null);
                    jdbc.update(sqlInsertaDestinatario, destPs);
                }
            }

            result.addProperty("P_OUT_IDEMAILOUT", idEmail);
            result.addProperty("P_OUT_ERRORESCONTADOR", 0L);
            result.add("P_OUT_MENSAJES", new JsonArray());
        } catch (Exception e) {
            result.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            JsonArray msgs = new JsonArray(); JsonObject msg = new JsonObject(); msg.addProperty("Tipo", "ERROR"); msg.addProperty("Descripcion", e.getMessage()); msgs.add(msg); result.add("P_OUT_MENSAJES", msgs);
        }
        return result;
    }

    private String loadSql(String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) { return new String(is.readAllBytes()); }
        catch (Exception e) { throw new RuntimeException(e); }
    }
}
