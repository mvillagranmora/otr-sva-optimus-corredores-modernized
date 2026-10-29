package cl.bcs.mesa.javacall.fwd.ws;

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
public class CuadraturaForwardWSService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(CuadraturaForwardWSService.class);
    private static final String SQL_SEL_CODIGO = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/ws/sql/CuadraturaForwardWS_SelectCodigoUsuario.sql");
    private static final String SQL_SEL_CUADRATURA = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/ws/sql/CuadraturaForwardWS_SelectCuadratura.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public CuadraturaForwardWSService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource;
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.FWD.ws.CuadraturaForwardWS"; }

    @Override
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);

            Map<String, Object> p = new HashMap<>();
            p.put("IdEmpresa", in.p_IdEmpresa);
            p.put("Fecha", in.p_Fecha);
            p.put("IdUsuario", in.p_IdUsuario);

            Salida out = new Salida();
            out.P_OUT_CODIGO_USUARIO = JavaCallInfra.query(conn, SQL_SEL_CODIGO, p, JavaCallInfra.fieldMapper(CodigoUsuario.class));
            out.P_OUT_CUADRATURA = JavaCallInfra.query(conn, SQL_SEL_CUADRATURA, p, JavaCallInfra.fieldMapper(Cuadratura.class));

            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("CuadraturaForwardWS failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo; public String p_Fecha;
    }
    public static class CodigoUsuario { public String CodigoUsuario; public String NombreUsuario; }
    public static class Cuadratura { public Long IdOperacion; public String IdEstado; public BigDecimal Diferencia; public String FechaCuadratura; }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<CodigoUsuario> P_OUT_CODIGO_USUARIO = new ArrayList<>();
        public List<Cuadratura> P_OUT_CUADRATURA = new ArrayList<>();
    }
}
