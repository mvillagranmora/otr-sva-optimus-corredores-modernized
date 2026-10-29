package cl.bcs.mesa.javacall.mrf;

import cl.bcs.mesa.javacall.JavaCallInfra;
import cl.bcs.mesa.javacall.JavaCallService;
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
import java.util.List;

@Service
public class MarcarOperacionMandatoService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(MarcarOperacionMandatoService.class);
    private static final String SQL_UPDATE = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/MarcarOperacionMandato_UpdateOperaciones.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    public MarcarOperacionMandatoService(DataSource dataSource) { this.dataSource = dataSource; }

    @Override public String fqn() { return "cl.bcs.mesa.MRF.MarcarOperacionMandato"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);
            var jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));

            if (in.p_Operaciones != null) {
                for (OperacionIn op : in.p_Operaciones) {
                    var p = new MapSqlParameterSource()
                            .addValue("IdEmpresa", in.p_IdEmpresa)
                            .addValue("IdOperacion", op.IdOperacion)
                            .addValue("IdMandato", op.IdMandato)
                            .addValue("IdUsuario", in.p_IdUsuario);
                    jdbc.update(SQL_UPDATE, p);
                }
            }
            Salida out = new Salida();
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("MarcarOperacionMandato failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class OperacionIn { public Long IdOperacion; public Long IdMandato; }
    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public List<OperacionIn> p_Operaciones;
    }
    public static class Salida { public Long P_OUT_ERRORESCONTADOR = 0L; }
}
