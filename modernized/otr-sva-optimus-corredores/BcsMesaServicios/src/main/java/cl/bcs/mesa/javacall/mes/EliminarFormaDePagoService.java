package cl.bcs.mesa.javacall.mes;

import cl.bcs.mesa.javacall.JavaCallInfra;
import cl.bcs.mesa.javacall.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

@Service
public class EliminarFormaDePagoService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(EliminarFormaDePagoService.class);
    private static final String SQL_DELETE = JavaCallInfra.loadSql("cl/bcs/mesa/MES/sql/EliminarFormaDePago_DeleteComprobantePagoOperacion.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;

    public EliminarFormaDePagoService(DataSource dataSource) { this.dataSource = dataSource; }

    @Override public String fqn() { return "cl.bcs.mesa.MES.EliminarFormaDePago"; }

    @Override
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);
            var jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));
            var p = new MapSqlParameterSource()
                    .addValue("IdComprobantePagoOperacion", in.p_IdComprobantePagoOperacion);
            jdbc.update(JavaCallInfra.loadSql("cl/bcs/mesa/MES/sql/EliminarFormaDePago_DeleteComprobantePagoOperacion.sql"), p);
            Salida out = new Salida();
            SalidaObj obj = new SalidaObj();
            obj.IdComprobantePagoOperacion = in.p_IdComprobantePagoOperacion;
            obj.NombreFormaDePago = in.p_NombreFormaDePago;
            out.P_OUT_COMPRABANTEPAGO.add(obj);
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("EliminarFormaDePago failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdComprobantePagoOperacion; public String p_NombreFormaDePago;
        public Long p_IdEmpresa; public Long p_IdUsuario;
    }
    public static class SalidaObj {
        public Long IdComprobantePagoOperacion; public String NombreFormaDePago;
        public Long IdEmpresa; public Long IdUsuario;
    }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<SalidaObj> P_OUT_COMPRABANTEPAGO = new ArrayList<>();
    }
}
