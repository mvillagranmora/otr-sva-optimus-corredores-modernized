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
public class CrearOperacionSpotService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(CrearOperacionSpotService.class);
    private static final String SQL_SEL_LIMITES = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/CrearOperacionSpot_ConsultarLimitesParidadYTipoCambio.sql");
    private static final String SQL_SEL_PUNTAS = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/CrearOperacionSpot_ConsultarPuntas.sql");
    private static final String SQL_SEL_PUNTAS_MARGEN = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/CrearOperacionSpot_ConsultarPuntasCalcularMargen.sql");
    private static final String SQL_SEL_PORTAFOLIO = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/CrearOperacionSpot_SelectPortafolio.sql");
    private static final String SQL_SEL_FLUJO = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/CrearOperacionSpot_ConsultarPortafoliosFlujoCaja.sql");
    private static final String SQL_SEL_REQUISITOS = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/CrearOperacionSpot_ConsultarRequisitosCrearOperacionSpot.sql");
    private static final String SQL_INS_OP = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/CrearOperacionSpot_InsertOperacionesSpot.sql");
    private static final String SQL_INS_WRAPPER = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/CrearOperacionSpot_InsertOperacionesSpotWrapper.sql");
    private static final String SQL_UPD_ESTADO = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/CrearOperacionSpot_UpdateOperacionesSpotInEstado.sql");
    private static final String SQL_UPD_MARGEN = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/CrearOperacionSpot_UpdateOperacionesSpotInMargen.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public CrearOperacionSpotService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource;
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.SPT.CrearOperacionSpot"; }

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
            p.put("IdMoneda", in.p_IdMoneda);
            p.put("Monto", in.p_Monto);

            Salida out = new Salida();
            out.P_OUT_LIMITES = JavaCallInfra.query(conn, SQL_SEL_LIMITES, p, JavaCallInfra.fieldMapper(Limite.class));
            out.P_OUT_PUNTAS = JavaCallInfra.query(conn, SQL_SEL_PUNTAS, p, JavaCallInfra.fieldMapper(Punta.class));
            out.P_OUT_PUNTAS_MARGEN = JavaCallInfra.query(conn, SQL_SEL_PUNTAS_MARGEN, p, JavaCallInfra.fieldMapper(Punta.class));
            out.P_OUT_PORTAFOLIO = JavaCallInfra.query(conn, SQL_SEL_PORTAFOLIO, p, JavaCallInfra.fieldMapper(Portafolio.class));
            out.P_OUT_FLUJO = JavaCallInfra.query(conn, SQL_SEL_FLUJO, p, JavaCallInfra.fieldMapper(FlujoCaja.class));
            out.P_OUT_REQUISITOS = JavaCallInfra.query(conn, SQL_SEL_REQUISITOS, p, JavaCallInfra.fieldMapper(Requisito.class));

            var jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));
            var pIns = new MapSqlParameterSource(p).addValue("IdUsuario", in.p_IdUsuario).addValue("IdModulo", in.p_IdModulo);
            jdbc.update(SQL_INS_OP, pIns);
            jdbc.update(SQL_INS_WRAPPER, pIns);
            jdbc.update(SQL_UPD_ESTADO, pIns);
            jdbc.update(SQL_UPD_MARGEN, pIns);

            plataforma.callAgregarLog(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "INFO",
                    "CrearOperacionSpot portafolio=" + in.p_IdPortafolio);
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("CrearOperacionSpot failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public Long p_IdPortafolio; public String p_IdMoneda; public BigDecimal p_Monto;
        public String p_TipoOperacion;
    }
    public static class Limite { public String TipoLimite; public BigDecimal Limite; public BigDecimal Utilizado; }
    public static class Punta { public BigDecimal Compra; public BigDecimal Venta; public BigDecimal Margen; }
    public static class Portafolio { public Long IdPortafolio; public BigDecimal Saldo; public String IdMoneda; }
    public static class FlujoCaja { public String IdMoneda; public BigDecimal Entrada; public BigDecimal Salida; }
    public static class Requisito { public String Codigo; public Boolean Cumple; }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Limite> P_OUT_LIMITES = new ArrayList<>();
        public List<Punta> P_OUT_PUNTAS = new ArrayList<>();
        public List<Punta> P_OUT_PUNTAS_MARGEN = new ArrayList<>();
        public List<Portafolio> P_OUT_PORTAFOLIO = new ArrayList<>();
        public List<FlujoCaja> P_OUT_FLUJO = new ArrayList<>();
        public List<Requisito> P_OUT_REQUISITOS = new ArrayList<>();
    }
}
