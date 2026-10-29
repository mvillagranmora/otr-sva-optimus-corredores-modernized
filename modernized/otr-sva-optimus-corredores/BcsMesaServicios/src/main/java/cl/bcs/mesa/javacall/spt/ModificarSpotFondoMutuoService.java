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
public class ModificarSpotFondoMutuoService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ModificarSpotFondoMutuoService.class);
    private static final String SQL_SEL_FORMAS = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ModificarSpotFondoMutuo_SelectFomasPagoFondoMutuo.sql");
    private static final String SQL_SEL_OPS = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ModificarSpotFondoMutuo_SelectOperacionesSpotFondoMutuo.sql");
    private static final String SQL_UPD = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ModificarSpotFondoMutuo_UpdateOperacionesSpotFondoMutuo.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public ModificarSpotFondoMutuoService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource;
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.SPT.ModificarSpotFondoMutuo"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);

            Map<String, Object> p = new HashMap<>();
            p.put("IdEmpresa", in.p_IdEmpresa);
            p.put("IdOperacion", in.p_IdOperacion);

            Salida out = new Salida();
            out.P_OUT_FORMAS_PAGO = JavaCallInfra.query(conn, SQL_SEL_FORMAS, p, JavaCallInfra.fieldMapper(FormaPago.class));
            out.P_OUT_OPERACIONES = JavaCallInfra.query(conn, SQL_SEL_OPS, p, JavaCallInfra.fieldMapper(OperacionFM.class));

            var jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));
            var pUpd = new MapSqlParameterSource(p)
                    .addValue("IdUsuario", in.p_IdUsuario)
                    .addValue("IdFormaPago", in.p_IdFormaPago)
                    .addValue("Cuotas", in.p_Cuotas)
                    .addValue("ValorCuota", in.p_ValorCuota);
            jdbc.update(SQL_UPD, pUpd);

            plataforma.callAgregarLog(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "INFO",
                    "ModificarSpotFondoMutuo op=" + in.p_IdOperacion);
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("ModificarSpotFondoMutuo failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public Long p_IdOperacion; public String p_IdFormaPago; public Integer p_Cuotas; public BigDecimal p_ValorCuota;
    }
    public static class FormaPago { public String IdFormaPago; public String Descripcion; }
    public static class OperacionFM { public Long IdOperacion; public BigDecimal Cuotas; public BigDecimal ValorCuota; public String IdFondo; }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<FormaPago> P_OUT_FORMAS_PAGO = new ArrayList<>();
        public List<OperacionFM> P_OUT_OPERACIONES = new ArrayList<>();
    }
}
