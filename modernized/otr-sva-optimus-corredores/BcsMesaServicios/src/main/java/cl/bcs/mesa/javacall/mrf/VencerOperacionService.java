package cl.bcs.mesa.javacall.mrf;

import cl.bcs.mesa.javacall.JavaCallInfra;
import cl.bcs.mesa.javacall.JavaCallService;
import cl.bcs.mesa.javacall.PlataformaBridge;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class VencerOperacionService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(VencerOperacionService.class);
    private static final String SQL_SEL_CUADRATURA = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/VencerOperacion_SelectCuadraturaCierre.sql");
    private static final String SQL_SEL_MODALIDAD = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/VencerOperacion_SelectModalidadDePago.sql");
    private static final String SQL_SEL_PREFERENCIA = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/VencerOperacion_SelectPreferenciaPagoPortafolio.sql");
    private static final String SQL_SEL_SUCURSAL = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/VencerOperacion_SelectSucursalEmpleado.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public VencerOperacionService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource;
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.MRF.VencerOperacion"; }

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
            p.put("IdPortafolio", in.p_IdPortafolio);

            Salida out = new Salida();
            out.P_OUT_CUADRATURA = JavaCallInfra.query(conn, SQL_SEL_CUADRATURA, p, JavaCallInfra.fieldMapper(Cuadratura.class));
            out.P_OUT_MODALIDAD = JavaCallInfra.query(conn, SQL_SEL_MODALIDAD, p, JavaCallInfra.fieldMapper(Modalidad.class));
            out.P_OUT_PREFERENCIA = JavaCallInfra.query(conn, SQL_SEL_PREFERENCIA, p, JavaCallInfra.fieldMapper(Preferencia.class));
            out.P_OUT_SUCURSAL = JavaCallInfra.query(conn, SQL_SEL_SUCURSAL, p, JavaCallInfra.fieldMapper(Sucursal.class));

            plataforma.callAgregarLog(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "INFO",
                    "VencerOperacion op=" + in.p_IdOperacion);
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("VencerOperacion failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public Long p_IdOperacion; public Long p_IdPortafolio; public String p_FechaVencimiento;
    }
    public static class Cuadratura { public String Fecha; public Long IdOperacion; public String Estado; }
    public static class Modalidad { public Long IdModalidadPago; public String Descripcion; }
    public static class Preferencia { public Long IdPortafolio; public Long IdModalidadPago; public String IdMoneda; }
    public static class Sucursal { public Long IdSucursal; public String Descripcion; }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Cuadratura> P_OUT_CUADRATURA = new ArrayList<>();
        public List<Modalidad> P_OUT_MODALIDAD = new ArrayList<>();
        public List<Preferencia> P_OUT_PREFERENCIA = new ArrayList<>();
        public List<Sucursal> P_OUT_SUCURSAL = new ArrayList<>();
    }
}
