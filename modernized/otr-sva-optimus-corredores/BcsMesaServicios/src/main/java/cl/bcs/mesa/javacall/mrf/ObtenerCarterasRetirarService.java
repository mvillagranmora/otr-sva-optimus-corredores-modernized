package cl.bcs.mesa.javacall.mrf;

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
public class ObtenerCarterasRetirarService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ObtenerCarterasRetirarService.class);
    private static final String SQL_SELECT_RF = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/ObtenerCarterasRetirar_SelectCarterasRF.sql");
    private static final String SQL_SELECT_IF = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/ObtenerCarterasRetirar_SelectCarterasIF.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    public ObtenerCarterasRetirarService(DataSource dataSource) { this.dataSource = dataSource; }

    @Override public String fqn() { return "cl.bcs.mesa.MRF.ObtenerCarterasRetirar"; }

    @Override
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);
            Map<String, Object> p = new HashMap<>();
            p.put("IdEmpresa", in.p_IdEmpresa);
            p.put("IdOperacion", in.p_IdOperacion);
            p.put("IdTipoProducto", in.p_IdTipoProducto);

            Salida out = new Salida();
            String sql = "MERF".equals(in.p_IdTipoProducto) ? SQL_SELECT_RF : SQL_SELECT_IF;
            out.P_OUT_CARTERAS = JavaCallInfra.query(conn, sql, p, JavaCallInfra.fieldMapper(Cartera.class));
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("ObtenerCarterasRetirar failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public Long p_IdOperacion; public String p_IdTipoProducto;
    }
    public static class Cartera {
        public Long IdCartera; public Long IdPortafolio; public String IdInstrumento;
        public BigDecimal Cantidad; public BigDecimal Disponible;
    }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Cartera> P_OUT_CARTERAS = new ArrayList<>();
    }
}
