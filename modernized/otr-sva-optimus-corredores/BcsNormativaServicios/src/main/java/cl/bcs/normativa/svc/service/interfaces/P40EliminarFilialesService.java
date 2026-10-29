package cl.bcs.normativa.svc.service.interfaces;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class P40EliminarFilialesService {

    private static final Logger log = LoggerFactory.getLogger(P40EliminarFilialesService.class);
    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_DELETE;

    public P40EliminarFilialesService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_DELETE = loadSql("cl/bcs/normativa/interfaces/sql/P40EliminarFiliales_DeleteFiliales.sql");
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("P40EliminarFilialesService.execute called");
        JsonObject out = new JsonObject();
        Long idSocioDeNegocio = params.has("p_IdSocioDeNegocio") ? params.get("p_IdSocioDeNegocio").getAsLong() : 0L;
        Long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;

        if (idSocioDeNegocio == null || idSocioDeNegocio == 0L) {
            out.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            out.addProperty("P_OUT_MENSAJE", "No hay Datos de entrada");
            return out;
        }

        try {
            MapSqlParameterSource p = new MapSqlParameterSource();
            p.addValue("IdEmpresa", idEmpresa);
            p.addValue("IdSocioDeNegocio", idSocioDeNegocio);
            jdbc.update(SQL_DELETE, p);
            out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
            out.addProperty("P_OUT_MENSAJE", "Datos Eliminados Exitosamente");
        } catch (Exception e) {
            log.error("Error en P40EliminarFiliales", e);
            out.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            out.addProperty("P_OUT_MENSAJE", "Filial no existe o error al eliminar: " + e.getMessage());
        }
        return out;
    }

    private static String loadSql(String path) {
        try (var is = P40EliminarFilialesService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) { log.warn("SQL resource not found: {}", path); return ""; }
            return new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) { throw new RuntimeException("Failed to load SQL: " + path, e); }
    }
}
