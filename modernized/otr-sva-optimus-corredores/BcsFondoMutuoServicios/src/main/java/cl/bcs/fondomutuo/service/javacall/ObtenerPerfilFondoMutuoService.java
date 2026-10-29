package cl.bcs.fondomutuo.service.javacall;

import com.google.gson.JsonObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Spring @Service migration of legacy JavaCall cl.bcs.fondomutuo.ObtenerPerfilFondoMutuo.
 */
@Service
public class ObtenerPerfilFondoMutuoService {

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlPerfilFondoMutuo;

    public ObtenerPerfilFondoMutuoService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlPerfilFondoMutuo = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/Comunes_SelectPerfilFondoMutuo.sql").getInputStream(),
            StandardCharsets.UTF_8);
    }

    public JsonObject execute(JsonObject params) {
        long idUsuario = params.has("p_IdUsuario") ? params.get("p_IdUsuario").getAsLong() : 0L;

        MapSqlParameterSource ps = new MapSqlParameterSource("IdUsuario", idUsuario);
        List<Map<String, Object>> rows = jdbc.queryForList(sqlPerfilFondoMutuo, ps);

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        if (!rows.isEmpty()) {
            Object perfil = rows.get(0).get("Perfil");
            out.addProperty("PerfilFondoMutuo", perfil != null ? perfil.toString() : "");
        } else {
            out.addProperty("PerfilFondoMutuo", "");
        }
        return out;
    }
}
