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
public class CrearOperacionSpotOrigenForwardService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(CrearOperacionSpotOrigenForwardService.class);
    private static final String SQL_FP_CLI_CI_PAGA = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/CrearOperacionSpotOrigenForward_ConsultarFormaDePagoClienteCuentaInversionPaga.sql");
    private static final String SQL_FP_CLI_CI_RECIBE = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/CrearOperacionSpotOrigenForward_ConsultarFormaDePagoClienteCuentaInversionRecibe.sql");
    private static final String SQL_FP_CLI_ND_PAGA = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/CrearOperacionSpotOrigenForward_ConsultarFormaDePagoClienteNoPorDefectoPaga.sql");
    private static final String SQL_FP_CLI_ND_RECIBE = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/CrearOperacionSpotOrigenForward_ConsultarFormaDePagoClienteNoPorDefectoRecibe.sql");
    private static final String SQL_FP_CLI_PAGA = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/CrearOperacionSpotOrigenForward_ConsultarFormaDePagoClientePaga.sql");
    private static final String SQL_FP_CLI_RECIBE = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/CrearOperacionSpotOrigenForward_ConsultarFormaDePagoClienteRecibe.sql");
    private static final String SQL_FP_MESA_PAGA = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/CrearOperacionSpotOrigenForward_ConsultarFormaDePagoMesaPaga.sql");
    private static final String SQL_FP_MESA_ND_PAGA = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/CrearOperacionSpotOrigenForward_ConsultarFormaDePagoMesaPorDefectoPaga.sql");
    private static final String SQL_FP_MESA_ND_RECIBE = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/CrearOperacionSpotOrigenForward_ConsultarFormaDePagoMesaPorDefectoRecibe.sql");
    private static final String SQL_FP_MESA_RECIBE = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/CrearOperacionSpotOrigenForward_ConsultarFormaDePagoMesaRecibe.sql");
    private static final String SQL_FP_FWD = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/CrearOperacionSpotOrigenForward_ConsultarFormaDePagoOperacionForward.sql");
    private static final String SQL_INS_SPOT = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/CrearOperacionSpotOrigenForward_InsertOperacionesSpotOrigenForwardWrapper.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public CrearOperacionSpotOrigenForwardService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource;
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.FWD.CrearOperacionSpotOrigenForward"; }

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
            p.put("IdOperacion", in.p_IdOperacionFwd);
            p.put("IdPortafolio", in.p_IdPortafolio);

            Salida out = new Salida();
            out.P_OUT_FP_FWD = JavaCallInfra.query(conn, SQL_FP_FWD, p, JavaCallInfra.fieldMapper(FormaPago.class));
            out.P_OUT_FP_CLI_PAGA = JavaCallInfra.query(conn, SQL_FP_CLI_PAGA, p, JavaCallInfra.fieldMapper(FormaPago.class));
            out.P_OUT_FP_CLI_RECIBE = JavaCallInfra.query(conn, SQL_FP_CLI_RECIBE, p, JavaCallInfra.fieldMapper(FormaPago.class));
            out.P_OUT_FP_MESA_PAGA = JavaCallInfra.query(conn, SQL_FP_MESA_PAGA, p, JavaCallInfra.fieldMapper(FormaPago.class));
            out.P_OUT_FP_MESA_RECIBE = JavaCallInfra.query(conn, SQL_FP_MESA_RECIBE, p, JavaCallInfra.fieldMapper(FormaPago.class));

            var jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));
            jdbc.update(SQL_INS_SPOT, new MapSqlParameterSource(p).addValue("IdUsuario", in.p_IdUsuario));

            plataforma.callAgregarLog(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "INFO",
                    "CrearOperacionSpotOrigenForward fwd=" + in.p_IdOperacionFwd);
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("CrearOperacionSpotOrigenForward failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public Long p_IdOperacionFwd; public Long p_IdPortafolio;
    }
    public static class FormaPago { public Long IdFormaPago; public String Descripcion; public String IdMoneda; }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<FormaPago> P_OUT_FP_FWD = new ArrayList<>();
        public List<FormaPago> P_OUT_FP_CLI_PAGA = new ArrayList<>();
        public List<FormaPago> P_OUT_FP_CLI_RECIBE = new ArrayList<>();
        public List<FormaPago> P_OUT_FP_MESA_PAGA = new ArrayList<>();
        public List<FormaPago> P_OUT_FP_MESA_RECIBE = new ArrayList<>();
    }
}
