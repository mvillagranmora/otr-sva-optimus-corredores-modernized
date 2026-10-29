package cl.bcs.mesa.javacall.spt.ws;

import cl.bcs.mesa.javacall.JavaCallInfra;
import cl.bcs.mesa.javacall.JavaCallService;
import cl.bcs.mesa.javacall.PlataformaBridge;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;

@Service
public class AnularOperacionSpotWsService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(AnularOperacionSpotWsService.class);
    private static final String SQL_UPD = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/AnularOperacionSpot_UpdateOperacionesSpotEstado.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public AnularOperacionSpotWsService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource;
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.SPT.ws.AnularOperacionSpotWs"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);
            var jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));
            var p = new MapSqlParameterSource()
                    .addValue("IdEmpresa", in.p_IdEmpresa)
                    .addValue("IdOperacion", in.p_IdOperacion)
                    .addValue("MotivoAnulacion", in.p_MotivoAnulacion)
                    .addValue("IdUsuario", in.p_IdUsuario);
            jdbc.update(SQL_UPD, p);
            plataforma.callAgregarLog(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "INFO",
                    "AnularOperacionSpotWs op=" + in.p_IdOperacion);
            return GSON.fromJson("{\"P_OUT_ERRORESCONTADOR\":0}", JsonObject.class);
        } catch (Exception e) {
            log.error("AnularOperacionSpotWs failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public Long p_IdOperacion; public String p_MotivoAnulacion;
    }
}
