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
public class AnularOperacionSpotService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(AnularOperacionSpotService.class);
    private static final String SQL_SEL_OP = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/AnularOperacionSpot_ConsultarOperacionesSpot.sql");
    private static final String SQL_SEL_REQUISITOS = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/AnularOperacionSpot_ConsultarRequisitosAnularOperacionSpot.sql");
    private static final String SQL_SEL_MOV_CTA = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/AnularOperacionSpot_ConsultarOperacionesSpotAnularMovimientosCtaInversion.sql");
    private static final String SQL_SEL_MOV_FAC = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/AnularOperacionSpot_ConsultarOperacionesSpotAnularMovimientosFacturacion.sql");
    private static final String SQL_SEL_MOV_TES = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/AnularOperacionSpot_ConsultarOperacionesSpotAnularMovimientosTesoreria.sql");
    private static final String SQL_UPD_ESTADO = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/AnularOperacionSpot_UpdateOperacionesSpotEstado.sql");
    private static final String SQL_UPD_ORDEN = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/AnularOperacionSpot_UpdateOrdenGeneraSpot.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public AnularOperacionSpotService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource;
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.SPT.AnularOperacionSpot"; }

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
            out.P_OUT_OP = JavaCallInfra.query(conn, SQL_SEL_OP, p, JavaCallInfra.fieldMapper(OperacionSpot.class));
            out.P_OUT_REQUISITOS = JavaCallInfra.query(conn, SQL_SEL_REQUISITOS, p, JavaCallInfra.fieldMapper(Requisito.class));
            out.P_OUT_MOV_CTA = JavaCallInfra.query(conn, SQL_SEL_MOV_CTA, p, JavaCallInfra.fieldMapper(Movimiento.class));
            out.P_OUT_MOV_FAC = JavaCallInfra.query(conn, SQL_SEL_MOV_FAC, p, JavaCallInfra.fieldMapper(Movimiento.class));
            out.P_OUT_MOV_TES = JavaCallInfra.query(conn, SQL_SEL_MOV_TES, p, JavaCallInfra.fieldMapper(Movimiento.class));

            var jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));
            var pUpd = new MapSqlParameterSource(p).addValue("IdUsuario", in.p_IdUsuario);
            jdbc.update(SQL_UPD_ESTADO, pUpd);
            jdbc.update(SQL_UPD_ORDEN, pUpd);

            plataforma.callAgregarLog(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "INFO",
                    "AnularOperacionSpot op=" + in.p_IdOperacion);
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("AnularOperacionSpot failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public Long p_IdOperacion; public String p_MotivoAnulacion;
    }
    public static class OperacionSpot { public Long IdOperacion; public String IdEstado; public BigDecimal Monto; public String IdMoneda; }
    public static class Requisito { public String Codigo; public Boolean Cumple; }
    public static class Movimiento { public Long IdMovimiento; public String Tipo; public BigDecimal Monto; }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<OperacionSpot> P_OUT_OP = new ArrayList<>();
        public List<Requisito> P_OUT_REQUISITOS = new ArrayList<>();
        public List<Movimiento> P_OUT_MOV_CTA = new ArrayList<>();
        public List<Movimiento> P_OUT_MOV_FAC = new ArrayList<>();
        public List<Movimiento> P_OUT_MOV_TES = new ArrayList<>();
    }
}
