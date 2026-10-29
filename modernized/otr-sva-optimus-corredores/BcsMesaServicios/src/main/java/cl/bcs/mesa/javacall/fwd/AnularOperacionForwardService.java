package cl.bcs.mesa.javacall.fwd;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnularOperacionForwardService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(AnularOperacionForwardService.class);
    private static final String SQL_SEL_OP = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/AnularOperacionForward_ConsultarOperacionesForward.sql");
    private static final String SQL_DEL_RESERVA_FOLIO = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/AnularOperacionForward_DeleteReservaFolioCarteraOrigen.sql");
    private static final String SQL_DEL_RESERVA_OP = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/AnularOperacionForward_DeleteReservaIdOperacion.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public AnularOperacionForwardService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource;
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.FWD.AnularOperacionForward"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);

            JsonObject reqResult = plataforma.callRevisarRequisitosBasicos(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
            if (reqResult.has("P_OUT_ERRORESCONTADOR") && reqResult.get("P_OUT_ERRORESCONTADOR").getAsLong() > 0) {
                return reqResult;
            }

            Map<String, Object> p = new HashMap<>();
            p.put("IdEmpresa", in.p_IdEmpresa);
            p.put("IdOperacion", in.p_IdOperacion);

            Salida out = new Salida();
            out.P_OUT_OPERACION = JavaCallInfra.query(conn, SQL_SEL_OP, p, JavaCallInfra.fieldMapper(OperacionFwd.class));

            var jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));
            var pUpd = new MapSqlParameterSource(p).addValue("IdUsuario", in.p_IdUsuario);
            jdbc.update(SQL_DEL_RESERVA_FOLIO, pUpd);
            jdbc.update(SQL_DEL_RESERVA_OP, pUpd);

            plataforma.callAgregarLog(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "INFO",
                    "AnularOperacionForward op=" + in.p_IdOperacion);
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("AnularOperacionForward failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public Long p_IdOperacion; public String p_MotivoAnulacion;
    }
    public static class OperacionFwd { public Long IdOperacion; public String IdEstado; public BigDecimal MontoCompra; public BigDecimal MontoVenta; }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<OperacionFwd> P_OUT_OPERACION = new ArrayList<>();
    }
}
