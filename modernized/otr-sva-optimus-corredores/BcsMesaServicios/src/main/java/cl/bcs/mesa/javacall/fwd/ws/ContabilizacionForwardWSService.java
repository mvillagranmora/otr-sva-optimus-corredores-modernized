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
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ContabilizacionForwardWSService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ContabilizacionForwardWSService.class);
    private static final String SQL_SEL_CENTRAL = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/ws/sql/ContabilizacionForwardWS_SelectCentralizacion.sql");
    private static final String SQL_SEL_CENTRAL_TIPO = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/ws/sql/ContabilizacionForwardWS_SelectCentralizacionTipo.sql");
    private static final String SQL_SEL_PRECOMP = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/ws/sql/ContabilizacionForwardWS_SelectPrecomprobantes.sql");
    private static final String SQL_DEL_PRECOMP = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/ws/sql/ContabilizacionForwardWS_EliminarPrecomprobantes.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public ContabilizacionForwardWSService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource;
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.FWD.ws.ContabilizacionForwardWS"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);

            Map<String, Object> p = new HashMap<>();
            p.put("IdEmpresa", in.p_IdEmpresa);
            p.put("Fecha", in.p_Fecha);

            Salida out = new Salida();
            out.P_OUT_CENTRAL = JavaCallInfra.query(conn, SQL_SEL_CENTRAL, p, JavaCallInfra.fieldMapper(Centralizacion.class));
            out.P_OUT_CENTRAL_TIPO = JavaCallInfra.query(conn, SQL_SEL_CENTRAL_TIPO, p, JavaCallInfra.fieldMapper(Centralizacion.class));
            out.P_OUT_PRECOMP = JavaCallInfra.query(conn, SQL_SEL_PRECOMP, p, JavaCallInfra.fieldMapper(Precomprobante.class));

            var jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));
            jdbc.update(SQL_DEL_PRECOMP, new MapSqlParameterSource(p));

            plataforma.callAgregarLog(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "INFO",
                    "ContabilizacionForwardWS fecha=" + in.p_Fecha);
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("ContabilizacionForwardWS failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo; public String p_Fecha;
    }
    public static class Centralizacion { public String TipoCentralizacion; public BigDecimal Monto; public String CuentaContable; }
    public static class Precomprobante { public Long IdPrecomprobante; public BigDecimal Monto; public String TipoMovimiento; }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Centralizacion> P_OUT_CENTRAL = new ArrayList<>();
        public List<Centralizacion> P_OUT_CENTRAL_TIPO = new ArrayList<>();
        public List<Precomprobante> P_OUT_PRECOMP = new ArrayList<>();
    }
}
