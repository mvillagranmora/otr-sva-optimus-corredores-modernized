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
public class CuadraturaCierreDiarioSPTService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(CuadraturaCierreDiarioSPTService.class);
    private static final String SQL_SEL_PENDIENTES = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/CuadraturaCierreDiario_SelectOperacionesPendientes.sql");
    private static final String SQL_SEL_PEND_CONF = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/CuadraturaCierreDiario_SelectOperacionesPendientesConfirmar.sql");
    private static final String SQL_SEL_PEND_FAC = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/CuadraturaCierreDiario_SelectOperacionesPendientesFacturacion.sql");
    private static final String SQL_SEL_PEND_LIQ = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/CuadraturaCierreDiario_SelectOperacionesPendientesLiquidacion.sql");
    private static final String SQL_SEL_OMA = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/CuadraturaCierreDiario_SelectInterfazOMA.sql");
    private static final String SQL_SEL_CONT = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/CuadraturaCierreDiario_SelectOperacionesDisponiblesContabilidad.sql");
    private static final String SQL_DEL = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/CuadraturaCierreDiario_DeleteCuadraturaDiariaOperaciones.sql");
    private static final String SQL_INS = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/CuadraturaCierreDiario_InsertCuadraturaDiariaOperaciones.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public CuadraturaCierreDiarioSPTService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource;
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.SPT.CuadraturaCierreDiario"; }

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
            out.P_OUT_PENDIENTES = JavaCallInfra.query(conn, SQL_SEL_PENDIENTES, p, JavaCallInfra.fieldMapper(Operacion.class));
            out.P_OUT_PEND_CONF = JavaCallInfra.query(conn, SQL_SEL_PEND_CONF, p, JavaCallInfra.fieldMapper(Operacion.class));
            out.P_OUT_PEND_FAC = JavaCallInfra.query(conn, SQL_SEL_PEND_FAC, p, JavaCallInfra.fieldMapper(Operacion.class));
            out.P_OUT_PEND_LIQ = JavaCallInfra.query(conn, SQL_SEL_PEND_LIQ, p, JavaCallInfra.fieldMapper(Operacion.class));
            out.P_OUT_OMA = JavaCallInfra.query(conn, SQL_SEL_OMA, p, JavaCallInfra.fieldMapper(InterfazOMA.class));
            out.P_OUT_CONTABILIDAD = JavaCallInfra.query(conn, SQL_SEL_CONT, p, JavaCallInfra.fieldMapper(Operacion.class));

            var jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));
            var pUpd = new MapSqlParameterSource(p).addValue("IdUsuario", in.p_IdUsuario);
            jdbc.update(SQL_DEL, pUpd);
            jdbc.update(SQL_INS, pUpd);

            plataforma.callAgregarLog(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "INFO",
                    "CuadraturaCierreDiario SPT: fecha=" + in.p_Fecha);
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("CuadraturaCierreDiario SPT failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo; public String p_Fecha;
    }
    public static class Operacion { public Long IdOperacion; public String IdEstado; public BigDecimal Monto; public String IdMoneda; }
    public static class InterfazOMA { public String TipoRegistro; public BigDecimal Monto; public String FechaValor; }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Operacion> P_OUT_PENDIENTES = new ArrayList<>();
        public List<Operacion> P_OUT_PEND_CONF = new ArrayList<>();
        public List<Operacion> P_OUT_PEND_FAC = new ArrayList<>();
        public List<Operacion> P_OUT_PEND_LIQ = new ArrayList<>();
        public List<InterfazOMA> P_OUT_OMA = new ArrayList<>();
        public List<Operacion> P_OUT_CONTABILIDAD = new ArrayList<>();
    }
}
