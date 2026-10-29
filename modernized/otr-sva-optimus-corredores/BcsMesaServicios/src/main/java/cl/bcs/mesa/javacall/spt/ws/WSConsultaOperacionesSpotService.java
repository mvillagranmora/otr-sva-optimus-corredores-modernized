package cl.bcs.mesa.javacall.spt.ws;

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
public class WSConsultaOperacionesSpotService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(WSConsultaOperacionesSpotService.class);
    private static final String SQL_SEL = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/WSConsultaOperacionesSpot_SelectOperacionesSpot.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;

    public WSConsultaOperacionesSpotService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override public String fqn() { return "cl.bcs.mesa.SPT.WSConsultaOperacionesSpot"; }

    @Override
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);
            Map<String, Object> p = new HashMap<>();
            p.put("IdEmpresa", in.p_IdEmpresa);
            p.put("FechaDesde", in.p_FechaDesde);
            p.put("FechaHasta", in.p_FechaHasta);
            p.put("IdEstado", in.p_IdEstado);

            Salida out = new Salida();
            out.P_OUT_OPERACIONES = JavaCallInfra.query(conn, SQL_SEL, p, JavaCallInfra.fieldMapper(OperacionSpot.class));
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("WSConsultaOperacionesSpot failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public String p_FechaDesde; public String p_FechaHasta; public String p_IdEstado;
    }
    public static class OperacionSpot {
        public Long IdOperacion; public String FechaOperacion; public BigDecimal MontoCompra;
        public BigDecimal MontoVenta; public String IdMoneda; public String IdEstado; public String IdCliente;
    }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<OperacionSpot> P_OUT_OPERACIONES = new ArrayList<>();
    }
}
