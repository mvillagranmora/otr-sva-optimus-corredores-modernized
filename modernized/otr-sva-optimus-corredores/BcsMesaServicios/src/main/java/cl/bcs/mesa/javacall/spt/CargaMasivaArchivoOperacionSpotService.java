package cl.bcs.mesa.javacall.spt;

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
import java.util.List;

@Service
public class CargaMasivaArchivoOperacionSpotService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(CargaMasivaArchivoOperacionSpotService.class);
    private static final String SQL_INS_ARCH = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/CargaMasivaArchivoSpot_Insert.sql");
    private static final String SQL_INS_DET = JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/CargaMasivaArchivoSpotDetalle_Insert.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public CargaMasivaArchivoOperacionSpotService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource;
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.SPT.CargaMasivaArchivoOperacionSpot"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);

            JsonObject reqResult = plataforma.callRevisarRequisitosBasicos(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
            if (reqResult.has("P_OUT_ERRORESCONTADOR") && reqResult.get("P_OUT_ERRORESCONTADOR").getAsLong() > 0) {
                return reqResult;
            }

            var jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));
            var pArch = new MapSqlParameterSource()
                    .addValue("IdEmpresa", in.p_IdEmpresa)
                    .addValue("IdUsuario", in.p_IdUsuario)
                    .addValue("NombreArchivo", in.p_NombreArchivo);
            jdbc.update(SQL_INS_ARCH, pArch);

            if (in.p_Detalles != null) {
                for (Detalle det : in.p_Detalles) {
                    var pDet = new MapSqlParameterSource()
                            .addValue("IdEmpresa", in.p_IdEmpresa)
                            .addValue("IdPortafolio", det.IdPortafolio)
                            .addValue("Monto", det.Monto)
                            .addValue("IdMoneda", det.IdMoneda);
                    jdbc.update(SQL_INS_DET, pDet);
                }
            }
            plataforma.callAgregarLog(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "INFO",
                    "CargaMasivaArchivoSpot archivo=" + in.p_NombreArchivo);
            return GSON.fromJson("{\"P_OUT_ERRORESCONTADOR\":0}", JsonObject.class);
        } catch (Exception e) {
            log.error("CargaMasivaArchivoOperacionSpot failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Detalle { public Long IdPortafolio; public BigDecimal Monto; public String IdMoneda; }
    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public String p_NombreArchivo; public List<Detalle> p_Detalles;
    }
}
