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
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConfirmarOperacionesService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ConfirmarOperacionesService.class);
    private static final String SQL_CONFIRMAR = JavaCallInfra.loadSql("cl/bcs/mesa/MES/sql/ConfirmarOperaciones.sql");
    private static final String SQL_SELECT_OP = JavaCallInfra.loadSql("cl/bcs/mesa/MES/sql/ConfirmarOperaciones_SelectOperacionIngresadaRF.sql");
    private static final String SQL_SELECT_DET = JavaCallInfra.loadSql("cl/bcs/mesa/MES/sql/ConfirmarOperaciones_SelectDetalleOperacionIngresadaRF.sql");
    private static final String SQL_SELECT_FP = JavaCallInfra.loadSql("cl/bcs/mesa/MES/sql/ConfirmarOperaciones_SelectFormasPagoOperacionIngresadaRF.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public ConfirmarOperacionesService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource; this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.MES.ConfirmarOperaciones"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);
            var jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));

            // Select operation data
            Map<String, Object> p = new HashMap<>();
            p.put("IdEmpresa", in.p_IdEmpresa);
            p.put("IdOperacion", in.p_IdOperacion);
            List<OperacionData> operaciones = JavaCallInfra.query(conn, SQL_SELECT_OP, p, JavaCallInfra.fieldMapper(OperacionData.class));

            if (operaciones.isEmpty()) {
                JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L);
                err.addProperty("error", "Operacion no encontrada"); return err;
            }

            // Confirm the operation
            var cp = new MapSqlParameterSource()
                    .addValue("IdEmpresa", in.p_IdEmpresa)
                    .addValue("IdOperacion", in.p_IdOperacion)
                    .addValue("IdUsuario", in.p_IdUsuario)
                    .addValue("IdModulo", in.p_IdModulo);
            jdbc.update(SQL_CONFIRMAR, cp);

            Salida out = new Salida();
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("ConfirmarOperaciones failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdOperacion; public Long p_IdEmpresa; public Long p_IdUsuario;
        public Long p_IdCliente; public String p_IdModulo; public String p_IdMesa;
    }
    public static class OperacionData {
        public Long IdOperacion; public Long FolioOperacion; public String IdTipoProducto;
        public String IdTipoOperacion; public BigDecimal Monto;
    }
    public static class Salida { public Long P_OUT_ERRORESCONTADOR = 0L; }
}
