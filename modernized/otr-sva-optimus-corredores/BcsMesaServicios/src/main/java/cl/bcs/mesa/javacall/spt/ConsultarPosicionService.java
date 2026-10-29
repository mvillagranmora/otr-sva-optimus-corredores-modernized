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
public class ConsultarPosicionService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarPosicionService.class);
    private static final String SQL_MOV_SPOT = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ConsultarPosicion_SelectMovimientosSpot.sql");
    private static final String SQL_MOV_FWD = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ConsultarPosicion_SelectMovimientosForward.sql");
    private static final String SQL_MOV_FUT_FWD = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ConsultarPosicion_SelectMovimientosFuturosForward.sql");
    private static final String SQL_MOV_VEN_FWD = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ConsultarPosicion_SelectMovimientosVencidosForward.sql");
    private static final String SQL_PARIDADES = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ConsultarPosicion_SelectParidades.sql");
    private static final String SQL_POS_INICIAL = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ConsultarPosicion_SelectPosicionInicial.sql");
    private static final String SQL_POS_INICIAL_MONEDA = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ConsultarPosicion_SelectPosicionInicialMoneda.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    public ConsultarPosicionService(DataSource dataSource) { this.dataSource = dataSource; }

    @Override public String fqn() { return "cl.bcs.mesa.SPT.ConsultarPosicion"; }

    @Override
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);
            Map<String, Object> p = new HashMap<>();
            p.put("IdEmpresa", in.p_IdEmpresa);
            p.put("Fecha", in.p_Fecha);
            p.put("IdPortafolio", in.p_IdPortafolio);

            Salida out = new Salida();
            out.P_OUT_MOV_SPOT = JavaCallInfra.query(conn, SQL_MOV_SPOT, p, JavaCallInfra.fieldMapper(Movimiento.class));
            out.P_OUT_MOV_FWD = JavaCallInfra.query(conn, SQL_MOV_FWD, p, JavaCallInfra.fieldMapper(Movimiento.class));
            out.P_OUT_MOV_FUT_FWD = JavaCallInfra.query(conn, SQL_MOV_FUT_FWD, p, JavaCallInfra.fieldMapper(Movimiento.class));
            out.P_OUT_MOV_VEN_FWD = JavaCallInfra.query(conn, SQL_MOV_VEN_FWD, p, JavaCallInfra.fieldMapper(Movimiento.class));
            out.P_OUT_PARIDADES = JavaCallInfra.query(conn, SQL_PARIDADES, p, JavaCallInfra.fieldMapper(Paridad.class));
            out.P_OUT_POS_INICIAL = JavaCallInfra.query(conn, SQL_POS_INICIAL, p, JavaCallInfra.fieldMapper(Posicion.class));
            out.P_OUT_POS_INICIAL_MONEDA = JavaCallInfra.query(conn, SQL_POS_INICIAL_MONEDA, p, JavaCallInfra.fieldMapper(Posicion.class));
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("ConsultarPosicion failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public String p_Fecha; public Long p_IdPortafolio;
    }
    public static class Movimiento { public Long IdOperacion; public BigDecimal Monto; public String IdMoneda; public String Tipo; }
    public static class Paridad { public String IdMoneda; public BigDecimal Valor; }
    public static class Posicion { public String IdMoneda; public BigDecimal Saldo; }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Movimiento> P_OUT_MOV_SPOT = new ArrayList<>();
        public List<Movimiento> P_OUT_MOV_FWD = new ArrayList<>();
        public List<Movimiento> P_OUT_MOV_FUT_FWD = new ArrayList<>();
        public List<Movimiento> P_OUT_MOV_VEN_FWD = new ArrayList<>();
        public List<Paridad> P_OUT_PARIDADES = new ArrayList<>();
        public List<Posicion> P_OUT_POS_INICIAL = new ArrayList<>();
        public List<Posicion> P_OUT_POS_INICIAL_MONEDA = new ArrayList<>();
    }
}
