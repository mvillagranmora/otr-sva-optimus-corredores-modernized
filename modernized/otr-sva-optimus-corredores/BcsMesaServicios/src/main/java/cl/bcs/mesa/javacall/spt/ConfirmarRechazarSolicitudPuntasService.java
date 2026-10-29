package cl.bcs.mesa.javacall.spt;

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
public class ConfirmarRechazarSolicitudPuntasService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ConfirmarRechazarSolicitudPuntasService.class);
    private static final String SQL_SEL = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ConfirmarRechazarSolicitudPuntas_SelectSolicitudPuntas.sql");
    private static final String SQL_UPD = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ConfirmarRechazarSolicitudPuntas_UpdateSolicitudPuntas.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public ConfirmarRechazarSolicitudPuntasService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource;
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.SPT.ConfirmarRechazarSolicitudPuntas"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);

            Map<String, Object> p = new HashMap<>();
            p.put("IdEmpresa", in.p_IdEmpresa);
            p.put("IdSolicitud", in.p_IdSolicitud);

            Salida out = new Salida();
            out.P_OUT_SOLICITUD = JavaCallInfra.query(conn, SQL_SEL, p, JavaCallInfra.fieldMapper(Solicitud.class));

            var jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));
            jdbc.update(SQL_UPD, new MapSqlParameterSource(p)
                    .addValue("IdEstado", in.p_Confirmado ? "CON" : "REC")
                    .addValue("IdUsuario", in.p_IdUsuario));

            plataforma.callAgregarLog(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "INFO",
                    "ConfirmarRechazarSolicitudPuntas sol=" + in.p_IdSolicitud);
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("ConfirmarRechazarSolicitudPuntas failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public Long p_IdSolicitud; public Boolean p_Confirmado;
    }
    public static class Solicitud { public Long IdSolicitud; public String IdEstado; public BigDecimal Monto; }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Solicitud> P_OUT_SOLICITUD = new ArrayList<>();
    }
}
