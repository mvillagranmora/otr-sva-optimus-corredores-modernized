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
public class AgregarEventoService implements JavaCallService {

    private final NamedParameterJdbcTemplate jdbc;

    public AgregarEventoService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.evt.AgregarEvento"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        try {
            String sqlInsertPaso = loadSql("cl/bcs/plataforma/evt/sql/AgregarEvento_InsertEventoTablaPaso.sql");
            String sqlInsertDirTemp = loadSql("cl/bcs/plataforma/evt/sql/InsertDirecciones_Temp.sql");
            String sqlInsertArchivosTemp = loadSql("cl/bcs/plataforma/evt/sql/AgregarEvento_InsertArchivos_Temp.sql");
            String sqlInsertValoresTemp = loadSql("cl/bcs/plataforma/evt/sql/InsertValores_Temp.sql");
            String sqlSelectPaso = loadSql("cl/bcs/plataforma/evt/sql/AgregarEvento_SelectEventoTablaPaso.sql");
            String sqlInsertOrigen = loadSql("cl/bcs/plataforma/evt/sql/AgregarEvento_InsertOrigenEvento.sql");
            String sqlInsertEvento = loadSql("cl/bcs/plataforma/evt/sql/AgregarEvento_InsertEvento.sql");
            String sqlInsertDetalle = loadSql("cl/bcs/plataforma/evt/sql/AgregarEvento_InsertEventoDetalle.sql");
            String sqlInsertAdjunto = loadSql("cl/bcs/plataforma/evt/sql/AgregarEvento_InsertEventoArchivoAdjunto.sql");
            String sqlInsertDirecciones = loadSql("cl/bcs/plataforma/evt/sql/AgregarEvento_InsertEventoDirecciones.sql");

            Long idEmpresa = params.has("p_IdEmpresa") && !params.get("p_IdEmpresa").isJsonNull() ? params.get("p_IdEmpresa").getAsLong() : null;
            Long idUsuario = params.has("p_IdUsuario") && !params.get("p_IdUsuario").isJsonNull() ? params.get("p_IdUsuario").getAsLong() : null;
            String idTipoMensaje = params.has("p_IdTipoMensaje") && !params.get("p_IdTipoMensaje").isJsonNull() ? params.get("p_IdTipoMensaje").getAsString() : null;

            MapSqlParameterSource ps = new MapSqlParameterSource()
                    .addValue("IdEmpresa", idEmpresa)
                    .addValue("IdUsuario", idUsuario)
                    .addValue("IdTipoMensaje", idTipoMensaje);

            jdbc.update(sqlInsertPaso, ps);
            List<Map<String, Object>> pasos = jdbc.queryForList(sqlSelectPaso, ps);

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
