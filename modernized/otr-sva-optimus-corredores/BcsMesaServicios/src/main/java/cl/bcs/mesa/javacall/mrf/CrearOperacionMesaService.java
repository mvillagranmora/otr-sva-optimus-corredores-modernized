package cl.bcs.mesa.javacall.mrf;

import cl.bcs.mesa.javacall.JavaCallInfra;
import cl.bcs.mesa.javacall.JavaCallService;
import cl.bcs.mesa.javacall.PlataformaBridge;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CrearOperacionMesaService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(CrearOperacionMesaService.class);
    private static final String SQL_SEL_CLIENTE = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/CrearOperacionMesaRF_SelectCliente.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public CrearOperacionMesaService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource;
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.MRF.CrearOperacionMesa"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);

            JsonObject reqResult = plataforma.callRevisarRequisitosBasicos(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
            if (reqResult.has("P_OUT_ERRORESCONTADOR") && reqResult.get("P_OUT_ERRORESCONTADOR").getAsLong() > 0) {
                return reqResult;
            }

            Map<String, Object> p = new HashMap<>();
            p.put("IdEmpresa", in.p_IdEmpresa);
            p.put("IdSocioNegocio", in.p_IdSocioNegocio);

            Salida out = new Salida();
            out.P_OUT_CLIENTE = JavaCallInfra.query(conn, SQL_SEL_CLIENTE, p, JavaCallInfra.fieldMapper(Cliente.class));

            // Delegate complex operation creation to plataforma
            JsonObject opParams = new JsonObject();
            opParams.addProperty("idEmpresa", in.p_IdEmpresa);
            opParams.addProperty("idUsuario", in.p_IdUsuario);
            opParams.addProperty("idModulo", in.p_IdModulo);
            opParams.addProperty("tipoProducto", in.p_IdTipoProducto);
            JsonObject opResult = plataforma.call("cl.bcs.plataforma.CrearOperacionMesa", opParams);
            if (opResult.has("P_OUT_IDOPERACION")) {
                out.P_OUT_IDOPERACION = opResult.get("P_OUT_IDOPERACION").getAsLong();
            }

            plataforma.callAgregarLog(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "INFO",
                    "CrearOperacionMesa ejecutado tipo=" + in.p_IdTipoProducto);
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("CrearOperacionMesa failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public Long p_IdSocioNegocio; public String p_IdTipoProducto; public Long p_IdPortafolio;
        public String p_IdInstrumento; public String p_FechaEmision; public String p_FechaVencimiento;
    }
    public static class Cliente { public Long IdSocioNegocio; public String RazonSocial; public String Rut; }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public Long P_OUT_IDOPERACION;
        public List<Cliente> P_OUT_CLIENTE = new ArrayList<>();
    }
}
