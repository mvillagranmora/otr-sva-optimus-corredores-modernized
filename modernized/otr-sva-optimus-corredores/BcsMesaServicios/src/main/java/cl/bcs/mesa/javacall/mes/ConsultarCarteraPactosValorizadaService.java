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
public class ConsultarCarteraPactosValorizadaService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarCarteraPactosValorizadaService.class);
    private static final String SQL_CLIENTE_MESA = JavaCallInfra.loadSql("cl/bcs/mesa/MES/sql/ConsultarCarteraPactosValorizada_SelectClienteMesa.sql");
    private static final String SQL_PORTAFOLIOS = JavaCallInfra.loadSql("cl/bcs/mesa/MES/sql/ConsultarCarteraPactosValorizada_SelectPortafoliosMesa.sql");
    private static final String SQL_COMPRAS = JavaCallInfra.loadSql("cl/bcs/mesa/MES/sql/ConsultarCarteraPactosValorizada_SelectComprasPacto.sql");
    private static final String SQL_VENTAS = JavaCallInfra.loadSql("cl/bcs/mesa/MES/sql/ConsultarCarteraPactosValorizada_SelectVentasPacto.sql");
    private static final String SQL_RESERVA = JavaCallInfra.loadSql("cl/bcs/mesa/MES/sql/ConsultarCarteraPactosValorizada_SelectReserva.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    public ConsultarCarteraPactosValorizadaService(DataSource dataSource) { this.dataSource = dataSource; }

    @Override public String fqn() { return "cl.bcs.mesa.MES.ConsultarCarteraPactosValorizada"; }

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
            out.P_OUT_CLIENTES = JavaCallInfra.query(conn, SQL_CLIENTE_MESA, p, JavaCallInfra.fieldMapper(ClienteMesa.class));
            out.P_OUT_PORTAFOLIOS = JavaCallInfra.query(conn, SQL_PORTAFOLIOS, p, JavaCallInfra.fieldMapper(PortafolioMesa.class));
            out.P_OUT_COMPRAS = JavaCallInfra.query(conn, SQL_COMPRAS, p, JavaCallInfra.fieldMapper(Operacion.class));
            out.P_OUT_VENTAS = JavaCallInfra.query(conn, SQL_VENTAS, p, JavaCallInfra.fieldMapper(Operacion.class));
            out.P_OUT_RESERVAS = JavaCallInfra.query(conn, SQL_RESERVA, p, JavaCallInfra.fieldMapper(Reserva.class));
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("ConsultarCarteraPactosValorizada failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public Long p_IdCliente; public String p_FechaPosicion; public String p_IdTipoProducto;
    }
    public static class ClienteMesa { public Long IdCliente; public String NombreCliente; public String RutCliente; }
    public static class PortafolioMesa { public Long IdPortafolio; public String NombrePortafolio; }
    public static class Operacion {
        public Long IdOperacion; public Long IdCartera; public String IdTipoProducto;
        public String IdTipoOperacion; public BigDecimal MontoMonedaPrincipal;
        public String MonedaPrincipal; public String FechaVencimiento; public String IdEstado;
    }
    public static class Reserva { public Long IdCartera; public BigDecimal MontoReserva; public String IdMoneda; }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<ClienteMesa> P_OUT_CLIENTES = new ArrayList<>();
        public List<PortafolioMesa> P_OUT_PORTAFOLIOS = new ArrayList<>();
        public List<Operacion> P_OUT_COMPRAS = new ArrayList<>();
        public List<Operacion> P_OUT_VENTAS = new ArrayList<>();
        public List<Reserva> P_OUT_RESERVAS = new ArrayList<>();
    }
}
