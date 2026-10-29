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
public class CrearSolicitudPuntasService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(CrearSolicitudPuntasService.class);
    private static final String SQL_SEL_CLIENTE = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/CrearSolicitudPuntas_SelectCliente.sql");
    private static final String SQL_INS = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/CrearSolicitudPuntas_InsertSolicitudPuntas.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public CrearSolicitudPuntasService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource;
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.SPT.CrearSolicitudPuntas"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);

            Map<String, Object> p = new HashMap<>();
            p.put("IdEmpresa", in.p_IdEmpresa);
            p.put("IdSocioNegocio", in.p_IdSocioNegocio);

            Salida out = new Salida();
            out.P_OUT_CLIENTE = JavaCallInfra.query(conn, SQL_SEL_CLIENTE, p, JavaCallInfra.fieldMapper(Cliente.class));

            var jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));
            var pIns = new MapSqlParameterSource(p)
                    .addValue("IdMoneda", in.p_IdMoneda)
                    .addValue("MontoSolicitud", in.p_MontoSolicitud)
                    .addValue("IdUsuario", in.p_IdUsuario);
            jdbc.update(SQL_INS, pIns);

            plataforma.callAgregarLog(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "INFO",
                    "CrearSolicitudPuntas cliente=" + in.p_IdSocioNegocio);
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("CrearSolicitudPuntas failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public Long p_IdSocioNegocio; public String p_IdMoneda; public BigDecimal p_MontoSolicitud;
    }
    public static class Cliente { public Long IdSocioNegocio; public String RazonSocial; public String Rut; }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Cliente> P_OUT_CLIENTE = new ArrayList<>();
    }
}
