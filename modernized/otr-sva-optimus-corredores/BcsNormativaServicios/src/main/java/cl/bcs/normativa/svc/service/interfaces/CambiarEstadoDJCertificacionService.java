package cl.bcs.normativa.svc.service.interfaces;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CambiarEstadoDJCertificacionService {

    private static final Logger log = LoggerFactory.getLogger(CambiarEstadoDJCertificacionService.class);
    private final NamedParameterJdbcTemplate jdbc;

    private static final String SQL_UPDATE = "update \"BSCL.Normativa.BD::TRI.ControlProceso\" "
        + "set \"Carga\" = 'S', \"Validacion\" = 'S', \"PreDeclaracion\" = 'S', "
        + "\"EstadoCertificado\" = (case \"EstadoCertificado\" when 'GEN' then 'GEA' else \"EstadoCertificado\" end) "
        + "WHERE \"Esquema\"= :Esquema AND \"Version\" = :Version";

    public CambiarEstadoDJCertificacionService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("CambiarEstadoDJCertificacionService.execute called");
        MapSqlParameterSource p = new MapSqlParameterSource();
        p.addValue("Esquema", params.has("Esquema") ? params.get("Esquema").getAsString() : null);
        p.addValue("Version", params.has("Version") ? params.get("Version").getAsString() : null);
        jdbc.update(SQL_UPDATE, p);
        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
        return out;
    }
}
