package cl.bcs.mesa.javacall.mes;

import cl.bcs.mesa.javacall.JavaCallInfra;
import cl.bcs.mesa.javacall.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MarcarOperacionService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(MarcarOperacionService.class);
    private static final String SQL_SELECT_OPS = JavaCallInfra.loadSql("cl/bcs/mesa/MES/sql/MarcarOperaciones_SelectOperaciones.sql");
    private static final String SQL_UPDATE_OPS = JavaCallInfra.loadSql("cl/bcs/mesa/MES/sql/MarcarOperaciones_UpdateOperaciones.sql");
    private static final String SQL_UPDATE_BLOQUEO = JavaCallInfra.loadSql("cl/bcs/mesa/MES/sql/MarcarOperaciones_UpdateBloqueoUsuarioEditor.sql");
    private static final String SQL_SELECT_AUTORIZACION = JavaCallInfra.loadSql("cl/bcs/mesa/MES/sql/MarcarOperaciones_SelectAutorizacionAgenteOperadorMesa.sql");
    private static final String SQL_SELECT_CODIGO_USUARIO = JavaCallInfra.loadSql("cl/bcs/mesa/MES/sql/MarcarOperaciones_SelectCodigoNombreUsuario.sql");
    private static final String SQL_SELECT_ES_SUPERVISOR = JavaCallInfra.loadSql("cl/bcs/mesa/MES/sql/MarcarOperaciones_SelectEsSupervisor.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;

    public MarcarOperacionService(DataSource dataSource) { this.dataSource = dataSource; }

    @Override public String fqn() { return "cl.bcs.mesa.MES.MarcarOperacion"; }

    @Override
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);
            var jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));

            Salida out = new Salida();
            if (in.p_Operaciones == null || in.p_Operaciones.isEmpty()) {
                return GSON.fromJson(GSON.toJson(out), JsonObject.class);
            }

            // Verify supervisor status
            Map<String, Object> supParams = new HashMap<>();
            supParams.put("IdEmpresa", in.p_IdEmpresa);
            supParams.put("IdUsuario", in.p_IdUsuario);
            supParams.put("IdModulo", in.p_IdModulo);
            List<Map<String, Object>> supRows = JavaCallInfra.query(conn, SQL_SELECT_ES_SUPERVISOR, supParams,
                    (rs, i) -> { Map<String, Object> m = new HashMap<>(); m.put("EsSupervisor", rs.getString(1)); return m; });
            boolean esSupervisor = !supRows.isEmpty() && "S".equals(supRows.get(0).get("EsSupervisor"));

            for (OperacionIn op : in.p_Operaciones) {
                var p = new MapSqlParameterSource()
                        .addValue("IdEmpresa", in.p_IdEmpresa)
                        .addValue("IdUsuario", in.p_IdUsuario)
                        .addValue("IdModulo", in.p_IdModulo)
                        .addValue("IdOperacion", op.IdOperacion)
                        .addValue("Accion", op.Accion);
                jdbc.update(SQL_UPDATE_BLOQUEO, p);
                OperacionOut result = new OperacionOut();
                result.IdOperacion = op.IdOperacion;
                out.P_OUT_OPERACIONES.add(result);
            }
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("MarcarOperacion failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class OperacionIn { public String Accion; public Long IdOperacion; }
    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public List<OperacionIn> p_Operaciones;
    }
    public static class OperacionOut { public Long IdOperacion; public String EstadoEdicion; public Long IdUsuarioEdicion; }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<OperacionOut> P_OUT_OPERACIONES = new ArrayList<>();
    }
}
