package cl.bcs.mesa.javacall.mes;

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
public class CrearModificarFormaPagoMesaService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(CrearModificarFormaPagoMesaService.class);
    private static final String SQL_SELECT = JavaCallInfra.loadSql("cl/bcs/mesa/sql/CrearModificarFormaPagoMesa_SelFormaPagoMesa.sql");
    private static final String SQL_INSERT = JavaCallInfra.loadSql("cl/bcs/mesa/sql/CrearModificarFormaPagoMesa_InsertFormaPagoMesa.sql");
    private static final String SQL_UPDATE = JavaCallInfra.loadSql("cl/bcs/mesa/sql/CrearModificarFormaPagoMesa_UpdateFormaPagoMesa.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public CrearModificarFormaPagoMesaService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource; this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.MES.CrearModificarFormaPagoMesa"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);
            var jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));

            // Check if exists
            Map<String, Object> sp = new HashMap<>();
            sp.put("IdEmpresa", in.p_IdEmpresa);
            sp.put("IdMesa", in.p_IdMesa);
            sp.put("IdMedioDePago", in.p_IdMedioDePago);
            List<FormaPago> existentes = JavaCallInfra.query(conn, SQL_SELECT, sp, JavaCallInfra.fieldMapper(FormaPago.class));

            var p = new MapSqlParameterSource()
                    .addValue("IdEmpresa", in.p_IdEmpresa)
                    .addValue("IdMesa", in.p_IdMesa)
                    .addValue("IdMedioDePago", in.p_IdMedioDePago)
                    .addValue("IdEstado", in.p_IdEstado)
                    .addValue("IdUsuario", in.p_IdUsuario);

            if (existentes.isEmpty()) {
                jdbc.update(SQL_INSERT, p);
            } else {
                jdbc.update(SQL_UPDATE, p);
            }

            Salida out = new Salida();
            out.P_OUT_FORMAS_PAGO = JavaCallInfra.query(conn, SQL_SELECT, sp, JavaCallInfra.fieldMapper(FormaPago.class));
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("CrearModificarFormaPagoMesa failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public Long p_IdMesa; public String p_IdMedioDePago; public String p_IdEstado;
    }
    public static class FormaPago {
        public Long IdMesa; public String IdMedioDePago; public String NombreMedioDePago; public String IdEstado;
    }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<FormaPago> P_OUT_FORMAS_PAGO = new ArrayList<>();
    }
}
