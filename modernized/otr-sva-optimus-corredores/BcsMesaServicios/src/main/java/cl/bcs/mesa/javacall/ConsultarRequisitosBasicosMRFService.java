package cl.bcs.mesa.javacall;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cl.bcs.mesa.javacall.JavaCallInfra.*;

@Service
public class ConsultarRequisitosBasicosMRFService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarRequisitosBasicosMRFService.class);

    private static final String SQL_SELECT_PLANTILLA  =
            loadSql("cl/bcs/mesa/sql/select_validacion_plantilla.sql");
    private static final String SQL_SELECT_PORTAFOLIO =
            loadSql("cl/bcs/mesa/sql/select_validacion_portafolio.sql");

    private final DataSource dataSource;

    public ConsultarRequisitosBasicosMRFService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public String fqn() { return "cl.bcs.mesa.ConsultarRequisitosBasicosMRF"; }

    // ─── Input ───────────────────────────────────────────────────────────────

    public static class EntradaValidacion {
        public Long   p_IdEmpresa;
        public Long   p_IdUsuario;
        public String p_IdModulo;
        public String p_IdTipoOperacion;
        public Long   p_IdPortafolio;
        public Long   p_IdEmpleado;
        public String p_IdTipoProducto;
    }

    // ─── Output ──────────────────────────────────────────────────────────────

    public static class SalidaValidacion {
        public int            p_out_AdvertenciasContador = 0;
        public int            p_out_ErroresContador      = 0;
        public List<DetalleSalida> p_out_AdvertenciaDetalle = new ArrayList<>();
        public List<DetalleSalida> p_out_ErroresDetalle     = new ArrayList<>();
    }

    public static class DetalleSalida {
        public Long   IdInterno;
        public String IdOperacion;
        public String Codigo;
        public String RequiereAutorizacion;
        public String Descripcion;

        public DetalleSalida(Long idInterno, String idOperacion,
                             String codigo, String descripcion) {
            this.IdInterno   = idInterno;
            this.IdOperacion = idOperacion;
            this.Codigo      = codigo;
            this.Descripcion = descripcion;
        }
    }

    private static class Resultado {
        public String resultado;
    }

    // ─── Public API ──────────────────────────────────────────────────────────

    @Override
    public JsonObject execute(JsonObject params) {
        log.info("INICIO::ConsultarRequisitosBasicosMRF");
        try (Connection conn = dataSource.getConnection()) {
            Gson gson = new Gson();
            EntradaValidacion in = gson.fromJson(params.toString(), EntradaValidacion.class);
            SalidaValidacion  out = logica(conn, in);
            log.info("FIN::ConsultarRequisitosBasicosMRF errores={}", out.p_out_ErroresContador);
            return JsonParser.parseString(gson.toJson(out)).getAsJsonObject();
        } catch (Exception e) {
            log.error("ConsultarRequisitosBasicosMRF failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    // ─── Business logic ──────────────────────────────────────────────────────

    private SalidaValidacion logica(Connection conn, EntradaValidacion entrada) throws Exception {
        SalidaValidacion salida = new SalidaValidacion();

        Map<String, Object> paramsPlanilla = new HashMap<>();
        paramsPlanilla.put("p_IdEmpresa",        entrada.p_IdEmpresa);
        paramsPlanilla.put("p_IdTipoOperacion",  entrada.p_IdTipoOperacion);
        paramsPlanilla.put("p_IdEmpleado",       entrada.p_IdEmpleado);

        Map<String, Object> paramsProducto = new HashMap<>();
        paramsProducto.put("p_IdEmpresa",        entrada.p_IdEmpresa);
        paramsProducto.put("p_IdPortafolio",     entrada.p_IdPortafolio);
        paramsProducto.put("p_IdTipoProducto",   entrada.p_IdTipoProducto);

        List<Resultado> tienePlantilla   = query(conn, SQL_SELECT_PLANTILLA,  paramsPlanilla,  fieldMapper(Resultado.class));
        List<Resultado> tieneProducto    = query(conn, SQL_SELECT_PORTAFOLIO, paramsProducto,  fieldMapper(Resultado.class));

        String resPlantilla = tienePlantilla.isEmpty()  ? "N" : tienePlantilla.get(0).resultado;
        String resProducto  = tieneProducto.isEmpty()   ? "N" : tieneProducto.get(0).resultado;

        if ("N".equals(resPlantilla)) {
            salida.p_out_ErroresDetalle.add(new DetalleSalida(
                    0L, "RECHAZO",
                    "OPERACION_NO_HABILITADA_PARA_EMPLEADO",
                    "UI_OPERACION_NO_HABILITADA_PARA_EMPLEADO"));
            salida.p_out_ErroresContador++;
        }

        if ("N".equals(resProducto)) {
            salida.p_out_ErroresDetalle.add(new DetalleSalida(
                    0L, "RECHAZO",
                    "PRODUCTO_NO_HABILITADO",
                    "UI_PRODUCTO_NO_HABILITADO"));
            salida.p_out_ErroresContador++;
        }

        return salida;
    }
}
