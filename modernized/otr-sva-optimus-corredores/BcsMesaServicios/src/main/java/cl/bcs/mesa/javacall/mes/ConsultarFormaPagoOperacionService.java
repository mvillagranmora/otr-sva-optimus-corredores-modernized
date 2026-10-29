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
public class ConsultarFormaPagoOperacionService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarFormaPagoOperacionService.class);
    private static final String SQL_SELECT = JavaCallInfra.loadSql("cl/bcs/mesa/sql/ConsultarFormaPagoOperacion_SelFormaPagoOperacion.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    public ConsultarFormaPagoOperacionService(DataSource dataSource) { this.dataSource = dataSource; }

    @Override public String fqn() { return "cl.bcs.mesa.MES.ConsultarFormaPagoOperacion"; }

    @Override
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);
            Map<String, Object> p = new HashMap<>();
            p.put("IdEmpresa", in.p_IdEmpresa);
            p.put("IdOperacion", in.p_IdOperacion);

            Salida out = new Salida();
            out.P_OUT_FORMAS_PAGO = JavaCallInfra.query(conn, SQL_SELECT, p, JavaCallInfra.fieldMapper(FormaPago.class));
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("ConsultarFormaPagoOperacion failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public Long p_IdOperacion;
    }
    public static class FormaPago {
        public Long IdComprobantePagoOperacion; public Long IdOperacion;
        public String NombreFormaDePago; public BigDecimal Monto; public String IdEstado;
        public String IdMedioDePago; public String IdModalidadDePago;
    }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<FormaPago> P_OUT_FORMAS_PAGO = new ArrayList<>();
    }
}
