package cl.bcs.mesa.javacall.fwd.ws;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CerrarMesaForwardWSService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(CerrarMesaForwardWSService.class);
    private static final String SQL_CREATE_TEMP = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/ws/sql/CerrarMesaForwardWS_CreateTablaTemporalCierreMesa.sql");
    private static final String SQL_DROP_TEMP = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/ws/sql/CerrarMesaForwardWS_DropTablaTemporalCierreMesa.sql");
    private static final String SQL_INS_TEMP = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/ws/sql/CerrarMesaForwardWS_InsertTablaTemporalCierreMesa.sql");
    private static final String SQL_SEL_MESA = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/ws/sql/CerrarMesaForwardWS_SelectMesaForward.sql");
    private static final String SQL_CERRAR = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/ws/sql/CerrarMesaForwardWS_CerrarMesas.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public CerrarMesaForwardWSService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource;
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.FWD.ws.CerrarMesaForwardWS"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);

            var jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));
            Map<String, Object> base = new HashMap<>();
            base.put("IdEmpresa", in.p_IdEmpresa);
            base.put("Fecha", in.p_Fecha);

            jdbc.getJdbcTemplate().execute(SQL_CREATE_TEMP);
            jdbc.update(SQL_INS_TEMP, base);

            Salida out = new Salida();
            out.P_OUT_MESA = JavaCallInfra.query(conn, SQL_SEL_MESA, base, JavaCallInfra.fieldMapper(Mesa.class));

            jdbc.update(SQL_CERRAR, base);
            jdbc.getJdbcTemplate().execute(SQL_DROP_TEMP);

            plataforma.callAgregarLog(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "INFO",
                    "CerrarMesaForwardWS fecha=" + in.p_Fecha);
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("CerrarMesaForwardWS failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo; public String p_Fecha;
    }
    public static class Mesa { public Long IdMesa; public String Descripcion; public String IdEstado; }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Mesa> P_OUT_MESA = new ArrayList<>();
    }
}
