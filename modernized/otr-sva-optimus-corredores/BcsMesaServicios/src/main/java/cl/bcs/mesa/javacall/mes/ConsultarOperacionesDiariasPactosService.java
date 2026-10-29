package cl.bcs.mesa.javacall.mes;

import cl.bcs.mesa.javacall.JavaCallInfra;
import cl.bcs.mesa.javacall.JavaCallService;
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
public class ConsultarOperacionesDiariasPactosService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarOperacionesDiariasPactosService.class);
    private static final String SQL_MAIN = JavaCallInfra.loadSql("cl/bcs/mesa/MES/sql/ConsultarOperacionesDiariasPactos.sql");
    private static final String SQL_DETALLE = JavaCallInfra.loadSql("cl/bcs/mesa/MES/sql/ConsultarOperacionesDiariasPactos_SelectDetalleOperaciones.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    public ConsultarOperacionesDiariasPactosService(DataSource dataSource) { this.dataSource = dataSource; }

    @Override public String fqn() { return "cl.bcs.mesa.MES.ConsultarOperacionesDiariasPactos"; }

    @Override
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);
            Map<String, Object> p = new HashMap<>();
            p.put("IdEmpresa", in.p_IdEmpresa);
            p.put("FechaOperacion", in.p_FechaOperacion);
            p.put("IdTipoProducto", in.p_IdTipoProducto);
            p.put("IdCliente", in.p_IdCliente);

            Salida out = new Salida();
            out.P_OUT_OPERACIONES = JavaCallInfra.query(conn, SQL_MAIN, p, JavaCallInfra.fieldMapper(Operacion.class));
            out.P_OUT_DETALLE = JavaCallInfra.query(conn, SQL_DETALLE, p, JavaCallInfra.fieldMapper(DetalleOperacion.class));
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("ConsultarOperacionesDiariasPactos failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public String p_FechaOperacion; public String p_IdTipoProducto; public Long p_IdCliente;
    }
    public static class Operacion {
        public Long IdOperacion; public String IdTipoOperacion; public String IdEstado;
        public BigDecimal MontoMonedaPrincipal; public String MonedaPrincipal;
        public String FechaOperacion; public String FechaVencimiento;
        public Long IdCliente; public String NombreCliente;
    }
    public static class DetalleOperacion {
        public Long IdOperacion; public Long IdCartera; public String IdTipoInstrumento;
        public BigDecimal Monto; public String IdMoneda;
    }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Operacion> P_OUT_OPERACIONES = new ArrayList<>();
        public List<DetalleOperacion> P_OUT_DETALLE = new ArrayList<>();
    }
}
