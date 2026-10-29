package cl.bcs.mesa.javacall.spt.ws;

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
public class WSIngresarOperacionSpotService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(WSIngresarOperacionSpotService.class);
    private static final String SQL_SEL_EJE = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/WSIngresarOperacionSpot_ConsultarEjecutivoDigitador.sql");
    private static final String SQL_SEL_EST = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/WSIngresarOperacionSpot_ConsultarEstadoMesa.sql");
    private static final String SQL_SEL_PLA = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/WSIngresarOperacionSpot_ConsultarPlataformas.sql");
    private static final String SQL_SEL_INS = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/WSIngresarOperacionSpot_SelecInstrumento.sql");
    private static final String SQL_SEL_CAN = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/WSIngresarOperacionSpot_SelectCanales.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public WSIngresarOperacionSpotService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource;
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.SPT.WSingresarOperacionSpot"; }

    @Override
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);
            Map<String, Object> p = new HashMap<>();
            p.put("IdEmpresa", in.p_IdEmpresa);
            p.put("IdUsuario", in.p_IdUsuario);

            Salida out = new Salida();
            out.P_OUT_EJECUTIVOS = JavaCallInfra.query(conn, SQL_SEL_EJE, p, JavaCallInfra.fieldMapper(Ejecutivo.class));
            out.P_OUT_ESTADO_MESA = JavaCallInfra.query(conn, SQL_SEL_EST, p, JavaCallInfra.fieldMapper(EstadoMesa.class));
            out.P_OUT_PLATAFORMAS = JavaCallInfra.query(conn, SQL_SEL_PLA, p, JavaCallInfra.fieldMapper(Plataforma.class));
            out.P_OUT_INSTRUMENTOS = JavaCallInfra.query(conn, SQL_SEL_INS, p, JavaCallInfra.fieldMapper(Instrumento.class));
            out.P_OUT_CANALES = JavaCallInfra.query(conn, SQL_SEL_CAN, p, JavaCallInfra.fieldMapper(Canal.class));

            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("WSIngresarOperacionSpot failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
    }
    public static class Ejecutivo { public Long IdUsuario; public String Nombre; public String IdEjecutivo; }
    public static class EstadoMesa { public String IdEstado; public String Descripcion; public Boolean MesaAbierta; }
    public static class Plataforma { public String IdPlataforma; public String Descripcion; }
    public static class Instrumento { public String IdInstrumento; public String Descripcion; public String IdMoneda; }
    public static class Canal { public String IdCanal; public String Descripcion; }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Ejecutivo> P_OUT_EJECUTIVOS = new ArrayList<>();
        public List<EstadoMesa> P_OUT_ESTADO_MESA = new ArrayList<>();
        public List<Plataforma> P_OUT_PLATAFORMAS = new ArrayList<>();
        public List<Instrumento> P_OUT_INSTRUMENTOS = new ArrayList<>();
        public List<Canal> P_OUT_CANALES = new ArrayList<>();
    }
}
