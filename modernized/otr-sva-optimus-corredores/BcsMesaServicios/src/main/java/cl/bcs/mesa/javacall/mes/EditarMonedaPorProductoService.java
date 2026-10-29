package cl.bcs.mesa.javacall.mes;

import cl.bcs.mesa.javacall.JavaCallInfra;
import cl.bcs.mesa.javacall.JavaCallService;
import cl.bcs.mesa.javacall.PlataformaBridge;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EditarMonedaPorProductoService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(EditarMonedaPorProductoService.class);
    private static final String SQL_SELECT_EXISTENTE = JavaCallInfra.loadSql("cl/bcs/mesa/MES/sql/EditarMonedaPorProducto_ConsultarMonedaPorProductoExistente.sql");
    private static final String SQL_SELECT = JavaCallInfra.loadSql("cl/bcs/mesa/MES/sql/EditarMonedaPorProducto_ConsultarMonedaPorProducto.sql");
    private static final String SQL_UPDATE = JavaCallInfra.loadSql("cl/bcs/mesa/MES/sql/EditarMonedaPorProducto_UpdateMonedaPorProducto.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public EditarMonedaPorProductoService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource;
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.MES.EditarMonedaPorProducto"; }

    @Override
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);
            var jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));

            // Validate requisitos basicos via plataforma
            JsonObject reqResult = plataforma.callRevisarRequisitosBasicos(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
            if (reqResult.has("P_OUT_ERRORESCONTADOR") && reqResult.get("P_OUT_ERRORESCONTADOR").getAsLong() > 0) {
                JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L);
                err.addProperty("error", "RevisarRequisitosBasicos failed"); return err;
            }

            // Update moneda por producto
            var p = new MapSqlParameterSource();
            p.addValue("IdEmpresa", in.p_IdEmpresa);
            p.addValue("IdTipoProducto", in.p_IdTipoProducto);
            p.addValue("IdMonedaPrincipal", in.p_IdMonedaPrincipal);
            p.addValue("IdMonedaSecundaria", in.p_IdMonedaSecundaria);
            p.addValue("IdEstado", in.p_IdEstado);
            jdbc.update(SQL_UPDATE, p);

            Salida out = new Salida();
            // Read updated record
            Map<String, Object> selectParams = new HashMap<>();
            selectParams.put("IdEmpresa", in.p_IdEmpresa);
            selectParams.put("IdTipoProducto", in.p_IdTipoProducto);
            List<MonedaProducto> resultados = JavaCallInfra.query(conn, SQL_SELECT, selectParams, JavaCallInfra.fieldMapper(MonedaProducto.class));
            out.P_OUT_MONEDAS = resultados;

            // Log operation
            plataforma.callAgregarLog(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "INFO", "EditarMonedaPorProducto ejecutado");

            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("EditarMonedaPorProducto failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public String p_IdTipoProducto; public String p_IdMonedaPrincipal;
        public String p_IdMonedaSecundaria; public String p_IdEstado;
    }
    public static class MonedaProducto {
        public Long IdEmpresa; public String IdTipoProducto;
        public String IdMonedaPrincipal; public String IdMonedaSecundaria; public String IdEstado;
    }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<MonedaProducto> P_OUT_MONEDAS = new ArrayList<>();
    }
}
