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
public class ConsultarPuntasMantenedorService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarPuntasMantenedorService.class);
    private static final String SQL_SEL = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ConsultarPuntasMantenedor.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    public ConsultarPuntasMantenedorService(DataSource dataSource) { this.dataSource = dataSource; }

    @Override public String fqn() { return "cl.bcs.mesa.SPT.ConsultarPuntasMantenedor"; }

    @Override
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);
            Map<String, Object> p = new HashMap<>();
            p.put("IdEmpresa", in.p_IdEmpresa);
            p.put("IdMoneda", in.p_IdMoneda);

            Salida out = new Salida();
            out.P_OUT_PUNTAS = JavaCallInfra.query(conn, SQL_SEL, p, JavaCallInfra.fieldMapper(Punta.class));
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("ConsultarPuntasMantenedor failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo; public String p_IdMoneda;
    }
    public static class Punta { public Long IdPunta; public String IdMoneda; public BigDecimal Compra; public BigDecimal Venta; }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Punta> P_OUT_PUNTAS = new ArrayList<>();
    }
}
