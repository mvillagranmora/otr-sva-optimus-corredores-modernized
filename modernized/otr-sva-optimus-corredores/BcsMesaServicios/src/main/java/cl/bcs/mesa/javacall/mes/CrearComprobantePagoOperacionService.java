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
public class CrearComprobantePagoOperacionService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(CrearComprobantePagoOperacionService.class);
    private static final String SQL_SELECT_DATOS_OP = JavaCallInfra.loadSql("cl/bcs/mesa/sql/CrearComprobantePagoOperacion_SelectDatosOperacion.sql");
    private static final String SQL_SELECT_MEDIOS_PAGO = JavaCallInfra.loadSql("cl/bcs/mesa/sql/CrearComprobantePagoOperacion_SelectMediosDePago.sql");
    private static final String SQL_SELECT_COMPROBANTE = JavaCallInfra.loadSql("cl/bcs/mesa/sql/CrearComprobantePagoOperacion_SelectComprobantePago.sql");
    private static final String SQL_DELETE_COMPROBANTE_OP = JavaCallInfra.loadSql("cl/bcs/mesa/sql/CrearComprobantePagoOperacion_DeleteComprobantePagoOperacion.sql");
    private static final String SQL_DELETE_COMPROBANTE = JavaCallInfra.loadSql("cl/bcs/mesa/sql/CrearComprobantePagoOperacion_DeleteComprobantePago.sql");
    private static final String SQL_INSERT = JavaCallInfra.loadSql("cl/bcs/mesa/sql/CrearComprobantePagoOperacion_InsertComprobantePago.sql");
    private static final String SQL_UPDATE = JavaCallInfra.loadSql("cl/bcs/mesa/sql/CrearComprobantePagoOperacion_UpdateComprobantePago.sql");
    private static final String SQL_SELECT_SALIDA = JavaCallInfra.loadSql("cl/bcs/mesa/sql/CrearComprobantePagoOperacion_SelectComprobantePagoSalida.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public CrearComprobantePagoOperacionService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource; this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.MES.CrearComprobantePagoOperacion"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);
            var jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));

            // Delete and recreate comprobante
            var p = new MapSqlParameterSource()
                    .addValue("IdEmpresa", in.p_IdEmpresa)
                    .addValue("IdOperacion", in.p_IdOperacion)
                    .addValue("IdUsuario", in.p_IdUsuario)
                    .addValue("IdModulo", in.p_IdModulo);

            jdbc.update(SQL_DELETE_COMPROBANTE_OP, p);
            jdbc.update(SQL_DELETE_COMPROBANTE, p);
            jdbc.update(SQL_INSERT, p);
            jdbc.update(SQL_UPDATE, p);

            // Select output
            Map<String, Object> sp = new HashMap<>();
            sp.put("IdEmpresa", in.p_IdEmpresa);
            sp.put("IdOperacion", in.p_IdOperacion);
            Salida out = new Salida();
            out.P_OUT_COMPROBANTES = JavaCallInfra.query(conn, SQL_SELECT_SALIDA, sp, JavaCallInfra.fieldMapper(Comprobante.class));
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("CrearComprobantePagoOperacion failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public Long p_IdOperacion;
        public List<ComprobantePagoIn> p_Comprobantes;
    }
    public static class ComprobantePagoIn {
        public Long IdComprobantePagoOperacion; public String IdMedioDePago;
        public String IdModalidadDePago; public BigDecimal Monto; public String NombreFormaDePago;
    }
    public static class Comprobante {
        public Long IdComprobantePagoOperacion; public Long IdOperacion;
        public String NombreFormaDePago; public BigDecimal Monto; public String IdEstado;
    }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Comprobante> P_OUT_COMPROBANTES = new ArrayList<>();
    }
}
