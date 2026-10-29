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
import java.sql.Connection;

@Service
public class ConfirmarModificarFechaMovimientoService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ConfirmarModificarFechaMovimientoService.class);
    private static final String SQL_UPDATE_CONFIRMAR = JavaCallInfra.loadSql("cl/bcs/mesa/MES/sql/ConfirmarModificarFechaMovimiento_UpdateConfirmarModificarFechaMovimiento.sql");
    private static final String SQL_UPDATE_COMPROBANTE = JavaCallInfra.loadSql("cl/bcs/mesa/MES/sql/ConfirmarModificarFechaMovimiento_UpdateModificarFechaComprobantePago.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public ConfirmarModificarFechaMovimientoService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource; this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.MES.ConfirmarModificarFechaMovimiento"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);
            var jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));

            var p = new MapSqlParameterSource()
                    .addValue("IdEmpresa", in.p_IdEmpresa)
                    .addValue("IdOperacion", in.p_IdOperacion)
                    .addValue("IdUsuario", in.p_IdUsuario)
                    .addValue("IdModulo", in.p_IdModulo)
                    .addValue("NuevaFechaMovimiento", in.p_NuevaFechaMovimiento);

            jdbc.update(SQL_UPDATE_CONFIRMAR, p);
            jdbc.update(SQL_UPDATE_COMPROBANTE, p);

            Salida out = new Salida();
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("ConfirmarModificarFechaMovimiento failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public Long p_IdOperacion; public String p_NuevaFechaMovimiento;
    }
    public static class Salida { public Long P_OUT_ERRORESCONTADOR = 0L; }
}
