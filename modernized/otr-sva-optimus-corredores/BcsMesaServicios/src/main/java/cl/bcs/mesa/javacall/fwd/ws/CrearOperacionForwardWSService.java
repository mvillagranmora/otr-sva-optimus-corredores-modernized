package cl.bcs.mesa.javacall.fwd.ws;

import cl.bcs.mesa.javacall.JavaCallInfra;
import cl.bcs.mesa.javacall.JavaCallService;
import cl.bcs.mesa.javacall.PlataformaBridge;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CrearOperacionForwardWSService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(CrearOperacionForwardWSService.class);
    private static final String SQL_SEL_CLASIFICACION = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/ws/sql/CrearOperacionForwardWS_SelectClasificacionCartera.sql");
    private static final String SQL_SEL_EJECUTIVO = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/ws/sql/CrearOperacionForwardWS_SelectEjecutivo.sql");
    private static final String SQL_SEL_FIXING = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/ws/sql/CrearOperacionForwardWS_SelectFixing.sql");
    private static final String SQL_SEL_FORMA_PAGO = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/ws/sql/CrearOperacionForwardWS_SelectFormaPago.sql");
    private static final String SQL_SEL_INSTRUMENTO = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/ws/sql/CrearOperacionForwardWS_SelectInstrumento.sql");
    private static final String SQL_SEL_MESA = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/ws/sql/CrearOperacionForwardWS_SelectMesa.sql");
    private static final String SQL_SEL_MODALIDAD = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/ws/sql/CrearOperacionForwardWS_SelectModalidadLiquidacion.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public CrearOperacionForwardWSService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource;
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.FWD.ws.CrearOperacionForwardWS"; }

    @Override
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);

            Map<String, Object> p = new HashMap<>();
            p.put("IdEmpresa", in.p_IdEmpresa);
            p.put("IdPortafolio", in.p_IdPortafolio);

            Salida out = new Salida();
            out.P_OUT_CLASIFICACION = JavaCallInfra.query(conn, SQL_SEL_CLASIFICACION, p, JavaCallInfra.fieldMapper(Clasificacion.class));
            out.P_OUT_EJECUTIVO = JavaCallInfra.query(conn, SQL_SEL_EJECUTIVO, p, JavaCallInfra.fieldMapper(Ejecutivo.class));
            out.P_OUT_FIXING = JavaCallInfra.query(conn, SQL_SEL_FIXING, p, JavaCallInfra.fieldMapper(Fixing.class));
            out.P_OUT_FORMA_PAGO = JavaCallInfra.query(conn, SQL_SEL_FORMA_PAGO, p, JavaCallInfra.fieldMapper(FormaPago.class));
            out.P_OUT_INSTRUMENTO = JavaCallInfra.query(conn, SQL_SEL_INSTRUMENTO, p, JavaCallInfra.fieldMapper(Instrumento.class));
            out.P_OUT_MESA = JavaCallInfra.query(conn, SQL_SEL_MESA, p, JavaCallInfra.fieldMapper(Mesa.class));
            out.P_OUT_MODALIDAD = JavaCallInfra.query(conn, SQL_SEL_MODALIDAD, p, JavaCallInfra.fieldMapper(Modalidad.class));

            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("CrearOperacionForwardWS failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo; public Long p_IdPortafolio;
    }
    public static class Clasificacion { public String IdClasificacion; public String Descripcion; }
    public static class Ejecutivo { public Long IdEjecutivo; public String Nombre; }
    public static class Fixing { public String IdFixing; public BigDecimal ValorFixing; }
    public static class FormaPago { public Long IdFormaPago; public String Descripcion; public String IdMoneda; }
    public static class Instrumento { public String IdInstrumento; public String Descripcion; }
    public static class Mesa { public Long IdMesa; public String Descripcion; }
    public static class Modalidad { public String IdModalidad; public String Descripcion; }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Clasificacion> P_OUT_CLASIFICACION = new ArrayList<>();
        public List<Ejecutivo> P_OUT_EJECUTIVO = new ArrayList<>();
        public List<Fixing> P_OUT_FIXING = new ArrayList<>();
        public List<FormaPago> P_OUT_FORMA_PAGO = new ArrayList<>();
        public List<Instrumento> P_OUT_INSTRUMENTO = new ArrayList<>();
        public List<Mesa> P_OUT_MESA = new ArrayList<>();
        public List<Modalidad> P_OUT_MODALIDAD = new ArrayList<>();
    }
}
