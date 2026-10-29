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
public class ValidarReasignacionMesaRFService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ValidarReasignacionMesaRFService.class);
    private static final String SQL_SELECT_OP = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/ValidarReasignacionMesaRF_SelectConsultarOperacionMesa.sql");
    private static final String SQL_SELECT_CARTERA = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/ValidarReasignacionMesaRF_SelectConsultarCarteraOperacionMesa.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    public ValidarReasignacionMesaRFService(DataSource dataSource) { this.dataSource = dataSource; }

    @Override public String fqn() { return "cl.bcs.mesa.MRF.ValidarReasignacionMesaRF"; }

    @Override
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);
            Map<String, Object> p = new HashMap<>();
            p.put("IdEmpresa", in.p_IdEmpresa);
            p.put("IdOperacion", in.p_IdOperacion);
            p.put("IdPortafolioDestino", in.p_IdPortafolioDestino);

            Salida out = new Salida();
            out.P_OUT_OPERACION = JavaCallInfra.query(conn, SQL_SELECT_OP, p, JavaCallInfra.fieldMapper(Operacion.class));
            out.P_OUT_CARTERA = JavaCallInfra.query(conn, SQL_SELECT_CARTERA, p, JavaCallInfra.fieldMapper(Cartera.class));
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("ValidarReasignacionMesaRF failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public Long p_IdOperacion; public Long p_IdPortafolioDestino;
    }
    public static class Operacion {
        public Long IdOperacion; public Long IdPortafolio; public String IdEstado;
        public BigDecimal Monto; public String IdTipoProducto;
    }
    public static class Cartera { public Long IdCartera; public BigDecimal Disponible; }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Operacion> P_OUT_OPERACION = new ArrayList<>();
        public List<Cartera> P_OUT_CARTERA = new ArrayList<>();
    }
}
