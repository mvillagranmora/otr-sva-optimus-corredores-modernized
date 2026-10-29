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
public class ModificarOperacionSpotService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ModificarOperacionSpotService.class);
    private static final String SQL_SEL_OPS = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ModificarOperacionSpot_ConsultarOperacionesSpot.sql");
    private static final String SQL_SEL_PORT = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ModificarOperacionSpot_ConsultarPortafoliosFlujoCaja.sql");
    private static final String SQL_UPD = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ModificarOperacionSpot_UpdateOperacionesSpotOriginales.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public ModificarOperacionSpotService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource;
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.SPT.ModificarOperacionSpot"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);

            Map<String, Object> p = new HashMap<>();
            p.put("IdEmpresa", in.p_IdEmpresa);
            p.put("IdOperacion", in.p_IdOperacion);

            Salida out = new Salida();
            out.P_OUT_OPERACIONES = JavaCallInfra.query(conn, SQL_SEL_OPS, p, JavaCallInfra.fieldMapper(OperacionSpot.class));
            out.P_OUT_PORTAFOLIOS = JavaCallInfra.query(conn, SQL_SEL_PORT, p, JavaCallInfra.fieldMapper(Portafolio.class));

            var jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));
            var pUpd = new MapSqlParameterSource(p)
                    .addValue("IdUsuario", in.p_IdUsuario)
                    .addValue("MontoCompra", in.p_MontoCompra)
                    .addValue("MontoVenta", in.p_MontoVenta)
                    .addValue("IdPortafolio", in.p_IdPortafolio);
            jdbc.update(SQL_UPD, pUpd);

            plataforma.callAgregarLog(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "INFO",
                    "ModificarOperacionSpot op=" + in.p_IdOperacion);
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("ModificarOperacionSpot failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public Long p_IdOperacion; public BigDecimal p_MontoCompra; public BigDecimal p_MontoVenta;
        public String p_IdPortafolio;
    }
    public static class OperacionSpot { public Long IdOperacion; public String IdEstado; public BigDecimal MontoCompra; public BigDecimal MontoVenta; }
    public static class Portafolio { public String IdPortafolio; public String Descripcion; }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<OperacionSpot> P_OUT_OPERACIONES = new ArrayList<>();
        public List<Portafolio> P_OUT_PORTAFOLIOS = new ArrayList<>();
    }
}
