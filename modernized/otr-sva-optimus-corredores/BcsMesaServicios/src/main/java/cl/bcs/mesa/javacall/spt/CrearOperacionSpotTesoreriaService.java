package cl.bcs.mesa.javacall.spt;

import cl.bcs.mesa.javacall.JavaCallInfra;
import cl.bcs.mesa.javacall.JavaCallService;
import cl.bcs.mesa.javacall.PlataformaBridge;
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

@Service
public class CrearOperacionSpotTesoreriaService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(CrearOperacionSpotTesoreriaService.class);
    private static final String SQL_SEL = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/CrearOperacionSpotTesoreria_SelectClientesPersona.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public CrearOperacionSpotTesoreriaService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource;
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.SPT.CrearOperacionSpotTesoreria"; }

    @Override
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);

            Map<String, Object> p = new HashMap<>();
            p.put("IdEmpresa", in.p_IdEmpresa);
            p.put("IdSocioNegocio", in.p_IdSocioNegocio);

            Salida out = new Salida();
            out.P_OUT_CLIENTES = JavaCallInfra.query(conn, SQL_SEL, p, JavaCallInfra.fieldMapper(Cliente.class));

            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("CrearOperacionSpotTesoreria failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo; public Long p_IdSocioNegocio;
    }
    public static class Cliente { public Long IdSocioNegocio; public String RazonSocial; public String Rut; }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Cliente> P_OUT_CLIENTES = new ArrayList<>();
    }
}
