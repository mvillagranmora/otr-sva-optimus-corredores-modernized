package cl.bcs.mesa.javacall.mes;

import cl.bcs.mesa.javacall.JavaCallInfra;
import cl.bcs.mesa.javacall.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Internal helper — call()→null in legacy; returns empty on direct dispatch
@Service
public class ConsultarConfiguracionMesaService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarConfiguracionMesaService.class);
    private static final String SQL_SELECT = JavaCallInfra.loadSql("cl/bcs/mesa/sql/ConsultarConfiguracionMesa.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    public ConsultarConfiguracionMesaService(DataSource dataSource) { this.dataSource = dataSource; }

    @Override public String fqn() { return "cl.bcs.mesa.utils.ConsultarConfiguracionMesa"; }

    @Override
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);
            Map<String, Object> p = new HashMap<>();
            p.put("IdEmpresa", in.p_IdEmpresa);
            p.put("IdTipoProducto", in.p_IdTipoProducto);
            p.put("CodigoParametro", in.p_CodigoParametro);

            Salida out = new Salida();
            out.P_OUT_CONFIGURACIONES = JavaCallInfra.query(conn, SQL_SELECT, p, JavaCallInfra.fieldMapper(Configuracion.class));
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("ConsultarConfiguracionMesa failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public String p_IdTipoProducto; public String p_CodigoParametro;
    }
    public static class Configuracion {
        public Long IdEmpresa; public String IdTipoProducto; public String CodigoParametro;
        public String Descripcion; public String Valor; public String ModificadorParametro;
    }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Configuracion> P_OUT_CONFIGURACIONES = new ArrayList<>();
    }
}
