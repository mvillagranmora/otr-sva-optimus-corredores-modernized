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
public class IngresarDetalleOperacionMesaService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(IngresarDetalleOperacionMesaService.class);
    private static final String SQL_SEL_CLIENTE = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/IngresarDetalleOperacionMesa_SelectCliente.sql");
    private static final String SQL_SEL_BLOQUEO = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/IngresarDetalleOperacionMesa_SelectBloqueoCarteras.sql");
    private static final String SQL_SEL_FORMA_PAGO_PORT = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/IngresarDetalleOperacionMesa_SelectFormaPagoPortafolio.sql");
    private static final String SQL_SEL_FORMAS_DEFECTO = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/IngresarDetalleOperacionMesa_SelectFormasDePagoDefecto.sql");
    private static final String SQL_SEL_FORMAS_DEFECTO_CLI = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/IngresarDetalleOperacionMesa_SelectFormasDePagoDefectoCliente.sql");
    private static final String SQL_SEL_ID_CARTERA = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/IngresarDetalleOperacionMesa_SelectIdCartera.sql");
    private static final String SQL_SEL_PARAMETRO = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/IngresarDetalleOperacionMesa_SelectParametroConfiguracion.sql");
    private static final String SQL_SEL_SUCURSAL = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/IngresarDetalleOperacionMesa_SelectSucursalEmpleado.sql");
    private static final String SQL_DEL_FORMAS = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/IngresarDetalleOperacionMesa_DeleteFormasDePagoDefecto.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public IngresarDetalleOperacionMesaService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource;
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.MRF.IngresarDetalleOperacionMesa"; }

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
            p.put("IdSocioNegocio", in.p_IdSocioNegocio);
            p.put("IdPortafolio", in.p_IdPortafolio);
            p.put("IdOperacion", in.p_IdOperacion);
            p.put("IdInstrumento", in.p_IdInstrumento);

            Salida out = new Salida();
            out.P_OUT_CLIENTE = JavaCallInfra.query(conn, SQL_SEL_CLIENTE, p, JavaCallInfra.fieldMapper(Cliente.class));
            out.P_OUT_BLOQUEO = JavaCallInfra.query(conn, SQL_SEL_BLOQUEO, p, JavaCallInfra.fieldMapper(Bloqueo.class));
            out.P_OUT_FORMA_PAGO = JavaCallInfra.query(conn, SQL_SEL_FORMA_PAGO_PORT, p, JavaCallInfra.fieldMapper(FormaPago.class));
            out.P_OUT_FORMAS_DEFECTO = JavaCallInfra.query(conn, SQL_SEL_FORMAS_DEFECTO, p, JavaCallInfra.fieldMapper(FormaPago.class));
            out.P_OUT_FORMAS_DEFECTO_CLI = JavaCallInfra.query(conn, SQL_SEL_FORMAS_DEFECTO_CLI, p, JavaCallInfra.fieldMapper(FormaPago.class));
            out.P_OUT_ID_CARTERA = JavaCallInfra.query(conn, SQL_SEL_ID_CARTERA, p, JavaCallInfra.fieldMapper(Cartera.class));
            out.P_OUT_PARAMETRO = JavaCallInfra.query(conn, SQL_SEL_PARAMETRO, p, JavaCallInfra.fieldMapper(Parametro.class));
            out.P_OUT_SUCURSAL = JavaCallInfra.query(conn, SQL_SEL_SUCURSAL, p, JavaCallInfra.fieldMapper(Sucursal.class));

            var jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));
            jdbc.update(SQL_DEL_FORMAS, new MapSqlParameterSource(p));

            plataforma.callAgregarLog(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "INFO",
                    "IngresarDetalleOperacionMesa op=" + in.p_IdOperacion);
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("IngresarDetalleOperacionMesa failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public Long p_IdSocioNegocio; public Long p_IdPortafolio; public Long p_IdOperacion;
        public String p_IdInstrumento;
    }
    public static class Cliente { public Long IdSocioNegocio; public String RazonSocial; public String Rut; }
    public static class Bloqueo { public Long IdCartera; public BigDecimal CantidadBloqueada; }
    public static class FormaPago { public Long IdFormaPago; public String Descripcion; public String IdMoneda; }
    public static class Cartera { public Long IdCartera; public Long IdPortafolio; }
    public static class Parametro { public String IdParametro; public String Valor; }
    public static class Sucursal { public Long IdSucursal; public String Descripcion; }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Cliente> P_OUT_CLIENTE = new ArrayList<>();
        public List<Bloqueo> P_OUT_BLOQUEO = new ArrayList<>();
        public List<FormaPago> P_OUT_FORMA_PAGO = new ArrayList<>();
        public List<FormaPago> P_OUT_FORMAS_DEFECTO = new ArrayList<>();
        public List<FormaPago> P_OUT_FORMAS_DEFECTO_CLI = new ArrayList<>();
        public List<Cartera> P_OUT_ID_CARTERA = new ArrayList<>();
        public List<Parametro> P_OUT_PARAMETRO = new ArrayList<>();
        public List<Sucursal> P_OUT_SUCURSAL = new ArrayList<>();
    }
}
