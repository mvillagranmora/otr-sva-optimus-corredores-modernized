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
public class ConsultarDetallePosicionMRFService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarDetallePosicionMRFService.class);
    private static final String SQL_POSICION = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/ConsultarDetallePosicionMRF_SelectPosicionMRF.sql");
    private static final String SQL_OPERACIONES = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/ConsultarDetallePosicionMRF_SelectOperacionesRFFechaPosicion.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    public ConsultarDetallePosicionMRFService(DataSource dataSource) { this.dataSource = dataSource; }

    @Override public String fqn() { return "cl.bcs.mesa.MRF.ConsultarDetallePosicionMRF"; }

    @Override
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);
            Map<String, Object> p = new HashMap<>();
            p.put("IdEmpresa", in.p_IdEmpresa);
            p.put("IdMetodo", in.p_IdMetodo);
            p.put("FechaPosicion", in.p_FechaPosicion);
            p.put("IdModulo", in.p_IdModulo);

            Salida out = new Salida();
            out.P_OUT_POSICION = JavaCallInfra.query(conn, SQL_POSICION, p, JavaCallInfra.fieldMapper(PosicionMRF.class));
            out.P_OUT_OPERACIONES = JavaCallInfra.query(conn, SQL_OPERACIONES, p, JavaCallInfra.fieldMapper(OperacionMRF.class));
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("ConsultarDetallePosicionMRF failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public Long p_IdMetodo; public String p_FechaPosicion;
    }
    public static class PosicionMRF {
        public BigDecimal Debe; public BigDecimal Haber; public Long IdEvento; public Long IdMesa;
        public String IdTipoEvento; public String IdTipoOperacion; public String NombreEvento;
    }
    public static class OperacionMRF {
        public Long IdOperacion; public String IdTipoProducto; public String IdTipoOperacion;
        public BigDecimal MtoEvento; public String IdEstado;
    }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<PosicionMRF> P_OUT_POSICION = new ArrayList<>();
        public List<OperacionMRF> P_OUT_OPERACIONES = new ArrayList<>();
    }
}
