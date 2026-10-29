package cl.bcs.mesa.javacall.mrf;

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
public class ReasignarOperacionService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ReasignarOperacionService.class);
    private static final String SQL_SEL_OP_ORIG = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/ReasignarOperacion_SelectOperacionOriginal.sql");
    private static final String SQL_SEL_CARTERA = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/ReasignarOperacion_SelectCarteraOperacionReasignada.sql");
    private static final String SQL_INS_OP = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/ReasignarOperacion_InsertOperacionReasignada.sql");
    private static final String SQL_INS_DETALLE = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/ReasignarOperacion_InsertDetalleOperacionReasignada.sql");
    private static final String SQL_INS_COMPROBANTE = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/ReasignarOperacion_InsertComprobantePagoOperacionReasignada.sql");
    private static final String SQL_UPD_REASIGNACION = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/ReasignarOperacion_UpdateIdReasignacion.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public ReasignarOperacionService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource;
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.MRF.ReasignarOperacion"; }

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
            p.put("IdPortafolioDestino", in.p_IdPortafolioDestino);

            // Load original operation
            List<OperacionOrig> origOps = JavaCallInfra.query(conn, SQL_SEL_OP_ORIG, p, JavaCallInfra.fieldMapper(OperacionOrig.class));
            if (origOps.isEmpty()) {
                JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L);
                err.addProperty("error", "Operacion original no encontrada: " + in.p_IdOperacion); return err;
            }

            List<Cartera> carteras = JavaCallInfra.query(conn, SQL_SEL_CARTERA, p, JavaCallInfra.fieldMapper(Cartera.class));

            var jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));
            var pIns = new MapSqlParameterSource(p)
                    .addValue("IdUsuario", in.p_IdUsuario)
                    .addValue("IdModulo", in.p_IdModulo);

            // Insert reasigned operation and its details/comprobante
            jdbc.update(SQL_INS_OP, pIns);
            jdbc.update(SQL_INS_DETALLE, pIns);
            jdbc.update(SQL_INS_COMPROBANTE, pIns);
            jdbc.update(SQL_UPD_REASIGNACION, pIns);

            plataforma.callAgregarLog(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "INFO",
                    "ReasignarOperacion: op=" + in.p_IdOperacion + " dest=" + in.p_IdPortafolioDestino);

            Salida out = new Salida();
            out.P_OUT_CARTERAS = carteras;
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("ReasignarOperacion failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public Long p_IdOperacion; public Long p_IdPortafolioDestino;
    }
    public static class OperacionOrig { public Long IdOperacion; public Long IdPortafolio; public BigDecimal Monto; public String IdTipoProducto; }
    public static class Cartera { public Long IdCartera; public Long IdPortafolio; public BigDecimal Disponible; }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Cartera> P_OUT_CARTERAS = new ArrayList<>();
    }
}
