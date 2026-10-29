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
public class CrearMovimientoForwardTesoreriaService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(CrearMovimientoForwardTesoreriaService.class);
    private static final String SQL_SEL_FP = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/CrearMovimientoForwardTesoreria_ConsultarFormaDePagoCompensada.sql");
    private static final String SQL_SEL_MOV = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/CrearMovimientoForwardTesoreria_SelectMovimientoForward.sql");
    private static final String SQL_SEL_MOV_TES = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/CrearMovimientoForwardTesoreria_SelectMovimientoForwardTesoreria.sql");
    private static final String SQL_SEL_PREFERENCIA = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/CrearMovimientoForwardTesoreria_SelectPreferenciaPagoTesoreria.sql");
    private static final String SQL_INS_TABLA_PASO = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/CrearMovimientoForwardTesoreria_InsertTablaPaso.sql");
    private static final String SQL_DEL_TABLA_PASO = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/CrearMovimientoForwardTesoreria_DeleteTablaPaso.sql");
    private static final String SQL_UPD_CARTERA = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/CrearMovimientoForwardTesoreria_UpdateCartera.sql");
    private static final String SQL_UPD_COMPROBANTE = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/CrearMovimientoForwardTesoreria_UpdateComprobantePagoOperacion.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public CrearMovimientoForwardTesoreriaService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource;
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.FWD.CrearMovimientoForwardTesoreria"; }

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
            out.P_OUT_MOV = JavaCallInfra.query(conn, SQL_SEL_MOV, p, JavaCallInfra.fieldMapper(Movimiento.class));
            out.P_OUT_MOV_TES = JavaCallInfra.query(conn, SQL_SEL_MOV_TES, p, JavaCallInfra.fieldMapper(Movimiento.class));
            out.P_OUT_PREFERENCIA = JavaCallInfra.query(conn, SQL_SEL_PREFERENCIA, p, JavaCallInfra.fieldMapper(Preferencia.class));

            var jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));
            var pUpd = new MapSqlParameterSource(p).addValue("IdUsuario", in.p_IdUsuario);
            jdbc.update(SQL_DEL_TABLA_PASO, pUpd);
            jdbc.update(SQL_INS_TABLA_PASO, pUpd);
            jdbc.update(SQL_UPD_CARTERA, pUpd);
            jdbc.update(SQL_UPD_COMPROBANTE, pUpd);

            plataforma.callAgregarLog(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "INFO",
                    "CrearMovimientoForwardTesoreria op=" + in.p_IdOperacion);
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("CrearMovimientoForwardTesoreria failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo; public Long p_IdOperacion;
    }
    public static class FormaPago { public Long IdFormaPago; public String Descripcion; public String IdMoneda; }
    public static class Movimiento { public Long IdMovimiento; public String Tipo; public BigDecimal Monto; public String FechaMovimiento; }
    public static class Preferencia { public Long IdFormaPago; public String IdMoneda; }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<FormaPago> P_OUT_FP = new ArrayList<>();
        public List<Movimiento> P_OUT_MOV = new ArrayList<>();
        public List<Movimiento> P_OUT_MOV_TES = new ArrayList<>();
        public List<Preferencia> P_OUT_PREFERENCIA = new ArrayList<>();
    }
}
