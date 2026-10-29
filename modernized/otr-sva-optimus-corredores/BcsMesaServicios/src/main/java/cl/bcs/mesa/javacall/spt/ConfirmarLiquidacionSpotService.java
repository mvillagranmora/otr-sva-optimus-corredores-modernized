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
public class ConfirmarLiquidacionSpotService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ConfirmarLiquidacionSpotService.class);
    private static final String SQL_SEL_FP = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ConfirmarLiquidacionSpot_ConsultarFormasDePagoSpot.sql");
    private static final String SQL_SEL_OP = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ConfirmarLiquidacionSpot_ConsultarOperacionesSpot.sql");
    private static final String SQL_SEL_IVA = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ConfirmarLiquidacionSpot_SelectCobroIva.sql");
    private static final String SQL_SEL_COND = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ConfirmarLiquidacionSpot_SelectCondicionDeLiquidacion.sql");
    private static final String SQL_UPD_ESTADO = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ConfirmarLiquidacionSpot_UpdateOperacionesSpotEstadoConfirmacion.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public ConfirmarLiquidacionSpotService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource;
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.SPT.ConfirmarLiquidacionSpot"; }

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
            out.P_OUT_FP = JavaCallInfra.query(conn, SQL_SEL_FP, p, JavaCallInfra.fieldMapper(FormaPago.class));
            out.P_OUT_OP = JavaCallInfra.query(conn, SQL_SEL_OP, p, JavaCallInfra.fieldMapper(OperacionSpot.class));
            out.P_OUT_IVA = JavaCallInfra.query(conn, SQL_SEL_IVA, p, JavaCallInfra.fieldMapper(CobroIva.class));
            out.P_OUT_COND = JavaCallInfra.query(conn, SQL_SEL_COND, p, JavaCallInfra.fieldMapper(CondLiq.class));

            var jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));
            jdbc.update(SQL_UPD_ESTADO, new MapSqlParameterSource(p).addValue("IdUsuario", in.p_IdUsuario));

            plataforma.callAgregarLog(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "INFO",
                    "ConfirmarLiquidacionSpot op=" + in.p_IdOperacion);
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("ConfirmarLiquidacionSpot failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo; public Long p_IdOperacion;
    }
    public static class FormaPago { public Long IdFormaPago; public String Descripcion; public String IdMoneda; }
    public static class OperacionSpot { public Long IdOperacion; public String IdEstado; public BigDecimal Monto; }
    public static class CobroIva { public Boolean CobraIva; public BigDecimal TasaIva; }
    public static class CondLiq { public String IdCondicion; public String Descripcion; }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<FormaPago> P_OUT_FP = new ArrayList<>();
        public List<OperacionSpot> P_OUT_OP = new ArrayList<>();
        public List<CobroIva> P_OUT_IVA = new ArrayList<>();
        public List<CondLiq> P_OUT_COND = new ArrayList<>();
    }
}
