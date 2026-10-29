package cl.bcs.mesa.javacall.spt;

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
public class CrearPuntasSpotService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(CrearPuntasSpotService.class);
    private static final String SQL_INS = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/CrearPuntasSpot_InsertPuntas.sql");
    private static final String SQL_UPD = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/CrearPuntasSpot_UpdatePuntas.sql");
    private static final String SQL_UPD_TRAMO = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/CrearPuntasSpot_UpdatePuntasPorTramo.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public CrearPuntasSpotService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource;
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.SPT.CrearPuntasSpot"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);

            var jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));

            if (in.p_Puntas != null) {
                for (PuntaIn punta : in.p_Puntas) {
                    var p = new MapSqlParameterSource()
                            .addValue("IdEmpresa", in.p_IdEmpresa)
                            .addValue("IdMoneda", punta.IdMoneda)
                            .addValue("Compra", punta.Compra)
                            .addValue("Venta", punta.Venta)
                            .addValue("IdUsuario", in.p_IdUsuario);
                    // Try update first, then insert
                    int rows = jdbc.update(SQL_UPD, p);
                    if (rows == 0) {
                        jdbc.update(SQL_INS, p);
                    }
                    jdbc.update(SQL_UPD_TRAMO, p);
                }
            }

            plataforma.callAgregarLog(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "INFO",
                    "CrearPuntasSpot ejecutado");
            return GSON.fromJson("{\"P_OUT_ERRORESCONTADOR\":0}", JsonObject.class);
        } catch (Exception e) {
            log.error("CrearPuntasSpot failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class PuntaIn { public String IdMoneda; public BigDecimal Compra; public BigDecimal Venta; }
    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public List<PuntaIn> p_Puntas;
    }
}
