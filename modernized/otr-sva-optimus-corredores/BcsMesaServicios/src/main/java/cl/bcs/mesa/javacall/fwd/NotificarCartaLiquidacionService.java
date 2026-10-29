package cl.bcs.mesa.javacall.fwd;

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
public class NotificarCartaLiquidacionService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(NotificarCartaLiquidacionService.class);
    private static final String SQL_DEL_TABLA = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/NotificarCartaLiquidacion_DeleteInfoCartasLiquidacionTablaPaso.sql");
    private static final String SQL_INS_TABLA = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/NotificarCartaLiquidacion_InsertInfoCartasLiquidacionTablaPaso.sql");
    private static final String SQL_SEL_INFO = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/NotificarCartaLiquidacion_SelectInformacionCartaLiquidacion.sql");
    private static final String SQL_SEL_CLIENTES = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/NotificarCartaLiquidacion_SelectInformacionClientes.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public NotificarCartaLiquidacionService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource;
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.FWD.NotificarCartaLiquidacion"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);

            Map<String, Object> p = new HashMap<>();
            p.put("IdEmpresa", in.p_IdEmpresa);
            p.put("Fecha", in.p_Fecha);

            var jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));
            var pIns = new MapSqlParameterSource(p).addValue("IdUsuario", in.p_IdUsuario);
            jdbc.update(SQL_DEL_TABLA, pIns);
            jdbc.update(SQL_INS_TABLA, pIns);

            Salida out = new Salida();
            out.P_OUT_CARTA = JavaCallInfra.query(conn, SQL_SEL_INFO, p, JavaCallInfra.fieldMapper(CartaInfo.class));
            out.P_OUT_CLIENTES = JavaCallInfra.query(conn, SQL_SEL_CLIENTES, p, JavaCallInfra.fieldMapper(Cliente.class));

            plataforma.callAgregarLog(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "INFO",
                    "NotificarCartaLiquidacion fecha=" + in.p_Fecha);
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("NotificarCartaLiquidacion failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo; public String p_Fecha;
    }
    public static class CartaInfo { public Long IdOperacion; public BigDecimal MontoLiquidar; public String FechaLiquidacion; public String NombreCliente; }
    public static class Cliente { public Long IdSocioNegocio; public String Email; public String NombreCliente; }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<CartaInfo> P_OUT_CARTA = new ArrayList<>();
        public List<Cliente> P_OUT_CLIENTES = new ArrayList<>();
    }
}
