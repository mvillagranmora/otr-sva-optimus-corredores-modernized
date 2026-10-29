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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConfirmarRechazarAutorizacionOperacionSpotService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ConfirmarRechazarAutorizacionOperacionSpotService.class);
    private static final String SQL_CREATE_EVT = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ConfirmarRechazarAutorizacionOperacionSpot_CreateLocalTempTableEventoOperacionSpot.sql");
    private static final String SQL_DROP_EVT = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ConfirmarRechazarAutorizacionOperacionSpot_DropLocalTempTableEventoOperacionSpot.sql");
    private static final String SQL_INS_EVT = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ConfirmarRechazarAutorizacionOperacionSpot_InsertTempEventoOperacionSpot.sql");
    private static final String SQL_CREATE_FP = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ConfirmarRechazarAutorizacionOperacionSpot_CreateLocalTempTableFormaPago.sql");
    private static final String SQL_DROP_FP = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ConfirmarRechazarAutorizacionOperacionSpot_DropLocalTempTableFormaPago.sql");
    private static final String SQL_INS_FP = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ConfirmarRechazarAutorizacionOperacionSpot_InsertTempFormaPago.sql");
    private static final String SQL_CREATE_OP = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ConfirmarRechazarAutorizacionOperacionSpot_CreateLocalTempTableOperacionSpot.sql");
    private static final String SQL_DROP_OP = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ConfirmarRechazarAutorizacionOperacionSpot_DropLocalTempTableOperacionSpot.sql");
    private static final String SQL_INS_OP = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ConfirmarRechazarAutorizacionOperacionSpot_InsertTempOperacionSpot.sql");
    private static final String SQL_SEL_EVT = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ConfirmarRechazarAutorizacionOperacionSpot_SelectEventoOperacionSpot.sql");
    private static final String SQL_SEL_FP = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ConfirmarRechazarAutorizacionOperacionSpot_SelectFormaPago.sql");
    private static final String SQL_SEL_OP = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ConfirmarRechazarAutorizacionOperacionSpot_SelectOperacionSpot.sql");
    private static final String SQL_UPD_ESTADO = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ConfirmarRechazarAutorizacionOperacionSpot_UpdateEstadoOperacion.sql");
    private static final String SQL_UPD_EVT = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ConfirmarRechazarAutorizacionOperacionSpot_UpdateEvento.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public ConfirmarRechazarAutorizacionOperacionSpotService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource;
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.SPT.ConfirmarRechazarAutorizacionOperacionSpot"; }

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
            jdbc.getJdbcTemplate().execute(SQL_CREATE_EVT);
            jdbc.getJdbcTemplate().execute(SQL_CREATE_FP);
            jdbc.getJdbcTemplate().execute(SQL_CREATE_OP);

            if (in.p_Operaciones != null) {
                for (OperacionIn op : in.p_Operaciones) {
                    var pIns = new MapSqlParameterSource()
                            .addValue("IdEmpresa", in.p_IdEmpresa)
                            .addValue("IdOperacion", op.IdOperacion)
                            .addValue("IdEvento", op.IdEvento)
                            .addValue("Confirmado", op.Confirmado);
                    jdbc.update(SQL_INS_EVT, pIns);
                    jdbc.update(SQL_INS_OP, pIns);
                    if (op.FormasPago != null) {
                        for (FormaPagoIn fp : op.FormasPago) {
                            jdbc.update(SQL_INS_FP, new MapSqlParameterSource()
                                    .addValue("IdOperacion", op.IdOperacion)
                                    .addValue("IdFormaPago", fp.IdFormaPago)
                                    .addValue("Monto", fp.Monto));
                        }
                    }
                }
            }

            Map<String, Object> base = new HashMap<>();
            base.put("IdEmpresa", in.p_IdEmpresa);
            base.put("IdUsuario", in.p_IdUsuario);
            base.put("IdModulo", in.p_IdModulo);

            jdbc.update(SQL_UPD_ESTADO, base);
            jdbc.update(SQL_UPD_EVT, base);

            Salida out = new Salida();
            out.P_OUT_OP = JavaCallInfra.query(conn, SQL_SEL_OP, base, JavaCallInfra.fieldMapper(OperacionOut.class));
            out.P_OUT_EVT = JavaCallInfra.query(conn, SQL_SEL_EVT, base, JavaCallInfra.fieldMapper(Evento.class));
            out.P_OUT_FP = JavaCallInfra.query(conn, SQL_SEL_FP, base, JavaCallInfra.fieldMapper(FormaPagoOut.class));

            jdbc.getJdbcTemplate().execute(SQL_DROP_EVT);
            jdbc.getJdbcTemplate().execute(SQL_DROP_FP);
            jdbc.getJdbcTemplate().execute(SQL_DROP_OP);

            plataforma.callAgregarLog(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "INFO",
                    "ConfirmarRechazarAutorizacionOperacionSpot ejecutado");
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("ConfirmarRechazarAutorizacionOperacionSpot failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class FormaPagoIn { public Long IdFormaPago; public BigDecimal Monto; }
    public static class OperacionIn { public Long IdOperacion; public Long IdEvento; public Boolean Confirmado; public List<FormaPagoIn> FormasPago; }
    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public List<OperacionIn> p_Operaciones;
    }
    public static class OperacionOut { public Long IdOperacion; public String IdEstado; public BigDecimal Monto; }
    public static class Evento { public Long IdEvento; public Long IdOperacion; public String IdEstado; }
    public static class FormaPagoOut { public Long IdOperacion; public Long IdFormaPago; public BigDecimal Monto; }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<OperacionOut> P_OUT_OP = new ArrayList<>();
        public List<Evento> P_OUT_EVT = new ArrayList<>();
        public List<FormaPagoOut> P_OUT_FP = new ArrayList<>();
    }
}
