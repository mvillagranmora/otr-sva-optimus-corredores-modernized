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
public class ValorizacionForwardService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ValorizacionForwardService.class);
    private static final String SQL_SEL_CRITERIOS = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/ValorizacionForward_ConsultarCriteriosForward.sql");
    private static final String SQL_SEL_OPERACIONES = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/ValorizacionForward_ConsultarOperacionesProcesoValorizacionForward.sql");
    private static final String SQL_SEL_REQUISITOS = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/ValorizacionForward_ConsultarRequisitosValorizacionForward.sql");
    private static final String SQL_DEL_CONTROL = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/ValorizacionForward_DeleteControlProceso.sql");
    private static final String SQL_INS_CONTROL = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/ValorizacionForward_InsertControlProceso.sql");
    private static final String SQL_UPD_CARTERA = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/ValorizacionForward_UpdateCarteraOperacionesForward.sql");
    private static final String SQL_UPD_RESULTADO = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/ValorizacionForward_UpdateCarteraResultadoValorizacion.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public ValorizacionForwardService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource;
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.FWD.ValorizacionForward"; }

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
            out.P_OUT_CRITERIOS = JavaCallInfra.query(conn, SQL_SEL_CRITERIOS, p, JavaCallInfra.fieldMapper(Criterio.class));
            out.P_OUT_OPERACIONES = JavaCallInfra.query(conn, SQL_SEL_OPERACIONES, p, JavaCallInfra.fieldMapper(Operacion.class));
            out.P_OUT_REQUISITOS = JavaCallInfra.query(conn, SQL_SEL_REQUISITOS, p, JavaCallInfra.fieldMapper(Requisito.class));

            var jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));
            var pUpd = new MapSqlParameterSource(p).addValue("IdUsuario", in.p_IdUsuario);
            jdbc.update(SQL_DEL_CONTROL, pUpd);
            jdbc.update(SQL_INS_CONTROL, pUpd);
            jdbc.update(SQL_UPD_CARTERA, pUpd);
            jdbc.update(SQL_UPD_RESULTADO, pUpd);

            plataforma.callAgregarLog(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "INFO",
                    "ValorizacionForward: fecha=" + in.p_Fecha);
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("ValorizacionForward failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo; public String p_Fecha;
    }
    public static class Criterio { public String IdCriterio; public String Descripcion; public String Valor; }
    public static class Operacion { public Long IdOperacion; public BigDecimal ValorizacionActual; public BigDecimal ValorizacionAnterior; }
    public static class Requisito { public String Codigo; public Boolean Cumple; }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Criterio> P_OUT_CRITERIOS = new ArrayList<>();
        public List<Operacion> P_OUT_OPERACIONES = new ArrayList<>();
        public List<Requisito> P_OUT_REQUISITOS = new ArrayList<>();
    }
}
