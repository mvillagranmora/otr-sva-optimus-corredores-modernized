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
public class IngresarForwardCarteraService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(IngresarForwardCarteraService.class);
    private static final String SQL_SEL_INGRESO = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/IngresarForwardCartera_ConsultarOperacionesForwardIngresoCustodia.sql");
    private static final String SQL_SEL_RETIRO = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/IngresarForwardCartera_ConsultarOperacionesForwardRetiroCustodia.sql");
    private static final String SQL_SEL_REQUISITOS = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/IngresarForwardCartera_ConsultarRequisitosOperacionForwardCartera.sql");
    private static final String SQL_INS = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/IngresarForwardCartera_InsertOperacionesForwardWrapper.sql");
    private static final String SQL_UPD = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/IngresarForwardCartera_UpdateOperacionForward.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public IngresarForwardCarteraService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource;
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.FWD.IngresarForwardCartera"; }

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
            p.put("IdPortafolio", in.p_IdPortafolio);

            Salida out = new Salida();
            out.P_OUT_INGRESO = JavaCallInfra.query(conn, SQL_SEL_INGRESO, p, JavaCallInfra.fieldMapper(OperacionFwd.class));
            out.P_OUT_RETIRO = JavaCallInfra.query(conn, SQL_SEL_RETIRO, p, JavaCallInfra.fieldMapper(OperacionFwd.class));
            out.P_OUT_REQUISITOS = JavaCallInfra.query(conn, SQL_SEL_REQUISITOS, p, JavaCallInfra.fieldMapper(Requisito.class));

            var jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));
            var pIns = new MapSqlParameterSource(p).addValue("IdUsuario", in.p_IdUsuario);
            jdbc.update(SQL_INS, pIns);
            jdbc.update(SQL_UPD, pIns);

            plataforma.callAgregarLog(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "INFO",
                    "IngresarForwardCartera portafolio=" + in.p_IdPortafolio);
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("IngresarForwardCartera failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo; public Long p_IdPortafolio;
    }
    public static class OperacionFwd { public Long IdOperacion; public String IdEstado; public BigDecimal MontoCartera; public String TipoCustodia; }
    public static class Requisito { public String Codigo; public Boolean Cumple; }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<OperacionFwd> P_OUT_INGRESO = new ArrayList<>();
        public List<OperacionFwd> P_OUT_RETIRO = new ArrayList<>();
        public List<Requisito> P_OUT_REQUISITOS = new ArrayList<>();
    }
}
