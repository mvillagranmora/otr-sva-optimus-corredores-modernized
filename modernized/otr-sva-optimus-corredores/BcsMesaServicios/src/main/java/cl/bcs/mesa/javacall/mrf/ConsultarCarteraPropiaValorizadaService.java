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
public class ConsultarCarteraPropiaValorizadaService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarCarteraPropiaValorizadaService.class);
    private static final String SQL_PORTAFOLIOS = JavaCallInfra.loadSql("cl/bcs/mesa/MES/sql/ConsultarCarteraPropiaValorizada_SelectPortafoliosCuentaPropia.sql");
    private static final String SQL_CARTERA_LIBRE = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/ConsultarCarteraPropiaValorizada_SelectCarteraLibre.sql");
    private static final String SQL_CARTERA_GARANTIA = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/ConsultarCarteraPropiaValorizada_SelectCarteraGarantia.sql");
    private static final String SQL_CARTERA_VALORIZADA = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/ConsultarCarteraPropiaValorizada_SelectCarteraValorizada.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    public ConsultarCarteraPropiaValorizadaService(DataSource dataSource) { this.dataSource = dataSource; }

    @Override public String fqn() { return "cl.bcs.mesa.MRF.ConsultarCarteraPropiaValorizada"; }

    @Override
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);
            Map<String, Object> p = new HashMap<>();
            p.put("IdEmpresa", in.p_IdEmpresa);
            p.put("IdCliente", in.p_IdCliente);
            p.put("FechaPosicion", in.p_FechaPosicion);
            p.put("IdTipoProducto", in.p_IdTipoProducto);

            Salida out = new Salida();
            out.P_OUT_PORTAFOLIOS = JavaCallInfra.query(conn, SQL_PORTAFOLIOS, p, JavaCallInfra.fieldMapper(Portafolio.class));
            out.P_OUT_CARTERA_LIBRE = JavaCallInfra.query(conn, SQL_CARTERA_LIBRE, p, JavaCallInfra.fieldMapper(Cartera.class));
            out.P_OUT_CARTERA_GARANTIA = JavaCallInfra.query(conn, SQL_CARTERA_GARANTIA, p, JavaCallInfra.fieldMapper(Cartera.class));
            out.P_OUT_CARTERA_VALORIZADA = JavaCallInfra.query(conn, SQL_CARTERA_VALORIZADA, p, JavaCallInfra.fieldMapper(Cartera.class));
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("ConsultarCarteraPropiaValorizada failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public Long p_IdCliente; public String p_FechaPosicion; public String p_IdTipoProducto;
    }
    public static class Portafolio { public Long IdPortafolio; public String NombrePortafolio; public Long IdCliente; }
    public static class Cartera {
        public Long IdCartera; public String IdInstrumento; public BigDecimal Cantidad;
        public BigDecimal Valorizado; public String IdMoneda;
    }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Portafolio> P_OUT_PORTAFOLIOS = new ArrayList<>();
        public List<Cartera> P_OUT_CARTERA_LIBRE = new ArrayList<>();
        public List<Cartera> P_OUT_CARTERA_GARANTIA = new ArrayList<>();
        public List<Cartera> P_OUT_CARTERA_VALORIZADA = new ArrayList<>();
    }
}
