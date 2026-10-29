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
public class CrearOperacionesTerminoCargaMasivaService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(CrearOperacionesTerminoCargaMasivaService.class);
    private static final String SQL_SEL_CABECERA = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/CrearOperacionesTerminoCargaMasiva_SelectOperacionCabecera.sql");
    private static final String SQL_SEL_DETALLE = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/CrearOperacionesTerminoCargaMasiva_SelectOperacionDetalle.sql");
    private static final String SQL_SEL_CARTERAS = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/CrearOperacionesTerminoCargaMasiva_SelectCarteras.sql");
    private static final String SQL_SEL_INSTRUMENTO = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/CrearOperacionesTerminoCargaMasiva_SelectInstrumentos.sql");
    private static final String SQL_SEL_COND_LIQ = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/CrearOperacionesTerminoCargaMasiva_SelectCondicionLiquidacion.sql");
    private static final String SQL_UPD_CABECERA = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/CrearOperacionesTerminoCargaMasiva_UpdateCabecera.sql");
    private static final String SQL_UPD_DETALLE = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/CrearOperacionesTerminoCargaMasiva_UpdateDetalle.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public CrearOperacionesTerminoCargaMasivaService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource;
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.MRF.CrearOperacionesTerminoCargaMasiva"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);

            JsonObject reqResult = plataforma.callRevisarRequisitosBasicos(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
            if (reqResult.has("P_OUT_ERRORESCONTADOR") && reqResult.get("P_OUT_ERRORESCONTADOR").getAsLong() > 0) {
                return reqResult;
            }

            Map<String, Object> base = new HashMap<>();
            base.put("IdEmpresa", in.p_IdEmpresa);
            base.put("IdFolioAgrupador", in.p_IdFolioAgrupador);

            Salida out = new Salida();
            out.P_OUT_CABECERAS = JavaCallInfra.query(conn, SQL_SEL_CABECERA, base, JavaCallInfra.fieldMapper(Cabecera.class));
            out.P_OUT_DETALLES = JavaCallInfra.query(conn, SQL_SEL_DETALLE, base, JavaCallInfra.fieldMapper(Detalle.class));
            out.P_OUT_CARTERAS = JavaCallInfra.query(conn, SQL_SEL_CARTERAS, base, JavaCallInfra.fieldMapper(Cartera.class));
            out.P_OUT_INSTRUMENTOS = JavaCallInfra.query(conn, SQL_SEL_INSTRUMENTO, base, JavaCallInfra.fieldMapper(Instrumento.class));
            out.P_OUT_CONDICION = JavaCallInfra.query(conn, SQL_SEL_COND_LIQ, base, JavaCallInfra.fieldMapper(CondicionLiquidacion.class));

            var jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));
            var pUpd = new MapSqlParameterSource()
                    .addValue("IdEmpresa", in.p_IdEmpresa)
                    .addValue("IdFolioAgrupador", in.p_IdFolioAgrupador)
                    .addValue("IdUsuario", in.p_IdUsuario);
            jdbc.update(SQL_UPD_CABECERA, pUpd);
            jdbc.update(SQL_UPD_DETALLE, pUpd);

            plataforma.callAgregarLog(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "INFO",
                    "CrearOperacionesTerminoCargaMasiva: folio=" + in.p_IdFolioAgrupador);
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("CrearOperacionesTerminoCargaMasiva failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public Long p_IdFolioAgrupador;
    }
    public static class Cabecera { public Long IdFolioAgrupador; public String IdInstrumento; public String FechaTermino; public String IdEstado; }
    public static class Detalle { public Long IdFolioAgrupador; public BigDecimal Cantidad; public BigDecimal Precio; public String IdEstado; }
    public static class Cartera { public Long IdCartera; public Long IdPortafolio; public BigDecimal Disponible; }
    public static class Instrumento { public String IdInstrumento; public String Descripcion; }
    public static class CondicionLiquidacion { public String IdCondicion; public String Descripcion; }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Cabecera> P_OUT_CABECERAS = new ArrayList<>();
        public List<Detalle> P_OUT_DETALLES = new ArrayList<>();
        public List<Cartera> P_OUT_CARTERAS = new ArrayList<>();
        public List<Instrumento> P_OUT_INSTRUMENTOS = new ArrayList<>();
        public List<CondicionLiquidacion> P_OUT_CONDICION = new ArrayList<>();
    }
}
