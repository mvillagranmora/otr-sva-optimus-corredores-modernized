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
public class ConfirmarRechazarAutorizacionOperacionForwardService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ConfirmarRechazarAutorizacionOperacionForwardService.class);
    private static final String SQL_CREATE_TEMP = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/ConfirmarRechazarAutorizacionOperacionForward_CreateLocalTempTableEventoOperacionForward.sql");
    private static final String SQL_DROP_TEMP = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/ConfirmarRechazarAutorizacionOperacionForward_DropLocalTempTableEventoOperacionForward.sql");
    private static final String SQL_INSERT_TEMP = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/ConfirmarRechazarAutorizacionOperacionForward_InsertTempEventoOperacionForward.sql");
    private static final String SQL_SEL_OP = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/ConfirmarRechazarAutorizacionOperacionForward_SelectOperacionForward.sql");
    private static final String SQL_SEL_EVENTO = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/ConfirmarRechazarAutorizacionOperacionForward_SelectEventoOperacionForward.sql");
    private static final String SQL_UPD_CARTERA = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/ConfirmarRechazarAutorizacionOperacionForward_UpdateEstadoCartera.sql");
    private static final String SQL_UPD_ESTADO = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/ConfirmarRechazarAutorizacionOperacionForward_UpdateEstadoOperacion.sql");
    private static final String SQL_UPD_EVENTO = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/ConfirmarRechazarAutorizacionOperacionForward_UpdateEvento.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public ConfirmarRechazarAutorizacionOperacionForwardService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource;
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.FWD.ConfirmarRechazarAutorizacionOperacionForward"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);

            JsonObject reqResult = plataforma.callRevisarRequisitosBasicos(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
            if (reqResult.has("P_OUT_ERRORESCONTADOR") && reqResult.get("P_OUT_ERRORESCONTADOR").getAsLong() > 0) {
                return reqResult;
            }

            var jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));
            jdbc.getJdbcTemplate().execute(SQL_CREATE_TEMP);

            if (in.p_Operaciones != null) {
                for (OperacionIn op : in.p_Operaciones) {
                    var pIns = new MapSqlParameterSource()
                            .addValue("IdEmpresa", in.p_IdEmpresa)
                            .addValue("IdOperacion", op.IdOperacion)
                            .addValue("IdEvento", op.IdEvento)
                            .addValue("Confirmado", op.Confirmado);
                    jdbc.update(SQL_INSERT_TEMP, pIns);
                }
            }

            Map<String, Object> base = new HashMap<>();
            base.put("IdEmpresa", in.p_IdEmpresa);
            base.put("IdUsuario", in.p_IdUsuario);
            base.put("IdModulo", in.p_IdModulo);

            jdbc.update(SQL_UPD_CARTERA, base);
            jdbc.update(SQL_UPD_ESTADO, base);
            jdbc.update(SQL_UPD_EVENTO, base);

            Salida out = new Salida();
            out.P_OUT_OPERACION = JavaCallInfra.query(conn, SQL_SEL_OP, base, JavaCallInfra.fieldMapper(OperacionOut.class));
            out.P_OUT_EVENTO = JavaCallInfra.query(conn, SQL_SEL_EVENTO, base, JavaCallInfra.fieldMapper(Evento.class));

            jdbc.getJdbcTemplate().execute(SQL_DROP_TEMP);

            plataforma.callAgregarLog(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "INFO",
                    "ConfirmarRechazarAutorizacionOperacionForward ejecutado");
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("ConfirmarRechazarAutorizacionOperacionForward failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class OperacionIn { public Long IdOperacion; public Long IdEvento; public Boolean Confirmado; }
    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public List<OperacionIn> p_Operaciones;
    }
    public static class OperacionOut { public Long IdOperacion; public String IdEstado; public BigDecimal MontoCompra; public BigDecimal MontoVenta; }
    public static class Evento { public Long IdEvento; public Long IdOperacion; public String IdEstado; }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<OperacionOut> P_OUT_OPERACION = new ArrayList<>();
        public List<Evento> P_OUT_EVENTO = new ArrayList<>();
    }
}
