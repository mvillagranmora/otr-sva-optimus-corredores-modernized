package cl.bcs.mesa.javacall.mrf;

import cl.bcs.mesa.javacall.JavaCallInfra;
import cl.bcs.mesa.javacall.JavaCallService;
import cl.bcs.mesa.javacall.PlataformaBridge;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GenerarInformeUtilidadPerdidaService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(GenerarInformeUtilidadPerdidaService.class);
    private static final String SQL_INFORME = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/GenerarInformeUtilidadPerdida_ConsultarInformeOperacionesMRF.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public GenerarInformeUtilidadPerdidaService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource;
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.MRF.GenerarInformeUtilidadPerdida"; }

    @Override
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);

            JsonObject reqResult = plataforma.callRevisarRequisitosBasicos(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
            if (reqResult.has("P_OUT_ERRORESCONTADOR") && reqResult.get("P_OUT_ERRORESCONTADOR").getAsLong() > 0) {
                return reqResult;
            }

            Map<String, Object> p = new HashMap<>();
            p.put("IdEmpresa", in.p_IdEmpresa);
            p.put("FechaInicio", in.p_FechaInicio);
            p.put("FechaTermino", in.p_FechaTermino);
            p.put("IdPortafolio", in.p_IdPortafolio);

            Salida out = new Salida();
            out.P_OUT_INFORME = JavaCallInfra.query(conn, SQL_INFORME, p, JavaCallInfra.fieldMapper(InformeRow.class));

            plataforma.callAgregarLog(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "INFO",
                    "GenerarInformeUtilidadPerdida: " + in.p_FechaInicio + " - " + in.p_FechaTermino);
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("GenerarInformeUtilidadPerdida failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public String p_FechaInicio; public String p_FechaTermino; public Long p_IdPortafolio;
    }
    public static class InformeRow {
        public Long IdOperacion; public String IdInstrumento; public String FechaEmision;
        public String FechaTermino; public BigDecimal MontoCompra; public BigDecimal MontoVenta;
        public BigDecimal Utilidad; public BigDecimal Perdida; public BigDecimal TasaVenta;
    }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<InformeRow> P_OUT_INFORME = new ArrayList<>();
    }
}
