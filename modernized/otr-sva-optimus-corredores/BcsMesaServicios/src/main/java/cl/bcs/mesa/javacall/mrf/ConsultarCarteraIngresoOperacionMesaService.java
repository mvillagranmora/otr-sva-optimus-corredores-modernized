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
public class ConsultarCarteraIngresoOperacionMesaService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarCarteraIngresoOperacionMesaService.class);
    private static final String SQL_SELECT = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/ConsultarCarteraIngresoOperacionMesa_SelectConsultarCartera.sql");
    private static final String SQL_TRANSITOS = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/ConsultarCarteraIngresoOperacionMesa_SelectTransitos.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    public ConsultarCarteraIngresoOperacionMesaService(DataSource dataSource) { this.dataSource = dataSource; }

    @Override public String fqn() { return "cl.bcs.mesa.MRF.ConsultarCarteraIngresoOperacionMesa"; }

    @Override
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);
            Map<String, Object> p = new HashMap<>();
            p.put("IdEmpresa", in.p_IdEmpresa);
            p.put("IdCliente", in.p_IdCliente);
            p.put("IdPortafolio", in.p_IdPortafolio);
            p.put("IdTipoProducto", in.p_IdTipoProducto);
            p.put("IdTipoOperacion", in.p_IdTipoOperacion);

            Salida out = new Salida();
            out.P_OUT_CARTERA = JavaCallInfra.query(conn, SQL_SELECT, p, JavaCallInfra.fieldMapper(Cartera.class));
            out.P_OUT_TRANSITOS = JavaCallInfra.query(conn, SQL_TRANSITOS, p, JavaCallInfra.fieldMapper(Transito.class));
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("ConsultarCarteraIngresoOperacionMesa failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public Long p_IdCliente; public Long p_IdPortafolio;
        public String p_IdTipoProducto; public String p_IdTipoOperacion;
    }
    public static class Cartera {
        public Long IdCartera; public Long IdPortafolio; public String IdTipoProducto;
        public String IdInstrumento; public BigDecimal Cantidad; public BigDecimal Disponible;
    }
    public static class Transito { public Long IdCartera; public BigDecimal Cantidad; public String IdTipoMovimiento; }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Cartera> P_OUT_CARTERA = new ArrayList<>();
        public List<Transito> P_OUT_TRANSITOS = new ArrayList<>();
    }
}
