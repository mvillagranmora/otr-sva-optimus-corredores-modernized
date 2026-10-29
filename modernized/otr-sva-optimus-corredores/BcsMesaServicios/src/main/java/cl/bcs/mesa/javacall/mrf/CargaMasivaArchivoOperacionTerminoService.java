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
public class CargaMasivaArchivoOperacionTerminoService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(CargaMasivaArchivoOperacionTerminoService.class);
    private static final String SQL_SEL_MESA = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/CargaMasivaArchivoOperacionTermino_SelectMesa.sql");
    private static final String SQL_SEL_INDICADOR = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/CargaMasivaArchivoOperacionTermino_SelectIndicador.sql");
    private static final String SQL_SEL_INSTRUMENTO = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/CargaMasivaArchivoOperacionTermino_SelectInstrumentos.sql");
    private static final String SQL_SEL_CARTERA = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/CargaMasivaArchivoOperacionTermino_SelectCartera.sql");
    private static final String SQL_SEL_FOLIO = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/CargaMasivaArchivoOperacionTermino_SelectFolioAgrupador.sql");
    private static final String SQL_INS_ARCHIVO = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/CargaMasivaArchivoOperacionTermino_InsertArchivo.sql");
    private static final String SQL_INS_CABECERA = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/CargaMasivaArchivoOperacionTermino_InsertCabecera.sql");
    private static final String SQL_INS_DETALLE = JavaCallInfra.loadSql("cl/bcs/mesa/MRF/sql/CargaMasivaArchivoOperacionTermino_InsertDetalle.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public CargaMasivaArchivoOperacionTerminoService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource;
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.MRF.CargaMasivaArchivoOperacionTermino"; }

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
            Map<String, Object> base = new HashMap<>();
            base.put("IdEmpresa", in.p_IdEmpresa);
            base.put("IdUsuario", in.p_IdUsuario);

            Salida out = new Salida();
            out.P_OUT_MESA = JavaCallInfra.query(conn, SQL_SEL_MESA, base, JavaCallInfra.fieldMapper(Mesa.class));
            out.P_OUT_INDICADOR = JavaCallInfra.query(conn, SQL_SEL_INDICADOR, base, JavaCallInfra.fieldMapper(Indicador.class));
            out.P_OUT_INSTRUMENTO = JavaCallInfra.query(conn, SQL_SEL_INSTRUMENTO, base, JavaCallInfra.fieldMapper(Instrumento.class));

            // Insert file record
            var pArch = new MapSqlParameterSource()
                    .addValue("IdEmpresa", in.p_IdEmpresa)
                    .addValue("IdUsuario", in.p_IdUsuario)
                    .addValue("NombreArchivo", in.p_NombreArchivo);
            jdbc.update(SQL_INS_ARCHIVO, pArch);

            // Get folio agrupador
            List<FolioAgrupador> folios = JavaCallInfra.query(conn, SQL_SEL_FOLIO, base, JavaCallInfra.fieldMapper(FolioAgrupador.class));
            long idFolioAgrupador = folios.isEmpty() ? 0L : (folios.get(0).IdFolioAgrupador != null ? folios.get(0).IdFolioAgrupador : 0L);

            // Insert cabecera + detalles
            if (in.p_Cabeceras != null) {
                for (Cabecera cab : in.p_Cabeceras) {
                    var pCab = new MapSqlParameterSource()
                            .addValue("IdEmpresa", in.p_IdEmpresa)
                            .addValue("IdFolioAgrupador", idFolioAgrupador)
                            .addValue("IdInstrumento", cab.IdInstrumento)
                            .addValue("IdCartera", cab.IdCartera)
                            .addValue("FechaTermino", cab.FechaTermino);
                    jdbc.update(SQL_INS_CABECERA, pCab);

                    if (cab.Detalles != null) {
                        for (Detalle det : cab.Detalles) {
                            var pDet = new MapSqlParameterSource()
                                    .addValue("IdEmpresa", in.p_IdEmpresa)
                                    .addValue("IdFolioAgrupador", idFolioAgrupador)
                                    .addValue("Cantidad", det.Cantidad)
                                    .addValue("Precio", det.Precio);
                            jdbc.update(SQL_INS_DETALLE, pDet);
                        }
                    }
                }
            }

            plataforma.callAgregarLog(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "INFO",
                    "CargaMasivaArchivoOperacionTermino: folio=" + idFolioAgrupador);
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("CargaMasivaArchivoOperacionTermino failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Detalle { public BigDecimal Cantidad; public BigDecimal Precio; }
    public static class Cabecera {
        public String IdInstrumento; public Long IdCartera; public String FechaTermino;
        public List<Detalle> Detalles;
    }
    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public String p_NombreArchivo; public List<Cabecera> p_Cabeceras;
    }
    public static class Mesa { public Long IdMesa; public String Descripcion; }
    public static class Indicador { public String IdIndicador; public String Descripcion; }
    public static class Instrumento { public String IdInstrumento; public String Descripcion; public String IdTipoInstrumento; }
    public static class FolioAgrupador { public Long IdFolioAgrupador; }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Mesa> P_OUT_MESA = new ArrayList<>();
        public List<Indicador> P_OUT_INDICADOR = new ArrayList<>();
        public List<Instrumento> P_OUT_INSTRUMENTO = new ArrayList<>();
    }
}
