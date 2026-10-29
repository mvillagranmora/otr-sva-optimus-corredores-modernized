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
public class CuadraturaCierreDiarioFWDService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(CuadraturaCierreDiarioFWDService.class);
    private static final String SQL_SEL_OP_PENDIENTES = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/CuadraturaCierreDiario_SelectOperacionesPendientes.sql");
    private static final String SQL_SEL_OP_TESORERIA = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/CuadraturaCierreDiario_SelectOperacionesTesoreria.sql");
    private static final String SQL_SEL_OP_CTA_INV = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/CuadraturaCierreDiario_SelectOperacionesCuentaInversion.sql");
    private static final String SQL_SEL_OP_COMP = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/CuadraturaCierreDiario_SelectOperacionesModCompensado.sql");
    private static final String SQL_SEL_OP_ENTREGA = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/CuadraturaCierreDiario_SelectOperacionesModEntregaFisica.sql");
    private static final String SQL_SEL_OP_ENTREGA_SIN_SPOT = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/CuadraturaCierreDiario_SelectOperacionesModEntregaFisicaLiqSinSpot.sql");
    private static final String SQL_SEL_DATOS_CENTRAL = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/CuadraturaCierreDiario_SelectDatosCentralizacion.sql");
    private static final String SQL_SEL_INFORME_IX = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/CuadraturaCierreDiario_SelectInformeIX.sql");
    private static final String SQL_SEL_VERIF = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/CuadraturaCierreDiario_VerificarValorizacionRazonable.sql");
    private static final String SQL_DEL_CUADRATURA = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/CuadraturaCierreDiario_DeleteCuadraturaDiariaOperaciones.sql");
    private static final String SQL_INS_CUADRATURA = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/CuadraturaCierreDiario_InsertCuadraturaDiariaOperaciones.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public CuadraturaCierreDiarioFWDService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource;
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.FWD.CuadraturaCierreDiario"; }

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
            out.P_OUT_OP_PENDIENTES = JavaCallInfra.query(conn, SQL_SEL_OP_PENDIENTES, p, JavaCallInfra.fieldMapper(Operacion.class));
            out.P_OUT_OP_TESORERIA = JavaCallInfra.query(conn, SQL_SEL_OP_TESORERIA, p, JavaCallInfra.fieldMapper(Operacion.class));
            out.P_OUT_OP_CTA_INV = JavaCallInfra.query(conn, SQL_SEL_OP_CTA_INV, p, JavaCallInfra.fieldMapper(Operacion.class));
            out.P_OUT_OP_COMP = JavaCallInfra.query(conn, SQL_SEL_OP_COMP, p, JavaCallInfra.fieldMapper(Operacion.class));
            out.P_OUT_OP_ENTREGA = JavaCallInfra.query(conn, SQL_SEL_OP_ENTREGA, p, JavaCallInfra.fieldMapper(Operacion.class));
            out.P_OUT_OP_ENTREGA_SIN_SPOT = JavaCallInfra.query(conn, SQL_SEL_OP_ENTREGA_SIN_SPOT, p, JavaCallInfra.fieldMapper(Operacion.class));
            out.P_OUT_DATOS_CENTRAL = JavaCallInfra.query(conn, SQL_SEL_DATOS_CENTRAL, p, JavaCallInfra.fieldMapper(DatosC.class));
            out.P_OUT_INFORME_IX = JavaCallInfra.query(conn, SQL_SEL_INFORME_IX, p, JavaCallInfra.fieldMapper(InformeIX.class));

            var jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));
            var pUpd = new MapSqlParameterSource(p).addValue("IdUsuario", in.p_IdUsuario);
            jdbc.update(SQL_DEL_CUADRATURA, pUpd);
            jdbc.update(SQL_INS_CUADRATURA, pUpd);

            plataforma.callAgregarLog(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "INFO",
                    "CuadraturaCierreDiario FWD: fecha=" + in.p_Fecha);
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("CuadraturaCierreDiario FWD failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo; public String p_Fecha;
    }
    public static class Operacion { public Long IdOperacion; public String IdEstado; public BigDecimal MontoCompra; public BigDecimal MontoVenta; }
    public static class DatosC { public String TipoCentralizacion; public BigDecimal Monto; }
    public static class InformeIX { public String Codigo; public BigDecimal Valor; }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Operacion> P_OUT_OP_PENDIENTES = new ArrayList<>();
        public List<Operacion> P_OUT_OP_TESORERIA = new ArrayList<>();
        public List<Operacion> P_OUT_OP_CTA_INV = new ArrayList<>();
        public List<Operacion> P_OUT_OP_COMP = new ArrayList<>();
        public List<Operacion> P_OUT_OP_ENTREGA = new ArrayList<>();
        public List<Operacion> P_OUT_OP_ENTREGA_SIN_SPOT = new ArrayList<>();
        public List<DatosC> P_OUT_DATOS_CENTRAL = new ArrayList<>();
        public List<InformeIX> P_OUT_INFORME_IX = new ArrayList<>();
    }
}
