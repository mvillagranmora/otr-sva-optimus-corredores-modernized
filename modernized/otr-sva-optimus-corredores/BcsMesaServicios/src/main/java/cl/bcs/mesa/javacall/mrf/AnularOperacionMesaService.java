package cl.bcs.mesa.javacall.mrf;

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
public class AnularOperacionMesaService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(AnularOperacionMesaService.class);
    private static final String SQL_SELECT_OP = JavaCallInfra.loadSql("cl/bcs/mesa/sql/AnularOperacionMesa_SelectOperacion.sql");
    private static final String SQL_SELECT_CARTERA_RETIRO = JavaCallInfra.loadSql("cl/bcs/mesa/sql/AnularOperacionMesa_SelectCarteraRetiro.sql");
    private static final String SQL_SELECT_RESERVADA = JavaCallInfra.loadSql("cl/bcs/mesa/sql/AnularOperacionMesa_SelectCarteraReservada.sql");
    private static final String SQL_SELECT_COMPROMETIDA = JavaCallInfra.loadSql("cl/bcs/mesa/sql/AnularOperacionMesa_SelectCarteraComprometida.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public AnularOperacionMesaService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource; this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.MRF.AnularOperacionMesa"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);
            var jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));

            // Validate requisitos
            JsonObject reqResult = plataforma.callRevisarRequisitosBasicos(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
            if (reqResult.has("P_OUT_ERRORESCONTADOR") && reqResult.get("P_OUT_ERRORESCONTADOR").getAsLong() > 0) {
                return reqResult;
            }

            // Validate operation exists
            Map<String, Object> p = new HashMap<>();
            p.put("IdEmpresa", in.p_IdEmpresa);
            p.put("IdOperacion", in.p_IdOperacion);
            List<Operacion> operaciones = JavaCallInfra.query(conn, SQL_SELECT_OP, p, JavaCallInfra.fieldMapper(Operacion.class));

            if (operaciones.isEmpty()) {
                JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L);
                err.addProperty("error", "Operacion no encontrada: " + in.p_IdOperacion); return err;
            }

            Operacion op = operaciones.get(0);
            // Update state to ANU (Anulado)
            var up = new MapSqlParameterSource()
                    .addValue("IdEmpresa", in.p_IdEmpresa)
                    .addValue("IdOperacion", in.p_IdOperacion)
                    .addValue("IdEstado", "ANU")
                    .addValue("IdUsuario", in.p_IdUsuario)
                    .addValue("IdModulo", in.p_IdModulo);
            jdbc.update("UPDATE \"BSCL.Mesa.BD::MES.OperacionMesa\" SET \"IdEstado\" = :IdEstado, \"IdUsuarioModificacion\" = :IdUsuario WHERE \"IdEmpresa\" = :IdEmpresa AND \"IdOperacion\" = :IdOperacion", up);

            plataforma.callAgregarLog(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "INFO", "AnularOperacionMesa ejecutado op=" + in.p_IdOperacion);

            Salida out = new Salida();
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("AnularOperacionMesa failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public Long p_IdOperacion; public String p_MotivoAnulacion;
    }
    public static class Operacion {
        public Long IdOperacion; public String IdEstado; public String IdTipoProducto;
        public Long IdPortafolio; public BigDecimal Monto;
    }
    public static class Salida { public Long P_OUT_ERRORESCONTADOR = 0L; }
}
