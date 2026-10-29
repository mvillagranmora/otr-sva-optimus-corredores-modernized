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
public class NotificarEstadoSpotService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(NotificarEstadoSpotService.class);
    private static final String SQL_SEL = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/NotificarEstadoSpot_SelectInfoNotificacion.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;

    public NotificarEstadoSpotService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override public String fqn() { return "cl.bcs.mesa.SPT.NotificarEstadoSpot"; }

    @Override
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);
            Map<String, Object> p = new HashMap<>();
            p.put("IdEmpresa", in.p_IdEmpresa);
            p.put("IdOperacion", in.p_IdOperacion);

            Salida out = new Salida();
            out.P_OUT_INFO = JavaCallInfra.query(conn, SQL_SEL, p, JavaCallInfra.fieldMapper(InfoNotificacion.class));
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("NotificarEstadoSpot failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo; public Long p_IdOperacion;
    }
    public static class InfoNotificacion {
        public Long IdOperacion; public String IdEstado; public String Email; public BigDecimal Monto; public String IdMoneda;
    }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<InfoNotificacion> P_OUT_INFO = new ArrayList<>();
    }
}
