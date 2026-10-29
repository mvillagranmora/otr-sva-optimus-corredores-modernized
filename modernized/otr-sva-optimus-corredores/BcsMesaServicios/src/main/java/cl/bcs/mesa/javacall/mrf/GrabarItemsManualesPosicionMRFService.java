package cl.bcs.mesa.javacall.mrf;

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
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;

@Service
public class GrabarItemsManualesPosicionMRFService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(GrabarItemsManualesPosicionMRFService.class);
    private static final String SQL_CREATE_TABLE = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/GrabarItemsManualesPosicionMRF_CreateTable.sql");
    private static final String SQL_INSERT_TABLE = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/GrabarItemsManualesPosicionMRF_InsertTable.sql");
    private static final String SQL_DROP_TABLE = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/GrabarItemsManualesPosicionMRF_DropTable.sql");
    private static final String SQL_CALL = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/GrabarItemsManualesPosicionMRFCall.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public GrabarItemsManualesPosicionMRFService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource; this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.MRF.GrabarItemsManualesPosicionMRF"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);
            var jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));

            // Create temp table, insert items, call SP, drop table
            jdbc.getJdbcTemplate().execute(SQL_CREATE_TABLE);

            if (in.p_Items != null) {
                for (ItemManual item : in.p_Items) {
                    var p = new MapSqlParameterSource()
                            .addValue("IdEmpresa", in.p_IdEmpresa)
                            .addValue("IdMatriz", item.IdMatriz)
                            .addValue("MontoEvento", item.MontoEvento)
                            .addValue("Fecha", in.p_Fecha);
                    jdbc.update(SQL_INSERT_TABLE, p);
                }
            }

            var cp = new MapSqlParameterSource()
                    .addValue("IdEmpresa", in.p_IdEmpresa)
                    .addValue("IdUsuario", in.p_IdUsuario)
                    .addValue("IdModulo", in.p_IdModulo)
                    .addValue("Fecha", in.p_Fecha);
            jdbc.update(SQL_CALL, cp);
            jdbc.getJdbcTemplate().execute(SQL_DROP_TABLE);

            plataforma.callAgregarLog(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "INFO", "GrabarItemsManualesPosicionMRF ejecutado");
            Salida out = new Salida();
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("GrabarItemsManualesPosicionMRF failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class ItemManual { public Long IdMatriz; public BigDecimal MontoEvento; }
    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public String p_Fecha; public List<ItemManual> p_Items;
    }
    public static class Salida { public Long P_OUT_ERRORESCONTADOR = 0L; }
}
