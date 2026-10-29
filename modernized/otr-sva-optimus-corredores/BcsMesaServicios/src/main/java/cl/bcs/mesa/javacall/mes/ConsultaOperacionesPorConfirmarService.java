package cl.bcs.mesa.javacall.mes;

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
public class ConsultaOperacionesPorConfirmarService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ConsultaOperacionesPorConfirmarService.class);
    private static final String SQL_FWD = JavaCallInfra.loadSql("cl/bcs/mesa/MES/sql/ConsultaOperacionesPorConfirmar_SelectOperacionesForward.sql");
    private static final String SQL_RF  = JavaCallInfra.loadSql("cl/bcs/mesa/MES/sql/ConsultaOperacionesPorConfirmar_SelectOperacionesRF.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;

    public ConsultaOperacionesPorConfirmarService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override public String fqn() { return "cl.bcs.mesa.MES.ConsultaOperacionesPorConfirmar"; }

    @Override
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);
            Salida out = new Salida();
            Map<String, Object> p = new HashMap<>();
            p.put("IdEmpresa", in.p_IdEmpresa);
            p.put("IdTipoProducto", in.p_IdTipoProducto);
            p.put("IdTipoOperacion", in.p_IdTipoOperacion);
            p.put("IdMonedaPrincipal", in.p_IdMonedaPrincipal);
            p.put("IdMonedaSecundaria", in.p_IdMonedaSecundaria);
            p.put("EstadoConfirmacion", in.p_EstadoConfirmacion);
            p.put("IdCliente", in.p_IdCliente);
            p.put("FechaOperacion", in.p_Fecha);
            String sql = "MEFW".equals(in.p_IdTipoProducto) ? SQL_FWD : SQL_RF;
            out.P_OUT_OPERACIONES = JavaCallInfra.query(conn, sql, p, JavaCallInfra.fieldMapper(Operacion.class));
            out.P_OUT_ERRORESCONTADOR = 0L;
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("ConsultaOperacionesPorConfirmar failed", e);
            return error(e.getMessage());
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public String p_IdTipoProducto; public String p_IdTipoOperacion;
        public String p_IdMonedaPrincipal; public String p_IdMonedaSecundaria;
        public String p_EstadoConfirmacion; public Long p_IdCliente; public String p_Fecha;
    }
    public static class Operacion {
        public Long IdOperacion; public Long IdCartera; public Long FolioCartera;
        public String IdTipoInstrumento; public Long FolioOperacion; public String EstadoConfirmacion;
        public String TipoOperacion; public String NombreInstrumento; public Long IdCliente;
        public String RutCliente; public String NombreCliente; public String Portafolio;
        public String MonedaPrincipal; public String MonedaSecundaria;
        public BigDecimal MontoMonedaPrincipal; public BigDecimal TCCierre; public BigDecimal TCCosto;
        public BigDecimal ParidadCierre; public BigDecimal MontoMonedaSecundaria;
        public String FechaEntregamos; public String FechaRecibimos; public String EstadoOperacion;
        public String Canal; public String FechaOperacion; public String FechaLiquidacion;
        public String IdTipoReajuste; public BigDecimal Tasa; public Long PlazoEnDias;
        public BigDecimal MontoOperacion; public BigDecimal MontoOperacionMoneda;
        public String FechaVencimiento; public Long IdPortafolioCliente; public Long IdPortafolioCorredor;
        public Long IdClienteCorredor; public Long IdMesa;
    }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Operacion> P_OUT_OPERACIONES = new ArrayList<>();
    }

    private static JsonObject error(String msg) {
        JsonObject e = new JsonObject(); e.addProperty("P_OUT_ERRORESCONTADOR", 1L); e.addProperty("error", msg); return e;
    }
}
