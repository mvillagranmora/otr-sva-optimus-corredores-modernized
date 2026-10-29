package cl.bcs.mesa.javacall.fwd;

import cl.bcs.mesa.javacall.JavaCallInfra;
import cl.bcs.mesa.javacall.JavaCallService;
import cl.bcs.mesa.javacall.PlataformaBridge;
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
public class ValidarLimitesCuentaPropiaForwardService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ValidarLimitesCuentaPropiaForwardService.class);
    private static final String SQL_SEL_LIMITES = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/ValidarLimitesCuentaPropiaForward_SelectLimitesFWD.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public ValidarLimitesCuentaPropiaForwardService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource;
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.FWD.ValidarLimitesCuentaPropiaForward"; }

    @Override
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);

            Map<String, Object> p = new HashMap<>();
            p.put("IdEmpresa", in.p_IdEmpresa);
            p.put("IdPortafolio", in.p_IdPortafolio);
            p.put("MontoOperacion", in.p_MontoOperacion);

            Salida out = new Salida();
            out.P_OUT_LIMITES = JavaCallInfra.query(conn, SQL_SEL_LIMITES, p, JavaCallInfra.fieldMapper(Limite.class));

            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("ValidarLimitesCuentaPropiaForward failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public Long p_IdPortafolio; public BigDecimal p_MontoOperacion;
    }
    public static class Limite {
        public String TipoLimite; public BigDecimal LimiteMaximo; public BigDecimal LimiteUsado;
        public Boolean Excedido;
    }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Limite> P_OUT_LIMITES = new ArrayList<>();
    }
}
