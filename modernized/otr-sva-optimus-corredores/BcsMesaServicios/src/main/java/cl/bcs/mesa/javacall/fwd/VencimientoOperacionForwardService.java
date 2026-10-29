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
public class VencimientoOperacionForwardService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(VencimientoOperacionForwardService.class);
    private static final String SQL_SEL_OP = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/VencimientoOperacionForward_ConsultarOperacionesForward.sql");
    private static final String SQL_SEL_OP_ENTREGA = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/VencimientoOperacionForward_ConsultarOperacionesForwardEntregaFisica.sql");
    private static final String SQL_SEL_OP_ENTREGA_FP = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/VencimientoOperacionForward_ConsultarOperacionesForwardEntregaFisicaFormaDePago.sql");
    private static final String SQL_SEL_OP_ENTREGA_SIN_SPOT = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/VencimientoOperacionForward_ConsultarOperacionesForwardEntregaFisicaSinSpot.sql");
    private static final String SQL_UPD_CARTERA_ANTICIP = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/VencimientoOperacionForward_UpdateCarteraAnticipadaIdEstado.sql");
    private static final String SQL_UPD_CARTERA_REPROCESO = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/VencimientoOperacionForward_UpdateCarteraReproceso.sql");
    private static final String SQL_UPD_CARTERA_VEN_ENTREGA = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/VencimientoOperacionForward_UpdateCarteraVencimientoEntregaFisica.sql");
    private static final String SQL_UPD_CARTERA_VEN_MON = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/VencimientoOperacionForward_UpdateCarteraVencimientoEntregaFisicaMonedaLiquidacion.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public VencimientoOperacionForwardService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource;
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.FWD.VencimientoOperacionForward"; }

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
            p.put("Fecha", in.p_Fecha);

            Salida out = new Salida();
            out.P_OUT_OP = JavaCallInfra.query(conn, SQL_SEL_OP, p, JavaCallInfra.fieldMapper(OperacionFwd.class));
            out.P_OUT_OP_ENTREGA = JavaCallInfra.query(conn, SQL_SEL_OP_ENTREGA, p, JavaCallInfra.fieldMapper(OperacionFwd.class));
            out.P_OUT_OP_ENTREGA_FP = JavaCallInfra.query(conn, SQL_SEL_OP_ENTREGA_FP, p, JavaCallInfra.fieldMapper(OperacionFwd.class));
            out.P_OUT_OP_ENTREGA_SIN = JavaCallInfra.query(conn, SQL_SEL_OP_ENTREGA_SIN_SPOT, p, JavaCallInfra.fieldMapper(OperacionFwd.class));

            var jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));
            var pUpd = new MapSqlParameterSource(p).addValue("IdUsuario", in.p_IdUsuario);
            jdbc.update(SQL_UPD_CARTERA_ANTICIP, pUpd);
            jdbc.update(SQL_UPD_CARTERA_REPROCESO, pUpd);
            jdbc.update(SQL_UPD_CARTERA_VEN_ENTREGA, pUpd);
            jdbc.update(SQL_UPD_CARTERA_VEN_MON, pUpd);

            plataforma.callAgregarLog(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "INFO",
                    "VencimientoOperacionForward: fecha=" + in.p_Fecha);
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("VencimientoOperacionForward failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo; public String p_Fecha;
    }
    public static class OperacionFwd { public Long IdOperacion; public String IdEstado; public BigDecimal MontoCompra; public BigDecimal MontoVenta; }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<OperacionFwd> P_OUT_OP = new ArrayList<>();
        public List<OperacionFwd> P_OUT_OP_ENTREGA = new ArrayList<>();
        public List<OperacionFwd> P_OUT_OP_ENTREGA_FP = new ArrayList<>();
        public List<OperacionFwd> P_OUT_OP_ENTREGA_SIN = new ArrayList<>();
    }
}
