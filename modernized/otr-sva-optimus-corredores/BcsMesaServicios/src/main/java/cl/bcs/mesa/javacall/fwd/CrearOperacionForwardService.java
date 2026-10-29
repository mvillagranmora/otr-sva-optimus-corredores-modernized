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
public class CrearOperacionForwardService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(CrearOperacionForwardService.class);
    private static final String SQL_SEL_OP = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/CrearOperacionForward_ConsultarOperacionesForwardCalce.sql");
    private static final String SQL_SEL_PARAM = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/CrearOperacionForward_ConsultarParametroMesa.sql");
    private static final String SQL_SEL_PARAM_FWD = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/CrearOperacionForward_ConsultarParametrosMesaForward.sql");
    private static final String SQL_SEL_REQUISITOS = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/CrearOperacionForward_ConsultarRequisitosCrearOperacionForward.sql");
    private static final String SQL_INS_CALCE = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/CrearOperacionForward_InsertOperacionesForwardCalceWrapper.sql");
    private static final String SQL_INS_OP = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/CrearOperacionForward_InsertOperacionesForwardWrapper.sql");
    private static final String SQL_UPD_FP = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/CrearOperacionForward_UpdateFormaDePagoIdOperacion.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public CrearOperacionForwardService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource;
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.FWD.CrearOperacionForward"; }

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
            p.put("IdUsuario", in.p_IdUsuario);
            p.put("IdModulo", in.p_IdModulo);
            p.put("IdPortafolio", in.p_IdPortafolio);

            Salida out = new Salida();
            out.P_OUT_OP_CALCE = JavaCallInfra.query(conn, SQL_SEL_OP, p, JavaCallInfra.fieldMapper(OperacionCalce.class));
            out.P_OUT_PARAM = JavaCallInfra.query(conn, SQL_SEL_PARAM, p, JavaCallInfra.fieldMapper(Parametro.class));
            out.P_OUT_PARAM_FWD = JavaCallInfra.query(conn, SQL_SEL_PARAM_FWD, p, JavaCallInfra.fieldMapper(Parametro.class));
            out.P_OUT_REQUISITOS = JavaCallInfra.query(conn, SQL_SEL_REQUISITOS, p, JavaCallInfra.fieldMapper(Requisito.class));

            var jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));
            var pIns = new MapSqlParameterSource(p);
            jdbc.update(SQL_INS_OP, pIns);
            jdbc.update(SQL_INS_CALCE, pIns);
            jdbc.update(SQL_UPD_FP, pIns);

            plataforma.callAgregarLog(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "INFO",
                    "CrearOperacionForward portafolio=" + in.p_IdPortafolio);
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("CrearOperacionForward failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public Long p_IdPortafolio; public String p_IdInstrumento; public BigDecimal p_MontoCompra;
        public BigDecimal p_MontoVenta; public String p_FechaEmision; public String p_FechaTermino;
    }
    public static class OperacionCalce { public Long IdOperacion; public Long IdOperacionCalce; public BigDecimal MontoCalce; }
    public static class Parametro { public String IdParametro; public String Valor; }
    public static class Requisito { public String Codigo; public Boolean Cumple; public String Descripcion; }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<OperacionCalce> P_OUT_OP_CALCE = new ArrayList<>();
        public List<Parametro> P_OUT_PARAM = new ArrayList<>();
        public List<Parametro> P_OUT_PARAM_FWD = new ArrayList<>();
        public List<Requisito> P_OUT_REQUISITOS = new ArrayList<>();
    }
}
