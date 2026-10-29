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
public class CuadraturaCierreDiarioMRFService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(CuadraturaCierreDiarioMRFService.class);
    private static final String SQL_SEL_OPERACIONES = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/CuadraturaCierreDiario_SelectOperaciones.sql");
    private static final String SQL_SEL_CARTERAS_MRF = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/CuadraturaCierreDiario_SelectCarterasMRF.sql");
    private static final String SQL_SEL_PACTOS_VIGENTES = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/CuadraturaCierreDiario_SelectPactosVigentes.sql");
    private static final String SQL_SEL_PACTOS_VENCIMIENTO = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/CuadraturaCierreDiario_SelectPactosVencimiento.sql");
    private static final String SQL_SEL_PACTOS_ANTICIPO = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/CuadraturaCierreDiario_SelectPactosAnticipo.sql");
    private static final String SQL_SEL_MENOR_MAYOR = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/CuadraturaCierreDiario_SelectMenorMayorValorRF.sql");
    private static final String SQL_SEL_MOV_COM_TESORERIA = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/CuadraturaCierreDiario_SelectMovComTesoreria.sql");
    private static final String SQL_SEL_MOV_CTA_INV = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/CuadraturaCierreDiario_SelectMovCtaInversion.sql");
    private static final String SQL_SEL_INFORMES = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/CuadraturaCierreDiario_SelectInformes.sql");
    private static final String SQL_DEL_CUADRATURA = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/CuadraturaCierreDiario_DeleteCuadraturaDiariaOperaciones.sql");
    private static final String SQL_INS_CUADRATURA = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/CuadraturaCierreDiario_InsertCuadraturaDiariaOperaciones.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public CuadraturaCierreDiarioMRFService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource;
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.MRF.CuadraturaCierreDiario"; }

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
            out.P_OUT_OPERACIONES = JavaCallInfra.query(conn, SQL_SEL_OPERACIONES, p, JavaCallInfra.fieldMapper(Operacion.class));
            out.P_OUT_CARTERAS = JavaCallInfra.query(conn, SQL_SEL_CARTERAS_MRF, p, JavaCallInfra.fieldMapper(Cartera.class));
            out.P_OUT_PACTOS_VIGENTES = JavaCallInfra.query(conn, SQL_SEL_PACTOS_VIGENTES, p, JavaCallInfra.fieldMapper(Pacto.class));
            out.P_OUT_PACTOS_VENCIMIENTO = JavaCallInfra.query(conn, SQL_SEL_PACTOS_VENCIMIENTO, p, JavaCallInfra.fieldMapper(Pacto.class));
            out.P_OUT_PACTOS_ANTICIPO = JavaCallInfra.query(conn, SQL_SEL_PACTOS_ANTICIPO, p, JavaCallInfra.fieldMapper(Pacto.class));
            out.P_OUT_MENOR_MAYOR = JavaCallInfra.query(conn, SQL_SEL_MENOR_MAYOR, p, JavaCallInfra.fieldMapper(MenorMayor.class));
            out.P_OUT_MOV_COM_TES = JavaCallInfra.query(conn, SQL_SEL_MOV_COM_TESORERIA, p, JavaCallInfra.fieldMapper(MovComTesoreria.class));
            out.P_OUT_MOV_CTA_INV = JavaCallInfra.query(conn, SQL_SEL_MOV_CTA_INV, p, JavaCallInfra.fieldMapper(MovCtaInversion.class));
            out.P_OUT_INFORMES = JavaCallInfra.query(conn, SQL_SEL_INFORMES, p, JavaCallInfra.fieldMapper(Informe.class));

            // Refresh cuadratura
            var jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));
            var pIns = new MapSqlParameterSource()
                    .addValue("IdEmpresa", in.p_IdEmpresa)
                    .addValue("Fecha", in.p_Fecha)
                    .addValue("IdUsuario", in.p_IdUsuario);
            jdbc.update(SQL_DEL_CUADRATURA, pIns);
            jdbc.update(SQL_INS_CUADRATURA, pIns);

            plataforma.callAgregarLog(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "INFO",
                    "CuadraturaCierreDiario MRF: fecha=" + in.p_Fecha);
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("CuadraturaCierreDiario MRF failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo; public String p_Fecha;
    }
    public static class Operacion { public Long IdOperacion; public String IdEstado; public String IdTipoProducto; public BigDecimal Monto; }
    public static class Cartera { public Long IdCartera; public Long IdPortafolio; public String IdInstrumento; public BigDecimal Saldo; }
    public static class Pacto { public Long IdOperacion; public String IdInstrumento; public String FechaInicio; public String FechaTermino; public BigDecimal TasaPacto; }
    public static class MenorMayor { public String IdInstrumento; public BigDecimal MenorValor; public BigDecimal MayorValor; }
    public static class MovComTesoreria { public Long IdMovimiento; public String Tipo; public BigDecimal Monto; }
    public static class MovCtaInversion { public Long IdMovimiento; public Long IdCartera; public BigDecimal Monto; }
    public static class Informe { public Long IdInforme; public String Tipo; public String Fecha; }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Operacion> P_OUT_OPERACIONES = new ArrayList<>();
        public List<Cartera> P_OUT_CARTERAS = new ArrayList<>();
        public List<Pacto> P_OUT_PACTOS_VIGENTES = new ArrayList<>();
        public List<Pacto> P_OUT_PACTOS_VENCIMIENTO = new ArrayList<>();
        public List<Pacto> P_OUT_PACTOS_ANTICIPO = new ArrayList<>();
        public List<MenorMayor> P_OUT_MENOR_MAYOR = new ArrayList<>();
        public List<MovComTesoreria> P_OUT_MOV_COM_TES = new ArrayList<>();
        public List<MovCtaInversion> P_OUT_MOV_CTA_INV = new ArrayList<>();
        public List<Informe> P_OUT_INFORMES = new ArrayList<>();
    }
}
