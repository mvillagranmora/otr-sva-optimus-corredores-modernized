package cl.bcs.mesa.javacall.spt;

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
public class ValidarLimitesCuentaPropiaSpotService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ValidarLimitesCuentaPropiaSpotService.class);
    private static final String SQL_SEL = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ValidarLimitesCuentaPropiaSpot_SelectOperacionSpotIngresada.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;

    public ValidarLimitesCuentaPropiaSpotService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override public String fqn() { return "cl.bcs.mesa.SPT.ValidarLimitesCuentaPropiaSpot"; }

    @Override
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);
            Map<String, Object> p = new HashMap<>();
            p.put("IdEmpresa", in.p_IdEmpresa);
            p.put("IdOperacion", in.p_IdOperacion);
            p.put("IdMoneda", in.p_IdMoneda);

            Salida out = new Salida();
            out.P_OUT_OPERACIONES = JavaCallInfra.query(conn, SQL_SEL, p, JavaCallInfra.fieldMapper(OperacionIngresada.class));
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("ValidarLimitesCuentaPropiaSpot failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public Long p_IdOperacion; public String p_IdMoneda;
    }
    public static class OperacionIngresada {
        public Long IdOperacion; public BigDecimal MontoCompra; public BigDecimal MontoVenta;
        public String IdMoneda; public BigDecimal LimiteDisponible;
    }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<OperacionIngresada> P_OUT_OPERACIONES = new ArrayList<>();
    }
}
