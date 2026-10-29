package cl.bcs.mesa.javacall.spt;

import cl.bcs.mesa.javacall.JavaCallInfra;
import cl.bcs.mesa.javacall.JavaCallService;
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
public class ConsultarCalculadoraSpotService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarCalculadoraSpotService.class);
    private static final String SQL_SEL_RESUMEN = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ConsultarCalculadoraSpot_ResumenSpot.sql");
    private static final String SQL_SEL_MESAS = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ConsultarCalculadoraSpot_SelectMesas.sql");
    private static final String SQL_SEL_TIPO_INS = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ConsultarCalculadoraSpot_SelectTipoInstrumento.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    public ConsultarCalculadoraSpotService(DataSource dataSource) { this.dataSource = dataSource; }

    @Override public String fqn() { return "cl.bcs.mesa.SPT.ConsultarCalculadoraSpot"; }

    @Override
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);
            Map<String, Object> p = new HashMap<>();
            p.put("IdEmpresa", in.p_IdEmpresa);
            p.put("Fecha", in.p_Fecha);
            p.put("IdMoneda", in.p_IdMoneda);

            Salida out = new Salida();
            out.P_OUT_RESUMEN = JavaCallInfra.query(conn, SQL_SEL_RESUMEN, p, JavaCallInfra.fieldMapper(Resumen.class));
            out.P_OUT_MESAS = JavaCallInfra.query(conn, SQL_SEL_MESAS, p, JavaCallInfra.fieldMapper(Mesa.class));
            out.P_OUT_TIPO_INS = JavaCallInfra.query(conn, SQL_SEL_TIPO_INS, p, JavaCallInfra.fieldMapper(TipoInstrumento.class));
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("ConsultarCalculadoraSpot failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public String p_Fecha; public String p_IdMoneda;
    }
    public static class Resumen { public BigDecimal TotalCompra; public BigDecimal TotalVenta; public BigDecimal Diferencia; }
    public static class Mesa { public Long IdMesa; public String Descripcion; public BigDecimal Saldo; }
    public static class TipoInstrumento { public String IdTipoInstrumento; public String Descripcion; }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Resumen> P_OUT_RESUMEN = new ArrayList<>();
        public List<Mesa> P_OUT_MESAS = new ArrayList<>();
        public List<TipoInstrumento> P_OUT_TIPO_INS = new ArrayList<>();
    }
}
