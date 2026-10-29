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
public class ReversaAnticipoForwardService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ReversaAnticipoForwardService.class);
    private static final String SQL_SEL_OP = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/ReversaAnticipoForward_ConsultarOperacionesForward.sql");
    private static final String SQL_SEL_FP = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/ReversaAnticipoForward_ConsultarFormaDePagoOperacionReversada.sql");
    private static final String SQL_SEL_OP_SPOT = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/ReversaAnticipoForward_ConsultarOperacionSpot.sql");
    private static final String SQL_SEL_VALIDACION = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/ReversaAnticipoForward_ConsultarValidacionesReversaAnticipoForward.sql");
    private static final String SQL_SEL_MOV_FWD = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/ReversaAnticipoForward_ConsultarMovimientosFWDVencimiento.sql");
    private static final String SQL_SEL_MODAL = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/ReversaAnticipoForward_ConsultarModalidadLiquidacion.sql");
    private static final String SQL_SEL_ESTADO_COMP = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/ReversaAnticipoForward_SelectEstadoComprobante.sql");
    private static final String SQL_SEL_ESTADO_IE = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/ReversaAnticipoForward_SelectEstadoIngresoEgreso.sql");
    private static final String SQL_UPD_OP = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/ReversaAnticipoForward_UpdateOperacionesMesa.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public ReversaAnticipoForwardService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource;
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.FWD.ReversaAnticipoForward"; }

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
            out.P_OUT_OP = JavaCallInfra.query(conn, SQL_SEL_OP, p, JavaCallInfra.fieldMapper(OperacionFwd.class));
            out.P_OUT_FP = JavaCallInfra.query(conn, SQL_SEL_FP, p, JavaCallInfra.fieldMapper(FormaPago.class));
            out.P_OUT_OP_SPOT = JavaCallInfra.query(conn, SQL_SEL_OP_SPOT, p, JavaCallInfra.fieldMapper(OperacionSpot.class));
            out.P_OUT_VALIDACION = JavaCallInfra.query(conn, SQL_SEL_VALIDACION, p, JavaCallInfra.fieldMapper(Validacion.class));
            out.P_OUT_MOV = JavaCallInfra.query(conn, SQL_SEL_MOV_FWD, p, JavaCallInfra.fieldMapper(Movimiento.class));
            out.P_OUT_MODAL = JavaCallInfra.query(conn, SQL_SEL_MODAL, p, JavaCallInfra.fieldMapper(Modalidad.class));
            out.P_OUT_ESTADO_COMP = JavaCallInfra.query(conn, SQL_SEL_ESTADO_COMP, p, JavaCallInfra.fieldMapper(Estado.class));
            out.P_OUT_ESTADO_IE = JavaCallInfra.query(conn, SQL_SEL_ESTADO_IE, p, JavaCallInfra.fieldMapper(Estado.class));

            var jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));
            var pUpd = new MapSqlParameterSource(p).addValue("IdUsuario", in.p_IdUsuario);
            jdbc.update(SQL_UPD_OP, pUpd);

            plataforma.callAgregarLog(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "INFO",
                    "ReversaAnticipoForward op=" + in.p_IdOperacion);
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("ReversaAnticipoForward failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo; public Long p_IdOperacion;
    }
    public static class OperacionFwd { public Long IdOperacion; public String IdEstado; public BigDecimal MontoCompra; }
    public static class FormaPago { public Long IdFormaPago; public String Descripcion; public String IdMoneda; }
    public static class OperacionSpot { public Long IdOperacion; public String IdEstado; public BigDecimal MontoSpot; }
    public static class Validacion { public String Codigo; public Boolean Valida; public String Descripcion; }
    public static class Movimiento { public Long IdMovimiento; public BigDecimal Monto; public String Tipo; }
    public static class Modalidad { public String IdModalidad; public String Descripcion; }
    public static class Estado { public String IdEstado; public String Descripcion; }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<OperacionFwd> P_OUT_OP = new ArrayList<>();
        public List<FormaPago> P_OUT_FP = new ArrayList<>();
        public List<OperacionSpot> P_OUT_OP_SPOT = new ArrayList<>();
        public List<Validacion> P_OUT_VALIDACION = new ArrayList<>();
        public List<Movimiento> P_OUT_MOV = new ArrayList<>();
        public List<Modalidad> P_OUT_MODAL = new ArrayList<>();
        public List<Estado> P_OUT_ESTADO_COMP = new ArrayList<>();
        public List<Estado> P_OUT_ESTADO_IE = new ArrayList<>();
    }
}
