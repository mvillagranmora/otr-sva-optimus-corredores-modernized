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
public class ConsultarPosicionMRFService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarPosicionMRFService.class);
    private static final String SQL_POSICION = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/ConsultarPosicionMRF_SelectPosicionMRF.sql");
    private static final String SQL_POSICION_INICIAL = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/ConsultarPosicionMRF_SelectPosicionMRFInicial.sql");
    private static final String SQL_OPERACIONES = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/ConsultarPosicionMRF_SelectOperacionesRFFechaPosicion.sql");
    private static final String SQL_EVENTOS_MANUALES = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/ConsultarPosicionMRF_SelectEventosManualesRFFechaPosicion.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    public ConsultarPosicionMRFService(DataSource dataSource) { this.dataSource = dataSource; }

    @Override public String fqn() { return "cl.bcs.mesa.MRF.ConsultarPosicionMRF"; }

    @Override
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);
            Map<String, Object> p = new HashMap<>();
            p.put("IdEmpresa", in.p_IdEmpresa);
            p.put("IdUsuario", in.p_IdUsuario);
            p.put("IdPais", in.p_IdPais);
            p.put("IdMetodo", in.p_IdMetodo);
            p.put("NombreMetodo", in.p_NombreMetodo);
            p.put("IdModulo", in.p_IdModulo);
            p.put("FechaPosicion", in.p_FechaPosicion);
            p.put("TipoPosicion", in.p_TipoPosicion);

            Salida out = new Salida();
            out.P_OUT_POSICION = JavaCallInfra.query(conn, SQL_POSICION, p, JavaCallInfra.fieldMapper(PosicionMRF.class));
            out.P_OUT_OPERACIONES = JavaCallInfra.query(conn, SQL_OPERACIONES, p, JavaCallInfra.fieldMapper(OperacionMRF.class));
            out.P_OUT_EVENTOS_MANUALES = JavaCallInfra.query(conn, SQL_EVENTOS_MANUALES, p, JavaCallInfra.fieldMapper(EventoManual.class));
            // Calculate posicion final
            out.P_OUT_POSICIONFINAL = out.P_OUT_POSICION.stream()
                    .map(po -> po.Haber != null && po.Debe != null ? po.Haber.subtract(po.Debe) : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("ConsultarPosicionMRF failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public Long p_IdPais;
        public Long p_IdMetodo; public String p_NombreMetodo; public String p_IdModulo;
        public String p_FechaPosicion; public String p_TipoPosicion;
    }
    public static class PosicionMRF {
        public BigDecimal Debe; public BigDecimal Haber; public Long IdEvento; public Long IdMesa;
        public Long IdMetodo; public String IdTipoEvento; public String IdTipoOperacion;
        public String IdTipoProducto; public String IdTipoReajuste; public String IndAccion;
        public String NomMesa; public String NombreAgrupacion; public String NombreEvento;
        public String NombreMetodo;
    }
    public static class OperacionMRF {
        public String IdTipoProducto; public String IdTipoOperacion; public String IdEstado;
        public Long IdPortafolio; public BigDecimal MtoEvento;
    }
    public static class EventoManual {
        public Long IdPosicion; public String Fecha; public Long IdMatriz; public BigDecimal MontoEvento;
    }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public Long P_OUT_IDMESA = 0L;
        public BigDecimal P_OUT_POSICIONFINAL = BigDecimal.ZERO;
        public String P_OUT_NOMBREMESA = "";
        public List<PosicionMRF> P_OUT_POSICION = new ArrayList<>();
        public List<OperacionMRF> P_OUT_OPERACIONES = new ArrayList<>();
        public List<EventoManual> P_OUT_EVENTOS_MANUALES = new ArrayList<>();
    }
}
