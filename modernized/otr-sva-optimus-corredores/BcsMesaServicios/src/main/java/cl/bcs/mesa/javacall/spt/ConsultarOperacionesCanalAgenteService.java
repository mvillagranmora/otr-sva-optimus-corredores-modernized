package cl.bcs.mesa.javacall.spt;

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

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConsultarOperacionesCanalAgenteService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarOperacionesCanalAgenteService.class);
    private static final String SQL_DEL = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ConsultarOperacionesCanalAgente_DeleteIdPortafolioTablaPaso.sql");
    private static final String SQL_INS_FULL = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ConsultarOperacionesCanalAgente_InsertFullIdPortafolioTablaPaso.sql");
    private static final String SQL_INS = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ConsultarOperacionesCanalAgente_InsertIdPortafolioTablaPaso.sql");
    private static final String SQL_SEL = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ConsultarOperacionesCanalAgente_SelectOperaciones.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    public ConsultarOperacionesCanalAgenteService(DataSource dataSource) { this.dataSource = dataSource; }

    @Override public String fqn() { return "cl.bcs.mesa.SPT.ConsultarOperacionesCanalAgente"; }

    @Override
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);

            Map<String, Object> p = new HashMap<>();
            p.put("IdEmpresa", in.p_IdEmpresa);
            p.put("IdUsuario", in.p_IdUsuario);
            p.put("Fecha", in.p_Fecha);

            var jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));
            var mp = new MapSqlParameterSource(p);
            jdbc.update(SQL_DEL, mp);
            // Insert full or filtered depending on whether portafolios param present
            if (in.p_IdPortafolios == null || in.p_IdPortafolios.isEmpty()) {
                jdbc.update(SQL_INS_FULL, mp);
            } else {
                for (Long idPortafolio : in.p_IdPortafolios) {
                    jdbc.update(SQL_INS, new MapSqlParameterSource(p).addValue("IdPortafolio", idPortafolio));
                }
            }

            Salida out = new Salida();
            out.P_OUT_OPERACIONES = JavaCallInfra.query(conn, SQL_SEL, p, JavaCallInfra.fieldMapper(Operacion.class));
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("ConsultarOperacionesCanalAgente failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public String p_Fecha; public List<Long> p_IdPortafolios;
    }
    public static class Operacion { public Long IdOperacion; public String IdEstado; public BigDecimal Monto; public String IdMoneda; public Long IdPortafolio; }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Operacion> P_OUT_OPERACIONES = new ArrayList<>();
    }
}
