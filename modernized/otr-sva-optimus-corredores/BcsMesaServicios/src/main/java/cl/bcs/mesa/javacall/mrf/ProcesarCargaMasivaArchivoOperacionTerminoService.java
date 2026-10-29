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
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProcesarCargaMasivaArchivoOperacionTerminoService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ProcesarCargaMasivaArchivoOperacionTerminoService.class);
    private static final String SQL_SEL_CABECERA = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/ProcesarCargaMasivaArchivoOperacionTermino_SelectOperacionCabecera.sql");
    private static final String SQL_SEL_DETALLE = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/ProcesarCargaMasivaArchivoOperacionTermino_SelectOperacionDetalle.sql");
    private static final String SQL_SEL_CARTERAS = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/ProcesarCargaMasivaArchivoOperacionTermino_SelectCarteras.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public ProcesarCargaMasivaArchivoOperacionTerminoService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource;
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.MRF.ProcesarCargaMasivaArchivoOperacionTermino"; }

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
            p.put("IdFolioAgrupador", in.p_IdFolioAgrupador);

            Salida out = new Salida();
            out.P_OUT_CABECERA = JavaCallInfra.query(conn, SQL_SEL_CABECERA, p, JavaCallInfra.fieldMapper(Cabecera.class));
            out.P_OUT_DETALLE = JavaCallInfra.query(conn, SQL_SEL_DETALLE, p, JavaCallInfra.fieldMapper(Detalle.class));
            out.P_OUT_CARTERAS = JavaCallInfra.query(conn, SQL_SEL_CARTERAS, p, JavaCallInfra.fieldMapper(Cartera.class));

            plataforma.callAgregarLog(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "INFO",
                    "ProcesarCargaMasiva: folio=" + in.p_IdFolioAgrupador);
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("ProcesarCargaMasivaArchivoOperacionTermino failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public Long p_IdFolioAgrupador;
    }
    public static class Cabecera { public Long IdFolioAgrupador; public String IdInstrumento; public String FechaTermino; public String IdEstado; }
    public static class Detalle { public Long IdFolioDetalle; public BigDecimal Cantidad; public BigDecimal Precio; public String IdEstado; }
    public static class Cartera { public Long IdCartera; public Long IdPortafolio; public BigDecimal Disponible; }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Cabecera> P_OUT_CABECERA = new ArrayList<>();
        public List<Detalle> P_OUT_DETALLE = new ArrayList<>();
        public List<Cartera> P_OUT_CARTERAS = new ArrayList<>();
    }
}
