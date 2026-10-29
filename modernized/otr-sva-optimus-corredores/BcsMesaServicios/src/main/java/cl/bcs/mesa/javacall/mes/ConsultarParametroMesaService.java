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
public class ConsultarParametroMesaService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarParametroMesaService.class);
    private static final String SQL_SELECT = JavaCallInfra.loadSql("cl/bcs/mesa/sql/ConsultarParametroMesa.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    public ConsultarParametroMesaService(DataSource dataSource) { this.dataSource = dataSource; }

    @Override public String fqn() { return "cl.bcs.mesa.utils.ConsultarParametroMesa"; }

    @Override
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);
            Map<String, Object> p = new HashMap<>();
            p.put("IdEmpresa", in.p_IdEmpresa);
            p.put("NombreTipoParametro", in.p_NombreTipoParametro);
            p.put("NombreParametro", in.p_NombreParametro);
            p.put("IdEstado", in.p_IdEstado);

            Salida out = new Salida();
            out.P_OUT_PARAMETROS = JavaCallInfra.query(conn, SQL_SELECT, p, JavaCallInfra.fieldMapper(Parametro.class));
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("ConsultarParametroMesa failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public String p_NombreTipoParametro;
        public String p_NombreParametro; public String p_IdEstado;
    }
    public static class Parametro {
        public Long IdEmpresa; public Long IdParametro; public String NombreTipoParametro;
        public String ValorParametro; public String IdEstado; public String NombreParametro;
        public Long NumeroDiaSemana;
    }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Parametro> P_OUT_PARAMETROS = new ArrayList<>();
    }
}
