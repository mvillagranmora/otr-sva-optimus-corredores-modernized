package cl.bcs.mesa.javacall.mrf;

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
public class ConsultarResumenPosicionMRFService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarResumenPosicionMRFService.class);
    private static final String SQL_POSICION = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/ConsultarPosicionMRF_SelectPosicionMRF.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    public ConsultarResumenPosicionMRFService(DataSource dataSource) { this.dataSource = dataSource; }

    @Override public String fqn() { return "cl.bcs.mesa.MRF.ConsultarResumenPosicionMRF"; }

    @Override
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);
            Map<String, Object> p = new HashMap<>();
            p.put("IdEmpresa", in.p_IdEmpresa);
            p.put("FechaPosicion", in.p_FechaPosicion);
            p.put("IdModulo", in.p_IdModulo);
            p.put("IdMetodo", 0L);
            p.put("NombreMetodo", "");
            p.put("IdPais", in.p_IdPais);
            p.put("TipoPosicion", in.p_TipoPosicion);
            p.put("IdUsuario", in.p_IdUsuario);

            Salida out = new Salida();
            out.P_OUT_POSICION = JavaCallInfra.query(conn, SQL_POSICION, p, JavaCallInfra.fieldMapper(ResumenPosicion.class));
            out.P_OUT_POSICIONFINAL = out.P_OUT_POSICION.stream()
                    .map(po -> po.Haber != null && po.Debe != null ? po.Haber.subtract(po.Debe) : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("ConsultarResumenPosicionMRF failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public Long p_IdPais;
        public String p_IdModulo; public String p_FechaPosicion; public String p_TipoPosicion;
    }
    public static class ResumenPosicion {
        public BigDecimal Debe; public BigDecimal Haber; public Long IdMesa; public String NomMesa;
        public String IdTipoEvento; public String IdTipoProducto;
    }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public BigDecimal P_OUT_POSICIONFINAL = BigDecimal.ZERO;
        public List<ResumenPosicion> P_OUT_POSICION = new ArrayList<>();
    }
}
